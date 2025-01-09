package org.cobra.producer.internal;

import org.cobra.core.objects.VersioningBlob;

public class PopulationState {

    private final Atomic current;
    private final Atomic pending;

    private PopulationState(Atomic current, Atomic pending) {
        this.current = current;
        this.pending = pending;
    }

    public static PopulationState createDeltaChain(long initVersion) {
        return new PopulationState(atomic(initVersion), null);
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

    public PopulationState round(long pendingVersion) {
        if (getPending() != null)
            throw new IllegalStateException("Attempt to round other pending");

        return new PopulationState(this.current, atomic(pendingVersion));
    }

    public PopulationState commit() {
        if (getPending() == null)
            throw new IllegalStateException("Attempt to commit non-pending");

        return new PopulationState(this.pending, null);
    }

    public PopulationState rollback() {
        if (getPending() == null)
            throw new IllegalStateException("Attempt to rollback non-pending");

        return new PopulationState(this.current, null);
    }

    public PopulationState swap() {
        return new PopulationState(atomic(current.getVersion()), atomic(pending.getVersion()));
    }

    public interface Atomic {
        long getVersion();
    }
}
