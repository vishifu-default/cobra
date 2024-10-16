package org.cobra.networks.mocks;

import org.cobra.networks.ReceiveNetwork;

public class DelayedReceive {

    private final String source;
    private final ReceiveNetwork receive;

    public DelayedReceive(String source, ReceiveNetwork receive) {
        this.source = source;
        this.receive = receive;
    }

    public ReceiveNetwork getReceive() {
        return receive;
    }

    public String getSource() {
        return source;
    }
}
