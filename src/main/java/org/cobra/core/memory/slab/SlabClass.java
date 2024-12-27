package org.cobra.core.memory.slab;

import org.cobra.commons.Jvm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SlabClass {

    private static final Logger log = LoggerFactory.getLogger(SlabClass.class);
    private final int clsid;

    private final int chunkSize;
    private final int chunksPerPage;
    private final int chunkBitmask;

    private int totalChunks;

    private final List<SlabPage> pages = new ArrayList<>();
    private final Freelist freelist = new Freelist();

    public SlabClass(int clsid, int chunkSize, int chunksPerPage) {
        this.clsid = clsid;
        this.chunkSize = chunkSize;
        this.chunkBitmask = (chunksPerPage - 1);
        this.chunksPerPage = chunksPerPage;
    }

    public int getId() {
        return this.clsid;
    }

    public int getChunkSize() {
        return this.chunkSize;
    }

    public int getChunksPerPage() {
        return this.chunksPerPage;
    }

    public int getChunkMasking() {
        return this.chunkBitmask;
    }

    public int getTotalChunks() {
        return this.totalChunks;
    }

    SlabPage page(int index) {
        return this.pages.get(index);
    }

    SlabOffset allocate() {
        return doPollFreelistOffset();
    }

    void free(SlabOffset offset) {
        doFreeOffset(offset);
    }

    int countFreeNum() {
        int allocated = 0;
        for (SlabPage page : pages) {
            if (page == null) continue;

            allocated += page.getAllocatedSize();
        }

        return this.totalChunks - allocated;
    }

    private SlabOffset doPollFreelistOffset() {
        SlabOffset useOffset = this.freelist.poll();

        if (useOffset == null) {
            doAllocatePage();
            useOffset = this.freelist.poll();
        }

        page(useOffset.getPageId()).increaseAllocated();

        return useOffset;
    }

    private void doFreeOffset(SlabOffset offset) {
        freelist.offer(offset);
        page(offset.getPageId()).decreaseAllocated();
    }

    private void doAllocatePage() {
        final long startMs = System.currentTimeMillis();

        final int pageId = this.pages.size();
        final SlabPage newPage = new SlabPage();
        this.pages.addLast(newPage);

        newPage.preallocate();

        for (int i = chunksPerPage - 1; i >= 0; i--) {
            freelist.offer(new SlabOffset(clsid, pageId, i));
        }
        totalChunks += chunksPerPage;

        final long elapsedMs = System.currentTimeMillis() - startMs;
        log.debug("{} allocate page {} took {}ms", this, pageId, elapsedMs);
    }

    @Override
    public String toString() {
        return "SlabClass(clsid=%d, chunkSize=%d, chunksPerPage=%d, totalChunks=%d)"
                .formatted(clsid, chunkSize, chunksPerPage, totalChunks);
    }

    final class SlabPage {

        private long baseAddress;
        private int allocatedSize;

        public long getBaseAddress() {
            return this.baseAddress;
        }

        public int getAllocatedSize() {
            return this.allocatedSize;
        }

        public float getUtilized() {
            return (float) this.allocatedSize / chunksPerPage;
        }

        void increaseAllocated() {
            this.allocatedSize++;
        }

        void decreaseAllocated() {
            this.allocatedSize--;
        }

        void preallocate() {
            malloc();
            this.allocatedSize = 0;
        }

        private void malloc() {
            this.baseAddress = Jvm.osMemory().allocate((long) chunkSize * chunksPerPage);
        }

        @Override
        public String toString() {
            return "SlabPage(baseAddress=%d, allocatedSize=%d)".formatted(baseAddress, allocatedSize);
        }
    }
}
