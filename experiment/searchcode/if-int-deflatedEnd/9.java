public RealVector getEigenvector(final int i)
throws InvalidMatrixException, ArrayIndexOutOfBoundsException {
if (eigenvectors == null) {
final int l = 4 * deflatedEnd + pingPong - 1;

// step 2: flip array if needed
if ((dMin <= 0) || (deflatedEnd < end)) {

