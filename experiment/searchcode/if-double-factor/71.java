public class CompFactorPlan {
private double compFactor;
private double upperBound;
private double lowerBound;
public double getCompFactor() {
if (constant) {
return compFactor;
}

if (compFactor + delta > upperBound) {

