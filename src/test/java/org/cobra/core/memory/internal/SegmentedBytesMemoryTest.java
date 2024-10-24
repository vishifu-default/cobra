package org.cobra.core.memory.internal;

import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SegmentedBytesMemoryTest {

    private SegmentedBytesMemory segmentedBytesMemory;

    @BeforeEach
    void setupOnce() {
        segmentedBytesMemory = new SegmentedBytesMemory(3);
    }

    @AfterEach
    void tearDown() {
        segmentedBytesMemory.free();
    }

    @Test
    void writeAndRead() {
        int loop = 128;
        for (int i = 0; i < loop; i++) {
            segmentedBytesMemory.write(i, (byte) i);
        }

        for (int i = 0; i < loop; i++) {
            if ((byte) i != segmentedBytesMemory.read(i))
                Assertions.fail("expected: " + (byte) i + ", actual: " + segmentedBytesMemory.read(i));
        }
    }

    @Test
    void getSize() {
        int loop = 128;
        for (int i = 0; i < loop; i++) {
            segmentedBytesMemory.write(i, (byte) i);
        }

        Assertions.assertEquals(loop, segmentedBytesMemory.size());
    }

    @Test
    void copyMemory() {
        byte[] arr = TestUtils.randString(128).getBytes();
        segmentedBytesMemory.copyMemory(arr, 0, 0, 128);

        for (int i = 0; i < 128; i++) {
            if (arr[i] != segmentedBytesMemory.read(i))
                Assertions.fail("expected: " + arr[i] + ", actual: " + segmentedBytesMemory.read(i));
        }

        byte[] retArr = new byte[128];
        segmentedBytesMemory.copyMemory(0, retArr, 0, 128);

        Assertions.assertArrayEquals(retArr, arr);
    }

    @Test
    void orderedCopyMemory() {
        byte[] arr = TestUtils.randString(128).getBytes();
        segmentedBytesMemory.orderedCopyMemory(arr, 0, 0, 128);

        for (int i = 0; i < 128; i++) {
            if (arr[i] != segmentedBytesMemory.read(i))
                Assertions.fail("expected: " + arr[i] + ", actual: " + segmentedBytesMemory.read(i));
        }
    }
}
