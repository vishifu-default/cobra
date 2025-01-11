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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesystemBlobRetriever implements CobraConsumer.BlobRetriever {

    private static final Logger log = LoggerFactory.getLogger(FilesystemBlobRetriever.class);

    private final Path blobStorePath;

    public FilesystemBlobRetriever(Path blobStorePath) {
        this.blobStorePath = blobStorePath;

        ensurePathExists(blobStorePath);
    }

    @Override
    public CobraConsumer.HeaderBlob retrieveHeader(long desiredVersion) {
        Path execPath = blobStorePath.resolve("header-%d".formatted(desiredVersion));
        if (Files.exists(execPath)) {
            return new FilesystemHeader(execPath, desiredVersion);
        }

        return null;
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
                    long fileVersion = Long.parseLong(filename.substring(lastSplitIndex + 1));
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

        return null;
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

        return null;
    }

    @Override
    public void saveBlob(ByteBuffer buffer, String filename) throws IOException {
        final Path filepath = blobStorePath.resolve(filename);
        final RandomAccessFile raf = new RandomAccessFile(filepath.toFile(), "rw");

        final FileChannel fc = raf.getChannel();
        final int countWrites = fc.write(buffer);
        if (countWrites < 0)
            throw new CobraException("negative write bytes");

        fc.close();
        raf.close();

        log.debug("saved blob {}", filepath.toAbsolutePath());
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

        @Override
        public String toString() {
            return "FilesystemHeader(" +
                    "version=" + getVersion() + ", " +
                    "path=" + path.getFileName().toString() +
                    ')';
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

        @Override
        public String toString() {
            return "FilesystemBlob(" +
                    "path=" + path.getFileName().toString() +
                    ", fromVersion=" + fromVersion +
                    ", toVersion=" + toVersion +
                    ", blobType=" + blobType +
                    ')';
        }
    }
}
