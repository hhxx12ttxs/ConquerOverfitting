public RealVector add(RealVector v) {
checkVectorDimensions(v.getDimension());
if (v instanceof OpenMapRealVector) {
public OpenMapRealVector append(RealVector v) {
if (v instanceof OpenMapRealVector) {
return append((OpenMapRealVector) v);

