checkVectorDimensions(v.getDimension());
if (v instanceof OpenMapRealVector) {
return add((OpenMapRealVector) v);
* @return the sum of {@code this} and {@code v}.
* @throws DimensionMismatchException if the dimensions do not match.
*/
public OpenMapRealVector add(OpenMapRealVector v)

