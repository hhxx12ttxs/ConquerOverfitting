protected abstract double nextPoint(double h, double x0, double f0, double f, double L);

@Override
public double findMin(double leftBound, double rightBound, double L, double  eps) {
resetCounter();
double h = (2*eps) / L;
double x0 = leftBound + h /2;
double f0 = function.calculate(x0);

