return Math.round(value * n) / n;
}

protected static double dromLinear(double c) {
if (c <= 0.0031308) {
return 1.055 * Math.pow(c, 1 / 2.4) - 0.055;
}
}

protected static double toLinear(double c) {
if (c > 0.04045) {

