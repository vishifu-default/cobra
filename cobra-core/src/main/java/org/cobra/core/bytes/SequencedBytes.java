package org.cobra.core.bytes;

import java.nio.ByteBuffer;

/**
 * Provides a way to manipulate bytes memory in which use a `cursor` to read byte in order.
 * <p>
 * All provided methods, implicitly, write/read data at the current `cursor` position
 */
public interface SequencedBytes {

    /**
     * Writes a byte value
     *
     * @param b byte value
     */
    void write(byte b);

    /**
     * Writes from a byte array
     *
     * @param array byte array, write full of array (0 -> len)
     */
    void write(byte[] array);

    /**
     * Writes from a byte array
     *
     * @param array  byte array
     * @param offset offset starting to write from byte array
     * @param len    number of bytes need to write
     */
    void write(byte[] array, int offset, int len);

    /**
     * Writes a byte buffer
     *
     * @param buffer byte buffer, write from current buffer position till fulfilling remaining
     */
    void write(ByteBuffer buffer);

    /**
     * Writes a byte buffer
     *
     * @param buffer byte buffer
     * @param offset offset starting to write from byte buffer
     * @param len    number of byte need to write
     */
    void write(ByteBuffer buffer, int offset, int len);

    /**
     * Reads a byte value
     *
     * @return byte value
     */
    byte read();

    /**
     * Reads bytes into a destination byte array
     *
     * @param array destination byte array, read until fulfilling array (0 -> len)
     * @return number of bytes that actually read
     */
    int read(byte[] array);

    /**
     * Reads bytes into a destination byte array
     *
     * @param array  destination byte array
     * @param offset offset starting to write at array
     * @param len    number of bytes that actually read
     */
    int read(byte[] array, int offset, int len);

    /**
     * Reads bytes into a byte buffer
     *
     * @param buffer byte buffer, read until fulfilling buffer
     */
    int read(ByteBuffer buffer);

    /**
     * Reads bytes into a byte buffer
     *
     * @param buffer byte buffer
     * @param offset offset starting to write at buffer
     * @param len    number of bytes that actually read
     */
    int read(ByteBuffer buffer, int offset, int len);

    /**
     * @return the current position of cursor
     */
    long position();

    /**
     * Seeks cursor to given position
     */
    void position(long pos);
}
