* within the unit square, so convert infinities into -1 and +1.
*/
public double volume() {
if (cachedVolume < 0) {
double left = cube.getLeft(d);
if (Double.isInfinite(right)) {
right = +1;
}
if (Double.isInfinite(left)) {

