package space;

import io.word.WordFilter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import vocab.Vocab;
import common.DataStructureUtils;
import common.IOUtils;
import common.exception.ValueException;

public class RawSemanticSpace implements SemanticSpace{
    protected String[]                 words;
    protected HashMap<String, Integer> word2Index;
    protected double[][]                vectors;
    protected int                      vectorSize;

    public RawSemanticSpace(int wordNumber, int vectorSize) {
        vectors = new double[wordNumber][vectorSize];
        words = new String[wordNumber];
        word2Index = new HashMap<String, Integer>();
        this.vectorSize = vectorSize;
    }

    public RawSemanticSpace(List<String> wordList, List<double[]> vectorList) {
        words = DataStructureUtils.stringListToArray(wordList);
        word2Index = DataStructureUtils.arrayToMap(words);
        vectors = DataStructureUtils.arrayListTo2dArray(vectorList);
        vectorSize = vectors[0].length;
    }
    
    public RawSemanticSpace(String[] words, double[][] vectors) {
        this.words = words;
        this.vectors = vectors;
        vectorSize = vectors[0].length;
        word2Index = DataStructureUtils.arrayToMap(words);
    }
    
    public RawSemanticSpace(Vocab vocab, double[][] vectors, boolean copy){
        if (vocab.getVocabSize() != vectors.length) {
            throw new ValueException("vocab and vectors must have the same size");
        } else {
            
            vectorSize = vectors[0].length;
            
            if (!copy) {
                this.vectors = vectors;
            } else {
                this.vectors = vectors.clone();
            }
            
            int vocabSize = vocab.getVocabSize();
            words = new String[vocabSize];
            for (int i = 0; i < vocabSize; i++) {
                words[i] = vocab.getEntry(i).word;
            }
            
            word2Index = DataStructureUtils.arrayToMap(words);
        }
    }

    public static RawSemanticSpace readSpace(String vectorFile) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(
                    new BufferedInputStream(new FileInputStream(vectorFile)));
            String firstWord = readWord(inputStream);
            String secondWord = readWord(inputStream);
            int wordNumber = Integer.parseInt(firstWord);
            int vectorSize = Integer.parseInt(secondWord);
            RawSemanticSpace result = new RawSemanticSpace(wordNumber, vectorSize);
            result.readSpace(inputStream);
            inputStream.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void exportSpace(String textFile, boolean unknownWord) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(textFile));
            for (int i = 0; i < words.length; i++) {
                writer.write(words[i] + "\t");
                double[] vector = vectors[i];
                for (int j = 0; j < vectorSize; j++) {
                    writer.write("" + vector[j]);
                    if (j < vectorSize - 1) {
                        writer.write("\t");
                    } else {
                        writer.write("\n");
                    }
                }
            }
            if (unknownWord) {
                writer.write("UNK\t");
                for (int j = 0; j < vectorSize; j++) {
                    writer.write("0");
                    if (j < vectorSize - 1) {
                        writer.write("\t");
                    } else {
                        writer.write("\n");
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void exportSentimentSpace(String textFile, String wordFile, boolean printRawMat) {
        ArrayList<String> newWords = new ArrayList<String>();
        if (!printRawMat) {
            newWords.add("#UNKNOWN#");
        }
        newWords.addAll(Arrays.asList(words));
        IOUtils.printToFile(wordFile, newWords);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(textFile));
            if (!printRawMat) {
                writer.write("" + (words.length) + "\t" + vectorSize + "\n" );
            }
            for (int i = 0; i < words.length; i++) {
                if (!printRawMat) {
                    writer.write(words[i] + "\t");
                }
                double[] vector = vectors[i];
                for (int j = 0; j < vectorSize; j++) {
                    writer.write("" + vector[j]);
                    if (j < vectorSize - 1) {
                        writer.write("\t");
                    } else {
                        writer.write("\n");
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double[] parseVector(String[] elements, int startIndex) {
        double[] result = new double[elements.length - startIndex];
        for (int i = startIndex; i < elements.length; i++) {
            result[i - startIndex] = Float.parseFloat(elements[i]);
        }
        return result;
    }

    public static RawSemanticSpace importSpace(String textFile) {
        ArrayList<String> words = new ArrayList<String>();
        ArrayList<double[]> vectors = new ArrayList<double[]>();
        // int vectorSize = 0;
        try {

            BufferedReader reader = new BufferedReader(new FileReader(textFile));
            String line = reader.readLine();
            if (line != null && !line.equals("")) {
                String[] elements = line.split("( |\\t)");
                // vectorSize = elements.length - 1;
                double[] vector = parseVector(elements, 1);
                words.add(elements[0]);
                vectors.add(vector);

                line = reader.readLine();
                while (line != null && !line.equals("")) {
                    elements = line.split("( |\\t)");
                    vector = parseVector(elements, 1);
                    words.add(elements[0]);
                    vectors.add(vector);
                    line = reader.readLine();
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (words.size() == 0) {
            return null;
        } else {
            RawSemanticSpace result = new RawSemanticSpace(words, vectors);
            return result;
        }
    }
    
    

    public static String readWord(InputStream inputStream) throws IOException {
        StringBuffer buffer = new StringBuffer();
        while (true) {
            int nextByte = inputStream.read();
            if (nextByte == -1 || nextByte == ' ' || nextByte == '\n') {
                if (nextByte == -1 && buffer.length() == 0) {
                    return null;
                } else {
                    break;
                }
            } else {
                buffer.append((char) nextByte);
            }
        }
        return buffer.toString();
    }

    private void readSpace(InputStream inputStream) throws IOException {
        byte[] rowData = new byte[vectorSize * 4];
        ByteBuffer buffer = ByteBuffer.wrap(rowData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < words.length; i++) {
            String word = readWord(inputStream);
            words[i] = word;
            inputStream.read(rowData);
            for (int j = 0; j < vectorSize; j++) {
                vectors[i][j] = buffer.getFloat(j * 4);
            }
            word2Index.put(word, i);
            inputStream.read();
        }
    }
    
    public void saveSpace(String outputFile) {
        int vocabSize = words.length;

        try {
            BufferedOutputStream os = new BufferedOutputStream(
                    new FileOutputStream(outputFile));
            String firstLine = "" + vocabSize + " " + vectorSize
                    + "\n";
            os.write(firstLine.getBytes(Charset.forName("UTF-8")));
            // save vectors
            for (int i = 0; i < vocabSize; i++) {
                String word = words[i];
                os.write((word + " ").getBytes("UTF-8"));
                ByteBuffer buffer = ByteBuffer
                        .allocate(4 * vectorSize);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                for (int j = 0; j < vectorSize; j++) {
                    buffer.putFloat((float) vectors[i][j]);
                }
                os.write(buffer.array());
                os.write("\n".getBytes());
            }
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SimpleMatrix getVector(String word) {
        Integer index = word2Index.get(word);
        if (index == null) {
            return null;
        } else {
            return new SimpleMatrix(1, vectorSize, true, vectors[index]);
        }
    }
    
    public double getSim(String word1, String word2) {
        if (word2Index.containsKey(word1) && word2Index.containsKey(word2)) {
            int index1 = word2Index.get(word1);
            int index2 = word2Index.get(word2);
            return Similarity.cosine(vectors[index1], vectors[index2]);
        } else {
            return 0;
        }
    }

    public Neighbor[] getNeighbors(SimpleMatrix vector, int noNeighbor) {
        Neighbor[] neighbors = new Neighbor[words.length];
        double[] rawVector = vector.getMatrix().getData();
//        System.out.println("vocab size: " + words.length);
        for (int i = 0; i < words.length; i++) {
            neighbors[i] = new Neighbor(words[i], Similarity.cosine(rawVector,
                    vectors[i]));
        }
        Arrays.sort(neighbors, Neighbor.NeighborComparator);
        if (noNeighbor < words.length) {
            return Arrays.copyOfRange(neighbors, 0, noNeighbor);
        } else {
            return neighbors;
        }
    }

    public Neighbor[] getNeighbors(SimpleMatrix vector, int noNeighbor,
            String[] excludedWords) {
        double[] rawVector = vector.getMatrix().getData();
        Neighbor[] neighbors = new Neighbor[words.length - excludedWords.length];
        HashSet<String> excludedDict = DataStructureUtils
                .arrayToSet(excludedWords);
        int neighborIndex = 0;
        for (int i = 0; i < words.length; i++) {
            if (!excludedDict.contains(words[i])) {
                neighbors[neighborIndex] = new Neighbor(words[i],
                        Similarity.cosine(rawVector, vectors[i]));
                neighborIndex++;
            }
        }
        Arrays.sort(neighbors, Neighbor.NeighborComparator);
        if (noNeighbor < words.length) {
            return Arrays.copyOfRange(neighbors, 0, noNeighbor);
        } else {
            return neighbors;
        }
    }

    public Neighbor[] getNeighbors(String word, int noNeighbor) {
        SimpleMatrix  vector = getVector(word);
        if (vector == null) {
            return null;
        } else {
            return getNeighbors(vector, noNeighbor, new String[] { word });
        }
    }

    public static void printVector(double[] vector) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < vector.length; i++) {
            buffer.append(vector[i]);
            buffer.append(' ');
        }
        System.out.println(buffer.toString());
    }

    public boolean contains(String word) {
        return word2Index.containsKey(word);
    }

    public RawSemanticSpace getSubSpace(WordFilter filter) {
        ArrayList<String> newWordList = new ArrayList<String>();
        ArrayList<double[]> newVectors = new ArrayList<double[]>();
        for (int i = 0; i < words.length; i++) {
            if (!filter.isFiltered(words[i])) {
                newWordList.add(words[i]);
                newVectors.add(vectors[i]);
            }
        }
        return new RawSemanticSpace(newWordList, newVectors);
    }

    public RawSemanticSpace getSubCapSpace(Collection<String> wordList) {
        ArrayList<String> newWordList = new ArrayList<String>();
        ArrayList<double[]> newVectors = new ArrayList<double[]>();
        for (String word : wordList) {
            if (this.contains(word)) {
                newWordList.add(word);
                newVectors.add(this.getVector(word).getMatrix().getData());
            } else if (this.contains(word.toLowerCase())) {
                newWordList.add(word);
                newVectors.add(this.getVector(word.toLowerCase()).getMatrix().getData());
            }
        }
        return new RawSemanticSpace(newWordList, newVectors);
    }
    
    public RawSemanticSpace getSubSpace(Collection<String> wordList) {
        ArrayList<String> newWordList = new ArrayList<String>();
        ArrayList<double[]> newVectors = new ArrayList<double[]>();
        for (String word : wordList) {
            if (this.contains(word)) {
                newWordList.add(word);
                newVectors.add(this.getVector(word).getMatrix().getData());
            }
        }
        return new RawSemanticSpace(newWordList, newVectors);
    }

    public int getVectorSize() {
        return vectorSize;
    }
    
    public String[] getWords() {
        return words;
    }
    
    public double[][] getVectors() {
        return vectors;
    }
    
    public int getVocabSize() {
        return words.length;
    }
    

    public int findRank(SimpleMatrix vector, String word) {
        // TODO Auto-generated method stub
        Neighbor[] neighbors = new Neighbor[words.length];
        double[] rawVector = vector.getMatrix().getData();
//        System.out.println("vocab size: " + words.length);
        for (int i = 0; i < words.length; i++) {
            neighbors[i] = new Neighbor(words[i], Similarity.cosine(rawVector,
                    vectors[i]));
        }
        Arrays.sort(neighbors, Neighbor.NeighborComparator);
        for (int i = 0; i < words.length; i++) {
            if (neighbors[i].word.equals(word)) return i;
        }
        return -1;
    }
}
