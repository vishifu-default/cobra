package org.cobra.consumer.internal;

import org.cobra.commons.errors.CobraException;
import org.cobra.commons.utils.Elapsed;
import org.cobra.consumer.CobraConsumer;
import org.cobra.core.objects.BlobType;
import org.cobra.networks.CobraClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

public class FallbackRemoteBlobRetriever {

    private static final Logger log = LoggerFactory.getLogger(FallbackRemoteBlobRetriever.class);

    private final CobraClient client;
    private final CobraConsumer.BlobRetriever blobRetriever;

    public FallbackRemoteBlobRetriever(CobraClient client, CobraConsumer.BlobRetriever blobRetriever) {
        this.client = client;
        this.blobRetriever = blobRetriever;
    }

    public void retrieveRemoteHeader(long desiredVersion) {
        log.debug("retrieving remote header-blob version {}", desiredVersion);
        final long start = System.nanoTime();
        String filename = null;
        try {
            final ByteBuffer headerBuffer = client.fetchHeaderBuffer(desiredVersion);
            filename = "header-%d".formatted(desiredVersion);
            blobRetriever.saveBlob(headerBuffer, filename);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CobraException(e);
        } finally {
            log.debug("retrieved remote header-blob {}; elapsed: {}", filename,
                    Elapsed.toStr(System.nanoTime() - start));
        }
    }

    public void retrieveRemoteDelta(long desiredVersion) {
        log.debug("retrieving remote delta-blob version {}", desiredVersion);
        final long start = System.nanoTime();
        String filename = null;
        try {
            filename = doRetrieveRemoteBlob(desiredVersion - 1, desiredVersion);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CobraException(e);
        } finally {
            log.debug("retrieved remote delta-blob {}; elapsed: {}", filename,
                    Elapsed.toStr(System.nanoTime() - start));
        }
    }

    public void retrieveRemoteReverseDelta(long desiredVersion) {
        log.debug("retrieving remote reversedelta-blob version {}", desiredVersion);
        final long start = System.nanoTime();
        String filename = null;
        try {
            filename = doRetrieveRemoteBlob(desiredVersion + 1, desiredVersion);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CobraException(e);
        } finally {
            log.debug("retrieved remote reversedelta-blob {}; elapsed: {}", filename,
                    Elapsed.toStr(System.nanoTime() - start));
        }
    }


    private String doRetrieveRemoteBlob(long fromVersion, long toVersion) throws IOException {
        final ByteBuffer blobBuffer = client.fetchBlobBuffer(fromVersion, toVersion);

        final String prefix = fromVersion < toVersion ? BlobType.DELTA_BLOB.prefix() :
                BlobType.REVERSED_DELTA_BLOB.prefix();
        final String filename = "%s-%d-%d".formatted(prefix, fromVersion, toVersion);
        blobRetriever.saveBlob(blobBuffer, filename);

        return filename;
    }
}
