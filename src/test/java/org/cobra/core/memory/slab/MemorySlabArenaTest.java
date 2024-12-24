package org.cobra.core.memory.slab;

import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemorySlabArenaTest {

    @Test
    void init() {
        final MemorySlabArena arena = MemorySlabArena.init();

        assertNotNull(arena);

        final int clsidOf40 = arena.slabClassId(40);
        assertEquals(1, clsidOf40, "first slab-class is 32-byte-chunk");
    }

    @Test
    void allocate_free() {
        final MemorySlabArena arena = MemorySlabArena.init();

        final ChunkMemory chunkOf40 = arena.allocate(40);
        final ChunkMemory chunkOf1300 = arena.allocate(1300);

        assertNotNull(chunkOf40);
        assertNotNull(chunkOf1300);
    }

    @Test
    void allocate_many() {
        final MemorySlabArena arena = MemorySlabArena.init();

        for (int i = 0; i < 100_000; i++) {
            ChunkMemory chunk = arena.allocate(TestUtils.randInt(10, 2 * 1024 * 1024));
            if (chunk == null || chunk.loc().isNull())
                fail();
        }
    }

    @Test
    void writeSomeData() {
        final MemorySlabArena arena = MemorySlabArena.init();
        final byte[] data = TestUtils.randString(52).getBytes();

        final ChunkMemory chunk = arena.allocate(data.length);
        chunk.put(data);

        final long addr = chunk.address();
        final SlabLoc loc = ChunkMethods.getLoc(addr);
        final byte[] retData = ChunkMethods.getData(addr);

        assertEquals(chunk.loc().toString(), loc.toString());
        assertArrayEquals(data, retData);

        arena.free(addr);
    }
}