package org.cobra.networks;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public interface Network {
    InetSocketAddress getAddress();

    void bootstrap();

    void shutdown();

    default SocketChannel socketChannel() {
        throw new UnsupportedOperationException("Not supported in default");
    }
}
