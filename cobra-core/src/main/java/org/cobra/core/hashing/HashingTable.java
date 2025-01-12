package org.cobra.core.hashing;

import org.cobra.commons.errors.CobraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HashingTable implements Table {

    private static final Logger log = LoggerFactory.getLogger(HashingTable.class);

    private static final float DEFAULT_LOAD_FACTOR = .7f;
    private static final int DEFAULT_TABLE_CAPACITY = 128;
    private static final int DEFAULT_LOCK_TIMEOUT_SECS = 60;
    private static final int UNDEFINED_HASH = -1;
    private static final int UNDEFINED_VALUE = -1; // by design, this should store value of memory pointer
    private static final int PRIME_MOD_BUCKET = 29;

    private static final String ACQUIRE_LOCK_TIMEOUT = "Timeout for acquire read-lock";

    private static final HashEntry DELETED_ENTRY = new HashEntry(-1, -1);

    private final AtomicInteger size = new AtomicInteger(0);
    private final float loadFactor;
    private HashEntry[] table;

    private final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();
    private final Sensor sensor = new Sensor();

    public HashingTable() {
        this(DEFAULT_TABLE_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public HashingTable(int initCapacity) {
        this(initCapacity, DEFAULT_LOAD_FACTOR);
    }

    public HashingTable(int initCapacity, float loadFactor) {
        this.loadFactor = loadFactor;

        this.table = new HashEntry[initCapacity];
    }

    /* test */
    String dumpSensor() {
        return this.sensor.toString();
    }

    @Override
    public int size() {
        return this.size.get();
    }

    @Override
    public int capacity() {
        return this.table.length;
    }

    @Override
    public long get(int key) {
        final int retHashValue = findBucket(key);
        if (retHashValue == UNDEFINED_HASH)
            return UNDEFINED_VALUE;

        return entry(retHashValue).value;
    }

    @Override
    public void put(int key, long value) {
        this.reentrantLock.writeLock().lock();

        try {
            put0(key, value);
            if (size() > threshold()) {
                grow();
            }
        } finally {
            this.reentrantLock.writeLock().unlock();
        }
    }

    @Override
    public long remove(int key) {
        this.reentrantLock.writeLock().lock();
        try {
            int hash = hash(key);
            int firstHash = UNDEFINED_HASH;
            int let = 0;

            while (availSlot(hash, firstHash, key)) {
                if (firstHash == UNDEFINED_HASH) {
                    firstHash = hash;
                }

                hash = probing(key, ++let);
            }

            if (hash != firstHash && entry(hash) != null) {
                long result = entry(hash).value;
                this.table[hash] = DELETED_ENTRY;
                this.size.getAndDecrement();
                return result;
            }

            return UNDEFINED_VALUE; // not actual remove anything
        } finally {
            this.reentrantLock.writeLock().unlock();
        }
    }

    @Override
    public int findBucket(int key) {
        try {
            if (this.reentrantLock.readLock().tryLock(DEFAULT_LOCK_TIMEOUT_SECS, TimeUnit.SECONDS)) {
                int hash = hash(key);
                int firstHash = UNDEFINED_HASH;
                int let = 0;

                while (availSlot(hash, firstHash, key)) {
                    if (firstHash == UNDEFINED_HASH) {
                        firstHash = hash;
                    }

                    hash = probing(key, ++let);
                }

                if (entry(hash) == null || hash == firstHash) {
                    sensor.incMisses();
                    return UNDEFINED_HASH;
                }

                sensor.incHits();

                return hash;

            } else {
                throw new CobraException(ACQUIRE_LOCK_TIMEOUT);
            }
        } catch (Throwable cause) {
            log.error("Interrupt while acquire read-lock", cause);
            throw new CobraException(cause.getMessage(), cause);
        } finally {
            this.reentrantLock.readLock().unlock();
        }
    }

    @Override
    public void destroy() {
        this.reentrantLock.writeLock().lock();
        this.table = null;
        this.reentrantLock.writeLock().unlock();
    }

    @Override
    public String toString() {
        return "HashingTable(loadFactor=%f, capacity=%d, size=%s)"
                .formatted(this.loadFactor, capacity(), size);
    }

    private void put0(int key, long value) {
        int hash = hash(key);
        int firstHash = UNDEFINED_HASH;
        int hashOfDeleted = UNDEFINED_HASH;
        int let = 0;
        while (hash != firstHash
                && (entry(hash) == DELETED_ENTRY || entry(hash) != null && entry(hash).key != key)) {
            if (firstHash == UNDEFINED_HASH)
                firstHash = hash;

            if (entry(hash) == DELETED_ENTRY)
                hashOfDeleted = hash;

            hash = probing(key, ++let);
        }

        if ((entry(hash) == null || (hash == firstHash)) && hashOfDeleted != UNDEFINED_HASH) {
            this.table[hashOfDeleted] = new HashEntry(key, value);
            this.size.getAndIncrement();
            return;
        }

        if (hash != firstHash) {
            if (entry(hash) != DELETED_ENTRY && (entry(hash) != null && entry(hash).key == key)) {
                entry(hash).value = value;
            } else {
                this.table[hash] = new HashEntry(key, value);
                this.size.getAndIncrement();
            }
        }
    }

    private void grow() {
        int toCapacity = capacity() * 2;
        if (toCapacity <= 0)
            throw new IllegalStateException("Could not grow more; exceed max capacity and overflow");

        final HashEntry[] oldTable = this.table;
        this.table = new HashEntry[toCapacity];
        this.size.set(0);

        final long startWatch = System.currentTimeMillis();

        log.debug("start growing table {}", this);

        for (final HashEntry entry : oldTable) {
            if (entry == null || entry == DELETED_ENTRY)
                continue;

            put0(entry.key, entry.value);
        }

        final long elapsedMs = System.currentTimeMillis() - startWatch;
        log.debug("{} grow done; elapsed_time = {}ms", this, elapsedMs);
    }

    private boolean availSlot(int hash, int firstHash, int key) {
        return (hash != firstHash) &&
                (entry(hash) == DELETED_ENTRY || entry(hash) != null && entry(hash).key != key);
    }

    private HashEntry entry(int hash) {
        return this.table[hash];
    }

    private int threshold() {
        return (int) (capacity() * this.loadFactor);
    }

    private int hash(int key) {
        return (key % capacity());
    }

    private int secondHash(int key) {
        return (PRIME_MOD_BUCKET - (key % PRIME_MOD_BUCKET));
    }

    private int doubleHash(int key, int j) {
        return (hash(key) + j * secondHash(key)) % capacity();
    }

    private int probing(int key, int j) {
        this.sensor.incCollisions();
        return doubleHash(key, j);
    }

    private static class HashEntry {
        int key;
        long value;

        HashEntry(int key, long value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "HashEntry{key=%d, value=%d}".formatted(key, value);
        }
    }

    private static final class Sensor {
        private final AtomicInteger hits = new AtomicInteger();
        private final AtomicInteger misses = new AtomicInteger();
        private final AtomicInteger collisions = new AtomicInteger();

        void incCollisions() {
            this.collisions.getAndIncrement();
        }

        void incHits() {
            this.hits.getAndIncrement();
        }

        void incMisses() {
            this.misses.getAndIncrement();
        }

        @Override
        public String toString() {
            return "Sensor(collisions=%s, hits=%s, misses=%s)".formatted(collisions, hits, misses);
        }
    }
}
