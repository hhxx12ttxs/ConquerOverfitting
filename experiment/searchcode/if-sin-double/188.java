/*
 * Copyright 2010 Brendan Kenny
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gwt.ns.transforms.client;

/**
 * 4x4 matrix for representation of affine transformations in three dimensions
 * (using homogeneous coordinates). Matrix is "column-major," meaning that, in
 * the multiplication of a series of transforms, the first applied matrix is
 * on the far right. The translation values are found in column four.
 * 
 * <p>All methods have been optimized, but full multiplies and inversions still
 * incur relatively heavy performance costs. If possible, tend toward the
 * problem specific. For example, use <code>matrix.rotateX(angle)</code>
 * instead of <code>matrix.multiply(rotationMatrix)</code>, or note that the
 * transpose of an orthonormal matrix is also its inverse, so does not require
 * a full inverse calculation.<p>
 */
public class Matrix4x4 {
	private static boolean clinit_guard;
	
	/**
	 * "Scratch" matrix needed for multiplication temporary. Declared static
	 * since we are single threaded.
	 */
	private static Matrix4x4 multm;
	
	/**
	 * Internal representation of a matrix entry.
	 * First number is row number, second is column number.
	 */
	public double m11, m21, m31, m41, m12, m22, m32, m42, m13, m23, m33,
					m43, m14, m24, m34, m44;
	
	/**
	 * Construct a new 4x4 matrix set to identity
	 */
	public Matrix4x4() {
		// hackery to have static temp matrix but avoid clinits
		if (!clinit_guard) {
			clinit_guard = true;
			multm = new Matrix4x4();
		}
		
		setToIdentity();
	}
	
	/**
	 * Sets this matrix to the specified transform.
	 * 
	 * @param src The transform to be copied.
	 */
	public void copy(Matrix4x4 src) {
		m11 = src.m11; m12 = src.m12; m13 = src.m13; m14 = src.m14;
		m21 = src.m21; m22 = src.m22; m23 = src.m23; m24 = src.m24;
		m31 = src.m31; m32 = src.m32; m33 = src.m33; m34 = src.m34;
		m41 = src.m41; m42 = src.m42; m43 = src.m43; m44 = src.m44;
	}
	
	/*
	 * Calculate the determinant.
	 * 
	 * @return the determinant of this matrix
	 */
	//public double determinant() {
	//	// TODO: see: inverse calc
	//}
	
	/**
	 * Calculates the inverse of this matrix, if it exists, and stores it in
	 * dest. This is an intensive calculation and should be avoided if
	 * possible. If this matrix is orthonormal, try
	 * {@link #inverseOrthonormalAffine(Matrix4x4)}.
	 * 
	 * <p>Implementation from David Eberly's "The Laplace Expansion Theorem:
	 * Computing the Determinants and Inverses of Matrices".</p>
	 * 
	 * @param dest The destination of the inverse.
	 * 
	 * @see <a href="http://www.geometrictools.com/Documentation/LaplaceExpansionTheorem.pdf">"The Laplace Expansion Theorem: Computing the Determinants and Inverses of Matrices"</a> 
	 */
	public void inverse(Matrix4x4 dest) {
		// I count 95 multiplies, vs 64 for even a full multiply
		// or 201 for Richard Carling's reference matrix inverse implementation
		
		double s0 = m11*m22 - m21*m12;
		double s1 = m11*m23 - m21*m13;
		double s2 = m11*m24 - m21*m14;
		double s3 = m12*m23 - m22*m13;
		double s4 = m12*m24 - m22*m14;
		double s5 = m13*m24 - m23*m14;
		
		double c5 = m33*m44 - m43*m34;
		double c4 = m32*m44 - m42*m34;
		double c3 = m32*m43 - m42*m33;
		double c2 = m31*m44 - m41*m34;
		double c1 = m31*m43 - m41*m33;
		double c0 = m31*m42 - m41*m32;
		
		// adjugate
		dest.m11 =  m22*c5 - m23*c4 + m24*c3;
		dest.m12 = -m12*c5 + m13*c4 - m14*c3;
		dest.m13 =  m42*s5 - m43*s4 + m44*s3;
		dest.m14 = -m32*s5 + m33*s4 - m34*s3;
		
		dest.m21 = -m21*c5 + m23*c2 - m24*c1;
		dest.m22 =  m11*c5 - m13*c2 + m14*c1;
		dest.m23 = -m41*s5 + m43*s2 - m44*s1;
		dest.m24 =  m31*s5 - m33*s2 + m34*s1;
		
		dest.m31 =  m21*c4 - m22*c2 + m24*c0;
		dest.m32 = -m11*c4 + m12*c2 - m14*c0;
		dest.m33 =  m41*s4 - m42*s2 + m44*s0;
		dest.m34 = -m31*s4 + m32*s2 - m34*s0;
		
		dest.m41 = -m21*c3 + m22*c1 - m23*c0;
		dest.m42 =  m11*c3 - m12*c1 + m13*c0;
		dest.m43 = -m41*s3 + m42*s1 - m43*s0;
		dest.m44 =  m31*s3 - m32*s1 + m33*s0;
		
		double det = s0*c5 -s1*c4 + s2*c3 + s3*c2 - s4*c1 + s5*c0;
		
		if (Math.abs(det) < 1.e-8) {
			// TODO: an exception makes more sense than an assertion here
			// but not exactly parallel to JavaScriptException thrown by native
			// code. what to do?
			throw new RuntimeException("Matrix is singular");
		
		} else {
			det = 1. / det;
			dest.m11 *= det;
			dest.m12 *= det;
			dest.m13 *= det;
			dest.m14 *= det;
			dest.m21 *= det;
			dest.m22 *= det;
			dest.m23 *= det;
			dest.m24 *= det;
			dest.m31 *= det;
			dest.m32 *= det;
			dest.m33 *= det;
			dest.m34 *= det;
			dest.m41 *= det;
			dest.m42 *= det;
			dest.m43 *= det;
			dest.m44 *= det;
		}
	}
	
	/**
	 * Calculates the inverse of this matrix, assuming this is an orthonormal
	 * matrix + translation (e.g. concatenation of rotations and
	 * translations). Any perspective projection will likely have an adverse
	 * effect on the result. Incredibly fast compared to a full inverse.
	 * 
	 * @param dest The destination of the inverse.
	 */
	public void inverseOrthonormalAffine(Matrix4x4 dest) {
		assert (this != dest) : "destination matrix cannot be this matrix";
		
		// transpose upper 3x3
		dest.m11 = m11;
		dest.m12 = m21;
		dest.m13 = m31;
		dest.m21 = m12;
		dest.m22 = m22;
		dest.m23 = m32;
		dest.m31 = m13;
		dest.m32 = m23;
		dest.m33 = m33;
		
		// invert translation
		dest.m14 = -(dest.m11*m14 + dest.m12*m24 + dest.m13*m34);
		dest.m24 = -(dest.m21*m14 + dest.m22*m24 + dest.m23*m34);
		dest.m34 = -(dest.m31*m14 + dest.m32*m24 + dest.m33*m34);
		
		// since we're assured the transform is affine, bottom row is identity
		dest.m41 = 0;
		dest.m42 = 0;
		dest.m43 = 0;
		dest.m44 = 1;
	}
	
	/**
	 * Applies a transform in <em>local</em> coordinates to this matrix.
	 * Essentially, <code>this=this*local</code>.
	 * 
	 * @param local The transform to apply.
	 */
	public void multiply(Matrix4x4 local) {
		multm.multiply(this, local);
		copy(multm);
	}
	
	/**
	 * Sets this matrix equal to <code>view</code> multiplied by
	 * <code>local</code>, with <code>view</code> on the left and
	 * <code>local</code> on the right. This matrix is overwritten in process
	 * of multiplication, so should not itself be view or local.
	 * 
	 * <p>Note: this is the most efficient of the multiply methods, because
	 * no copy is required, but use the transformation-specific methods if at
	 * all possible. They are each optimized for their particular
	 * operation, which can result in needing only a small fraction of the
	 * calculations needed for this general method.</p>
	 * 
	 * <pre>this = view*local</pre>
	 * 
	 * @param view left-hand matrix
	 * @param local right-hand matrix
	 */
	public void multiply(Matrix4x4 view, Matrix4x4 local) {
		// TODO: is this still the most descriptive name for this method?
		assert (this != view && this != local) : "destination matrix should not be one of the factors";
		
		// results stored in dest
		m11 = view.m11*local.m11 + view.m12*local.m21 + view.m13*local.m31 + view.m14*local.m41;
		m12 = view.m11*local.m12 + view.m12*local.m22 + view.m13*local.m32 + view.m14*local.m42;
		m13 = view.m11*local.m13 + view.m12*local.m23 + view.m13*local.m33 + view.m14*local.m43;
		m14 = view.m11*local.m14 + view.m12*local.m24 + view.m13*local.m34 + view.m14*local.m44;
		
		m21 = view.m21*local.m11 + view.m22*local.m21 + view.m23*local.m31 + view.m24*local.m41;
		m22 = view.m21*local.m12 + view.m22*local.m22 + view.m23*local.m32 + view.m24*local.m42;
		m23 = view.m21*local.m13 + view.m22*local.m23 + view.m23*local.m33 + view.m24*local.m43;
		m24 = view.m21*local.m14 + view.m22*local.m24 + view.m23*local.m34 + view.m24*local.m44;
		
		m31 = view.m31*local.m11 + view.m32*local.m21 + view.m33*local.m31 + view.m34*local.m41;
		m32 = view.m31*local.m12 + view.m32*local.m22 + view.m33*local.m32 + view.m34*local.m42;
		m33 = view.m31*local.m13 + view.m32*local.m23 + view.m33*local.m33 + view.m34*local.m43;
		m34 = view.m31*local.m14 + view.m32*local.m24 + view.m33*local.m34 + view.m34*local.m44;
		
		m41 = view.m41*local.m11 + view.m42*local.m21 + view.m43*local.m31 + view.m44*local.m41;
		m42 = view.m41*local.m12 + view.m42*local.m22 + view.m43*local.m32 + view.m44*local.m42;
		m43 = view.m41*local.m13 + view.m42*local.m23 + view.m43*local.m33 + view.m44*local.m43;
		m44 = view.m41*local.m14 + view.m42*local.m24 + view.m43*local.m34 + view.m44*local.m44;
	}
	
	/**
	 * Applies a transform in <em>view</em> coordinates to this matrix.
	 * 
	 * <pre>this=view*this</pre>
	 * 
	 * @param view The transform to apply
	 */
	public void multiplyView(Matrix4x4 view) {
		multm.multiply(view, this);
		copy(multm);
	}
	
	/**
	 * Rotates matrix about x-axis by angle, in <em>local</em> coordinates.
	 * 
	 * @param theta The angle of rotation, in radians.
	 */
	public void rotateX(double theta) {
		/* equivalent to the following, but since tempm is mostly the identity,
		 * saves 3/4 of the multiplies and the copy.
		 * 
		 * tempm.m22 = cos;
		 * tempm.m23 = -sin;
		 * tempm.m32 = sin;
		 * tempm.m33 = cos;
		 * 
		 * multiply(tempm);
		 */
		
		double cos = Math.cos(theta);
		double sin = Math.sin(theta);
		
		double c2 = cos*m12 + m13*sin;
		double c3 = cos*m13 - m12*sin;
		m12 = c2;
		m13 = c3;
		
		c2 = cos*m22 + m23*sin;
		c3 = cos*m23 - m22*sin;
		m22 = c2;
		m23 = c3;
		
		c2 = cos*m32 + m33*sin;
		c3 = cos*m33 - m32*sin;
		m32 = c2;
		m33 = c3;
		
		c2 = cos*m42 + m43*sin;
		c3 = cos*m43 - m42*sin;
		m42 = c2;
		m43 = c3;
	}
	
	/**
	 * Rotates matrix about y-axis by angle, in <em>local</em> coordinates.
	 * 
	 * @param theta The angle of rotation, in radians
	 */
	public void rotateY(double theta) {
		/* equivalent to the following, but since tempm is mostly the identity,
		 * saves 3/4 of the multiplies and the copy.
		 * 
		 * tempm.m11 = cos;
		 * tempm.m13 = sin;
		 * tempm.m31 = -sin;
		 * tempm.m33 = cos;
		 * 
		 * multiply(tempm);
		 */
		
		double cos = Math.cos(theta);
		double sin = Math.sin(theta);
		
		double c1 = cos*m11 - m13*sin;
		double c3 = cos*m13 + m11*sin;
		m11 = c1;
		m13 = c3;
		
		c1 = cos*m21 - m23*sin;
		c3 = cos*m23 + m21*sin;
		m21 = c1;
		m23 = c3;
		
		c1 = cos*m31 - m33*sin;
		c3 = cos*m33 + m31*sin;
		m31 = c1;
		m33 = c3;
		
		c1 = cos*m41 - m43*sin;
		c3 = cos*m43 + m41*sin;
		m41 = c1;
		m43 = c3;
	}
	
	/**
	 * Rotates matrix about z-axis by angle, in <em>local</em> coordinates.
	 * 
	 * @param theta The angle of rotation, in radians
	 */
	public void rotateZ(double theta) {
		/* equivalent to the following, but since tempm is mostly the identity,
		 * saves 3/4 of the multiplies and the copy.
		 * 
		 * tempm.m11 = cos;
		 * tempm.m12 = -sin;
		 * tempm.m21 = sin;
		 * tempm.m22 = cos;
		 * 
		 * multiply(tempm);
		 */
		
		double cos = Math.cos(theta);
		double sin = Math.sin(theta);
		
		double c1 = cos*m11 + m12*sin;
		double c2 = cos*m12 - m11*sin;
		m11 = c1;
		m12 = c2;
		
		c1 = cos*m21 + m22*sin;
		c2 = cos*m22 - m21*sin;
		m21 = c1;
		m22 = c2;
		
		c1 = cos*m31 + m32*sin;
		c2 = cos*m32 - m31*sin;
		m31 = c1;
		m32 = c2;
		
		c1 = cos*m41 + m42*sin;
		c2 = cos*m42 - m41*sin;
		m41 = c1;
		m42 = c2;
	}
	
	/**
	 * Scales the matrix by the given vector in <em>local</em> coordinates.
	 * 
	 * @param sx The x coordinate of the scale vector
	 * @param sy The y coordinate of the scale vector
	 * @param sz The z coordinate of the scale vector
	 */
	public void scale(double sx, double sy, double sz) {
		/* equivalent to the following, but since tempm is mostly the identity,
		 * saves 13/16 of the multiplies and the copy.
		 * 
		 * tempm.m11 = sx;
		 * tempm.m22 = sy;
		 * tempm.m33 = sz;
		 * 
		 * multiply(tempm);
		 */
		
		// just scaling the basis vectors (columns)
		m11 *= sx;
		m21 *= sx;
		m31 *= sx;
		m41 *= sx;
		
		m12 *= sy;
		m22 *= sy;
		m32 *= sy;
		m42 *= sy;
		
		m13 *= sz;
		m23 *= sz;
		m33 *= sz;
		m43 *= sz;
	}
	
	/**
	 * Scales the matrix by the given vector in <em>local</em> coordinates. In
	 * 2-space, the vector is [sx, sy]. In 3, it is [sx, sy, 1].
	 * 
	 * @param sx The x coordinate of the scale vector
	 * @param sy The y coordinate of the scale vector
	 */
	public void scale(double sx, double sy) {
		/* equivalent to the following
		 * 
		 * tempm.m11 = sx;
		 * tempm.m22 = sy;
		 * tempm.m33 = 1;
		 * 
		 * multiply(tempm);
		 */
		
		// just scaling the basis vectors (columns)
		m11 *= sx;
		m21 *= sx;
		m31 *= sx;
		m41 *= sx;
		
		m12 *= sy;
		m22 *= sy;
		m32 *= sy;
		m42 *= sy;
	}
	
	/**
	 * Set this matrix to the identity.
	 */
	public void setToIdentity() {
		m11 = 1; m12 = 0; m13 = 0; m14 = 0;
		m21 = 0; m22 = 1; m23 = 0; m24 = 0;
		m31 = 0; m32 = 0; m33 = 1; m34 = 0;
		m41 = 0; m42 = 0; m43 = 0; m44 = 1;
	}
	
	
	/**
	 * Shear space along the x-axis by a value multiple of a y-coordinate.
	 * 
	 * @param value Shear value.
	 */
	public void shearX(double value) {
		/* equivalent to the following, but since tempm is mostly the identity,
		 * saves 15/16 of the multiplies and the copy.
		 * 
		 * tempm.m12 = value;
		 * multiply(tempm);
		 */
		
		m12 += m11*value;
		m22 += m21*value;
		m32 += m31*value;
		m42 += m41*value;
	}
	
	/**
	 * Shear space along the y-axis by a value multiple of a x-coordinate.
	 * 
	 * @param value Shear value.
	 */
	public void shearY(double value) {
		/* equivalent to the following, but since tempm is mostly the identity,
		 * saves 15/16 of the multiplies and the copy.
		 * 
		 * tempm.m21 = value;
		 * multiply(tempm);
		 */
		
		m11 += m12*value;
		m21 += m22*value;
		m31 += m32*value;
		m41 += m42*value;
	}
	
	/**
	 * Returns the x-component of the image of point (x, y, z, w) under the
	 * current transform. A w-value of 1 is generally used for points, 0 for
	 * vectors.
	 * 
	 * @param x The x coordinate of point to transform
	 * @param y The y coordinate of point to transform
	 * @param z The z coordinate of point to transform
	 * @param w The w coordinate of point to transform
	 * @return The x coordinate of the transformed point
	 */
	public double transformX(double x, double y, double z, double w) {
		return x*m11 + y*m12 + z*m13 + w*m14;
	}
	
	/**
	 * Returns the y-component of the image of local-space point (x, y, z, w)
	 * under the current transform. A w value of 1 is generally used for
	 * points, 0 for vectors.
	 * 
	 * @param x The x coordinate of point to transform
	 * @param y The y coordinate of point to transform
	 * @param z The z coordinate of point to transform
	 * @param w The w coordinate of point to transform
	 * @return The y coordinate of the transformed point
	 */
	public double transformY(double x, double y, double z, double w) {
		return x*m21 + y*m22 + z*m23 + w*m24;
	}
	
	/**
	 * Returns the z-component of the image of local-space point (x, y, z, w)
	 * under the current transform. A w value of 1 is generally used for
	 * points, 0 for vectors.
	 * 
	 * @param x The x coordinate of point to transform
	 * @param y The y coordinate of point to transform
	 * @param z The z coordinate of point to transform
	 * @param w The w coordinate of point to transform
	 * @return The z coordinate of the transformed point
	 */
	public double transformZ(double x, double y, double z, double w) {
		return x*m31 + y*m32 + z*m33 + w*m34;
	}
	
	/**
	 * Returns the w-component of the image of local-space point (x, y, z, w)
	 * under the current transform. A w value of 1 is generally used for
	 * points, 0 for vectors.
	 * 
	 * @param x The x coordinate of point to transform
	 * @param y The y coordinate of point to transform
	 * @param z The z coordinate of point to transform
	 * @param w The w coordinate of point to transform
	 * @return The w coordinate of the transformed point
	 */
	public double transformW(double x, double y, double z, double w) {
		return x*m41 + y*m42 + z*m43 + w*m44;
	}
	
	/**
	 * Translates the matrix by a given vector, in <em>local</em> coordinates.
	 * 
	 * @param tx The x coordinate of the translation vector
	 * @param ty The y coordinate of the translation vector
	 * @param tz The z coordinate of the translation vector
	 */
	public void translate(double tx, double ty, double tz) {
		/* equivalent to the following, but since tempm is mostly the identity,
		 * saves 13/16 of the multiplies and the copy.
		 * 
		 * tempm.m14 = tx;
		 * tempm.m24 = ty;
		 * tempm.m34 = tz;
		 * multiply(tempm);
		 */
		
		// adding translation vector in changed basis
		m14 += m11*tx + m12*ty + m13*tz;
		m24 += m21*tx + m22*ty + m23*tz;
		m34 += m31*tx + m32*ty + m33*tz;
		m44 += m41*tx + m42*ty + m43*tz;
	}
	
	/**
	 * Translates the matrix by a given vector, in <em>local</em> coordinates.
	 * 
	 * @param tx The x coordinate of the translation vector
	 * @param ty The y coordinate of the translation vector
	 */
	public void translate(double tx, double ty) {
		/* equivalent to the following
		 * 
		 * tempm.m14 = tx;
		 * tempm.m24 = ty;
		 * multiply(tempm);
		 */
		
		// adding translation vector in changed basis
		m14 += m11*tx + m12*ty;
		m24 += m21*tx + m22*ty;
		m34 += m31*tx + m32*ty;
		m44 += m41*tx + m42*ty;
	}
}

