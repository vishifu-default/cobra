package org.cobra.commons.pools;

import java.nio.ByteBuffer;

public class AutoGrowBufferSupplier implements BufferSupplier {

    private ByteBuffer cached = null;

    /**
     * Try to allocate a new ByteBuffer if it's not cached, if have cache and the cached buffer size is greater than
     * the given capacity, we'll reuse it
     *
     * @param capacity min required buffer capacity
     * @return a ByteBuffer that have capacity >= the required size
     */
    @Override
    public ByteBuffer get(int capacity) {
        if (cached != null && cached.capacity() > capacity) {
            ByteBuffer ret = cached;
            cached = null;
            return ret;
        }

        cached = null;
        return ByteBuffer.allocate(capacity);
    }

    /**
     * Cache the buffer
     *
     * @param buffer provided used buffer
     */
    @Override
    public void release(ByteBuffer buffer) {
        buffer.clear();
        cached = buffer;
    }

    /**
     * Clear the cached buffer
     */
    @Override
    public void close() {
        cached = null;
    }

    @Override
    public String toString() {
        return "AutoGrowBufferSupplier(" +
                "cached=" + cached +
                ')';
    }
}
