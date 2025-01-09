package org.cobra.core.memory.slab;

public class SlabOffset {

    private final int clsid;
    private final int pageId;
    private final int chunkId;

    public SlabOffset(int clsid, int pageId, int chunkId) {
        this.clsid = clsid;
        this.pageId = pageId;
        this.chunkId = chunkId;
    }

    public int getClsid() {
        return clsid;
    }

    public int getPageId() {
        return pageId;
    }

    public int getChunkId() {
        return chunkId;
    }

    @Override
    public String toString() {
        return "SlabOffset(clsid=%d, pageId=%d, chunkId=%d)".formatted(clsid, pageId, chunkId);
    }
}
