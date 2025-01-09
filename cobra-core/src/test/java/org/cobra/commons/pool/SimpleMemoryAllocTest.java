package org.cobra.commons.pool;

import org.cobra.commons.pools.SimpleMemoryAlloc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleMemoryAllocTest {


    private SimpleMemoryAlloc memoryPool;

    @BeforeEach
    public void setup() {
        memoryPool = new SimpleMemoryAlloc(100);
    }

    @AfterEach
    public void tearDown() throws IOException {
        memoryPool.close();
    }

    @Test
    public void testMaxSingleAlloc_zero() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleMemoryAlloc(0));
    }

    @Test
    public void testAllocate_negativeSize() {
        assertThrows(IllegalArgumentException.class, () -> memoryPool.allocate(-1));
    }

    @Test
    public void testAllocate_overMaxSingleAlloc() {
        assertThrows(IllegalArgumentException.class, () -> memoryPool.allocate(1000));
    }

    @Test
    public void testRelease_nullBuffer() {
        assertThrows(IllegalArgumentException.class, () -> memoryPool.release(null));
    }

    @Test
    public void testRelease_foreignBuffer() {
        ByteBuffer another = ByteBuffer.allocate(100);
        assertThrows(IllegalStateException.class, () -> memoryPool.release(another));
    }
}
