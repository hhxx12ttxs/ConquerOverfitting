public class StandardUnit extends Unit {

private double factorToReference;

public StandardUnit(String name, Type type, System system, double factorToReference) {
public void setFactorToReference(double factor) {
if(factor > 0) {
this.factorToReference = factor;
}
}
}

