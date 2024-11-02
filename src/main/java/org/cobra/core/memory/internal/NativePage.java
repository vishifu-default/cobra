package org.cobra.core.memory.internal;

import org.cobra.commons.Jvm;
import org.cobra.commons.utils.Utils;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a virtual memory page (in 32KiB for each allocation).
 * Conceptually, have free-list to track empty chunk_slot for new entry, we only put an entry into this page, if we have
 * any available chunk_slot (or several) that best-fit for entry.
 */
public class NativePage {


    private final long address;
    private final int size;

    private final ChunkState chunkState;
    private AtomicInteger freeSpace;
    private BitSet chunkBitSet;

    public NativePage() {
        this(Jvm.ofUnsafeMemory().pageSize(), ChunkState.ofDefault());
    }

    public NativePage(int pageSize, ChunkState chunkState) {
        assert Jvm.SKIP_ASSERTION || (Utils.isLog2(pageSize) && chunkState != null);

        this.size = pageSize;
        this.freeSpace = new AtomicInteger(pageSize);
        this.chunkState = chunkState;
        this.chunkBitSet = new BitSet(pageSize / chunkState.getSize());

        this.address = allocate(pageSize);
    }

    private long allocate(final int size) {
        return Jvm.ofUnsafeMemory().allocate(size);
    }

    private long translate(int offset) {
        return this.address + offset;
    }

    public void write(final int offset, final byte[] arr) {
        write(offset, arr, 0, arr.length);
    }

    public void write(final int offset, final byte[] arr, final int arrOffset, final int len) {
        if ((arrOffset + len) > arr.length)
            throw new IllegalArgumentException("Illegal ending offset of array; [" + arrOffset + ":" + arrOffset +
                    " + " + len + "); out of bound array length: " + arr.length);

        if (len == 0)
            return;

        /* writes to memory page */
        Jvm.ofUnsafeMemory().copyMemory(translate(offset), arr, arrOffset, len);

        /* calculate how many chunks are spanned */
        int spanChunk = 1;
        if ((arrOffset + len) > this.chunkState.getSize()) {
            int spanSize = arrOffset + len;
            spanChunk = (int) Math.ceil((double) spanSize / this.chunkState.getSize());
        }
        markChunksOccupied(offset, spanChunk);
    }

    public int claimAvailChunkStartOffset(int spanChunk) {
        throw new UnsupportedOperationException("implement me");
    }

    private void markChunksOccupied(int startOffset, int spanChunk) {
        this.freeSpace.addAndGet(-1 * spanChunk * this.chunkState.getSize());
        int chunkIndex = startOffset >>> this.chunkState.getShiftBits();
        for (int i = 0; i < spanChunk; i++) {
            this.chunkBitSet.set(chunkIndex + i);
        }
    }

    public static class ChunkState {
        private final int size;
        private final int shiftBits;

        private static final ChunkState DEFAULT = new ChunkState(Jvm.DEFAULT_PAGE_CHUNK_SIZE_BYTE);

        public static ChunkState ofDefault() {
            return DEFAULT;
        }

        public ChunkState(int size) {
            assert Jvm.SKIP_ASSERTION || (Utils.isLog2(size));

            this.size = size;
            this.shiftBits = size - 1;
        }

        public int getSize() {
            return this.size;
        }

        public int getShiftBits() {
            return this.shiftBits;
        }
    }

}
