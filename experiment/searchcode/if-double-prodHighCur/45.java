* @throws DimensionMismatchException if the array lengths differ.
* @since 3.1
*/
public static double[] ebeAdd(double[] a,
double[] b) {
if (a.length != b.length) {
throw new DimensionMismatchException(a.length, b.length);

