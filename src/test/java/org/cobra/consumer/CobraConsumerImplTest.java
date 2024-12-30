package org.cobra.consumer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CobraConsumerImplTest {

    @Test
    void initialize() {
        CobraConsumer consumer = CobraConsumer.fromBuilder().build();

        assertNotNull(consumer);
    }

}