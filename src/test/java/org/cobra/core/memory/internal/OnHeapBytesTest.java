package org.cobra.core.memory.internal;

import org.cobra.commons.pools.BytesPool;
import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OnHeapBytesTest {

    private OnHeapBytes onHeapBytes;

    @BeforeEach
    void setUp() {
        this.onHeapBytes = new OnHeapBytes(3, BytesPool.NONE);
        Assertions.assertEquals(1 << 3, onHeapBytes.capacity(), "capacity must be 2^3");
    }

    @Test
    void write_read() {
        byte v1 = 'a';
        byte v2 = 'z';

        onHeapBytes.write(v1);
        Assertions.assertEquals(1, onHeapBytes.position());
        onHeapBytes.write(4, v2);

        onHeapBytes.seek(0);
        Assertions.assertEquals(v1, onHeapBytes.read(), "read at position zero must be 'a'");
        Assertions.assertEquals(v2, onHeapBytes.read(4), "read at position 4 must be 'z'");
    }

    @Test
    void write_read_needGrow() {
        byte v1 = 'a';

        for (int i = 0; i < 100; i++) {
            onHeapBytes.write(v1);
        }

        onHeapBytes.seek(0);
        for (int i = 0; i < 100; i++) {
            Assertions.assertEquals(v1, onHeapBytes.read());
        }
    }

    @Test
    void write_read_array() {
        byte[] arr1 = TestUtils.randString(4).getBytes();
        byte[] arr2 = TestUtils.randString(4).getBytes();

        onHeapBytes.writeArray(arr1);
        Assertions.assertEquals(4, onHeapBytes.position());

        onHeapBytes.seek(0);
        byte[] read1 = onHeapBytes.readArray(4);
        Assertions.assertArrayEquals(arr1, read1, "read array must be equal to arr1");

        onHeapBytes.writeArray(2, arr2);
        Assertions.assertArrayEquals(arr2, onHeapBytes.readArray(2, 4),
                "read array must be equal to arr2");

    }

    @Test
    void write_read_array_needGrow() {
        byte[] arr = TestUtils.randString(128).getBytes();
        onHeapBytes.writeArray(10, arr);

        Assertions.assertArrayEquals(arr, onHeapBytes.readArray(10, 128),
                "read array at position must be equal to arr");

        onHeapBytes.seek(10L);
        Assertions.assertArrayEquals(arr, onHeapBytes.readArray(128),
                "read array must be equal to arr");
    }
}