package org.cobra.networks.requests;

import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.protocol.MessageAccessor;

import java.nio.ByteBuffer;

public class SampleResponse extends AbstractResponse {

    private final SampleResponseMessage data;

    public SampleResponse(SampleResponseMessage data) {
        super(Apikey.SAMPLE_REQUEST);
        this.data = data;
    }

    public static SampleResponse doParse(ByteBuffer buffer) {
        SampleResponseMessage message = new SampleResponseMessage();
        message.read(new MessageAccessor(buffer));
        return new SampleResponse(message);
    }

    @Override
    public SampleResponseMessage data() {
        return data;
    }

    public String text() {
        return data.text;
    }

    @Override
    public String toString() {
        return "SampleResponse(" +
                "text=" + text() +
                ")";
    }
}
