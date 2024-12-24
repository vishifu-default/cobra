package org.cobra.core.memory.slab;

import org.cobra.commons.Jvm;
import org.cobra.commons.errors.CobraException;
import org.cobra.core.memory.OSMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class SlabClass {

    private static final OSMemory memory = Jvm.osMemory();
    private static final Logger log = LoggerFactory.getLogger(SlabClass.class);

    private final int clsid;
    private final int chunkSize;
    private final int chunksPerPage;

    private int numChunks = 0;
    private int numFreeChunks = 0;

    private final List<SlabPage> pages = new ArrayList<>();

    private final Set<Integer> reassignPageIndex = new LinkedHashSet<>();
    private final Set<Integer> availPages = new LinkedHashSet<>();

    private final ReentrantLock mutex = new ReentrantLock();

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
     * @return the instance of set of re-assign page-index
     */
    public Set<Integer> reassignPageIndex() {
        return this.reassignPageIndex;
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
        try {
            this.mutex.lock();

            if (this.numFreeChunks == 0)
                allocateSlabPage();

            return doAllocate();
        } finally {
            this.mutex.unlock();
        }
    }

    /**
     * Free a chunk within given loc
     *
     * @param loc location
     */
    public void free(SlabLoc loc) {
        try {
            this.mutex.lock();

            this.numFreeChunks++;
            page(loc.getPageIndex()).doOfferFreeChunkId(loc.getChunkIndex());
        } finally {
            this.mutex.unlock();

        }
    }

    private ChunkMemory doAllocate() {
        int pollPageId;
        final Iterator<Integer> it = this.availPages.iterator();

        while (true) {
            pollPageId = it.next();

            if (this.reassignPageIndex.contains(pollPageId) || page(pollPageId).isFull()) {
                it.remove();
            } else {
                break;
            }
        }

        final SlabPage page = page(pollPageId);
        final SlabLoc pollLoc = new SlabLoc(this.clsid, pollPageId, page.doPollFreeChunkId());

        if (page.isFull())
            it.remove();

        this.numFreeChunks--;

        return getChunk(pollLoc);
    }

    /*
     * Allocate new slab_page within this slab class, the new allocated memory region should be = chunkPerPage *
     * chunk_size.
     * After allocating, add new page to member list and offer all chunks to freelist
     */
    private void allocateSlabPage() {
        final long startMs = System.currentTimeMillis();

        // todo: take from re-assign
        int pageId = this.pages.size();


        final long justAllocatedAddr = memory.allocate((long) this.chunksPerPage * this.chunkSize);
        final SlabPage newPage = new SlabPage(justAllocatedAddr);

        /* add new_page to the end of list of pages */
        if (pageId < this.pages.size()) {
            this.pages.add(pageId, newPage);
        } else {
            this.pages.addLast(newPage);
        }

        /* add all chunks of new_page to freelist */
        newPage.preallocate();

        /* add pageId to available_pages */
        this.availPages.add(pageId);

        /* increase num_of_chunks */
        this.numChunks += this.chunksPerPage;
        this.numFreeChunks += this.chunksPerPage;

        /* debug executing time */
        final long elapsedMs = System.currentTimeMillis() - startMs;
        log.debug("allocating page in slab-class {} took {}ms; page: {}; pages: {}", this.clsid, elapsedMs, newPage,
                this.pages.size());
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
    public final class SlabPage {

        private final long baseAddress;
        private int allocatedChunks = 0;

        // todo: chunk-node only need to hold chunk index (clsid, pageId from other outers)
        private ChunkNode freelist = null;

        private SlabPage(long baseAddress) {
            this.baseAddress = baseAddress;
        }

        public long getBaseAddress() {
            return this.baseAddress;
        }

        int getFreelistNum() {
            return chunksPerPage - this.allocatedChunks;
        }

        boolean isFull() {
            return this.getFreelistNum() == 0;
        }

        void doOfferFreeChunkId(int id) {
            ChunkNode chunk = new ChunkNode(id);
            chunk.setNext(this.freelist);
            this.freelist = chunk;
            decreaseAllocated();
        }

        int doPollFreeChunkId() {
            if (this.freelist == null)
                throw new CobraException("free-chunk is null, cannot poll anymore");

            final int pollFreeChunkId = this.freelist.getSelf();
            this.freelist = this.freelist.getNext();
            increaseAllocated();

            return pollFreeChunkId;
        }

        void preallocate() {
            final ChunkNode head = new ChunkNode(0);
            ChunkNode tmpPointer = head;
            for (int i = 1; i < chunksPerPage; i++) {
                tmpPointer.setNext(new ChunkNode(i));
                tmpPointer = tmpPointer.getNext();
            }
            this.freelist = head;
            this.allocatedChunks = 0;
        }

        ChunkNode getFreelist() {
            return this.freelist;
        }

        private void increaseAllocated() {
            this.allocatedChunks++;
        }

        private void decreaseAllocated() {
            this.allocatedChunks--;
        }


        @Override
        public String toString() {
            return "SlabPage(address=%d)".formatted(baseAddress);
        }
    }
}
