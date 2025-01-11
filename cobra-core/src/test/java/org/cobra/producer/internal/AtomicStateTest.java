package org.cobra.producer.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtomicStateTest {

    @Test
    void lifecycle() {
        AtomicState atomic = AtomicState.initChain(0);

        assertThrows(IllegalStateException.class, atomic::rollback);

        atomic = atomic.stage(2);
        assertEquals(0, atomic.getCurrent().getVersion());
        assertEquals(2, atomic.getPending().getVersion());

        atomic = atomic.swap();
        assertEquals(2, atomic.getCurrent().getVersion());
        assertEquals(0, atomic.getPending().getVersion());

        atomic = atomic.commit();
        assertEquals(2, atomic.getCurrent().getVersion());

        assertThrows(IllegalStateException.class, atomic::rollback);

        atomic = atomic.stage(5);
        atomic = atomic.swap();
        atomic = atomic.commit();

        assertEquals(5, atomic.getCurrent().getVersion());
        assertNull(atomic.getPending());
    }

}