private double [][] CoefficientMatrix=new double[number][number];
private double [] ConstantTerm=new double[number];

public double[][] getA() {
return CoefficientMatrix;
public void setCoefficientMatrix(double[][] coefficientMatrix) {
CoefficientMatrix = coefficientMatrix;
}

public double[] getConstantTerm() {
return ConstantTerm;

