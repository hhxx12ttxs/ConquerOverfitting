* @throws IllegalStateException if maximal number of iterations is reached
*/
public double evaluate(double x, double epsilon, int maxIterations) {
final double b = getB(n, x);

double dN = a + b * dPrev;
if (Precision.isEquals(dN, 0.0, small)) {

