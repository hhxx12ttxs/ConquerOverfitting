for(Double d : results) {
if(d > best) best = d;
}
return best;
}

public static double getStdDev(Collection<Double> results) {
double mean = getMean(results);
double stdDev = 0;

