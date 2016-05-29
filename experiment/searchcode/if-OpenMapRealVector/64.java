public OpenMapRealVector append(RealVector v) {
if (v instanceof OpenMapRealVector) {
return append((OpenMapRealVector) v);
/** {@inheritDoc} */
@Override
public double dotProduct(RealVector v) {
if(v instanceof OpenMapRealVector) {

