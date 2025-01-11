package org.cobra.consumer.internal;

import org.cobra.commons.CobraConstants;
import org.cobra.consumer.CobraConsumer;
import org.cobra.consumer.read.BlobReaderImpl;
import org.cobra.consumer.read.StateReadEngine;
import org.cobra.core.memory.MemoryMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ConsumerDataPlane {

    private static final Logger log = LoggerFactory.getLogger(ConsumerDataPlane.class);

    private volatile DataUpdater dataUpdater;
    private final TransitionUpdater transitionUpdater;
    private final MemoryMode memoryMode;
    private final StateReadEngine stateReadEngine;

    public ConsumerDataPlane(TransitionUpdater transitionUpdater, MemoryMode memoryMode, StateReadEngine stateReadEngine) {
        this.transitionUpdater = transitionUpdater;
        this.memoryMode = memoryMode;
        this.stateReadEngine = stateReadEngine;

        this.dataUpdater = new DataUpdater(new TransitionStats(), memoryMode,
                new BlobReaderImpl(memoryMode, stateReadEngine));
    }

    public long currentVersion() {
        return dataUpdater.currentVersion();
    }

    public synchronized boolean update(CobraConsumer.VersionInformation versionInformation) throws IOException {
        final long requestVersion = versionInformation.getVersion();
        if (requestVersion == currentVersion()) {
            log.debug("no version to update");
            if (requestVersion == CobraConstants.VERSION_NULL && dataUpdater == null) {
                dataUpdater = new DataUpdater(new TransitionStats(), memoryMode,
                        new BlobReaderImpl(memoryMode, stateReadEngine));
            }

            return true;
        }

        final TransitionPlan updatePlan = transitionUpdater.plan(currentVersion(), requestVersion);
        if (updatePlan.number() == 0 && requestVersion == CobraConstants.VERSION_LAST) {
            throw new IllegalStateException("Could not create an update-plan, no existed version for request %d".formatted(requestVersion));
        }

        if (updatePlan.getFinalDestinationVersion() == currentVersion() || updatePlan.getFinalDestinationVersion() == CobraConstants.VERSION_NULL)
            return true;

        dataUpdater.update(updatePlan);
        return currentVersion() == requestVersion;
    }
}
