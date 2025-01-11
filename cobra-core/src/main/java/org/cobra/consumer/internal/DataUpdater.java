package org.cobra.consumer.internal;

import org.cobra.commons.CobraConstants;
import org.cobra.commons.utils.Elapsed;
import org.cobra.consumer.CobraConsumer;
import org.cobra.consumer.read.BlobReader;
import org.cobra.core.memory.MemoryMode;
import org.cobra.core.objects.BlobInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DataUpdater {

    private static final Logger log = LoggerFactory.getLogger(DataUpdater.class);

    private final TransitionStats transitionStats;
    private final MemoryMode memoryMode;
    private final BlobReader blobReader;

    private long currentVersion = CobraConstants.VERSION_NULL;

    public DataUpdater(TransitionStats transitionStats, MemoryMode memoryMode, BlobReader blobReader) {
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

    public void update(TransitionPlan plan) throws IOException {
        applyDeltaPlan(plan);
    }

    private void applyDeltaPlan(TransitionPlan plan) throws IOException {
        for (VersionTransition transition : plan.getTransitions()) {
            applyTransition(transition);
        }
    }

    private void applyTransition(VersionTransition transition) throws IOException {
        log.debug("Applying delta-transition {}", transition);

        final long start = System.nanoTime();
        applyHeader(transition.getHeader());
        applyStateEngineTransition(transition.getDeltaBlob());

        final long elapsed = System.nanoTime() - start;
        log.debug("Applied delta-transition version {}; took: {}", transition.getVersion(), Elapsed.toStr(elapsed));
    }

    private void applyHeader(CobraConsumer.HeaderBlob headerBlob) throws IOException {
        blobReader.applyHeader(BlobInput.randomAccessFile(headerBlob.file()));
    }

    private void applyStateEngineTransition(CobraConsumer.Blob deltaBlob) throws IOException {

        try (BlobInput input = BlobInput.randomAccessFile(deltaBlob.file())) {
            this.blobReader.applyDelta(input);
        } catch (Throwable cause) {
            this.transitionStats.markFailTransition(deltaBlob);
            throw cause;
        }


        currentVersion(deltaBlob.toVersion());

        logDeltaDone(deltaBlob);
    }

    private void logDeltaDone(CobraConsumer.Blob blob) {
        String prefix = blob.isDeltaBlob() ? "[DELTA-TRANSITION]" : "[REVERSED-DELTA-TRANSITION]";
        log.info("{} complete; from_v: {}; to_v: {}", prefix, blob.fromVersion(), blob.toVersion());
    }
}
