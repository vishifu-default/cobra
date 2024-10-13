package org.cobra.networks.client;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A class for keeping members and providing the value of exponential retry backoff, exponential reconnect backoff, ...
 * <p>
 * formula:
 * Backoff(attempts) = random(1 - jitter, 1 + jitter) * interval * multiplier ^ attempts
 */
public class ExponentialBackoff {
    private final long initialInterval;
    private final long maxInterval;
    private final double jitter;
    private final double exponentMax;
    private final int multiplier;

    public ExponentialBackoff(long initialInterval, long maxInterval, double jitter, int multiplier) {
        this.initialInterval = initialInterval;
        this.maxInterval = maxInterval;
        this.jitter = jitter;
        this.multiplier = multiplier;
        this.exponentMax = maxInterval > initialInterval
                ? Math.log(maxInterval / (double) Math.max(initialInterval, 1)) / Math.log(multiplier)
                : 0;
    }

    public long getInitialInterval() {
        return this.initialInterval;
    }

    public long backoff(long attempts) {
        if (this.exponentMax == 0)
            return initialInterval;

        double exp = Math.min(attempts, this.exponentMax);
        double term = initialInterval * Math.pow(multiplier, exp);
        double randomFactor = jitter < Double.MIN_NORMAL ? 1.0 : ThreadLocalRandom.current().nextDouble(1 - jitter,
                1 + jitter);
        long backoffValue = (long) (randomFactor * term);
        return Math.min(backoffValue, maxInterval);
    }

    @Override
    public String toString() {
        return "ExponentialBackoff(" +
                "exponentMax=" + exponentMax +
                ", initialInterval=" + initialInterval +
                ", maxInterval=" + maxInterval +
                ", jitter=" + jitter +
                ", multiplier=" + multiplier +
                ')';
    }
}
