maxi = i;
}
}
return maxi;
}

public static double logsum(double loga, double logb) {
if (Double.isInfinite(loga)) {
return Math.log1p(Math.exp(loga - logb)) + logb;
}
}

public static double logsum(double[] tosum, int length) {
if (length == 1) {

