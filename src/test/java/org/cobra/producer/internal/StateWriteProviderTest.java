package org.cobra.producer.internal;

import org.cobra.commons.Clock;
import org.cobra.producer.state.ProducerStateContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateWriteProviderTest {

    @Test
    void open_close() {
        try (StateWriteProvider wp = new StateWriteProvider(new ProducerStateContext(), 1,
                Clock.system())) {
            assertEquals(1, wp.getVersion());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}