package org.cobra.core.memory.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeapBytesStoreTest {

    HeapBytesStore byteWrapHeap;
    HeapBytesStore bufferWrapHeap;


    @BeforeEach
    void setUp() {
        byteWrapHeap = HeapBytesStore.wrap(new byte[16]);
        bufferWrapHeap = HeapBytesStore.wrap(ByteBuffer.allocate(16));
    }

    @Test
    void putByte_getByte() {
        byte v1 = 'a';
        byte v2 = 'b';

        byteWrapHeap.putByte(4, v1);
        byteWrapHeap.putByte(5, v2);

        bufferWrapHeap.putByte(6, v1);
        bufferWrapHeap.putByte(7, v2);

        assertEquals(v1, byteWrapHeap.getByte(4));
        assertEquals(v2, byteWrapHeap.getByte(5));
        assertEquals(v1, bufferWrapHeap.getByte(6));
        assertEquals(v2, bufferWrapHeap.getByte(7));
    }

    @Test
    void putInt_getInt() {
        int v1 = 15;
        int v2 = 95;

        byteWrapHeap.putInt(0, v1);
        byteWrapHeap.putInt(6, v2);
        bufferWrapHeap.putInt(6, v1);
        bufferWrapHeap.putInt(11, v2);

        assertEquals(v1, byteWrapHeap.getInt(0));
        assertEquals(v2, byteWrapHeap.getInt(6));
        assertEquals(v1, bufferWrapHeap.getInt(6));
        assertEquals(v2, bufferWrapHeap.getInt(11));
    }

    @Test
    void putLong_getLong() {
        long v1 = 567L;
        long v2 = 9530L;

        byteWrapHeap.putLong(0, v1);
        bufferWrapHeap.putLong(2, v2);

        assertEquals(v1, byteWrapHeap.getLong(0));
        assertEquals(v2, bufferWrapHeap.getLong(2));
    }

    @Test
    void putBytes_getBytes() {
        byte[] arr1 = "foo".getBytes();
        byte[] arr2 = "-bar---".getBytes();

        byteWrapHeap.putBytes(2, arr1, 0, arr1.length);
        bufferWrapHeap.putBytes(5, arr2, 1, 3);

        byte[] ret1 = new byte[arr1.length];
        byte[] ret2 = new byte[3];

        assertEquals(3, byteWrapHeap.getBytes(2, ret1, 0, ret1.length));
        assertEquals(3, bufferWrapHeap.getBytes(5, ret2, 0, ret2.length));
        assertEquals("foo", new String(ret1));
        assertEquals("bar", new String(ret2));
    }
}