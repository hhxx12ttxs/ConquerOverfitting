dot = x * q1.x + y * q1.y + z * q1.z + w * q1.w;

if (dot < 0) {
// negate quaternion
q1.x = -q1.x;
q1.y = -q1.y;
q1.z = -q1.z;
q1.w = -q1.w;
dot = -dot;
}

if ((1.0 - dot) > EPS) {

