* @throws RuntimeException if the algorithm fails to converge.
*/
public double evaluate(double x, double epsilon) throws RuntimeException {
boolean infinite = false;
if (Double.isInfinite(p2) || Double.isInfinite(q2)) {

