public RealVector add(RealVector v) throws DimensionMismatchException {

checkVectorDimensions(v.getDimension());
if(v instanceof OpenMapRealVector) {
return add((OpenMapRealVector)v);
* @throws DimensionMismatchException
*             if the dimensions do not match.
*/
public OpenMapRealVector add(OpenMapRealVector v) throws DimensionMismatchException {

