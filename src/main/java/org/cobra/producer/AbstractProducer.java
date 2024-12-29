package org.cobra.producer;

import org.cobra.commons.Clock;
import org.cobra.commons.CobraConstants;
import org.cobra.commons.errors.CobraException;
import org.cobra.core.ModelSchema;
import org.cobra.producer.internal.Artifact;
import org.cobra.producer.internal.Blob;
import org.cobra.producer.internal.HeaderBlob;
import org.cobra.producer.internal.PopulationAtomic;
import org.cobra.producer.internal.ScopedProducerStateWriter;
import org.cobra.producer.state.BlobWriter;
import org.cobra.producer.state.BlobWriterImpl;
import org.cobra.producer.state.ProducerStateContext;
import org.cobra.producer.state.StateWriteEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractProducer implements CobraProducer {

    private static final Logger log = LoggerFactory.getLogger(AbstractProducer.class);

    private final CobraProducer.VersionMinter versionMinter;
    private final CobraProducer.BlobStagger blobStagger;
    private final CobraProducer.BlobPublisher blobPublisher;
    private final StateWriteEngine stateWriteEngine;
    private final ProducerStateContext producerStateContext;

    private final Clock clock;

    private PopulationAtomic populationAtomic;
    private long lastSuccessVersion = CobraConstants.VERSION_NULL;


    protected AbstractProducer(Builder builder) {
        this.versionMinter = builder.versionMinter;
        this.blobStagger = builder.blobStagger;
        this.blobPublisher = builder.blobPublisher;
        this.clock = builder.clock;

        this.producerStateContext = new ProducerStateContext();
        this.stateWriteEngine = new StateWriteEngine(this.producerStateContext);

        this.populationAtomic = PopulationAtomic.createDeltaChain(CobraConstants.VERSION_NULL);
    }

    @Override
    public void registerModel(Class<?> clazz) {
        final ModelSchema modelSchema = new ModelSchema(clazz);
        this.producerStateContext.registerModel(modelSchema);
    }

    protected long runProduce(Populator task) {
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
                PopulationAtomic candidates = populationAtomic.round(toVersion);
                candidates = doCheckout(candidates, artifact);

                announce(candidates);
                populationAtomic = candidates.commit();

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
            log.debug("producer a version: version: {}; elapsed: {} ms", toVersion, endMillis - startMillis);
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

    void announce(PopulationAtomic atomic) {
        log.debug("implement Announcer");
        // todo: implement Announcer
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

    private PopulationAtomic doCheckout(PopulationAtomic atomic, Artifact artifact){
        PopulationAtomic result = atomic;

        if (result.hasCurrentState()) {
            if (artifact.hasDelta()) {
                result = atomic.swap();
            }
        }

        return result;
    }
}