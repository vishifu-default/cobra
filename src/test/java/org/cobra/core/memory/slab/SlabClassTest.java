package org.cobra.core.memory.slab;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SlabClassTest {

    @Test
    void init() {
        final SlabClass slabClass = new SlabClass(0, 32, 8);
        assertEquals(0, slabClass.totalChunks(), "no chunk when just new a slab class");
        assertEquals(32, slabClass.chunkSize());
    }

    @Test
    void allocate_free() {
        final int clsid = 0;
        final SlabClass slabClass = new SlabClass(clsid, 32, 8);

        final ChunkMemory chunk = slabClass.allocate();

        assertNotNull(chunk, "just-allocated chunk is null");
        assertFalse(chunk.loc().isNull(), "just-allocated chunk location is null");
        assertEquals(7, slabClass.getNumFreeChunks(), "-1 free-chunk");

        slabClass.free(chunk.loc());
        assertEquals(8, slabClass.getNumFreeChunks());
    }

    @Test
    void allocateMorePage() {
        final int clsid = 0;
        final SlabClass slabClass = new SlabClass(clsid, 32, 2);

        // take 2 chunks
        slabClass.allocate();
        slabClass.allocate();

        // allocate page
        slabClass.allocate();

        assertEquals(4, slabClass.totalChunks());
        assertNotNull(slabClass.page(1));
    }
}