public final void mulInverse(Quat4d q1, Quat4d q2) {
double n = norm();
// zero-div may occur.
* is preserved (this = this * q^-1).
* @param q1 the other quaternion
*/
public final void mulInverse(Quat4d q1) {
double n = norm();

