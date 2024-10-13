package org.cobra.networks;

import org.cobra.networks.auth.SecurityProtocol;
import org.cobra.networks.plaintext.PlaintextChannelBuilder;

public class ChannelBuilderFactory {

    public static ChannelBuilder defaultChannelBuilder(SecurityProtocol securityProtocol) {
        return of(securityProtocol);
    }

    private static ChannelBuilder of(SecurityProtocol securityProtocol) {
        return switch (securityProtocol) {
            case PLAINTEXT -> new PlaintextChannelBuilder();
            case SASL_PLAINTEXT -> throw new UnsupportedOperationException("implement me");
            case null -> throw new IllegalArgumentException("Unknown security protocol");
        };
    }
}
