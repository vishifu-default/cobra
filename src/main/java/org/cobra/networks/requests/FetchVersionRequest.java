package org.cobra.networks.requests;

import org.cobra.networks.protocol.ApiMessage;
import org.cobra.networks.protocol.Apikey;

public class FetchVersionRequest extends AbstractRequest {

    private final FetchVersionRequestMessage data;

    public FetchVersionRequest() {
        super(Apikey.FETCH_VERSION);
        this.data = new FetchVersionRequestMessage();
    }

    public static FetchVersionRequest doParse() {
        return new FetchVersionRequest();
    }

    @Override
    public AbstractResponse toErrorResponse(Throwable cause) {
        return null;
    }

    @Override
    public ApiMessage data() {
        return data;
    }

    @Override
    public String toString() {
        return "FetchVersionRequest()";
    }

    public static final class Builder extends AbstractRequest.Builder<FetchVersionRequest> {

        public Builder() {
            super(Apikey.FETCH_VERSION);
        }

        @Override
        public FetchVersionRequest build() {
            return new FetchVersionRequest();
        }
    }
}
