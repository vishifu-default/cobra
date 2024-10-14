package org.cobra.networks.server;

import org.cobra.networks.auth.CobraPrincipal;

import java.net.InetAddress;
import java.util.Objects;

public class ClientSession {

    private final CobraPrincipal principal;
    private final InetAddress clientIpAddress;

    public ClientSession(CobraPrincipal principal, InetAddress clientIpAddress) {
        this.principal = principal;
        this.clientIpAddress = clientIpAddress;
    }

    public InetAddress getClientIpAddress() {
        return clientIpAddress;
    }

    public CobraPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientSession that = (ClientSession) o;
        return Objects.equals(principal, that.principal) && Objects.equals(clientIpAddress, that.clientIpAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principal, clientIpAddress);
    }

    @Override
    public String toString() {
        return "ClientSession(" +
                "clientIpAddress=" + clientIpAddress +
                ", principal=" + principal +
                ')';
    }
}
