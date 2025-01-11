package org.cobra.consumer.internal;

import org.cobra.consumer.CobraConsumer;

import java.util.HashSet;
import java.util.Set;

public class TransitionStats {
    private final Set<DeltaTransition> failDelta = new HashSet<>();
    private final Set<DeltaTransition> successDelta = new HashSet<>();

    public void markFailPlan(TransitionPlan plan) {
        for (VersionTransition transition : plan.getTransitions())
            markFailTransition(transition.getDeltaBlob());
    }

    public void markFailTransition(CobraConsumer.Blob transition) {
        this.failDelta.add(deltaTransition(transition));
    }

    public void markSuccessPlan(TransitionPlan plan) {
        for (VersionTransition transition : plan.getTransitions())
            markSuccessTransition(transition.getDeltaBlob());
    }

    public void markSuccessTransition(CobraConsumer.Blob transition) {
        this.successDelta.add(deltaTransition(transition));
    }

    public long getNumOfFailTransition() {
        return this.failDelta.size();
    }

    public long getNumOfSuccessTransition() {
        return this.successDelta.size();
    }

    public void clear() {
        this.failDelta.clear();
        this.successDelta.clear();
    }

    public String dumpStats() {
        StringBuilder sb = new StringBuilder()
                .append("FAIL DELTA TRANSITION: ").append(String.format("%d time(s)", getNumOfFailTransition()))
                .append("SUCCESS DELTA TRANSITION: ").append(String.format("%d times(s)", getNumOfSuccessTransition()));

        return sb.toString();
    }

    private DeltaTransition deltaTransition(CobraConsumer.Blob transition) {
        return new DeltaTransition(transition.fromVersion(), transition.toVersion());
    }

    private static class DeltaTransition {
        long fromVersion;
        long toVersion;

        public DeltaTransition(long fromVersion, long toVersion) {
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
        }

        @Override
        public int hashCode() {
            return (int) fromVersion
                    ^ (int) (fromVersion >> 32)
                    ^ (int) toVersion
                    ^ (int) (toVersion >> 32);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DeltaTransition other) {
                return other.fromVersion == fromVersion && other.toVersion == toVersion;
            }
            return false;
        }
    }

}
