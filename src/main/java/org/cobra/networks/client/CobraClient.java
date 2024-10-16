package org.cobra.networks.client;

import org.cobra.commons.Clock;
import org.cobra.commons.Jvm;
import org.cobra.commons.errors.AuthenticationException;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.networks.CobraChannelIdentifier;
import org.cobra.networks.ReceiveNetwork;
import org.cobra.networks.Send;
import org.cobra.networks.SendNetwork;
import org.cobra.networks.SocketNode;
import org.cobra.networks.requests.AbstractRequest;
import org.cobra.networks.requests.AbstractResponse;
import org.cobra.networks.requests.HeaderRequest;
import org.cobra.networks.requests.RequestCompletionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class CobraClient implements Client {

    private static final Logger log = LoggerFactory.getLogger(CobraClient.class);

    private final Clock clock;
    private final MemoryAlloc memoryAlloc;
    private final ConnectionStateControlPlane connectionStateControlPlane;

    private final AtomicBoolean isClosedRef = new AtomicBoolean(false);
    private final SocketNode socketNode;
    private SocketChannel socketChannel;
    private String clientId;
    private final int socketSendBufferSize;
    private final int socketReceiveBufferSize;
    private final int socketTimeoutMs;

    private final AtomicLong correlationIdCounter = new AtomicLong(0);

    public CobraClient(
            Clock clock,
            MemoryAlloc memoryAlloc,
            SocketNode socketNode,
            int soSndBufferSz,
            int soRcvBufferSz,
            int soTimeoutMs,
            int reconnectBackoffMs,
            int reconnectBackoffMsMax,
            int setupTimeoutBackoffMs,
            int setupTimeoutBackoffMsMax
    ) {
        this.clock = clock;
        this.memoryAlloc = memoryAlloc;
        this.socketNode = socketNode;
        this.socketSendBufferSize = soSndBufferSz;
        this.socketReceiveBufferSize = soRcvBufferSz;
        this.socketTimeoutMs = soTimeoutMs;

        this.connectionStateControlPlane = new ConnectionStateControlPlane(reconnectBackoffMs, reconnectBackoffMsMax,
                setupTimeoutBackoffMs, setupTimeoutBackoffMsMax);
    }


    @Override
    public boolean ready(long nowMs) {
        if (isReady(nowMs))
            return true;

        if (connectionStateControlPlane.canConnect(nowMs))
            initiateConnection(socketNode, nowMs);

        return false;
    }

    private void initiateConnection(SocketNode node, long nowMs) {
        try {
            /* mark connecting */
            connectionStateControlPlane.connecting(nowMs);
            InetSocketAddress inetAddress = new InetSocketAddress(node.host(), node.port());

            /* do open socket to node */
            socketChannel = SocketChannel.open(inetAddress);

            /* configure socket */
            {
                socketChannel.socket().setKeepAlive(true);
                socketChannel.socket().setTcpNoDelay(true);
                socketChannel.socket().setSoTimeout(socketTimeoutMs);

                if (socketSendBufferSize != Jvm.USE_DEFAULT_SOCKET_BUFFER_SIZE)
                    socketChannel.socket().setSendBufferSize(socketSendBufferSize);
                if (socketReceiveBufferSize != Jvm.USE_DEFAULT_SOCKET_BUFFER_SIZE)
                    socketChannel.socket().setReceiveBufferSize(socketReceiveBufferSize);

            }

            clientId = CobraChannelIdentifier.identifier((InetSocketAddress) socketChannel.getLocalAddress());
            log.info("connect to {}; client_id: {}", node, clientId);

            connectionStateControlPlane.ready();

        } catch (IOException e) {
            log.error("failed to connect to {}; client_id: {}", node, clientId, e);
            connectionStateControlPlane.disconnected(nowMs);
        }
    }

    @Override
    public boolean isReady(long nowMs) {
        return canSendRequest();
    }

    private boolean canSendRequest() {
        return socketChannel != null && socketChannel.isConnected()
                && connectionStateControlPlane.isReady();
    }

    @Override
    public boolean active() {
        return !isClosedRef.get();
    }

    @Override
    public boolean isConnectFailed() {
        return connectionStateControlPlane.isDisconnected();
    }

    @Override
    public Optional<AuthenticationException> authenticationException() {
        return Optional.ofNullable(connectionStateControlPlane.getAuthException());
    }

    @Override
    public long ioLatencyMs(long nowMs) {
        return connectionStateControlPlane.ioLatencyMs(nowMs);
    }

    @Override
    public boolean send(ClientRequest clientRequest, long nowMs) {
        ensureActive();

        if (!canSendRequest())
            throw new IllegalStateException("Could not send request now");

        boolean isDone = false;

        final HeaderRequest toHeader = clientRequest.toHeaderRequest();
        final AbstractRequest requestBody = clientRequest.getRequestBuilder().build();
        final Send toSend = requestBody.toSend(toHeader);
        final SendNetwork sendNetwork = new SendNetwork(socketNode.id(), toSend);

        ReceiveNetwork receiveNetwork = null;
        try {
            sendNetwork.writeTo(socketChannel);

            log.debug("sent {} request to {}; header: {}; ({})", toHeader.apikey(), socketNode, toHeader, requestBody);

            receiveNetwork = new ReceiveNetwork(socketNode.id(), memoryAlloc);
            receiveNetwork.readFrom(socketChannel);

            ByteBuffer receivePayload = receiveNetwork.payload().rewind();

            AbstractResponse responseBody = AbstractResponse.parse(receivePayload, toHeader);

            log.debug("receive {} response from {}; header: {}; ({})",
                    toHeader.apikey(), socketNode, toHeader, responseBody);

            clientRequest.getCallback().consume(responseBody); // todo: callback

            isDone = true;

        } catch (SocketTimeoutException e) {
            log.warn("socket IO timeout", e);
        } catch (IOException e) {
            log.error("error while receive and complete response", e);
        } finally {
            if (receiveNetwork != null) receiveNetwork.close();
        }

        return isDone;
    }

    @Override
    public void disconnect() throws IOException {
        if (connectionStateControlPlane.isDisconnected()) {
            log.debug("attempt to disconnect while that was already disconnected");
            return;
        }

        socketChannel.close();
        connectionStateControlPlane.disconnected(clock.milliseconds());
        log.info("disconnect to {}; client_id: {}", socketNode.id(), clientId);
    }

    @Override
    public ClientRequest createClientRequest(AbstractRequest.Builder<?> requestBuilder, long creationMs) {
        return createClientRequest(requestBuilder, creationMs, null);
    }

    @Override
    public ClientRequest createClientRequest(
            AbstractRequest.Builder<?> requestBuilder,
            long creationMs,
            RequestCompletionCallback callback) {
        return new ClientRequest(clientId, assignCorrelationId(), creationMs, requestBuilder, callback);
    }

    @Override
    public void close() throws IOException {
        if (isClosedRef.compareAndSet(false, true)) {
            socketChannel.close();
        }
    }

    private void ensureActive() {
        if (isClosedRef.get())
            throw new IllegalStateException("Client is closed");
    }

    private long assignCorrelationId() {
        long correlation = correlationIdCounter.getAndIncrement();
        if (correlation == Long.MAX_VALUE)
            correlationIdCounter.set(0);

        return correlation;
    }
}
