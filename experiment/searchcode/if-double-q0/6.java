public class Quaternion {
/**
* Scalar Component (w)
*/
public double q0;	// Scalar Component
public Quaternion setDCM(Matrix3 m) {
final double t = 1. + m.m11 + m.m22 + m.m33;
if (t > 0.000000000000001) {
q0 = Math.sqrt(t) * 0.5;

