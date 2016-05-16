package common;

/**
 * An instance of this class pre-computed values for the sigmoid function
 * Its main purpose to increase the speed of the program (or so people say :P)
 * since e^-x takes longer time then mult/add
 *
 */
public class SigmoidTable {
    
    // Default parameters for the table
    public static final double DEFAULT_MAX_X              = 6;
    public static final int   DEFAULT_SIGMOID_TABLE_SIZE = 10000000;

    /*
     * This sigmoidTable holds the precomputed sigmoid values of variables in the range
     * [-maxX, maxX]
     * tableSize decides the interval between two consecutive values that we
     * compute the sigmoid function for, i.e. the precision of the returned
     * sigmoid values
     */
    private double[]           sigmoidTable;
    private double             maxX;
    private int                tableSize;


    public SigmoidTable(int tableSize, double maxX) {
        this.tableSize = tableSize;
        this.maxX = maxX;
        initTable();
    }

    /**
     * Default constructor
     * Initialize with default values
     */
    public SigmoidTable() {
        this(DEFAULT_SIGMOID_TABLE_SIZE, DEFAULT_MAX_X);
    }
    
    /**
     * Initialize the precomputed sigmoid table.
     * The table consists of "tableSize" precomputed values for sigmoid 
     * function for input values from -maxX to maxX (The difference between to
     * consecutive input value would be: 2 * maxX / (tableSize - 1)
     */
    public void initTable() {
        sigmoidTable = new double[tableSize];
        double step = (2 * maxX) / (tableSize - 1);
        for (int i = 0; i < tableSize - 1; i++) {
            double x = -maxX + i * step;
            sigmoidTable[i] = MathUtils.sigmoid(x);
        }
    }

    /**
     * Get the sigmoid function for x from the pre-computed table
     */
    public double getSigmoid(double x) {
        if (x > maxX)
            return 1;
        else if (x < -maxX)
            return 0;
        else {
            int index = (int) Math.round((x + maxX) / (2 * maxX) * (tableSize - 1));
            return sigmoidTable[index];
        }
//        double result = MathUtils.sigmoid(x);
//        return result;
    }

}
