package org.cobra.networks;

import org.cobra.commons.Clock;
import org.cobra.commons.Jvm;
import org.cobra.commons.errors.AuthenticationException;
import org.cobra.commons.errors.CobraException;
import org.cobra.commons.errors.DelayedResponseAuthenticationException;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.commons.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A nio.Selector interface for doing non-blocking multi-connection network I/O.
 * <p>
 * {@link SendNetwork} and {@link ReceiveNetwork} contribute into transmission size-delimited network request and response.
 * <p>
 * A connection can be added  to the nio.Selector associated with an id of transport layer.
 * <p>
 * The connect() call does not block on the creation of the TCP connection, so connect() call only begins initiating
 * the connection. The successful invocation of this method does not mean a valid connection has been established.
 * <p>
 * Send requests, receives response, processing connection completions, disconnections are all happened in poll()
 */
public class CobraSelector implements Selectable {

    private static final Logger log = LoggerFactory.getLogger(CobraSelector.class);
    public static final long NO_FAILED_AUTHENTICATION_DELAY = 0L;

    /**
     * Closing mode use to mark a channel closing is notifiable or not.
     */
    private enum ClosingMode {
        /* process outstanding buffer receives, notify disconnected */
        GRACEFUL(true),
        /* discard any outstanding receives, notify disconnect */
        NOTIFY_ONLY(true),
        /* discard any outstanding receives, and do NOT notify disconnect */
        DISCARD_NO_NOTIFY(false);

        final boolean notifiable;

        ClosingMode(boolean notifiable) {
            this.notifiable = notifiable;
        }
    }

    private final java.nio.channels.Selector ioSelector;
    private final Map<String, CobraChannel> channelMap;
    private final Map<String, CobraChannel> closingChannelMap;
    private final Map<String, ChannelState> disconnectedChannelMap;
    private final LinkedHashMap<String, ReceiveNetwork> completedReceiveMap;
    private final LinkedHashMap<String, DelayAuthenticationFailureClose> delayAuthFailureCloseMap;
    private final Set<SelectionKey> immediateKeys;
    private final Set<CobraChannel> explicitMutedChannels;
    private final List<String> connectedChannels;
    private final List<String> failedSends;
    private final List<SendNetwork> completedSends;
    private final ChannelBuilder channelBuilder;
    private final Clock clock;
    private final MemoryAlloc memoryAlloc;
    private final IdleChannelControlPlane idleNodeControl;
    private final long delayAuthenticationMs;
    private final boolean recordTimePerConnection;
    private Set<SelectionKey> keyWithBufferedRead;
    private boolean hasReadProgressLasPoll = false;

    public CobraSelector(
            long connIdleMs,
            ChannelBuilder channelBuilder,
            Clock clock,
            MemoryAlloc memoryAlloc
    ) {
        this(connIdleMs, true, channelBuilder, clock, memoryAlloc);
    }

    public CobraSelector(
            long connIdleMs,
            boolean recordTimePerConnection,
            ChannelBuilder channelBuilder,
            Clock clock,
            MemoryAlloc memoryAlloc
    ) {
        this(connIdleMs, NO_FAILED_AUTHENTICATION_DELAY, recordTimePerConnection, channelBuilder, clock, memoryAlloc);
    }

    public CobraSelector(
            long connIdleMs,
            long delayAuthenticationMs,
            boolean recordTimePerConnection,
            ChannelBuilder channelBuilder,
            Clock clock,
            MemoryAlloc memoryAlloc
    ) {
        try {
            ioSelector = java.nio.channels.Selector.open();
        } catch (IOException e) {
            throw new CobraException("Fail to open nio-selector", e);
        }

        this.delayAuthenticationMs = delayAuthenticationMs;
        this.channelBuilder = channelBuilder;
        this.clock = clock;
        this.memoryAlloc = memoryAlloc;
        this.recordTimePerConnection = recordTimePerConnection;

        this.idleNodeControl = new IdleChannelControlPlane(Duration.ofMillis(connIdleMs).toNanos(), clock);

        this.channelMap = new HashMap<>();
        this.closingChannelMap = new HashMap<>();
        this.disconnectedChannelMap = new HashMap<>();
        this.immediateKeys = new HashSet<>();
        this.explicitMutedChannels = new HashSet<>();
        this.keyWithBufferedRead = new HashSet<>();
        this.connectedChannels = new ArrayList<>();
        this.failedSends = new ArrayList<>();
        this.completedSends = new ArrayList<>();
        this.completedReceiveMap = new LinkedHashMap<>();
        this.delayAuthFailureCloseMap = delayAuthenticationMs > NO_FAILED_AUTHENTICATION_DELAY ?
                new LinkedHashMap<>() : null;
    }

    public CobraChannel channel(String channelId) {
        return channelMap.get(channelId);
    }

    public CobraChannel closingChannel(String channelId) {
        return closingChannelMap.get(channelId);
    }

    public Collection<CobraChannel> allChannels() {
        return channelMap.values();
    }

    /**
     * Retrieve current open/closing channel by node id
     *
     * @param channelId node id
     * @return open/closing channel
     */
    private CobraChannel getOpenOrClosingChannel(String channelId) {
        CobraChannel channel = channelMap.get(channelId);
        if (channel == null)
            channel = closingChannelMap.get(channelId);
        if (channel == null)
            throw new IllegalStateException("attempt to get a connection that we dont have; node = " + channelId);

        return channel;
    }

    @Override
    public SocketChannel connect(String channelId, InetSocketAddress address, int sendBufferSize, int rcvBufferSize) throws IOException {
        ensureNotDuplicateId(channelId);
        SocketChannel socketChannel = SocketChannel.open();
        SelectionKey key = null;

        try {
            configureSocketChannel(socketChannel, sendBufferSize, rcvBufferSize);
            boolean connected = doConnect(socketChannel, address);
            key = doRegisterChannel(channelId, socketChannel, SelectionKey.OP_CONNECT);

            if (connected) {
                immediateKeys.add(key);
                key.interestOps(0);
                log.debug("immediately connect to node {}", channelId);
            }
        } catch (IOException | RuntimeException e) {
            if (key != null)
                immediateKeys.remove(key);

            channelMap.remove(channelId);
            socketChannel.close();
            throw e;
        }

        return socketChannel;
    }

    public void register(String channelId, SocketChannel socketChannel) throws IOException {
        ensureNotDuplicateId(channelId);
        doRegisterChannel(channelId, socketChannel, SelectionKey.OP_READ);
    }

    private void configureSocketChannel(SocketChannel socketChannel, int sndBufSize, int rcvBufSize) throws IOException {
        socketChannel.configureBlocking(false);
        Socket socket = socketChannel.socket();

        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);

        if (sndBufSize != Jvm.USE_DEFAULT_SOCKET_BUFFER_SIZE)
            socket.setSendBufferSize(sndBufSize);

        if (rcvBufSize != Jvm.USE_DEFAULT_SOCKET_BUFFER_SIZE)
            socket.setReceiveBufferSize(rcvBufSize);
    }

    /**
     * Register socket-channel to current nio.Selector with given interest ops.
     * Put it to current active channel-map and idle-control
     */
    private SelectionKey doRegisterChannel(String channelId, SocketChannel socketChannel, int interestOps) throws IOException {
        SelectionKey key = socketChannel.register(ioSelector, interestOps);
        CobraChannel channel = buildAndAttachChannel(channelId, key);

        channelMap.put(channelId, channel);

        // update idle-node-control
        idleNodeControl.update(channel.id(), clock.nanoseconds());

        return key;
    }

    protected boolean doConnect(SocketChannel socketChannel, InetSocketAddress address) throws IOException {
        return socketChannel.connect(address);
    }

    /**
     * Use channel-builder to build a {@link CobraChannel} and attach to socket-chanel key
     */
    private CobraChannel buildAndAttachChannel(String channelId, SelectionKey key) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            final CobraChannel channel = channelBuilder.build(channelId, key, memoryAlloc);
            key.attach(channel);
            return channel;
        } catch (Exception e) {
            try {
                socketChannel.close();
            } finally {
                key.cancel();
            }
            throw new IOException("Fail to create channel for socket " + socketChannel, e);
        }
    }

    @Override
    public void wakeup() {
        ioSelector.wakeup();
    }

    /**
     * Ensure that this id is not register yet, to prevent overwrite in channel map
     *
     * @param channelId channel ID
     */
    private void ensureNotDuplicateId(String channelId) {
        if (channelMap.containsKey(channelId))
            throw new IllegalStateException("Channel " + channelId + " already registered");

        if (closingChannelMap.containsKey(channelId))
            throw new IllegalStateException("Channel " + channelId + " already closed");
    }

    @Override
    public void poll(long timeout) throws IOException {
        if (timeout < 0)
            throw new IllegalArgumentException("Negative timeout");

        boolean readOnLastCall = hasReadProgressLasPoll;
        clearResult();

        boolean dataInBuffers = !keyWithBufferedRead.isEmpty();

        if (!immediateKeys.isEmpty() || (readOnLastCall && dataInBuffers))
            timeout = 0;

        long beginSelectTimeNanos = clock.nanoseconds();
        int selectedNum = select(timeout);
        long endSelectTimeNanos = clock.nanoseconds();
        if (selectedNum > 0)
            log.trace("poll selection {} key(s); elapsed_time = {}", selectedNum,
                    (endSelectTimeNanos - beginSelectTimeNanos));

        if (selectedNum > 0 || !immediateKeys.isEmpty() || dataInBuffers) {
            Set<SelectionKey> readyKeys = ioSelector.selectedKeys();

            // poll from channels that have buffer data
            if (dataInBuffers) {
                // remove all read_keys, we don't want to poll a key twice
                keyWithBufferedRead.removeAll(readyKeys);
                Set<SelectionKey> toPollKeys = keyWithBufferedRead;

                // reset it after assign a temp variable
                keyWithBufferedRead = new HashSet<>();

                processPollKeys(toPollKeys, false);
            }

            // poll from channels that underlying socket have data
            processPollKeys(readyKeys, false);
            readyKeys.clear();

            // poll from immediate connected channel
            processPollKeys(immediateKeys, true);
            immediateKeys.clear();
        } else {
            hasReadProgressLasPoll = true;
        }

        long endIONanos = clock.nanoseconds();
        completeDelayedChannelCloses(endIONanos);
        maybeCloseLruChannel(endIONanos);
    }

    /**
     * Invoke nio.Selector to select ready keys.
     *
     * @param timeout timeout of selecting, if timeout = 0 selectNow(), otherwise select(timeout)
     * @return number of ready keys.
     */
    private int select(long timeout) throws IOException {
        if (timeout == 0)
            return ioSelector.selectNow();

        return ioSelector.select(timeout);
    }

    private void processPollKeys(Set<SelectionKey> toPollKeys, boolean isImmediately) {
        for (final SelectionKey key : toPollKeys) {
            CobraChannel channel = (CobraChannel) key.attachment();
            String node = channel.id();
            long beginChannelNanos = clock.nanoseconds();
            boolean sendFail = false;

            idleNodeControl.update(node, beginChannelNanos);

            try {
                // complete any I/O that have finished handshaking (either normal or immediate)
                if (isImmediately || key.isConnectable()) {
                    if (channel.finishConnect()) {
                        connectedChannels.add(node);

                        Socket socket = channel.socket();
                        log.debug("create socket channel; id: {}, SO_RCVBUF: {}, SO_SNDBUF: {}, SO_TIMEOUT: {}",
                                node, socket.getReceiveBufferSize(), socket.getSendBufferSize(), socket.getSoTimeout());
                    } else {
                        continue;
                    }
                }

                // if channel is not ready, do complete prepare
                if (channel.isConnected() && !channel.ready()) {
                    channel.prepare();
                    if (channel.ready()) log.debug("successfully authenticated with {}", channel.socketAddressDesc());
                }

                // bring channels to READY
                if (channel.ready() && channel.state() == ChannelState.NOT_CONNECTED)
                    channel.state(ChannelState.READY);


                // if channel is ready and has bytes to read from socket/buffer, has not previous completed receive
                if (channel.ready() && (key.isReadable() || channel.hasBuffer())
                        && !hasCompletedReceive(channel)
                        && !explicitMutedChannels.contains(channel)) {
                    attemptToRead(channel);
                }

                if (channel.hasBuffer() && !explicitMutedChannels.contains(channel)) {
                    keyWithBufferedRead.add(key);
                }

                try {
                    attemptToWrite(key, channel);
                } catch (Exception e) {
                    sendFail = true;
                    log.warn("channel send fail; node = {}", node, e);
                    throw e;
                }

                // cancel any defunct socket
                if (!key.isValid())
                    doClose(channel, ClosingMode.GRACEFUL);
            } catch (Exception e) {
                String desc = String.format("%s (node = %s)", channel.socketAddressDesc(), node);
                if (e instanceof IOException) {
                    log.debug("Connection with {} disconnected", desc, e);
                } else if (e instanceof AuthenticationException) {
                    String exMessage = e.getMessage();
                    if (e instanceof DelayedResponseAuthenticationException) {
                        exMessage = e.getCause().getMessage();
                    }

                    log.info("Failed authentication with {}, ({})", desc, exMessage);
                } else {
                    log.warn("Unexpected error from {}; closing channel", desc, e);
                }

                if (e instanceof DelayedResponseAuthenticationException) {
                    closeOnAuthenticationFail(channel);
                } else {
                    doClose(channel, sendFail ? ClosingMode.NOTIFY_ONLY : ClosingMode.GRACEFUL);
                }
            } finally {
                recordSpentTimeOnChannel(channel, beginChannelNanos);
            }
        }
    }

    @Override
    public void sent(SendNetwork send) {
        String node = send.getDestination();
        CobraChannel channel = getOpenOrClosingChannel(node);

        if (closingChannelMap.containsKey(node)) {
            failedSends.add(node);
            return;
        }

        try {
            channel.setSend(send);
        } catch (Exception e) {
            channel.state(ChannelState.FAILED_SEND);
            this.failedSends.add(node);
            doClose(channel, ClosingMode.DISCARD_NO_NOTIFY);
            if (!(e instanceof CancelledKeyException)) {
                log.error("error while send data, closing node {} and throw error", node, e);
                throw e;
            }
        }
    }

    @Override
    public List<SendNetwork> completedSends() {
        return completedSends;
    }

    @Override
    public List<ReceiveNetwork> completedReceives() {
        return completedReceiveMap.values().stream().toList();
    }

    @Override
    public List<String> connectedChannels() {
        return connectedChannels;
    }

    @Override
    public Map<String, ChannelState> disconnectedChannels() {
        return disconnectedChannelMap;
    }

    @Override
    public boolean isReadyChannel(String channelId) {
        CobraChannel channel = channelMap.get(channelId);
        return channel != null && channel.ready();
    }

    private boolean maybeReadFromClosingChannel(CobraChannel channel) {
        boolean pending;
        if (channel.state().value() != ChannelState.State.READY) {
            pending = false;
        } else if (explicitMutedChannels.contains(channel) || hasCompletedReceive(channel)) {
            pending = true;
        } else {
            try {
                attemptToRead(channel);
                pending = hasCompletedReceive(channel);
            } catch (Throwable cause) {
                log.trace("error while reading from closing channel", cause);
                pending = false;
            }
        }
        return pending;
    }

    /**
     * Clear all result that a poll() can be gathered, we do not want to process a result twice
     */
    private void clearResult() {
        completedReceiveMap.clear();
        completedSends.clear();
        disconnectedChannelMap.clear();
        connectedChannels.clear();

        final Iterator<Map.Entry<String, CobraChannel>> closingChannelIt = closingChannelMap.entrySet().iterator();
        while (closingChannelIt.hasNext()) {
            final CobraChannel channel = closingChannelIt.next().getValue();
            boolean sendFailed = failedSends.remove(channel.id());
            boolean hasPending = false;

            if (!sendFailed)
                hasPending = maybeReadFromClosingChannel(channel);
            if (!hasPending) {
                doClose(channel, true);
                closingChannelIt.remove();
            }
        }

        for (String node : failedSends)
            disconnectedChannelMap.put(node, ChannelState.FAILED_SEND);

        failedSends.clear();
        hasReadProgressLasPoll = false;
    }

    /* visibility test */
    void clearCompletedReceives() {
        completedReceiveMap.clear();
    }

    /* visibility test */
    void clearCompletedSends() {
        completedSends.clear();
    }

    private void maybeCloseLruChannel(long atNanos) {
        Map.Entry<String, Long> expiredNode = idleNodeControl.pollLruExpiredNode(atNanos);
        if (expiredNode == null)
            return;

        String node = expiredNode.getKey();
        CobraChannel channel = channel(node);
        if (channel != null) {
            if (log.isDebugEnabled()) {
                log.debug("about to close idle node from {} due to idle time for {}ms",
                        node, Duration.ofNanos(atNanos - expiredNode.getValue()).toMillis());
            }

            channel.state(ChannelState.EXPIRED);
            doClose(channel, ClosingMode.GRACEFUL);
        }
    }

    private void completeDelayedChannelCloses(long atNanos) {
        if (delayAuthFailureCloseMap == null)
            return;

        while (!delayAuthFailureCloseMap.isEmpty()) {
            DelayAuthenticationFailureClose delayClose = delayAuthFailureCloseMap.values().iterator().next();
            if (!delayClose.tryClose(atNanos))
                break;
        }
    }

    private void recordSpentTimeOnChannel(CobraChannel channel, long beginAtNanos) {
        if (recordTimePerConnection)
            channel.accumulateThreadElapsedTime(clock.nanoseconds() - beginAtNanos);
    }

    private void closeOnAuthenticationFail(CobraChannel channel) {
        DelayAuthenticationFailureClose delayClose = new DelayAuthenticationFailureClose(channel,
                this::handleCloseOnAuthenticationFailCallback,
                delayAuthenticationMs);

        if (delayAuthFailureCloseMap != null)
            delayAuthFailureCloseMap.put(channel.id(), delayClose);
        else
            delayClose.closeNow();
    }

    private void handleCloseOnAuthenticationFailCallback(CobraChannel channel) {
        try {
            channel.doCloseOnAuthenticationFail();
        } catch (Exception e) {
            log.error("error while closing on authentication failure; node = {}", channel.id(), e);
        } finally {
            doClose(channel, ClosingMode.GRACEFUL);
        }

    }

    private void doClose(CobraChannel closingChannel, boolean notifyClose) {
        SelectionKey key = closingChannel.selectionKey();
        try {
            immediateKeys.remove(key);
            keyWithBufferedRead.remove(key);
            closingChannel.close();
        } catch (Exception e) {
            log.error("error while do closing channel; node = {}", closingChannel.id(), e);
        } finally {
            key.cancel();
            key.attach(null);
        }

        explicitMutedChannels.remove(closingChannel);

        if (notifyClose)
            disconnectedChannelMap.put(closingChannel.id(), closingChannel.state());

        log.debug("close channel {}", closingChannel.id());
    }

    private void doClose(CobraChannel closingChannel, ClosingMode closingMode) {
        closingChannel.disconnect();

        // ensure that `connected_nodes` not have any closed channel. This could happen if `prepare` throws exception
        // in the poll() invocation when finishConnect() succeeds.
        connectedChannels.remove(closingChannel.id());

        // keep tracking of closed channel with pending receives so that all records maybe processed, for example
        // when producer with ack=0 sends some records and close its connection, a single poll() may receive records
        // and handle close(). When remote closes connection, the channel is retained until a send fails or all
        // outstanding receives are processed one-by-one in order
        if (closingMode == ClosingMode.GRACEFUL && maybeReadFromClosingChannel(closingChannel)) {
            closingChannelMap.put(closingChannel.id(), closingChannel);
            log.debug("tracking closing channel {}, process outstanding request", closingChannel.id());
        } else {
            doClose(closingChannel, closingMode.notifiable);
        }

        channelMap.remove(closingChannel.id());

        if (delayAuthFailureCloseMap != null)
            delayAuthFailureCloseMap.remove(closingChannel.id());

        idleNodeControl.remove(closingChannel.id());
    }

    /**
     * Attempt to perform a channel read operation, if read bytes > 0, mark selector "hasReadProgressLastPoll" and
     * retrieve completed {@link ReceiveNetwork} from channel
     *
     * @param channel channel to read
     */
    private void attemptToRead(CobraChannel channel) throws IOException {
        long reads = channel.read();

        if (reads != 0) {
            hasReadProgressLasPoll = true;
            ReceiveNetwork receive = channel.maybeCompletedReceive();

            if (receive != null)
                addCompletedReceive(channel, receive);
        }

        if (!channel.isMuted())
            hasReadProgressLasPoll = true;
    }

    /**
     * Check if given selection key is writable and channel has a set {@link SendNetwork}, then do write and check
     * completed send of channel.
     *
     * @param key     selection key of channel
     * @param channel channel write to
     */
    private void attemptToWrite(SelectionKey key, CobraChannel channel) throws IOException {
        if (channel.hasSend() && channel.ready() && key.isWritable()) {
            write(channel);
        }
    }

    /**
     * Do write operation to given channel, then check if {@link SendNetwork} is completed, if has, put to
     * "completed-sends" of selector
     *
     * @param channel channel to write
     */
    void write(CobraChannel channel) throws IOException {
        long writes = channel.write();
        SendNetwork send = channel.maybeCompleteSend();

        if (writes > 0 && send != null)
            addCompletedSend(send);
    }

    /**
     * Put a {@link SendNetwork} to "completed-sends" of this selector
     *
     * @param sendNetwork {@link SendNetwork} of channel
     */
    private void addCompletedSend(SendNetwork sendNetwork) {
        completedSends.add(sendNetwork);
    }

    /**
     * Mark channel has completed receive, put {@link ReceiveNetwork} to "completed-receives" of current selector
     *
     * @param channel channel that has receive
     * @param receive {@link ReceiveNetwork} of channel
     */
    private void addCompletedReceive(CobraChannel channel, ReceiveNetwork receive) {
        if (hasCompletedReceive(channel))
            throw new IllegalStateException("channel " + channel.id() + " already completed receive");

        completedReceiveMap.put(channel.id(), receive);
    }

    /**
     * Check if completed-receives contains channel
     *
     * @param channel channel to check
     * @return true if channel has received, otherwise false
     */
    private boolean hasCompletedReceive(CobraChannel channel) {
        return completedReceiveMap.containsKey(channel.id());
    }


    @Override
    public void mute(String channelId) {
        CobraChannel channel = getOpenOrClosingChannel(channelId);
        doMute(channel);
    }

    @Override
    public void unmute(String channelId) {
        CobraChannel channel = getOpenOrClosingChannel(channelId);
        doUnmute(channel);
    }

    @Override
    public void close(String channelId) {
        CobraChannel channel = channel(channelId);
        if (channel != null) {
            channel.state(ChannelState.LOCAL_CLOSE);
            doClose(channel, ClosingMode.DISCARD_NO_NOTIFY);
        } else {
            channel = closingChannel(channelId);
            doClose(channel, false);
        }
    }

    @Override
    public void close() throws IOException {
        List<String> channels = new ArrayList<>(channelMap.keySet());
        Utils.closeSilently("release channels",
                channels.stream()
                        .map(id -> (AutoCloseable) () -> close(id))
                        .collect(Collectors.toList()));
        Utils.closeSilently("release nio-selector", ioSelector);
        Utils.closeSilently("release buffer-channel", channelBuilder);
    }

    /**
     * Mute channel, then add this channel to "explicitMute" and remove "keyWithBufferRead"
     *
     * @param channel channel to mute
     */
    private void doMute(CobraChannel channel) {
        channel.mute();
        addExplicitMuteChannel(channel);
        removeKeyWithBufferedRead(channel.selectionKey());
    }

    /**
     * Unmute a channel, if not maybe Unmute just end.
     * Remove from "explicitMute" and add again "keyWithBufferRead" if has buffer
     *
     * @param channel channel to unmute
     */
    private void doUnmute(CobraChannel channel) {
        if (channel.maybeUnmute()) {
            removeExplicitMuteChannel(channel);
            if (channel.hasBuffer()) {
                addKeyWithBufferedRead(channel.selectionKey());
                hasReadProgressLasPoll = true;
            }
        }
    }

    private void addExplicitMuteChannel(CobraChannel channel) {
        explicitMutedChannels.add(channel);
    }

    private void removeExplicitMuteChannel(CobraChannel channel) {
        explicitMutedChannels.remove(channel);
    }

    private void addKeyWithBufferedRead(SelectionKey key) {
        keyWithBufferedRead.add(key);
    }

    private void removeKeyWithBufferedRead(SelectionKey key) {
        keyWithBufferedRead.remove(key);
    }

}
