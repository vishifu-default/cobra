package org.cobra.networks.server;

import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.server.internal.ApiHandler;
import org.cobra.networks.server.internal.SimpleRequest;
import org.cobra.networks.server.internal.handler.SampleRequestApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CobraApis implements ApiHandler {

    private static final Logger log = LoggerFactory.getLogger(CobraApis.class);

    private final RequestChanel requestChannelPlane;

    public CobraApis(RequestChanel requestChannelPlane) {
        this.requestChannelPlane = requestChannelPlane;
    }

    @Override
    public void handle(SimpleRequest simpleRequest) {
        log.debug("handle request {} from '{}'; security_protocol: {}; principal: {}",
                simpleRequest.getHeaderRequest().apikey(),
                simpleRequest.getRequestContext().getChannelId(),
                simpleRequest.getRequestContext().getSecurityProtocol(),
                simpleRequest.getRequestContext().getPrincipal());

        Apikey apikey = simpleRequest.getRequestContext().getHeader().apikey();

        switch (apikey) {
            case SAMPLE_REQUEST -> SampleRequestApi.handle(simpleRequest, requestChannelPlane);
            case null -> throw new IllegalStateException("Null apikey from request");
        }
    }

    @Override
    public void close() throws Exception {
        log.info("closing CobraApis");
    }
}
