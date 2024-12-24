package org.cobra.core.memory.slab;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlabLocTest {

    @Test
    void getMember() {
        final SlabLoc loc = new SlabLoc(1, 2, 3);
        assertEquals(1, loc.getClassIndex());
        assertEquals(2, loc.getPageIndex());
        assertEquals(3, loc.getChunkIndex());
    }

    @Test
    void nullLoc() {
        assertTrue(SlabLoc.NULL_LOC.isNull());
    }
}