final RealMatrix arz, final int[] arindex,
final RealMatrix xold) {
double negccov = 0;
if (ccov1 + ccovmu > 0) {
* @param negccov Negative covariance factor.
*/
private void updateBD(double negccov) {
if (ccov1 + ccovmu + negccov > 0 &amp;&amp;

