package org.cobra.networks;

import org.cobra.commons.Jvm;
import org.cobra.commons.errors.CobraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class CobraClient implements Network {

    private static final Logger log = LoggerFactory.getLogger(CobraClient.class);
    private final InetSocketAddress inetAddress;
    private SocketChannel socket;

    private final ByteBuffer sizeBuffer = ByteBuffer.allocate(4);

    public CobraClient(InetSocketAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    @Override
    public InetSocketAddress getAddress() {
        return inetAddress;
    }

    @Override
    public void bootstrap() {
        try {
            init();
            socket.connect(getAddress());
        } catch (IOException e) {
            throw new CobraException(e);
        }
    }

    @Override
    public void shutdown() {
        log.debug("client {} shutting down...", this);

        try {
            socket.close();
        } catch (IOException e) {
            throw new CobraException(e);
        }

        log.debug("client {} shutdown", this);
    }

    @Override
    public SocketChannel socketChannel() {
        return socket;
    }

    public void init() throws IOException {
        socket = SocketChannel.open();
        socket.configureBlocking(true);
    }

    public void socketSend(Send send) {
        try {
            send.writeTo(socket);
        } catch (IOException e) {
            throw new CobraException(e);
        }
    }

    public long fetchVersion() throws IOException {
        Send send = new SendByteBuffer(Apikey.FETCH_VERSION, Jvm.EMPTY_BUFFER);
        socketSend(send);

        ByteBuffer readBuffer = doRead();
        return readBuffer.getLong();
    }

    public ByteBuffer fetchHeaderBuffer(long version) throws IOException {
        Send send = new SendByteBuffer(Apikey.FETCH_HEADER, ByteBuffer.allocate(8).putLong(version).flip());
        socketSend(send);

        return doRead();
    }

    public ByteBuffer fetchBlobBuffer(long fromVersion, long toVersion) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(fromVersion);
        buffer.putLong(toVersion);
        buffer.flip();

        Send send = new SendByteBuffer(Apikey.FETCH_BLOB, buffer);
        socketSend(send);

        return doRead();
    }

    private ByteBuffer doRead() throws IOException {
        sizeBuffer.rewind();
        socket.read(sizeBuffer);

        sizeBuffer.flip();
        int mustReceiveSize = sizeBuffer.getInt();
        if (mustReceiveSize < 0)
            throw new IllegalArgumentException("negative size");

        ByteBuffer buffer = ByteBuffer.allocate(mustReceiveSize);

        int reads = 0;
        while (reads < mustReceiveSize) {
            int actualReads = socket.read(buffer);
            if (actualReads < 0)
                throw new IOException("unexpected end of stream");
            reads += actualReads;
        }

        buffer.flip();
        return buffer;
    }
}
