package org.cobra.networks.protocol;

import org.cobra.commons.utils.Stringx;
import org.cobra.networks.Send;
import org.cobra.networks.SendByteBuffer;
import org.cobra.networks.SendQueued;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SendBuilder implements MessageWritable {

    private final ByteBuffer buffer;

    private final Queue<Send> sendQueue;
    private final List<ByteBuffer> buffers;

    public SendBuilder(int size) {
        this.buffer = ByteBuffer.allocate(size);
        this.buffer.mark();
        this.sendQueue = new LinkedList<>();
        this.buffers = new ArrayList<>();
    }

    public static Send wraps(Message header, Message apiMessage) {
        int size = header.size() + apiMessage.size();
        SendBuilder builder = new SendBuilder(size + Integer.BYTES);

        builder.writeInt(size);
        header.write(builder);
        apiMessage.write(builder);

        return builder.build();
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
        flushPendingBuffer();
        addBuffer(bb.duplicate());
    }

    @Override
    public void writeString(String val) {
        if (Stringx.isBlank(val))
            writeInt(0);
        else {
            writeInt(val.length());
            writeBytes(val.getBytes());
        }
    }

    public Send build() {
        flushPendingSend();

        if (sendQueue.size() == 1)
            return sendQueue.poll();

        return new SendQueued(sendQueue);
    }

    private void flushPendingSend() {
        flushPendingBuffer();
        if (!buffers.isEmpty()) {
            ByteBuffer[] bbArr = buffers.toArray(new ByteBuffer[0]);
            addSend(new SendByteBuffer(bbArr));
            clearBuffers();
        }
    }

    private void flushPendingBuffer() {
        final int lastPosition = buffer.position();
        buffer.reset();

        if (lastPosition > buffer.position()) {
            buffer.limit(lastPosition);
            addBuffer(buffer.slice());

            buffer.position(lastPosition);
            buffer.limit(buffer.position());
            buffer.mark();
        }
    }

    private void addSend(Send send) {
        sendQueue.add(send);
    }

    private void addBuffer(ByteBuffer bb) {
        buffers.add(bb);
    }

    private void clearBuffers() {
        buffers.clear();
    }
}
