public RealMatrix solve(RealMatrix b) {
final int m = lTData.length;
if (b.getRowDimension() != m) {
throw new DimensionMismatchException(b.getRowDimension(), m);
}

final int nColB = b.getColumnDimension();
final double[][] x = b.getData();

