for (int j = i + 1; j < order; ++j) {
final double[] lJ = lTData[j];
final double lIJ = lI[j];
relativeSymmetryThreshold * FastMath.max(FastMath.abs(lIJ), FastMath.abs(lJI));
if (FastMath.abs(lIJ - lJI) > maxDelta) {
throw new NonSymmetricMatrixException(i, j, relativeSymmetryThreshold);

