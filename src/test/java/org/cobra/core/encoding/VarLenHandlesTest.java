package org.cobra.core.encoding;

import org.cobra.commons.Jvm;
import org.cobra.core.bytes.OnHeapBytes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VarLenHandlesTest {

    private byte[] byteArray;
    private OnHeapBytes onHeapBytes;

    static VarLenHandles varHandles;

    @BeforeAll
    static void setupOnce() {
        varHandles = Jvm.varint();
    }

    @BeforeEach
    void setUp() {
        byteArray = new byte[16];
        onHeapBytes = OnHeapBytes.createLog2Align(8);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void writeAndRead_null() {
        /* byte array */
        varHandles.writeNull(byteArray, 0);
        assertTrue(varHandles.readNull(byteArray, 0));

        varHandles.writeNull(byteArray, 10);
        assertTrue(varHandles.readNull(byteArray, 10));

        /* random, sequenced bytes*/
        varHandles.writeNull(onHeapBytes);
        onHeapBytes.position(0);
        assertTrue(varHandles.readNull(onHeapBytes));

        varHandles.writeNull(byteArray, 4);
        assertTrue(varHandles.readNull(byteArray, 4));
    }

    @Test
    void writeAndRead_int() {
        /* byte array */
        varHandles.writeVarInt(byteArray, 0, 10);
        assertEquals(10, varHandles.readVarInt(byteArray, 0));

        varHandles.writeVarInt(byteArray, 10, 10);
        assertEquals(10, varHandles.readVarInt(byteArray, 10));

        /* bytes store */

    }

    @Test
    void writeAndRead_long() {
        /* byte array */
        varHandles.writeVarLong(byteArray, 0, 10);
        assertEquals(10, varHandles.readVarLong(byteArray, 0));

        varHandles.writeVarLong(byteArray, 10, 10);
        assertEquals(10, varHandles.readVarLong(byteArray, 10));

        /* bytes store */
    }

    @Test
    void sizeOfVarint() {
        /* int_32_bits */
        assertEquals(5, varHandles.sizeOfVarint(-1));
        assertEquals(1, varHandles.sizeOfVarint((1 << 7) - 1));
        assertEquals(2, varHandles.sizeOfVarint(1 << 7));
        assertEquals(2, varHandles.sizeOfVarint((1 << 14) - 1));
        assertEquals(3, varHandles.sizeOfVarint(1 << 14));
        assertEquals(3, varHandles.sizeOfVarint((1 << 21) - 1));
        assertEquals(4, varHandles.sizeOfVarint(1 << 21));
        assertEquals(4, varHandles.sizeOfVarint((1 << 28) - 1));
        assertEquals(5, varHandles.sizeOfVarint(1 << 28));
        assertEquals(5, varHandles.sizeOfVarint(1 << 29));
        /* int_64_bits */
        assertEquals(10, varHandles.sizeOfVarint(-1L));
        assertEquals(5, varHandles.sizeOfVarint((1L << 35) - 1));
        assertEquals(6, varHandles.sizeOfVarint(1L << 35));
        assertEquals(6, varHandles.sizeOfVarint((1L << 42) - 1));
        assertEquals(7, varHandles.sizeOfVarint(1L << 42));
        assertEquals(7, varHandles.sizeOfVarint((1L << 49) - 1));
        assertEquals(8, varHandles.sizeOfVarint(1L << 49));
        assertEquals(8, varHandles.sizeOfVarint((1L << 56) - 1));
        assertEquals(9, varHandles.sizeOfVarint(1L << 56));
        assertEquals(9, varHandles.sizeOfVarint(1L << 61));
    }
}