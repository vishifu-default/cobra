package org.cobra.networks.requests;

import org.cobra.networks.protocol.ApiData;
import org.cobra.networks.protocol.ApiMessage;
import org.cobra.networks.protocol.Apikey;

public class HeaderResponse implements ApiData {

    final HeaderResponseMessage data;

    public HeaderResponse(HeaderResponseMessage data) {
        this.data = data;
    }

    public HeaderResponse(short apikeyId, long correlationId) {
        this.data = new HeaderResponseMessage(apikeyId, correlationId);
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
