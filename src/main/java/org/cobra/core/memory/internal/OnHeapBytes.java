package org.cobra.core.memory.internal;

import org.cobra.commons.Jvm;
import org.cobra.commons.pools.BytesPool;
import org.cobra.core.memory.neo.VanillaBytes;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class OnHeapBytes implements VanillaBytes {

    private static final UnsafeMemory memory = Jvm.ofUnsafeMemory();
    private static final boolean SKIP_ASSERT = Jvm.SKIP_ASSERTION;
    private static final int DEFAULT_SEGMENT_LOG2 = 14;
    private static final int MAX_SEGMENT_LOG2 = 30;

    private final int bitshift;
    private final int bitmask;
    private long position;
    private long capacity;
    private byte[][] bytesmap;
    private final BytesPool bytesPool;

    public OnHeapBytes() {
        this(DEFAULT_SEGMENT_LOG2);
    }

    public OnHeapBytes(final int log2OfSegment) {
        this(log2OfSegment, BytesPool.NONE);
    }

    public OnHeapBytes(final int log2OfSegment, final BytesPool bytesPool) {
        if (log2OfSegment > MAX_SEGMENT_LOG2)
            throw new IllegalArgumentException("log2OfSegment > 30, means that segment must allocate more than 1GiB");

        this.bytesPool = bytesPool;
        this.bitshift = log2OfSegment;
        this.bitmask = (1 << log2OfSegment) - 1;

        this.bytesmap = new byte[2][];
        this.bytesmap[0] = bytesPool.allocateArray((1 << log2OfSegment));

        this.capacity = calcCapacity();

        this.position = 0L;
    }

    @Override
    public void write(byte b) {
        write(this.position++, b);
    }

    @Override
    public void write(long pos, byte b) {
        assert SKIP_ASSERT || pos >= 0;

        ensureCapacity(pos);
        final int segmentIndex = (int) (pos >>> this.bitshift);
        final int byteOffset = (int) (pos & this.bitmask);

        memory.writeByte(segment(segmentIndex), translate(byteOffset), b);
    }

    @Override
    public void writeArray(byte[] arr) {
        writeArray(arr, 0, arr.length);
    }

    @Override
    public void writeArray(byte[] arr, int arrOffset, int len) {
        writeArray(this.position, arr, arrOffset, len);
        this.position += len;
    }

    @Override
    public void writeArray(long pos, byte[] arr) {
        writeArray(pos, arr, 0, arr.length);
    }

    @Override
    public void writeArray(long pos, byte[] arr, int arrOffset, int len) {
        assert SKIP_ASSERT || (pos >= 0 && arrOffset >= 0 && len >= 0);

        if (len == 0)
            return;

        if (arrOffset + len > arr.length)
            throw new IllegalArgumentException("Illegal offset/length to copy that over array's length");

        ensureCapacity(pos + len);

        while (len > 0) {
            int segmentIndex = (int) (pos >>> this.bitshift);
            int byteOffset = (int) (pos & this.bitmask);

            int loopCopies = Math.min(len, segment(segmentIndex).length - byteOffset);

            memory.copyMemory(arr, arrOffset, segment(segmentIndex), byteOffset, loopCopies);

            len -= loopCopies;
            arrOffset += loopCopies;
            pos += loopCopies;
        }
    }

    @Override
    public byte read() {
        return read(this.position++);
    }

    @Override
    public byte read(long pos) {
        assert SKIP_ASSERT || pos >= 0;

        checkReadPosition(pos);

        final int segmentIndex = (int) (pos >>> this.bitshift);
        final int byteOffset = (int) (pos & this.bitmask);
        return memory.readByte(segment(segmentIndex), translate(byteOffset));
    }

    @Override
    public byte @NotNull [] readArray(int len) {
        byte[] result = readArray(this.position, len);
        this.position += len;
        return result;
    }

    @Override
    public byte @NotNull [] readArray(long pos, int len) {
        assert SKIP_ASSERT || (pos >= 0 && len >= 0);

        if (len == 0)
            return new byte[0];

        checkReadPosition(pos + len);

        byte[] result = new byte[len];
        int destOffset = 0;

        while (len > 0) {
            int segmentIndex = (int) (pos >>> this.bitshift);
            int byteOffset = (int) (pos & this.bitmask);

            int loopCopies = Math.min(len, segment(segmentIndex).length - byteOffset);

            memory.copyMemory(segment(segmentIndex), byteOffset,
                    result, destOffset, loopCopies);

            len -= loopCopies;
            pos += loopCopies;
            destOffset += loopCopies;
        }

        return result;
    }

    @Override
    public long capacity() {
        return this.capacity;
    }

    @Override
    public long position() {
        return this.position;
    }

    @Override
    public void seek(long pos) {
        if (pos > this.capacity || pos < 0)
            throw new IllegalArgumentException("Illegal position to seek; seek_pos: " + pos + " ;capacity: " + this.capacity);

        this.position = pos;
    }

    @Override
    public void release() {
        for (byte[] bytes : this.bytesmap)
            this.bytesPool.free(bytes);

        this.bytesmap = null;
        this.position = 0L;
        this.capacity = 0L;
    }

    private void ensureCapacity(final long requiredCapacity) {
        if (requiredCapacity < this.capacity)
            return;

        int segmentIndex = (int) (requiredCapacity >>> this.bitshift);
        while (segmentIndex >= this.bytesmap.length)
            this.bytesmap = Arrays.copyOf(this.bytesmap, this.bytesmap.length * 2);

        for (int i = 0; i < this.bytesmap.length; i++) {
            if (this.bytesmap[i] == null)
                this.bytesmap[i] = this.bytesPool.allocateArray(1 << this.bitshift);
        }

        this.capacity = calcCapacity();
    }

    private void checkReadPosition(final long ending) {
        if (ending >= this.capacity)
            throw new IllegalArgumentException("Illegal required read position; ending_pos: " + ending);
    }

    private int translate(int offset) {
        return UnsafeMemory.ARRAY_BYTE_BASE_OFFSET + offset;
    }

    private byte[] segment(int i) {
        return this.bytesmap[i];
    }

    private long calcCapacity() {
        return Arrays.stream(this.bytesmap)
                .filter(Objects::nonNull)
                .mapToInt(x -> x.length)
                .sum();
    }
}
