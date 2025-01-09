package org.cobra.core.bytes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OnHeapBytesStoreTest {

    OnHeapBytesStore heaps;

    @BeforeEach
    void setUp() {
        heaps = OnHeapBytesStore.create(3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initiateInstance() {
        final OnHeapBytesStore store1 = OnHeapBytesStore.create();
        assertNotNull(store1);
        assertEquals(1 << 14, store1.size());

        final OnHeapBytesStore store2 = OnHeapBytesStore.create(3);
        assertNotNull(store2);
        assertEquals(8, store2.size());
    }

    @Test
    void write_read() {
        byte a = 'a';
        byte b = 'b';

        heaps.writeAt(1, a);
        heaps.writeAt(4, b);

        assertEquals('a', heaps.readAt(1));
        assertEquals('b', heaps.readAt(4));
        assertEquals(0, heaps.readAt(2));
        assertEquals(0, heaps.readAt(5));
    }

    @Test
    void write_read_array() {
        byte[] foo = "foo".getBytes();
        byte[] bar = "bar".getBytes();

        heaps.writeAt(1, foo);
        heaps.writeAt(5, bar);

        byte[] ret1 = new byte[3];
        assertEquals(3, heaps.readAt(1, ret1));
        assertArrayEquals(foo, ret1);

        byte[] ret5 = new byte[3];
        assertEquals(3, heaps.readAt(5, ret5));
        assertArrayEquals(bar, ret5);

        byte[] foomix = "000foo0".getBytes();
        heaps.writeAt(0, foomix, 3, 3);

        byte[] ret0 = new byte[3];
        assertEquals(3, heaps.readAt(0, ret0));
        assertArrayEquals(foo, ret0);
    }

    @Test
    void write_read_buffer() {
        ByteBuffer fooBb = ByteBuffer.wrap("foo".getBytes());
        ByteBuffer barBb = ByteBuffer.wrap("bar".getBytes());

        heaps.writeAt(0, fooBb);
        heaps.writeAt(4, barBb);

        ByteBuffer ret0 = ByteBuffer.allocate(3);
        assertEquals(3, heaps.readAt(0, ret0));
        assertArrayEquals(fooBb.array(), ret0.array());

        ByteBuffer ret4 = ByteBuffer.allocate(3);
        assertEquals(3, heaps.readAt(4, ret4));
        assertArrayEquals(barBb.array(), ret4.array());
    }

    @Test
    void resizing() {
        byte a = 'a';
        heaps.attemptResizing(100);
        heaps.writeAt(100, a);

        assertEquals(a, heaps.readAt(100));

        byte[] foo = "foo".getBytes();
        heaps.attemptResizing(1_000);
        heaps.writeAt(900, foo);
        byte[] ret900 = new byte[3];
        assertEquals(3, heaps.readAt(900, ret900));
        assertArrayEquals(foo, ret900);
    }

}