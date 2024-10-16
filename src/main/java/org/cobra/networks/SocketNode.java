package org.cobra.networks;

import java.net.InetSocketAddress;

public class SocketNode {

    private String id = null;
    private final int port;
    private final String host;
    private final InetSocketAddress inetAddress;

    public SocketNode(InetSocketAddress inetAddress) {
        this.inetAddress = inetAddress;
        this.host = inetAddress.getAddress().getHostAddress();
        this.port = inetAddress.getPort();
    }

    public SocketNode(String host, int port) {
        this(new InetSocketAddress(host, port));
    }

    /**
     * @return test whether this node is empty, true if is `empty`
     */
    public boolean isResolved() {
        return !inetAddress.isUnresolved();
    }

    public String id() {
        if (id == null) id = CobraChannelIdentifier.identifier(inetAddress);
        return id;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
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
