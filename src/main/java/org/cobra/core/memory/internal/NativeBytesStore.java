package org.cobra.core.memory.internal;

import org.cobra.commons.Jvm;
import org.cobra.commons.utils.Utils;
import org.cobra.core.memory.BytesStore;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public final class NativeBytesStore implements BytesStore {

    private static final boolean SKIP_ASSERT = Jvm.SKIP_ASSERTION;
    private static final long NATIVE_MAX_LIMIT = 1L << 60;

    private static final String OFFSET_OUT_OF_BOUND = "Offset is out of bound";

    private long capacity;
    private long address;
    private final long limitSize;

    private NativeBytesStore(@NotNull ByteBuffer buffer, long limitSize) {
        this(Jvm.osMemory().addressOf(buffer), buffer.capacity(), limitSize);
    }

    private NativeBytesStore(long address, long initCapacity, long limitSize) {
        this.address = address;
        this.capacity = initCapacity;
        this.limitSize = limitSize;
    }

    public static NativeBytesStore wrap(@NotNull ByteBuffer buffer) {
        return wrap(buffer, NATIVE_MAX_LIMIT);
    }

    public static NativeBytesStore wrap(@NotNull ByteBuffer buffer, long limitSize) {
        return new NativeBytesStore(buffer, limitSize);
    }

    public static NativeBytesStore allocate(long size) {
        return allocate(size, NATIVE_MAX_LIMIT);
    }

    public static NativeBytesStore allocate(long size, long limitSize) {
        long allocAddress = Jvm.osMemory().allocate(size);
        return new NativeBytesStore(allocAddress, size, limitSize);
    }

    private long translate(long offset) {
        return this.address + offset;
    }

    private void ensureOffsetNotOutOfBound(long offset) {
        if (offset >= this.capacity)
            throw new IllegalArgumentException(OFFSET_OUT_OF_BOUND + "; offset=" + offset + "; capacity=" + this.capacity);
    }

    @Override
    public void putByte(long offset, byte b) {
        assert SKIP_ASSERT || offset >= 0;
        ensureOffsetNotOutOfBound(offset);

        Jvm.osMemory().writeByte(translate(offset), b);
    }

    @Override
    public void putInt(long offset, int i32) {
        assert SKIP_ASSERT || offset >= 0;
        ensureOffsetNotOutOfBound(offset + Integer.BYTES - 1);

        Jvm.osMemory().writeInt(translate(offset), i32);
    }

    @Override
    public void putLong(long offset, long i64) {
        assert SKIP_ASSERT || offset >= 0;
        ensureOffsetNotOutOfBound(offset + Long.BYTES - 1);

        Jvm.osMemory().writeLong(translate(offset), i64);
    }

    @Override
    public void putBytes(long offset, byte[] src, int srcOffset, int len) {
        assert SKIP_ASSERT || offset >= 0;
        assert SKIP_ASSERT || (srcOffset >= 0 && len >= 0);

        if (len == 0)
            return;

        if (srcOffset + len > src.length)
            throw new IllegalArgumentException(OFFSET_OUT_OF_BOUND + "; offset=" + offset + "; len=" + len);

        Jvm.osMemory().copyMemory(src, srcOffset, null, translate(offset), len);
    }

    @Override
    public byte getByte(long offset) {
        assert SKIP_ASSERT || offset >= 0;
        ensureOffsetNotOutOfBound(offset);

        return Jvm.osMemory().readByte(translate(offset));
    }

    @Override
    public int getInt(long offset) {
        assert SKIP_ASSERT || offset >= 0;
        ensureOffsetNotOutOfBound(offset);

        return Jvm.osMemory().readInt(translate(offset));
    }

    @Override
    public long getLong(long offset) {
        assert SKIP_ASSERT || offset >= 0;
        ensureOffsetNotOutOfBound(offset);

        return Jvm.osMemory().readLong(translate(offset));
    }

    @Override
    public int getBytes(long offset, byte[] dst, int dstOffset, int len) {
        assert SKIP_ASSERT || offset >= 0;
        assert SKIP_ASSERT || (dstOffset >= 0 && len >= 0);

        if (len == 0)
            return 0;

        if (dstOffset + len > dst.length)
            throw new IllegalArgumentException(OFFSET_OUT_OF_BOUND + "; offset=" + offset + "; len=" + len);

        final int reads = (int) Math.min(len, capacity() - offset);

        Jvm.osMemory().copyMemory(null, translate(offset), dst,
                OSMemory.ARRAY_BYTE_BASE_OFFSET + dstOffset, reads);
        return reads;
    }

    @Override
    public long capacity() {
        return this.capacity;
    }

    @Override
    public void checkResizing(long requiredSize) {
        assert SKIP_ASSERT || requiredSize > 0;
        if (requiredSize < capacity() || this.limitSize == capacity())
            return;

        final long toSize = Math.min(alignSize(requiredSize), this.limitSize);
        resize(toSize);
    }

    private void resize(long toSize) {
        final long newAddress = Jvm.osMemory().allocate(toSize);
        moveMemory0(this.address, newAddress, capacity());

        Jvm.osMemory().freeMemory(this.address, capacity());

        this.address = newAddress;
        this.capacity = toSize;
    }

    private void moveMemory0(long srcAddr, long dstAddr, long len) {
        while (len > 0) {
            final int copies = (int) Math.min(len, Jvm.DEFAULT_MEMORY_THRESHOLD);
            Jvm.osMemory().copyMemory(srcAddr, dstAddr, copies);

            len -= copies;
            srcAddr += copies;
            dstAddr += copies;
        }
    }

    private long alignSize(long size) {
        return Utils.nextPowerOf2(size);
    }
}
