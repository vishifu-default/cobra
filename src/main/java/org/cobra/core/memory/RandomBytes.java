package org.cobra.core.memory;

import java.nio.ByteBuffer;

/**
 * This interface provide a way to manipulate an array of bytes, served by a "cursor".
 * We can use the cursor the read at some position or just get it current position.
 */
public interface RandomBytes {
    void write(long pos, byte[] array);

    void write(long pos, byte[] array, int offset, int len);

    void write(long pos, ByteBuffer buffer);

    int read(long pos, byte[] array);

    int read(long pos, byte[] array, int offset, int len);

    int read(long pos, ByteBuffer buffer);

    long capacity();
}
