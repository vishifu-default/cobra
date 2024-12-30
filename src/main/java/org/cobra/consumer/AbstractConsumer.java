package org.cobra.consumer;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractConsumer implements CobraConsumer {

    protected final ReadWriteLock fetchLock = new ReentrantReadWriteLock();

    protected AbstractConsumer() {
    }

    protected void runFetchVersion() {
        throw new UnsupportedOperationException();
    }
}
