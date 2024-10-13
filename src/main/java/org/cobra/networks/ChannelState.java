package org.cobra.networks;

import org.cobra.commons.errors.AuthenticationException;

public class ChannelState {

    public static final ChannelState NOT_CONNECTED = new ChannelState(State.NOT_CONNECTED);
    public static final ChannelState AUTHENTICATED = new ChannelState(State.AUTHENTICATED);
    public static final ChannelState READY = new ChannelState(State.READY);
    public static final ChannelState EXPIRED = new ChannelState(State.EXPIRED);
    public static final ChannelState FAILED_SEND = new ChannelState(State.FAILED_SENT);
    public static final ChannelState FAILED_AUTHENTICATION = new ChannelState(State.FAILED_AUTHENTICATION);
    public static final ChannelState LOCAL_CLOSE = new ChannelState(State.LOCAL_CLOSE);
    private final State state;
    private final AuthenticationException authException;
    private final String remoteAddress;

    public ChannelState(State state) {
        this(state, null);
    }

    public ChannelState(State state, String remoteAddress) {
        this(state, null, remoteAddress);
    }

    public ChannelState(State state, AuthenticationException authException, String remoteAddress) {
        this.state = state;
        this.authException = authException;
        this.remoteAddress = remoteAddress;
    }

    public State value() {
        return state;
    }

    public AuthenticationException getAuthException() {
        return authException;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public enum State {
        NOT_CONNECTED,
        AUTHENTICATED,
        READY,
        EXPIRED,
        FAILED_SENT,
        FAILED_AUTHENTICATION,
        LOCAL_CLOSE,
    }
}
