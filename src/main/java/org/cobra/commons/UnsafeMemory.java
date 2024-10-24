package org.cobra.commons;

import org.cobra.commons.errors.CobraException;
import org.cobra.commons.utils.Utils;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeMemory {

    public static final int UNSAFE_MEMORY_THRESHOLD = 1024 * 1024;

    private static final Unsafe UNSAFE;
    public static final UnsafeMemory INSTANCE;

    static {
        Field theUnsafe;
        try {
            theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
            INSTANCE = new UnsafeMemory();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new CobraException(e);
        }
    }

    public int pageSize() {
        return UNSAFE.pageSize();
    }

    public void putByteVolatile(Object object,long offset, byte b) {
        UNSAFE.putByteVolatile(object, offset, b);
    }

    public void copyMemory(@NotNull Object src, int srcOffset, @NotNull Object dest, int destOffset, int len) {
        copyMemory0(src, srcOffset, dest, destOffset, len);
    }

    public void copyMemoryOrdered(byte[] src, int srcOffset, byte[] dest, int destOffset, int len) {
        copyMemoryOrdered0(src, srcOffset, dest, destOffset, len);
    }

    private static void copyMemory0(@NotNull Object src, int srcOffset, @NotNull Object dest, int destOffset, int len) {
        while (len > 0) {
            int toCopies = Utils.min(len, UNSAFE_MEMORY_THRESHOLD);
            UNSAFE.copyMemory(src, srcOffset, dest, destOffset, toCopies);
            len -= toCopies;
            srcOffset += toCopies;
            destOffset += toCopies;
        }
    }

    private static void copyMemoryOrdered0(byte[] src, int srcOffset, byte[] dest, int destOffset, int len) {
        int i = 0;
        for (; i < len - 15; i += 16) {
            long a = UNSAFE.getLongVolatile(src, srcOffset + i);
            long b = UNSAFE.getLongVolatile(dest, destOffset + i + 8);
            UNSAFE.putLongVolatile(dest, destOffset + i, a);
            UNSAFE.putLongVolatile(dest, destOffset + i + 8, b);
        }
        for (; i < len - 3; i += 4) {
            int i32 = UNSAFE.getIntVolatile(src, srcOffset + i);
            UNSAFE.putIntVolatile(dest, destOffset + i, i32);
        }
        for (; i < len; i++) {
            byte b = UNSAFE.getByteVolatile(src, srcOffset + i);
            UNSAFE.putByteVolatile(dest, destOffset + i, b);
        }
    }
}
