public static double doubleCoV(double[] numbers) {
double cov = 0.0;
double stdDev = 0.0;
double mean = Stats.doubleMean(numbers);
for (Double value : numbers) { // for each locus in loci
stdDev = Math.pow((value - mean), 2); // (value - mean) squared

