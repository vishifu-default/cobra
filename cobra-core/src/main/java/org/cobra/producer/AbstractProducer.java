package org.cobra.producer;

import org.cobra.commons.Clock;
import org.cobra.commons.CobraConstants;
import org.cobra.commons.errors.CobraException;
import org.cobra.core.ModelSchema;
import org.cobra.core.hashing.Table;
import org.cobra.networks.CobraServer;
import org.cobra.producer.handler.FetchBlobHandler;
import org.cobra.producer.handler.FetchHeaderBlobHandler;
import org.cobra.producer.handler.FetchVersionHandler;
import org.cobra.producer.internal.Artifact;
import org.cobra.producer.internal.AtomicState;
import org.cobra.producer.internal.Blob;
import org.cobra.producer.internal.HeaderBlob;
import org.cobra.producer.internal.StateWriteProvider;
import org.cobra.producer.state.BlobWriter;
import org.cobra.producer.state.BlobWriterImpl;
import org.cobra.producer.state.ProducerStateContext;
import org.cobra.producer.state.StateWriteEngine;
import org.cobra.producer.state.VersionStateImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractProducer implements CobraProducer {

    private static final Logger log = LoggerFactory.getLogger(AbstractProducer.class);

    protected final CobraProducer.VersionState versionState;
    protected final CobraProducer.BlobStagger blobStagger;
    protected final CobraProducer.BlobPublisher blobPublisher;
    protected final StateWriteEngine stateWriteEngine;
    protected final ProducerStateContext producerStateContext;
    protected final Announcer announcer;
    protected final Clock clock;

    protected AtomicState populationAtomic;

    protected final CobraServer network;
    protected boolean isBootstrap = false;

    protected final ReentrantLock lock = new ReentrantLock();

    protected AbstractProducer(Builder builder) {
        this.blobStagger = builder.blobStagger;
        this.blobPublisher = builder.blobPublisher;
        this.clock = builder.clock;
        this.announcer = builder.announcer;

        this.versionState = new VersionStateImpl();
        this.producerStateContext = new ProducerStateContext();
        this.stateWriteEngine = new StateWriteEngine(this.producerStateContext);

        this.populationAtomic = AtomicState.initChain(CobraConstants.VERSION_NULL);

        network = new CobraServer(new InetSocketAddress(builder.localPort));
        network.registerHandler(new FetchVersionHandler(announcer),
                new FetchHeaderBlobHandler(builder.blobStorePath),
                new FetchBlobHandler(builder.blobStorePath));
    }

    @Override
    public void bootstrapServer() {
        network.bootstrap();
        isBootstrap = true;
    }

    @Override
    public void registerModel(Class<?> clazz) {
        final ModelSchema modelSchema = new ModelSchema(clazz);
        this.producerStateContext.registerModel(modelSchema);
    }

    @Override
    public long currentVersion() {
        return versionState.current();
    }

    @Override
    public Table lookupTable() {
        return producerStateContext.getLocalData().lookupTable();
    }

    protected long runProduce(Populator task) {
        if (!isBootstrap)
            throw new CobraException("producer must be bootstrap before produce a cycle");

        long toVersion = this.versionState.mint();
        Artifact artifact = new Artifact();

        long startMillis = clock.milliseconds();

        try {
            this.lock.lock();

            /* 1. state to next cycle */
            stateWriteEngine.moveToNextCycle();

            /* 2. population */
            populateTask(task, toVersion);

            /* 3. produce state */
            if (stateWriteEngine.isModified()) {
                publish(artifact, toVersion);

                AtomicState candidate = this.populationAtomic.stage(toVersion);
                candidate = doCheckout(candidate, artifact);

                announce(candidate);
                populationAtomic = candidate.commit();

                versionState.pin(toVersion);
            } else {
                log.debug("state not modified, revert to last state");
                stateWriteEngine.revertToLastState();
            }
        } catch (IOException e) {
            try {
                stateWriteEngine.revertToLastState();
            } catch (Throwable cause) {
                log.error("error when revert to last state", e);
                // swallow inner throwable
            }

            throw new CobraException(e);
        } finally {
            long endMillis = clock.milliseconds();
            this.lock.unlock();
            log.info("producer a version: version: {}; elapsed: {} ms", toVersion, endMillis - startMillis);
        }

        return latestVersion();
    }

    protected boolean moveToVersion(long version) {
        if (version > latestVersion()) {
            log.info("Cannot move version {} to version {}", version, latestVersion());
            return false;
        }

        lock.lock();
        try {
            AtomicState candidate = this.populationAtomic.stage(version);

            candidate = candidate.swap();

            announce(candidate);
            populationAtomic = candidate.commit();
            versionState.pin(populationAtomic.getCurrent().getVersion());

            return true;
        } catch (Throwable cause) {
            log.error("error when move version {} to version {}", version, latestVersion(), cause);
            return false;
        } finally {
            lock.unlock();
        }
    }

    void populateTask(Populator task, long toVersion) {
        try (
                final StateWriteProvider scoped = new StateWriteProvider(
                        this.producerStateContext,
                        toVersion,
                        this.clock
                )
        ) {
            task.populate(scoped);
        } catch (Exception e) {
            log.error("error while populating task", e);
            throw new CobraException(e);
        }
    }

    void publish(Artifact artifact, long toVersion) throws IOException {
        artifact.setHeaderBlob(blobStagger.stageHeader(toVersion));
        doStageAndPublishHeaderBlob(artifact.getHeaderBlob());

        if (!this.populationAtomic.hasCurrentState()) {
            log.debug("end if state not start");
            return;
        }

        artifact.setDeltaBlob(doStage(blobStagger.stageDelta(
                populationAtomic.getCurrent().getVersion(),
                toVersion)));
        artifact.setReversedDeltaBlob(doStage(blobStagger.stageReverseDelta(
                toVersion,
                populationAtomic.getCurrent().getVersion()
        )));

        doPublishBlob(artifact.getDeltaBlob());
        doPublishBlob(artifact.getReversedDeltaBlob());
    }

    void announce(AtomicState atomic) {
        announcer.announce(atomic.getCurrent().getVersion());
    }

    private void doStageAndPublishHeaderBlob(HeaderBlob headerBlob) throws IOException {
        BlobWriter blobWriter = new BlobWriterImpl(stateWriteEngine);
        headerBlob.write(blobWriter);

        blobPublisher.publish(headerBlob);
    }

    private Blob doStage(Blob blob) throws IOException {
        BlobWriter blobWriter = new BlobWriterImpl(stateWriteEngine);
        blob.write(blobWriter);

        return blob;
    }

    private void doPublishBlob(Blob blob) {
        blobPublisher.publish(blob);
    }

    private AtomicState doCheckout(AtomicState atomic, Artifact artifact) {
        AtomicState result = atomic;

        if (result.hasCurrentState()) {
            if (artifact.hasDelta()) {
                result = atomic.swap();
            }
        }

        return result;
    }

    long latestVersion() {
        return versionState.current();
    }
}
