final int m = lTData.length;
if (b.getDimension() != m) {
throw new DimensionMismatchException(b.getDimension(), m);
public RealMatrix solve(RealMatrix b) {
final int m = lTData.length;
if (b.getRowDimension() != m) {

