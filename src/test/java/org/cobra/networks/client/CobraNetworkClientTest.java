package org.cobra.networks.client;

import org.cobra.commons.Clock;
import org.cobra.networks.ReceiveNetwork;
import org.cobra.networks.SocketNode;
import org.cobra.networks.mocks.ClusterSample;
import org.cobra.networks.mocks.MockSelector;
import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.requests.HeaderResponse;
import org.cobra.networks.requests.SampleRequest;
import org.cobra.networks.requests.SampleResponse;
import org.cobra.networks.requests.SampleResponseMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CobraNetworkClientTest {


    private final Clock m_clock = Clock.system();
    private final MockSelector m_selector = new MockSelector(m_clock);
    private final SocketNode m_node = ClusterSample.singletonCluster().getFirst();

    private final int defaultRequestTimeoutMs = 1_000 * 60;
    private final long reconnectBackoffMs = 10 * 1_000;
    private final long reconnectBackoffMaxMs = 10 * 10_000;
    private final long connectionSetupTimeoutMs = 5 * 1_000;
    private final long connectionSetupTimeoutMaxMs = 127 * 10_000;

    private final CobraNetworkClient m_clientNetwork = createNetworkClient(reconnectBackoffMaxMs);
    private final CobraNetworkClient m_staticClientNetwork = createNetworkClientWithStaticNode();

    @BeforeEach
    public void setup() {
        this.m_selector.reset();
    }

    @Test
    public void testSend_unreadyNode() {
        long nowMs = m_clock.milliseconds();
        ClientRequest request = m_clientNetwork.createClientRequest(
                new SampleRequest.Builder("test"),
                m_clock.milliseconds());

        assertThrows(IllegalStateException.class, () -> m_clientNetwork.send(request, nowMs));
    }

    @Test
    public void testSimpleRequestResponse() {
        assertRequestResponse(m_clientNetwork);
    }

    @Test
    public void testSimpleRequestResponse_withStaticNode() {
        assertRequestResponse(m_staticClientNetwork);
    }

//    @Test
//    public void testDnsLookupFail() {
//        assertFalse(m_clientNetwork.ready(this.m_clock.milliseconds()));
//    }

    private void assertRequestResponse(CobraNetworkClient client) {
        awaitReady(client);
        SampleRequest.Builder builder = new SampleRequest.Builder("foo");
        ClientRequest request = client.createClientRequest(builder,
                m_clock.milliseconds());

        client.send(request, m_clock.milliseconds());
        client.poll(1);

         assertEquals(1, client.countInflightRequests());

        SampleResponseMessage sampleResponseMessage = new SampleResponseMessage("foo");
        SampleResponse sampleResponse = new SampleResponse(sampleResponseMessage);
        ByteBuffer buffer = sampleResponse.toBufferIncludeHeader(new HeaderResponse(Apikey.SAMPLE_REQUEST.id(),
                request.getCorrelationId()));
        m_selector.doCompleteReceive(new ReceiveNetwork(m_node.source(), buffer));

        List<ClientResponse> responses = client.poll(1);
        assertEquals(1, responses.size());
    }

    private void awaitReady(NetworkClient client) {
        while (!client.ready(m_clock.milliseconds()))
            client.poll(1L);

        m_selector.clear();
    }

    private CobraNetworkClient createNetworkClient(long reconnectBackoffMaxMs) {
        return new CobraNetworkClient(
                m_selector,
                m_clock,
                m_node,
                "mock-client",
                64 * 1024,
                64 * 1024,
                Integer.MAX_VALUE,
                reconnectBackoffMs,
                reconnectBackoffMaxMs,
                connectionSetupTimeoutMs,
                connectionSetupTimeoutMaxMs,
                defaultRequestTimeoutMs
        );
    }

    private CobraNetworkClient createNetworkClientWithStaticNode() {
        return new CobraNetworkClient(
                m_selector,
                m_clock,
                m_node,
                "mock-static-client",
                64 * 1024,
                64 * 1024,
                Integer.MAX_VALUE,
                0,
                0,
                connectionSetupTimeoutMs,
                connectionSetupTimeoutMaxMs,
                defaultRequestTimeoutMs
        );
    }
}
