package org.cobra.networks.server.internal;

import org.cobra.networks.Send;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class Response {

    final SimpleRequest simpleRequest;

    protected Response(SimpleRequest simpleRequest) {
        this.simpleRequest = simpleRequest;
    }

    public SimpleRequest getRequest() {
        return simpleRequest;
    }

    public int processorId() {
        return simpleRequest.processorId;
    }

    public Optional<Consumer<Send>> onCompleteCallback() {
        return Optional.empty();
    }
}
