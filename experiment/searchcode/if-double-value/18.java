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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayNumber;

/**
 * Represents a 4x4 matrix for representation of affine transformations in
 * three dimensions (using homogeneous coordinates). The matrix is
 * "column-major," meaning that, in the multiplication of a series of
 * transforms, the first applied matrix is on the far right. The translation
 * values are found in column four.
 * 
 * <p>The implementing structure is platform dependent. Where available, this
 * class uses implementations of CSSMatrix for the speed of native-code matrix
 * operations. Everywhere else, the matrix is simply a {@link Matrix4x4}.</p>
 */
public abstract class Transform {
	protected static final int STYLE_PRECISION = 10;
	
	// static array used to construct filter string
	private static JsArrayNumber filterArray;
	
	// flag for static array initialization
	private static boolean filterArrayInited = false;
	
	/**
	 * Returns a new, platform appropriate Transform object.
	 */
	public static Transform create() {
		if (GWT.isScript()) {
			return GWT.create(Transform.class);
		} else {
			return new TransformEmulated();
		}
	}
	
	/**
	 * Creates a native array of filter string components. When odd entries are
	 * filled, array can be joined for filter string.
	 * 
	 * NOTE: breaks type safety
	 * 
	 * @return filter array
	 */
	private static native JsArrayNumber getFilterArray() /*-{
		return [
			"progid:DXImageTransform.Microsoft.Matrix(M11=",
			1,
			", M12=",
			0,
			", M21=",
			0,
			", M22=",
			1,
			", SizingMethod = 'auto expand')"
			];
	}-*/;
	
	/**
	 * Convert a floating point number to a string with the default number
	 * of digits after the decimal place.
	 * 
	 * @param value to round and convert
	 * @return String representation of value
	 */
	protected static final String toFixed(double value) {
		return toFixed(value, STYLE_PRECISION);
	}
	
	/**
	 * Convert a floating point number to a string with the specified number
	 * of digits after the decimal place (note: that is <em>not</em> total
	 * digits).
	 * 
	 * @param value to round and convert
	 * @param numDigits	number of digits after the decimal point
	 * @return String representation of value
	 */
	protected static final native String toFixed(double value, int numDigits) /*-{
		return value.toFixed(numDigits);
	}-*/;
	
	/**
	 * @return The first component of the first basis vector of the transform.
	 */
	public double getA() {
		return getM11();
	}
	
	/**
	 * @return The second component of the first basis vector of the transform.
	 */
	public double getB() {
		return getM21();
	}
	
	/**
	 * @return The first component of the second basis vector of the transform.
	 */
	public double getC() {
		return getM12();
	}
	
	/**
	 * @return The second component of the second basis vector of the transform.
	 */
	public double getD() {
		return getM22();
	}
	
	/**
	 * @return The first component of the translation vector of the transform.
	 */
	public double getE() {
		return getM14();
	}
	
	/**
	 * @return The second component of the translation vector of the transform.
	 */
	public double getF() {
		return getM24();
	}
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the first row and first column.
	 */
	public abstract double getM11();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the first row and second column.
	 */
	public abstract double getM12();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the first row and third column.
	 */
	public abstract double getM13();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the first row and fourth column.
	 */
	public abstract double getM14();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the second row and first column.
	 */
	public abstract double getM21();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the second row and second column.
	 */
	public abstract double getM22();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the second row and third column.
	 */
	public abstract double getM23();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the second row and fourth column.
	 */
	public abstract double getM24();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the third row and first column.
	 */
	public abstract double getM31();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the third row and second column.
	 */
	public abstract double getM32();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the third row and third column.
	 */
	public abstract double getM33();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the third row and fourth column.
	 */
	public abstract double getM34();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the fourth row and first column.
	 */
	public abstract double getM41();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the fourth row and second column.
	 */
	public abstract double getM42();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the fourth row and third column.
	 */
	public abstract double getM43();
	
	/**
	 * One of the 16 values for the 4x4 homogeneous matrix.
	 * @return The matrix value in the fourth row and fourth column.
	 */
	public abstract double getM44();
	
	/**
	 * Calculates the inverse of this matrix, if it exists, and stores it in
	 * dest. This matrix is not modified by this method.
	 * 
	 * @param dest the destination of the inverse
	 */
	public abstract void inverse(Transform dest);
	
	/**
	 * Calculates the inverse of this matrix, assuming this is an
	 * <em>orthonormal</em> matrix + translation (e.g. concatenation of
	 * rotations and translations), and stores it in dest. Any perspective
	 * projection will likely have an adverse effect on the result. When this
	 * method is executed in pure Javascript, it is significantly faster than
	 * the brute-force {@link #inverse(Transform)}.
	 * 
	 * <p>This matrix is not modified by this method.</p>
	 * 
	 * @param dest The destination of the inverse.
	 */
	public abstract void inverseOrthonormalAffine(Transform dest);
	
	// TODO: another, more general 3x4 matrix inversion method?
	
	/**
	 * Applies a transform in <em>local</em> coordinates to this matrix.
	 * Essentially, <code>this=this*local</code>.
	 * 
	 * @param local The transform to apply.
	 */
	public abstract void multiply(Transform local);
	
	/**
	 * Sets this matrix equal to <code>view</code> multiplied by
	 * <code>local</code>, with <code>view</code> on the left and
	 * <code>local</code> on the right. This matrix is overwritten in process
	 * of multiplication, so should not itself be view or local.
	 * 
	 * <pre>this = view*local</pre>
	 * 
	 * @param view left-hand matrix
	 * @param local right-hand matrix
	 */
	public abstract void multiply(Transform view, Transform local);
	
	/**
	 * Applies a transform in <em>view</em> coordinates to this matrix.
	 * 
	 * <pre>this=view*this</pre>
	 * 
	 * @param view The transform to apply
	 */
	public abstract void multiplyView(Transform view);
	
	/**
	 * Rotation in <em>local</em> (transformed) coordinates by 
	 * angle theta.
	 * 
	 * <p><em>Note:</em> due to definition of screen coordinates
	 * (with +y pointing down), positive values of angle rotate
	 * <em>clockwise</em>.
	 * 
	 * @param theta The angle to rotate, in radians
	 */
	public void rotate(double theta) {
		rotateZ(theta);
	}
	
	/**
	 * Rotates matrix about x-axis by angle, in <em>local</em> coordinates.
	 * 
	 * @param theta The angle of rotation, in radians.
	 */
	public abstract void rotateX(double theta);
	
	/**
	 * Rotates matrix about y-axis by angle, in <em>local</em> coordinates.
	 * 
	 * @param theta The angle of rotation, in radians
	 */
	public abstract void rotateY(double theta);
	
	/**
	 * Rotates matrix about z-axis by angle, in <em>local</em> coordinates.
	 * 
	 * @param theta The angle of rotation, in radians
	 */
	public abstract void rotateZ(double theta);
	
	// TODO: public abstract void rotate(double angleX, double angleY, double angleZ);
	
	// TODO: rotateAxisAngle(double x, double y, double z, double angle);
	
	/**
	 * Scales the matrix by the given vector in <em>local</em> coordinates.
	 * 
	 * @param sx The x component of the scale vector
	 * @param sy The y component of the scale vector
	 */
	public abstract void scale(double sx, double sy);
	
	/**
	 * Scales the matrix by the given vector in <em>local</em> coordinates.
	 * 
	 * @param sx The x component of the scale vector
	 * @param sy The y component of the scale vector
	 * @param sz The z component of the scale vector
	 */
	public abstract void scale(double sx, double sy, double sz);

	/**
	 * Set the first component of the first basis vector of the transform.
	 * @param value new value of component
	 */
	public void setA(double value) {
		setM11(value);
	}
	
	/**
	 * Set the second component of the first basis vector of the transform.
	 * @param value new value of component
	 */
	public void setB(double value) {
		setM21(value);
	}
	
	/**
	 * Set the first component of the second basis vector of the transform.
	 * @param value new value of component
	 */
	public void setC(double value) {
		setM12(value);
	}
	
	/**
	 * Set the second component of the second basis vector of the transform.
	 * @param value new value of component
	 */
	public void setD(double value) {
		setM22(value);
	}
	
	/**
	 * Set the first component of the third basis vector of the transform.
	 * @param value new value of component
	 */
	public void setE(double value) {
		setM14(value);
	}
	
	/**
	 * Set the second component of the third basis vector of the transform.
	 * @param value new value of component
	 */
	public void setF(double value) {
		setM24(value);
	}
	
	/**
	 * Set the matrix value in the first row and first column.
	 * @param value the new value
	 */
	public abstract void setM11(double value);
	
	/**
	 * Set the matrix value in the first row and second column.
	 * @param value the new value
	 */
	public abstract void setM12(double value);
	
	/**
	 * Set the matrix value in the first row and third column.
	 * @param value the new value
	 */
	public abstract void setM13(double value);
	
	/**
	 * Set the matrix value in the first row and fourth column.
	 * @param value the new value
	 */
	public abstract void setM14(double value);
	
	/**
	 * Set the matrix value in the second row and first column.
	 * @param value the new value
	 */
	public abstract void setM21(double value);
	
	/**
	 * Set the matrix value in the second row and second column.
	 * @param value the new value
	 */
	public abstract void setM22(double value);
	
	/**
	 * Set the matrix value in the second row and third column.
	 * @param value the new value
	 */
	public abstract void setM23(double value);
	
	/**
	 * Set the matrix value in the second row and fourth column.
	 * @param value the new value
	 */
	public abstract void setM24(double value);
	
	/**
	 * Set the matrix value in the third row and first column.
	 * @param value the new value
	 */
	public abstract void setM31(double value);
	
	/**
	 * Set the matrix value in the third row and second column.
	 * @param value the new value
	 */
	public abstract void setM32(double value);
	
	/**
	 * Set the matrix value in the third row and third column.
	 * @param value the new value
	 */
	public abstract void setM33(double value);
	
	/**
	 * Set the matrix value in the third row and fourth column.
	 * @param value the new value
	 */
	public abstract void setM34(double value);
	
	/**
	 * Set the matrix value in the fourth row and first column.
	 * @param value the new value
	 */
	public abstract void setM41(double value);
	
	/**
	 * Set the matrix value in the fourth row and second column.
	 * @param value the new value
	 */
	public abstract void setM42(double value);
	
	/**
	 * Set the matrix value in the fourth row and third column.
	 * @param value the new value
	 */
	public abstract void setM43(double value);
	
	/**
	 * Set the matrix value in the fourth row and fourth column.
	 * @param value the new value
	 */
	public abstract void setM44(double value);
	
	/**
	 * Set this matrix to the identity.
	 */
	public abstract void setToIdentity();
	
	/**
	 * Sets this matrix equal to the specified transform.
	 * 
	 * @param src The transform to be copied.
	 */
	public abstract void setTransform(Transform src);
	
	/**
	 * Sets this matrix equal to the specified transform.
	 * 
	 * @param src The transform to be copied.
	 */
	public abstract void setTransform(Matrix4x4 src);
	
	// TODO public abstract void setMatrixValue(String matrix);
	
	/**
	 * Skews <em>local</em> (transformed) coordinates along the x-axis by 
	 * the given angle.
	 * 
	 * @param theta The skew angle, in radians.
	 */
	public abstract void skewX(double theta);
	
	/**
	 * Skews <em>local</em> (transformed) coordinates along the y-axis by
	 * the given angle.
	 * 
	 * @param theta The skew angle, in radians.
	 */
	public abstract void skewY(double theta);
	
	/**
	 * Create a string representation of this matrix, suitable for the CSS3
	 * matrix transform function property.
	 * 
	 * @see <a href="http://www.w3.org/TR/css3-2d-transforms/#transform-functions">Matrix Transformation Function</a>
	 * 
	 * @return A string representation of the matrix&#x2019;s values
	 */
	public String toCss2dTransformString() {
		String tmp = "matrix(";
		tmp += toFixed(getA()) + ", ";
		tmp += toFixed(getB()) + ", ";
		tmp += toFixed(getC()) + ", ";
		tmp += toFixed(getD()) + ", ";
		tmp += toFixed(getE()) + ", ";
		tmp += toFixed(getF()) + ")";
			
		return tmp;
	}
	
	/**
	 * Create a string representation of this matrix, suitable for the CSS3
	 * matrix3d transform function property.
	 * 
	 * @see <a href="http://www.w3.org/TR/css3-3d-transforms/#transform-functions">Matrix3D Transformation Function</a>
	 * 
	 * @return A string representation of the matrix&#x2019;s values
	 */
	public String toCss3dTransformString() {
		// TODO: this needs to be replaced, but currently unused
		String tmp = "matrix3d(";
		tmp += toFixed(getM11()) + ", ";
		tmp += toFixed(getM21()) + ", ";
		tmp += toFixed(getM31()) + ", ";
		tmp += toFixed(getM41()) + ", ";
		tmp += toFixed(getM12()) + ", ";
		tmp += toFixed(getM22()) + ", ";
		tmp += toFixed(getM32()) + ", ";
		tmp += toFixed(getM42()) + ", ";
		tmp += toFixed(getM13()) + ", ";
		tmp += toFixed(getM23()) + ", ";
		tmp += toFixed(getM33()) + ", ";
		tmp += toFixed(getM43()) + ", ";
		tmp += toFixed(getM14()) + ", ";
		tmp += toFixed(getM24()) + ", ";
		tmp += toFixed(getM34()) + ", ";
		tmp += toFixed(getM44()) + ")";
			
		return tmp;
	}
	
	/**
	 * Create a Firefox-specific string representation of this matrix, suitable
	 * for the CSS3 matrix transform function property in that browser.
	 * Specifically, the last two values need units.
	 * 
	 * @see <a href="http://www.w3.org/TR/css3-2d-transforms/#transform-functions">Matrix Transformation Function</a>
	 * 
	 * @return A string representation of the matrix&#x2019;s values
	 */
	public String toFirefoxCss2dTransformString() {
		String tmp = "matrix(";
		tmp += toFixed(getA()) + ", ";
		tmp += toFixed(getB()) + ", ";
		tmp += toFixed(getC()) + ", ";
		tmp += toFixed(getD()) + ", ";
		tmp += toFixed(getE()) + "px, ";
		tmp += toFixed(getF()) + "px)";
			
		return tmp;
	}
	
	/**
	 * Constructs a Matrix Filter string for the 2d linear transformation
	 * portion of the specified transform.
	 * 
	 * @return A Matrix Filter string
	 */
	public String toIEFilterString() {
		// construct only once
		if (!filterArrayInited) {
			filterArray = getFilterArray();
			filterArrayInited = true;
		}
		
		filterArray.set(1, getA());
		filterArray.set(3, getC());
		filterArray.set(5, getB());
		filterArray.set(7, getD());
		return filterArray.join("");
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
	public abstract double transformX(double x, double y, double z, double w);
	
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
	public abstract double transformY(double x, double y, double z, double w);
	
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
	public abstract double transformZ(double x, double y, double z, double w);
	
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
	public abstract double transformW(double x, double y, double z, double w);
	
	/**
	 * Translates the matrix by a given vector, in <em>local</em> coordinates.
	 * 
	 * @param tx The x coordinate of the translation vector
	 * @param ty The y coordinate of the translation vector
	 */
	public abstract void translate(double tx, double ty);
	
	/**
	 * Translates the matrix by a given vector, in <em>local</em> coordinates.
	 * 
	 * @param tx The x coordinate of the translation vector
	 * @param ty The y coordinate of the translation vector
	 * @param tz The z coordinate of the translation vector
	 */
	public abstract void translate(double tx, double ty, double tz);
}

