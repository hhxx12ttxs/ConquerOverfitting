public class ChiSquareDistribution implements IDistribution{

private int degreesOfFreedom;

private double entropy;

public ChiSquareDistribution(int degreesOfFreedom) {
public double Mean() {
return degreesOfFreedom;
}

@Override
public double Variance() {

