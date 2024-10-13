package org.cobra.networks.client;

import org.cobra.commons.Clock;
import org.cobra.commons.pools.MemoryAlloc;
import org.cobra.networks.SocketNode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CobraNetworkClientFactoryTest {

    @Test
    public void createClientNetwork_simple() {
        CobraNetworkClient clientNetwork = CobraNetworkClientFactory.createClientNetwork(
                UUID.randomUUID().toString(),
                DefaultClientConfigs.CONFIG_DEF,
                Clock.system(),
                MemoryAlloc.NONE,
                new SocketNode(1, 9002, "localhost"),
                1000,
                0L
        );

        assertTrue(clientNetwork.active());
    }

}
