package com.ai.utils;

import java.util.*;

public class LossMap {

    public Map<Object, LossVector> loss = new HashMap<>();

    List<String> headers; // optional
    // cluster of maps
    private IndexMap indexMap; // optional
    private NormalizationMap normalizationMap; // optional
    int data_index;

    // Loss Vector = [ Should be Z, mistook: [ X:2, Y: 3], ...]
    public class LossVector {

        CountMap<Object> mistookFor = new CountMap<>();

        public void mistake(Object _for) {
            mistookFor.incr(_for);
        }

        public List<MapItem<Object,Integer>> mistakesMap() {
            return mistookFor.mapping();
        }
        public List<MapItem<Object,Double>> mistakesMap_normalized() {
            return mistookFor.mapping_normalized();
        }

        public void clear() { mistookFor.clear(); }

        @Override
        public String toString() {
            JSONBuilder bldr = new JSONBuilder();
            for (MapItem<Object,Integer> item : mistakesMap()) {
                bldr.insert(true_key(item.key), item.value);
            }
            return bldr.json_nl();
        }
    }

    // Functions :

    // (1) output X mistook Y for Z
    public void addLoss(Object expected, Object got) {
        // If Expected DNE in mapping, insert it
        loss.putIfAbsent(expected, new LossVector());
        // add new loss
        loss.get(expected).mistake(got);
    }

    // (2) What is output X's loss vector?
    public List<MapItem<Object,Integer>> lossFor(Object expected) {
        if (loss.containsKey(expected))
            return loss.get(expected).mistakesMap();
        // DNE - no loss
        return null;
    }


    // (3) What are all the loss vectors?
    public List<MapItem<Object, List<MapItem<Object,Integer>>>> allLoss() {
        List<MapItem<Object, List<MapItem<Object,Integer>>>> allloss = new ArrayList<>();
        for (Object exp : loss.keySet()) {
            allloss.add(new MapItem<>(exp,lossFor(exp)));
        }
        return allloss;
    }


    // Additional

    public void clearLoss(Object _for) {
        if (loss.containsKey(_for))
            loss.get(_for).clear();
    }

    public void clear() {
        for (Object obj: loss.entrySet())
            loss.get(obj).clear();
    }

    // Optionally provide data headers to clean up the output
    public LossMap withHeaders(List<String> headers) {
        this.headers = headers;
        return this;
    }

    // Optionally provide data index map to clean up the output
    public LossMap withaDataMappings(IndexMap indexMap, NormalizationMap normalizationMap, int data_index) {
        this.indexMap = indexMap;
        this.normalizationMap = normalizationMap;
        this.data_index = data_index;
        return this;
    }

    private boolean usingDataMappings() {
        return indexMap != null && normalizationMap != null;
    }

    // Key should be normalized item (ex. 0.111111)
    private String true_key(Object _normalized_key) {
        double normalized_key = (double) _normalized_key;
        int true_key_mapping = (int) normalizationMap.getUnnormalized(data_index,normalized_key);
        String true_key;
        if (usingDataMappings() && indexMap.isMapped(data_index)) {
            true_key = (String) indexMap.mapping(data_index,true_key_mapping);
        } else {
            true_key = true_key_mapping + "";
        }
        return true_key;
    }

    @Override
    public String toString() {
        JSONBuilder bldr = new JSONBuilder();
        bldr.insert("HEADER", headers.get(data_index));
        for (MapItem<Object, List<MapItem<Object,Integer>>> item: allLoss()) {
            bldr.insert(true_key(item.key), item.value);
        }
        return bldr.json_nl();
    }
}
