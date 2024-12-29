package org.cobra.producer;

import org.cobra.core.objects.StreamingBlob;
import org.cobra.core.serialization.SerdeClassResolver;
import org.cobra.producer.internal.Blob;
import org.cobra.producer.internal.HeaderBlob;
import org.cobra.producer.state.BlobWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public interface CobraProducer {

    long produce(Populator populator);


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

        void close();

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
        void putObject(String key, Object object);

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
}
