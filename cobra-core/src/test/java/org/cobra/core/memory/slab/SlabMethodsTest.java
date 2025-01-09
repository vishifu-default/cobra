package org.cobra.core.memory.slab;

import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SlabMethodsTest {

    private SlabArena arena;

    @BeforeEach
    void setUp() {
        arena = SlabArena.initialize();
    }

    @Test
    void put_and_get() {
        SlabMethods slabMethods = new SlabMethods(arena);
        SlabOffset offset = arena.slab(0).allocate();

        int hash = 1;
        byte[] raw = TestUtils.randString(4).getBytes();

        slabMethods.put(offset, hash, raw);

        long addrOf = slabMethods.addressOf(offset);

        // get loc
        SlabOffset loc = slabMethods.location(addrOf);
        byte[] retData = slabMethods.get(loc);

        assertArrayEquals(raw, retData);
    }
}