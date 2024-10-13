package org.cobra.networks.requests;

import org.cobra.networks.protocol.ApiMessage;
import org.cobra.networks.protocol.MessageReadable;
import org.cobra.networks.protocol.MessageWritable;

public class HeaderResponseMessage implements ApiMessage {

    public static final long INF_CORRELATION_ID = -1L;

    short requestApikey;
    long correlationId;

    public HeaderResponseMessage() {
        this.requestApikey = -1;
        this.correlationId = INF_CORRELATION_ID;
    }

    public HeaderResponseMessage(short requestApikey, long correlationId) {
        this.requestApikey = requestApikey;
        this.correlationId = correlationId;
    }

    public HeaderResponseMessage(MessageReadable _readable) {
        read(_readable);
    }

    @Override
    public short apikeyId() {
        return -1;
    }

    @Override
    public int size() {
        return Short.BYTES + Long.BYTES;
    }

    @Override
    public void write(MessageWritable _writable) {
        _writable.writeShort(requestApikey);
        _writable.writeLong(correlationId);
    }

    @Override
    public void read(MessageReadable _readable) {
        requestApikey = _readable.readShort();
        correlationId = _readable.readLong();
    }
}
