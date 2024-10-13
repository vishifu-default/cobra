package org.cobra.networks.client;

import org.cobra.commons.Clock;
import org.cobra.commons.configs.ConfigDef;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.networks.*;
import org.cobra.networks.auth.SecurityProtocol;

import java.util.UUID;

public class CobraNetworkClientFactory {

    public static CobraNetworkClient createClientNetwork(
            SocketNode destinationNode,
            ConfigDef configDef,
            Clock clock,
            MemoryAlloc memoryAlloc,
            int maxInflightRequestPerConn
    ) {
        return createClientNetwork(UUID.randomUUID().toString(), destinationNode, configDef, clock,
                memoryAlloc, maxInflightRequestPerConn);
    }

    public static CobraNetworkClient createClientNetwork(
            String clientId,
            SocketNode destinationNode,
            ConfigDef configDef,
            Clock clock,
            MemoryAlloc memoryAlloc,
            int maxInflightRequestPerConn
    ) {
        long requestTimeoutMs = configDef.get(DefaultClientConfigs.REQUEST_TIMEOUT_MS_CONF).value();
        return createClientNetwork(clientId, configDef, clock, memoryAlloc,
                destinationNode, maxInflightRequestPerConn, requestTimeoutMs);
    }

    public static CobraNetworkClient createClientNetwork(
            String clientId,
            ConfigDef configDef,
            Clock clock,
            MemoryAlloc memoryAlloc,
            SocketNode destinationSocket,
            int maxInflightRequestPerConn,
            long defaultRequestTimeoutMs
    ) {
        ChannelBuilder channelBuilder;
        Selectable selectable;

        // todo: hard-code
        channelBuilder = ChannelBuilderFactory.defaultChannelBuilder(
                SecurityProtocol.ofName(configDef.get(DefaultClientConfigs.CLIENT_SECURITY_PROTOCOL_CONF).value()));

        final long connIdleMaxMillis = configDef.get(DefaultClientConfigs.CONNECTION_MAX_IDLE_MILLIS_CONF).value();
        selectable = new CobraSelector(connIdleMaxMillis, channelBuilder, clock, memoryAlloc);

        final int sndBufferSize = configDef.get(DefaultClientConfigs.SOCKET_SEND_BUFFER_SIZE_CONF).value();
        final int rcvBufferSize = configDef.get(DefaultClientConfigs.SOCKET_RECEIVE_BUFFER_SIZE_CONF).value();
        final long reconnectBackoffMs = configDef.get(DefaultClientConfigs.CONNECTION_RECONNECT_BACKOFF_CONF).value();
        final long reconnectBackoffMaxMs = configDef.get(DefaultClientConfigs.CONNECTION_RECONNECT_BACKOFF_MAX_CONF).value();
        final long socketConnectionSetupTimeoutMs =
                configDef.get(DefaultClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG).value();
        final long socketConnectionSetupTimeoutMaxMs =
                configDef.get(DefaultClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG).value();

        return new CobraNetworkClient(
                selectable,
                clock,
                destinationSocket,
                clientId,
                sndBufferSize,
                rcvBufferSize,
                maxInflightRequestPerConn,
                reconnectBackoffMs,
                reconnectBackoffMaxMs,
                socketConnectionSetupTimeoutMs,
                socketConnectionSetupTimeoutMaxMs,
                defaultRequestTimeoutMs
        );
    }
}
