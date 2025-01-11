package org.cobra.consumer.internal;

import org.cobra.commons.utils.Elapsed;
import org.cobra.consumer.CobraConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlobRetrieverFacade {

    private static final Logger log = LoggerFactory.getLogger(BlobRetrieverFacade.class);

    private final CobraConsumer.BlobRetriever blobRetriever;
    private final FallbackRemoteBlobRetriever fallbackRetriever;

    public BlobRetrieverFacade(CobraConsumer.BlobRetriever blobRetriever, FallbackRemoteBlobRetriever fallbackRetriever) {
        this.blobRetriever = blobRetriever;
        this.fallbackRetriever = fallbackRetriever;
    }

    public CobraConsumer.BlobRetriever blobRetriever() {
        return blobRetriever;
    }

    public FallbackRemoteBlobRetriever remoteBlobRetriever() {
        return fallbackRetriever;
    }

    public CobraConsumer.HeaderBlob getHeaderBlob(long desiredVersion) {
        final long start = System.nanoTime();
        CobraConsumer.HeaderBlob headerBlob = blobRetriever.retrieveHeader(desiredVersion);

        if (headerBlob == null) {
            fallbackRetriever.retrieveRemoteHeader(desiredVersion);
            headerBlob = blobRetriever.retrieveHeader(desiredVersion);
        }

        log.debug("get header-blob {}; took: {}", desiredVersion, Elapsed.toStr(System.nanoTime() - start));

        return headerBlob;
    }

    public CobraConsumer.Blob getDeltaBlob(long desiredVersion) {
        final long start = System.nanoTime();
        CobraConsumer.Blob deltaBlob = blobRetriever.retrieveDelta(desiredVersion);

        if (deltaBlob == null) {
            fallbackRetriever.retrieveRemoteDelta(desiredVersion);
            deltaBlob = blobRetriever.retrieveDelta(desiredVersion);
        }
        log.debug("get delta-blob {}; took: {}", desiredVersion, Elapsed.toStr(System.nanoTime() - start));

        return deltaBlob;
    }

    public CobraConsumer.Blob getReverseDeltaBlob(long desiredVersion) {
        final long start = System.nanoTime();
        CobraConsumer.Blob reverseDeltaBlob = blobRetriever.retrieveReversedDelta(desiredVersion);

        if (reverseDeltaBlob == null) {
            fallbackRetriever.retrieveRemoteReverseDelta(desiredVersion);
            reverseDeltaBlob = blobRetriever.retrieveReversedDelta(desiredVersion);
        }

        log.debug("get reversedelta-blob {}; took: {}", desiredVersion, Elapsed.toStr(System.nanoTime() - start));

        return reverseDeltaBlob;
    }
}
