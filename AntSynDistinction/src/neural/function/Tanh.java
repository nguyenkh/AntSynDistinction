package neural.function;

import common.TanhTable;

public class Tanh implements ActivationFunction {
    public static final TanhTable tanhTable = new TanhTable();

    @Override
    public double activation(double x) {
        // TODO Auto-generated method stub
        return tanhTable.getTanh(x);
    }

    @Override
    public double derivative(double x) {
        // TODO Auto-generated method stub
        double tanh = tanhTable.getTanh(x);
        return 1 - (tanh * tanh);
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "tanh";
    }

}
