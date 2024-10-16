package org.cobra.networks;

import org.cobra.commons.errors.AuthenticationException;

import java.io.IOException;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface TransportLayer extends TransferableChannel, ScatteringByteChannel {

    /**
     * @return true if channel has handshake and authentication done
     */
    boolean ready();

    /**
     * @return true if channel ha completely done process of connections
     */
    boolean isFinishConnection() throws IOException;

    /**
     * @return true if channel has connection
     */
    boolean isConnected();

    /**
     * @return the underlying selection key
     */
    SocketChannel channel();

    /**
     * @return the underlying selection key
     */
    SelectionKey selectionKey();

    /**
     * in term of SSL connection, perform SSL handshake
     */
    void handshake() throws AuthenticationException, IOException;

    /**
     * Disconnect socket channel
     */
    void disconnect();

    /**
     * Register interest ops for channel
     *
     * @param interestOps selection ops
     */
    void addInterestOps(int interestOps);

    /**
     * Remove interest ops for channel
     *
     * @param interestOps selection ops
     */
    void removeInterestOps(int interestOps);

    /**
     * @return true if channel has bytes to be read in any intermediate buffer, without read data from channel.
     */
    boolean hasBuffer();
}
