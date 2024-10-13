package org.cobra.networks;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;

public interface TransferableChannel extends GatheringByteChannel {

    /**
     * Test if channel has pending writes operation
     *
     * @return true if in progress of a send writing
     */
    boolean hasPendingWrites();

    /**
     * Transfer data from {@link FileChannel} into this channel.
     *
     * @param fc       file channel
     * @param position start to copy position
     * @param count    number of byte to transfer
     * @return number of bytes has been transfer
     */
    long transferFrom(FileChannel fc, long position, long count) throws IOException;
}
