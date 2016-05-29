package util;

public class IncrementalSD extends IncrementalMean {
protected double stdDev = 0;
stdDev = stdDev + (newValue - oldAverage) * (newValue - average);
}

public double getSD() {
if (numElements <=  1)
return Double.NaN;
return Math.sqrt(stdDev / (numElements - 1));

