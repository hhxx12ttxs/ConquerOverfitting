public class ConstantSeparationPenaltyFunction implements PenaltyFunction {

private double penalty;
private double minSeparation;
private int separationSquare;

public ConstantSeparationPenaltyFunction(double penalty, double minSeparation) {

