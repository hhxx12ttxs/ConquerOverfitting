protected double lowerBound, upperBound;



public RangeConstraint(double lower, double upper){
lowerBound = lower;
upperBound = upper;
}

public boolean satisfy(Object fieldValue) {
Double v ;

