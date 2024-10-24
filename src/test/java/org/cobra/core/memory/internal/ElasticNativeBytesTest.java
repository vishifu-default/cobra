package org.cobra.core.memory.internal;

import org.cobra.commons.pools.BytesPool;
import org.cobra.commons.utils.Utils;
import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ElasticNativeBytesTest {

    private static final int SMALL_ALIGN = 3;

    private ElasticNativeBytes elasticNativeBytes;

    @BeforeEach
    void setUp() {
        elasticNativeBytes = new ElasticNativeBytes(SMALL_ALIGN, BytesPool.NONE);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initialCapacity() {
        int log2 = Utils.min(SMALL_ALIGN, ElasticMemoryBlock.MINIMUM_INIT_ALLOCATION);
        assertEquals(1L << log2, elasticNativeBytes.capacity());
    }

    @Test
    void writeAndRead_byte() {
        elasticNativeBytes.write((byte) 'a');
        elasticNativeBytes.write((byte) 'b');

        elasticNativeBytes.seek(0L);

        /* assert */
        assertEquals('a', elasticNativeBytes.read());
        assertEquals('b', elasticNativeBytes.read());
    }

    @Test
    void writeAndRead_array() {
        byte[] arr = TestUtils.randString(4).getBytes();
        elasticNativeBytes.copyMemory(arr);

        /* read from offset 0 */
        elasticNativeBytes.seek(0L);
        assertArrayEquals(arr, elasticNativeBytes.readBlock(arr.length));
    }

    @Test
    void writeAndReadAtOffset_byte() {
        elasticNativeBytes.write((byte) 'a', 1);
        elasticNativeBytes.write((byte) 'b', 3);

        /* assert at offset */
        assertEquals('a', elasticNativeBytes.read(1));
        assertEquals(0, elasticNativeBytes.read(2));
        assertEquals('b', elasticNativeBytes.read(3));
    }

    @Test
    void writeAndReadAtOffset_array() {
        byte[] arr = TestUtils.randString(4).getBytes();
        elasticNativeBytes.copyMemory(arr, 1);

        /* assert array at offset */
        assertArrayEquals(arr, elasticNativeBytes.readBlock(arr.length, 1));
    }

    @Test
    void writeAndRead_byte_needToResize() {
        for (int i = 0; i < 100; i++)
            elasticNativeBytes.write((byte) i);

        /* assert all writes */
        elasticNativeBytes.seek(0);
        for (int i = 0; i < 100; i++)
            assertEquals((byte) i, elasticNativeBytes.read());
    }

    @Test
    void writeAndRead_array_needToResize() {
        int size = 1024;
        byte[] arr = TestUtils.randString(size).getBytes();
        elasticNativeBytes.copyMemory(arr);

        /* assert array at offset 0*/
        elasticNativeBytes.seek(0);
        assertArrayEquals(arr, elasticNativeBytes.readBlock(size));

        ElasticNativeBytes otherElasticBytes = new ElasticNativeBytes(12, BytesPool.NONE);
        otherElasticBytes.copyMemory(arr);
        otherElasticBytes.seek(0);
        assertArrayEquals(arr, otherElasticBytes.readBlock(size));
    }

    @Test
    void givenOffset_outOfBound_shouldThrows() {
        assertThrows(IllegalArgumentException.class, () -> elasticNativeBytes.seek(100));
        assertThrows(IllegalArgumentException.class, () -> elasticNativeBytes.write((byte) 'a', 99));
        assertThrows(IllegalArgumentException.class, () -> elasticNativeBytes.read(99));
        assertThrows(IllegalArgumentException.class, () -> elasticNativeBytes.copyMemory("abc".getBytes(), 99));
        assertThrows(IllegalArgumentException.class, () -> elasticNativeBytes.readBlock(3, 100));
    }

    @Test
    void readAtOffset_equalCapacity_shouldThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> elasticNativeBytes.read(this.elasticNativeBytes.capacity()));

        elasticNativeBytes.seekEnd();
        assertThrows(IllegalArgumentException.class,
                () -> elasticNativeBytes.read(this.elasticNativeBytes.capacity()));
    }

    @Test
    void readArrayAtOffset_outOfBound_shouldThrows() {
        final long cap = elasticNativeBytes.capacity();
        assertThrows(IllegalArgumentException.class,
                () -> elasticNativeBytes.readBlock(10, cap - 3));
    }

    @Test
    void offsetManipulate() {
        /* assert seek position */
        elasticNativeBytes.seek(2);
        assertEquals(2, elasticNativeBytes.position());

        /* assert seekEnd */
        elasticNativeBytes.seekEnd();
        assertEquals(elasticNativeBytes.capacity(), elasticNativeBytes.position());
    }
}
