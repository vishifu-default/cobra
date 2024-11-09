package org.cobra.core.hashing;

public interface Maps {

    /**
     * Puts a key-value into index
     *
     * @return hash index of key
     */
    int put(String key, byte[] value);

    /**
     * Gets value of record by key
     */
    byte[] get(String key);

    /**
     * Removes a key-value record from index
     */
    byte[] remove(String key);

    /**
     * Test as if key is existed in index
     */
    boolean contains(String key);
}
