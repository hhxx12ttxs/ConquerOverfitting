public double pow(double x, int n) {
if (n > 0)
return absPow(x, n);
else
return 1.0 / absPow(x, -n);
}

private double absPow(double x, int n) {
if (n == 0)
return 1;

