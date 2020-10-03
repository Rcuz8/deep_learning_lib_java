package com.ai.utils;

public class MapItem<K, V> {
    public K key;
    public V value;

    public MapItem(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        JSONBuilder bldr = new JSONBuilder();
        bldr.insert(key.toString(),value.toString());
        return bldr.json();
    }
}
