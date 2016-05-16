package common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import neural.function.ActivationFunction;

import org.apache.commons.lang.ArrayUtils;
import org.ejml.alg.dense.mult.MatrixDimensionException;
import org.ejml.simple.SimpleMatrix;

/**
 * This class provides some utility method for the SimpleMatrix class
 *
 */
public class SimpleMatrixUtils {
    
    /**
     * Return a matrix which consist of the values of sigmoid function of every
     * element of the input matrix
     * The sigmoid values are taken from an instance of SigmoidTable class
     * @param inputMatrix
     * @param sigmoidTable
     * @return
     */
    public static SimpleMatrix elementwiseApplySigmoid(SimpleMatrix inputMatrix, SigmoidTable sigmoidTable) {
        double[][] matrix = new double[inputMatrix.numRows()][inputMatrix.numCols()];
        for (int i = 0; i < inputMatrix.numRows(); i++)
            for (int j = 0; j < inputMatrix.numCols(); j++) {
                matrix[i][j] = sigmoidTable.getSigmoid(inputMatrix.get(i,j));
            }
        return new SimpleMatrix(matrix);
    }
    
    public static SimpleMatrix elementwiseApplyTanh(SimpleMatrix inputMatrix, TanhTable tanhTable) {
        double[][] matrix = new double[inputMatrix.numRows()][inputMatrix.numCols()];
        for (int i = 0; i < inputMatrix.numRows(); i++)
            for (int j = 0; j < inputMatrix.numCols(); j++) {
                matrix[i][j] = tanhTable.getTanh(inputMatrix.get(i,j));
            }
        return new SimpleMatrix(matrix);
    }
    
    public static SimpleMatrix elementwiseApplyTanhDerivative(SimpleMatrix inputMatrix, TanhTable tanhTable) {
        double[][] matrix = new double[inputMatrix.numRows()][inputMatrix.numCols()];
        for (int i = 0; i < inputMatrix.numRows(); i++)
            for (int j = 0; j < inputMatrix.numCols(); j++) {
                double tanh = tanhTable.getTanh(inputMatrix.get(i,j));
                matrix[i][j] = 1 - (tanh * tanh); 
            }
        return new SimpleMatrix(matrix);
    }
    
    /**
     * Stack two matrices vertically
     * @param matrix1
     * @param matrix2
     * @return
     * @throws MatrixDimensionException
     */
    public static SimpleMatrix vStack(SimpleMatrix matrix1, SimpleMatrix matrix2) throws MatrixDimensionException{
        if (matrix1.numCols() != matrix2.numCols()) {
            throw new MatrixDimensionException("Number of columns do not match");
        }
        int numCols = matrix1.numCols();
        int numRows1 = matrix1.numRows();
        int numRows2 = matrix2.numRows();
        double[] newData = new double[numCols * (numRows1 + numRows2)];
        System.arraycopy(matrix1.getMatrix().data, 0, newData, 0, numCols * numRows1);
        System.arraycopy(matrix2.getMatrix().data, 0, newData, numCols * numRows1, numCols * numRows2);
        return new SimpleMatrix(numRows1 + numRows2, numCols, true, newData);
    }
    
    /**
     * Stack two matrices horizontally
     * @param matrix1
     * @param matrix2
     * @return
     * @throws MatrixDimensionException
     */
    public static SimpleMatrix hStack(SimpleMatrix matrix1, SimpleMatrix matrix2) throws MatrixDimensionException{
        if (matrix1.numRows() != matrix2.numRows()) {
            throw new MatrixDimensionException("Number of rows do not match");
        }
        int numCols1 = matrix1.numCols();
        int numCols2 = matrix2.numCols();
        int numRows = matrix1.numRows();
        double[] newData = new double[numRows * (numCols1 + numCols2)];
        System.arraycopy(matrix1.transpose().getMatrix().data, 0, newData, 0, numRows * numCols1);
        System.arraycopy(matrix2.transpose().getMatrix().data, 0, newData, numRows * numCols1, numRows * numCols2);
        return new SimpleMatrix(numRows, numCols1 + numCols2, false, newData);
    }
    
    /**
     * Return the data of a SimpleMatrix as a 2d array
     * (Since the internal structure of a SimpleMatrix is a 1d array, not 2d) 
     * @param matrix
     * @return
     */
    public static double[][] to2DArray(SimpleMatrix matrix) {
        double[] oneDArray = matrix.getMatrix().data;
        int numRows = matrix.numRows();
        int numCols = matrix.numCols();
        double[][] result = new double[numRows][numCols];
        for (int i = 0; i < result.length; i++) {
            System.arraycopy(oneDArray, i * numCols, result[i], 0, numCols);
        }
        return result;
    }
    
    public static SimpleMatrix applyActivationFunction(SimpleMatrix input, ActivationFunction activation) {
        double[] data = input.getMatrix().getData();
        double[] newData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            newData[i] = activation.activation(data[i]);
        }
        return new SimpleMatrix(input.numRows(), input.numCols(), true, newData);
    }
    
    // TODO: turn this into a UnitTest
    public static void main(String[] args) {
        SimpleMatrix matrix1 = new SimpleMatrix(2,2,true, new double[]{1,2,3,4});
        SimpleMatrix matrix2 = new SimpleMatrix(2,2,false, new double[]{5,6,7,8});
        System.out.println("mat 1" + matrix1);
        System.out.println("mat 2" + matrix2);
        System.out.println("vstacked" + vStack(matrix1, matrix2));
        System.out.println("hstacked" + hStack(matrix1, matrix2));
    }

    public static SimpleMatrix applyDerivative(SimpleMatrix input, ActivationFunction activation) {
        double[] data = input.getMatrix().getData();
        double[] newData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            newData[i] = activation.derivative(data[i]);
        }
        return new SimpleMatrix(input.numRows(), input.numCols(), true, newData);
    }
    
    public static SimpleMatrix getRows(SimpleMatrix originalMatrix, int[] indices) {
        int numCols = originalMatrix.numCols();
        double[] originalData = originalMatrix.getMatrix().getData();
        double[] destinationData = new double[indices.length * numCols];
        for (int i = 0;i < indices.length; i++) {
            int index = indices[i];
            System.arraycopy(originalData, numCols * index, destinationData, 
                    i * numCols, numCols);
        }
        return new SimpleMatrix(indices.length, numCols, true, destinationData);
    }
    
    public static SimpleMatrix concatenateVectors(List<SimpleMatrix> vectors) {
        // TODO: check not concatenating matrices
        int length = 0;
        for (SimpleMatrix vector: vectors) length += vector.numRows();
        double[] newData = new double[length];
        int pos = 0; 
        for (SimpleMatrix vector: vectors) {
            int vectorLength = vector.numRows();
            System.arraycopy(vector.getMatrix().getData(), 0, newData, pos, vectorLength);
            pos += vectorLength;
        }
        return new SimpleMatrix(length,1,true,newData);
    }
    
    public static SimpleMatrix extractPartialVector(SimpleMatrix original, int partNum, int pos) {
        int length = original.numRows();
        int partLength = length / partNum;
        double[] newData = new double[partLength];
        System.arraycopy(original.getMatrix().getData(), pos * partLength, newData, 0, partLength);
        return new SimpleMatrix(partLength, 1, true, newData);
    }
    
    public static double cosine(SimpleMatrix v1, SimpleMatrix v2) {
        double normF1 = v1.normF();
        double normF2 = v2.normF();
        if (normF1 == 0 || normF2 == 0) return 0;
        return v1.dot(v2) / (normF1 * normF2);
    }
    
    // row vector
    public static SimpleMatrix massCosine(SimpleMatrix matrix, SimpleMatrix vector) {
        double normFV = vector.normF();
        if (normFV == 0) {
            return new SimpleMatrix(matrix.numRows(),1);
        }
        SimpleMatrix vectorLenghts = elementSqrt(sumRow(matrix.elementMult(matrix)));
        SimpleMatrix result = matrix.mult(vector.transpose());
        result = result.elementMult(elementInverse(vectorLenghts));
        result = result.scale(1 / normFV);
        return result.transpose();
        
    }
    
    public static SimpleMatrix sumRow(SimpleMatrix input) {
        double[] data = input.getMatrix().getData();
        int numRows = input.numRows();
        int numCols = input.numCols();
        double[] newData = new double[numRows];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++)
                newData[i] += data[i * numCols + j];
        }
        return new SimpleMatrix(numRows,1,true, newData);
    }
    
    public static SimpleMatrix elementInverse(SimpleMatrix input) {
        double[] data = input.getMatrix().getData();
        int numRows = input.numRows();
        int numCols = input.numCols();
        double[] newData = new double[data.length];
        for (int i = 0; i < newData.length; i++) {
            newData[i] = 1 / data[i];
        }
        return new SimpleMatrix(numRows, numCols, true, newData);
    }
    
    public static SimpleMatrix elementSqrt(SimpleMatrix input) {
        double[] data = input.getMatrix().getData();
        int numRows = input.numRows();
        int numCols = input.numCols();
        double[] newData = new double[data.length];
        for (int i = 0; i < newData.length; i++) {
            newData[i] = Math.sqrt(data[i]);
        }
        return new SimpleMatrix(numRows, numCols, true, newData);
    }
    
    
    // TODO: can be rewrite so that it doesn't require extra library
    public static double elementMax(SimpleMatrix input) {
        double[] matData = input.getMatrix().data;
        List<Double> dataAsList = Arrays.asList(ArrayUtils.toObject(matData));
        return Collections.max(dataAsList);
    }
    
    public static double elementMin(SimpleMatrix input) {
        double[] matData = input.getMatrix().data;
        List<Double> dataAsList = Arrays.asList(ArrayUtils.toObject(matData));
        return Collections.min(dataAsList);
    }
    
    // TODO: optimize this (plus for inplace?)
    public static SimpleMatrix rowNormalize(SimpleMatrix input) {
        int numCols = input.numCols();
        int numRows = input.numRows();
        SimpleMatrix output = new SimpleMatrix(numRows, numCols);
        for (int i = 0; i < input.numRows(); i++) {
            SimpleMatrix row = input.extractMatrix(i, i + 1, 0, numCols);
            double length = row.normF();
            if (length != 0) {
                row = row.scale(1 / length);
                output.setRow(i, 0, row.getMatrix().getData());
            }
        }
        return output;
    }
    
    /**
     * 
     * @param input
     * @return
     */
    public static SimpleMatrix normalize(SimpleMatrix matrix, double wantedNormF) {
        double normF = matrix.normF();
        if (normF > wantedNormF) {
            matrix = matrix.scale(wantedNormF / normF);
        }
        return matrix;
    }
    
    public static void checkNaN(SimpleMatrix input) {
        //TODO: remove checkNaN every where
//        int size = input.numCols() * input.numRows();
//        double[] data = input.getMatrix().data;
//        for (int i = 0; i < size; i++) {
//            if (Double.isNaN(data[i])) {
//                throw new ValueException("NaN");
//            }
//            if (Double.isInfinite(data[i])) {
//                throw new ValueException("Inf");
//            }
//        }
    }
    
    public static boolean checkValueInRange(SimpleMatrix input, double lowerBound, double upperBound) {
        int size = input.numCols() * input.numRows();
        double[] data = input.getMatrix().data;
        for (int i = 0; i < size; i++) {
            if (data[i] < lowerBound || data[i] > upperBound) {
                return false;
            }
        }
        return true;
    }
    
    public static SimpleMatrix createUniformMatrix(int numRows, int numCols, double value) {
        SimpleMatrix result = new SimpleMatrix(numRows, numCols);
        double[] rawData = result.getMatrix().data;
        for (int i = 0; i < rawData.length; i++) {
            rawData[i] = value;
        }
        return result;
    }
    
    public static SimpleMatrix paddOne2ColumnVector(SimpleMatrix input, int numOne) {
        double[] rawData = input.getMatrix().data;
        int length = rawData.length;
        int newLength = length + numOne;
        SimpleMatrix result = new SimpleMatrix(newLength, 1);
        double[] resultData = result.getMatrix().data;
        for (int i = 0; i < newLength; i++) {
            System.arraycopy(rawData, 0, resultData, 0, length);
        }
        for (int i = 0; i < numOne; i++) {
            resultData[length + i] = 1;
        }
        return result;
    }
    
    public static SimpleMatrix sumSplit(SimpleMatrix input, int numSplit) {
        
        double[] rawData = input.getMatrix().data;
        int length = rawData.length;
        int newLength = length / numSplit;
        SimpleMatrix result = new SimpleMatrix(newLength, 1);
        double[] resultData = result.getMatrix().data;
        for (int i = 0; i < newLength; i++) {
            for (int j = 0; j < numSplit; j++)
            resultData[i] += rawData[i + j*newLength];
        }
        return result;
    }
    
    public static SimpleMatrix duplicateRows(SimpleMatrix input, int numDup) {
        double[] rawData = input.getMatrix().data;
        int length = rawData.length;
        SimpleMatrix result = new SimpleMatrix(input.numRows() * numDup, input.numCols());
        double[] resultData = result.getMatrix().data;
        for (int i = 0; i < numDup; i++) {
            System.arraycopy(rawData, 0, resultData, i * length, length);
        }
        return result;
    }
    
    public static SimpleMatrix cosineDerivative(SimpleMatrix x, SimpleMatrix a) {
        double lengthX = x.normF();
        double lengthA = a.normF();
        double dotP = x.mult(a.transpose()).get(0,0);
        double rToScaleA = 1 / (lengthX * lengthA);
        double rToScaleX = dotP / (lengthA * lengthX * lengthX * lengthX);
        SimpleMatrix result = (a.scale(rToScaleA)).minus(x.scale(rToScaleX));
        
        return result;
    }
    
    public static double cosineSim(SimpleMatrix u, SimpleMatrix v) {
        double z2 = u.dot(v);
        return z2;
    }
}
