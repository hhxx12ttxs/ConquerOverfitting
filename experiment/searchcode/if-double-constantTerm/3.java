private final double constantTerm;

public LinearObjectiveFunction(double[] coefficients ,double constantTerm) {
this(new org.apache.commons.math.linear.ArrayRealVector(coefficients), constantTerm);
public LinearObjectiveFunction(org.apache.commons.math.linear.RealVector coefficients ,double constantTerm) {

