package common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import common.exception.ValueException;

import tree.Tree;

/**
 * This class contains a set of utility IO method 
 */
public class IOUtils {

    /**
     * Print an integer array to standard out
     */
    public static void printInts(int[] array) {
//        System.out.println(array.length);
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            buffer.append(array[i]);
            buffer.append(' ');
        }
        System.out.println(buffer.toString());
    }

    /**
     * Print a long array to standard out
     */
    public static void printLongs(long[] array) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            buffer.append(array[i]);
            buffer.append(' ');
        }
        System.out.println(buffer.toString());
    }
    
    /**
     * Print a long array to standard out
     */
    public static void printDoubles(double[] array) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            buffer.append(array[i]);
            buffer.append(' ');
        }
        System.out.println(buffer.toString());
    }
    
    public static void printListPair(ArrayList<String[]> pairs, String outputFile) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        for (int i = 0; i < pairs.size(); i++) {
            String[] pair = pairs.get(i);
            writer.write(pair[0]);
            for (int j = 1; j < pair.length; j++) {
                writer.write("\t" + pair[j]);
            }
            writer.write("\n");
        }
        writer.close();
    }
    
    /**
     * Read all the lines of a file
     * @param inputFile: path to the input file
     * @return a list of string
     */
    public static ArrayList<String> readFile(String inputFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            ArrayList<String> slResult = new ArrayList<String>();
            String line = reader.readLine();
            while (line != null && !line.equals("")) {
                slResult.add(line);
                line = reader.readLine();
            }
            reader.close();
            return slResult;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        
    }
    
    /**
     * Read all the lines of a file
     * @param inputFile: path to the input file
     * @return a list of string
     */
    public static ArrayList<String[]> readTupleList(String inputFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            ArrayList<String[]> slResult = new ArrayList<String[]>();
            String line = reader.readLine();
            while (line != null && !line.equals("")) {
                slResult.add(line.split("\\s"));
                line = reader.readLine();
            }
            reader.close();
            return slResult;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        
    }
    
    /**
     * Print a list of strings to a file
     * @param outputFile: the path to the output file
     * @param strings
     */
    public static void printToFile(String outputFile, List<String> strings) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            for (String line: strings) {
                writer.write(line);
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static <T> void printToFile(String outputFile, T[] inputArray) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            for (T element: inputArray) {
                writer.write("" + element + "\n");
            }
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Save a 2d array to a BufferedOutputStream in either text format or binary
     * format
     * @param outputStream
     * @param matrix
     * @param binary
     */
    public static void saveMatrix(BufferedOutputStream outputStream, double[][] matrix, boolean binary) {
        // Save the word vectors
        // save number of words, length of each vector
        int numRow = matrix.length;
        int numColumn = matrix[0].length;
        try {
            String firstLine = "" + numRow + " " + numColumn
                    + "\n";
            outputStream.write(firstLine.getBytes(Charset.forName("UTF-8")));
            // save vectors
            for (int i = 0; i < matrix.length; i++) {
                if (binary) {
                    ByteBuffer buffer = ByteBuffer
                            .allocate(8 * numColumn);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    for (int j = 0; j < numColumn; j++) {
                        buffer.putDouble(matrix[i][j]);
                    }
                    outputStream.write(buffer.array());
                } else {
                    StringBuffer sBuffer = new StringBuffer();
                    for (int j = 0; j < numColumn; j++) {
                        sBuffer.append("" + matrix[i][j] + " ");
                    }
                    outputStream.write(sBuffer.toString().getBytes());
                }
                outputStream.write("\n".getBytes());
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String readWord(BufferedInputStream inputStream) throws IOException{
        StringBuffer buffer = new StringBuffer();
        while (true) {
            int nextByte = inputStream.read();
            if (nextByte == -1 || nextByte == ' ' || nextByte == '\n' || nextByte == '\t') {
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
    
    public static double[][] readMatrix(BufferedInputStream inputStream, boolean binary) {
        try {
            if (binary) {
                int numRows = Integer.parseInt(readWord(inputStream));
                int numCols = Integer.parseInt(readWord(inputStream));
                byte[] rowData = new byte[numCols * 8];
                ByteBuffer buffer = ByteBuffer.wrap(rowData);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                double[][] result = new double[numRows][numCols];
                for (int i = 0; i < numRows; i++) {
                    inputStream.read(rowData);
                    for (int j = 0; j < numCols; j++) {
                        result[i][j] = buffer.getDouble(j * 8);
                    }
                    inputStream.read();
                }
                return result;
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = reader.readLine();
                int numRows = Integer.parseInt(line.split(" ")[0]);
                int numCols = Integer.parseInt(line.split(" ")[1]);
                double[][] result = new double[numRows][numCols];
                for (int i = 0; i < numRows; i++) {
                    line = reader.readLine();
                    String[] elements = line.split(" ");
                    for (int j = 0; j < numCols; j++) {
                        result[i][j] = Double.parseDouble(elements[j]); 
                    }
                }
                return result;
            }  
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Save a 2d array to a file in either text format or binary format
     * @param matrixFile: the path to the output file
     * @param matrix: a 2d array contains the elements of the matrix
     * @param binary: the format of the output file (true if binary)
     */
    public static void saveMatrix(String matrixFile, double[][] matrix, boolean binary) {
        try {
            BufferedOutputStream os = new BufferedOutputStream(
                    new FileOutputStream(matrixFile));
            saveMatrix(os, matrix, binary);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Save a SimpleMatrix instance to an BufferedOutputStream
     * @param outputStream
     * @param matrix
     * @param binary
     */
    public static void saveMatrix(BufferedOutputStream outputStream, SimpleMatrix matrix, boolean binary) {
        saveMatrix(outputStream, SimpleMatrixUtils.to2DArray(matrix), binary);
    }
    
    public static HashMap<String, String> readConstructionGroup(String filePath) {
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();
            
            while (line != null && !"".equals(line)) {
                if (line.startsWith("#")) {
                    line = reader.readLine();
                    continue;
                }
                String[] elements = line.split("( |\\t)");
                String construction = elements[1] + " " + elements[2] + " " 
                                    + elements[3];
                String group = elements[0];
                result.put(construction, group);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public static void printConstructions(HashMap<String, String> constructionGroups) {
        for (String key: constructionGroups.keySet()) {
            System.out.println(key + " " + constructionGroups.get(key));
        }
        System.out.println("******************");
    }
    
    public static ArrayList<Double> readLog(String logfile, String variableName) {
        ArrayList<Double> result = new ArrayList<Double>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logfile));
            String line = reader.readLine();
            
            while (line != null && !"".equals(line)) {
                String[] elements = line.split(" ");
                if (elements.length >= 2) {
                    int lenght = elements.length;
                    if (elements[lenght - 2].startsWith(variableName)) {
                        result.add(new Double(elements[lenght - 1]));
                    }
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public static ArrayList<Tree> readTree(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            ArrayList<Tree> result = new ArrayList<Tree>();
            String line = reader.readLine();
            while (line != null && !line.equals("")) {
                try {
                    line = line.replaceAll("\\(\\)", "LRB)");
                    line = line.replaceAll(" \\)", " RRB");
                    result.add(Tree.fromPennTree(line));
                } catch (ValueException e) {
//                    e.printStackTrace();
                }
                line = reader.readLine();
            }
            reader.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
