//TZ: fields are much faster than arrays.
double q0, q1, q2, q3; //!< x, y, z, w
public void set(double x, double y, double z, double w) {
static final double ccdQuatLen2(final ccd_quat_t q)
{
double len;

len  = q.q0 * q.q0;
len += q.q1 * q.q1;
len += q.q2 * q.q2;

