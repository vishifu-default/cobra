package org.cobra.core.memory.internal;

import org.cobra.commons.pools.BytesPool;

import java.nio.ByteBuffer;

class ElasticMemoryBlock {

    static final int MINIMUM_INIT_ALLOCATION = 6;
    private static final String GIVEN_OFFSET_OUT_OF_BOUND = "Offset is out of bound";

    private final BytesPool bytesPool;
    private ByteBuffer buffer;
    private final int limitSize;

    ElasticMemoryBlock(int log2OfAlign, final BytesPool bytesPool) {
        this.limitSize = 1 << log2OfAlign;
        this.bytesPool = bytesPool;
        this.buffer = bytesPool.allocateBufferDirect(calcInitBufferSize(log2OfAlign));
    }

    int capacity() {
        return this.buffer.capacity();
    }

    int limitSize() {
        return this.limitSize;
    }

    void write(int offset, byte b) {
        ensureOffsetInBound(offset);
        this.buffer.put(offset, b);
    }

    void writeArray(int offset, byte[] src) {
        writeArray(offset, src, 0, src.length);
    }

    void writeArray(int offset, byte[] src, int srcOffset, int len) {
        ensureOffsetInBound(offset);
        this.buffer.put(offset, src, srcOffset, len);
    }

    byte read(int offset) {
        ensureOffsetInBound(offset);
        return this.buffer.get(offset);
    }

    byte[] readArray(int offset, int len) {
        ensureOffsetInBound(offset);
        byte[] dst = new byte[len];
        this.buffer.get(offset, dst, 0, len);
        return dst;
    }

    boolean maybeGrow() {
        if (capacity() == this.limitSize)
            return false;

        int growthCapacity = Math.min(capacity() * 3 / 2, limitSize);

        ByteBuffer tempBuffer = this.buffer;
        int tempPosition = tempBuffer.position();
        ByteBuffer justAlloc = bytesPool.allocateBufferDirect(growthCapacity);
        justAlloc.put(tempBuffer);
        justAlloc.position(tempPosition);
        justAlloc.limit(growthCapacity);

        this.buffer = justAlloc;

        tempBuffer.clear();
        this.bytesPool.free(tempBuffer);

        return true;
    }

    int remaining(int offset) {
        return capacity() - offset;
    }

    private int calcInitBufferSize(int log2OfAlign) {
        int initLog2 = Math.min(MINIMUM_INIT_ALLOCATION, log2OfAlign);
        return 1 << initLog2;
    }

    private void ensureOffsetInBound(int offset) {
        if (offset >= capacity())
            throw new IndexOutOfBoundsException(GIVEN_OFFSET_OUT_OF_BOUND);
    }

    public void clear() {
        this.buffer.clear();
    }
}
