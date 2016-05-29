protected double lowerBound, upperBound;



public RangeConstraint(double lower, double upper){
lowerBound = lower;
Double v ;
if (fieldValue instanceof Double) {
v = (Double) fieldValue;
}
else if (fieldValue instanceof String) { //old code

