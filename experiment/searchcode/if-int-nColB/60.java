public RealVector solve(final RealVector b) {
final int m = lTData.length;
if (b.getDimension() != m) {
public RealMatrix solve(RealMatrix b) {
final int m = lTData.length;
if (b.getRowDimension() != m) {
throw new DimensionMismatchException(b.getRowDimension(), m);

