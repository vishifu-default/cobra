package org.cobra.networks.protocol;

import org.cobra.commons.utils.Strings;

import java.nio.ByteBuffer;

public class MessageAccessor implements MessageWritable, MessageReadable {

    private final ByteBuffer buffer;

    public MessageAccessor(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public MessageAccessor(int size) {
        this.buffer = ByteBuffer.allocate(size);
    }

    @Override
    public byte readByte() {
        return buffer.get();
    }

    @Override
    public short readShort() {
        return buffer.getShort();
    }

    @Override
    public int readInt() {
        return buffer.getInt();
    }

    @Override
    public long readLong() {
        return buffer.getLong();
    }

    @Override
    public float readFloat() {
        return buffer.getFloat();
    }

    @Override
    public double readDouble() {
        return buffer.getDouble();
    }

    @Override
    public byte[] readBytes(int len) {
        int remains = buffer.remaining();
        if (len > remains)
            throw new IllegalArgumentException("Given len to read is out of bounds");

        byte[] dst = new byte[len];
        buffer.get(dst);
        return dst;
    }

    @Override
    public ByteBuffer sliceBuffer(int len) {
        ByteBuffer slice = buffer.slice();
        slice.limit(len);

        buffer.position(buffer.position() + len);
        return slice;
    }

    @Override
    public void writeByte(byte val) {
        buffer.put(val);
    }

    @Override
    public void writeShort(short val) {
        buffer.putShort(val);
    }

    @Override
    public void writeInt(int val) {
        buffer.putInt(val);
    }

    @Override
    public void writeLong(long val) {
        buffer.putLong(val);
    }

    @Override
    public void writeFloat(float val) {
        buffer.putFloat(val);
    }

    @Override
    public void writeDouble(double val) {
        buffer.putDouble(val);
    }

    @Override
    public void writeBytes(byte[] src) {
        buffer.put(src);
    }

    @Override
    public void writeByteBuffer(ByteBuffer bb) {
        buffer.put(bb);
    }

    @Override
    public void writeString(String val) {
        if (Strings.isBlank(val))
            writeInt(0);
        else {
            writeInt(val.length());
            writeBytes(val.getBytes());
        }
    }

    public void flip() {
        buffer.flip();
    }

    public ByteBuffer buffer() {
        return buffer;
    }
}
