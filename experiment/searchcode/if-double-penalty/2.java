public class LinearSeparationPenaltyFunction implements PenaltyFunction {

private double maxPenalty;
private double minPenalty;
public double getPenalty(double dist, double t) {
if (dist > minSeparation) {
return 0;
} else {

