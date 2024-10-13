package org.cobra.networks.protocol;

import org.cobra.commons.utils.Bytes;

import java.nio.ByteBuffer;

public interface MessageReadable {

    byte readByte();

    short readShort();

    int readInt();

    long readLong();

    float readFloat();

    double readDouble();

    byte[] readBytes(int len);

    ByteBuffer sliceBuffer(int len);

    default String readString(int len) {
        if (len == 0)
            return "";

        return Bytes.utf8(readBytes(len));
    }

    default String readString() {
        int len = readInt();
        return readString(len);
    }
}
