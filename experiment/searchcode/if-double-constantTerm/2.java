private Matrix CoefficientMatrix;
private Vector ConstantTerm;

public double getAccuracy() {
return accuracy;
public void setConstantTerm(Vector ConstantTerm) {
this.ConstantTerm = ConstantTerm;
}

public Iteration(int number,double accuracy,Matrix CoefficientMatrix, Vector ConstantTerm)

