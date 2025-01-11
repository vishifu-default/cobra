package org.cobra.consumer.read;

import org.cobra.core.objects.BlobInput;

import java.io.IOException;
import java.io.InputStream;

public interface BlobReader {
    void applyHeader(BlobInput blobInput) throws IOException;

    void applyHeader(InputStream is) throws IOException;

    void applyDelta(BlobInput blobInput) throws IOException;

    void applyDelta(InputStream is) throws IOException;
}
