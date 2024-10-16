package org.cobra.networks.client;

import org.cobra.commons.Clock;
import org.cobra.commons.configs.ConfigDef;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.networks.SocketNode;

public class CobraClientFactory {

    public static CobraClient createClient(
            Clock clock,
            MemoryAlloc memoryAlloc,
            SocketNode socketNode,
            ConfigDef configDef
    ) {
        int reconnectBackoffMs = configDef.valueOf(DefaultClientConfigs.CONNECTION_RECONNECT_BACKOFF_CONF);
        int reconnectBackoffMsMax = configDef.valueOf(DefaultClientConfigs.CONNECTION_RECONNECT_BACKOFF_MAX_CONF);
        int setupTimeoutBackoffMs = configDef.valueOf(DefaultClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG);
        int setupTimeoutBackoffMsMax = configDef.valueOf(DefaultClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG);
        int soSendBufferSize = configDef.valueOf(DefaultClientConfigs.SOCKET_SEND_BUFFER_SIZE_CONF);
        int soReceiveBufferSize = configDef.valueOf(DefaultClientConfigs.SOCKET_SEND_BUFFER_SIZE_CONF);
        int soTimeoutMs = configDef.valueOf(DefaultClientConfigs.SOCKET_TIMEOUT_MS_CONF);

        return new CobraClient(clock, memoryAlloc, socketNode, soSendBufferSize, soReceiveBufferSize, soTimeoutMs,
                reconnectBackoffMs, reconnectBackoffMsMax, setupTimeoutBackoffMs, setupTimeoutBackoffMsMax);
    }
}
