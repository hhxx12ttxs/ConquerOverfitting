public final void mulInverse(Quat4d q1, Quat4d q2) {
double n = q2.norm();
// zero-div may occur.
double n = Math.sqrt(q1.norm());
if (Double.isNaN(n))

