public double q2;	// j (y)
/**
* z component (k)
*/
public double q3;	// k (z)

public Quaternion() {
q0 = 1;
q1 = 0;
q2 = 0;
q3 = 0;
}

public Quaternion(double angle, Vector3 axis) {
final double sinAngle = Math.sin(0.5 * angle);

