* @throws DimensionMismatchException if the dimensions do not match.
*/
public OpenMapRealVector add(OpenMapRealVector v)
public OpenMapRealVector append(RealVector v) {
if (v instanceof OpenMapRealVector) {
return append((OpenMapRealVector) v);

