package io.sentence;

import io.word.Phrase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import common.DataStructureUtils;

import vocab.Vocab;
import vocab.VocabEntry;

public class SubSamplingSentenceInputStream implements SentenceInputStream {

    SentenceInputStream inputStream;
    double               frequencyThreshold;
    int[]               sentence;
    Phrase[]            phrases;
    Random              rand = new Random();

    public SubSamplingSentenceInputStream(SentenceInputStream inputStream,
            double frequencyThreshold) {
        this.inputStream = inputStream;
        this.frequencyThreshold = frequencyThreshold;
    }

    protected boolean isSampled(long count, long totalCount) {
        double randomThreshold = (double) (Math.sqrt(count
                / (frequencyThreshold * totalCount)) + 1)
                * (frequencyThreshold * totalCount) / count;
        if (randomThreshold >= rand.nextFloat()) {
            return true;
        } else {
            return false;
        }
    }

    protected void filterSentence(int[] unFilteredSentence,
            Phrase[] unFilteredPhrases, Vocab vocab) {
        ArrayList<Integer> filteredIndices = new ArrayList<Integer>();
        long totalCount = vocab.getTrainWords();
        int[] newPositions = new int[unFilteredSentence.length];
        int newPosition = 0;
        for (int i = 0; i < unFilteredSentence.length; i++) {
            int vocabEntryIndex = unFilteredSentence[i];
            if (vocabEntryIndex == -1) {
                newPositions[i] = Integer.MIN_VALUE;
                continue;
            }
            VocabEntry entry = vocab.getEntry(vocabEntryIndex);
            long count = entry.frequency;

            if (isSampled(count, totalCount)) {
                filteredIndices.add(vocabEntryIndex);
                newPositions[i] = newPosition;
                newPosition++;
            }
            // set those words'positions that are not in vocab to -1
            else {
                newPositions[i] = Integer.MIN_VALUE;
            }
        }
//        System.out.println("\nOld Sentence:");
//        for (int i = 0; i < unFilteredSentence.length; i++)
//        {
//            System.out.print(" "+unFilteredSentence[i]);
//        }
//        System.out.println("\nOld phrase:");
//        for (int i = 0; i < unFilteredPhrases.length; i++)
//        {
//            System.out.print("("+unFilteredPhrases[i].startPosition + " " + +unFilteredPhrases[i].endPosition + ") ");
//        }
//        System.out.println();
        sentence = DataStructureUtils.intListToArray(filteredIndices);

        ArrayList<Phrase> fileterPhraseList = new ArrayList<Phrase>();
        for (Phrase unFilteredPhrase : unFilteredPhrases) {
            int phraseType = unFilteredPhrase.phraseType;
            int startPosition = newPositions[unFilteredPhrase.startPosition];
            int endPosition = newPositions[unFilteredPhrase.endPosition];
            // TODO: check if this condition is correct
            if (endPosition - startPosition == unFilteredPhrase.endPosition - unFilteredPhrase.startPosition) {
                Phrase phrase = new Phrase(phraseType, startPosition,
                        endPosition, unFilteredPhrase.tree);
                fileterPhraseList.add(phrase);
            } 
            else if (Math.max(startPosition, endPosition) >= 0) {
                int maxPosition = Math.max(startPosition, endPosition);
                Phrase phrase = new Phrase(phraseType, maxPosition,
                        maxPosition, unFilteredPhrase.tree);
                fileterPhraseList.add(phrase);
            }
        }
//        System.out.println("New pos:");
//        for (int i = 0; i < newPositions.length; i++)
//        {
//            System.out.print(""+i+":"+newPositions[i] + " ");
//        }
//        System.out.println("\nNew Sentence:");
//        for (int i = 0; i < sentence.length; i++)
//        {
//            System.out.print(" "+sentence[i]);
//        }
//        System.out.println("\nNew phrase:");
        phrases = DataStructureUtils.phraseListToArray(fileterPhraseList);
//        for (int i = 0; i < phrases.length; i++)
//        {
//            System.out.print("("+phrases[i].startPosition + " " + +phrases[i].endPosition + ") ");
//        }
//        System.out.println();
        
    }

    @Override
    public boolean readNextSentence(Vocab vocab) throws IOException {
        boolean hasNextSentence = inputStream.readNextSentence(vocab);
        if (hasNextSentence) {
            int[] unFilteredSentence = inputStream.getCurrentSentence();
            Phrase[] unFilteredPhrases = inputStream.getCurrentPhrases();
            filterSentence(unFilteredSentence, unFilteredPhrases, vocab);
        }
        return hasNextSentence;
    }

    @Override
    public int[] getCurrentSentence() throws IOException {
        return sentence;
    }

    @Override
    public Phrase[] getCurrentPhrases() throws IOException {
        return phrases;
    }

    @Override
    public long getWordCount() {
        return inputStream.getWordCount();
    }

    @Override
    public boolean crossDocBoundary() {
        // TODO Auto-generated method stub
        return inputStream.crossDocBoundary();
    }

}
