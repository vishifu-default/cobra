package org.cobra.commons.pools;

import java.nio.ByteBuffer;

public interface MemoryAlloc extends AutoCloseable {
    MemoryAlloc NONE = new MemoryAlloc() {

        @Override
        public ByteBuffer allocate(int size) {
            return ByteBuffer.allocate(size);
        }

        @Override
        public void release(ByteBuffer buffer) {
            // nop
        }

        @Override
        public void close() {
            // nop
        }

    };

    /**
     * Attempt to acquire a ByteBuffer with size
     *
     * @param size allocation size
     * @return buffer with size
     */
    ByteBuffer allocate(int size);

    /**
     * Return previous allocated buffer to pool
     *
     * @param buffer previous allocation
     */
    void release(ByteBuffer buffer);

    /**
     * @return total size of this pool can provide
     */
    default long size() {
        return -1;
    }
}
