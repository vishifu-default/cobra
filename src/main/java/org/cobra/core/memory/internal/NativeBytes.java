package org.cobra.core.memory.internal;

import org.cobra.core.memory.BytesStore;

public class NativeBytes extends VanillaBytes {

    protected NativeBytes(BytesStore bytesStore) {
        super(bytesStore);
    }

    public static NativeBytes create(long initSize) {
        return new NativeBytes(NativeBytesStore.allocate(initSize));
    }

    public static NativeBytes create(long initSize, long limit) {
        return new NativeBytes(NativeBytesStore.allocate(initSize, limit));
    }
}
