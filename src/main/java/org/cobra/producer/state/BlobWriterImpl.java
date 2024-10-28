package org.cobra.producer.state;

import java.io.IOException;
import java.io.OutputStream;

public class BlobWriterImpl implements BlobWriter {
    @Override
    public void writeHeader(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public void writeDelta(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public void writeReversedDelta(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("implement me");
    }
}
