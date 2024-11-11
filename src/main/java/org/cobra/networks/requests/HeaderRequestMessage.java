package org.cobra.networks.requests;

import org.cobra.commons.utils.Stringx;
import org.cobra.networks.protocol.ApiMessage;
import org.cobra.networks.protocol.MessageReadable;
import org.cobra.networks.protocol.MessageWritable;

public class HeaderRequestMessage implements ApiMessage {

    short requestApikey;
    long correlationId;
    String clientId;

    public HeaderRequestMessage() {
    }

    public HeaderRequestMessage(MessageReadable _readable) {
        read(_readable);
    }

    public HeaderRequestMessage(short requestApikey, long correlationId, String clientId) {
        this.requestApikey = requestApikey;
        this.correlationId = correlationId;
        this.clientId = clientId;
    }

    @Override
    public short apikeyId() {
        return -1;
    }

    @Override
    public int size() {
        return Short.BYTES + Long.BYTES + Stringx.fixedLength(clientId);
    }

    @Override
    public void write(MessageWritable _writable) {
        _writable.writeShort(requestApikey);
        _writable.writeLong(correlationId);
        _writable.writeString(clientId);
    }

    @Override
    public void read(MessageReadable _readable) {
        requestApikey = _readable.readShort();
        correlationId = _readable.readLong();
        clientId = _readable.readString();
    }

    @Override
    public String toString() {
        return "HeaderRequestMessage(" +
                "clientId='" + clientId + '\'' +
                ", requestApikey=" + requestApikey +
                ", correlationId=" + correlationId +
                ')';
    }
}
