package org.cobra.consumer;

import org.cobra.commons.CobraConstants;
import org.cobra.commons.pools.BytesPool;
import org.cobra.commons.threads.CobraThreadExecutor;
import org.cobra.core.memory.MemoryMode;
import org.cobra.core.objects.StreamingBlob;
import org.cobra.core.objects.VersioningBlob;

import java.nio.file.Path;

public interface CobraConsumer {

    interface AnnouncementWatcher {
        long NO_ANNOUNCEMENT_AVAILABLE = CobraConstants.VERSION_NULL;

        long getLatestVersion();

        void subscribeToUpdates(CobraConsumer consumer);

        default VersionInformation getLatestVersionInformation() {
            return new VersionInformation(getLatestVersion());
        }
    }

    interface BlobFetcher {
        HeaderBlob fetchHeaderBlob(long desiredVersion);

        Blob fetchDeltaBlob(long desiredVersion);

        Blob fetchReversedDeltaBlob(long desiredVersion);
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
        BlobFetcher blobFetcher;
        MemoryMode memoryMode;
        BytesPool bytesPool;
        Path blobStorePath;
        CobraThreadExecutor refreshExecutor;
        AnnouncementWatcher announcementWatcher;

        public Builder withBlobFetcher(BlobFetcher blobFetcher) {
            this.blobFetcher = blobFetcher;
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

        public Builder withBlobStorePath(Path blobStorePath) {
            this.blobStorePath = blobStorePath;
            return this;
        }

        public Builder withRefreshExecutor(CobraThreadExecutor refreshExecutor) {
            this.refreshExecutor = refreshExecutor;
            return this;
        }

        public Builder withAnnouncementWatcher(AnnouncementWatcher announcementWatcher) {
            this.announcementWatcher = announcementWatcher;
            return this;
        }

        public CobraConsumer build() {
            return new CobraConsumerImpl(this);
        }
    }
}
