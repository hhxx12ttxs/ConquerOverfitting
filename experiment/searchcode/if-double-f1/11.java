public static double OddsRatioTest(double f1, double f2, double n1,
double n2)
{
double odds = Math.log((f1 * (1 - f2)) / ((1 - f1) * f2));
double se = Math.sqrt(1 / (f1 * n1) + 1 / ((1 - f1) * n1) + 1
/ (f2 * n2) + 1 / ((1 - f2) * n2));

