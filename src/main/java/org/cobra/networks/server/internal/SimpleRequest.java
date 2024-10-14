package org.cobra.networks.server.internal;

import org.cobra.commons.Jvm;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.networks.Send;
import org.cobra.networks.requests.AbstractRequest;
import org.cobra.networks.requests.AbstractResponse;
import org.cobra.networks.requests.HeaderRequest;
import org.cobra.networks.server.ClientSession;
import org.cobra.networks.server.RequestContext;

import java.nio.ByteBuffer;

public class SimpleRequest {

    final int processorId;
    final RequestContext requestContext;
    final ClientSession clientSession;
    final MemoryAlloc memoryAlloc;

    public long responseCompleteAtNanos = Jvm.INF_TIMESTAMP;
    public long requestDequeAtNanos = Jvm.INF_TIMESTAMP;
    public long apiLocalCompleteAtNanos = Jvm.INF_TIMESTAMP;

    private final AbstractRequest.RequestAndSize requestAndSize;

    private ByteBuffer buffer;

    public SimpleRequest(
            int processorId,
            ByteBuffer buffer,
            RequestContext requestContext,
            MemoryAlloc memoryAlloc
    ) {
        this.processorId = processorId;
        this.buffer = buffer;
        this.requestContext = requestContext;
        this.memoryAlloc = memoryAlloc;

        this.clientSession = new ClientSession(requestContext.getPrincipal(), requestContext.getClientAddress());
        this.requestAndSize = requestContext.parseToRequestAndSize(buffer);
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public HeaderRequest getHeaderRequest() {
        return getRequestContext().getHeader();
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractRequest> T body() {
        try {
            return (T) requestAndSize.request();
        } catch (Exception e) {
            throw new ClassCastException("Unknown request type: " + requestAndSize.request().getClass().getTypeName());
        }
    }

    public Send toSend(AbstractResponse response) {
        return getRequestContext().toSend(response);
    }

    public void releaseBuffer() {
        if (buffer == null)
            return;

        memoryAlloc.release(buffer);
        buffer = null;
    }

    @Override
    public String toString() {
        return "RequestEvent(" +
                "apiLocalCompleteAtNanos=" + apiLocalCompleteAtNanos +
                ", processorId=" + processorId +
                ", requestContext=" + requestContext +
                ", clientSession=" + clientSession +
                ", memoryAlloc=" + memoryAlloc +
                ", responseCompleteAtNanos=" + responseCompleteAtNanos +
                ", requestDequeAtNanos=" + requestDequeAtNanos +
                ", requestAndSize=" + requestAndSize +
                ", buffer=" + buffer +
                ')';
    }
}
