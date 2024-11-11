package org.cobra.core.memory;

import org.cobra.commons.Jvm;
import org.cobra.commons.errors.CobraException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class OSMemory {

    private final static sun.misc.Unsafe UNSAFE;
    public final static OSMemory MEMORY;

    public static final int ARRAY_BYTE_BASE_OFFSET = sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;
    private static final boolean SKIP_ASSERT = Jvm.SKIP_ASSERTION;
    private static final int UNSAFE_IO_THRESHOLD = Jvm.DEFAULT_MEMORY_THRESHOLD;

    private final AtomicLong nativeMemUsed = new AtomicLong(0);

    /* Prevent construct outside */
    OSMemory() {
    }

    /* static construct for Unsafe and Instance */
    static {
        Field sunUnsafe;
        try {
            sunUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            sunUnsafe.setAccessible(true);
            UNSAFE = (sun.misc.Unsafe) sunUnsafe.get(null);
            MEMORY = new OSMemory();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new CobraException(e);
        }
    }

    public int pageSize() {
        return UNSAFE.pageSize();
    }

    public long nativeMemoryUsed() {
        return this.nativeMemUsed.get();
    }

    public void freeMemory(long address, long size) {
        assert SKIP_ASSERT || size >= 0;
        if (address != 0)
            UNSAFE.freeMemory(address);

        this.nativeMemUsed.addAndGet(-size);
    }

    public void setMemory(long address, long size, byte b) {
        assert SKIP_ASSERT || size >= 0;
        UNSAFE.setMemory(address, size, b);
    }

    public void setMemory(@NotNull Object object, long offset, long len, byte b) {
        assert SKIP_ASSERT || offset >= 0;
        UNSAFE.setMemory(object, offset, len, b);
    }

    public long allocate(long size) {
        if (size <= 0)
            throw new IllegalArgumentException("Illegal required size: " + size);

        long address = UNSAFE.allocateMemory(size);
        if (address == 0)
            throw new OutOfMemoryError("Not enough memory; required size: " + size / 1024 + "KiB");

        this.nativeMemUsed.addAndGet(size);

        return address;
    }

    public long addressOf(final ByteBuffer bb) {
        Objects.requireNonNull(bb);
        return ((sun.nio.ch.DirectBuffer) bb).address();
    }

    public void writeByte(long address, byte i8) {
        assert SKIP_ASSERT || address > 0;
        UNSAFE.putByte(address, i8);
    }

    public void writeByte(Object object, long offset, byte i8) {
        assert SKIP_ASSERT || offset >= 0;
        UNSAFE.putByte(object, offset, i8);
    }

    public void writeByteVolatile(Object object, long offset, byte i8) {
        assert SKIP_ASSERT || offset >= 0;
        UNSAFE.putByteVolatile(object, offset, i8);
    }

    public byte readByte(long address) {
        assert SKIP_ASSERT || address > 0;
        return UNSAFE.getByte(address);
    }

    public byte readByte(Object object, long offset) {
        assert SKIP_ASSERT || offset >= 0;
        return UNSAFE.getByte(object, offset);
    }

    public byte readByteVolatile(Object object, long offset) {
        assert SKIP_ASSERT || offset >= 0;
        return UNSAFE.getByteVolatile(object, offset);
    }

    public void writeInt(long address, int i32) {
        assert SKIP_ASSERT || address > 0;
        UNSAFE.putInt(address, i32);
    }

    public void writeInt(Object object, long offset, int i32) {
        assert SKIP_ASSERT || offset >= 0;
        UNSAFE.putInt(object, offset, i32);
    }

    public void writeIntVolatile(Object object, long offset, int i32) {
        assert SKIP_ASSERT || offset >= 0;
        UNSAFE.putIntVolatile(object, offset, i32);
    }

    public int readInt(long address) {
        assert SKIP_ASSERT || address > 0;
        return UNSAFE.getInt(address);
    }

    public int readInt(Object object, long offset) {
        assert SKIP_ASSERT || offset >= 0;
        return UNSAFE.getInt(object, offset);
    }

    public int readIntVolatile(Object object, long offset) {
        assert SKIP_ASSERT || offset >= 0;
        return UNSAFE.getIntVolatile(object, offset);
    }

    public void writeLong(long address, long i64) {
        assert SKIP_ASSERT || address > 0;
        UNSAFE.putLong(address, i64);
    }

    public void writeLong(Object object, long offset, long i64) {
        assert SKIP_ASSERT || offset >= 0;
        UNSAFE.putLong(object, offset, i64);
    }

    public void writeLongVolatile(Object object, long offset, long i64) {
        assert SKIP_ASSERT || offset >= 0;
        UNSAFE.putLongVolatile(object, offset, i64);
    }

    public long readLong(long address) {
        assert SKIP_ASSERT || address > 0;
        return UNSAFE.getLong(address);
    }

    public long readLong(Object object, long offset) {
        assert SKIP_ASSERT || offset >= 0;
        return UNSAFE.getLong(object, offset);
    }

    public long readLongVolatile(Object object, long offset) {
        assert SKIP_ASSERT || offset >= 0;
        return UNSAFE.getLongVolatile(object, offset);
    }

    public void copyMemory(long address, byte[] arr, int arrOffset, int len) {
        assert SKIP_ASSERT || address > 0;
        assert SKIP_ASSERT || arrOffset >= 0 && len >= 0;

        if (arrOffset + len > arr.length)
            throw new IllegalArgumentException("Invalid offset/len; array's length: " + arr.length);

        if (len == 0)
            return;

        UNSAFE.copyMemory(arr, ARRAY_BYTE_BASE_OFFSET + arrOffset, null, address, len);
    }

    public void copyMemory(byte[] src, int srcOffset, byte[] dest, int destOffset, int len) {
        assert SKIP_ASSERT || (srcOffset >= 0 && destOffset >= 0 && len > 0);
        assert SKIP_ASSERT || (src != null && dest != null);
        final long srcArrOffset = ARRAY_BYTE_BASE_OFFSET + srcOffset;
        final long destArrOffset = ARRAY_BYTE_BASE_OFFSET + destOffset;
        if (len < UNSAFE_IO_THRESHOLD)
            UNSAFE.copyMemory(src, srcArrOffset, dest, destArrOffset, len);
        else
            copyMemory0(src, srcArrOffset, dest, destArrOffset, len);
    }

    public void copyMemory(@Nullable Object src, long srcOffset, @Nullable Object dest, long destOffset, int len) {
        assert SKIP_ASSERT || !(src == null && dest == null);
        assert SKIP_ASSERT || srcOffset >= 0 && destOffset >= 0 && len >= 0;
        if (len == 0)
            return;

        if (src instanceof byte[]) {
            if (dest instanceof byte[])
                copyMemory((byte[]) src, Math.toIntExact(srcOffset),
                        (byte[]) dest, Math.toIntExact(destOffset), len);
            else
                copyMemoryLoops(src, ARRAY_BYTE_BASE_OFFSET + Math.toIntExact(srcOffset), dest, destOffset, len);
            return;
        }

        if (src == null) {
            if (dest == null)
                copyMemory(srcOffset, destOffset, len);
            else
                copyMemoryLoops(null, srcOffset, dest, destOffset, len);
            return;
        }

        copyMemoryLoops(src, srcOffset, dest, destOffset, len);
    }

    public void copyMemory(long srcAddress, long destAddress, int len) {
        if (len < UNSAFE_IO_THRESHOLD)
            UNSAFE.copyMemory(srcAddress, destAddress, len);
        else
            copyMemory0(null, srcAddress, null, destAddress, len);
    }

    private void copyMemoryLoops(Object src, long srcOffset, Object dest, long destOffset, int len) {
        assert SKIP_ASSERT || srcOffset >= 0 && destOffset >= 0 && len >= 0;

        if (src == dest && srcOffset < destOffset) {
            copyMemoryLoopBacks(src, srcOffset, destOffset, len);
            return;
        }

        int i = 0;
        for (; i < len - 15; i += 16) {
            long a = UNSAFE.getLong(src, srcOffset + i);
            long b = UNSAFE.getLong(dest, destOffset + i + 8);
            UNSAFE.putLong(dest, destOffset + i, a);
            UNSAFE.putLong(dest, destOffset + i + 8, b);
        }
        for (; i < len - 3; i += 4) {
            int i32 = UNSAFE.getInt(src, srcOffset + i);
            UNSAFE.putInt(dest, destOffset + i, i32);
        }
        for (; i < len; i++) {
            byte b = UNSAFE.getByte(src, srcOffset + i);
            UNSAFE.putByte(dest, destOffset + i, b);
        }
    }

    private void copyMemoryLoopBacks(Object target, long srcOffset, long destOffset, int len) {
        assert SKIP_ASSERT || (srcOffset >= 0 && destOffset >= 0 && len >= 0);
        srcOffset += len;
        destOffset += len;
        int i = 0;
        for (; i < len - 7; i += 8) {
            UNSAFE.putLong(target, destOffset - 8 - i, UNSAFE.getLong(target, srcOffset - 8 - i));
        }
        for (; i < len; i++) {
            UNSAFE.putByte(target, destOffset - 1 - i, UNSAFE.getByte(target, destOffset - 1 - i));
        }
    }

    private void copyMemory0(@Nullable Object src, long srcOffset, @Nullable Object dest, long destOffset, int len) {
        assert SKIP_ASSERT || srcOffset >= 0 && destOffset >= 0 && len >= 0;

        while (len > 0) {
            final long loopSize = Math.min(len, UNSAFE_IO_THRESHOLD);
            UNSAFE.copyMemory(src, srcOffset, dest, destOffset, loopSize);
            len -= (int) loopSize;
            srcOffset += loopSize;
            destOffset += loopSize;
        }
    }
}
