throw new SingularMatrixException();
}

final int m = realEigenvalues.length;
if (b.length != m) {
throw MathRuntimeException.createIllegalArgumentException(
final int m = realEigenvalues.length;
if (b.getRowDimension() != m) {
throw MathRuntimeException.createIllegalArgumentException(

