package vocab;

import java.util.ArrayList;
import java.util.Collections;

import common.DataStructureUtils;

public class HuffmanTree {
    int[]  binaries;
    int[]  parentNodes;
    long[] counts;
    int    vocabSize;

    /*
     * Create binary Huffman tree using the word counts intCounts must already
     * be sorted (descending order)
     */
    public HuffmanTree(long[] inCounts) {

        vocabSize = inCounts.length;

        /*
         * counts: the count each node in a tree binaries: the code bit going
         * from the parent to the current node parentNodes: the direct parent of
         * each node
         * 
         * These arrays are splitted into 2 groups: leaf nodes: 0 -> vocabSize-1
         * (descending counts) internal nodes: vocabSize -> 2 * vocabSize - 2
         * (ascending counts)
         */
        counts = new long[2 * vocabSize - 1];
        binaries = new int[2 * vocabSize - 1];
        parentNodes = new int[2 * vocabSize - 1];

        // creating a
        for (int i = 0; i < vocabSize; i++) {
            counts[i] = inCounts[i];
        }
        for (int i = vocabSize; i < vocabSize * 2 - 1; i++) {
            counts[i] = (int) 1e15;
        }

        int pos1 = vocabSize - 1; // traverse in the leaf node indices
        int pos2 = vocabSize; // traverse in the internal node indices

        /*
         * Following algorithm constructs the Huffman tree by creating one
         * internal node at a time
         */

        int min1i, min2i;
        for (int i = 0; i < vocabSize - 1; i++) {

            // First, find node with smallest count 'min1'
            if (pos1 >= 0) {
                if (counts[pos1] < counts[pos2]) {
                    min1i = pos1;
                    pos1--;
                } else {
                    min1i = pos2;
                    pos2++;
                }
            } else {
                min1i = pos2;
                pos2++;
            }
            // Then, find node with next smallest count 'min2'
            if (pos1 >= 0) {
                if (counts[pos1] < counts[pos2]) {
                    min2i = pos1;
                    pos1--;
                } else {
                    min2i = pos2;
                    pos2++;
                }
            } else {
                min2i = pos2;
                pos2++;
            }

            // sum the count, create a new node with the sum as its count
            counts[vocabSize + i] = counts[min1i] + counts[min2i];
            // update the code & parent information
            parentNodes[min1i] = vocabSize + i;
            parentNodes[min2i] = vocabSize + i;
            binaries[min1i] = 0; // which is default in Java
            binaries[min2i] = 1;
        }
    }

    /*
     * retrieve the Huffman code of a index_th input entry (i.e. word in a
     * vocab)
     */
    public String getCode(int index) {
        int parentIndex = index;

        StringBuffer code = new StringBuffer();
        // traverse from the node to the root to get the reversed code
        // reverse and return the code
        while (true) {
            code.append(binaries[parentIndex]);
            parentIndex = parentNodes[parentIndex];
            if (parentIndex > vocabSize * 2 - 2) {
                System.out.println(parentIndex);
            }
            if (parentIndex == vocabSize * 2 - 2) {
                break;
            }
        }
        return new StringBuilder(code.toString()).reverse().toString();
    }

    /*
     * retrieve the ancestors of a index_th input entry in the Huffman tree
     */
    public int[] getParentIndices(int index) {
        int currentIndex = index;
        ArrayList<Integer> parentIndices = new ArrayList<Integer>();

        /*
         * traverse from the node to the root to get the reversed list of parent
         * indices in the internal node list (the original indices subtracted by
         * vocabSize) reverse the list, turn it into an array and return
         */

        while (true) {
            int parentIndex = parentNodes[currentIndex];
            parentIndices.add(parentIndex - vocabSize);
            currentIndex = parentIndex;
            if (parentIndex == vocabSize * 2 - 2) {
                break;
            }
        }
        Collections.reverse(parentIndices);
        return DataStructureUtils.intListToArray(parentIndices);
    }
}
