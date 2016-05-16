package common.wordnet;

import common.IOUtils;
import vocab.Vocab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.google.common.collect.Sets;


public class LexicalResourceNoun {
    HashMap<Integer, HashSet<Integer>> antonyms;
    HashMap<Integer, HashSet<Integer>> synonyms;
    HashMap<Integer, HashSet<Integer>> features;
    //Vocab vocab;
    Random random;
    
    public LexicalResourceNoun(String antFile, String synFile, String featureFile, Vocab vocab) throws IOException {
        antonyms = readLexical(antFile, vocab);
        synonyms = readLexical(synFile, vocab);
        features = readFeatures(featureFile, vocab);
        //this.vocab = vocab;
        random = new Random();
    }
       
    public HashMap<Integer, HashSet<Integer>> readLexical(String inputFile, Vocab vocab) throws IOException {
        HashMap<Integer, HashSet<Integer>> lexical = new HashMap<Integer, HashSet<Integer>>();
        ArrayList<String> data = IOUtils.readFile(inputFile);
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("\t");
            String key = elements[0];
            int keyIndex = vocab.getWordIndex(key);
            if (keyIndex == -1) continue;
            HashSet<Integer> value = new HashSet<Integer>();
            for (int j = 1; j < elements.length; j++ ) {
                int wordIndex = vocab.getWordIndex(elements[j]);;
                if (wordIndex == -1) continue;
                //value.add(elements[j]);
                value.add(wordIndex);
            }
            lexical.put(keyIndex, value);
        }
        return lexical;
    }
    
    public HashMap<Integer, HashSet<Integer>> readFeatures(String inputFile, Vocab vocab) throws IOException {
        HashMap<Integer, HashSet<Integer>> features = new HashMap<Integer, HashSet<Integer>>();
        ArrayList<String> data = IOUtils.readFile(inputFile);
        for (int i = 0; i < data.size(); i++) {
            String dataPiece = data.get(i);
            String elements[] = dataPiece.split("\t");
            String key = elements[0];
            int keyIndex = vocab.getWordIndex(key);
            if (keyIndex == -1) continue;
            HashSet<Integer> value = new HashSet<Integer>();
            for (int j = 1; j < elements.length; j++ ) {
                int wordIndex = -1;
                wordIndex = vocab.getWordIndex(elements[j]);
                if (wordIndex == -1) continue;
                value.add(wordIndex);
            }
            features.put(keyIndex, value);
        }
        return features;
    }
    /*
    public HashSet<String> intersectionAnt(String target, String feature) {
        HashSet<String> intersection = new HashSet<String>();
        if (antonyms.containsKey(target) && features.containsKey(feature)) {
            HashSet<String> setTargets = antonyms.get(target);
            HashSet<String> setFeatures = features.get(feature);
            intersection = getIntersection(setTargets, setFeatures);                
        }
        return intersection;
    }
    
    public HashSet<String> intersectionSyn(String target, String feature) {
        HashSet<String> intersection = new HashSet<String>();
        if (synonyms.containsKey(target) && features.containsKey(feature)) {
            HashSet<String> setTargets = synonyms.get(target);
            HashSet<String> setFeatures = features.get(feature);
            intersection = getIntersection(setTargets, setFeatures);
        }                
        return intersection;
    }*/
    
    public Set<Integer> intersectionAnt(Integer targetIndex, Integer featureIndex) {
        Set<Integer> intersection = new HashSet<Integer>();
        if (antonyms.containsKey(targetIndex) && features.containsKey(featureIndex)) {
            Set<Integer> setTargets = antonyms.get(targetIndex);
            Set<Integer> setFeatures = features.get(featureIndex);
            intersection = Sets.intersection(setTargets, setFeatures);                
        }
        return intersection;
    }
    
    public Set<Integer> intersectionSyn(Integer targetIndex, Integer featureIndex) {
        Set<Integer> intersection = new HashSet<Integer>();
        if (synonyms.containsKey(targetIndex) && features.containsKey(featureIndex)) {
            Set<Integer> setTargets = synonyms.get(targetIndex);
            Set<Integer> setFeatures = features.get(featureIndex);
            intersection = Sets.intersection(setTargets, setFeatures);
        }                
        return intersection;
    }
    
    public HashSet<Integer> getIntersection(HashSet<Integer> hs1, HashSet<Integer> hs2) {
        HashSet<Integer> intersection = new HashSet<Integer>();
        for (Integer element: hs1) {
            if (hs2.contains(element)) intersection.add(element);
        }
        return intersection;
    }
    
    public int getRandom(Set<Integer> antonyms) {
        List<Integer> listAnts = new ArrayList<Integer>(antonyms);
        int id = random.nextInt(listAnts.size());
        return listAnts.get(id);
    }
    
    public boolean hasTarget(Integer targetIndex) {
        return antonyms.containsKey(targetIndex) || synonyms.containsKey(targetIndex);
    }
    
    public boolean hasAntonyms(Integer targetIndex) {
        return antonyms.containsKey(targetIndex);
    }
    
    public boolean hasSynonyms(Integer targetIndex) {
        return synonyms.containsKey(targetIndex);
    }
    
    public boolean hasFeature(Integer featureIndex) {
        return features.containsKey(featureIndex);
    }
    
    public HashSet<Integer> getAntonyms(Integer targetIndex) {
        return antonyms.get(targetIndex);
    }
    
    public HashSet<Integer> getSynonyms(Integer targetIndex) {
        return synonyms.get(targetIndex);
    }
    
    public HashSet<Integer> getFeatures(Integer featureIndex) {
        return features.get(featureIndex);
    }

}
