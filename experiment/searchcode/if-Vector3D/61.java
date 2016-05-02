package util;

/**
 *
 * @author Zetro
 * @author FireFly
 * 
 * TODO: Make separate methods for creating a new Vector and not create a new one when doing calcs.
 * By not creating a ****load of object while raytracing will severely optimize performance.
 * TODO: Add comments/javadoc
 * 
 *   I made it immutable anyway >:3 //FF
 */
public class Vector3d {
	public final double x, y, z;
	public static final Vector3d ORIGO = new Vector3d(0, 0, 0);
	public static final Vector3d ONE = new Vector3d(1, 1, 1);

	public static final Vector3d BASIS_X = new Vector3d(1, 0, 0);
	public static final Vector3d BASIS_Y = new Vector3d(0, 1, 0);
	public static final Vector3d BASIS_Z = new Vector3d(0, 0, 1);
	
	public static final Vector3d NEGATIVE_X = new Vector3d(-1,  0,  0);
	public static final Vector3d NEGATIVE_Y = new Vector3d( 0, -1,  0);
	public static final Vector3d NEGATIVE_Z = new Vector3d( 0,  0, -1);
	
	public Vector3d(double x, double y) {
		this(x, y, 0);
	}
	
	public Vector3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double squaredLength() {
		return x*x + y*y + z*z;
	}
	
	public double length() {
		return Math.sqrt(this.squaredLength());
	}
	
	public Vector3d negate() {
		return new Vector3d(-this.x, -this.y, -this.z);
	}
	
	public Vector3d normalize() {
		double length = length();
		if (length == 0) return new Vector3d(0, 0, 0);
		return this.mul(1.0 / length);
	}
	
	public Vector3d mul(double scalar) {
		return new Vector3d(this.x*scalar, this.y*scalar, this.z*scalar);
	}
	public Vector3d div(double scalar) {
		return new Vector3d(this.x/scalar, this.y/scalar, this.z/scalar);
	}
	public Vector3d add(Vector3d v) {
		return new Vector3d(this.x+v.x, this.y+v.y, this.z+v.z);
	}
	public Vector3d mul(Vector3d v) {
		return new Vector3d(this.x*v.x, this.y*v.y, this.z*v.z);
	}
	public Vector3d sub(Vector3d v) {
		return new Vector3d(this.x-v.x, this.y-v.y, this.z-v.z);
	}
	public Vector3d div(Vector3d v) {
		return new Vector3d(this.x/v.x, this.y/v.y, this.z/v.z);
	}
	
	public Vector3d cross(Vector3d v) {
		double dx = (this.y * v.z) - (this.z * v.y);
		double dy = (this.z * v.x) - (this.x * v.z);
		double dz = (this.x * v.y) - (this.y * v.x);
		
		return new Vector3d(dx, dy, dz);
	}
	
	public double dot(Vector3d v) {
		return Vector3d.dot(this, v);
	}

	public static double dot(Vector3d v1, Vector3d v2) {
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}

	public double angle(Vector3d v) {
		return angle(this, v);
	}

	public static double angle(Vector3d v1, Vector3d v2) {
		return Math.acos(Vector3d.dot(v1, v2));
	}
	
	public Vector3d addX(double dx) {
		return new Vector3d(this.x + dx, this.y, this.z);
	}
	public Vector3d addY(double dy) {
		return new Vector3d(this.x, this.y + dy, this.z);
	}
	public Vector3d addZ(double dz) {
		return new Vector3d(this.x, this.y, this.z + dz);
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
		hash = 29 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
		hash = 29 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj == this) {
			return true;
		} else if (obj instanceof Vector3d) {
			Vector3d v = (Vector3d) obj;

			return v.x == this.x && v.y == this.y && v.z == this.z;

		}
		return false;
	}
	
	@Override
	public String toString() {
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}
}

