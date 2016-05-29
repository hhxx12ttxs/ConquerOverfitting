public Double getRoot(Function<Double, Double> function, Double xLower, Double xUpper) {
checkInputs(function, xLower, xUpper);
if (xLower.equals(xUpper)) {
double x3 = xUpper;
double delta = 0;
double oldDelta = 0;
double f1 = function.apply(x1);
double f2 = function.apply(x2);

