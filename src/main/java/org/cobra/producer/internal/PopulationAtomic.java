package org.cobra.producer.internal;

import org.cobra.core.objects.VersioningBlob;

public class PopulationAtomic {

    private final Atomic current;
    private final Atomic pending;

    private PopulationAtomic(Atomic current, Atomic pending) {
        this.current = current;
        this.pending = pending;
    }

    static PopulationAtomic createDeltaChain(long initVersion) {
        return new PopulationAtomic(atomic(initVersion), null);
    }

    static Atomic atomic(long version) {
        return () -> version;
    }

    Atomic getCurrent() {
        return this.current;
    }

    Atomic getPending() {
        return this.pending;
    }

    boolean hasCurrentState() {
        return getCurrent() != null;
    }

    long pendingVersion() {
        return getPending() == null ? VersioningBlob.VERSION_UNDEFINED : getPending().getVersion();
    }

    PopulationAtomic round(long pendingVersion) {
        if (getPending() != null)
            throw new IllegalStateException("Attempt to round other pending");

        return new PopulationAtomic(this.current, atomic(pendingVersion));
    }

    PopulationAtomic commit() {
        if (getPending() == null)
            throw new IllegalStateException("Attempt to commit non-pending");

        return new PopulationAtomic(this.pending, null);
    }

    PopulationAtomic rollback() {
        if (getPending() == null)
            throw new IllegalStateException("Attempt to rollback non-pending");

        return new PopulationAtomic(this.current, null);
    }

    interface Atomic {
        long getVersion();
    }
}
