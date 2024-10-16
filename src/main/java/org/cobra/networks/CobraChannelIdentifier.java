package org.cobra.networks;

import java.net.InetSocketAddress;
import java.net.Socket;

public class CobraChannelIdentifier {

    /**
     * Generate an ID for channel
     */
    public static String identifier(CobraChannel channel) {
        Socket socket = channel.socket();
        return identifier(socket);
    }

    /**
     * Generate an ID for socket by using socket address
     */
    public static String identifier(Socket socket) {
        return identifier(new InetSocketAddress(socket.getInetAddress(), socket.getPort()));
    }

    public static String identifier(InetSocketAddress inetAddress) {
        String ipAddress = inetAddress.getAddress().getHostAddress();
        int port = inetAddress.getPort();

        return ipAddress + ":" + port;
    }
}
