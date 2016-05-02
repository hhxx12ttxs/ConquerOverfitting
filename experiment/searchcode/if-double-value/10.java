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
 * A subclass of {@link TransformStandard}, but using Webkit's
 * WebKitCSSMatrix. This is the implementation for the <em>most recent</em>
 * releases of Webkit.
 * 
 * <p>Currently, WebKitCSSMatrix is in a strange place, where
 * multiplication is done as if the matrix were column-major, but the elements
 * are accessed at the transpose of where that would imply. This class merely
 * maps elements to their transpose to correct this (hopefully) soon to be
 * corrected oversight, and implements skew(), which is not yet widely
 * supported.</p>
 */
class TransformWebKit extends TransformStandard {
	@Override
	public double getM11() {
		return transform.getM11();
	}
	
	@Override
	public double getM12() {
		return transform.getM21();
	}
	
	@Override
	public double getM13() {
		return transform.getM31();
	}
	
	@Override
	public double getM14() {
		return transform.getM41();
	}
	
	@Override
	public double getM21() {
		return transform.getM12();
	}
	
	@Override
	public double getM22() {
		return transform.getM22();
	}
	
	@Override
	public double getM23() {
		return transform.getM32();
	}
	
	@Override
	public double getM24() {
		return transform.getM42();
	}
	
	@Override
	public double getM31() {
		return transform.getM13();
	}
	
	@Override
	public double getM32() {
		return transform.getM23();
	}
	
	@Override
	public double getM33() {
		return transform.getM33();
	}
	
	@Override
	public double getM34() {
		return transform.getM43();
	}
	
	@Override
	public double getM41() {
		return transform.getM14();
	}
	
	@Override
	public double getM42() {
		return transform.getM24();
	}
	
	@Override
	public double getM43() {
		return transform.getM34();
	}
	
	@Override
	public double getM44() {
		return transform.getM44();
	}
	
	@Override
	public void setM11(double value) {
		transform.setM11(value);
	}

	@Override
	public void setM12(double value) {
		transform.setM21(value);
	}

	@Override
	public void setM13(double value) {
		transform.setM31(value);
	}

	@Override
	public void setM14(double value) {
		transform.setM41(value);
	}

	@Override
	public void setM21(double value) {
		transform.setM12(value);
	}

	@Override
	public void setM22(double value) {
		transform.setM22(value);
	}

	@Override
	public void setM23(double value) {
		transform.setM32(value);
	}

	@Override
	public void setM24(double value) {
		transform.setM42(value);
	}

	@Override
	public void setM31(double value) {
		transform.setM13(value);
	}

	@Override
	public void setM32(double value) {
		transform.setM23(value);
	}

	@Override
	public void setM33(double value) {
		transform.setM33(value);
	}

	@Override
	public void setM34(double value) {
		transform.setM43(value);
	}

	@Override
	public void setM41(double value) {
		transform.setM14(value);
	}

	@Override
	public void setM42(double value) {
		transform.setM24(value);
	}

	@Override
	public void setM43(double value) {
		transform.setM34(value);
	}

	@Override
	public void setM44(double value) {
		transform.setM44(value);
	}
	
	@Override
	public void skewX(double theta) {
		// for some reason this isn't implemented on (at least) Windows yet
		CssMatrix tmp = createNewMatrix();
		tmp.setM21(Math.tan(theta)); // m<column><row>
		transform = transform.multiply(tmp);
	}

	@Override
	public void skewY(double theta) {
		// for some reason this isn't implemented on (at least) Windows yet
		CssMatrix tmp = createNewMatrix();
		tmp.setM12(Math.tan(theta)); // m<column><row>
		transform = transform.multiply(tmp);
	}
	
	@Override
	CssMatrix createNewMatrix() {
		return createWebKitCssMatrix();
	}
	
	// lies
	final native CssMatrix createWebKitCssMatrix() /*-{
		return new WebKitCSSMatrix();
	}-*/;
}

