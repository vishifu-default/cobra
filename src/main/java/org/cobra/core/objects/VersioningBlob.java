package org.cobra.core.objects;

public abstract class VersioningBlob {

    public static final long VERSION_UNDEFINED = 0L;
    public static final long VERSION_LAST = Long.MAX_VALUE;

    protected final long fromVersion;
    protected final long toVersion;
    protected final BlobType blobType;

    protected VersioningBlob(long fromVersion, long toVersion) {
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;

        if (isDeltaBlob())
            this.blobType = BlobType.DELTA_BLOB;
        else
            this.blobType = BlobType.REVERSED_DELTA_BLOB;

    }

    protected VersioningBlob(long toVersion) {
        this(VERSION_UNDEFINED, toVersion);
    }

    public final boolean isDeltaBlob() {
        return this.toVersion > this.fromVersion;
    }

    public final boolean isReversedDeltaBlob() {
        return !isDeltaBlob();
    }

    public final BlobType blobType() {
        return this.blobType;
    }

    public final long fromVersion() {
        return this.fromVersion;
    }

    public final long toVersion() {
        return this.toVersion;
    }

    @Override
    public String toString() {
        return "VersioningBlob(" +
                "fromVersion=" + fromVersion +
                ", toVersion=" + toVersion +
                ", blobType=" + blobType +
                ')';
    }
}
