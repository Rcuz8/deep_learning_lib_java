package com.ai.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class IndexMap {
    private HashMap<Double, LinkedList<Object>> map;  // maps attibute indices to a list of their values

    public IndexMap() { map = new HashMap<>(); }
    public double put(double attributeIndex, Object data ) {
        List<Object> list = map.get(attributeIndex);
        if (list != null) list.add(data);
        else {
            LinkedList<Object> l = new LinkedList<>();
            l.add(data);
            map.put(attributeIndex, l);
        }
        return map.get(attributeIndex).size();
    }
    public double indexFor(double attributeIndex, Object data) {
        if (map.get(attributeIndex) == null) return -1;
        return map.get(attributeIndex).indexOf(data);
    }
    // If you want to invert the mapping (get the string by mapped indices
    public Object mapping(double attributeIndex, int key_index) {
        if (map.get(attributeIndex) == null) return null;
        return  map.get(attributeIndex).get(key_index);
    }

    public boolean isMapped(double attributeIndex) {
        return map.containsKey(attributeIndex);
    }
}