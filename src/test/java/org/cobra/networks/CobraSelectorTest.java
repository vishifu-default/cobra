package org.cobra.networks;

import org.cobra.commons.Clock;
import org.cobra.commons.errors.CobraException;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.networks.auth.Authenticator;
import org.cobra.networks.auth.SecurityProtocol;
import org.cobra.networks.mocks.EchoServer;
import org.cobra.networks.plaintext.PlaintextChannelBuilder;
import org.cobra.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(240)
public class CobraSelectorTest {
    private static final int BUFFERSIZE = 4 * 1024;
    private static final long CONNECTION_MAX_IDLE_MS = 5_000;
    private static final Logger log = LoggerFactory.getLogger(CobraSelectorTest.class);

    private EchoServer echoServer;
    private Clock clock;
    private CobraSelector selector;
    private ChannelBuilder channelBuilder;

    @BeforeEach
    public void setup() throws IOException {
        this.echoServer = new EchoServer(SecurityProtocol.PLAINTEXT);
        this.echoServer.start();

        this.clock = Clock.system();
        this.channelBuilder = new PlaintextChannelBuilder();
        this.selector = new CobraSelector(CONNECTION_MAX_IDLE_MS, 0, true, channelBuilder, clock, MemoryAlloc.NONE);
    }

    @AfterEach
    public void teardown() throws Exception {
        selector.close();
        echoServer.close();
    }

    /**
     * Validate when server disconnect socket, a client sends up with given nodeId in the `disconnected list`
     */
    @Test
    public void testServerDisconnect() throws Exception {
        final String nodeId = "node-0";
        blockingConnect(nodeId);

        assertEquals("foo", blockingRequest(nodeId, "foo"));
        CobraChannel channel = selector.channel(nodeId);

        // disconnect
        echoServer.closeConnections();

        TestUtils.waitForCondition(() -> {
            try {
                selector.poll(1_000L);
                return selector.disconnectedChannels().containsKey(nodeId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 5_000, 100, () -> "failed to observe disconnected nodes");

        assertNull(channel.selectionKey().attachment());

        blockingConnect(nodeId);
        assertEquals("foo", blockingRequest(nodeId, "foo"));
    }

    /**
     * Sending a request with one already in flight should result in exception
     */
    @Test
    public void testCannotSend_withInProgress() throws IOException {
        final String nodeId = "0";
        blockingConnect(nodeId);
        selector.sent(createSendNetwork(nodeId, "test-this"));

        assertThrows(IllegalStateException.class, () ->
                selector.sent(createSendNetwork(nodeId, "test-another")));

        selector.poll(0);
        assertTrue(selector.disconnectedChannels().containsKey(nodeId), "node was not canceled");
        assertEquals(ChannelState.FAILED_SEND, selector.disconnectedChannels().get(nodeId));
    }

    /**
     * Send a request without perform connection socket
     */
    @Test
    public void testSend_withoutConnected() {
        assertThrows(IllegalStateException.class, () -> selector.sent(createSendNetwork("0", "test")));
    }

    @Test
    public void testConnect_refused() throws IOException {
        final String nodeId = "0";
        final ServerSocket noListeningSocket = new ServerSocket(0);
        final int noListeningPort = noListeningSocket.getLocalPort();

        selector.connect(nodeId, new InetSocketAddress("localhost", noListeningPort), -1, -1);
        while (selector.disconnectedChannels().containsKey(nodeId)) {
            assertEquals(org.cobra.networks.ChannelState.NOT_CONNECTED, selector.disconnectedChannels().get(nodeId));
            selector.poll(0);
        }
        noListeningSocket.close();
    }

    @Test
    public void testNormalOperation() throws IOException {
        final int conns = 5;
        final int reqs = 10;

        // create connections
        final InetSocketAddress socketAddress = new InetSocketAddress("localhost", echoServer.port);
        for (int i = 0; i < conns; i++) {
            connect(Integer.toString(i), socketAddress);
        }

        // send requests, receive responses
        final Map<String, Integer> requests = new HashMap<>();
        int respCount = 0;
        for (int i = 0; i < conns; i++) {
            selector.sent(createSendNetwork(Integer.toString(i), String.format("node-%s", i)));
        }

        while (respCount < conns * reqs) {
            selector.poll(0);

            assertEquals(0, selector.disconnectedChannels().size(), "should have no channel disconnected");

            for (ReceiveNetwork networkRcv : selector.completedReceives()) {
                String[] pieces = new String(networkRcv.payload().array()).split("-");
                assertEquals(2, pieces.length, "receive should have 2 parts");
                assertEquals(networkRcv.source(), pieces[1], "check receive source");
                assertEquals(0, networkRcv.payload().position(), "check receive payload position");

                respCount++;
            }

            for (SendNetwork networkSend : selector.completedSends()) {
                String dest = networkSend.getDestination();

                if (requests.containsKey(dest)) requests.put(dest, requests.get(dest) + 1);
                else requests.put(dest, 1);

                if (requests.get(dest) < reqs)
                    selector.sent(createSendNetwork(dest, String.format("node-%s", dest)));
            }
        }
    }

    @Test
    public void testSend_largeRequest() throws IOException {
        String node = "0";
        blockingConnect(node);
        byte[] largeBytes = new byte[10 * BUFFERSIZE];
        new Random().nextBytes(largeBytes);

        assertEquals(new String(largeBytes), blockingRequest(node, new String(largeBytes)));
    }

    @Test
    public void testSend_largeMessageSequence() throws IOException {
        String node = "node-0";
        int bufSize = 512 * 1024;
        int reqs = 50;
        InetSocketAddress remoteAddress = new InetSocketAddress("localhost", echoServer.port);
        connect(node, remoteAddress);
        String randStr = TestUtils.randString(bufSize);

        loopSendAndReceive(node, randStr, reqs);
    }

    @Test
    public void testSend_empty() throws IOException {
        String node = "0";
        blockingConnect(node);
        assertEquals("", blockingRequest(node, ""));
    }

    @Test
    public void testBlockingConnect_alreadyNodeId() throws IOException {
        String node = "0";
        blockingConnect(node);
        assertThrows(IllegalStateException.class, () -> blockingConnect(node));
    }

    @Test
    public void testClearSendsAndReceive() throws IOException {
        String node = "0";
        InetSocketAddress address = new InetSocketAddress("localhost", echoServer.port);
        connect(node, address);

        selector.sent(createSendNetwork(node, "test-this"));
        boolean sent = false;
        boolean received = false;

        while (!sent || !received) {
            selector.poll(0);
            assertEquals(0, selector.disconnectedChannels().size(), "should have no channel disconnected");

            if (!selector.completedSends().isEmpty()) {
                assertEquals(1, selector.completedSends().size());
                selector.clearCompletedSends();
                assertEquals(0, selector.disconnectedChannels().size(), "should have no channel disconnected");
                sent = true;
            }

            if (!selector.completedReceives().isEmpty()) {
                assertEquals(1, selector.completedReceives().size());
                selector.clearCompletedReceives();
                assertEquals(0, selector.disconnectedChannels().size(), "should have no channel disconnected");
                received = true;
            }
        }
    }

    @Test
    public void testMute() throws IOException {
        final String node1 = "node-1";
        final String node2 = "node-2";
        blockingConnect(node1);
        blockingConnect(node2);

        selector.sent(createSendNetwork(node1, "foo-of-1"));
        selector.sent(createSendNetwork(node2, "foo-of-2"));

        // mute "2"
        selector.mute(node2);

        while (selector.completedReceives().isEmpty()) {
            selector.poll(10);
        }

        assertEquals(1, selector.completedReceives().size());
        assertEquals(node1, selector.completedReceives().iterator().next().source());

        // unmute "1"
        selector.unmute(node2);

        do {
            selector.poll(10);
        } while (selector.completedReceives().isEmpty());

        assertEquals(1, selector.completedReceives().size());
        assertEquals(node2, selector.completedReceives().iterator().next().source());
    }

    @Test
    public void testCloseAllChannels() throws Exception {
        AtomicInteger closedChannels = new AtomicInteger(0);
        ChannelBuilder channelBuilder = new PlaintextChannelBuilder() {
            private int channelId = 0;

            @Override
            protected CobraChannel buildChannel(
                    String id,
                    TransportLayer transportLayer,
                    Supplier<Authenticator> authenticatorSupplier,
                    MemoryAlloc memoryAlloc) throws CobraException {
                return new CobraChannel(id, transportLayer, authenticatorSupplier, memoryAlloc) {
                    private final int index = channelId++;

                    @Override
                    public void close() {
                        closedChannels.incrementAndGet();
                        super.close();
                        if (closedChannels.get() == 0)
                            throw new RuntimeException("should fail");
                    }
                };
            }
        };

        CobraSelector selector = new CobraSelector(CONNECTION_MAX_IDLE_MS, 100, true, channelBuilder,
                clock, MemoryAlloc.NONE);

        selector.connect("0", new InetSocketAddress("localhost", echoServer.port), BUFFERSIZE, BUFFERSIZE);
        selector.connect("1", new InetSocketAddress("localhost", echoServer.port), BUFFERSIZE, BUFFERSIZE);

        selector.close();
        assertEquals(2, closedChannels.get());
    }

    @Test
    public void testRegister_fail() throws Exception {
        final String node = "1";
        final ChannelBuilder channelBuilder = Mockito.mock(ChannelBuilder.class);
        Mockito.when(channelBuilder.build(Mockito.eq(node), Mockito.any(SelectionKey.class),
                        Mockito.any(MemoryAlloc.class)))
                .thenThrow(new RuntimeException("test should fail"));

        final CobraSelector selector = new CobraSelector(CONNECTION_MAX_IDLE_MS, 100, true, channelBuilder, clock,
                MemoryAlloc.NONE);
        final SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        IOException exception = assertThrows(IOException.class, () ->
                selector.register(node, socketChannel));

        assertTrue(exception.getCause().getMessage().contains("test should fail"));
        assertFalse(socketChannel.isOpen(), "socket not close");

        selector.close();
    }

    @Test
    public void testCloseLruChannel() throws Exception {
        final String node = "0";
        selector.connect(node, new InetSocketAddress("localhost", echoServer.port), BUFFERSIZE, BUFFERSIZE);
        TestUtils.waitForCondition(() -> {
            selector.poll(0);
            return selector.channel(node).isConnected();
        }, 30_000, () -> String.format("channel %s is not connected after timeout", node));

        Thread.sleep(CONNECTION_MAX_IDLE_MS + 1_000);
        selector.poll(0);

        assertTrue(selector.disconnectedChannels().containsKey(node), "channel is disconnected");
        assertEquals(ChannelState.EXPIRED, selector.disconnectedChannels().get(node),
                "channel is disconnected");
    }

    @Test
    public void testIdleExpiry_withoutSelectionKey() throws IOException, InterruptedException {
        final String node = "0";
        selector.connect(node, new InetSocketAddress("localhost", echoServer.port), BUFFERSIZE, BUFFERSIZE);

        CobraChannel channel = selector.channel(node);
        channel.selectionKey().interestOps(0);

        Thread.sleep(CONNECTION_MAX_IDLE_MS + 1_000);

        selector.poll(0);
        assertTrue(selector.disconnectedChannels().containsKey(node), "channel is disconnected");
        assertEquals(ChannelState.EXPIRED, selector.disconnectedChannels().get(node));
    }

    @Test
    public void testImmediatelyConnected() {
        try (CobraSelector selector = new ImmediateConnectSelector(CONNECTION_MAX_IDLE_MS, 100, channelBuilder,
                clock, MemoryAlloc.NONE)) {
            verifyImmediatelyConnectionCleaned(selector, true);
            verifyImmediatelyConnectionCleaned(selector, false);
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testClose_modeGraceful() throws Exception {
        int maxRcvAfterClose = 0;
        for (int i = 6; i <= 100 && maxRcvAfterClose < 5; i++) {
            int rcvCount = 0;
            CobraChannel channel = createChannelWithPendingReceives(i);
            TestUtils.waitForCondition(() -> {
                selector.poll(10);
                return !selector.completedReceives().isEmpty();
            }, 5_000, () -> "receiving not completed");

            echoServer.closeConnections();

            while (selector.disconnectedChannels().isEmpty()) {
                selector.poll(10);
                rcvCount += selector.completedReceives().size();
                assertTrue(selector.completedReceives().size() <= 1);
            }

            assertEquals(channel.id(), selector.disconnectedChannels().keySet().iterator().next());
            maxRcvAfterClose = Math.max(maxRcvAfterClose, rcvCount);
        }
        assertTrue(maxRcvAfterClose >= 5);
    }


    private void verifyImmediatelyConnectionCleaned(CobraSelector selector, boolean closeAfterFirstPoll) throws Exception {
        final String node = "0";
        selector.connect(node, new InetSocketAddress("localhost", echoServer.port), BUFFERSIZE, BUFFERSIZE);

        verifyNonEmptyImmediatelyConnectedKeys(selector);

        if (closeAfterFirstPoll) {
            selector.poll(0);
            verifyEmptyImmediatelyConnectedKeys(selector);
        }

        selector.close(node);
    }

    private SendNetwork createSendNetwork(String node, String s) {
        return new SendNetwork(node, SendByteBuffer.toPrefixedLenSend(ByteBuffer.wrap(s.getBytes())));
    }

    private String blockingRequest(String node, String s) throws IOException {
        selector.sent(createSendNetwork(node, s));
        while (true) {
            selector.poll(1_000L);
            for (ReceiveNetwork receive : selector.completedReceives()) {
                if (receive.source().equals(node)) {
                    log.info("receive blocking request {}", receive);
                    return new String(receive.payload().array());
                }
            }
        }
    }

    private void loopSendAndReceive(String node, String request, int end) throws IOException {
        int requests = 0;
        int responses = 0;
        int start = 0;

        selector.sent(createSendNetwork(node, request + "@" + start));
        requests++;

        while (responses < end) {
            selector.poll(0);
            assertEquals(0, selector.disconnectedChannels().size(), "should have no channel disconnected");

            for (ReceiveNetwork rcv : selector.completedReceives()) {
                assertEquals(request + "@" + responses, new String(rcv.payload().array()));
                responses++;
            }

            for (int i = 0; i < selector.completedSends().size() && requests < end; i++, requests++) {
                selector.sent(createSendNetwork(node, request + "@" + requests));
            }
        }

    }

    private void connect(String node, InetSocketAddress address) throws IOException {
        selector.connect(node, address, BUFFERSIZE, BUFFERSIZE);
    }

    private void blockingConnect(String node) throws IOException {
        blockingConnect(node, new InetSocketAddress("localhost", echoServer.port));
    }

    private void blockingConnect(String node, InetSocketAddress address) throws IOException {
        selector.connect(node, address, BUFFERSIZE, BUFFERSIZE);
        TestUtils.waitChannelReady(selector, node);
    }

    private void verifyNonEmptyImmediatelyConnectedKeys(CobraSelector selector) throws Exception {
        Field field = CobraSelector.class.getDeclaredField("immediateKeys");
        field.setAccessible(true);
        Collection<?> immediatelyConnectedKeys = (Collection<?>) field.get(selector);
        assertFalse(immediatelyConnectedKeys.isEmpty(), "immediatelyConnectedKeys should be empty");
    }

    private void verifyEmptyImmediatelyConnectedKeys(CobraSelector selector) throws Exception {
        Field field = CobraSelector.class.getDeclaredField("immediateKeys");
        field.setAccessible(true);
        Collection<?> immediatelyConnectedKeys = (Collection<?>) field.get(selector);
        assertTrue(immediatelyConnectedKeys.isEmpty(), "immediatelyConnectedKeys should be empty");
    }

    private CobraChannel createChannelWithPendingReceives(int pendingReceives) throws IOException {
        final String node = "0";
        blockingConnect(node);
        CobraChannel channel = selector.channel(node);

        sendNoReceive(channel, pendingReceives);

        return channel;
    }

    private void sendNoReceive(CobraChannel channel, int numReqs) throws IOException {
        selector.mute(channel.id());

        for (int i = 0; i < numReqs; i++) {
            selector.sent(createSendNetwork(channel.id(), String.valueOf(i)));
            do {
                selector.poll(10);
            } while (selector.completedSends().isEmpty());
        }

        selector.unmute(channel.id());
    }

    private static final class ImmediateConnectSelector extends CobraSelector {

        public ImmediateConnectSelector(long connectionMaxIdleMillis, long delayFailAuthenticationMs,
                                        ChannelBuilder channelBuilder, Clock clock, MemoryAlloc memoryAlloc) {
            super(connectionMaxIdleMillis, delayFailAuthenticationMs, true,
                    channelBuilder, clock, memoryAlloc);
        }

        @Override
        protected boolean doConnect(SocketChannel socketChannel, InetSocketAddress address) throws IOException {
            socketChannel.configureBlocking(true);
            boolean connected = super.doConnect(socketChannel, address);
            socketChannel.configureBlocking(false);

            return connected;
        }
    }
}
