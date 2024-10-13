package org.cobra.networks.client;

import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.requests.AbstractRequest;
import org.cobra.networks.requests.HeaderRequest;
import org.cobra.networks.requests.HeaderRequestMessage;
import org.cobra.networks.requests.RequestCompletionCallback;

public class ClientRequest {

    private final String destination;
    private final String clientId;
    private final long correlationId;
    private final long timeoutMs;
    private final long createdAtMs;
    private final AbstractRequest.Builder<?> requestBuilder;
    private final RequestCompletionCallback callback;

    public ClientRequest(
            String destination,
            String clientId,
            long correlationId,
            long timeoutMs,
            long createdAtMs,
            AbstractRequest.Builder<?> requestBuilder,
            RequestCompletionCallback callback) {
        this.destination = destination;
        this.clientId = clientId;
        this.correlationId = correlationId;
        this.timeoutMs = timeoutMs;
        this.createdAtMs = createdAtMs;
        this.requestBuilder = requestBuilder;
        this.callback = callback;
    }

    public Apikey apikey() {
        return requestBuilder.apikey();
    }

    public RequestCompletionCallback getCallback() {
        return callback;
    }

    public String getClientId() {
        return clientId;
    }

    public long getCorrelationId() {
        return correlationId;
    }

    public long getCreatedAtMs() {
        return createdAtMs;
    }

    public String getDestination() {
        return destination;
    }

    public AbstractRequest.Builder<?> getRequestBuilder() {
        return requestBuilder;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public HeaderRequest toHeaderRequest() {
        Apikey apikey = apikey();
        return new HeaderRequest(new HeaderRequestMessage(apikey.id(), correlationId, clientId));
    }

    @Override
    public String toString() {
        return "ClientRequest(" +
                "callback=" + callback +
                ", destination='" + destination + '\'' +
                ", clientId='" + clientId + '\'' +
                ", correlationId=" + correlationId +
                ", timeoutMs=" + timeoutMs +
                ", createdAtMs=" + createdAtMs +
                ", requestBuilder=" + requestBuilder +
                ')';
    }
}
