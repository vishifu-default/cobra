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

public class FilesystemBlobFetcher implements CobraConsumer.BlobFetcher {

    private static final Logger log = LoggerFactory.getLogger(FilesystemBlobFetcher.class);

    private final Path blobStorePath;
    private final CobraConsumer.BlobFetcher fallbackBlobFetcher;

    public FilesystemBlobFetcher(Path blobStorePath, CobraConsumer.BlobFetcher fallbackBlobFetcher) {
        this.blobStorePath = blobStorePath;
        this.fallbackBlobFetcher = fallbackBlobFetcher;

        ensurePathExists(blobStorePath);
    }

    @Override
    public CobraConsumer.HeaderBlob fetchHeaderBlob(long desiredVersion) {
        Path execPath = blobStorePath.resolve("header-%d".formatted(desiredVersion));
        if (Files.exists(execPath)) {
            return new FsHeaderBlob(execPath, desiredVersion);
        }

        CobraConsumer.HeaderBlob remoteFsBlob = null;
        if (fallbackBlobFetcher != null) {
            remoteFsBlob = fallbackBlobFetcher.fetchHeaderBlob(desiredVersion);
        }

        if (remoteFsBlob == null)
            throw new CobraException("Fallback header-blob could not be fetched.");

        return remoteFsBlob;
    }


    @Override
    public CobraConsumer.Blob fetchDeltaBlob(long desiredVersion) {
        try (
                DirectoryStream<Path> dirStream = Files.newDirectoryStream(blobStorePath)
        ) {
            for (Path path : dirStream) {
                String filename = path.getFileName().toString();
                if (filename.startsWith("delta-") && filename.endsWith("%d".formatted(desiredVersion))) {
                    long fromVersion = Long.parseLong(
                            filename.substring(filename.indexOf('-') + 1, filename.lastIndexOf('-')));
                    return fsBlob(BlobType.DELTA_BLOB, fromVersion, desiredVersion);
                }
            }
        } catch (IOException e) {
            throw new CobraException(e);
        }

        CobraConsumer.Blob remoteFsBlob = null;
        if (fallbackBlobFetcher != null) {
            remoteFsBlob = fallbackBlobFetcher.fetchDeltaBlob(desiredVersion);
        }

        if (remoteFsBlob == null)
            throw new CobraException("Fallback delta-blob could not be fetched.");

        return remoteFsBlob;
    }

    @Override
    public CobraConsumer.Blob fetchReversedDeltaBlob(long desiredVersion) {
        try (
                DirectoryStream<Path> dirStream = Files.newDirectoryStream(blobStorePath)
        ) {
            for (Path path : dirStream) {
                String filename = path.getFileName().toString();
                if (filename.startsWith("reversedelta-%d".formatted(desiredVersion))) {
                    long toVersion = Long.parseLong(filename.substring(filename.lastIndexOf('-') + 1));
                    return fsBlob(BlobType.REVERSED_DELTA_BLOB, desiredVersion, toVersion);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CobraConsumer.Blob remoteFsBlob = null;
        if (fallbackBlobFetcher != null) {
            remoteFsBlob = fallbackBlobFetcher.fetchReversedDeltaBlob(desiredVersion);
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
                yield new FsBlob(path, fromVersion, toVersion);
            }
        };
    }

    private static class FsHeaderBlob extends CobraConsumer.HeaderBlob {

        private final Path path;

        private FsHeaderBlob(Path path, long version) {
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

    private static class FsBlob extends CobraConsumer.Blob {

        private final Path path;

        protected FsBlob(Path path, long fromVersion, long toVersion) {
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
