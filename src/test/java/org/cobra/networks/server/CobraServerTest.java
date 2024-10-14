package org.cobra.networks.server;

import org.cobra.commons.Clock;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.networks.ChannelNode;
import org.cobra.networks.client.*;
import org.cobra.networks.requests.SampleRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CobraServerTest {

    private final ChannelNode channelNode = new ChannelNode("localhost", 9002);
    private final Clock clock = Clock.system();

    @Test
    public void initServer() {
        CobraServer server = new CobraServer(clock, MemoryAlloc.NONE, DefaultServerConfigs.CONFIG_DEF);
        assertNotNull(server);

        server.bootstrap();
        assertFalse(server.isShutdown());
    }

    @Test
    public void sendAndReceive() {
        /* bootstrap server */
        CobraServer server = new CobraServer(
                Clock.system(),
                MemoryAlloc.NONE,
                DefaultServerConfigs.CONFIG_DEF
        );
        server.bootstrap();

        /* init client */
        CobraNetworkClient clientNetwork = CobraNetworkClientFactory.createClientNetwork(
                channelNode,
                DefaultClientConfigs.CONFIG_DEF,
                Clock.system(),
                MemoryAlloc.NONE,
                500);

        awaitReady(clientNetwork);

        assertRequestResponse(clientNetwork);
    }

    private void assertRequestResponse(NetworkClient client) {
        awaitReady(client);
        SampleRequest.Builder builder = new SampleRequest.Builder("test");
        ClientRequest request = client.createClientRequest(builder, clock.milliseconds());

        client.send(request, clock.milliseconds());
        client.poll(1);

        assertEquals(1, client.countInflightRequests(), "send 1 request");

        boolean shouldBreak = false;

        while (!shouldBreak) {
            List<ClientResponse> responses = client.poll(1);
            if (!responses.isEmpty())
                shouldBreak = true;
        }
    }

    private void awaitReady(NetworkClient client) {
        while (!client.ready(clock.milliseconds()))
            client.poll(1L);
    }
}
