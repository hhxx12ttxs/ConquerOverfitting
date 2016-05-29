public RealMatrix getP() {
if ((cachedP == null) &amp;&amp; !singular) {
final int m = pivot.length;
cachedP = MatrixUtils.createRealMatrix(m, m);
} catch (ClassCastException cce) {

final int m = pivot.length;
if (b.getDimension() != m) {
throw new DimensionMismatchException(b.getDimension(), m);

