} catch (ClassCastException cce) {

final int m = pivot.length;
if (b.getDimension() != m) {
throw MathRuntimeException.createIllegalArgumentException(
final int m = pivot.length;
if (b.getRowDimension() != m) {
throw MathRuntimeException.createIllegalArgumentException(

