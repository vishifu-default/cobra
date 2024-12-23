package org.cobra.core.memory.slab;

import org.cobra.commons.Jvm;
import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RegionMethodsTest {

    private long address;
    private SlabLoc slabLoc;

    @BeforeEach
    void setup() {
        address = Jvm.osMemory().allocate(32);
        slabLoc = new SlabLoc(0, 5, 10);
    }

    @Test
    void getLoc() {
        final byte[] randData = TestUtils.randString(8).getBytes();

        ChunkMethods.putData(address, slabLoc, randData);

        final SlabLoc retLoc = ChunkMethods.getLoc(address);
        assertEquals(0, retLoc.getClassIndex());
        assertEquals(5, retLoc.getPageIndex());
        assertEquals(10, retLoc.getChunkIndex());
    }

    @Test
    void getData() {
        final byte[] randData = TestUtils.randString(8).getBytes();
        ChunkMethods.putData(address, slabLoc, randData);

        final byte[] retData = ChunkMethods.getData(address);
        assertArrayEquals(randData, retData);
    }
}