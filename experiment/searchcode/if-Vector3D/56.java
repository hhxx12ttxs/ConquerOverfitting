package jp.dip.commonsense.vector;

public class Vector3D {
	public static final Vector3D ZERO = new Vector3D();
	private double x, y, z;
	public Vector3D() {
		x = y = z = 0;
	}
	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Vector3D add(Vector3D o) {
		return new Vector3D(x + o.x, y + o.y, z + o.z); 
	}
	public Vector3D subtract(Vector3D o) {
		return new Vector3D(x - o.x, y - o.y, z - o.z); 
	}
	public Vector3D multiply(double scalar) {
		return new Vector3D(x * scalar, y * scalar, z * scalar); 
	}
	public Vector3D divide(double scalar) {
		return new Vector3D(x / scalar, y / scalar, z / scalar); 
	}
	public double dotProduct(Vector3D o) {
		return (x * o.x) + (y * o.y) + (z * o.z); 
	}
	public void normalize() {
		double oneOverMagnitude = 1.0 / magnitude(this);
		x *= oneOverMagnitude;
		y *= oneOverMagnitude;
		z *= oneOverMagnitude;
	}
	public static double magnitude(Vector3D o) {
		return Math.sqrt(o.x*o.x + o.y*o.y + o.z*o.z);
	}
	public static Vector3D crossProduct(Vector3D a, Vector3D b) {
		return new Vector3D(
				a.y*b.z - a.z*b.y,
				a.z*b.x - a.x*b.z,
				a.x*b.y - a.y*b.x
			);
	}
	public static double distance(Vector3D a, Vector3D b) {
		Vector3D d = a.subtract(b);
		return Math.sqrt(d.x*d.x + d.y*d.y + d.z*d.z);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector3D other = (Vector3D) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
			return false;
		return true;
	}
}

