if (values.length > 0)
{
n -= 1;
}
return variance/n;
}

public static double calculateStdDev(double[] values)
{
double stdDev = 0;
if (values.length > 0)
{
double variance = calculateVariance(values);

