throw new IllegalArgumentException(&quot;Invalid x: &quot; + x);
}

double ibeta = 0.0;
if (x == 0.0) {
ibeta = 0.0;
double a1 = alpha - 1.;
double b1 = beta - 1.;

if (p <= 0.0) {
return 0.0;

