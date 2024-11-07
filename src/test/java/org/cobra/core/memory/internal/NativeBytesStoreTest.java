package org.cobra.core.memory.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NativeBytesStoreTest {

    private NativeBytesStore nativeBytes;

    @BeforeEach
    void setUp() {
        nativeBytes = NativeBytesStore.allocate(16, 32);
    }

    @Test
    void putByte_getByte() {
        byte v1 = 'a';
        byte v2 = 'b';

        nativeBytes.putByte(2, v1);
        nativeBytes.putByte(4, v2);

        assertEquals(v1, nativeBytes.getByte(2));
        assertEquals(0, nativeBytes.getByte(3));
        assertEquals(v2, nativeBytes.getByte(4));
    }

    @Test
    void putInt_getInt() {
        int v1 = 100;
        int v2 = 500;

        nativeBytes.putInt(0, v1);
        nativeBytes.putInt(5, v2);

        assertEquals(v1, nativeBytes.getInt(0));
        assertEquals(v2, nativeBytes.getInt(5));
    }

    @Test
    void putLong_getLong() {
        long v1 = 25_000;
        long v2 = 99_111;

        nativeBytes.putLong(0, v1);
        nativeBytes.putLong(8, v2);

        assertEquals(v1, nativeBytes.getLong(0));
        assertEquals(v2, nativeBytes.getLong(8));
    }

    @Test
    void resizing() {
        byte[] foo = "foo".getBytes();
        nativeBytes.putBytes(0, foo, 0, foo.length);
        nativeBytes.checkResizing(30);

        assertEquals(32, nativeBytes.capacity());

        byte[] ret = new byte[3];
        int reads = nativeBytes.getBytes(0, ret, 0, ret.length);
        assertEquals(3, reads);
        assertArrayEquals(foo, ret);
    }
}