package org.cobra.networks.client;

import org.cobra.commons.Jvm;
import org.cobra.commons.errors.AuthenticationException;
import org.cobra.networks.ConnectionState;

public class ConnectionStateControl {

    static final int RECONNECT_BACKOFF_EXP_BASE = 2;
    static final int CONNECTION_SETUP_TIMEOUT_EXP_BASE = 2;
    static final float RECONNECT_BACKOFF_EXP_JITTER = .2f;
    static final float CONNECTION_SETUP_TIMEOUT_EXP_JITTER = .2f;

    private final ExponentialBackoff reconnectBackoff;
    private final ExponentialBackoff connectionSetupTimeoutBackoff;
    private ConnectionState connectionState;
    private AuthenticationException authException;

    long lastAttemptConnectAtMs = Jvm.INF_TIMESTAMP;
    long reconnectBackoffMs = 0;
    long connectionSetupTimeoutMs = 0;
    int failAttempts = 0;
    int failConnectAttempts = 0;

    public ConnectionStateControl(
            long reconnectBackoffMs,
            long reconnectBackoffMaxMs,
            long connectionSetupTimeoutMs,
            long connectionSetupTimeoutMaxMs
    ) {
        this.reconnectBackoff = new ExponentialBackoff(reconnectBackoffMs, reconnectBackoffMaxMs,
                RECONNECT_BACKOFF_EXP_JITTER, RECONNECT_BACKOFF_EXP_BASE);
        this.connectionSetupTimeoutBackoff = new ExponentialBackoff(connectionSetupTimeoutMs, connectionSetupTimeoutMaxMs,
                CONNECTION_SETUP_TIMEOUT_EXP_JITTER, CONNECTION_SETUP_TIMEOUT_EXP_BASE);
        this.connectionState = ConnectionState.DISCONNECTED;
    }

    public void connecting(long nowMs) {
        connectionState = ConnectionState.CONNECTING;
        lastAttemptConnectAtMs = nowMs;
        reconnectBackoffMs = reconnectBackoff.backoff(0);
        connectionSetupTimeoutMs = connectionSetupTimeoutBackoff.backoff(0);
    }

    public void disconnected(long nowMs) {
        lastAttemptConnectAtMs = nowMs;
        updateReconnectBackoff();

        if (connectionState == ConnectionState.CONNECTING)
            updateConnectionSetupTimeout();
        else
            resetConnectionSetupTimeout();

        connectionState = ConnectionState.DISCONNECTED;
    }

    public void ready() {
        connectionState = ConnectionState.READY;
        resetConnectionSetupTimeout();
        resetReconnectBackoff();
    }

    public boolean isReady() {
        return connectionState == ConnectionState.READY;
    }

    public boolean canConnect(long nowMs) {
        return connectionState.isDisconnected() && (nowMs - lastAttemptConnectAtMs) >= reconnectBackoffMs;
    }

    public void authFailed(long nowMs, AuthenticationException autException) {
        connectionState = ConnectionState.AUTHENTICATION_FAILED;
        lastAttemptConnectAtMs = nowMs;
        this.authException = autException;
        updateReconnectBackoff();
    }

    public AuthenticationException getAuthException() {
        return authException;
    }

    public boolean isConnected() {
        return connectionState.isConnected();
    }

    public boolean isDisconnected() {
        return connectionState.isDisconnected();
    }

    public boolean isConnectionSetupTimeout(long nowMs) {
        if (connectionState != ConnectionState.CONNECTING)
            return false;

        return nowMs - lastAttemptConnectAtMs > connectionSetupTimeoutMs;
    }

    public long ioWorkDelayMs(long nowMs) {
        if (connectionState.isConnecting())
            return connectionSetupTimeoutMs;
        if (connectionState.isDisconnected()) {
            long mustWaitMs = nowMs - lastAttemptConnectAtMs;
            return Math.max(0, reconnectBackoffMs - mustWaitMs);
        }

        return Long.MAX_VALUE;
    }

    private void updateReconnectBackoff() {
        reconnectBackoffMs = reconnectBackoff.backoff(failAttempts++);
    }

    private void resetReconnectBackoff() {
        failAttempts = 0;
        reconnectBackoffMs = reconnectBackoff.backoff(0);
    }

    private void updateConnectionSetupTimeout() {
        connectionSetupTimeoutMs = connectionSetupTimeoutBackoff.backoff(failConnectAttempts++);
    }

    private void resetConnectionSetupTimeout() {
        failConnectAttempts = 0;
        connectionSetupTimeoutMs = connectionSetupTimeoutBackoff.backoff(0);
    }

}
