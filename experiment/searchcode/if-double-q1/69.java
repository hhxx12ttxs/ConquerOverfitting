public static Quaternion mult(Quaternion q1, Quaternion q2) {
double w = q1.w * q2.w - q1.x * q2.x - q1.y * q2.y - q1.z * q2.z;
double x = q1.w * q2.x + q1.x * q2.w + q1.y * q2.z - q1.z * q2.y;

