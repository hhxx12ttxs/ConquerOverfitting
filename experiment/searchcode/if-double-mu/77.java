double mu;

// the scale parameter
double beta;

// the maximum density
double c;
* Cumulative distribution function
*/
public double cdf(double x) {
if (x == mu) return 0.5;
else return (0.5) * (1 + ((x - mu) / Math.abs(x - mu))

