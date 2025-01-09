package org.cobra.producer.fs;

import org.cobra.commons.Jvm;
import org.cobra.commons.errors.CobraException;
import org.cobra.commons.utils.IOx;
import org.cobra.producer.CobraProducer;
import org.cobra.producer.internal.Blob;
import org.cobra.producer.internal.HeaderBlob;
import org.cobra.producer.state.BlobWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesystemBlobStagger implements CobraProducer.BlobStagger {

    private final Path stagingPathDir;
    private final CobraProducer.BlobCompressor compressor;

    public FilesystemBlobStagger() {
        this(CobraProducer.BlobCompressor.EMPTY_INSTANCE);
    }

    public FilesystemBlobStagger(CobraProducer.BlobCompressor compressor) {
        this.stagingPathDir = Path.of(Jvm.SYSTEM_TEMPDIR);
        this.compressor = compressor;
    }

    @Override
    public HeaderBlob stageHeader(long version) {
        return new FilesystemHeaderBlob(version, stagingPathDir, compressor);
    }

    @Override
    public Blob stageDelta(long fromVersion, long toVersion) {
        return new FilesystemBlob(fromVersion, toVersion, stagingPathDir, compressor);
    }

    @Override
    public Blob stageReverseDelta(long fromVersion, long toVersion) {
        return new FilesystemBlob(fromVersion, toVersion, stagingPathDir, compressor);
    }

    public static class FilesystemHeaderBlob extends HeaderBlob {
        private final CobraProducer.BlobCompressor compressor;

        public FilesystemHeaderBlob(long version, Path path) {
            this(version, path, CobraProducer.BlobCompressor.EMPTY_INSTANCE);
        }

        public FilesystemHeaderBlob(long version, Path path, CobraProducer.BlobCompressor compressor) {
            super(version, path);
            this.compressor = compressor;
        }

        @Override
        public void write(BlobWriter blobWriter) throws IOException {
            Path parent = this.path.getParent();
            IOx.mkdirs(parent);
            IOx.touch(this.path);

            try (
                    OutputStream os =
                            new BufferedOutputStream(this.compressor.compress(Files.newOutputStream(this.path)))
            ) {
                blobWriter.writeHeader(os);
            }

        }

        @Override
        public void cleanup() {
            if (this.path != null) {
                try {
                    Files.deleteIfExists(this.path);
                } catch (IOException e) {
                    throw new CobraException(e);
                }
            }
        }

        @Override
        public Path getPath() {
            return this.path;
        }

        @Override
        public InputStream input() throws IOException {
            return new BufferedInputStream(this.compressor.decompress(Files.newInputStream(this.path)));
        }
    }


    public static class FilesystemBlob extends Blob {

        private final CobraProducer.BlobCompressor compressor;

        public FilesystemBlob(long fromVersion, long toVersion, Path dir, CobraProducer.BlobCompressor compressor) {
            super(dir, fromVersion, toVersion);
            this.compressor = compressor;
        }

        @Override
        public void write(BlobWriter blobWriter) throws IOException {
            Path parentDir = this.path.getParent();

            IOx.mkdirs(parentDir);
            IOx.touch(this.path);

            try (
                    OutputStream os =
                            new BufferedOutputStream(this.compressor.compress(Files.newOutputStream(this.path)))
            ) {
                switch (blobType) {
                    case DELTA_BLOB -> blobWriter.writeDelta(os);
                    case REVERSED_DELTA_BLOB -> blobWriter.writeReversedDelta(os);
                    default -> throw new IllegalStateException("Unknown blob type");
                }
            }
        }

        @Override
        public void cleanup() {
            if (this.path != null) {
                try {
                    Files.deleteIfExists(this.path);
                } catch (IOException e) {
                    throw new CobraException(e);
                }
            }
        }

        @Override
        public InputStream input() throws IOException {
            return new BufferedInputStream(this.compressor.decompress(Files.newInputStream(this.path)));
        }
    }
}
