package org.cobra.networks.requests;

import org.cobra.networks.protocol.ApiData;
import org.cobra.networks.protocol.ApiMessage;
import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.protocol.MessageAccessor;

import java.nio.ByteBuffer;

public class HeaderResponse implements ApiData {

    final HeaderResponseMessage data;

    public HeaderResponse(HeaderResponseMessage data) {
        this.data = data;
    }

    public HeaderResponse(short apikeyId, long correlationId, String clientId) {
        this.data = new HeaderResponseMessage(apikeyId, correlationId, clientId);
    }

    public static HeaderResponse parse(ByteBuffer buffer) {
        HeaderResponseMessage data = new HeaderResponseMessage(new MessageAccessor(buffer));
        return new HeaderResponse(data);
    }

    public Apikey apikey() {
        return Apikey.ofId(data.requestApikey);
    }

    public long correlationId() {
        return data.correlationId;
    }

    @Override
    public ApiMessage data() {
        return data;
    }

    @Override
    public String toString() {
        return "HeaderResponse(" +
                "apikey=" + apikey() +
                ", correlationId=" + correlationId() +
                ')';
    }
}
