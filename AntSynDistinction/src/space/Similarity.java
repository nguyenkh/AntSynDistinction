package space;

import org.ejml.simple.SimpleMatrix;

import common.MathUtils;
import common.SimpleMatrixUtils;

public class Similarity {
    public static double cosine(double[] v1, double[] v2) {
        return MathUtils.cosine(v1, v2);
    }
    
    public static double cosine(SimpleMatrix v1, SimpleMatrix v2) {
        return SimpleMatrixUtils.cosine(v1, v2);
    }
    
    public static SimpleMatrix massCosine(SimpleMatrix matrix, SimpleMatrix vector) {
        return SimpleMatrixUtils.massCosine(matrix, vector);
    }
}
