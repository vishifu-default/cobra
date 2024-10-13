package org.cobra.networks;

import org.cobra.networks.auth.SecurityProtocol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ChannelBuilderFactoryTest {

    @Test
    public void createDefaultChannelBuilder_shouldPlaintextProtocol() {
        ChannelBuilder channelBuilder = ChannelBuilderFactory.defaultChannelBuilder(SecurityProtocol.PLAINTEXT);
        assertNotNull(channelBuilder);
    }
}
