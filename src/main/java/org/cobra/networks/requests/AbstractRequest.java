package org.cobra.networks.requests;


import org.cobra.networks.Send;
import org.cobra.networks.protocol.ApiData;
import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.protocol.Message;
import org.cobra.networks.protocol.SendBuilder;

import java.nio.ByteBuffer;

public abstract class AbstractRequest implements ApiData {

    protected final Apikey apikey;

    protected AbstractRequest(Apikey apikey) {
        this.apikey = apikey;
    }

    /**
     * Do parse ByteBuffer into {@link AbstractRequest} and let a buffer-size of remaining bytes
     *
     * @param apikey request type
     * @param buffer buffer payload
     * @return Request and Size of buffer
     */
    public static RequestAndSize parse(Apikey apikey, ByteBuffer buffer) {
        int bufferSize = buffer.remaining();
        return new RequestAndSize(parseExactly(apikey, buffer), bufferSize);
    }

    /**
     * Do parse exactly a buffer into a request type
     *
     * @param apikey request type
     * @param buffer buffer payload
     * @return parsed {@link AbstractRequest}
     */
    public static AbstractRequest parseExactly(Apikey apikey, ByteBuffer buffer) {
        return switch (apikey) {
            case SAMPLE_REQUEST -> SampleRequest.doParse(buffer);
            case null -> throw new IllegalArgumentException("Null apikey");
        };
    }

    public abstract AbstractResponse toErrorResponse(Throwable cause);

    /**
     * @return the apikey of request type
     */
    public final Apikey apikey() {
        return apikey;
    }

    /**
     * @return the size (in bytes) of underlying data of request
     */
    public final int sizeInBytes() {
        return data().size();
    }

    /**
     * @return convert data message into a ByteBuffer
     */
    public ByteBuffer toBuffer() {
        return Message.toBuffer(data());
    }

    /**
     * @param header header go with request
     * @return converted buffer of header and body data
     */
    public ByteBuffer toBufferIncludeHeader(HeaderRequest header) {
        return ApiData.makeBufferWithHeader(header.data(), data());
    }

    public Send toSend(HeaderRequest header) {
        return SendBuilder.wraps(header.data(), data());
    }

    public static abstract class Builder<T extends AbstractRequest> {
        private final Apikey apikey;

        protected Builder(Apikey apikey) {
            this.apikey = apikey;
        }

        public abstract T build();

        public final Apikey apikey() {
            return apikey;
        }
    }

    public record RequestAndSize(AbstractRequest request, int size) {
    }
}
