package org.cobra.core.bytes;

import java.nio.ByteBuffer;

/**
 * Provides a way to manipulate bytes memory in which we can access to any position, as if it is in bound
 */
public interface RandomBytes {

    /**
     * Writes a byte at given position
     *
     * @param pos memory position
     * @param b   byte value
     */
    void writeAt(long pos, byte b);

    /**
     * Writes a byte array at given memory position
     *
     * @param pos   given memory position
     * @param array source byte array
     */
    void writeAt(long pos, byte[] array);

    /**
     * Writes a byte array at given position
     *
     * @param pos    memory position
     * @param array  byte array
     * @param offset offset starting of byte array
     * @param len    number of bytes to write from array
     */
    void writeAt(long pos, byte[] array, int offset, int len);

    /**
     * Writes a byte buffer at given memory position
     *
     * @param pos    memory position
     * @param buffer source byte buffer to write from, begin at current buffer position till fulfill remaining
     */
    void writeAt(long pos, ByteBuffer buffer);

    /**
     * Writes a buffer at the given position
     *
     * @param pos    memory position
     * @param buffer ByteBuffer to write from
     * @param offset offset starting to write from buffer
     * @param len    number of bytes to write from buffer
     */
    void writeAt(long pos, ByteBuffer buffer, int offset, int len);

    /**
     * Reads a byte value from given position
     *
     * @param pos given memory position
     * @return byte value
     */
    byte readAt(long pos);

    /**
     * Reads bytes memory into an array of byte
     *
     * @param pos  given memory position
     * @param dest destination of byte array
     * @return number of bytes that actually read
     */
    int readAt(long pos, byte[] dest);

    /**
     * Reads bytes memory at given position into an array of byte
     *
     * @param pos    given memory position
     * @param dest   destination byte array
     * @param offset offset starting to write in destination
     * @param len    number of bytes to read
     * @return number of bytes that actually read
     */
    int readAt(long pos, byte[] dest, int offset, int len);

    /**
     * Reads bytes memory at given memory position into a byte buffer
     *
     * @param pos  given memory position
     * @param dest byte buffer to read into, starting from current position till fulfill remaining
     * @return number of bytes that actually read
     */
    int readAt(long pos, ByteBuffer dest);

    /**
     * Reads byte memory at given memory position into a byte buffer
     *
     * @param pos    given memory position
     * @param dest   byte buffer to read into
     * @param offset starting offset of byte buffer
     * @param len    number should read into buffer
     * @return number of bytes that actually read
     */
    int readAt(long pos, ByteBuffer dest, int offset, int len);
}
