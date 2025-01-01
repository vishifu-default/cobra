package org.cobra.producer;

import org.cobra.commons.Clock;
import org.cobra.core.objects.StreamingBlob;
import org.cobra.networks.NetworkConfig;
import org.cobra.producer.internal.Blob;
import org.cobra.producer.internal.HeaderBlob;
import org.cobra.producer.internal.SequencedVersionMinter;
import org.cobra.producer.state.BlobWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public interface CobraProducer {

    void bootstrapServer();

    long produce(Populator populator);

    void registerModel(Class<?> clazz);

    interface Announcer {
        void announce(long version);

        long retrieve();
    }

    interface BlobPublisher {
        void publish(PublishableArtifact publishable);
    }

    interface BlobStagger {
        HeaderBlob stageHeader(long version);

        Blob stageDelta(long fromVersion, long toVersion);

        Blob stageReverseDelta(long fromVersion, long toVersion);
    }

    interface BlobCompressor {
        BlobCompressor EMPTY_INSTANCE = new BlobCompressor() {
            @Override
            public OutputStream compress(OutputStream os) {
                return os;
            }

            @Override
            public InputStream decompress(InputStream is) {
                return is;
            }
        };

        OutputStream compress(OutputStream os);

        InputStream decompress(InputStream is);
    }

    /**
     * Provides a publishable blob
     */
    interface PublishableArtifact extends StreamingBlob {
        void write(BlobWriter blobWriter) throws IOException;

        void cleanup();

        Path getPath();
    }

    interface StateWriter {
        /**
         * Puts an object into current state writer. Conceptually, each class type (Schema) will have separated state
         * so should have unique key for each object of each schema.
         *
         * @param key    identity key (should be unique).
         * @param object source object.
         */
        void addObject(String key, Object object);

        /**
         * Removes an object from current state writer
         *
         * @param key   identity key of object
         * @param clazz class type of that object
         */
        void removeObject(String key, Class<?> clazz);

        /**
         * @return the currently version of state writer
         */
        long getVersion();
    }

    /**
     * Provides a way to mint version for application.
     */
    interface VersionMinter {
        /**
         * Mints a new applicable version
         *
         * @return new version
         */
        long mint();
    }

    @FunctionalInterface
    interface Populator {
        void populate(StateWriter stateWriter);
    }

    static Builder fromBuilder() {
        return new Builder();
    }

    class Builder {
        BlobPublisher blobPublisher;
        BlobStagger blobStagger;
        BlobCompressor blobCompressor;
        VersionMinter versionMinter;
        Clock clock;
        Announcer announcer;
        Path blobStorePath;
        int localPort = 0;

        public Builder withBlobPublisher(BlobPublisher blobPublisher) {
            this.blobPublisher = blobPublisher;
            return this;
        }

        public Builder withBlobStagger(BlobStagger blobStagger) {
            this.blobStagger = blobStagger;
            return this;
        }

        public Builder withBlobCompressor(BlobCompressor blobCompressor) {
            this.blobCompressor = blobCompressor;
            return this;
        }

        public Builder withVersionMinter(VersionMinter versionMinter) {
            this.versionMinter = versionMinter;
            return this;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder withAnnouncer(Announcer announcer) {
            this.announcer = announcer;
            return this;
        }

        public Builder withBlobStorePath(Path path) {
            this.blobStorePath = path;
            return this;
        }

        public Builder withLocalPort(int localPort) {
            this.localPort = localPort;
            return this;
        }

        public CobraProducer buildSimple() {
            if (localPort == 0)
                localPort = NetworkConfig.DEFAULT_PORT;

            if (versionMinter == null)
                versionMinter = new SequencedVersionMinter();

            if (clock == null)
                clock = Clock.system();

            return new CobraSimpleProducer(this);
        }
    }
}
