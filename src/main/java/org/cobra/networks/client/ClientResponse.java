package org.cobra.networks.client;

import org.cobra.commons.errors.AuthenticationException;
import org.cobra.networks.requests.AbstractResponse;
import org.cobra.networks.requests.HeaderRequest;
import org.cobra.networks.requests.RequestCompletionCallback;

public class ClientResponse {

    private final String destination;
    private final long receivedTimeMillis;
    private final long latencyMillis;
    private final boolean disconnected;
    private final boolean hasTimeout;
    private final AuthenticationException authenticationException;
    private final HeaderRequest header;
    private final RequestCompletionCallback callback;
    private final AbstractResponse responseBody;

    public ClientResponse(
            String destination,
            long receivedTimeMillis,
            long createdTimeMillis,
            boolean disconnected,
            boolean hasTimeout,
            AuthenticationException authenticationException,
            HeaderRequest header,
            RequestCompletionCallback callback,
            AbstractResponse responseBody) {
        this.destination = destination;
        this.receivedTimeMillis = receivedTimeMillis;
        this.latencyMillis = receivedTimeMillis - createdTimeMillis;
        this.disconnected = disconnected;
        this.hasTimeout = hasTimeout;
        this.authenticationException = authenticationException;
        this.header = header;
        this.callback = callback;
        this.responseBody = responseBody;
    }

    public ClientResponse(
            String destination,
            long createdAtMs,
            long receivedAtMs,
            boolean disconnected,
            AuthenticationException authenticationException,
            HeaderRequest header,
            AbstractResponse responseBody,
            RequestCompletionCallback callback
    ) {
        this(destination, createdAtMs, receivedAtMs, disconnected, false,
                authenticationException, header, callback, responseBody);
    }

    public long getReceivedTimeMillis() {
        return receivedTimeMillis;
    }

    public long getLatencyMillis() {
        return latencyMillis;
    }

    public boolean wasDisconnected() {
        return disconnected;
    }

    public boolean wasTimeout() {
        return hasTimeout;
    }

    public AuthenticationException getAuthenticationException() {
        return authenticationException;
    }

    public HeaderRequest getHeader() {
        return header;
    }

    public AbstractResponse getResponseBody() {
        return responseBody;
    }

    public void onComplete() throws Exception {
        if (callback != null)
            callback.onComplete();
    }

    @Override
    public String toString() {
        return "ClientResponse(" +
                "destination='" + destination + '\'' +
                ", disconnected=" + disconnected +
                ", latencyMillis=" + latencyMillis +
                ", receivedTimeMillis=" + receivedTimeMillis +
                ", responseBody=" + responseBody +
                ", header=" + header +
                ')';
    }
}
