package org.cobra.networks.plaintext;

import org.cobra.networks.auth.AuthenticationContext;
import org.cobra.networks.auth.SecurityProtocol;

import java.net.InetAddress;
import java.util.Objects;

public class PlaintextAuthenticationContext implements AuthenticationContext {

    private final InetAddress inetAddress;

    public PlaintextAuthenticationContext(InetAddress inetAddress) {
        this.inetAddress = Objects.requireNonNull(inetAddress, "inetAddress is null");
    }

    @Override
    public SecurityProtocol securityProtocol() {
        return SecurityProtocol.PLAINTEXT;
    }

    @Override
    public InetAddress clientAddress() {
        return inetAddress;
    }

    @Override
    public String toString() {
        return "PlaintextAuthenticationContext(" +
                "inetAddress=" + inetAddress +
                ')';
    }
}
