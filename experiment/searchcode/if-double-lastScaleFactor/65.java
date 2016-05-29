* @return the value of the continued fraction evaluated at x.
* @throws MathException if the algorithm fails to converge.
*/
public double evaluate(double x) throws MathException {
double p2 = a * p1 + b * p0;
double q2 = a * q1 + b * q0;
boolean infinite = false;
if (Double.isInfinite(p2) || Double.isInfinite(q2)) {

