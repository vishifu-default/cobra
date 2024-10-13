package org.cobra.networks.requests;

import org.cobra.networks.protocol.ApiMessage;
import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.protocol.MessageAccessor;

import java.nio.ByteBuffer;

public class SampleRequest extends AbstractRequest {

    final SampleRequestMessage data;

    public SampleRequest(SampleRequestMessage data) {
        super(Apikey.SAMPLE_REQUEST);
        this.data = data;
    }

    public static SampleRequest doParse(ByteBuffer buffer) {
        SampleRequestMessage message = new SampleRequestMessage();
        message.read(new MessageAccessor(buffer));
        return new SampleRequest(message);
    }

    @Override
    public AbstractResponse toErrorResponse() {
        return null;
    }

    @Override
    public SampleRequestMessage data() {
        return data;
    }

    public String text() {
        return data.text;
    }

    @Override
    public String toString() {
        return "SampleRequest(" +
                "text=" + text() +
                ")";
    }

    public static final class Builder extends AbstractRequest.Builder<SampleRequest> {

        private final SampleRequestMessage data;

        public Builder(SampleRequestMessage data) {
            super(Apikey.SAMPLE_REQUEST);
            this.data = data;
        }

        public Builder(String txt) {
            this(new SampleRequestMessage(txt));
        }

        @Override
        public SampleRequest build() {
            return new SampleRequest(data);
        }
    }
}
