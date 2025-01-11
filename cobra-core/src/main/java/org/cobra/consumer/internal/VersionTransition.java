package org.cobra.consumer.internal;

import org.cobra.consumer.CobraConsumer;

public class VersionTransition {

    private final long version;
    private final CobraConsumer.HeaderBlob header;
    private final CobraConsumer.Blob deltaBlob;

    public VersionTransition(long version, CobraConsumer.HeaderBlob header, CobraConsumer.Blob deltaBlob) {
        this.version = version;
        this.header = header;
        this.deltaBlob = deltaBlob;
    }

    public long getVersion() {
        return version;
    }

    public CobraConsumer.HeaderBlob getHeader() {
        return header;
    }

    public CobraConsumer.Blob getDeltaBlob() {
        return deltaBlob;
    }

    @Override
    public String toString() {
        return "VersionTransition(" +
                "version=" + version +
                ", header=" + header +
                ", deltaBlob=" + deltaBlob +
                ')';
    }
}
