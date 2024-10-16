package org.cobra.networks.server.internal;

import org.cobra.networks.Send;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SimpleResponse extends Response {

    private final Send send;
    private final Optional<Consumer<Send>> onCompleteCallback;

    public SimpleResponse(
            SimpleRequest simpleRequest,
            Send send,
            Optional<Consumer<Send>> onCompleteCallback) {
        super(simpleRequest);
        this.send = send;
        this.onCompleteCallback = onCompleteCallback;
    }

    @Override
    public Optional<Consumer<Send>> onCompleteCallback() {
        return onCompleteCallback;
    }

    public Send getSend() {
        return send;
    }

    @Override
    public String toString() {
        return "SimpleResponse{" +
                "onCompleteCallback=" + onCompleteCallback +
                ", send=" + send +
                '}';
    }
}
