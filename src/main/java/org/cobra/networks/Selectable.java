package org.cobra.networks;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

/**
 * Present operation for async and multi-channel network I/O. Normally, each poll() we try to do single I/O on each
 * channel
 *
 * <ul>
 * <li> Complete any I/O that have finished handshaking, if channel return true for "finishConnect" skip it </li>
 * <li> If channel is not ready, do "prepare" for it. From this point, any further I/O should check ready channel </li>
 * <li> Bring channel to READY state </li>
 * <li> Attempt to read receive from then channel </li>
 * <li> Attempt to write send to the channel </li>
 * <li> Cancel any defunct socket </li>
 * </ul>
 */
public interface Selectable extends Closeable {

    /**
     * Establish a socket connection to given address
     *
     * @param channelId      node id for this connection
     * @param address        remote address
     * @param sendBufferSize send buffer size for socket
     * @param rcvBufferSize  receive buffer size for socket
     * @return connection id
     */
    SocketChannel connect(String channelId, InetSocketAddress address, int sendBufferSize, int rcvBufferSize) throws IOException;

    /**
     * Wakeup this applicable selector, interrupt the nioSelector if it's blocked on I/O
     */
    void wakeup();

    /**
     * Do I/O (reads, writes, connection establishing, ...) asynchronously
     *
     * @param timeout time to block thread in millis
     */
    void poll(long timeout) throws IOException;

    /**
     * Queue up given network payload for sending
     *
     * @param send a network send
     */
    void sent(SendNetwork send);

    /**
     * @return list of completed sends on the last poll invocation
     */
    List<SendNetwork> completedSends();

    /**
     * @return list of completed receives on the last poll invocation
     */
    List<ReceiveNetwork> completedReceives();

    /**
     * @return list of all connected nodes (that have established connection completely)
     */
    List<String> connectedChannels();

    /**
     * @return a map of node id and channel state of disconnected nodes.
     */
    Map<String, ChannelState> disconnectedChannels();

    /**
     * Test if node is ready
     *
     * @param channelId connection id
     * @return true if node is ready for I/O, otherwise false
     */
    boolean isReadyChannel(String channelId);

    /**
     * Mute channel with given id
     *
     * @param channelId channel ID
     */
    void mute(String channelId);

    /**
     * Unmute channel with given id
     *
     * @param channelId channel ID
     */
    void unmute(String channelId);

    /**
     * Close the socket connection by given channel id
     *
     * @param channelId connection id
     */
    void close(String channelId);

}
