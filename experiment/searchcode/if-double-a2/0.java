public static double computeArea(double a1, double a2, double a3) {
double area = 0;
if (isValid(a1, a2, a3)) {
double p = (a1 + a2 + a3) / 2;
area = Math.sqrt(p * (p - a1) * (p - a2) * (p - a3));
public static boolean isValid(double a1, double a2, double a3) {
boolean result = true;
if ((a1 + a2 <= a3)
|| a1 + a3 <= a2
|| a2 + a3 <= a1)
result = false;
return result;
}
}

