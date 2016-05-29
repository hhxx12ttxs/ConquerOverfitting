/** Inverse of twice the square of the standard deviation. */
private final double i2s2;
/** Normalization factor. */
private final double norm;
final double diff = x - param[1];
final double i2s2 = 1 / (2 * param[2] * param[2]);
return Gaussian.value(diff, param[0], i2s2);

