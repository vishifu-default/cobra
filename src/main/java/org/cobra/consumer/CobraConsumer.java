package org.cobra.consumer;

import org.cobra.core.objects.StreamingBlob;
import org.cobra.core.objects.VersioningBlob;

public interface CobraConsumer {

    void fetch();


    interface BlobFetcher {
        HeaderBlob fetchHeaderBlob(long version);

        Blob fetchDeltaBlob(long version);

        Blob fetchReversedDeltaBlob(long version);
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
}
