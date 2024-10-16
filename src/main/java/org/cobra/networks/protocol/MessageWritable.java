package org.cobra.networks.protocol;

import java.nio.ByteBuffer;

public interface MessageWritable {

    void writeByte(byte val);

    void writeShort(short val);

    void writeInt(int val);

    void writeLong(long val);

    void writeFloat(float val);

    void writeDouble(double val);

    void writeBytes(byte[] src);

    void writeByteBuffer(ByteBuffer buffer);

    void writeString(String val);
}
