package org.cobra.core.memory.slab;

import org.cobra.commons.config.ConfigDef;

public class MemoryConfig {

    public static final String SLAB_CHUNK_MAX_SIZE = "memory.slab.chunk.max_size";
    public static final int SLAB_CHUNK_MAX_SIZE_DEFAULT = 4 * 1024 * 1024; // 4Mbi

    public static final String SLAB_PAGE_SIZE = "memory.slab.page_size";
    public static final int SLAB_PAGE_SIZE_DEFAULT = 1 << 16;

    public static final String SLAB_PAGE_CONSIST_CHUNKS_NUM = "memory.slab.page_chunks_num";
    public static final int SLAB_PAGE_CONSIST_CHUNKS_NUM_DEFAULT = 256;

    public static ConfigDef DEFAULT_CONFIG = new ConfigDef()
            .define(SLAB_CHUNK_MAX_SIZE, SLAB_CHUNK_MAX_SIZE_DEFAULT)
            .define(SLAB_PAGE_SIZE, SLAB_PAGE_SIZE_DEFAULT)
            .define(SLAB_PAGE_CONSIST_CHUNKS_NUM, SLAB_PAGE_CONSIST_CHUNKS_NUM_DEFAULT);
}
