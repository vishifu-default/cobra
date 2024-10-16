package org.cobra.networks.auth;

import java.net.InetAddress;

public interface AuthenticationContext {

    SecurityProtocol securityProtocol();

    InetAddress clientAddress();
}
