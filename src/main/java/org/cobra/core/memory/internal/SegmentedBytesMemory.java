package org.cobra.core.memory.internal;

import org.cobra.commons.Jvm;
import org.cobra.commons.pools.BytesPool;
import sun.misc.Unsafe;

import java.util.Arrays;

/**
 * Provides a segmented byte array that can is satisfied to allocate more than Integer.MAX_VALUE as sequenced bytes.
 * <p>
 * Bytes are aligned as segment with a specified value, when reach a segment index that is out bound, we resize the
 * number of segment by 50% until number of segments is over index
 */
public class SegmentedBytesMemory {

    private byte[][] segments;
    private final int log2Alignment;
    private final int alignBitmask;
    private final BytesPool bytesPool = BytesPool.NONE;

    public SegmentedBytesMemory(int log2SegmentAlignment) {
        this.log2Alignment = log2SegmentAlignment;
        this.alignBitmask = (1 << this.log2Alignment) - 1;
        this.segments = new byte[2][];
    }

    public long size() {
        long size = 0;
        for (byte[] segment : segments)
            if (segment != null) size += segment.length;

        return size;
    }

    /**
     * Writes a byte value into segmented heap bytes
     *
     * @param offset absolute position to write
     * @param b      byte value
     */
    public void write(long offset, byte b) {
        int whichSegment = (int) (offset >>> log2Alignment);
        checkResizing(whichSegment);
        int whichByte = (int) (offset & alignBitmask);

        segments[whichSegment][whichByte] = b;
    }

    /**
     * Reads a byte value from the absolute offset of segmented bytes
     *
     * @param offset position to read
     * @return byte value at offset
     */
    public byte read(long offset) {
        int whichSegment = (int) (offset >> log2Alignment);
        int whichByte = (int) (offset & alignBitmask);
        return segments[whichSegment][whichByte];
    }

    /**
     * Copies a byte array into the given segmented byte array
     *
     * @param src       source byte array
     * @param srcOffset starting offset of byte array
     * @param offset    starting offset of destination
     * @param len       number of bytes to copy
     */
    public void copyMemory(byte[] src, int srcOffset, long offset, int len) {
        int segmentSize = 1 << log2Alignment;
        while (len > 0) {
            int whichSegment = (int) (offset >>> log2Alignment);
            int whichByte = (int) (offset & alignBitmask);
            int remainingSegmentSize = segmentSize - whichByte;
            int toCopies = Math.min(len, remainingSegmentSize);
            checkResizing(whichSegment);
            Jvm.memory().copyMemory(src, srcOffset + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                    segments[whichSegment], whichByte + Unsafe.ARRAY_BYTE_BASE_OFFSET, toCopies);

            len -= toCopies;
            srcOffset += toCopies;
            offset += toCopies;
        }
    }

    public void orderedCopyMemory(byte[] src, int srcOffset, long offset, int len) {
        int segmentSize = 1 << log2Alignment;
        while (len > 0) {
            int whichSegment = (int) (offset >>> log2Alignment);
            int whichByte = (int) (offset & alignBitmask);
            int remainingSegmentSize = segmentSize - whichByte;
            int toCopies = Math.min(len, remainingSegmentSize);
            checkResizing(whichSegment);
            copyMemoryVolatile0(src, srcOffset, segments[whichSegment], whichByte, toCopies);

            len -= toCopies;
            srcOffset += toCopies;
            offset += toCopies;
        }
    }

    /**
     * Copies the source memory into the given byte array destination
     *
     * @param srcOffset  offset starting to copy
     * @param dest       destination byte array
     * @param destOffset offset starting to write
     * @param len        number of bytes to copy
     */
    public void copyMemory(long srcOffset, byte[] dest, int destOffset, int len) {
        int segmentSize = 1 << log2Alignment;
        while (len > 0) {
            int whichSegment = (int) (srcOffset >>> log2Alignment);
            int whichByte = (int) (srcOffset & alignBitmask);
            int remainingSegmentSize = segmentSize - whichByte;
            int toCopies = Math.min(len, remainingSegmentSize);
            checkResizing(whichSegment);

            Jvm.memory().copyMemory(segments[whichSegment], (whichByte + Unsafe.ARRAY_BYTE_BASE_OFFSET),
                    dest, destOffset + Unsafe.ARRAY_BYTE_BASE_OFFSET, len);
            len -= toCopies;
            srcOffset += toCopies;
            destOffset += toCopies;
        }
    }

    private void copyMemoryVolatile0(byte[] src, int srcOffset, byte[] dest, int destOffset, int len) {
        int endOffset = srcOffset + len;
        destOffset += Unsafe.ARRAY_BYTE_BASE_OFFSET;

        while (srcOffset < endOffset) {
            Jvm.memory().putByteVolatile(dest, destOffset++, src[srcOffset++]);
        }

    }

    /**
     * Ensure that segment index is not out of bound, grow segments 2nd-size if needed.
     *
     * @param index requested segment index
     */
    private void checkResizing(int index) {
        while (index >= segments.length)
            segments = Arrays.copyOf(segments, segments.length * 3 / 2);

        if (segments[index] == null)
            segments[index] = bytesPool.allocateArray(1 << log2Alignment);
    }

    /**
     * Free all segment byte arrays
     */
    public void free() {
        for (byte[] segment : segments) {
            if (segment != null)
                bytesPool.free(segment);
        }
    }
}
