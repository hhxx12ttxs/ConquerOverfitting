public double getEntry(int row, int column)
throws MatrixIndexException {
if (!isValidCoordinate(row,column)) {
throw new MatrixIndexException(&quot;matrix entry does not exist&quot;);
public double[] solve(double[] b) throws IllegalArgumentException, InvalidMatrixException {
int nRows = this.getRowDimension();
if (b.length != nRows) {

