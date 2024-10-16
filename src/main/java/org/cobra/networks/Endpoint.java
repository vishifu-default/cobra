package org.cobra.networks;

import org.cobra.networks.auth.SecurityProtocol;

import java.util.Objects;

public class Endpoint {

    private final String host;
    private final int port;
    private final SecurityProtocol securityProtocol;

    public Endpoint(String host, int port, SecurityProtocol securityProtocol) {
        this.host = host;
        this.port = port;
        this.securityProtocol = securityProtocol;
    }

    public String host() {
        return this.host;
    }

    public int port() {
        return this.port;
    }

    public SecurityProtocol securityProtocol() {
        return this.securityProtocol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Endpoint endpoint = (Endpoint) o;
        return port == endpoint.port && Objects.equals(host, endpoint.host)
                && securityProtocol == endpoint.securityProtocol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, securityProtocol);
    }

    @Override
    public String toString() {
        return "CobraEndpoint(" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", securityProtocol=" + securityProtocol +
                ')';
    }
}
