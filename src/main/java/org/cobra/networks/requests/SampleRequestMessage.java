package org.cobra.networks.requests;

import org.cobra.commons.utils.Strings;
import org.cobra.networks.protocol.ApiMessage;
import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.protocol.MessageReadable;
import org.cobra.networks.protocol.MessageWritable;

public class SampleRequestMessage implements ApiMessage {

    String text;

    public SampleRequestMessage() {
    }

    public SampleRequestMessage(String text) {
        this.text = text;
    }

    @Override
    public short apikeyId() {
        return Apikey.SAMPLE_REQUEST.id();
    }

    @Override
    public int size() {
        return Strings.fixedLength(text);
    }

    @Override
    public void write(MessageWritable _writable) {
        _writable.writeString(text);
    }

    @Override
    public void read(MessageReadable _readable) {
        text = _readable.readString();
    }
}
