package word2vec;

import io.sentence.SentenceInputStream;
import io.sentence.SubSamplingSentenceInputStream;
import io.word.Phrase;

import java.io.IOException;
import java.util.ArrayList;

import space.RawSemanticSpace;

import common.correlation.MenCorrelation;

/**
 * Still abstract class for learning words' vectors
 * Implement some common methods
 *
 */
public abstract class SingleThreadWord2Vec extends AbstractWord2Vec {

    protected long oldWordCount;
    protected MenCorrelation men;
    protected RawSemanticSpace outputSpace;
    protected RawSemanticSpace negSpace;
    

    public SingleThreadWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample);
    }
    
    public SingleThreadWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample, String menFile) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample);
        men = new MenCorrelation(menFile);
    }

    @Override
    public void trainModel(ArrayList<SentenceInputStream> inputStreams) {
        // single-threaded instead of multi-threaded
        oldWordCount = 0;
        wordCount = 0;
        trainWords = vocab.getTrainWords();
        System.out.println("train words: " + trainWords);
        System.out.println("vocab size: " + vocab.getVocabSize());
        System.out.println("hidden size: " + projectionLayerSize);
        System.out.println("first word:" + vocab.getEntry(0).word);
        System.out.println("last word:"
                + vocab.getEntry(vocab.getVocabSize() - 1).word);
        
        if (men != null) {
            outputSpace = new RawSemanticSpace(vocab, weights0, false);
            if (negativeSamples > 0) {
                negSpace = new RawSemanticSpace(vocab, negativeWeights1, false);
            }
        }
        
        for (SentenceInputStream inputStream : inputStreams) {
            if (subSample > 0) {
                inputStream = new SubSamplingSentenceInputStream(inputStream, subSample);
            }
            trainModelThread(inputStream);
        }
        System.out.println("total word count: " + wordCount);
    }

    void trainModelThread(SentenceInputStream inputStream) {
        oldWordCount = wordCount;
        long lastWordCount = wordCount;
        try {
            int iteration = 0;
            while (true) {

                // read the whole sentence sentence,
                // the output would be the list of the word's indices in the
                // dictionary
                boolean hasNextSentence = inputStream.readNextSentence(vocab);
                if (!hasNextSentence) break;
                int[] sentence = inputStream.getCurrentSentence();
                Phrase[] phrases = inputStream.getCurrentPhrases();
                // if end of file, finish
                if (sentence.length == 0) {
                    continue;
//                    if (!hasNextSentence)
//                        break;
                }

                // check word count
                // update alpha
                wordCount = oldWordCount + inputStream.getWordCount();
//                System.out.println(wordCount);
//                System.out.println(inputStream.getWordCount());
                
                if (wordCount - lastWordCount > 10000) {
                    iteration++;
                    // if (wordCount - lastWordCount > 50) {
                    
                    // update alpha
                    // what about thread safe???
                    alpha = starting_alpha
                            * (1 - (double) wordCount / (trainWords + 1));
                    if (alpha < starting_alpha * 0.0001) {
                        alpha = starting_alpha * 0.0001;
                    }
                    if (men != null && outputSpace != null && iteration %10 == 0) {
                        System.out.println("men: " + men.evaluateSpacePearson(outputSpace));
//                        System.out.println("men neg: " + men.evaluateSpacePearson(negSpace));
                        printStatistics();
                    }
                    if (iteration % 10 == 0) {
                        System.out.println("Trained: " + wordCount + " words");
                        System.out.println("Training rate: " + alpha);
                    }
                    lastWordCount = wordCount;
                }
                trainSentence(sentence);
                trainPhrases(phrases, sentence);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void trainPhrases(Phrase[] phrases, int[] sentence) {
        for (Phrase phrase : phrases) {
            trainSinglePhrase(phrase, sentence);
        }
    }
    
    public void printStatistics() {
    }

    public abstract void trainSinglePhrase(Phrase phrase,
            int[] sentence);

    public abstract void trainSentence(int[] sentence);

}
