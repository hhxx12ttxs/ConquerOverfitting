public Quaternion setDCM(Matrix3 m) {
final double t = 1. + m.m11 + m.m22 + m.m33;
if (t > 0.000000000000001) {
q0 = Math.sqrt(t) * 0.5;
q3 = s * (m.m31 + m.m13);
q0 = s * (m.m23 - m.m32);
} else if (m.m22 > m.m33) {
q2 = Math.sqrt(1.0 + m.m22 - m.m11 - m.m33) * 0.5;

