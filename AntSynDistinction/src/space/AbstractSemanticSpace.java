package space;

import org.ejml.simple.SimpleMatrix;

import common.SimpleMatrixUtils;
import common.exception.OutOfVocabularyException;

public abstract class AbstractSemanticSpace implements SemanticSpace{

    @Override
    public double getSim(String word1, String word2) {
        // TODO Auto-generated method stub
        SimpleMatrix vector1 = getVector(word1);
        SimpleMatrix vector2 = getVector(word2);
        if (vector1 == null) {
            throw new OutOfVocabularyException(word1 +" not found");
        } else if (vector2 == null) {
            throw new OutOfVocabularyException(word2 +" not found");
        }
        return SimpleMatrixUtils.cosine(vector1, vector2);
    }

}
