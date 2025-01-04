package org.cobra.consumer.fs;

import org.cobra.commons.errors.CobraException;
import org.cobra.commons.utils.IOx;
import org.cobra.consumer.CobraConsumer;
import org.cobra.core.objects.BlobType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesystemBlobRetriever implements CobraConsumer.BlobRetriever {

    private static final Logger log = LoggerFactory.getLogger(FilesystemBlobRetriever.class);

    private final Path blobStorePath;
    private final CobraConsumer.BlobRetriever fallbackBlobRetriever;

    public FilesystemBlobRetriever(Path blobStorePath) {
        this(blobStorePath, null);
    }

    public FilesystemBlobRetriever(Path blobStorePath, CobraConsumer.BlobRetriever fallbackBlobRetriever) {
        this.blobStorePath = blobStorePath;
        this.fallbackBlobRetriever = fallbackBlobRetriever;

        ensurePathExists(blobStorePath);
    }

    @Override
    public CobraConsumer.HeaderBlob retrieveHeader(long desiredVersion) {
        Path execPath = blobStorePath.resolve("header-%d".formatted(desiredVersion));
        if (Files.exists(execPath)) {
            return new FilesystemHeader(execPath, desiredVersion);
        }

        CobraConsumer.HeaderBlob remoteFsBlob = null;
        if (fallbackBlobRetriever != null) {
            remoteFsBlob = fallbackBlobRetriever.retrieveHeader(desiredVersion);
        }

        if (remoteFsBlob == null)
            throw new CobraException("Fallback header-blob could not be fetched.");

        return remoteFsBlob;
    }


    @Override
    public CobraConsumer.Blob retrieveDelta(long desiredVersion) {
        try (
                DirectoryStream<Path> dirStream = Files.newDirectoryStream(blobStorePath)
        ) {
            for (Path path : dirStream) {
                String filename = path.getFileName().toString();
                if (filename.startsWith("delta-") && filename.endsWith("%d".formatted(desiredVersion))) {

                    // MORE CHECK
                    int lastSplitIndex = filename.lastIndexOf("-");
                    long fileVersion = Long.parseLong(filename.substring(lastSplitIndex));
                    if (fileVersion != desiredVersion)
                        continue;

                    long fromVersion = Long.parseLong(
                            filename.substring(filename.indexOf('-') + 1, lastSplitIndex));
                    return fsBlob(BlobType.DELTA_BLOB, fromVersion, desiredVersion);
                }
            }
        } catch (IOException e) {
            throw new CobraException(e);
        }

        CobraConsumer.Blob remoteFsBlob = null;
        if (fallbackBlobRetriever != null) {
            remoteFsBlob = fallbackBlobRetriever.retrieveDelta(desiredVersion);
        }

        if (remoteFsBlob == null)
            throw new CobraException("Fallback delta-blob could not be fetched.");

        return remoteFsBlob;
    }

    @Override
    public CobraConsumer.Blob retrieveReversedDelta(long desiredVersion) {
        try (
                DirectoryStream<Path> dirStream = Files.newDirectoryStream(blobStorePath)
        ) {
            for (Path path : dirStream) {
                String filename = path.getFileName().toString();

                if (filename.startsWith("reversedelta-") && filename.endsWith("%d".formatted(desiredVersion))) {
                    int lastSplitIndex = filename.lastIndexOf("-");
                    long fileVersion = Long.parseLong(filename.substring(lastSplitIndex + 1));

                    if (fileVersion != desiredVersion)
                        continue;

                    int firstSplitIndex = filename.indexOf("-");
                    long fromVersion = Long.parseLong(filename.substring(firstSplitIndex + 1, lastSplitIndex));
                    return fsBlob(BlobType.REVERSED_DELTA_BLOB, fromVersion, desiredVersion);

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CobraConsumer.Blob remoteFsBlob = null;
        if (fallbackBlobRetriever != null) {
            remoteFsBlob = fallbackBlobRetriever.retrieveReversedDelta(desiredVersion);
        }

        if (remoteFsBlob == null)
            throw new CobraException("Fallback delta-blob could not be fetched.");

        return remoteFsBlob;
    }

    private void ensurePathExists(Path path) {
        IOx.mkdirs(path);
    }

    private CobraConsumer.Blob fsBlob(BlobType blobType, long fromVersion, long toVersion) {
        Path path;
        return switch (blobType) {
            case DELTA_BLOB, REVERSED_DELTA_BLOB -> {
                path = blobStorePath.resolve("%s-%d-%d".formatted(blobType.prefix(), fromVersion, toVersion));
                yield new FilesystemBlob(path, fromVersion, toVersion);
            }
        };
    }

    public static class FilesystemHeader extends CobraConsumer.HeaderBlob {

        private final Path path;

        public FilesystemHeader(Path path, long version) {
            super(version);
            this.path = path;
        }

        @Override
        public InputStream input() throws IOException {
            return new BufferedInputStream(Files.newInputStream(path));
        }

        @Override
        public File file() throws IOException {
            return path.toFile();
        }
    }

    public static class FilesystemBlob extends CobraConsumer.Blob {

        private final Path path;

        public FilesystemBlob(Path path, long fromVersion, long toVersion) {
            super(fromVersion, toVersion);
            this.path = path;
        }

        @Override
        public InputStream input() throws IOException {
            return new BufferedInputStream(Files.newInputStream(path));
        }

        @Override
        public File file() throws IOException {
            return path.toFile();
        }
    }
}
