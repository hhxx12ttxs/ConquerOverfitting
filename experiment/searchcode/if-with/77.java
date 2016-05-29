// TODO Auto-generated method stub
double a = 2, b = -10; //
double pow = powerWithRecurSquaring(a, b);
if (b < 0) {
pow = 1 / pow;
// a power b, runs in ~ O(2 log b) time
public static double powerWithRecurSquaring(double a, double b) {
if (b < 0) {
b = -b; //   b = Math.abs(b);

