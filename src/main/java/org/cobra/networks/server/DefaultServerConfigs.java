package org.cobra.networks.server;

import org.cobra.commons.configs.ConfigDef;

public class DefaultServerConfigs {
    public static final String CONN_MAX_IDLE_MS_CONF = "conn.idle_ms.max";
    public static final long CONN_MAX_IDLE_MS_DEFAULT = 10 * 60 * 1000L;


    public static final String SOCKET_REQUEST_MAX_BYTE_CONF = "socket.request.bytes.max";
    public static final int SOCKET_REQUEST_MAX_BYTE_SIZE = 100 * 1024 * 1024;

    public static final String SOCKET_SEND_BUFFER_SIZE_CONF = "socket.send.size";
    public static final int SOCKET_SEND_BUFFER_SIZE_DEFAULT = 100 * 1024;

    public static final String SOCKET_RECEIVE_BUFFER_SIZE_CONF = "socket.receive.size";
    public static final int SOCKET_RECEIVE_BUFFER_SIZE_DEFAULT = 100 * 1024;

    public static final String SOCKET_LISTEN_BACKLOG_CONF = "socket.listen.backlog";
    public static final int SOCKET_LISTEN_BACKLOG_DEFAULT = 100;

    public static final String SOCKET_QUEUED_CAPACITY_CONF = "socket.queued.capacity";
    public static final int SOCKET_QUEUED_CAPACITY_DEFAULT = 500;


    public static final String ENDPOINT_HOST_CONF = "endpoint.host";
    public static final String ENDPOINT_HOST_DEFAULT = "localhost";

    public static final String ENDPOINT_PORT_CONF = "endpoint.port";
    public static final int ENDPOINT_PORT_DEFAULT = 9002;

    public static final String ENDPOINT_SECURITY_PROTOCOL_CONF = "endpoint.security";
    public static final String ENDPOINT_SECURITY_PROTOCOL_DEFAULT = "PLAINTEXT";


    public static final String NUM_NETWORK_THREADS_CONFIG = "num.network.threads";
    public static final int NUM_NETWORK_THREADS_DEFAULT = 3;

    public ConfigDef definition() {
        return CONFIG_DEF;
    }

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(CONN_MAX_IDLE_MS_CONF, CONN_MAX_IDLE_MS_DEFAULT)
            .define(SOCKET_REQUEST_MAX_BYTE_CONF, SOCKET_REQUEST_MAX_BYTE_SIZE)
            .define(SOCKET_SEND_BUFFER_SIZE_CONF, SOCKET_SEND_BUFFER_SIZE_DEFAULT)
            .define(SOCKET_RECEIVE_BUFFER_SIZE_CONF, SOCKET_RECEIVE_BUFFER_SIZE_DEFAULT)
            .define(SOCKET_LISTEN_BACKLOG_CONF, SOCKET_LISTEN_BACKLOG_DEFAULT)
            .define(SOCKET_QUEUED_CAPACITY_CONF, SOCKET_QUEUED_CAPACITY_DEFAULT)
            .define(ENDPOINT_HOST_CONF, ENDPOINT_HOST_DEFAULT)
            .define(ENDPOINT_PORT_CONF, ENDPOINT_PORT_DEFAULT)
            .define(ENDPOINT_SECURITY_PROTOCOL_CONF, ENDPOINT_SECURITY_PROTOCOL_DEFAULT)
            .define(NUM_NETWORK_THREADS_CONFIG, NUM_NETWORK_THREADS_DEFAULT);
}
