/*
 * Copyright (C) 2010 Klaus Reimer <k@ailis.de>
 * See LICENSE.txt for licensing information.
 */

package de.ailis.gramath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.util.Arrays;


/**
 * Base class for all 3x3 double matrices.
 *
 * @author Klaus Reimer (k@ailis.de)
 */

public abstract class Matrix3d extends Matrix
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The internal representation of the matrix. */
    protected double[] m;

    /** The NIO buffer representation of the matrix. */
    private transient DoubleBuffer buffer;


    /**
     * Creates a new uninitialized matrix.
     */

    protected Matrix3d()
    {
        this.m = new double[9];
    }


    /**
     * Constructs a new matrix with the specified values.
     *
     * @param values
     *            The matrix values (Must have at least 9 values)
     */

    public Matrix3d(final double... values)
    {
        this(values, 0);
    }


    /**
     * Constructs a new matrix with the elements from the specified matrix.
     *
     * @param matrix
     *            The matrix to copy the elements from. Must not be null.
     */

    public Matrix3d(final Matrix3d matrix)
    {
        this(matrix.m, 0);
    }


    /**
     * Constructs a new matrix with the elements from the specified matrix.
     *
     * @param matrix
     *            The matrix to copy the elements from. Must not be null.
     */

    public Matrix3d(final Matrix3f matrix)
    {
        this();
        final double[] a = this.m;
        final float[] b = matrix.m;
        a[0] = b[0];
        a[1] = b[1];
        a[2] = b[2];
        a[3] = b[3];
        a[4] = b[4];
        a[5] = b[5];
        a[6] = b[6];
        a[7] = b[7];
        a[8] = b[8];
    }


    /**
     * Constructs a new matrix with the elements from the specified matrix.
     *
     * @param matrix
     *            The matrix to copy the elements from. Must not be null.
     */

    public Matrix3d(final Matrix4d matrix)
    {
        this();
        final double[] a = this.m;
        final double[] b = matrix.m;
        a[0] = b[0];
        a[1] = b[1];
        a[2] = b[2];
        a[3] = b[4];
        a[4] = b[5];
        a[5] = b[6];
        a[6] = b[8];
        a[7] = b[9];
        a[8] = b[10];
    }


    /**
     * Constructs a new matrix with the elements from the specified matrix.
     *
     * @param matrix
     *            The matrix to copy the elements from. Must not be null.
     */

    public Matrix3d(final Matrix4f matrix)
    {
        this();
        final double[] a = this.m;
        final float[] b = matrix.m;
        a[0] = b[0];
        a[1] = b[1];
        a[2] = b[2];
        a[3] = b[4];
        a[4] = b[5];
        a[5] = b[6];
        a[6] = b[8];
        a[7] = b[9];
        a[8] = b[10];
    }


    /**
     * Constructs a new matrix with values read from the specified array
     * beginning at the given index.
     *
     * @param values
     *            The matrix values (Must have at least index+9 values)
     * @param index
     *            The start index
     */

    public Matrix3d(final double[] values, final int index)
    {
        this();
        if (values.length - index < 9)
            throw new IllegalArgumentException(
                "Not enough matrix elements specified");
        System.arraycopy(values, index, this.m, 0, 9);
    }


    /**
     * Creates a new empty instance of the matrix class.
     *
     * @return A new empty instance. Never null.
     */

    protected abstract Matrix3d newInstance();


    /**
     * Creates a new empty instance of the matrix with the same type as the
     * specified matrix. The main purpose of this method is extracting the
     * one-and-only type-check warning into a single method so only this single
     * line needs to be annotated with SuppressWarnings.
     *
     * @param <T>
     *            The matrix type
     * @param m
     *            The matrix to use as a template
     * @return The new empty matrix
     */

    @SuppressWarnings("unchecked")
    private static <T extends Matrix3d> T newInstance(final T m)
    {
        return (T) m.newInstance();
    }


    /**
     * Returns the determinant of the matrix.
     *
     * @return The determinant of the matrix
     */

    public abstract double getDeterminant();


    /**
     * @see java.lang.Object#toString()
     */

    @Override
    public final String toString()
    {
        final double[] m = this.m;
        return "[[ " + m[0] + ", " + m[3] + ", " + m[6] + " ], "
             + "[ " + m[1] + ", " + m[4] + ", " + m[7] + " ], "
             + "[ " + m[2] + ", " + m[5] + ", " + m[8] + " ]]";
    }


    /**
     * @see java.lang.Object#hashCode()
     */

    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.m);
        return result;
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */

    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Matrix3d other = (Matrix3d) obj;
        if (!Arrays.equals(this.m, other.m)) return false;
        return true;
    }


    /**
     * Calculates the adjoint matrix from matrix m and stores the result in
     * matrix r. All matrices are represented as double arrays with 9 elements.
     * It is safe to specify the source matrix as result matrix.
     *
     * @param m
     *            The matrix to calculate the adjoint matrix from.
     * @param r
     *            The matrix to store the result in.
     * @return The result matrix.
     */

    public static final double[] cofactor(final double[] m, final double[] r)
    {
        final double r0 = m[4] * m[8] - m[5] * m[7];
        final double r3 = m[2] * m[7] - m[1] * m[8];
        final double r6 = m[1] * m[5] - m[2] * m[4];

        final double r1 = m[5] * m[6] - m[3] * m[8];
        final double r4 = m[0] * m[8] - m[2] * m[6];
        final double r7 = m[2] * m[3] - m[0] * m[5];

        r[2] = m[3] * m[7] - m[4] * m[6];
        r[5] = m[1] * m[6] - m[0] * m[7];
        r[8] = m[0] * m[4] - m[1] * m[3];

        r[0] = r0;
        r[1] = r1;
        r[3] = r3;
        r[4] = r4;
        r[6] = r6;
        r[7] = r7;

        return r;
    }


    /**
     * Converts the specified matrix to its cofactor matrix.
     *
     * @param m
     *            The matrix to convert.
     * @return The matric for chaining.
     */

    public static final double[] cofactor(final double[] m)
    {
        return cofactor(m, m);
    }


    /**
     * Calculates the adjoint matrix from matrix m and stores the result in
     * matrix r. All matrices are represented as double arrays with 9 elements.
     * It is safe to specify the source matrix as result matrix.
     *
     * @param m
     *            The matrix to calculate the adjoint matrix from.
     * @param r
     *            The matrix to store the result in.
     * @return The result matrix.
     */

    public static final double[] adjoint(final double[] m, final double[] r)
    {
        return transpose(cofactor(m, r));
    }


    /**
     * Converts the specified matrix to the adjoint matrix.
     *
     * @param m
     *            The matrix to covert.
     * @return The matrix for chaining.
     */

    public static final double[] adjoint(final double[] m)
    {
        return adjoint(m, m);
    }


    /**
     * Multiplies the matrix a with matrix b and stores the result in matrix c.
     * All matrices are represented as double arrays with 9 elements. It is safe
     * to specify one of the source matrices as the result matrix.
     *
     * @param a
     *            The first matrix
     * @param b
     *            The second matrix
     * @param r
     *            The result matrix
     * @return The result matrix
     */

    public static final double[] multiply(final double[] a,
        final double[] b, final double[] r)
    {
        final double r1, r2, r3, r5, r6, r7;

        r1 = a[1] * b[0] + a[4] * b[1] + a[7] * b[2];
        r2 = a[2] * b[0] + a[5] * b[1] + a[8] * b[2];
        r3 = a[0] * b[3] + a[3] * b[4] + a[6] * b[5];
        r5 = a[2] * b[3] + a[5] * b[4] + a[8] * b[5];
        r6 = a[0] * b[6] + a[3] * b[7] + a[6] * b[8];
        r7 = a[1] * b[6] + a[4] * b[7] + a[7] * b[8];
        r[0] = a[0] * b[0] + a[3] * b[1] + a[6] * b[2];
        r[4] = a[1] * b[3] + a[4] * b[4] + a[7] * b[5];
        r[8] = a[2] * b[6] + a[5] * b[7] + a[8] * b[8];

        r[1] = r1;
        r[2] = r2;
        r[3] = r3;
        r[5] = r5;
        r[6] = r6;
        r[7] = r7;

        return r;
    }


    /**
     * Returns the determinant of the specified matrix.
     *
     * @param m
     *            The matrix as a double array with 9 elements.
     * @return The determinant.
     */

    public static final double determinant(final double[] m)
    {
        return - m[2] * m[4] * m[6] + m[1] * m[5] * m[6]
             + m[2] * m[3] * m[7] - m[0] * m[5] * m[7]
             - m[1] * m[3] * m[8] + m[0] * m[4] * m[8];
    }


    /**
     * Checks if the specified matrix is an identity matrix.
     *
     * @param m
     *            The matrix as a double array with 9 elements.
     * @return True if matrix is an identity matrix, false if not
     */

    public static final boolean isIdentity(final double[] m)
    {
        return m[0] == 1 && m[3] == 0 && m[6] == 0
            && m[1] == 0 && m[4] == 1 && m[7] == 0
            && m[2] == 0 && m[5] == 0 && m[8] == 1;
    }


    /**
     * Sets the matrix to the identity matrix.
     *
     * @param m
     *            The matrix to set to identity.
     * @return The matrix for chaining.
     */

    public static final double[] setIdentity(final double[] m)
    {
        m[0] = 1;
        m[1] = 0;
        m[2] = 0;
        m[3] = 0;
        m[4] = 1;
        m[5] = 0;
        m[6] = 0;
        m[7] = 0;
        m[8] = 1;

        return m;
    }


    /**
     * Transposes matrix m and stores the result in matrix r. It is safe to use
     * the source matrix as result matrix.
     *
     * @param m
     *            The matrix to transpose.
     * @param r
     *            The result matrix.
     * @return The result matrix.
     */

    public static final double[] transpose(final double[] m, final double[] r)
    {
        double r1, r2, r5;

        r[0] = m[0];
        r1 = m[3];
        r2 = m[6];
        r[3] = m[1];
        r[4] = m[4];
        r5 = m[7];
        r[6] = m[2];
        r[7] = m[5];
        r[8] = m[8];

        r[1] = r1;
        r[2] = r2;
        r[5] = r5;

        return r;
    }


    /**
     * Transposes the specified matrix.
     *
     * @param m
     *            The matrix to transpose.
     * @return The matrix for chaining.
     */

    public static final double[] transpose(final double[] m)
    {
        return transpose(m, m);
    }


    /**
     * Sets the matrix m to a rotation matrix.
     *
     * @param m
     *            The matrix as a double array with 9 elements.
     * @param a
     *            The rotation angle in anti-clock-wise RAD.
     * @return The matrix for chaining.
     */

    public static final double[] setRotation(final double[] m, final double a)
    {
        final double s = Math.sin(a);
        final double c = Math.cos(a);
        m[0] = c;
        m[1] = s;
        m[2] = 0;
        m[3] = -s;
        m[4] = c;
        m[5] = 0;
        m[6] = 0;
        m[7] = 0;
        m[8] = 1;
        return m;
    }


    /**
     * Sets the matrix m to a translation matrix.
     *
     * @param m
     *            The matrix as a double array with 9 elements.
     * @param tx
     *            The X translation.
     * @param ty
     *            The Y translation.
     * @return The matrix for chaining.
     */

    public static final double[] setTranslation(final double[] m, final double tx,
        final double ty)
    {
        m[0] = 1;
        m[1] = 0;
        m[2] = 0;
        m[3] = 0;
        m[4] = 1;
        m[5] = 0;
        m[6] = tx;
        m[7] = ty;
        m[8] = 1;
        return m;
    }


    /**
     * Sets the matrix m to a translation matrix along the X axis.
     *
     * @param m
     *            The matrix as a double array with 9 elements.
     * @param t
     *            The translation.
     * @return The matrix for chaining.
     */

    public static final double[] setTranslationX(final double[] m, final double t)
    {
        return setTranslation(m, t, 0);
    }


    /**
     * Sets the matrix m to a translation matrix along the Y axis.
     *
     * @param m
     *            The matrix as a double array with 9 elements.
     * @param t
     *            The translation.
     * @return The matrix for chaining.
     */

    public static final double[] setTranslationY(final double[] m, final double t)
    {
        return setTranslation(m, 0, t);
    }


    /**
     * Sets the matrix m to a scaling matrix.
     *
     * @param m
     *            The matrix as a double array with 9 elements.
     * @param sx
     *            The X scale factor.
     * @param sy
     *            The Y scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] setScaling(final double[] m, final double sx,
        final double sy)
    {
        m[0] = sx;
        m[1] = 0;
        m[2] = 0;
        m[3] = 0;
        m[4] = sy;
        m[5] = 0;
        m[6] = 0;
        m[7] = 0;
        m[8] = 1;
        return m;
    }


    /**
     * Sets the matrix m to a scaling matrix.
     *
     * @param m
     *            The matrix as a double array with 9 elements.
     * @param s
     *            The scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] setScaling(final double[] m, final double s)
    {
        return setScaling(m, s, s);
    }


    /**
     * Sets the matrix m to a scaling matrix along the X axis.
     *
     * @param m
     *            The matrix as a double array with 9 elements.
     * @param s
     *            The scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] setScalingX(final double[] m, final double s)
    {
        return setScaling(m, s, 1);
    }


    /**
     * Sets the matrix m to a scaling matrix along the Y axis.
     *
     * @param m
     *            The matrix as a double array with 9 elements.
     * @param s
     *            The scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] setScalingY(final double[] m, final double s)
    {
        return setScaling(m, 1, s);
    }


    /**
     * Rotates matrix m and stores the result in matrix r.
     *
     * @param m
     *            The matrix to rotate.
     * @param a
     *            The rotation in anti-clockwise RAD.
     * @param r
     *            The storage for the result matrix.
     * @return The result matrix.
     */

    public static final double[] rotate(final double[] m, final double a,
        final double[] r)
    {
        return multiply(m, setRotation(new double[9], a), r);
    }


    /**
     * Rotates the specified matrix.
     *
     * @param m
     *            The matrix to rotate. This matrix is changed in-place.
     * @param a
     *            The rotation in anti-clockwise RAD.
     * @return The matrix for chaining.
     */

    public static final double[] rotate(final double[] m, final double a)
    {
        return rotate(m, a, m);
    }


    /**
     * Translates matrix m and stores the result in matrix r.
     *
     * @param m
     *            The matrix to translate.
     * @param tx
     *            The X translation delta.
     * @param ty
     *            The Y translation delta.
     * @param r
     *            The result matrix.
     * @return The result matrix.
     */

    public static final double[] translate(final double[] m,
        final double tx, final double ty, final double[] r)
    {
        return multiply(m, setTranslation(new double[9], tx, ty), r);
    }


    /**
     * Translates the specified matrix in-place.
     *
     * @param m
     *            The matrix to translate. It is changed in-place.
     * @param tx
     *            The X translation delta.
     * @param ty
     *            The Y translation delta.
     * @return The matrix for chaining.
     */

    public static final double[] translate(final double[] m,
        final double tx, final double ty)
    {
        return translate(m, tx, ty, m);
    }


    /**
     * Translates matrix m along the X axis and stores the result in matrix r.
     *
     * @param m
     *            The matrix to translate.
     * @param t
     *            The translation delta.
     * @param r
     *            The result matrix.
     * @return The result matrix.
     */

    public static final double[] translateX(final double[] m, final double t,
        final double[] r)
    {
        return multiply(m, setTranslationX(new double[9], t), r);
    }


    /**
     * Translates the specified matrix in-place along the X-axis.
     *
     * @param m
     *            The matrix to translate. It is changed in-place.
     * @param t
     *            The translation delta.
     * @return The matrix for chaining.
     */

    public static final double[] translateX(final double[] m, final double t)
    {
        return translateX(m, t, m);
    }


    /**
     * Translates matrix m along the Y axis and stores the result in matrix r.
     *
     * @param m
     *            The matrix to translate.
     * @param t
     *            The translation delta.
     * @param r
     *            The result matrix.
     * @return The result matrix.
     */

    public static final double[] translateY(final double[] m, final double t,
        final double[] r)
    {
        return multiply(m, setTranslationY(new double[9], t), r);
    }


    /**
     * Translates the specified matrix in-place along the Y-axis.
     *
     * @param m
     *            The matrix to translate. It is changed in-place.
     * @param t
     *            The translation delta.
     * @return The matrix for chaining.
     */

    public static final double[] translateY(final double[] m, final double t)
    {
        return translateY(m, t, m);
    }


    /**
     * Scales matrix m with the given scale factor on all axes and stores the
     * result in matrix r.
     *
     * @param m
     *            The matrix to scale.
     * @param s
     *            The scale factor.
     * @param r
     *            The result matrix.
     * @return The result matrix.
     */

    public static final double[] scale(final double[] m, final double s,
        final double[] r)
    {
        return multiply(m, setScaling(new double[9], s), r);
    }


    /**
     * Scales the specified matrix in-place along all axes with the given scale
     * factor.
     *
     * @param m
     *            The matrix to scale. The matrix is changed in-place.
     * @param s
     *            The scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] scale(final double[] m, final double s)
    {
        return scale(m, s, m);
    }


    /**
     * Scales matrix m with the given scale factors and stores the result in
     * matrix r.
     *
     * @param m
     *            The matrix to scale.
     * @param sx
     *            The scale factor.
     * @param sy
     *            The Y scale factor.
     * @param r
     *            The result matrix.
     * @return The result matrix.
     */

    public static final double[] scale(final double[] m, final double sx,
        final double sy, final double[] r)
    {
        return multiply(m, setScaling(new double[9], sx, sy), r);
    }


    /**
     * Scales the specified matrix in-place with the given scale factors.
     *
     * @param m
     *            The matrix to scale. The matrix is changed in-place.
     * @param sx
     *            The X scale factor.
     * @param sy
     *            The Y scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] scale(final double[] m, final double sx,
        final double sy)
    {
        return scale(m, sx, sy, m);
    }


    /**
     * Scales matrix m with the given scale factor along the X axis and stores
     * the result in matrix r.
     *
     * @param m
     *            The matrix to scale.
     * @param s
     *            The scale factor.
     * @param r
     *            The result matrix.
     * @return The result matrix.
     */

    public static final double[] scaleX(final double[] m, final double s,
        final double[] r)
    {
        return multiply(m, setScalingX(new double[9], s), r);
    }


    /**
     * Scales the specified matrix in-place with the given scale factor along
     * the X axis.
     *
     * @param m
     *            The matrix to scale. The matrix is changed in-place.
     * @param s
     *            The scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] scaleX(final double[] m, final double s)
    {
        return scaleX(m, s, m);
    }


    /**
     * Scales matrix m with the given scale factor along the Y axis and stores
     * the result in matrix r.
     *
     * @param m
     *            The matrix to scale.
     * @param s
     *            The scale factor.
     * @param r
     *            The result matrix.
     * @return The result matrix.
     */

    public static final double[] scaleY(final double[] m, final double s,
        final double[] r)
    {
        return multiply(m, setScalingY(new double[9], s), r);
    }


    /**
     * Scales the specified matrix in-place with the given scale factor along
     * the Y axis.
     *
     * @param m
     *            The matrix to scale. The matrix is changed in-place.
     * @param s
     *            The scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] scaleY(final double[] m, final double s)
    {
        return scaleY(m, s, m);
    }


    /**
     * Multiplies all elements of the matrix m with the specified scalar and
     * stores the result in matrix r.
     *
     * @param m
     *            The source matrix.
     * @param s
     *            The scalar.
     * @param r
     *            The result matrix.
     * @return The result matrix
     */

    public static final double[] multiply(final double[] m, final double s,
        final double[] r)
    {
        r[0] = m[0] * s;
        r[1] = m[1] * s;
        r[2] = m[2] * s;
        r[3] = m[3] * s;
        r[4] = m[4] * s;
        r[5] = m[5] * s;
        r[6] = m[6] * s;
        r[7] = m[7] * s;
        r[8] = m[8] * s;
        return r;
    }


    /**
     * Multiplies all elements of the specified matrix with the specified
     * scalar.
     *
     * @param m
     *            The matrix. It is changed in-place.
     * @param s
     *            The scalar
     * @return The matrix for chaining.
     */

    public static final double[] multiply(final double[] m, final double s)
    {
        return multiply(m, s, m);
    }


    /**
     * Divides all elements of the matrix m by the specified scalar and stores
     * the result in matrix r.
     *
     * @param m
     *            The source matrix.
     * @param s
     *            The scalar.
     * @param r
     *            The result matrix.
     * @return The result matrix
     */

    public static final double[] divide(final double[] m, final double s,
        final double[] r)
    {
        r[0] = m[0] / s;
        r[1] = m[1] / s;
        r[2] = m[2] / s;
        r[3] = m[3] / s;
        r[4] = m[4] / s;
        r[5] = m[5] / s;
        r[6] = m[6] / s;
        r[7] = m[7] / s;
        r[8] = m[8] / s;
        return r;
    }


    /**
     * Divides all elements of the specified matrix by the specified scalar.
     *
     * @param m
     *            The matrix. It is changed in-place.
     * @param s
     *            The scalar
     * @return The matrix for chaining.
     */

    public static final double[] divide(final double[] m, final double s)
    {
        return divide(m, s, m);
    }


    /**
     * Creates a new array, copies the elements of the matrix into it and
     * returns the array.
     *
     * @return The array with the matrix elements.
     */

    public final double[] getElements()
    {
        return getElements(null, 0);
    }


    /**
     * Copies the matrix elements into the specified array.
     *
     * @param elements
     *            The array in which to store the matrix elements. If null
     *            is specified then a new array is created.
     * @return The elements array.
     */

    public final double[] getElements(final double[] elements)
    {
        return getElements(elements, 0);
    }


    /**
     * Copies the matrix elements into the specified array starting at the
     * specified index.
     *
     * @param elements
     *            The array in which to store the matrix elements. If null
     *            is specified then a new array is created.
     * @param index
     *            The index to the first array element to which the matrix
     *            elements should be written.
     * @return The elements array.
     */

    public final double[] getElements(final double[] elements, final int index)
    {
        System.arraycopy(this.m, 0, elements, index, 9);
        return elements;
    }


    /**
     * Returns the element at the specified row and column.
     *
     * @param row
     *            The row index.
     * @param column
     *            The column index.
     * @return The element.
     */

    public final double getElement(final int row, final int column)
    {
        return this.m[column * 3 + row];
    }


    /**
     * Multiplies matrix a with matrix b and returns a new matrix with the
     * result. The new matrix has the same type as matrix a.
     *
     * @param <T>
     *            The result matrix type.
     * @param a
     *            The first matrix.
     * @param b
     *            The second matrix.
     * @return The new result matrix.
     */

    public static <T extends Matrix3d> T multiply(final T a, final Matrix3d b)
    {
        final T result = newInstance(a);
        multiply(a.m, b.m, result.m);
        return result;
    }


    /**
     * Multiplies matrix a with matrix b and returns a new matrix with the
     * result. The new matrix has the same type as matrix a.
     *
     * @param <T>
     *            The result matrix type.
     * @param a
     *            The first matrix.
     * @param b
     *            The second matrix as a double array with 9 elements.
     * @return The new result matrix.
     */

    public static <T extends Matrix3d> T multiply(final T a, final double[] b)
    {
        final T result = newInstance(a);
        multiply(a.m, b, result.m);
        return result;
    }


    /**
     * Multiplies matrix a with matrix b and returns a new matrix with the
     * result. The new matrix has the same type as matrix b.
     *
     * @param <T>
     *            The result matrix type.
     * @param a
     *            The first matrix as a double array with 9 elements.
     * @param b
     *            The second matrix.
     * @return The new result matrix.
     */

    public static <T extends Matrix3d> T multiply(final double[] a, final T b)
    {
        final T result = newInstance(b);
        multiply(a, b.m, result.m);
        return result;
    }


    /**
     * Rotates the specified matrix by the given angle and returns a new matrix
     * with the result.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to rotate. Must not be null.
     * @param angle
     *            The rotation angle in anti-clock-wise RAD
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix3d> T rotate(final T matrix,
        final double angle)
    {
        final T result = newInstance(matrix);
        rotate(matrix.m, angle, result.m);
        return result;
    }


    /**
     * Translates the specified matrix and returns a new matrix with the result.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to translate. Must not be null.
     * @param tx
     *            The X translation
     * @param ty
     *            The Y translation
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix3d> T translate(final T matrix,
        final double tx, final double ty)
    {
        final T result = newInstance(matrix);
        translate(matrix.m, tx, ty, result.m);
        return result;
    }


    /**
     * Translates the specified matrix along the X axis and returns a new matrix
     * with the result.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to translate. Must not be null.
     * @param t
     *            The translation
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix3d> T translateX(final T matrix,
        final double t)
    {
        final T result = newInstance(matrix);
        translateX(matrix.m, t, result.m);
        return result;
    }


    /**
     * Translates the specified matrix along the Y axis and returns a new matrix
     * with the result.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to translate. Must not be null.
     * @param t
     *            The translation
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix3d> T translateY(final T matrix,
        final double t)
    {
        final T result = newInstance(matrix);
        translateY(matrix.m, t, result.m);
        return result;
    }


    /**
     * Scales the specified matrix and returns a new matrix with the result.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to scale. Must not be null.
     * @param sx
     *            The X scale factor
     * @param sy
     *            The Y scale factor
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix3d> T scale(final T matrix,
        final double sx, final double sy)
    {
        final T result = newInstance(matrix);
        scale(matrix.m, sx, sy, result.m);
        return result;
    }


    /**
     * Scales the specified matrix and returns a new matrix with the result.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to scale. Must not be null.
     * @param s
     *            The scale factor
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix3d> T scale(final T matrix,
        final double s)
    {
        final T result = newInstance(matrix);
        scale(matrix.m, s, result.m);
        return result;
    }


    /**
     * Scales the specified matrix along the X axis and returns a new matrix
     * with the result.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to scale. Must not be null.
     * @param s
     *            The scale factor
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix3d> T scaleX(final T matrix,
        final double s)
    {
        final T result = newInstance(matrix);
        scaleX(matrix.m, s, result.m);
        return result;
    }


    /**
     * Scales the specified matrix along the Y axis and returns a new matrix
     * with the result.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to scale. Must not be null.
     * @param s
     *            The scale factor
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix3d> T scaleY(final T matrix,
        final double s)
    {
        final T result = newInstance(matrix);
        scaleY(matrix.m, s, result.m);
        return result;
    }


    /**
     * Calculates the adjoint matrix from the specified matrix and returns it.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to calculate the adjoint matrix from. Must not be
     *            null.
     * @return A new matrix with the result. Never null.
     */

    public static final <T extends Matrix3d> T adjoint(final T matrix)
    {
        final T result = newInstance(matrix);
        adjoint(matrix.m, result.m);
        return result;
    }


    /**
     * Calculates the cofactor matrix from the specified matrix and returns it.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to calculate the cofactor matrix from. Must not be
     *            null.
     * @return A new matrix with the result. Never null.
     */

    public static final <T extends Matrix3d> T cofactor(final T matrix)
    {
        final T result = newInstance(matrix);
        cofactor(matrix.m, result.m);
        return result;
    }


    /**
     * Calculates the transpose matrix from the specified matrix and returns it.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to calculate the transpose matrix from. Must not be
     *            null.
     * @return A new matrix with the result. Never null.
     */

    public static final <T extends Matrix3d> T transpose(final T matrix)
    {
        final T result = newInstance(matrix);
        transpose(matrix.m, result.m);
        return result;
    }


    /**
     * Returns the inverted form of the matrix. The source matrix itself is not
     * modified.
     *
     * @return The inverted matrix. Never null.
     */

    public abstract Matrix3d getInverse();


    /**
     * Returns the adjoint of the matrix. The source matrix itself is not
     * modified.
     *
     * @return The adjoint matrix. Never null.
     */

    public abstract Matrix3d getAdjoint();


    /**
     * Returns the cofactor matrix. The source matrix itself is not modified.
     *
     * @return The cofactor matrix. Never null.
     */

    public abstract Matrix3d getCofactor();


    /**
     * Returns the transpose matrix of this matrix. The source matrix itself is
     * not modified.
     *
     * @return The transpose matrix. Never null.
     */

    public abstract Matrix3d getTranspose();


    /**
     * Returns the direct NIO double buffer in native ordering containing the
     * matrix elements. The returned double buffer is cached and mutable but
     * modifications do not modify the matrix class itself.
     *
     * @return The matrix as a direct NIO double buffer.
     */

    public final DoubleBuffer getBuffer()
    {
        if (this.buffer == null)
            this.buffer = ByteBuffer.allocateDirect(9 * 8)
                    .order(ByteOrder.nativeOrder()).asDoubleBuffer();
        this.buffer.rewind();
        this.buffer.put(this.m);
        this.buffer.rewind();
        return this.buffer;
    }
}

