package org.cobra.consumer;

import org.cobra.commons.Clock;
import org.cobra.commons.pools.BytesPool;
import org.cobra.commons.threads.CobraThread;
import org.cobra.consumer.read.ConsumerStateContext;
import org.cobra.core.memory.MemoryMode;
import org.cobra.core.objects.StreamingBlob;
import org.cobra.core.objects.VersioningBlob;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface CobraConsumer {

    ConsumerStateContext context();

    long currentVersion();

    void poll();

    void poll(int timeoutMs);

    interface AnnouncementWatcher {
        void setLatestVersion(long latestVersion);

        long getLatestVersion();

        default VersionInformation getLatestVersionInformation() {
            return new VersionInformation(getLatestVersion());
        }
    }

    interface BlobRetriever {
        HeaderBlob retrieveHeader(long desiredVersion);

        Blob retrieveDelta(long desiredVersion);

        Blob retrieveReversedDelta(long desiredVersion);

        void saveBlob(ByteBuffer buffer, String filename) throws IOException;
    }

    abstract class HeaderBlob implements StreamingBlob {
        protected final long version;

        protected HeaderBlob(long version) {
            this.version = version;
        }

        public final long getVersion() {
            return version;
        }

        @Override
        public String toString() {
            return "HeaderBlob(" +
                    "version=" + version +
                    ')';
        }
    }

    abstract class Blob extends VersioningBlob implements StreamingBlob {

        protected Blob(long fromVersion, long toVersion) {
            super(fromVersion, toVersion);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    class VersionInformation {
        private final long version;

        public VersionInformation(long version) {
            this.version = version;
        }

        public long getVersion() {
            return version;
        }
    }

    static Builder fromBuilder() {
        return new Builder();
    }

    class Builder {
        BlobRetriever blobRetriever;
        MemoryMode memoryMode;
        BytesPool bytesPool;
        ExecutorService executor;
        Clock clock;
        InetSocketAddress producerAddress;

        public Builder withBlobRetriever(BlobRetriever blobRetriever) {
            this.blobRetriever = blobRetriever;
            return this;
        }

        public Builder withBytesPool(BytesPool bytesPool) {
            this.bytesPool = bytesPool;
            return this;
        }

        public Builder withRefreshExecutor(ExecutorService refreshExecutor) {
            this.executor = refreshExecutor;
            return this;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder withInetAddress(InetSocketAddress address) {
            this.producerAddress = address;
            return this;
        }

        public CobraConsumer build() {
            Objects.requireNonNull(blobRetriever, "blob-retriever implementation is required");

            if (clock == null) {
                clock = Clock.system();
            }

            if (bytesPool == null) {
                bytesPool = BytesPool.NONE;
            }

            if (executor == null) {
                executor = Executors.newSingleThreadExecutor(r ->
                        CobraThread.daemon(r, "consumer", "client"));
            }

            memoryMode = MemoryMode.VIRTUAL_MAPPED;

            return new CobraConsumerImpl(this);
        }
    }
}
