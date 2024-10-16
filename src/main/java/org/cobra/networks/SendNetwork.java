package org.cobra.networks;

import java.io.IOException;
import java.nio.channels.GatheringByteChannel;

/**
 * A network send that is backed by another send (usually {@link SendByteBuffer}), we will write the backed send to
 * destination node.
 */
public class SendNetwork implements Send {

    private final String destination;
    private final Send send;

    public SendNetwork(String destination, Send send) {
        this.destination = destination;
        this.send = send;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public boolean isCompleted() {
        return send.isCompleted();
    }

    @Override
    public long writeTo(GatheringByteChannel channel) throws IOException {
        return send.writeTo(channel);
    }

    @Override
    public long size() {
        return send.size();
    }

    @Override
    public void close() throws IOException {
        send.close();
    }

    @Override
    public String toString() {
        return "SendNetwork(" +
                "destination='" + destination + '\'' +
                ", send=" + send +
                ')';
    }
}
