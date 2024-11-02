package org.benchmark.memory;

import org.cobra.commons.pools.BytesPool;
import org.cobra.core.memory.internal.OnHeapBytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Using NONE pool to allocate
 */
public class OnHeapStoreBenchmark {

    private static final Logger log = LoggerFactory.getLogger(OnHeapStoreBenchmark.class);

    //2024-11-03T15:38:47.009 [INFO ]  - Elapsed time writing 2^22 bytes: 2527ms
    //2024-11-03T15:38:47.851 [INFO ]  - Elapsed time reading 2^22 bytes: 842ms
    public static void main(String[] args) throws IOException {
        OnHeapBytes store = new OnHeapBytes(14, BytesPool.NONE);
        long count = 1 << 30;
        long start = System.currentTimeMillis();
        for (long i = 0; i < count; i++) {
            store.write((byte) i);
        }
        long elapsed = System.currentTimeMillis() - start;
        log.info("Elapsed time writing 2^30 bytes (1Gib): {}ms", elapsed);

        store.seek(0);
        start = System.currentTimeMillis();
        for (long i = 0; i < count; i++) {
            store.read();
        }
        elapsed = System.currentTimeMillis() - start;
        log.info("Elapsed time reading 2^30 bytes (1Gib): {}ms", elapsed);
    }

}
