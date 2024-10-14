package org.cobra.networks.server;

import org.cobra.commons.Clock;
import org.cobra.commons.errors.CobraException;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.commons.threads.CobraThread;
import org.cobra.commons.utils.Utils;
import org.cobra.networks.*;
import org.cobra.networks.auth.SecurityProtocol;
import org.cobra.networks.requests.HeaderRequest;
import org.cobra.networks.server.internal.LocalCloseResponse;
import org.cobra.networks.server.internal.Response;
import org.cobra.networks.server.internal.SimpleRequest;
import org.cobra.networks.server.internal.SimpleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class Processor implements Runnable, AutoCloseable {

    private enum State {
        NOT_STARTED,
        STARTED;
    }

    private static final Logger log = LoggerFactory.getLogger(Processor.class);
    public static final int DEFAULT_CONN_QUEUED_CAPACITY = 126;

    private final int id;
    private final int connQueuedCapacity;
    private final Clock clock;
    private final SecurityProtocol securityProtocol;
    private final MemoryAlloc memoryAlloc;
    private final RequestChanel requestChanel;
    private final CobraSelector selector;
    private final CobraThread mainThread;
    private final BlockingQueue<SocketChannel> newSocketChannels;
    private final BlockingQueue<Response> responseQueue;
    private final Map<String, Response> inflightResponse;
    private final AtomicReference<State> stateReferent = new AtomicReference<>(State.NOT_STARTED);

    public Processor(
            int id,
            int connQueuedCapacity,
            long connIdleMaxMs,
            long failedAuthenticationDelayMs,
            String threadName,
            Clock clock,
            SecurityProtocol securityProtocol,
            RequestChanel requestChanel,
            MemoryAlloc memoryAlloc
    ) {
        this.id = id;
        this.connQueuedCapacity = connQueuedCapacity;
        this.clock = clock;
        this.securityProtocol = securityProtocol;
        this.requestChanel = requestChanel;
        this.memoryAlloc = memoryAlloc;

        this.selector = initiateSelector(
                ChannelBuilderFactory.defaultChannelBuilder(securityProtocol),
                connIdleMaxMs,
                failedAuthenticationDelayMs);

        this.newSocketChannels = new ArrayBlockingQueue<>(connQueuedCapacity);
        this.inflightResponse = new HashMap<>();
        this.responseQueue = new LinkedBlockingQueue<>();

        this.mainThread = CobraThread.nonDaemon(this, threadName);
    }

    public int id() {
        return id;
    }

    public void offerResponse(Response simpleResponse) throws InterruptedException {
        responseQueue.put(simpleResponse);
        wakeup();
    }

    public Response pollResponse() {
        final Response response = responseQueue.poll();
        if (response != null)
            response.getRequest().responseCompleteAtNanos = clock.nanoseconds();

        return response;
    }

    public void wakeup() {
        selector.wakeup();
    }

    @Override
    public void run() {
        try {
            while (stateReferent.get() == State.STARTED) {
                configureNewSocketChannels();
                processNewResponse();
                poll();
                processCompletedReceives();
                processCompletedSends();
                processDisconnectedChannels();
            }
        } catch (Exception e) {
            log.error("uncaught error", e);
        } finally {
            log.trace("closing processor {}", this);
            Utils.swallow("processor-closing-all-resources", this::closeAll);
        }
    }

    /**
     * Do selector poll with timeout, if we have new socket-channel need to configured, then timeout = 0, otherwise
     * timeout = 300.
     * If selector report exception, leave processor to continue without interrupt
     */
    void poll() {
        final long timeout = newSocketChannels.isEmpty() ? 300L : 0;
        try {
            selector.poll(timeout);
        } catch (IOException e) {
            log.error("uncaught error while doing polling; processor: {}", this);
        }
    }

    boolean accept(SocketChannel socketChannel, boolean maybeBlock) {
        boolean accepted = false;

        if (newSocketChannels.offer(socketChannel)) {
            accepted = true;
        } else if (maybeBlock) {
            try {
                newSocketChannels.put(socketChannel);
                accepted = true;
            } catch (InterruptedException e) {
                throw new CobraException(e);
            }
        }

        if (accepted)
            wakeup();

        return accepted;
    }

    /**
     * Start this processor if it is in NOT_STARTED, otherwise just do nothing.
     */
    void start() {
        if (stateReferent.get() == State.STARTED) return;

        /* set state to STARTED, start mainThread */
        stateReferent.set(State.STARTED);
        mainThread.start();
        log.debug("start processor {}", this);
    }

    /**
     * Send response to related-processor of response
     *
     * @param response a response to send
     * @param send     payload data
     */
    void sendResponse(Response response, Send send) {
        String channelId = response.getRequest().getRequestContext().getChannelId();
        log.trace("socket-processor {} received response to send to {}; register for read & write {}",
                id(), channelId, response);

        if (channel(channelId).isEmpty())
            log.warn("socket-processor {} attempt to send response via a channel that have no connection; " +
                    "channel: {}", id(), channelId);


        if (openOrClosingChannel(channelId).isPresent()) {
            selector.sent(new SendNetwork(channelId, send));
            inflightResponse.put(channelId, response);
        }
    }

    Optional<CobraChannel> openOrClosingChannel(String channelId) {
        final CobraChannel activeChannel = selector.channel(channelId);
        if (activeChannel != null) return Optional.of(activeChannel);

        final CobraChannel closingChannel = selector.closingChannel(channelId);
        if (closingChannel != null) return Optional.of(closingChannel);

        return Optional.empty();
    }

    Optional<CobraChannel> channel(String channelId) {
        return Optional.ofNullable(selector.channel(channelId));
    }

    void closeChannel(String channelId) {
        if (openOrClosingChannel(channelId).isEmpty()) {
            log.warn("attempt to close a channel that have been already closed; processor: {} channel_id: {};",
                    this, channelId);
            return;
        }

        selector.close(channelId);
        inflightResponse.remove(channelId);

        log.debug("closing channel {}", channelId);
    }

    void closeAll() {
        try {
            while (!newSocketChannels.isEmpty()) {
                newSocketChannels.poll().close();
                selector.allChannels().forEach(CobraChannel::close);
                selector.close();
            }
        } catch (IOException e) {
            throw new CobraException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (stateReferent.get() != State.STARTED) {
            log.debug("attempt to close a processor that not in STARTED; processor: {}", this);
            return;
        }

        /* set start to NOT_STARTED */
        stateReferent.set(State.NOT_STARTED);
        wakeup();

        /* interrupt mainThread */
        mainThread.join();

        try {
            closeAll();
        } catch (Exception e) {
            log.error("error while close all resources", e);
        }
    }

    private CobraSelector initiateSelector(ChannelBuilder channelBuilder, long connIdleMaxMs, long delayFailAuthMs) {
        return new CobraSelector(connIdleMaxMs, delayFailAuthMs, true, channelBuilder, clock, memoryAlloc);
    }

    private void configureNewSocketChannels() {
        int processed = 0;
        while (processed < connQueuedCapacity && !newSocketChannels.isEmpty()) {
            final SocketChannel socketChannel = newSocketChannels.poll();
            try {
                selector.register(CobraChannelIdentifier.identifier(socketChannel.socket()), socketChannel);
                processed++;
                log.debug("listen to new socket channel {}; processor_id: {}", socketChannel.socket(), id());
            } catch (IOException e) {
                log.error("error while configure socket channel {}; processor_id: {}", socketChannel.socket(), id(), e);
            }
        }
    }

    private void processNewResponse() {
        Response response;
        while ((response = pollResponse()) != null) {
            final String channelId = response.getRequest().getRequestContext().getChannelId();
            switch (response) {
                case SimpleResponse simpleResponse -> sendResponse(simpleResponse, simpleResponse.getSend());

                case LocalCloseResponse localCloseResponse -> {
                    closeChannel(channelId);
                    log.trace("socket-processor receive event to close; event: {}", localCloseResponse);
                }

                case null, default -> throw new IllegalStateException("Unexpected value: " + response);
            }
        }
    }

    private void processCompletedReceives() {
        final Collection<ReceiveNetwork> completedReceives = selector.completedReceives();
        for (ReceiveNetwork receiveNetwork : completedReceives) {
            final String channelId = receiveNetwork.source();

            try {
                final Optional<CobraChannel> maybeAChannel = openOrClosingChannel(channelId);
                if (maybeAChannel.isEmpty())
                    throw new IllegalStateException("Channel is removed from select before process receives; " +
                            "processor: " + this + "; channel_id: " + channelId);

                /* parse buffer (payload) -> HeaderRequest */
                final HeaderRequest headerRequest = HeaderRequest.parse(receiveNetwork.payload());

                /* check is session expired */
                final long nowNanos = clock.nanoseconds();
                final CobraChannel channel = maybeAChannel.get();
                if (channel.serverSideSessionExpired(nowNanos)) {
                    log.debug("about to closing expired channel {}", channelId);
                    closeChannel(channelId);
                    return;
                }

                /* queue request to RequestChannel plane in order to process */
                final RequestContext requestContext = new RequestContext(securityProtocol, headerRequest,
                        channel.socketAddress(), channel.principal());
                final SimpleRequest simpleRequest = new SimpleRequest(id(), receiveNetwork.payload(),
                        requestContext, memoryAlloc);

                requestChanel.event(simpleRequest);
                selector.mute(channelId);
                handleChannelMuteEvent(channelId, CobraChannel.MuteEvent.REQUEST_RECEIVED);
            } catch (Exception e) {
                caughtError(channelId, e, "error while processing receives from channel " + channelId);
            }
        }
    }

    private void processCompletedSends() {
        final Collection<SendNetwork> completedSends = selector.completedSends();
        for (SendNetwork sendNetwork : completedSends) {
            final String channelId = sendNetwork.getDestination();
            try {
                final Response response = inflightResponse.remove(channelId);
                if (response == null)
                    throw new IllegalStateException("Response is null for channel " + channelId);

                handleChannelMuteEvent(channelId, CobraChannel.MuteEvent.RESPONSE_SENT);
                attemptToUnmuteChannel(channelId);
            } catch (Exception e) {
                caughtError(channelId, e, "error while processing sends to channel " + channelId);
            }
        }
    }

    private void processDisconnectedChannels() {
        final Collection<String> disconnectedChannels = selector.disconnectedChannels().keySet();
        disconnectedChannels.forEach(inflightResponse::remove);
    }

    private void attemptToUnmuteChannel(String channelId) {
        openOrClosingChannel(channelId).ifPresent(CobraChannel::maybeUnmute);
    }

    private void handleChannelMuteEvent(String channelId, CobraChannel.MuteEvent muteEvent) {
        openOrClosingChannel(channelId).ifPresent(ch -> ch.handleMuteEvent(muteEvent));
    }

    private void caughtError(String channelId, Throwable cause, String message) {
        if (openOrClosingChannel(channelId).isPresent()) {
            closeChannel(channelId);
            log.warn("closing channel {} due to uncaught error", channelId, cause);
        }

        log.error(message, cause);
    }

    @Override
    public String toString() {
        return "Processor(" +
                "id=" + id +
                ", connQueuedCapacity=" + connQueuedCapacity +
                ", mainThread=" + mainThread +
                ", securityProtocol=" + securityProtocol +
                ", stateReferent=" + stateReferent +
                ')';
    }
}
