package org.cobra.commons.utils;

import java.util.Objects;

public class KVPair<K, V> {

    private final K key;
    private final V value;

    private KVPair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> KVPair<K, V> of(K key, V value) {
        return new KVPair<>(key, value);
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        KVPair<?, ?> kvPair = (KVPair<?, ?>) o;
        return Objects.equals(key, kvPair.key) && Objects.equals(value, kvPair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "KVPair(key=%s, value=%s)".formatted(key, value);
    }
}
