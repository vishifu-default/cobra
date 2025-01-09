package org.cobra.core.bytes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NativeBytesStoreTest {

    NativeBytesStore nativeStore;

    @BeforeEach
    void setUp() {
        nativeStore = NativeBytesStore.create(3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initiate() {
        final NativeBytesStore nativeStore1 = NativeBytesStore.create(3);
        assertNotNull(nativeStore1);
        assertEquals(8, nativeStore1.size());
    }

    @Test
    void write_read() {
        byte a = 'a';
        byte b = 'b';

        nativeStore.writeAt(1, a);
        nativeStore.writeAt(2, b);

        assertEquals(a, nativeStore.readAt(1));
        assertEquals(b, nativeStore.readAt(2));
    }

    @Test
    void write_read_array() {
        byte[] foo = "foo".getBytes();
        byte[] bar = "bar".getBytes();

        nativeStore.writeAt(1, foo);
        nativeStore.writeAt(4, bar);

        byte[] ret1 = new byte[3];
        assertEquals(3, nativeStore.readAt(1, ret1));
        assertArrayEquals(foo, ret1);

        byte[] ret4 = new byte[3];
        assertEquals(3, nativeStore.readAt(4, ret4));
        assertArrayEquals(bar, ret4);

        byte[] foomix = "000foo0".getBytes();
        nativeStore.writeAt(1, foomix, 3, 3);

        assertEquals(3, nativeStore.readAt(1, ret1));
        assertArrayEquals(foo, ret1);
    }

    @Test
    void write_read_buffer() {
        ByteBuffer fooBb = ByteBuffer.wrap("foo".getBytes());
        ByteBuffer barBb = ByteBuffer.wrap("---bar-".getBytes());

        nativeStore.writeAt(1, fooBb);
        nativeStore.writeAt(4, barBb, 3, 3);

        ByteBuffer ret1 = ByteBuffer.allocate(3);
        assertEquals(3, nativeStore.readAt(1, ret1));
        assertArrayEquals("foo".getBytes(), ret1.array());

        ByteBuffer ret4 = ByteBuffer.allocate(3);
        assertEquals(3, nativeStore.readAt(4, ret4));
        assertArrayEquals("bar".getBytes(), ret4.array());
    }

    @Test
    void writeOutBound_shouldThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            nativeStore.writeAt(100L, (byte) 'a');
        });
    }

    @Test
    void resizing() {
        byte a = 'a';

        nativeStore.attemptResizing(100);
        nativeStore.writeAt(100, a);

        assertEquals(a, nativeStore.readAt(100));
    }
}