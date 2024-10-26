package org.cobra.core.memory.internal;

import org.cobra.commons.pools.BytesPool;
import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElasticMemoryBlockTest {

    private ElasticMemoryBlock memoryBlock;

    @BeforeEach
    void setUp() {
        /* limit at 126 bytes for block */
        memoryBlock = new ElasticMemoryBlock(7, BytesPool.NONE);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void limitSize() {
        assertEquals(1 << 7, memoryBlock.limitSize(), "limit is power 2 of alignment");
    }

    @Test
    void initialAllocatedCapacity() {
        assertEquals(1 << ElasticMemoryBlock.MINIMUM_INIT_ALLOCATION, memoryBlock.capacity(),
                "minimum initial allocated capacity");
    }

    @Test
    void writeAndRead_byte() {
        byte b1 = (byte) 'a';
        byte b2 = (byte) 'b';

        memoryBlock.write(0, b1);
        memoryBlock.write(1, b2);

        assertEquals(b1, memoryBlock.read(0));
        assertEquals(b2, memoryBlock.read(1));
    }

    @Test
    void writeAndRead_array() {
        // write all arr
        byte[] arr = TestUtils.randString(32).getBytes();
        memoryBlock.writeArray(0, arr);
        assertArrayEquals(arr, memoryBlock.readArray(0, arr.length));

        // write all arr with given offset and len
        arr = TestUtils.randString(32).getBytes();
        memoryBlock.writeArray(10, arr, 0, arr.length);
        assertArrayEquals(arr, memoryBlock.readArray(10, arr.length));

        // write partial arr
        arr = TestUtils.randString(32).getBytes();
        byte[] sliceArr = new byte[12];
        System.arraycopy(arr, 5, sliceArr, 0, 12);
        memoryBlock.writeArray(10, arr, 5, 12);
        assertArrayEquals(sliceArr, memoryBlock.readArray(10, 12));
    }

    @Test
    void write_offsetOutCapacity() {
        assertThrows(IndexOutOfBoundsException.class, () -> memoryBlock.write(300, (byte) 'a'));
        assertThrows(IndexOutOfBoundsException.class, () -> memoryBlock.writeArray(300, "abc".getBytes(), 0, 3));
    }

    @Test
    void read_offsetOutCapacity() {
        assertThrows(IndexOutOfBoundsException.class, () -> memoryBlock.read(300));
        assertThrows(IndexOutOfBoundsException.class, () -> memoryBlock.readArray(300, 3));
    }

    @Test
    void maybeGrow() {
        for (int i = 0; i < 126; i++) {
            if (memoryBlock.remaining(i) == 0)
                assertTrue(memoryBlock.maybeGrow(), "No remain space need to grow");
            memoryBlock.write(i, (byte) i);
        }

        assertFalse(memoryBlock.maybeGrow());
        assertFalse(memoryBlock.maybeGrow());
    }
}