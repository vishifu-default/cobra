package org.cobra.core.memory.slab;

import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlabArenaTest {

    @Test
    void init() {
        final SlabArena arena = SlabArena.initialize();

        assertNotNull(arena);
        assertTrue(arena.largestIndex() > 1);

        for (int i = 0; i < arena.largestIndex(); i++) {
            assertNotNull(arena.slab(i));
        }

        assertEquals(0, arena.clsid(4));
        assertEquals(1, arena.clsid(50));
    }

    @Test
    void allocate_free() {
        final SlabArena arena = SlabArena.initialize();

        List<Long> addresses = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long addr = arena.allocate(i, TestUtils.randString(4).getBytes());
            addresses.add(addr);
        }

        for (long addr : addresses) {
            arena.free(addr);
        }

        assertEquals(0, arena.slab(0).page(0).getOccupiedCount());
    }

    @Test
    void allocateMany() {

        final SlabArena arena = SlabArena.initialize();
        List<byte[]> randBytes = new ArrayList<>();

        for (int i = 0; i < 10_000; i++) {
            randBytes.add(TestUtils.randString(TestUtils.randInt(10, 18 * 1024)).getBytes());
        }

        final long start = System.currentTimeMillis();
        List<Long> addresses = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            long addr = arena.allocate(i, randBytes.get(i));
            addresses.add(addr);
            assertTrue(addr > 0);
        }

        assertEquals(10_000, addresses.size());

        final long end = System.currentTimeMillis();
        System.out.printf("allocate 10_000 took %dms%n", end - start);
    }
}