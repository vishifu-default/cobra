package org.cobra.core.memory.internal;

import org.cobra.core.memory.BytesStore;

public class HeapBytes extends VanillaBytes {

    protected HeapBytes(BytesStore bytesStore) {
        super(bytesStore);
    }

    public static HeapBytes of(int initSize) {
        return new HeapBytes(HeapBytesStore.allocate(initSize));
    }

    public static HeapBytes of(int initSize, long limit) {
        return new HeapBytes(HeapBytesStore.allocate(initSize, limit));
    }
}
