package com.ai.learn.general;

import java.util.ArrayList;
import java.util.List;

public class Attribute<G, V> {
    G uid;
    List<V> domain;
    int inputVectorIndex;
    public Attribute(G uid) {
        this.uid = uid;
        this.domain = new ArrayList<>();
        this.inputVectorIndex = 0;
    }

    public Attribute(G uid, List<V> domain, int inputVectorIndex) {
        this.uid = uid;
        this.domain = domain;
        this.inputVectorIndex = inputVectorIndex;
    }

    public G getUid() { return uid; }
    public List<V> getDomain() { return domain; }
    public int getIndex() { return inputVectorIndex; }
    public void setIndex(int to) { inputVectorIndex = to; }

    public void addToDomain(V v) { domain.add(v); }

    @Override
    public String toString() {
        return uid.toString();
    }

    public static <G,V> Attribute findInList(G uid, List<Attribute<G,V>> attributes ) {
        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).getUid().equals(uid)) return attributes.get(i);
        }
        return null;
    }
}
