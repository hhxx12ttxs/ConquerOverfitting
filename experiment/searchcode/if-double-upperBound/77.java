private double lowerBound;


private double upperBound;


public GrayPaintScale() {
this(0.0, 1.0);
}


public GrayPaintScale(double lowerBound, double upperBound) {
if (lowerBound >= upperBound) {

