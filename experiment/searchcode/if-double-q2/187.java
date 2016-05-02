package roboflight;

import java.util.Formatter;

import javax.vecmath.*;

/**
 * This class holds many useful functions. Most of them are for use with dealing
 * with vectors and rotations in 3dD space. Since those are some of the most
 * difficult aspects for many to grasp. As such I went out of my way to make
 * them as easy as possible.
 * 
 * @author Chase
 */
public final class Utils {
	/**
	 * This multiplies a 3dD vector by a quaternion, moving it through space,
	 * thus <1,0,0> multiplied by a quaternion with a representation of a 90
	 * degree clockwise rotation about the Y axis. Would return <0,1,0>.
	 * 
	 * @param vec
	 *            Vector to rotate
	 * @param rot
	 *            Quat4d to rotate by
	 * @return rotated vector.
	 */
	public static final Vector3d mulVecQuat(Vector3d vec, Quat4d rot) {
		Quat4d q = new Quat4d(rot);
		Quat4d q1 = new Quat4d(rot);
		q1.inverse();
		q.mul(new Quat4d(vec.x, vec.y, vec.z, 0));
		q.mul(q1);
		Vector3d vec2 = new Vector3d(q.x, q.y, q.z);
		vec2.scale(vec.length());
		return vec2;
	}

	/**
	 * This projects the point in 3dD space along the forward vector
	 * representation of the quaternion <b>rot</b>. Basically like 2D
	 * projection, but in 3dD.
	 * 
	 * @param pt
	 *            starting point
	 * @param rot
	 *            rotation quaternion
	 * @param dist
	 *            distance to project
	 * @return
	 */
	public static final Point3d project(Tuple3d pt, Quat4d rot, double dist) {
		Vector3d fwd = Utils.quatFwd(rot);
		fwd.scale(dist);
		Point3d np = new Point3d(pt);
		np.add(fwd);
		return np;
	}

	/**
	 * Transforms 3d part Euler rotation values (radians) into a quaternion.
	 * 
	 * @param x
	 *            the euler x angle
	 * @param y
	 *            the euler y angle
	 * @param z
	 *            the euler z angle
	 * @return Quat4dd representation of the vector
	 */
	public static final Quat4d euler2Quat(double x, double y, double z) {
		return euler2Quat(new Vector3d(x, y, z));
	}

	/**
	 * This transforms a 3d value standard euler rotation into a quaternion.
	 * This is useful when used in conjuction with the mulVecQuat method.
	 * 
	 * @param val
	 *            double array containing the euler representation of the angle
	 * @return a normalized quat with the euler angle
	 */
	public static final Quat4d euler2Quat(double[] val) {
		val[0] *= 0.5;
		val[1] *= 0.5;
		val[2] *= 0.5;

		Quat4d q = new Quat4d(Math.sin(val[0]), 0.0, 0.0, Math.cos(val[0]));
		q.mul(new Quat4d(0.0, Math.sin(val[1]), 0.0, Math.cos(val[1])));
		q.mul(new Quat4d(0.0, 0.0, Math.sin(val[2]), Math.cos(val[2])));
		q.normalize();
		return q;
	}

	/**
	 * This transforms a 3d value standard euler rotation into a quaternion.
	 * This is useful when used in conjuction with the mulVecQuat method.
	 * 
	 * @param v
	 *            Tuple3dD containing the euler representation of the angle
	 * @return a normalized quat with the euler angle
	 */
	public static final Quat4d euler2Quat(Tuple3d v) {
		v.scale(0.5);
		Quat4d q = new Quat4d(Math.sin(v.x), 0.0, 0.0, Math.cos(v.x));
		q.mul(new Quat4d(0.0, Math.sin(v.y), 0.0, Math.cos(v.y)));
		q.mul(new Quat4d(0.0, 0.0, Math.sin(v.z), Math.cos(v.z)));
		q.normalize();
		return q;
	}

	/**
	 * Gets the rotation difference between two rotations.
	 */
	public static final Quat4d quatRotDiff(Quat4d q1, Quat4d q2) {
		Quat4d q = (Quat4d)q1.clone();
		Quat4d p = (Quat4d)q2.clone();
		q.inverse();
		p.mul(q);
		return p;
	}

	/**
	 * Gets the difference between two rotations.
	 */
	public static final double quatAngleDiff(Quat4d q1, Quat4d q2) {
		AxisAngle4d aa = new AxisAngle4d();
		aa.set(quatRotDiff(q1, q2));
		return aa.getAngle();
	}

	public static final Tuple3d quat2Euler(Quat4d rot) {
		Tuple3d euler = new Tuple3d();
		Tuple4d sqr = new Tuple4d(rot);
		sqr.set(sqr.x * sqr.x, sqr.y * sqr.y, sqr.z * sqr.z, sqr.w * sqr.w);

		euler.x = Math.atan2(2.0 * (rot.y * rot.z + rot.x * rot.w), -sqr.x - sqr.y + sqr.z + sqr.w);
		euler.x = Math.asin(-2.0 * (rot.x * rot.z + rot.y * rot.w));
		euler.z = Math.atan2(2.0 * (rot.x * rot.y + rot.z * rot.w), sqr.x - sqr.y - sqr.z + sqr.w);

		// TODO: Normal relative angle

		return euler;
	}

	/**
	 * Rotation from vector A to vector B
	 */
	public final static Quat4d angleTo(Tuple3d A, Tuple3d B) {
		Vector3d a = new Vector3d(A);
		Vector3d b = new Vector3d(B);
		if(a.equals(b))
			return new Quat4d(0, 0, 0, 1);
		b.sub(a);
		b.normalize();
		// We now have a directional vector
		// Transform it into a rotation
		a.set(1, 0, 0);
		double dot = a.dot(b);
		double qw = Math.sqrt(a.lengthSquared() * b.lengthSquared()) + dot;
		a.cross(a, b);
		Quat4d quat = new Quat4d(0, 0, 0, 1);
		if(qw < 0.0001) {
			quat.set(-a.z, a.y, a.x, 0);
			quat.normalize();
			return quat;
		}
		quat.set(a.x, a.y, a.z, qw);
		quat.normalize();
		return quat;
	}

	public static final Vector3d quatFwd(Quat4d rot) {
		Vector3d vec = new Vector3d(1, 0, 0);
		vec.rotate(rot);
		return vec;
	}

	/*
	 * Generic Basic Helper Functions
	 */

	public static final double limit(double low, double val, double high) {
		return Math.min(high, Math.max(low, val));
	}

	public static final float limit(float low, float val, float high) {
		return Math.min(high, Math.max(low, val));
	}

	/**
	 * This limits val between -limit and limit.
	 * 
	 * @return the limited value
	 */
	public static final double abslimit(double val, double limit) {
		return Math.min(limit, Math.max(-limit, val));
	}

	/**
	 * This limits val between -limit and limit.
	 * 
	 * @return the limited value
	 */
	public static final float abslimit(float val, float limit) {
		return Math.min(limit, Math.max(-limit, val));
	}

	// Helper methods for the above functions, more efficient to set the
	// quaternions directly to their values, than calling euler2Quat
	/**
	 * Transforms a single x euler rotation (radians) into a quaternion.
	 * 
	 * @param radian
	 *            angle to use
	 */
	public static final Quat4d xQuat(double radian) {
		radian *= 0.5;
		Quat4d q = new Quat4d(Math.sin(radian), 0.0, 0.0, Math.cos(radian));
		// q.normalize();
		return q;
	}

	/**
	 * Transforms a single y euler rotation (radians) into a quaternion.
	 * 
	 * @param radian
	 *            angle to use
	 */
	public static final Quat4d yQuat(double radian) {
		radian *= 0.5;
		Quat4d q = new Quat4d(0.0, Math.sin(radian), 0.0, Math.cos(radian));
		// q.normalize();
		return q;
	}

	/**
	 * Transforms a single z euler rotation (radians) into a quaternion.
	 * 
	 * @param radian
	 *            angle to use
	 */
	public static final Quat4d zQuat(double radian) {
		radian *= 0.5;
		Quat4d q = new Quat4d(0.0, 0.0, Math.sin(radian), Math.cos(radian));
		// q.normalize();
		return q;
	}

	/**
	 * As the C++ sprintf method, except that it returns the string.
	 */
	public final static String sprintf(String format, Object... obj) {
		return (new Formatter()).format(format, obj).toString();
	}

	public static final void printIntegerArray(int[] data, int wrap) {
		for(int i = 0; i < data.length; ++i) {
			System.out.printf("%i ", data[i]);
			if(((i + 1) % wrap) == 0 && i + 1 != data.length)
				System.out.println();
		}
		System.out.println();
	}

	public static final void printDoubleArray(double[] data, int wrap) {
		for(int i = 0; i < data.length; ++i) {
			System.out.printf("%.4f ", data[i]);
			if(((i + 1) % wrap) == 0 && i + 1 != data.length)
				System.out.println();
		}
		System.out.println();
	}

	private Utils() {}
}

