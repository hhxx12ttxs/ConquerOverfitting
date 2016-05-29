public void setFunction(C1Function1D f) {
_f = f;
}

public Pair<Double,Double> optimize(double x) {
double x1 = x;
double f1 = _f.eval(x1);
double f2 = _f.eval(x2);
boolean tooClose = (2*abs(f2-f1) < TOL*(abs(f2)+abs(f1)+EPS));
double factor = tooClose ? 10 : 0.1;

