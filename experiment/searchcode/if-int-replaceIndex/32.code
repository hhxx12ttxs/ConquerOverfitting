probDifferences[k] = minimumProbs[k] - probability;
}

int replaceIndex = 0;
for (int k = 1; k < base; k++) {
replaceIndex = (probDifferences[k] >= probDifferences[replaceIndex] ? k:replaceIndex);
}
if (probDifferences[replaceIndex] > 0) {

