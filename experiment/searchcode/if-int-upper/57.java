boolean[] isComposite = new boolean[upperBound + 1];
for (int m = 2; m <= upperBoundSquareRoot; m++) {
if (!isComposite[m]) {
isComposite[k] = true;
}
}
for (int m = upperBoundSquareRoot; m <= upperBound; m++)
if (!isComposite[m])

