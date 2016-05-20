/*
 * $RCSfile: Transform3D.java,v $
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.10 $
 * $Date: 2007/04/12 17:34:07 $
 * $State: Exp $
 */

package org.jcae.geometry;

import javax.vecmath.*;

/**
 * A generalized transform object represented internally as a 4x4
 * double-precision floating point matrix.  The mathematical
 * representation is
 * row major, as in traditional matrix mathematics.
 * A Transform3D is used to perform translations, rotations, and
 * scaling and shear effects.<P>
 *
 * A transform has an associated type, and
 * all type classification is left to the Transform3D object.
 * A transform will typically have multiple types, unless it is a
 * general, unclassifiable matrix, in which case it won't be assigned
 * a type.  <P>
 *
 * The Transform3D type is internally computed when the transform
 * object is constructed and updated any time it is modified. A
 * matrix will typically have multiple types. For example, the type
 * associated with an identity matrix is the result of ORing all of
 * the types, except for ZERO and NEGATIVE_DETERMINANT, together.
 * There are public methods available to get the ORed type of the
 * transformation, the sign of the determinant, and the least
 * general matrix type. The matrix type flags are defined as
 * follows:<P>
 * <UL>
 * <LI>ZERO - zero matrix. All of the elements in the matrix
 * have the value 0.</LI><P>
 * <LI>IDENTITY - identity matrix. A matrix with ones on its
 * main diagonal and zeros every where else.</LI><P>
 * <LI>SCALE - the matrix is a uniform scale matrix - there are
 * no rotational or translation components.</LI><P>
 * <LI>ORTHOGONAL - the four row vectors that make up an orthogonal
 * matrix form a basis, meaning that they are mutually orthogonal.
 * The scale is unity and there are no translation components.</LI><P>
 * <LI>RIGID - the upper 3 X 3 of the matrix is orthogonal, and
 * there is a translation component-the scale is unity.</LI><P>
 * <LI>CONGRUENT - this is an angle- and length-preserving matrix,
 * meaning that it can translate, rotate, and reflect about an axis,
 * and scale by an amount that is uniform in all directions. These
 * operations preserve the distance between any two points, and the
 * angle between any two intersecting lines.</LI><P>
 * <LI>AFFINE - an affine matrix can translate, rotate, reflect,
 * scale anisotropically, and shear. Lines remain straight, and parallel
 * lines remain parallel, but the angle between intersecting lines can
 * change.</LI><P>
 * </UL>
 * A matrix is also classified by the sign of its determinant:<P>
 * <UL>
 * NEGATIVE_DETERMINANT - this matrix has a negative determinant.
 * An orthogonal matrix with a positive determinant is a rotation
 * matrix. An orthogonal matrix with a negative determinant is a
 * reflection and rotation matrix.<P></UL>
 * The Java 3D model for 4 X 4 transformations is:<P>
 * <UL><pre>
 * [ m00 m01 m02 m03 ]   [ x ]   [ x' ]
 * [ m10 m11 m12 m13 ] . [ y ] = [ y' ]
 * [ m20 m21 m22 m23 ]   [ z ]   [ z' ]
 * [ m30 m31 m32 m33 ]   [ w ]   [ w' ]
 *
 * x' = m00 . x+m01 . y+m02 . z+m03 . w
 * y' = m10 . x+m11 . y+m12 . z+m13 . w
 * z' = m20 . x+m21 . y+m22 . z+m23 . w
 * w' = m30 . x+m31 . y+m32 . z+m33 . w
 * </pre></ul><P>
 * Note: When transforming a Point3f or a Point3d, the input w is set to
 * 1. When transforming a Vector3f or Vector3d, the input w is set to 0.
 */

public class Transform3D {

    double[] mat = new double[16];
    //double[] rot = new double[9];
    //double[] scales = new double[3];
    // allocate the memory only when it is needed. Following three places will allocate the memory,
    // void setScaleTranslation(), void computeScales() and void computeScaleRotation()
    double[] rot;
    double[] scales;

    // Unknown until lazy classification is done
    private int type;

    // Dirty bit for classification, this is used
    // for classify()
    private static final int AFFINE_BIT     = 0x01;
    private static final int ORTHO_BIT = 0x02;
    private static final int CONGRUENT_BIT  = 0x04;
    private static final int RIGID_BIT      = 0x08;
    private static final int CLASSIFY_BIT   = 0x10;

    // this is used for scales[], rot[]
    private static final int SCALE_BIT      = 0x20;
    private static final int ROTATION_BIT   = 0x40;
    // set when SVD renormalization is necessary
    private static final int SVD_BIT        = 0x80;

    private static final int CLASSIFY_ALL_DIRTY = AFFINE_BIT |
                                                  ORTHO_BIT |
                                                  CONGRUENT_BIT |
                                                  RIGID_BIT |
                                                  CLASSIFY_BIT;
    private static final int ROTSCALESVD_DIRTY = SCALE_BIT |
                                                  ROTATION_BIT |
                                                  SVD_BIT;
    private static final int ALL_DIRTY = CLASSIFY_ALL_DIRTY | ROTSCALESVD_DIRTY;

    private int dirtyBits;

    boolean autoNormalize;	// Don't auto normalize by default
    /*
    // reused temporaries for compute_svd
    private boolean svdAllocd =false;
    private double[] u1 = null;
    private double[] v1 = null;
    private double[] t1 = null; // used by both compute_svd and compute_qr
    private double[] t2 = null; // used by both compute_svd and compute_qr
    private double[] ts = null;
    private double[] svdTmp = null;
    private double[] svdRot = null;
    private double[] single_values = null;
    private double[] e = null;
    private double[] svdScales = null;
    // from svrReorder
    private int[] svdOut = null;
    private double[] svdMag = null;

    // from compute_qr
    private double[]   cosl  = null;
    private double[]   cosr  = null;
    private double[]   sinl  = null;
    private double[]   sinr  = null;
    private double[]   qr_m  = null;
    */

    private static final double EPS = 1.110223024E-16;

    static final double EPSILON = 1.0e-10;
    static final double EPSILON_ABSOLUTE = 1.0e-5;
    static final double EPSILON_RELATIVE = 1.0e-4;
    /**
     * A zero matrix.
     */
    public static final int ZERO = 0x01;

   /**
    * An identity matrix.
    */
    public static final int IDENTITY = 0x02;


   /**
    * A Uniform scale matrix with no translation or other
    * off-diagonal components.
    */
    public static final int SCALE = 0x04;

   /**
    * A translation-only matrix with ones on the diagonal.
    *
    */
    public static final int TRANSLATION = 0x08;

   /**
    * The four row vectors that make up an orthogonal matrix form a basis,
    * meaning that they are mutually orthogonal; an orthogonal matrix with
    * positive determinant is a pure rotation matrix; a negative
    * determinant indicates a rotation and a reflection.
    */
    public static final int ORTHOGONAL = 0x10;

   /**
    * This matrix is a rotation and a translation with unity scale;
    * The upper 3x3 of the matrix is orthogonal, and there is a
    * translation component.
    */
    public static final int RIGID = 0x20;

   /**
    * This is an angle and length preserving matrix, meaning that it
    * can translate, rotate, and reflect
    * about an axis, and scale by an amount that is uniform in all directions.
    * These operations preserve the distance between any two points and the
    * angle between any two intersecting lines.
    */
    public static final int CONGRUENT = 0x40;

   /**
    * An affine matrix can translate, rotate, reflect, scale anisotropically,
    * and shear.  Lines remain straight, and parallel lines remain parallel,
    * but the angle between intersecting lines can change. In order for a
    * transform to be classified as affine, the 4th row must be: [0, 0, 0, 1].
    */
    public static final int AFFINE = 0x80;

   /**
    * This matrix has a negative determinant; an orthogonal matrix with
    * a positive determinant is a rotation matrix; an orthogonal matrix
    * with a negative determinant is a reflection and rotation matrix.
    */
    public static final int NEGATIVE_DETERMINANT = 0x100;

    /**
     * The upper 3x3 column vectors that make up an orthogonal
     * matrix form a basis meaning that they are mutually orthogonal.
     * It can have non-uniform or zero x/y/z scale as long as
     * the dot product of any two column is zero.
     * This one is used by Java3D internal only and should not
     * expose to the user.
     */
    private static final int ORTHO = 0x40000000;

    /**
     * Constructs and initializes a transform from the 4 x 4 matrix.  The
     * type of the constructed transform will be classified automatically.
     * @param m1 the 4 x 4 transformation matrix
     */
    public Transform3D(Matrix4f m1) {
	set(m1);
    }

    /**
     * Constructs and initializes a transform from the 4 x 4 matrix.  The
     * type of the constructed transform will be classified automatically.
     * @param m1 the 4 x 4 transformation matrix
     */
    public Transform3D(Matrix4d m1) {
	set(m1);
    }

    /**
     * Constructs and initializes a transform from the Transform3D object.
     * @param t1  the transformation object to be copied
     */
    public Transform3D(Transform3D t1) {
	set(t1);
    }

    /**
     * Constructs and initializes a transform to the identity matrix.
     */
    public Transform3D() {
        setIdentity();			// this will also classify the matrix
    }

   /**
     * Constructs and initializes a transform from the float array of
     * length 16; the top row of the matrix is initialized to the first
     * four elements of the array, and so on.  The type of the transform
     * object is classified internally.
     * @param matrix  a float array of 16
     */
    public Transform3D(float[] matrix) {
	set(matrix);
    }

   /**
     * Constructs and initializes a transform from the double precision array
     * of length 16; the top row of the matrix is initialized to the first
     * four elements of the array, and so on.  The type of the transform is
     * classified internally.
     * @param matrix  a float array of 16
     */
    public Transform3D(double[] matrix) {
	set(matrix);
    }

   /**
     * Constructs and initializes a transform from the quaternion,
     * translation, and scale values.   The scale is applied only to the
     * rotational components of the matrix (upper 3 x 3) and not to the
     * translational components of the matrix.
     * @param q1  the quaternion value representing the rotational component
     * @param t1  the translational component of the matrix
     * @param s   the scale value applied to the rotational components
     */
    public Transform3D(Quat4d q1, Vector3d t1, double s) {
	set(q1, t1, s);
    }

   /**
     * Constructs and initializes a transform from the quaternion,
     * translation, and scale values.   The scale is applied only to the
     * rotational components of the matrix (upper 3 x 3) and not to the
     * translational components of the matrix.
     * @param q1  the quaternion value representing the rotational component
     * @param t1  the translational component of the matrix
     * @param s   the scale value applied to the rotational components
     */
    public Transform3D(Quat4f q1, Vector3d t1, double s) {
	set(q1, t1, s);
    }

   /**
     * Constructs and initializes a transform from the quaternion,
     * translation, and scale values.   The scale is applied only to the
     * rotational components of the matrix (upper 3 x 3) and not to the
     * translational components of the matrix.
     * @param q1  the quaternion value representing the rotational component
     * @param t1  the translational component of the matrix
     * @param s   the scale value applied to the rotational components
     */
    public Transform3D(Quat4f q1, Vector3f t1, float s) {
	set(q1, t1, s);
    }

   /**
     * Constructs a transform and initializes it to the upper 4 x 4
     * of the GMatrix argument.  If the parameter matrix is
     * smaller than 4 x 4, the remaining elements in the transform matrix are
     * assigned to zero.
     * @param m1 the GMatrix
     */
    public Transform3D(GMatrix m1) {
	set(m1);
    }

   /**
     * Constructs and initializes a transform from the rotation matrix,
     * translation, and scale values.   The scale is applied only to the
     * rotational component of the matrix (upper 3x3) and not to the
     * translational component of the matrix.
     * @param m1  the rotation matrix representing the rotational component
     * @param t1  the translational component of the matrix
     * @param s   the scale value applied to the rotational components
     */
    public Transform3D(Matrix3f m1, Vector3d t1, double s) {
	set(m1, t1, s);
    }

   /**
     * Constructs and initializes a transform from the rotation matrix,
     * translation, and scale values.   The scale is applied only to the
     * rotational components of the matrix (upper 3x3) and not to the
     * translational components of the matrix.
     * @param m1  the rotation matrix representing the rotational component
     * @param t1  the translational component of the matrix
     * @param s   the scale value applied to the rotational components
     */
    public Transform3D(Matrix3d m1, Vector3d t1, double s) {
	set(m1, t1, s);
    }


   /**
     * Constructs and initializes a transform from the rotation matrix,
     * translation, and scale values.   The scale is applied only to the
     * rotational components of the matrix (upper 3x3) and not to the
     * translational components of the matrix.
     * @param m1  the rotation matrix representing the rotational component
     * @param t1  the translational component of the matrix
     * @param s   the scale value applied to the rotational components
     */
    public Transform3D(Matrix3f m1, Vector3f t1, float s) {
	set(m1, t1, s);
    }

   /**
     * Returns the type of this matrix as an or'ed bitmask of
     * of all of the type classifications to which it belongs.
     * @return  or'ed bitmask of all of the type classifications
     * of this transform
     */
    public final int getType() {
	if ((dirtyBits & CLASSIFY_BIT) != 0) {
	    classify();
	}
	// clear ORTHO bit which only use internally
	return (type & ~ORTHO);
    }

    // True if type is ORTHO
    // Since ORTHO didn't take into account the last row.
    final boolean isOrtho() {
	if ((dirtyBits & ORTHO_BIT) != 0) {
	    if ((almostZero(mat[0]*mat[2] + mat[4]*mat[6] +
			    mat[8]*mat[10]) &&
		 almostZero(mat[0]*mat[1] + mat[4]*mat[5] +
			    mat[8]*mat[9]) &&
		 almostZero(mat[1]*mat[2] + mat[5]*mat[6] +
			    mat[9]*mat[10]))) {
		type |= ORTHO;
		dirtyBits &= ~ORTHO_BIT;
		return true;
	    } else {
		type &= ~ORTHO;
		dirtyBits &= ~ORTHO_BIT;
		return false;
	    }
	}
	return ((type & ORTHO) != 0);
    }

    final boolean isCongruent() {
	if ((dirtyBits & CONGRUENT_BIT) != 0) {
	    // This will also classify AFFINE
		classifyRigid();
	}
	return ((type & CONGRUENT) != 0);
    }

    final boolean isAffine() {
	if ((dirtyBits & AFFINE_BIT) != 0) {
	    classifyAffine();
	}
	return ((type & AFFINE) != 0);
    }

    final boolean isRigid() {
	if ((dirtyBits & RIGID_BIT) != 0) {


	    // This will also classify AFFINE & CONGRUENT
	    if ((dirtyBits & CONGRUENT_BIT) != 0) {
		classifyRigid();
	    } else {

		if ((type & CONGRUENT) != 0) {
		    // Matrix is Congruent, need only
		    // to check scale
		    double s;
		    if ((dirtyBits & SCALE_BIT) != 0){
			s = mat[0]*mat[0] + mat[4]*mat[4] +
			    mat[8]*mat[8];
			// Note that
			// scales[0] = sqrt(s);
			// but since sqrt(1) = 1,
			// we don't need to do s = sqrt(s) here.
		    } else {
			if(scales == null)
			    scales = new double[3];
			s = scales[0];
		    }
		    if (almostOne(s)) {
			type |= RIGID;
		    } else {
			type &= ~RIGID;
		    }
		} else {
		    // Not even congruent, so isRigid must be false
		    type &= ~RIGID;
		}
		dirtyBits &= ~RIGID_BIT;
	    }
	}
	return ((type & RIGID) != 0);
    }


   /**
     * Returns the least general type of this matrix; the order of
     * generality from least to most is: ZERO, IDENTITY,
     * SCALE/TRANSLATION, ORTHOGONAL, RIGID, CONGRUENT, AFFINE.
     * If the matrix is ORTHOGONAL, calling the method
     * getDeterminantSign() will yield more information.
     * @return the least general matrix type
     */
    public final int getBestType() {
	getType();   // force classify if necessary

	if ((type & ZERO)                 != 0 ) return ZERO;
	if ((type & IDENTITY)             != 0 ) return IDENTITY;
	if ((type & SCALE)                != 0 ) return SCALE;
	if ((type & TRANSLATION)          != 0 ) return TRANSLATION;
	if ((type & ORTHOGONAL)           != 0 ) return ORTHOGONAL;
	if ((type & RIGID)                != 0 ) return RIGID;
	if ((type & CONGRUENT)            != 0 ) return CONGRUENT;
	if ((type & AFFINE)               != 0 ) return AFFINE;
	if ((type & NEGATIVE_DETERMINANT) != 0 ) return NEGATIVE_DETERMINANT;
	return 0;
    }

    /*
    private void print_type() {
        if ((type & ZERO)                 > 0 ) System.err.print(" ZERO");
	if ((type & IDENTITY)             > 0 ) System.err.print(" IDENTITY");
	if ((type & SCALE)                > 0 ) System.err.print(" SCALE");
	if ((type & TRANSLATION)          > 0 ) System.err.print(" TRANSLATION");
	if ((type & ORTHOGONAL)           > 0 ) System.err.print(" ORTHOGONAL");
	if ((type & RIGID)                > 0 ) System.err.print(" RIGID");
	if ((type & CONGRUENT)            > 0 ) System.err.print(" CONGRUENT");
	if ((type & AFFINE)               > 0 ) System.err.print(" AFFINE");
	if ((type & NEGATIVE_DETERMINANT) > 0 ) System.err.print(" NEGATIVE_DETERMINANT");
	}
    */

    /**
     * Returns the sign of the determinant of this matrix; a return value
     * of true indicates a non-negative determinant; a return value of false
     * indicates a negative determinant. A value of true will be returned if
     * the determinant is NaN. In general, an orthogonal matrix
     * with a positive determinant is a pure rotation matrix; an orthogonal
     * matrix with a negative determinant is a both a rotation and a
     * reflection matrix.
     * @return  determinant sign : true means non-negative, false means negative
     */
    public final boolean getDeterminantSign() {
        double det = determinant();
        if (Double.isNaN(det)) {
            return true;
        }
        return det >= 0;
    }

    /**
     * Sets a flag that enables or disables automatic SVD
     * normalization.  If this flag is enabled, an automatic SVD
     * normalization of the rotational components (upper 3x3) of this
     * matrix is done after every subsequent matrix operation that
     * modifies this matrix.  This is functionally equivalent to
     * calling normalize() after every subsequent call, but may be
     * less computationally expensive.
     * The default value for this parameter is false.
     * @param autoNormalize  the boolean state of auto normalization
     */
    public final void setAutoNormalize(boolean autoNormalize) {
	this.autoNormalize = autoNormalize;

	if (autoNormalize) {
	    normalize();
	}
    }

    /**
     * Returns the state of auto-normalization.
     * @return  boolean state of auto-normalization
     */
    public final boolean getAutoNormalize() {
	return this.autoNormalize;
    }

    /**
     * Transforms the point parameter with this transform and
     * places the result into pointOut.  The fourth element of the
     * point input paramter is assumed to be one.
     * @param point  the input point to be transformed
     * @param pointOut  the transformed point
     */
    void transform(Point3d point, Point4d pointOut) {
        
        pointOut.x = mat[0]*point.x + mat[1]*point.y +
                mat[2]*point.z + mat[3];
        pointOut.y = mat[4]*point.x + mat[5]*point.y +
                mat[6]*point.z + mat[7];
        pointOut.z = mat[8]*point.x + mat[9]*point.y +
                mat[10]*point.z + mat[11];
        pointOut.w = mat[12]*point.x + mat[13]*point.y +
                mat[14]*point.z + mat[15];
    }
   
    
    
    private static final boolean almostZero(double a) {
	return ((a < EPSILON_ABSOLUTE) && (a > -EPSILON_ABSOLUTE));
    }

    private static final boolean almostOne(double a) {
	return ((a < 1+EPSILON_ABSOLUTE) && (a > 1-EPSILON_ABSOLUTE));
    }

    private static final boolean almostEqual(double a, double b) {
	double diff = a-b;

	if (diff >= 0) {
	    if (diff < EPSILON) {
		return true;
	    }
	    // a > b
	    if ((b > 0) || (a > -b)) {
		return (diff < EPSILON_RELATIVE*a);
	    } else {
		return (diff < -EPSILON_RELATIVE*b);
	    }

	} else {
	    if (diff > -EPSILON) {
		return true;
	    }
	    // a < b
	    if ((b < 0) || (-a > b)) {
		return (diff > EPSILON_RELATIVE*a);
	    } else {
		return (diff > -EPSILON_RELATIVE*b);
	    }
	}
    }

    private final void classifyAffine() {
        if (!isInfOrNaN() &&
                almostZero(mat[12]) &&
                almostZero(mat[13]) &&
                almostZero(mat[14]) &&
                almostOne(mat[15])) {
	    type |= AFFINE;
	} else {
	    type &= ~AFFINE;
	}
	dirtyBits &= ~AFFINE_BIT;
    }

    // same amount of work to classify rigid and congruent
    private final void classifyRigid() {

	if ((dirtyBits & AFFINE_BIT) != 0) {
	    // should not touch ORTHO bit
	    type &= ORTHO;
	    classifyAffine();
	} else {
	    // keep the affine bit if there is one
	    // and clear the others (CONGRUENT/RIGID) bit
	    type &= (ORTHO | AFFINE);
	}

	if ((type & AFFINE) != 0) {
	    // checking orthogonal condition
	    if (isOrtho()) {
		if ((dirtyBits & SCALE_BIT) != 0) {
		    double s0 = mat[0]*mat[0] + mat[4]*mat[4] +
			mat[8]*mat[8];
		    double s1 = mat[1]*mat[1] + mat[5]*mat[5] +
			mat[9]*mat[9];
		    if (almostEqual(s0, s1)) {
			double s2 = mat[2]*mat[2] + mat[6]*mat[6] +
			    mat[10]*mat[10];
			if (almostEqual(s2, s0)) {
			    type |= CONGRUENT;
			    // Note that scales[0] = sqrt(s0);
			    if (almostOne(s0)) {
				type |= RIGID;
			    }
			}
		    }
		} else {
		    if(scales == null)
			scales = new double[3];

		    double s = scales[0];
		    if (almostEqual(s, scales[1]) &&
			almostEqual(s, scales[2])) {
			type |= CONGRUENT;
			if (almostOne(s)) {
			    type |= RIGID;
			}
		    }
		}
	    }
	}
	dirtyBits &= (~RIGID_BIT | ~CONGRUENT_BIT);
    }

    /**
     * Classifies a matrix.
     */
    private final void classify() {

	if ((dirtyBits & (RIGID_BIT|AFFINE_BIT|CONGRUENT_BIT)) != 0) {
	    // Test for RIGID, CONGRUENT, AFFINE.
	    classifyRigid();
	}

	// Test for ZERO, IDENTITY, SCALE, TRANSLATION,
	// ORTHOGONAL, NEGATIVE_DETERMINANT
	if ((type & AFFINE) != 0) {
	    if ((type & CONGRUENT) != 0) {
		if ((type & RIGID) != 0) {
		    if (zeroTranslation()) {
			type |= ORTHOGONAL;
			if (rotateZero()) {
			    // mat[0], mat[5], mat[10] can be only be
			    // 1 or -1 when reach here
			    if ((mat[0] > 0) &&
				(mat[5] > 0) &&
				(mat[10] > 0)) {
				type |= IDENTITY|SCALE|TRANSLATION;
			    }
			}
		    } else {
			if (rotateZero()) {
			    type |= TRANSLATION;
			}
		    }
		} else {
		    // uniform scale
		    if (zeroTranslation() && rotateZero()) {
			type |= SCALE;
		    }
		}

	    }
	} else {
	    // last row is not (0, 0, 0, 1)
	    if (almostZero(mat[12]) &&
		almostZero(mat[13]) &&
		almostZero(mat[14]) &&
		almostZero(mat[15]) &&
		zeroTranslation() &&
		rotateZero() &&
		almostZero(mat[0]) &&
		almostZero(mat[5]) &&
		almostZero(mat[10])) {
		type |= ZERO;
	    }
	}

	if (!getDeterminantSign()) {
	    type |= NEGATIVE_DETERMINANT;
	}
	dirtyBits &= ~CLASSIFY_BIT;
    }

    final boolean zeroTranslation() {
	return (almostZero(mat[3]) &&
		almostZero(mat[7]) &&
		almostZero(mat[11]));
    }

    final boolean rotateZero() {
	return (almostZero(mat[1]) && almostZero(mat[2]) &&
		almostZero(mat[4]) && almostZero(mat[6]) &&
		almostZero(mat[8]) && almostZero(mat[9]));
    }

   /**
     * Returns the matrix elements of this transform as a string.
     * @return  the matrix elements of this transform
     */
    public String toString() {
	// also, print classification?
	return
	    mat[0] + ", " + mat[1] + ", " + mat[2] + ", " + mat[3] + "\n" +
	    mat[4] + ", " + mat[5] + ", " + mat[6] + ", " + mat[7] + "\n" +
	    mat[8] + ", " + mat[9] + ", " + mat[10] + ", " + mat[11] + "\n" +
	    mat[12] + ", " + mat[13] + ", " + mat[14] + ", " + mat[15]
	    + "\n";
    }

    /**
     * Sets this transform to the identity matrix.
     */
    public final void setIdentity() {
	mat[0] = 1.0;  mat[1] = 0.0;  mat[2] = 0.0;  mat[3] = 0.0;
	mat[4] = 0.0;  mat[5] = 1.0;  mat[6] = 0.0;  mat[7] = 0.0;
	mat[8] = 0.0;  mat[9] = 0.0;  mat[10] = 1.0; mat[11] = 0.0;
	mat[12] = 0.0; mat[13] = 0.0; mat[14] = 0.0; mat[15] = 1.0;
	type = IDENTITY | SCALE |  ORTHOGONAL | RIGID | CONGRUENT |
	       AFFINE | TRANSLATION | ORTHO;
	dirtyBits = SCALE_BIT | ROTATION_BIT;
	// No need to set SVD_BIT
    }

   /**
     * Sets this transform to all zeros.
     */
    public final void setZero() {
	mat[0] = 0.0;  mat[1] = 0.0;  mat[2] = 0.0;  mat[3] = 0.0;
	mat[4] = 0.0;  mat[5] = 0.0;  mat[6] = 0.0;  mat[7] = 0.0;
	mat[8] = 0.0;  mat[9] = 0.0;  mat[10] = 0.0; mat[11] = 0.0;
	mat[12] = 0.0; mat[13] = 0.0; mat[14] = 0.0; mat[15] = 0.0;

	type = ZERO | ORTHO;
	dirtyBits = SCALE_BIT | ROTATION_BIT;
    }


   /**
     * Adds this transform to transform t1 and places the result into
     * this: this = this + t1.
     * @param t1  the transform to be added to this transform
     */
    public final void add(Transform3D t1) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] += t1.mat[i];
	}

	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    normalize();
	}

    }

    /**
     * Adds transforms t1 and t2 and places the result into this transform.
     * @param t1  the transform to be added
     * @param t2  the transform to be added
     */
    public final void add(Transform3D t1, Transform3D t2) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] = t1.mat[i] + t2.mat[i];
	}

	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    normalize();
	}

    }

    /**
     * Subtracts transform t1 from this transform and places the result
     * into this: this = this - t1.
     * @param t1  the transform to be subtracted from this transform
     */
    public final void sub(Transform3D t1) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] -= t1.mat[i];
	}

	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    normalize();
	}
    }


    /**
     * Subtracts transform t2 from transform t1 and places the result into
     * this: this = t1 - t2.
     * @param t1   the left transform
     * @param t2   the right transform
     */
    public final void sub(Transform3D t1, Transform3D t2) {
	for (int i=0 ; i<16 ; i++) {
	    mat[i] = t1.mat[i] - t2.mat[i];
	}

	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    normalize();
	}

    }


   /**
     * Transposes this matrix in place.
     */
    public final void transpose() {
        double temp;

        temp = mat[4];
        mat[4] = mat[1];
        mat[1] = temp;

        temp = mat[8];
        mat[8] = mat[2];
        mat[2] = temp;

        temp = mat[12];
        mat[12] = mat[3];
        mat[3] = temp;

        temp = mat[9];
        mat[9] = mat[6];
        mat[6] = temp;

        temp = mat[13];
        mat[13] = mat[7];
        mat[7] = temp;

        temp = mat[14];
        mat[14] = mat[11];
        mat[11] = temp;

	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    normalize();
	}
    }

    /**
     * Transposes transform t1 and places the value into this transform.
     * The transform t1 is not modified.
     * @param t1  the transform whose transpose is placed into this transform
     */
    public final void transpose(Transform3D t1) {

       if (this != t1) {
           mat[0] =  t1.mat[0];
           mat[1] =  t1.mat[4];
           mat[2] =  t1.mat[8];
           mat[3] =  t1.mat[12];
           mat[4] =  t1.mat[1];
           mat[5] =  t1.mat[5];
           mat[6] =  t1.mat[9];
           mat[7] =  t1.mat[13];
           mat[8] =  t1.mat[2];
           mat[9] =  t1.mat[6];
           mat[10] = t1.mat[10];
           mat[11] = t1.mat[14];
           mat[12] = t1.mat[3];
           mat[13] = t1.mat[7];
           mat[14] = t1.mat[11];
           mat[15] = t1.mat[15];

	   dirtyBits = ALL_DIRTY;

	   if (autoNormalize) {
	       normalize();
	   }
       } else {
           this.transpose();
       }

    }

   /**
     * Sets the value of this transform to the matrix conversion of the
     * single precision quaternion argument; the non-rotational
     * components are set as if this were an identity matrix.
     * @param q1  the quaternion to be converted
     */
    public final void set(Quat4f q1) {

        mat[0] = (1.0f - 2.0f*q1.y*q1.y - 2.0f*q1.z*q1.z);
        mat[4] = (2.0f*(q1.x*q1.y + q1.w*q1.z));
        mat[8] = (2.0f*(q1.x*q1.z - q1.w*q1.y));

        mat[1] = (2.0f*(q1.x*q1.y - q1.w*q1.z));
        mat[5] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.z*q1.z);
        mat[9] = (2.0f*(q1.y*q1.z + q1.w*q1.x));

        mat[2] = (2.0f*(q1.x*q1.z + q1.w*q1.y));
        mat[6] = (2.0f*(q1.y*q1.z - q1.w*q1.x));
        mat[10] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.y*q1.y);

        mat[3] =  0.0;
        mat[7] =  0.0;
        mat[11] = 0.0;

        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(q1)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	dirtyBits = CLASSIFY_BIT | SCALE_BIT | ROTATION_BIT;
	type = RIGID | CONGRUENT | AFFINE | ORTHO;
    }

   /**
     * Sets the value of this transform to the matrix conversion of the
     * double precision quaternion argument; the non-rotational
     * components are set as if this were an identity matrix.
     * @param q1  the quaternion to be converted
     */
    public final void set(Quat4d q1) {

        mat[0] = (1.0 - 2.0*q1.y*q1.y - 2.0*q1.z*q1.z);
        mat[4] = (2.0*(q1.x*q1.y + q1.w*q1.z));
        mat[8] = (2.0*(q1.x*q1.z - q1.w*q1.y));

        mat[1] = (2.0*(q1.x*q1.y - q1.w*q1.z));
        mat[5] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.z*q1.z);
        mat[9] = (2.0*(q1.y*q1.z + q1.w*q1.x));

        mat[2] = (2.0*(q1.x*q1.z + q1.w*q1.y));
        mat[6] = (2.0*(q1.y*q1.z - q1.w*q1.x));
        mat[10] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.y*q1.y);

        mat[3] =  0.0;
        mat[7] =  0.0;
        mat[11] = 0.0;

        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(q1)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	dirtyBits = CLASSIFY_BIT | SCALE_BIT | ROTATION_BIT;
	type = RIGID | CONGRUENT | AFFINE | ORTHO;
    }

   /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix values in the double precision Matrix3d argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * will be preserved; the argument matrix m1 will be checked for proper
     * normalization when this transform is internally classified.
     * @param m1   the double precision 3x3 matrix
     */
   public final void setRotation(Matrix3d m1) {

       if ((dirtyBits & SCALE_BIT)!= 0) {
	  computeScales(false);
       }

        mat[0] = m1.m00*scales[0];
	mat[1] = m1.m01*scales[1];
	mat[2] = m1.m02*scales[2];
	mat[4] = m1.m10*scales[0];
	mat[5] = m1.m11*scales[1];
	mat[6] = m1.m12*scales[2];
	mat[8] = m1.m20*scales[0];
	mat[9] = m1.m21*scales[1];
	mat[10]= m1.m22*scales[2];

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    // the matrix pass in may not normalize
	    normalize();
	}
   }

   /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix values in the single precision Matrix3f argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * will be preserved; the argument matrix m1 will be checked for proper
     * normalization when this transform is internally classified.
     * @param m1   the single precision 3x3 matrix
     */
     public final void setRotation(Matrix3f m1) {

	 if ((dirtyBits & SCALE_BIT)!= 0) {
	     computeScales(false);
	 }

	 mat[0] = m1.m00*scales[0];
	 mat[1] = m1.m01*scales[1];
	 mat[2] = m1.m02*scales[2];
	 mat[4] = m1.m10*scales[0];
	 mat[5] = m1.m11*scales[1];
	 mat[6] = m1.m12*scales[2];
	 mat[8] = m1.m20*scales[0];
	 mat[9] = m1.m21*scales[1];
	 mat[10]= m1.m22*scales[2];

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    normalize();
	}
     }


    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix equivalent values of the quaternion argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * in the transform is preserved.
     * @param q1    the quaternion that specifies the rotation
    */
    public final void setRotation(Quat4f q1) {

	if ((dirtyBits & SCALE_BIT)!= 0) {
	    computeScales(false);
	}

        mat[0] = (1.0 - 2.0*q1.y*q1.y - 2.0*q1.z*q1.z)*scales[0];
        mat[4] = (2.0*(q1.x*q1.y + q1.w*q1.z))*scales[0];
        mat[8] = (2.0*(q1.x*q1.z - q1.w*q1.y))*scales[0];

        mat[1] = (2.0*(q1.x*q1.y - q1.w*q1.z))*scales[1];
        mat[5] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.z*q1.z)*scales[1];
        mat[9] = (2.0*(q1.y * q1.z + q1.w * q1.x))*scales[1];

        mat[2] = (2.0*(q1.x*q1.z + q1.w*q1.y))*scales[2];
        mat[6] = (2.0*(q1.y*q1.z - q1.w*q1.x))*scales[2];
        mat[10] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.y*q1.y)*scales[2];

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(q1)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

        dirtyBits |= CLASSIFY_BIT | ROTATION_BIT;
	dirtyBits &= ~ORTHO_BIT;
	type |= ORTHO;
	type &= ~(ORTHOGONAL|IDENTITY|SCALE|TRANSLATION|SCALE|ZERO);
      }


    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix equivalent values of the quaternion argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * in the transform is preserved.
     * @param q1    the quaternion that specifies the rotation
     */
     public final void setRotation(Quat4d q1) {

	 if ((dirtyBits & SCALE_BIT)!= 0) {
	     computeScales(false);
	 }

	 mat[0] = (1.0 - 2.0*q1.y*q1.y - 2.0*q1.z*q1.z)*scales[0];
	 mat[4] = (2.0*(q1.x*q1.y + q1.w*q1.z))*scales[0];
	 mat[8] = (2.0*(q1.x*q1.z - q1.w*q1.y))*scales[0];

	 mat[1] = (2.0*(q1.x*q1.y - q1.w*q1.z))*scales[1];
	 mat[5] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.z*q1.z)*scales[1];
	 mat[9] = (2.0*(q1.y * q1.z + q1.w * q1.x))*scales[1];

	 mat[2] = (2.0*(q1.x*q1.z + q1.w*q1.y))*scales[2];
	 mat[6] = (2.0*(q1.y*q1.z - q1.w*q1.x))*scales[2];
	 mat[10] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.y*q1.y)*scales[2];

         // Issue 253: set all dirty bits if input is infinity or NaN
         if (isInfOrNaN(q1)) {
             dirtyBits = ALL_DIRTY;
             return;
         }

         dirtyBits |= CLASSIFY_BIT | ROTATION_BIT;
         dirtyBits &= ~ORTHO_BIT;
         type |= ORTHO;
         type &= ~(ORTHOGONAL|IDENTITY|SCALE|TRANSLATION|SCALE|ZERO);
      }


    /**
     * Sets the value of this transform to the matrix conversion
     * of the single precision axis-angle argument; all of the matrix
     * values are modified.
     * @param a1 the axis-angle to be converted (x, y, z, angle)
     */
    public final void set(AxisAngle4f a1) {

	double mag = Math.sqrt( a1.x*a1.x + a1.y*a1.y + a1.z*a1.z);

	if (almostZero(mag)) {
	    setIdentity();
	} else {
	    mag = 1.0/mag;
	    double ax = a1.x*mag;
	    double ay = a1.y*mag;
	    double az = a1.z*mag;

	    double sinTheta = Math.sin((double)a1.angle);
	    double cosTheta = Math.cos((double)a1.angle);
	    double t = 1.0 - cosTheta;

	    double xz = ax * az;
	    double xy = ax * ay;
	    double yz = ay * az;

	    mat[0] = t * ax * ax + cosTheta;
	    mat[1] = t * xy - sinTheta * az;
	    mat[2] = t * xz + sinTheta * ay;
	    mat[3] = 0.0;

	    mat[4] = t * xy + sinTheta * az;
	    mat[5] = t * ay * ay + cosTheta;
	    mat[6] = t * yz - sinTheta * ax;
	    mat[7] = 0.0;

	    mat[8] = t * xz - sinTheta * ay;
	    mat[9] = t * yz + sinTheta * ax;
	    mat[10] = t * az * az + cosTheta;
	    mat[11] = 0.0;

	    mat[12] = 0.0;
	    mat[13] = 0.0;
	    mat[14] = 0.0;
	    mat[15] = 1.0;

            // Issue 253: set all dirty bits if input is infinity or NaN
            if (isInfOrNaN(a1)) {
                dirtyBits = ALL_DIRTY;
                return;
            }

            type = CONGRUENT | AFFINE | RIGID | ORTHO;
	    dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
	}
    }


    /**
     * Sets the value of this transform to the matrix conversion
     * of the double precision axis-angle argument; all of the matrix
     * values are modified.
     * @param a1 the axis-angle to be converted (x, y, z, angle)
     */
    public final void set(AxisAngle4d a1) {

	double mag = Math.sqrt( a1.x*a1.x + a1.y*a1.y + a1.z*a1.z);

	if (almostZero(mag)) {
	    setIdentity();
	} else {
	    mag = 1.0/mag;
	    double ax = a1.x*mag;
	    double ay = a1.y*mag;
	    double az = a1.z*mag;

	    double sinTheta = Math.sin(a1.angle);
	    double cosTheta = Math.cos(a1.angle);
	    double t = 1.0 - cosTheta;

	    double xz = ax * az;
	    double xy = ax * ay;
	    double yz = ay * az;

	    mat[0] = t * ax * ax + cosTheta;
	    mat[1] = t * xy - sinTheta * az;
	    mat[2] = t * xz + sinTheta * ay;
	    mat[3] = 0.0;

	    mat[4] = t * xy + sinTheta * az;
	    mat[5] = t * ay * ay + cosTheta;
	    mat[6] = t * yz - sinTheta * ax;
	    mat[7] = 0.0;

	    mat[8] = t * xz - sinTheta * ay;
	    mat[9] = t * yz + sinTheta * ax;
	    mat[10] = t * az * az + cosTheta;
	    mat[11] = 0.0;

	    mat[12] = 0.0;
	    mat[13] = 0.0;
	    mat[14] = 0.0;
	    mat[15] = 1.0;

            // Issue 253: set all dirty bits if input is infinity or NaN
            if (isInfOrNaN(a1)) {
                dirtyBits = ALL_DIRTY;
                return;
            }

	    type = CONGRUENT | AFFINE | RIGID | ORTHO;
	    dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
	}
    }


   /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix equivalent values of the axis-angle argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * in the transform is preserved.
     * @param a1 the axis-angle to be converted (x, y, z, angle)
     */
    public final void setRotation(AxisAngle4d a1) {

	if ((dirtyBits & SCALE_BIT)!= 0) {
	    computeScales(false);
	}

	double mag = Math.sqrt( a1.x*a1.x + a1.y*a1.y + a1.z*a1.z);

	if (almostZero(mag)) {
	    mat[0] = scales[0];
	    mat[1] = 0.0;
	    mat[2] = 0.0;
	    mat[4] = 0.0;
	    mat[5] = scales[1];
	    mat[6] = 0.0;
	    mat[8] = 0.0;
	    mat[9] = 0.0;
	    mat[10] = scales[2];
	} else {
	    mag = 1.0/mag;
	    double ax = a1.x*mag;
	    double ay = a1.y*mag;
	    double az = a1.z*mag;

	    double sinTheta = Math.sin(a1.angle);
	    double cosTheta = Math.cos(a1.angle);
	    double t = 1.0 - cosTheta;

	    double xz = ax * az;
	    double xy = ax * ay;
	    double yz = ay * az;

	    mat[0] = (t * ax * ax + cosTheta)*scales[0];
	    mat[1] = (t * xy - sinTheta * az)*scales[1];
	    mat[2] = (t * xz + sinTheta * ay)*scales[2];

	    mat[4] = (t * xy + sinTheta * az)*scales[0];
	    mat[5] = (t * ay * ay + cosTheta)*scales[1];
	    mat[6] = (t * yz - sinTheta * ax)*scales[2];

	    mat[8] = (t * xz - sinTheta * ay)*scales[0];
	    mat[9] = (t * yz + sinTheta * ax)*scales[1];
	    mat[10] = (t * az * az + cosTheta)*scales[2];
	}

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(a1)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	// Rigid remain rigid, congruent remain congruent after
	// set rotation
	dirtyBits |= CLASSIFY_BIT | ROTATION_BIT;
	dirtyBits &= ~ORTHO_BIT;
	type |= ORTHO;
	type &= ~(ORTHOGONAL|IDENTITY|SCALE|TRANSLATION|SCALE|ZERO);
    }


   /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix equivalent values of the axis-angle argument; the other
     * elements of this transform are unchanged; any pre-existing scale
     * in the transform is preserved.
     * @param a1 the axis-angle to be converted (x, y, z, angle)
     */
    public final void setRotation(AxisAngle4f a1)  {

	if ((dirtyBits & SCALE_BIT)!= 0) {
	    computeScales(false);
	}

	double mag = Math.sqrt( a1.x*a1.x + a1.y*a1.y + a1.z*a1.z);

	if (almostZero(mag)) {
	    mat[0] = scales[0];
	    mat[1] = 0.0;
	    mat[2] = 0.0;
	    mat[4] = 0.0;
	    mat[5] = scales[1];
	    mat[6] = 0.0;
	    mat[8] = 0.0;
	    mat[9] = 0.0;
	    mat[10] = scales[2];
	} else {
	    mag = 1.0/mag;
	    double ax = a1.x*mag;
	    double ay = a1.y*mag;
	    double az = a1.z*mag;

	    double sinTheta = Math.sin(a1.angle);
	    double cosTheta = Math.cos(a1.angle);
	    double t = 1.0 - cosTheta;

	    double xz = ax * az;
	    double xy = ax * ay;
	    double yz = ay * az;

	    mat[0] = (t * ax * ax + cosTheta)*scales[0];
	    mat[1] = (t * xy - sinTheta * az)*scales[1];
	    mat[2] = (t * xz + sinTheta * ay)*scales[2];

	    mat[4] = (t * xy + sinTheta * az)*scales[0];
	    mat[5] = (t * ay * ay + cosTheta)*scales[1];
	    mat[6] = (t * yz - sinTheta * ax)*scales[2];

	    mat[8] = (t * xz - sinTheta * ay)*scales[0];
	    mat[9] = (t * yz + sinTheta * ax)*scales[1];
	    mat[10] = (t * az * az + cosTheta)*scales[2];
	}

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(a1)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	// Rigid remain rigid, congruent remain congruent after
	// set rotation
	dirtyBits |= CLASSIFY_BIT | ROTATION_BIT;
	dirtyBits &= (~ORTHO_BIT | ~SVD_BIT);
	type |= ORTHO;
	type &= ~(ORTHOGONAL|IDENTITY|SCALE|TRANSLATION|SCALE|ZERO);
    }


    /**
     * Sets the value of this transform to a counter clockwise rotation
     * about the x axis. All of the non-rotational components are set as
     * if this were an identity matrix.
     * @param angle the angle to rotate about the X axis in radians
     */
    public void rotX(double angle) {
        double sinAngle = Math.sin(angle);
        double cosAngle = Math.cos(angle);

        mat[0] = 1.0;
        mat[1] = 0.0;
        mat[2] = 0.0;
        mat[3] = 0.0;

        mat[4] = 0.0;
        mat[5] = cosAngle;
        mat[6] = -sinAngle;
        mat[7] = 0.0;

        mat[8] = 0.0;
        mat[9] = sinAngle;
        mat[10] = cosAngle;
        mat[11] = 0.0;

        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(angle)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	type = CONGRUENT | AFFINE | RIGID | ORTHO;
	dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
    }

    /**
     * Sets the value of this transform to a counter clockwise rotation about
     * the y axis. All of the non-rotational components are set as if this
     * were an identity matrix.
     * @param angle the angle to rotate about the Y axis in radians
     */
    public void rotY(double angle) {
        double sinAngle = Math.sin(angle);
        double cosAngle = Math.cos(angle);

        mat[0] = cosAngle;
        mat[1] = 0.0;
        mat[2] = sinAngle;
        mat[3] = 0.0;

        mat[4] = 0.0;
        mat[5] = 1.0;
        mat[6] = 0.0;
        mat[7] = 0.0;

        mat[8] = -sinAngle;
        mat[9] = 0.0;
        mat[10] = cosAngle;
        mat[11] = 0.0;

        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(angle)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	type = CONGRUENT | AFFINE | RIGID | ORTHO;
	dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
    }


    /**
     * Sets the value of this transform to a counter clockwise rotation
     * about the z axis.  All of the non-rotational components are set
     * as if this were an identity matrix.
     * @param angle the angle to rotate about the Z axis in radians
     */
    public void rotZ(double angle)  {
        double sinAngle = Math.sin(angle);
        double cosAngle = Math.cos(angle);

        mat[0] = cosAngle;
        mat[1] = -sinAngle;
        mat[2] = 0.0;
        mat[3] = 0.0;

        mat[4] = sinAngle;
        mat[5] = cosAngle;
        mat[6] = 0.0;
        mat[7] = 0.0;

        mat[8] = 0.0;
        mat[9] = 0.0;
        mat[10] = 1.0;
        mat[11] = 0.0;

        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(angle)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	type = CONGRUENT | AFFINE | RIGID | ORTHO;
	dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
    }


   /**
     * Sets the translational value of this matrix to the Vector3f parameter
     * values, and sets the other components of the matrix as if this
     * transform were an identity matrix.
     * @param trans  the translational component
     */
    public final void set(Vector3f trans) {
       mat[0] = 1.0; mat[1] = 0.0; mat[2] = 0.0; mat[3] = trans.x;
       mat[4] = 0.0; mat[5] = 1.0; mat[6] = 0.0; mat[7] = trans.y;
       mat[8] = 0.0; mat[9] = 0.0; mat[10] = 1.0; mat[11] = trans.z;
       mat[12] = 0.0; mat[13] = 0.0; mat[14] = 0.0; mat[15] = 1.0;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(trans)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

       type = CONGRUENT | AFFINE | RIGID | ORTHO;
       dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
    }

    /**
     * Sets the translational value of this matrix to the Vector3d paramter
     * values, and sets the other components of the matrix as if this
     * transform were an identity matrix.
     * @param trans  the translational component
     */
    public final void set(Vector3d trans) {
       mat[0] = 1.0; mat[1] = 0.0; mat[2] = 0.0; mat[3] = trans.x;
       mat[4] = 0.0; mat[5] = 1.0; mat[6] = 0.0; mat[7] = trans.y;
       mat[8] = 0.0; mat[9] = 0.0; mat[10] = 1.0; mat[11] = trans.z;
       mat[12] = 0.0; mat[13] = 0.0; mat[14] = 0.0; mat[15] = 1.0;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(trans)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

       type = CONGRUENT | AFFINE | RIGID | ORTHO;
       dirtyBits = CLASSIFY_BIT | ROTATION_BIT | SCALE_BIT;
    }

    /**
     * Sets the scale component of the current transform; any existing
     * scale is first factored out of the existing transform before
     * the new scale is applied.
     * @param scale  the new scale amount
     */
    public final void setScale(double scale) {
	if ((dirtyBits & ROTATION_BIT)!= 0) {
	    computeScaleRotation(false);
	}

	scales[0] = scales[1] = scales[2] = scale;
	mat[0] = rot[0]*scale;
	mat[1] = rot[1]*scale;
	mat[2] = rot[2]*scale;
	mat[4] = rot[3]*scale;
	mat[5] = rot[4]*scale;
	mat[6] = rot[5]*scale;
	mat[8] = rot[6]*scale;
	mat[9] = rot[7]*scale;
	mat[10] = rot[8]*scale;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(scale)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	dirtyBits |= (CLASSIFY_BIT | RIGID_BIT | CONGRUENT_BIT | SVD_BIT);
	dirtyBits &= ~SCALE_BIT;
    }


    /**
     * Sets the possibly non-uniform scale component of the current
     * transform; any existing scale is first factored out of the
     * existing transform before the new scale is applied.
     * @param scale  the new x,y,z scale values
     */
     public final void setScale(Vector3d scale) {

	if ((dirtyBits & ROTATION_BIT)!= 0) {
	    computeScaleRotation(false);
	}

	scales[0] = scale.x;
	scales[1] = scale.y;
	scales[2] = scale.z;

	mat[0] = rot[0]*scale.x;
	mat[1] = rot[1]*scale.y;
	mat[2] = rot[2]*scale.z;
	mat[4] = rot[3]*scale.x;
	mat[5] = rot[4]*scale.y;
	mat[6] = rot[5]*scale.z;
	mat[8] = rot[6]*scale.x;
	mat[9] = rot[7]*scale.y;
	mat[10] = rot[8]*scale.z;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(scale)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	dirtyBits |= (CLASSIFY_BIT | RIGID_BIT | CONGRUENT_BIT | SVD_BIT);
	dirtyBits &= ~SCALE_BIT;
    }


    /**
     * Replaces the current transform with a non-uniform scale transform.
     * All values of the existing transform are replaced.
     * @param xScale the new X scale amount
     * @param yScale the new Y scale amount
     * @param zScale the new Z scale amount
     * @deprecated Use setScale(Vector3d) instead of setNonUniformScale;
     * note that the setScale only modifies the scale component
     */
    public final void setNonUniformScale(double xScale,
					 double yScale,
					 double zScale) {
	if(scales == null)
	    scales = new double[3];

	scales[0] = xScale;
	scales[1] = yScale;
	scales[2] = zScale;
	mat[0] = xScale;
	mat[1] = 0.0;
	mat[2] = 0.0;
	mat[3] = 0.0;
	mat[4] = 0.0;
	mat[5] = yScale;
	mat[6] = 0.0;
	mat[7] = 0.0;
	mat[8] = 0.0;
	mat[9] = 0.0;
	mat[10] = zScale;
	mat[11] = 0.0;
	mat[12] = 0.0;
	mat[13] = 0.0;
	mat[14] = 0.0;
	mat[15] = 1.0;

        // Issue 253: set all dirty bits
        dirtyBits = ALL_DIRTY;
    }

    /**
     * Replaces the translational components of this transform to the values
     * in the Vector3f argument; the other values of this transform are not
     * modified.
     * @param trans  the translational component
     */
    public final void setTranslation(Vector3f trans) {
       mat[3] = trans.x;
       mat[7] = trans.y;
       mat[11] = trans.z;

       // Issue 253: set all dirty bits if input is infinity or NaN
       if (isInfOrNaN(trans)) {
           dirtyBits = ALL_DIRTY;
           return;
       }

       // Only preserve CONGRUENT, RIGID, ORTHO
       type &= ~(ORTHOGONAL|IDENTITY|SCALE|TRANSLATION|SCALE|ZERO);
       dirtyBits |= CLASSIFY_BIT;
    }


    /**
     * Replaces the translational components of this transform to the values
     * in the Vector3d argument; the other values of this transform are not
     * modified.
     * @param trans  the translational component
     */
    public final void setTranslation(Vector3d trans) {
       mat[3] = trans.x;
       mat[7] = trans.y;
       mat[11] = trans.z;

       // Issue 253: set all dirty bits if input is infinity or NaN
       if (isInfOrNaN(trans)) {
           dirtyBits = ALL_DIRTY;
           return;
       }

       type &= ~(ORTHOGONAL|IDENTITY|SCALE|TRANSLATION|SCALE|ZERO);
       dirtyBits |= CLASSIFY_BIT;
    }


    /**
     * Sets the value of this matrix from the rotation expressed
     * by the quaternion q1, the translation t1, and the scale s.
     * @param q1 the rotation expressed as a quaternion
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Quat4d q1, Vector3d t1, double s) {
	if(scales == null)
	    scales = new double[3];

	scales[0] = scales[1] = scales[2] = s;

        mat[0] = (1.0 - 2.0*q1.y*q1.y - 2.0*q1.z*q1.z)*s;
        mat[4] = (2.0*(q1.x*q1.y + q1.w*q1.z))*s;
        mat[8] = (2.0*(q1.x*q1.z - q1.w*q1.y))*s;

        mat[1] = (2.0*(q1.x*q1.y - q1.w*q1.z))*s;
        mat[5] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.z*q1.z)*s;
        mat[9] = (2.0*(q1.y*q1.z + q1.w*q1.x))*s;

        mat[2] = (2.0*(q1.x*q1.z + q1.w*q1.y))*s;
        mat[6] = (2.0*(q1.y*q1.z - q1.w*q1.x))*s;
        mat[10] = (1.0 - 2.0*q1.x*q1.x - 2.0*q1.y*q1.y)*s;

        mat[3] = t1.x;
        mat[7] = t1.y;
        mat[11] = t1.z;
        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;
    }

    /**
     * Sets the value of this matrix from the rotation expressed
     * by the quaternion q1, the translation t1, and the scale s.
     * @param q1 the rotation expressed as a quaternion
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Quat4f q1, Vector3d t1, double s) {
	if(scales == null)
	    scales = new double[3];

	scales[0] = scales[1] = scales[2] = s;

        mat[0] = (1.0f - 2.0f*q1.y*q1.y - 2.0f*q1.z*q1.z)*s;
        mat[4] = (2.0f*(q1.x*q1.y + q1.w*q1.z))*s;
        mat[8] = (2.0f*(q1.x*q1.z - q1.w*q1.y))*s;

        mat[1] = (2.0f*(q1.x*q1.y - q1.w*q1.z))*s;
        mat[5] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.z*q1.z)*s;
        mat[9] = (2.0f*(q1.y*q1.z + q1.w*q1.x))*s;

        mat[2] = (2.0f*(q1.x*q1.z + q1.w*q1.y))*s;
        mat[6] = (2.0f*(q1.y*q1.z - q1.w*q1.x))*s;
        mat[10] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.y*q1.y)*s;

        mat[3] = t1.x;
        mat[7] = t1.y;
        mat[11] = t1.z;
        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;
    }

    /**
     * Sets the value of this matrix from the rotation expressed
     * by the quaternion q1, the translation t1, and the scale s.
     * @param q1 the rotation expressed as a quaternion
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Quat4f q1, Vector3f t1, float s) {
	if(scales == null)
	    scales = new double[3];

	scales[0] = scales[1] = scales[2] = s;

        mat[0] = (1.0f - 2.0f*q1.y*q1.y - 2.0f*q1.z*q1.z)*s;
        mat[4] = (2.0f*(q1.x*q1.y + q1.w*q1.z))*s;
        mat[8] = (2.0f*(q1.x*q1.z - q1.w*q1.y))*s;

        mat[1] = (2.0f*(q1.x*q1.y - q1.w*q1.z))*s;
        mat[5] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.z*q1.z)*s;
        mat[9] = (2.0f*(q1.y*q1.z + q1.w*q1.x))*s;

        mat[2] = (2.0f*(q1.x*q1.z + q1.w*q1.y))*s;
        mat[6] = (2.0f*(q1.y*q1.z - q1.w*q1.x))*s;
        mat[10] = (1.0f - 2.0f*q1.x*q1.x - 2.0f*q1.y*q1.y)*s;

        mat[3] = t1.x;
        mat[7] = t1.y;
        mat[11] = t1.z;
        mat[12] = 0.0;
        mat[13] = 0.0;
        mat[14] = 0.0;
        mat[15] = 1.0;

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;
    }

    /**
     * Sets the value of this matrix from the rotation expressed
     * by the rotation matrix m1, the translation t1, and the scale s.
     * The scale is only applied to the
     * rotational component of the matrix (upper 3x3) and not to the
     * translational component of the matrix.
     * @param m1 the rotation matrix
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Matrix3f m1, Vector3f t1, float s) {
	mat[0]=m1.m00*s;
	mat[1]=m1.m01*s;
	mat[2]=m1.m02*s;
	mat[3]=t1.x;
	mat[4]=m1.m10*s;
	mat[5]=m1.m11*s;
	mat[6]=m1.m12*s;
	mat[7]=t1.y;
	mat[8]=m1.m20*s;
	mat[9]=m1.m21*s;
	mat[10]=m1.m22*s;
	mat[11]=t1.z;
	mat[12]=0.0;
	mat[13]=0.0;
	mat[14]=0.0;
	mat[15]=1.0;

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;

	if (autoNormalize) {
	    // input matrix may not normalize
	    normalize();
	}
    }

    /**
     * Sets the value of this matrix from the rotation expressed
     * by the rotation matrix m1, the translation t1, and the scale s.
     * The scale is only applied to the
     * rotational component of the matrix (upper 3x3) and not to the
     * translational component of the matrix.
     * @param m1 the rotation matrix
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Matrix3f m1, Vector3d t1, double s) {
	mat[0]=m1.m00*s;
	mat[1]=m1.m01*s;
	mat[2]=m1.m02*s;
	mat[3]=t1.x;
	mat[4]=m1.m10*s;
	mat[5]=m1.m11*s;
	mat[6]=m1.m12*s;
	mat[7]=t1.y;
	mat[8]=m1.m20*s;
	mat[9]=m1.m21*s;
	mat[10]=m1.m22*s;
	mat[11]=t1.z;
	mat[12]=0.0;
	mat[13]=0.0;
	mat[14]=0.0;
	mat[15]=1.0;

        // Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}
    }

    /**
     * Sets the value of this matrix from the rotation expressed
     * by the rotation matrix m1, the translation t1, and the scale s.
     * The scale is only applied to the
     * rotational component of the matrix (upper 3x3) and not to the
     * translational component of the matrix.
     * @param m1 the rotation matrix
     * @param t1 the translation
     * @param s the scale value
     */
    public final void set(Matrix3d m1, Vector3d t1, double s) {
	mat[0]=m1.m00*s;
	mat[1]=m1.m01*s;
	mat[2]=m1.m02*s;
	mat[3]=t1.x;
	mat[4]=m1.m10*s;
	mat[5]=m1.m11*s;
	mat[6]=m1.m12*s;
	mat[7]=t1.y;
	mat[8]=m1.m20*s;
	mat[9]=m1.m21*s;
	mat[10]=m1.m22*s;
	mat[11]=t1.z;
	mat[12]=0.0;
	mat[13]=0.0;
	mat[14]=0.0;
	mat[15]=1.0;

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}
    }

    /**
     * Sets the matrix values of this transform to the matrix values in the
     * upper 4x4 corner of the GMatrix parameter.  If the parameter matrix is
     * smaller than 4x4, the remaining elements in the transform matrix are
     * assigned to zero.  The transform matrix type is classified
     * internally by the Transform3D class.
     * @param matrix  the general matrix from which the Transform3D matrix is derived
     */
    public final void set(GMatrix matrix) {
	int i,j, k;
	int numRows = matrix.getNumRow();
	int numCol = matrix.getNumCol();

	for(i=0 ; i<4 ; i++) {
	    k = i*4;
	    for(j=0 ; j<4 ; j++) {
		if(i>=numRows || j>=numCol)
		    mat[k+j] = 0.0;
		else
		    mat[k+j] = matrix.getElement(i,j);
	    }
	}

	dirtyBits = ALL_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}

    }

    /**
     * Sets the matrix, type, and state of this transform to the matrix,
     * type, and state of transform t1.
     * @param t1  the transform to be copied
     */
    public final void set(Transform3D t1){
	mat[0] = t1.mat[0];
	mat[1] = t1.mat[1];
	mat[2] = t1.mat[2];
	mat[3] = t1.mat[3];
	mat[4] = t1.mat[4];
	mat[5] = t1.mat[5];
	mat[6] = t1.mat[6];
	mat[7] = t1.mat[7];
	mat[8] = t1.mat[8];
	mat[9] = t1.mat[9];
	mat[10] = t1.mat[10];
	mat[11] = t1.mat[11];
	mat[12] = t1.mat[12];
	mat[13] = t1.mat[13];
	mat[14] = t1.mat[14];
	mat[15] = t1.mat[15];
	type = t1.type;

	// don't copy rot[] and scales[]
	dirtyBits = t1.dirtyBits | ROTATION_BIT | SCALE_BIT;
        autoNormalize = t1.autoNormalize;
    }

    // This version gets a lock before doing the set.  It is used internally
    synchronized void setWithLock(Transform3D t1) {
	this.set(t1);
    }

    // This version gets a lock before doing the get.  It is used internally
    synchronized void getWithLock(Transform3D t1) {
	t1.set(this);
    }

    /**
     * Sets the matrix values of this transform to the matrix values in the
     * double precision array parameter.  The matrix type is classified
     * internally by the Transform3D class.
     * @param matrix  the double precision array of length 16 in row major format
     */
    public final void set(double[] matrix) {
	mat[0] = matrix[0];
	mat[1] = matrix[1];
	mat[2] = matrix[2];
	mat[3] = matrix[3];
	mat[4] = matrix[4];
	mat[5] = matrix[5];
	mat[6] = matrix[6];
	mat[7] = matrix[7];
	mat[8] = matrix[8];
	mat[9] = matrix[9];
	mat[10] = matrix[10];
	mat[11] = matrix[11];
	mat[12] = matrix[12];
	mat[13] = matrix[13];
	mat[14] = matrix[14];
	mat[15] = matrix[15];

	dirtyBits = ALL_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}

    }

   /**
     * Sets the matrix values of this transform to the matrix values in the
     * single precision array parameter.  The matrix type is classified
     * internally by the Transform3D class.
     * @param matrix  the single precision array of length 16 in row major format
     */
    public final void set(float[] matrix) {
	mat[0] = matrix[0];
	mat[1] = matrix[1];
	mat[2] = matrix[2];
	mat[3] = matrix[3];
	mat[4] = matrix[4];
	mat[5] = matrix[5];
	mat[6] = matrix[6];
	mat[7] = matrix[7];
	mat[8] = matrix[8];
	mat[9] = matrix[9];
	mat[10] = matrix[10];
	mat[11] = matrix[11];
	mat[12] = matrix[12];
	mat[13] = matrix[13];
	mat[14] = matrix[14];
	mat[15] = matrix[15];

	dirtyBits = ALL_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}

    }

    /**
     * Sets the matrix values of this transform to the matrix values in the
     * double precision Matrix4d argument.  The transform type is classified
     * internally by the Transform3D class.
     * @param m1   the double precision 4x4 matrix
     */
    public final void set(Matrix4d m1) {
	mat[0] = m1.m00;
	mat[1] = m1.m01;
	mat[2] = m1.m02;
	mat[3] = m1.m03;
	mat[4] = m1.m10;
	mat[5] = m1.m11;
	mat[6] = m1.m12;
	mat[7] = m1.m13;
	mat[8] = m1.m20;
	mat[9] = m1.m21;
	mat[10] = m1.m22;
	mat[11] = m1.m23;
	mat[12] = m1.m30;
	mat[13] = m1.m31;
	mat[14] = m1.m32;
	mat[15] = m1.m33;

	dirtyBits = ALL_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}
    }


    /**
     * Sets the matrix values of this transform to the matrix values in the
     * single precision Matrix4f argument.  The transform type is classified
     * internally by the Transform3D class.
     * @param m1   the single precision 4x4 matrix
     */
    public final void set(Matrix4f m1) {
	mat[0] = m1.m00;
	mat[1] = m1.m01;
	mat[2] = m1.m02;
	mat[3] = m1.m03;
	mat[4] = m1.m10;
	mat[5] = m1.m11;
	mat[6] = m1.m12;
	mat[7] = m1.m13;
	mat[8] = m1.m20;
	mat[9] = m1.m21;
	mat[10] = m1.m22;
	mat[11] = m1.m23;
	mat[12] = m1.m30;
	mat[13] = m1.m31;
	mat[14] = m1.m32;
	mat[15] = m1.m33;
        
	dirtyBits = ALL_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}
    }


    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix values in the single precision Matrix3f argument; the other
     * elements of this transform are initialized as if this were an identity
     * matrix (i.e., affine matrix with no translational component).
     * @param m1   the single precision 3x3 matrix
     */
    public final void set(Matrix3f m1) {
	mat[0] = m1.m00;
	mat[1] = m1.m01;
	mat[2] = m1.m02;
	mat[3] = 0.0;
	mat[4] = m1.m10;
	mat[5] = m1.m11;
	mat[6] = m1.m12;
	mat[7] = 0.0;
	mat[8] = m1.m20;
	mat[9] = m1.m21;
	mat[10] = m1.m22;
	mat[11] = 0.0;
	mat[12] = 0.0;
	mat[13] = 0.0;
	mat[14] = 0.0;
	mat[15] = 1.0;

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}
    }


    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * matrix values in the double precision Matrix3d argument; the other
     * elements of this transform are initialized as if this were an identity
     * matrix (ie, affine matrix with no translational component).
     * @param m1   the double precision 3x3 matrix
     */
    public final void set(Matrix3d m1) {
	mat[0] = m1.m00;
	mat[1] = m1.m01;
	mat[2] = m1.m02;
	mat[3] = 0.0;
	mat[4] = m1.m10;
	mat[5] = m1.m11;
	mat[6] = m1.m12;
	mat[7] = 0.0;
	mat[8] = m1.m20;
	mat[9] = m1.m21;
	mat[10] = m1.m22;
	mat[11] = 0.0;
	mat[12] = 0.0;
	mat[13] = 0.0;
	mat[14] = 0.0;
	mat[15] = 1.0;

	// Issue 253: set all dirty bits
	dirtyBits = ALL_DIRTY;

	if (autoNormalize)  {
	    normalize();
	}

    }


    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * rotation matrix converted from the Euler angles provided; the other
     * non-rotational elements are set as if this were an identity matrix.
     * The euler parameter is a Vector3d consisting of three rotation angles
     * applied first about the X, then Y then Z axis.
     * These rotations are applied using a static frame of reference. In
     * other words, the orientation of the Y rotation axis is not affected
     * by the X rotation and the orientation of the Z rotation axis is not
     * affected by the X or Y rotation.
     * @param euler  the Vector3d consisting of three rotation angles about X,Y,Z
     *
     */
    public final void setEuler(Vector3d euler) {
	double sina, sinb, sinc;
	double cosa, cosb, cosc;

	sina = Math.sin(euler.x);
	sinb = Math.sin(euler.y);
	sinc = Math.sin(euler.z);
	cosa = Math.cos(euler.x);
	cosb = Math.cos(euler.y);
	cosc = Math.cos(euler.z);

	mat[0] = cosb * cosc;
	mat[1] = -(cosa * sinc) + (sina * sinb * cosc);
	mat[2] = (sina * sinc) + (cosa * sinb *cosc);
	mat[3] = 0.0;

	mat[4] = cosb * sinc;
	mat[5] = (cosa * cosc) + (sina * sinb * sinc);
	mat[6] = -(sina * cosc) + (cosa * sinb *sinc);
	mat[7] = 0.0;

	mat[8] = -sinb;
	mat[9] = sina * cosb;
	mat[10] = cosa * cosb;
	mat[11] = 0.0;

	mat[12] = 0.0;
	mat[13] = 0.0;
	mat[14] = 0.0;
	mat[15] = 1.0;

        // Issue 253: set all dirty bits if input is infinity or NaN
        if (isInfOrNaN(euler)) {
            dirtyBits = ALL_DIRTY;
            return;
        }

	type = AFFINE | CONGRUENT | RIGID | ORTHO;
	dirtyBits = CLASSIFY_BIT | SCALE_BIT | ROTATION_BIT;
    }


    /**
     * Places the values of this transform into the double precision array
     * of length 16.  The first four elements of the array wil
