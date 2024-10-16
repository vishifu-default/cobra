package org.cobra.networks.server;

import org.cobra.commons.Clock;
import org.cobra.commons.threads.CobraThread;
import org.cobra.networks.server.internal.ApiHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestHandlerExecutor implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(RequestHandlerExecutor.class);

    private final RequestChanel requestChanelPlane;
    private final ApiHandler apiHandler;
    private final Clock clock;

    private final String threadPrefix;
    private final AtomicInteger threadPoolSize;
    private final List<RequestHandler> handlerRunnables;

    public RequestHandlerExecutor(
            RequestChanel requestChanel,
            ApiHandler apiHandler,
            Clock clock,
            int threadNum,
            String threadPrefix
    ) {
        this.threadPrefix = threadPrefix;
        this.threadPoolSize = new AtomicInteger(threadNum);
        this.requestChanelPlane = requestChanel;
        this.clock = clock;
        this.apiHandler = apiHandler;

        this.handlerRunnables = new ArrayList<>();
    }

    public void submit() {
        for (int i = 0; i < threadPoolSize.get(); i++)
            startHandler(i);
    }

    public synchronized void startHandler(int id) {
        handlerRunnables.add(new RequestHandler(id, clock, requestChanelPlane, apiHandler));
        CobraThread.daemon(handlerRunnables.get(id), threadPrefix).start();
    }

    public synchronized void resizePoolSize(int newSize) {
        final int currentSize = threadPoolSize.get();
        log.info("resizing request handler thread-pool size from {} to {}", currentSize, newSize);

        if (newSize > currentSize)
            for (int i = currentSize; i < newSize; i++)
                startHandler(i);
        else
            for (int i = 1; i < (currentSize - newSize); i++)
                handlerRunnables.remove(currentSize - i).close();

        threadPoolSize.set(newSize);
    }

    @Override
    public void close() throws Exception {
        log.info("closing executor");
        for (RequestHandler handler : handlerRunnables)
            handler.close();

        for (RequestHandler handler : handlerRunnables)
            handler.awaitClosing();

        log.info("closed executor");
    }
}
