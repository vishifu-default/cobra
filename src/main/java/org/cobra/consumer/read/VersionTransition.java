package org.cobra.consumer.read;

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
}
