return res;
}

/** {@inheritDoc} */
public OpenMapRealVector append(RealVector v) {
if (v instanceof OpenMapRealVector) {
public double dotProduct(RealVector v) throws IllegalArgumentException {
if(v instanceof OpenMapRealVector) {

