public double calc(double f1, double f2);

}

public static interface Func3 {

public double calc(double f1, double f2, double f3);
public double calc(double f1, double f2, double f3) {
final double t = (f1 - f2) / (f3 - f2);
if (t < 0)

