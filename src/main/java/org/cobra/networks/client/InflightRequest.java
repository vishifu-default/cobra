package org.cobra.networks.client;

import org.cobra.networks.Send;
import org.cobra.networks.requests.AbstractRequest;
import org.cobra.networks.requests.AbstractResponse;
import org.cobra.networks.requests.HeaderRequest;
import org.cobra.networks.requests.RequestCompletionCallback;

public class InflightRequest {

    final HeaderRequest headerRequest;
    final AbstractRequest request;
    final Send send;
    final RequestCompletionCallback completionCallback;
    final String destination;
    final long sentAtMs;
    final long createdAtMs;
    final long timeoutMs;

    public InflightRequest(
            ClientRequest clientRequest,
            HeaderRequest headerRequest,
            AbstractRequest request,
            Send send,
            long sendAtMs) {
        this(
                headerRequest,
                request,
                send,
                clientRequest.getCallback(),
                clientRequest.getDestination(),
                sendAtMs,
                clientRequest.getCreatedAtMs(),
                clientRequest.getTimeoutMs());
    }

    public InflightRequest(
            HeaderRequest headerRequest,
            AbstractRequest request,
            Send send,
            RequestCompletionCallback completionCallback,
            String destination,
            long sentAtMs,
            long createdAtMs,
            long timeoutMs) {
        this.headerRequest = headerRequest;
        this.request = request;
        this.send = send;
        this.completionCallback = completionCallback;
        this.destination = destination;
        this.sentAtMs = sentAtMs;
        this.createdAtMs = createdAtMs;
        this.timeoutMs = timeoutMs;
    }

    public RequestCompletionCallback getCompletionCallback() {
        return completionCallback;
    }

    public long getCreatedAtMs() {
        return createdAtMs;
    }

    public String getDestination() {
        return destination;
    }

    public HeaderRequest getHeaderRequest() {
        return headerRequest;
    }

    public AbstractRequest getRequest() {
        return request;
    }

    public Send getSend() {
        return send;
    }

    public long getSentAtMs() {
        return sentAtMs;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public long getElapsedMs(long nowMs) {
        return Math.max(0, nowMs - createdAtMs);
    }

    public long getElapsedMsFromSend(long nowMs) {
        return Math.max(0, nowMs - sentAtMs);
    }

    public ClientResponse toCompleted(AbstractResponse response, long nowMs) {
        return new ClientResponse(destination, createdAtMs, nowMs, false,
                null, headerRequest, response, completionCallback);
    }

    public ClientResponse toTimeout(long nowMs) {
        return new ClientResponse(destination, createdAtMs, nowMs, true,
                null, headerRequest, null, completionCallback);
    }

    public ClientResponse toDisconnected(long nowMs) {
        return new ClientResponse(destination, createdAtMs, nowMs, true,
                null, headerRequest, null, completionCallback);
    }
}
