* @throws IllegalStateException if maximal number of iterations is reached
*/
public double evaluate(double x, double epsilon, int maxIterations) {
if (Precision.isEquals(dN, 0.0, small)) {
dN = small;
}
double cN = a + b / cPrev;
if (Precision.isEquals(cN, 0.0, small)) {

