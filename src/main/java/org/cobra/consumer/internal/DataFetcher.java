package org.cobra.consumer.internal;

import org.cobra.commons.CobraConstants;
import org.cobra.consumer.CobraConsumer;

public class DataFetcher {

    private final CobraConsumer.BlobFetcher blobFetcher;

    public DataFetcher(CobraConsumer.BlobFetcher blobFetcher) {
        this.blobFetcher = blobFetcher;
    }

    public DataUpdatePlan plan(long fromVersion, long toVersion) {
        if (fromVersion == toVersion)
            return new DataUpdatePlan();

        return goDeltaPlan(fromVersion, toVersion);
    }

    private DataUpdatePlan goDeltaPlan(long fromVersion, long toVersion) {
        DataUpdatePlan plan = new DataUpdatePlan();

        if (fromVersion < toVersion) {
            applyDeltaPlan(fromVersion, toVersion, plan);
        } else {
            applyReversedDeltaPlan(fromVersion, toVersion, plan);
        }

        return plan;
    }

    private long applyDeltaPlan(long currentVersion, long toVersion, DataUpdatePlan plan) {
        while (currentVersion < toVersion) {
            currentVersion = includeDelta(plan, currentVersion + 1);
        }

        return currentVersion;
    }

    private long applyReversedDeltaPlan(long currentVersion, long toVersion, DataUpdatePlan plan) {
        long pending = currentVersion;

        while (currentVersion > toVersion) {
            currentVersion = includeReversedDelta(plan, currentVersion);
            if (currentVersion != CobraConstants.VERSION_NULL)
                pending = currentVersion;

        }

        return pending;
    }

    private long includeDelta(DataUpdatePlan plan, long toVersion) {
        CobraConsumer.HeaderBlob headerBlob = blobFetcher.fetchHeaderBlob(toVersion);
        CobraConsumer.Blob deltaBlob = blobFetcher.fetchDeltaBlob(toVersion);

        VersionTransition transition = new VersionTransition(toVersion, headerBlob, deltaBlob);

        if (deltaBlob == null)
            return CobraConstants.VERSION_LAST;

        if (deltaBlob.toVersion() <= toVersion) {
            plan.add(transition);
        }

        return deltaBlob.toVersion();
    }

    private long includeReversedDelta(DataUpdatePlan plan, long currentVersion) {
        CobraConsumer.HeaderBlob headerBlob = blobFetcher.fetchHeaderBlob(currentVersion);
        CobraConsumer.Blob reversedDeltaBlob = blobFetcher.fetchReversedDeltaBlob(currentVersion);

        VersionTransition transition = new VersionTransition(currentVersion, headerBlob, reversedDeltaBlob);

        if (reversedDeltaBlob == null)
            return CobraConstants.VERSION_NULL;

        plan.add(transition);
        return reversedDeltaBlob.toVersion();
    }
}
