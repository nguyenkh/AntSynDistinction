package vocab.filter;

import vocab.VocabEntry;
import vocab.VocabEntryFilter;

public class MinFrequencyVocabFilter implements VocabEntryFilter {
    protected int minFrequency;

    public MinFrequencyVocabFilter(int minFrequency) {
        this.minFrequency = minFrequency;
    }

    @Override
    public boolean isFiltered(VocabEntry entry) {
        // if the frequency of the word is less the minFrequency, return true
        // to filter it
        return entry.frequency < minFrequency;
    }

}
