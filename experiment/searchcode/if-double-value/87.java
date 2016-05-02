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

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its
 * external form is a string wrapped in curly braces with colons between the
 * names and values, and commas between the values and names. The internal form
 * is an object having <code>get</code> and <code>opt</code> methods for
 * accessing the values by name, and <code>put</code> methods for adding or
 * replacing values by name. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>,
 * <code>Number</code>, <code>String</code>, or the <code>JSONObject.NULL</code>
 * object. A JSONObject constructor can be used to convert an external form
 * JSON text into an internal form whose values can be retrieved with the
 * <code>get</code> and <code>opt</code> methods, or to convert values into a
 * JSON text using the <code>put</code> and <code>toString</code> methods.
 * A <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p/>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coersion for you.
 * <p/>
 * The <code>put</code> methods adds values to an object. For example, <pre>
 *     myString = new JSONObject().put("JSON", "Hello, World!").toString();</pre>
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p/>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON sysntax rules.
 * The constructors are more forgiving in the texts they will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces,
 * and if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers
 * and if they are not the reserved words <code>true</code>,
 * <code>false</code>, or <code>null</code>.</li>
 * <li>Keys can be followed by <code>=</code> or <code>=></code> as well as
 * by <code>:</code>.</li>
 * <li>Values can be followed by <code>;</code> <small>(semicolon)</small> as
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
public class JSONObject implements JSONElement,Map {

    /**
     * JSONObject.NULL is equivalent to the value that JavaScript calls null,
     * whilst Java's null is equivalent to the value that JavaScript calls
     * undefined.
     */
    public static final class Null {

        /**
         * There is only intended to be a single instance of the NULL object,
         * so the clone method returns itself.
         *
         * @return NULL.
         */
        @Override
        protected final Object clone() {
            return this;
        }


        /**
         * A Null object is equal to the null value and to itself.
         *
         * @param object An object to test for nullness.
         * @return true if the object parameter is the JSONObject.NULL object
         *         or null.
         */
        @Override
        public boolean equals(Object object) {
            return object == null || object == this;
        }

        /**
         * Null in JSON should evaluate to false
         *
         * @return false
         */
        public boolean asBoolean() {
            return false;
        }


        /**
         * Get the "null" string value.
         *
         * @return The string "null".
         */
        @Override
        public String toString() {
            return "null";
        }
    }


    /**
     * The hash map where the JSONObject's properties are kept.
     */
    private HashMap myHashMap;


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
        this.myHashMap = new HashMap();
    }


    /**
     * Construct a JSONObject from a subset of another JSONObject.
     * An array of strings is used to identify the keys that should be copied.
     * Missing keys are ignored.
     *
     * @param jo A JSONObject.
     * @param sa An array of strings.
     * @throws JSONException If a value is a non-finite number.
     */
    public JSONObject(JSONObject jo, String[] sa) throws JSONException {
        this();
        for (int i = 0; i < sa.length; i += 1) {
            putOpt(sa[i], jo.opt(sa[i]));
        }
    }


    /**
     * Construct a JSONObject from a JSONTokener.
     *
     * @param x A JSONTokener object containing the source string.
     * @throws JSONException If there is a syntax error in the source string.
     */
    public JSONObject(JSONTokener x) throws JSONException {
        this();
        char c;
        String key;

        if (x.nextClean() != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        for (; ;) {
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

            /*
             * The key is followed by ':'. We will also tolerate '=' or '=>'.
             */

            c = x.nextClean();
            if (c == '=') {
                if (x.next() != '>') {
                    x.back();
                }
            } else if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }
            this.myHashMap.put(key, x.nextValue());

            /*
             * Pairs are separated by ','. We will also tolerate ';'.
             */

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
     *            the JSONObject.
     */
    public JSONObject(Map map) {
        this.myHashMap = new HashMap(map);
    }


    /**
     * Construct a JSONObject from a string.
     * This is the most commonly used JSONObject constructor.
     *
     * @param string A string beginning
     *               with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *               with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If there is a syntax error in the source string.
     */
    public JSONObject(String string) throws JSONException {
        this(new JSONTokener(string));
    }


    /**
     * Accumulate values under a key. It is similar to the put method except
     * that if there is already an object stored under the key then a
     * JSONArray is stored under the key to hold all of the accumulated values.
     * If there is already a JSONArray, then the new value is appended to it.
     * In contrast, the put method replaces the previous value.
     *
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this
     * @throws JSONException If the value is an invalid number
     *                       or if the key is null.
     */
    public JSONObject accumulate(String key, Object value)
            throws JSONException {
        testValidity(value);
        Object o = opt(key);
        if (o == null) {
            put(key, value);
        } else if (o instanceof JSONArray) {
            ((JSONArray) o).put(value);
        } else {
            put(key, new JSONArray().put(o).put(value));
        }
        return this;
    }


    /**
     * Get the value object associated with a key.
     *
     * @param key A key string.
     * @return The object associated with the key.
     * @throws JSONException if the key is not found.
     */
    public Object get(String key) throws JSONException {
        Object o = opt(key);
        if (o == null) {
            throw new JSONException("JSONObject[" + quote(key) +
                    "] not found.");
        }
        return o;
    }


    /**
     * Get the boolean value associated with a key.
     *
     * @param key A key string.
     * @return The truth.
     * @throws JSONException if the value is not a Boolean or the String "true" or "false".
     */
    public boolean getBoolean(String key) throws JSONException {
        Object o = get(key);
        if (o.equals(Boolean.FALSE) ||
                (o instanceof String &&
                        ((String) o).equalsIgnoreCase("false"))) {
            return false;
        } else if (o.equals(Boolean.TRUE) ||
                (o instanceof String &&
                        ((String) o).equalsIgnoreCase("true"))) {
            return true;
        }
        throw new JSONException("JSONObject[" + quote(key) +
                "] is not a Boolean.");
    }


    /**
     * Get the double value associated with a key.
     *
     * @param key A key string.
     * @return The numeric value.
     * @throws JSONException if the key is not found or
     *                       if the value is not a Number object and cannot be converted to a number.
     */
    public double getDouble(String key) throws JSONException {
        Object o = get(key);
        try {
            return o instanceof Number ?
                    ((Number) o).doubleValue() : Double.parseDouble((String) o);
        } catch (Exception e) {
            throw new JSONException("JSONObject[" + quote(key) +
                    "] is not a number.");
        }
    }


    /**
     * Get the int value associated with a key. If the number value is too
     * large for an int, it will be clipped.
     *
     * @param key A key string.
     * @return The integer value.
     * @throws JSONException if the key is not found or if the value cannot
     *                       be converted to an integer.
     */
    public int getInt(String key) throws JSONException {
        Object o = get(key);
        return o instanceof Number ?
                ((Number) o).intValue() : (int) getDouble(key);
    }


    /**
     * Get the JSONArray value associated with a key.
     *
     * @param key A key string.
     * @return A JSONArray which is the value.
     * @throws JSONException if the key is not found or
     *                       if the value is not a JSONArray.
     */
    public JSONArray getJSONArray(String key) throws JSONException {
        Object o = get(key);
        if (o instanceof JSONArray) {
            return (JSONArray) o;
        }
        throw new JSONException("JSONObject[" + quote(key) +
                "] is not a JSONArray.");
    }


    /**
     * Get the JSONObject value associated with a key.
     *
     * @param key A key string.
     * @return A JSONObject which is the value.
     * @throws JSONException if the key is not found or
     *                       if the value is not a JSONObject.
     */
    public JSONObject getJSONObject(String key) throws JSONException {
        Object o = get(key);
        if (o instanceof JSONObject) {
            return (JSONObject) o;
        }
        throw new JSONException("JSONObject[" + quote(key) +
                "] is not a JSONObject.");
    }


    /**
     * Get the long value associated with a key. If the number value is too
     * long for a long, it will be clipped.
     *
     * @param key A key string.
     * @return The long value.
     * @throws JSONException if the key is not found or if the value cannot
     *                       be converted to a long.
     */
    public long getLong(String key) throws JSONException {
        Object o = get(key);
        return o instanceof Number ?
                ((Number) o).longValue() : (long) getDouble(key);
    }


    /**
     * Get the string associated with a key.
     *
     * @param key A key string.
     * @return A string which is the value.
     * @throws JSONException if the key is not found.
     */
    public String getString(String key) throws JSONException {
        return get(key).toString();
    }


    /**
     * Determine if the JSONObject contains a specific key.
     *
     * @param key A key string.
     * @return true if the key exists in the JSONObject.
     */
    public boolean has(String key) {
        return myHashMap.containsKey(key);
    }


    /**
     * Determine if the value associated with the key is null or if there is
     * no value.
     *
     * @param key A key string.
     * @return true if there is no value associated with the key or if
     *         the value is the JSONObject.NULL object.
     */
    public boolean isNull(String key) {
        return JSONObject.NULL.equals(opt(key));
    }


    /**
     * Get an enumeration of the keys of the JSONObject.
     *
     * @return An iterator of the keys.
     */
    public Iterator keys() {
        return myHashMap.keySet().iterator();
    }


    /**
     * Get the number of keys stored in the JSONObject.
     *
     * @return The number of keys in the JSONObject.
     */
    public int length() {
        return myHashMap.size();
    }


    /**
     * Produce a JSONArray containing the names of the elements of this
     * JSONObject.
     *
     * @return A JSONArray containing the key strings, or null if the JSONObject
     *         is empty.
     */
    public JSONArray names() {
        JSONArray ja = new JSONArray();
        Iterator keys = keys();
        while (keys.hasNext()) {
            ja.put(keys.next());
        }
        return ja.length() == 0 ? null : ja;
    }

    /**
     * Produce a string from a number.
     *
     * @param n A Number
     * @return A String.
     * @throws JSONException If n is a non-finite number.
     */
    static public String numberToString(Number n)
            throws JSONException {
        if (n == null) {
            throw new JSONException("Null pointer");
        }
        testValidity(n);

// Shave off trailing zeros and decimal point, if possible.

        String s = n.toString();
        if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }

    static public String dateToString(Date d) throws JSONException {
        return "new Date(" + d.getTime() + ")";
    }


    /**
     * Get an optional value associated with a key.
     *
     * @param key A key string.
     * @return An object which is the value, or null if there is no value.
     */
    public Object opt(String key) {
        return key == null ? null : this.myHashMap.get(key);
    }


    /**
     * Get an optional boolean associated with a key.
     * It returns false if there is no such key, or if the value is not
     * Boolean.TRUE or the String "true".
     *
     * @param key A key string.
     * @return The truth.
     */
    public boolean optBoolean(String key) {
        return optBoolean(key, false);
    }


    /**
     * Get an optional boolean associated with a key.
     * It returns the defaultValue if there is no such key, or if it is not
     * a Boolean or the String "true" or "false" (case insensitive).
     *
     * @param key          A key string.
     * @param defaultValue The default.
     * @return The truth.
     */
    public boolean optBoolean(String key, boolean defaultValue) {
        try {
            return getBoolean(key);
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
     * @param key A string which is the key.
     * @return An object which is the value.
     */
    public double optDouble(String key) {
        return optDouble(key, Double.NaN);
    }


    /**
     * Get an optional double associated with a key, or the
     * defaultValue if there is no such key or if its value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key          A key string.
     * @param defaultValue The default.
     * @return An object which is the value.
     */
    public double optDouble(String key, double defaultValue) {
        try {
            Object o = opt(key);
            return o instanceof Number ? ((Number) o).doubleValue() :
                    Double.valueOf((String) o);
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
     * @param key A key string.
     * @return An object which is the value.
     */
    public int optInt(String key) {
        return optInt(key, 0);
    }


    /**
     * Get an optional int value associated with a key,
     * or the default if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key          A key string.
     * @param defaultValue The default.
     * @return An object which is the value.
     */
    public int optInt(String key, int defaultValue) {
        try {
            return getInt(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional JSONArray associated with a key.
     * It returns null if there is no such key, or if its value is not a
     * JSONArray.
     *
     * @param key A key string.
     * @return A JSONArray which is the value.
     */
    public JSONArray optJSONArray(String key) {
        Object o = opt(key);
        return o instanceof JSONArray ? (JSONArray) o : null;
    }


    /**
     * Get an optional JSONObject associated with a key.
     * It returns null if there is no such key, or if its value is not a
     * JSONObject.
     *
     * @param key A key string.
     * @return A JSONObject which is the value.
     */
    public JSONObject optJSONObject(String key) {
        Object o = opt(key);
        return o instanceof JSONObject ? (JSONObject) o : null;
    }


    /**
     * Get an optional long value associated with a key,
     * or zero if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key A key string.
     * @return An object which is the value.
     */
    public long optLong(String key) {
        return optLong(key, 0);
    }


    /**
     * Get an optional long value associated with a key,
     * or the default if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key          A key string.
     * @param defaultValue The default.
     * @return An object which is the value.
     */
    public long optLong(String key, long defaultValue) {
        try {
            return getLong(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional string associated with a key.
     * It returns an empty string if there is no such key. If the value is not
     * a string and is not null, then it is coverted to a string.
     *
     * @param key A key string.
     * @return A string which is the value.
     */
    public String optString(String key) {
        return optString(key, "");
    }


    /**
     * Get an optional string associated with a key.
     * It returns the defaultValue if there is no such key.
     *
     * @param key          A key string.
     * @param defaultValue The default.
     * @return A string which is the value.
     */
    public String optString(String key, String defaultValue) {
        Object o = opt(key);
        return o != null ? o.toString() : defaultValue;
    }


    /**
     * Put a key/boolean pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A boolean which is the value.
     * @return this
     * @throws JSONException If the key is null.
     */
    public JSONObject put(String key, boolean value) throws JSONException {
        put(key, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }


    /**
     * Put a key/double pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A double which is the value.
     * @return this
     * @throws JSONException If the key is null or if the number is invalid.
     */
    public JSONObject put(String key, double value) throws JSONException {
        put(key, Double.valueOf(value));
        return this;
    }


    /**
     * Put a key/int pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value An int which is the value.
     * @return this
     * @throws JSONException If the key is null.
     */
    public JSONObject put(String key, int value) throws JSONException {
        put(key, Integer.valueOf(value));
        return this;
    }


    /**
     * Put a key/long pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A long which is the value.
     * @return this
     * @throws JSONException If the key is null.
     */
    public JSONObject put(String key, long value) throws JSONException {
        put(key, Long.valueOf(value));
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject. If the value is null,
     * then the key will be removed from the JSONObject if it is present.
     *
     * @param key   A key string.
     * @param value An object which is the value. It should be of one of these
     *              types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
     *              or the JSONObject.NULL object.
     * @return this
     * @throws JSONException If the value is non-finite number
     *                       or if the key is null.
     */
    public JSONObject put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        if (value != null) {
            testValidity(value);
            this.myHashMap.put(key, value);
        } else {
            remove(key);
        }
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject, but only if the
     * key and the value are both non-null.
     *
     * @param key   A key string.
     * @param value An object which is the value. It should be of one of these
     *              types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
     *              or the JSONObject.NULL object.
     * @return this
     * @throws JSONException If the value is a non-finite number.
     */
    public JSONObject putOpt(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            put(key, value);
        }
        return this;
    }


    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within </, allowing JSON
     * text to be delivered in HTML. In JSON text, a string cannot contain a
     * control character or an unescaped quote or backslash.
     *
     * @param string A String
     * @return A String correctly formatted for insertion in a JSON text.
     */
    public static String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char b;
        char c = 0;
        int i;
        int len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    if (b == '<') {
                        sb.append('\\');
                    }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Remove a name and its value, if present.
     *
     * @param key The name to be removed.
     * @return The value that was associated with the name,
     *         or null if there was no value.
     */
    public Object remove(String key) {
        return myHashMap.remove(key);
    }

    /**
     * Throw an exception if the object is an NaN or infinite number.
     *
     * @param o The object to test.
     * @throws JSONException If o is a non-finite number.
     */
    static void testValidity(Object o) throws JSONException {
        if (o != null) {
            if (o instanceof Double) {
                if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
                    throw new JSONException(
                            "JSON does not allow non-finite numbers");
                }
            } else if (o instanceof Float) {
                if (((Float) o).isInfinite() || ((Float) o).isNaN()) {
                    throw new JSONException(
                            "JSON does not allow non-finite numbers.");
                }
            }
        }
    }


    /**
     * Produce a JSONArray containing the values of the members of this
     * JSONObject.
     *
     * @param names A JSONArray containing a list of key strings. This
     *              determines the sequence of the values in the result.
     * @return A JSONArray of values.
     * @throws JSONException If any of the values are non-finite numbers.
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
     * Make an JSON text of this JSONObject. For compactness, no whitespace
     * is added. If this would not result in a syntactically correct JSON text,
     * then null will be returned instead.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, portable, transmittable
     *         representation of the object, beginning
     *         with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *         with <code>}</code>&nbsp;<small>(right brace)</small>.
     */
    @Override
    public String toString() {
        try {
            Iterator keys = keys();
            StringBuilder sb = new StringBuilder("{");

            while (keys.hasNext()) {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                Object o = keys.next();
                sb.append(quote(o.toString()));
                sb.append(':');
                sb.append(valueToString(this.myHashMap.get(o)));
            }
            sb.append('}');
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Make a prettyprinted JSON text of this JSONObject.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param indentFactor The number of spaces to add to each level of
     *                     indentation.
     * @return a printable, displayable, portable, transmittable
     *         representation of the object, beginning
     *         with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *         with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the object contains an invalid number.
     */
    public String toString(int indentFactor) throws JSONException {
        return toString(indentFactor, 0);
    }


    /**
     * Make a prettyprinted JSON text of this JSONObject.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param indentFactor The number of spaces to add to each level of
     *                     indentation.
     * @param indent       The indentation of the top level.
     * @return a printable, displayable, transmittable
     *         representation of the object, beginning
     *         with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *         with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the object contains an invalid number.
     */
    String toString(int indentFactor, int indent) throws JSONException {
        int i;
        int n = length();
        if (n == 0) {
            return "{}";
        }
        Iterator keys = keys();
        StringBuilder sb = new StringBuilder("{");
        int newindent = indent + indentFactor;
        Object o;
        if (n == 1) {
            o = keys.next();
            sb.append(quote(o.toString()));
            sb.append(": ");
            sb.append(valueToString(this.myHashMap.get(o), indentFactor,
                    indent));
        } else {
            while (keys.hasNext()) {
                o = keys.next();
                if (sb.length() > 1) {
                    sb.append(",\n");
                } else {
                    sb.append('\n');
                }
                for (i = 0; i < newindent; i += 1) {
                    sb.append(' ');
                }
                sb.append(quote(o.toString()));
                sb.append(": ");
                sb.append(valueToString(this.myHashMap.get(o), indentFactor,
                        newindent));
            }
            if (sb.length() > 1) {
                sb.append('\n');
                for (i = 0; i < indent; i += 1) {
                    sb.append(' ');
                }
            }
        }
        sb.append('}');
        return sb.toString();
    }


    /**
     * Make a JSON text of an object value.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value The value to be serialized.
     * @return a printable, displayable, transmittable
     *         representation of the object, beginning
     *         with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *         with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the value is or contains an invalid number.
     */
    static String valueToString(Object value) throws JSONException {
        if (value == null || value.equals(null)) {
            return "null";
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if (value instanceof Date) {
            return dateToString((Date) value);
        }
        if (value instanceof Boolean || value instanceof JSONObject ||
                value instanceof JSONArray) {
            return value.toString();
        }
        return quote(value.toString());
    }


    /**
     * Make a prettyprinted JSON text of an object value.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param value        The value to be serialized.
     * @param indentFactor The number of spaces to add to each level of
     *                     indentation.
     * @param indent       The indentation of the top level.
     * @return a printable, displayable, transmittable
     *         representation of the object, beginning
     *         with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *         with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the object contains an invalid number.
     */
    static String valueToString(Object value, int indentFactor, int indent)
            throws JSONException {
        if (value == null || value.equals(null)) {
            return "null";
        }
        if (value instanceof Number) {
            return numberToString((Number) value);
        }
        if (value instanceof Date) {
            return dateToString((Date) value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof JSONObject) {
            return ((JSONObject) value).toString(indentFactor, indent);
        }
        if (value instanceof JSONArray) {
            return ((JSONArray) value).toString(indentFactor, indent);
        }
        return quote(value.toString());
    }


    /**
     * Write the contents of the JSONObject as JSON text to a writer.
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
            Iterator keys = keys();
            writer.write('{');

            while (keys.hasNext()) {
                if (b) {
                    writer.write(',');
                }
                Object k = keys.next();
                writer.write(quote(k.toString()));
                writer.write(':');
                Object v = this.myHashMap.get(k);
                if (v instanceof JSONObject) {
                    ((JSONObject) v).write(writer);
                } else if (v instanceof JSONArray) {
                    ((JSONArray) v).write(writer);
                } else {
                    writer.write(valueToString(v));
                }
                b = true;
            }
            writer.write('}');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    public int size() {
        return myHashMap.size();
    }

    public boolean isEmpty() {
        return myHashMap.isEmpty();
    }

    public boolean containsKey(Object o) {
        return myHashMap.containsKey(o);
    }

    public boolean containsValue(Object o) {
        return myHashMap.containsValue(o);
    }

    public Object get(Object o) {
        return myHashMap.get(o);
    }

    public Object put(Object o, Object o1) {
        return myHashMap.put(o, o1);
    }

    public Object remove(Object o) {
        return myHashMap.remove(o);
    }

    public void putAll(Map map) {
        this.myHashMap.putAll(map);
    }

    public void clear() {
        this.myHashMap.clear();
    }

    public Set keySet() {
        return myHashMap.keySet();
    }

    public Collection values() {
        return myHashMap.values();
    }

    public Set entrySet() {
        return myHashMap.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JSONObject that = (JSONObject) o;

        if (myHashMap != null ? !myHashMap.equals(that.myHashMap) : that.myHashMap != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (myHashMap != null ? myHashMap.hashCode() : 0);
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
