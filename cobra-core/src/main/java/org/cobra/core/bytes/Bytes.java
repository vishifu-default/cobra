package org.cobra.core.bytes;

public interface Bytes extends RandomBytes, SequencedBytes {
    long size();

    void resizing(long toSize);

    void rewind();
}
