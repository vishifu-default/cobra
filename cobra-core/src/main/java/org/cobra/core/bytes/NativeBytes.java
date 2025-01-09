package org.cobra.core.bytes;

public class NativeBytes extends VanillaBytes {

    protected NativeBytes(BytesStore store) {
        super(store);
    }

    public static NativeBytes create() {
        return new NativeBytes(NativeBytesStore.create());
    }

    public static NativeBytes createLog2Align(int log2) {
        return new NativeBytes(NativeBytesStore.create(log2));
    }
}
