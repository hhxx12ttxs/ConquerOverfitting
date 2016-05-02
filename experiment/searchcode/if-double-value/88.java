<<<<<<< HEAD
/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.math;

import java.util.Random;

import com.ardor3d.math.type.ReadOnlyVector3;

public class MathUtils {

    /** A "close to zero" double epsilon value for use */
    public static final double EPSILON = 2.220446049250313E-16d;

    /** A "close to zero" double epsilon value for use */
    public static final double ZERO_TOLERANCE = 0.0001;

    public static final double ONE_THIRD = 1.0 / 3.0;

    /** The value PI as a double. (180 degrees) */
    public static final double PI = Math.PI;

    /** The value 2PI as a double. (360 degrees) */
    public static final double TWO_PI = 2.0 * PI;

    /** The value PI/2 as a double. (90 degrees) */
    public static final double HALF_PI = 0.5 * PI;

    /** The value PI/4 as a double. (45 degrees) */
    public static final double QUARTER_PI = 0.25 * PI;

    /** The value 1/PI as a double. */
    public static final double INV_PI = 1.0 / PI;

    /** The value 1/(2PI) as a double. */
    public static final double INV_TWO_PI = 1.0 / TWO_PI;

    /** A value to multiply a degree value by, to convert it to radians. */
    public static final double DEG_TO_RAD = PI / 180.0;

    /** A value to multiply a radian value by, to convert it to degrees. */
    public static final double RAD_TO_DEG = 180.0 / PI;

    /** A precreated random object for random numbers. */
    public static final Random rand = new Random(System.currentTimeMillis());

    /**
     * Fast Trig functions for x86. This forces the trig functiosn to stay within the safe area on the x86 processor
     * (-45 degrees to +45 degrees) The results may be very slightly off from what the Math and StrictMath trig
     * functions give due to rounding in the angle reduction but it will be very very close.
     * 
     * note: code from wiki posting on java.net by jeffpk
     */
    private static double reduceSinAngle(double radians) {
        radians %= TWO_PI; // put us in -2PI to +2PI space
        if (Math.abs(radians) > PI) { // put us in -PI to +PI space
            radians = radians - (TWO_PI);
        }
        if (Math.abs(radians) > HALF_PI) {// put us in -PI/2 to +PI/2 space
            radians = PI - radians;
        }

        return radians;
    }

    /**
     * Returns sine of a value.
     * 
     * note: code from wiki posting on java.net by jeffpk
     * 
     * @param dValue
     *            The value to sine, in radians.
     * @return The sine of dValue.
     * @see java.lang.Math#sin(double)
     */
    public static double sin(double dValue) {
        dValue = reduceSinAngle(dValue); // limits angle to between -PI/2 and +PI/2
        if (Math.abs(dValue) <= QUARTER_PI) {
            return Math.sin(dValue);
        }

        return Math.cos(HALF_PI - dValue);
    }

    /**
     * Returns cos of a value.
     * 
     * @param dValue
     *            The value to cosine, in radians.
     * @return The cosine of dValue.
     * @see java.lang.Math#cos(double)
     */
    public static double cos(final double dValue) {
        return sin(dValue + HALF_PI);
    }

    /**
     * Converts a point from Spherical coordinates to Cartesian (using positive Y as up) and stores the results in the
     * store var.
     */
    public static Vector3 sphericalToCartesian(final ReadOnlyVector3 sphereCoords, final Vector3 store) {
        final double a = sphereCoords.getX() * cos(sphereCoords.getZ());
        final double x = a * cos(sphereCoords.getY());
        final double y = sphereCoords.getX() * sin(sphereCoords.getZ());
        final double z = a * sin(sphereCoords.getY());

        return store.set(x, y, z);
    }

    /**
     * Converts a point from Cartesian coordinates (using positive Y as up) to Spherical and stores the results in the
     * store var. (Radius, Azimuth, Polar)
     */
    public static Vector3 cartesianToSpherical(final ReadOnlyVector3 cartCoords, final Vector3 store) {
        final double cartX = Double.compare(cartCoords.getX(), 0.0) == 0 ? EPSILON : cartCoords.getX();
        final double cartY = cartCoords.getY();
        final double cartZ = cartCoords.getZ();

        final double x = Math.sqrt((cartX * cartX) + (cartY * cartY) + (cartZ * cartZ));
        final double y = Math.atan(cartZ / cartX) + (Double.compare(cartX, 0.0) < 0 ? PI : 0);
        final double z = Math.asin(cartY / x);
        return store.set(x, y, z);
    }

    /**
     * Converts a point from Spherical coordinates to Cartesian (using positive Z as up) and stores the results in the
     * store var.
     */
    public static Vector3 sphericalToCartesianZ(final ReadOnlyVector3 sphereCoords, final Vector3 store) {
        final double a = sphereCoords.getX() * cos(sphereCoords.getZ());
        final double x = a * cos(sphereCoords.getY());
        final double y = a * sin(sphereCoords.getY());
        final double z = sphereCoords.getX() * sin(sphereCoords.getZ());

        return store.set(x, y, z);
    }

    /**
     * Converts a point from Cartesian coordinates (using positive Z as up) to Spherical and stores the results in the
     * store var. (Radius, Azimuth, Polar)
     */
    public static Vector3 cartesianZToSpherical(final ReadOnlyVector3 cartCoords, final Vector3 store) {
        final double cartX = Double.compare(cartCoords.getX(), 0.0) == 0 ? EPSILON : cartCoords.getX();
        final double cartY = cartCoords.getY();
        final double cartZ = cartCoords.getZ();

        final double x = Math.sqrt((cartX * cartX) + (cartY * cartY) + (cartZ * cartZ));
        final double y = Math.asin(cartY / x);
        final double z = Math.atan(cartZ / cartX) + (Double.compare(cartX, 0.0) < 0 ? PI : 0);
        return store.set(x, y, z);
    }

    /**
     * Returns true if the number is a power of 2 (2,4,8,16...)
     * 
     * A good implementation found on the Java boards. note: a number is a power of two if and only if it is the
     * smallest number with that number of significant bits. Therefore, if you subtract 1, you know that the new number
     * will have fewer bits, so ANDing the original number with anything less than it will give 0.
     * 
     * @param number
     *            The number to test.
     * @return True if it is a power of two.
     */
    public static boolean isPowerOfTwo(final int number) {
        return (number > 0) && (number & (number - 1)) == 0;
    }

    /**
     * @param number
     * @return the closest power of two to the given number.
     */
    public static int nearestPowerOfTwo(final int number) {
        return (int) Math.pow(2, Math.ceil(Math.log(number) / Math.log(2)));
    }

    /**
     * @param value
     * @param base
     * @return the logarithm of value with given base, calculated as log(value)/log(base) such that pow(base,
     *         return)==value
     */
    public static double log(final double value, final double base) {
        return Math.log(value) / Math.log(base);
    }

    /**
     * Sets the seed to use for "random" operations. The default is the current system milliseconds.
     * 
     * @param seed
     */
    public static void setRandomSeed(final long seed) {
        rand.setSeed(seed);
    }

    /**
     * Returns a random double between 0 and 1.
     * 
     * @return A random double between <tt>0.0</tt> (inclusive) to <tt>1.0</tt> (exclusive).
     */
    public static double nextRandomDouble() {
        return rand.nextDouble();
    }

    /**
     * Returns a random float between 0 and 1.
     * 
     * @return A random float between <tt>0.0f</tt> (inclusive) to <tt>1.0f</tt> (exclusive).
     */
    public static float nextRandomFloat() {
        return rand.nextFloat();
    }

    /**
     * @return A random int between Integer.MIN_VALUE and Integer.MAX_VALUE.
     */
    public static int nextRandomInt() {
        return rand.nextInt();
    }

    /**
     * Returns a random int between min and max.
     * 
     * @return A random int between <tt>min</tt> (inclusive) to <tt>max</tt> (inclusive).
     */
    public static int nextRandomInt(final int min, final int max) {
        return (int) (nextRandomFloat() * (max - min + 1)) + min;
    }

    /**
     * 
     * @param percent
     * @param startValue
     * @param endValue
     * @return
     */
    public static float lerp(final float percent, final float startValue, final float endValue) {
        if (startValue == endValue) {
            return startValue;
        }
        return ((1 - percent) * startValue) + (percent * endValue);
    }

    /**
     * 
     * @param percent
     * @param startValue
     * @param endValue
     * @return
     */
    public static double lerp(final double percent, final double startValue, final double endValue) {
        if (startValue == endValue) {
            return startValue;
        }
        return ((1 - percent) * startValue) + (percent * endValue);
    }

    /**
     * 
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param nearZ
     * @param farZ
     * @param store
     */
    public static void matrixFrustum(final double left, final double right, final double bottom, final double top,
            final double nearZ, final double farZ, final Matrix4 store) {
        final double x = (2.0 * nearZ) / (right - left);
        final double y = (2.0 * nearZ) / (top - bottom);
        final double a = (right + left) / (right - left);
        final double b = (top + bottom) / (top - bottom);
        final double c = -(farZ + nearZ) / (farZ - nearZ);
        final double d = -(2.0 * farZ * nearZ) / (farZ - nearZ);

        store.set(x, 0.0, 0.0, 0.0, 0.0, y, 0.0, 0.0, a, b, c, -1.0, 0.0, 0.0, d, 0.0);
    }

    /**
     * 
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param nearZ
     * @param farZ
     * @param store
     */
    public static void matrixOrtho(final double left, final double right, final double bottom, final double top,
            final double nearZ, final double farZ, final Matrix4 store) {
        store.set(2.0 / (right - left), 0.0, 0.0, 0.0, 0.0, 2.0 / (top - bottom), 0.0, 0.0, 0.0, 0.0, -2.0
                / (farZ - nearZ), 0.0, -(right + left) / (right - left), -(top + bottom) / (top - bottom),
                -(farZ + nearZ) / (farZ - nearZ), 1.0);
    }

    /**
     * 
     * @param fovY
     * @param aspect
     * @param zNear
     * @param zFar
     * @param store
     */
    public static void matrixPerspective(final double fovY, final double aspect, final double zNear, final double zFar,
            final Matrix4 store) {
        final double height = zNear * Math.tan(fovY * 0.5 * DEG_TO_RAD);
        final double width = height * aspect;

        matrixFrustum(-width, width, -height, height, zNear, zFar, store);
    }

    /**
     * 
     * @param position
     * @param target
     * @param up
     * @param store
     */
    public static void matrixLookAt(final ReadOnlyVector3 position, final ReadOnlyVector3 target,
            final ReadOnlyVector3 worldUp, final Matrix4 store) {
        final Vector3 direction = Vector3.fetchTempInstance();
        final Vector3 side = Vector3.fetchTempInstance();
        final Vector3 up = Vector3.fetchTempInstance();

        direction.set(target).subtractLocal(position).normalizeLocal();
        direction.cross(worldUp, side).normalizeLocal();
        side.cross(direction, up);

        store.set(side.getX(), up.getX(), -direction.getX(), 0.0, side.getY(), up.getY(), -direction.getY(), 0.0, side
                .getZ(), up.getZ(), -direction.getZ(), 0.0, side.getX() * -position.getX() + side.getY()
                * -position.getY() + side.getZ() * -position.getZ(), up.getX() * -position.getX() + up.getY()
                * -position.getY() + up.getZ() * -position.getZ(), -direction.getX() * -position.getX()
                + -direction.getY() * -position.getY() + -direction.getZ() * -position.getZ(), 1.0);
    }

    /**
     * 
     * @param position
     * @param target
     * @param up
     * @param store
     */
    public static void matrixLookAt(final ReadOnlyVector3 position, final ReadOnlyVector3 target,
            final ReadOnlyVector3 worldUp, final Matrix3 store) {
        final Vector3 direction = Vector3.fetchTempInstance();
        final Vector3 side = Vector3.fetchTempInstance();
        final Vector3 up = Vector3.fetchTempInstance();

        direction.set(target).subtractLocal(position).normalizeLocal();
        direction.cross(worldUp, side).normalizeLocal();
        side.cross(direction, up);

        store.set(side.getX(), up.getX(), -direction.getX(), side.getY(), up.getY(), -direction.getY(), side.getZ(), up
                .getZ(), -direction.getZ());
    }
}

=======
package org.codehaus.groovy.grails.web.json;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import org.apache.commons.lang.UnhandledException;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * A JSONArray is an ordered sequence of values. Its external text form is a
 * string wrapped in square brackets with commas separating the values. The
 * internal form is an object having <code>get</code> and <code>opt</code>
 * methods for accessing the values by index, and <code>put</code> methods for
 * adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p/>
 * The constructor can convert a JSON text into a Java object. The
 * <code>toString</code> method converts to JSON text.
 * <p/>
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p/>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coersion for you.
 * <p/>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * JSON syntax rules. The constructors are more forgiving in the texts they will
 * accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there
 * is <code>,</code>&nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces,
 * and if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers
 * and if they are not the reserved words <code>true</code>,
 * <code>false</code>, or <code>null</code>.</li>
 * <li>Values can be separated by <code>;</code> <small>(semicolon)</small> as
 * well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0-</code> <small>(octal)</small> or
 * <code>0x-</code> <small>(hex)</small> prefix.</li>
 * <li>Comments written in the slashshlash, slashstar, and hash conventions
 * will be ignored.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JSONArray implements JSONElement, List {


    /**
     * The arrayList where the JSONArray's properties are kept.
     */
    private ArrayList myArrayList;


    /**
     * Construct an empty JSONArray.
     */
    public JSONArray() {
        this.myArrayList = new ArrayList();
    }

    /**
     * Construct a JSONArray from a JSONTokener.
     *
     * @param x A JSONTokener
     * @throws JSONException If there is a syntax error.
     */
    public JSONArray(JSONTokener x) throws JSONException {
        this();
        if (x.nextClean() != '[') {
            throw x.syntaxError("A JSONArray text must start with '['");
        }
        if (x.nextClean() == ']') {
            return;
        }
        x.back();
        for (; ;) {
            if (x.nextClean() == ',') {
                x.back();
                this.myArrayList.add(null);
            } else {
                x.back();
                this.myArrayList.add(x.nextValue());
            }
            switch (x.nextClean()) {
                case ';':
                case ',':
                    if (x.nextClean() == ']') {
                        return;
                    }
                    x.back();
                    break;
                case ']':
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or ']'");
            }
        }
    }


    /**
     * Construct a JSONArray from a source sJSON text.
     *
     * @param string A string that begins with
     *               <code>[</code>&nbsp;<small>(left bracket)</small>
     *               and ends with <code>]</code>&nbsp;<small>(right bracket)</small>.
     * @throws JSONException If there is a syntax error.
     */
    public JSONArray(String string) throws JSONException {
        this(new JSONTokener(string));
    }


    /**
     * Construct a JSONArray from a Collection.
     *
     * @param collection A Collection.
     */
    public JSONArray(Collection collection) {
        this.myArrayList = new ArrayList(collection);
    }


    /**
     * Get the object value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return An object value.
     * @throws JSONException If there is no value for the index.
     */
    public Object get(int index) {
        Object o = opt(index);
        if (o == null) {
            throw new UnhandledException(new JSONException("JSONArray[" + index + "] not found."));
        }
        return o;
    }

    public Object set(int i, Object o) {
        return myArrayList.set(i, o);
    }

    public boolean add(Object o) {
        return myArrayList.add(o);
    }

    public void add(int i, Object o) {
        myArrayList.add(i, o);
    }

    public Object remove(int i) {
        return myArrayList.remove(i);
    }

    public boolean remove(Object o) {
        return myArrayList.remove(o);
    }

    public void clear() {
        myArrayList.clear();
    }

    public boolean addAll(Collection collection) {
        return myArrayList.addAll(collection);
    }

    public boolean addAll(int i, Collection collection) {
        return myArrayList.addAll(i, collection);
    }

    public Iterator iterator() {
        return myArrayList.iterator();
    }

    public ListIterator listIterator() {
        return myArrayList.listIterator();
    }

    public ListIterator listIterator(int i) {
        return myArrayList.listIterator(i);
    }

    public List subList(int i, int i1) {
        return myArrayList.subList(i, i1);
    }

    public boolean containsAll(Collection collection) {
        return myArrayList.containsAll(collection);
    }

    public boolean removeAll(Collection collection) {
        return myArrayList.removeAll(collection);
    }

    public boolean retainAll(Collection collection) {
        return myArrayList.retainAll(collection);
    }


    /**
     * Get the boolean value associated with an index.
     * The string values "true" and "false" are converted to boolean.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The truth.
     * @throws JSONException If there is no value for the index or if the
     *                       value is not convertable to boolean.
     */
    public boolean getBoolean(int index) throws JSONException {
        Object o = get(index);
        if (o.equals(Boolean.FALSE) ||
                (o instanceof String &&
                        ((String) o).equalsIgnoreCase("false"))) {
            return false;
        } else if (o.equals(Boolean.TRUE) ||
                (o instanceof String &&
                        ((String) o).equalsIgnoreCase("true"))) {
            return true;
        }
        throw new JSONException("JSONArray[" + index + "] is not a Boolean.");
    }


    /**
     * Get the double value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws JSONException If the key is not found or if the value cannot
     *                       be converted to a number.
     */
    public double getDouble(int index) throws JSONException {
        Object o = get(index);
        try {
            return o instanceof Number ?
                    ((Number) o).doubleValue() : Double.parseDouble((String) o);
        } catch (Exception e) {
            throw new JSONException("JSONArray[" + index +
                    "] is not a number.");
        }
    }


    /**
     * Get the int value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws JSONException If the key is not found or if the value cannot
     *                       be converted to a number.
     *                       if the value cannot be converted to a number.
     */
    public int getInt(int index) throws JSONException {
        Object o = get(index);
        return o instanceof Number ?
                ((Number) o).intValue() : (int) getDouble(index);
    }


    /**
     * Get the JSONArray associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A JSONArray value.
     * @throws JSONException If there is no value for the index. or if the
     *                       value is not a JSONArray
     */
    public JSONArray getJSONArray(int index) throws JSONException {
        Object o = get(index);
        if (o instanceof JSONArray) {
            return (JSONArray) o;
        }
        throw new JSONException("JSONArray[" + index +
                "] is not a JSONArray.");
    }


    /**
     * Get the JSONObject associated with an index.
     *
     * @param index subscript
     * @return A JSONObject value.
     * @throws JSONException If there is no value for the index or if the
     *                       value is not a JSONObject
     */
    public JSONObject getJSONObject(int index) throws JSONException {
        Object o = get(index);
        if (o instanceof JSONObject) {
            return (JSONObject) o;
        }
        throw new JSONException("JSONArray[" + index +
                "] is not a JSONObject.");
    }


    /**
     * Get the long value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     * @throws JSONException If the key is not found or if the value cannot
     *                       be converted to a number.
     */
    public long getLong(int index) throws JSONException {
        Object o = get(index);
        return o instanceof Number ?
                ((Number) o).longValue() : (long) getDouble(index);
    }


    /**
     * Get the string associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A string value.
     * @throws JSONException If there is no value for the index.
     */
    public String getString(int index) throws JSONException {
        return get(index).toString();
    }


    /**
     * Determine if the value is null.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return true if the value at the index is null, or if there is no value.
     */
    public boolean isNull(int index) {
        return JSONObject.NULL.equals(opt(index));
    }


    /**
     * Make a string from the contents of this JSONArray. The
     * <code>separator</code> string is inserted between each element.
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param separator A string that will be inserted between the elements.
     * @return a string.
     * @throws JSONException If the array contains an invalid number.
     */
    public String join(String separator) throws JSONException {
        int len = length();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < len; i += 1) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(JSONObject.valueToString(this.myArrayList.get(i)));
        }
        return sb.toString();
    }


    /**
     * Get the number of elements in the JSONArray, included nulls.
     *
     * @return The length (or size).
     */
    public int length() {
        return myArrayList.size();
    }


    /**
     * Get the optional object value associated with an index.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return An object value, or null if there is no
     *         object at that index.
     */
    public Object opt(int index) {
        return (index < 0 || index >= length()) ?
                null : this.myArrayList.get(index);
    }


    /**
     * Get the optional boolean value associated with an index.
     * It returns false if there is no value at that index,
     * or if the value is not Boolean.TRUE or the String "true".
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The truth.
     */
    public boolean optBoolean(int index) {
        return optBoolean(index, false);
    }


    /**
     * Get the optional boolean value associated with an index.
     * It returns the defaultValue if there is no value at that index or if
     * it is not a Boolean or the String "true" or "false" (case insensitive).
     *
     * @param index        The index must be between 0 and length() - 1.
     * @param defaultValue A boolean default.
     * @return The truth.
     */
    public boolean optBoolean(int index, boolean defaultValue) {
        try {
            return getBoolean(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get the optional double value associated with an index.
     * NaN is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     */
    public double optDouble(int index) {
        return optDouble(index, Double.NaN);
    }


    /**
     * Get the optional double value associated with an index.
     * The defaultValue is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index        subscript
     * @param defaultValue The default value.
     * @return The value.
     */
    public double optDouble(int index, double defaultValue) {
        try {
            return getDouble(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get the optional int value associated with an index.
     * Zero is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     */
    public int optInt(int index) {
        return optInt(index, 0);
    }


    /**
     * Get the optional int value associated with an index.
     * The defaultValue is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index        The index must be between 0 and length() - 1.
     * @param defaultValue The default value.
     * @return The value.
     */
    public int optInt(int index, int defaultValue) {
        try {
            return getInt(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get the optional JSONArray associated with an index.
     *
     * @param index subscript
     * @return A JSONArray value, or null if the index has no value,
     *         or if the value is not a JSONArray.
     */
    public JSONArray optJSONArray(int index) {
        Object o = opt(index);
        return o instanceof JSONArray ? (JSONArray) o : null;
    }


    /**
     * Get the optional JSONObject associated with an index.
     * Null is returned if the key is not found, or null if the index has
     * no value, or if the value is not a JSONObject.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A JSONObject value.
     */
    public JSONObject optJSONObject(int index) {
        Object o = opt(index);
        return o instanceof JSONObject ? (JSONObject) o : null;
    }


    /**
     * Get the optional long value associated with an index.
     * Zero is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return The value.
     */
    public long optLong(int index) {
        return optLong(index, 0);
    }


    /**
     * Get the optional long value associated with an index.
     * The defaultValue is returned if there is no value for the index,
     * or if the value is not a number and cannot be converted to a number.
     *
     * @param index        The index must be between 0 and length() - 1.
     * @param defaultValue The default value.
     * @return The value.
     */
    public long optLong(int index, long defaultValue) {
        try {
            return getLong(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get the optional string value associated with an index. It returns an
     * empty string if there is no value at that index. If the value
     * is not a string and is not null, then it is coverted to a string.
     *
     * @param index The index must be between 0 and length() - 1.
     * @return A String value.
     */
    public String optString(int index) {
        return optString(index, "");
    }


    /**
     * Get the optional string associated with an index.
     * The defaultValue is returned if the key is not found.
     *
     * @param index        The index must be between 0 and length() - 1.
     * @param defaultValue The default value.
     * @return A String value.
     */
    public String optString(int index, String defaultValue) {
        Object o = opt(index);
        return o != null ? o.toString() : defaultValue;
    }


    /**
     * Append a boolean value. This increases the array's length by one.
     *
     * @param value A boolean value.
     * @return this
     */
    public JSONArray put(boolean value) {
        put(value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }


    /**
     * Append a double value. This increases the array's length by one.
     *
     * @param value A double value.
     * @return this
     * @throws JSONException if the value is not finite.
     */
    public JSONArray put(double value) throws JSONException {
        Double d = Double.valueOf(value);
        JSONObject.testValidity(d);
        put(d);
        return this;
    }


    /**
     * Append an int value. This increases the array's length by one.
     *
     * @param value An int value.
     * @return this
     */
    public JSONArray put(int value) {
        put(Integer.valueOf(value));
        return this;
    }


    /**
     * Append an long value. This increases the array's length by one.
     *
     * @param value A long value.
     * @return this
     */
    public JSONArray put(long value) {
        put(Long.valueOf(value));
        return this;
    }


    /**
     * Append an object value. This increases the array's length by one.
     *
     * @param value An object value.  The value should be a
     *              Boolean, Double, Integer, JSONArray, JSObject, Long, or String, or the
     *              JSONObject.NULL object.
     * @return this
     */
    public JSONArray put(Object value) {
        this.myArrayList.add(value);
        return this;
    }


    /**
     * Put or replace a boolean value in the JSONArray. If the index is greater
     * than the length of the JSONArray, then null elements will be added as
     * necessary to pad it out.
     *
     * @param index The subscript.
     * @param value A boolean value.
     * @return this
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(int index, boolean value) throws JSONException {
        put(index, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }


    /**
     * Put or replace a double value. If the index is greater than the length of
     * the JSONArray, then null elements will be added as necessary to pad
     * it out.
     *
     * @param index The subscript.
     * @param value A double value.
     * @return this
     * @throws JSONException If the index is negative or if the value is
     *                       not finite.
     */
    public JSONArray put(int index, double value) throws JSONException {
        put(index, Double.valueOf(value));
        return this;
    }


    /**
     * Put or replace an int value. If the index is greater than the length of
     * the JSONArray, then null elements will be added as necessary to pad
     * it out.
     *
     * @param index The subscript.
     * @param value An int value.
     * @return this
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(int index, int value) throws JSONException {
        put(index, Integer.valueOf(value));
        return this;
    }


    /**
     * Put or replace a long value. If the index is greater than the length of
     * the JSONArray, then null elements will be added as necessary to pad
     * it out.
     *
     * @param index The subscript.
     * @param value A long value.
     * @return this
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(int index, long value) throws JSONException {
        put(index, Long.valueOf(value));
        return this;
    }


    /**
     * Put or replace an object value in the JSONArray. If the index is greater
     * than the length of the JSONArray, then null elements will be added as
     * necessary to pad it out.
     *
     * @param index The subscript.
     * @param value The value to put into the array.
     * @return this
     * @throws JSONException If the index is negative or if the the value is
     *                       an invalid number.
     */
    public JSONArray put(int index, Object value) throws JSONException {
        JSONObject.testValidity(value);
        if (index < 0) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        if (index < length()) {
            this.myArrayList.set(index, value);
        } else {
            while (index != length()) {
                put(null);
            }
            put(value);
        }
        return this;
    }


    /**
     * Produce a JSONObject by combining a JSONArray of names with the values
     * of this JSONArray.
     *
     * @param names A JSONArray containing a list of key strings. These will be
     *              paired with the values.
     * @return A JSONObject, or null if there are no names or if this JSONArray
     *         has no values.
     * @throws JSONException If any of the names are null.
     */
    public JSONObject toJSONObject(JSONArray names) throws JSONException {
        if (names == null || names.length() == 0 || length() == 0) {
            return null;
        }
        JSONObject jo = new JSONObject();
        for (int i = 0; i < names.length(); i += 1) {
            jo.put(names.getString(i), this.opt(i));
        }
        return jo;
    }


    /**
     * Make an JSON text of this JSONArray. For compactness, no
     * unnecessary whitespace is added. If it is not possible to produce a
     * syntactically correct JSON text then null will be returned instead. This
     * could occur if the array contains an invalid number.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, transmittable
     *         representation of the array.
     */
    @Override
    public String toString() {
        try {
            return '[' + join(",") + ']';
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Make a prettyprinted JSON text of this JSONArray.
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param indentFactor The number of spaces to add to each level of
     *                     indentation.
     * @return a printable, displayable, transmittable
     *         representation of the object, beginning
     *         with <code>[</code>&nbsp;<small>(left bracket)</small> and ending
     *         with <code>]</code>&nbsp;<small>(right bracket)</small>.
     * @throws JSONException
     */
    public String toString(int indentFactor) throws JSONException {
        return toString(indentFactor, 0);
    }


    /**
     * Make a prettyprinted JSON text of this JSONArray.
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param indentFactor The number of spaces to add to each level of
     *                     indentation.
     * @param indent       The indention of the top level.
     * @return a printable, displayable, transmittable
     *         representation of the array.
     * @throws JSONException
     */
    String toString(int indentFactor, int indent) throws JSONException {
        int len = length();
        if (len == 0) {
            return "[]";
        }
        int i;
        StringBuilder sb = new StringBuilder("[");
        if (len == 1) {
            sb.append(JSONObject.valueToString(this.myArrayList.get(0),
                    indentFactor, indent));
        } else {
            int newindent = indent + indentFactor;
            sb.append('\n');
            for (i = 0; i < len; i += 1) {
                if (i > 0) {
                    sb.append(",\n");
                }
                for (int j = 0; j < newindent; j += 1) {
                    sb.append(' ');
                }
                sb.append(JSONObject.valueToString(this.myArrayList.get(i),
                        indentFactor, newindent));
            }
            sb.append('\n');
            for (i = 0; i < indent; i += 1) {
                sb.append(' ');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * If the passed object is a JSONArray, then the underlying collection must be equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) return false;
        if (this == obj) return true;

        JSONArray that = (JSONArray) obj;

        if (myArrayList != null ? !myArrayList.equals(that.myArrayList) : that.myArrayList != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return  myArrayList != null ? myArrayList.hashCode() : super.hashCode();
    }

    /**
     * Write the contents of the JSONArray as JSON text to a writer.
     * For compactness, no whitespace is added.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return The writer.
     * @throws JSONException
     */
    public Writer write(Writer writer) throws JSONException {
        try {
            boolean b = false;
            int len = length();

            writer.write('[');

            for (int i = 0; i < len; i += 1) {
                if (b) {
                    writer.write(',');
                }
                Object v = this.myArrayList.get(i);
                if (v instanceof JSONObject) {
                    ((JSONObject) v).write(writer);
                } else if (v instanceof JSONArray) {
                    ((JSONArray) v).write(writer);
                } else {
                    writer.write(JSONObject.valueToString(v));
                }
                b = true;
            }
            writer.write(']');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    public void trimToSize() {
        myArrayList.trimToSize();
    }

    public void ensureCapacity(int i) {
        myArrayList.ensureCapacity(i);
    }

    public int size() {
        return myArrayList.size();
    }

    public boolean isEmpty() {
        return myArrayList.isEmpty();
    }

    public boolean contains(Object o) {
        return myArrayList.contains(o);
    }

    public int indexOf(Object o) {
        return myArrayList.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return myArrayList.lastIndexOf(o);
    }

    @Override
    public Object clone() {
        return myArrayList.clone();
    }

    public Object[] toArray() {
        return myArrayList.toArray();
    }

    public Object[] toArray(Object[] objects) {
        return myArrayList.toArray(objects);
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
