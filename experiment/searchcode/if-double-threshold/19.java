private double threshold;

public RocMatcher() {
this.threshold = 0;
}

public void setThreshold(double threshold) {
public double apply(double cellValue) {
if (cellValue > threshold) {
return 1;
}
return 0;
}
}

