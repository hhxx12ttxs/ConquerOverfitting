public final void mul(Quat4d q1, Quat4d q2)
{
if (this != q1 &amp;&amp; this != q2) {
this.w = q1.w*q2.w - q1.x*q2.x - q1.y*q2.y - q1.z*q2.z;
this.z = q1.w*q2.z + q2.w*q1.z + q1.x*q2.y - q1.y*q2.x;
} else {
double	x, y, w;

w = q1.w*q2.w - q1.x*q2.x - q1.y*q2.y - q1.z*q2.z;

