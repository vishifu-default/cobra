package org.cobra.networks;

import java.util.function.Consumer;

public class DelayAuthenticationFailureClose {

    private final CobraChannel channel;
    private final Consumer<CobraChannel> channelFailureCallback;
    private final long endNanos;
    private boolean closed;

    public DelayAuthenticationFailureClose(CobraChannel channel, Consumer<CobraChannel> channelFailureCallback, long endNanos) {
        this.channel = channel;
        this.channelFailureCallback = channelFailureCallback;
        this.endNanos = endNanos;
        this.closed = false;
    }

    public boolean tryClose(long nanos) {
        if (nanos > endNanos)
            closeNow();
        return closed;
    }

    public void closeNow() {
        if (closed)
            throw new IllegalStateException("Attempt to close a closed channel");

        channelFailureCallback.accept(channel);
        closed = true;
    }
}
