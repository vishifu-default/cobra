package org.cobra.core.hashing;

/**
 * Provides a table of [hash_key, pointer_data]
 */
public interface Table {

    /**
     * @return number of entry has been put in table
     */
    int size();

    /**
     * @return table capacity
     */
    int capacity();

    /**
     * Retrieves pointer of data from a hash key
     *
     * @param key hash key
     * @return long pointer if exists, otherwise -1
     */
    long get(int key);

    // todo: returning previous value if not DELETED
    /**
     * Puts a pair [hash_key, pointer_data] into table
     *
     * @param key   hash key
     * @param value pointer data
     */
    void put(int key, long value);

    /**
     * Removes a pointer from table
     *
     * @param key hash key
     * @return deleted data if exists, otherwise -1
     */
    long remove(int key);

    /**
     * Given the key, get the bucket (hash-value) that hold key
     *
     * @param key hash key
     * @return bucket value
     */
    int findBucket(int key);

    void destroy();
}
