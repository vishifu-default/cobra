package org.cobra.networks.client;

import org.cobra.commons.Clock;
import org.cobra.commons.errors.AuthenticationException;
import org.cobra.networks.*;
import org.cobra.networks.requests.AbstractRequest;
import org.cobra.networks.requests.AbstractResponse;
import org.cobra.networks.requests.HeaderRequest;
import org.cobra.networks.requests.RequestCompletionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class CobraNetworkClient implements NetworkClient {

    private enum State {
        ACTIVE, CLOSING, CLOSED
    }

    private static final Logger log = LoggerFactory.getLogger(CobraNetworkClient.class);

    private final Selectable selector;
    private final ChannelNode channelNode;
    private final InflightRequestTopic inflightRequestTopic;
    private final Clock clock;
    private final AtomicReference<State> atomicState;
    private final ConnectionStateControl connectionStateControl;
    private Optional<String> clientId;
    private final int socketSendBufferSize;
    private final int socketReceiveBufferSize;
    private final AtomicLong correlation;
    private final long defaultRequestTimeoutMillis;

    private SocketChannel underlyingConnectedChannel;

    public CobraNetworkClient(
            Selectable selectable,
            Clock clock,
            ChannelNode channelNode,
            int socketSendBufferSize,
            int socketReceiveBufferSize,
            int maxInflightRequestPerConnection,
            long reconnectBackoffMs,
            long reconnectBackoffMaxMs,
            long connectionSetupTimeoutMs,
            long connectionSetupTimeoutMaxMs,
            long defaultRequestTimeoutMillis
    ) {
        this.selector = selectable;
        this.clock = clock;
        this.channelNode = channelNode;
        this.socketSendBufferSize = socketSendBufferSize;
        this.socketReceiveBufferSize = socketReceiveBufferSize;
        this.defaultRequestTimeoutMillis = defaultRequestTimeoutMillis;
        this.correlation = new AtomicLong(0);

        this.inflightRequestTopic = new InflightRequestTopic(maxInflightRequestPerConnection);
        this.connectionStateControl = new ConnectionStateControl(reconnectBackoffMs, reconnectBackoffMaxMs,
                connectionSetupTimeoutMs, connectionSetupTimeoutMaxMs);
        this.atomicState = new AtomicReference<>(State.ACTIVE);
    }

    @Override
    public boolean ready(long nowMs) {
        if (isReady(nowMs))
            return true;

        if (connectionStateControl.canConnect(nowMs))
            initiateConnection(nowMs);

        return false;
    }

    private void initiateConnection(long nowMs) {
        try {
            /* mark control to CONNECTING state */
            connectionStateControl.connecting(nowMs);
            InetSocketAddress socketAddress = new InetSocketAddress(channelNode.host(), channelNode.port());

            /* connect to node via selector */
            underlyingConnectedChannel = selector.connect(channelNode.id(), socketAddress, socketSendBufferSize,
                    socketReceiveBufferSize);
            log.info("initiating connection to {}; client_id: {}", channelNode, clientId);

        } catch (IOException e) {
            log.warn("error while initiating connection {}; client_id: {}", channelNode, clientId, e);
            connectionStateControl.disconnected(nowMs);
        }
    }

    @Override
    public boolean isReady(long nowMs) {
        return canSendRequest();
    }

    private boolean canSendRequest() {
        if (underlyingConnectedChannel == null)
            return false;

        try {
            if (underlyingConnectedChannel.getLocalAddress() != null) {
                clientId =
                        Optional.of(CobraChannelIdentifier.identifier((InetSocketAddress) underlyingConnectedChannel.getLocalAddress()));
            }
        } catch (IOException e) {
            return false;
        }

        return connectionStateControl.isReady()
                && selector.isReadyChannel(channelNode.id())
                && inflightRequestTopic.canSendMore()
                && clientId.isPresent();
    }

    @Override
    public boolean active() {
        return atomicState.get() == State.ACTIVE;
    }

    @Override
    public boolean isConnectFailed() {
        return connectionStateControl.isDisconnected();
    }

    @Override
    public Optional<AuthenticationException> authenticationException() {
        return Optional.ofNullable(connectionStateControl.getAuthException());
    }

    @Override
    public long connectionDelayMs(long nowMs) {
        return connectionStateControl.ioWorkDelayMs(nowMs);
    }

    @Override
    public long pollDelayMs(long nowMs) {
        return connectionStateControl.ioWorkDelayMs(nowMs);
    }

    @Override
    public void send(ClientRequest clientRequest, long nowMs) {
        ensureActive();

        if (!canSendRequest())
            throw new IllegalStateException("Client can not send request now");

        if (!clientRequest.getDestination().equals(channelNode.id()))
            throw new IllegalStateException("Mismatched destination; expected: " + channelNode.id() +
                    ", actual: " + clientRequest.getDestination());

        final String destination = clientRequest.getDestination();
        final HeaderRequest headerRequest = clientRequest.toHeaderRequest();

        log.debug("sending {} request to {} at {}ms; header: {}; timeout: {}",
                clientRequest.apikey(), destination, nowMs, headerRequest, clientRequest.getTimeoutMs());

        /* send to selector */
        AbstractRequest request = clientRequest.getRequestBuilder().build();
        Send toSend = request.toSend(headerRequest);
        InflightRequest inflightRequest = new InflightRequest(clientRequest, headerRequest, request, toSend, nowMs);
        inflightRequestTopic.offer(inflightRequest);
        selector.sent(new SendNetwork(destination, toSend));
    }

    @Override
    public List<ClientResponse> poll(long timeout) {
        ensureActive();

        try {
            /* do selector polling */
            selector.poll(timeout);
        } catch (IOException e) {
            log.error("error while polling request; client_id: {}", clientId, e);
        }

        /* handle sends, receives, timeout request ... */
        long nowMs = clock.milliseconds();
        List<ClientResponse> responses = new ArrayList<>();
        handleCompleteReceives(responses, nowMs);
        handleDisconnected(responses, nowMs);
        handleConnection();
        handleTimeoutConnection(responses, nowMs);
        handleTimeoutRequest(responses, nowMs);
        doCompleteResponse(responses);

        return responses;
    }

    @Override
    public void disconnect() {
        if (connectionStateControl.isDisconnected()) {
            log.debug("client requested disconnection from {}, which was already disconnected; client_id: {}",
                    channelNode, clientId);
            return;
        }

        selector.close(channelNode.id());
        connectionStateControl.disconnected(clock.milliseconds());
        log.info("client requested disconnection from {}; client_id: {}", channelNode, clientId);
    }

    @Override
    public void wakeup() {
        selector.wakeup();
    }

    @Override
    public ClientRequest createClientRequest(AbstractRequest.Builder<?> requestBuilder, long creationMs) {
        return createClientRequest(requestBuilder, creationMs, defaultRequestTimeoutMillis, null);
    }

    @Override
    public ClientRequest createClientRequest(AbstractRequest.Builder<?> requestBuilder,
                                             long creationMs,
                                             long timeoutMs,
                                             RequestCompletionCallback callback) {
        return new ClientRequest(channelNode.id(), clientId.get(), nextCorrelationId(),
                timeoutMs, creationMs, requestBuilder, callback);
    }

    @Override
    public int countInflightRequests() {
        return inflightRequestTopic.size();
    }

    @Override
    public void beginShutdown() {
        if (atomicState.compareAndSet(State.ACTIVE, State.CLOSING))
            selector.wakeup();
    }

    @Override
    public void close() throws IOException {
        atomicState.compareAndSet(State.ACTIVE, State.CLOSING);
        if (atomicState.compareAndSet(State.CLOSED, State.CLOSED))
            selector.close();
        else
            log.warn("attempt to close a client that have been already closed; client_id: {}", clientId);
    }

    private long nextCorrelationId() {
        long ans = correlation.getAndIncrement();
        if (correlation.get() == Long.MAX_VALUE)
            correlation.set(0);

        return ans;
    }

    private void ensureActive() {
        if (!active())
            throw new IllegalStateException("Client is not in ACTIVE");
    }


    private void handleCompleteReceives(List<ClientResponse> responses, long nowMs) {
        final Collection<ReceiveNetwork> completedReceives = selector.completedReceives();
        for (ReceiveNetwork receiveNetwork : completedReceives) {
            InflightRequest inflightRequest = inflightRequestTopic.takeNext();
            AbstractResponse response = AbstractResponse.parse(receiveNetwork.payload(), inflightRequest.headerRequest);

            log.debug("receive {} response from {}; header: {}; ({})",
                    inflightRequest.headerRequest.apikey(), inflightRequest.getDestination(),
                    inflightRequest.headerRequest, response);

            responses.add(inflightRequest.toCompleted(response, nowMs));
        }
    }

    /**
     * Although client only need selector to connect to one server, but we can still leverage selector "disconnected"
     * to handle connection disconnection
     */
    private void handleDisconnected(List<ClientResponse> responses, long nowMs) {
        final Map<String, ChannelState> disconnectedEntries = selector.disconnectedChannels();
        for (final Map.Entry<String, ChannelState> entry : disconnectedEntries.entrySet()) {
            final ChannelState channelState = entry.getValue();

            /* log reason of disconnection */
            if (channelState == ChannelState.EXPIRED)
                log.debug("about to disconnect idle connection; client_id: {}; destination: {}",
                        clientId, entry.getKey());
            else
                log.debug("disconnect channel; client_id: {}; destination: {}",
                        clientId, entry.getKey());

            doDisconnect(responses, channelState, nowMs, false);
        }
    }

    private void handleConnection() {
        for (String channelId : selector.connectedChannels()) {
            connectionStateControl.ready();
            log.debug("complete connection to node {}", channelId);
        }
    }

    private void handleTimeoutConnection(List<ClientResponse> responses, long nowMs) {
        if (connectionStateControl.isConnectionSetupTimeout(nowMs)) {
            selector.close(channelNode.id());
            doDisconnect(responses, ChannelState.LOCAL_CLOSE, nowMs, true);

            log.info("disconnect from node {} due to socket connection setup timeout; timeout: {}",
                    channelNode, connectionStateControl.connectionSetupTimeoutMs);
        }
    }

    private void handleTimeoutRequest(List<ClientResponse> responses, long nowMs) {
        if (inflightRequestTopic.anyExpiredRequest(nowMs)) {
            selector.close(channelNode.id());
            doDisconnect(responses, ChannelState.LOCAL_CLOSE, nowMs, true);

            log.info("disconnect from node {} due to request timeout;", channelNode);
        }
    }

    private void doCompleteResponse(List<ClientResponse> responses) {
        for (ClientResponse response : responses) {
            try {
                response.onComplete();
            } catch (Exception e) {
                log.error("uncaught error", e);
            }
        }
    }

    private void doDisconnect(
            List<ClientResponse> responses,
            ChannelState channelState,
            long nowMs,
            boolean hasTimeout
    ) {
        /* handle CHANNEL_STATE */
        connectionStateControl.disconnected(nowMs);
        switch (channelState.value()) {
            case FAILED_AUTHENTICATION -> {
                AuthenticationException authException = channelState.getAuthException();
                connectionStateControl.authFailed(nowMs, authException);
                log.warn("connect to node {} failed; due to {}", channelNode, authException.getMessage());
            }

            case AUTHENTICATED ->
                    log.warn("connect to node {} was interrupted, this maybe any related network issues; remote: {}",
                            channelNode, channelState.getRemoteAddress());

            case NOT_CONNECTED -> log.warn("error while establishing connection; remote: {}",
                    channelState.getRemoteAddress());

        }

        /* handle inflight-request canceling */
        cancelInflightRequest(responses, nowMs, hasTimeout);
    }

    private void cancelInflightRequest(List<ClientResponse> responses, long nowMs, boolean hasTimeout) {
        final Iterable<InflightRequest> inflightRequestIterable = inflightRequestTopic.removeAll();
        for (InflightRequest inflightRequest : inflightRequestIterable) {
            if (log.isDebugEnabled())
                log.debug("cancel in-flight request {}, correlation_id = {}, node = {}; " +
                                "(elapsed time since creation: {}ms, " +
                                "elapsed time since send: {}ms, " +
                                "timeout: {}ms): \n {}",
                        inflightRequest.headerRequest.apikey(),
                        inflightRequest.headerRequest.correlationId(),
                        channelNode,
                        inflightRequest.getElapsedMs(nowMs),
                        inflightRequest.getElapsedMsFromSend(nowMs),
                        inflightRequest.getTimeoutMs(),
                        inflightRequest.request);
            else
                log.info("cancel in-flight request {}, correlation_id = {}, node = {}; " +
                                "(elapsed time since creation: {}ms, " +
                                "elapsed time since send: {}ms, " +
                                "timeout: {}ms)",
                        inflightRequest.headerRequest.apikey(),
                        inflightRequest.headerRequest.correlationId(), channelNode,
                        inflightRequest.getElapsedMs(nowMs),
                        inflightRequest.getElapsedMsFromSend(nowMs),
                        inflightRequest.getTimeoutMs());

            if (responses != null) {
                ClientResponse clientResponse;
                if (hasTimeout) clientResponse = inflightRequest.toTimeout(nowMs);
                else clientResponse = inflightRequest.toDisconnected(nowMs);

                responses.add(clientResponse);
            }
        }
    }

}
