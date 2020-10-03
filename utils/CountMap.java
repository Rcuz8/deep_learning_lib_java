package com.ai.utils;

/* Count map implementation */

import java.util.*;

public class CountMap<K> {

    public HashMap<K, Integer> map;

    private K max;
    private int maxn = 0;
    private int total_n = 0;

    public CountMap() {
        map = new HashMap<>();
    }

    public void incr(K key) {
        if (map.get(key) != null)
            map.put(key, map.get(key) + 1);
        else map.put(key, 1);

        if (map.get(key) > maxn) {
            max = key;
            maxn = map.get(key);
        }
        total_n++;
    }

    public K getMax() {
        return max;
    }

    public int getMaxCount() {
        return maxn;
    }

    public void clear() {
        map.clear();
        max = null;
        maxn = 0;
        total_n = 0;
    }

    public List<MapItem<K,Integer>> mapping() {
        List<MapItem<K,Integer>> mapping = new ArrayList<>();
        for (Map.Entry<K,Integer> entry : map.entrySet())
            mapping.add(new MapItem<>(entry.getKey(),entry.getValue()));
        return mapping;
    }

    public List<MapItem<K,Double>> mapping_normalized() {
        List<MapItem<K,Double>> mapping = new ArrayList<>();
        for (Map.Entry<K,Integer> entry : map.entrySet())
            mapping.add(new MapItem<>(entry.getKey(),((double) entry.getValue()) / total_n));
        return mapping;
    }


}
