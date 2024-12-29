package org.cobra.producer.internal;

import org.cobra.core.objects.VersioningBlob;

public class PopulationAtomic {

    private final Atomic current;
    private final Atomic pending;

    private PopulationAtomic(Atomic current, Atomic pending) {
        this.current = current;
        this.pending = pending;
    }

    public static PopulationAtomic createDeltaChain(long initVersion) {
        return new PopulationAtomic(atomic(initVersion), null);
    }

    public static Atomic atomic(long version) {
        return () -> version;
    }

    public Atomic getCurrent() {
        return this.current;
    }

    public Atomic getPending() {
        return this.pending;
    }

    public boolean hasCurrentState() {
        return getCurrent() != null;
    }

    public long pendingVersion() {
        return getPending() == null ? VersioningBlob.VERSION_UNDEFINED : getPending().getVersion();
    }

    public PopulationAtomic round(long pendingVersion) {
        if (getPending() != null)
            throw new IllegalStateException("Attempt to round other pending");

        return new PopulationAtomic(this.current, atomic(pendingVersion));
    }

    public PopulationAtomic commit() {
        if (getPending() == null)
            throw new IllegalStateException("Attempt to commit non-pending");

        return new PopulationAtomic(this.pending, null);
    }

    public PopulationAtomic rollback() {
        if (getPending() == null)
            throw new IllegalStateException("Attempt to rollback non-pending");

        return new PopulationAtomic(this.current, null);
    }

    public PopulationAtomic swap() {
        return new PopulationAtomic(atomic(current.getVersion()), atomic(pending.getVersion()));
    }

    public interface Atomic {
        long getVersion();
    }
}
