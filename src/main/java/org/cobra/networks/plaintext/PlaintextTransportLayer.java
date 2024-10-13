package org.cobra.networks.plaintext;

import org.cobra.commons.errors.AuthenticationException;
import org.cobra.networks.CobraChannelIdentifier;
import org.cobra.networks.TransportLayer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class PlaintextTransportLayer implements TransportLayer {

    private final SelectionKey selectionKey;
    private final SocketChannel socketChannel;

    public PlaintextTransportLayer(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
        this.socketChannel = (SocketChannel) selectionKey.channel();
    }

    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public boolean isFinishConnection() throws IOException {
        boolean connected = socketChannel.finishConnect();
        if (connected)
            selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_CONNECT | SelectionKey.OP_READ);

        return connected;
    }

    @Override
    public boolean isConnected() {
        return socketChannel.isConnected();
    }

    @Override
    public SocketChannel channel() {
        return socketChannel;
    }

    @Override
    public SelectionKey selectionKey() {
        return selectionKey;
    }

    @Override
    public void handshake() throws AuthenticationException {
        // nop
    }

    @Override
    public void disconnect() {
        selectionKey.cancel();
    }

    @Override
    public void addInterestOps(int interestOps) {
        int currentInterestOps = selectionKey.interestOps();
        selectionKey.interestOps(currentInterestOps | interestOps);
    }

    @Override
    public void removeInterestOps(int interestOps) {
        int currentInterestOps = selectionKey.interestOps();
        selectionKey.interestOps(currentInterestOps & ~interestOps);
    }

    @Override
    public boolean hasBuffer() {
        return false;
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        return socketChannel.read(dsts, offset, length);
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException {
        return socketChannel.read(dsts);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return socketChannel.read(dst);
    }

    @Override
    public boolean hasPendingWrites() {
        return false;
    }

    @Override
    public long transferFrom(FileChannel fc, long position, long count) throws IOException {
        return fc.transferTo(position, count, socketChannel);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        return socketChannel.write(srcs, offset, length);
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException {
        return socketChannel.write(srcs);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return socketChannel.read(src);
    }

    @Override
    public boolean isOpen() {
        return socketChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        socketChannel.close();
        socketChannel.socket().close();
    }

    @Override
    public String toString() {
        return "PlaintextTransportLayer(" +
                "selectionKey=" + selectionKey +
                ", socketChannel=" + socketChannel +
                ')';
    }
}
