package org.cobra.networks.protocol;

import org.cobra.commons.utils.Bytes;

import java.nio.ByteBuffer;

/**
 * Every api send of network will be treated as a message, can be read/write by given interface
 */
public interface Message {

    /**
     * @return size of message
     */
    int size();

    /**
     * Write this message to into a Writable
     *
     * @param _writable placeholder to write to
     */
    void write(MessageWritable _writable);

    /**
     * Read message from Readable
     *
     * @param _readable placeholder to read from
     */
    void read(MessageReadable _readable);


    static ByteBuffer toBuffer(Message message) {
        MessageAccessor accessor = new MessageAccessor(message.size());
        message.write(accessor);
        accessor.flip();
        return accessor.buffer();
    }

    static byte[] toBytes(Message message) {
        ByteBuffer buffer = toBuffer(message);

        if (buffer.hasArray() && buffer.arrayOffset() == 0 && buffer.position() == 0
                && (buffer.limit() == buffer.array().length))
            return buffer.array();

        return Bytes.toArray(buffer);
    }
}
