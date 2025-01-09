package org.cobra.networks;

import org.cobra.commons.config.ConfigDef;

public class NetworkConfig {

    public static final String SERVER_SOCKET_PORT = "server.socket.port";
    public static final int DEFAULT_PORT = 7070;

    public static final String LOCAL_NETWORK_SOCKET = "local.network.socket";
    public static final String DEFAULT_LOCAL_NETWORK_SOCKET = "127.0.0.1";

    public static final ConfigDef DEFAULT_CONFIG = new ConfigDef()
            .define(SERVER_SOCKET_PORT, DEFAULT_PORT)
            .define(LOCAL_NETWORK_SOCKET, DEFAULT_LOCAL_NETWORK_SOCKET);
}
