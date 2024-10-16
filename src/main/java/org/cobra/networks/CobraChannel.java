package org.cobra.networks;

import org.cobra.commons.errors.AuthenticationException;
import org.cobra.commons.errors.CobraException;
import org.cobra.commons.errors.DelayedResponseAuthenticationException;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.commons.utils.Utils;
import org.cobra.networks.auth.Authenticator;
import org.cobra.networks.auth.CobraPrincipal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.function.Supplier;

/**
 * Channel connection is either client or server.
 *
 * <ul>
 *     <li> a unique id identify the connected node for client-server </li>
 *     <li> a reference of transport layer to perform I/O </li>
 *     <li> a Authenticator that perform authenticate/re-authenticate </li>
 *     <li> a memory pool to allocate buffer </li>
 *     <li> a ReceiveNetwork use to receive data from the opposite side </li>
 *     <li> a SendNetwork use to send data to the opposite site </li>
 *     <li> MuteState use to mute/unmute a node </li>
 *     <li> ChannelState record and document the channel state </li>
 * </ul>
 */
@SuppressWarnings("resource")
public class CobraChannel implements AutoCloseable {

    private final String id;
    private final TransportLayer transportLayer;
    private final Supplier<Authenticator> authenticatorCreator;
    private final MemoryAlloc memoryAlloc;
    private Authenticator authenticator;
    private ReceiveNetwork receiveNetwork;
    private SendNetwork sendNetwork;
    private ChannelState channelState;
    private MuteState muteState;
    private SocketAddress remoteAddress;
    private long threadElapsedTimeMillis;
    private boolean disconnected;

    /* Constructor with NONE of {@link MemoryAlloc} */
    public CobraChannel(
            String id,
            TransportLayer transportLayer,
            Supplier<Authenticator> authenticatorCreator
    ) {
        this(id, transportLayer, authenticatorCreator, MemoryAlloc.NONE);
    }

    /* Basic constructor */
    public CobraChannel(
            String id,
            TransportLayer transportLayer,
            Supplier<Authenticator> authenticatorCreator,
            MemoryAlloc memoryAlloc
    ) {
        this.id = id;
        this.transportLayer = transportLayer;
        this.authenticatorCreator = authenticatorCreator;
        this.memoryAlloc = memoryAlloc;

        this.authenticator = authenticatorCreator.get();
        this.channelState = ChannelState.NOT_CONNECTED;
        this.muteState = MuteState.NOT_MUTED;
        this.threadElapsedTimeMillis = 0L;

        this.receiveNetwork = null;
        this.sendNetwork = null;

        this.disconnected = false;
    }

    /**
     * @return identity of channel, use the underlying transport id
     */
    public String id() {
        return id;
    }

    /**
     * @return the principal of authenticator
     */
    public CobraPrincipal principal() {
        return authenticator.principal();
    }

    /**
     * @return the selection key of underlying transport layer
     */
    public SelectionKey selectionKey() {
        return transportLayer.selectionKey();
    }

    /**
     * @return the underlying socket of channel
     */
    public Socket socket() {
        return transportLayer.channel().socket();
    }

    /**
     * transit channel to given state
     *
     * @param channelState next channel's state
     */
    public void state(ChannelState channelState) {
        this.channelState = channelState;
    }

    public ChannelState state() {
        return channelState;
    }

    /**
     * @return true if channel completely done connection via transport layer
     */
    public boolean finishConnect() throws IOException {
        SocketChannel socketChannel = transportLayer.channel();
        if (socketChannel != null)
            remoteAddress = socketChannel.getRemoteAddress();

        boolean connected = transportLayer.isFinishConnection();
        if (connected) {
            if (ready())
                state(ChannelState.READY);
            else if (remoteAddress != null)
                state(new ChannelState(ChannelState.State.AUTHENTICATED, remoteAddress.toString()));
            else
                state(ChannelState.AUTHENTICATED);
        }

        return connected;
    }

    /**
     * @return true if underlying transport layer have connected
     */
    public boolean isConnected() {
        return transportLayer.isConnected();
    }

    /**
     * @return true if underlying transport layer ready and authenticator is completed
     */
    public boolean ready() {
        return transportLayer.ready() && authenticator.isCompleted();
    }

    /**
     * @return the inet address of socket transport layer which is connected.
     */
    public InetAddress socketAddress() {
        return socket().getInetAddress();
    }

    /**
     * @return the port number of socket which channel is connected
     */
    public int socketPort() {
        return socket().getPort();
    }

    /**
     * @return description of socket address, can be remote or local inet address
     */
    public String socketAddressDesc() {
        final Socket socket = socket();
        InetAddress inetAddress;
        if ((inetAddress = socket.getInetAddress()) != null)
            return inetAddress.toString();

        return socket.getLocalAddress().toString();
    }

    /**
     * prepare channel before do polling
     * <p>
     * if transport layer is not ready, do handshake().
     * <p>
     * if authentication is not completed, do authenticate()
     */
    public void prepare() {
        boolean authenticating = false;

        try {
            if (!transportLayer.ready())
                transportLayer.handshake();

            if (transportLayer.ready() && !authenticator.isCompleted()) {
                authenticating = true;
                authenticator.authenticate();
            }

        } catch (AuthenticationException e) {
            final String remoteAddress = this.remoteAddress != null ? this.remoteAddress.toString() : null;
            state(new ChannelState(ChannelState.State.FAILED_AUTHENTICATION, remoteAddress));
            if (authenticating) {
                delayCloseOnAuthenticationFail();
                throw new DelayedResponseAuthenticationException(e);
            }
            throw e;
        } catch (IOException e) {
            throw new CobraException(e);
        }

        if (ready()) {
            state(ChannelState.READY);
        }
    }

    /**
     * Call the underlying transport layer do disconnection
     */
    public void disconnect() {
        disconnected = true;
        if (channelState == ChannelState.NOT_CONNECTED && remoteAddress != null)
            state(new ChannelState(ChannelState.State.NOT_CONNECTED, remoteAddress.toString()));

        transportLayer.disconnect();
    }

    /**
     * @return current mute state of channel
     */
    public MuteState getMuteState() {
        return muteState;
    }

    /**
     * Do mute channel, if it is NOT_MUTED currently
     */
    public void mute() {
        if (muteState == MuteState.NOT_MUTED) {
            if (!disconnected)
                transportLayer.removeInterestOps(SelectionKey.OP_READ);

            muteState = MuteState.MUTED;
        }
    }

    /**
     * Unmute channel, if it is in MUTED currently
     *
     * @return true if state is NOT_MUTED
     */
    public boolean maybeUnmute() {
        if (muteState == MuteState.MUTED) {
            if (!disconnected)
                transportLayer.addInterestOps(SelectionKey.OP_READ);

            muteState = MuteState.NOT_MUTED;
        }

        return muteState == MuteState.NOT_MUTED;
    }

    /**
     * @return true if channel maybe in MUTED state
     */
    public boolean isInMutedState() {
        if (receiveNetwork == null || receiveNetwork.isMemoryAllocated())
            return false;

        return transportLayer.ready();
    }

    /**
     * @return true if channel has been explicitly muted
     */
    public boolean isMuted() {
        return getMuteState() != MuteState.NOT_MUTED;
    }

    /**
     * Handle specific channel muted event,
     *
     * @param event interest mute event
     */
    public void handleMuteEvent(MuteEvent event) {
        boolean changed = false;
        switch (event) {
            case REQUEST_RECEIVED -> {
                if (getMuteState() == MuteState.MUTED) {
                    muteState = MuteState.MUTED_AND_RESPONDING;
                    changed = true;
                }
            }

            case RESPONSE_SENT -> {
                if (getMuteState() == MuteState.MUTED_AND_RESPONDING) {
                    muteState = MuteState.MUTED;
                    changed = true;
                }
            }

        }

        if (!changed)
            throw new IllegalStateException("Could not transit from " + muteState.name() + " via event " + event.name());
    }

    /**
     * @return true if the {@link SendNetwork} is set into channel
     */
    public boolean hasSend() {
        return sendNetwork != null;
    }

    /**
     * set a network send into channel
     *
     * @param send network send
     */
    public void setSend(SendNetwork send) {
        if (sendNetwork != null)
            throw new IllegalStateException("Attempt to overwrite another send that is in progress");

        sendNetwork = send;
        this.transportLayer.addInterestOps(SelectionKey.OP_WRITE);
    }

    /**
     * check if a {@link SendNetwork} is set and completed, if completed, remove OP_WRITE from selector and set send
     * to null
     *
     * @return a network send that have completed
     */
    public SendNetwork maybeCompleteSend() {
        if (sendNetwork != null && sendNetwork.isCompleted()) {
            transportLayer.removeInterestOps(SelectionKey.OP_WRITE);
            final SendNetwork send = sendNetwork;
            sendNetwork = null;
            return send;
        }

        return null;
    }

    /**
     * perform write operation via transport layer, using prepared {@link SendNetwork} to write data.
     *
     * @return number of bytes that are written
     */
    public long write() throws IOException {
        if (sendNetwork == null)
            return 0;

        return sendNetwork.writeTo(transportLayer);
    }

    public ReceiveNetwork maybeCompletedReceive() {
        if (receiveNetwork != null && receiveNetwork.isCompleted()) {
            receiveNetwork.payload().rewind();
            final ReceiveNetwork receive = receiveNetwork;
            receiveNetwork = null;
            return receive;
        }
        return null;
    }

    /**
     * @return current {@link ReceiveNetwork} of channel
     */
    public ReceiveNetwork currentReceive() {
        return receiveNetwork;
    }

    /**
     * perform read operation via transport layer, read data into {@link ReceiveNetwork}
     *
     * @return number of bytes that are read
     */
    public long read() throws IOException {
        if (receiveNetwork == null)
            receiveNetwork = new ReceiveNetwork(id(), memoryAlloc);

        long reads;
        reads = receiveNetwork.readFrom(transportLayer);

        if (receiveNetwork.requiredMemoryKnown() && !receiveNetwork.isMemoryAllocated() && isInMutedState())
            mute();

        return reads;
    }

    /**
     * @return true if underlying transport layer has buffer
     */
    public boolean hasBuffer() {
        return transportLayer.hasBuffer();
    }

    void accumulateThreadElapsedTime(long deltaMillis) {
        threadElapsedTimeMillis += deltaMillis;
    }

    long getAndResetThreadElapsedTime() {
        final long result = threadElapsedTimeMillis;
        threadElapsedTimeMillis = 0;
        return result;
    }

    public boolean serverSideSessionExpired(long nanos) {
        final Long expiryTimeNanos = authenticator.serverSideSessionExpiryTimeNanos().orElse(null);
        return expiryTimeNanos != null && (nanos - expiryTimeNanos) > 0;
    }

    /**
     * delay channel close by authentication failure, this will remove write operation from selection key until
     * {@link #doCloseOnAuthenticationFail()} call.
     */
    private void delayCloseOnAuthenticationFail() {
        transportLayer.removeInterestOps(SelectionKey.OP_WRITE);
    }

    void doCloseOnAuthenticationFail() {
        transportLayer.addInterestOps(SelectionKey.OP_WRITE);
        authenticator.handleAuthenticationFailure();
    }

    @Override
    public void close() {
        disconnected = true;
        Utils.closeAllIfPossible(
                transportLayer,
                authenticator,
                receiveNetwork,
                sendNetwork
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        CobraChannel that = (CobraChannel) obj;
        return id().equals(that.id());
    }

    @Override
    public int hashCode() {
        return id().hashCode();
    }

    @Override
    public String toString() {
        return "CobraChannel(id = " + id() + ")";
    }

    public enum MuteState {
        NOT_MUTED,
        MUTED,
        MUTED_AND_RESPONDING,
    }

    public enum MuteEvent {
        REQUEST_RECEIVED,
        RESPONSE_SENT,
    }
}
