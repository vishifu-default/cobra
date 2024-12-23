package org.cobra.core.memory.slab;

/**
 * Define slab location, which provide value about slab-class, page-index, chunk-index.
 * By design, each loc consist of:
 * - 1 byte for clsid (class id). Means that will have maximum 128 class (start at default 64 byte and grow factor of 2)
 * - 4 byte (int) for page id
 * - 4 byte (int) for chunk id
 */
public class SlabLoc {

    public static final SlabLoc NULL_LOC = new SlabLoc(-1, -1, -1);

    public static final int CLASS_FOOTPRINT = Byte.BYTES;
    public static final int PAGE_FOOTPRINT = Integer.BYTES;
    public static final int CHUNK_FOOTPRINT = Integer.BYTES;
    public static final int FOOTPRINT = CLASS_FOOTPRINT + PAGE_FOOTPRINT + CHUNK_FOOTPRINT;

    private final short classIndex;
    private final int pageIndex;
    private final int chunkIndex;

    public SlabLoc(int classIndex, int pageIndex, int chunkIndex) {
        this.classIndex = (short) classIndex;
        this.pageIndex = pageIndex;
        this.chunkIndex = chunkIndex;
    }

    public short getClassIndex() {
        return this.classIndex;
    }

    int getChunkIndex() {
        return chunkIndex;
    }

    int getPageIndex() {
        return pageIndex;
    }

    boolean isNull() {
        return this.pageIndex < 0 && this.chunkIndex < 0;
    }

    @Override
    public String toString() {
        return "SlabLoc(classIndex=%s, pageIndex=%d, chunkIndex=%d)".formatted(classIndex, pageIndex, chunkIndex);
    }
}
