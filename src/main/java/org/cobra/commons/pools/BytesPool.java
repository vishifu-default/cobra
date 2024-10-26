package org.cobra.commons.pools;

import java.nio.ByteBuffer;

public interface BytesPool {

    /**
     * A pool instance with no implementation
     */
    BytesPool NONE = new BytesPool() {

        @Override
        public byte[] allocateArray(int size) {
            return new byte[size];
        }

        @Override
        public ByteBuffer allocateBuffer(int capacity) {
            return ByteBuffer.allocate(capacity);
        }

        @Override
        public ByteBuffer allocateBufferDirect(int capacity) {
            return ByteBuffer.allocateDirect(capacity);
        }

        @Override
        public void free(byte[] arr) {
            // nop
        }

        @Override
        public void free(ByteBuffer buffer) {
            // nop
        }
    };

    /**
     * Allocate a block array of memory for given size
     *
     * @param size requested array size
     * @return byte array
     */
    byte[] allocateArray(int size);

    /**
     * Allocate a block of ByteBuffer for given capacity
     *
     * @param capacity requested capacity
     * @return ByteBuffer
     */
    ByteBuffer allocateBuffer(int capacity);

    /**
     * Allocate a native block of ByteBuffer (off-heap) for given capacity
     *
     * @param capacity requested capacity
     * @return ByteBuffer
     */
    ByteBuffer allocateBufferDirect(int capacity);

    /**
     * Free a block of memory and maybe return it to pool
     *
     * @param arr target memory array
     */
    void free(byte[] arr);

    /**
     * Free a block of memory and maybe return it to pool
     *
     * @param buffer target buffer
     */
    void free(ByteBuffer buffer);
}
