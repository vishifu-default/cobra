package org.cobra.networks.server;

import org.cobra.commons.Clock;
import org.cobra.commons.configs.ConfigDef;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.networks.Endpoint;
import org.cobra.networks.auth.SecurityProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ChannelServer implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ChannelServer.class);

    private final String id;
    private final Clock clock;
    private final MemoryAlloc memoryAlloc;
    private final ConfigDef configDef;
    private final RequestChanel dataPlaneRequestChannel;
    private final Map<Endpoint, SocketAcceptor.DataPlaneAcceptor> dataPlaneAcceptors;

    private final AtomicInteger processorIdCounter = new AtomicInteger(0);
    private final AtomicBoolean killedReferent = new AtomicBoolean(false);

    public ChannelServer(
            String id,
            Clock clock,
            MemoryAlloc memoryAlloc,
            ConfigDef configDef,
            int processorNum
    ) {
        this.id = id;
        this.clock = clock;
        this.memoryAlloc = memoryAlloc;
        this.configDef = configDef;

        this.dataPlaneAcceptors = new ConcurrentHashMap<>();

        int queuedCapacity = configDef.valueOf(DefaultServerConfigs.SOCKET_QUEUED_CAPACITY_CONF);
        this.dataPlaneRequestChannel = new RequestChanel(clock, queuedCapacity);

        {
            String host = configDef.valueOf(DefaultServerConfigs.ENDPOINT_HOST_CONF);
            int port = configDef.valueOf(DefaultServerConfigs.ENDPOINT_PORT_CONF);
            SecurityProtocol securityProtocol =
                    SecurityProtocol.ofName(configDef.valueOf(DefaultServerConfigs.ENDPOINT_SECURITY_PROTOCOL_CONF));

            Endpoint endpoint = new Endpoint(host, port, securityProtocol);
            createDataPlaneAcceptorAndProcessor(endpoint, processorNum);
        }
    }

    int assignNextProcessorId() {
        return processorIdCounter.incrementAndGet();
    }

    String id() {
        return id;
    }

    RequestChanel getDataPlaneRequestChannel() {
        return dataPlaneRequestChannel;
    }

    public synchronized void stopProcessingRequests() {
        if (killedReferent.get())
            return;

        killedReferent.set(true);
        dataPlaneAcceptors.values().forEach(SocketAcceptor.DataPlaneAcceptor::shutdown);
        dataPlaneAcceptors.values().forEach(SocketAcceptor::shutdown);

        dataPlaneAcceptors.clear();

        log.info("channel-server stop processing request; id: {}", id());
    }

    @Override
    public void close() throws Exception {
        log.info("closing channel-server {}", id());

        synchronized (this) {
            stopProcessingRequests();
            dataPlaneRequestChannel.close();
        }

        log.info("closing channel-server {} completely", id());
    }

    private synchronized void createDataPlaneAcceptorAndProcessor(Endpoint endpoint, int processorNum) {
        if (killedReferent.get())
            return;

        SocketAcceptor.DataPlaneAcceptor dataPlaneAcceptor = new SocketAcceptor.DataPlaneAcceptor(
                UUID.randomUUID().toString(), this, endpoint, clock, memoryAlloc,
                dataPlaneRequestChannel, configDef, processorNum);

        dataPlaneAcceptors.put(endpoint, dataPlaneAcceptor);
        dataPlaneAcceptor.start();
    }
}
