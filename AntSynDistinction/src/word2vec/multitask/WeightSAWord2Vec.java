package word2vec.multitask;

import java.util.HashSet;
import java.util.Set;

import org.ejml.simple.SimpleMatrix;

import word2vec.MultiThreadWord2Vec;
import common.SimpleMatrixUtils;
import common.wordnet.LexicalResourceAdj;
import common.wordnet.LexicalResourceNoun;
import common.wordnet.LexicalResourceVerb;

public class WeightSAWord2Vec extends MultiThreadWord2Vec{
    public static final int DEFAULT_SYNONYM_SAMPLES = 5;
    public static final double DEFAULT_MARGIN = 0.4;
    public static final double DEFAULT_ANTONYM_IMPORTANCE = 8.0;
    protected int synonymSamples = DEFAULT_SYNONYM_SAMPLES;
    protected double margin = DEFAULT_MARGIN;
    
    protected LexicalResourceAdj lexicalAdj;
    protected LexicalResourceNoun lexicalNoun;
    protected LexicalResourceVerb lexicalVerb;
    protected HashSet<String> forbiddenWords;
    
    
    public WeightSAWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, int synonymSamples, double subSample) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample);
        this.synonymSamples = synonymSamples;
    }
    
    public WeightSAWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, int synonymSamples, double subSample, int iter) {
        super(projectionLayerSize, windowSize, hierarchicalSoftmax,
                negativeSamples, subSample, iter);
        this.synonymSamples = synonymSamples;
    }

    public void setForbiddenWords(HashSet<String> forbiddenWords) {
        this.forbiddenWords = forbiddenWords;
    }
    public void setLexicalAdj(LexicalResourceAdj lexicalAdj) {
        this.lexicalAdj = lexicalAdj;
    }
    public void setLexicalNoun(LexicalResourceNoun lexicalNoun) {
        this.lexicalNoun = lexicalNoun;
    }
    public void setLexicalVerb(LexicalResourceVerb lexicalVerb) {
        this.lexicalVerb = lexicalVerb;
    }
    
    public void trainSentence(int[] sentence) {
        // the parameter is a list of word's indices in the vocabulary
        // train with the sentence
        double[] a1 = new double[projectionLayerSize];
        double[] a1error = new double[projectionLayerSize];
        int sentenceLength = sentence.length;
        int iWordIndex = 0;
        // TODO: set the thing here
        double r = DEFAULT_ANTONYM_IMPORTANCE;

        
        boolean updateAtTheEnd=false;
        
        for (int wordPosition = 0; wordPosition < sentence.length; wordPosition++) {

            int wordIndex = sentence[wordPosition];

            // no way it will go here
            if (wordIndex == -1)
                continue;

            for (int i = 0; i < projectionLayerSize; i++) {
                a1[i] = 0;
                a1error[i] = 0;
            }

            // random actual window size
            int start = rand.nextInt(windowSize);

            //VocabEntry targetWord = vocab.getEntry(wordIndex);
            //String percept = targetWord.word;      

            //modality 1
            for (int i = start; i < windowSize * 2 + 1 - start; i++) {
                if (i != windowSize) {
                    int iPos = wordPosition - windowSize + i;
                    if (iPos < 0 || iPos >= sentenceLength)
                        continue;
                    iWordIndex = sentence[iPos];
                    if (iWordIndex == -1)
                        continue;
                    
                    //VocabEntry context = vocab.getEntry(iWordIndex);
                    
                 // NEGATIVE SAMPLING
                    if (negativeSamples > 0) {
                        for (int l = 0; l < negativeSamples + 1; l++) {
                            int target;
                            int label;

                            if (l == 0) {
                                target = iWordIndex;
                                label = 1;
                            } else {
                                target = unigram.randomWordIndex();
                                if (target == 0) {
                                    target = rand.nextInt(vocab.getVocabSize() - 1) + 1;
                                }
                                if (target == iWordIndex)
                                    continue;
                                label = 0;
                            }
                            double z2 = 0;
                            double gradient;
                            for (int j = 0; j < projectionLayerSize; j++) {
                                z2 += weights0[wordIndex][j]
                                        * negativeWeights1[target][j];
                            }
                            double a2 = sigmoidTable.getSigmoid(z2);
                            
                            gradient = (double) ((label - a2) * alpha);
                            for (int j = 0; j < projectionLayerSize; j++) {
                                a1error[j] += gradient
                                        * negativeWeights1[target][j];
                            }
                            for (int j = 0; j < projectionLayerSize; j++) {
                                negativeWeights1[target][j] += gradient *r
                                        * weights0[wordIndex][j];
                            }
                        }
                    }
                    // Learn weights input -> hidden
                    if (!updateAtTheEnd){
                        for (int j = 0; j < projectionLayerSize; j++) {
                            weights0[wordIndex][j] += a1error[j];
                            a1error[j] = 0;

                        }
                    }
                    //modality 2
                    
                    ////////////////////////////////////////////
                    //INTEGRATING LEXICAL CONTRAST INFORMATION
                    ////////////////////////////////////////////
                    
                    //Compute adjective lexical
                    if (lexicalAdj.hasTarget(wordIndex) && lexicalAdj.hasFeature(iWordIndex)) {
                        SimpleMatrix a1error_temp = new SimpleMatrix(1, a1error.length);
                        SimpleMatrix wordVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[wordIndex]);
                        boolean isAntonyms = lexicalAdj.hasAntonyms(wordIndex);
                        boolean isSynonyms = lexicalAdj.hasSynonyms(wordIndex); 
                        double gradient = 0;
                        int countAnts = 0;
                        int countSyns = 0;
                        SimpleMatrix synonymError = new SimpleMatrix(1, a1error.length);
                        SimpleMatrix antonymError = new SimpleMatrix(1, a1error.length);
                        if (isAntonyms) {
                            Set<Integer> antonyms = lexicalAdj.intersectionAnt(wordIndex, iWordIndex);
                            if (antonyms.isEmpty()) continue;
                            for (Integer antonymIndex: antonyms) {
                                SimpleMatrix antonymVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[antonymIndex]);
                                antonymError = antonymError.plus(SimpleMatrixUtils.cosineDerivative(wordVector, antonymVector));
                                countAnts = countAnts + 1;
                            }
                        }
                        
                        if (isSynonyms) {
                            Set<Integer> synonyms = lexicalAdj.intersectionSyn(wordIndex, iWordIndex);
                            if (synonyms.isEmpty()) continue;
                            for (Integer synonymIndex: synonyms) {
                                countSyns = countSyns + 1;
                                SimpleMatrix synonymVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[synonymIndex]);
                                synonymError = synonymError.plus(SimpleMatrixUtils.cosineDerivative(wordVector, synonymVector));
                            }
                        }
                        if (countAnts == 0) countAnts = 1;
                        if (countSyns == 0) countSyns = 1;
                        gradient = (double)(alpha*r);
                        a1error_temp = a1error_temp.plus(synonymError.scale(countAnts));
                        a1error_temp = a1error_temp.minus(antonymError.scale(countSyns));
                        a1error_temp = a1error_temp.scale(gradient);
                            
                        double[] errorArray = a1error_temp.getMatrix().data;
                            
                        // Learn weights input -> hidden
                            
                        for (int j = 0; j < projectionLayerSize; j++) {
                            weights0[wordIndex][j] += errorArray[j];
                            a1error[j] = 0;
                        }
                    }
                    //Compute noun lexical
                    if (lexicalNoun.hasTarget(wordIndex) && lexicalNoun.hasFeature(iWordIndex)) {
                        SimpleMatrix a1error_temp = new SimpleMatrix(1, a1error.length);
                        SimpleMatrix wordVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[wordIndex]);
                        boolean isAntonyms = lexicalNoun.hasAntonyms(wordIndex);
                        boolean isSynonyms = lexicalNoun.hasSynonyms(wordIndex); 
                        double gradient = 0;
                        int countAnts = 0;
                        int countSyns = 0;
                        SimpleMatrix synonymError = new SimpleMatrix(1, a1error.length);
                        SimpleMatrix antonymError = new SimpleMatrix(1, a1error.length);
                        if (isAntonyms) {
                            Set<Integer> antonyms = lexicalNoun.intersectionAnt(wordIndex, iWordIndex);
                            if (antonyms.isEmpty()) continue;
                            for (Integer antonymIndex: antonyms) {
                                SimpleMatrix antonymVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[antonymIndex]);
                                antonymError = antonymError.plus(SimpleMatrixUtils.cosineDerivative(wordVector, antonymVector));
                                countAnts = countAnts + 1;
                            }
                        }
                        
                        if (isSynonyms) {
                            Set<Integer> synonyms = lexicalNoun.intersectionSyn(wordIndex, iWordIndex);
                            if (synonyms.isEmpty()) continue;
                            for (Integer synonymIndex: synonyms) {
                                countSyns = countSyns + 1;
                                SimpleMatrix synonymVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[synonymIndex]);
                                synonymError = synonymError.plus(SimpleMatrixUtils.cosineDerivative(wordVector, synonymVector));
                            }
                        }
                        if (countAnts == 0) countAnts = 1;
                        if (countSyns == 0) countSyns = 1;
                        gradient = (double)(alpha*r);
                        a1error_temp = a1error_temp.plus(synonymError.scale(countAnts));
                        a1error_temp = a1error_temp.minus(antonymError.scale(countSyns));
                        a1error_temp = a1error_temp.scale(gradient);
                        double[] errorArray = a1error_temp.getMatrix().data;
                            
                        // Learn weights input -> hidden
                            
                        for (int j = 0; j < projectionLayerSize; j++) {
                            weights0[wordIndex][j] += errorArray[j];
                            a1error[j] = 0;
                        }
                    }
                    //Compute verb lexical
                    if (lexicalVerb.hasTarget(wordIndex) && lexicalVerb.hasFeature(iWordIndex)) {
                        SimpleMatrix a1error_temp = new SimpleMatrix(1, a1error.length);
                        SimpleMatrix wordVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[wordIndex]);
                        boolean isAntonyms = lexicalVerb.hasAntonyms(wordIndex);
                        boolean isSynonyms = lexicalVerb.hasSynonyms(wordIndex); 
                        double gradient = 0;
                        int countAnts = 0;
                        int countSyns = 0;
                        SimpleMatrix synonymError = new SimpleMatrix(1, a1error.length);
                        SimpleMatrix antonymError = new SimpleMatrix(1, a1error.length);
                        if (isAntonyms) {
                            Set<Integer> antonyms = lexicalVerb.intersectionAnt(wordIndex, iWordIndex);
                            if (antonyms.isEmpty()) continue;
                            for (Integer antonymIndex: antonyms) {
                                SimpleMatrix antonymVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[antonymIndex]);
                                antonymError = antonymError.plus(SimpleMatrixUtils.cosineDerivative(wordVector, antonymVector));
                                countAnts = countAnts + 1;
                            }
                        }
                        
                        if (isSynonyms) {
                            Set<Integer> synonyms = lexicalVerb.intersectionSyn(wordIndex, iWordIndex);
                            if (synonyms.isEmpty()) continue;
                            for (Integer synonymIndex: synonyms) {
                                countSyns = countSyns + 1;
                                SimpleMatrix synonymVector = new SimpleMatrix(1, projectionLayerSize, true, weights0[synonymIndex]);
                                synonymError = synonymError.plus(SimpleMatrixUtils.cosineDerivative(wordVector, synonymVector));
                            }
                        }
                        if (countAnts == 0) countAnts = 1;
                        if (countSyns == 0) countSyns = 1;
                        gradient = (double)(alpha*r);
                        a1error_temp = a1error_temp.plus(synonymError.scale(countAnts));
                        a1error_temp = a1error_temp.minus(antonymError.scale(countSyns));
                        a1error_temp = a1error_temp.scale(gradient);
                        double[] errorArray = a1error_temp.getMatrix().data;
                            
                        // Learn weights input -> hidden
                            
                        for (int j = 0; j < projectionLayerSize; j++) {
                            weights0[wordIndex][j] += errorArray[j];
                            a1error[j] = 0;
                        }
                    }
                }
                  
            }
        }
    }
}