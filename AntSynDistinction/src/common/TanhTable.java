package common;

/**
 * An instance of this class pre-computed values for the tanh function
 * Its main purpose to increase the speed of the program (or so people say :P)
 * since e^-x takes longer time then mult/add
 *
 */
public class TanhTable {
    
    // Default parameters for the table
    public static final double DEFAULT_MAX_X              = 6;
    public static final int   DEFAULT_TANH_TABLE_SIZE = 10000000;

    /*
     * This tanhTable holds the precomputed tanh values of variables in the range
     * [-maxX, maxX]
     * tableSize decides the interval between two consecutive values that we
     * compute the tanh function for, i.e. the precision of the returned
     * tanh values
     */
    private double[]           tanhTable;
    private double             maxX;
    private int               tableSize;


    public TanhTable(int tableSize, double maxX) {
        this.tableSize = tableSize;
        this.maxX = maxX;
        initTable();
    }

    /**
     * Default constructor
     * Initialize with default values
     */
    public TanhTable() {
        this(DEFAULT_TANH_TABLE_SIZE, DEFAULT_MAX_X);
    }
    
    /**
     * Initialize the precomputed tanh table.
     * The table consists of "tableSize" precomputed values for tanh 
     * function for input values from -maxX to maxX (The difference between to
     * consecutive input value would be: 2 * maxX / (tableSize - 1)
     */
    public void initTable() {
        tanhTable = new double[tableSize];
        double step = (2 * maxX) / (tableSize - 1);
        for (int i = 0; i < tableSize - 1; i++) {
            double x = -maxX + i * step;
            tanhTable[i] = MathUtils.tanh(x);
        }
    }

    /**
     * Get the tanh function for x from the pre-computed table
     */
    public double getTanh(double x) {
//        if (x > 1000) {
//            System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
//            System.out.println("x: " + x);
//            return 1;
//        } else if (x < -1000) {
//            System.out.println("-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
//            return -1;
//        }
//        return MathUtils.tanh(x);
        if (x > maxX)
            return 1;
        else if (x < -maxX)
            return -1;
        else {
//            int index = (int) Math.round((x + maxX) / (2 * maxX) * (tableSize - 1));
//            return tanhTable[index];
            return MathUtils.tanh(x);
        }

    }

}
