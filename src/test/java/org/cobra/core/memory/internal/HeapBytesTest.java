package org.cobra.core.memory.internal;

import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeapBytesTest {

    HeapBytes heapBytes;

    @BeforeEach
    void setUp() {
        heapBytes = HeapBytes.of(16, 32);
    }

    @Test
    void write_read() {
        byte[] array = "foo".getBytes();

        heapBytes.write(array);
        heapBytes.position(0);

        byte[] readArr = new byte[3];
        heapBytes.read(readArr);
        assertArrayEquals(array, readArr);
    }

    @Test
    void write_read_atPosition() {
        byte[] array = "foo".getBytes();

        heapBytes.write(2, array);

        byte[] readArr = new byte[3];
        heapBytes.read(2, readArr);
        assertArrayEquals(array, readArr);
    }

    @Test
    void write_read_buffer() {
        ByteBuffer buffer = ByteBuffer.wrap("foo".getBytes());
        heapBytes.write(buffer);
        ByteBuffer readBb = ByteBuffer.allocate(3);
        heapBytes.position(0);
        heapBytes.read(readBb);

        assertArrayEquals(buffer.array(), readBb.array());

        heapBytes.write(2, buffer);
        readBb.clear();
        heapBytes.read(2, readBb);

        assertArrayEquals(buffer.array(), readBb.array());
    }

    @Test
    void resizing() {
        byte[] arr = TestUtils.randString(20).getBytes();
        heapBytes.write(arr);

        byte[] retArr = new byte[arr.length];
        heapBytes.position(0);
        heapBytes.read(retArr);

        assertArrayEquals(arr, retArr);
        assertTrue(heapBytes.capacity() > 16);
    }
}