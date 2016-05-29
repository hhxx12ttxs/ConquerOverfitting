/** Inverse of twice the square of the standard deviation. */
private final double i2s2;
/** Normalization factor. */
private final double norm;

/**
* Gaussian with given normalization factor, mean and standard deviation.
public Gaussian(double norm, double mean, double sigma) throws NotStrictlyPositiveException {

if(sigma <= 0) {

