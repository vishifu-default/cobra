package org.cobra.networks.server.internal;

public class LocalCloseResponse extends Response {

    public LocalCloseResponse(SimpleRequest simpleRequest) {
        super(simpleRequest);
    }

    @Override
    public String toString() {
        return "LocalCloseResponse()";
    }
}
