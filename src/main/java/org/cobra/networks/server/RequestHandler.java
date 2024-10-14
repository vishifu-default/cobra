package org.cobra.networks.server;

import org.cobra.commons.Clock;
import org.cobra.commons.errors.CobraException;
import org.cobra.networks.server.internal.ApiHandler;
import org.cobra.networks.server.internal.SimpleRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class RequestHandler implements Runnable, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final int id;
    private final RequestChanel requestChanel;
    private final Clock clock;
    private final ApiHandler apiHandler;

    private final CountDownLatch closingLatch = new CountDownLatch(1);
    private boolean isClosed = false;

    public RequestHandler(
            int id,
            Clock clock,
            RequestChanel requestChanel,
            ApiHandler apiHandler
    ) {
        this.id = id;
        this.clock = clock;
        this.requestChanel = requestChanel;
        this.apiHandler = apiHandler;
    }

    @Override
    public void run() {
        while (!isClosed) {
            SimpleRequest simpleRequest;
            try {
                simpleRequest = requestChanel.takeRequest();
            } catch (InterruptedException e) {
                log.error("interrupt while receive request from {}", requestChanel, e);
                throw new CobraException("Interrupt while receive request", e);
            }

            try {
                log.trace("handling request {}; handler_id: {};", simpleRequest, id);
                simpleRequest.requestDequeAtNanos = clock.nanoseconds();
                apiHandler.handle(simpleRequest);
            } catch (Throwable cause) {
                log.error("error while handling request {}", simpleRequest, cause);
            } finally {
                simpleRequest.releaseBuffer();
            }
        }

        closeAll();
    }

    public void awaitClosing() throws InterruptedException {
        closingLatch.await();
    }

    public void closeAll() {
        try {
            closingLatch.countDown();
        } catch (Exception e) {
            throw new CobraException(e);
        }
    }

    @Override
    public void close() {
        isClosed = true;
    }

}
