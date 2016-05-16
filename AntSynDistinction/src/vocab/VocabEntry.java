package vocab;

import java.util.Comparator;

public class VocabEntry {
    // count the word in the training file
    public long   frequency;

    // the ancestors' indices in the huffman tree
    public int[]  ancestors;

    // the surface string
    public String word;

    // the huffman code
    public String code;

    public VocabEntry() {
        word = "";
        frequency = 0;
    }

    public VocabEntry(String word, int frequency) {
        this.word = word;
        this.frequency = frequency;
    }

    public static Comparator<VocabEntry> VocabEntryFrequencyComparator = new Comparator<VocabEntry>() {

                                                                           @Override
                                                                           public int compare(
                                                                                   VocabEntry o1,
                                                                                   VocabEntry o2) {
                                                                               if (o1.frequency > o2.frequency) {
                                                                                   return 1;
                                                                               } else if (o1.frequency < o2.frequency) {
                                                                                   return -1;
                                                                               } else {
                                                                                   return 0;
                                                                               }
                                                                           }

                                                                       };

}
