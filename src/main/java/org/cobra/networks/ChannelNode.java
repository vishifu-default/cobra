package org.cobra.networks;

import java.net.InetSocketAddress;

public class ChannelNode {

    private final String id;
    private final int port;
    private final String host;
    private final InetSocketAddress inetAddress;

    public ChannelNode(String host, int port) {
        this.host = host;
        this.port = port;
        this.inetAddress = new InetSocketAddress(host, port);
        this.id = CobraChannelIdentifier.identifier(inetAddress);
    }

    /**
     * @return test whether this node is empty, true if is `empty`
     */
    public boolean isResolved() {
        return !inetAddress.isUnresolved();
    }

    public String id() {
        return id;
    }

    public String host() {
        return this.host;
    }

    public int port() {
        return this.port;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int result = 31 + host.hashCode();
        result *= 31;
        result += port;
        result *= 31;
        return result;
    }

    @Override
    public String toString() {
        return "ChannelNode [host=" + host + ", port=" + port + "]";
    }
}
