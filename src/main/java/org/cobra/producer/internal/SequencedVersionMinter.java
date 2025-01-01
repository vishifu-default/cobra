package org.cobra.producer.internal;

import org.cobra.producer.CobraProducer;

import java.util.concurrent.atomic.AtomicLong;

public class SequencedVersionMinter implements CobraProducer.VersionMinter {

    private static final long INITIAL_VALUE = 0;
    private static final AtomicLong versionCounter = new AtomicLong(INITIAL_VALUE);

    @Override
    public long mint() {
        return versionCounter.incrementAndGet();
    }
}
