package org.cobra.consumer.internal;

import org.cobra.commons.CobraConstants;
import org.cobra.consumer.CobraConsumer;
import org.cobra.consumer.read.BlobReader;
import org.cobra.core.memory.MemoryMode;
import org.cobra.core.objects.BlobInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DataBlobTransits {

    private static final Logger log = LoggerFactory.getLogger(DataBlobTransits.class);

    private final TransitionStats transitionStats;
    private final MemoryMode memoryMode;
    private final BlobReader blobReader;

    private long currentVersion = CobraConstants.VERSION_NULL;

    public DataBlobTransits(TransitionStats transitionStats, MemoryMode memoryMode, BlobReader blobReader) {
        this.transitionStats = transitionStats;
        this.memoryMode = memoryMode;
        this.blobReader = blobReader;
    }

    public long currentVersion() {
        return this.currentVersion;
    }

    public void currentVersion(long version) {
        this.currentVersion = version;
    }

    public void update(DataUpdatePlan plan) throws IOException {
        applyDeltaPlan(plan);
    }

    private void applyDeltaPlan(DataUpdatePlan plan) throws IOException {
        for (VersionTransition transition : plan.getTransitions()) {
            applyTransition(transition);
        }
    }

    private void applyTransition(VersionTransition transition) throws IOException {
        if (!this.memoryMode.equals(MemoryMode.ON_HEAP)) {
            log.warn("Skipping, apply delta-transition must be in ON_HEAP memory mode");
            return;
        }

        applyHeader(transition.getHeader());
        applyStateEngineTransition(transition.getDeltaBlob());
    }

    private void applyHeader(CobraConsumer.HeaderBlob headerBlob) throws IOException {
        this.blobReader.applyHeader(headerBlob.input());
    }

    private void applyStateEngineTransition(CobraConsumer.Blob deltaBlob) throws IOException {

        try (BlobInput input = BlobInput.serial(deltaBlob.input())) {
            this.blobReader.applyDelta(input);
        } catch (Throwable cause) {
            this.transitionStats.markFailTransition(deltaBlob);
            throw cause;
        }


        currentVersion(deltaBlob.toVersion());

        log.info(String.format("[DELTA-TRANSITION] transition complete; from_v = %d; to_v = %d",
                deltaBlob.fromVersion(), deltaBlob.toVersion()));
    }

}
