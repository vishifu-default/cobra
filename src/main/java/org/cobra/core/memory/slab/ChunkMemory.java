package org.cobra.core.memory.slab;

/**
 * A chunk-region represent a region of memory that fit to a chunk of a slab-class.
 */
public class ChunkMemory {

    private final SlabClass slabClass;
    private final SlabLoc loc;

    public ChunkMemory(SlabClass slabClass, SlabLoc loc) {
        this.slabClass = slabClass;
        this.loc = loc;
    }

    public SlabLoc loc() {
        return this.loc;
    }

    public long address() {
        final long pageAddress = this.slabClass.page(this.loc.getPageIndex()).getBaseAddress();
        return pageAddress + ((long) this.slabClass.chunkSize() * this.loc.getChunkIndex());
    }

    public void put(byte[] arr) {
        ChunkMethods.putData(address(), loc, arr);
    }

    public byte[] getData() {
        return ChunkMethods.getData(address());
    }
}
