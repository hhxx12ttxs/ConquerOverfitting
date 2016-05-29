int variance = (int) (Math.random() * (upperBound - lowerBound) + lowerBound);
long result = basedOn + variance;
//        if (result > upperBound || result < lowerBound){
public double generateValue(double basedOn) {
double upperBound = Double.MAX_VALUE / 1000000;
double lowerBound = Double.MIN_VALUE / 1000000;

