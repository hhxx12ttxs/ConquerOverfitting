public class RA20 {

public static double ra20(double t, double tt, double f) {
if (t <= 0) {
throw new IllegalArgumentException(&quot;tt: Menor que t&quot;);
}

double tc = tt;
double ta = t;
double i = 1;

while (i <= f) {

