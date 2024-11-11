package org.cobra.core.bytes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OnHeapBytesTest {

    OnHeapBytes heapBytes;

    @BeforeEach
    void setUp() {
        heapBytes = OnHeapBytes.createLog2Align(3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initiate() {
        final OnHeapBytes heapBytes1 = OnHeapBytes.create();
        assertNotNull(heapBytes1);

        final OnHeapBytes heapBytes2 = OnHeapBytes.createLog2Align(3);
        assertNotNull(heapBytes2);
        assertEquals(1 << 3, heapBytes2.size());
    }

    @Test
    void write_read_byte() {
        byte a = 'a';
        byte b = 'b';

        heapBytes.write(a);
        heapBytes.writeAt(5L, b);

        assertEquals(1, heapBytes.position(), "write 'a'; increase position");
        assertEquals(0, heapBytes.read(), "position is 1; read empty");

        heapBytes.position(0);
        assertEquals(a, heapBytes.read(), "position is 0; read 'a'");

        assertEquals(b, heapBytes.readAt(5L), "position 5 is 'b'");
    }

    @Test
    void write_read_array() {
        byte[] foo = "foo".getBytes();
        byte[] bar = "bar".getBytes();

        heapBytes.position(1); // seek to 1
        heapBytes.write(foo); // write at 1
        assertEquals(4, heapBytes.position(), "after seek 1 and write 'foo' position is 4");
        heapBytes.position(1);
        byte[] ret1 = new byte[3];
        assertEquals(3, heapBytes.read(ret1), "read 3 bytes of 'foo'");
        assertArrayEquals(foo, ret1, "read bytes is 'foo'");

        heapBytes.writeAt(4, bar); // write at 4
        byte[] ret4 = new byte[3];
        assertEquals(3, heapBytes.readAt(4, ret4), "read 3 bytes of 'bar' at position 4");
        assertArrayEquals(bar, ret4, "read bytes is 'bar'");

        /* --- */

        byte[] foomix = "000foo0".getBytes();
        heapBytes.position(1); // seek 1
        heapBytes.write(foomix, 3, 3);
        assertEquals(4, heapBytes.position(), "after seek 1 and write 'foo' position is 4");
        heapBytes.position(1);
        assertEquals(3, heapBytes.read(ret1, 0, 3), "read 3 bytes of 'foo'");
        assertArrayEquals(foo, ret1, "read bytes is 'foo'");
    }

    @Test
    void write_read_buffer() {
        ByteBuffer fooBb = ByteBuffer.wrap("foo".getBytes());
        ByteBuffer barBb = ByteBuffer.wrap("bar".getBytes());

        heapBytes.write(fooBb);
        assertEquals(3, heapBytes.position(), "position is 3 after write 'foo'");
        heapBytes.position(0);
        ByteBuffer ret0 = ByteBuffer.allocate(3);
        assertEquals(3, heapBytes.read(ret0), "read 3 bytes of 'foo'");
        assertArrayEquals(fooBb.array(), ret0.array(), "read bytes is 'foo'");

        heapBytes.writeAt(4, barBb);
        ByteBuffer ret4 = ByteBuffer.allocate(3);
        assertEquals(3, heapBytes.readAt(4, ret4), "read 3 bytes of 'bar' at position 4");
        assertArrayEquals(barBb.array(), ret4.array(), "read bytes is 'bar'");
    }

    @Test
    void write_autogrow() {
        byte a = 'a';
        byte[] foo = "foo".getBytes();

        heapBytes.writeAt(30, a);
        heapBytes.writeAt(100, foo);

        assertEquals(a, heapBytes.readAt(30), "read at position 30 is 'a''");

        byte[] ret100 = new byte[3];
        assertEquals(3, heapBytes.readAt(100, ret100), "read 3 bytes of 'foo'");
        assertArrayEquals(foo, ret100, "read bytes is 'foo'");
    }
}