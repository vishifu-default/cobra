package org.cobra.networks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;

public class SendByteBuffer implements Send {

    private final Apikey apikey;
    private final ByteBuffer byteBuffer;
    private int remains;

    public SendByteBuffer(Apikey apikey, ByteBuffer byteBuffer) {
        this.apikey = apikey;
        this.byteBuffer = byteBuffer;
        this.remains = byteBuffer.remaining();
    }

    @Override
    public boolean isCompleted() {
        return remains <= 0;
    }

    @Override
    public Apikey apikey() {
        return apikey;
    }

    @Override
    public void writeTo(GatheringByteChannel channel) throws IOException {
        ByteBuffer writableBuffer = toWritableBuffer();

        int written = channel.write(writableBuffer);
        remains -= written;
    }

    private ByteBuffer toWritableBuffer() {
        ByteBuffer writableBuffer = ByteBuffer.allocate(1 + byteBuffer.remaining());
        writableBuffer.put((byte) apikey().id());
        writableBuffer.put(this.byteBuffer);
        writableBuffer.flip();
        return writableBuffer;
    }
}
