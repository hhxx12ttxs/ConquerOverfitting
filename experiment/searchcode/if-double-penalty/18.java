private boolean solved = false;

private double[][] penaltyValueMatrix;

public DNANucleobaseStringAlignmentProblem(DNANucleobaseAlignmentPenalty penaltyStrategy, String a, String b) {
init();
}

private void init() {
this.penaltyValueMatrix = new double[originalFirstString.length() + 1][originalSecondString.length() + 1];

