public void runEratosthenesSieve(int upperBound) {
int uppeBoundSquareRoo = (int) Math.sqrt(upperBound);
boolean [] isComposite = new boolean[upperBound +1];
for (int m = 2; m <= upperBoundSquareRoot; ++m) {

