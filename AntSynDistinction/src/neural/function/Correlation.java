package neural.function;

import java.util.Random;

import common.MathUtils;
import common.exception.ValueException;

public class Correlation {
//    double[] predicted;
    String name = "correlation";
    double[] gold;
    int length;
    double aveY;
    double aveY2;
//    public train
    public Correlation(double[] gold) {
//        this.predicted = predicted;
        this.gold = gold;
        precompute();
    }
    
    protected void precompute() {
        aveY = 0;
        aveY2 = 0;
        length = gold.length;
        
        for (int i = 0; i < gold.length; i++) {
            aveY += gold[i];
            aveY2 += gold[i] * gold[i];
        }
        aveY /= length;
        aveY2 /= length;
    }
    
    public Correlation(double[][] vectors, int[][] pairs) {
        gold = new double[pairs.length];
        for (int i = 0; i < pairs.length; i++) {
            gold[i] = MathUtils.cosine(vectors[pairs[i][0]], vectors[pairs[i][1]]);
        }
        precompute();
    }
    
    
    public double[] derivative(double[] predicted) {
        if (length != predicted.length) {
            throw new ValueException("Value must be the same");
        }
        double[] result = new double[length];
        double aveX = 0;
        double aveX2 = 0;
        double aveXY =0;
        for (int i = 0; i < gold.length; i++) {
            aveX += predicted[i];
            aveX2 += predicted[i] * predicted[i];
            aveXY += predicted[i] * gold[i];
        }
        aveX /= length;
        aveX2 /= length;
        aveXY /= length;
        double ave2X = aveX * aveX;
        double ave2Y = aveY * aveY;
        double covXY = (aveXY - (aveX * aveY));
        double covX = (aveX2 - ave2X);
        double covY = (aveY2 - ave2Y);
        double sCovX = Math.sqrt(covX);
        double sCovY = Math.sqrt(covY);
        
        double correlation =  covXY / (sCovX * sCovY);
        for (int i = 0; i < length; i++) {
            result[i] = 1 / (covX * sCovY);
            result[i] *= (((gold[i] - aveY) * sCovX) - ((covXY / sCovY) * (predicted[i] - aveX))) / length;
        }
            
        System.out.println(name + ": " + correlation);
        return result;
    }
    
    public double[][] derivative(double[][] vectors, int[][] pairs) {
        int vocabSize = vectors.length;
        int vectorSize = vectors[0].length;
        double[][] result = new double[vocabSize][vectorSize];
        double[] cosines = new double[gold.length];
        for (int i = 0; i < pairs.length; i++) {
            cosines[i] = MathUtils.cosine(vectors[pairs[i][0]], vectors[pairs[i][1]]);
        }
        double[] cosDerivative = derivative(cosines);
        for (int i = 0; i < pairs.length; i++) {
            int index1 = pairs[i][0];
            int index2 = pairs[i][1];
            // TODO: optimize here
            double[] deltaX1 = MathUtils.cosineDerivative(vectors[index1], vectors[index2]);
            double[] deltaX2 = MathUtils.cosineDerivative(vectors[index2], vectors[index1]);
            for (int j = 0; j < vectorSize; j++) {
                result[index1][j] += cosDerivative[i] * deltaX1[j];
                result[index2][j] += cosDerivative[i] * deltaX2[j];
            }
        }
        
        return result;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public static void testPearsonDerivative() {
        Random random = new Random();
        int arrayLength = 1000;
        double[] gold = new double[arrayLength];
        double[] predicted = new double[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            gold[i] = random.nextDouble();
            predicted[i] = random.nextDouble();
        }
        double alpha = 1;
        int iteration = 1000;
        Correlation cor = new Correlation(gold);
        for (int i = 0; i < iteration; i++) {
            double[] derivative = cor.derivative(predicted);
            for (int j = 0; j < derivative.length; j++) {
                predicted[j] += alpha * derivative[j];
            }
        }
    }
    
    public static void testPearsonCosDerivative() {
        Random random = new Random();
        int arrayLength = 3000;
        double[] gold = new double[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            gold[i] = random.nextDouble();
        }
        Correlation cor = new Correlation(gold);
        int vectorNum = 1000;
        int[][] pairs = new int[arrayLength][2];
        int index = 0;
        while (index < arrayLength) {
            int i = random.nextInt(vectorNum);
            int j = random.nextInt(vectorNum);
            if (i == j) continue;
            pairs[index][0] = i;
            pairs[index][1] = j;
            index++;
        }
        int vectorSize = 100;
        double[][] vectors = new double[vectorNum][vectorSize]; 
        for (int i = 0; i < vectorNum; i++) {
            for (int j = 0; j < vectorSize; j++) {
                vectors[i][j] = random.nextDouble();
            }
        }
        
        int iteration = 10000; 
        double alpha = 1;
        for (int iter = 0; iter < iteration; iter++) {
            double[][] delta = cor.derivative(vectors, pairs);
            for (int i = 0; i < vectorNum; i++) {
                for (int j = 0; j < vectorSize; j++) {
                    vectors[i][j] += alpha * delta[i][j];
                }
            }
        }
    }
    
    public static void main(String[] args) {
//        testPearsonDerivative();
        testPearsonCosDerivative();
    }
    
    
    
}

