package org.cobra.core.memory.internal;

import org.cobra.commons.Jvm;
import org.cobra.commons.pools.BytesPool;
import org.cobra.commons.utils.Utils;
import org.cobra.core.memory.Bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ElasticNativeBytes implements Bytes {


    private static final int IO_BYTES_THRESHOLD = Jvm.memory().pageSize();

    private final int log2Alignment;
    private final int alignBitmask;
    private long capacity;
    private long position;

    private ElasticMemoryBlock[] memoryBlocks;
    private final BytesPool bytesPool;
    private int blockUsed;

    public ElasticNativeBytes(int log2Alignment, BytesPool bytesPool) {
        this.log2Alignment = log2Alignment;
        this.bytesPool = bytesPool;
        this.alignBitmask = (1 << log2Alignment) - 1; // calculate masking

        this.blockUsed = 1;

        /* init memory blocks */
        {
            this.memoryBlocks = new ElasticMemoryBlock[blockUsed]; // init with 1 block
            for (int i = 0; i < blockUsed; i++) {
                memoryBlocks[i] = new ElasticMemoryBlock(log2Alignment, bytesPool);
                this.capacity = memoryBlocks[0].capacity();
            }
        }
    }

    public long capacity() {
        return this.capacity;
    }

    @Override
    public void write(byte b) {
        write(b, this.position++);
    }

    @Override
    public void write(byte b, long offset) {
        ensureOffsetInBound(offset);
        checkResizing(offset);
        final SlotOffset slotOffset = translate(offset);
        block(slotOffset.index).write(slotOffset.offset, b);
    }

    @Override
    public void copyMemory(byte[] arr) {
        copyMemory(arr, this.position);
        this.position += arr.length;
    }

    @Override
    public void copyMemory(byte[] arr, long offset) {
        ensureOffsetInBound(offset);

        int len = arr.length;
        int arrPos = 0;
        while (len > 0) {
            checkResizing(offset);
            final SlotOffset slotOffset = translate(offset);
            int eachCopies = Utils.min(block(slotOffset.index).remaining(slotOffset.offset), len, IO_BYTES_THRESHOLD);

            block(slotOffset.index).writeArray(slotOffset.offset, arr, arrPos, eachCopies);

            len -= eachCopies;
            offset += eachCopies;
            arrPos += eachCopies;
        }
    }

    @Override
    public byte read() {
        return read(this.position++);
    }

    @Override
    public byte read(long offset) {
        if (offset >= capacity())
            throw new IllegalArgumentException("Unexpected offset that is out of bound; offset: " + offset + ", " +
                    "capacity: " + capacity());

        final SlotOffset slotOffset = translate(offset);
        return block(slotOffset.index).read(slotOffset.offset);
    }

    @Override
    public byte[] readBlock(int len) {
        byte[] result = readBlock(len, this.position);
        this.position += len;
        return result;
    }

    @Override
    public byte[] readBlock(int len, long offset) {
        ensurePartialInBound(offset, len);
        ByteBuffer buffer = ByteBuffer.allocate(len);

        while (len > 0) {
            final SlotOffset slotOffset = translate(offset);
            int eachCopies = Utils.min(block(slotOffset.index).remaining(slotOffset.offset), len, IO_BYTES_THRESHOLD);

            byte[] eachReadArray = block(slotOffset.index).readArray(slotOffset.offset, eachCopies);
            buffer.put(eachReadArray);

            len -= eachCopies;
            offset += eachCopies;
        }

        return buffer.array();
    }

    @Override
    public long position() {
        return this.position;
    }

    @Override
    public void seek(long offset) {
        ensureOffsetInBound(offset);
        this.position = offset;
    }

    @Override
    public void seekEnd() {
        this.position = capacity();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("implement me");
    }

    private ElasticMemoryBlock block(int index) {
        return this.memoryBlocks[index];
    }

    private ElasticMemoryBlock lastBlock() {
        return block(this.blockUsed - 1);
    }

    private void ensureOffsetInBound(long offset) {
        if (offset > capacity())
            throw new IllegalArgumentException("Offset is out of bound; offset: " + offset + "; capacity: " + capacity());
    }

    private void ensurePartialInBound(long offset, int followingSize) {
        if (offset + followingSize > capacity())
            throw new IllegalArgumentException("The asking range is invalid: " +
                    "asking [" + offset + " : " + offset + "+" + followingSize + ");" +
                    " capacity: " + capacity());
    }

    private void checkResizing(long offset) {
        if (offset < this.capacity)
            return;

        final ElasticMemoryBlock lastBlock = lastBlock();
        long tmpCap = capacity() - lastBlock.capacity();
        if (lastBlock.maybeGrow()) {
            tmpCap += lastBlock.capacity();
            this.capacity = tmpCap;
        } else {
            pushbackNewMemoryBlock();
        }
    }

    private void pushbackNewMemoryBlock() {
        if (this.blockUsed == this.memoryBlocks.length)
            this.memoryBlocks = Arrays.copyOf(this.memoryBlocks, (this.memoryBlocks.length + 1) * 3 / 2);

        this.memoryBlocks[this.blockUsed] = new ElasticMemoryBlock(this.log2Alignment, this.bytesPool);

        this.capacity += block(this.blockUsed).capacity();
        this.blockUsed++;
    }

    private SlotOffset translate(long offset) {
        int whichBlock = (int) (offset >>> this.log2Alignment);
        int whichOffset = (int) (offset & this.alignBitmask);
        return new SlotOffset(whichBlock, whichOffset);
    }

    private record SlotOffset(int index, int offset) {
    }
}
