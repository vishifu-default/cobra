package org.cobra.core.objects;

import org.cobra.commons.utils.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class BlobByteBuffer {

    private final ByteBuffer[] buffers;
    private final long size;
    private final int shift;
    private final int alignMasking;

    private long offset;

    public BlobByteBuffer(ByteBuffer[] buffers, long size, int shift, int alignMasking) {
        this(buffers, size, shift, alignMasking, 0);
    }

    public BlobByteBuffer(ByteBuffer[] buffers, long size, int shift, int alignMasking, long offset) {
        if (!buffers[0].order().equals(ByteOrder.BIG_ENDIAN))
            throw new UnsupportedOperationException("Only support BIT_ENDIAN order");

        this.buffers = buffers;
        this.size = size;
        this.shift = shift;
        this.alignMasking = alignMasking;
        this.offset = offset;
    }

    public static BlobByteBuffer mmap(FileChannel fc, int singleBufferCapacity) throws IOException {
        long fcSize = fc.size();
        if (fcSize == 0)
            throw new IllegalStateException("FileChannel to be mmap-ed has not data");

        if (!Utils.isLog2(singleBufferCapacity))
            throw new IllegalArgumentException("Single buffer capacity must be  log2 size");

        final int bufferCapacity = fcSize > (long) singleBufferCapacity ? singleBufferCapacity :
                Integer.highestOneBit((int) fcSize);

        long bufferCounter = fcSize % bufferCapacity == 0
                ? fcSize / (long) bufferCapacity
                : (fcSize / (long) bufferCapacity) + 1;

        if (bufferCounter > Integer.MAX_VALUE)
            throw new IllegalArgumentException("File is too large; fc_size: " + fcSize);

        int shift = 31 - Integer.numberOfLeadingZeros(bufferCapacity);
        int masking = (1 << shift) - 1;
        ByteBuffer[] spines = new MappedByteBuffer[(int) bufferCounter];
        for (int i = 0; i < bufferCounter; i++) {
            long pos = (long) i * bufferCapacity;
            int cap = i == (bufferCounter - 1) ? (int) (fcSize - pos) : bufferCapacity;

            ByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, pos, cap);
            spines[i] = buffer;
        }
        return new BlobByteBuffer(spines, fcSize, shift, masking);
    }

    public BlobByteBuffer duplicate() {
        return new BlobByteBuffer(this.buffers, this.size, this.shift, this.alignMasking, this.offset);
    }

    public long position() {
        return this.offset;
    }

    public void position(long pos) {
        if (pos > this.size || pos < 0)
            throw new IllegalArgumentException("Invalid position; position: " + pos + "; capacity: " + this.size);

        this.offset = pos;
    }

    public byte getByte(long pos) {
        if (pos < this.size) {
            int bufferIndex = (int) (pos >>> this.shift);
            int byteIndex = (int) (pos & this.alignMasking);
            return this.buffers[bufferIndex].get(byteIndex);
        } else {
            throw new IllegalArgumentException("Invalid position; position: " + pos + "; capacity: " + this.size);
        }
    }

    public long getLong(long pos) {
        byte[] bytes = new byte[Long.BYTES];
        for (int i = 0; i < Long.BYTES; i++) {
            bytes[i] = getByte(pos + i);
        }

        return ((((long) (bytes[0])) << 56) |
                (((long) (bytes[1] & 0xff)) << 48) |
                (((long) (bytes[2] & 0xff)) << 40) |
                (((long) (bytes[3] & 0xff)) << 32) |
                (((long) (bytes[4] & 0xff)) << 24) |
                (((long) (bytes[5] & 0xff)) << 16) |
                (((long) (bytes[6] & 0xff)) << 8) |
                (((long) (bytes[7] & 0xff))));
    }
}
