package utils.imlp;

public class IntervalLowerUpper extends Interval {

private final double lowerBound;
public IntervalLowerUpper(double lowerBound, double upperBound) {
//make sure that lowerBound is smaller than upperBound
if (lowerBound <= upperBound) {

