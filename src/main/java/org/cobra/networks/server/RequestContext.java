package org.cobra.networks.server;

import org.cobra.networks.Send;
import org.cobra.networks.auth.AuthorizedRequestContext;
import org.cobra.networks.auth.CobraPrincipal;
import org.cobra.networks.auth.SecurityProtocol;
import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.requests.AbstractRequest;
import org.cobra.networks.requests.AbstractResponse;
import org.cobra.networks.requests.HeaderRequest;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class RequestContext implements AuthorizedRequestContext {


    private final SecurityProtocol securityProtocol;
    private final HeaderRequest headerRequest;
    private final InetAddress clientIpAddress;
    private final CobraPrincipal principal;

    public RequestContext(
            SecurityProtocol securityProtocol,
            HeaderRequest headerRequest,
            InetAddress clientIpAddress,
            CobraPrincipal principal) {
        this.securityProtocol = securityProtocol;
        this.headerRequest = headerRequest;
        this.clientIpAddress = clientIpAddress;
        this.principal = principal;
    }

    @Override
    public SecurityProtocol getSecurityProtocol() {
        return securityProtocol;
    }

    @Override
    public InetAddress getClientAddress() {
        return clientIpAddress;
    }

    @Override
    public CobraPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public int getRequestType() {
        return headerRequest.apikey().id();
    }

    @Override
    public long getCorrelationId() {
        return headerRequest.correlationId();
    }

    @Override
    public String getChannelId() {
        return headerRequest.clientId();
    }

    public HeaderRequest getHeader() {
        return headerRequest;
    }

    public Send toSend(AbstractResponse response) {
        return response.toSend(headerRequest);
    }

    public ByteBuffer toBufferIncludeHeader(AbstractResponse response) {
        return response.toBufferIncludeHeader(headerRequest.toResponse());
    }

    public AbstractRequest.RequestAndSize parseToRequestAndSize(ByteBuffer buffer) {
        Apikey apikey = headerRequest.apikey();
        return AbstractRequest.parse(apikey, buffer);
    }

    @Override
    public String toString() {
        return "RequestContext(" +
                "clientIpAddress=" + clientIpAddress +
                ", securityProtocol=" + securityProtocol +
                ", headerRequest=" + headerRequest +
                ", principal=" + principal +
                ')';
    }
}
