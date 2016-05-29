if (isBetterThanOptimal(value)) {
c_wordSets = new ArrayList<WordSet>();
addToSets(set);
c_optimalValue = value;
} else if (equalsOptimal(value)) {
addToSets(set);
}
}

private void addToSets(WordSet set) {

