final double absolutePositivityThreshold) {
if (!matrix.isSquare()) {
throw new NonSquareMatrixException(matrix.getRowDimension(),
throw new NonPositiveDefiniteMatrixException(ltI[i], i, absolutePositivityThreshold);
}

ltI[i] = FastMath.sqrt(ltI[i]);
final double inverse = 1.0 / ltI[i];

