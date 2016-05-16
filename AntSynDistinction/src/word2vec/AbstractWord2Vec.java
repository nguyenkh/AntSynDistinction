package word2vec;

import io.sentence.SentenceInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

import vocab.Vocab;
import vocab.VocabEntry;
import common.IOUtils;
import common.SigmoidTable;
import common.exception.ValueException;

/**
 * Abstract class of word2vec
 * Define common attributes and methods for learning word vectors
 *
 */

public abstract class AbstractWord2Vec {
    public static final double DEFAULT_STARTING_ALPHA      = 0.025;
    public static final int    DEFAULT_MAX_SENTENCE_LENGTH = 1000;

    protected double           starting_alpha;
    protected double           alpha;

    /*
     * objective function options: - hierarchicalSoftmax:
     * log(p(output_word_code|input_words)) - negativeSampling
     */
    protected boolean          hierarchicalSoftmax;
    protected int              negativeSamples;

    protected double            subSample;

    // projection/hidden layer size
    protected int              projectionLayerSize;

    protected int              windowSize;

    protected Vocab            vocab;

    // parameters to keep track of the training progress
    protected long             wordCount;
    protected long             trainWords;

    // uniGram language model, used in negativeSampling method
    protected UniGram          unigram;
    // pre-computed sigmoid Table for fast computing sigmoid
    // while losing a bit of precision
    protected SigmoidTable     sigmoidTable;

    /*
     * weights of neural network: - weights0: projection matrices size = V * H,
     * only w * 2 * H are estimated at a time - weights1: projection layer to
     * output layer: hierarchical softmax size = H * (V - 1), only H * log(V)
     * are estimated - negativeWeights1: projection layer to output layer:
     * negative Sampling size = H * V, only (k + 1) * V are estimated, where k =
     * negativeSamples
     */
    protected double[][]                  weights0, weights1, negativeWeights1;

    // Random instance
    // for randomize window size, initial weights, subsampling probability
    // and negative samples
    protected Random                     rand;
    
    public AbstractWord2Vec(int projectionLayerSize, int windowSize,
            boolean hierarchicalSoftmax, int negativeSamples, double subSample) {
        this.projectionLayerSize = projectionLayerSize;
        this.hierarchicalSoftmax = hierarchicalSoftmax;
        this.windowSize = windowSize;
        this.subSample = subSample;
        this.negativeSamples = negativeSamples;

        this.rand = new Random();
        sigmoidTable = new SigmoidTable();
        // TODO: setting alpha
        starting_alpha = DEFAULT_STARTING_ALPHA;
        alpha = starting_alpha;
    }

    protected void saveProjectionMatrices(String weightFile) {
        try {
            BufferedOutputStream outStream = new BufferedOutputStream(
                    new FileOutputStream(weightFile));
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(vocab.getVocabSize());
            buffer.putInt(projectionLayerSize);
            outStream.write(buffer.array());
            for (int i = 0; i < vocab.getVocabSize(); i++) {
                buffer = ByteBuffer.allocate(4 * projectionLayerSize);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                for (int j = 0; j < projectionLayerSize; j++) {
                    buffer.putFloat((float) weights0[i][j]);
                }
                outStream.write(buffer.array());
            }
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void loadProjectionMatrices(String weightFile) {
        try {
            BufferedInputStream inStream = new BufferedInputStream(
                    new FileInputStream(weightFile));
            byte[] array = new byte[4];
            inStream.read(array);
            ByteBuffer buffer = ByteBuffer.wrap(array);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            int newVocabSize = buffer.getInt(0);
            inStream.read(array);
            int newLayer1Size = buffer.getInt(0);
            System.out.println("new vocab size:" + newVocabSize);
            System.out.println("new layer1 size:" + newLayer1Size);
            for (int i = 0; i < vocab.getVocabSize(); i++) {
                for (int j = 0; j < projectionLayerSize; j++) {
                    inStream.read(array);
                    weights0[i][j] = buffer.getFloat(0);
                }
            }
            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected void initBare() {
        int vocabSize = vocab.getVocabSize();
        if (negativeSamples > 0) {
            unigram = new UniGram(vocab);
        }
        weights0 = new double[vocabSize][projectionLayerSize];
        if (hierarchicalSoftmax) {
            weights1 = new double[vocabSize - 1][projectionLayerSize];
        }
        if (negativeSamples > 0) {
            negativeWeights1 = new double[vocabSize][projectionLayerSize];
//            negativeWeights1 = weights0;
        }
        vocab.assignCode();
    }

    public void initNetwork() {
        initBare();
        randomInitProjectionMatrices();
        
    }

    public void initNetwork(String initFile) {
        initBare();
        boolean readInit = (new File(initFile)).exists();
        if (!readInit) {
            System.out.println("Init file does not exist, using random initialization");
            randomInitProjectionMatrices();
            saveNetwork(initFile, true);
        } else {
            loadNetwork(initFile, true);
        }
    }

    /**
     * randomly initialize the input to hidden weights The weight should be from
     * [-0.5 / hidden_layer_size, 0.5 / hidden_layer_size]
     */
    protected void randomInitProjectionMatrices() {
        for (int i = 0; i < vocab.getVocabSize(); i++) {
            for (int j = 0; j < projectionLayerSize; j++) {
                weights0[i][j] = (double) (rand.nextFloat() - 0.5)
                        / projectionLayerSize;
            }
        }
    }

    public void saveVector(String outputFile, boolean binary) {
        // Save the word vectors
        // save number of words, length of each vector
        int vocabSize = vocab.getVocabSize();
        try {
            BufferedOutputStream os = new BufferedOutputStream(
                    new FileOutputStream(outputFile));
            String firstLine = "" + vocabSize + " " + projectionLayerSize
                    + "\n";
            os.write(firstLine.getBytes(Charset.forName("UTF-8")));
            // save vectors
            for (int i = 0; i < vocabSize; i++) {
                VocabEntry word = vocab.getEntry(i);
                os.write((word.word + " ").getBytes("UTF-8"));
                if (binary) {
                    ByteBuffer buffer = ByteBuffer
                            .allocate(4 * projectionLayerSize);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    for (int j = 0; j < projectionLayerSize; j++) {
                        buffer.putFloat((float) weights0[i][j]);
                    }
                    os.write(buffer.array());
                } else {
                    StringBuffer sBuffer = new StringBuffer();
                    for (int j = 0; j < projectionLayerSize; j++) {
                        sBuffer.append("" + weights0[i][j] + " ");
                    }
                    os.write(sBuffer.toString().getBytes());
                }
                os.write("\n".getBytes());
            }
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveNegVectors(String outputFile, boolean binary) {
        // Save the word vectors
        // save number of words, length of each vector
        int vocabSize = vocab.getVocabSize();
        if (negativeSamples <= 0) 
            throw new ValueException("cannot store negative vectors for hierarchical softmax");
        try {
            BufferedOutputStream os = new BufferedOutputStream(
                    new FileOutputStream(outputFile));
            String firstLine = "" + vocabSize + " " + projectionLayerSize
                    + "\n";
            os.write(firstLine.getBytes(Charset.forName("UTF-8")));
            // save vectors
            for (int i = 0; i < vocabSize; i++) {
                VocabEntry word = vocab.getEntry(i);
                os.write((word.word + " ").getBytes("UTF-8"));
                if (binary) {
                    ByteBuffer buffer = ByteBuffer
                            .allocate(4 * projectionLayerSize);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    for (int j = 0; j < projectionLayerSize; j++) {
                        buffer.putFloat((float) negativeWeights1[i][j]);
                    }
                    os.write(buffer.array());
                } else {
                    StringBuffer sBuffer = new StringBuffer();
                    for (int j = 0; j < projectionLayerSize; j++) {
                        sBuffer.append("" + negativeWeights1[i][j] + " ");
                    }
                    os.write(sBuffer.toString().getBytes());
                }
                os.write("\n".getBytes());
            }
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
 
    public void setVocab(Vocab vocab) {
        this.vocab = vocab;
    }
    
    public void saveNetwork(String outputFile, boolean binary) {
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
            IOUtils.saveMatrix(outputStream, weights0, binary);
            if (hierarchicalSoftmax) {
                IOUtils.saveMatrix(outputStream, weights1, binary);
            }
            if (negativeSamples > 0) {
                IOUtils.saveMatrix(outputStream, negativeWeights1, binary);
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public void loadNetwork(String inputFile, boolean binary) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            double[][] matrix = IOUtils.readMatrix(inputStream, binary);
            if (matrix.length != vocab.getVocabSize() || matrix[0].length != projectionLayerSize) {
                System.out.println("matrix size does not match");
            } else {
                weights0 = matrix;
            }
            
            if (hierarchicalSoftmax) {
                matrix = IOUtils.readMatrix(inputStream, binary);
                if (matrix.length != vocab.getVocabSize() -1 || matrix[0].length != projectionLayerSize) {
                    System.out.println("matrix size does not match");
                } else {
                    weights1 = matrix;
                }
            }
            if (negativeSamples > 0) {
                matrix = IOUtils.readMatrix(inputStream, binary);
                System.out.println("matrix size: " + matrix.length + " " + matrix[0].length);
                System.out.println("expect size: " + vocab.getVocabSize() + " " + projectionLayerSize);
                if (matrix.length != vocab.getVocabSize() || matrix[0].length != projectionLayerSize) {
                    System.out.println("matrix size does not match");
                } else {
                    negativeWeights1 = matrix;
                }
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void trainModel(ArrayList<SentenceInputStream> inputStreams);

}
