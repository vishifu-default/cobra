package org.cobra.networks.server;

public interface Server {
    void bootstrap();

    void shutdown();

    boolean isShutdown();

    void awaitShutdown() throws InterruptedException;
}
