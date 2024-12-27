package org.cobra.core.memory.slab;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class FreelistTest {

    @Test
    void offer_poll() {
        final Freelist freelist = new Freelist();
        SlabOffset offset1 = new SlabOffset(1, 1, 100);

        assertNull(freelist.poll());

        freelist.offer(offset1);
        SlabOffset offset2 = freelist.poll();
        assertSame(offset1, offset2);

        freelist.offer(offset1);

        SlabOffset offset3 = new SlabOffset(1, 1, 200);
        freelist.offer(offset3);
        SlabOffset offset4 = new SlabOffset(1, 1, 300);
        freelist.offer(offset4);

        assertSame(offset4, freelist.poll());
        assertSame(offset3, freelist.poll());
        assertSame(offset1, freelist.poll());
    }

}