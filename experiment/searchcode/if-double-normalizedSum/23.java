public static double[] normalizeArray(double[] values, double normalizedSum) {
if (Double.isInfinite(normalizedSum)) {
throw new MathIllegalArgumentException(LocalizedFormats.NORMALIZE_INFINITE);
}
if (Double.isNaN(normalizedSum)) {
throw new MathIllegalArgumentException(LocalizedFormats.NORMALIZE_NAN);

