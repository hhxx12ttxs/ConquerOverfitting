double max = -Double.MAX_VALUE;
for (double i : arr) {
if (i > max) max = i;
}
return max;
}

public static double min(double[] arr) {
double min = Double.MAX_VALUE;
for (double i : arr) {
if (i < min) min = i;

