package org.cobra.core.hashing;

import org.jetbrains.annotations.Nullable;

public interface EntryMap {

    /**
     * Puts a key-value into index
     *
     * @return hash index of key
     */
    int put(String key, byte[] value);

    /**
     * Gets value of record by key
     */
    byte @Nullable [] get(String key);

    /**
     * Removes a key-value record from index
     */
    byte @Nullable [] remove(String key);

    /**
     * Test as if key is existed in index
     */
    boolean exist(String key);
}
