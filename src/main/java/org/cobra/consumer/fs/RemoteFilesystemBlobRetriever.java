package org.cobra.consumer.fs;

import org.cobra.commons.errors.CobraException;
import org.cobra.consumer.CobraConsumer;
import org.cobra.core.objects.BlobType;
import org.cobra.networks.CobraClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class RemoteFilesystemBlobRetriever implements CobraConsumer.BlobRetriever {

    private static final Logger log = LoggerFactory.getLogger(RemoteFilesystemBlobRetriever.class);
    private final CobraClient client;
    private final Path blobStorePath;

    public RemoteFilesystemBlobRetriever(CobraClient client, Path blobStorePath) {
        this.client = client;
        this.blobStorePath = blobStorePath;
    }

    @Override
    public CobraConsumer.HeaderBlob retrieveHeader(long desiredVersion) {
        try {
            log.debug("retrieving header-blob {}", desiredVersion);

            ByteBuffer headerBuffer = client.fetchHeaderBuffer(desiredVersion);
            Path filepath = blobStorePath.resolve("header-%d".formatted(desiredVersion));

            RandomAccessFile raf = new RandomAccessFile(filepath.toFile(), "rw");
            FileChannel channel = raf.getChannel();
            channel.write(headerBuffer);
            channel.close();
            raf.close();

            log.info("retrieve header-blob {}", desiredVersion);

            return new FilesystemBlobRetriever.FilesystemHeader(filepath, desiredVersion);
        } catch (IOException e) {
            throw new CobraException(e);
        }
    }

    @Override
    public CobraConsumer.Blob retrieveDelta(long desiredVersion) {
        try {
            Path filepath = doRetrieveBlob(desiredVersion - 1, desiredVersion);
            log.info("retrieve delta-blob {}", desiredVersion);
            return new FilesystemBlobRetriever.FilesystemBlob(filepath, desiredVersion - 1, desiredVersion);
        } catch (IOException e) {
            throw new CobraException(e);
        }
    }

    @Override
    public CobraConsumer.Blob retrieveReversedDelta(long desiredVersion) {
        try {
            Path filepath = doRetrieveBlob(desiredVersion, desiredVersion + 1);
            log.info("retrieve reversed-delta-blob {}", desiredVersion);
            return new FilesystemBlobRetriever.FilesystemBlob(filepath, desiredVersion, desiredVersion + 1);
        } catch (IOException e) {
            throw new CobraException(e);
        }
    }

    private Path doRetrieveBlob(long fromVersion, long toVersion) throws IOException {
        ByteBuffer blobBuffer = client.fetchBlobBuffer(fromVersion, toVersion);
        String prefix = fromVersion < toVersion ? BlobType.DELTA_BLOB.prefix() : BlobType.REVERSED_DELTA_BLOB.prefix();

        Path filepath = blobStorePath.resolve("%s-%d-%d".formatted(prefix, fromVersion, toVersion));
        RandomAccessFile raf = new RandomAccessFile(filepath.toFile(), "rw");
        FileChannel channel = raf.getChannel();
        channel.write(blobBuffer);
        channel.close();
        raf.close();

        return filepath;
    }
}
