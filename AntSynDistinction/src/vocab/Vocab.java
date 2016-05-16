package vocab;

import io.word.PushBackWordStream;
import io.word.WordInputStream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Vocab {
    // maximum length for a word
    // not particularly necessary
    public static int                  MAX_LENGTH = 100;

    // hashmap: from word to index, for fast retrieval
    protected HashMap<String, Integer> vocabHash;

    /*
     * list of VocabEntry/word in the vocabulary It is expected that this list
     * of VocabEntry is always sorted based on frequency at any time after a
     * public method/constructor is called
     */
    protected ArrayList<VocabEntry>    vocab;

    // vocab Size
    protected int                      vocabSize  = 0;

    // min frequency
    protected int                      minFrequency;

    /*
     * the number of words' occurrences to expect from the training corpus It is
     * to keep track of the training progress, and modifying the learning rate
     * accordingly (could use file size and the number of bytes processed
     * instead but it's kind of hard with GzipInputStream)
     */
    long                               trainWords = 0;

    /*
     * Default constructor
     */
    public Vocab() {
        this.minFrequency = 0;
        vocabHash = new HashMap<String, Integer>();
        vocab = new ArrayList<VocabEntry>(10000);
    }

    public Vocab(int minFrequency) {
        this();
        this.minFrequency = minFrequency;
    }

    /*
     * Add a new word to the vocabulary, the new word must not exist in the
     * vocabulary. if it does, return without doing anything
     */
    protected void addWordToVocab(String word) {
        if (vocabHash.get(word) != null) {
            return;
        }
        VocabEntry newWord = new VocabEntry(word, 0);
        vocabHash.put(word, vocabSize);
        vocab.add(newWord);
        vocabSize++;
    }

    /*
     * Add a new entry to the vocabulary
     */
    protected void addEntry(VocabEntry newEntry) {
        if (vocabHash.get(newEntry.word) != null) {
            System.out.println("duplicated word:" + newEntry.word);
        } else {
            vocab.add(newEntry);
            vocabHash.put(newEntry.word, vocabSize);
            vocabSize++;
        }

    }

    /*
     * retrieve an entry using its position in the Vocabulary
     */
    public VocabEntry getEntry(int position) {
        if (position >= vocabSize || position < 0) {
            return null;
        } else {
            return vocab.get(position);
        }
    }

    /*
     * retrieve an entry using its surface string
     */
    public VocabEntry getEntry(String word) {
        return getEntry(getWordIndex(word));
    }

    /*
     * retrieve the index of a word in the vocabulary
     */
    public int getWordIndex(String word) {
        Integer index = vocabHash.get(word);
        if (index == null)
            return -1;
        return index.intValue();
    }

    /*
     * Learn the vocabulary from a text file by: - creating a WordInputStream -
     * learn the vocabulary from the stream
     */
    public void learnVocabFromTrainFile(String fileName) {
        try {
            PushBackWordStream is = new PushBackWordStream(fileName, MAX_LENGTH);
            learnVocabFromTrainStream(is);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    /*
     * Learn the vocabulary from a WordInputStream
     */
    public void learnVocabFromTrainStream(WordInputStream inputStream) {
        String word;
        trainWords = 0;
        vocabHash = new HashMap<String, Integer>();
        vocab = new ArrayList<VocabEntry>();
        try {
            vocabSize = 0;
            addWordToVocab("</s>");
            while (true) {
                word = inputStream.readWord();
                if ("".equals(word)) {
                    System.out.println("End of file?: "
                            + inputStream.endOfFile());
                    break;
                }
                trainWords++;
                if (trainWords % 100000 == 0) {
                    System.out.println("" + (trainWords / 1000) + "K words");
                }
                int i = getWordIndex(word);
                if (i == -1) {
                    addWordToVocab(word);
                    vocab.get(vocabSize - 1).frequency += 1;
                } else {
                    vocab.get(i).frequency += 1;
                }
            }
            sortAndReduceVocab(minFrequency);
            System.out.println("Vocab size: " + vocabSize);
            System.out.println("Words in train file: " + trainWords);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /*
     * Sorts the vocabulary by frequency using word counts, and remove words
     * with frequency less than minCount
     */
    protected void sortAndReduceVocab(int minCount) {
        // modify frequency of "new line" to keep it always at the top
        long newLineFrequency = vocab.get(0).frequency;
        vocab.get(0).frequency = Integer.MAX_VALUE;

        // sort the vocabulary based on frequency
        Collections.sort(vocab, Collections
                .reverseOrder(VocabEntry.VocabEntryFrequencyComparator));

        // creating a new HashMap
        // loop through the vocabulary and keep only words that have frequency
        // larger than minCount
        vocabHash = new HashMap<String, Integer>();
        System.out.println("read train words: " + trainWords);
        trainWords = 0;
        int newIndex = 0;
        for (int index = 0; index < vocabSize; index++) {
            if (vocab.get(index).frequency >= minCount) {
                VocabEntry entry = vocab.get(index);
                vocab.set(newIndex, entry);
                vocabHash.put(entry.word, newIndex);
                trainWords += vocab.get(newIndex).frequency;
                newIndex++;
            }
        }

        // throw away the rest
        vocabSize = newIndex;
        if (vocabSize < vocab.size()) {
            ArrayList<VocabEntry> newVocab = new ArrayList<VocabEntry>(
                    vocabSize);
            for (int a = 0; a < vocabSize; a++) {
                newVocab.add(vocab.get(a));
            }
            vocab = newVocab;
        }

        // restore the frequency of "new line"
        vocab.get(0).frequency = newLineFrequency;
//        trainWords = trainWords - Integer.MAX_VALUE + newLineFrequency;
        trainWords = trainWords - Integer.MAX_VALUE;
        System.out.println("read train words: " + trainWords);
    }

    /*
     * Sorts the vocabulary by frequency using word counts, and remove words
     * with frequency less than minCount
     */
    public void applyFilter(VocabEntryFilter filter) {

        // creating a new HashMap
        vocabHash = new HashMap<String, Integer>();
        System.out.println("read train words: " + trainWords);

        // keeping the newLine
        trainWords = vocab.get(0).frequency;
        vocabHash.put(vocab.get(0).word, 0);

        // loop through the vocabulary and keep only words that are not filtered
        // by the filter
        int newIndex = 1;
        for (int index = 1; index < vocabSize; index++) {
            if (!filter.isFiltered(vocab.get(index))) {
                VocabEntry entry = vocab.get(index);
                vocab.set(newIndex, entry);
                vocabHash.put(entry.word, newIndex);
                trainWords += vocab.get(newIndex).frequency;
                newIndex++;
            }
        }

        // throw away the rest
        vocabSize = newIndex;
        if (vocabSize < vocab.size()) {
            ArrayList<VocabEntry> newVocab = new ArrayList<VocabEntry>(
                    vocabSize);
            for (int a = 0; a < vocabSize; a++) {
                newVocab.add(vocab.get(a));
            }
            vocab = newVocab;
        }

        System.out.println("read train words: " + trainWords);
    }

    /*
     * Store the vocabulary in a file Only store the words and their frequencies
     */
    public void saveVocab(String vocabFile) {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(vocabFile));
            for (int i = 0; i < vocabSize; i++) {
                VocabEntry curWord = vocab.get(i);
                writer.write(curWord.word + " " + curWord.frequency + "\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Error in writing vocabulary");
            e.printStackTrace();
        }

    }

    /*
     * Read words and their statistics from a file
     */
    protected void readWords(String vocabFile) {
        vocab = new ArrayList<VocabEntry>();
        vocabHash = new HashMap<String, Integer>();
        vocabSize = 0;
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(vocabFile));
            String line = reader.readLine();
            while (line != null) {
                String[] elements = line.split(" ");
                if (elements.length != 2) {
                    System.out.println("vocabulary entry needs two elements");
                    System.exit(1);
                } else {
                    VocabEntry newEntry = new VocabEntry(elements[0],
                            Integer.parseInt(elements[1]));
                    addEntry(newEntry);
                }
                line = reader.readLine();
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /*
     * Load the vocabulary from a file Sort and move words with frequency
     * smaller than minFrequency
     */
    public void loadVocab(String vocabFile) {
        readWords(vocabFile);
        sortAndReduceVocab(minFrequency);
    }

    /*
     * Create a binary tree & assign the code to the words in the vocabulary
     * Must do this step before learning word vectors using Hierarchical Softmax
     */
    public void assignCode() {
        // Now assign binary code to each vocabulary word
        long[] counts = new long[vocabSize];
        for (int a = 0; a < vocabSize; a++) {
            counts[a] = vocab.get(a).frequency;
        }
        HuffmanTree huffmanTree = new HuffmanTree(counts);
        for (int index = 0; index < vocabSize; index++) {
            VocabEntry word = vocab.get(index);
            word.ancestors = huffmanTree.getParentIndices(index);
            word.code = huffmanTree.getCode(index);
        }
    }

    // get methods
    public int getVocabSize() {
        return vocabSize;
    }

    public long getTrainWords() {
        return trainWords;
    }

}
