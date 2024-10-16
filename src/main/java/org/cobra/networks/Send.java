package org.cobra.networks;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.GatheringByteChannel;

/**
 * Modeling the in-progress sending data similar send of socket
 */
public interface Send extends Closeable {

    /**
     * Test if send cycle is completed
     *
     * @return true if this send is completed, otherwise false
     */
    boolean isCompleted();

    /**
     * Write bytes from this send package to the given channel
     *
     * @param channel destination gathering bytes channel
     * @return number of written bytes
     */
    long writeTo(GatheringByteChannel channel) throws IOException;

    /**
     * @return size of send in bytes
     */
    long size();

}
