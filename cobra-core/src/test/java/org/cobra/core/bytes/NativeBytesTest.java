package org.cobra.core.bytes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NativeBytesTest {

    NativeBytes nativeBytes;

    @BeforeEach
    void setUp() {
        nativeBytes = NativeBytes.createLog2Align(3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initiate() {
        final NativeBytes heapBytes1 = NativeBytes.create();
        assertNotNull(heapBytes1);

        final NativeBytes heapBytes2 = NativeBytes.createLog2Align(3);
        assertNotNull(heapBytes2);
        assertEquals(1 << 3, heapBytes2.size());
    }

    @Test
    void write_read_byte() {
        byte a = 'a';
        byte b = 'b';

        nativeBytes.write(a);
        nativeBytes.writeAt(5L, b);

        assertEquals(1, nativeBytes.position(), "write 'a'; increase position");
        assertEquals(0, nativeBytes.read(), "position is 1; read empty");

        nativeBytes.position(0);
        assertEquals(a, nativeBytes.read(), "position is 0; read 'a'");

        assertEquals(b, nativeBytes.readAt(5L), "position 5 is 'b'");
    }

    @Test
    void write_read_array() {
        byte[] foo = "foo".getBytes();
        byte[] bar = "bar".getBytes();

        nativeBytes.position(1); // seek to 1
        nativeBytes.write(foo); // write at 1
        assertEquals(4, nativeBytes.position(), "after seek 1 and write 'foo' position is 4");
        nativeBytes.position(1);
        byte[] ret1 = new byte[3];
        assertEquals(3, nativeBytes.read(ret1), "read 3 bytes of 'foo'");
        assertArrayEquals(foo, ret1, "read bytes is 'foo'");

        nativeBytes.writeAt(4, bar); // write at 4
        byte[] ret4 = new byte[3];
        assertEquals(3, nativeBytes.readAt(4, ret4), "read 3 bytes of 'bar' at position 4");
        assertArrayEquals(bar, ret4, "read bytes is 'bar'");

        /* --- */

        byte[] foomix = "000foo0".getBytes();
        nativeBytes.position(1); // seek 1
        nativeBytes.write(foomix, 3, 3);
        assertEquals(4, nativeBytes.position(), "after seek 1 and write 'foo' position is 4");
        nativeBytes.position(1);
        assertEquals(3, nativeBytes.read(ret1, 0, 3), "read 3 bytes of 'foo'");
        assertArrayEquals(foo, ret1, "read bytes is 'foo'");
    }

    @Test
    void write_read_buffer() {
        ByteBuffer fooBb = ByteBuffer.wrap("foo".getBytes());
        ByteBuffer barBb = ByteBuffer.wrap("bar".getBytes());

        nativeBytes.write(fooBb);
        assertEquals(3, nativeBytes.position(), "position is 3 after write 'foo'");
        nativeBytes.position(0);
        ByteBuffer ret0 = ByteBuffer.allocate(3);
        assertEquals(3, nativeBytes.read(ret0), "read 3 bytes of 'foo'");
        assertArrayEquals(fooBb.array(), ret0.array(), "read bytes is 'foo'");

        nativeBytes.writeAt(4, barBb);
        ByteBuffer ret4 = ByteBuffer.allocate(3);
        assertEquals(3, nativeBytes.readAt(4, ret4), "read 3 bytes of 'bar' at position 4");
        assertArrayEquals(barBb.array(), ret4.array(), "read bytes is 'bar'");
    }

    @Test
    void write_autogrow() {
        byte a = 'a';
        byte[] foo = "foo".getBytes();

        nativeBytes.writeAt(30, a);
        nativeBytes.writeAt(100, foo);

        assertEquals(a, nativeBytes.readAt(30), "read at position 30 is 'a''");

        byte[] ret100 = new byte[3];
        assertEquals(3, nativeBytes.readAt(100, ret100), "read 3 bytes of 'foo'");
        assertArrayEquals(foo, ret100, "read bytes is 'foo'");
    }
}