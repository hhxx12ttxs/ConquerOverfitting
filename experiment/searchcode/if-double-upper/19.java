public double differentiatePDF(final double x) {
if (x == getLower() || x == getUpper())
final double upper = getUpper();
if (x < lower || x > upper)
return Double.NEGATIVE_INFINITY;

