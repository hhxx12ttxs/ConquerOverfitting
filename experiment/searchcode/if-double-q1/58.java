public final void mulInverse(Quat4d q1, Quat4d q2) {
double n = norm();
// zero-div may occur.
public final void mulInverse(Quat4d q1) {
double n = norm();
// zero-div may occur.
n = (n == 0.0 ? n : 1/n);
// store on stack once for aliasing-safty

