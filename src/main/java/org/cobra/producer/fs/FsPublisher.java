package org.cobra.producer.fs;

import org.cobra.commons.errors.CobraException;
import org.cobra.commons.utils.IOx;
import org.cobra.producer.CobraProducer;
import org.cobra.producer.internal.Blob;
import org.cobra.producer.internal.HeaderBlob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FsPublisher implements CobraProducer.BlobPublisher {

    private static final Logger log = LoggerFactory.getLogger(FsPublisher.class);

    private final Path pathDir;

    public FsPublisher(Path pathDir) {
        this.pathDir = pathDir;
        IOx.mkdirs(pathDir);
    }

    @Override
    public void publish(CobraProducer.PublishableArtifact publishable) {
        try {
            if (publishable instanceof HeaderBlob headerBlob) {
                doPublishHeader(headerBlob);
            } else if (publishable instanceof Blob blob) {
                doPublishBlob(blob);
            }
        } catch (IOException e) {
            throw new CobraException(e);
        }
    }

    private void doPublishHeader(HeaderBlob headerBlob) throws IOException {
        Path dest = pathDir.resolve("header-%d".formatted(headerBlob.getVersion()));
        doPublishContent(headerBlob, dest);
    }

    private void doPublishBlob(Blob blob) throws IOException {
        Path dest = pathDir.resolve("%s-%d-%d".formatted(blob.blobType().prefix(),
                blob.fromVersion(), blob.toVersion()));
        doPublishContent(blob, dest);
    }

    private void doPublishContent(CobraProducer.PublishableArtifact publishable, Path dest) throws IOException {
        final long startMillis = System.currentTimeMillis();

        try (
                InputStream is = publishable.input();
                OutputStream os = Files.newOutputStream(dest);
        ) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) {
                os.write(buf, 0, n);
            }
        }

        final long elapsedMillis = System.currentTimeMillis() - startMillis;
        log.info("BLOB publish; blob: {}; destination: {}; elapsed: {}", publishable, dest, elapsedMillis);
    }
}
