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

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.util.Debug;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.export.Savable;
import com.ardor3d.util.pool.ObjectPool;

/**
 * Vector3 represents a point or vector in a three dimensional system. This implementation stores its data in
 * double-precision.
 */
public class Vector3 implements Cloneable, Savable, Externalizable, ReadOnlyVector3 {

    private static final long serialVersionUID = 1L;

    private static final Vector3Pool VEC_POOL = new Vector3Pool(11);

    /**
     * 0, 0, 0
     */
    public final static ReadOnlyVector3 ZERO = new Vector3(0, 0, 0);

    /**
     * 1, 0, 0
     */
    public final static ReadOnlyVector3 UNIT_X = new Vector3(1, 0, 0);
    /**
     * 0, 1, 0
     */
    public final static ReadOnlyVector3 UNIT_Y = new Vector3(0, 1, 0);
    /**
     * 0, 0, 1
     */
    public final static ReadOnlyVector3 UNIT_Z = new Vector3(0, 0, 1);
    /**
     * 1, 1, 1
     */
    public final static ReadOnlyVector3 UNIT_XYZ = new Vector3(1, 1, 1);

    protected double _x = 0;
    protected double _y = 0;
    protected double _z = 0;

    /**
     * Constructs a new vector set to (0, 0, 0).
     */
    public Vector3() {
        this(0, 0, 0);
    }

    /**
     * Constructs a new vector set to the (x, y, z) values of the given source vector.
     * 
     * @param src
     */
    public Vector3(final ReadOnlyVector3 src) {
        this(src.getX(), src.getY(), src.getZ());
    }

    /**
     * Constructs a new vector set to (x, y, z).
     * 
     * @param x
     * @param y
     * @param z
     */
    public Vector3(final double x, final double y, final double z) {
        _x = x;
        _y = y;
        _z = z;
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
     * @param index
     * @return x value if index == 0, y value if index == 1 or z value if index == 2
     * @throws IllegalArgumentException
     *             if index is not one of 0, 1, 2.
     */
    public double getValue(final int index) {
        switch (index) {
            case 0:
                return getX();
            case 1:
                return getY();
            case 2:
                return getZ();
        }
        throw new IllegalArgumentException("index must be either 0, 1 or 2");
    }

    /**
     * @param index
     *            which field index in this vector to set.
     * @param value
     *            to set to one of x, y or z.
     * @throws IllegalArgumentException
     *             if index is not one of 0, 1, 2.
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
        }
        throw new IllegalArgumentException("index must be either 0, 1 or 2");
    }

    /**
     * Stores the double values of this vector in the given double array.
     * 
     * @param store
     *            if null, a new double[3] array is created.
     * @return the double array
     * @throws NullPointerException
     *             if store is null.
     * @throws ArrayIndexOutOfBoundsException
     *             if store is not at least length 3.
     */
    public double[] toArray(double[] store) {
        if (store == null) {
            store = new double[3];
        }

        // do last first to ensure size is correct before any edits occur.
        store[2] = getZ();
        store[1] = getY();
        store[0] = getX();
        return store;
    }

    /**
     * Stores the double values of this vector in the given float array.
     * 
     * @param store
     *            if null, a new float[3] array is created.
     * @return the float array
     * @throws NullPointerException
     *             if store is null.
     * @throws ArrayIndexOutOfBoundsException
     *             if store is not at least length 3.
     */
    public float[] toFloatArray(float[] store) {
        if (store == null) {
            store = new float[3];
        }

        // do last first to ensure size is correct before any edits occur.
        store[2] = (float) getZ();
        store[1] = (float) getY();
        store[0] = (float) getX();
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
     * Sets the value of this vector to (x, y, z)
     * 
     * @param x
     * @param y
     * @param z
     * @return this vector for chaining
     */
    public Vector3 set(final double x, final double y, final double z) {
        setX(x);
        setY(y);
        setZ(z);
        return this;
    }

    /**
     * Sets the value of this vector to the (x, y, z) values of the provided source vector.
     * 
     * @param source
     * @return this vector for chaining
     * @throws NullPointerException
     *             if source is null.
     */
    public Vector3 set(final ReadOnlyVector3 source) {
        setX(source.getX());
        setY(source.getY());
        setZ(source.getZ());
        return this;
    }

    /**
     * Sets the value of this vector to (0, 0, 0)
     * 
     * @return this vector for chaining
     */
    public Vector3 zero() {
        return set(0, 0, 0);
    }

    /**
     * Adds the given values to those of this vector and returns them in store * @param store the vector to store the
     * result in for return. If null, a new vector object is created and returned. .
     * 
     * @param x
     * @param y
     * @param z
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return (this.x + x, this.y + y, this.z + z)
     */
    public Vector3 add(final double x, final double y, final double z, final Vector3 store) {
        Vector3 result = store;
        if (result == null) {
            result = new Vector3();
        }

        return result.set(getX() + x, getY() + y, getZ() + z);
    }

    /**
     * Increments the values of this vector with the given x, y and z values.
     * 
     * @param x
     * @param y
     * @param z
     * @return this vector for chaining
     */
    public Vector3 addLocal(final double x, final double y, final double z) {
        return set(getX() + x, getY() + y, getZ() + z);
    }

    /**
     * Adds the values of the given source vector to those of this vector and returns them in store.
     * 
     * @param source
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return (this.x + source.x, this.y + source.y, this.z + source.z)
     * @throws NullPointerException
     *             if source is null.
     */
    public Vector3 add(final ReadOnlyVector3 source, final Vector3 store) {
        return add(source.getX(), source.getY(), source.getZ(), store);
    }

    /**
     * Increments the values of this vector with the x, y and z values of the given vector.
     * 
     * @param source
     * @return this vector for chaining
     * @throws NullPointerException
     *             if source is null.
     */
    public Vector3 addLocal(final ReadOnlyVector3 source) {
        return addLocal(source.getX(), source.getY(), source.getZ());
    }

    /**
     * Subtracts the given values from those of this vector and returns them in store.
     * 
     * @param x
     * @param y
     * @param z
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return (this.x - x, this.y - y, this.z - z)
     */
    public Vector3 subtract(final double x, final double y, final double z, final Vector3 store) {
        Vector3 result = store;
        if (result == null) {
            result = new Vector3();
        }

        return result.set(getX() - x, getY() - y, getZ() - z);
    }

    /**
     * Decrements the values of this vector by the given x, y and z values.
     * 
     * @param x
     * @param y
     * @param z
     * @return this vector for chaining
     */
    public Vector3 subtractLocal(final double x, final double y, final double z) {
        return set(getX() - x, getY() - y, getZ() - z);
    }

    /**
     * Subtracts the values of the given source vector from those of this vector and returns them in store.
     * 
     * @param source
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return (this.x - source.x, this.y - source.y, this.z - source.z)
     * @throws NullPointerException
     *             if source is null.
     */
    public Vector3 subtract(final ReadOnlyVector3 source, final Vector3 store) {
        return subtract(source.getX(), source.getY(), source.getZ(), store);
    }

    /**
     * Decrements the values of this vector by the x, y and z values from the given source vector.
     * 
     * @param source
     * @return this vector for chaining
     * @throws NullPointerException
     *             if source is null.
     */
    public Vector3 subtractLocal(final ReadOnlyVector3 source) {
        return subtractLocal(source.getX(), source.getY(), source.getZ());
    }

    /**
     * Multiplies the values of this vector by the given scalar value and returns the result in store.
     * 
     * @param scalar
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return a new vector (this.x * scalar, this.y * scalar, this.z * scalar)
     */
    public Vector3 multiply(final double scalar, final Vector3 store) {
        Vector3 result = store;
        if (result == null) {
            result = new Vector3();
        }

        return result.set(getX() * scalar, getY() * scalar, getZ() * scalar);
    }

    /**
     * Internally modifies the values of this vector by multiplying them each by the given scalar value.
     * 
     * @param scalar
     * @return this vector for chaining
     */
    public Vector3 multiplyLocal(final double scalar) {
        return set(getX() * scalar, getY() * scalar, getZ() * scalar);
    }

    /**
     * Multiplies the values of this vector by the given scale values and returns the result in store.
     * 
     * @param scale
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return a new vector (this.x * scale.x, this.y * scale.y, this.z * scale.z)
     */
    public Vector3 multiply(final ReadOnlyVector3 scale, final Vector3 store) {
        Vector3 result = store;
        if (result == null) {
            result = new Vector3();
        }

        return result.set(getX() * scale.getX(), getY() * scale.getY(), getZ() * scale.getZ());
    }

    /**
     * Internally modifies the values of this vector by multiplying them each by the given scale values.
     * 
     * @param scalar
     * @return this vector for chaining
     */
    public Vector3 multiplyLocal(final ReadOnlyVector3 scale) {
        return set(getX() * scale.getX(), getY() * scale.getY(), getZ() * scale.getZ());
    }

    /**
     * Divides the values of this vector by the given scalar value and returns the result in store.
     * 
     * @param scalar
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return a new vector (this.x / scalar, this.y / scalar, this.z / scalar)
     */
    public Vector3 divide(final double scalar, final Vector3 store) {
        Vector3 result = store;
        if (result == null) {
            result = new Vector3();
        }

        return result.set(getX() / scalar, getY() / scalar, getZ() / scalar);
    }

    /**
     * Internally modifies the values of this vector by dividing them each by the given scalar value.
     * 
     * @param scalar
     * @return this vector for chaining
     * @throws ArithmeticException
     *             if scalar is 0
     */
    public Vector3 divideLocal(final double scalar) {
        final double invScalar = 1.0 / scalar;

        return set(getX() * invScalar, getY() * invScalar, getZ() * invScalar);
    }

    /**
     * Divides the values of this vector by the given scale values and returns the result in store.
     * 
     * @param scale
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return a new vector (this.x / scale.x, this.y / scale.y, this.z / scale.z)
     */
    public Vector3 divide(final ReadOnlyVector3 scale, final Vector3 store) {
        Vector3 result = store;
        if (result == null) {
            result = new Vector3();
        }

        return result.set(getX() / scale.getX(), getY() / scale.getY(), getZ() / scale.getZ());
    }

    /**
     * Internally modifies the values of this vector by dividing them each by the given scale values.
     * 
     * @param scale
     * @return this vector for chaining
     */
    public Vector3 divideLocal(final ReadOnlyVector3 scale) {
        return set(getX() / scale.getX(), getY() / scale.getY(), getZ() / scale.getZ());
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
    public Vector3 scaleAddLocal(final float scale, final ReadOnlyVector3 add) {
        _x = _x * scale + add.getX();
        _y = _y * scale + add.getY();
        _z = _z * scale + add.getZ();
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
    public Vector3 scaleAdd(final double scale, final ReadOnlyVector3 add, final Vector3 store) {
        Vector3 result = store;
        if (result == null) {
            result = new Vector3();
        }

        result.setX(_x * scale + add.getX());
        result.setY(_y * scale + add.getY());
        result.setY(_z * scale + add.getZ());
        return result;
    }

    /**
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return same as multiply(-1, store)
     */
    public Vector3 negate(final Vector3 store) {
        return multiply(-1, store);
    }

    /**
     * @return same as multiplyLocal(-1)
     */
    public Vector3 negateLocal() {
        return multiplyLocal(-1);
    }

    /**
     * Creates a new unit length vector from this one by dividing by length. If the length is 0, (ie, if the vector is
     * 0, 0, 0) then a new vector (0, 0, 0) is returned.
     * 
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return a new unit vector (or 0, 0, 0 if this unit is 0 length)
     */
    public Vector3 normalize(final Vector3 store) {
        final double length = length();
        if (Double.compare(length, 0.0) != 0) {
            return divide(length, store);
        }

        return store != null ? store.set(ZERO) : new Vector3(ZERO);
    }

    /**
     * Converts this vector into a unit vector by dividing it internally by its length. If the length is 0, (ie, if the
     * vector is 0, 0, 0) then no action is taken.
     * 
     * @return this vector for chaining
     */
    public Vector3 normalizeLocal() {
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
    public Vector3 lerp(final ReadOnlyVector3 endVec, final double scalar, final Vector3 store) {
        Vector3 result = store;
        if (result == null) {
            result = new Vector3();
        }

        final double x = (1.0 - scalar) * getX() + scalar * endVec.getX();
        final double y = (1.0 - scalar) * getY() + scalar * endVec.getY();
        final double z = (1.0 - scalar) * getZ() + scalar * endVec.getZ();
        return result.set(x, y, z);
    }

    /**
     * Performs a linear interpolation between this vector and the given end vector, using the given scalar as a
     * percent. iow, if changeAmnt is closer to 0, the result will be closer to the current value of this vector and if
     * it is closer to 1, the result will be closer to the end value. The result is stored back in this vector.
     * 
     * @param endVec
     * @param scalar
     * @return this vector for chaining
     * @throws NullPointerException
     *             if endVec is null.
     */
    public Vector3 lerpLocal(final ReadOnlyVector3 endVec, final double scalar) {
        setX((1.0 - scalar) * getX() + scalar * endVec.getX());
        setY((1.0 - scalar) * getY() + scalar * endVec.getY());
        setZ((1.0 - scalar) * getZ() + scalar * endVec.getZ());
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
     *            the vector to store the result in for return. If null, a new vector object is created and returned. It
     *            IS safe for store to be the same as the begin or end vector.
     * @return a new vector as described above.
     * @throws NullPointerException
     *             if beginVec or endVec are null.
     */
    public static Vector3 lerp(final ReadOnlyVector3 beginVec, final ReadOnlyVector3 endVec, final double scalar,
            final Vector3 store) {
        Vector3 result = store;
        if (result == null) {
            result = new Vector3();
        }

        final double x = (1.0 - scalar) * beginVec.getX() + scalar * endVec.getX();
        final double y = (1.0 - scalar) * beginVec.getY() + scalar * endVec.getY();
        final double z = (1.0 - scalar) * beginVec.getZ() + scalar * endVec.getZ();
        return result.set(x, y, z);
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
     * @throws NullPointerException
     *             if beginVec or endVec are null.
     */
    public Vector3 lerpLocal(final ReadOnlyVector3 beginVec, final ReadOnlyVector3 endVec, final double scalar) {
        setX((1.0 - scalar) * beginVec.getX() + scalar * endVec.getX());
        setY((1.0 - scalar) * beginVec.getY() + scalar * endVec.getY());
        setZ((1.0 - scalar) * beginVec.getZ() + scalar * endVec.getZ());
        return this;
    }

    /**
     * @return the magnitude or distance between the origin (0, 0, 0) and the point described by this vector (x, y, z).
     *         Effectively the square root of the value returned by {@link #lengthSquared()}.
     */
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /**
     * @return the squared magnitude or squared distance between the origin (0, 0, 0) and the point described by this
     *         vector (x, y, z)
     */
    public double lengthSquared() {
        return getX() * getX() + getY() * getY() + getZ() * getZ();
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return the squared distance between the point described by this vector and the given x, y, z point. When
     *         comparing the relative distance between two points it is usually sufficient to compare the squared
     *         distances, thus avoiding an expensive square root operation.
     */
    public double distanceSquared(final double x, final double y, final double z) {
        final double dx = getX() - x;
        final double dy = getY() - y;
        final double dz = getZ() - z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * @param destination
     * @return the squared distance between the point described by this vector and the given destination point. When
     *         comparing the relative distance between two points it is usually sufficient to compare the squared
     *         distances, thus avoiding an expensive square root operation.
     * @throws NullPointerException
     *             if destination is null.
     */
    public double distanceSquared(final ReadOnlyVector3 destination) {
        return distanceSquared(destination.getX(), destination.getY(), destination.getZ());
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return the distance between the point described by this vector and the given x, y, z point.
     */
    public double distance(final double x, final double y, final double z) {
        return Math.sqrt(distanceSquared(x, y, z));
    }

    /**
     * @param destination
     * @return the distance between the point described by this vector and the given destination point.
     * @throws NullPointerException
     *             if destination is null.
     */
    public double distance(final ReadOnlyVector3 destination) {
        return Math.sqrt(distanceSquared(destination));
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return the dot product of this vector with the given x, y, z values.
     */
    public double dot(final double x, final double y, final double z) {
        return (getX() * x) + (getY() * y) + (getZ() * z);
    }

    /**
     * @param vec
     * @return the dot product of this vector with the x, y, z values of the given vector.
     * @throws NullPointerException
     *             if vec is null.
     */
    public double dot(final ReadOnlyVector3 vec) {
        return dot(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return the cross product of this vector with the given x, y, z values.
     */
    public Vector3 cross(final double x, final double y, final double z, final Vector3 store) {
        Vector3 result = store;
        if (result == null) {
            result = new Vector3();
        }
        final double newX = (getY() * z) - (getZ() * y);
        final double newY = (getZ() * x) - (getX() * z);
        final double newZ = (getX() * y) - (getY() * x);
        result.set(newX, newY, newZ);
        return result;
    }

    /**
     * @param vec
     * @param store
     *            the vector to store the result in for return. If null, a new vector object is created and returned.
     * @return the cross product of this vector with the given vector's x, y, z values
     * @throws NullPointerException
     *             if destination is null.
     */
    public Vector3 cross(final ReadOnlyVector3 vec, final Vector3 store) {
        return cross(vec.getX(), vec.getY(), vec.getZ(), store);
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return this vector, set to the cross product of this vector with the given x, y, z values.
     */
    public Vector3 crossLocal(final double x, final double y, final double z) {
        final double newX = ((getY() * z) - (getZ() * y));
        final double newY = ((getZ() * x) - (getX() * z));
        final double newZ = ((getX() * y) - (getY() * x));
        set(newX, newY, newZ);
        return this;
    }

    /**
     * @param vec
     * @return this vector, set to the cross product of this vector with the given vector's x, y, z values
     * @throws NullPointerException
     *             if vec is null.
     */
    public Vector3 crossLocal(final ReadOnlyVector3 vec) {
        return crossLocal(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return the determinate of this vector with the given x, y, z values.
     */
    public double determinant(final double x, final double y, final double z) {
        return (getX() * x) - (getY() * y) - (getZ() * z);
    }

    /**
     * @param vec
     * @return the determinate of this vector with the x, y, z values of the given vector.
     * @throws NullPointerException
     *             if destination is null.
     */
    public double determinant(final ReadOnlyVector3 vec) {
        return determinant(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * @param otherVector
     *            a unit vector to find the angle against
     * @return the minimum angle (in radians) between two vectors. It is assumed that both this vector and the given
     *         vector are unit vectors (normalized).
     * @throws NullPointerException
     *             if otherVector is null.
     */
    public double smallestAngleBetween(final ReadOnlyVector3 otherVector) {
        return Math.acos(dot(otherVector));
    }

    /**
     * Check a vector... if it is null or its doubles are NaN or infinite, return false. Else return true.
     * 
     * @param vector
     *            the vector to check
     * @return true or false as stated above.
     */
    public static boolean isValid(final ReadOnlyVector3 vector) {
        if (vector == null) {
            return false;
        }
        if (Double.isNaN(vector.getX()) || Double.isNaN(vector.getY()) || Double.isNaN(vector.getZ())) {
            return false;
        }
        if (Double.isInfinite(vector.getX()) || Double.isInfinite(vector.getY()) || Double.isInfinite(vector.getZ())) {
            return false;
        }
        return true;
    }

    /**
     * @return the string representation of this vector.
     */
    @Override
    public String toString() {
        return "com.ardor3d.math.Vector3 [X=" + getX() + ", Y=" + getY() + ", Z=" + getZ() + "]";
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

        return result;
    }

    /**
     * @param o
     *            the object to compare for equality
     * @return true if this vector and the provided vector have the same x, y and z values.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReadOnlyVector3)) {
            return false;
        }
        final ReadOnlyVector3 comp = (ReadOnlyVector3) o;
        if (Double.compare(getX(), comp.getX()) == 0 && Double.compare(getY(), comp.getY()) == 0
                && Double.compare(getZ(), comp.getZ()) == 0) {
            return true;
        }
        return false;
    }

    // /////////////////
    // Method for Cloneable
    // /////////////////

    @Override
    public Vector3 clone() {
        try {
            return (Vector3) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new AssertionError(); // can not happen
        }
    }

    // /////////////////
    // Methods for Savable
    // /////////////////

    public Class<? extends Vector3> getClassTag() {
        return this.getClass();
    }

    public void write(final Ardor3DExporter e) throws IOException {
        final OutputCapsule capsule = e.getCapsule(this);
        capsule.write(getX(), "x", 0);
        capsule.write(getY(), "y", 0);
        capsule.write(getZ(), "z", 0);
    }

    public void read(final Ardor3DImporter e) throws IOException {
        final InputCapsule capsule = e.getCapsule(this);
        setX(capsule.readDouble("x", 0));
        setY(capsule.readDouble("y", 0));
        setZ(capsule.readDouble("z", 0));
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
    }

    // /////////////////
    // Methods for creating temp variables (pooling)
    // /////////////////

    /**
     * @return An instance of Vector3 that is intended for temporary use in calculations and so forth. Multiple calls to
     *         the method should return instances of this class that are not currently in use.
     */
    public final static Vector3 fetchTempInstance() {
        if (Debug.useMathPools) {
            return VEC_POOL.fetch();
        } else {
            return new Vector3();
        }
    }

    /**
     * Releases a Vector3 back to be used by a future call to fetchTempInstance. TAKE CARE: this Vector3 object should
     * no longer have other classes referencing it or "Bad Things" will happen.
     * 
     * @param vec
     *            the Vector3 to release.
     */
    public final static void releaseTempInstance(final Vector3 vec) {
        if (Debug.useMathPools) {
            VEC_POOL.release(vec);
        }
    }

    static final class Vector3Pool extends ObjectPool<Vector3> {
        public Vector3Pool(final int initialSize) {
            super(initialSize);
        }

        @Override
        protected Vector3 newInstance() {
            return new Vector3();
        }
    }
}
=======
package org.json2;

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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its external
 * form is a string wrapped in curly braces with colons between the names and
 * values, and commas between the values and names. The internal form is an
 * object having <code>get</code> and <code>opt</code> methods for accessing the
 * values by name, and <code>put</code> methods for adding or replacing values
 * by name. The values can be any of these types: <code>Boolean</code>,
 * <code>JSONArray</code>, <code>JSONObject</code>, <code>Number</code>,
 * <code>String</code>, or the <code>JSONObject.NULL</code> object. A JSONObject
 * constructor can be used to convert an external form JSON text into an
 * internal form whose values can be retrieved with the <code>get</code> and
 * <code>opt</code> methods, or to convert values into a JSON text using the
 * <code>put</code> and <code>toString</code> methods. A <code>get</code> method
 * returns a value if one can be found, and throws an exception if one cannot be
 * found. An <code>opt</code> method returns a default value instead of throwing
 * an exception, and so is useful for obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you. The opt methods differ from the get methods in that they do
 * not throw. Instead, they return a specified value, such as null.
 * <p>
 * The <code>put</code> methods add or replace values in an object. For example,
 *
 * <pre>
 * myString = new JSONObject().put(&quot;JSON&quot;, &quot;Hello, World!&quot;).toString();
 * </pre>
 *
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON syntax rules. The constructors are more forgiving in the texts they
 * will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * <li>Keys can be followed by <code>=</code> or <code>=></code> as well as by
 * <code>:</code>.</li>
 * <li>Values can be followed by <code>;</code> <small>(semicolon)</small> as
 * well as by <code>,</code> <small>(comma)</small>.</li>
 * </ul>
 *
 * @author JSON.org
 * @version 2012-04-20
 */
public class JSONObject {

    /**
     * JSONObject.NULL is equivalent to the value that JavaScript calls null,
     * whilst Java's null is equivalent to the value that JavaScript calls
     * undefined.
     */
     private static final class Null {

        /**
         * There is only intended to be a single instance of the NULL object,
         * so the clone method returns itself.
         * @return     NULL.
         */
        protected final Object clone() {
            return this;
        }

        /**
         * A Null object is equal to the null value and to itself.
         * @param object    An object to test for nullness.
         * @return true if the object parameter is the JSONObject.NULL object
         *  or null.
         */
        public boolean equals(Object object) {
            return object == null || object == this;
        }

        /**
         * Get the "null" string value.
         * @return The string "null".
         */
        public String toString() {
            return "null";
        }
    }


    /**
     * The map where the JSONObject's properties are kept.
     */
    private final Map map;


    /**
     * It is sometimes more convenient and less ambiguous to have a
     * <code>NULL</code> object than to use Java's <code>null</code> value.
     * <code>JSONObject.NULL.equals(null)</code> returns <code>true</code>.
     * <code>JSONObject.NULL.toString()</code> returns <code>"null"</code>.
     */
    public static final Object NULL = new Null();


    /**
     * Construct an empty JSONObject.
     */
    public JSONObject() {
        this.map = new HashMap();
    }


    /**
     * Construct a JSONObject from a subset of another JSONObject.
     * An array of strings is used to identify the keys that should be copied.
     * Missing keys are ignored.
     * @param jo A JSONObject.
     * @param names An array of strings.
     * @throws org.json2.JSONException
     * @exception org.json2.JSONException If a value is a non-finite number or if a name is duplicated.
     */
    public JSONObject(JSONObject jo, String[] names) {
        this();
        for (int i = 0; i < names.length; i += 1) {
            try {
                this.putOnce(names[i], jo.opt(names[i]));
            } catch (Exception ignore) {
            }
        }
    }


    /**
     * Construct a JSONObject from a JSONTokener.
     * @param x A JSONTokener object containing the source string.
     * @throws org.json2.JSONException If there is a syntax error in the source string
     *  or a duplicated key.
     */
    public JSONObject(JSONTokener x) throws JSONException {
        this();
        char c;
        String key;

        if (x.nextClean() != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        for (;;) {
            c = x.nextClean();
            switch (c) {
            case 0:
                throw x.syntaxError("A JSONObject text must end with '}'");
            case '}':
                return;
            default:
                x.back();
                key = x.nextValue().toString();
            }

// The key is followed by ':'. We will also tolerate '=' or '=>'.

            c = x.nextClean();
            if (c == '=') {
                if (x.next() != '>') {
                    x.back();
                }
            } else if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }
            this.putOnce(key, x.nextValue());

// Pairs are separated by ','. We will also tolerate ';'.

            switch (x.nextClean()) {
            case ';':
            case ',':
                if (x.nextClean() == '}') {
                    return;
                }
                x.back();
                break;
            case '}':
                return;
            default:
                throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }


    /**
     * Construct a JSONObject from a Map.
     *
     * @param map A map object that can be used to initialize the contents of
     *  the JSONObject.
     * @throws org.json2.JSONException
     */
    public JSONObject(Map map) {
        this.map = new HashMap();
        if (map != null) {
            Iterator i = map.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry)i.next();
                Object value = e.getValue();
                if (value != null) {
                    this.map.put(e.getKey(), wrap(value));
                }
            }
        }
    }


    /**
     * Construct a JSONObject from an Object using bean getters.
     * It reflects on all of the public methods of the object.
     * For each of the methods with no parameters and a name starting
     * with <code>"get"</code> or <code>"is"</code> followed by an uppercase letter,
     * the method is invoked, and a key and the value returned from the getter method
     * are put into the new JSONObject.
     *
     * The key is formed by removing the <code>"get"</code> or <code>"is"</code> prefix.
     * If the second remaining character is not upper case, then the first
     * character is converted to lower case.
     *
     * For example, if an object has a method named <code>"getName"</code>, and
     * if the result of calling <code>object.getName()</code> is <code>"Larry Fine"</code>,
     * then the JSONObject will contain <code>"name": "Larry Fine"</code>.
     *
     * @param bean An object that has getter methods that should be used
     * to make a JSONObject.
     */
    public JSONObject(Object bean) {
        this();
        this.populateMap(bean);
    }


    /**
     * Construct a JSONObject from an Object, using reflection to find the
     * public members. The resulting JSONObject's keys will be the strings
     * from the names array, and the values will be the field values associated
     * with those keys in the object. If a key is not found or not visible,
     * then it will not be copied into the new JSONObject.
     * @param object An object that has fields that should be used to make a
     * JSONObject.
     * @param names An array of strings, the names of the fields to be obtained
     * from the object.
     */
    public JSONObject(Object object, String names[]) {
        this();
        Class c = object.getClass();
        for (int i = 0; i < names.length; i += 1) {
            String name = names[i];
            try {
                this.putOpt(name, c.getField(name).get(object));
            } catch (Exception ignore) {
            }
        }
    }


    /**
     * Construct a JSONObject from a source JSON text string.
     * This is the most commonly used JSONObject constructor.
     * @param source    A string beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @exception org.json2.JSONException If there is a syntax error in the source
     *  string or a duplicated key.
     */
    public JSONObject(String source) throws JSONException {
        this(new JSONTokener(source));
    }


    /**
     * Construct a JSONObject from a ResourceBundle.
     * @param baseName The ResourceBundle base name.
     * @param locale The Locale to load the ResourceBundle for.
     * @throws org.json2.JSONException If any JSONExceptions are detected.
     */
    public JSONObject(String baseName, Locale locale) throws JSONException {
        this();
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale,
                Thread.currentThread().getContextClassLoader());

// Iterate through the keys in the bundle.

        Enumeration keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key instanceof String) {

// Go through the path, ensuring that there is a nested JSONObject for each
// segment except the last. Add the value using the last segment's name into
// the deepest nested JSONObject.

                String[] path = ((String)key).split("\\.");
                int last = path.length - 1;
                JSONObject target = this;
                for (int i = 0; i < last; i += 1) {
                    String segment = path[i];
                    JSONObject nextTarget = target.optJSONObject(segment);
                    if (nextTarget == null) {
                        nextTarget = new JSONObject();
                        target.put(segment, nextTarget);
                    }
                    target = nextTarget;
                }
                target.put(path[last], bundle.getString((String)key));
            }
        }
    }


    /**
     * Accumulate values under a key. It is similar to the put method except
     * that if there is already an object stored under the key then a
     * JSONArray is stored under the key to hold all of the accumulated values.
     * If there is already a JSONArray, then the new value is appended to it.
     * In contrast, the put method replaces the previous value.
     *
     * If only one value is accumulated that is not a JSONArray, then the
     * result will be the same as using put. But if multiple values are
     * accumulated, then the result will be like append.
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws org.json2.JSONException If the value is an invalid number
     *  or if the key is null.
     */
    public JSONObject accumulate(
        String key,
        Object value
    ) throws JSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, value instanceof JSONArray
                    ? new JSONArray().put(value)
                    : value);
        } else if (object instanceof JSONArray) {
            ((JSONArray)object).put(value);
        } else {
            this.put(key, new JSONArray().put(object).put(value));
        }
        return this;
    }


    /**
     * Append values to the array under a key. If the key does not exist in the
     * JSONObject, then the key is put in the JSONObject with its value being a
     * JSONArray containing the value parameter. If the key was already
     * associated with a JSONArray, then the value parameter is appended to it.
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws org.json2.JSONException If the key is null or if the current value
     *  associated with the key is not a JSONArray.
     */
    public JSONObject append(String key, Object value) throws JSONException {
        testValidity(value);
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, new JSONArray().put(value));
        } else if (object instanceof JSONArray) {
            this.put(key, ((JSONArray)object).put(value));
        } else {
            throw new JSONException("JSONObject[" + key +
                    "] is not a JSONArray.");
        }
        return this;
    }


    /**
     * Produce a string from a double. The string "null" will be returned if
     * the number is not finite.
     * @param  d A double.
     * @return A String.
     */
    public static String doubleToString(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            return "null";
        }

// Shave off trailing zeros and decimal point, if possible.

        String string = Double.toString(d);
        if (string.indexOf('.') > 0 && string.indexOf('e') < 0 &&
                string.indexOf('E') < 0) {
            while (string.endsWith("0")) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }


    /**
     * Get the value object associated with a key.
     *
     * @param key   A key string.
     * @return      The object associated with the key.
     * @throws org.json2.JSONException if the key is not found.
     */
    public Object get(String key) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        Object object = this.opt(key);
        if (object == null) {
            throw new JSONException("JSONObject[" + quote(key) +
                    "] not found.");
        }
        return object;
    }


    /**
     * Get the boolean value associated with a key.
     *
     * @param key   A key string.
     * @return      The truth.
     * @throws org.json2.JSONException
     *  if the value is not a Boolean or the String "true" or "false".
     */
    public boolean getBoolean(String key) throws JSONException {
        Object object = this.get(key);
        if (object.equals(Boolean.FALSE) ||
                (object instanceof String &&
                ((String)object).equalsIgnoreCase("false"))) {
            return false;
        } else if (object.equals(Boolean.TRUE) ||
                (object instanceof String &&
                ((String)object).equalsIgnoreCase("true"))) {
            return true;
        }
        throw new JSONException("JSONObject[" + quote(key) +
                "] is not a Boolean.");
    }


    /**
     * Get the double value associated with a key.
     * @param key   A key string.
     * @return      The numeric value.
     * @throws org.json2.JSONException if the key is not found or
     *  if the value is not a Number object and cannot be converted to a number.
     */
    public double getDouble(String key) throws JSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number
                ? ((Number)object).doubleValue()
                : Double.parseDouble((String)object);
        } catch (Exception e) {
            throw new JSONException("JSONObject[" + quote(key) +
                "] is not a number.");
        }
    }


    /**
     * Get the int value associated with a key.
     *
     * @param key   A key string.
     * @return      The integer value.
     * @throws org.json2.JSONException if the key is not found or if the value cannot
     *  be converted to an integer.
     */
    public int getInt(String key) throws JSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number
                ? ((Number)object).intValue()
                : Integer.parseInt((String)object);
        } catch (Exception e) {
            throw new JSONException("JSONObject[" + quote(key) +
                "] is not an int.");
        }
    }


    /**
     * Get the JSONArray value associated with a key.
     *
     * @param key   A key string.
     * @return      A JSONArray which is the value.
     * @throws org.json2.JSONException if the key is not found or
     *  if the value is not a JSONArray.
     */
    public JSONArray getJSONArray(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONArray) {
            return (JSONArray)object;
        }
        throw new JSONException("JSONObject[" + quote(key) +
                "] is not a JSONArray.");
    }


    /**
     * Get the JSONObject value associated with a key.
     *
     * @param key   A key string.
     * @return      A JSONObject which is the value.
     * @throws org.json2.JSONException if the key is not found or
     *  if the value is not a JSONObject.
     */
    public JSONObject getJSONObject(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONObject) {
            return (JSONObject)object;
        }
        throw new JSONException("JSONObject[" + quote(key) +
                "] is not a JSONObject.");
    }


    /**
     * Get the long value associated with a key.
     *
     * @param key   A key string.
     * @return      The long value.
     * @throws org.json2.JSONException if the key is not found or if the value cannot
     *  be converted to a long.
     */
    public long getLong(String key) throws JSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number
                ? ((Number)object).longValue()
                : Long.parseLong((String)object);
        } catch (Exception e) {
            throw new JSONException("JSONObject[" + quote(key) +
                "] is not a long.");
        }
    }


    /**
     * Get an array of field names from a JSONObject.
     *
     * @return An array of field names, or null if there are no names.
     */
    public static String[] getNames(JSONObject jo) {
        int length = jo.length();
        if (length == 0) {
            return null;
        }
        Iterator iterator = jo.keys();
        String[] names = new String[length];
        int i = 0;
        while (iterator.hasNext()) {
            names[i] = (String)iterator.next();
            i += 1;
        }
        return names;
    }


    /**
     * Get an array of field names from an Object.
     *
     * @return An array of field names, or null if there are no names.
     */
    public static String[] getNames(Object object) {
        if (object == null) {
            return null;
        }
        Class klass = object.getClass();
        Field[] fields = klass.getFields();
        int length = fields.length;
        if (length == 0) {
            return null;
        }
        String[] names = new String[length];
        for (int i = 0; i < length; i += 1) {
            names[i] = fields[i].getName();
        }
        return names;
    }


    /**
     * Get the string associated with a key.
     *
     * @param key   A key string.
     * @return      A string which is the value.
     * @throws org.json2.JSONException if there is no string value for the key.
     */
    public String getString(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof String) {
            return (String)object;
        }
        throw new JSONException("JSONObject[" + quote(key) +
            "] not a string.");
    }


    /**
     * Determine if the JSONObject contains a specific key.
     * @param key   A key string.
     * @return      true if the key exists in the JSONObject.
     */
    public boolean has(String key) {
        return this.map.containsKey(key);
    }


    /**
     * Increment a property of a JSONObject. If there is no such property,
     * create one with a value of 1. If there is such a property, and if
     * it is an Integer, Long, Double, or Float, then add one to it.
     * @param key  A key string.
     * @return this.
     * @throws org.json2.JSONException If there is already a property with this name
     * that is not an Integer, Long, Double, or Float.
     */
    public JSONObject increment(String key) throws JSONException {
        Object value = this.opt(key);
        if (value == null) {
            this.put(key, 1);
        } else if (value instanceof Integer) {
            this.put(key, ((Integer)value).intValue() + 1);
        } else if (value instanceof Long) {
            this.put(key, ((Long)value).longValue() + 1);
        } else if (value instanceof Double) {
            this.put(key, ((Double)value).doubleValue() + 1);
        } else if (value instanceof Float) {
            this.put(key, ((Float)value).floatValue() + 1);
        } else {
            throw new JSONException("Unable to increment [" + quote(key) + "].");
        }
        return this;
    }


    /**
     * Determine if the value associated with the key is null or if there is
     *  no value.
     * @param key   A key string.
     * @return      true if there is no value associated with the key or if
     *  the value is the JSONObject.NULL object.
     */
    public boolean isNull(String key) {
        return JSONObject.NULL.equals(this.opt(key));
    }


    /**
     * Get an enumeration of the keys of the JSONObject.
     *
     * @return An iterator of the keys.
     */
    public Iterator keys() {
        return this.map.keySet().iterator();
    }


    /**
     * Get the number of keys stored in the JSONObject.
     *
     * @return The number of keys in the JSONObject.
     */
    public int length() {
        return this.map.size();
    }


    /**
     * Produce a JSONArray containing the names of the elements of this
     * JSONObject.
     * @return A JSONArray containing the key strings, or null if the JSONObject
     * is empty.
     */
    public JSONArray names() {
        JSONArray ja = new JSONArray();
        Iterator  keys = this.keys();
        while (keys.hasNext()) {
            ja.put(keys.next());
        }
        return ja.length() == 0 ? null : ja;
    }

    /**
     * Produce a string from a Number.
     * @param  number A Number
     * @return A String.
     * @throws org.json2.JSONException If n is a non-finite number.
     */
    public static String numberToString(Number number)
            throws JSONException {
        if (number == null) {
            throw new JSONException("Null pointer");
        }
        testValidity(number);

// Shave off trailing zeros and decimal point, if possible.

        String string = number.toString();
        if (string.indexOf('.') > 0 && string.indexOf('e') < 0 &&
                string.indexOf('E') < 0) {
            while (string.endsWith("0")) {
                string = string.substring(0, string.length() - 1);
            }
            if (string.endsWith(".")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }


    /**
     * Get an optional value associated with a key.
     * @param key   A key string.
     * @return      An object which is the value, or null if there is no value.
     */
    public Object opt(String key) {
        return key == null ? null : this.map.get(key);
    }


    /**
     * Get an optional boolean associated with a key.
     * It returns false if there is no such key, or if the value is not
     * Boolean.TRUE or the String "true".
     *
     * @param key   A key string.
     * @return      The truth.
     */
    public boolean optBoolean(String key) {
        return this.optBoolean(key, false);
    }


    /**
     * Get an optional boolean associated with a key.
     * It returns the defaultValue if there is no such key, or if it is not
     * a Boolean or the String "true" or "false" (case insensitive).
     *
     * @param key              A key string.
     * @param defaultValue     The default.
     * @return      The truth.
     */
    public boolean optBoolean(String key, boolean defaultValue) {
        try {
            return this.getBoolean(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional double associated with a key,
     * or NaN if there is no such key or if its value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A string which is the key.
     * @return      An object which is the value.
     */
    public double optDouble(String key) {
        return this.optDouble(key, Double.NaN);
    }


    /**
     * Get an optional double associated with a key, or the
     * defaultValue if there is no such key or if its value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A key string.
     * @param defaultValue     The default.
     * @return      An object which is the value.
     */
    public double optDouble(String key, double defaultValue) {
        try {
            return this.getDouble(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional int value associated with a key,
     * or zero if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A key string.
     * @return      An object which is the value.
     */
    public int optInt(String key) {
        return this.optInt(key, 0);
    }


    /**
     * Get an optional int value associated with a key,
     * or the default if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A key string.
     * @param defaultValue     The default.
     * @return      An object which is the value.
     */
    public int optInt(String key, int defaultValue) {
        try {
            return this.getInt(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional JSONArray associated with a key.
     * It returns null if there is no such key, or if its value is not a
     * JSONArray.
     *
     * @param key   A key string.
     * @return      A JSONArray which is the value.
     */
    public JSONArray optJSONArray(String key) {
        Object o = this.opt(key);
        return o instanceof JSONArray ? (JSONArray)o : null;
    }


    /**
     * Get an optional JSONObject associated with a key.
     * It returns null if there is no such key, or if its value is not a
     * JSONObject.
     *
     * @param key   A key string.
     * @return      A JSONObject which is the value.
     */
    public JSONObject optJSONObject(String key) {
        Object object = this.opt(key);
        return object instanceof JSONObject ? (JSONObject)object : null;
    }


    /**
     * Get an optional long value associated with a key,
     * or zero if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key   A key string.
     * @return      An object which is the value.
     */
    public long optLong(String key) {
        return this.optLong(key, 0);
    }


    /**
     * Get an optional long value associated with a key,
     * or the default if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key          A key string.
     * @param defaultValue The default.
     * @return             An object which is the value.
     */
    public long optLong(String key, long defaultValue) {
        try {
            return this.getLong(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional string associated with a key.
     * It returns an empty string if there is no such key. If the value is not
     * a string and is not null, then it is converted to a string.
     *
     * @param key   A key string.
     * @return      A string which is the value.
     */
    public String optString(String key) {
        return this.optString(key, "");
    }


    /**
     * Get an optional string associated with a key.
     * It returns the defaultValue if there is no such key.
     *
     * @param key   A key string.
     * @param defaultValue     The default.
     * @return      A string which is the value.
     */
    public String optString(String key, String defaultValue) {
        Object object = this.opt(key);
        return NULL.equals(object) ? defaultValue : object.toString();
    }


    private void populateMap(Object bean) {
        Class klass = bean.getClass();

// If klass is a System class then set includeSuperClass to false.

        boolean includeSuperClass = klass.getClassLoader() != null;

        Method[] methods = includeSuperClass
                ? klass.getMethods()
                : klass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i += 1) {
            try {
                Method method = methods[i];
                if (Modifier.isPublic(method.getModifiers())) {
                    String name = method.getName();
                    String key = "";
                    if (name.startsWith("get")) {
                        if ("getClass".equals(name) ||
                                "getDeclaringClass".equals(name)) {
                            key = "";
                        } else {
                            key = name.substring(3);
                        }
                    } else if (name.startsWith("is")) {
                        key = name.substring(2);
                    }
                    if (key.length() > 0 &&
                            Character.isUpperCase(key.charAt(0)) &&
                            method.getParameterTypes().length == 0) {
                        if (key.length() == 1) {
                            key = key.toLowerCase();
                        } else if (!Character.isUpperCase(key.charAt(1))) {
                            key = key.substring(0, 1).toLowerCase() +
                                key.substring(1);
                        }

                        Object result = method.invoke(bean, (Object[])null);
                        if (result != null) {
                            this.map.put(key, wrap(result));
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
    }


    /**
     * Put a key/boolean pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A boolean which is the value.
     * @return this.
     * @throws org.json2.JSONException If the key is null.
     */
    public JSONObject put(String key, boolean value) throws JSONException {
        this.put(key, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject, where the value will be a
     * JSONArray which is produced from a Collection.
     * @param key   A key string.
     * @param value A Collection value.
     * @return      this.
     * @throws org.json2.JSONException
     */
    public JSONObject put(String key, Collection value) throws JSONException {
        this.put(key, new JSONArray(value));
        return this;
    }


    /**
     * Put a key/double pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A double which is the value.
     * @return this.
     * @throws org.json2.JSONException If the key is null or if the number is invalid.
     */
    public JSONObject put(String key, double value) throws JSONException {
        this.put(key, new Double(value));
        return this;
    }


    /**
     * Put a key/int pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value An int which is the value.
     * @return this.
     * @throws org.json2.JSONException If the key is null.
     */
    public JSONObject put(String key, int value) throws JSONException {
        this.put(key, new Integer(value));
        return this;
    }


    /**
     * Put a key/long pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A long which is the value.
     * @return this.
     * @throws org.json2.JSONException If the key is null.
     */
    public JSONObject put(String key, long value) throws JSONException {
        this.put(key, new Long(value));
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject, where the value will be a
     * JSONObject which is produced from a Map.
     * @param key   A key string.
     * @param value A Map value.
     * @return      this.
     * @throws org.json2.JSONException
     */
    public JSONObject put(String key, Map value) throws JSONException {
        this.put(key, new JSONObject(value));
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject. If the value is null,
     * then the key will be removed from the JSONObject if it is present.
     * @param key   A key string.
     * @param value An object which is the value. It should be of one of these
     *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
     *  or the JSONObject.NULL object.
     * @return this.
     * @throws org.json2.JSONException If the value is non-finite number
     *  or if the key is null.
     */
    public JSONObject put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        if (value != null) {
            testValidity(value);
            this.map.put(key, value);
        } else {
            this.remove(key);
        }
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject, but only if the key and the
     * value are both non-null, and only if there is not already a member
     * with that name.
     * @param key
     * @param value
     * @return his.
     * @throws org.json2.JSONException if the key is a duplicate
     */
    public JSONObject putOnce(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            if (this.opt(key) != null) {
                throw new JSONException("Duplicate key \"" + key + "\"");
            }
            this.put(key, value);
        }
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject, but only if the
     * key and the value are both non-null.
     * @param key   A key string.
     * @param value An object which is the value. It should be of one of these
     *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
     *  or the JSONObject.NULL object.
     * @return this.
     * @throws org.json2.JSONException If the value is a non-finite number.
     */
    public JSONObject putOpt(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            this.put(key, value);
        }
        return this;
    }


    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within </, producing <\/,
     * allowing JSON text to be delivered in HTML. In JSON text, a string
     * cannot contain a control character or an unescaped quote or backslash.
     * @param string A String
     * @return  A String correctly formatted for insertion in a JSON text.
     */
    public static String quote(String string) {
        StringWriter sw = new StringWriter();
        synchronized (sw.getBuffer()) {
            try {
                return quote(string, sw).toString();
            } catch (IOException ignored) {
                // will never happen - we are writing to a string writer
                return "";
            }
        }
    }

    public static Writer quote(String string, Writer w) throws IOException {
        if (string == null || string.length() == 0) {
            w.write("\"\"");
            return w;
        }

        char b;
        char c = 0;
        String hhhh;
        int i;
        int len = string.length();

        w.write('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                w.write('\\');
                w.write(c);
                break;
            case '/':
                if (b == '<') {
                    w.write('\\');
                }
                w.write(c);
                break;
            case '\b':
                w.write("\\b");
                break;
            case '\t':
                w.write("\\t");
                break;
            case '\n':
                w.write("\\n");
                break;
            case '\f':
                w.write("\\f");
                break;
            case '\r':
                w.write("\\r");
                break;
            default:
                if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
                        || (c >= '\u2000' && c < '\u2100')) {
                    hhhh = "000" + Integer.toHexString(c);
                    w.write("\\u" + hhhh.substring(hhhh.length() - 4));
                } else {
                    w.write(c);
                }
            }
        }
        w.write('"');
        return w;
    }

    /**
     * Remove a name and its value, if present.
     * @param key The name to be removed.
     * @return The value that was associated with the name,
     * or null if there was no value.
     */
    public Object remove(String key) {
        return this.map.remove(key);
    }

    /**
     * Try to convert a string into a number, boolean, or null. If the string
     * can't be converted, return the string.
     * @param string A String.
     * @return A simple JSON value.
     */
    public static Object stringToValue(String string) {
        Double d;
        if (string.equals("")) {
            return string;
        }
        if (string.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (string.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (string.equalsIgnoreCase("null")) {
            return JSONObject.NULL;
        }

        /*
         * If it might be a number, try converting it.
         * If a number cannot be produced, then the value will just
         * be a string. Note that the plus and implied string
         * conventions are non-standard. A JSON parser may accept
         * non-JSON forms as long as it accepts all correct JSON forms.
         */

        char b = string.charAt(0);
        if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
            try {
                if (string.indexOf('.') > -1 ||
                        string.indexOf('e') > -1 || string.indexOf('E') > -1) {
                    d = Double.valueOf(string);
                    if (!d.isInfinite() && !d.isNaN()) {
                        return d;
                    }
                } else {
                    Long myLong = new Long(string);
                    if (myLong.longValue() == myLong.intValue()) {
                        return new Integer(myLong.intValue());
                    } else {
                        return myLong;
                    }
                }
            }  catch (Exception ignore) {
            }
        }
        return string;
    }


    /**
     * Throw an exception if the object is a NaN or infinite number.
     * @param o The object to test.
     * @throws org.json2.JSONException If o is a non-finite number.
     */
    public static void testValidity(Object o) throws JSONException {
        if (o != null) {
            if (o instanceof Double) {
                if (((Double)o).isInfinite() || ((Double)o).isNaN()) {
                    throw new JSONException(
                        "JSON does not allow non-finite numbers.");
                }
            } else if (o instanceof Float) {
                if (((Float)o).isInfinite() || ((Float)o).isNaN()) {
                    throw new JSONException(
                        "JSON does not allow non-finite numbers.");
                }
            }
        }
    }


    /**
     * Produce a JSONArray containing the values of the members of this
     * JSONObject.
     * @param names A JSONArray containing a list of key strings. This
     * determines the sequence of the values in the result.
     * @return A JSONArray of values.
     * @throws org.json2.JSONException If any of the values are non-finite numbers.
     */
    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        if (names == null || names.length() == 0) {
            return null;
        }
        JSONArray ja = new JSONArray();
        for (int i = 0; i < names.length(); i += 1) {
            ja.put(this.opt(names.getString(i)));
        }
        return ja;
    }

    /**
     * Make a JSON text of this JSONObject. For compactness, no whitespace
     * is added. If this would not result in a syntactically correct JSON text,
     * then null will be returned instead.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, portable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     */
    public String toString() {
        try {
            return this.toString(0);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Make a prettyprinted JSON text of this JSONObject.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @return a printable, displayable, portable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws org.json2.JSONException If the object contains an invalid number.
     */
    public String toString(int indentFactor) throws JSONException {
        StringWriter w = new StringWriter();
        synchronized (w.getBuffer()) {
            return this.write(w, indentFactor, 0).toString();
        }
    }

    /**
     * Make a JSON text of an Object value. If the object has an
     * value.toJSONString() method, then that method will be used to produce
     * the JSON text. The method is required to produce a strictly
     * conforming text. If the object does not contain a toJSONString
     * method (which is the most common case), then a text will be
     * produced by other means. If the value is an array or Collection,
     * then a JSONArray will be made from it and its toJSONString method
     * will be called. If the value is a MAP, then a JSONObject will be made
     * from it and its toJSONString method will be called. Otherwise, the
     * value's toString method will be called, and the result will be quoted.
     *
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * @param value The value to be serialized.
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws org.json2.JSONException If the value is or contains an invalid number.
     */
    public static String valueToString(Object value) throws JSONException {
        if (value == null || value.equals(null)) {
            return "null";
        }
        if (value instanceof JSONString) {
            Object object;
            try {
                object = ((JSONString)value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            if (object instanceof String) {
                return (String)object;
            }
            throw new JSONException("Bad value from toJSONString: " + object);
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if (value instanceof Boolean || value instanceof JSONObject ||
                value instanceof JSONArray) {
            return value.toString();
        }
        if (value instanceof Map) {
            return new JSONObject((Map)value).toString();
        }
        if (value instanceof Collection) {
            return new JSONArray((Collection)value).toString();
        }
        if (value.getClass().isArray()) {
            return new JSONArray(value).toString();
        }
        return quote(value.toString());
    }

     /**
      * Wrap an object, if necessary. If the object is null, return the NULL
      * object. If it is an array or collection, wrap it in a JSONArray. If
      * it is a map, wrap it in a JSONObject. If it is a standard property
      * (Double, String, et al) then it is already wrapped. Otherwise, if it
      * comes from one of the java packages, turn it into a string. And if
      * it doesn't, try to wrap it in a JSONObject. If the wrapping fails,
      * then null is returned.
      *
      * @param object The object to wrap
      * @return The wrapped value
      */
     public static Object wrap(Object object) {
         try {
             if (object == null) {
                 return NULL;
             }
             if (object instanceof JSONObject || object instanceof JSONArray ||
                     NULL.equals(object)      || object instanceof JSONString ||
                     object instanceof Byte   || object instanceof Character  ||
                     object instanceof Short  || object instanceof Integer    ||
                     object instanceof Long   || object instanceof Boolean    ||
                     object instanceof Float  || object instanceof Double     ||
                     object instanceof String) {
                 return object;
             }

             if (object instanceof Collection) {
                 return new JSONArray((Collection)object);
             }
             if (object.getClass().isArray()) {
                 return new JSONArray(object);
             }
             if (object instanceof Map) {
                 return new JSONObject((Map)object);
             }
             Package objectPackage = object.getClass().getPackage();
             String objectPackageName = objectPackage != null
                 ? objectPackage.getName()
                 : "";
             if (
                 objectPackageName.startsWith("java.") ||
                 objectPackageName.startsWith("javax.") ||
                 object.getClass().getClassLoader() == null
             ) {
                 return object.toString();
             }
             return new JSONObject(object);
         } catch(Exception exception) {
             return null;
         }
     }


     /**
      * Write the contents of the JSONObject as JSON text to a writer.
      * For compactness, no whitespace is added.
      * <p>
      * Warning: This method assumes that the data structure is acyclical.
      *
      * @return The writer.
      * @throws org.json2.JSONException
      */
     public Writer write(Writer writer) throws JSONException {
        return this.write(writer, 0, 0);
    }


    static final Writer writeValue(Writer writer, Object value,
            int indentFactor, int indent) throws JSONException, IOException {
        if (value instanceof JSONObject) {
            ((JSONObject) value).write(writer, indentFactor, indent);
        } else if (value instanceof JSONArray) {
            ((JSONArray) value).write(writer, indentFactor, indent);
        } else if (value instanceof Map) {
            new JSONObject((Map) value).write(writer, indentFactor, indent);
        } else if (value instanceof Collection) {
            new JSONArray((Collection) value).write(writer, indentFactor,
                    indent);
        } else if (value.getClass().isArray()) {
            new JSONArray(value).write(writer, indentFactor, indent);
        } else if (value instanceof Number) {
            writer.write(numberToString((Number) value));
        } else if (value instanceof Boolean) {
            writer.write(value.toString());
        } else if (value instanceof JSONString) {
            Object o;
            try {
                o = ((JSONString) value).toJSONString();
            } catch (Exception e) {
                throw new JSONException(e);
            }
            writer.write(o != null ? o.toString() : quote(value.toString()));
        } else if (value == null || value.equals(null)) {
            writer.write("null");
        } else {
            quote(value.toString(), writer);
        }
        return writer;
    }

    static final void indent(Writer writer, int indent) throws IOException {
        for (int i = 0; i < indent; i += 1) {
            writer.write(' ');
        }
    }

    /**
     * Write the contents of the JSONObject as JSON text to a writer. For
     * compactness, no whitespace is added.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return The writer.
     * @throws org.json2.JSONException
     */
    Writer write(Writer writer, int indentFactor, int indent)
            throws JSONException {
        try {
            boolean commanate = false;
            final int length = this.length();
            Iterator keys = this.keys();
            writer.write('{');

            if (length == 1) {
                Object key = keys.next();
                writer.write(quote(key.toString()));
                writer.write(':');
                if (indentFactor > 0) {
                    writer.write(' ');
                }
                writeValue(writer, this.map.get(key), indentFactor, indent);
            } else if (length != 0) {
                final int newindent = indent + indentFactor;
                while (keys.hasNext()) {
                    Object key = keys.next();
                    if (commanate) {
                        writer.write(',');
                    }
                    if (indentFactor > 0) {
                        writer.write('\n');
                    }
                    indent(writer, newindent);
                    writer.write(quote(key.toString()));
                    writer.write(':');
                    if (indentFactor > 0) {
                        writer.write(' ');
                    }
                    writeValue(writer, this.map.get(key), indentFactor,
                            newindent);
                    commanate = true;
                }
                if (indentFactor > 0) {
                    writer.write('\n');
                }
                indent(writer, indent);
            }
            writer.write('}');
            return writer;
        } catch (IOException exception) {
            throw new JSONException(exception);
        }
     }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

