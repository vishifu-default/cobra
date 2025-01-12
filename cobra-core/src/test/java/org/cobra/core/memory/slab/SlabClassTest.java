package org.cobra.core.memory.slab;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SlabClassTest {

    @Test
    void initialize() {
        final SlabClass slab = new SlabClass(0, 32, 4);

        assertEquals(0, slab.getId());
        assertEquals(32, slab.getChunkSize());
        assertEquals(4, slab.getChunksPerPage());
        assertEquals(3, slab.getChunkMasking());

        assertEquals(0, slab.getTotalChunks(), "not yet pre-allocated");
    }

    @Test
    void allocate_free() {
        final SlabClass slab = new SlabClass(0, 32, 4);
        SlabOffset offset0 = slab.allocate();
        SlabOffset offset1 = slab.allocate();

        assertEquals(2, slab.page(0).getOccupiedCount());
        assertEquals(4, slab.getTotalChunks(), "poll offset 2-time, must allocate 1 page");

        assertEquals(0, offset0.getClsid());
        assertEquals(0, offset0.getPageId());
        assertEquals(0, offset1.getPageId());
        assertEquals(0, offset0.getChunkId());
        assertEquals(1, offset1.getChunkId());

        slab.free(offset0);
        slab.free(offset1);
    }

    @Test
    void allocate_grow() {
        final SlabClass slab = new SlabClass(0, 32, 4);

        for (int i = 0; i < 10; i++) {
            slab.allocate();
        }

        assertEquals(12, slab.getTotalChunks());
        assertNotNull(slab.page(2));
        assertEquals(1, slab.page(0).getUtilized());
        assertEquals(1, slab.page(1).getUtilized());
        assertEquals(0.5f, slab.page(2).getUtilized());
        assertEquals(2, slab.countFreeNum());
    }

}