package org.cobra.core.memory.slab;

import org.cobra.commons.Jvm;
import org.cobra.commons.errors.CobraException;
import org.cobra.core.memory.OSMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SlabClass {

    private static final OSMemory memory = Jvm.osMemory();
    private static final Logger log = LoggerFactory.getLogger(SlabClass.class);

    private final int clsid;
    private final int chunkSize;
    private final int chunksPerPage;

    private int numChunks = 0;
    private int numFreeChunks = 0;

    private ChunkNode freelist = new ChunkNode(SlabLoc.NULL_LOC);
    private final List<SlabPage> pages = new ArrayList<>();

    public SlabClass(int clsid, int chunkSize, int chunksPerPage) {
        this.clsid = clsid;
        this.chunkSize = chunkSize;
        this.chunksPerPage = chunksPerPage;
    }

    /**
     * @param id page index
     * @return page from member list
     */
    public SlabPage page(int id) {
        return this.pages.get(id);
    }

    public int totalChunks() {
        return this.numChunks;
    }

    /**
     * @return the chunk size (item size) that this slab class serves.
     */
    public int chunkSize() {
        return this.chunkSize;
    }

    /**
     * Get the virtual chunk region the reflects the slab location within this slab class.
     *
     * @param loc slab location
     * @return lazy-load chunk region
     */
    public ChunkMemory getChunk(SlabLoc loc) {
        return new ChunkMemory(this, loc);
    }

    /**
     * Allocate a lazy-load chunk region serves for new data.
     * If not have any free_chunk, allocate new slab_page, then add all chunks to freelist.
     * Poll a free slab_loc from freelist
     *
     * @return a lazy-load chunk region of a page within this slab class.
     */
    public ChunkMemory allocate() {
        if (this.numFreeChunks == 0)
            allocateSlabPage();

        return doAllocate();
    }

    /**
     * Free a chunk within given loc
     *
     * @param loc location
     */
    public void free(SlabLoc loc) {
        doFreeChunk(loc);
    }

    /**
     * Offer a new slab location to freelist
     *
     * @param loc location
     */
    private void offerFreeLoc(SlabLoc loc) {
        ChunkNode chunk = new ChunkNode(loc);
        chunk.setNext(this.freelist);
        this.freelist = chunk;
        this.numFreeChunks++;
    }

    /**
     * Poll a slab location from freelist. If the polling is null throw exception for encountering a null loc.
     *
     * @return free location
     */
    private SlabLoc pollFreeLoc() {
        if (this.freelist.getSelf().isNull())
            throw new CobraException("free-chunk is null, cannot poll anymore");

        final SlabLoc pollLoc = this.freelist.getSelf();
        this.freelist = this.freelist.getNext();
        this.numFreeChunks--;

        return pollLoc;
    }

    private ChunkMemory doAllocate() {
        final SlabLoc pollLoc = pollFreeLoc();
        return getChunk(pollLoc);
    }

    private void doFreeChunk(SlabLoc loc) {
        offerFreeLoc(loc);
    }

    /**
     * Allocate new slab_page within this slab class, the new allocated memory region should be = chunkPerPage *
     * chunk_size.
     * After allocating, add new page to member list and offer all chunks to freelist
     */
    private void allocateSlabPage() {
        final long startMs = System.currentTimeMillis();

        final long justAllocatedAddr = memory.allocate((long) this.chunksPerPage * this.chunkSize);
        final SlabPage newPage = new SlabPage(justAllocatedAddr);

        /* add new_page to the end of list of pages */
        this.pages.addLast(newPage);
        final int pageId = this.pages.size() - 1;

        /* add all chunks of new_page to freelist */
        for (int i = this.chunksPerPage - 1; i >= 0; i--) {
            offerFreeLoc(new SlabLoc(this.clsid, pageId, i));
        }

        /* increase num_of_chunks */
        this.numChunks += this.chunksPerPage;

        final long elapsedMs = System.currentTimeMillis() - startMs;
        log.debug("allocating page in slab-class {} took {}ms; page: {}; pages: {}", this.clsid, elapsedMs, newPage,
                this.pages.size());
    }

    /* test visibility */
    ChunkNode getFreelist() {
        return this.freelist;
    }

    /* test visibility */
    int getNumFreeChunks() {
        return this.numFreeChunks;
    }

    @Override
    public String toString() {
        return "SlabClass(chunkSize=%d, chunksPerPage=%d, numChunks=%d, numFreeChunks=%d, numPages=%d)"
                .formatted(chunkSize, chunksPerPage, numChunks, numFreeChunks, this.pages.size());
    }

    /**
     * Slab-page represents a container (slab) of continuous chunks.
     * We can calculate any chunk address by using the {@code baseAddress}
     */
    public static final class SlabPage {

        private final long baseAddress;

        private SlabPage(long baseAddress) {
            this.baseAddress = baseAddress;
        }

        public long getBaseAddress() {
            return this.baseAddress;
        }

        @Override
        public String toString() {
            return "SlabPage(address=%d)".formatted(baseAddress);
        }
    }
}
