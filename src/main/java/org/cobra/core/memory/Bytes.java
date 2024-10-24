package org.cobra.core.memory;

/**
 * This interface provide a way to manipulate an array of bytes, served by a "cursor".
 * We can use the cursor the read at some position or just get it current position.
 */
public interface Bytes {

    long MAX_CAPACITY = (1L << 62);

    /**
     * Write a single byte
     *
     * @param b byte
     */
    void write(byte b);

    /**
     * Write a single byte into destination at given position
     *
     * @param b      byte
     * @param offset destination writing position
     */
    void write(byte b, long offset);

    /**
     * Write an array of byte
     *
     * @param arr bytes array
     */
    void copyMemory(byte[] arr);

    /**
     * Write an array of bytes to destination at a given position
     *
     * @param arr  source array of byte
     * @param offset destination start position
     */
    void copyMemory(byte[] arr, long offset);

    /**
     * Read a single byte at the current position
     *
     * @return read byte
     */
    byte read();

    /**
     * Read a single byte at the given position
     *
     * @param offset position to read
     * @return read byte
     */
    byte read(long offset);

    /**
     * Read an array of bytes from current cursor position
     *
     * @param len number of bytes
     * @return array of byte
     */
    byte[] readBlock(int len);

    /**
     * Read an array number of byte from given position
     *
     * @param offset start position to read
     * @param len    number of bytes to read
     * @return an array of bytes
     */
    byte[] readBlock(int len, long offset);

    /**
     * @return current position of cursor
     */
    long position();

    /**
     * Move cursor to given position
     *
     * @param offset position to set
     */
    void seek(long offset);

    /**
     * Move cursor to the end of memory
     */
    void seekEnd();

    /**
     * Clear all existing data
     */
    void clear();
}
