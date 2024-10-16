package org.cobra.networks;

import org.cobra.commons.errors.InvalidSocketReceiveSizeException;
import org.cobra.commons.pools.MemoryAlloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;

public class ReceiveNetwork implements Receive {

    private static final Logger log = LoggerFactory.getLogger(ReceiveNetwork.class);

    private static final int UNLIMITED = -1;
    private static final String UNKNOWN_SOURCE = "UNKNOWN";
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

    private final String source;
    private final MemoryAlloc memoryAlloc;
    private final ByteBuffer sizeBuffer;
    private ByteBuffer buffer;
    private int requestedSize;

    public ReceiveNetwork() {
        this(UNKNOWN_SOURCE);
    }

    public ReceiveNetwork(String source) {
        this(source, MemoryAlloc.NONE);
    }

    public ReceiveNetwork(String source, ByteBuffer buffer) {
        this(source);
        this.buffer = buffer;
    }

    public ReceiveNetwork(String source, MemoryAlloc memoryAlloc) {
        this.source = source;
        this.memoryAlloc = memoryAlloc;
        this.requestedSize = UNLIMITED;
        this.sizeBuffer = ByteBuffer.allocate(4);
    }

    public ByteBuffer payload() {
        return buffer;
    }

    public int readBytes() {
        int ans = sizeBuffer.position();
        if (!isBlankBuffer())
            ans += buffer.position();

        return ans;
    }

    @Override
    public String source() {
        return this.source;
    }

    @Override
    public boolean isCompleted() {
        return !sizeBuffer.hasRemaining() && buffer != null && !buffer.hasRemaining();
    }

    @Override
    public long readFrom(ScatteringByteChannel channel) throws IOException {
        int reads = 0;

        if (sizeBuffer.hasRemaining()) {
            int sizeRead = channel.read(sizeBuffer);
            if (sizeRead < 0)
                throw new EOFException("attempt to read a negative bytes, maybe channel is closed");

            reads += sizeRead;

            if (!sizeBuffer.hasRemaining()) {
                sizeBuffer.rewind();
                int needSize = sizeBuffer.getInt();

                if (needSize < 0)
                    throw new InvalidSocketReceiveSizeException(needSize);

                requestedSize = needSize;
                if (needSize == 0)
                    buffer = EMPTY_BUFFER;
            }
        }

        if (buffer == null && requestedSize != UNLIMITED) {
            buffer = memoryAlloc.allocate(requestedSize);
            if (buffer == null)
                log.trace("could not allocate buffer memory, maybe low memory; requested_size = {}", requestedSize);
        }

        if (buffer != null) {
            int byteReads = channel.read(buffer);
            if (byteReads < 0)
                throw new EOFException("attempt to read a negative bytes, maybe channel is closed");

            reads += byteReads;
        }

        return reads;
    }

    @Override
    public boolean requiredMemoryKnown() {
        return requestedSize != UNLIMITED;
    }

    @Override
    public boolean isMemoryAllocated() {
        return buffer != null;
    }

    @Override
    public void close() {
        if (isBlankBuffer())
            return;

        memoryAlloc.release(buffer);
        buffer = null;
    }

    private boolean isBlankBuffer() {
        return buffer == null || buffer == EMPTY_BUFFER;
    }

    @Override
    public String toString() {
        return "ReceiveNetwork(" +
                "buffer=" + buffer +
                ", source='" + source + '\'' +
                ", memoryAlloc=" + memoryAlloc +
                ", sizeBuffer=" + sizeBuffer +
                ", requestedSize=" + requestedSize +
                ')';
    }
}
