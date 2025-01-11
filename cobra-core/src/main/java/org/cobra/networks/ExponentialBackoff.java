package org.cobra.networks;

import java.util.concurrent.ThreadLocalRandom;

/**
 * https://en.wikipedia.org/wiki/Exponential_backoff
 */
public class ExponentialBackoff {

    private static final int RETRY_BACKOFF_EXP_BASE = 2;
    private static final double RETRY_BACKOFF_JITTER = 0.2;

    private final long initialInterval;
    private final long maxInterval;
    private final int multiplier;
    private final double jitter;
    private final double exponent;

    public ExponentialBackoff(long initialInterval, long maxInterval) {
        this(initialInterval, maxInterval, RETRY_BACKOFF_EXP_BASE, RETRY_BACKOFF_JITTER);
    }

    public ExponentialBackoff(long initialInterval, long maxInterval, int multiplier, double jitter) {
        this.initialInterval = initialInterval;
        this.maxInterval = maxInterval;
        this.multiplier = multiplier;
        this.jitter = jitter;
        this.exponent = maxInterval > initialInterval ?
                Math.log(maxInterval / (double) Math.max(initialInterval, 1)) / Math.log(multiplier) : 0;
    }

    public long backoff(int attempts) {
        if (exponent == 0)
            return initialInterval;

        double exp = Math.min(attempts, exponent);
        double term = initialInterval * Math.pow(multiplier, exp);
        double randFactor = jitter < Double.MIN_NORMAL ? 1.0 :
                ThreadLocalRandom.current().nextDouble(1 - jitter, 1 + jitter);

        long backoffVale = (long) (randFactor * term);
        return Math.min(backoffVale, maxInterval);
    }

    @Override
    public String toString() {
        return "ExponentialBackoff(" +
                "initialInterval=" + initialInterval +
                ", maxInterval=" + maxInterval +
                ", multiplier=" + multiplier +
                ", jitter=" + jitter +
                ", exponent=" + exponent +
                ')';
    }
}
