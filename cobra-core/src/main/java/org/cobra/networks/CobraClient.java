package org.cobra.networks;

import org.cobra.commons.Jvm;
import org.cobra.commons.errors.CobraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Duration;

public class CobraClient implements Network {

    private static final Logger log = LoggerFactory.getLogger(CobraClient.class);

    private static final long BACKOFF_INITIAL_INTERVAL_NANOS = (long) 1e8; // 100ms
    private static final long BACKOFF_MAX_INTERVAL_NANOS = (long) 1e10; // 10_000ms

    private final InetSocketAddress inetAddress;
    private SocketChannel socket;

    private final ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
    private final ExponentialBackoff exponentBackoff;

    private int connectAttempts = 0;
    private long lastAttemptTime = 0;

    public CobraClient(InetSocketAddress inetAddress) {
        this.inetAddress = inetAddress;
        this.exponentBackoff = new ExponentialBackoff(BACKOFF_INITIAL_INTERVAL_NANOS, BACKOFF_MAX_INTERVAL_NANOS);
    }

    @Override
    public InetSocketAddress getAddress() {
        return inetAddress;
    }

    @Override
    public void bootstrap() {
        try {
            tryConnect();
        } catch (IOException e) {
            throw new CobraException(e);
        }
    }

    public boolean isReady() {
        return socket != null && socket.isConnected() && socket.isOpen();
    }

    public void tryConnect() throws IOException {
        try {

            long backoffTime = exponentBackoff.backoff(connectAttempts);
            if ((System.nanoTime() - lastAttemptTime) < backoffTime) {
                Duration duration = Duration.ofNanos(backoffTime - (System.nanoTime() - lastAttemptTime));
                Thread.sleep(duration.toMillis());
            }

            if (socket == null || socket.socket().isClosed() || !socket.socket().isConnected())
                initializeSocket();

            socket.connect(getAddress());

            log.info("establish connection success; remote: {}", this);
        } catch (ConnectException connEx) {
            log.warn("establish connection failed; remote: {}; attempts: {}", this, connectAttempts);
        } catch (IOException e) {
            log.error("client I/O error", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            log.error("client interrupted", e);
            throw new RuntimeException(e);
        } finally {
            connectAttempts++;
            lastAttemptTime = System.nanoTime();
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

    public void initializeSocket() throws IOException {
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

    @Override
    public String toString() {
        return "CobraClient(" +
                "inetAddress=" + inetAddress.toString() +
                ')';
    }
}
