package org.cobra.core.bytes;

public interface BytesStore extends RandomBytes {

    /**
     * @return the current capacity of memory block
     */
    long size();

    /**
     * Resizes current memory block to given size
     */
    void attemptResizing(long toSize);

    /**
     * Clear all data and destroy it, means that we can not use it anymore
     */
    void destroy();
}
