package org.cobra.core.hashing;

public interface Indexable<V> {

    /**
     * Puts a key-value into index
     *
     * @return hash index of key
     */
    int put(String key, V value);

    /**
     * Gets value of record by key
     */
    V get(String key);

    /**
     * Removes a key-value record from index
     */
    V remove(String key);

    /**
     * Test as if key is existed in index
     */
    boolean contains(String key);
}
