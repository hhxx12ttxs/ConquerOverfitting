columnDimension = X.getColumnDimension();

if (Xstar.getRowDimension() != rowDimension) {
throw new IllegalArgumentException(&quot;X and Xstar do not have the same number of rows&quot;);
RealMatrix J = new Array2DRowRealMatrix(rowDimension, rowDimension);

if (allowTranslation) {
//           J <- diag(n) - 1/n * matrix(1, n, n)

