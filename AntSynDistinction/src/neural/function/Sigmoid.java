package neural.function;

import common.SigmoidTable;

public class Sigmoid implements ActivationFunction {
    public static final SigmoidTable sigmoidTable = new SigmoidTable();

    @Override
    public double activation(double x) {
        // TODO Auto-generated method stub
        return sigmoidTable.getSigmoid(x);
    }

    @Override
    public double derivative(double x) {
        // TODO Auto-generated method stub
        double sigmoid = sigmoidTable.getSigmoid(x);
        return sigmoid * (1 - sigmoid);
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "sigmoid";
    }

}
