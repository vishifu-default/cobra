package org.cobra.producer.internal;

import lombok.Getter;

public class AtomicState {

    @Getter
    private final Atomic current;

    @Getter
    private final Atomic pending;

    private AtomicState(Atomic current, Atomic pending) {
        this.current = current;
        this.pending = pending;
    }

    public static AtomicState initChain(long initVersion) {
        return new AtomicState(atomic(initVersion), null);
    }

    private static Atomic atomic(long version) {
        return () -> version;
    }

    public AtomicState stage(long pending) {
        if (getPending() != null)
            throw new IllegalStateException("Attempt to stage other atomic-state");

        return new AtomicState(this.current, atomic(pending));
    }

    public AtomicState swap() {
        return new AtomicState(atomic(pending.getVersion()), atomic(current.getVersion()));
    }

    /* commit perform after swap */
    public AtomicState commit() {
        return new AtomicState(atomic(current.getVersion()), null);
    }

    public AtomicState rollback() {
        if (getPending() == null)
            throw new IllegalStateException("Attempt to rollback empty state");
        return new AtomicState(atomic(pending.getVersion()), null);
    }

    public boolean hasCurrentState() {
        return getCurrent() != null;
    }

    public interface Atomic {
        long getVersion();
    }
}
