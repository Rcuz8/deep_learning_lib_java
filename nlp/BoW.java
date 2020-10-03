package com.ai.nlp;

import com.ai.input.FileInputReader;
import com.ai.input.InputReader;
import com.ai.utils.CollectionUtils;
import com.ai.utils.console;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// bag of words
public class BoW {

    private Map<String, Word> wordMap = new HashMap<>();
    private List<String> vocab = new ArrayList<>();
    String stored_bow_filename = "src/main/java/com/ai/nlp/test/data/data-synonyms.txt";
    Map<String, String> unweightMap = new HashMap<>(); // lessen the weights of certain words (by shortening them)
    Map<String, String> reweightMap = new HashMap<>();

    public void addWord(String word) {
        word = word.toLowerCase();
        Word w = wordFor(word);
        if (w == null) {
            wordMap.put(word, new Word(word));
            vocab.add(word);
        }
    }



    private void addWord(Word w) {
        wordMap.putIfAbsent(w.word, w);
        vocab.add(w.word);
    }

    // Leaving un-implemented for now bc unnecessary. BUT this is core for creating bag from file input
    // Idea: (input below)
    // table chair seat
    // soccer game table    *** table here, so should merge lists
    private void recursiveAddMerge(Word w) {
        // if any words in w's group match another word, merge w's group into the other word
    }

    // CAREFUL WITH USAGE, LIKELY YOU NEED wordFor()
    private Word mapget(String word) {
        return wordMap.get(word.toLowerCase());
    }

    // *SMART GET* gets the word. If DNE, adds to nearest, then returns nearest
    private Word wordget(String word) {
        word = word.toLowerCase();
        Word w = wordFor(word);
        if (w == null) {
            addToNearest(word);
            w = wordFor(word);
        }
        return w;
    }

    // *SMART GET* gets the word's *base word*. If DNE, adds to nearest, then returns nearest's *base word*
    public String get(String word) {
        if (unweightMap.containsKey(word)) word = unweightMap.get(word);
        Word w = wordget(word);
        return w != null ? w.word : null;
    }

    public void addSynonym(String thisMeans,String theSameAsThis) {
        Word w = wordFor(thisMeans);
        if (w == null) {
            w = wordFor(theSameAsThis);
            if (w == null) {
                addWord(thisMeans);
                mapget(thisMeans).addSynonym(theSameAsThis);
                return;
            }
            w.addSynonym(thisMeans);
            return;
        }
        w.addSynonym(theSameAsThis);
    }

    private Word wordFor(String word) {
        word = word.toLowerCase();
        for (Map.Entry<String, Word> wordEntry : wordMap.entrySet()) {
            if (wordEntry.getValue().means(word)) {
                return wordEntry.getValue();
            }
        }
        return null;
    }

    public List<String> synonymsFor(String word) {
        Word w = wordFor(word);
        if (w == null) return null;
        List<String> group = w.group();
        group.remove(word.toLowerCase());
        return group;
    }

    public static BoW fromFile(String filename) {
        BoW bag = new BoW();
        InputReader reader = new FileInputReader(filename, ",");
        while (reader.hasNext()) {
            List<String> synonyms = reader.next();
            bag.addWord(Word.fromSynonyms(synonyms));
        }
        return bag;
    }

    public BoW from(String filename) {
        stored_bow_filename = filename;
        return this;
    }

    public BoW fetch() {
        return fromFile(stored_bow_filename);
    }

    public void write() {
        try {
            FileWriter scribe = new FileWriter(stored_bow_filename,false);
            for (String word : vocab) {
                String str = wordFor(word).stringify();
                String toWrite = str != null ? str + "\n" : "";
                scribe.write(toWrite);
            }
            scribe.close();
        } catch (IOException e) {
            console.log("Couldn't perform write: IO Exception. Msg: " + e.getLocalizedMessage());
        }
    }

    private Word nearest(String word) {
        word = word.toLowerCase();
        Word nearest = null;
        double maxNearness = 0;
        List<Word> maxTie = new ArrayList<>();
        // find max nearness word
        for (Map.Entry<String, Word> entry : wordMap.entrySet()) {
            double nearness = entry.getValue().maxNearness(word);
            if (nearness > maxNearness) {
                nearest = entry.getValue();
                maxNearness = nearness;
                maxTie.clear();
                maxTie.add(entry.getValue());
            } else if (nearness == maxNearness) {
                maxTie.add(entry.getValue());
            }
        }
        if (maxTie.size() == 1) return nearest;
        // if 2+ closest, get highest avg
        double maxavg = 0;
        for (Word w : maxTie) {
            double nearavg = w.avgNearness(word);
            if (nearavg > maxavg) {
                nearest = w;
                maxavg = nearavg;
            } // ties broken by which came first
        }

        return nearest;
    }

    public void addToNearest(String word) {
        nearest(word).addSynonym(word);
    }

//    public BoW trimWeight(String word, int weight) {
//        String newWord = word.substring(0,weight);
//        unweightMap.put(word, newWord);
//        return this;
//    }

    public static void main(String[] args) {
        BoW bag = BoW.fromFile("src/main/java/com/ai/nlp/test/data/synonyms");
        console.log(bag.vocab);
        console.log(bag.synonymsFor("Football"));
        console.log(bag.get("FOOBA"));
        bag.write();
    }

}
