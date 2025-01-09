package org.cobra.producer.state;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Provides writer definitions to write data into OutputStream
 */
public interface BlobWriter {
    void writeHeader(OutputStream os) throws IOException;

    void writeDelta(OutputStream os) throws IOException;

    void writeReversedDelta(OutputStream os) throws IOException;
}
