private double sum;
private double sumsq;
private double min;
private double max;

private double mean;
public double mean() {
if (!valid)
computeStats(true);
return mean;
}
public double conf(double confidenceFactor)

