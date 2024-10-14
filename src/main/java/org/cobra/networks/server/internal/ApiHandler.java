package org.cobra.networks.server.internal;

public interface ApiHandler extends AutoCloseable {

    void handle(SimpleRequest simpleRequest);

    default void tryCompleteAction() {
        // nop
    }
}
