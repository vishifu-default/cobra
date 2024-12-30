package org.cobra.networks.requests;

import org.cobra.networks.protocol.ApiMessage;
import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.protocol.MessageReadable;
import org.cobra.networks.protocol.MessageWritable;

public class FetchVersionRequestMessage implements ApiMessage {

    @Override
    public short apikeyId() {
        return Apikey.FETCH_VERSION.id();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void write(MessageWritable _writable) {
    }

    @Override
    public void read(MessageReadable _readable) {
    }
}
