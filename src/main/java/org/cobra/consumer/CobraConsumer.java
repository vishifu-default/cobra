package org.cobra.consumer;

import org.cobra.commons.Clock;
import org.cobra.commons.CobraConstants;
import org.cobra.commons.pools.BytesPool;
import org.cobra.commons.threads.CobraThreadExecutor;
import org.cobra.core.memory.MemoryMode;
import org.cobra.core.objects.StreamingBlob;
import org.cobra.core.objects.VersioningBlob;
import org.cobra.networks.CobraClient;

import java.nio.file.Path;

public interface CobraConsumer {

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
    }

    abstract class Blob extends VersioningBlob implements StreamingBlob {

        protected Blob(long fromVersion, long toVersion) {
            super(fromVersion, toVersion);
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
        CobraThreadExecutor refreshExecutor;
        Clock clock = Clock.system();
        CobraClient client;

        public Builder withBlobRetriever(BlobRetriever blobRetriever) {
            this.blobRetriever = blobRetriever;
            return this;
        }

        public Builder withMemoryMode(MemoryMode memoryMode) {
            this.memoryMode = memoryMode;
            return this;
        }

        public Builder withBytesPool(BytesPool bytesPool) {
            this.bytesPool = bytesPool;
            return this;
        }

        public Builder withRefreshExecutor(CobraThreadExecutor refreshExecutor) {
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
            return new CobraConsumerImpl(this);
        }
    }
}
