public class TriangleFuzzyMembership extends FuzzyMembership {

/**
*
*/
private static final long serialVersionUID = 1L;
private double lowerBound;
public TriangleFuzzyMembership(double lower, double upper, double top){
lowerBound = lower;
upperBound = upper;

