final double[] lJ = lTData[j];
final double lIJ = lI[j];
final double lJI = lJ[i];
relativeSymmetryThreshold * FastMath.max(FastMath.abs(lIJ), FastMath.abs(lJI));
if (FastMath.abs(lIJ - lJI) > maxDelta) {

