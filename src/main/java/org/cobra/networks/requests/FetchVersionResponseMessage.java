package org.cobra.networks.requests;

import org.cobra.networks.protocol.ApiMessage;
import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.protocol.MessageReadable;
import org.cobra.networks.protocol.MessageWritable;

public class FetchVersionResponseMessage implements ApiMessage {

    long version;

    public FetchVersionResponseMessage(long version) {
        this.version = version;
    }

    public FetchVersionResponseMessage(MessageReadable _readable) {
        read(_readable);
    }

    public long getVersion() {
        return version;
    }

    @Override
    public short apikeyId() {
        return Apikey.FETCH_VERSION.id();
    }

    @Override
    public int size() {
        return Long.BYTES;
    }

    @Override
    public void write(MessageWritable _writable) {
        _writable.writeLong(version);
    }

    @Override
    public void read(MessageReadable _readable) {
        version = _readable.readLong();
    }

    @Override
    public String toString() {
        return "FetchVersionResponseMessage(" +
                "version=" + version +
                ')';
    }
}
