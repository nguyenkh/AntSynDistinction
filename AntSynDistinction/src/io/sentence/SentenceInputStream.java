package io.sentence;

import io.word.Phrase;

import java.io.IOException;

import vocab.Vocab;

public interface SentenceInputStream {
    public boolean readNextSentence(Vocab vocab) throws IOException;

    public int[] getCurrentSentence() throws IOException;
    
    public boolean crossDocBoundary();

    public Phrase[] getCurrentPhrases() throws IOException;

    public long getWordCount();
}
