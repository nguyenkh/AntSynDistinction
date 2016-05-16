package common.wordnet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import common.IOUtils;

public class WordNetVerb {
    HashMap<String, Synset> id2Synset;
    HashMap<String, ArrayList<String>> word2SynsetIds;
    Random random;
    public WordNetVerb(String verbFile) throws IOException {
        id2Synset = WordNetReader.readSynsets(verbFile);
        word2SynsetIds = WordNetReader.getWord2SynsetIds(id2Synset);
        random = new Random();
    }
    
    public static String[] collection2Array(Collection<String> collection) {
        String[] result = new String[collection.size()];
        return collection.toArray(result);
    }
    
    public static void printStrings(String[] array) {
        StringBuffer sb = new StringBuffer();
        if (array == null || array.length == 0) {
            System.out.println();
            return;
        }
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            String word = array[i];
            sb.append(", " + word );
        }
        System.out.println(sb.toString());
    }
    
    public boolean hasVerbSynset(String word) {
        return word2SynsetIds.containsKey(word);
    }
    
    public static ArrayList<String> getShuffledSynonyms(String[] synonyms, String[] simnyms) {
        ArrayList<String> result = new ArrayList<String>(Arrays.asList(synonyms));
        List<String> simList = Arrays.asList(simnyms);
        Collections.shuffle(simList);
        result.addAll(simList);
        return result;
    }
    
    public String[][] getRandomSynoAntoSimNyms(String word, HashSet<String> forbiddenWords) {
        ArrayList<String> synsetIds = word2SynsetIds.get(word);
        if (synsetIds == null) return null;
        String[][] result = new String[3][];
        HashSet<String> antonyms = new HashSet<String>();
        String id = synsetIds.get(random.nextInt(synsetIds.size()));
        Synset synset = id2Synset.get(id);
        if (synset.antonymSSId == null) {
            
        } else {
            Synset antoSynset = id2Synset.get(synset.antonymSSId);
            for (String antonym: antoSynset.words) {
                if (!forbiddenWords.contains(antonym))
                    antonyms.add(antonym);
            }
        }
        ArrayList<String> synonyms = new ArrayList<String>();
        for (String synonym: synset.words) {
            if (!forbiddenWords.contains(synonym))
                synonyms.add(synonym);
        }
        // TODO: check this
        synonyms.remove(word);
        // TODO: include type s
        ArrayList<String> simnyms = new ArrayList<String>(); 
        String[] hypoSSId = synset.hypoSSId;
        for (String simId: hypoSSId) {
            Synset simSynset = id2Synset.get(simId);
            for (String simnym: simSynset.words) {
                if (!forbiddenWords.contains(simnym))
                    simnyms.add(simnym);
            }
        }
        String[] hyperSSId = synset.hyperSSId;
        for (String simId: hyperSSId) {
            Synset simSynset = id2Synset.get(simId);
            for (String simnym: simSynset.words) {
                if (!forbiddenWords.contains(simnym))
                    simnyms.add(simnym);
            }
        }
        simnyms.remove(word);
        result[2] =collection2Array(simnyms);
        result[1] = collection2Array(synonyms);
        result[0] = collection2Array(antonyms);
        return result;
    }
    
    public String[][] getRandomSynoAntoSimNyms(String word) {
        ArrayList<String> synsetIds = word2SynsetIds.get(word);
        if (synsetIds == null) return null;
        String[][] result = new String[3][];
        HashSet<String> antonyms = new HashSet<String>();
        String id = synsetIds.get(random.nextInt(synsetIds.size()));
        Synset synset = id2Synset.get(id);
        if (synset.antonymSSId == null) {
        
        } else {
            Synset antoSynset = id2Synset.get(synset.antonymSSId);
            for (String antonym: antoSynset.words) {
                antonyms.add(antonym);
            }
        }
        ArrayList<String> synonyms = new ArrayList<String>();
        for (String synonym: synset.words) {
            synonyms.add(synonym);
        }
        // TODO: check this
        synonyms.remove(word);
        // TODO: include type s
        ArrayList<String> simnyms = new ArrayList<String>(); 
        String[] hypoSSId = synset.hypoSSId;
        for (String simId: hypoSSId) {
            Synset simSynset = id2Synset.get(simId);
            for (String simnym: simSynset.words) {
                simnyms.add(simnym);
            }
        }
        String[] hyperSSId = synset.hyperSSId;
        for (String simId: hyperSSId) {
            Synset simSynset = id2Synset.get(simId);
            for (String simnym: simSynset.words) {
                simnyms.add(simnym);
            }
        }
        simnyms.remove(word);
        result[2] =collection2Array(simnyms);
        result[1] = collection2Array(synonyms);
        result[0] = collection2Array(antonyms);
        return result;
    }
    
    
    public ArrayList<String[][]> getAllSenseAntonym(String word) {
        ArrayList<String> synsetIds = word2SynsetIds.get(word);
        ArrayList<String[][]> result = new ArrayList<String[][]>();
        if (synsetIds == null) return result;
        for (String id: synsetIds) {
            String[][] antoSynonyms = new String[3][];
            HashSet<String> antonyms = new HashSet<String>();
            Synset synset = id2Synset.get(id);
            if (synset.antonymSSId == null) {
                continue;
            }
            else {
                Synset antoSynset = id2Synset.get(synset.antonymSSId);
                for (String antonym: antoSynset.words) {
                    antonyms.add(antonym);
                }
            }
            ArrayList<String> synonyms = new ArrayList<String>();
            for (String synonym: synset.words) {
                synonyms.add(synonym);
            }
            // TODO: check this
            synonyms.remove(word);
            // TODO: include type s
            ArrayList<String> simnyms = new ArrayList<String>(); 
            String[] hypoSSId = synset.hypoSSId;
            for (String simId: hypoSSId) {
                Synset simSynset = id2Synset.get(simId);
                for (String simnym: simSynset.words) {
                    simnyms.add(simnym);
                }
            }
            String[] hyperSSId = synset.hyperSSId;
            for (String simId: hyperSSId) {
                Synset simSynset = id2Synset.get(simId);
                for (String simnym: simSynset.words) {
                    simnyms.add(simnym);
                }
            }
            simnyms.remove(word);
            antoSynonyms[2] =collection2Array(simnyms);
            antoSynonyms[1] = collection2Array(synonyms);
            antoSynonyms[0] = collection2Array(antonyms);
            result.add(antoSynonyms);
        }
        return result;
    }
    
    public void printAllInfo(String outFile) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
            for (String word: word2SynsetIds.keySet()) {
                List<String[][]> wordInfo = getAllSenseAntonym(word);
                if (wordInfo.size() == 0) continue;
                writer.write("___\n");
                writer.write(word + "\n");
                
                for (String[][] antoSynonyms: wordInfo) {
                    writer.write("+++\n");
                    printStringArray(writer, antoSynonyms[0]);
                    printStringArray(writer, antoSynonyms[1]);
                    printStringArray(writer, antoSynonyms[2]);
                }
                
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void printSplitInfo(String outFile, String wordFile) {
        ArrayList<String> allWords = new ArrayList<String>(word2SynsetIds.keySet());
        ArrayList<String> chosenWords = randomSublist(allWords, 400);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
            for (String word: chosenWords) {
                List<String[][]> wordInfo = getAllSenseAntonym(word);
                if (wordInfo.size() == 0) continue;
                writer.write("___\n");
                writer.write(word + "\n");
                
                for (String[][] antoSynonyms: wordInfo) {
                    writer.write("+++\n");
                    printStringArray(writer, antoSynonyms[0]);
                    printStringArray(writer, antoSynonyms[1]);
                    printStringArray(writer, antoSynonyms[2]);
                }
                
            }
            writer.close();
            IOUtils.printToFile(wordFile, chosenWords);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void printStringArray(BufferedWriter writer, String[] array) throws IOException {
        if (array.length != 0) {
            writer.write(array[0]);
            for (int i =10; i < array.length; i++) {
                writer.write(","+array[i]);
            }
        }
        writer.write("\n");
    }
    
    public static ArrayList<String> randomSublist(List<String> input, int sublistLength) {
        ArrayList<String> tmpList = new ArrayList<String>(input);
        Collections.shuffle(tmpList);
        return new ArrayList<String>(tmpList.subList(0, sublistLength));
    }
    
    public Set<String> getAllWords() {
        return word2SynsetIds.keySet();
    }
    
    public String[] getAllAntonyms(String word) {
        ArrayList<String> synsetIds = word2SynsetIds.get(word);
        if (synsetIds == null) return null;
        HashSet<String> antonyms = new HashSet<String>();
        for (String id: synsetIds) {
            Synset synset = id2Synset.get(id);
            if (synset.antonymSSId == null) {
                // TODO: if it's an s type?
                if (synset.synsetType.equals("s")) {
                    for (String simId: synset.simSSId) {
                        Synset simSynset = id2Synset.get(simId);
                        if (simSynset.antonymSSId == null) continue;
                        Synset antoSynset = id2Synset.get(simSynset.antonymSSId);
                        for (String antonym: antoSynset.words) {
                            antonyms.add(antonym);
                        }
                    }
                }
            }
            else {
                Synset antoSynset = id2Synset.get(synset.antonymSSId);
                for (String antonym: antoSynset.words) {
                    antonyms.add(antonym);
                }
            }
        }
        return collection2Array(antonyms);
    }
    
    public String[] getAllSynonyms(String word) {
        ArrayList<String> synsetIds = word2SynsetIds.get(word);
        if (synsetIds == null) return null;
        HashSet<String> synonyms = new HashSet<String>();
        for (String id: synsetIds) {
            Synset synset = id2Synset.get(id);
            
            // TODO: include type s
//          if (synset.synsetType.equals("s")) continue;
            
            for (String synonym: synset.words) {
                synonyms.add(synonym);
            }
        }
        // TODO: check this
        synonyms.remove(word);
        return collection2Array(synonyms);
    }
    
    public String[] getAllSimilars(String word) {
        ArrayList<String> synsetIds = word2SynsetIds.get(word);
        if (synsetIds == null) return null;
        HashSet<String> simnyms = new HashSet<String>();
        for (String id: synsetIds) {
            Synset synset = id2Synset.get(id);
//           TODO: include type s
//          if (synset.synsetType.equals("s")) continue;
                        
            String[] simSSId = synset.hypoSSId;
            for (String simId: simSSId) {
                Synset simSynset = id2Synset.get(simId);
                for (String simnym: simSynset.words) {
                    simnyms.add(simnym);
                }
            }
        }
        simnyms.remove(word);
        return collection2Array(simnyms);
    }
    
    public static void main(String[] args) throws IOException{
        String verbFile = args[0];
        WordNetVerb wordnetVerb = new WordNetVerb(verbFile);
        String word = "love";
        System.out.println("***   " + word + "   ***");
        
        String[][] antoSynoSimNyms = wordnetVerb.getRandomSynoAntoSimNyms(word);
        System.out.print("antonyms:");
        printStrings(antoSynoSimNyms[0]);
        System.out.print("synonyms:");
        printStrings(antoSynoSimNyms[1]);
        System.out.print("simnyms:");
        printStrings(antoSynoSimNyms[2]);
    }
}
