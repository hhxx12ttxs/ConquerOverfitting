* WARNING: This cache will grow forever
*/
double sumLog[] = { 0.0 };

public static Binomial get() {
if (binomial == null) binomial = new Binomial();
public double cdf(double p, int k, int n) {
if (k < 0) return 0;
if (k >= n) return 1.0;

double cdf = 0;
for (int i = 0; i <= k; i++)

