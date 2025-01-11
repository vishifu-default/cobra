package org.cobra.consumer.internal;

import org.cobra.commons.CobraConstants;
import org.cobra.consumer.CobraConsumer;

public class TransitionUpdater {

    private final BlobRetrieverFacade retrieverFacade;

    public TransitionUpdater(BlobRetrieverFacade blobRetrieverFacade) {
        this.retrieverFacade = blobRetrieverFacade;
    }

    public TransitionPlan plan(long fromVersion, long toVersion) {
        if (fromVersion == toVersion)
            return new TransitionPlan();

        return goDeltaPlan(fromVersion, toVersion);
    }

    private TransitionPlan goDeltaPlan(long fromVersion, long toVersion) {
        TransitionPlan plan = new TransitionPlan();

        if (fromVersion < toVersion) {
            applyDeltaPlan(fromVersion, toVersion, plan);
        } else {
            applyReversedDeltaPlan(fromVersion, toVersion, plan);
        }

        return plan;
    }

    private long applyDeltaPlan(long currentVersion, long toVersion, TransitionPlan plan) {
        while (currentVersion < toVersion) {
            currentVersion = includeDelta(plan, currentVersion + 1);
        }

        return currentVersion;
    }

    private long applyReversedDeltaPlan(long currentVersion, long toVersion, TransitionPlan plan) {
        long pending = currentVersion;

        while (currentVersion > toVersion) {
            currentVersion = includeReversedDelta(plan, currentVersion - 1);
            if (currentVersion != CobraConstants.VERSION_NULL)
                pending = currentVersion;

        }

        return pending;
    }

    private long includeDelta(TransitionPlan plan, long toVersion) {
        CobraConsumer.HeaderBlob headerBlob = retrieverFacade.getHeaderBlob(toVersion);
        CobraConsumer.Blob deltaBlob = retrieverFacade.getDeltaBlob(toVersion);

        VersionTransition transition = new VersionTransition(toVersion, headerBlob, deltaBlob);

        if (deltaBlob == null)
            return CobraConstants.VERSION_LAST;

        if (deltaBlob.toVersion() <= toVersion) {
            plan.add(transition);
        }

        return deltaBlob.toVersion();
    }

    private long includeReversedDelta(TransitionPlan plan, long toVersion) {
        CobraConsumer.HeaderBlob headerBlob = retrieverFacade.getHeaderBlob(toVersion);
        CobraConsumer.Blob reversedDeltaBlob = retrieverFacade.getReverseDeltaBlob(toVersion);
        VersionTransition transition = new VersionTransition(toVersion, headerBlob, reversedDeltaBlob);

        if (reversedDeltaBlob == null)
            return CobraConstants.VERSION_NULL;

        plan.add(transition);
        return reversedDeltaBlob.toVersion();
    }
}
