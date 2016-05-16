package neural.function;

public interface ActivationFunction {
    public double activation(double x);
    public double derivative(double x); 
    public String getName();
}
