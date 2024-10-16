package org.cobra.networks.mocks;

import org.cobra.networks.TransferableChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ByteBufferChannel implements TransferableChannel {
    private final ByteBuffer buffer;
    private boolean closed;

    public ByteBufferChannel(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public ByteBufferChannel(int size) {
        this.buffer = ByteBuffer.allocate(size);
    }

    @Override
    public boolean hasPendingWrites() {
        return false;
    }

    @Override
    public long transferFrom(FileChannel fc, long position, long count) throws IOException {
        return fc.transferTo(position, count, this);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || (offset + length) > srcs.length)
            throw new IndexOutOfBoundsException();

        int pos = this.buffer.position();
        int count = offset + length;
        for (int i = offset; i < count; i++) {
            this.buffer.put(srcs[i].duplicate());
        }
        return this.buffer.position() - pos;
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return (int) write(new ByteBuffer[]{src});
    }

    @Override
    public boolean isOpen() {
        return !this.closed;
    }

    @Override
    public void close() throws IOException {
        this.buffer.flip();
        this.closed = true;
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }
}
