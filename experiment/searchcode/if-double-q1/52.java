public double q0;	// Scalar Component
/**
* x component (i)
*/
public double q1;	// i (x)
/**
* y component (j)
*/
public double q2;	// j (y)
q0 = 1;
q1 = 0;
q2 = 0;
q3 = 0;
}

public Quaternion(double angle, Vector3 axis) {
final double sinAngle = Math.sin(0.5 * angle);

