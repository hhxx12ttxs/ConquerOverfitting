super(p, a);
}

@Override
public double ease(double t, double b, double c, double d) {
if (t == 0) {
double aa = a;
double s;
if (0 == aa || aa < Math.abs(c)) {
aa = c;

