public String name; // defaults to &quot;&quot;
private double sum;
private double sumsq;
private double min;
private double max;
public double mean() {
if (!valid)
computeStats();
return mean;
}

public double winPercent() {

