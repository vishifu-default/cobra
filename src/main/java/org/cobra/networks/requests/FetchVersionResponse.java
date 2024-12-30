package org.cobra.networks.requests;

import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.protocol.MessageAccessor;

import java.nio.ByteBuffer;

public class FetchVersionResponse extends AbstractResponse {

    private final FetchVersionResponseMessage data;

    public FetchVersionResponse(FetchVersionResponseMessage data) {
        super(Apikey.FETCH_VERSION);
        this.data = data;
    }

    public FetchVersionResponse(long version) {
        this(new FetchVersionResponseMessage(version));
    }

    public static FetchVersionResponse doParse(ByteBuffer buffer) {
        FetchVersionResponseMessage respMessage = new FetchVersionResponseMessage(new MessageAccessor(buffer));
        return new FetchVersionResponse(respMessage);
    }

    @Override
    public FetchVersionResponseMessage data() {
        return data;
    }

    @Override
    public String toString() {
        return "FetchVersionResponse(" +
                "data=" + data +
                ')';
    }
}
