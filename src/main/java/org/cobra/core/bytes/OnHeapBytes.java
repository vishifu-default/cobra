package org.cobra.core.bytes;

import org.cobra.commons.pools.BytesPool;

public class OnHeapBytes extends VanillaBytes {

    protected OnHeapBytes(BytesStore store) {
        super(store);
    }

    public static OnHeapBytes create(BytesStore store) {
        return new OnHeapBytes(store);
    }

    public static OnHeapBytes create() {
        return create(OnHeapBytesStore.create());
    }

    public static OnHeapBytes create(BytesPool bytesPool) {
        return create(OnHeapBytesStore.create(bytesPool));
    }

    public static OnHeapBytes createLog2Align(int log2) {
        return create(OnHeapBytesStore.create(log2));
    }

    public static OnHeapBytes createLog2Align(int log2, BytesPool bytesPool) {
        return create(OnHeapBytesStore.create(log2, bytesPool));
    }
}
