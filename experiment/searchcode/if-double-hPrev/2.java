* @throws ConvergenceException if the algorithm fails to converge.
*/
public double evaluate(double x) throws ConvergenceException {
dN = 1 / dN;
final double deltaN = cN * dN;
hN = hPrev * deltaN;

if (Double.isInfinite(hN)) {

