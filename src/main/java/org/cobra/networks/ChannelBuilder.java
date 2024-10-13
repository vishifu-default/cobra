package org.cobra.networks;

import org.cobra.commons.pools.MemoryAlloc;

import java.nio.channels.SelectionKey;

public interface ChannelBuilder extends AutoCloseable {

    /**
     * Build provided arguments into channel
     *
     * @param id           node id
     * @param selectionKey registered selection key
     * @param memoryAlloc  memory pool to allocate ByteBuffer
     * @return a new {@link CobraChannel}
     */
    CobraChannel build(final String id, final SelectionKey selectionKey, final MemoryAlloc memoryAlloc);
}
