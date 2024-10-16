package org.cobra.networks.auth;

import java.net.InetAddress;

public interface AuthorizedRequestContext {

    SecurityProtocol getSecurityProtocol();

    InetAddress getClientAddress();

    CobraPrincipal getPrincipal();

    int getRequestType();

    long getCorrelationId();

    String getChannelId();
}
