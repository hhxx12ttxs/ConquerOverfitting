boolean[] isComposite = new boolean[upperBound + 1];
for (int m = 2; m <= upperBoundSquareRoot; m++) {
if (!isComposite[m]) {
for (int m = upperBoundSquareRoot; m <= upperBound; m++)
if (!isComposite[m])
System.out.print(m + &quot; &quot;);

