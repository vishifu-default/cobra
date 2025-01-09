package org.cobra.consumer;

import org.cobra.commons.Clock;
import org.cobra.commons.CobraConstants;
import org.cobra.commons.pools.BytesPool;
import org.cobra.commons.threads.CobraThread;
import org.cobra.consumer.read.ConsumerStateContext;
import org.cobra.core.memory.MemoryMode;
import org.cobra.core.objects.StreamingBlob;
import org.cobra.core.objects.VersioningBlob;
import org.cobra.networks.CobraClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface CobraConsumer {

    void triggerRefreshWithDelay(int ms);

    ConsumerStateContext context();

    long currentVersion();

    interface AnnouncementWatcher {
        long NO_ANNOUNCEMENT_AVAILABLE = CobraConstants.VERSION_NULL;

        void setLatestVersion(long latestVersion);

        long getLatestVersion();

        void subscribeToUpdates(CobraConsumer consumer);

        default VersionInformation getLatestVersionInformation() {
            return new VersionInformation(getLatestVersion());
        }
    }

    interface BlobRetriever {
        HeaderBlob retrieveHeader(long desiredVersion);

        Blob retrieveDelta(long desiredVersion);

        Blob retrieveReversedDelta(long desiredVersion);
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

    public static Builder fromBuilder() {
        return new Builder();
    }

    class Builder {
        BlobRetriever blobRetriever;
        MemoryMode memoryMode;
        BytesPool bytesPool;
        ExecutorService refreshExecutor;
        Clock clock;
        CobraClient client;

        public Builder withBlobRetriever(BlobRetriever blobRetriever) {
            this.blobRetriever = blobRetriever;
            return this;
        }

        public Builder withBytesPool(BytesPool bytesPool) {
            this.bytesPool = bytesPool;
            return this;
        }

        public Builder withRefreshExecutor(ExecutorService refreshExecutor) {
            this.refreshExecutor = refreshExecutor;
            return this;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder withNetworkClient(CobraClient client) {
            this.client = client;
            return this;
        }

        public CobraConsumer build() {
            if (clock == null)
                clock = Clock.system();

            if (bytesPool == null)
                bytesPool = BytesPool.NONE;

            if (refreshExecutor == null)
                refreshExecutor = Executors.newSingleThreadExecutor(r -> CobraThread.daemon(r, "refresh-executor"));

            memoryMode = MemoryMode.VIRTUAL_MAPPED;

            return new CobraConsumerImpl(this);
        }
    }
}
