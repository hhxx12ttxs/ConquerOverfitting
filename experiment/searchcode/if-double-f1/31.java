b = Math.min(f1.getXi()+f1.getH(), f2.getXi()+f2.getH());
b = Math.min(right, b);
}

public double getKe(final IFunction1D p) {
return gauss.gau(a, b, new IFunction1D() {

public double calculate(double x) {
return f1.calculate(x)*f2.calculate(x)*q.calculate(x);

