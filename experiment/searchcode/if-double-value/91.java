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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.ardor3d.math.type.ReadOnlyVector4;
import com.ardor3d.util.Debug;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.export.Savable;
import com.ardor3d.util.pool.ObjectPool;

/**
 * Vector4 represents a point or vector in a four dimensional system. This implementation stores its data in
 * double-precision.
 */
public class Vector4 implements Cloneable, Savable, Externalizable, ReadOnlyVector4 {

    private static final long serialVersionUID = 1L;

    private static final Vector4Pool VEC_POOL = new Vector4Pool(11);

    /**
     * 0, 0, 0, 0
     */
    public final static ReadOnlyVector4 ZERO = new Vector4(0, 0, 0, 0);

    /**
     * 1, 0, 0, 0
     */
    public final static ReadOnlyVector4 UNIT_X = new Vector4(1, 0, 0, 0);
    /**
     * 0, 1, 0, 0
     */
    public final static ReadOnlyVector4 UNIT_Y = new Vector4(0, 1, 0, 0);
    /**
     * 0, 0, 1, 0
     */
    public final static ReadOnlyVector4 UNIT_Z = new Vector4(0, 0, 1, 0);
    /**
     * 0, 0, 0, 1
     */
    public final static ReadOnlyVector4 UNIT_W = new Vector4(0, 0, 0, 1);
    /**
     * 1, 1, 1, 1
     */
    public final static ReadOnlyVector4 UNIT_XYZ = new Vector4(1, 1, 1, 1);

    protected double _x = 0;
    protected double _y = 0;
    protected double _z = 0;
    protected double _w = 0;

    /**
     * Constructs a new vector set to (0, 0, 0, 0).
     */
    public Vector4() {
        this(0, 0, 0, 0);
    }

    /**
     * Constructs a new vector set to the (x, y, z, w) values of the given source vector.
     * 
     * @param src
     */
    public Vector4(final ReadOnlyVector4 src) {
        this(src.getX(), src.getY(), src.getZ(), src.getW());
    }

    /**
     * Constructs a new vector set to (x, y, z, w).
     * 
     * @param x
     * @param y
     * @param z
     * @param w
     */
    public Vector4(final double x, final double y, final double z, final double w) {
        _x = x;
        _y = y;
        _z = z;
        _w = w;
    }

    public double getX() {
        return _x;
    }

    public double getY() {
        return _y;
    }

    public double getZ() {
        return _z;
    }

    public double getW() {
        return _w;
    }

    /**
     * @return x as a float, to decrease need for explicit casts.
     */
    public float getXf() {
        return (float) _x;
    }

    /**
     * @return y as a float, to decrease need for explicit casts.
     */
    public float getYf() {
        return (float) _y;
    }

    /**
     * @return z as a float, to decrease need for explicit casts.
     */
    public float getZf() {
        return (float) _z;
    }

    /**
     * @return w as a float, to decrease need for explicit casts.
     */
    public float getWf() {
        return (float) _w;
    }

    /**
     * @param index
     * @return x value if index == 0, y value if index == 1, z value if index == 2 or w value if index == 3
     * @throws IllegalArgumentException
     *             if index is not one of 0, 1, 2, 3.
     */
    public double getValue(final int index) {
        switch (index) {
            case 0:
                return getX();
            case 1:
                return getY();
            case 2:
                return getZ();
            case 3:
                return getW();
        }
        throw new IllegalArgumentException("index must be either 0, 1, 2 or 3");
    }

    /**
     * @param index
     *            which field index in this vector to set.
     * @param value
     *            to set to one of x, y, z or w.
     * @throws IllegalArgumentException
     *             if index is not one of 0, 1, 2, 3.
     * 
     *             if this vector is read only
     */
    public void setValue(final int index, final double value) {
        switch (index) {
            case 0:
                setX(value);
                return;
            case 1:
                setY(value);
                return;
            case 2:
                setZ(value);
                return;
            case 3:
                setW(value);
                return;
        }
        throw new IllegalArgumentException("index must be either 0, 1, 2 or 3");
    }

    /**
     * Stores the double values of this vector in the given double array.
     * 
     * @param store
     *            if null, a new double[4] array is created.
     * @return the double array
     * @throws NullPointerException
     *             if store is null.
     * @throws ArrayIndexOutOfBoundsException
     *             if store is not at least length 4.
     */
    public double[] toArray(double[] store) {
        if (store == null) {
            store = new double[4];
        }
        // do last first to ensure size is correct before any edits occur.
        store[3] = getW();
        store[2] = getZ();
        store[1] = getY();
        store[0] = getX();
        return store;
    }

    /**
     * Sets the first component of this vector to the given double value.
     * 
     * @param x
     */
    public void setX(final double x) {
        _x = x;
    }

    /**
     * Sets the second component of this vector to the given double value.
     * 
     * @param y
     */
    public void setY(final double y) {
        _y = y;
    }

    /**
     * Sets the third component of this vector to the given double value.
     * 
     * @param z
     */
    public void setZ(final double z) {
        _z = z;
    }

    /**
     * Sets the fourth component of this vector to the given double value.
     * 
     * @param w
     */
    public void setW(final double w) {
        _w = w;
    }

    /**
     * Sets the value of this vector to (x, y, z, w)
     * 
     * @param x
     * @param y
     * @param z
     * @param w
     * @return this vector for chaining
     */
    public Vector4 set(final double x, final double y, final double z, final double w) {
        setX(x);
        setY(y);
        setZ(z);
        setW(w);
        return this;
    }

    /**
     * Sets the value of this vector to the (x, y, z, w) values of the provided source vector.
     * 
     * @param source
     * @return this vector for chaining
     * @throws NullPointerException
     *             if source is null.
     */
    public Vector4 set(final ReadOnlyVector4 source) {
        setX(source.getX());
        setY(source.getY());
        setZ(source.getZ());
        setW(source.getW());
        return this;
    }

    /**
     * Sets the value of this vector to (0, 0, 0, 0)
     * 
     * @return this vector for chaining
     */
    public Vector4 zero() {
        return set(0, 0, 0, 0);
    }

    /**
     * Adds the given values to those of this vector and returns them in store.
     * 
     * @param x
     * @param y
     * @param z
     * @param w
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return (this.x + x, this.y + y, this.z + z, this.w + w)
     */
    public Vector4 add(final double x, final double y, final double z, final double w, final Vector4 store) {
        Vector4 result = store;
        if (result == null) {
            result = new Vector4();
        }

        return result.set(getX() + x, getY() + y, getZ() + z, getW() + w);
    }

    /**
     * Increments the values of this vector with the given x, y, z and w values.
     * 
     * @param x
     * @param y
     * @param z
     * @param w
     * @return this vector for chaining
     */
    public Vector4 addLocal(final double x, final double y, final double z, final double w) {
        return set(getX() + x, getY() + y, getZ() + z, getW() + w);
    }

    /**
     * Adds the values of the given source vector to those of this vector and returns them in store.
     * 
     * @param source
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return (this.x + source.x, this.y + source.y, this.z + source.z, this.w + source.w)
     * @throws NullPointerException
     *             if source is null.
     */
    public Vector4 add(final ReadOnlyVector4 source, final Vector4 store) {
        return add(source.getX(), source.getY(), source.getZ(), source.getW(), store);
    }

    /**
     * Increments the values of this vector with the x, y, z and w values of the given vector.
     * 
     * @param source
     * @return this vector for chaining
     * @throws NullPointerException
     *             if source is null.
     */
    public Vector4 addLocal(final ReadOnlyVector4 source) {
        return addLocal(source.getX(), source.getY(), source.getZ(), source.getW());
    }

    /**
     * Subtracts the given values from those of this vector and returns them in store.
     * 
     * @param x
     * @param y
     * @param z
     * @param w
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return (this.x - x, this.y - y, this.z - z, this.w - w)
     */
    public Vector4 subtract(final double x, final double y, final double z, final double w, final Vector4 store) {
        Vector4 result = store;
        if (result == null) {
            result = new Vector4();
        }

        return result.set(getX() - x, getY() - y, getZ() - z, getW() - w);
    }

    /**
     * Decrements the values of this vector by the given x, y, z and w values.
     * 
     * @param x
     * @param y
     * @param z
     * @param w
     * @return this vector for chaining
     */
    public Vector4 subtractLocal(final double x, final double y, final double z, final double w) {
        return set(getX() - x, getY() - y, getZ() - z, getW() - w);
    }

    /**
     * Subtracts the values of the given source vector from those of this vector and returns them in store.
     * 
     * @param source
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return (this.x - source.x, this.y - source.y, this.z - source.z, this.w - source.w)
     * @throws NullPointerException
     *             if source is null.
     */
    public Vector4 subtract(final ReadOnlyVector4 source, final Vector4 store) {
        return subtract(source.getX(), source.getY(), source.getZ(), source.getW(), store);
    }

    /**
     * Decrements the values of this vector by the x, y, z and w values from the given source vector.
     * 
     * @param source
     * @return this vector for chaining
     * @throws NullPointerException
     *             if source is null.
     */
    public Vector4 subtractLocal(final ReadOnlyVector4 source) {
        return subtractLocal(source.getX(), source.getY(), source.getZ(), source.getW());
    }

    /**
     * Multiplies the values of this vector by the given scalar value and returns the result in store.
     * 
     * @param scalar
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return a new vector (this.x * scalar, this.y * scalar, this.z * scalar, this.w * scalar)
     */
    public Vector4 multiply(final double scalar, final Vector4 store) {
        Vector4 result = store;
        if (result == null) {
            result = new Vector4();
        }

        return result.set(getX() * scalar, getY() * scalar, getZ() * scalar, getW() * scalar);
    }

    /**
     * Internally modifies the values of this vector by multiplying them each by the given scalar value.
     * 
     * @param scalar
     * @return this vector for chaining
     */
    public Vector4 multiplyLocal(final double scalar) {
        return set(getX() * scalar, getY() * scalar, getZ() * scalar, getW() * scalar);
    }

    /**
     * Multiplies the values of this vector by the given scalar value and returns the result in store.
     * 
     * @param scale
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return a new vector (this.x * scale.x, this.y * scale.y, this.z * scale.z, this.w * scale.w)
     */
    public Vector4 multiply(final ReadOnlyVector4 scale, final Vector4 store) {
        Vector4 result = store;
        if (result == null) {
            result = new Vector4();
        }

        return result.set(getX() * scale.getX(), getY() * scale.getY(), getZ() * scale.getZ(), getW() * scale.getW());
    }

    /**
     * Internally modifies the values of this vector by multiplying them each by the given scale values.
     * 
     * @param scale
     * @return this vector for chaining
     */
    public Vector4 multiplyLocal(final ReadOnlyVector4 scale) {
        return set(getX() * scale.getX(), getY() * scale.getY(), getZ() * scale.getZ(), getW() * scale.getW());
    }

    /**
     * Divides the values of this vector by the given scalar value and returns the result in store.
     * 
     * @param scalar
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return a new vector (this.x / scalar, this.y / scalar, this.z / scalar, this.w / scalar)
     */
    public Vector4 divide(final double scalar, final Vector4 store) {
        Vector4 result = store;
        if (result == null) {
            result = new Vector4();
        }

        return result.set(getX() / scalar, getY() / scalar, getZ() / scalar, getW() / scalar);
    }

    /**
     * Internally modifies the values of this vector by dividing them each by the given scalar value.
     * 
     * @param scalar
     * @return this vector for chaining
     * 
     * 
     * @throws ArithmeticException
     *             if scalar is 0
     */
    public Vector4 divideLocal(final double scalar) {
        final double invScalar = 1.0 / scalar;

        return set(getX() * invScalar, getY() * invScalar, getZ() * invScalar, getW() * invScalar);
    }

    /**
     * Divides the values of this vector by the given scale values and returns the result in store.
     * 
     * @param scale
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return a new vector (this.x / scale.x, this.y / scale.y, this.z / scale.z, this.w / scale.w)
     */
    public Vector4 divide(final ReadOnlyVector4 scale, final Vector4 store) {
        Vector4 result = store;
        if (result == null) {
            result = new Vector4();
        }

        return result.set(getX() / scale.getX(), getY() / scale.getY(), getZ() / scale.getZ(), getW() / scale.getW());
    }

    /**
     * Internally modifies the values of this vector by dividing them each by the given scale values.
     * 
     * @param scale
     * @return this vector for chaining
     */
    public Vector4 divideLocal(final ReadOnlyVector4 scale) {
        return set(getX() / scale.getX(), getY() / scale.getY(), getZ() / scale.getZ(), getW() / scale.getW());
    }

    /**
     * 
     * Internally modifies this vector by multiplying its values with a given scale value, then adding a given "add"
     * value.
     * 
     * @param scale
     *            the value to multiply this vector by.
     * @param add
     *            the value to add to the result
     * @return this vector for chaining
     */
    public Vector4 scaleAddLocal(final float scale, final Vector4 add) {
        _x = _x * scale + add.getX();
        _y = _y * scale + add.getY();
        _z = _z * scale + add.getZ();
        _w = _w * scale + add.getW();
        return this;
    }

    /**
     * Scales this vector by multiplying its values with a given scale value, then adding a given "add" value. The
     * result is store in the given store parameter.
     * 
     * @param scale
     *            the value to multiply by.
     * @param add
     *            the value to add
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return the store variable
     */
    public Vector4 scaleAdd(final double scale, final ReadOnlyVector4 add, final Vector4 store) {
        Vector4 result = store;
        if (result == null) {
            result = new Vector4();
        }

        result.setX(_x * scale + add.getX());
        result.setY(_y * scale + add.getY());
        result.setY(_z * scale + add.getZ());
        result.setY(_w * scale + add.getW());
        return result;
    }

    /**
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return same as multiply(-1, store)
     */
    public Vector4 negate(final Vector4 store) {
        return multiply(-1, store);
    }

    /**
     * @return same as multiplyLocal(-1)
     */
    public Vector4 negateLocal() {
        return multiplyLocal(-1);
    }

    /**
     * Creates a new unit length vector from this one by dividing by length. If the length is 0, (ie, if the vector is
     * 0, 0, 0, 0) then a new vector (0, 0, 0, 0) is returned.
     * 
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return a new unit vector (or 0, 0, 0, 0 if this unit is 0 length)
     */
    public Vector4 normalize(final Vector4 store) {
        final double length = length();
        if (Double.compare(length, 0.0) != 0) {
            return divide(length, store);
        }

        return store != null ? store.set(ZERO) : new Vector4(ZERO);
    }

    /**
     * Converts this vector into a unit vector by dividing it internally by its length. If the length is 0, (ie, if the
     * vector is 0, 0, 0, 0) then no action is taken.
     * 
     * @return this vector for chaining
     */
    public Vector4 normalizeLocal() {
        final double length = length();
        if (Double.compare(length, 0.0) != 0) {
            return divideLocal(length);
        }

        return this;
    }

    /**
     * Performs a linear interpolation between this vector and the given end vector, using the given scalar as a
     * percent. iow, if changeAmnt is closer to 0, the result will be closer to the current value of this vector and if
     * it is closer to 1, the result will be closer to the end value. The result is returned as a new vector object.
     * 
     * @param endVec
     * @param scalar
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return a new vector as described above.
     * @throws NullPointerException
     *             if endVec is null.
     */
    public Vector4 lerp(final ReadOnlyVector4 endVec, final double scalar, final Vector4 store) {
        Vector4 result = store;
        if (result == null) {
            result = new Vector4();
        }

        final double x = (1.0 - scalar) * getX() + scalar * endVec.getX();
        final double y = (1.0 - scalar) * getY() + scalar * endVec.getY();
        final double z = (1.0 - scalar) * getZ() + scalar * endVec.getZ();
        final double w = (1.0 - scalar) * getW() + scalar * endVec.getW();
        return result.set(x, y, z, w);
    }

    /**
     * Performs a linear interpolation between this vector and the given end vector, using the given scalar as a
     * percent. iow, if changeAmnt is closer to 0, the result will be closer to the current value of this vector and if
     * it is closer to 1, the result will be closer to the end value. The result is stored back in this vector.
     * 
     * @param endVec
     * @param scalar
     * @return this vector for chaining
     * 
     * 
     * @throws NullPointerException
     *             if endVec is null.
     */
    public Vector4 lerpLocal(final ReadOnlyVector4 endVec, final double scalar) {
        setX((1.0 - scalar) * getX() + scalar * endVec.getX());
        setY((1.0 - scalar) * getY() + scalar * endVec.getY());
        setZ((1.0 - scalar) * getZ() + scalar * endVec.getZ());
        setW((1.0 - scalar) * getW() + scalar * endVec.getW());
        return this;
    }

    /**
     * Performs a linear interpolation between the given begin and end vectors, using the given scalar as a percent.
     * iow, if changeAmnt is closer to 0, the result will be closer to the begin value and if it is closer to 1, the
     * result will be closer to the end value. The result is returned as a new vector object.
     * 
     * @param beginVec
     * @param endVec
     * @param scalar
     *            the scalar as a percent.
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return a new vector as described above.
     * @throws NullPointerException
     *             if beginVec or endVec are null.
     */
    public static Vector4 lerp(final ReadOnlyVector4 beginVec, final ReadOnlyVector4 endVec, final double scalar,
            final Vector4 store) {
        Vector4 result = store;
        if (result == null) {
            result = new Vector4();
        }

        final double x = (1.0 - scalar) * beginVec.getX() + scalar * endVec.getX();
        final double y = (1.0 - scalar) * beginVec.getY() + scalar * endVec.getY();
        final double z = (1.0 - scalar) * beginVec.getZ() + scalar * endVec.getZ();
        final double w = (1.0 - scalar) * beginVec.getW() + scalar * endVec.getW();
        return result.set(x, y, z, w);
    }

    /**
     * Performs a linear interpolation between the given begin and end vectors, using the given scalar as a percent.
     * iow, if changeAmnt is closer to 0, the result will be closer to the begin value and if it is closer to 1, the
     * result will be closer to the end value. The result is stored back in this vector.
     * 
     * @param beginVec
     * @param endVec
     * @param changeAmnt
     *            the scalar as a percent.
     * @return this vector for chaining
     * 
     * 
     * @throws NullPointerException
     *             if beginVec or endVec are null.
     */
    public Vector4 lerpLocal(final ReadOnlyVector4 beginVec, final ReadOnlyVector4 endVec, final double scalar) {
        setX((1.0 - scalar) * beginVec.getX() + scalar * endVec.getX());
        setY((1.0 - scalar) * beginVec.getY() + scalar * endVec.getY());
        setZ((1.0 - scalar) * beginVec.getZ() + scalar * endVec.getZ());
        setW((1.0 - scalar) * beginVec.getW() + scalar * endVec.getW());
        return this;
    }

    /**
     * @return the magnitude or distance between the origin (0, 0, 0, 0) and the point described by this vector (x, y,
     *         z, w). Effectively the square root of the value returned by {@link #lengthSquared()}.
     */
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /**
     * @return the squared magnitude or squared distance between the origin (0, 0, 0, 0) and the point described by this
     *         vector (x, y, z, w)
     */
    public double lengthSquared() {
        return getX() * getX() + getY() * getY() + getZ() * getZ() + getW() * getW();
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param w
     * @return the squared distance between the point described by this vector and the given x, y, z, w point. When
     *         comparing the relative distance between two points it is usually sufficient to compare the squared
     *         distances, thus avoiding an expensive square root operation.
     */
    public double distanceSquared(final double x, final double y, final double z, final double w) {
        final double dx = getX() - x;
        final double dy = getY() - y;
        final double dz = getZ() - z;
        final double dw = getW() - w;
        return dx * dx + dy * dy + dz * dz + dw * dw;
    }

    /**
     * @param destination
     * @return the squared distance between the point described by this vector and the given destination point. When
     *         comparing the relative distance between two points it is usually sufficient to compare the squared
     *         distances, thus avoiding an expensive square root operation.
     * @throws NullPointerException
     *             if destination is null.
     */
    public double distanceSquared(final ReadOnlyVector4 destination) {
        return distanceSquared(destination.getX(), destination.getY(), destination.getZ(), destination.getW());
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param w
     * @return the distance between the point described by this vector and the given x, y, z, w point.
     */
    public double distance(final double x, final double y, final double z, final double w) {
        return Math.sqrt(distanceSquared(x, y, z, w));
    }

    /**
     * @param destination
     * @return the distance between the point described by this vector and the given destination point.
     * @throws NullPointerException
     *             if destination is null.
     */
    public double distance(final ReadOnlyVector4 destination) {
        return Math.sqrt(distanceSquared(destination));
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param w
     * @return the dot product of this vector with the given x, y, z, w values.
     */
    public double dot(final double x, final double y, final double z, final double w) {
        return (getX() * x) + (getY() * y) + (getZ() * z) + (getW() * w);
    }

    /**
     * @param vec
     * @return the dot product of this vector with the x, y, z, w values of the given vector.
     * @throws NullPointerException
     *             if vec is null.
     */
    public double dot(final ReadOnlyVector4 vec) {
        return dot(vec.getX(), vec.getY(), vec.getZ(), vec.getW());
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param w
     * @return the determinate of this vector with the given x, y, z, w values.
     */
    public double determinant(final double x, final double y, final double z, final double w) {
        return (getX() * x) - (getY() * y) - (getZ() * z) - (getW() * w);
    }

    /**
     * @param vec
     * @return the determinate of this vector with the x, y, z, w values of the given vector.
     * @throws NullPointerException
     *             if destination is null.
     */
    public double determinant(final ReadOnlyVector4 vec) {
        return determinant(vec.getX(), vec.getY(), vec.getZ(), vec.getW());
    }

    /**
     * Check a vector... if it is null or its doubles are NaN or infinite, return false. Else return true.
     * 
     * @param vector
     *            the vector to check
     * @return true or false as stated above.
     */
    public static boolean isValid(final ReadOnlyVector4 vector) {
        if (vector == null) {
            return false;
        }
        if (Double.isNaN(vector.getX()) || Double.isNaN(vector.getY()) || Double.isNaN(vector.getZ())
                || Double.isNaN(vector.getW())) {
            return false;
        }
        if (Double.isInfinite(vector.getX()) || Double.isInfinite(vector.getY()) || Double.isInfinite(vector.getZ())
                || Double.isInfinite(vector.getW())) {
            return false;
        }
        return true;
    }

    /**
     * @return the string representation of this vector.
     */
    @Override
    public String toString() {
        return "com.ardor3d.math.Vector4 [X=" + getX() + ", Y=" + getY() + ", Z=" + getZ() + ", W=" + getW() + "]";
    }

    /**
     * @return returns a unique code for this vector object based on its values. If two vectors are numerically equal,
     *         they will return the same hash code value.
     */
    @Override
    public int hashCode() {
        int result = 17;

        final long x = Double.doubleToLongBits(getX());
        result += 31 * result + (int) (x ^ (x >>> 32));

        final long y = Double.doubleToLongBits(getY());
        result += 31 * result + (int) (y ^ (y >>> 32));

        final long z = Double.doubleToLongBits(getZ());
        result += 31 * result + (int) (z ^ (z >>> 32));

        final long w = Double.doubleToLongBits(getW());
        result += 31 * result + (int) (w ^ (w >>> 32));

        return result;
    }

    /**
     * @param o
     *            the object to compare for equality
     * @return true if this vector and the provided vector have the same x, y, z and w values.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReadOnlyVector4)) {
            return false;
        }
        final ReadOnlyVector4 comp = (ReadOnlyVector4) o;
        if (Double.compare(getX(), comp.getX()) == 0 && Double.compare(getY(), comp.getY()) == 0
                && Double.compare(getZ(), comp.getZ()) == 0 && Double.compare(getW(), comp.getW()) == 0) {
            return true;
        }
        return false;
    }

    // /////////////////
    // Method for Cloneable
    // /////////////////

    @Override
    public Vector4 clone() {
        try {
            return (Vector4) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new AssertionError(); // can not happen
        }
    }

    // /////////////////
    // Methods for Savable
    // /////////////////

    public Class<? extends Vector4> getClassTag() {
        return this.getClass();
    }

    public void write(final Ardor3DExporter e) throws IOException {
        final OutputCapsule capsule = e.getCapsule(this);
        capsule.write(getX(), "x", 0);
        capsule.write(getY(), "y", 0);
        capsule.write(getZ(), "z", 0);
        capsule.write(getW(), "w", 0);
    }

    public void read(final Ardor3DImporter e) throws IOException {
        final InputCapsule capsule = e.getCapsule(this);
        setX(capsule.readDouble("x", 0));
        setY(capsule.readDouble("y", 0));
        setZ(capsule.readDouble("z", 0));
        setW(capsule.readDouble("w", 0));
    }

    // /////////////////
    // Methods for Externalizable
    // /////////////////

    /**
     * Used with serialization. Not to be called manually.
     * 
     * @param in
     *            ObjectInput
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        setX(in.readDouble());
        setY(in.readDouble());
        setZ(in.readDouble());
        setW(in.readDouble());
    }

    /**
     * Used with serialization. Not to be called manually.
     * 
     * @param out
     *            ObjectOutput
     * @throws IOException
     */
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeDouble(getX());
        out.writeDouble(getY());
        out.writeDouble(getZ());
        out.writeDouble(getW());
    }

    // /////////////////
    // Methods for creating temp variables (pooling)
    // /////////////////

    /**
     * @return An instance of Vector4 that is intended for temporary use in calculations and so forth. Multiple calls to
     *         the method should return instances of this class that are not currently in use.
     */
    public final static Vector4 fetchTempInstance() {
        if (Debug.useMathPools) {
            return VEC_POOL.fetch();
        } else {
            return new Vector4();
        }
    }

    /**
     * Releases a Vector4 back to be used by a future call to fetchTempInstance. TAKE CARE: this Vector4 object should
     * no longer have other classes referencing it or "Bad Things" will happen.
     * 
     * @param vec
     *            the Vector4 to release.
     */
    public final static void releaseTempInstance(final Vector4 vec) {
        if (Debug.useMathPools) {
            VEC_POOL.release(vec);
        }
    }

    static final class Vector4Pool extends ObjectPool<Vector4> {
        public Vector4Pool(final int initialSize) {
            super(initialSize);
        }

        @Override
        protected Vector4 newInstance() {
            return new Vector4();
        }
    }

=======
/*
 * $Id: JGraph.java,v 1.92 2009/04/06 18:43:24 david Exp $
 *
 * Copyright (c) 2001-2008 Gaudenz Alder
 *
 */
package org.jgraph;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.accessibility.Accessible;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.event.GraphLayoutCacheEvent.GraphLayoutCacheChange;
import org.jgraph.event.GraphModelEvent.GraphModelChange;
import org.jgraph.graph.AbstractCellView;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultGraphSelectionModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphSelectionModel;
import org.jgraph.graph.PortView;
import org.jgraph.plaf.GraphUI;
import org.jgraph.plaf.basic.BasicGraphUI;

/**
 * A control that displays a network of related objects using the well-known
 * paradigm of a graph.
 * <p>
 * A JGraph object doesn't actually contain your data; it simply provides a view
 * of the data. Like any non-trivial Swing component, the graph gets data by
 * querying its data model.
 * <p>
 * JGraph displays its data by drawing individual elements. Each element
 * displayed by the graph contains exactly one item of data, which is called a
 * cell. A cell may either be a vertex or an edge. Vertices may have neighbours
 * or not, and edges may have source and target vertices or not, depending on
 * whether they are connected.
 * <p>
 * <strong>Creating a Graph </strong>
 * <p>
 * The following code creates a JGraph object:
 * <p>
 * JGraph graph = new JGraph(); <br>
 * ... <br>
 * JScrollPane graphLayoutCache = new JScrollPane(graph)
 * <p>
 * The code creates an instance of JGraph and puts it in a scroll pane. JGraphs
 * constructor is called with no arguments in this example, which causes the
 * constructor to create a sample model.
 * <p>
 * <strong>Editing </strong>
 * <p>
 * Outmoved, cloned, resized, and shaped, or connected/disconnected to or from
 * other cells.
 * <p>
 * <strong>Keyboard Bindings </strong>
 * <p>
 * JGraph defines the following set of keyboard bindings:
 * <p>
 * <ul>
 * <li>Alt-Click forces marquee selection if over a cell.
 * <li>Shift- or Ctrl-Select extends or toggles the selection.
 * <li>Shift-Drag constrains the offset to one direction.
 * <li>Ctrl-Drag clones the selection.
 * <li>Doubleclick/F2 starts editing a cell.
 * </ul>
 * You can change the number of clicks that triggers editing using
 * setEditClickCount().
 * <p>
 * <strong>Customization </strong>
 * <p>
 * There are a number of additional methods that customize JGraph. For example,
 * setMinimumMove() defines the minimum amount of pixels before a move operation
 * is initiated. setSnapSize() defines the maximum distance for a cell to be
 * selected. setFloatEnabled() enables/disables port floating.
 * <p>
 * With setDisconnectOnMove() you can indicate if the selected subgraph should
 * be disconnected from the unselected rest when a move operation is initiated.
 * setDragEnabled() enables/disables the use of Drag And Drop, and
 * setDropEnabled() sets if the graph accepts Drops from external sources.
 * <p>
 * <strong>Customizing a graphs display </strong>
 * <p>
 * JGraph performs some look-and-feel specific painting. You can customize this
 * painting in a limited way. For example, you can modify the grid using
 * setGridColor() and setGridSize(), and you can change the handle colors using
 * setHandleColor() and setLockedHandleColor().
 * <p>
 * If you want finer control over the rendering, you can subclass one of the
 * default renderers, and extend its paint()-method. A renderer is a
 * Component-extension that paints a cell based on its attributes. Thus, neither
 * the JGraph nor its look-and-feel-specific implementation actually contain the
 * code that paints the cell. Instead, the graph uses the cell renderers
 * painting code.
 * <p>
 * <strong>Selection </strong>
 * <p>
 * Apart from the single-cell and marquee-selection, JGraphs selection model
 * also allows to "step-into" groups, and select children. This feature can be
 * disabled using the setAllowsChildSelection() method of the selection model
 * instance.
 * <p>
 * If you are interested in knowing when the selection changes implement the
 * <code>GraphSelectionListener</code> interface and add the instance using
 * the method <code>addGraphSelectionListener</code>.
 * <code>valueChanged</code> will be invoked when the selection changes, that
 * is if the user clicks twice on the same vertex <code>valueChanged</code>
 * will only be invoked once.
 * <p>
 * <strong>Change Notification </strong>
 * <p>
 * For detection of double-clicks or when a user clicks on a cell, regardless of
 * whether or not it was selected, I recommend you implement a MouseListener and
 * use <code>getFirstCellForLocation</code>.
 * <p>
 * <strong>Undo Support </strong>
 * <p>
 * To enable Undo-Support, a <code>GraphUndoManager</code> must be added using
 * <code>addGraphSelectionListener</code>. The GraphUndoManager is an
 * extension of Swing's <code>GraphUndoManager</code> that maintains a command
 * history in the context of multiple views. In this setup, a cell may have a
 * set of attributes in each view attached to the model.
 * <p>
 * For example, consider a position that is stored separately in each view. If a
 * node is inserted, the change will be visible in all attached views, resulting
 * in a new node that pops-up at the initial position. If the node is
 * subsequently moved, say, in view1, this does not constitute a change in
 * view2. If view2 does an "undo", the move <i>and </i> the insertion must be
 * undone, whereas an "undo" in view1 will only undo the previous move
 * operation.
 * <p>
 * Like all <code>JComponent</code> classes, you can use
 * {@link javax.swing.InputMap}and {@link javax.swing.ActionMap}to associate
 * an {@link javax.swing.Action}object with a {@link javax.swing.KeyStroke}and
 * execute the action under specified conditions.
 * 
 * @author Gaudenz Alder
 * @version 2.1 16/03/03
 * 
 */
public class JGraph extends JComponent implements Scrollable, Accessible, Serializable {

	public static final String VERSION = "JGraph (v5.12.4.0)";

	public static final int DOT_GRID_MODE = 0;

	public static final int CROSS_GRID_MODE = 1;

	public static final int LINE_GRID_MODE = 2;

	// Turn off XOR painting on MACs since it doesn't work
	public static boolean IS_MAC = false;

	static {
		try {
			String osName = System.getProperty("os.name");
			if (osName != null) {
				IS_MAC = osName.toLowerCase().startsWith("mac os x");
			}
			String javaVersion = System.getProperty("java.version");
			if (javaVersion.startsWith("1.4") || javaVersion.startsWith("1.5")) {
				// TODO different double buffering for 1.6 JVM?
			}
		} catch (Exception e) {
			// ignore
		}
	}

	/**
	 * @see #getUIClassID
	 * @see #readObject
	 */
	private static final String uiClassID = "GraphUI";

	/** Creates a new event and passes it off the <code>selectionListeners</code>. */
	protected transient GraphSelectionRedirector selectionRedirector;

	//
	// Bound Properties
	//
	/**
	 * The model that defines the graph displayed by this object. Bound
	 * property.
	 */
	transient protected GraphModel graphModel;

	/**
	 * The view that defines the display properties of the model. Bound
	 * property.
	 */
	transient protected GraphLayoutCache graphLayoutCache;

	/** Models the set of selected objects in this graph. Bound property. */
	transient protected GraphSelectionModel selectionModel;

	/** Handler for marquee selection. */
	transient protected BasicMarqueeHandler marquee;

	/** Off screen image for double buffering */
	protected transient Image offscreen;

	/** The bounds of the offscreen buffer */
	protected transient Rectangle2D offscreenBounds;
	
	/** The offset of the offscreen buffer */
	protected transient Point2D offscreenOffset;

	/** Graphics object of off screen image */
	protected transient Graphics offgraphics;
	
	/** Whether or not the current background image is correct */
	protected transient Rectangle2D offscreenDirty = null;
	
	protected transient boolean wholeOffscreenDirty = false;
	
	protected transient double wholeOffscreenDirtyProportion = 0.8;

	/**
	 * The buffer around the offscreen graphics object that provides the
	 * specified distance of scrolling before the buffer has to be redrawn.
	 * Increasing the value means fewer redraws but more memory is required.
	 */
	protected transient int offscreenBuffer = 0;
	
	/**
	 * Whether or not to try to use a volatile offscreen buffer for double
	 * buffering. Volatile 
	 */
	protected boolean volatileOffscreen = false;
	
	/** Stores whether the last double buffer allocation worked or not */
	protected boolean lastBufferAllocated = true;

	/** Holds the background image. */
	protected ImageIcon backgroundImage;

	/** A Component responsible for drawing the background image, if any */
	protected Component backgroundComponent;

	/** Whether or not the background image is scaled on zooming */
	protected boolean backgroundScaled = true;

	/** Scale of the graph. Default is 1. Bound property. */
	protected double scale = 1.0;

	/** True if the graph is anti-aliased. Default is false. Bound property. */
	protected boolean antiAliased = false;

	/** True if the graph allows editing the value of a cell. Bound property. */
	protected boolean editable = true;

	/** True if the graph allows editing of non-leaf cells. Bound property. */
	protected boolean groupsEditable = false;
	
	/**
	 * True if the graph allows selection of cells. Note: You must also disable
	 * selectNewCells if you disable this. Bound property.
	 */
	protected boolean selectionEnabled = true;

	/**
	 * True if the graph allows invalid null ports during previews (aka flip
	 * back edges). Default is true.
	 */
	protected boolean previewInvalidNullPorts = true;

	/** True if the grid is visible. Bound property. */
	protected boolean gridVisible = false;

	/** The size of the grid in points. Default is 10. Bound property. */
	protected double gridSize = 10;

	/** The style of the grid. Use one of the _GRID_MODE constants. */
	protected int gridMode = DOT_GRID_MODE;

	/** True if the ports are visible. Bound property. */
	protected boolean portsVisible = false;

	/** True if the ports are scaled. Bound property. */
	protected boolean portsScaled = true;

	/** True if port are painted above all other cells. */
	protected boolean portsOnTop = true;
	
	/** True if the graph allows to move cells below zero. */
	protected boolean moveBelowZero = false;
	
	/** True if the graph allows to move cells beyond the graph bounds */
	protected boolean moveBeyondGraphBounds  = true;

	/** True if the labels on edges may be moved. */
	protected boolean edgeLabelsMovable = true;

	/**
	 * True if the graph should be auto resized when cells are moved below the
	 * bottom right corner. Default is true.
	 */
	protected boolean autoResizeGraph = true;

	//
	// Look-And-Feel dependent
	//
	/** Highlight Color. This color is used to draw the selection border of
	 * unfocused cells. Changes when the Look-and-Feel changes. */
	protected Color highlightColor = Color.green;

	/**
	 * Color of the handles and locked handles. Changes when the Look-and-Feel
	 * changes. This color is also used to draw the selection border
	 * of focused cells.
	 */
	protected Color handleColor, lockedHandleColor;

	/** Color of the marquee. Changes when the Look-and-Feel changes. */
	protected Color marqueeColor;

	/** The color of the grid. This color is used to draw the selection border
	 * for cells with selected children. Changes when the Look-and-Feel changes. */
	protected Color gridColor;

	//
	// Datatransfer
	//
	/**
	 * True if Drag-and-Drop should be used for move operations. Default is
	 * false due to a JDK bug.
	 */
	protected boolean dragEnabled = false;

	/**
	 * True if the graph accepts transfers from other components (graphs). This
	 * also affects the clipboard. Default is true.
	 */
	protected boolean dropEnabled = true;

	/**
	 * True if the graph accepts transfers from other components (graphs). This
	 * also affects the clipboard. Default is true.
	 */
	protected boolean xorEnabled = !IS_MAC;

	//
	// Unbound Properties
	//
	/** Number of clicks for editing to start. Default is 2 clicks. */
	protected int editClickCount = 2;

	/** True if the graph allows interactions. Default is true. */
	protected boolean enabled = true;

	/** True if the snap method should be active (snap to grid). */
	protected boolean gridEnabled = false;

	/** Size of a handle. Default is 3 pixels. */
	protected int handleSize = 3;

	/** Maximum distance between a cell and the mousepointer. Default is 4. */
	protected int tolerance = 4;

	/** Minimum amount of pixels to start a move transaction. Default is 5. */
	protected int minimumMove = 5;

	/**
	 * True if getPortViewAt should return the default port if no other port is
	 * found. Default is false.
	 */
	protected boolean isJumpToDefaultPort = false;

	/**
	 * Specifies if cells should be added to a group when moved over the group's
	 * area. Default is false.
	 */
	protected boolean isMoveIntoGroups = false;

	/**
	 * Specifies if cells should be removed from groups when removed from the
	 * group area. Default is false.
	 */
	protected boolean isMoveOutOfGroups = false;

	/**
	 * True if selected edges are disconnected from unselected vertices on move.
	 * Default is false.
	 */
	protected boolean disconnectOnMove = false;

	/** True if the graph allows move operations. Default is true. */
	protected boolean moveable = true;

	/** True if the graph allows "ctrl-drag" operations. Default is false. */
	protected boolean cloneable = false;

	/** True if the graph allows cells to be resized. Default is true. */
	protected boolean sizeable = true;

	/**
	 * True if the graph allows points to be modified/added/removed. Default is
	 * true.
	 */
	protected boolean bendable = true;

	/**
	 * True if the graph allows new connections to be established. Default is
	 * true.
	 */
	protected boolean connectable = true;

	/**
	 * True if the graph allows existing connections to be removed. Default is
	 * true.
	 */
	protected boolean disconnectable = true;

	/**
	 * If true, when editing is to be stopped by way of selection changing, data
	 * in graph changing or other means <code>stopCellEditing</code> is
	 * invoked, and changes are saved. If false, <code>cancelCellEditing</code>
	 * is invoked, and changes are discarded.
	 */
	protected boolean invokesStopCellEditing;

	//
	// Bound propery names
	//
	/**
	 * Bound property name for <code>graphModel</code>.
	 */
	public final static String GRAPH_MODEL_PROPERTY = "model";

	/**
	 * Bound property name for <code>graphModel</code>.
	 */
	public final static String GRAPH_LAYOUT_CACHE_PROPERTY = "view";

	/**
	 * Bound property name for <code>graphModel</code>.
	 */
	public final static String MARQUEE_HANDLER_PROPERTY = "marquee";

	/**
	 * Bound property name for <code>editable</code>.
	 */
	public final static String EDITABLE_PROPERTY = "editable";

	/**
	 * Bound property name for <code>selectionEnabled</code>.
	 */
	public final static String SELECTIONENABLED_PROPERTY = "selectionEnabled";

	/**
	 * Bound property name for <code>scale</code>.
	 */
	public final static String SCALE_PROPERTY = "scale";

	/**
	 * Bound property name for <code>antiAliased</code>.
	 */
	public final static String ANTIALIASED_PROPERTY = "antiAliased";

	/**
	 * Bound property name for <code>gridSize</code>.
	 */
	public final static String GRID_SIZE_PROPERTY = "gridSize";

	/**
	 * Bound property name for <code>gridVisible</code>.
	 */
	public final static String GRID_VISIBLE_PROPERTY = "gridVisible";

	/**
	 * Bound property name for <code>gridColor</code>.
	 */
	public final static String GRID_COLOR_PROPERTY = "gridColor";

	/**
	 * Bound property name for <code>gridColor</code>.
	 */
	public final static String HANDLE_COLOR_PROPERTY = "handleColor";

	/**
	 * Bound property name for <code>gridColor</code>.
	 */
	public final static String HANDLE_SIZE_PROPERTY = "handleSize";

	/**
	 * Bound property name for <code>gridColor</code>.
	 */
	public final static String LOCKED_HANDLE_COLOR_PROPERTY = "lockedHandleColor";

	/**
	 * Bound property name for <code>gridVisible</code>.
	 */
	public final static String PORTS_VISIBLE_PROPERTY = "portsVisible";

	/**
	 * Bound property name for <code>portsScaled</code>.
	 */
	public final static String PORTS_SCALED_PROPERTY = "portsScaled";

	/**
	 * Bound property name for <code>selectionModel</code>.
	 */
	public final static String SELECTION_MODEL_PROPERTY = "selectionModel";

	/**
	 * Bound property name for <code>messagesStopCellEditing</code>.
	 */
	public final static String INVOKES_STOP_CELL_EDITING_PROPERTY = "invokesStopCellEditing";

	/**
	 * Bound property name for <code>backgroundImage</code>.
	 */
	public final static String PROPERTY_BACKGROUNDIMAGE = "backgroundImage";

	/**
	 * Creates and returns a sample <code>GraphModel</code>. Used primarily
	 * for beanbuilders to show something interesting.
	 */
	public static void addSampleData(GraphModel model) {
		ConnectionSet cs = new ConnectionSet();
		Map attributes = new Hashtable();
		// Styles For Implement/Extend/Aggregation
		AttributeMap implementStyle = new AttributeMap();
		GraphConstants.setLineBegin(implementStyle,
				GraphConstants.ARROW_TECHNICAL);
		GraphConstants.setBeginSize(implementStyle, 10);
		GraphConstants.setDashPattern(implementStyle, new float[] { 3, 3 });
		if (GraphConstants.DEFAULTFONT != null) {
			GraphConstants.setFont(implementStyle, GraphConstants.DEFAULTFONT
					.deriveFont(10));
		}
		AttributeMap extendStyle = new AttributeMap();
		GraphConstants
				.setLineBegin(extendStyle, GraphConstants.ARROW_TECHNICAL);
		GraphConstants.setBeginFill(extendStyle, true);
		GraphConstants.setBeginSize(extendStyle, 10);
		if (GraphConstants.DEFAULTFONT != null) {
			GraphConstants.setFont(extendStyle, GraphConstants.DEFAULTFONT
					.deriveFont(10));
		}
		AttributeMap aggregateStyle = new AttributeMap();
		GraphConstants.setLineBegin(aggregateStyle,
				GraphConstants.ARROW_DIAMOND);
		GraphConstants.setBeginFill(aggregateStyle, true);
		GraphConstants.setBeginSize(aggregateStyle, 6);
		GraphConstants.setLineEnd(aggregateStyle, GraphConstants.ARROW_SIMPLE);
		GraphConstants.setEndSize(aggregateStyle, 8);
		GraphConstants.setLabelPosition(aggregateStyle, new Point2D.Double(500,
				0));
		if (GraphConstants.DEFAULTFONT != null) {
			GraphConstants.setFont(aggregateStyle, GraphConstants.DEFAULTFONT
					.deriveFont(10));
		}
		//
		// The Swing MVC Pattern
		//
		// Model Column
		DefaultGraphCell gm = new DefaultGraphCell("GraphModel");
		attributes.put(gm,
				createBounds(new AttributeMap(), 20, 100, Color.blue));
		gm.addPort(null, "GraphModel/Center");
		DefaultGraphCell dgm = new DefaultGraphCell("DefaultGraphModel");
		attributes.put(dgm, createBounds(new AttributeMap(), 20, 180,
				Color.blue));
		dgm.addPort(null, "DefaultGraphModel/Center");
		DefaultEdge dgmImplementsGm = new DefaultEdge("implements");
		cs.connect(dgmImplementsGm, gm.getChildAt(0), dgm.getChildAt(0));
		attributes.put(dgmImplementsGm, implementStyle);
		DefaultGraphCell modelGroup = new DefaultGraphCell("ModelGroup");
		modelGroup.add(gm);
		modelGroup.add(dgm);
		modelGroup.add(dgmImplementsGm);
		// JComponent Column
		DefaultGraphCell jc = new DefaultGraphCell("JComponent");
		attributes.put(jc, createBounds(new AttributeMap(), 180, 20,
				Color.green));
		jc.addPort(null, "JComponent/Center");
		DefaultGraphCell jg = new DefaultGraphCell("JGraph");
		attributes.put(jg, createBounds(new AttributeMap(), 180, 100,
				Color.green));
		jg.addPort(null, "JGraph/Center");
		DefaultEdge jgExtendsJc = new DefaultEdge("extends");
		cs.connect(jgExtendsJc, jc.getChildAt(0), jg.getChildAt(0));
		attributes.put(jgExtendsJc, extendStyle);
		// UI Column
		DefaultGraphCell cu = new DefaultGraphCell("ComponentUI");
		attributes
				.put(cu, createBounds(new AttributeMap(), 340, 20, Color.red));
		cu.addPort(null, "ComponentUI/Center");
		DefaultGraphCell gu = new DefaultGraphCell("GraphUI");
		attributes.put(gu,
				createBounds(new AttributeMap(), 340, 100, Color.red));
		gu.addPort(null, "GraphUI/Center");
		DefaultGraphCell dgu = new DefaultGraphCell("BasicGraphUI");
		attributes.put(dgu, createBounds(new AttributeMap(), 340, 180,
				Color.red));
		dgu.addPort(null, "BasicGraphUI/Center");
		DefaultEdge guExtendsCu = new DefaultEdge("extends");
		cs.connect(guExtendsCu, cu.getChildAt(0), gu.getChildAt(0));
		attributes.put(guExtendsCu, extendStyle);
		DefaultEdge dguImplementsDu = new DefaultEdge("implements");
		cs.connect(dguImplementsDu, gu.getChildAt(0), dgu.getChildAt(0));
		attributes.put(dguImplementsDu, implementStyle);
		DefaultGraphCell uiGroup = new DefaultGraphCell("UIGroup");
		uiGroup.add(cu);
		uiGroup.add(gu);
		uiGroup.add(dgu);
		uiGroup.add(dguImplementsDu);
		uiGroup.add(guExtendsCu);
		// Aggregations
		DefaultEdge jgAggregatesGm = new DefaultEdge("model");
		cs.connect(jgAggregatesGm, jg.getChildAt(0), gm.getChildAt(0));
		attributes.put(jgAggregatesGm, aggregateStyle);
		DefaultEdge jcAggregatesCu = new DefaultEdge("ui");
		cs.connect(jcAggregatesCu, jc.getChildAt(0), cu.getChildAt(0));
		attributes.put(jcAggregatesCu, aggregateStyle);
		// Insert Cells into model
		Object[] cells = new Object[] { jgAggregatesGm, jcAggregatesCu,
				modelGroup, jc, jg, jgExtendsJc, uiGroup };
		model.insert(cells, attributes, cs, null, null);
	}

	/**
	 * Returns an attributeMap for the specified position and color.
	 */
	public static Map createBounds(AttributeMap map, int x, int y, Color c) {
		GraphConstants.setBounds(map, map.createRect(x, y, 90, 30));
		GraphConstants.setBorder(map, BorderFactory.createRaisedBevelBorder());
		GraphConstants.setBackground(map, c.darker().darker());
		GraphConstants
				.setGradientColor(map, c.brighter().brighter().brighter());
		GraphConstants.setForeground(map, Color.white);
		if (GraphConstants.DEFAULTFONT != null) {
			GraphConstants.setFont(map, GraphConstants.DEFAULTFONT.deriveFont(
					Font.BOLD, 12));
		}
		GraphConstants.setOpaque(map, true);
		return map;
	}

	/**
	 * Returns a <code>JGraph</code> with a sample model.
	 */
	public JGraph() {
		this((GraphModel) null);
	}

	/**
	 * Returns an instance of <code>JGraph</code> which displays the the
	 * specified data model.
	 * 
	 * @param model
	 *            the <code>GraphModel</code> to use as the data model
	 */
	public JGraph(GraphModel model) {
		this(model, (GraphLayoutCache) null);
	}

	/**
	 * Returns an instance of <code>JGraph</code> which displays the data
	 * model using the specified view.
	 * 
	 * @param cache
	 *            the <code>GraphLayoutCache</code> to use as the view
	 */
	public JGraph(GraphLayoutCache cache) {
		this((cache != null) ? cache.getModel() : null, cache);
	}

	/**
	 * Returns an instance of <code>JGraph</code> which displays the specified
	 * data model using the specified view.
	 * 
	 * @param model
	 *            the <code>GraphModel</code> to use as the data model
	 * @param cache
	 *            the <code>GraphLayoutCache</code> to use as the cache
	 */
	public JGraph(GraphModel model, GraphLayoutCache cache) {
		this(model, cache, new BasicMarqueeHandler());
	}

	/**
	 * Returns an instance of <code>JGraph</code> which displays the specified
	 * data model and assigns the specified marquee handler
	 * 
	 * @param model
	 *            the <code>GraphModel</code> to use as the data model
	 * @param mh
	 *            the <code>BasicMarqueeHandler</code> to use as the marquee
	 *            handler
	 */
	public JGraph(GraphModel model, BasicMarqueeHandler mh) {
		this(model, null, mh);
	}

	/**
	 * Returns an instance of <code>JGraph</code> which displays the specified
	 * data model using the specified view and assigns the specified marquee
	 * handler
	 * 
	 * @param model
	 *            the <code>GraphModel</code> to use as the data model
	 * @param layoutCache
	 *            the <code>GraphLayoutCache</code> to use as the cache
	 * @param mh
	 *            the <code>BasicMarqueeHandler</code> to use as the marquee
	 *            handler
	 */
	public JGraph(GraphModel model, GraphLayoutCache layoutCache,
			BasicMarqueeHandler mh) {
		setDoubleBuffered(true);
		selectionModel = new DefaultGraphSelectionModel(this);
		setLayout(null);
		marquee = mh;
		if (model == null) {
			model = new DefaultGraphModel();
			setModel(model);
			addSampleData(model);
		} else
			setModel(model);
		if (layoutCache == null)
			layoutCache = new GraphLayoutCache(model,
					new DefaultCellViewFactory());
		setGraphLayoutCache(layoutCache);
		updateUI();
	}

	//
	// UI-delegate (GraphUI)
	//
	/**
	 * Returns the L&F object that renders this component.
	 * 
	 * @return the GraphUI object that renders this component
	 */
	public GraphUI getUI() {
		return (GraphUI) ui;
	}

	/**
	 * Sets the L&F object that renders this component.
	 * 
	 * @param ui
	 *            the GraphUI L&F object
	 * @see javax.swing.UIDefaults#getUI(JComponent)
	 * 
	 */
	public void setUI(GraphUI ui) {
		if ((GraphUI) this.ui != ui) {
			super.setUI(ui);
		}
	}

	/**
	 * Notification from the <code>UIManager</code> that the L&F has changed.
	 * Replaces the current UI object with the latest version from the
	 * <code>UIManager</code>. Subclassers can override this to support
	 * different GraphUIs.
	 * 
	 * @see JComponent#updateUI
	 * 
	 */
	public void updateUI() {
		setUI(new org.jgraph.plaf.basic.BasicGraphUI());
		invalidate();
	}

	/**
	 * Returns the name of the L&F class that renders this component.
	 * 
	 * @return the string "GraphUI"
	 * @see JComponent#getUIClassID
	 * 
	 */
	public String getUIClassID() {
		return uiClassID;
	}

	//
	// Content
	//
	/**
	 * Returns all root cells (cells that have no parent) that the model
	 * contains.
	 */
	public Object[] getRoots() {
		return DefaultGraphModel.getRoots(graphModel);
	}

	/**
	 * Returns all cells that intersect the given rectangle.
	 */
	public Object[] getRoots(Rectangle clip) {
		CellView[] views = graphLayoutCache.getRoots(clip);
		Object[] cells = new Object[views.length];
		for (int i = 0; i < views.length; i++)
			cells[i] = views[i].getCell();
		return cells;
	}

	/**
	 * Returns all <code>cells</code> including all descendants in the passed
	 * in order of cells.
	 */
	public Object[] getDescendants(Object[] cells) {
		return DefaultGraphModel.getDescendants(getModel(), cells).toArray();
	}

	/**
	 * Returns all <code>cells</code> including all descendants ordered using
	 * the current layering data stored by the model.
	 */
	public Object[] order(Object[] cells) {
		return DefaultGraphModel.order(getModel(), cells);
	}

	/**
	 * Returns a map of (cell, clone)-pairs for all <code>cells</code> and
	 * their children. Special care is taken to replace the anchor references
	 * between ports. (Iterative implementation.)
	 */
	public Map cloneCells(Object[] cells) {
		return graphModel.cloneCells(cells);
	}

	/**
	 * Returns the topmost cell view at the specified location using the view's
	 * bounds on non-leafs to check for containment. If reverse is true this
	 * will return the innermost view.
	 */
	public CellView getTopmostViewAt(double x, double y, boolean reverse,
			boolean leafsOnly) {
		Rectangle2D r = new Rectangle2D.Double(x, y, 1, 1);
		Object[] cells = getDescendants(getRoots());
		for (int i = (reverse) ? cells.length - 1 : 0; i >= 0
				&& i < cells.length; i += (reverse) ? -1 : +1) {
			CellView view = getGraphLayoutCache().getMapping(cells[i], false);
			if (view != null
					&& (!leafsOnly || view.isLeaf())
					&& ((view.isLeaf() && view.intersects(this, r)) || (!view
							.isLeaf() && view.getBounds().contains(x, y))))
				return view;
		}
		return null;
	}

	/**
	 * Returns the topmost cell at the specified location.
	 * 
	 * @param x
	 *            an integer giving the number of pixels horizontally from the
	 *            left edge of the display area, minus any left margin
	 * @param y
	 *            an integer giving the number of pixels vertically from the top
	 *            of the display area, minus any top margin
	 * @return the topmost cell at the specified location
	 */
	public Object getFirstCellForLocation(double x, double y) {
		return getNextCellForLocation(null, x, y);
	}

	/**
	 * Returns the cell at the specified location that is "behind" the
	 * <code>current</code> cell. Returns the topmost cell if there are no
	 * more cells behind <code>current</code>. Note: This does only return
	 * visible cells.
	 */
	public Object getNextCellForLocation(Object current, double x, double y) {
		CellView cur = graphLayoutCache.getMapping(current, false);
		CellView cell = getNextViewAt(cur, x, y);
		if (cell != null)
			return cell.getCell();
		return null;
	}

	/**
	 * Returns the bounding rectangle of the specified cell.
	 */
	public Rectangle2D getCellBounds(Object cell) {
		CellView view = graphLayoutCache.getMapping(cell, false);
		if (view != null)
			return view.getBounds();
		return null;
	}

	/**
	 * Returns the bounding rectangle of the specified cells.
	 */
	public Rectangle2D getCellBounds(Object[] cells) {
		if (cells != null && cells.length > 0) {
			Rectangle2D r = getCellBounds(cells[0]);
			Rectangle2D ret = (r != null) ? (Rectangle2D) r.clone() : null;
			for (int i = 1; i < cells.length; i++) {
				r = getCellBounds(cells[i]);
				if (r != null) {
					if (ret == null)
						ret = (r != null) ? (Rectangle2D) r.clone() : null;
					else
						Rectangle2D.union(ret, r, ret);
				}
			}
			return ret;
		}
		return null;
	}

	/**
	 * Returns the next view at the specified location wrt. <code>current</code>.
	 * This is used to iterate overlapping cells, and cells that are grouped.
	 * The current selection affects this method. <br>
	 * Note: This returns the next <i>selectable </i> view. <br>
	 * Note: Arguments are not expected to be scaled (they are scaled in here).
	 */
	public CellView getNextViewAt(CellView current, double x, double y) {
		return getNextViewAt(current, x, y, false);
	}

	/**
	 * Returns the next view at the specified location wrt. <code>current</code>.
	 * This is used to iterate overlapping cells, and cells that are grouped.
	 * The current selection affects this method. <br>
	 * Note: This returns the next <i>selectable </i> view. <br>
	 * Note: Arguments are not expected to be scaled (they are scaled in here).
	 */
	public CellView getNextViewAt(CellView current, double x, double y,
			boolean leafsOnly) {
		CellView[] cells = AbstractCellView
				.getDescendantViews(getGraphLayoutCache().getRoots());
		return getNextViewAt(cells, current, x, y, leafsOnly);
	}

	/**
	 * Note: Arguments are not expected to be scaled (they are scaled in here).
	 */
	public CellView getNextSelectableViewAt(CellView current, double x, double y) {
		CellView[] selectables = getGraphLayoutCache().getMapping(
				getSelectionModel().getSelectables(), false);
		return getNextViewAt(selectables, current, x, y);
	}

	/**
	 * Returns the next view at the specified location wrt. <code>c</code> in
	 * the specified array of views. The views must be in order, as returned,
	 * for example, by GraphLayoutCache.order(Object[]).
	 */
	public CellView getNextViewAt(CellView[] cells, CellView c, double x,
			double y) {
		return getNextViewAt(cells, c, x, y, false);
	}

	/**
	 * Returns the next view at the specified location wrt. <code>c</code> in
	 * the specified array of views. The views must be in order, as returned,
	 * for example, by GraphLayoutCache.order(Object[]).
	 */
	public CellView getNextViewAt(CellView[] cells, CellView c, double x,
			double y, boolean leafsOnly) {
		if (cells != null) {
			Rectangle2D r = fromScreen(new Rectangle2D.Double(x - tolerance, y
					- tolerance, 2 * tolerance, 2 * tolerance));
			// Ensure the tolerance is at least 1.0 (can go below in high zoom
			// in case).
			if (r.getWidth() < 1.0) {
				r.setFrame(r.getX(), r.getY(), 1.0, r.getHeight());
			}
			if (r.getHeight() < 1.0) {
				r.setFrame(r.getX(), r.getY(), r.getWidth(), 1.0);
			}
			// Iterate through cells and switch to active
			// if current is traversed. Cache first cell.
			CellView first = null;
			boolean active = (c == null);
			for (int i = 0; i < cells.length; i++) {
				if (cells[i] != null && (!leafsOnly || cells[i].isLeaf())
						&& cells[i].intersects(this, r)) {
					// TODO: This behaviour is specific to selection and
					// should be parametrized (it only returns a group with
					// selected children if no other portview is available)
					if (active
							&& !selectionModel.isChildrenSelected(cells[i]
									.getCell())) {
						return cells[i];
					} else if (first == null)
						first = cells[i];
					active = active | (cells[i] == c);
				}
			}
			return first;
		}
		return null;
	}

	/**
	 * Returns the next view at the specified location wrt. <code>c</code> in
	 * the specified array of views. The views must be in order, as returned,
	 * for example, by GraphLayoutCache.order(Object[]).
	 */
	public CellView getLeafViewAt(double x, double y) {
		return getNextViewAt(null, x, y, true);
	}

	/**
	 * Convenience method to return the port at the specified location.
	 */
	public Object getPortForLocation(double x, double y) {
		PortView view = getPortViewAt(x, y, tolerance);
		if (view != null)
			return view.getCell();
		return null;
	}

	/**
	 * Returns the portview at the specified location. <br>
	 * Note: Arguments are not expected to be scaled (they are scaled in here).
	 */
	public PortView getPortViewAt(double x, double y) {
		return getPortViewAt(x, y, tolerance);
	}

	/**
	 * Returns the portview at the specified location. <br>
	 * Note: Arguments are not expected to be scaled (they are scaled in here).
	 */
	public PortView getPortViewAt(double x, double y, int tolerance) {
		double sx = x / scale;
		double sy = y / scale;
		Rectangle2D r = new Rectangle2D.Double(sx - tolerance, sy - tolerance,
				2 * tolerance, 2 * tolerance);
		PortView[] ports = graphLayoutCache.getPorts();
		if (ports != null) {
			for (int i = ports.length - 1; i >= 0; i--)
				if (ports[i] != null && ports[i].intersects(this, r))
					return ports[i];
			if (isJumpToDefaultPort()) {
				CellView cellView = getNextViewAt(null, x, y, true);

				// Finds a non-edge cell under the mousepointer
				if (cellView != null && graphModel.isEdge(cellView.getCell())) {
					CellView nextView = getNextViewAt(cellView, x, y, true);
					while (nextView != cellView
							&& graphModel.isEdge(nextView.getCell())) {
						nextView = getNextViewAt(nextView, x, y, true);
					}
					cellView = nextView;
				}
				if (cellView != null) {
					PortView defaultPort = getDefaultPortForCell(cellView
							.getCell());
					return defaultPort;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the default portview for the specified cell. The default
	 * implementation returns the first floating port (ie. the first port that
	 * does not define an offset) or <b>the </b> port, if there is only one
	 * port.
	 * 
	 * @param cell
	 *            the cell whose port is to be obtained
	 * @return the port view of the specified cell
	 */
	public PortView getDefaultPortForCell(Object cell) {
		if (cell != null && !getModel().isEdge(cell)) {
			int childCount = getModel().getChildCount(cell);
			for (int i = 0; i < childCount; i++) {
				Object childCell = getModel().getChild(cell, i);
				CellView child = getGraphLayoutCache().getMapping(childCell,
						false);
				if (child instanceof PortView) {
					Point2D offset = GraphConstants.getOffset(child
							.getAllAttributes());
					if (offset == null || childCount == 1)
						return (PortView) child;
				}
			}
		}
		return null;
	}

	/**
	 * Converts the specified value to string. If the value is an instance of
	 * CellView then the corresponding value or cell is used.
	 */
	public String convertValueToString(Object value) {
		if (value instanceof CellView)
			value = ((CellView) value).getCell();
		return String.valueOf(value);
	}

	//
	// Grid and Scale
	//
	/**
	 * Returns the given point applied to the grid.
	 * 
	 * @param p
	 *            a point in screen coordinates.
	 * @return the same point applied to the grid.
	 */
	public Point2D snap(Point2D p) {
		if (gridEnabled && p != null) {
			double sgs = gridSize * getScale();
			p.setLocation(Math.round(Math.round(p.getX() / sgs) * sgs), Math
					.round(Math.round(p.getY() / sgs) * sgs));
		}
		return p;
	}

	/**
	 * Returns the given rectangle applied to the grid.
	 * 
	 * @param r
	 *            a rectangle in screen coordinates.
	 * @return the same rectangle applied to the grid.
	 */
	public Rectangle2D snap(Rectangle2D r) {
		if (gridEnabled && r != null) {
			double sgs = gridSize * getScale();
			r.setFrame(Math.round(Math.round(r.getX() / sgs) * sgs), Math
					.round(Math.round(r.getY() / sgs) * sgs), 1 + Math
					.round(Math.round(r.getWidth() / sgs) * sgs), 1 + Math
					.round(Math.round(r.getHeight() / sgs) * sgs));
		}
		return r;
	}

	/**
	 * Returns the given dimension applied to the grid.
	 * 
	 * @param d
	 *            a dimension in screen coordinates to snap to.
	 * @return the same dimension applied to the grid.
	 */
	public Dimension2D snap(Dimension2D d) {
		if (gridEnabled && d != null) {
			double sgs = gridSize * getScale();
			d.setSize(1 + Math.round(Math.round(d.getWidth() / sgs) * sgs),
					1 + Math.round(Math.round(d.getHeight() / sgs) * sgs));
		}
		return d;
	}

	/**
	 * Upscale the given point in place, using the given instance.
	 * 
	 * @param p
	 *            the point to be upscaled
	 * @return the upscaled point instance
	 */
	public Point2D toScreen(Point2D p) {
		if (p == null)
			return null;
		p.setLocation(Math.round(p.getX() * scale), Math
				.round(p.getY() * scale));
		return p;
	}

	/**
	 * Downscale the given point in place, using the given instance.
	 * 
	 * @param p
	 *            the point to be downscaled
	 * @return the downscaled point instance
	 */
	public Point2D fromScreen(Point2D p) {
		if (p == null)
			return null;
		p.setLocation(Math.round(p.getX() / scale), Math
				.round(p.getY() / scale));
		return p;
	}

	/**
	 * Upscale the given rectangle in place, using the given instance.
	 * 
	 * @param rect
	 *            the rectangle to be upscaled
	 * @return the upscaled rectangle instance
	 */
	public Rectangle2D toScreen(Rectangle2D rect) {
		if (rect == null)
			return null;
		rect.setFrame(rect.getX() * scale, rect.getY() * scale, rect.getWidth()
				* scale, rect.getHeight() * scale);
		return rect;
	}

	/**
	 * Downscale the given rectangle in place, using the given instance.
	 * 
	 * @param rect
	 *            the rectangle to be downscaled
	 * @return the down-scaled rectangle instance
	 */
	public Rectangle2D fromScreen(Rectangle2D rect) {
		if (rect == null)
			return null;
		rect.setFrame(rect.getX() / scale, rect.getY() / scale, rect.getWidth()
				/ scale, rect.getHeight() / scale);
		return rect;
	}

	/**
	 * Computes and updates the size for <code>view</code>.
	 */
	public void updateAutoSize(CellView view) {
		if (view != null && !isEditing()) {
			Rectangle2D bounds = (view.getAttributes() != null) ? GraphConstants
					.getBounds(view.getAttributes())
					: null;
			AttributeMap attrs = getModel().getAttributes(view.getCell());
			if (bounds == null)
				bounds = GraphConstants.getBounds(attrs);
			if (bounds != null) {
				boolean autosize = GraphConstants.isAutoSize(view
						.getAllAttributes());
				boolean resize = GraphConstants.isResize(view
						.getAllAttributes());
				if (autosize || resize) {
					Dimension2D d = getUI().getPreferredSize(this, view);
					bounds.setFrame(bounds.getX(), bounds.getY(), d.getWidth(),
							d.getHeight());
					// Remove resize attribute
					snap(bounds);
					if (resize) {
						if (view.getAttributes() != null)
							view.getAttributes().remove(GraphConstants.RESIZE);
						attrs.remove(GraphConstants.RESIZE);
					}
					view.refresh(getGraphLayoutCache(), getGraphLayoutCache(), false);
				}
			}
		}
	}

	/**
	 * Returns the attributes for the specified cell. If the layout cache
	 * returns a view for the cell then this method returns allAttributes,
	 * otherwise the method returns model.getAttributes(cell).
	 */
	public AttributeMap getAttributes(Object cell) {
		AttributeMap attrs;
		CellView cellView = getGraphLayoutCache().getMapping(cell, false);
		if (cellView != null) {
			attrs = cellView.getAllAttributes();
		} else {
			attrs = getModel().getAttributes(cell);
		}
		return attrs;
	}

	//
	// Unbound Properties
	//
	/**
	 * Returns the number of clicks for editing to start.
	 */
	public int getEditClickCount() {
		return editClickCount;
	}

	/**
	 * Sets the number of clicks for editing to start.
	 */
	public void setEditClickCount(int count) {
		editClickCount = count;
	}

	/**
	 * Returns true if the graph accepts drops/pastes from external sources.
	 */
	public boolean isDropEnabled() {
		return dropEnabled;
	}

	/**
	 * Sets if the graph accepts drops/pastes from external sources.
	 */
	public void setDropEnabled(boolean flag) {
		dropEnabled = flag;
	}

	/**
	 * Returns true if the graph accepts drops/pastes from external sources.
	 */
	public boolean isXorEnabled() {
		return (xorEnabled && isOpaque());
	}

	/**
	 * Sets if the graph accepts drops/pastes from external sources.
	 */
	public void setXorEnabled(boolean flag) {
		xorEnabled = flag;
	}

	/**
	 * Returns true if the graph uses Drag-and-Drop to move cells.
	 */
	public boolean isDragEnabled() {
		return dragEnabled;
	}

	/**
	 * Sets if the graph uses Drag-and-Drop to move cells.
	 */
	public void setDragEnabled(boolean flag) {
		dragEnabled = flag;
	}

	/*
	 * Returns true if the graph allows movement of cells.
	 */
	public boolean isMoveable() {
		return moveable;
	}

	/**
	 * Sets if the graph allows movement of cells.
	 */
	public void setMoveable(boolean flag) {
		moveable = flag;
	}

	/**
	 * Returns true if the graph allows adding/removing/modifying points.
	 */
	public boolean isBendable() {
		return bendable;
	}

	/**
	 * Sets if the graph allows adding/removing/modifying points.
	 */
	public void setBendable(boolean flag) {
		bendable = flag;
	}

	/**
	 * Returns true if the graph allows new connections to be established.
	 */
	public boolean isConnectable() {
		return connectable;
	}

	/**
	 * Setse if the graph allows new connections to be established.
	 */
	public void setConnectable(boolean flag) {
		connectable = flag;
	}

	/**
	 * Returns true if the graph allows existing connections to be removed.
	 */
	public boolean isDisconnectable() {
		return disconnectable;
	}

	/**
	 * Sets if the graph allows existing connections to be removed.
	 */
	public void setDisconnectable(boolean flag) {
		disconnectable = flag;
	}

	/**
	 * Returns true if cells are cloned on CTRL-Drag operations.
	 */
	public boolean isCloneable() {
		return cloneable;
	}

	/**
	 * Sets if cells are cloned on CTRL-Drag operations.
	 */
	public void setCloneable(boolean flag) {
		cloneable = flag;
	}

	/**
	 * Returns true if the graph allows cells to be resized.
	 */
	public boolean isSizeable() {
		return sizeable;
	}

	/**
	 * Sets if the graph allows cells to be resized.
	 */
	public void setSizeable(boolean flag) {
		sizeable = flag;
	}

	/**
	 * Sets if selected edges should be disconnected from unselected vertices
	 * when they are moved.
	 */
	public void setDisconnectOnMove(boolean flag) {
		disconnectOnMove = flag;
	}

	/**
	 * Returns true if selected edges should be disconnected from unselected
	 * vertices when they are moved.
	 */
	public boolean isDisconnectOnMove() {
		return disconnectOnMove && disconnectable;
	}

	/**
	 * Sets if getPortViewAt should return the default port if no other port is
	 * found.
	 */
	public void setJumpToDefaultPort(boolean flag) {
		isJumpToDefaultPort = flag;
	}

	/**
	 * Returns true if getPortViewAt should return the default port if no other
	 * port is found.
	 */
	public boolean isJumpToDefaultPort() {
		return isJumpToDefaultPort;
	}

	/**
	 * Specifies if cells should be added to groups when moved over the group's
	 * area.
	 */
	public void setMoveIntoGroups(boolean flag) {
		isMoveIntoGroups = flag;
	}

	/**
	 * Returns true if cells should be added to groups when moved over the
	 * group's area.
	 */
	public boolean isMoveIntoGroups() {
		return isMoveIntoGroups;
	}

	/**
	 * Specifies if cells should be removed from groups when removed from the
	 * group's area.
	 */
	public void setMoveOutOfGroups(boolean flag) {
		isMoveOutOfGroups = flag;
	}

	/**
	 * Returns true if cells should be removed from groups when removed from the
	 * group's area.
	 */
	public boolean isMoveOutOfGroups() {
		return isMoveOutOfGroups;
	}

	/**
	 * Returns true if the grid is active.
	 * 
	 * @see #snap(Point2D)
	 * 
	 */
	public boolean isGridEnabled() {
		return gridEnabled;
	}

	/**
	 * If set to true, the grid will be active.
	 * 
	 * @see #snap(Point2D)
	 * 
	 */
	public void setGridEnabled(boolean flag) {
		gridEnabled = flag;
	}

	/**
	 * Returns true if the graph allows to move cells below zero.
	 */
	public boolean isMoveBelowZero() {
		return moveBelowZero;
	}

	/**
	 * Sets if the graph should auto resize when cells are being moved below the
	 * bottom right corner.
	 */
	public void setMoveBelowZero(boolean moveBelowZero) {
		this.moveBelowZero = moveBelowZero;
	}

	/**
	 * @return the moveBeyondGraphBounds
	 */
	public boolean isMoveBeyondGraphBounds() {
		return moveBeyondGraphBounds;
	}

	/**
	 * @param moveBeyondGraphBounds the moveBeyondGraphBounds to set
	 */
	public void setMoveBeyondGraphBounds(boolean moveBeyondGraphBounds) {
		this.moveBeyondGraphBounds = moveBeyondGraphBounds;
	}

	/**
	 * Returns true if edge labels may be dragged and dropped.
	 * 
	 * @return whether edge labels may be dragged and dropped
	 */
	public boolean getEdgeLabelsMovable() {
		return edgeLabelsMovable;
	}

	/**
	 * Set if edge labels may be moved with the mouse or not.
	 * 
	 * @param edgeLabelsMovable
	 *            true if edge labels may be dragged
	 */
	public void setEdgeLabelsMovable(boolean edgeLabelsMovable) {
		this.edgeLabelsMovable = edgeLabelsMovable;
	}

	/**
	 * Returns true if the graph should be automatically resized when cells are
	 * being moved below the bottom right corner. Note if the value of
	 * <code>moveBeyondGraphBounds</code> if <code>false</code> auto resizing
	 * is automatically disabled
	 */
	public boolean isAutoResizeGraph() {
		if (!moveBeyondGraphBounds) {
			return false;
		}
		return autoResizeGraph;
	}

	/**
	 * Sets whether or not the graph should be automatically resize when cells 
	 * are being moved below the bottom right corner
	 */
	public void setAutoResizeGraph(boolean autoResizeGraph) {
		this.autoResizeGraph = autoResizeGraph;
	}

	/**
	 * Returns the maximum distance between the mousepointer and a cell to be
	 * selected.
	 */
	public int getTolerance() {
		return tolerance;
	}

	/**
	 * Sets the maximum distance between the mousepointer and a cell to be
	 * selected.
	 */
	public void setTolerance(int size) {
		if (size < 1) {
			size = 1;
		}
		tolerance = size;
	}

	/**
	 * Returns the size of the handles.
	 */
	public int getHandleSize() {
		return handleSize;
	}

	/**
	 * Sets the size of the handles.
	 */
	public void setHandleSize(int size) {
		int oldValue = handleSize;
		handleSize = size;
		firePropertyChange(HANDLE_SIZE_PROPERTY, oldValue, size);
	}

	/**
	 * Returns the miminum amount of pixels for a move operation.
	 */
	public int getMinimumMove() {
		return minimumMove;
	}

	/**
	 * Sets the miminum amount of pixels for a move operation.
	 */
	public void setMinimumMove(int pixels) {
		minimumMove = pixels;
	}

	//
	// Laf-Specific color scheme. These colors are changed
	// by BasicGraphUI when the laf changes.
	//
	/**
	 * Returns the current grid color.
	 */
	public Color getGridColor() {
		return gridColor;
	}

	/**
	 * Sets the current grid color.
	 */
	public void setGridColor(Color newColor) {
		Color oldValue = gridColor;
		gridColor = newColor;
		firePropertyChange(GRID_COLOR_PROPERTY, oldValue, newColor);
	}

	/**
	 * Returns the current handle color.
	 */
	public Color getHandleColor() {
		return handleColor;
	}

	/**
	 * Sets the current handle color.
	 */
	public void setHandleColor(Color newColor) {
		Color oldValue = handleColor;
		handleColor = newColor;
		firePropertyChange(HANDLE_COLOR_PROPERTY, oldValue, newColor);
	}

	/**
	 * Returns the current second handle color.
	 */
	public Color getLockedHandleColor() {
		return lockedHandleColor;
	}

	/**
	 * Sets the current second handle color.
	 */
	public void setLockedHandleColor(Color newColor) {
		Color oldValue = lockedHandleColor;
		lockedHandleColor = newColor;
		firePropertyChange(LOCKED_HANDLE_COLOR_PROPERTY, oldValue, newColor);
	}

	/**
	 * Returns the current marquee color.
	 */
	public Color getMarqueeColor() {
		return marqueeColor;
	}

	/**
	 * Sets the current marquee color.
	 */
	public void setMarqueeColor(Color newColor) {
		marqueeColor = newColor;
	}

	/**
	 * Returns the current highlight color.
	 */
	public Color getHighlightColor() {
		return highlightColor;
	}

	/**
	 * Sets the current selection highlight color.
	 */
	public void setHighlightColor(Color newColor) {
		highlightColor = newColor;
	}

	//
	// Bound properties
	//
	/**
	 * Returns the current scale.
	 * 
	 * @return the current scale as a double
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * Sets the current scale.
	 * <p>
	 * Fires a property change for the SCALE_PROPERTY.
	 * 
	 * @param newValue
	 *            the new scale
	 */
	public void setScale(double newValue) {
		Point2D centerPoint = getCenterPoint();
		setScale(newValue, centerPoint);
	}

	/**
	 * Sets the current scale and centers the graph to the specified point
	 * 
	 * @param newValue
	 *            the new scale
	 * @param center
	 *            the center of the graph
	 */
	public void setScale(double newValue, Point2D center) {
		if (newValue > 0 && newValue != this.scale) {
			Rectangle2D view = getViewPortBounds();
			double oldValue = this.scale;
			scale = newValue;
			boolean zoomIn = true;
			Rectangle newView = null;
			clearOffscreen();
			if (view != null) {
				double scaleRatio = newValue / oldValue;
				int newCenterX = (int) (center.getX() * scaleRatio);
				int newCenterY = (int) (center.getY() * scaleRatio);
				int newX = (int) (newCenterX - view.getWidth() / 2.0);
				int newY = (int) (newCenterY - view.getHeight() / 2.0);
				newView = new Rectangle(newX, newY, (int) view.getWidth(),
						(int) view.getHeight());
				// When zooming out scroll before revalidation otherwise
				// revalidation causes one scroll and scrollRectToVisible
				// another
				if (scaleRatio < 1.0) {
					scrollRectToVisible(newView);
					zoomIn = false;
				}
			}
			firePropertyChange(SCALE_PROPERTY, oldValue, newValue);
			// When zooming in, do it after the revalidation otherwise
			// it intermittently moves to the old co-ordinate system
			if (zoomIn && newView != null) {
				scrollRectToVisible(newView);
			}
		}
	}

	/**
	 * Invalidate the offscreen region, do not just delete it, since if the new
	 * region is smaller than the old you may not wish to re-create the buffer
	 */
	public void clearOffscreen() {

		if (offscreen != null) {
			int h = offscreen.getHeight(this);
			int w = offscreen.getWidth(this);
			Rectangle2D dirtyRegion = new Rectangle2D.Double(0, 0, w, h);
			fromScreen(dirtyRegion);
			addOffscreenDirty(dirtyRegion);
		}
	}

	/**
	 * Returns the center of the component relative to the parent viewport's
	 * position.
	 */
	public Point2D getCenterPoint() {
		Rectangle2D viewBounds = getViewPortBounds();
		if (viewBounds != null) {
			return new Point2D.Double(viewBounds.getCenterX(), viewBounds
					.getCenterY());
		}
		viewBounds = getBounds();
		return new Point2D.Double(viewBounds.getCenterX(), viewBounds
				.getCenterY());
	}

	/**
	 * Return the bounds of the parent viewport, if one exists. If one does not
	 * exist, null is returned
	 * 
	 * @return the bounds of the parent viewport
	 */
	public Rectangle2D getViewPortBounds() {
		if (getParent() instanceof JViewport) {
			return ((JViewport) getParent()).getViewRect();
		}
		return null;
	}

	/**
	 * Returns the size of the grid in pixels.
	 * 
	 * @return the size of the grid as an int
	 */
	public double getGridSize() {
		return gridSize;
	}

	/**
	 * Returns the current grid view mode.
	 */
	public int getGridMode() {
		return gridMode;
	}

	/**
	 * Sets the size of the grid.
	 * <p>
	 * Fires a property change for the GRID_SIZE_PROPERTY.
	 * 
	 * @param newSize
	 *            the new size of the grid in pixels
	 */
	public void setGridSize(double newSize) {
		double oldValue = this.gridSize;
		this.gridSize = newSize;
		firePropertyChange(GRID_SIZE_PROPERTY, oldValue, newSize);
	}

	/**
	 * Sets the current grid view mode.
	 * 
	 * @param mode
	 *            The current grid view mode. Valid values are <CODE>
	 *            DOT_GRID_MODE</CODE>,<CODE>CROSS_GRID_MODE</CODE>, and
	 *            <CODE>LINE_GRID_MODE</CODE>.
	 */
	public void setGridMode(int mode) {
		if (mode == DOT_GRID_MODE || mode == CROSS_GRID_MODE
				|| mode == LINE_GRID_MODE) {
			gridMode = mode;
			repaint();
		}
	}

	/**
	 * Returns true if the grid will be visible.
	 * 
	 * @return true if the grid is visible
	 */
	public boolean isGridVisible() {
		return gridVisible;
	}

	/**
	 * If set to true, the grid will be visible.
	 * <p>
	 * Fires a property change for the GRID_VISIBLE_PROPERTY.
	 */
	public void setGridVisible(boolean flag) {
		boolean oldValue = gridVisible;
		gridVisible = flag;
		// Clear the double buffer if the grid has been enabled
		if (flag != oldValue) {
			clearOffscreen();
		}
		firePropertyChange(GRID_VISIBLE_PROPERTY, oldValue, flag);
	}

	/**
	 * Returns true if the ports will be visible.
	 * 
	 * @return true if the ports are visible
	 */
	public boolean isPortsVisible() {
		return portsVisible;
	}

	/**
	 * If set to true, the ports will be visible.
	 * <p>
	 * Fires a property change for the PORTS_VISIBLE_PROPERTY.
	 */
	public void setPortsVisible(boolean flag) {
		boolean oldValue = portsVisible;
		portsVisible = flag;
		firePropertyChange(PORTS_VISIBLE_PROPERTY, oldValue, flag);
	}

	/**
	 * Returns true if the ports will be scaled.
	 * 
	 * @return true if the ports are visible
	 */
	public boolean isPortsScaled() {
		return portsScaled;
	}

	/**
	 * If set to true, the ports will be scaled.
	 * <p>
	 * Fires a property change for the PORTS_SCALED_PROPERTY.
	 */
	public void setPortsScaled(boolean flag) {
		boolean oldValue = portsScaled;
		portsScaled = flag;
		firePropertyChange(PORTS_SCALED_PROPERTY, oldValue, flag);
	}

	public boolean isPortsOnTop() {
		return portsOnTop;
	}

	public void setPortsOnTop(boolean portsOnTop) {
		this.portsOnTop = portsOnTop;
	}

	/**
	 * Returns true if the graph will be anti aliased.
	 * 
	 * @return true if the graph is anti aliased
	 */
	public boolean isAntiAliased() {
		return antiAliased;
	}

	/**
	 * Sets antialiasing on or off based on the boolean value.
	 * <p>
	 * Fires a property change for the ANTIALIASED_PROPERTY.
	 * 
	 * @param newValue
	 *            whether to turn antialiasing on or off
	 */
	public void setAntiAliased(boolean newValue) {
		boolean oldValue = this.antiAliased;
		this.antiAliased = newValue;
		firePropertyChange(ANTIALIASED_PROPERTY, oldValue, newValue);
	}

	/**
	 * Returns true if the graph is editable (if it allows cells to be edited).
	 * 
	 * @return true if the graph is editable
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * Determines whether the graph is editable. Fires a property change event
	 * if the new setting is different from the existing setting.
	 * <p>
	 * Note: Editable determines whether the graph allows editing. This is not
	 * to be confused with enabled, which allows the graph to handle mouse
	 * events (including editing).
	 * 
	 * @param flag
	 *            a boolean value, true if the graph is editable
	 */
	public void setEditable(boolean flag) {
		boolean oldValue = this.editable;
		this.editable = flag;
		firePropertyChange(EDITABLE_PROPERTY, oldValue, flag);
	}

	/**
	 * @return the groupsEditable
	 */
	public boolean isGroupsEditable() {
		return groupsEditable;
	}

	/**
	 * @param groupsEditable the groupsEditable to set
	 */
	public void setGroupsEditable(boolean groupsEditable) {
		this.groupsEditable = groupsEditable;
	}

	/**
	 * Returns true if the cell selection is enabled
	 * 
	 * @return true if the cell selection is enabled
	 */
	public boolean isSelectionEnabled() {
		return selectionEnabled;
	}

	/**
	 * Determines whether cell selection is enabled. Fires a property change
	 * event if the new setting is different from the existing setting.
	 * 
	 * @param flag
	 *            a boolean value, true if cell selection is enabled
	 */
	public void setSelectionEnabled(boolean flag) {
		boolean oldValue = this.selectionEnabled;
		this.selectionEnabled = flag;
		firePropertyChange(SELECTIONENABLED_PROPERTY, oldValue, flag);
	}

	/**
	 * Returns true if graph allows invalid null ports during previews
	 * 
	 * @return true if the graph allows invalid null ports during previews
	 */
	public boolean isPreviewInvalidNullPorts() {
		return previewInvalidNullPorts;
	}

	/**
	 * Determines whether the graph allows invalid null ports during previews
	 * 
	 * @param flag
	 *            a boolean value, true if the graph allows invalid null ports
	 *            during previews
	 */
	public void setPreviewInvalidNullPorts(boolean flag) {
		this.previewInvalidNullPorts = flag;
	}

	/**
	 * Returns the current double buffering graphics object. Checks to see if
	 * the graph bounds has changed since the last time the off screen image was
	 * created and if so, creates a new image.
	 * 
	 * @return the off screen graphics
	 */
	public Graphics getOffgraphics() {
		if (!isDoubleBuffered()) {
			// If double buffering is not enabled
			return null;
		}
		// Get the bounds of the entire graph
		Rectangle2D graphBounds = getBounds();
		// Get the visible area if in a scroll pane
		Rectangle2D viewPortBounds = null;//getViewPortBounds();
		// The result can be null if not a view port.
		if (viewPortBounds == null) {
			viewPortBounds = graphBounds;
		}
		// Find the size of the double buffer in the JVM
		int x = Math
				.max(0, (int) viewPortBounds.getX() - (int) offscreenBuffer);
		int y = Math
				.max(0, (int) viewPortBounds.getY() - (int) offscreenBuffer);
		int width = (int) viewPortBounds.getWidth() + (int) offscreenBuffer * 2;
		int height = (int) viewPortBounds.getHeight() + (int) offscreenBuffer
				* 2;
		// Code below used to work out max possible screen size, might not
		// be needed in JVM 1.6?
		//		try {
		//		Rectangle virtualBounds = new Rectangle();
		//		GraphicsEnvironment ge = GraphicsEnvironment
		//		.getLocalGraphicsEnvironment();
		//		GraphicsDevice[] devices = ge.getScreenDevices();
		//		for (int i = 0; i < devices.length; i++) {
		//		GraphicsConfiguration gc = devices[i].getDefaultConfiguration();
		//		virtualBounds = virtualBounds.union(gc.getBounds());
		//		}
		//		doubleBufferSize = new Dimension(virtualBounds.width,
		//		virtualBounds.height);
		//		} catch (HeadlessException e) {
		//		doubleBufferSize = new Dimension((int) graphBounds.getWidth(), (int)
		//		graphBounds
		//		.getHeight());
		//		}
		// If the screen size exceed either of the graph dimensions, limit them
		// to the graph's
		if (width > graphBounds.getWidth()) {
			width = (int) graphBounds.getWidth();
		}
		if (height > graphBounds.getHeight()) {
			height = (int) graphBounds.getHeight();
		}

		Rectangle2D newOffscreenBuffer = new Rectangle2D.Double(0, 0, width,
				height);
		// Check whether the visible area is completely contained within the
		// buffer. If not, the buffer need to be re-generated
		if ((offscreen == null || offgraphics == null || offscreenBounds == null)
				|| !(offscreenBounds.contains(newOffscreenBuffer))) {
			if (offscreen != null) {
				offscreen.flush();
			}
			if (offgraphics != null) {
				offgraphics.dispose();
			}
			offscreen = null;
			offgraphics = null;
			Runtime runtime = Runtime.getRuntime();
			long maxMemory = runtime.maxMemory();
			long allocatedMemory = runtime.totalMemory();
			long freeMemory = runtime.freeMemory();
			long totalFreeMemory = (freeMemory + (maxMemory - allocatedMemory)) / 1024;
			// Calculate size of buffer required (assuming TYPE_INT_RGB which
			// stores each pixel in a 32-bit int )
			long memoryRequired = width*height*4/1024;
			if (memoryRequired > totalFreeMemory) {
				if (lastBufferAllocated) {
					// If the last attempt to allocate a buffer worked it might
					// be we need to reclaim the memory before the next one
					// will work
					System.gc();
				}
				lastBufferAllocated = false;
				return null;
			}
			if (offscreen == null && volatileOffscreen) {
				try {
					offscreen = createVolatileImage(width, height);
				} catch (OutOfMemoryError e) {
					offscreen = null;
					offgraphics = null;
				}
			}
			if (offscreen == null) {
				// Probably running in headless mode, try to create a buffered
				// image.
				createBufferedImage(width, height);
			}
			if (offscreen == null) {
				// TODO assume the graph is too large and only buffer part
				// of it, might also be faster to calculate in
				// advance whether they is enough memory to create image
				// rather than let it try and throw error.
				lastBufferAllocated = false;
				return null;
			}
			lastBufferAllocated = true;
			setupOffScreen(x, y, width, height, newOffscreenBuffer);
		} else if (offscreen instanceof VolatileImage) {
			int valCode = ((VolatileImage) offscreen)
					.validate(getGraphicsConfiguration());
			if (!volatileOffscreen) {
				offscreen.flush();
				offgraphics.dispose();
				offscreen = null;
				offgraphics = null;
				createBufferedImage(width,height);
				setupOffScreen(x, y, width, height, newOffscreenBuffer);
			} else if (valCode == VolatileImage.IMAGE_INCOMPATIBLE) {
				offscreen.flush();
				offgraphics.dispose();
				try {
					offscreen = createVolatileImage(width, height);
				} catch (OutOfMemoryError e) {
					offscreen = null;
					offgraphics = null;
					return null;
				}
				setupOffScreen(x, y, width, height, newOffscreenBuffer);
			} else if (valCode == VolatileImage.IMAGE_RESTORED) {
				addOffscreenDirty(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
			}
		}
		Rectangle2D offscreenDirty = getOffscreenDirty();
		if (offscreenDirty != null) {
			if (isOpaque()) {
				offgraphics.setColor(getBackground());
				offgraphics.setPaintMode();
			} else {
				((Graphics2D) offgraphics).setComposite(AlphaComposite.getInstance(
					      AlphaComposite.CLEAR, 0.0f));
			}
			toScreen(offscreenDirty);
			offscreenDirty.setRect(offscreenDirty.getX()
					- (getHandleSize() + 1), offscreenDirty.getY()
					- (getHandleSize() + 1), offscreenDirty.getWidth()
					+ (getHandleSize() + 1) * 2, offscreenDirty.getHeight()
					+ (getHandleSize() + 1) * 2);
			offgraphics.fillRect((int) offscreenDirty.getX(),
					(int) offscreenDirty.getY(), (int) offscreenDirty
							.getWidth(), (int) offscreenDirty.getHeight());
			if (!isOpaque()) {
				((Graphics2D) offgraphics).setComposite(AlphaComposite.SrcOver);
			}
			((BasicGraphUI) getUI()).drawGraph(offgraphics, offscreenDirty);
			clearOffscreenDirty();
		}
		return offgraphics;
	}

	/**
	 * Utility method to create a standard buffered image
	 * @param width
	 * @param height
	 */
	protected void createBufferedImage(int width, int height) {
		GraphicsConfiguration graphicsConfig = getGraphicsConfiguration();
		if (graphicsConfig != null) {
			try {
				offscreen = graphicsConfig.createCompatibleImage(width, height,
						(isOpaque()) ? Transparency.OPAQUE
								: Transparency.TRANSLUCENT);
			} catch (OutOfMemoryError e) {
				offscreen = null;
				offgraphics = null;
			} catch (NegativeArraySizeException e) {
				// Customer reported this exception in DataBufferInt 13/12/2008
				offscreen = null;
				offgraphics = null;
//				System.out.println("width = " + width);
//				System.out.println("height = " + height);
			}
		} else {
			try {
				offscreen = new BufferedImage(width, height,
						isOpaque() ? BufferedImage.TYPE_INT_RGB
								: BufferedImage.TYPE_INT_ARGB);
			} catch (OutOfMemoryError e) {
				offscreen = null;
				offgraphics = null;
			}
		}
	}

	/**
	 * Utility method that initialises the offscreen graphics area
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param newOffscreenBuffer
	 */
	protected void setupOffScreen(int x, int y, int width, int height, Rectangle2D newOffscreenBuffer) {
		offgraphics = offscreen.getGraphics();
		if (isOpaque()) {
			offgraphics.setColor(getBackground());
			offgraphics.setPaintMode();
		} else {
			((Graphics2D) offgraphics).setComposite(AlphaComposite.getInstance(
					AlphaComposite.CLEAR, 0.0f));
		}
		offgraphics.fillRect(0, 0, width, height);
		if (!isOpaque()) {
			 ((Graphics2D) offgraphics).setComposite(AlphaComposite.SrcOver);
		}
		((BasicGraphUI)getUI()).drawGraph(offgraphics, null);
		offscreenBounds = newOffscreenBuffer;
		offscreenOffset = new Point2D.Double(x, y);
		// Clear the offscreen, we've just drawn the whole thing
		clearOffscreenDirty();
	}

	/**
	 * @return the offscreen
	 */
	public Image getOffscreen() {
		return offscreen;
	}

	/**
	 * Returns the area that is deemed dirty for the next double buffered redraw
	 * 
	 * @return the area that is deemed dirty for the next double buffered redraw
	 */
	public Rectangle2D getOffscreenDirty() {
		return offscreenDirty;
	}

	/**
	 * Adds the specified area to the region deemed dirty for the next double
	 * buffered redraw
	 * 
	 * @param offscreenDirty
	 *            the region to add
	 */
	public void addOffscreenDirty(Rectangle2D offscreenDirty) {
		if (this.offscreenDirty == null && offscreenDirty != null) {
			this.offscreenDirty = (Rectangle2D) offscreenDirty.clone();
		} else if (offscreenDirty != null) {
			this.offscreenDirty.add(offscreenDirty);
		}
	}

	/**
	 * Clears the region deemed dirty for the next double buffered redraw
	 */
	public void clearOffscreenDirty() {
		offscreenDirty = null;
	}
	
	/**
	 * Schedules the offscreen resources taken by the offscreen buffer to
	 * be reclaimed. Note that this does not force garbage collection
	 */
	public void releaseOffscreenResources() {
		offscreen.flush();
		offgraphics.dispose();
		offscreen = null;
		offgraphics = null;
	}
	
	/**
	 * Utility method to draw the off screen buffer
	 * 
	 * @param dx1
	 *            the <i>x</i> coordinate of the first corner of the
	 *            destination rectangle.
	 * @param dy1
	 *            the <i>y</i> coordinate of the first corner of the
	 *            destination rectangle.
	 * @param dx2
	 *            the <i>x</i> coordinate of the second corner of the
	 *            destination rectangle.
	 * @param dy2
	 *            the <i>y</i> coordinate of the second corner of the
	 *            destination rectangle.
	 * @param sx1
	 *            the <i>x</i> coordinate of the first corner of the source
	 *            rectangle.
	 * @param sy1
	 *            the <i>y</i> coordinate of the first corner of the source
	 *            rectangle.
	 * @param sx2
	 *            the <i>x</i> coordinate of the second corner of the source
	 *            rectangle.
	 * @param sy2
	 *            the <i>y</i> coordinate of the second corner of the source
	 *            rectangle.
	 * @return <code>true</code> if the current output representation is
	 *         complete; <code>false</code> otherwise.
	 */
	public boolean drawImage(int dx1, int dy1, int dx2, int dy2, int sx1,
			int sy1, int sx2, int sy2) {
		getOffgraphics();
		return getGraphics().drawImage(offscreen, (int) sx1, (int) sy1,
				(int) sx2, (int) sy2, (int) sx1, (int) sy1, (int) sx2,
				(int) sy2, this);
	}

	public boolean drawImage(Graphics g) {
		Rectangle rect = getBounds();
		return getGraphics().drawImage(offscreen, rect.x, rect.y,
				rect.x + rect.width, rect.y + rect.height, rect.x, rect.y,
				rect.x + rect.width, rect.y + rect.height, this);

	}

	/**
	 * Returns the background image.
	 * 
	 * @return Returns the backgroundImage.
	 */
	public ImageIcon getBackgroundImage() {
		return backgroundImage;
	}

	/**
	 * Sets the background image. Fires a property change event for
	 * {@link #PROPERTY_BACKGROUNDIMAGE}.
	 * 
	 * @param backgroundImage
	 *            The backgroundImage to set.
	 */
	public void setBackgroundImage(ImageIcon backgroundImage) {
		ImageIcon oldValue = this.backgroundImage;
		this.backgroundImage = backgroundImage;
		clearOffscreen();
		firePropertyChange(PROPERTY_BACKGROUNDIMAGE, oldValue, backgroundImage);
	}

	/**
	 * Override parent to clear offscreen double buffer
	 */
	public void setBackground(Color bg) {
		clearOffscreen();
		super.setBackground(bg);
	}

	/**
	 * @return the backgroundScaled
	 */
	public boolean isBackgroundScaled() {
		return backgroundScaled;
	}

	/**
	 * @return the offscreenOffset
	 */
	public Point2D getOffscreenOffset() {
		return offscreenOffset;
	}

	/**
	 * @param offscreenOffset the offscreenOffset to set
	 */
	public void setOffscreenOffset(Point2D offscreenOffset) {
		this.offscreenOffset = offscreenOffset;
	}

	/**
	 * @return the volatileOffscreen
	 */
	public boolean isVolatileOffscreen() {
		return volatileOffscreen;
	}

	/**
	 * @param volatileOffscreen the volatileOffscreen to set
	 */
	public void setVolatileOffscreen(boolean volatileOffscreen) {
		this.volatileOffscreen = volatileOffscreen;
	}

	/**
	 * @param backgroundScaled
	 *            the backgroundScaled to set
	 */
	public void setBackgroundScaled(boolean backgroundScaled) {
		this.backgroundScaled = backgroundScaled;
	}

	/**
	 * @return the backgroundComponent
	 */
	public Component getBackgroundComponent() {
		return backgroundComponent;
	}

	/**
	 * @param backgroundComponent
	 *            the backgroundComponent to set
	 */
	public void setBackgroundComponent(Component backgroundComponent) {
		clearOffscreen();
		this.backgroundComponent = backgroundComponent;
	}

	/*
	 * Overriden to change painting style for opaque components
	 * @see javax.swing.JComponent#setOpaque(boolean)
	 */
	public void setOpaque(boolean opaque) {
		// Due to problems with XOR painting on transparent backgrounds
		// switch off XOR for non-opaque components
		if (!opaque) {
			setXorEnabled(false);
		}
		super.setOpaque(opaque);
	}

	/**
	 * Returns the <code>GraphModel</code> that is providing the data.
	 * 
	 * @return the model that is providing the data
	 */
	public GraphModel getModel() {
		return graphModel;
	}

	/**
	 * Sets the <code>GraphModel</code> that will provide the data. Note:
	 * Updates the current GraphLayoutCache's model using setModel if the
	 * GraphLayoutCache points to a different model.
	 * <p>
	 * Fires a property change for the GRAPH_MODEL_PROPERTY.
	 * 
	 * @param newModel
	 *            the <code>GraphModel</code> that is to provide the data
	 */
	public void setModel(GraphModel newModel) {
		GraphModel oldModel = graphModel;
		graphModel = newModel;
		clearOffscreen();
		firePropertyChange(GRAPH_MODEL_PROPERTY, oldModel, graphModel);
		// FIX: Use Listener
		if (graphLayoutCache != null
				&& graphLayoutCache.getModel() != graphModel)
			graphLayoutCache.setModel(graphModel);
		clearSelection();
		invalidate();
	}

	/**
	 * Returns the <code>GraphLayoutCache</code> that is providing the
	 * view-data.
	 * 
	 * @return the view that is providing the view-data
	 */
	public GraphLayoutCache getGraphLayoutCache() {
		return graphLayoutCache;
	}

	/**
	 * Sets the <code>GraphLayoutCache</code> that will provide the view-data.
	 * <p>
	 * Note: Updates the graphs's model using using the model from the layout
	 * cache.
	 * <p>
	 * Fires a property change for the GRAPH_LAYOUT_CACHE_PROPERTY.
	 * 
	 * @param newLayoutCache
	 *            the <code>GraphLayoutCache</code> that is to provide the
	 *            view-data
	 */
	public void setGraphLayoutCache(GraphLayoutCache newLayoutCache) {
		if (!isSelectionEmpty())
		{
			clearSelection();
		}
		GraphLayoutCache oldLayoutCache = graphLayoutCache;
		graphLayoutCache = newLayoutCache;
		clearOffscreen();
		firePropertyChange(GRAPH_LAYOUT_CACHE_PROPERTY, oldLayoutCache,
				graphLayoutCache);
		if (graphLayoutCache != null
				&& graphLayoutCache.getModel() != getModel()) {
			setModel(graphLayoutCache.getModel());
		} else {
			// Forces an update of the layout cache internal state (ports field)
			graphLayoutCache.update();
		}
		invalidate();
	}

	/**
	 * Returns the <code>MarqueeHandler</code> that will handle marquee
	 * selection.
	 */
	public BasicMarqueeHandler getMarqueeHandler() {
		return marquee;
	}

	/**
	 * Sets the <code>MarqueeHandler</code> that will handle marquee
	 * selection.
	 * 
	 * @param newMarquee
	 *            the <code>BasicMarqueeHandler</code> that is to provide
	 *            marquee handling
	 */
	public void setMarqueeHandler(BasicMarqueeHandler newMarquee) {
		BasicMarqueeHandler oldMarquee = marquee;
		marquee = newMarquee;
		firePropertyChange(MARQUEE_HANDLER_PROPERTY, oldMarquee, newMarquee);
		invalidate();
	}

	/**
	 * Determines what happens when editing is interrupted by selecting another
	 * cell in the graph, a change in the graph's data, or by some other means.
	 * Setting this property to <code>true</code> causes the changes to be
	 * automatically saved when editing is interrupted.
	 * <p>
	 * Fires a property change for the INVOKES_STOP_CELL_EDITING_PROPERTY.
	 * 
	 * @param newValue
	 *            true means that <code>stopCellEditing</code> is invoked when
	 *            editing is interruped, and data is saved; false means that
	 *            <code>cancelCellEditing</code> is invoked, and changes are
	 *            lost
	 */
	public void setInvokesStopCellEditing(boolean newValue) {
		boolean oldValue = invokesStopCellEditing;
		invokesStopCellEditing = newValue;
		firePropertyChange(INVOKES_STOP_CELL_EDITING_PROPERTY, oldValue,
				newValue);
	}

	/**
	 * Returns the indicator that tells what happens when editing is
	 * interrupted.
	 * 
	 * @return the indicator that tells what happens when editing is interrupted
	 * @see #setInvokesStopCellEditing
	 * 
	 */
	public boolean getInvokesStopCellEditing() {
		return invokesStopCellEditing;
	}

	/**
	 * Returns <code>true</code> if the graph and the cell are editable. This
	 * is invoked from the UI before editing begins to ensure that the given
	 * cell can be edited.
	 * 
	 * @return true if the specified cell is editable
	 * @see #isEditable
	 * 
	 */
	public boolean isCellEditable(Object cell) {
		if (cell != null) {
			CellView view = graphLayoutCache.getMapping(cell, false);
			if (view != null) {
				return isEditable()
						&& GraphConstants.isEditable(view.getAllAttributes());
			}
		}
		return false;
	}

	/**
	 * Overrides <code>JComponent</code>'s<code>getToolTipText</code>
	 * method in order to allow the graph to create a tooltip for the topmost
	 * cell under the mousepointer. This differs from JTree where the renderers
	 * tooltip is used.
	 * <p>
	 * NOTE: For <code>JGraph</code> to properly display tooltips of its
	 * renderers, <code>JGraph</code> must be a registered component with the
	 * <code>ToolTipManager</code>. This can be done by invoking
	 * <code>ToolTipManager.sharedInstance().registerComponent(graph)</code>.
	 * This is not done automatically!
	 * 
	 * @param e
	 *            the <code>MouseEvent</code> that initiated the
	 *            <code>ToolTip</code> display
	 * @return a string containing the tooltip or <code>null</code> if
	 *         <code>event</code> is null
	 */
	public String getToolTipText(MouseEvent e) {
		if (e != null) {
			Object cell = getFirstCellForLocation(e.getX(), e.getY());
			CellView view = getGraphLayoutCache().getMapping(cell, false);
			if (view != null) {
				Component c = view.getRendererComponent(this, false, false,
						false);
				if (c instanceof JComponent) {
					Rectangle2D rect = getCellBounds(cell);
					Point2D where = fromScreen(e.getPoint());
					// Pass the event to the renderer in graph coordinates;
					// the renderer is ignorant of screen scaling
					e = new MouseEvent(c, e.getID(), e.getWhen(), e
							.getModifiers(),
							(int) (where.getX() - rect.getX()), (int) (where
									.getY() - rect.getY()), e.getClickCount(),
							e.isPopupTrigger());
					return ((JComponent) c).getToolTipText(e);
				}
			}
		}
		return super.getToolTipText(e);
	}

	//
	// The following are convenience methods that get forwarded to the
	// current GraphSelectionModel.
	//
	/**
	 * Sets the graph's selection model. When a <code>null</code> value is
	 * specified an emtpy <code>selectionModel</code> is used, which does not
	 * allow selections.
	 * 
	 * @param selectionModel
	 *            the <code>GraphSelectionModel</code> to use, or
	 *            <code>null</code> to disable selections
	 * @see GraphSelectionModel
	 * 
	 */
	public void setSelectionModel(GraphSelectionModel selectionModel) {
		if (selectionModel == null)
			selectionModel = EmptySelectionModel.sharedInstance();
		GraphSelectionModel oldValue = this.selectionModel;
		// Remove Redirector From Old Selection Model
		if (this.selectionModel != null && selectionRedirector != null)
			this.selectionModel
					.removeGraphSelectionListener(selectionRedirector);
		this.selectionModel = selectionModel;
		// Add Redirector To New Selection Model
		if (selectionRedirector != null)
			this.selectionModel.addGraphSelectionListener(selectionRedirector);
		firePropertyChange(SELECTION_MODEL_PROPERTY, oldValue,
				this.selectionModel);
	}

	/**
	 * Returns the model for selections. This should always return a non-
	 * <code>null</code> value. If you don't want to allow anything to be
	 * selected set the selection model to <code>null</code>, which forces an
	 * empty selection model to be used.
	 * 
	 * @return the current selection model
	 * @see #setSelectionModel
	 * 
	 */
	public GraphSelectionModel getSelectionModel() {
		return selectionModel;
	}

	/**
	 * Clears the selection.
	 */
	public void clearSelection() {
		getSelectionModel().clearSelection();
	}

	/**
	 * Returns true if the selection is currently empty.
	 * 
	 * @return true if the selection is currently empty
	 */
	public boolean isSelectionEmpty() {
		return getSelectionModel().isSelectionEmpty();
	}

	/**
	 * Adds a listener for <code>GraphSelection</code> events.
	 * 
	 * @param tsl
	 *            the <code>GraphSelectionListener</code> that will be
	 *            notified when a cell is selected or deselected (a "negative
	 *            selection")
	 */
	public void addGraphSelectionListener(GraphSelectionListener tsl) {
		listenerList.add(GraphSelectionListener.class, tsl);
		if (listenerList.getListenerCount(GraphSelectionListener.class) != 0
				&& selectionRedirector == null) {
			selectionRedirector = new GraphSelectionRedirector();
			selectionModel.addGraphSelectionListener(selectionRedirector);
		}
	}

	/**
	 * Removes a <code>GraphSelection</code> listener.
	 * 
	 * @param tsl
	 *            the <code>GraphSelectionListener</code> to remove
	 */
	public void removeGraphSelectionListener(GraphSelectionListener tsl) {
		listenerList.remove(GraphSelectionListener.class, tsl);
		if (listenerList.getListenerCount(GraphSelectionListener.class) == 0
				&& selectionRedirector != null) {
			selectionModel.removeGraphSelectionListener(selectionRedirector);
			selectionRedirector = null;
		}
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @param e
	 *            the <code>GraphSelectionEvent</code> generated by the
	 *            <code>GraphSelectionModel</code> when a cell is selected or
	 *            deselected
	 * @see javax.swing.event.EventListenerList
	 * 
	 */
	protected void fireValueChanged(GraphSelectionEvent e) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GraphSelectionListener.class) {
				((GraphSelectionListener) listeners[i + 1]).valueChanged(e);
			}
		}
	}

	/**
	 * Selects the specified cell.
	 * 
	 * @param cell
	 *            the <code>Object</code> specifying the cell to select
	 */
	public void setSelectionCell(Object cell) {
		getSelectionModel().setSelectionCell(cell);
	}

	/**
	 * Selects the specified cells.
	 * 
	 * @param cells
	 *            an array of objects that specifies the cells to select
	 */
	public void setSelectionCells(Object[] cells) {
		getSelectionModel().setSelectionCells(cells);
	}

	/**
	 * Adds the cell identified by the specified <code>Object</code> to the
	 * current selection.
	 * 
	 * @param cell
	 *            the cell to be added to the selection
	 */
	public void addSelectionCell(Object cell) {
		getSelectionModel().addSelectionCell(cell);
	}

	/**
	 * Adds each cell in the array of cells to the current selection.
	 * 
	 * @param cells
	 *            an array of objects that specifies the cells to add
	 */
	public void addSelectionCells(Object[] cells) {
		getSelectionModel().addSelectionCells(cells);
	}

	/**
	 * Removes the cell identified by the specified Object from the current
	 * selection.
	 * 
	 * @param cell
	 *            the cell to be removed from the selection
	 */
	public void removeSelectionCell(Object cell) {
		getSelectionModel().removeSelectionCell(cell);
	}

	/**
	 * Returns the first selected cell.
	 * 
	 * @return the <code>Object</code> for the first selected cell, or
	 *         <code>null</code> if nothing is currently selected
	 */
	public Object getSelectionCell() {
		return getSelectionModel().getSelectionCell();
	}

	/**
	 * Returns all selected cells.
	 * 
	 * @return an array of objects representing the selected cells, or
	 *         <code>null</code> if nothing is currently selected
	 */
	public Object[] getSelectionCells() {
		return getSelectionModel().getSelectionCells();
	}

	/**
	 * Returns all selected cells in <code>cells</code>.
	 */
	public Object[] getSelectionCells(Object[] cells) {
		if (cells != null) {
			List selected = new ArrayList(cells.length);
			for (int i = 0; i < cells.length; i++) {
				if (isCellSelected(cells[i]))
					selected.add(cells[i]);
			}
			return selected.toArray();
		}
		return null;
	}

	/**
	 * Returns the selection cell at the specified location.
	 * 
	 * @return Returns the selection cell for <code>pt</code>.
	 */
	public Object getSelectionCellAt(Point2D pt) {
		pt = fromScreen((Point2D) pt.clone());
		Object[] cells = getSelectionCells();
		if (cells != null) {
			for (int i = 0; i < cells.length; i++)
				if (getCellBounds(cells[i]).contains(pt.getX(), pt.getY()))
					return cells[i];
		}
		return null;
	}

	/**
	 * Returns the number of cells selected.
	 * 
	 * @return the number of cells selected
	 */
	public int getSelectionCount() {
		return getSelectionModel().getSelectionCount();
	}

	/**
	 * Returns true if the cell is currently selected.
	 * 
	 * @param cell
	 *            an object identifying a cell
	 * @return true if the cell is selected
	 */
	public boolean isCellSelected(Object cell) {
		return getSelectionModel().isCellSelected(cell);
	}

	/**
	 * Scrolls to the specified cell. Only works when this <code>JGraph</code>
	 * is contained in a <code>JScrollPane</code>.
	 * 
	 * @param cell
	 *            the object identifying the cell to bring into view
	 */
	public void scrollCellToVisible(Object cell) {
		Rectangle2D bounds = getCellBounds(cell);
		if (bounds != null) {
			Rectangle2D b2 = toScreen((Rectangle2D) bounds.clone());
			scrollRectToVisible(new Rectangle((int) b2.getX(), (int) b2.getY(),
					(int) b2.getWidth(), (int) b2.getHeight()));
		}
	}

	/**
	 * Makes sure the specified point is visible.
	 * 
	 * @param p
	 *            the point that should be visible
	 */
	public void scrollPointToVisible(Point2D p) {
		if (p != null)
			scrollRectToVisible(new Rectangle((int) p.getX(), (int) p.getY(),
					1, 1));
	}

	/**
	 * Returns true if the graph is being edited. The item that is being edited
	 * can be obtained using <code>getEditingCell</code>.
	 * 
	 * @return true if the user is currently editing a cell
	 * @see #getSelectionCell
	 * 
	 */
	public boolean isEditing() {
		GraphUI graph = getUI();
		if (graph != null)
			return graph.isEditing(this);
		return false;
	}

	/**
	 * Ends the current editing session. (The
	 * <code>DefaultGraphCellEditor</code> object saves any edits that are
	 * currently in progress on a cell. Other implementations may operate
	 * differently.) Has no effect if the tree isn't being edited. <blockquote>
	 * <b>Note: </b> <br>
	 * To make edit-saves automatic whenever the user changes their position in
	 * the graph, use {@link #setInvokesStopCellEditing}. </blockquote>
	 * 
	 * @return true if editing was in progress and is now stopped, false if
	 *         editing was not in progress
	 */
	public boolean stopEditing() {
		GraphUI graph = getUI();
		if (graph != null)
			return graph.stopEditing(this);
		return false;
	}

	/**
	 * Cancels the current editing session. Has no effect if the graph isn't
	 * being edited.
	 */
	public void cancelEditing() {
		GraphUI graph = getUI();
		if (graph != null)
			graph.cancelEditing(this);
	}

	/**
	 * Selects the specified cell and initiates editing. The edit-attempt fails
	 * if the <code>CellEditor</code> does not allow editing for the specified
	 * item.
	 */
	public void startEditingAtCell(Object cell) {
		GraphUI graph = getUI();
		if (graph != null)
			graph.startEditingAtCell(this, cell);
	}

	/**
	 * Returns the cell that is currently being edited.
	 * 
	 * @return the cell being edited
	 */
	public Object getEditingCell() {
		GraphUI graph = getUI();
		if (graph != null)
			return graph.getEditingCell(this);
		return null;
	}

	/**
	 * Messaged when the graph has changed enough that we need to resize the
	 * bounds, but not enough that we need to remove the cells (e.g cells were
	 * inserted into the graph). You should never have to invoke this, the UI
	 * will invoke this as it needs to. (Note: This is invoked by GraphUI, eg.
	 * after moving.)
	 */
	public void graphDidChange() {
		revalidate();
		repaint();
	}
	
	/**
	 * Repaints the entire graph, regardless of what is marked dirty
	 */
	public void refresh() {
		clearOffscreen();
		repaint();
	}

	// /* (non-Javadoc)
	// * @see javax.swing.JComponent#isOptimizedDrawingEnabled()
	// */
	// @Override
	// public boolean isOptimizedDrawingEnabled() {
	// return true;
	// }

	/**
	 * Returns a {@link BufferedImage} for the graph using inset as an empty
	 * border around the cells of the graph. If bg is null then a transparent
	 * background is applied to the image, else the background is filled with
	 * the bg color. Therefore, one should only use a null background if the
	 * fileformat support transparency, eg. GIF and PNG. For JPG, you can use
	 * <code>Color.WHITE</code> for example.
	 * 
	 * @return Returns an image of the graph.
	 */
	public BufferedImage getImage(Color bg, int inset) {
		// TODO, this method could just use the offscreen if available
		Object[] cells = getRoots();
		Rectangle2D bounds = getCellBounds(cells);
		if (bounds != null) {
			toScreen(bounds);
			GraphicsConfiguration graphicsConfig = getGraphicsConfiguration();
			BufferedImage img = null;
			if (graphicsConfig != null) {
				img = getGraphicsConfiguration().createCompatibleImage(
						(int) bounds.getWidth() + 2 * inset,
						(int) bounds.getHeight() + 2 * inset,
						(bg != null) ? Transparency.OPAQUE
								: Transparency.BITMASK);
			} else {
				img = new BufferedImage((int) bounds.getWidth() + 2 * inset,
						(int) bounds.getHeight() + 2 * inset,
						(bg != null) ? BufferedImage.TYPE_INT_RGB
								: BufferedImage.TYPE_INT_ARGB);
			}

			Graphics2D graphics = img.createGraphics();
			if (bg != null) {
				graphics.setColor(bg);
				graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
			} else {
				graphics.setComposite(AlphaComposite.getInstance(
						AlphaComposite.CLEAR, 0.0f));
				graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
				graphics.setComposite(AlphaComposite.SrcOver);
			}
			graphics.translate((int) (-bounds.getX() + inset), (int) (-bounds
					.getY() + inset));
			print(graphics);
			graphics.dispose();
			return img;
		}
		return null;
	}

	/**
	 * Calculates the clip 
	 * @param change
	 * @return the total region dirty as a result of this change
	 */
	public Rectangle2D getClipRectangle(GraphLayoutCacheChange change) {
		List removed = DefaultGraphModel.getDescendants(getModel(), change.getRemoved());
		Rectangle2D removedBounds = (removed != null && !removed.isEmpty()) ? getCellBounds(removed.toArray()) : null;
		List inserted = DefaultGraphModel.getDescendants(getModel(), change.getInserted());
		Rectangle2D insertedBounds = (inserted != null && !inserted.isEmpty()) ? getCellBounds(inserted.toArray()) : null;
		List changed = DefaultGraphModel.getDescendants(getModel(), change.getChanged());
		Rectangle2D changedBounds = (changed != null && !changed.isEmpty()) ? getCellBounds(changed.toArray()) : null;
		List context = DefaultGraphModel.getDescendants(getModel(), change.getContext());
		Rectangle2D contextBounds = (context != null && !context.isEmpty()) ? getCellBounds(context.toArray()) : null;

		Rectangle2D clip = removedBounds;

		if (clip == null) {
			clip = insertedBounds;
		} else if (insertedBounds != null) {
			clip.add(insertedBounds);
		}

		if (clip == null) {
			clip = changedBounds;
		} else if (changedBounds != null) {
			clip.add(changedBounds);
		}

		if (clip == null) {
			clip = contextBounds;
		} else if (contextBounds != null) {
			clip.add(contextBounds);
		}

		return clip;
	}

	/**
	 * Serialization support.
	 */
	private void writeObject(ObjectOutputStream s) throws IOException {
		Vector values = new Vector();
		s.defaultWriteObject();
		// Save the cellEditor, if its Serializable.
		if (graphModel instanceof Serializable) {
			values.addElement("graphModel");
			values.addElement(graphModel);
		}
		// Save the graphModel, if its Serializable.
		values.addElement("graphLayoutCache");
		values.addElement(graphLayoutCache);

		// Save the selectionModel, if its Serializable.
		if (selectionModel instanceof Serializable) {
			values.addElement("selectionModel");
			values.addElement(selectionModel);
		}
		// Save the marquee handler, if its Serializable.
		if (marquee instanceof Serializable) {
			values.addElement("marquee");
			values.addElement(marquee);
		}
		s.writeObject(values);
		if (getUIClassID().equals(uiClassID)) {
			/*
			 * byte count = JComponent.getWriteObjCounter(this);
			 * JComponent.setWriteObjCounter(this, --count);
			 */
			if (/* count == 0 && */
			ui != null) {
				ui.installUI(this);
			}
		}
	}

	/**
	 * Serialization support.
	 */
	private void readObject(ObjectInputStream s) throws IOException,
			ClassNotFoundException {
		s.defaultReadObject();
		Vector values = (Vector) s.readObject();
		int indexCounter = 0;
		int maxCounter = values.size();
		if (indexCounter < maxCounter
				&& values.elementAt(indexCounter).equals("graphModel")) {
			graphModel = (GraphModel) values.elementAt(++indexCounter);
			indexCounter++;
		}
		if (indexCounter < maxCounter
				&& values.elementAt(indexCounter).equals("graphLayoutCache")) {
			graphLayoutCache = (GraphLayoutCache) values
					.elementAt(++indexCounter);
			indexCounter++;
		}
		if (indexCounter < maxCounter
				&& values.elementAt(indexCounter).equals("selectionModel")) {
			selectionModel = (GraphSelectionModel) values
					.elementAt(++indexCounter);
			indexCounter++;
		}
		if (indexCounter < maxCounter
				&& values.elementAt(indexCounter).equals("marquee")) {
			marquee = (BasicMarqueeHandler) values.elementAt(++indexCounter);
			indexCounter++;
		}
		// Reinstall the redirector.
		if (listenerList.getListenerCount(GraphSelectionListener.class) != 0) {
			selectionRedirector = new GraphSelectionRedirector();
			selectionModel.addGraphSelectionListener(selectionRedirector);
		}
	}

	/**
	 * <code>EmptySelectionModel</code> is a <code>GraphSelectionModel</code>
	 * that does not allow anything to be selected.
	 * <p>
	 * <strong>Warning: </strong> Serialized objects of this class will not be
	 * compatible with future Swing releases. The current serialization support
	 * is appropriate for short term storage or RMI between applications running
	 * the same version of Swing. A future release of Swing will provide support
	 * for long term persistence.
	 */
	public static class EmptySelectionModel extends DefaultGraphSelectionModel {

		/** Unique shared instance. */
		protected static final EmptySelectionModel sharedInstance = new EmptySelectionModel();

		/**
		 * A <code>null</code> implementation that constructs an
		 * EmptySelectionModel.
		 */
		public EmptySelectionModel() {
			super(null);
		}

		/** Returns a shared instance of an empty selection model. */
		static public EmptySelectionModel sharedInstance() {
			return sharedInstance;
		}

		/** A <code>null</code> implementation that selects nothing. */
		public void setSelectionCells(Object[] cells) {
		}

		/** A <code>null</code> implementation that adds nothing. */
		public void addSelectionCells(Object[] cells) {
		}

		/** A <code>null</code> implementation that removes nothing. */
		public void removeSelectionCells(Object[] cells) {
		}
	}

	/**
	 * Handles creating a new <code>GraphSelectionEvent</code> with the
	 * <code>JGraph</code> as the source and passing it off to all the
	 * listeners.
	 * <p>
	 * <strong>Warning: </strong> Serialized objects of this class will not be
	 * compatible with future Swing releases. The current serialization support
	 * is appropriate for short term storage or RMI between applications running
	 * the same version of Swing. A future release of Swing will provide support
	 * for long term persistence.
	 */
	protected class GraphSelectionRedirector implements Serializable,
			GraphSelectionListener {

		/**
		 * Invoked by the <code>GraphSelectionModel</code> when the selection
		 * changes.
		 * 
		 * @param e
		 *            the <code>GraphSelectionEvent</code> generated by the
		 *            <code>GraphSelectionModel</code>
		 */
		public void valueChanged(GraphSelectionEvent e) {
			GraphSelectionEvent newE;
			newE = (GraphSelectionEvent) e.cloneWithSource(JGraph.this);
			fireValueChanged(newE);
		}
	} // End of class JGraph.GraphSelectionRedirector

	//
	// Scrollable interface
	//
	/**
	 * Returns the preferred display size of a <code>JGraph</code>. The
	 * height is determined from <code>getPreferredWidth</code>.
	 * 
	 * @return the graph's preferred size
	 */
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/**
	 * Returns the amount to increment when scrolling. The amount is 4.
	 * 
	 * @param visibleRect
	 *            the view area visible within the viewport
	 * @param orientation
	 *            either <code>SwingConstants.VERTICAL</code> or
	 *            <code>SwingConstants.HORIZONTAL</code>
	 * @param direction
	 *            less than zero to scroll up/left, greater than zero for
	 *            down/right
	 * @return the "unit" increment for scrolling in the specified direction
	 * @see javax.swing.JScrollBar#setUnitIncrement(int)
	 * 
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.VERTICAL) {
			return 2;
		}
		return 4;
	}

	/**
	 * Returns the amount for a block increment, which is the height or width of
	 * <code>visibleRect</code>, based on <code>orientation</code>.
	 * 
	 * @param visibleRect
	 *            the view area visible within the viewport
	 * @param orientation
	 *            either <code>SwingConstants.VERTICAL</code> or
	 *            <code>SwingConstants.HORIZONTAL</code>
	 * @param direction
	 *            less than zero to scroll up/left, greater than zero for
	 *            down/right.
	 * @return the "block" increment for scrolling in the specified direction
	 * @see javax.swing.JScrollBar#setBlockIncrement(int)
	 * 
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return (orientation == SwingConstants.VERTICAL) ? visibleRect.height
				: visibleRect.width;
	}

	/**
	 * Returns false to indicate that the width of the viewport does not
	 * determine the width of the graph, unless the preferred width of the graph
	 * is smaller than the viewports width. In other words: ensure that the
	 * graph is never smaller than its viewport.
	 * 
	 * @return false
	 * @see Scrollable#getScrollableTracksViewportWidth
	 * 
	 */
	public boolean getScrollableTracksViewportWidth() {
		if (getParent() instanceof JViewport) {
			return (((JViewport) getParent()).getWidth() > getPreferredSize().width);
		}
		return false;
	}

	/**
	 * Returns false to indicate that the height of the viewport does not
	 * determine the height of the graph, unless the preferred height of the
	 * graph is smaller than the viewports height. In other words: ensure that
	 * the graph is never smaller than its viewport.
	 * 
	 * @return false
	 * @see Scrollable#getScrollableTracksViewportHeight
	 * 
	 */
	public boolean getScrollableTracksViewportHeight() {
		if (getParent() instanceof JViewport) {
			return (((JViewport) getParent()).getHeight() > getPreferredSize().height);
		}
		return false;
	}

	/**
	 * Returns a string representation of this <code>JGraph</code>. This
	 * method is intended to be used only for debugging purposes, and the
	 * content and format of the returned string may vary between
	 * implementations. The returned string may be empty but may not be
	 * <code>null</code>.
	 * 
	 * @return a string representation of this <code>JGraph</code>.
	 */
	protected String paramString() {
		String editableString = (editable ? "true" : "false");
		String invokesStopCellEditingString = (invokesStopCellEditing ? "true"
				: "false");
		return super.paramString() + ",editable=" + editableString
				+ ",invokesStopCellEditing=" + invokesStopCellEditingString;
	}

	public static void main(String[] args) {
		System.out.println(VERSION);
	}
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

