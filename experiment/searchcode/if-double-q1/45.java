* @param q1 the first quaternion
* @param q2 the second quaternion
*/
public final void mul(Quat4f q1, Quat4f q2)
{
if (this != q1 &amp;&amp; this != q2) {
norm = (q1.x*q1.x + q1.y*q1.y + q1.z*q1.z + q1.w*q1.w);

if (norm > 0.0f) {
norm = 1.0f/(float)Math.sqrt(norm);

