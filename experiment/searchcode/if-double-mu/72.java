// return phi(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
public static double phi(double x, double mu, double sigma) {
// return Phi(z) = standard Gaussian cdf using Taylor approximation
public static double Phi(double z) {
if (z < -8.0) return 0.0;

