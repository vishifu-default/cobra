package org.cobra.producer.state;

public class StateWriteEngine {

    private enum Phasing {
        WRITING_PHASE,
        NEXT_CYCLE;
    }

    public void moveToWritePhase() {
        throw new UnsupportedOperationException("implement me");
    }

    public void moveToNextCycle() {
        throw new UnsupportedOperationException("implement me");
    }

    public boolean isWriting() {
        throw new UnsupportedOperationException("implement me");
    }

    public long getOriginRandomizedTag() {
        throw new UnsupportedOperationException("implement me");
    }

    public long getDestinationRandomizedTag() {
        throw new UnsupportedOperationException("implement me");
    }

    private long mintRandomizedTag() {
        throw new UnsupportedOperationException("implement me");
    }
}
