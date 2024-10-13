package org.cobra.networks.protocol;

import java.nio.ByteBuffer;

public interface ApiData {

    /**
     * Join 2 part header and body into a ByteBuffer
     */
    static ByteBuffer makeBufferWithHeader(Message headerPart, Message bodyPart) {
        MessageAccessor accessor = new MessageAccessor(headerPart.size() + bodyPart.size());
        headerPart.write(accessor);
        bodyPart.write(accessor);

        return accessor.buffer().flip();
    }

    ApiMessage data();
}
