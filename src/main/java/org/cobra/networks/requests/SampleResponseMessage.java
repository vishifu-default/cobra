package org.cobra.networks.requests;

import org.cobra.commons.utils.Stringx;
import org.cobra.networks.protocol.ApiMessage;
import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.protocol.MessageReadable;
import org.cobra.networks.protocol.MessageWritable;

public class SampleResponseMessage implements ApiMessage {

    String text;

    public SampleResponseMessage() {
    }

    public SampleResponseMessage(String text) {
        this.text = text;
    }

    public SampleResponseMessage(MessageReadable _readable) {
        read(_readable);
    }

    public String getText() {
        return text;
    }

    @Override
    public short apikeyId() {
        return Apikey.SAMPLE_REQUEST.id();
    }

    @Override
    public int size() {
        return Stringx.fixedLength(text);
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
