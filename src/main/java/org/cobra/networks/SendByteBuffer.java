package org.cobra.networks;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;

/**
 * A network send backed by an array of ByteBuffer
 */
public class SendByteBuffer implements Send {

    private final long size;
    private final ByteBuffer[] buffers;
    private long remaining;

    public SendByteBuffer(ByteBuffer... buffers) {
        this.buffers = buffers;
        remaining = 0;
        for (ByteBuffer buffer : buffers)
            remaining += buffer.remaining();

        size = remaining;
    }

    public static SendByteBuffer toPrefixedLenSend(ByteBuffer buffer) {
        ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
        sizeBuffer.putInt(0, buffer.remaining());
        return new SendByteBuffer(sizeBuffer, buffer);
    }

    @Override
    public boolean isCompleted() {
        return remaining == 0;
    }

    @Override
    public long writeTo(GatheringByteChannel channel) throws IOException {
        long writes = channel.write(buffers);
        if (writes < 0)
            throw new EOFException("attempt to write negative bytes to channel");

        remaining -= writes;
        return writes;
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public void close() {
        // nop
    }
}
