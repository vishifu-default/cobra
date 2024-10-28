package org.cobra.producer.internal;

import org.cobra.commons.Clock;
import org.cobra.producer.CobraProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerStateWriter implements CobraProducer.StateWriter, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ProducerStateWriter.class);
    private volatile boolean isClosed = false;

    private final ProducerSchemaDelegated schemaWriteDelegate;
    private final long version;
    private final Clock clock;

    public ProducerStateWriter(ProducerSchemaDelegated schemaWriteDelegate, long version, Clock clock) {
        this.schemaWriteDelegate = schemaWriteDelegate;
        this.version = version;
        this.clock = clock;
    }

    @Override
    public void putObject(String key, Object object) {
        requireNotClosed();
        this.schemaWriteDelegate.addObject(key, object);
    }

    @Override
    public void removeObject(String key, Class<?> clazz) {
        requireNotClosed();
        this.schemaWriteDelegate.removeObject(key, clazz);
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
        return "ScopeProducerState()";
    }
}
