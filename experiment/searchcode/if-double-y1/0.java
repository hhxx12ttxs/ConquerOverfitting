public static double E(double Y, double A, double Cy) {
return A + Cy * Y;
}

public static double balance(double Y1, double A, double Cy) {
throw new IllegalArgumentException();
}
double e = E(Y1, A, Cy);
if (e != Y1) {
Y1 = e;
System.out.println(&quot;New Y1: &quot; + Y1);

