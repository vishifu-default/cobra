package org.cobra.producer.internal;

import org.cobra.commons.utils.Utils;
import org.cobra.producer.CobraProducer;

import java.nio.file.Path;

public abstract class HeaderBlob implements CobraProducer.PublishableArtifact {

    protected final long version;
    protected final Path path;

    protected HeaderBlob(long version, Path path) {
        this.version = version;
        this.path = path.resolve(String.format("header-%d.%s", version, Utils.randInt()));
    }

    public long getVersion() {
        return this.version;
    }

    @Override
    public String toString() {
        return "HeaderBlob(version=%d)".formatted(version);
    }
}
