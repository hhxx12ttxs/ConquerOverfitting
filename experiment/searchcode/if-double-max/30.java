double min = arr[0];
for (double a : arr) {
if (a < min) min = a;
}
return min;
}
public static double getMax(double[] arr) {
double max = arr[0];
for (double a : arr) {
if (a > max) max = a;
}
return max;
}
}

