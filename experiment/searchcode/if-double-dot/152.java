/*
 * Copyright (C) 2010 Klaus Reimer <k@ailis.de>
 * See LICENSE.txt for licensing information.
 */

package de.ailis.gramath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;


/**
 * Base class for vectors with two double elements.
 *
 * @author Klaus Reimer (k@ailis.de)
 */

public abstract class Vector2d extends Vector
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The X coordinate. */
    protected double x;

    /** The Y coordinate. */
    protected double y;

    /** The buffer representation of the vector. */
    private transient DoubleBuffer buffer;

    /** If cached length is valid. */
    private boolean lengthValid = false;

    /** The cached vector length. */
    private double length;


    /**
     * Constructs an uninitialized vector.
     */

    protected Vector2d()
    {
        // Empty
    }


    /**
     * Constructs a new vector with the specified elements.
     *
     * @param x
     *            The X coordinate
     * @param y
     *            The Y coordinate
     */

    public Vector2d(final double x, final double y)
    {
        this.x = x;
        this.y = y;
    }


    /**
     * Constructs a new vector from the elements of the specified point.
     *
     * @param point
     *            The point to copy the elements from.
     */

    public Vector2d(final Point2d point)
    {
        this.x = point.x;
        this.y = point.y;
    }


    /**
     * Constructs a new vector from the elements of the specified point.
     *
     * @param point
     *            The point to copy the elements from.
     */

    public Vector2d(final Point2f point)
    {
        this.x = point.x;
        this.y = point.y;
    }


    /**
     * Constructs a new vector from the elements of the specified point. The
     * Z coordinate of the point is lost.
     *
     * @param point
     *            The point to copy the elements from.
     */

    public Vector2d(final Point3d point)
    {
        this.x = point.x;
        this.y = point.y;
    }


    /**
     * Constructs a new vector from the elements of the specified point. The
     * Z coordinate of the point is lost.
     *
     * @param point
     *            The point to copy the elements from.
     */

    public Vector2d(final Point3f point)
    {
        this.x = point.x;
        this.y = point.y;
    }


    /**
     * Constructs a new vector from the elements of the specified vector.
     *
     * @param vector
     *            The vector to copy the elements from.
     */

    public Vector2d(final Vector2d vector)
    {
        this.x = vector.x;
        this.y = vector.y;
    }


    /**
     * Constructs a new vector from the elements of the specified vector.
     *
     * @param vector
     *            The vector to copy the elements from.
     */

    public Vector2d(final Vector2f vector)
    {
        this.x = vector.x;
        this.y = vector.y;
    }


    /**
     * Constructs a new vector from the elements of the specified vector. The
     * Z coordinate of the vector is lost.
     *
     * @param vector
     *            The vector to copy the elements from.
     */

    public Vector2d(final Vector3d vector)
    {
        this.x = vector.x;
        this.y = vector.y;
    }


    /**
     * Constructs a new vector from the elements of the specified vector. The
     * Z coordinate of the vector is lost.
     *
     * @param vector
     *            The vector to copy the elements from.
     */

    public Vector2d(final Vector3f vector)
    {
        this.x = vector.x;
        this.y = vector.y;
    }


    /**
     * Creates a new empty instance of the vector class.
     *
     * @return A new empty instance. Never null.
     */

    protected abstract Vector2d newInstance();


    /**
     * Creates a new empty instance of the vector with the same type as the
     * specified vector. The main purpose of this method is extracting the
     * one-and-only type-check warning into a single method so only this single
     * line needs to be annotated with SuppressWarnings.
     *
     * @param <T>
     *            The vector type
     * @param v
     *            The vector to use as a template
     * @return The new empty vector
     */

    @SuppressWarnings("unchecked")
    private static <T extends Vector2d> T newInstance(final T v)
    {
        return (T) v.newInstance();
    }


    /**
     * Invalidates the vector so cached values will be re-calculated.
     */

    protected final void invalidate()
    {
        this.lengthValid = false;
    }


    /**
     * Adds vector a and vector b and stores the result into the specified
     * result vector. It is safe to use one of the source vectors as result
     * vector.
     *
     * @param <T>
     *            The result vector type
     * @param a
     *            The first vector.
     * @param b
     *            The second vector.
     * @param result
     *            The result vector.
     * @return The result vector.
     */

    public static <T extends Vector2d> T add(final Vector2d a,
        final Vector2d b, final T result)
    {
        result.x = a.x + b.x;
        result.y = a.y + b.y;
        result.invalidate();
        return result;
    }


    /**
     * Subtracts vector b from vector a and stores the result into the specified
     * result vector. It is safe to use one of the source vectors as result
     * vector.
     *
     * @param <T>
     *            The result vector type
     * @param a
     *            The first vector.
     * @param b
     *            The second vector.
     * @param result
     *            The result vector.
     * @return The result vector.
     */

    public static <T extends Vector2d> T sub(final Vector2d a,
        final Vector2d b, final T result)
    {
        result.x = a.x - b.x;
        result.y = a.y - b.y;
        result.invalidate();
        return result;
    }


    /**
     * Normalizes the specified vector and stores the result in the specified
     * result vector. It is safe the use the source vector as result vector. If
     * source vector is a null vector then result vector is also a null vector.
     *
     * @param <T>
     *            The result vector type.
     * @param vector
     *            The vector to normalize. Must not be null.
     * @param result
     *            The result vector. Must not be null.
     * @return The result vector. Never null.
     */

    public static <T extends Vector2d> T normalize(final Vector2d vector,
        final T result)
    {
        final double l = vector.getLength();
        result.x = l == 0 ? 0 : vector.x / l;
        result.y = l == 0 ? 0 : vector.y / l;
        result.invalidate();
        return result;
    }


    /**
     * Negates the specified vector and stores the result in the specified
     * result vector. It is safe the use the source vector as result vector.
     *
     * @param <T>
     *            The result vector type.
     * @param vector
     *            The vector to negate. Must not be null.
     * @param result
     *            The result vector. Must not be null.
     * @return The result vector. Never null.
     */

    public static <T extends Vector2d> T negate(final Vector2d vector,
        final T result)
    {
        result.x = -vector.x;
        result.y = -vector.y;
        result.invalidate();
        return result;
    }


    /**
     * Transforms the specified vector with the specified matrix and stores the
     * result in the specified result vector. It is safe the use the source
     * vector as result vector.
     *
     * @param <T>
     *            The result vector type.
     * @param vector
     *            The vector to transform. Must not be null.
     * @param matrix
     *            The transformation matrix
     * @param result
     *            The result vector. Must not be null.
     * @return The result vector. Never null.
     */

    public static <T extends Vector2d> T transform(final Vector2d vector,
        final Matrix3d matrix, final T result)
    {
        final double[] m = matrix.m;
        final double x = vector.x;
        final double y = vector.y;
        result.x = m[0] * x + m[3] * y + m[6];
        result.y = m[1] * x + m[4] * y + m[7];
        result.invalidate();
        return result;
    }


    /**
     * Rotates the specified vector and stores the result in the specified
     * result vector. It is safe the use the source vector as result vector.
     *
     * @param <T>
     *            The result vector type.
     * @param vector
     *            The vector to transform. Must not be null.
     * @param angle
     *            The rotation angle in anti-clockwise RAD.
     * @param result
     *            The result vector. Must not be null.
     * @return The result vector. Never null.
     */

    public static <T extends Vector2d> T rotate(final Vector2d vector,
        final double angle, final T result)
    {
        final double s = Math.sin(angle);
        final double c = Math.cos(angle);
        final double x = vector.x;
        final double y = vector.y;
        result.x = x * c - y * s;
        result.y = x * s + y * c;
        result.invalidate();
        return result;
    }


    /**
     * Scales the specified vector and stores the result in the specified result
     * vector. It is safe the use the source vector as result vector.
     *
     * @param <T>
     *            The result vector type.
     * @param vector
     *            The vector to transform. Must not be null.
     * @param s
     *            The scale factor for both axes.
     * @param result
     *            The result vector. Must not be null.
     * @return The result vector. Never null.
     */

    public static <T extends Vector2d> T scale(final Vector2d vector,
        final double s, final T result)
    {
        result.x = vector.x * s;
        result.y = vector.y * s;
        result.invalidate();
        return result;
    }


    /**
     * Scales the specified vector and stores the result in the specified result
     * vector. It is safe the use the source vector as result vector.
     *
     * @param <T>
     *            The result vector type.
     * @param vector
     *            The vector to transform. Must not be null.
     * @param sx
     *            The X scale factor.
     * @param sy
     *            The Y scale factor.
     * @param result
     *            The result vector. Must not be null.
     * @return The result vector. Never null.
     */

    public static <T extends Vector2d> T scale(final Vector2d vector,
        final double sx, final double sy, final T result)
    {
        result.x = vector.x * sx;
        result.y = vector.y * sy;
        result.invalidate();
        return result;
    }


    /**
     * Scales the specified vector along the X axis and stores the result in the
     * specified result vector. It is safe the use the source vector as result
     * vector.
     *
     * @param <T>
     *            The result vector type.
     * @param vector
     *            The vector to transform. Must not be null.
     * @param s
     *            The scale factor.
     * @param result
     *            The result vector. Must not be null.
     * @return The result vector. Never null.
     */

    public static <T extends Vector2d> T scaleX(final Vector2d vector,
        final double s, final T result)
    {
        result.x = vector.x * s;
        result.y = vector.y;
        result.invalidate();
        return result;
    }


    /**
     * Scales the specified vector along the Y axis and stores the result in the
     * specified result vector. It is safe the use the source vector as result
     * vector.
     *
     * @param <T>
     *            The result vector type.
     * @param vector
     *            The vector to transform. Must not be null.
     * @param s
     *            The scale factor.
     * @param result
     *            The result vector. Must not be null.
     * @return The result vector. Never null.
     */

    public static <T extends Vector2d> T scaleY(final Vector2d vector,
        final double s, final T result)
    {
        result.x = vector.x;
        result.y = vector.y * s;
        result.invalidate();
        return result;
    }


    /**
     * Translates the specified vector and stores the result in the specified
     * result vector. It is safe the use the source vector as result vector.
     *
     * @param <T>
     *            The result vector type.
     * @param vector
     *            The vector to transform. Must not be null.
     * @param tx
     *            The X translation.
     * @param ty
     *            The Y translation.
     * @param result
     *            The result vector. Must not be null.
     * @return The result vector. Never null.
     */

    public static <T extends Vector2d> T translate(final Vector2d vector,
        final double tx, final double ty, final T result)
    {
        result.x = vector.x + tx;
        result.y = vector.y + ty;
        result.invalidate();
        return result;
    }


    /**
     * Translates the specified vector along the X axis and stores the result in
     * the specified result vector. It is safe the use the source vector as
     * result vector.
     *
     * @param <T>
     *            The result vector type.
     * @param vector
     *            The vector to transform. Must not be null.
     * @param t
     *            The translation.
     * @param result
     *            The result vector. Must not be null.
     * @return The result vector. Never null.
     */

    public static <T extends Vector2d> T translateX(final Vector2d vector,
        final double t, final T result)
    {
        result.x = vector.x + t;
        result.y = vector.y;
        result.invalidate();
        return result;
    }


    /**
     * Translates the specified vector along the Y axis and stores the result in
     * the specified result vector. It is safe the use the source vector as
     * result vector.
     *
     * @param <T>
     *            The result vector type.
     * @param vector
     *            The vector to transform. Must not be null.
     * @param t
     *            The translation.
     * @param result
     *            The result vector. Must not be null.
     * @return The result vector. Never null.
     */

    public static <T extends Vector2d> T translateY(final Vector2d vector,
        final double t, final T result)
    {
        result.x = vector.x;
        result.y = vector.y + t;
        result.invalidate();
        return result;
    }


    /**
     * @see java.lang.Object#toString()
     */

    @Override
    public final String toString()
    {
        return "[ " + this.x + ", " + this.y + " ]";
    }


    /**
     * Returns the direct NIO double buffer in native ordering containing the
     * vector elements. The returned double buffer is cached and mutable but
     * modifications do not modify the vector class itself.
     *
     * @return The vector as a direct NIO double buffer. Never null.
     */

    public final DoubleBuffer getBuffer()
    {
        if (this.buffer == null)
            this.buffer = ByteBuffer.allocateDirect(2 * 8)
                .order(ByteOrder.nativeOrder()).asDoubleBuffer();
        this.buffer.rewind();
        this.buffer.put(this.x).put(this.y);
        this.buffer.rewind();
        return this.buffer;
    }


    /**
     * Returns the X coordinate.
     *
     * @return The X coordinate.
     */

    public final double getX()
    {
        return this.x;
    }


    /**
     * Returns the Y coordinate.
     *
     * @return The Y coordinate.
     */

    public final double getY()
    {
        return this.y;
    }


    /**
     * Returns the length of the vector. This length is automatically cached in
     * the vector and automatically invalidated when the vector is changed.
     *
     * @return The length of the vector.
     */

    public final double getLength()
    {
        if (!this.lengthValid)
        {
            this.length = Math.sqrt(this.x * this.x + this.y
                * this.y);
            this.lengthValid = true;
        }
        return this.length;
    }


    /**
     * Returns the normalization of this vector.
     *
     * @return This normalization of this vector. Never null.
     */

    public abstract Vector2d getNormalization();


    /**
     * Returns the dot product between this vector and the specified vector.
     *
     * @param v
     *            The vector to dot-multiplicate this one with. Must not be
     *            null.
     * @return The dot product.
     */

    public final double dot(final Vector2d v)
    {
        return dot(this, v);
    }


    /**
     * Returns the angle between this vector and the specified one.
     *
     * @param v
     *            The other vector Must not be null.
     * @return The angle in RAD.
     */

    public final double angle(final Vector2d v)
    {
        return Math.acos(getNormalization().dot(v.getNormalization()));
    }


    /**
     * @see de.ailis.gramath.Vector#isNull()
     */

    @Override
    public final boolean isNull()
    {
        return this.x == 0 && this.y == 0;
    }


    /**
     * Returns the dot product between the two specified vectors.
     *
     * @param a
     *            The first vector. Must not be null.
     * @param b
     *            The second vector. Must not be null.
     * @return The dot product. Never null.
     */

    public static double dot(final Vector2d a, final Vector2d b)
    {
        return a.x * b.x + a.y * b.y;
    }


    /**
     * Adds the specified vectors and returns a new vector with the result. The
     * returned vector has the same type as the first specified vector.
     *
     * @param <T>
     *            The result vector type
     * @param a
     *            The first vector. Most not be null.
     * @param b
     *            The second vector. Most not be null.
     * @return The result vector. Never null.
     */

    public static <T extends Vector2d> T add(final T a, final Vector2d b)
    {
        return add(a, b, newInstance(a));
    }


    /**
     * Subtracts vector b from vector a and returns a new vector with the
     * result. The returned vector has the same type as the first specified
     * vector.
     *
     * @param <T>
     *            The result vector type.
     * @param a
     *            The first vector. Most not be null.
     * @param b
     *            The second vector. Most not be null.
     * @return The result vector. Never null.
     */

    public static <T extends Vector2d> T sub(final T a, final Vector2d b)
    {
        return sub(a, b, newInstance(a));
    }


    /**
     * Negates the specified vector and returns a new vector with the result.
     *
     * @param <T>
     *            The vector type
     * @param vector
     *            The vector to negate. Must not be null.
     * @return The negated vector. Never null.
     */

    public static <T extends Vector2d> T negate(final T vector)
    {
        return negate(vector, newInstance(vector));
    }


    /**
     * Rotates the specified vector and returns a new vector with the result.
     *
     * @param <T>
     *            The vector type.
     * @param vector
     *            The vector to rotate. Must not be null.
     * @param angle
     *            The rotation angle in anti-clockwise RAD.
     * @return The rotated vector. Never null.
     */

    public static <T extends Vector2d> T rotate(final T vector,
        final double angle)
    {
        return rotate(vector, angle, newInstance(vector));
    }


    /**
     * Scales the specified vector and returns a new vector with the result.
     *
     * @param <T>
     *            The vector type.
     * @param vector
     *            The vector to scale. Must not be null.
     * @param s
     *            The scale factor.
     * @return The scaled vector. Never null.
     */

    public static <T extends Vector2d> T scale(final T vector,
        final double s)
    {
        return scale(vector, s, newInstance(vector));
    }


    /**
     * Scales the specified vector and returns a new vector with the result.
     *
     * @param <T>
     *            The vector type.
     * @param vector
     *            The vector to scale. Must not be null.
     * @param sx
     *            The X scale factor.
     * @param sy
     *            The Y scale factor.
     * @return The scaled vector. Never null.
     */

    public static <T extends Vector2d> T scale(final T vector,
        final double sx, final double sy)
    {
        return scale(vector, sx, sy, newInstance(vector));
    }


    /**
     * Scales the specified vector along the X axis and returns a new vector
     * with the result.
     *
     * @param <T>
     *            The vector type.
     * @param vector
     *            The vector to scale. Must not be null.
     * @param s
     *            The scale factor.
     * @return The scaled vector. Never null.
     */

    public static <T extends Vector2d> T scaleX(final T vector,
        final double s)
    {
        return scaleX(vector, s, newInstance(vector));
    }


    /**
     * Scales the specified vector along the Y axis and returns a new vector
     * with the result.
     *
     * @param <T>
     *            The vector type.
     * @param vector
     *            The vector to scale. Must not be null.
     * @param s
     *            The scale factor.
     * @return The scaled vector. Never null.
     */

    public static <T extends Vector2d> T scaleY(final T vector,
        final double s)
    {
        return scaleY(vector, s, newInstance(vector));
    }


    /**
     * Translates the specified vector and returns a new vector with the result.
     *
     * @param <T>
     *            The vector type.
     * @param vector
     *            The vector to translate. Must not be null.
     * @param tx
     *            The X translation.
     * @param ty
     *            The Y translation.
     * @return The translated vector. Never null.
     */

    public static <T extends Vector2d> T translate(final T vector,
        final double tx, final double ty)
    {
        return translate(vector, tx, ty, newInstance(vector));
    }


    /**
     * Translates the specified vector along the X axis and returns a new vector
     * with the result.
     *
     * @param <T>
     *            The vector type.
     * @param vector
     *            The vector to translate. Must not be null.
     * @param t
     *            The translation.
     * @return The translated vector. Never null.
     */

    public static <T extends Vector2d> T translateX(final T vector,
        final double t)
    {
        return translateX(vector, t, newInstance(vector));
    }


    /**
     * Translates the specified vector along the Y axis and returns a new vector
     * with the result.
     *
     * @param <T>
     *            The vector type.
     * @param vector
     *            The vector to translate. Must not be null.
     * @param t
     *            The translation.
     * @return The translated vector. Never null.
     */

    public static <T extends Vector2d> T translateY(final T vector,
        final double t)
    {
        return translateY(vector, t, newInstance(vector));
    }


    /**
     * Transforms the specified vector with the specified matrix and returns a
     * new vector with the result.
     *
     * @param <T>
     *            The vector type.
     * @param v
     *            The vector to transform.
     * @param m
     *            The transformation matrix.
     * @return The result vector.
     */

    public static <T extends Vector2d> T transform(final T v, final Matrix3d m)
    {
        return transform(v, m, v);
    }


    /**
     * @see java.lang.Object#hashCode()
     */

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(this.x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Vector2d other = (Vector2d) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y))
            return false;
        return true;
    }
}

