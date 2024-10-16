package org.cobra.networks.server;

import org.cobra.commons.Clock;
import org.cobra.commons.Jvm;
import org.cobra.networks.Send;
import org.cobra.networks.requests.AbstractRequest;
import org.cobra.networks.requests.AbstractResponse;
import org.cobra.networks.requests.HeaderRequest;
import org.cobra.networks.server.internal.LocalCloseResponse;
import org.cobra.networks.server.internal.Response;
import org.cobra.networks.server.internal.SimpleRequest;
import org.cobra.networks.server.internal.SimpleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RequestChanel implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(RequestChanel.class);

    private final Clock clock;
    private final Map<Integer, Processor> socketProcessors;
    private final BlockingQueue<SimpleRequest> eventQueue;

    public RequestChanel(
            Clock clock,
            int queuedSize
    ) {
        this.clock = clock;
        this.socketProcessors = new ConcurrentHashMap<>();
        this.eventQueue = new ArrayBlockingQueue<>(queuedSize);
    }

    public void addProcessor(Processor processor) {
        Processor prev = socketProcessors.putIfAbsent(processor.id(), processor);
        if (prev != null)
            log.warn("could not add this processor due to duplication; processor: {}", processor);
    }

    public void removeProcessor(int id) {
        socketProcessors.remove(id);
    }

    public void event(SimpleRequest event) throws InterruptedException {
        eventQueue.put(event);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public void sendResponse(
            SimpleRequest simpleRequest,
            AbstractResponse responseBody,
            Optional<Consumer<Send>> onCompleteCallback
    ) {
        doSendResponse(new SimpleResponse(simpleRequest, simpleRequest.toSend(responseBody), onCompleteCallback));
    }

    public void sendErrorResponse(SimpleRequest simpleRequest, Throwable cause) {
        AbstractRequest requestBody = simpleRequest.body();
        AbstractResponse responseBody = requestBody.toErrorResponse(cause);

        if (responseBody == null)
            closeSocketConnection(simpleRequest);
        else
            sendResponse(simpleRequest, responseBody, Optional.empty());
    }

    public SimpleRequest takeRequest() throws InterruptedException {
        return eventQueue.take();
    }

    public void clear() {
        eventQueue.clear();
    }

    private void closeSocketConnection(SimpleRequest simpleRequest) {
        doSendResponse(new LocalCloseResponse(simpleRequest));
    }

    private void doSendResponse(Response response) {
        final SimpleRequest simpleRequest = response.getRequest();
        final long nowNanos = clock.nanoseconds();

        if (simpleRequest.apiLocalCompleteAtNanos == Jvm.INF_TIMESTAMP)
            simpleRequest.apiLocalCompleteAtNanos = nowNanos;

        Processor processor = socketProcessors.get(response.processorId());
        if (processor != null) {
            try {
                processor.offerResponse(response);
            } catch (InterruptedException e) {
                log.error("error while offer response to processor; process: {}; resposen: {}",
                        processor, response);
            }
        }

        HeaderRequest headerRequest = response.getRequest().getHeaderRequest();
        log.trace("sending response {} to client {}; size: {}", headerRequest.apikey(), headerRequest.clientId(),
                response);
    }

    @Override
    public void close() throws Exception {
        clear();
    }

    @Override
    public String toString() {
        return "RequestChanel(" +
                "clock=" + clock +
                ", socketProcessors=" + socketProcessors +
                ", eventQueue=" + eventQueue +
                ')';
    }
}
