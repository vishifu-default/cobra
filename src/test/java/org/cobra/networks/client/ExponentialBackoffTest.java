package org.cobra.networks.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExponentialBackoffTest {

    @Test
    public void testExponentialBackoff() {
        int scaleFactor = 100;
        int ratio = 2;
        int backoffMax = 2000;
        double jitter = .2f;

        ExponentialBackoff exponentBackoff = new ExponentialBackoff(scaleFactor, backoffMax, jitter, ratio);

        for (int i = 0; i < 100; i++) {
            for (int attempt = 0; attempt < 10; attempt++) {
                if (attempt < 4)
                    assertEquals(scaleFactor * Math.pow(ratio, attempt), exponentBackoff.backoff(attempt),
                            scaleFactor * Math.pow(ratio, attempt) * jitter);
                else
                    assertTrue(exponentBackoff.backoff(attempt) <= backoffMax * (1 + jitter));
            }
        }
    }

    @Test
    public void testExponentBackoff_withoutJitter() {
        ExponentialBackoff exponentialBackoff = new ExponentialBackoff(100, 400, 0.0, 2);
        assertEquals(100, exponentialBackoff.backoff(0));
        assertEquals(200, exponentialBackoff.backoff(1));
        assertEquals(400, exponentialBackoff.backoff(2));
        assertEquals(400, exponentialBackoff.backoff(3));
    }
}
