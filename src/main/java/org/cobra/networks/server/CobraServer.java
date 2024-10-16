package org.cobra.networks.server;

import org.cobra.commons.Clock;
import org.cobra.commons.configs.ConfigDef;
import org.cobra.commons.errors.CobraException;
import org.cobra.commons.pools.MemoryAlloc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class CobraServer implements Server {

    /**
     * The server state.
     * Expected transition of state:
     * NOT_RUNNING -> BOOTSTRAP -> RUNNING -> SHUTTING_DOWN
     */
    private enum State {
        /**
         * State that server be when firstly construct.
         */
        NOT_RUNNING,

        /**
         * State that server be when after call bootstrap.
         */
        BOOTSTRAP,

        /**
         * State that server be after all setup is done, indicate that server is running and accept clients/requests.
         */
        RUNNING,

        /**
         * State that server be after is killed/shutdown.
         */
        SHUTTING_DOWN;

        public boolean isShutdown() {
            return this == SHUTTING_DOWN || this == NOT_RUNNING;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(CobraServer.class);

    private final String id;
    private final Clock clock;
    private final MemoryAlloc memoryAlloc;
    private final ConfigDef configDef;

    private ChannelServer channelServer;
    private CobraApis dataPlaneApis;
    private RequestHandlerExecutor handlerExecutor;

    private CountDownLatch shutdownLatch = new CountDownLatch(1);
    private final AtomicReference<State> stateReferent = new AtomicReference<>(State.NOT_RUNNING);

    public CobraServer(Clock clock, MemoryAlloc memoryAlloc, ConfigDef configDef) {
        this.id = UUID.randomUUID().toString();
        this.memoryAlloc = memoryAlloc;
        this.clock = clock;
        this.configDef = configDef;
    }

    @Override
    public void bootstrap() {
        try {
            log.info("starting bootstrap the server network");

            // check if server completely bootstrap, end.
            if (stateReferent.get() == State.RUNNING)
                return;

            // check if server is shutting down, throw exception.
            if (stateReferent.get() == State.SHUTTING_DOWN)
                throw new IllegalStateException("rafale-network-server is shutting down, could not start");

            boolean shouldStart = stateReferent.compareAndSet(State.NOT_RUNNING, State.BOOTSTRAP);
            if (shouldStart) {
                stateReferent.set(State.BOOTSTRAP);
                log.info("bootstrap the server network; cluster_id: {}", id);

                int threadRuns = configDef.valueOf(DefaultServerConfigs.NUM_NETWORK_THREADS_CONFIG);

                /* construct channelServer */
                channelServer = new ChannelServer(UUID.randomUUID().toString(), clock, memoryAlloc, configDef, threadRuns);

                /* construct dataPlaneApis */
                dataPlaneApis = createApis(channelServer.getDataPlaneRequestChannel());

                /* submit handler executor with threadNum */
                handlerExecutor = new RequestHandlerExecutor(channelServer.getDataPlaneRequestChannel(),
                        dataPlaneApis, clock, threadRuns, SocketAcceptor.DataPlaneAcceptor.PREFIX);
                handlerExecutor.submit();
            }

            stateReferent.set(State.RUNNING);
            shutdownLatch = new CountDownLatch(1);

            log.info("bootstrap the server network completed; cluster_id: {}", id);
        } catch (Throwable cause) {
            log.error("error while bootstrap server network; cluster_id: {}", id, cause);
            throw new CobraException(cause);
        }
    }

    @Override
    public void shutdown() {
        try {
            log.info("shutting down the server network; cluster_id: {}", id);

            if (stateReferent.get() == State.BOOTSTRAP)
                throw new IllegalStateException("rafale-network-server is still bootstrap, could not be shutdown");

            boolean shouldKill = shutdownLatch.getCount() > 0 && stateReferent.get() != State.SHUTTING_DOWN;

            if (!shouldKill)
                return;

            handlerExecutor.close();
            dataPlaneApis.close();
            channelServer.close();

            stateReferent.set(State.NOT_RUNNING);
            shutdownLatch.countDown();

            log.info("kill the server network completed; cluster_id: {}", id);
        } catch (Throwable cause) {
            log.error("error while killing the server network", cause);
            stateReferent.set(State.NOT_RUNNING);
            throw new CobraException(cause);
        }
    }

    @Override
    public boolean isShutdown() {
        return stateReferent.get().isShutdown();
    }

    @Override
    public void awaitShutdown() throws InterruptedException {
        shutdownLatch.await();
    }

    private CobraApis createApis(RequestChanel requestChannel) {
        return new CobraApis(requestChannel);
    }
}
