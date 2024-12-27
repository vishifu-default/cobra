package org.cobra.core.memory.slab;

import org.cobra.commons.configs.ConfigDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class SlabArena {

    private static final Logger log = LoggerFactory.getLogger(SlabArena.class);

    private static final String ERROR_NOT_FIND_CLSID = "Could not find any slab-class for required size";

    private static final int MAX_SLAB_NUMBER = (63 + 1);
    private static final int SMALLEST_BASE2 = 1;
    private static final int DEFAULT_SMALLEST_CHUNK_BASE2_SIZE = 5;
    private static final int FAILED_CLSID = -1;
    private static final int FACTOR = 2;

    private final AtomicLong memAllocated = new AtomicLong(0);
    private final ConfigDef configDef;
    private final SlabMethods slabMethods;
    private final SlabClass[] slabs = new SlabClass[MAX_SLAB_NUMBER];

    private int largestIndex;

    private SlabArena(ConfigDef configDef) {
        this.configDef = configDef;
        this.slabMethods = new SlabMethods(this);
    }

    public static SlabArena initialize() {
        return initialize(MemoryConfig.DEFAULT_CONFIG);
    }

    public static SlabArena initialize(ConfigDef configDef) {
        final SlabArena arena = new SlabArena(configDef);

        int chunkSize = 1 << DEFAULT_SMALLEST_CHUNK_BASE2_SIZE;
        final int maxChunkSize = configDef.valueOf(MemoryConfig.SLAB_CHUNK_MAX_SIZE);

        int i = SMALLEST_BASE2 - 1;
        while (i <= MAX_SLAB_NUMBER - 1) {
            if (chunkSize > maxChunkSize)
                break;

            arena.doAllocateSlab(i, chunkSize);

            chunkSize *= FACTOR;
            i++;
        }

        arena.largestIndex = i;
        return arena;
    }

    public SlabMethods methods() {
        return slabMethods;
    }

    public int clsid(int sizeof) {
        int ans = SMALLEST_BASE2 - 1;
        while (ans < this.largestIndex && sizeof > slab(ans).getChunkSize())
            ans++;

        if (ans == this.largestIndex)
            return FAILED_CLSID;

        return ans;
    }

    public SlabClass slab(int i) {
        return this.slabs[i];
    }

    public long allocate(int hash, byte[] arr) {
        final int requiredSize = arr.length + SlabMethods.SLAB_META_FOOTPRINT;
        final int clsid = clsid(requiredSize);
        if (clsid == FAILED_CLSID) {
            log.error("failed to find a fit-size to allocate for size {}", requiredSize);
            throw new IllegalStateException(ERROR_NOT_FIND_CLSID);
        }

        final SlabClass slab = slab(clsid);
        final SlabOffset justOffset = slab.allocate();

        this.slabMethods.put(justOffset, hash, arr);

        return slabMethods.addressOf(justOffset);
    }

    public void free(long address) {
        final SlabOffset slabOffset = slabMethods.location(address);
        final SlabClass slab = slab(slabOffset.getClsid());
        slab.free(slabOffset);
    }

    private void doAllocateSlab(int clsid, int chunkSize) {
        final int pageSize = configDef.valueOf(MemoryConfig.SLAB_PAGE_SIZE);
        int mustConsists = configDef.valueOf(MemoryConfig.SLAB_PAGE_CONSIST_CHUNKS_NUM);

        int chunksPerPage = Math.max(pageSize / chunkSize, mustConsists);

        final SlabClass slab = new SlabClass(clsid, chunkSize, chunksPerPage);
        this.slabs[clsid] = slab;

        log.debug("allocate slab-class; clsid: {}; chunk-size {}", clsid, chunkSize);
    }

    int largestIndex() {
        return this.largestIndex;
    }

    @Override
    public String toString() {
        return "SlabArena(memAllocated=%s, largestIndex=%d)".formatted(memAllocated, largestIndex);
    }
}
