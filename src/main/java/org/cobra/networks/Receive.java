package org.cobra.networks;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ScatteringByteChannel;

public interface Receive extends Closeable {

    /**
     * @return the source id from which data is read
     */
    String source();

    /**
     * test if reading progress is completed
     *
     * @return true if reading is done, otherwise false
     */
    boolean isCompleted();

    /**
     * read bytes into a receive from the given channel
     *
     * @param channel channel to read from
     * @return number of bytes read from channel
     */
    long readFrom(ScatteringByteChannel channel) throws IOException;

    /**
     * whether doing known yet the size of buffer need for fulfill the receive
     *
     * @return true if know memory size
     */
    boolean requiredMemoryKnown();

    /**
     * @return true if underlying memory has been allocated
     */
    boolean isMemoryAllocated();
}
