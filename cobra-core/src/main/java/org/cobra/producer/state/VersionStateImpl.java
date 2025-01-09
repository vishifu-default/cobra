package org.cobra.producer.state;

import org.cobra.producer.CobraProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class VersionStateImpl implements CobraProducer.VersionState {

    private static final Logger log = LoggerFactory.getLogger(VersionStateImpl.class);
    private final AtomicLong version = new AtomicLong(0);

    @Override
    public long mint() {
        return this.version.incrementAndGet();
    }

    @Override
    public long current() {
        return this.version.get();
    }

    @Override
    public void pin(long version) {
        log.info("version is pinned to {}", version);
        this.version.set(version);
    }
}
