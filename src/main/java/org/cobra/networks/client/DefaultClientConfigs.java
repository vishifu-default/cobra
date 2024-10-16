package org.cobra.networks.client;

import org.cobra.commons.configs.ConfigDef;

import java.time.Duration;

public class DefaultClientConfigs {

    public static final String CONNECTION_MAX_IDLE_MILLIS_CONF = "connection.max.idle.millis";
    public static final int CONNECTION_MAX_IDLE_MILLIS_DEFAULT = (int) Duration.ofSeconds(600).toMillis();

    public static final String CONNECTION_RECONNECT_BACKOFF_CONF = "connection.reconnect.backoff";
    public static final int CONNECTION_RECONNECT_BACKOFF_DEFAULT = 50;

    public static final String CONNECTION_RECONNECT_BACKOFF_MAX_CONF = "connection.reconnect.backoff.max";
    public static final int CONNECTION_RECONNECT_BACKOFF_MAX_DEFAULT = (int) Duration.ofSeconds(1).toMillis();

    public static final String CLIENT_SECURITY_PROTOCOL_CONF = "client.security.protocol";
    public static final String CLIENT_SECURITY_PROTOCOL_DEFAULT = "PLAINTEXT";

    public static final String REQUEST_TIMEOUT_MS_CONF = "request.timeout.ms";
    public static final int REQUEST_TIMEOUT_MS_DEFAULT = (int) Duration.ofSeconds(30).toMillis();

    public static final String SOCKET_SEND_BUFFER_SIZE_CONF = "socket.send.buffer.size";
    public static final int SOCKET_SEND_BUFFER_SIZE_DEFAULT = -1;

    public static final String SOCKET_RECEIVE_BUFFER_SIZE_CONF = "socket.receive.buffer.size";
    public static final int SOCKET_RECEIVE_BUFFER_SIZE_DEFAULT = -1;

    public static final String SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG = "socket.connection.setup.timeout.ms";
    public static final int SOCKET_CONNECTION_SETUP_TIMEOUT_MS_DEFAULT = 10 * 1000;

    public static final String SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG = "socket.connection.setup.timeout.max.ms";
    public static final int SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_DEFAULT = 30 * 1000;

    public static final String SOCKET_TIMEOUT_MS_CONF = "socket.timeout";
    public static final int SOCKET_TIMEOUT_MS_DEFAULT = 30_000;

    public static ConfigDef CONFIG_DEF = new ConfigDef()
            .define(CONNECTION_MAX_IDLE_MILLIS_CONF, CONNECTION_MAX_IDLE_MILLIS_DEFAULT)
            .define(CONNECTION_RECONNECT_BACKOFF_CONF, CONNECTION_RECONNECT_BACKOFF_DEFAULT)
            .define(CONNECTION_RECONNECT_BACKOFF_MAX_CONF, CONNECTION_RECONNECT_BACKOFF_MAX_DEFAULT)
            .define(CLIENT_SECURITY_PROTOCOL_CONF, CLIENT_SECURITY_PROTOCOL_DEFAULT)
            .define(REQUEST_TIMEOUT_MS_CONF, REQUEST_TIMEOUT_MS_DEFAULT)
            .define(SOCKET_SEND_BUFFER_SIZE_CONF, SOCKET_SEND_BUFFER_SIZE_DEFAULT)
            .define(SOCKET_RECEIVE_BUFFER_SIZE_CONF, SOCKET_RECEIVE_BUFFER_SIZE_DEFAULT)
            .define(SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG, SOCKET_CONNECTION_SETUP_TIMEOUT_MS_DEFAULT)
            .define(SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG, SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_DEFAULT)
            .define(SOCKET_TIMEOUT_MS_CONF, SOCKET_TIMEOUT_MS_DEFAULT);
}
