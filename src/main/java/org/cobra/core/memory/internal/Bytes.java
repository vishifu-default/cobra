package org.cobra.core.memory.internal;

import org.cobra.core.memory.RandomBytes;

import java.nio.ByteBuffer;

public interface Bytes extends RandomBytes {
    void write(byte[] arr);

    void write(byte[] arr, int off, int len);

    void write(ByteBuffer buffer);

    int read(byte[] arr);

    int read(byte[] arr, int off, int len);

    int read(ByteBuffer buffer);
}
