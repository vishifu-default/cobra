package org.cobra.networks;

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
        return socket.getRemoteSocketAddress().toString();
    }
}
