checkVectorDimensions(v.getDimension());
if (v instanceof OpenMapRealVector) {
return add((OpenMapRealVector) v);
return res;
}

/** {@inheritDoc} */
public OpenMapRealVector append(RealVector v) {
if (v instanceof OpenMapRealVector) {

