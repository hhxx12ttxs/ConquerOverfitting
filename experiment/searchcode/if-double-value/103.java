<<<<<<< HEAD
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang;

/**
 * The wrapper for the primitive type {@code double}.
 *
 * @see java.lang.Number
 * @since 1.0
 */
public final class Double extends Number implements Comparable<Double> {

    private static final long serialVersionUID = -9172774392245257468L;

    /**
     * The value which the receiver represents.
     */
    private final double value;

    /**
     * Constant for the maximum {@code double} value, (2 - 2<sup>-52</sup>) *
     * 2<sup>1023</sup>.
     */
    public static final double MAX_VALUE = 1.79769313486231570e+308;

    /**
     * Constant for the minimum {@code double} value, 2<sup>-1074</sup>.
     */
    public static final double MIN_VALUE = 5e-324;

    /* 4.94065645841246544e-324 gets rounded to 9.88131e-324 */

    /**
     * Constant for the Not-a-Number (NaN) value of the {@code double} type.
     */
    public static final double NaN = 0.0 / 0.0;

    /**
     * Constant for the Positive Infinity value of the {@code double} type.
     */
    public static final double POSITIVE_INFINITY = 1.0 / 0.0;

    /**
     * Constant for the Negative Infinity value of the {@code double} type.
     */
    public static final double NEGATIVE_INFINITY = -1.0 / 0.0;

    /**
     * The {@link Class} object that represents the primitive type {@code
     * double}.
     *
     * @since 1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Double> TYPE
            = (Class<Double>) double[].class.getComponentType();

    // Note: This can't be set to "double.class", since *that* is
    // defined to be "java.lang.Double.TYPE";

    /**
     * Constant for the number of bits needed to represent a {@code double} in
     * two's complement form.
     *
     * @since 1.5
     */
    public static final int SIZE = 64;

    /**
     * Constructs a new {@code Double} with the specified primitive double
     * value.
     *
     * @param value
     *            the primitive double value to store in the new instance.
     */
    public Double(double value) {
        this.value = value;
    }

    /**
     * Constructs a new {@code Double} from the specified string.
     *
     * @param string
     *            the string representation of a double value.
     * @throws NumberFormatException
     *             if {@code string} can not be decoded into a double value.
     * @see #parseDouble(String)
     */
    public Double(String string) throws NumberFormatException {
        this(parseDouble(string));
    }

    /**
     * Compares this object to the specified double object to determine their
     * relative order. There are two special cases:
     * <ul>
     * <li>{@code Double.NaN} is equal to {@code Double.NaN} and it is greater
     * than any other double value, including {@code Double.POSITIVE_INFINITY};</li>
     * <li>+0.0d is greater than -0.0d</li>
     * </ul>
     *
     * @param object
     *            the double object to compare this object to.
     * @return a negative value if the value of this double is less than the
     *         value of {@code object}; 0 if the value of this double and the
     *         value of {@code object} are equal; a positive value if the value
     *         of this double is greater than the value of {@code object}.
     * @throws NullPointerException
     *             if {@code object} is {@code null}.
     * @see java.lang.Comparable
     * @since 1.2
     */
    public int compareTo(Double object) {
        return compare(value, object.value);
    }

    @Override
    public byte byteValue() {
        return (byte) value;
    }

    /**
     * Converts the specified double value to a binary representation conforming
     * to the IEEE 754 floating-point double precision bit layout. All
     * <em>Not-a-Number (NaN)</em> values are converted to a single NaN
     * representation ({@code 0x7ff8000000000000L}).
     *
     * @param value
     *            the double value to convert.
     * @return the IEEE 754 floating-point double precision representation of
     *         {@code value}.
     * @see #doubleToRawLongBits(double)
     * @see #longBitsToDouble(long)
     */
    public static native long doubleToLongBits(double value);

    /**
     * Converts the specified double value to a binary representation conforming
     * to the IEEE 754 floating-point double precision bit layout.
     * <em>Not-a-Number (NaN)</em> values are preserved.
     *
     * @param value
     *            the double value to convert.
     * @return the IEEE 754 floating-point double precision representation of
     *         {@code value}.
     * @see #doubleToLongBits(double)
     * @see #longBitsToDouble(long)
     */
    public static native long doubleToRawLongBits(double value);

    /**
     * Gets the primitive value of this double.
     *
     * @return this object's primitive value.
     */
    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Tests this double for equality with {@code object}.
     * To be equal, {@code object} must be an instance of {@code Double} and
     * {@code doubleToLongBits} must give the same value for both objects.
     * 
     * <p>Note that, unlike {@code ==}, {@code -0.0} and {@code +0.0} compare
     * unequal, and {@code NaN}s compare equal by this method.
     * 
     * @param object
     *            the object to compare this double with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Double}; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        return (object == this)
                || (object instanceof Double)
                && (doubleToLongBits(this.value) == doubleToLongBits(((Double) object).value));
    }

    @Override
    public float floatValue() {
        return (float) value;
    }

    @Override
    public int hashCode() {
        long v = doubleToLongBits(value);
        return (int) (v ^ (v >>> 32));
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    /**
     * Indicates whether this object represents an infinite value.
     *
     * @return {@code true} if the value of this double is positive or negative
     *         infinity; {@code false} otherwise.
     */
    public boolean isInfinite() {
        return isInfinite(value);
    }

    /**
     * Indicates whether the specified double represents an infinite value.
     *
     * @param d
     *            the double to check.
     * @return {@code true} if the value of {@code d} is positive or negative
     *         infinity; {@code false} otherwise.
     */
    public static boolean isInfinite(double d) {
        return (d == POSITIVE_INFINITY) || (d == NEGATIVE_INFINITY);
    }

    /**
     * Indicates whether this object is a <em>Not-a-Number (NaN)</em> value.
     *
     * @return {@code true} if this double is <em>Not-a-Number</em>;
     *         {@code false} if it is a (potentially infinite) double number.
     */
    public boolean isNaN() {
        return isNaN(value);
    }

    /**
     * Indicates whether the specified double is a <em>Not-a-Number (NaN)</em>
     * value.
     *
     * @param d
     *            the double value to check.
     * @return {@code true} if {@code d} is <em>Not-a-Number</em>;
     *         {@code false} if it is a (potentially infinite) double number.
     */
    public static boolean isNaN(double d) {
        return d != d;
    }

    /**
     * Converts the specified IEEE 754 floating-point double precision bit
     * pattern to a Java double value.
     *
     * @param bits
     *            the IEEE 754 floating-point double precision representation of
     *            a double value.
     * @return the double value converted from {@code bits}.
     * @see #doubleToLongBits(double)
     * @see #doubleToRawLongBits(double)
     */
    public static native double longBitsToDouble(long bits);

    @Override
    public long longValue() {
        return (long) value;
    }

    /**
     * Parses the specified string as a double value.
     *
     * @param string
     *            the string representation of a double value.
     * @return the primitive double value represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as a double value.
     */
    public static double parseDouble(String string)
            throws NumberFormatException {
        return org.apache.harmony.luni.util.FloatingPointParser
                .parseDouble(string);
    }

    @Override
    public short shortValue() {
        return (short) value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * specified double value.
     *
     * @param d
     *             the double to convert to a string.
     * @return a printable representation of {@code d}.
     */
    public static String toString(double d) {
        return org.apache.harmony.luni.util.NumberConverter.convert(d);
    }

    /**
     * Parses the specified string as a double value.
     *
     * @param string
     *            the string representation of a double value.
     * @return a {@code Double} instance containing the double value represented
     *         by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as a double value.
     * @see #parseDouble(String)
     */
    public static Double valueOf(String string) throws NumberFormatException {
        return parseDouble(string);
    }

    /**
     * Compares the two specified double values. There are two special cases:
     * <ul>
     * <li>{@code Double.NaN} is equal to {@code Double.NaN} and it is greater
     * than any other double value, including {@code Double.POSITIVE_INFINITY};</li>
     * <li>+0.0d is greater than -0.0d</li>
     * </ul>
     *
     * @param double1
     *            the first value to compare.
     * @param double2
     *            the second value to compare.
     * @return a negative value if {@code double1} is less than {@code double2};
     *         0 if {@code double1} and {@code double2} are equal; a positive
     *         value if {@code double1} is greater than {@code double2}.
     */
    public static int compare(double double1, double double2) {
        // Non-zero, non-NaN checking.
        if (double1 > double2) {
            return 1;
        }
        if (double2 > double1) {
            return -1;
        }
        if (double1 == double2 && 0.0d != double1) {
            return 0;
        }

        // NaNs are equal to other NaNs and larger than any other double
        if (isNaN(double1)) {
            if (isNaN(double2)) {
                return 0;
            }
            return 1;
        } else if (isNaN(double2)) {
            return -1;
        }

        // Deal with +0.0 and -0.0
        long d1 = doubleToRawLongBits(double1);
        long d2 = doubleToRawLongBits(double2);
        // The below expression is equivalent to:
        // (d1 == d2) ? 0 : (d1 < d2) ? -1 : 1
        return (int) ((d1 >> 63) - (d2 >> 63));
    }

    /**
     * Returns a {@code Double} instance for the specified double value.
     *
     * @param d
     *            the double value to store in the instance.
     * @return a {@code Double} instance containing {@code d}.
     * @since 1.5
     */
    public static Double valueOf(double d) {
        return new Double(d);
    }

    /**
     * Converts the specified double into its hexadecimal string representation.
     *
     * @param d
     *            the double to convert.
     * @return the hexadecimal string representation of {@code d}.
     * @since 1.5
     */
    public static String toHexString(double d) {
        /*
         * Reference: http://en.wikipedia.org/wiki/IEEE_754
         */
        if (d != d) {
            return "NaN"; //$NON-NLS-1$
        }
        if (d == POSITIVE_INFINITY) {
            return "Infinity"; //$NON-NLS-1$
        }
        if (d == NEGATIVE_INFINITY) {
            return "-Infinity"; //$NON-NLS-1$
        }

        long bitValue = doubleToLongBits(d);

        boolean negative = (bitValue & 0x8000000000000000L) != 0;
        // mask exponent bits and shift down
        long exponent = (bitValue & 0x7FF0000000000000L) >>> 52;
        // mask significand bits and shift up
        long significand = bitValue & 0x000FFFFFFFFFFFFFL;

        if (exponent == 0 && significand == 0) {
            return (negative ? "-0x0.0p0" : "0x0.0p0"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        StringBuilder hexString = new StringBuilder(10);
        if (negative) {
            hexString.append("-0x"); //$NON-NLS-1$
        } else {
            hexString.append("0x"); //$NON-NLS-1$
        }

        if (exponent == 0) { // denormal (subnormal) value
            hexString.append("0."); //$NON-NLS-1$
            // significand is 52-bits, so there can be 13 hex digits
            int fractionDigits = 13;
            // remove trailing hex zeros, so Integer.toHexString() won't print
            // them
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            // this assumes Integer.toHexString() returns lowercase characters
            String hexSignificand = Long.toHexString(significand);

            // if there are digits left, then insert some '0' chars first
            if (significand != 0 && fractionDigits > hexSignificand.length()) {
                int digitDiff = fractionDigits - hexSignificand.length();
                while (digitDiff-- != 0) {
                    hexString.append('0');
                }
            }
            hexString.append(hexSignificand);
            hexString.append("p-1022"); //$NON-NLS-1$
        } else { // normal value
            hexString.append("1."); //$NON-NLS-1$
            // significand is 52-bits, so there can be 13 hex digits
            int fractionDigits = 13;
            // remove trailing hex zeros, so Integer.toHexString() won't print
            // them
            while ((significand != 0) && ((significand & 0xF) == 0)) {
                significand >>>= 4;
                fractionDigits--;
            }
            // this assumes Integer.toHexString() returns lowercase characters
            String hexSignificand = Long.toHexString(significand);

            // if there are digits left, then insert some '0' chars first
            if (significand != 0 && fractionDigits > hexSignificand.length()) {
                int digitDiff = fractionDigits - hexSignificand.length();
                while (digitDiff-- != 0) {
                    hexString.append('0');
                }
            }

            hexString.append(hexSignificand);
            hexString.append('p');
            // remove exponent's 'bias' and convert to a string
            hexString.append(Long.toString(exponent - 1023));
        }
        return hexString.toString();
    }
=======
/************************************************************************
  Animation.java is part of Ti4j 3.1.0  Copyright 2013 Emitrom LLC

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
**************************************************************************/
package com.emitrom.ti4j.mobile.client.ui;

import com.emitrom.ti4j.core.client.ProxyObject;
import com.emitrom.ti4j.mobile.client.core.events.EventDispatcher;
import com.emitrom.ti4j.mobile.client.core.handlers.ui.AnimationCompleteHandler;
import com.emitrom.ti4j.mobile.client.core.handlers.ui.AnimationStartHandler;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * The animation object is used for specifying lower-level animation properties
 * and more low-level control of events during an animation.
 */
public class Animation extends EventDispatcher {

    public Animation() {
        createPeer();
    }

    private Animation(JavaScriptObject obj) {
        jsObj = obj;
    }

    /**
     * @return The property specifies if the animation should be replayed in
     *         reverse upon completion
     */
    public native boolean isAutoReverse() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.autoreverse;
    }-*/;

    public native void setAutoReverse(boolean value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.autoreverse = value;
    }-*/;

    /**
     * @return Value of the backgroundcolor property to change during animation
     */
    public native String getBackgroundColor() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.backgroundColor;
    }-*/;

    public native void setBackgroundColor(String value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.backgroundColor = value;
    }-*/;

    private void createPeer() {
        jsObj = UI.createAnimation();
    }

    /**
     * @return Value of the bottom property to change during animation
     */
    public native double getBottom() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.bottom;
    }-*/;

    public native void setBottom(double value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.bottom = value;
    }-*/;

    /**
     * @return Value of the center property to change during animation
     */
    public native Object getCenter() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.center;
    }-*/;

    public native void setCenter(Object value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.center = value;
    }-*/;

    /**
     * @return Value of the color property to change during animation
     */
    public native String getColor() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.color;
    }-*/;

    public native void setColor(String value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.color = value;
    }-*/;

    /**
     * @return The curve of the animation
     */
    public native int getCurve() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.curve;
    }-*/;

    public native void setCurve(int value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.curve = value;
    }-*/;

    /**
     * @return The duration of time in milliseconds before starting the
     *         animation
     */
    public native double getDelay() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.delay;
    }-*/;

    public native void setDelay(double value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.delay = value;
    }-*/;

    /**
     * @return The duration of time in milliseconds to perform the animation
     */
    public native double getDuration() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.duration;
    }-*/;

    public native void setDuration(double value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.duration = value;
    }-*/;

    /**
     * @return Value of the height property to change during animation
     */
    public native double getHeight() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.height;
    }-*/;

    public void setHeight(double value) {
    	setHeight("" + value);
    }
    public native void setHeight(String value) /*-{
	var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
	jso.height = value;
	}-*/;


    /**
     * @return Value of the left property to change during animation
     */
    public native double getLeft() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.left;
    }-*/;

    public native void setLeft(double value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.left = value;
    }-*/;
    
    public native void setLeft(String value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.left = value;
    }-*/;

    /**
     * @return Value of the opacity property to change during animation
     */
    public native double getOpacity() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.opacity;
    }-*/;

    public native void setOpacity(double value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.opacity = value;
    }-*/;

    /**
     * @return Value of the opaque property to change during animation
     */
    public native boolean isOpaque() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.opaque;
    }-*/;

    public native void setOpaque(boolean value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.opaque = value;
    }-*/;

    /**
     * @return The number of times the animation should be performed
     */
    public native int getRepeat() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.repeat;
    }-*/;

    public native void setRepeat(int value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.repeat = value;
    }-*/;

    /**
     * @return Value of the right property to change during animation
     */
    public native double getRight() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.right;
    }-*/;

    public native void setRight(double value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.right = value;
    }-*/;
    
    public native void setRight(String value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.right = value;
	}-*/;

    /**
     * @return Value of the top property to change during animation
     */
    public native double getTop() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.top;
    }-*/;

    public native void setTop(double value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.top = value;
    }-*/;

    public native void setTop(String value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.top = value;
    }-*/;

    /**
     * @return Value of the transform property to change during animation
     */
    public native Object getTransform() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.transform;
    }-*/;

    public native void setTransform(Object value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.transform = value;
    }-*/;

    /**
     * @return During a transition animation, jso is the constant to the type of
     *         transition to use
     */
    public native int getTransition() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.transition;
    }-*/;

    public native void setTransition(int value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.transition = value;
    }-*/;

    /**
     * @return Value of the visible property to change during animation
     */
    public native boolean isVisible() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.visible;
    }-*/;

    public native void setVisible(boolean value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.visible = value;
    }-*/;

    /**
     * @return Value of the width property to change during animation
     */
    public native double getWidth() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.width;
    }-*/;

    public native void setWidth(double value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.width = value;
    }-*/;

    public native void setWidth(String value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.width = value;
    }-*/;

    /**
     * @return Value of the zindex property to change during animation
     */
    public native int getZIndex() /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		return jso.zIndex;
    }-*/;

    public native void setZIndex(int value) /*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso.zIndex = value;
    }-*/;

    public native void addCompleteHandler(AnimationCompleteHandler handler)/*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso
				.addEventListener(
						@com.emitrom.ti4j.mobile.client.core.events.CompleteEvent::COMPLETE,
						function(e) {
							var eventObject = @com.emitrom.ti4j.mobile.client.core.events.CompleteEvent::new(Lcom/google/gwt/core/client/JavaScriptObject;)(e);
							handler.@com.emitrom.ti4j.mobile.client.core.handlers.ui.AnimationCompleteHandler::onComplete(Lcom/emitrom/ti4j/mobile/client/core/events/CompleteEvent;)(eventObject);
						});
    }-*/;

    public native void addStartHandler(AnimationStartHandler handler)/*-{
		var jso = this.@com.emitrom.ti4j.core.client.ProxyObject::getJsObj()();
		jso
				.addEventListener(
						@com.emitrom.ti4j.mobile.client.core.events.ui.UIEvent::START,
						function(e) {
							var eventObject = @com.emitrom.ti4j.mobile.client.core.events.ui.UIEvent::new(Lcom/google/gwt/core/client/JavaScriptObject;)(e);
							handler.@com.emitrom.ti4j.mobile.client.core.handlers.ui.AnimationStartHandler::onStart(Lcom/emitrom/ti4j/mobile/client/core/events/ui/UIEvent;)(eventObject);
						});
    }-*/;

    public static Animation from(ProxyObject proxy) {
        return new Animation(proxy.getJsObj());
    }

>>>>>>> 76aa07461566a5976980e6696204781271955163
}

