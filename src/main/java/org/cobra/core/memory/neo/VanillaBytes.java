package org.cobra.core.memory.neo;

import org.jetbrains.annotations.NotNull;

public interface VanillaBytes {

    void write(byte b);

    void write(long pos, byte b);

    void writeArray(byte[] arr);

    void writeArray(byte[] arr, int arrOffset, int len);

    void writeArray(long pos, byte[] arr);

    void writeArray(long pos, byte[] arr, int arrOffset, int len);

    byte read();

    byte read(long pos);

    byte @NotNull [] readArray(int len);

    byte @NotNull [] readArray(long pos, int len);

    long capacity();

    long position();

    void seek(long pos);

    void release();
}
