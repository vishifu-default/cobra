package org.cobra.producer;

import org.cobra.commons.Clock;
import org.cobra.commons.CobraConstants;
import org.cobra.commons.errors.CobraException;
import org.cobra.core.ModelSchema;
import org.cobra.networks.CobraServer;
import org.cobra.producer.handler.FetchBlobHandler;
import org.cobra.producer.handler.FetchHeaderBlobHandler;
import org.cobra.producer.handler.FetchVersionHandler;
import org.cobra.producer.internal.Artifact;
import org.cobra.producer.internal.Blob;
import org.cobra.producer.internal.HeaderBlob;
import org.cobra.producer.internal.PopulationState;
import org.cobra.producer.internal.ScopedProducerStateWriter;
import org.cobra.producer.state.BlobWriter;
import org.cobra.producer.state.BlobWriterImpl;
import org.cobra.producer.state.ProducerStateContext;
import org.cobra.producer.state.StateWriteEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public abstract class AbstractProducer implements CobraProducer {

    private static final Logger log = LoggerFactory.getLogger(AbstractProducer.class);

    private final CobraProducer.VersionMinter versionMinter;
    private final CobraProducer.BlobStagger blobStagger;
    private final CobraProducer.BlobPublisher blobPublisher;
    private final StateWriteEngine stateWriteEngine;
    private final ProducerStateContext producerStateContext;
    private final Announcer announcer;
    private final Clock clock;

    private PopulationState populationState;
    private long lastSuccessVersion = CobraConstants.VERSION_NULL;

    private final CobraServer network;
    private boolean isBootstrap = false;

    protected AbstractProducer(Builder builder) {
        this.versionMinter = builder.versionMinter;
        this.blobStagger = builder.blobStagger;
        this.blobPublisher = builder.blobPublisher;
        this.clock = builder.clock;
        this.announcer = builder.announcer;

        this.producerStateContext = new ProducerStateContext();
        this.stateWriteEngine = new StateWriteEngine(this.producerStateContext);

        this.populationState = PopulationState.createDeltaChain(CobraConstants.VERSION_NULL);

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

    protected long runProduce(Populator task) {
        if (!isBootstrap)
            throw new CobraException("producer must be bootstrap before produce a cycle");

        long toVersion = versionMinter.mint();
        Artifact artifact = new Artifact();

        long startMillis = clock.milliseconds();

        try {
            /* 1. state to next cycle */
            stateWriteEngine.moveToNextCycle();

            /* 2. population */
            populateTask(task, toVersion);

            /* 3. produce state */
            if (stateWriteEngine.isModified()) {
                publish(artifact, toVersion);
                PopulationState candidates = populationState.round(toVersion);
                candidates = doCheckout(candidates, artifact);

                announce(candidates);
                populationState = candidates.commit();

                lastSuccessVersion = toVersion;
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
            log.info("producer a version: version: {}; elapsed: {} ms", toVersion, endMillis - startMillis);
        }
        return lastSuccessVersion;
    }

    void populateTask(Populator task, long toVersion) {
        try (
                final ScopedProducerStateWriter scoped = new ScopedProducerStateWriter(
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

        if (!this.populationState.hasCurrentState()) {
            log.debug("end if state not start");
            return;
        }

        artifact.setDeltaBlob(doStage(blobStagger.stageDelta(
                populationState.getCurrent().getVersion(),
                toVersion)));
        artifact.setReversedDeltaBlob(doStage(blobStagger.stageReverseDelta(
                toVersion,
                populationState.getCurrent().getVersion()
        )));

        doPublishBlob(artifact.getDeltaBlob());
        doPublishBlob(artifact.getReversedDeltaBlob());
    }

    void announce(PopulationState atomic) {
        announcer.announce(atomic.getPending().getVersion());
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

    private PopulationState doCheckout(PopulationState atomic, Artifact artifact) {
        PopulationState result = atomic;

        if (result.hasCurrentState()) {
            if (artifact.hasDelta()) {
                result = atomic.swap();
            }
        }

        return result;
    }
}
