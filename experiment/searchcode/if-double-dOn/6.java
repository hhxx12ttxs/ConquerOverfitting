protected Pair<Double, Double> calcArithmeticPrediction(double mean1, double dev1, double mean2, double dev2)
{
// I don&#39;t know if this math is right at all.  Check it some day!!!
double mean = mean1 * mean2;
double dev = Math.sqrt((mean1 * mean1 * dev2 * dev2) + (mean2 * mean2 * dev1 * dev1) + (dev1 * dev1 * dev2 * dev2));

