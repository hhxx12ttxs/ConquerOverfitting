/********************************************************************************
 * @author bherila
 * File:   Vector.java
 * @date   7 Feb 2011
 * Asgn:   Solar
 ********************************************************************************/
package solar;

import java.util.Random;

/**
 * Vector is a class used for x,y, and z components
 * within solar.  It makes it easier as x,y, and z
 * are usually used in grouping.
 */
public class Vector3 implements IPositionable {

    private final double m_x,  m_y,  m_z;
    private static Vector3 m_zero = new Vector3(0, 0, 0);
    private static Random m_rand = new Random();

    /**
     * create a vector with position 0,0,0
     */
    private Vector3() {
        m_x = 0;
        m_y = 0;
        m_z = 0;
    }

    /**
     * create a vector with position x,y,z
     * @param x
     * @param y
     * @param z
     */
    private Vector3(double x, double y, double z) {
        m_x = x;
        m_y = y;
        m_z = z;
    }

    public double getX() {
        return m_x;
    }

    public double getY() {
        return m_y;
    }

    public double getZ() {
        return m_z;
    }

    /**
     * Gets the zero vector
     */
    public static Vector3 zero() {
        return m_zero;
    }

    /**
     * Gets a vector -- Why don't we use a constructor here? We can save
     * memory by reusing the static zero vector. 
     */
    public static Vector3 create(double x, double y, double z) {
        if (x * x + y * y + z * z < 1e-6) {
            return zero();
        } else {
            return new Vector3(x, y, z);
        }
    }

    /**
     * Returns a random vector.
     * @return
     */
    public static Vector3 getRandom() {
        return new Vector3(
                m_rand.nextDouble(),
                m_rand.nextDouble(),
                m_rand.nextDouble());
    }

    /**
     * Multiplies each element of this vector by a constant and returns the
     * result. (does not modify this vector)
     * @param scaleFactor
     * @return
     */
    public Vector3 scalarMultiply(double scaleFactor) {
        return new Vector3(
                m_x * scaleFactor,
                m_y * scaleFactor,
                m_z * scaleFactor);
    }

    /**
     * Returns the sum of this vector and another ('addend')
     */
    public Vector3 add(Vector3 addend) {
        return new Vector3(
                m_x + addend.m_x,
                m_y + addend.m_x,
                m_z + addend.m_z);
    }

    /*
     * Returns the dot product of this and another vector.
     */
    public double dot(Vector3 other) {
        return m_x * other.m_x + m_y * other.m_y + m_z * other.m_z;
    }

    public Vector3 cross(Vector3 o) {
        return new Vector3(
                m_y * o.m_z - m_z * o.m_y,
                m_z * o.m_x - m_x * o.m_z,
                m_x * o.m_y - m_y * o.m_x);
    }

    /*
     * Returns the difference of this vector and another by subtracting
     * 'subtrahend' on the right.
     */
    public Vector3 subtract(Vector3 subtrahend) {
        return new Vector3(
                m_x - subtrahend.m_x,
                m_y - subtrahend.m_y,
                m_z - subtrahend.m_z);
    }

    /*
     * Returns the magnitude (or length) of this Vector3, a double representing
     * its length
     */
    public double magnitude() {
        return Math.sqrt(m_x * m_x + m_y * m_y + m_z * m_z);
    }

    /*
     * Returns the magnitude of this Vector3, a double representing
     * its length; faster than magnitude because it doesn't have the sqrt
     * call.
     */
    public double magnitude2() {
        return (m_x * m_x + m_y * m_y + m_z * m_z);
    }

    /**
     * Returns a vector of length 1 which points in the direction of this vector.
     * The zero vector normalized is still the zero vector.
     * @return
     */
    public Vector3 getNormalized() {
        double m = magnitude2();
        return (m < 1e-6) ? m_zero : scalarMultiply(1.0 / Math.sqrt(m));
    }

    /**
     * Compares a vector for exact equality (note that due to floating-point
     * roudning errors introduced in calculations, two vectors that should
     * mathematically be equal might differ slightly, in which case this will
     * return false.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Vector3) {
            Vector3 v = (Vector3) o;
            return m_x == v.m_x && m_y == v.m_y && m_z == v.m_z;
        }
        return false;
    }

    /**
     * @return this vector as a "(x, y, z)" string, for debugging purposes.
     */
    @Override
    public String toString() {
        return "(" + m_x + ", " + m_y + ", " + m_z + ")";
    }
}

