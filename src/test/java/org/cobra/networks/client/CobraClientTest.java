package org.cobra.networks.client;

import org.cobra.commons.Clock;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.networks.SocketNode;
import org.cobra.networks.auth.SecurityProtocol;
import org.cobra.networks.mocks.EchoServer;
import org.cobra.networks.requests.AbstractResponse;
import org.cobra.networks.requests.RequestCompletionCallback;
import org.cobra.networks.requests.SampleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CobraClientTest {

    private static final Logger log = LoggerFactory.getLogger(CobraClientTest.class);
    private final Clock m_clock = Clock.system();
    private SocketNode m_socketNode;
    private EchoServer echoServer;

    private final int defaultRequestTimeoutMs = 1_000;
    private final int reconnectBackoffMs = 10 * 1_000;
    private final int reconnectBackoffMaxMs = 10 * 10_000;
    private final int connectionSetupTimeoutMs = 5 * 1_000;
    private final int connectionSetupTimeoutMaxMs = 127 * 10_000;

    private Client client;

    @BeforeEach
    public void setupOnce() throws IOException {
        echoServer = new EchoServer(SecurityProtocol.PLAINTEXT);
        echoServer.start();
        m_socketNode = new SocketNode("localhost", echoServer.port);

        client = new CobraClient(m_clock, MemoryAlloc.NONE, m_socketNode,
                64 * 1024, 62 * 1024, reconnectBackoffMs, reconnectBackoffMaxMs,
                connectionSetupTimeoutMs, connectionSetupTimeoutMaxMs, defaultRequestTimeoutMs);
    }

    @Test
    public void testSendSample() {
        client.ready(m_clock.milliseconds());
        assertSendSampleRequest(client);
    }

    private void assertSendSampleRequest(Client client) {
        awaitReady(client);

        SampleRequest.Builder builder = new SampleRequest.Builder("foo-bar");
        ClientRequest clientRequest = client.createClientRequest(builder, m_clock.milliseconds(),
                new SampleRequestCompletionCallback());
        client.send(clientRequest, m_clock.milliseconds());
    }

    private void awaitReady(Client client) {
        boolean ready;
        do {
            ready = client.ready(m_clock.milliseconds());
        } while (!ready);
    }

    private static final class SampleRequestCompletionCallback implements RequestCompletionCallback {

        @Override
        public void consume(AbstractResponse response) {
            log.info("consume response {}", response);
        }
    }
}
