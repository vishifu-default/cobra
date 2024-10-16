package org.cobra.commons.pools;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class BufferSupplierTest {

    @Test
    public void testAutoGrow() {
        AutoGrowBufferSupplier bufferSupplier = new AutoGrowBufferSupplier();

        ByteBuffer bf1 = bufferSupplier.get(1024);
        assertEquals(0, bf1.position());
        assertEquals(1024, bf1.capacity());
        bufferSupplier.release(bf1);

        bufferSupplier.close();
    }

    @Test
    public void testAutoGrow_release() {

        AutoGrowBufferSupplier bufferSupplier = new AutoGrowBufferSupplier();

        ByteBuffer bf1 = bufferSupplier.get(1024);
        bufferSupplier.release(bf1);

        ByteBuffer bf2 = bufferSupplier.get(512);
        assertSame(bf1, bf2);

        ByteBuffer bf3 = bufferSupplier.get(4096);
        assertNotSame(bf1, bf3);
        assertEquals(0, bf3.position());
        assertEquals(4096, bf3.capacity());

        bufferSupplier.close();
    }
}
