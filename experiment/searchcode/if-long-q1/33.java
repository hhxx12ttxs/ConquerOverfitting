public final void mul(UnQuat4f q1, UnQuat4f q2)
{
if (this != q1 &amp;&amp; this != q2) {
this.w = q1.w*q2.w - q1.x*q2.x - q1.y*q2.y - q1.z*q2.z;
norm = (q1.x*q1.x + q1.y*q1.y + q1.z*q1.z + q1.w*q1.w);

if (norm > 0.0f) {
norm = 1.0f/(float)Math.sqrt(norm);

