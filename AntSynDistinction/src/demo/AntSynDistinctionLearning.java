package demo;

import io.sentence.PlainSentenceInputStream;
import io.word.CombinedWordInputStream;
import io.word.PushBackWordStream;
import io.word.WordInputStream;
import io.sentence.SentenceInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.HashSet;

import common.IOUtils;
import common.exception.ValueException;
import common.wordnet.LexicalResourceAdj;
import common.wordnet.LexicalResourceNoun;
import common.wordnet.LexicalResourceVerb;
import vocab.Vocab;
import word2vec.MultiThreadWord2Vec;
import word2vec.multitask.WeightSAWord2Vec;


public class AntSynDistinctionLearning {
    public static void main(String[] args) throws IOException{
        
        
        MultiThreadWord2Vec word2vec = null;
        String configFile = args[0]; 
        int size = Integer.parseInt(args[1]);
        boolean adj = Boolean.parseBoolean(args[2]);
        boolean noun = Boolean.parseBoolean(args[3]);
        boolean verb = Boolean.parseBoolean(args[4]);
        int iter = Integer.parseInt(args[5]);
        
        String forbiddenWordFile = null;
        if (args.length == 7) {
            forbiddenWordFile = args[6];
        }
        W2vProperties properties = new W2vProperties(configFile);
        boolean softmax = Boolean.parseBoolean(properties.getProperty("HierarchialSoftmax"));
        int negativeSamples = Integer.parseInt(properties.getProperty("NegativeSampling"));
        double subSampling = Double.parseDouble(properties.getProperty("SubSampling"));
        String trainDirPath = properties.getProperty("TrainDir");
        String outputFile = properties.getProperty("WordVectorFile");
        String vocabFile = properties.getProperty("VocabFile");
        //String menFile = properties.getProperty("MenFile");
        outputFile = outputFile.replaceAll(".bin", "_" + size + ".bin");
        
        File trainDir = new File(trainDirPath);
        File[] trainFiles = trainDir.listFiles();
        System.out.println("Starting training using dir " + trainDirPath);
        System.out.println("Output file: " + outputFile);

        boolean learnVocab = !(new File(vocabFile)).exists();
        Vocab vocab = new Vocab(Integer.parseInt(properties.getProperty("MinFrequency")));
        if (!learnVocab)
            vocab.loadVocab(vocabFile);// ,minFrequency);
        else {
            ArrayList<WordInputStream> wordStreamList = new ArrayList<>();
            for (File trainFile: trainFiles) {
                WordInputStream wordStream = new PushBackWordStream(trainFile.getAbsolutePath(), 200);
                wordStreamList.add(wordStream);
            }
          
            CombinedWordInputStream wordStream = new CombinedWordInputStream(wordStreamList);
            vocab.learnVocabFromTrainStream(wordStream);
            // save vocabulary
            vocab.saveVocab(vocabFile);
        }
        
        word2vec = new WeightSAWord2Vec(size, 5, softmax, negativeSamples, 5, subSampling, iter);
        if (!(noun || adj || verb)) {
            throw new ValueException("should train with at least one lexical resource");
        } else {
            
            WeightSAWord2Vec antoWord2Vec = (WeightSAWord2Vec) word2vec;
            outputFile = outputFile.replaceAll(".bin", "_antsyn.bin");
            HashSet<String> forbiddenSet = new HashSet<String>();
            if (forbiddenWordFile != null) {
                ArrayList<String> forbiddenWords = IOUtils.readFile(forbiddenWordFile);
                forbiddenSet = new HashSet<String>(forbiddenWords);
                outputFile = outputFile.replaceAll(".bin", "_train.bin");
            }
            antoWord2Vec.setForbiddenWords(forbiddenSet);
            
            if (noun) {
                LexicalResourceNoun lexicalNoun = new LexicalResourceNoun(properties.getProperty("antNoun"), 
                        properties.getProperty("synNoun"),
                        properties.getProperty("featureNoun"), vocab);
                antoWord2Vec.setLexicalNoun(lexicalNoun);
                outputFile = outputFile.replaceAll(".bin", "_noun.bin");
            }
            if (adj) {
                LexicalResourceAdj lexicalAdj = new LexicalResourceAdj(properties.getProperty("antAdj"),
                        properties.getProperty("synAdj"),
                        properties.getProperty("featureAdj"), vocab);
                antoWord2Vec.setLexicalAdj(lexicalAdj);
                outputFile = outputFile.replaceAll(".bin", "_adj.bin");
            }
            if (verb) {
                LexicalResourceVerb lexicalVerb = new LexicalResourceVerb(properties.getProperty("antVerb"),
                        properties.getProperty("synVerb"),
                        properties.getProperty("featureVerb"), vocab);
                antoWord2Vec.setLexicalVerb(lexicalVerb);
                outputFile = outputFile.replaceAll(".bin", "_verb.bin");  
            }
            
        }        

        word2vec.setVocab(vocab);
        word2vec.initNetwork();

        System.out.println("Start training");
        try {
            ArrayList<SentenceInputStream> inputStreams = new ArrayList<SentenceInputStream>();
            for (File trainFile: trainFiles) {
                SentenceInputStream sentenceInputStream = new PlainSentenceInputStream(
                    new PushBackWordStream(trainFile.getAbsolutePath(), 200));
                inputStreams.add(sentenceInputStream);
            }
            word2vec.trainModel(inputStreams); //pass the list of sentences in the corpus
            word2vec.saveVector(outputFile, true);
            System.out.println("The vocab size: " + vocab.getVocabSize() + " words");
        } catch (IOException e) {
            System.exit(1);
        }

    }
}
