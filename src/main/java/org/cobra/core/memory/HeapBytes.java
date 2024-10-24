package org.cobra.core.memory;

import org.cobra.core.memory.internal.SegmentedBytesMemory;

/**
 * Provides simple ways to manipulate on-heap bytes with offset, we can seek to any offset and read its value.
 * This is backed by an underlying {@link SegmentedBytesMemory}
 */
public class HeapBytes implements Bytes {

    private static final int DEFAULT_LOG2_SEGMENT_ALIGN = 14;

    private final SegmentedBytesMemory segmentMemory;
    private long position = 0L;

    public HeapBytes(int log2SegmentAlign) {
        this.segmentMemory = new SegmentedBytesMemory(log2SegmentAlign);
    }

    public HeapBytes() {
        this(DEFAULT_LOG2_SEGMENT_ALIGN);
    }

    @Override
    public void write(byte b) {
        this.segmentMemory.write(position++, b);
    }

    @Override
    public void write(byte b, long offset) {
        this.segmentMemory.write(offset, b);
    }

    @Override
    public void copyMemory(byte[] arr) {
        this.segmentMemory.copyMemory(arr, 0, position, arr.length);
        this.position += arr.length;
    }

    @Override
    public void copyMemory(byte[] arr, long offset) {
        this.segmentMemory.copyMemory(arr, 0, offset, arr.length);
    }

    @Override
    public byte read() {
        return this.segmentMemory.read(position);
    }

    @Override
    public byte read(long offset) {
        return this.segmentMemory.read(offset);
    }

    @Override
    public byte[] readBlock(int len) {
        return readBlock(len, this.position);
    }

    @Override
    public byte[] readBlock(int len, long offset) {
        byte[] result = new byte[len];
        this.segmentMemory.copyMemory(offset, result, 0, result.length);

        return result;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public void seek(long offset) {
        this.position = offset;
    }

    @Override
    public void seekEnd() {
        seek(this.segmentMemory.size());
    }

    @Override
    public void clear() {
        this.segmentMemory.free();
        this.position = 0L;
    }
}
