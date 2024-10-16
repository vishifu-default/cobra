package org.cobra.networks.server;

public interface Acceptor extends Runnable, AutoCloseable {

    /**
     * Start this acceptor to run in another thread.
     * Open a {@link java.nio.channels.ServerSocketChannel} for given endpoint.
     * Start all Processor that are present.
     * Start main thread
     */
    void start();

    /**
     * Interrupt current main thread.
     */
    void shutdown();

    /**
     * Close all resources of this instance
     */
    void closeAll();
}
