package org.cobra.networks.requests;

import org.cobra.commons.errors.CorrelationIDMismatchException;
import org.cobra.networks.Send;
import org.cobra.networks.protocol.*;

import java.nio.ByteBuffer;

public abstract class AbstractResponse implements ApiData {

    protected final Apikey apikey;

    protected AbstractResponse(Apikey apikey) {
        this.apikey = apikey;
    }

    public static AbstractResponse parse(ByteBuffer buffer, HeaderRequest header) {
        HeaderResponse headerResponse = new HeaderResponse(new HeaderResponseMessage(new MessageAccessor(buffer)));
        if (headerResponse.correlationId() != header.correlationId())
            throw new CorrelationIDMismatchException(header.correlationId(), headerResponse.correlationId());

        return doParsePayload(header.apikey(), buffer);
    }

    private static AbstractResponse doParsePayload(Apikey apikey, ByteBuffer buffer) {
        return switch (apikey) {
            case SAMPLE_REQUEST -> SampleResponse.doParse(buffer);
            case null -> throw new UnsupportedOperationException("Null apikey");
        };
    }

    public Apikey apikey() {
        return apikey;
    }

    public ByteBuffer toBuffer() {
        return Message.toBuffer(data());
    }

    public ByteBuffer toBufferIncludeHeader(HeaderResponse header) {
        return ApiData.makeBufferWithHeader(header.data(), data());
    }

    public Send toSend(HeaderRequest header) {
        return SendBuilder.wraps(header.data(), data());
    }
}
