private double max = Double.NEGATIVE_INFINITY;
private double sum;
private double sumSq;
private long missing;

public Numeric toNumeric(long recordCount) {
return new Numeric(min, max, mean, stdDev, missing);
}

public void update(double d) {
if (Double.isNaN(d)) {
missing++;
} else {
sum += d;

