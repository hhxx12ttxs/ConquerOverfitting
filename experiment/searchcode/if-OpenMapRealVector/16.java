public OpenMapRealVector add(RealVector v) throws IllegalArgumentException {
checkVectorDimensions(v.getDimension());
if (v instanceof OpenMapRealVector) {
return add((OpenMapRealVector) v);
/** {@inheritDoc} */
public OpenMapRealVector append(RealVector v) {
if (v instanceof OpenMapRealVector) {

