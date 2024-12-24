package org.cobra.core.memory.slab;

import org.cobra.commons.configs.ConfigDef;
import org.cobra.commons.errors.CobraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class MemorySlabArena {

    private static final Logger log = LoggerFactory.getLogger(MemorySlabArena.class);

    private static final String ERROR_FIND_FIT_CLASS = "Could not find any fit sized slab-class";

    /* limited number of slab class at 64, but usually not reach that limit, default settings should stop each chunk at 4mb */
    private static final int MAX_NUMBER_SLAB_CLASS = (63 + 1);
    private static final int BASE2_SMALLEST = 1;
    private static final int DEFAULT_CHUNK_BASE2_SMALLEST = 5; // 2^5 = 32 byte
    private static final int FAILED_CLSID = -1;
    private static final int FACTOR = 2;
    private static final long INF_MEM_LIMIT = -1;

    private final AtomicLong memAllocated;
    private final ConfigDef configDef;
    private final SlabClass[] slabClasses = new SlabClass[MAX_NUMBER_SLAB_CLASS];

    private int largestIndex;

    private MemorySlabArena(ConfigDef configDef) {
        this.memAllocated = new AtomicLong();
        this.configDef = configDef;
    }

    public static MemorySlabArena init() {
        return init(DefaultMemoryConfig.CONFIG_DEF);
    }

    public static MemorySlabArena init(ConfigDef configDef) {
        final MemorySlabArena arena = new MemorySlabArena(configDef);

        int chunkSize = 1 << DEFAULT_CHUNK_BASE2_SMALLEST;
        final int maxChunkSize = configDef.valueOf(DefaultMemoryConfig.SLAB_CHUNK_MAX_SIZE);

        int i = BASE2_SMALLEST - 1;
        while (i <= MAX_NUMBER_SLAB_CLASS - 1) {
            if (chunkSize > maxChunkSize)
                break; // out of loop once reach limit of chunk_size

            arena.allocateSlabClass(i, chunkSize);

            chunkSize *= FACTOR;
            i++;
        }

        arena.largestIndex = i;
        return arena;
    }

    /**
     * Find the first-fit slab class to fit the required size
     *
     * @param sizeof required size
     * @return class id (clsid)
     */
    public final int slabClassId(int sizeof) {
        int ans = 0;
        /* Keep finding first-fit SlabClass id (clsid) */
        while (ans < this.largestIndex && sizeof > this.slabClasses[ans].chunkSize())
            ans++;

        if (ans == this.largestIndex)
            return FAILED_CLSID;

        return ans;
    }

    public ChunkMemory allocate(int sizeof) {
        final int requiredSize = sizeof + SlabLoc.FOOTPRINT;

        final int clsid = slabClassId(requiredSize);
        if (clsid == FAILED_CLSID) {
            log.error("failed to find an fit-allocated slab-class for size of {} bytes", sizeof);
            throw new CobraException(ERROR_FIND_FIT_CLASS);
        }

        final SlabClass slabClass = slabClass(clsid);
        final ChunkMemory chunk = slabClass.allocate();

        this.memAllocated.addAndGet(slabClass.chunkSize());

        return chunk;
    }

    public void free(long address) {
        final SlabLoc loc = ChunkMethods.getLoc(address);
        final SlabClass slabClass = slabClass(loc.getClassIndex());

        this.memAllocated.addAndGet(-slabClass.chunkSize());
        slabClass.free(loc);
    }

    private void allocateSlabClass(int clsid, int chunkSize) {
        int chunksPerPage;
        final int pageSize = this.configDef.valueOf(DefaultMemoryConfig.SLAB_PAGE_SIZE);
        if (chunkSize < pageSize / FACTOR) {
            chunksPerPage = pageSize / chunkSize;
        } else {
            chunksPerPage = this.configDef.valueOf(DefaultMemoryConfig.SLAB_PAGE_CONSIST_CHUNKS_NUM);
        }

        final SlabClass slabClass = new SlabClass(clsid, chunkSize, chunksPerPage);
        this.slabClasses[clsid] = slabClass;

        log.debug("allocate slab-class; id: {}; size: {}", clsid, chunkSize);
    }

    SlabClass slabClass(int i) {
        return this.slabClasses[i];
    }

    @Override
    public String toString() {
        return "MemorySlabArena(" +
                ", memAllocated=" + memAllocated.get() +
                ", largestIndex=" + largestIndex +
                ')';
    }
}
