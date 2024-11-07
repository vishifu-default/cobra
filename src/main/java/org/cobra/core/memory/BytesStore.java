package org.cobra.core.memory;

public interface BytesStore {

    /**
     * Puts a byte value into given offset memory
     *
     * @param offset offset memory
     * @param b      byte value
     */
    void putByte(long offset, byte b);

    /**
     * Puts an integer value into given offset memory
     *
     * @param offset offset memory
     * @param i32    integer value
     */
    void putInt(long offset, int i32);

    /**
     * Puts a long value into given offset memory
     *
     * @param offset offset memory
     * @param i64    long value
     */
    void putLong(long offset, long i64);

    /**
     * Puts a byte array into the given memory offset
     *
     * @param offset    starting writing memory offset
     * @param src       source byte array
     * @param srcOffset starting position of source
     * @param len       number of bytes to write
     */
    void putBytes(long offset, byte[] src, int srcOffset, int len);

    /**
     * Gets a byte value from offset memory
     *
     * @param offset offset memory
     * @return byte value
     */
    byte getByte(long offset);

    /**
     * Gets a integer value from offset memory
     *
     * @param offset offset memory
     * @return integer value
     */
    int getInt(long offset);

    /**
     * Gets a long value from offset memory
     *
     * @param offset offset memory
     * @return long value
     */
    long getLong(long offset);

    /**
     * Reads a block of memory into a byte array destination
     *
     * @param offset    starting reading offset of the memory
     * @param dst       destination byte array
     * @param dstOffset starting position writing of destination
     * @param len       expected number of bytes to read
     * @return number of bytes that have been read
     */
    int getBytes(long offset, byte[] dst, int dstOffset, int len);

    /**
     * @return the current capacity of store.
     */
    long capacity();

    /**
     * Checks as if we can resize the current store
     *
     * @param requiredSize new size to grow
     * @return true if perform resizing, otherwise false
     */
    void checkResizing(long requiredSize);
}
