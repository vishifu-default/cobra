package org.cobra.producer.internal;

import org.cobra.commons.Clock;
import org.cobra.producer.CobraProducer;
import org.cobra.producer.state.ProducerStateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateWriteProvider implements CobraProducer.StateWriter, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(StateWriteProvider.class);
    private volatile boolean isClosed = false;

    private final ProducerStateContext stateContext;
    private final long version;
    private final Clock clock;

    public StateWriteProvider(ProducerStateContext stateContext, long version, Clock clock) {
        this.stateContext = stateContext;
        this.version = version;
        this.clock = clock;
    }

    @Override
    public void addObject(String key, Object object) {
        requireNotClosed();
        this.stateContext.addObject(key, object);
    }

    @Override
    public void removeObject(String key, Class<?> clazz) {
        requireNotClosed();
        this.stateContext.removeObject(key, clazz);
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public void close() throws Exception {
        log.debug("closing {} at {}ms", this, this.clock.milliseconds());
        this.isClosed = true;
    }

    private void requireNotClosed() {
        if (this.isClosed)
            throw new IllegalStateException("Attempt to operate on closed producer");
    }

    @Override
    public String toString() {
        return "ProducerStateWriter(isClosed=%s, version=%d)".formatted(isClosed, version);
    }
}
