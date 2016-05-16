package io.sentence;

import io.word.Phrase;
import io.word.WordInputStream;

import java.io.IOException;
import java.util.ArrayList;

import common.DataStructureUtils;

import vocab.Vocab;

public class PlainSentenceInputStream implements SentenceInputStream {
    public static final int DEFAULT_MAX_SENTENCE_LENGTH = 1000;
    WordInputStream         inputStream;
    long                    wordCount;
    int[]                   sentence;

    public PlainSentenceInputStream(WordInputStream inputStream) {
        this.inputStream = inputStream;
        wordCount = 0;
    }

    @Override
    public boolean readNextSentence(Vocab vocab) throws IOException {
        ArrayList<Integer> currentSentence = new ArrayList<Integer>();
        while (true) {
            // read the next word & the word index
            String word = "";
            word = inputStream.readWord();

            if ("".equals(word))
                break;
            int wordIndex = vocab.getWordIndex(word);

            // if the word is not in the vocabulary, continue
            if (wordIndex == -1)
                continue;
            else
                wordCount++;

            // end of sentence -> break;
            if (wordIndex == 0) {
                // System.out.println("end of sentence: " + word);
                break;
            }

            currentSentence.add(wordIndex);
            // break if sentence is too long
            if (currentSentence.size() >= DEFAULT_MAX_SENTENCE_LENGTH)
                break;

        }
        // System.out.println("sentence length: " + sentence.size());
        sentence = DataStructureUtils.intListToArray(currentSentence);
        if (sentence.length == 0 && inputStream.endOfFile())
            return false;
        else
            return true;

    }

    @Override
    public int[] getCurrentSentence() throws IOException {
        return sentence;
    }

    @Override
    public Phrase[] getCurrentPhrases() throws IOException {
        return new Phrase[0];
    }

    @Override
    public long getWordCount() {
        return wordCount;
    }

    @Override
    public boolean crossDocBoundary() {
        // TODO Auto-generated method stub
        return false;
    }
}
