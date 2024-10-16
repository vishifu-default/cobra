package org.cobra.networks.server.internal.handler;

import org.cobra.networks.protocol.MessageAccessor;
import org.cobra.networks.requests.SampleResponse;
import org.cobra.networks.requests.SampleResponseMessage;
import org.cobra.networks.server.RequestChanel;
import org.cobra.networks.server.internal.SimpleRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SampleRequestApi {

    private static final Logger log = LoggerFactory.getLogger(SampleRequestApi.class);

    public static void handle(SimpleRequest simpleRequest, RequestChanel requestChanel) {
        log.info("handle sample request {}; ({})", simpleRequest, simpleRequest.body());

        MessageAccessor accessor = new MessageAccessor(7);
        accessor.writeString("foo");
        accessor.flip();
        SampleResponse response = new SampleResponse(new SampleResponseMessage(accessor));

        requestChanel.sendResponse(simpleRequest, response, Optional.empty());
    }
}
