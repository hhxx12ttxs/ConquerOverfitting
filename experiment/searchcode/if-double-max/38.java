public static double min(double[] d) {
double min = d[0];
for (int i = 1; i < d.length; i++) {
if (d[i] < min) {
min = d[i];
}
}
return min;
}

public static double max(double[] d) {
double max = d[0];

