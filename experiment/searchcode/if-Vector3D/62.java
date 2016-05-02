package org.bitbucket.kensho.kepler;
import static java.lang.Math.acos;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static org.bitbucket.kensho.kepler.Astrodynamics.HALF_PI;
import static org.bitbucket.kensho.kepler.Astrodynamics.PI2;

/**
 * this class represents a vector in 
 * 3-dimensional space using double precision
 * 
 * it can represent cartesian and spherical coordinates
 * and convert between these
 */
public class Vector3d
{
	public static final Vector3d ZERO = new Vector3d(0, 0, 0);
	public static final Vector3d UNIT_X = new Vector3d(1, 0, 0);
	public static final Vector3d UNIT_Y = new Vector3d(0, 1, 0);
	public static final Vector3d UNIT_Z = new Vector3d(0, 0, 1);
	
	public double x, y, z;
	
	/**
	 * create the identity vector
	 */
	public Vector3d()
	{
		this(0,0,0);
	}
	
	/**
	 * create a new vector by copying the values
	 * of the specified vector
	 * 
	 * @param vec
	 */
	public Vector3d(Vector3d vec)
	{
		this(vec.x, vec.y, vec.z);
	}
	
	/**
	 * create a new vector with the specified coordinates
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vector3d(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3d set(Vector3d v)
	{
		x = v.x;
		y = v.y;
		z = v.z;
		
		return this;
	}
	
	public Vector3d set(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		
		return this;
	}
	
	/**
	 * add the specified values to this vector
	 * 
	 * @param vec
	 * @return this vector
	 */
	public Vector3d add(double x, double y, double z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
		
		return this;
	}
	
	/**
	 * add the given vector to this vector
	 * 
	 * @param vec
	 * @return this vector
	 */
	public Vector3d add(Vector3d vec)
	{
		x += vec.x;
		y += vec.y;
		z += vec.z;
		
		return this;
	}
	
	/**
	 * subtract the given vector from this vector
	 * 
	 * @param vec
	 * @return this vector
	 */
	public Vector3d subtract(Vector3d vec)
	{
		x -= vec.x;
		y -= vec.y;
		z -= vec.z;
		
		return this;
	}
	
	/**
	 * negate this vector
	 * 
	 * @return this vector
	 */
	public Vector3d negate()
	{
		x = -x;
		y = -y;
		z = -z;
		
		return this;
	}
	
	/**
	 * normalize this vector
	 * 
	 * @return this vector
	 */
	public Vector3d normalize()
	{
		divide(length());
		
		return this;
	}
	
	/**
	 * get the length of this vector
	 * 
	 * @return this vectors length
	 */
	public double length()
	{
		return sqrt(lengthSquared());
	}
	
	/**
	 * get the squared length of this vector
	 * 
	 * @return the length squared
	 */
	public double lengthSquared()
	{
		return x * x + y * y + z * z;
	}
	
	/**
	 * multiply this vector with the given scalar
	 * 
	 * @param scalar
	 * @return this vector
	 */
	public Vector3d mult(double scalar)
	{
		x = x * scalar;
		y = y * scalar;
		z = z * scalar;
		
		return this;
	}
	
	/**
	 * divide this vector by the given scalar
	 * 
	 * @param scalar
	 * @return this vector
	 */
	public Vector3d divide(double scalar)
	{
		x = x / scalar;
		y = y / scalar;
		z = z / scalar;
		
		return this;
	}
	
	/**
	 * calculate the dot product of this vector 
	 * with the specified vector
	 * 
	 * @param vec
	 * @return the dot product
	 */
	public double dot(Vector3d vec)
	{
		return x * vec.x + y * vec.y + z * vec.z;
	}
	
	/**
	 * calculate the cross product of this
	 * vector and the supplied vector
	 * 
	 * @param vec
	 * @return the new vector
	 */
	public Vector3d cross(Vector3d vec)
	{
		double xCross, yCross, zCross;
		
		xCross = y * vec.z - z * vec.y;
		yCross = z * vec.x - x * vec.z;
		zCross = x * vec.y - y * vec.x;
		
		return new Vector3d(xCross, yCross, zCross);
	}
	
	/**
	 * convert this vector to cartesian coordinates
	 * 
	 * @return this vector
	 */
	public Vector3d toCartesian()
	{
		double r, theta, phi;
		r = x; theta = y; phi = z;
		
		x = r * sin(theta) * cos(phi);
		y = r * sin(theta) * sin(phi);
		z = r * cos(theta);
		
		return this;
	}
	
	/**
	 * convert this vector to spherical coordinates
	 * 
	 * x -> r (length), 
	 * y -> theta (angle between +z-axis and r), 
	 * z -> phi (angle between +x-axis and r)
	 * 
	 * @return this vector
	 */
	public Vector3d toSpherical()
	{
		double x2, y2, z2, r, theta, phi;
		
		x2 = pow(x,2); y2 = pow(y,2); z2 = pow(z,2);
		
		r = sqrt(x2 + y2 + z2);
		
		if (y >= 0)
		{
			phi = acos( x / sqrt(x2 + y2));
		}
		else
		{
			phi = PI2 - acos( x / sqrt(x2 + y2));
		}
		
		theta = HALF_PI - atan( z / sqrt(x2 + y2));
		
		x = r;
		y = theta;
		z = phi;
		
		return this;
	}
	
	/**
	 * multiply this vector with the specified matrix
	 * 
	 * @param matrix
	 * @return this vector
	 */
	public Vector3d mult(double[][] matrix)
	{
		if (matrix.length != 3 || matrix[0].length != 3)
			throw new IllegalArgumentException();
		
		double xTmp, yTmp, zTmp;
		
		xTmp = matrix[0][0] * x + matrix[0][1] * y + matrix[0][2] * z;
		yTmp = matrix[1][0] * x + matrix[1][1] * y + matrix[1][2] * z;
		zTmp = matrix[2][0] * x + matrix[2][1] * y + matrix[2][2] * z;
		
		x = xTmp; y = yTmp; z = zTmp;
		
		return this;
	}

	public Vector3d rotateXAxis(double phi)
	{
		double sin = Math.sin(phi);
		double cos = Math.cos(phi);

		return mult(new double[][] {
			{ 1.0, 	0.0, 	0.0 },
			{ 0.0, 	cos, 	-sin },
			{ 0.0, 	sin, 	cos }
		});
	}
	
	public Vector3d rotateYAxis(double phi)
	{
		double sin = Math.sin(phi);
		double cos = Math.cos(phi);

		return mult(new double[][] {
			{ cos, 	0.0, 	sin },
			{ 0.0, 	1.0, 	0.0 },
			{ -sin,	0.0, 	cos }
		});
	}
	
	public Vector3d rotateZAxis(double phi)
	{
		double sin = Math.sin(phi);
		double cos = Math.cos(phi);

		return mult(new double[][] {
			{ cos, 	-sin, 	0.0 },
			{ sin, 	cos, 	0.0 },
			{ 0.0, 	0.0, 	1.0 }
		});
	}
	
	public String toString()
	{
		return "[" + x + ", " + y + ", " + z + "]";
	}
}

