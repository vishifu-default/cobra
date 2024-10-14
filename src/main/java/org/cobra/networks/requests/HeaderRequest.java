package org.cobra.networks.requests;

import org.cobra.networks.protocol.ApiData;
import org.cobra.networks.protocol.ApiMessage;
import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.protocol.MessageAccessor;

import java.nio.ByteBuffer;

public class HeaderRequest implements ApiData {

    final HeaderRequestMessage data;

    public HeaderRequest(HeaderRequestMessage data) {
        this.data = data;
    }

    public HeaderRequest(short apikey, long correlationId, String clientId) {
        this.data = new HeaderRequestMessage(apikey, correlationId, clientId);
    }

    public static HeaderRequest parse(ByteBuffer buffer) {
        final HeaderRequestMessage headerData = new HeaderRequestMessage(new MessageAccessor(buffer));
        if (headerData.clientId == null)
            throw new IllegalArgumentException("clientId is null");

        return new HeaderRequest(headerData);
    }

    public Apikey apikey() {
        return Apikey.ofId(data.requestApikey);
    }

    public long correlationId() {
        return data.correlationId;
    }

    public String clientId() {
        return data.clientId;
    }

    @Override
    public ApiMessage data() {
        return data;
    }

    public HeaderResponse toResponse() {
        return new HeaderResponse(new HeaderResponseMessage(
                data.requestApikey,
                data.correlationId
        ));
    }

    @Override
    public String toString() {
        return "HeaderRequest(" +
                "apikey=" + apikey() +
                ", correlationId=" + correlationId() +
                ", clientId='" + clientId() + '\'' +
                ')';
    }
}
