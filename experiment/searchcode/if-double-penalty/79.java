private DNANucleobaseAlphabet alphabet;
private Map<AlphabetElementPair, Double> penaltyMatrix;

private class AlphabetElementPair {
this.penaltyMatrix = new HashMap<DNANucleobaseAlignmentPenalty.AlphabetElementPair, Double>();

fillPenaltyMatrix();
}

private void fillPenaltyMatrix() {

