package space;

import org.ejml.simple.SimpleMatrix;

public interface SemanticSpace {
//    public boolean containsWord(String word);
    public int getVectorSize();
    public SimpleMatrix getVector(String word);
    public double getSim(String word1, String word2);
    public Neighbor[] getNeighbors(String word, int noNeighbor);
    public Neighbor[] getNeighbors(SimpleMatrix vector, int noNeighbor, String[] excludedWords);
}
