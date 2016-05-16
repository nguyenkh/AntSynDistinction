package common;

import io.word.Phrase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * This class provides a set of utility method to turn one data structure to
 * another
 *
 */
public class DataStructureUtils {
    
    /**
     * This template method turns an array into a HashSet of the same type 
     */
    public static <T> HashSet<T> arrayToSet(T[] inputArray) {
        HashSet<T> result = new HashSet<T>();
        if (inputArray != null) {
            for (int i = 0; i < inputArray.length; i++) {
                result.add(inputArray[i]);
            }
        }
        return result;
    }

    /**
     * This template method turns an array into a HashMap that maps an element
     * of the array to its index 
     */
    public static <T> HashMap<T, Integer> arrayToMap(T[] inputArray) {
        HashMap<T, Integer> result = new HashMap<T, Integer>();
        if (inputArray != null) {
            for (int i = 0; i < inputArray.length; i++) {
                result.put(inputArray[i], i);
            }
        }
        return result;
    }

    /**
     * This template method turns an array into an (Array)List of the same type 
     */
    public static <T> ArrayList<T> arrayToList(T[] inputArray) {
        ArrayList<T> result = new ArrayList<T>();
        if (inputArray != null) {
            for (int i = 0; i < inputArray.length; i++) {
                result.add(inputArray[i]);
            }
        }
        return result;
    }

    /*
     * The following set of methods turn a list into an array of the same type
     * The Java compiler cannot initialize an array without knowing the type of
     * the elements. Therefore, one cannot generalize with a template method
     */
    
    public static double[][] arrayListTo2dArray(List<double[]> list) {
        double[][] array = new double[list.size()][list.get(0).length];
        list.toArray(array);
        return array;
    }
    
    /**
     * This template method turns an array into a HashMap that maps an element
     * of the array to its index 
     */
    public static <T> HashMap<T, Integer> listToMap(List<T> inputArray) {
        HashMap<T, Integer> result = new HashMap<T, Integer>();
        if (inputArray != null) {
            for (int i = 0; i < inputArray.size(); i++) {
                result.put(inputArray.get(i), i);
            }
        }
        return result;
    }

    public static String[] stringListToArray(List<String> list) {
        String[] array = new String[list.size()];
        list.toArray(array);
        return array;
    }

    public static Phrase[] phraseListToArray(List<Phrase> list) {
        Phrase[] array = new Phrase[list.size()];
        list.toArray(array);
        return array;
    }

    public static int[] intListToArray(List<Integer> list) {
        int[] array = new int[list.size()];
        int i = 0;
        for (Integer element : list) {
            array[i] = element;
            i++;
        }
        return array;
    }

    /**
     * Search through an small int array for a given value
     * @param array
     * @param key
     * @return the index of the first element to have a value equal to the key
     */
    public static int searchSmallIntArray(int[] array, int key) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == key)
                return i;
        }
        return -1;
    }
    
}
