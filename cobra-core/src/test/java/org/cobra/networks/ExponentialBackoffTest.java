package org.cobra.networks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExponentialBackoffTest {

    @Test
    void exponentialBackoff() {
        long scaleFactor = 100;
        long backoffMax = 2_000;
        int ratio = 2;
        double jitter = 0.2;
        final ExponentialBackoff exponentialBackoff = new ExponentialBackoff(scaleFactor, backoffMax, ratio, jitter);

        for (int i = 0; i < 100; i++) {
            for (int attempt = 0; attempt <= 10; attempt++) {
                if (attempt <= 4) {
                    assertEquals(scaleFactor * Math.pow(ratio, attempt), exponentialBackoff.backoff(attempt),
                            scaleFactor * Math.pow(ratio, attempt) * jitter);
                } else {
                    assertTrue(exponentialBackoff.backoff(attempt) <= backoffMax * (1 + jitter));
                }
            }
        }
    }

    @Test
    void withoutJitter() {
       final ExponentialBackoff exponentialBackoff = new ExponentialBackoff(100, 400, 2, 0.0);
       assertEquals(100, exponentialBackoff.backoff(0));
       assertEquals(200, exponentialBackoff.backoff(1));
       assertEquals(400, exponentialBackoff.backoff(2));
       assertEquals(400, exponentialBackoff.backoff(3));
       assertEquals(400, exponentialBackoff.backoff(4));
    }

}