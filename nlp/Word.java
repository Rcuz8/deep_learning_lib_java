package com.ai.nlp;

import com.ai.utils.CollectionUtils;
import com.ai.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Word {

    public String word;
    List<String> synonyms = new ArrayList<>();

    public Word(String word) {
        this.word = word.toLowerCase();
    }

    public void addSynonym(String synonym) {
        synonym = synonym.toLowerCase();
        if (!synonym.equals(word))
            synonyms.add(synonym);
    }
    public boolean hasSynonym(String synonym) {
        return synonyms.contains(synonym.toLowerCase());
    }

    public boolean means(String word) {
        return this.word.equalsIgnoreCase(word) || synonyms.contains(word.toLowerCase());
    }

    // word + synonyms
    public List<String> group() {
        return CollectionUtils.combine(Arrays.asList(synonyms, Arrays.asList(word)));
    }

    public static Word fromSynonyms(List<String> synonyms) {
        Word w = new Word(synonyms.remove(0).toLowerCase());
        while (!synonyms.isEmpty()) {
            w.addSynonym(synonyms.remove(0).toLowerCase());
        }
        return w;
    }

    public double maxNearness(String to) {
        double max = 0;
        for (String s : group()) {
            double common = StringUtils.nearness(s, to);
            if (common > max) {
                max = common;
            }
        }
        return max;
    }

    public double avgNearness(String to) {
        double total = 0;
        List<String> group = group();
        for (String s : group) {
            total += StringUtils.nearness(s, to);
        }
        return total / group.size();
    }

    public String stringify() {
        String str = word + ",";
        for (String synonym : synonyms) {
            str += synonym + ",";
        }
        str = str.substring(0,str.length()-1);
        return str;
    }
}
