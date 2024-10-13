package org.cobra.networks;

public class SocketNode {
    private static final SocketNode NO_NODE = new SocketNode(-1, -1, "");

    private final int port;
    private final int id;
    private final String host;

    private Integer hash;

    public SocketNode(int id, int port, String host) {
        this.id = id;
        this.port = port;
        this.host = host;
    }

    public static SocketNode noNode() {
        return NO_NODE;
    }

    /**
     * @return test whether this node is empty, true if is `empty`
     */
    public boolean isEmpty() {
        return this.host == null || this.host.isEmpty() || this.port < 0;
    }

    public String source() {
        return String.valueOf(this.id);
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
        Integer result = this.hash;
        if (result == null) {
            result = 31 + ((host == null) ? 0 : host.hashCode());
            result *= 31;
            result += id + port;
            result *= 31;
            this.hash = result;
        }
        return result;
    }

    @Override
    public String toString() {
        return host + ":" + port + "(id = " + this.id + ")";
    }
}
