package org.cobra.core.memory.internal;

import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeBytesTest {

    NativeBytes nativeBytes;

    @BeforeEach
    void setup() {
        this.nativeBytes = NativeBytes.create(16, 32);
    }

    @Test
    void write_read() {
        byte[] arr = "foo".getBytes();
        nativeBytes.write(arr);
        assertEquals(3, nativeBytes.position());

        byte[] retArr = new byte[3];
        nativeBytes.position(0);
        nativeBytes.read(retArr);
        assertArrayEquals(arr, retArr);

    }

    @Test
    void write_read_atPosition() {
        byte[] arr = "foo".getBytes();
        byte[] retArr = new byte[3];

        nativeBytes.write(10, arr);
        nativeBytes.read(10, retArr);
        assertArrayEquals(arr, retArr);
    }

    @Test
    void write_read_buffer() {
        ByteBuffer bb = ByteBuffer.wrap("foo".getBytes());
        nativeBytes.write(bb);
        assertEquals(3, nativeBytes.position());

        ByteBuffer retBb = ByteBuffer.allocate(3);
        nativeBytes.position(0);
        nativeBytes.read(retBb);
        assertArrayEquals(bb.array(), retBb.array());
    }

    @Test
    void resizing() {
        byte[] arr = TestUtils.randString(30).getBytes();
        nativeBytes.write(arr);

        assertEquals(30, nativeBytes.position());
        assertTrue(nativeBytes.capacity() > 16);
    }
}