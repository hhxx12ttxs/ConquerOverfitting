for (double a : list) s += (a - m) * (a - m);
return s / (n - 1);
}
public static double getMin(double[] arr) {
double min = arr[0];
for (double a : arr) {
if (a < min) min = a;
}
return min;
}
public static double getMax(double[] arr) {

