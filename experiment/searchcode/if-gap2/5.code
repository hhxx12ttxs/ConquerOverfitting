int gap1 = aMatrix[i][j-1] + gapFunction.calcPenalty(1);
int gap2 = aMatrix[i-1][j] + gapFunction.calcPenalty(1);
int match = aMatrix[i-1][j-1] + scoringMatrix.getScore(sequence.getSequenceA()[i-1], sequence.getSequenceB()[j-1]);
aMatrix[i][j] = Math.max(0, Math.max(match, Math.max(gap1, gap2)));

}

public void make() {

