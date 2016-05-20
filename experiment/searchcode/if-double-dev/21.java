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

package gwt.ns.transformedelement.client;

import gwt.ns.transforms.client.Matrix4x4;
import gwt.ns.transforms.client.Transform;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

/**
 * The base class for applying transformations to document elements. Typical
 * use:
 * 
 * <p><code>TransformedElement box = {@link TransformedElement#wrap(Element)};<br>
 * ...<br>
 * box.setTransform(existingMatrix);<br>
 * box.rotate(...);<br>
 * box.translate(...);<br>
 * ...
 * </code></p>
 * 
 * <p>Transformation methods should cover most needs, but more sophisticated
 * control can be had through explicitly setting the transformation matrix. The
 * class will concatenate all transforms and apply them to the DOM only
 * once per execution context.</p>
 * 
 * <p>Relative positioning is still rather quirk-prone. It will work perfectly
 * on platforms with full CSS3 2D transform support -- where content will be
 * unmoved by a neighboring transformed element -- but will disrupt page flow
 * in Internet Explorer. The easiest workaround is to position the element
 * absolutely within some relatively positioned parent element.</p>
 * 
 * <p>For full performance in Internet Explorer, if changing
 * the transformation on an object more than once (especially for an
 * animation), the TransformedElement object should be retained and used
 * for further transformations on that element. For more modern browsers, this
 * is essentially a wrapper for a matrix and a convenient way to accumulate
 * transformations before application. Create and discard accordingly.</p>
 * 
 * <p>2D is supported across all browsers with some kind of transform
 * mechanism. 3D is supported in theory; on most platforms, elements will be
 * orthographically projected onto the screen (a point <code>[x, y, z]</code>
 * will collapse to <code>[x, y, 0]</code>). This is true even on platforms
 * that ostensibly support 3D CSS transforms, but do not yet have support for
 * perspective projections (like WebKit on Windows).</p>
 * 
 * <p>If 3D transforms are used, with the final projection onto the screen any
 * depth information may be lost. Browsers that support 3D transforms
 * <em>may</em> honor the depth information when z-sorting elements, but on
 * browsers with only 2D transforms, no sorting will be done. This information
 * can be derived manually from the transformation matrix.</p>
 * 
 * @see <a href="http://www.w3.org/TR/css3-2d-transforms/">Current working draft of CSS 2D Transforms Module Level 3</a>
 */
public abstract class TransformedElement {
	// TODO: static "apply transform and retain nothing" method?

	protected static final int STYLE_PRECISION = 10;

	protected static TransformCommitScheduler transformScheduler;

	/**
	 * Convenience method. Creates a new DOM element and returns a
	 * TransformedElement New handle to it.
	 */
	public static TransformedElement create() {
		return wrap(Document.get().createDivElement());
	}

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
	 * Create an affine transform handle for DOM element elem.
	 * 
	 * @param elem The element to Transform
	 */
	public static TransformedElement wrap(Element elem) {
		if (transformScheduler == null)
			transformScheduler = new TransformCommitScheduler();

		// get a system appropriate implementation of TransformedElement
		TransformedElement transElem = (TransformedElement) GWT.create(TransformedElement.class);

		// allow transforms module to handle binding
		transElem.transform = Transform.create();

		transElem.target = elem;

		return transElem;
	}

	protected boolean scheduled = false;
	protected Element target;
	protected Transform transform;

	/**
	 * Manually apply the current transform to this Element.
	 * This method involves DOM access and style setting, so will be slow.
	 * Whenever a transform is set, a commit is automatically scheduled using
	 * {@link Scheduler#scheduleFinally}, so call this method only if forcing a
	 * commit is necessary.
	 * 
	 * <p><em>Note:</em> if a transform is applied before the element is
	 * attached to the DOM, commitTranform() must be called at least once after
	 * attachment for proper positioning.</p>
	 */
	public abstract void commitTransform();

	/**
	 * Returns a handle to the DOM element being transformed.
	 */
	public Element getElement() {
		return target;
	}

	/**
	 * Apply a transformation in the current <em>local</em> coordinate system
	 * by setting the transform equal to the current transform multiplied by
	 * <code>local</code>, with <code>local</code> on the right.
	 * 
	 * @param local the transformation to apply
	 */
	public void multiply(Transform local) {
		transform.multiply(local);
		scheduleCommit();
	}

	/**
	 * Set the current transform equal to <code>view</code> multiplied by
	 * <code>local</code>, with <code>view</code> on the left and
	 * <code>local</code> on the right.
	 * 
	 * <p>The element's transformation before calling this method will have no
	 * effect on the result and will be lost in the process.</p>
	 * 
	 * @param view the view transformation
	 * @param local the local transfomation
	 */
	public void multiply(Transform view, Transform local) {
		transform.multiply(local);
		scheduleCommit();
	}

	/**
	 * Apply a transformation in the current <em>view</em> coordinate system
	 * by setting the transform equal to <code>view</code> multiplied by the
	 * current transform, with <code>view</code> on the left.
	 * 
	 * @param view the transformation to apply
	 */
	public void multiplyView(Transform view) {
		transform.multiplyView(view);
		scheduleCommit();
	}

	/**
	 * A copy of the current transform is written to dest. Future changes to
	 * dest will not affect this element unless reapplied with e.g.
	 * {@link #setTransform(Transform)}.
	 * 
	 * @param dest The destination transform for a copy of the element's current transform.
	 */
	public void getTransform(Transform dest) {
		dest.setTransform(transform);
	}

	// TODO public void resetTranform() {
	// if target was originally wrapped, set transform to whatever transform was originally set
	//}

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
		transform.rotate(theta);
		scheduleCommit();
	}

	/**
	 * Rotates matrix about x-axis by angle, in <em>local</em> coordinates.
	 * 
	 * @param theta The angle of rotation, in radians.
	 */
	public void rotateX(double theta) {
		transform.rotateX(theta);
		scheduleCommit();
	}

	/**
	 * Rotates matrix about y-axis by angle, in <em>local</em> coordinates.
	 * 
	 * @param theta The angle of rotation, in radians
	 */
	public void rotateY(double theta) {
		transform.rotateY(theta);
		scheduleCommit();
	}

	/**
	 * Rotates matrix about z-axis by angle, in <em>local</em> coordinates.
	 * 
	 * @param theta The angle of rotation, in radians
	 */
	public void rotateZ(double theta) {
		transform.rotateZ(theta);
		scheduleCommit();
	}

	// TODO: rotate3d(<number>, <number>, <number>, <angle>) would map to rotateAxisAngle

	/**
	 * Scale element in <em>local</em> (transformed) coordinates by vector
	 * (sx, sy).
	 * 
	 * @param sx The scaling along the local x-axis
	 * @param sy The scaling along the local y-axis
	 */
	public void scale(double sx, double sy) {
		transform.scale(sx, sy);
		scheduleCommit();
	}

	/**
	 * Scale element in <em>local</em> (transformed) coordinates along the
	 * x-axis by factor sx.
	 * 
	 * @param sx The scaling along the local x-axis
	 */
	public void scaleX(double sx) {
		transform.scale(sx, 1);
		scheduleCommit();
	}

	/**
	 * Scale element in <em>local</em> (transformed) coordinates along the
	 * y-axis by factor sy.
	 * 
	 * @param sy The scaling along the local y-axis
	 */
	public void scaleY(double sy) {
		transform.scale(1, sy);
		scheduleCommit();
	}

	/**
	 * Scale element in <em>local</em> (transformed) coordinates along the
	 * z-axis by factor sz.
	 * 
	 * @param sz The scaling along the local z-axis
	 */
	public void scaleZ(double sz) {
		transform.scale(1, 1, sz);
		scheduleCommit();
	}

	/**
	 * Scale element in <em>local</em> (transformed) coordinates by vector
	 * (sx, sy, sz).
	 * 
	 * @param sx The scaling along the local x-axis
	 * @param sy The scaling along the local y-axis
	 * @param sz The scaling along the local z-axis
	 */
	public void scale3d(double sx, double sy, double sz) {
		transform.scale(sx, sy, sz);
		scheduleCommit();
	}

	/**
	 * Sets the origin of the transformation, relative to the
	 * <em>border box</em> of the element, specified as a percentage of that
	 * box's dimensions. In other words, this point will be affected by
	 * translations only. All other transformations will leave this (and only
	 * this) point unmoved.
	 * 
	 * <p>The default origin is (50%, 50%).<p>
	 * 
	 * @param ox The x-coordinate of the new origin, as a percentage
	 * @param oy The y-coordinate of the new origin, as a percentage
	 * 
	 * @see <a href="http://dev.w3.org/csswg/css3-2d-transforms/#transform-origin">Transform Origin Property</a>
	 */
	public abstract void setOriginPercentage(double ox, double oy);
	
	/**
	 * Sets the origin of the transformation, relative to the
	 * <em>border box</em> of the element, specified in pixels. In other
	 * words, this point will be affected by translations only. All other
	 * transformations will leave this (and only this) point unmoved.
	 * 
	 * <p>The default origin is (50%, 50%).<p>
	 * 
	 * @param ox The x-coordinate of the new origin, in pixels
	 * @param oy The y-coordinate of the new origin, in pixels
	 * 
	 * @see <a href="http://dev.w3.org/csswg/css3-2d-transforms/#transform-origin">Transform Origin Property</a>
	 */
	public abstract void setOriginPixels(double ox, double oy);

	/**
	 * Reset the current transform to the identity. This will leave the
	 * element untransformed.
	 */
	public void setToIdentityTransform() {
		transform.setToIdentity();
		scheduleCommit();
	}

	/**
	 * Apply a new transform to this element from a {@link Matrix4x4}.
	 * 
	 * @param newTransform The transform to apply
	 */
	public void setTransform(Matrix4x4 newTransform) {
		transform.setTransform(newTransform);
		scheduleCommit();
	}

	/**
	 * Apply a new transform to this element from a {@link Transform}.
	 * 
	 * @param newTransform The transform to apply
	 */
	public void setTransform(Transform newTransform) {
		transform.setTransform(newTransform);
		scheduleCommit();
	}

	/**
	 * Skews <em>local</em> (transformed) coordinates along the x-axis by 
	 * the given angle.
	 * 
	 * @param theta The skew angle, in radians.
	 */
	public void skewX(double theta) {
		transform.skewX(theta);
		scheduleCommit();
	}

	/**
	 * Skews <em>local</em> (transformed) coordinates along the y-axis by
	 * the given angle.
	 * 
	 * @param theta The skew angle, in radians.
	 */
	public void skewY(double theta) {
		transform.skewY(theta);
		scheduleCommit();
	}

	/**
	 * Translate element in <em>local</em> (transformed) coordinates by vector
	 * (tx, ty).
	 * 
	 * @param tx The translation along local x-axis
	 * @param ty The translation along local y-axis
	 */
	public void translate(double tx, double ty) {
		transform.translate(tx, ty);
		scheduleCommit();
	}

	/**
	 * Translate element in <em>local</em> (transformed) coordinates by vector
	 * (tx, ty, tz).
	 * 
	 * @param tx The translation along local x-axis
	 * @param ty The translation along local y-axis
	 * @param tz The translation along local y-axis
	 */
	public void translate3d(double tx, double ty, double tz) {
		transform.translate(tx, ty, tz);
		scheduleCommit();
	}

	/**
	 * Schedule a call to commitTransform() so that any transform changes will
	 * be written to the element's style.
	 */
	void scheduleCommit() {
		if (!scheduled) {
			scheduled = true;
			transformScheduler.scheduleCommit(this);
		}
	}

	/**
	 * Scheduler callback.
	 */
	final void executeCommit() {
		commitTransform();
		scheduled = false;
	}
}
