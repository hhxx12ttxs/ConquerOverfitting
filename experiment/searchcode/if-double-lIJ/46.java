for (int j = i + 1; j < order; ++j) {
final double[] lJ = lTData[j];
final double lIJ = lI[j];
final double lJI = lJ[i];
final double maxDelta =
relativeSymmetryThreshold * Math.max(Math.abs(lIJ), Math.abs(lJI));
if (Math.abs(lIJ - lJI) > maxDelta) {

