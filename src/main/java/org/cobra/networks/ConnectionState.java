package org.cobra.networks;

public enum ConnectionState {
    DISCONNECTED,
    CONNECTING,
    READY,
    AUTHENTICATION_FAILED;

    public boolean isDisconnected() {
        return this == DISCONNECTED || this == AUTHENTICATION_FAILED;
    }

    public boolean isConnecting() {
        return this == CONNECTING;
    }

    public boolean isConnected() {
        return this == READY;
    }
}
