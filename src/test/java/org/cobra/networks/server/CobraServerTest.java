package org.cobra.networks.server;

import org.cobra.commons.Clock;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.networks.SocketNode;
import org.cobra.networks.client.ClientRequest;
import org.cobra.networks.client.CobraClient;
import org.cobra.networks.client.CobraClientFactory;
import org.cobra.networks.client.DefaultClientConfigs;
import org.cobra.networks.protocol.Apikey;
import org.cobra.networks.requests.AbstractResponse;
import org.cobra.networks.requests.RequestCompletionCallback;
import org.cobra.networks.requests.sample.SampleRequest;
import org.cobra.networks.requests.sample.SampleResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CobraServerTest {

    private static final Logger log = LoggerFactory.getLogger(CobraServerTest.class);
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
        final CobraServer server = new CobraServer(
                Clock.system(),
                MemoryAlloc.NONE,
                DefaultServerConfigs.CONFIG_DEF
        );
        server.bootstrap();

        final SocketNode socketNode = new SocketNode("localhost", 9002);
        final CobraClient client = CobraClientFactory.createClient(clock, MemoryAlloc.NONE, socketNode,
                DefaultClientConfigs.CONFIG_DEF);

        client.ready(clock.milliseconds());

        SampleRequest.Builder builder = new SampleRequest.Builder("foo");
        ClientRequest clientRequest = client.createClientRequest(builder, clock.milliseconds(),
                new SampleRequestCompletionCallback());

        client.send(clientRequest, clock.milliseconds());
    }

    private static void assertSampleResponse(SampleResponse response) {
        assertEquals(Apikey.SAMPLE_REQUEST.id(), response.apikey().id());
        assertEquals("foo", response.data().getText());
    }

    private static final class SampleRequestCompletionCallback implements RequestCompletionCallback {

        @Override
        public void consume(AbstractResponse response) {
            invokeConsume((SampleResponse) response);
        }

        private void invokeConsume(SampleResponse response) {
            log.info("consume response {}", response);
            assertSampleResponse(response);
        }
    }
}
