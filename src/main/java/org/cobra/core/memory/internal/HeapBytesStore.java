package org.cobra.core.memory.internal;

import org.cobra.commons.Jvm;
import org.cobra.commons.utils.Utils;
import org.cobra.core.memory.BytesStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.cobra.commons.utils.Utils.uncheckedCast;

public final class HeapBytesStore implements BytesStore {

    private static final OSMemory memory = Jvm.osMemory();
    private static final boolean SKIP_ASSERT = Jvm.SKIP_ASSERTION;

    private static final String OFFSET_OUT_OF_BOUND = "offset is out of bound";
    private static final String UNDERLYING_POINTER_NULL = "underlying object is null";

    private static final long HEAP_MAX_LIMIT = 1L << 60;

    @Nullable
    private Object underlying;
    private final Class<?> underlyingClazz;
    private long dataAddr;
    private int capacity;
    private final long limitSize;

    private HeapBytesStore(byte[] arr, long limitSize) {
        assert SKIP_ASSERT || arr != null;

        if (arr == null) {
            throw new NullPointerException(UNDERLYING_POINTER_NULL);
        }

        initiate(arr);
        this.underlyingClazz = arr.getClass();
        this.limitSize = limitSize;
    }

    private HeapBytesStore(@NotNull ByteBuffer buffer, long limitSize) {
        initiate(buffer);
        this.underlyingClazz = buffer.getClass();
        this.limitSize = limitSize;
    }

    public static HeapBytesStore allocate(int size) {
        return allocate(size, HEAP_MAX_LIMIT);
    }

    public static HeapBytesStore allocate(int size, long limitSize) {
        return wrap(new byte[size], limitSize);
    }

    public static HeapBytesStore wrap(byte[] arr) {
        return wrap(arr, HEAP_MAX_LIMIT);
    }

    public static HeapBytesStore wrap(byte[] arr, long limitSize) {
        return new HeapBytesStore(arr, limitSize);
    }

    public static HeapBytesStore wrap(ByteBuffer buffer) {
        return wrap(buffer, HEAP_MAX_LIMIT);
    }

    public static HeapBytesStore wrap(@NotNull ByteBuffer buffer, long limitSize) {
        return new HeapBytesStore(buffer, limitSize);
    }

    @Override
    public void putByte(long offset, byte b) {
        assert SKIP_ASSERT || offset >= 0;
        ensureOffsetInBound(offset);

        memory.writeByte(this.underlying, translateOffset(offset), b);
    }

    @Override
    public void putInt(long offset, int i32) {
        assert SKIP_ASSERT || offset >= 0;
        ensureOffsetInBound(offset + Integer.BYTES - 1);

        memory.writeInt(this.underlying, translateOffset(offset), i32);
    }

    @Override
    public void putLong(long offset, long i64) {
        assert SKIP_ASSERT || offset >= 0;
        ensureOffsetInBound(offset + Long.BYTES - 1);

        memory.writeLong(this.underlying, translateOffset(offset), i64);
    }

    @Override
    public void putBytes(long offset, byte[] src, int srcOffset, int len) {
        assert SKIP_ASSERT || offset >= 0;
        assert SKIP_ASSERT || (srcOffset >= 0 && len >= 0);

        if (len == 0)
            return;

        ensureOffsetInBound(offset + len);
        if ((srcOffset + len) > src.length)
            throw new IllegalArgumentException("Illegal requirement for array source due to out of bound;" +
                    "; require bound [" + srcOffset + ":" + srcOffset + "+" + len + "); array_len=" + src.length);

        memory.copyMemory(src, srcOffset,
                this.underlying, translateOffset(offset) - OSMemory.ARRAY_BYTE_BASE_OFFSET, len);
    }

    @Override
    public byte getByte(long offset) {
        assert SKIP_ASSERT || offset >= 0;
        ensureOffsetInBound(offset);

        return memory.readByte(this.underlying, translateOffset(offset));
    }

    @Override
    public int getInt(long offset) {
        assert SKIP_ASSERT || offset >= 0;
        ensureOffsetInBound(offset);

        return memory.readInt(this.underlying, translateOffset(offset));
    }

    @Override
    public long getLong(long offset) {
        assert SKIP_ASSERT || offset >= 0;
        ensureOffsetInBound(offset);

        return memory.readLong(this.underlying, translateOffset(offset));
    }

    @Override
    public int getBytes(long offset, byte[] dst, int dstOffset, int len) {
        assert SKIP_ASSERT || offset >= 0;
        assert SKIP_ASSERT || (dstOffset >= 0 && len >= 0);

        if (len == 0)
            return 0;

        ensureOffsetInBound(offset + len);
        if ((dstOffset + len) > dst.length)
            throw new IllegalArgumentException("Illegal destination array due to out of bound;" +
                    "; required bound [" + dstOffset + ":" + dstOffset + "+" + len + "); array_len=" + len);

        int reads = (int) Math.min(len, capacity() - offset);
        memory.copyMemory(this.underlying, translateOffset(offset) - OSMemory.ARRAY_BYTE_BASE_OFFSET, dst,
                dstOffset, reads);

        return reads;
    }

    @Override
    public long capacity() {
        return this.capacity;
    }

    @Override
    public void checkResizing(long requiredSize) {
        assert SKIP_ASSERT || requiredSize >= 0;
        if (requiredSize < capacity() || capacity() == this.limitSize)
            return;
        final int toSize = (int) Math.min(alignSize(requiredSize), this.limitSize);
        resize(toSize);
    }

    private void resize(int toSize) {
        byte[] newArr = new byte[toSize];
        if (isUnderlyingByteBuffer()) {
            final ByteBuffer growBuffer = ByteBuffer.wrap(newArr);

            /* copies whole underlying data (all capacity) */
            memory.copyMemory(this.underlying, translateOffset(0) - OSMemory.ARRAY_BYTE_BASE_OFFSET,
                    growBuffer.array(), 0, (int) capacity());

            initiate(growBuffer);
            return;
        }

        memory.copyMemory(this.underlying, translateOffset(0) - OSMemory.ARRAY_BYTE_BASE_OFFSET,
                newArr, 0, (int) capacity());
        initiate(newArr);
    }

    private long translateOffset(long offset) {
        return this.dataAddr + offset;
    }

    private void ensureOffsetInBound(long offset) {
        if (offset >= this.capacity)
            throw new IllegalArgumentException(OFFSET_OUT_OF_BOUND + "; offset=" + offset + ", capacity=" + this.capacity);
    }

    private long alignSize(long size) {
        return Utils.nextPowerOf2(size);
    }

    private boolean isUnderlyingByteBuffer() {
        return this.underlyingClazz.isInstance(ByteBuffer.class);
    }

    private void initiate(byte[] arr) {
        this.underlying = uncheckedCast(arr);
        this.dataAddr = OSMemory.ARRAY_BYTE_BASE_OFFSET;
        this.capacity = arr.length;
    }

    private void initiate(ByteBuffer buffer) {
        buffer.order(ByteOrder.nativeOrder());
        this.underlying = uncheckedCast(buffer.array());
        this.dataAddr = OSMemory.ARRAY_BYTE_BASE_OFFSET + buffer.arrayOffset();
        this.capacity = buffer.capacity();
    }

}
