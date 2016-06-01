public TruncatedNormalDistribution(double mean, double sd, double lower, double upper) {


if (lower == upper)
upper += 1.E-4;
upperCDF = standardNormalCdf((upper-mean)/sd);
} else {
upperCDF = 1;
}

if(lower!=Double.NEGATIVE_INFINITY){

