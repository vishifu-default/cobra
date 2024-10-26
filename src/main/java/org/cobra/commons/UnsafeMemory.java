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

    private static final int ARRAY_BASE_OFFSET = Unsafe.ARRAY_BYTE_BASE_OFFSET;

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

    public void unsafePutByte(@NotNull Object object, long offset, byte b) {
        UNSAFE.putByte(object,  Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, b);
    }

    public byte unsafeGetByte(@NotNull Object object, long offset) {
        return UNSAFE.getByte(object, ARRAY_BASE_OFFSET + offset);
    }

    public void unsafePutBytes(@NotNull Object object, long offset, byte[] src) {
        UNSAFE.copyMemory(src, ARRAY_BASE_OFFSET,
                object, ARRAY_BASE_OFFSET + offset, src.length);
    }

    public void unsafePutBytes(@NotNull Object object, long offset, byte[] src, int srcOffset, int len) {
        UNSAFE.copyMemory(src, ARRAY_BASE_OFFSET + srcOffset,
                object, ARRAY_BASE_OFFSET + offset, len);
    }

    public byte[] unsafeGetBytes(@NotNull Object object, long offset, int len) {
        byte[] result = new byte[len];
        UNSAFE.copyMemory(object, ARRAY_BASE_OFFSET + offset, result, ARRAY_BASE_OFFSET, len);
        return result;
    }

    public void putByteVolatile(Object object, long offset, byte b) {
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
