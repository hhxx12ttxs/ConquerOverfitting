* @param q2 the second quaternion
*/
public final void mul(Quat4d q1, Quat4d q2)
{
if (this != q1 &amp;&amp; this != q2) {
this.w = q1.w*q2.w - q1.x*q2.x - q1.y*q2.y - q1.z*q2.z;
public final void normalize(Quat4d q1)
{
double norm;

norm = (q1.x*q1.x + q1.y*q1.y + q1.z*q1.z + q1.w*q1.w);

if (norm > 0.0) {

