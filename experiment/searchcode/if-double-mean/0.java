public static double sum(double[] a) {
if (a.length > 0) {
double sum = 0;

for (double i : a) {
sum += i;
}
return sum;
}
return 0;
}

public static double mean(double[] a) {
double sum = sum(a);

