public TruncatedDistribution(Distribution source, double lower, double upper) {
this.source = source;

if (lower == upper) {
this.upper = upper;

if (!Double.isInfinite(this.lower)) {
this.lowerCDF = source.cdf(lower);

