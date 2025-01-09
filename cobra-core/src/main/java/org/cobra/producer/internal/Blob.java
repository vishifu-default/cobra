package org.cobra.producer.internal;

import org.cobra.core.objects.VersioningBlob;
import org.cobra.producer.CobraProducer;

import java.nio.file.Path;

public abstract class Blob extends VersioningBlob implements CobraProducer.PublishableArtifact {

    protected final Path path;

    protected Blob(Path path, long fromVersion, long toVersion) {
        super(fromVersion, toVersion);

        String blobPrefix = blobType().prefix();
        switch (blobType()) {
            case DELTA_BLOB -> this.path = path.resolve(String.format("%s-%d-%d",
                    blobPrefix, fromVersion, toVersion));
            case REVERSED_DELTA_BLOB -> this.path = path.resolve(String.format("%s-%d-%d",
                    blobPrefix, toVersion, fromVersion));
            default -> throw new IllegalStateException("Unknown blob type: " + blobType());
        }
    }

    @Override
    public Path getPath() {
        return this.path;
    }

    @Override
    public String toString() {
        return "Blob(fromVersion=%d, toVersion=%d)".formatted(fromVersion, toVersion);
    }
}
