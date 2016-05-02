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
 * Base class for all 4x4 double matrices.
 *
 * @author Klaus Reimer (k@ailis.de)
 */

public abstract class Matrix4d extends Matrix
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

    protected Matrix4d()
    {
        this.m = new double[16];
    }


    /**
     * Constructs a new matrix with the specified values.
     *
     * @param values
     *            The matrix values (Must have at least 16 values)
     */

    public Matrix4d(final double... values)
    {
        this(values, 0);
    }


    /**
     * Constructs a new matrix with the elements from the specified matrix.
     *
     * @param matrix
     *            The matrix to copy the elements from. Must not be null.
     */

    public Matrix4d(final Matrix4d matrix)
    {
        this(matrix.m, 0);
    }


    /**
     * Constructs a new matrix with the elements from the specified matrix.
     *
     * @param matrix
     *            The matrix to copy the elements from. Must not be null.
     */

    public Matrix4d(final Matrix4f matrix)
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
        a[9] = b[9];
        a[10] = b[10];
        a[11] = b[11];
        a[12] = b[12];
        a[13] = b[13];
        a[14] = b[14];
        a[15] = b[15];
    }


    /**
     * Constructs a new matrix with the elements from the specified matrix.
     *
     * @param matrix
     *            The matrix to copy the elements from. Must not be null.
     */

    public Matrix4d(final Matrix3d matrix)
    {
        this();
        final double[] a = this.m;
        final double[] b = matrix.m;
        a[0] = b[0];
        a[1] = b[1];
        a[2] = b[2];
        a[3] = 0;
        a[4] = b[3];
        a[5] = b[4];
        a[6] = b[5];
        a[7] = 0;
        a[8] = b[6];
        a[9] = b[7];
        a[10] = b[8];
        a[11] = 0;
        a[12] = 0;
        a[13] = 0;
        a[14] = 0;
        a[15] = 1;
    }


    /**
     * Constructs a new matrix with the elements from the specified matrix.
     *
     * @param matrix
     *            The matrix to copy the elements from. Must not be null.
     */

    public Matrix4d(final Matrix3f matrix)
    {
        this();
        final double[] a = this.m;
        final float[] b = matrix.m;
        a[0] = b[0];
        a[1] = b[1];
        a[2] = b[2];
        a[3] = 0;
        a[4] = b[3];
        a[5] = b[4];
        a[6] = b[5];
        a[7] = 0;
        a[8] = b[6];
        a[9] = b[7];
        a[10] = b[8];
        a[11] = 0;
        a[12] = 0;
        a[13] = 0;
        a[14] = 0;
        a[15] = 1;
    }


    /**
     * Constructs a new matrix with values read from the specified array
     * beginning at the given index.
     *
     * @param values
     *            The matrix values (Must have at least index+16 values)
     * @param index
     *            The start index
     */

    public Matrix4d(final double[] values, final int index)
    {
        this();
        if (values.length - index < 16)
            throw new IllegalArgumentException(
                "Not enough matrix elements specified");
        System.arraycopy(values, index, this.m, 0, 16);
    }


    /**
     * Creates a new empty instance of the matrix class.
     *
     * @return A new empty instance. Never null.
     */

    protected abstract Matrix4d newInstance();


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
    private static <T extends Matrix4d> T newInstance(final T m)
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
        return "[[ " + m[0] + ", " + m[4] + ", " + m[8] + ", " + m[12] + " ], "
             + "[ " + m[1] + ", " + m[5] + ", " + m[9] + ", " + m[13] + " ], "
             + "[ " + m[2] + ", " + m[6] + ", " + m[10] + ", " + m[14] + " ], "
             + "[ " + m[3] + ", " + m[7] + ", " + m[11] + ", " + m[15] + " ]]";
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
        final Matrix4d other = (Matrix4d) obj;
        if (!Arrays.equals(this.m, other.m)) return false;
        return true;
    }


    /**
     * Calculates the adjoint matrix from matrix m and stores the result in
     * matrix r. All matrices are represented as double arrays with 16 elements.
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
        final double r0 = m[6] * m[11] * m[13] - m[7] * m[10] * m[13] + m[7]
            * m[9] * m[14] - m[5] * m[11] * m[14] - m[6] * m[9] * m[15] + m[5]
            * m[10] * m[15];

        final double r4 = m[3] * m[10] * m[13] - m[2] * m[11] * m[13] - m[3]
            * m[9] * m[14] + m[1] * m[11] * m[14] + m[2] * m[9] * m[15] - m[1]
            * m[10] * m[15];

        final double r8 = m[2] * m[7] * m[13] - m[3] * m[6] * m[13] + m[3]
            * m[5] * m[14] - m[1] * m[7] * m[14] - m[2] * m[5] * m[15] + m[1]
            * m[6] * m[15];

        final double r12 = m[3] * m[6] * m[9] - m[2] * m[7] * m[9] - m[3]
            * m[5]
            * m[10] + m[1] * m[7] * m[10] + m[2] * m[5] * m[11] - m[1] * m[6]
            * m[11];

        final double r1 = m[7] * m[10] * m[12] - m[6] * m[11] * m[12] - m[7]
            * m[8] * m[14] + m[4] * m[11] * m[14] + m[6] * m[8] * m[15] - m[4]
            * m[10] * m[15];

        final double r5 = m[2] * m[11] * m[12] - m[3] * m[10] * m[12] + m[3]
            * m[8] * m[14] - m[0] * m[11] * m[14] - m[2] * m[8] * m[15] + m[0]
            * m[10] * m[15];

        final double r9 = m[3] * m[6] * m[12] - m[2] * m[7] * m[12] - m[3]
            * m[4] * m[14] + m[0] * m[7] * m[14] + m[2] * m[4] * m[15] - m[0]
            * m[6] * m[15];

        final double r13 = m[2] * m[7] * m[8] - m[3] * m[6] * m[8] + m[3]
            * m[4]
            * m[10] - m[0] * m[7] * m[10] - m[2] * m[4] * m[11] + m[0] * m[6]
            * m[11];

        final double r2 = m[5] * m[11] * m[12] - m[7] * m[9] * m[12] + m[7]
            * m[8] * m[13] - m[4] * m[11] * m[13] - m[5] * m[8] * m[15] + m[4]
            * m[9] * m[15];

        final double r6 = m[3] * m[9] * m[12] - m[1] * m[11] * m[12] - m[3]
            * m[8] * m[13] + m[0] * m[11] * m[13] + m[1] * m[8] * m[15] - m[0]
            * m[9] * m[15];

        final double r10 = m[1] * m[7] * m[12] - m[3] * m[5] * m[12] + m[3]
            * m[4] * m[13] - m[0] * m[7] * m[13] - m[1] * m[4] * m[15] + m[0]
            * m[5] * m[15];

        final double r14 = m[3] * m[5] * m[8] - m[1] * m[7] * m[8] - m[3]
            * m[4]
            * m[9] + m[0] * m[7] * m[9] + m[1] * m[4] * m[11] - m[0] * m[5]
            * m[11];

        final double r3 = m[6] * m[9] * m[12] - m[5] * m[10] * m[12] - m[6]
            * m[8] * m[13] + m[4] * m[10] * m[13] + m[5] * m[8] * m[14] - m[4]
            * m[9] * m[14];

        final double r7 = m[1] * m[10] * m[12] - m[2] * m[9] * m[12] + m[2]
            * m[8] * m[13] - m[0] * m[10] * m[13] - m[1] * m[8] * m[14] + m[0]
            * m[9] * m[14];

        final double r11 = m[2] * m[5] * m[12] - m[1] * m[6] * m[12] - m[2]
            * m[4] * m[13] + m[0] * m[6] * m[13] + m[1] * m[4] * m[14] - m[0]
            * m[5] * m[14];

        final double r15 = m[1] * m[6] * m[8] - m[2] * m[5] * m[8] + m[2]
            * m[4]
            * m[9] - m[0] * m[6] * m[9] - m[1] * m[4] * m[10] + m[0] * m[5]
            * m[10];

        r[0] = r0;
        r[1] = r1;
        r[2] = r2;
        r[3] = r3;
        r[4] = r4;
        r[5] = r5;
        r[6] = r6;
        r[7] = r7;
        r[8] = r8;
        r[9] = r9;
        r[10] = r10;
        r[11] = r11;
        r[12] = r12;
        r[13] = r13;
        r[14] = r14;
        r[15] = r15;

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
     * matrix r. All matrices are represented as double arrays with 16 elements.
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
     * All matrices are represented as double arrays with 16 elements. It is
     * safe to specify one of the source matrices as the result matrix.
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
        final double r1, r2, r3, r4, r6, r7, r8, r9, r11, r12, r13, r14;

        r1 = a[1] * b[0] + a[5] * b[1] + a[9] * b[2] + a[13] * b[3];
        r2 = a[2] * b[0] + a[6] * b[1] + a[10] * b[2] + a[14] * b[3];
        r3 = a[3] * b[0] + a[7] * b[1] + a[11] * b[2] + a[15] * b[3];
        r4 = a[0] * b[4] + a[4] * b[5] + a[8] * b[6] + a[12] * b[7];
        r6 = a[2] * b[4] + a[6] * b[5] + a[10] * b[6] + a[14] * b[7];
        r7 = a[3] * b[4] + a[7] * b[5] + a[11] * b[6] + a[15] * b[7];
        r8 = a[0] * b[8] + a[4] * b[9] + a[8] * b[10] + a[12] * b[11];
        r9 = a[1] * b[8] + a[5] * b[9] + a[9] * b[10] + a[13] * b[11];
        r11 = a[3] * b[8] + a[7] * b[9] + a[11] * b[10] + a[15] * b[11];
        r12 = a[0] * b[12] + a[4] * b[13] + a[8] * b[14] + a[12] * b[15];
        r13 = a[1] * b[12] + a[5] * b[13] + a[9] * b[14] + a[13] * b[15];
        r14 = a[2] * b[12] + a[6] * b[13] + a[10] * b[14] + a[14] * b[15];
        r[0] = a[0] * b[0] + a[4] * b[1] + a[8] * b[2] + a[12] * b[3];
        r[5] = a[1] * b[4] + a[5] * b[5] + a[9] * b[6] + a[13] * b[7];
        r[10] = a[2] * b[8] + a[6] * b[9] + a[10] * b[10] + a[14] * b[11];
        r[15] = a[3] * b[12] + a[7] * b[13] + a[11] * b[14] + a[15] * b[15];

        r[1] = r1;
        r[2] = r2;
        r[3] = r3;
        r[4] = r4;
        r[6] = r6;
        r[7] = r7;
        r[8] = r8;
        r[9] = r9;
        r[11] = r11;
        r[12] = r12;
        r[13] = r13;
        r[14] = r14;

        return r;
    }


    /**
     * Returns the determinant of the specified matrix.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @return The determinant.
     */

    public static final double determinant(final double[] m)
    {
        return m[3] * m[6] * m[9] * m[12] - m[2] * m[7] * m[9] * m[12]
             - m[3] * m[5] * m[10] * m[12] + m[1] * m[7] * m[10] * m[12]
             + m[2] * m[5] * m[11] * m[12] - m[1] * m[6] * m[11] * m[12]
             - m[3] * m[6] * m[8] * m[13] + m[2] * m[7] * m[8] * m[13]
             + m[3] * m[4] * m[10] * m[13] - m[0] * m[7] * m[10] * m[13]
             - m[2] * m[4] * m[11] * m[13] + m[0] * m[6] * m[11] * m[13]
             + m[3] * m[5] * m[8] * m[14] - m[1] * m[7] * m[8] * m[14]
             - m[3] * m[4] * m[9] * m[14] + m[0] * m[7] * m[9] * m[14]
             + m[1] * m[4] * m[11] * m[14] - m[0] * m[5] * m[11] * m[14]
             - m[2] * m[5] * m[8] * m[15] + m[1] * m[6] * m[8] * m[15]
             + m[2] * m[4] * m[9] * m[15] - m[0] * m[6] * m[9] * m[15]
             - m[1] * m[4] * m[10] * m[15] + m[0] * m[5] * m[10] * m[15];
    }


    /**
     * Checks if the specified matrix is an identity matrix.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @return True if matrix is an identity matrix, false if not
     */

    public static final boolean isIdentity(final double[] m)
    {
        return m[0] == 1 && m[4] == 0 && m[8] == 0 && m[12] == 0
            && m[1] == 0 && m[5] == 1 && m[9] == 0 && m[13] == 0
            && m[2] == 0 && m[6] == 0 && m[10] == 1 && m[14] == 0
            && m[3] == 0 && m[7] == 0 && m[11] == 0 && m[15] == 1;
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
        m[4] = 0;
        m[5] = 1;
        m[6] = 0;
        m[7] = 0;
        m[8] = 0;
        m[9] = 0;
        m[10] = 1;
        m[11] = 0;
        m[12] = 0;
        m[13] = 0;
        m[14] = 0;
        m[15] = 1;
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
        double r1, r2, r3, r4, r6, r7, r8, r9, r11, r12, r13, r14;

        r1 = m[4];
        r2 = m[8];
        r3 = m[12];
        r4 = m[1];
        r6 = m[9];
        r7 = m[13];
        r8 = m[2];
        r9 = m[6];
        r11 = m[14];
        r12 = m[3];
        r13 = m[7];
        r14 = m[11];

        r[0] = m[0];
        r[1] = r1;
        r[2] = r2;
        r[3] = r3;
        r[4] = r4;
        r[5] = m[5];
        r[6] = r6;
        r[7] = r7;
        r[8] = r8;
        r[9] = r9;
        r[10] = m[10];
        r[11] = r11;
        r[12] = r12;
        r[13] = r13;
        r[14] = r14;
        r[15] = m[15];
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
     *            The matrix as a double array with 16 elements.
     * @param v
     *            The rotation axis.
     * @param a
     *            The rotation angle in anti-clock-wise RAD.
     * @return The matrix for chaining.
     */

    public static final double[] setRotation(final double[] m,
        final Vector3d v,
        final double a)
    {
        final double s = Math.sin(a);
        final double c = Math.cos(a);
        final double x = v.x;
        final double y = v.y;
        final double z = v.z;
        final double x2 = x * x;
        final double y2 = y * y;
        final double z2 = z * z;
        final double t = 1 - c;
        final double tYZ = t * y * z;
        final double tXY = t * x * y;
        final double sZ = s * z;
        final double sY = s * y;
        final double sX = s * x;
        m[0] = t * x2 + c;
        m[1] = tXY + sZ;
        m[2] = t * x * z - sY;
        m[3] = 0;
        m[4] = tXY - sZ;
        m[5] = t * y2 + c;
        m[6] = tYZ + sX;
        m[7] = 0;
        m[8] = tXY + sY;
        m[9] = tYZ - sX;
        m[10] = t * z2 + c;
        m[11] = 0;
        m[12] = 0;
        m[13] = 0;
        m[14] = 0;
        m[15] = 1;
        return m;
    }


    /**
     * Sets the matrix m to a rotation matrix around the X axis.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @param a
     *            The rotation angle in anti-clock-wise RAD.
     * @return The matrix for chaining.
     */

    public static final double[] setRotationX(final double[] m, final double a)
    {
        final double s = Math.sin(a);
        final double c = Math.cos(a);
        m[0] = 1;
        m[1] = 0;
        m[2] = 0;
        m[3] = 0;
        m[4] = 0;
        m[5] = c;
        m[6] = s;
        m[7] = 0;
        m[8] = 0;
        m[9] = -s;
        m[10] = c;
        m[11] = 0;
        m[12] = 0;
        m[13] = 0;
        m[14] = 0;
        m[15] = 1;
        return m;
    }


    /**
     * Sets the matrix m to a rotation matrix around the Y axis.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @param a
     *            The rotation angle in anti-clock-wise RAD.
     * @return The matrix for chaining.
     */

    public static final double[] setRotationY(final double[] m, final double a)
    {
        final double s = Math.sin(a);
        final double c = Math.cos(a);
        m[0] = c;
        m[1] = 0;
        m[2] = -s;
        m[3] = 0;
        m[4] = 0;
        m[5] = 1;
        m[6] = 0;
        m[7] = 0;
        m[8] = s;
        m[9] = 0;
        m[10] = c;
        m[11] = 0;
        m[12] = 0;
        m[13] = 0;
        m[14] = 0;
        m[15] = 1;
        return m;
    }


    /**
     * Sets the matrix m to a rotation matrix around the Z axis.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @param a
     *            The rotation angle in anti-clock-wise RAD.
     * @return The matrix for chaining.
     */

    public static final double[] setRotationZ(final double[] m, final double a)
    {
        final double s = Math.sin(a);
        final double c = Math.cos(a);
        m[0] = c;
        m[1] = s;
        m[2] = 0;
        m[3] = 0;
        m[4] = -s;
        m[5] = c;
        m[6] = 0;
        m[7] = 0;
        m[8] = 0;
        m[9] = 0;
        m[10] = 1;
        m[11] = 0;
        m[12] = 0;
        m[13] = 0;
        m[14] = 0;
        m[15] = 1;
        return m;
    }


    /**
     * Sets the matrix m to a translation matrix.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @param tx
     *            The X translation.
     * @param ty
     *            The Y translation.
     * @param tz
     *            The Z translation.
     * @return The matrix for chaining.
     */

    public static final double[] setTranslation(final double[] m,
        final double tx,
        final double ty, final double tz)
    {
        m[0] = 1;
        m[1] = 0;
        m[2] = 0;
        m[3] = 0;
        m[4] = 0;
        m[5] = 1;
        m[6] = 0;
        m[7] = 0;
        m[8] = 0;
        m[9] = 0;
        m[10] = 1;
        m[11] = 0;
        m[12] = tx;
        m[13] = ty;
        m[14] = tz;
        m[15] = 1;
        return m;
    }


    /**
     * Sets the matrix m to a translation matrix along the X axis.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @param t
     *            The translation.
     * @return The matrix for chaining.
     */

    public static final double[] setTranslationX(final double[] m,
        final double t)
    {
        return setTranslation(m, t, 0, 0);
    }


    /**
     * Sets the matrix m to a translation matrix along the Y axis.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @param t
     *            The translation.
     * @return The matrix for chaining.
     */

    public static final double[] setTranslationY(final double[] m,
        final double t)
    {
        return setTranslation(m, 0, t, 0);
    }


    /**
     * Sets the matrix m to a translation matrix along the Z axis.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @param t
     *            The translation.
     * @return The matrix for chaining.
     */

    public static final double[] setTranslationZ(final double[] m,
        final double t)
    {
        return setTranslation(m, 0, 0, t);
    }


    /**
     * Sets the matrix m to a scaling matrix.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @param sx
     *            The X scale factor.
     * @param sy
     *            The Y scale factor.
     * @param sz
     *            The Z scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] setScaling(final double[] m, final double sx,
        final double sy, final double sz)
    {
        m[0] = sx;
        m[1] = 0;
        m[2] = 0;
        m[3] = 0;
        m[4] = 0;
        m[5] = sy;
        m[6] = 0;
        m[7] = 0;
        m[8] = 0;
        m[9] = 0;
        m[10] = sz;
        m[11] = 0;
        m[12] = 0;
        m[13] = 0;
        m[14] = 0;
        m[15] = 1;
        return m;
    }


    /**
     * Sets the matrix m to a scaling matrix.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @param s
     *            The scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] setScaling(final double[] m, final double s)
    {
        return setScaling(m, s, s, s);
    }


    /**
     * Sets the matrix m to a scaling matrix along the X axis.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @param s
     *            The scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] setScalingX(final double[] m, final double s)
    {
        return setScaling(m, s, 1, 1);
    }


    /**
     * Sets the matrix m to a scaling matrix along the Y axis.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @param s
     *            The scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] setScalingY(final double[] m, final double s)
    {
        return setScaling(m, 1, s, 1);
    }


    /**
     * Sets the matrix m to a scaling matrix along the Z axis.
     *
     * @param m
     *            The matrix as a double array with 16 elements.
     * @param s
     *            The scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] setScalingZ(final double[] m, final double s)
    {
        return setScaling(m, 1, 1, s);
    }


    /**
     * Rotates matrix m by the given angle around the given vector and stores
     * the result in matrix r.
     *
     * @param m
     *            The matrix to rotate.
     * @param v
     *            The rotation axis.
     * @param a
     *            The rotation angle in anti-clock-wise RAD
     * @param r
     *            The result matrix.
     * @return The result matrix.
     */

    public static final double[] rotate(final double[] m,
        final Vector3d v, final double a, final double[] r)
    {
        return multiply(m, setRotation(new double[16], v, a), r);
    }


    /**
     * Rotates the specified-matrix in-place by the given angle around the given
     * vector.
     *
     * @param m
     *            The matrix to rotate. It is changed in-place.
     * @param v
     *            The rotation axis.
     * @param a
     *            The rotation angle in anti-clock-wise RAD
     * @return The matrix for chaining.
     */

    public static final double[] rotate(final double[] m,
        final Vector3d v, final double a)
    {
        return rotate(m, v, a, m);
    }


    /**
     * Rotates matrix m around the X axis and stores the result in matrix r.
     *
     * @param m
     *            The matrix to rotate.
     * @param a
     *            The rotation in anti-clockwise RAD.
     * @param r
     *            The storage for the result matrix.
     * @return The result matrix.
     */

    public static final double[] rotateX(final double[] m, final double a,
        final double[] r)
    {
        return multiply(m, setRotationX(new double[16], a), r);
    }


    /**
     * Rotates the specified matrix around the X axis.
     *
     * @param m
     *            The matrix to rotate. This matrix is changed in-place.
     * @param a
     *            The rotation in anti-clockwise RAD.
     * @return The matrix for chaining.
     */

    public static final double[] rotateX(final double[] m, final double a)
    {
        return rotateX(m, a, m);
    }


    /**
     * Rotates matrix m around the Y axis and stores the result in matrix r.
     *
     * @param m
     *            The matrix to rotate.
     * @param a
     *            The rotation in anti-clockwise RAD.
     * @param r
     *            The storage for the result matrix.
     * @return The result matrix.
     */

    public static final double[] rotateY(final double[] m, final double a,
        final double[] r)
    {
        return multiply(m, setRotationY(new double[16], a), r);
    }


    /**
     * Rotates the specified matrix around the Y axis.
     *
     * @param m
     *            The matrix to rotate. This matrix is changed in-place.
     * @param a
     *            The rotation in anti-clockwise RAD.
     * @return The matrix for chaining.
     */

    public static final double[] rotateY(final double[] m, final double a)
    {
        return rotateY(m, a, m);
    }


    /**
     * Rotates matrix m around the Z axis and stores the result in matrix r.
     *
     * @param m
     *            The matrix to rotate.
     * @param a
     *            The rotation in anti-clockwise RAD.
     * @param r
     *            The storage for the result matrix.
     * @return The result matrix.
     */

    public static final double[] rotateZ(final double[] m, final double a,
        final double[] r)
    {
        return multiply(m, setRotationZ(new double[16], a), r);
    }


    /**
     * Rotates the specified matrix around the Z axis.
     *
     * @param m
     *            The matrix to rotate. This matrix is changed in-place.
     * @param a
     *            The rotation in anti-clockwise RAD.
     * @return The matrix for chaining.
     */

    public static final double[] rotateZ(final double[] m, final double a)
    {
        return rotateZ(m, a, m);
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
     * @param tz
     *            The Z translation delta.
     * @param r
     *            The result matrix.
     * @return The result matrix.
     */

    public static final double[] translate(final double[] m,
        final double tx, final double ty, final double tz, final double[] r)
    {
        return multiply(m, setTranslation(new double[16], tx, ty, tz), r);
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
     * @param tz
     *            The Z translation delta.
     * @return The matrix for chaining.
     */

    public static final double[] translate(final double[] m,
        final double tx, final double ty, final double tz)
    {
        return translate(m, tx, ty, tz, m);
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
        return multiply(m, setTranslationX(new double[16], t), r);
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
        return multiply(m, setTranslationY(new double[16], t), r);
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
     * Translates matrix m along the Z axis and stores the result in matrix r.
     *
     * @param m
     *            The matrix to translate.
     * @param t
     *            The translation delta.
     * @param r
     *            The result matrix.
     * @return The result matrix.
     */

    public static final double[] translateZ(final double[] m, final double t,
        final double[] r)
    {
        return multiply(m, setTranslationZ(new double[16], t), r);
    }


    /**
     * Translates the specified matrix in-place along the Z-axis.
     *
     * @param m
     *            The matrix to translate. It is changed in-place.
     * @param t
     *            The translation delta.
     * @return The matrix for chaining.
     */

    public static final double[] translateZ(final double[] m, final double t)
    {
        return translateZ(m, t, m);
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
        return multiply(m, setScaling(new double[16], s), r);
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
     * @param sz
     *            The Z scale factor.
     * @param r
     *            The result matrix.
     * @return The result matrix.
     */

    public static final double[] scale(final double[] m, final double sx,
        final double sy, final double sz, final double[] r)
    {
        return multiply(m, setScaling(new double[16], sx, sy, sz), r);
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
     * @param sz
     *            The Z scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] scale(final double[] m, final double sx,
        final double sy, final double sz)
    {
        return scale(m, sx, sy, sz, m);
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
        return multiply(m, setScalingX(new double[16], s), r);
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
        return multiply(m, setScalingY(new double[16], s), r);
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
     * Scales matrix m with the given scale factor along the Z axis and stores
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

    public static final double[] scaleZ(final double[] m, final double s,
        final double[] r)
    {
        return multiply(m, setScalingZ(new double[16], s), r);
    }


    /**
     * Scales the specified matrix in-place with the given scale factor along
     * the Z axis.
     *
     * @param m
     *            The matrix to scale. The matrix is changed in-place.
     * @param s
     *            The scale factor.
     * @return The matrix for chaining.
     */

    public static final double[] scaleZ(final double[] m, final double s)
    {
        return scaleZ(m, s, m);
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
        r[9] = m[9] * s;
        r[10] = m[10] * s;
        r[11] = m[11] * s;
        r[12] = m[12] * s;
        r[13] = m[13] * s;
        r[14] = m[14] * s;
        r[15] = m[15] * s;
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
        r[9] = m[9] / s;
        r[10] = m[10] / s;
        r[11] = m[11] / s;
        r[12] = m[12] / s;
        r[13] = m[13] / s;
        r[14] = m[14] / s;
        r[15] = m[15] / s;
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
        System.arraycopy(this.m, 0, elements, index, 16);
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
        return this.m[column * 4 + row];
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

    public static <T extends Matrix4d> T multiply(final T a, final Matrix4d b)
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
     *            The second matrix as a double array with 16 elements.
     * @return The new result matrix.
     */

    public static <T extends Matrix4d> T multiply(final T a, final double[] b)
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
     *            The first matrix as a double array with 16 elements.
     * @param b
     *            The second matrix.
     * @return The new result matrix.
     */

    public static <T extends Matrix4d> T multiply(final double[] a, final T b)
    {
        final T result = newInstance(b);
        multiply(a, b.m, result.m);
        return result;
    }


    /**
     * Rotates the specified matrix by the given angle around the given axis and
     * returns a new matrix with the result.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to rotate. Must not be null.
     * @param axis
     *            The rotation axis. Must not be null.
     * @param angle
     *            The rotation angle in anti-clock-wise RAD
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix4d> T rotate(final T matrix,
        final Vector3d axis, final double angle)
    {
        final T result = newInstance(matrix);
        rotate(matrix.m, axis, angle, result.m);
        return result;
    }


    /**
     * Rotates the specified matrix by the given angle around the X axis and
     * returns a new matrix with the result.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to rotate. Must not be null.
     * @param angle
     *            The rotation angle in anti-clock-wise RAD
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix4d> T rotateX(final T matrix,
        final double angle)
    {
        final T result = newInstance(matrix);
        rotateX(matrix.m, angle, result.m);
        return result;
    }


    /**
     * Rotates the specified matrix by the given angle around the Y axis and
     * returns a new matrix with the result.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to rotate. Must not be null.
     * @param angle
     *            The rotation angle in anti-clock-wise RAD
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix4d> T rotateY(final T matrix,
        final double angle)
    {
        final T result = newInstance(matrix);
        rotateY(matrix.m, angle, result.m);
        return result;
    }


    /**
     * Rotates the specified matrix by the given angle around the Z axis and
     * returns a new matrix with the result.
     *
     * @param <T>
     *            The result matrix type.
     * @param matrix
     *            The matrix to rotate. Must not be null.
     * @param angle
     *            The rotation angle in anti-clock-wise RAD
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix4d> T rotateZ(final T matrix,
        final double angle)
    {
        final T result = newInstance(matrix);
        rotateZ(matrix.m, angle, result.m);
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
     * @param tz
     *            The Z translation
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix4d> T translate(final T matrix,
        final double tx, final double ty, final double tz)
    {
        final T result = newInstance(matrix);
        translate(matrix.m, tx, ty, tz, result.m);
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

    public static final <T extends Matrix4d> T translateX(final T matrix,
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

    public static final <T extends Matrix4d> T translateY(final T matrix,
        final double t)
    {
        final T result = newInstance(matrix);
        translateY(matrix.m, t, result.m);
        return result;
    }


    /**
     * Translates the specified matrix along the Z axis and returns a new matrix
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

    public static final <T extends Matrix4d> T translateZ(final T matrix,
        final double t)
    {
        final T result = newInstance(matrix);
        translateZ(matrix.m, t, result.m);
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
     * @param sz
     *            The Z scale factor
     * @return The result matrix. Never null.
     */

    public static final <T extends Matrix4d> T scale(final T matrix,
        final double sx, final double sy, final double sz)
    {
        final T result = newInstance(matrix);
        scale(matrix.m, sx, sy, sz, result.m);
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

    public static final <T extends Matrix4d> T scale(final T matrix,
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

    public static final <T extends Matrix4d> T scaleX(final T matrix,
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

    public static final <T extends Matrix4d> T scaleY(final T matrix,
        final double s)
    {
        final T result = newInstance(matrix);
        scaleY(matrix.m, s, result.m);
        return result;
    }


    /**
     * Scales the specified matrix along the Z axis and returns a new matrix
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

    public static final <T extends Matrix4d> T scaleZ(final T matrix,
        final double s)
    {
        final T result = newInstance(matrix);
        scaleZ(matrix.m, s, result.m);
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

    public static final <T extends Matrix4d> T adjoint(final T matrix)
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

    public static final <T extends Matrix4d> T cofactor(final T matrix)
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

    public static final <T extends Matrix4d> T transpose(final T matrix)
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

    public abstract Matrix4d getInverse();


    /**
     * Returns the adjoint of the matrix. The source matrix itself is not
     * modified.
     *
     * @return The adjoint matrix. Never null.
     */

    public abstract Matrix4d getAdjoint();


    /**
     * Returns the cofactor matrix. The source matrix itself is not modified.
     *
     * @return The cofactor matrix. Never null.
     */

    public abstract Matrix4d getCofactor();


    /**
     * Returns the transpose matrix of this matrix. The source matrix itself is
     * not modified.
     *
     * @return The transpose matrix. Never null.
     */

    public abstract Matrix4d getTranspose();


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
            this.buffer = ByteBuffer.allocateDirect(16 * 8)
                    .order(ByteOrder.nativeOrder()).asDoubleBuffer();
        this.buffer.rewind();
        this.buffer.put(this.m);
        this.buffer.rewind();
        return this.buffer;
    }
}

