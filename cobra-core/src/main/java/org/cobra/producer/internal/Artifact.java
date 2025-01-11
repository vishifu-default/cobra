package org.cobra.producer.internal;

public final class Artifact {

    private HeaderBlob headerBlob = null;
    private Blob deltaBlob = null;
    private Blob reversedDeltaBlob = null;

    private boolean isClosed = false;

    public boolean hasDelta() {
        return this.deltaBlob != null;
    }

    public boolean hasReversedDelta() {
        return this.reversedDeltaBlob != null;
    }

    public HeaderBlob getHeaderBlob() {
        return this.headerBlob;
    }

    public void setHeaderBlob(HeaderBlob headerBlob) {
        this.headerBlob = headerBlob;
    }

    public Blob getDeltaBlob() {
        return this.deltaBlob;
    }

    public void setDeltaBlob(Blob deltaBlob) {
        this.deltaBlob = deltaBlob;
    }

    public Blob getReversedDeltaBlob() {
        return this.reversedDeltaBlob;
    }

    public void setReversedDeltaBlob(Blob reversedDeltaBlob) {
        this.reversedDeltaBlob = reversedDeltaBlob;
    }

    public synchronized void close() {
        if (this.isClosed)
            return;

        this.isClosed = true;

        if (hasDelta()) {
            this.deltaBlob.cleanup();
            this.deltaBlob = null;
        }

        if (hasReversedDelta()) {
            this.reversedDeltaBlob.cleanup();
            this.reversedDeltaBlob = null;
        }
    }

}
