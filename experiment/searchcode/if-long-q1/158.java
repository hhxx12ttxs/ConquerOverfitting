/*
 * $RCSfile$
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision: 127 $
 * $Date: 2008-02-28 20:18:51 +0000 (Thu, 28 Feb 2008) $
 * $State$
 */

package toxi.geom;

import toxi.math.MathUtils;

/**
 * A single precision floating point 4 by 4 matrix. Primarily to support 3D
 * rotations.
 * 
 */
public class Matrix4f implements java.io.Serializable, Cloneable {

    // Compatible with 1.1
    static final long serialVersionUID = -8405036035410109353L;

    /**
     * Solves a set of linear equations. The input parameters "matrix1", and
     * "row_perm" come from luDecompostionD4x4 and do not change here. The
     * parameter "matrix2" is a set of column vectors assembled into a 4x4
     * matrix of floating-point values. The procedure takes each column of
     * "matrix2" in turn and treats it as the right-hand side of the matrix
     * equation Ax = LUx = b. The solution vector replaces the original column
     * of the matrix.
     * 
     * If "matrix2" is the identity matrix, the procedure replaces its contents
     * with the inverse of the matrix from which "matrix1" was originally
     * derived.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling,
    // _Numerical_Recipes_in_C_, Cambridge University Press,
    // 1988, pp 44-45.
    //
    static void luBacksubstitution(double[] matrix1, int[] row_perm,
            double[] matrix2) {

        int i, ii, ip, j, k;
        int rp;
        int cv, rv;

        // rp = row_perm;
        rp = 0;

        // For each column vector of matrix2 ...
        for (k = 0; k < 4; k++) {
            // cv = &(matrix2[0][k]);
            cv = k;
            ii = -1;

            // Forward substitution
            for (i = 0; i < 4; i++) {
                double sum;

                ip = row_perm[rp + i];
                sum = matrix2[cv + 4 * ip];
                matrix2[cv + 4 * ip] = matrix2[cv + 4 * i];
                if (ii >= 0) {
                    // rv = &(matrix1[i][0]);
                    rv = i * 4;
                    for (j = ii; j <= i - 1; j++) {
                        sum -= matrix1[rv + j] * matrix2[cv + 4 * j];
                    }
                } else if (sum != 0.0) {
                    ii = i;
                }
                matrix2[cv + 4 * i] = sum;
            }

            // Backsubstitution
            // rv = &(matrix1[3][0]);
            rv = 3 * 4;
            matrix2[cv + 4 * 3] /= matrix1[rv + 3];

            rv -= 4;
            matrix2[cv + 4 * 2] = (matrix2[cv + 4 * 2] - matrix1[rv + 3]
                    * matrix2[cv + 4 * 3])
                    / matrix1[rv + 2];

            rv -= 4;
            matrix2[cv + 4 * 1] = (matrix2[cv + 4 * 1] - matrix1[rv + 2]
                    * matrix2[cv + 4 * 2] - matrix1[rv + 3]
                    * matrix2[cv + 4 * 3])
                    / matrix1[rv + 1];

            rv -= 4;
            matrix2[cv + 4 * 0] = (matrix2[cv + 4 * 0] - matrix1[rv + 1]
                    * matrix2[cv + 4 * 1] - matrix1[rv + 2]
                    * matrix2[cv + 4 * 2] - matrix1[rv + 3]
                    * matrix2[cv + 4 * 3])
                    / matrix1[rv + 0];
        }
    }

    /**
     * The first element of the first row.
     */
    public float m00;

    /**
     * The second element of the first row.
     */
    public float m01;

    /**
     * The third element of the first row.
     */
    public float m02;

    /**
     * The fourth element of the first row.
     */
    public float m03;

    /**
     * The first element of the second row.
     */
    public float m10;

    /**
     * The second element of the second row.
     */
    public float m11;

    /**
     * The third element of the second row.
     */
    public float m12;

    /**
     * The fourth element of the second row.
     */
    public float m13;

    /**
     * The first element of the third row.
     */
    public float m20;

    /**
     * The second element of the third row.
     */
    public float m21;

    /**
     * The third element of the third row.
     */
    public float m22;

    /**
     * The fourth element of the third row.
     */
    public float m23;

    /**
     * The first element of the fourth row.
     */
    public float m30;

    /**
     * The second element of the fourth row.
     */
    public float m31;
    /**
     * The third element of the fourth row.
     */
    public float m32;

    /**
     * The fourth element of the fourth row.
     */
    public float m33;

    /*
     * double[] tmp = new double[9]; double[] tmp_scale = new double[3];
     * double[] tmp_rot = new double[9];
     */
    private static final double EPS = 1.0E-8;

    /**
     * Constructs and initializes a Matrix4f to all zeros.
     */
    public Matrix4f() {
        this.m00 = (float) 0.0;
        this.m01 = (float) 0.0;
        this.m02 = (float) 0.0;
        this.m03 = (float) 0.0;

        this.m10 = (float) 0.0;
        this.m11 = (float) 0.0;
        this.m12 = (float) 0.0;
        this.m13 = (float) 0.0;

        this.m20 = (float) 0.0;
        this.m21 = (float) 0.0;
        this.m22 = (float) 0.0;
        this.m23 = (float) 0.0;

        this.m30 = (float) 0.0;
        this.m31 = (float) 0.0;
        this.m32 = (float) 0.0;
        this.m33 = (float) 0.0;

    }

    /**
     * Constructs and initializes a Matrix4f from the specified 16 values.
     * 
     * @param m00
     *            the [0][0] element
     * @param m01
     *            the [0][1] element
     * @param m02
     *            the [0][2] element
     * @param m03
     *            the [0][3] element
     * @param m10
     *            the [1][0] element
     * @param m11
     *            the [1][1] element
     * @param m12
     *            the [1][2] element
     * @param m13
     *            the [1][3] element
     * @param m20
     *            the [2][0] element
     * @param m21
     *            the [2][1] element
     * @param m22
     *            the [2][2] element
     * @param m23
     *            the [2][3] element
     * @param m30
     *            the [3][0] element
     * @param m31
     *            the [3][1] element
     * @param m32
     *            the [3][2] element
     * @param m33
     *            the [3][3] element
     */
    public Matrix4f(float m00, float m01, float m02, float m03, float m10,
            float m11, float m12, float m13, float m20, float m21, float m22,
            float m23, float m30, float m31, float m32, float m33) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;

        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;

        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;

        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;

    }

    /**
     * Constructs and initializes a Matrix4f from the specified 16 element
     * array. this.m00 =v[0], this.m01=v[1], etc.
     * 
     * @param v
     *            the array of length 16 containing in order
     */
    public Matrix4f(float[] v) {
        this.m00 = v[0];
        this.m01 = v[1];
        this.m02 = v[2];
        this.m03 = v[3];

        this.m10 = v[4];
        this.m11 = v[5];
        this.m12 = v[6];
        this.m13 = v[7];

        this.m20 = v[8];
        this.m21 = v[9];
        this.m22 = v[10];
        this.m23 = v[11];

        this.m30 = v[12];
        this.m31 = v[13];
        this.m32 = v[14];
        this.m33 = v[15];

    }

    /**
     * Constructs and initializes a Matrix4f from the rotation matrix,
     * translation, and scale values; the scale is applied only to the
     * rotational components of the matrix (upper 3x3) and not to the
     * translational components of the matrix.
     * 
     * @param m1
     *            the rotation matrix representing the rotational components
     * @param t1
     *            the translational components of the matrix
     * @param s
     *            the scale value applied to the rotational components
     */
    public Matrix4f(Matrix3d m1, Vec3D t1, float s) {
        this.m00 = (float) (m1.m00 * s);
        this.m01 = (float) (m1.m01 * s);
        this.m02 = (float) (m1.m02 * s);
        this.m03 = t1.x;

        this.m10 = (float) (m1.m10 * s);
        this.m11 = (float) (m1.m11 * s);
        this.m12 = (float) (m1.m12 * s);
        this.m13 = t1.y;

        this.m20 = (float) (m1.m20 * s);
        this.m21 = (float) (m1.m21 * s);
        this.m22 = (float) (m1.m22 * s);
        this.m23 = t1.z;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 1.0f;

    }

    /**
     * Constructs a new matrix with the same values as the Matrix4f parameter.
     * 
     * @param m1
     *            the source matrix
     */
    public Matrix4f(Matrix4f m1) {
        this.m00 = m1.m00;
        this.m01 = m1.m01;
        this.m02 = m1.m02;
        this.m03 = m1.m03;

        this.m10 = m1.m10;
        this.m11 = m1.m11;
        this.m12 = m1.m12;
        this.m13 = m1.m13;

        this.m20 = m1.m20;
        this.m21 = m1.m21;
        this.m22 = m1.m22;
        this.m23 = m1.m23;

        this.m30 = m1.m30;
        this.m31 = m1.m31;
        this.m32 = m1.m32;
        this.m33 = m1.m33;

    }

    /**
     * Constructs and initializes a Matrix4f from the quaternion, translation,
     * and scale values; the scale is applied only to the rotational components
     * of the matrix (upper 3x3) and not to the translational components.
     * 
     * @param q1
     *            the quaternion value representing the rotational component
     * @param t1
     *            the translational component of the matrix
     * @param s
     *            the scale value applied to the rotational components
     */
    public Matrix4f(Quaternion q1, Vec3D t1, float s) {
        m00 = (float) (s * (1.0 - 2.0 * q1.y * q1.y - 2.0 * q1.z * q1.z));
        m10 = (float) (s * (2.0 * (q1.x * q1.y + q1.w * q1.z)));
        m20 = (float) (s * (2.0 * (q1.x * q1.z - q1.w * q1.y)));

        m01 = (float) (s * (2.0 * (q1.x * q1.y - q1.w * q1.z)));
        m11 = (float) (s * (1.0 - 2.0 * q1.x * q1.x - 2.0 * q1.z * q1.z));
        m21 = (float) (s * (2.0 * (q1.y * q1.z + q1.w * q1.x)));

        m02 = (float) (s * (2.0 * (q1.x * q1.z + q1.w * q1.y)));
        m12 = (float) (s * (2.0 * (q1.y * q1.z - q1.w * q1.x)));
        m22 = (float) (s * (1.0 - 2.0 * q1.x * q1.x - 2.0 * q1.y * q1.y));

        m03 = t1.x;
        m13 = t1.y;
        m23 = t1.z;

        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        m33 = 1.0f;

    }

    /**
     * Adds a scalar to each component of this matrix.
     * 
     * @param scalar
     *            the scalar adder
     */
    public final void add(float scalar) {
        m00 += scalar;
        m01 += scalar;
        m02 += scalar;
        m03 += scalar;
        m10 += scalar;
        m11 += scalar;
        m12 += scalar;
        m13 += scalar;
        m20 += scalar;
        m21 += scalar;
        m22 += scalar;
        m23 += scalar;
        m30 += scalar;
        m31 += scalar;
        m32 += scalar;
        m33 += scalar;
    }

    /**
     * Adds a scalar to each component of the matrix m1 and places the result
     * into this. Matrix m1 is not modified.
     * 
     * @param scalar
     *            the scalar adder
     * @param m1
     *            the original matrix values
     */
    public final void add(float scalar, Matrix4f m1) {
        this.m00 = m1.m00 + scalar;
        this.m01 = m1.m01 + scalar;
        this.m02 = m1.m02 + scalar;
        this.m03 = m1.m03 + scalar;
        this.m10 = m1.m10 + scalar;
        this.m11 = m1.m11 + scalar;
        this.m12 = m1.m12 + scalar;
        this.m13 = m1.m13 + scalar;
        this.m20 = m1.m20 + scalar;
        this.m21 = m1.m21 + scalar;
        this.m22 = m1.m22 + scalar;
        this.m23 = m1.m23 + scalar;
        this.m30 = m1.m30 + scalar;
        this.m31 = m1.m31 + scalar;
        this.m32 = m1.m32 + scalar;
        this.m33 = m1.m33 + scalar;
    }

    /**
     * Sets the value of this matrix to the sum of itself and matrix m1.
     * 
     * @param m1
     *            the other matrix
     */
    public final void add(Matrix4f m1) {
        this.m00 += m1.m00;
        this.m01 += m1.m01;
        this.m02 += m1.m02;
        this.m03 += m1.m03;

        this.m10 += m1.m10;
        this.m11 += m1.m11;
        this.m12 += m1.m12;
        this.m13 += m1.m13;

        this.m20 += m1.m20;
        this.m21 += m1.m21;
        this.m22 += m1.m22;
        this.m23 += m1.m23;

        this.m30 += m1.m30;
        this.m31 += m1.m31;
        this.m32 += m1.m32;
        this.m33 += m1.m33;
    }

    /**
     * Sets the value of this matrix to the matrix sum of matrices m1 and m2.
     * 
     * @param m1
     *            the first matrix
     * @param m2
     *            the second matrix
     */
    public final void add(Matrix4f m1, Matrix4f m2) {
        this.m00 = m1.m00 + m2.m00;
        this.m01 = m1.m01 + m2.m01;
        this.m02 = m1.m02 + m2.m02;
        this.m03 = m1.m03 + m2.m03;

        this.m10 = m1.m10 + m2.m10;
        this.m11 = m1.m11 + m2.m11;
        this.m12 = m1.m12 + m2.m12;
        this.m13 = m1.m13 + m2.m13;

        this.m20 = m1.m20 + m2.m20;
        this.m21 = m1.m21 + m2.m21;
        this.m22 = m1.m22 + m2.m22;
        this.m23 = m1.m23 + m2.m23;

        this.m30 = m1.m30 + m2.m30;
        this.m31 = m1.m31 + m2.m31;
        this.m32 = m1.m32 + m2.m32;
        this.m33 = m1.m33 + m2.m33;
    }

    /**
     * Creates a new object of the same class as this object.
     * 
     * @return a clone of this instance.
     * @exception OutOfMemoryError
     *                if there is not enough memory.
     * @see java.lang.Cloneable
     * @since vecmath 1.3
     */
    public Object clone() {
        Matrix4f m1 = null;
        try {
            m1 = (Matrix4f) super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }

        return m1;
    }

    /**
     * Computes the determinate of this matrix.
     * 
     * @return the determinate of the matrix
     */
    public final float determinant() {
        float det;

        // cofactor exapainsion along first row

        det = m00
                * (m11 * m22 * m33 + m12 * m23 * m31 + m13 * m21 * m32 - m13
                        * m22 * m31 - m11 * m23 * m32 - m12 * m21 * m33);
        det -= m01
                * (m10 * m22 * m33 + m12 * m23 * m30 + m13 * m20 * m32 - m13
                        * m22 * m30 - m10 * m23 * m32 - m12 * m20 * m33);
        det += m02
                * (m10 * m21 * m33 + m11 * m23 * m30 + m13 * m20 * m31 - m13
                        * m21 * m30 - m10 * m23 * m31 - m11 * m20 * m33);
        det -= m03
                * (m10 * m21 * m32 + m11 * m22 * m30 + m12 * m20 * m31 - m12
                        * m21 * m30 - m10 * m22 * m31 - m11 * m20 * m32);

        return (det);
    }

    /**
     * Returns true if the L-infinite distance between this matrix and matrix m1
     * is less than or equal to the epsilon parameter, otherwise returns false.
     * The L-infinite distance is equal to MAX[i=0,1,2,3 ; j=0,1,2,3 ;
     * abs(this.m(i,j) - m1.m(i,j)]
     * 
     * @param m1
     *            the matrix to be compared to this matrix
     * @param epsilon
     *            the threshold value
     */
    public boolean epsilonEquals(Matrix4f m1, float epsilon) {

        boolean status = true;

        if (Math.abs(this.m00 - m1.m00) > epsilon) {
            status = false;
        }
        if (Math.abs(this.m01 - m1.m01) > epsilon) {
            status = false;
        }
        if (Math.abs(this.m02 - m1.m02) > epsilon) {
            status = false;
        }
        if (Math.abs(this.m03 - m1.m03) > epsilon) {
            status = false;
        }

        if (Math.abs(this.m10 - m1.m10) > epsilon) {
            status = false;
        }
        if (Math.abs(this.m11 - m1.m11) > epsilon) {
            status = false;
        }
        if (Math.abs(this.m12 - m1.m12) > epsilon) {
            status = false;
        }
        if (Math.abs(this.m13 - m1.m13) > epsilon) {
            status = false;
        }

        if (Math.abs(this.m20 - m1.m20) > epsilon) {
            status = false;
        }
        if (Math.abs(this.m21 - m1.m21) > epsilon) {
            status = false;
        }
        if (Math.abs(this.m22 - m1.m22) > epsilon) {
            status = false;
        }
        if (Math.abs(this.m23 - m1.m23) > epsilon) {
            status = false;
        }

        if (Math.abs(this.m30 - m1.m30) > epsilon) {
            status = false;
        }
        if (Math.abs(this.m31 - m1.m31) > epsilon) {
            status = false;
        }
        if (Math.abs(this.m32 - m1.m32) > epsilon) {
            status = false;
        }
        if (Math.abs(this.m33 - m1.m33) > epsilon) {
            status = false;
        }

        return (status);

    }

    /**
     * Returns true if all of the data members of Matrix4f m1 are equal to the
     * corresponding data members in this Matrix4f.
     * 
     * @param m1
     *            the matrix with which the comparison is made.
     * @return true or false
     */
    public boolean equals(Matrix4f m1) {
        try {
            return (this.m00 == m1.m00 && this.m01 == m1.m01
                    && this.m02 == m1.m02 && this.m03 == m1.m03
                    && this.m10 == m1.m10 && this.m11 == m1.m11
                    && this.m12 == m1.m12 && this.m13 == m1.m13
                    && this.m20 == m1.m20 && this.m21 == m1.m21
                    && this.m22 == m1.m22 && this.m23 == m1.m23
                    && this.m30 == m1.m30 && this.m31 == m1.m31
                    && this.m32 == m1.m32 && this.m33 == m1.m33);
        } catch (NullPointerException e2) {
            return false;
        }

    }

    /**
     * Returns true if the Object t1 is of type Matrix4f and all of the data
     * members of t1 are equal to the corresponding data members in this
     * Matrix4f.
     * 
     * @param t1
     *            the matrix with which the comparison is made.
     * @return true or false
     */
    public boolean equals(Object t1) {
        try {
            Matrix4f m2 = (Matrix4f) t1;
            return (this.m00 == m2.m00 && this.m01 == m2.m01
                    && this.m02 == m2.m02 && this.m03 == m2.m03
                    && this.m10 == m2.m10 && this.m11 == m2.m11
                    && this.m12 == m2.m12 && this.m13 == m2.m13
                    && this.m20 == m2.m20 && this.m21 == m2.m21
                    && this.m22 == m2.m22 && this.m23 == m2.m23
                    && this.m30 == m2.m30 && this.m31 == m2.m31
                    && this.m32 == m2.m32 && this.m33 == m2.m33);
        } catch (ClassCastException e1) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    /**
     * Performs an SVD normalization of this matrix in order to acquire the
     * normalized rotational component; the values are placed into the Matrix3d
     * parameter.
     * 
     * @param m1
     *            matrix into which the rotational component is placed
     */
    public final void get(Matrix3d m1) {

        double[] tmp_rot = new double[9]; // scratch matrix
        double[] tmp_scale = new double[3]; // scratch matrix

        getScaleRotate(tmp_scale, tmp_rot);

        m1.m00 = tmp_rot[0];
        m1.m01 = tmp_rot[1];
        m1.m02 = tmp_rot[2];

        m1.m10 = tmp_rot[3];
        m1.m11 = tmp_rot[4];
        m1.m12 = tmp_rot[5];

        m1.m20 = tmp_rot[6];
        m1.m21 = tmp_rot[7];
        m1.m22 = tmp_rot[8];

    }

    /**
     * Performs an SVD normalization of this matrix to calculate the rotation as
     * a 3x3 matrix, the translation, and the scale. None of the matrix values
     * are modified.
     * 
     * @param m1
     *            the normalized matrix representing the rotation
     * @param t1
     *            the translation component
     * @return the scale component of this transform
     */
    public final double get(Matrix3d m1, Vec3D t1) {
        double[] tmp_rot = new double[9];
        double[] tmp_scale = new double[3];
        getScaleRotate(tmp_scale, tmp_rot);

        m1.m00 = tmp_rot[0];
        m1.m01 = tmp_rot[1];
        m1.m02 = tmp_rot[2];

        m1.m10 = tmp_rot[3];
        m1.m11 = tmp_rot[4];
        m1.m12 = tmp_rot[5];

        m1.m20 = tmp_rot[6];
        m1.m21 = tmp_rot[7];
        m1.m22 = tmp_rot[8];

        t1.x = m03;
        t1.y = m13;
        t1.z = m23;

        return MathUtils.max(tmp_scale);

    }

    /**
     * Performs an SVD normalization of this matrix in order to acquire the
     * normalized rotational component; the values are placed into the Quat4f
     * parameter.
     * 
     * @param q1
     *            quaternion into which the rotation component is placed
     */
    public final void get(Quaternion q1) {
        double[] tmp_rot = new double[9]; // scratch matrix
        double[] tmp_scale = new double[3]; // scratch matrix
        getScaleRotate(tmp_scale, tmp_rot);

        double ww;

        ww = 0.25 * (1.0 + tmp_rot[0] + tmp_rot[4] + tmp_rot[8]);
        if (!((ww < 0 ? -ww : ww) < 1.0e-30)) {
            q1.w = (float) Math.sqrt(ww);
            ww = 0.25 / q1.w;
            q1.x = (float) ((tmp_rot[7] - tmp_rot[5]) * ww);
            q1.y = (float) ((tmp_rot[2] - tmp_rot[6]) * ww);
            q1.z = (float) ((tmp_rot[3] - tmp_rot[1]) * ww);
            return;
        }

        q1.w = 0.0f;
        ww = -0.5 * (tmp_rot[4] + tmp_rot[8]);
        if (!((ww < 0 ? -ww : ww) < 1.0e-30)) {
            q1.x = (float) Math.sqrt(ww);
            ww = 0.5 / q1.x;
            q1.y = (float) (tmp_rot[3] * ww);
            q1.z = (float) (tmp_rot[6] * ww);
            return;
        }

        q1.x = 0.0f;
        ww = 0.5 * (1.0 - tmp_rot[8]);
        if (!((ww < 0 ? -ww : ww) < 1.0e-30)) {
            q1.y = (float) (Math.sqrt(ww));
            q1.z = (float) (tmp_rot[7] / (2.0 * q1.y));
            return;
        }

        q1.y = 0.0f;
        q1.z = 1.0f;

    }

    /**
     * Retrieves the translational components of this matrix.
     * 
     * @param trans
     *            the vector that will receive the translational component
     */
    public final void get(Vec3D trans) {
        trans.x = m03;
        trans.y = m13;
        trans.z = m23;
    }

    /**
     * Copies the matrix values in the specified column into the array
     * parameter.
     * 
     * @param column
     *            the matrix column
     * @param v
     *            the array into which the matrix row values will be copied
     */
    public final void getColumn(int column, float v[]) {
        if (column == 0) {
            v[0] = m00;
            v[1] = m10;
            v[2] = m20;
            v[3] = m30;
        } else if (column == 1) {
            v[0] = m01;
            v[1] = m11;
            v[2] = m21;
            v[3] = m31;
        } else if (column == 2) {
            v[0] = m02;
            v[1] = m12;
            v[2] = m22;
            v[3] = m32;
        } else if (column == 3) {
            v[0] = m03;
            v[1] = m13;
            v[2] = m23;
            v[3] = m33;
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }

    }

    /**
     * Copies the matrix values in the specified column into the vector
     * parameter.
     * 
     * @param column
     *            the matrix column
     * @param v
     *            the vector into which the matrix row values will be copied
     */
    public final void getColumn(int column, Vec4D v) {
        if (column == 0) {
            v.x = m00;
            v.y = m10;
            v.z = m20;
            v.w = m30;
        } else if (column == 1) {
            v.x = m01;
            v.y = m11;
            v.z = m21;
            v.w = m31;
        } else if (column == 2) {
            v.x = m02;
            v.y = m12;
            v.z = m22;
            v.w = m32;
        } else if (column == 3) {
            v.x = m03;
            v.y = m13;
            v.z = m23;
            v.w = m33;
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }

    }

    /**
     * Retrieves the value at the specified row and column of this matrix.
     * 
     * @param row
     *            the row number to be retrieved (zero indexed)
     * @param column
     *            the column number to be retrieved (zero indexed)
     * @return the value at the indexed element
     */
    public final float getElement(int row, int column) {
        switch (row) {
            case 0:
                switch (column) {
                    case 0:
                        return (this.m00);
                    case 1:
                        return (this.m01);
                    case 2:
                        return (this.m02);
                    case 3:
                        return (this.m03);
                    default:
                        break;
                }
                break;
            case 1:
                switch (column) {
                    case 0:
                        return (this.m10);
                    case 1:
                        return (this.m11);
                    case 2:
                        return (this.m12);
                    case 3:
                        return (this.m13);
                    default:
                        break;
                }
                break;

            case 2:
                switch (column) {
                    case 0:
                        return (this.m20);
                    case 1:
                        return (this.m21);
                    case 2:
                        return (this.m22);
                    case 3:
                        return (this.m23);
                    default:
                        break;
                }
                break;

            case 3:
                switch (column) {
                    case 0:
                        return (this.m30);
                    case 1:
                        return (this.m31);
                    case 2:
                        return (this.m32);
                    case 3:
                        return (this.m33);
                    default:
                        break;
                }
                break;

            default:
                break;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    /**
     * Get the first matrix element in the first row.
     * 
     * @return Returns the m00.
     * 
     * @since vecmath 1.5
     */
    public final float getM00() {
        return m00;
    }

    /**
     * Get the second matrix element in the first row.
     * 
     * @return Returns the m01.
     * 
     * @since vecmath 1.5
     */
    public final float getM01() {
        return m01;
    }

    /**
     * Get the third matrix element in the first row.
     * 
     * @return Returns the m02.
     * 
     * @since vecmath 1.5
     */
    public final float getM02() {
        return m02;
    }

    /**
     * Get the fourth element of the first row.
     * 
     * @return Returns the m03.
     * 
     * @since vecmath 1.5
     */
    public final float getM03() {
        return m03;
    }

    /**
     * Get first matrix element in the second row.
     * 
     * @return Returns the m10.
     * 
     * @since vecmath 1.5
     */
    public final float getM10() {
        return m10;
    }

    /**
     * Get second matrix element in the second row.
     * 
     * @return Returns the m11.
     * 
     * @since vecmath 1.5
     */
    public final float getM11() {
        return m11;
    }

    /**
     * Get the third matrix element in the second row.
     * 
     * @return Returns the m12.
     * 
     * @since vecmath 1.5
     */
    public final float getM12() {
        return m12;
    }

    /**
     * Get the fourth element of the second row.
     * 
     * @return Returns the m13.
     * 
     * @since vecmath 1.5
     */
    public final float getM13() {
        return m13;
    }

    /**
     * Get the first matrix element in the third row.
     * 
     * @return Returns the m20.
     * 
     * @since vecmath 1.5
     */
    public final float getM20() {
        return m20;
    }

    /**
     * Get the second matrix element in the third row.
     * 
     * @return Returns the m21.
     * 
     * @since vecmath 1.5
     */
    public final float getM21() {
        return m21;
    }

    /**
     * Get the third matrix element in the third row.
     * 
     * @return Returns the m22.
     * 
     * @since vecmath 1.5
     */
    public final float getM22() {
        return m22;
    }

    /**
     * Get the fourth element of the third row.
     * 
     * @return Returns the m23.
     * 
     * @since vecmath 1.5
     */
    public final float getM23() {
        return m23;
    }

    /**
     * Get the first element of the fourth row.
     * 
     * @return Returns the m30.
     * 
     * @since vecmath 1.5
     */
    public final float getM30() {
        return m30;
    }

    /**
     * Get the second element of the fourth row.
     * 
     * @return Returns the m31.
     * 
     * @since vecmath 1.5
     */
    public final float getM31() {
        return m31;
    }

    /**
     * Get the third element of the fourth row.
     * 
     * @return Returns the m32.
     * 
     * @since vecmath 1.5
     */
    public final float getM32() {
        return m32;
    }

    /**
     * Get the fourth element of the fourth row.
     * 
     * @return Returns the m33.
     * 
     * @since vecmath 1.5
     */
    public final float getM33() {
        return m33;
    }

    /**
     * Gets the upper 3x3 values of this matrix and places them into the matrix
     * m1.
     * 
     * @param m1
     *            the matrix that will hold the values
     */
    public final void getRotationScale(Matrix3d m1) {
        m1.m00 = m00;
        m1.m01 = m01;
        m1.m02 = m02;
        m1.m10 = m10;
        m1.m11 = m11;
        m1.m12 = m12;
        m1.m20 = m20;
        m1.m21 = m21;
        m1.m22 = m22;
    }

    /**
     * Copies the matrix values in the specified row into the array parameter.
     * 
     * @param row
     *            the matrix row
     * @param v
     *            the array into which the matrix row values will be copied
     */
    public final void getRow(int row, float v[]) {
        if (row == 0) {
            v[0] = m00;
            v[1] = m01;
            v[2] = m02;
            v[3] = m03;
        } else if (row == 1) {
            v[0] = m10;
            v[1] = m11;
            v[2] = m12;
            v[3] = m13;
        } else if (row == 2) {
            v[0] = m20;
            v[1] = m21;
            v[2] = m22;
            v[3] = m23;
        } else if (row == 3) {
            v[0] = m30;
            v[1] = m31;
            v[2] = m32;
            v[3] = m33;
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }

    }

    /**
     * Copies the matrix values in the specified row into the vector parameter.
     * 
     * @param row
     *            the matrix row
     * @param v
     *            the vector into which the matrix row values will be copied
     */
    public final void getRow(int row, Vec4D v) {
        if (row == 0) {
            v.x = m00;
            v.y = m01;
            v.z = m02;
            v.w = m03;
        } else if (row == 1) {
            v.x = m10;
            v.y = m11;
            v.z = m12;
            v.w = m13;
        } else if (row == 2) {
            v.x = m20;
            v.y = m21;
            v.z = m22;
            v.w = m23;
        } else if (row == 3) {
            v.x = m30;
            v.y = m31;
            v.z = m32;
            v.w = m33;
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }

    }

    /**
     * Performs an SVD normalization of this matrix to calculate and return the
     * uniform scale factor. If the matrix has non-uniform scale factors, the
     * largest of the x, y, and z scale factors will be returned. This matrix is
     * not modified.
     * 
     * @return the scale factor of this matrix
     */
    public final float getScale() {
        double[] tmp_scale = new double[3];
        getScaleRotate(tmp_scale, new double[9]);
        return ((float) MathUtils.max(tmp_scale));
    }

    private final void getScaleRotate(double scales[], double rots[]) {

        double[] tmp = new double[9]; // scratch matrix
        tmp[0] = m00;
        tmp[1] = m01;
        tmp[2] = m02;

        tmp[3] = m10;
        tmp[4] = m11;
        tmp[5] = m12;

        tmp[6] = m20;
        tmp[7] = m21;
        tmp[8] = m22;

        Matrix3d.compute_svd(tmp, scales, rots);
    }

    /**
     * Returns a hash code value based on the data values in this object. Two
     * different Matrix4f objects with identical data values (i.e.,
     * Matrix4f.equals returns true) will return the same hash code value. Two
     * objects with different data members may return the same hash value,
     * although this is not likely.
     * 
     * @return the integer hash code value
     */
    public int hashCode() {
        long bits = 1L;
        bits = 31L * bits + VecMathUtil.floatToIntBits(m00);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m01);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m02);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m03);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m10);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m11);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m12);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m13);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m20);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m21);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m22);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m23);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m30);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m31);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m32);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m33);
        return (int) (bits ^ (bits >> 32));
    }

    /**
     * Inverts this matrix in place.
     */
    public final void invert() {
        invertGeneral(this);
    }

    /**
     * Sets the value of this matrix to the matrix inverse of the passed (user
     * declared) matrix m1.
     * 
     * @param m1
     *            the matrix to be inverted
     */
    public final void invert(Matrix4f m1) {

        invertGeneral(m1);
    }

    /**
     * General invert routine. Inverts m1 and places the result in "this". Note
     * that this routine handles both the "this" version and the non-"this"
     * version.
     * 
     * Also note that since this routine is slow anyway, we won't worry about
     * allocating a little bit of garbage.
     */
    final void invertGeneral(Matrix4f m1) {
        double temp[] = new double[16];
        double result[] = new double[16];
        int row_perm[] = new int[4];
        int i, r, c;

        // Use LU decomposition and backsubstitution code specifically
        // for floating-point 4x4 matrices.

        // Copy source matrix to t1tmp
        temp[0] = m1.m00;
        temp[1] = m1.m01;
        temp[2] = m1.m02;
        temp[3] = m1.m03;

        temp[4] = m1.m10;
        temp[5] = m1.m11;
        temp[6] = m1.m12;
        temp[7] = m1.m13;

        temp[8] = m1.m20;
        temp[9] = m1.m21;
        temp[10] = m1.m22;
        temp[11] = m1.m23;

        temp[12] = m1.m30;
        temp[13] = m1.m31;
        temp[14] = m1.m32;
        temp[15] = m1.m33;

        // Calculate LU decomposition: Is the matrix singular?
        if (!Matrix4x4.LUDecomposition(temp, row_perm, 4)) {
            // Matrix has no inverse
            throw new SingularMatrixException();
        }

        // Perform back substitution on the identity matrix
        for (i = 0; i < 16; i++) {
            result[i] = 0.0;
        }
        result[0] = 1.0;
        result[5] = 1.0;
        result[10] = 1.0;
        result[15] = 1.0;
        luBacksubstitution(temp, row_perm, result);

        this.m00 = (float) result[0];
        this.m01 = (float) result[1];
        this.m02 = (float) result[2];
        this.m03 = (float) result[3];

        this.m10 = (float) result[4];
        this.m11 = (float) result[5];
        this.m12 = (float) result[6];
        this.m13 = (float) result[7];

        this.m20 = (float) result[8];
        this.m21 = (float) result[9];
        this.m22 = (float) result[10];
        this.m23 = (float) result[11];

        this.m30 = (float) result[12];
        this.m31 = (float) result[13];
        this.m32 = (float) result[14];
        this.m33 = (float) result[15];

    }

    /**
     * Multiplies each element of this matrix by a scalar.
     * 
     * @param scalar
     *            the scalar multiplier.
     */
    public final void mul(float scalar) {
        m00 *= scalar;
        m01 *= scalar;
        m02 *= scalar;
        m03 *= scalar;
        m10 *= scalar;
        m11 *= scalar;
        m12 *= scalar;
        m13 *= scalar;
        m20 *= scalar;
        m21 *= scalar;
        m22 *= scalar;
        m23 *= scalar;
        m30 *= scalar;
        m31 *= scalar;
        m32 *= scalar;
        m33 *= scalar;
    }

    /**
     * Multiplies each element of matrix m1 by a scalar and places the result
     * into this. Matrix m1 is not modified.
     * 
     * @param scalar
     *            the scalar multiplier.
     * @param m1
     *            the original matrix.
     */
    public final void mul(float scalar, Matrix4f m1) {
        this.m00 = m1.m00 * scalar;
        this.m01 = m1.m01 * scalar;
        this.m02 = m1.m02 * scalar;
        this.m03 = m1.m03 * scalar;
        this.m10 = m1.m10 * scalar;
        this.m11 = m1.m11 * scalar;
        this.m12 = m1.m12 * scalar;
        this.m13 = m1.m13 * scalar;
        this.m20 = m1.m20 * scalar;
        this.m21 = m1.m21 * scalar;
        this.m22 = m1.m22 * scalar;
        this.m23 = m1.m23 * scalar;
        this.m30 = m1.m30 * scalar;
        this.m31 = m1.m31 * scalar;
        this.m32 = m1.m32 * scalar;
        this.m33 = m1.m33 * scalar;
    }

    /**
     * Sets the value of this matrix to the result of multiplying itself with
     * matrix m1.
     * 
     * @param m1
     *            the other matrix
     */
    public final void mul(Matrix4f m1) {
        float m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33; // vars
                                                                                              // for
                                                                                              // temp
                                                                                              // result
                                                                                              // matrix

        m00 = this.m00 * m1.m00 + this.m01 * m1.m10 + this.m02 * m1.m20
                + this.m03 * m1.m30;
        m01 = this.m00 * m1.m01 + this.m01 * m1.m11 + this.m02 * m1.m21
                + this.m03 * m1.m31;
        m02 = this.m00 * m1.m02 + this.m01 * m1.m12 + this.m02 * m1.m22
                + this.m03 * m1.m32;
        m03 = this.m00 * m1.m03 + this.m01 * m1.m13 + this.m02 * m1.m23
                + this.m03 * m1.m33;

        m10 = this.m10 * m1.m00 + this.m11 * m1.m10 + this.m12 * m1.m20
                + this.m13 * m1.m30;
        m11 = this.m10 * m1.m01 + this.m11 * m1.m11 + this.m12 * m1.m21
                + this.m13 * m1.m31;
        m12 = this.m10 * m1.m02 + this.m11 * m1.m12 + this.m12 * m1.m22
                + this.m13 * m1.m32;
        m13 = this.m10 * m1.m03 + this.m11 * m1.m13 + this.m12 * m1.m23
                + this.m13 * m1.m33;

        m20 = this.m20 * m1.m00 + this.m21 * m1.m10 + this.m22 * m1.m20
                + this.m23 * m1.m30;
        m21 = this.m20 * m1.m01 + this.m21 * m1.m11 + this.m22 * m1.m21
                + this.m23 * m1.m31;
        m22 = this.m20 * m1.m02 + this.m21 * m1.m12 + this.m22 * m1.m22
                + this.m23 * m1.m32;
        m23 = this.m20 * m1.m03 + this.m21 * m1.m13 + this.m22 * m1.m23
                + this.m23 * m1.m33;

        m30 = this.m30 * m1.m00 + this.m31 * m1.m10 + this.m32 * m1.m20
                + this.m33 * m1.m30;
        m31 = this.m30 * m1.m01 + this.m31 * m1.m11 + this.m32 * m1.m21
                + this.m33 * m1.m31;
        m32 = this.m30 * m1.m02 + this.m31 * m1.m12 + this.m32 * m1.m22
                + this.m33 * m1.m32;
        m33 = this.m30 * m1.m03 + this.m31 * m1.m13 + this.m32 * m1.m23
                + this.m33 * m1.m33;

        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    /**
     * Sets the value of this matrix to the result of multiplying the two
     * argument matrices together.
     * 
     * @param m1
     *            the first matrix
     * @param m2
     *            the second matrix
     */
    public final void mul(Matrix4f m1, Matrix4f m2) {
        if (this != m1 && this != m2) {

            this.m00 = m1.m00 * m2.m00 + m1.m01 * m2.m10 + m1.m02 * m2.m20
                    + m1.m03 * m2.m30;
            this.m01 = m1.m00 * m2.m01 + m1.m01 * m2.m11 + m1.m02 * m2.m21
                    + m1.m03 * m2.m31;
            this.m02 = m1.m00 * m2.m02 + m1.m01 * m2.m12 + m1.m02 * m2.m22
                    + m1.m03 * m2.m32;
            this.m03 = m1.m00 * m2.m03 + m1.m01 * m2.m13 + m1.m02 * m2.m23
                    + m1.m03 * m2.m33;

            this.m10 = m1.m10 * m2.m00 + m1.m11 * m2.m10 + m1.m12 * m2.m20
                    + m1.m13 * m2.m30;
            this.m11 = m1.m10 * m2.m01 + m1.m11 * m2.m11 + m1.m12 * m2.m21
                    + m1.m13 * m2.m31;
            this.m12 = m1.m10 * m2.m02 + m1.m11 * m2.m12 + m1.m12 * m2.m22
                    + m1.m13 * m2.m32;
            this.m13 = m1.m10 * m2.m03 + m1.m11 * m2.m13 + m1.m12 * m2.m23
                    + m1.m13 * m2.m33;

            this.m20 = m1.m20 * m2.m00 + m1.m21 * m2.m10 + m1.m22 * m2.m20
                    + m1.m23 * m2.m30;
            this.m21 = m1.m20 * m2.m01 + m1.m21 * m2.m11 + m1.m22 * m2.m21
                    + m1.m23 * m2.m31;
            this.m22 = m1.m20 * m2.m02 + m1.m21 * m2.m12 + m1.m22 * m2.m22
                    + m1.m23 * m2.m32;
            this.m23 = m1.m20 * m2.m03 + m1.m21 * m2.m13 + m1.m22 * m2.m23
                    + m1.m23 * m2.m33;

            this.m30 = m1.m30 * m2.m00 + m1.m31 * m2.m10 + m1.m32 * m2.m20
                    + m1.m33 * m2.m30;
            this.m31 = m1.m30 * m2.m01 + m1.m31 * m2.m11 + m1.m32 * m2.m21
                    + m1.m33 * m2.m31;
            this.m32 = m1.m30 * m2.m02 + m1.m31 * m2.m12 + m1.m32 * m2.m22
                    + m1.m33 * m2.m32;
            this.m33 = m1.m30 * m2.m03 + m1.m31 * m2.m13 + m1.m32 * m2.m23
                    + m1.m33 * m2.m33;
        } else {
            float m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33; // vars
                                                                                                  // for
                                                                                                  // temp
                                                                                                  // result
                                                                                                  // matrix
            m00 = m1.m00 * m2.m00 + m1.m01 * m2.m10 + m1.m02 * m2.m20 + m1.m03
                    * m2.m30;
            m01 = m1.m00 * m2.m01 + m1.m01 * m2.m11 + m1.m02 * m2.m21 + m1.m03
                    * m2.m31;
            m02 = m1.m00 * m2.m02 + m1.m01 * m2.m12 + m1.m02 * m2.m22 + m1.m03
                    * m2.m32;
            m03 = m1.m00 * m2.m03 + m1.m01 * m2.m13 + m1.m02 * m2.m23 + m1.m03
                    * m2.m33;

            m10 = m1.m10 * m2.m00 + m1.m11 * m2.m10 + m1.m12 * m2.m20 + m1.m13
                    * m2.m30;
            m11 = m1.m10 * m2.m01 + m1.m11 * m2.m11 + m1.m12 * m2.m21 + m1.m13
                    * m2.m31;
            m12 = m1.m10 * m2.m02 + m1.m11 * m2.m12 + m1.m12 * m2.m22 + m1.m13
                    * m2.m32;
            m13 = m1.m10 * m2.m03 + m1.m11 * m2.m13 + m1.m12 * m2.m23 + m1.m13
                    * m2.m33;

            m20 = m1.m20 * m2.m00 + m1.m21 * m2.m10 + m1.m22 * m2.m20 + m1.m23
                    * m2.m30;
            m21 = m1.m20 * m2.m01 + m1.m21 * m2.m11 + m1.m22 * m2.m21 + m1.m23
                    * m2.m31;
            m22 = m1.m20 * m2.m02 + m1.m21 * m2.m12 + m1.m22 * m2.m22 + m1.m23
                    * m2.m32;
            m23 = m1.m20 * m2.m03 + m1.m21 * m2.m13 + m1.m22 * m2.m23 + m1.m23
                    * m2.m33;

            m30 = m1.m30 * m2.m00 + m1.m31 * m2.m10 + m1.m32 * m2.m20 + m1.m33
                    * m2.m30;
            m31 = m1.m30 * m2.m01 + m1.m31 * m2.m11 + m1.m32 * m2.m21 + m1.m33
                    * m2.m31;
            m32 = m1.m30 * m2.m02 + m1.m31 * m2.m12 + m1.m32 * m2.m22 + m1.m33
                    * m2.m32;
            m33 = m1.m30 * m2.m03 + m1.m31 * m2.m13 + m1.m32 * m2.m23 + m1.m33
                    * m2.m33;

            this.m00 = m00;
            this.m01 = m01;
            this.m02 = m02;
            this.m03 = m03;
            this.m10 = m10;
            this.m11 = m11;
            this.m12 = m12;
            this.m13 = m13;
            this.m20 = m20;
            this.m21 = m21;
            this.m22 = m22;
            this.m23 = m23;
            this.m30 = m30;
            this.m31 = m31;
            this.m32 = m32;
            this.m33 = m33;
        }
    }

    /**
     * Multiplies the transpose of matrix m1 times the transpose of matrix m2,
     * and places the result into this.
     * 
     * @param m1
     *            the matrix on the left hand side of the multiplication
     * @param m2
     *            the matrix on the right hand side of the multiplication
     */
    public final void mulTransposeBoth(Matrix4f m1, Matrix4f m2) {
        if (this != m1 && this != m2) {
            this.m00 = m1.m00 * m2.m00 + m1.m10 * m2.m01 + m1.m20 * m2.m02
                    + m1.m30 * m2.m03;
            this.m01 = m1.m00 * m2.m10 + m1.m10 * m2.m11 + m1.m20 * m2.m12
                    + m1.m30 * m2.m13;
            this.m02 = m1.m00 * m2.m20 + m1.m10 * m2.m21 + m1.m20 * m2.m22
                    + m1.m30 * m2.m23;
            this.m03 = m1.m00 * m2.m30 + m1.m10 * m2.m31 + m1.m20 * m2.m32
                    + m1.m30 * m2.m33;

            this.m10 = m1.m01 * m2.m00 + m1.m11 * m2.m01 + m1.m21 * m2.m02
                    + m1.m31 * m2.m03;
            this.m11 = m1.m01 * m2.m10 + m1.m11 * m2.m11 + m1.m21 * m2.m12
                    + m1.m31 * m2.m13;
            this.m12 = m1.m01 * m2.m20 + m1.m11 * m2.m21 + m1.m21 * m2.m22
                    + m1.m31 * m2.m23;
            this.m13 = m1.m01 * m2.m30 + m1.m11 * m2.m31 + m1.m21 * m2.m32
                    + m1.m31 * m2.m33;

            this.m20 = m1.m02 * m2.m00 + m1.m12 * m2.m01 + m1.m22 * m2.m02
                    + m1.m32 * m2.m03;
            this.m21 = m1.m02 * m2.m10 + m1.m12 * m2.m11 + m1.m22 * m2.m12
                    + m1.m32 * m2.m13;
            this.m22 = m1.m02 * m2.m20 + m1.m12 * m2.m21 + m1.m22 * m2.m22
                    + m1.m32 * m2.m23;
            this.m23 = m1.m02 * m2.m30 + m1.m12 * m2.m31 + m1.m22 * m2.m32
                    + m1.m32 * m2.m33;

            this.m30 = m1.m03 * m2.m00 + m1.m13 * m2.m01 + m1.m23 * m2.m02
                    + m1.m33 * m2.m03;
            this.m31 = m1.m03 * m2.m10 + m1.m13 * m2.m11 + m1.m23 * m2.m12
                    + m1.m33 * m2.m13;
            this.m32 = m1.m03 * m2.m20 + m1.m13 * m2.m21 + m1.m23 * m2.m22
                    + m1.m33 * m2.m23;
            this.m33 = m1.m03 * m2.m30 + m1.m13 * m2.m31 + m1.m23 * m2.m32
                    + m1.m33 * m2.m33;
        } else {
            float m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, // vars
                                                                              // for
                                                                              // temp
                                                                              // result
                                                                              // matrix
            m30, m31, m32, m33;

            m00 = m1.m00 * m2.m00 + m1.m10 * m2.m01 + m1.m20 * m2.m02 + m1.m30
                    * m2.m03;
            m01 = m1.m00 * m2.m10 + m1.m10 * m2.m11 + m1.m20 * m2.m12 + m1.m30
                    * m2.m13;
            m02 = m1.m00 * m2.m20 + m1.m10 * m2.m21 + m1.m20 * m2.m22 + m1.m30
                    * m2.m23;
            m03 = m1.m00 * m2.m30 + m1.m10 * m2.m31 + m1.m20 * m2.m32 + m1.m30
                    * m2.m33;

            m10 = m1.m01 * m2.m00 + m1.m11 * m2.m01 + m1.m21 * m2.m02 + m1.m31
                    * m2.m03;
            m11 = m1.m01 * m2.m10 + m1.m11 * m2.m11 + m1.m21 * m2.m12 + m1.m31
                    * m2.m13;
            m12 = m1.m01 * m2.m20 + m1.m11 * m2.m21 + m1.m21 * m2.m22 + m1.m31
                    * m2.m23;
            m13 = m1.m01 * m2.m30 + m1.m11 * m2.m31 + m1.m21 * m2.m32 + m1.m31
                    * m2.m33;

            m20 = m1.m02 * m2.m00 + m1.m12 * m2.m01 + m1.m22 * m2.m02 + m1.m32
                    * m2.m03;
            m21 = m1.m02 * m2.m10 + m1.m12 * m2.m11 + m1.m22 * m2.m12 + m1.m32
                    * m2.m13;
            m22 = m1.m02 * m2.m20 + m1.m12 * m2.m21 + m1.m22 * m2.m22 + m1.m32
                    * m2.m23;
            m23 = m1.m02 * m2.m30 + m1.m12 * m2.m31 + m1.m22 * m2.m32 + m1.m32
                    * m2.m33;

            m30 = m1.m03 * m2.m00 + m1.m13 * m2.m01 + m1.m23 * m2.m02 + m1.m33
                    * m2.m03;
            m31 = m1.m03 * m2.m10 + m1.m13 * m2.m11 + m1.m23 * m2.m12 + m1.m33
                    * m2.m13;
            m32 = m1.m03 * m2.m20 + m1.m13 * m2.m21 + m1.m23 * m2.m22 + m1.m33
                    * m2.m23;
            m33 = m1.m03 * m2.m30 + m1.m13 * m2.m31 + m1.m23 * m2.m32 + m1.m33
                    * m2.m33;

            this.m00 = m00;
            this.m01 = m01;
            this.m02 = m02;
            this.m03 = m03;
            this.m10 = m10;
            this.m11 = m11;
            this.m12 = m12;
            this.m13 = m13;
            this.m20 = m20;
            this.m21 = m21;
            this.m22 = m22;
            this.m23 = m23;
            this.m30 = m30;
            this.m31 = m31;
            this.m32 = m32;
            this.m33 = m33;
        }

    }

    /**
     * Multiplies the transpose of matrix m1 times matrix m2, and places the
     * result into this.
     * 
     * @param m1
     *            the matrix on the left hand side of the multiplication
     * @param m2
     *            the matrix on the right hand side of the multiplication
     */
    public final void mulTransposeLeft(Matrix4f m1, Matrix4f m2) {
        if (this != m1 && this != m2) {
            this.m00 = m1.m00 * m2.m00 + m1.m10 * m2.m10 + m1.m20 * m2.m20
                    + m1.m30 * m2.m30;
            this.m01 = m1.m00 * m2.m01 + m1.m10 * m2.m11 + m1.m20 * m2.m21
                    + m1.m30 * m2.m31;
            this.m02 = m1.m00 * m2.m02 + m1.m10 * m2.m12 + m1.m20 * m2.m22
                    + m1.m30 * m2.m32;
            this.m03 = m1.m00 * m2.m03 + m1.m10 * m2.m13 + m1.m20 * m2.m23
                    + m1.m30 * m2.m33;

            this.m10 = m1.m01 * m2.m00 + m1.m11 * m2.m10 + m1.m21 * m2.m20
                    + m1.m31 * m2.m30;
            this.m11 = m1.m01 * m2.m01 + m1.m11 * m2.m11 + m1.m21 * m2.m21
                    + m1.m31 * m2.m31;
            this.m12 = m1.m01 * m2.m02 + m1.m11 * m2.m12 + m1.m21 * m2.m22
                    + m1.m31 * m2.m32;
            this.m13 = m1.m01 * m2.m03 + m1.m11 * m2.m13 + m1.m21 * m2.m23
                    + m1.m31 * m2.m33;

            this.m20 = m1.m02 * m2.m00 + m1.m12 * m2.m10 + m1.m22 * m2.m20
                    + m1.m32 * m2.m30;
            this.m21 = m1.m02 * m2.m01 + m1.m12 * m2.m11 + m1.m22 * m2.m21
                    + m1.m32 * m2.m31;
            this.m22 = m1.m02 * m2.m02 + m1.m12 * m2.m12 + m1.m22 * m2.m22
                    + m1.m32 * m2.m32;
            this.m23 = m1.m02 * m2.m03 + m1.m12 * m2.m13 + m1.m22 * m2.m23
                    + m1.m32 * m2.m33;

            this.m30 = m1.m03 * m2.m00 + m1.m13 * m2.m10 + m1.m23 * m2.m20
                    + m1.m33 * m2.m30;
            this.m31 = m1.m03 * m2.m01 + m1.m13 * m2.m11 + m1.m23 * m2.m21
                    + m1.m33 * m2.m31;
            this.m32 = m1.m03 * m2.m02 + m1.m13 * m2.m12 + m1.m23 * m2.m22
                    + m1.m33 * m2.m32;
            this.m33 = m1.m03 * m2.m03 + m1.m13 * m2.m13 + m1.m23 * m2.m23
                    + m1.m33 * m2.m33;
        } else {
            float m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, // vars
                                                                              // for
                                                                              // temp
                                                                              // result
                                                                              // matrix
            m30, m31, m32, m33;

            m00 = m1.m00 * m2.m00 + m1.m10 * m2.m10 + m1.m20 * m2.m20 + m1.m30
                    * m2.m30;
            m01 = m1.m00 * m2.m01 + m1.m10 * m2.m11 + m1.m20 * m2.m21 + m1.m30
                    * m2.m31;
            m02 = m1.m00 * m2.m02 + m1.m10 * m2.m12 + m1.m20 * m2.m22 + m1.m30
                    * m2.m32;
            m03 = m1.m00 * m2.m03 + m1.m10 * m2.m13 + m1.m20 * m2.m23 + m1.m30
                    * m2.m33;

            m10 = m1.m01 * m2.m00 + m1.m11 * m2.m10 + m1.m21 * m2.m20 + m1.m31
                    * m2.m30;
            m11 = m1.m01 * m2.m01 + m1.m11 * m2.m11 + m1.m21 * m2.m21 + m1.m31
                    * m2.m31;
            m12 = m1.m01 * m2.m02 + m1.m11 * m2.m12 + m1.m21 * m2.m22 + m1.m31
                    * m2.m32;
            m13 = m1.m01 * m2.m03 + m1.m11 * m2.m13 + m1.m21 * m2.m23 + m1.m31
                    * m2.m33;

            m20 = m1.m02 * m2.m00 + m1.m12 * m2.m10 + m1.m22 * m2.m20 + m1.m32
                    * m2.m30;
            m21 = m1.m02 * m2.m01 + m1.m12 * m2.m11 + m1.m22 * m2.m21 + m1.m32
                    * m2.m31;
            m22 = m1.m02 * m2.m02 + m1.m12 * m2.m12 + m1.m22 * m2.m22 + m1.m32
                    * m2.m32;
            m23 = m1.m02 * m2.m03 + m1.m12 * m2.m13 + m1.m22 * m2.m23 + m1.m32
                    * m2.m33;

            m30 = m1.m03 * m2.m00 + m1.m13 * m2.m10 + m1.m23 * m2.m20 + m1.m33
                    * m2.m30;
            m31 = m1.m03 * m2.m01 + m1.m13 * m2.m11 + m1.m23 * m2.m21 + m1.m33
                    * m2.m31;
            m32 = m1.m03 * m2.m02 + m1.m13 * m2.m12 + m1.m23 * m2.m22 + m1.m33
                    * m2.m32;
            m33 = m1.m03 * m2.m03 + m1.m13 * m2.m13 + m1.m23 * m2.m23 + m1.m33
                    * m2.m33;

            this.m00 = m00;
            this.m01 = m01;
            this.m02 = m02;
            this.m03 = m03;
            this.m10 = m10;
            this.m11 = m11;
            this.m12 = m12;
            this.m13 = m13;
            this.m20 = m20;
            this.m21 = m21;
            this.m22 = m22;
            this.m23 = m23;
            this.m30 = m30;
            this.m31 = m31;
            this.m32 = m32;
            this.m33 = m33;
        }

    }

    /**
     * Multiplies matrix m1 times the transpose of matrix m2, and places the
     * result into this.
     * 
     * @param m1
     *            the matrix on the left hand side of the multiplication
     * @param m2
     *            the matrix on the right hand side of the multiplication
     */
    public final void mulTransposeRight(Matrix4f m1, Matrix4f m2) {
        if (this != m1 && this != m2) {
            this.m00 = m1.m00 * m2.m00 + m1.m01 * m2.m01 + m1.m02 * m2.m02
                    + m1.m03 * m2.m03;
            this.m01 = m1.m00 * m2.m10 + m1.m01 * m2.m11 + m1.m02 * m2.m12
                    + m1.m03 * m2.m13;
            this.m02 = m1.m00 * m2.m20 + m1.m01 * m2.m21 + m1.m02 * m2.m22
                    + m1.m03 * m2.m23;
            this.m03 = m1.m00 * m2.m30 + m1.m01 * m2.m31 + m1.m02 * m2.m32
                    + m1.m03 * m2.m33;

            this.m10 = m1.m10 * m2.m00 + m1.m11 * m2.m01 + m1.m12 * m2.m02
                    + m1.m13 * m2.m03;
            this.m11 = m1.m10 * m2.m10 + m1.m11 * m2.m11 + m1.m12 * m2.m12
                    + m1.m13 * m2.m13;
            this.m12 = m1.m10 * m2.m20 + m1.m11 * m2.m21 + m1.m12 * m2.m22
                    + m1.m13 * m2.m23;
            this.m13 = m1.m10 * m2.m30 + m1.m11 * m2.m31 + m1.m12 * m2.m32
                    + m1.m13 * m2.m33;

            this.m20 = m1.m20 * m2.m00 + m1.m21 * m2.m01 + m1.m22 * m2.m02
                    + m1.m23 * m2.m03;
            this.m21 = m1.m20 * m2.m10 + m1.m21 * m2.m11 + m1.m22 * m2.m12
                    + m1.m23 * m2.m13;
            this.m22 = m1.m20 * m2.m20 + m1.m21 * m2.m21 + m1.m22 * m2.m22
                    + m1.m23 * m2.m23;
            this.m23 = m1.m20 * m2.m30 + m1.m21 * m2.m31 + m1.m22 * m2.m32
                    + m1.m23 * m2.m33;

            this.m30 = m1.m30 * m2.m00 + m1.m31 * m2.m01 + m1.m32 * m2.m02
                    + m1.m33 * m2.m03;
            this.m31 = m1.m30 * m2.m10 + m1.m31 * m2.m11 + m1.m32 * m2.m12
                    + m1.m33 * m2.m13;
            this.m32 = m1.m30 * m2.m20 + m1.m31 * m2.m21 + m1.m32 * m2.m22
                    + m1.m33 * m2.m23;
            this.m33 = m1.m30 * m2.m30 + m1.m31 * m2.m31 + m1.m32 * m2.m32
                    + m1.m33 * m2.m33;
        } else {
            float m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, // vars
                                                                              // for
                                                                              // temp
                                                                              // result
                                                                              // matrix
            m30, m31, m32, m33;

            m00 = m1.m00 * m2.m00 + m1.m01 * m2.m01 + m1.m02 * m2.m02 + m1.m03
                    * m2.m03;
            m01 = m1.m00 * m2.m10 + m1.m01 * m2.m11 + m1.m02 * m2.m12 + m1.m03
                    * m2.m13;
            m02 = m1.m00 * m2.m20 + m1.m01 * m2.m21 + m1.m02 * m2.m22 + m1.m03
                    * m2.m23;
            m03 = m1.m00 * m2.m30 + m1.m01 * m2.m31 + m1.m02 * m2.m32 + m1.m03
                    * m2.m33;

            m10 = m1.m10 * m2.m00 + m1.m11 * m2.m01 + m1.m12 * m2.m02 + m1.m13
                    * m2.m03;
            m11 = m1.m10 * m2.m10 + m1.m11 * m2.m11 + m1.m12 * m2.m12 + m1.m13
                    * m2.m13;
            m12 = m1.m10 * m2.m20 + m1.m11 * m2.m21 + m1.m12 * m2.m22 + m1.m13
                    * m2.m23;
            m13 = m1.m10 * m2.m30 + m1.m11 * m2.m31 + m1.m12 * m2.m32 + m1.m13
                    * m2.m33;

            m20 = m1.m20 * m2.m00 + m1.m21 * m2.m01 + m1.m22 * m2.m02 + m1.m23
                    * m2.m03;
            m21 = m1.m20 * m2.m10 + m1.m21 * m2.m11 + m1.m22 * m2.m12 + m1.m23
                    * m2.m13;
            m22 = m1.m20 * m2.m20 + m1.m21 * m2.m21 + m1.m22 * m2.m22 + m1.m23
                    * m2.m23;
            m23 = m1.m20 * m2.m30 + m1.m21 * m2.m31 + m1.m22 * m2.m32 + m1.m23
                    * m2.m33;

            m30 = m1.m30 * m2.m00 + m1.m31 * m2.m01 + m1.m32 * m2.m02 + m1.m33
                    * m2.m03;
            m31 = m1.m30 * m2.m10 + m1.m31 * m2.m11 + m1.m32 * m2.m12 + m1.m33
                    * m2.m13;
            m32 = m1.m30 * m2.m20 + m1.m31 * m2.m21 + m1.m32 * m2.m22 + m1.m33
                    * m2.m23;
            m33 = m1.m30 * m2.m30 + m1.m31 * m2.m31 + m1.m32 * m2.m32 + m1.m33
                    * m2.m33;

            this.m00 = m00;
            this.m01 = m01;
            this.m02 = m02;
            this.m03 = m03;
            this.m10 = m10;
            this.m11 = m11;
            this.m12 = m12;
            this.m13 = m13;
            this.m20 = m20;
            this.m21 = m21;
            this.m22 = m22;
            this.m23 = m23;
            this.m30 = m30;
            this.m31 = m31;
            this.m32 = m32;
            this.m33 = m33;
        }

    }

    /**
     * Negates the value of this matrix: this = -this.
     */
    public final void negate() {
        m00 = -m00;
        m01 = -m01;
        m02 = -m02;
        m03 = -m03;
        m10 = -m10;
        m11 = -m11;
        m12 = -m12;
        m13 = -m13;
        m20 = -m20;
        m21 = -m21;
        m22 = -m22;
        m23 = -m23;
        m30 = -m30;
        m31 = -m31;
        m32 = -m32;
        m33 = -m33;
    }

    /**
     * Sets the value of this matrix equal to the negation of of the Matrix4f
     * parameter.
     * 
     * @param m1
     *            the source matrix
     */
    public final void negate(Matrix4f m1) {
        this.m00 = -m1.m00;
        this.m01 = -m1.m01;
        this.m02 = -m1.m02;
        this.m03 = -m1.m03;
        this.m10 = -m1.m10;
        this.m11 = -m1.m11;
        this.m12 = -m1.m12;
        this.m13 = -m1.m13;
        this.m20 = -m1.m20;
        this.m21 = -m1.m21;
        this.m22 = -m1.m22;
        this.m23 = -m1.m23;
        this.m30 = -m1.m30;
        this.m31 = -m1.m31;
        this.m32 = -m1.m32;
        this.m33 = -m1.m33;
    }

    /**
     * Sets the value of this matrix to a counter clockwise rotation about the x
     * axis.
     * 
     * @param angle
     *            the angle to rotate about the X axis in radians
     */
    public final void rotX(float angle) {
        float sinAngle, cosAngle;

        sinAngle = (float) Math.sin(angle);
        cosAngle = (float) Math.cos(angle);

        this.m00 = (float) 1.0;
        this.m01 = (float) 0.0;
        this.m02 = (float) 0.0;
        this.m03 = (float) 0.0;

        this.m10 = (float) 0.0;
        this.m11 = cosAngle;
        this.m12 = -sinAngle;
        this.m13 = (float) 0.0;

        this.m20 = (float) 0.0;
        this.m21 = sinAngle;
        this.m22 = cosAngle;
        this.m23 = (float) 0.0;

        this.m30 = (float) 0.0;
        this.m31 = (float) 0.0;
        this.m32 = (float) 0.0;
        this.m33 = (float) 1.0;
    }

    /**
     * Sets the value of this matrix to a counter clockwise rotation about the y
     * axis.
     * 
     * @param angle
     *            the angle to rotate about the Y axis in radians
     */
    public final void rotY(float angle) {
        float sinAngle, cosAngle;

        sinAngle = (float) Math.sin(angle);
        cosAngle = (float) Math.cos(angle);

        this.m00 = cosAngle;
        this.m01 = (float) 0.0;
        this.m02 = sinAngle;
        this.m03 = (float) 0.0;

        this.m10 = (float) 0.0;
        this.m11 = (float) 1.0;
        this.m12 = (float) 0.0;
        this.m13 = (float) 0.0;

        this.m20 = -sinAngle;
        this.m21 = (float) 0.0;
        this.m22 = cosAngle;
        this.m23 = (float) 0.0;

        this.m30 = (float) 0.0;
        this.m31 = (float) 0.0;
        this.m32 = (float) 0.0;
        this.m33 = (float) 1.0;
    }

    /**
     * Sets the value of this matrix to a counter clockwise rotation about the z
     * axis.
     * 
     * @param angle
     *            the angle to rotate about the Z a
