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

import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyQuaternion;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.util.Debug;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.export.Savable;
import com.ardor3d.util.pool.ObjectPool;

/**
 * Quaternion represents a 4 value math object used in Ardor3D to describe rotations. It has the advantage of being able
 * to avoid lock by adding a 4th dimension to rotation.
 * 
 * Note: some algorithms in this class were ported from Eberly, Wolfram, Game Gems and others to Java by myself and
 * others, originally for jMonkeyEngine.
 */
public class Quaternion implements Cloneable, Savable, Externalizable, ReadOnlyQuaternion {

    private static final long serialVersionUID = 1L;

    private static final QuaternionPool QUAT_POOL = new QuaternionPool(11);

    /**
     * x=0, y=0, z=0, w=1
     */
    public final static ReadOnlyQuaternion IDENTITY = new Quaternion(0, 0, 0, 1);

    protected double _x = 0;
    protected double _y = 0;
    protected double _z = 0;
    protected double _w = 1;

    /**
     * Constructs a new quaternion set to (0, 0, 0, 1).
     */
    public Quaternion() {
        this(IDENTITY);
    }

    /**
     * Constructs a new quaternion set to the (x, y, z, w) values of the given source quaternion.
     * 
     * @param source
     */
    public Quaternion(final ReadOnlyQuaternion source) {
        this(source.getX(), source.getY(), source.getZ(), source.getW());
    }

    /**
     * Constructs a new quaternion set to (x, y, z, w).
     * 
     * @param x
     * @param y
     * @param z
     * @param w
     */
    public Quaternion(final double x, final double y, final double z, final double w) {
        _x = x;
        _y = y;
        _z = z;
        _w = w;
    }

    /**
     * Constructs a new quaternion from the given Euler rotation angles (y,r,p).
     * 
     * @param angles
     *            the Euler angles of rotation (in radians).
     * @throws IllegalArgumentException
     *             if angles is not length 3
     * @throws NullPointerException
     *             if angles is null.
     */
    public Quaternion(final double[] angles) {
        this(0, 0, 0, 1);
        fromAngles(angles);
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

    public float getXf() {
        return (float) _x;
    }

    public float getYf() {
        return (float) _y;
    }

    public float getZf() {
        return (float) _z;
    }

    public float getWf() {
        return (float) _w;
    }

    /**
     * Stores the double values of this quaternion in the given double array as (x,y,z,w).
     * 
     * @param store
     *            The array in which to store the values of this quaternion. If null, a new double[4] array is created.
     * @return the double array
     * @throws IllegalArgumentException
     *             if store is not null and is not at least length 4
     */
    public double[] toArray(final double[] store) {
        double[] result = store;
        if (result == null) {
            result = new double[4];
        } else if (result.length < 4) {
            throw new IllegalArgumentException("store array must have at least three elements");
        }
        result[0] = getX();
        result[1] = getY();
        result[2] = getZ();
        result[3] = getW();
        return result;
    }

    /**
     * Sets the x component of this quaternion to the given double value.
     * 
     * @param x
     */
    public void setX(final double x) {
        _x = x;
    }

    /**
     * Sets the y component of this quaternion to the given double value.
     * 
     * @param y
     */
    public void setY(final double y) {
        _y = y;
    }

    /**
     * Sets the z component of this quaternion to the given double value.
     * 
     * @param z
     */
    public void setZ(final double z) {
        _z = z;
    }

    /**
     * Sets the w component of this quaternion to the given double value.
     * 
     * @param w
     */
    public void setW(final double w) {
        _w = w;
    }

    /**
     * Sets the value of this quaternion to (x, y, z, w)
     * 
     * @param x
     * @param y
     * @param z
     * @param w
     * @return this quaternion for chaining
     */
    public Quaternion set(final double x, final double y, final double z, final double w) {
        setX(x);
        setY(y);
        setZ(z);
        setW(w);
        return this;
    }

    /**
     * Sets the value of this quaternion to the (x, y, z, w) values of the provided source quaternion.
     * 
     * @param source
     * @return this quaternion for chaining
     * @throws NullPointerException
     *             if source is null.
     */
    public Quaternion set(final ReadOnlyQuaternion source) {
        setX(source.getX());
        setY(source.getY());
        setZ(source.getZ());
        setW(source.getW());
        return this;
    }

    /**
     * Updates this quaternion from the given Euler rotation angles (y,r,p).
     * 
     * @param angles
     *            the Euler angles of rotation (in radians).
     * @return this quaternion for chaining
     * @throws IllegalArgumentException
     *             if angles is not length 3
     * @throws NullPointerException
     *             if angles is null.
     */
    public Quaternion fromAngles(final double[] angles) {
        if (angles.length != 3) {
            throw new IllegalArgumentException("Angles array must have three elements");
        }

        return fromAngles(angles[0], angles[1], angles[2]);
    }

    /**
     * Updates this quaternion from the given Euler rotation angles (y,r,p). Note that we are applying in order: roll,
     * pitch, yaw but we've ordered them in x, y, and z for convenience. See:
     * http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm
     * 
     * @param yaw
     *            the Euler yaw of rotation (in radians). (aka Bank, often rot around x)
     * @param roll
     *            the Euler roll of rotation (in radians). (aka Heading, often rot around y)
     * @param pitch
     *            the Euler pitch of rotation (in radians). (aka Attitude, often rot around z)
     * @return this quaternion for chaining
     */
    public Quaternion fromAngles(final double yaw, final double roll, final double pitch) {
        double angle = pitch * 0.5;
        final double sinPitch = MathUtils.sin(angle);
        final double cosPitch = MathUtils.cos(angle);
        angle = roll * 0.5;
        final double sinRoll = MathUtils.sin(angle);
        final double cosRoll = MathUtils.cos(angle);
        angle = yaw * 0.5;
        final double sinYaw = MathUtils.sin(angle);
        final double cosYaw = MathUtils.cos(angle);

        // variables used to reduce multiplication calls.
        final double cosRollXcosPitch = cosRoll * cosPitch;
        final double sinRollXsinPitch = sinRoll * sinPitch;
        final double cosRollXsinPitch = cosRoll * sinPitch;
        final double sinRollXcosPitch = sinRoll * cosPitch;

        final double w = (cosRollXcosPitch * cosYaw - sinRollXsinPitch * sinYaw);
        final double x = (cosRollXcosPitch * sinYaw + sinRollXsinPitch * cosYaw);
        final double y = (sinRollXcosPitch * cosYaw + cosRollXsinPitch * sinYaw);
        final double z = (cosRollXsinPitch * cosYaw - sinRollXcosPitch * sinYaw);

        set(x, y, z, w);

        return normalizeLocal();
    }

    /**
     * converts this quaternion to Euler rotation angles (yaw, roll, pitch). See
     * http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/index.htm
     * 
     * @param store
     *            the double[] array to store the computed angles in. If null, a new double[] will be created
     * @return the double[] array.
     * @throws IllegalArgumentException
     *             if non-null store is not at least length 3
     */
    public double[] toAngles(final double[] store) {
        double[] result = store;
        if (result == null) {
            result = new double[3];
        } else if (result.length < 3) {
            throw new IllegalArgumentException("store array must have at least three elements");
        }

        final double sqw = getW() * getW();
        final double sqx = getX() * getX();
        final double sqy = getY() * getY();
        final double sqz = getZ() * getZ();
        final double unit = sqx + sqy + sqz + sqw; // if normalized is one, otherwise
        // is correction factor
        final double test = getX() * getY() + getZ() * getW();
        if (test > 0.499 * unit) { // singularity at north pole
            result[1] = 2 * Math.atan2(getX(), getW());
            result[2] = MathUtils.HALF_PI;
            result[0] = 0;
        } else if (test < -0.499 * unit) { // singularity at south pole
            result[1] = -2 * Math.atan2(getX(), getW());
            result[2] = -MathUtils.HALF_PI;
            result[0] = 0;
        } else {
            result[1] = Math.atan2(2 * getY() * getW() - 2 * getX() * getZ(), sqx - sqy - sqz + sqw); // roll or heading
            result[2] = Math.asin(2 * test / unit); // pitch or attitude
            result[0] = Math.atan2(2 * getX() * getW() - 2 * getY() * getZ(), -sqx + sqy - sqz + sqw); // yaw or bank
        }
        return result;
    }

    /**
     * Sets the value of this quaternion to the rotation described by the given matrix.
     * 
     * @param matrix
     * @return this quaternion for chaining
     * @throws NullPointerException
     *             if matrix is null.
     */
    public Quaternion fromRotationMatrix(final ReadOnlyMatrix3 matrix) {
        return fromRotationMatrix(matrix.getValue(0, 0), matrix.getValue(0, 1), matrix.getValue(0, 2), matrix.getValue(
                1, 0), matrix.getValue(1, 1), matrix.getValue(1, 2), matrix.getValue(2, 0), matrix.getValue(2, 1),
                matrix.getValue(2, 2));
    }

    /**
     * Sets the value of this quaternion to the rotation described by the given matrix values.
     * 
     * @param m00
     * @param m01
     * @param m02
     * @param m10
     * @param m11
     * @param m12
     * @param m20
     * @param m21
     * @param m22
     * @return this quaternion for chaining
     */
    public Quaternion fromRotationMatrix(final double m00, final double m01, final double m02, final double m10,
            final double m11, final double m12, final double m20, final double m21, final double m22) {
        // Uses the Graphics Gems code, from
        // ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z
        // *NOT* the "Matrix and Quaternions FAQ", which has errors!

        // the trace is the sum of the diagonal elements; see
        // http://mathworld.wolfram.com/MatrixTrace.html
        final double t = m00 + m11 + m22;

        // we protect the division by s by ensuring that s>=1
        double x, y, z, w;
        if (t >= 0) { // |w| >= .5
            double s = Math.sqrt(t + 1); // |s|>=1 ...
            w = 0.5 * s;
            s = 0.5 / s; // so this division isn't bad
            x = (m21 - m12) * s;
            y = (m02 - m20) * s;
            z = (m10 - m01) * s;
        } else if ((m00 > m11) && (m00 > m22)) {
            double s = Math.sqrt(1.0 + m00 - m11 - m22); // |s|>=1
            x = s * 0.5; // |x| >= .5
            s = 0.5 / s;
            y = (m10 + m01) * s;
            z = (m02 + m20) * s;
            w = (m21 - m12) * s;
        } else if (m11 > m22) {
            double s = Math.sqrt(1.0 + m11 - m00 - m22); // |s|>=1
            y = s * 0.5; // |y| >= .5
            s = 0.5 / s;
            x = (m10 + m01) * s;
            z = (m21 + m12) * s;
            w = (m02 - m20) * s;
        } else {
            double s = Math.sqrt(1.0 + m22 - m00 - m11); // |s|>=1
            z = s * 0.5; // |z| >= .5
            s = 0.5 / s;
            x = (m02 + m20) * s;
            y = (m21 + m12) * s;
            w = (m10 - m01) * s;
        }

        return set(x, y, z, w);
    }

    /**
     * @param store
     *            the matrix to store our result in. If null, a new matrix is created.
     * @return the rotation matrix representation of this quaternion (normalized)
     * 
     *         if store is not null and is read only.
     */
    public Matrix3 toRotationMatrix(final Matrix3 store) {
        Matrix3 result = store;
        if (result == null) {
            result = new Matrix3();
        }

        final double norm = magnitudeSquared();
        final double s = Double.compare(norm, 0.0) > 0.0 ? 2.0 / norm : 0.0;

        // compute xs/ys/zs first to save 6 multiplications, since xs/ys/zs
        // will be used 2-4 times each.
        final double xs = getX() * s;
        final double ys = getY() * s;
        final double zs = getZ() * s;
        final double xx = getX() * xs;
        final double xy = getX() * ys;
        final double xz = getX() * zs;
        final double xw = getW() * xs;
        final double yy = getY() * ys;
        final double yz = getY() * zs;
        final double yw = getW() * ys;
        final double zz = getZ() * zs;
        final double zw = getW() * zs;

        // using s=2/norm (instead of 1/norm) saves 9 multiplications by 2 here
        result.setValue(0, 0, 1.0 - (yy + zz));
        result.setValue(0, 1, xy - zw);
        result.setValue(0, 2, xz + yw);
        result.setValue(1, 0, xy + zw);
        result.setValue(1, 1, 1.0 - (xx + zz));
        result.setValue(1, 2, yz - xw);
        result.setValue(2, 0, xz - yw);
        result.setValue(2, 1, yz + xw);
        result.setValue(2, 2, 1.0 - (xx + yy));

        return result;
    }

    /**
     * @param store
     *            the matrix to store our result in. If null, a new matrix is created.
     * @return the rotation matrix representation of this quaternion (normalized)
     */
    public Matrix4 toRotationMatrix(final Matrix4 store) {
        Matrix4 result = store;
        if (result == null) {
            result = new Matrix4();
        }

        final double norm = magnitude();
        final double s = Double.compare(norm, 0.0) > 0.0 ? 2.0 / norm : 0.0;

        // compute xs/ys/zs first to save 6 multiplications, since xs/ys/zs
        // will be used 2-4 times each.
        final double xs = getX() * s;
        final double ys = getY() * s;
        final double zs = getZ() * s;
        final double xx = getX() * xs;
        final double xy = getX() * ys;
        final double xz = getX() * zs;
        final double xw = getW() * xs;
        final double yy = getY() * ys;
        final double yz = getY() * zs;
        final double yw = getW() * ys;
        final double zz = getZ() * zs;
        final double zw = getW() * zs;

        // using s=2/norm (instead of 1/norm) saves 9 multiplications by 2 here
        result.setValue(0, 0, 1.0 - (yy + zz));
        result.setValue(0, 1, xy - zw);
        result.setValue(0, 2, xz + yw);
        result.setValue(1, 0, xy + zw);
        result.setValue(1, 1, 1.0 - (xx + zz));
        result.setValue(1, 2, yz - xw);
        result.setValue(2, 0, xz - yw);
        result.setValue(2, 1, yz + xw);
        result.setValue(2, 2, 1.0 - (xx + yy));

        return result;
    }

    /**
     * @param index
     *            the 3x3 rotation matrix column to retrieve from this quaternion (normalized). Must be between 0 and 2.
     * @param store
     *            the vector object to store the result in. if null, a new one is created.
     * @return the column specified by the index.
     */
    public Vector3 getRotationColumn(final int index, final Vector3 store) {
        Vector3 result = store;
        if (result == null) {
            result = new Vector3();
        }

        final double norm = magnitude();
        final double s = (Double.compare(norm, 1.0) == 0.0) ? 2.0 : (Double.compare(norm, 1.0) > 0.0) ? 2.0 / norm : 0;

        // compute xs/ys/zs first to save 6 multiplications, since xs/ys/zs
        // will be used 2-4 times each.
        final double xs = getX() * s;
        final double ys = getY() * s;
        final double zs = getZ() * s;
        final double xx = getX() * xs;
        final double xy = getX() * ys;
        final double xz = getX() * zs;
        final double xw = getW() * xs;
        final double yy = getY() * ys;
        final double yz = getY() * zs;
        final double yw = getW() * ys;
        final double zz = getZ() * zs;
        final double zw = getW() * zs;

        // using s=2/norm (instead of 1/norm) saves 3 multiplications by 2 here
        double x, y, z;
        switch (index) {
            case 0:
                x = 1.0 - (yy + zz);
                y = xy + zw;
                z = xz - yw;
                break;
            case 1:
                x = xy - zw;
                y = 1.0 - (xx + zz);
                z = yz + xw;
                break;
            case 2:
                x = xz + yw;
                y = yz - xw;
                z = 1.0 - (xx + yy);
                break;
            default:
                throw new IllegalArgumentException("Invalid column index. " + index);
        }

        return result.set(x, y, z);
    }

    /**
     * Sets the values of this quaternion to the values represented by a given angle and axis of rotation. Note that
     * this method creates an object, so use fromAngleNormalAxis if your axis is already normalized. If axis == 0,0,0
     * the quaternion is set to identity.
     * 
     * @param angle
     *            the angle to rotate (in radians).
     * @param axis
     *            the axis of rotation.
     * @return this quaternion for chaining
     * @throws NullPointerException
     *             if axis is null
     */
    public Quaternion fromAngleAxis(final double angle, final ReadOnlyVector3 axis) {
        final Vector3 temp = Vector3.fetchTempInstance();
        final Quaternion quat = fromAngleNormalAxis(angle, axis.normalize(temp));
        Vector3.releaseTempInstance(temp);
        return quat;
    }

    /**
     * Sets the values of this quaternion to the values represented by a given angle and unit length axis of rotation.
     * If axis == 0,0,0 the quaternion is set to identity.
     * 
     * @param angle
     *            the angle to rotate (in radians).
     * @param axis
     *            the axis of rotation (already normalized - unit length).
     * @throws NullPointerException
     *             if axis is null
     */
    public Quaternion fromAngleNormalAxis(final double angle, final ReadOnlyVector3 axis) {
        if (axis.equals(Vector3.ZERO)) {
            return setIdentity();
        }

        final double halfAngle = 0.5 * angle;
        final double sin = MathUtils.sin(halfAngle);
        final double w = MathUtils.cos(halfAngle);
        final double x = sin * axis.getX();
        final double y = sin * axis.getY();
        final double z = sin * axis.getZ();
        return set(x, y, z, w);
    }

    /**
     * Returns the rotation angle represented by this quaternion. If a non-null vector is provided, the axis of rotation
     * is stored in that vector as well.
     * 
     * @param axisStore
     *            the object we'll store the computed axis in. If null, no computations are done to determine axis.
     * @return the angle of rotation in radians.
     */
    public double toAngleAxis(final Vector3 axisStore) {
        final double sqrLength = getX() * getX() + getY() * getY() + getZ() * getZ();
        double angle;
        if (Double.compare(sqrLength, 0.0) == 0.0) {
            angle = 0.0;
            if (axisStore != null) {
                axisStore.setX(1.0);
                axisStore.setY(0.0);
                axisStore.setZ(0.0);
            }
        } else {
            angle = (2.0 * Math.acos(getW()));
            if (axisStore != null) {
                final double invLength = (1.0 / Math.sqrt(sqrLength));
                axisStore.setX(getX() * invLength);
                axisStore.setY(getY() * invLength);
                axisStore.setZ(getZ() * invLength);
            }
        }

        return angle;
    }

    /**
     * @param store
     *            the Quaternion to store the result in. if null, a new one is created.
     * @return a new quaternion that represents a unit length version of this Quaternion.
     */
    public Quaternion normalize(final Quaternion store) {
        Quaternion result = store;
        if (result == null) {
            result = new Quaternion();
        }

        final double n = 1.0 / magnitude();
        final double x = getX() * n;
        final double y = getY() * n;
        final double z = getZ() * n;
        final double w = getW() * n;
        return result.set(x, y, z, w);
    }

    /**
     * @return this quaternion, modified to be unit length, for chaining.
     */
    public Quaternion normalizeLocal() {
        final double n = 1.0 / magnitude();
        final double x = getX() * n;
        final double y = getY() * n;
        final double z = getZ() * n;
        final double w = getW() * n;
        return set(x, y, z, w);
    }

    /**
     * creates a new quaternion that holds the inverted values of this quaternion as if multiply(-1, store) had been
     * called.
     * 
     * @param store
     *            the Quaternion to store the result in. if null, a new one is created.
     * @return a new quaternion as described above.
     */
    public Quaternion invert(final Quaternion store) {
        return multiply(-1, store);
    }

    /**
     * internally inverts this quaternion's values as if multiplyLocal(-1) had been called.
     * 
     * @return this quaternion for chaining
     */
    public Quaternion invertLocal() {
        return multiplyLocal(-1);
    }

    /**
     * @param quat
     * @param store
     *            the Quaternion to store the result in. if null, a new one is created.
     * @return a quaternion representing the fields of this quaternion added to those of the given quaternion.
     */
    public Quaternion add(final ReadOnlyQuaternion quat, final Quaternion store) {
        Quaternion result = store;
        if (result == null) {
            result = new Quaternion();
        }

        return result.set(getX() + quat.getX(), getY() + quat.getY(), getZ() + quat.getZ(), getW() + quat.getW());
    }

    /**
     * Internally increments the fields of this quaternion with the field values of the given quaternion.
     * 
     * @param quat
     * @return this quaternion for chaining
     */
    public Quaternion addLocal(final ReadOnlyQuaternion quat) {
        setX(getX() + quat.getX());
        setY(getY() + quat.getY());
        setZ(getZ() + quat.getZ());
        setW(getW() + quat.getW());
        return this;
    }

    /**
     * @param quat
     * @param store
     *            the Quaternion to store the result in. if null, a new one is created.
     * @return a quaternion representing the fields of this quaternion subtracted from those of the given quaternion.
     */
    public Quaternion subtract(final ReadOnlyQuaternion quat, final Quaternion store) {
        Quaternion result = store;
        if (result == null) {
            result = new Quaternion();
        }

        return result.set(getX() - quat.getX(), getY() - quat.getY(), getZ() - quat.getZ(), getW() - quat.getW());
    }

    /**
     * Internally decrements the fields of this quaternion by the field values of the given quaternion.
     * 
     * @param quat
     * @return this quaternion for chaining.
     */
    public Quaternion subtractLocal(final ReadOnlyQuaternion quat) {
        setX(getX() - quat.getX());
        setY(getY() - quat.getY());
        setZ(getZ() - quat.getZ());
        setW(getW() - quat.getW());
        return this;
    }

    /**
     * Multiplies each value of this quaternion by the given scalar value.
     * 
     * @param scalar
     *            the quaternion to multiply this quaternion by.
     * @param store
     *            the Quaternion to store the result in. if null, a new one is created.
     * @return the resulting quaternion.
     */
    public Quaternion multiply(final double scalar, final Quaternion store) {
        Quaternion result = store;
        if (result == null) {
            result = new Quaternion();
        }

        return result.set(scalar * getX(), scalar * getY(), scalar * getZ(), scalar * getW());
    }

    /**
     * Multiplies each value of this quaternion by the given scalar value. The result is stored in this quaternion.
     * 
     * @param scalar
     *            the quaternion to multiply this quaternion by.
     * @return this quaternion for chaining.
     */
    public Quaternion multiplyLocal(final double scalar) {
        setX(getX() * scalar);
        setY(getY() * scalar);
        setZ(getZ() * scalar);
        setW(getW() * scalar);
        return this;
    }

    /**
     * Multiplies this quaternion by the supplied quaternion. The result is stored in the given store quaternion or a
     * new quaternion if store is null.
     * 
     * It IS safe for quat and store to be the same object.
     * 
     * @param quat
     *            the quaternion to multiply this quaternion by.
     * @param store
     *            the quaternion to store the result in.
     * @return the new quaternion.
     * 
     *         if the given store is read only.
     */
    public Quaternion multiply(final ReadOnlyQuaternion quat, Quaternion store) {
        if (store == null) {
            store = new Quaternion();
        }
        final double x = getX() * quat.getW() + getY() * quat.getZ() - getZ() * quat.getY() + getW() * quat.getX();
        final double y = -getX() * quat.getZ() + getY() * quat.getW() + getZ() * quat.getX() + getW() * quat.getY();
        final double z = getX() * quat.getY() - getY() * quat.getX() + getZ() * quat.getW() + getW() * quat.getZ();
        final double w = -getX() * quat.getX() - getY() * quat.getY() - getZ() * quat.getZ() + getW() * quat.getW();
        return store.set(x, y, z, w);
    }

    /**
     * Multiplies this quaternion by the supplied quaternion. The result is stored locally.
     * 
     * @param quat
     *            The Quaternion to multiply this one by.
     * @return this quaternion for chaining
     * @throws NullPointerException
     *             if quat is null.
     */
    public Quaternion multiplyLocal(final ReadOnlyQuaternion quat) {
        return multiplyLocal(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
    }

    /**
     * Multiplies this quaternion by the supplied matrix. The result is stored locally.
     * 
     * @param matrix
     *            the matrix to apply to this quaternion.
     * @return this quaternion for chaining
     * @throws NullPointerException
     *             if matrix is null.
     */
    public Quaternion multiplyLocal(final ReadOnlyMatrix3 matrix) {
        final double oldX = getX(), oldY = getY(), oldZ = getZ(), oldW = getW();
        fromRotationMatrix(matrix);
        final double tempX = getX(), tempY = getY(), tempZ = getZ(), tempW = getW();

        final double x = oldX * tempW + oldY * tempZ - oldZ * tempY + oldW * tempX;
        final double y = -oldX * tempZ + oldY * tempW + oldZ * tempX + oldW * tempY;
        final double z = oldX * tempY - oldY * tempX + oldZ * tempW + oldW * tempZ;
        final double w = -oldX * tempX - oldY * tempY - oldZ * tempZ + oldW * tempW;
        return set(x, y, z, w);
    }

    /**
     * Multiplies this quaternion by the supplied quaternion values. The result is stored locally.
     * 
     * @param qx
     * @param qy
     * @param qz
     * @param qw
     * @return this quaternion for chaining
     */
    public Quaternion multiplyLocal(final double qx, final double qy, final double qz, final double qw) {
        final double x = getX() * qw + getY() * qz - getZ() * qy + getW() * qx;
        final double y = -getX() * qz + getY() * qw + getZ() * qx + getW() * qy;
        final double z = getX() * qy - getY() * qx + getZ() * qw + getW() * qz;
        final double w = -getX() * qx - getY() * qy - getZ() * qz + getW() * qw;
        return set(x, y, z, w);
    }

    /**
     * Rotates the given vector by this quaternion. If supplied, the result is stored into the supplied "store" vector.
     * 
     * @param vec
     *            the vector to multiply this quaternion by.
     * @param store
     *            the vector to store the result in. If store is null, a new vector is created. Note that it IS safe for
     *            vec and store to be the same object.
     * @return the store vector, or a new vector if store is null.
     * @throws NullPointerException
     *             if vec is null
     * 
     *             if the given store is read only.
     */
    public Vector3 apply(final ReadOnlyVector3 vec, Vector3 store) {
        if (store == null) {
            store = new Vector3();
        }
        if (vec.equals(Vector3.ZERO)) {
            store.set(0, 0, 0);
        } else {
            final double x = getW() * getW() * vec.getX() + 2 * getY() * getW() * vec.getZ() - 2 * getZ() * getW()
                    * vec.getY() + getX() * getX() * vec.getX() + 2 * getY() * getX() * vec.getY() + 2 * getZ()
                    * getX() * vec.getZ() - getZ() * getZ() * vec.getX() - getY() * getY() * vec.getX();
            final double y = 2 * getX() * getY() * vec.getX() + getY() * getY() * vec.getY() + 2 * getZ() * getY()
                    * vec.getZ() + 2 * getW() * getZ() * vec.getX() - getZ() * getZ() * vec.getY() + getW() * getW()
                    * vec.getY() - 2 * getX() * getW() * vec.getZ() - getX() * getX() * vec.getY();
            final double z = 2 * getX() * getZ() * vec.getX() + 2 * getY() * getZ() * vec.getY() + getZ() * getZ()
                    * vec.getZ() - 2 * getW() * getY() * vec.getX() - getY() * getY() * vec.getZ() + 2 * getW()
                    * getX() * vec.getY() - getX() * getX() * vec.getZ() + getW() * getW() * vec.getZ();
            store.set(x, y, z);
        }
        return store;
    }

    /**
     * Updates this quaternion to represent a rotation formed by the given three axes. These axes are assumed to be
     * orthogonal and no error checking is applied. It is the user's job to insure that the three axes being provided
     * indeed represent a proper right handed coordinate system.
     * 
     * @param axes
     *            the array containing the three vectors representing the coordinate system.
     * @return this quaternion for chaining
     * @throws IllegalArgumentException
     *             if the given axes array is smaller than 3 elements.
     */
    public Quaternion fromAxes(final ReadOnlyVector3[] axes) {
        if (axes.length < 3) {
            throw new IllegalArgumentException("axes array must have at least three elements");
        }
        return fromAxes(axes[0], axes[1], axes[2]);
    }

    /**
     * Updates this quaternion to represent a rotation formed by the given three axes. These axes are assumed to be
     * orthogonal and no error checking is applied. It is the user's job to insure that the three axes being provided
     * indeed represent a proper right handed coordinate system.
     * 
     * @param xAxis
     *            vector representing the x-axis of the coordinate system.
     * @param yAxis
     *            vector representing the y-axis of the coordinate system.
     * @param zAxis
     *            vector representing the z-axis of the coordinate system.
     * @return this quaternion for chaining
     */
    public Quaternion fromAxes(final ReadOnlyVector3 xAxis, final ReadOnlyVector3 yAxis, final ReadOnlyVector3 zAxis) {
        return fromRotationMatrix(xAxis.getX(), yAxis.getX(), zAxis.getX(), xAxis.getY(), yAxis.getY(), zAxis.getY(),
                xAxis.getZ(), yAxis.getZ(), zAxis.getZ());
    }

    /**
     * Converts this quaternion to a rotation matrix and then extracts rotation axes.
     * 
     * @param axes
     *            the array of vectors to be filled.
     * @throws IllegalArgumentException
     *             if the given axes array is smaller than 3 elements.
     */
    public void toAxes(final Vector3 axes[]) {
        if (axes.length < 3) {
            throw new IllegalArgumentException("axes array must have at least three elements");
        }
        final Matrix3 tempMat = toRotationMatrix(Matrix3.fetchTempInstance());
        axes[0] = tempMat.getColumn(0, axes[0]);
        axes[1] = tempMat.getColumn(1, axes[1]);
        axes[2] = tempMat.getColumn(2, axes[2]);
        Matrix3.releaseTempInstance(tempMat);
    }

    /**
     * Does a spherical linear interpolation between this quaternion and the given end quaternion by the given change
     * amount.
     * 
     * @param endQuat
     * @param changeAmnt
     * @param store
     *            the quaternion to store the result in for return. If null, a new quaternion object is created and
     *            returned.
     * @return a new quaternion containing the result.
     */
    public Quaternion slerp(final ReadOnlyQuaternion endQuat, final double changeAmnt, final Quaternion store) {
        return slerp(this, endQuat, changeAmnt, store);
    }

    /**
     * Does a spherical linear interpolation between this quaternion and the given end quaternion by the given change
     * amount. Stores the results locally in this quaternion.
     * 
     * @param endQuat
     * @param changeAmnt
     * @return this quaternion for chaining.
     */
    public Quaternion slerpLocal(final ReadOnlyQuaternion endQuat, final double changeAmnt) {
        return slerpLocal(this, endQuat, changeAmnt);
    }

    /**
     * Does a spherical linear interpolation between the given start and end quaternions by the given change amount.
     * Returns the result as a new quaternion.
     * 
     * @param startQuat
     * @param endQuat
     * @param changeAmnt
     * @param store
     *            the quaternion to store the result in for return. If null, a new quaternion object is created and
     *            returned.
     * @return the new quaternion
     */
    public static Quaternion slerp(final ReadOnlyQuaternion startQuat, final ReadOnlyQuaternion endQuat,
            final double changeAmnt, final Quaternion store) {
        Quaternion result = store;
        if (result == null) {
            result = new Quaternion();
        }

        final Quaternion q2 = Quaternion.fetchTempInstance().set(endQuat);
        // Check for equality and skip operation.
        if (startQuat.equals(q2)) {
            return result.set(startQuat);
        }

        double dotP = startQuat.dot(q2);

        if (dotP < 0.0) {
            // Negate the second quaternion and the result of the dot product
            q2.multiplyLocal(-1);
            dotP = -dotP;
        }

        // Set the first and second scale for the interpolation
        double scale0 = 1 - changeAmnt;
        double scale1 = changeAmnt;

        // Check if the angle between the 2 quaternions was big enough to
        // warrant such calculations
        if ((1 - dotP) > 0.1) {// Get the angle between the 2 quaternions,
            // and then store the sin() of that angle
            final double theta = Math.acos(dotP);
            final double invSinTheta = 1f / MathUtils.sin(theta);

            // Calculate the scale for q1 and q2, according to the angle and
            // it's sine value
            scale0 = MathUtils.sin((1 - changeAmnt) * theta) * invSinTheta;
            scale1 = MathUtils.sin((changeAmnt * theta)) * invSinTheta;
        }

        // Calculate the x, y, z and w values for the quaternion by using a
        // special form of linear interpolation for quaternions.
        final double x = (scale0 * startQuat.getX()) + (scale1 * q2.getX());
        final double y = (scale0 * startQuat.getY()) + (scale1 * q2.getY());
        final double z = (scale0 * startQuat.getZ()) + (scale1 * q2.getZ());
        final double w = (scale0 * startQuat.getW()) + (scale1 * q2.getW());

        Quaternion.releaseTempInstance(q2);

        // Return the interpolated quaternion
        return result.set(x, y, z, w);
    }

    /**
     * Does a spherical linear interpolation between the given start and end quaternions by the given change amount.
     * Stores the result locally.
     * 
     * @param startQuat
     * @param endQuat
     * @param changeAmnt
     * @return this quaternion for chaining.
     * 
     * 
     * @throws NullPointerException
     *             if startQuat or endQuat are null.
     */
    public Quaternion slerpLocal(final ReadOnlyQuaternion startQuat, final ReadOnlyQuaternion endQuat,
            final double changeAmnt) {
        // Check for equality and skip operation.
        if (startQuat.equals(endQuat)) {
            this.set(startQuat);
            return this;
        }

        double result = startQuat.dot(endQuat);
        final Quaternion end = Quaternion.fetchTempInstance().set(endQuat);

        if (result < 0.0) {
            // Negate the second quaternion and the result of the dot product
            end.multiplyLocal(-1);
            result = -result;
        }

        // Set the first and second scale for the interpolation
        double scale0 = 1 - changeAmnt;
        double scale1 = changeAmnt;

        // Check if the angle between the 2 quaternions was big enough to
        // warrant such calculations
        if ((1 - result) > 0.1) {// Get the angle between the 2 quaternions,
            // and then store the sin() of that angle
            final double theta = Math.acos(result);
            final double invSinTheta = 1f / MathUtils.sin(theta);

            // Calculate the scale for q1 and q2, according to the angle and
            // it's sine value
            scale0 = MathUtils.sin((1 - changeAmnt) * theta) * invSinTheta;
            scale1 = MathUtils.sin((changeAmnt * theta)) * invSinTheta;
        }

        // Calculate the x, y, z and w values for the quaternion by using a
        // special form of linear interpolation for quaternions.
        final double x = (scale0 * startQuat.getX()) + (scale1 * end.getX());
        final double y = (scale0 * startQuat.getY()) + (scale1 * end.getY());
        final double z = (scale0 * startQuat.getZ()) + (scale1 * end.getZ());
        final double w = (scale0 * startQuat.getW()) + (scale1 * end.getW());
        set(x, y, z, w);

        Quaternion.releaseTempInstance(end);

        // Return the interpolated quaternion
        return this;
    }

    /**
     * Modifies this quaternion to equal the rotation required to point the z-axis at 'direction' and the y-axis to
     * 'up'.
     * 
     * @param direction
     *            where to 'look' at
     * @param up
     *            a vector indicating the local up direction.
     */
    public void lookAt(final ReadOnlyVector3 direction, final Vector3 up) {
        final Vector3 xAxis = Vector3.fetchTempInstance();
        final Vector3 yAxis = Vector3.fetchTempInstance();
        final Vector3 zAxis = Vector3.fetchTempInstance();
        direction.normalize(zAxis);
        up.normalize(xAxis).crossLocal(zAxis);
        zAxis.cross(xAxis, yAxis);

        fromAxes(xAxis, yAxis, zAxis);

        Vector3.releaseTempInstance(xAxis);
        Vector3.releaseTempInstance(yAxis);
        Vector3.releaseTempInstance(zAxis);
    }

    /**
     * @return the squared magnitude of this quaternion.
     */
    public double magnitudeSquared() {
        return getW() * getW() + getX() * getX() + getY() * getY() + getZ() * getZ();
    }

    /**
     * @return the magnitude of this quaternion. basically sqrt({@link #magnitude()})
     */
    public double magnitude() {
        final double magnitudeSQ = magnitudeSquared();
        if (Double.compare(magnitudeSQ, 1.0) == 0) {
            return 1.0;
        }

        return Math.sqrt(magnitudeSQ);
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param w
     * @return the dot product of this quaternion with the given x,y,z and w values.
     */
    public double dot(final double x, final double y, final double z, final double w) {
        return getX() * x + getY() * y + getZ() * z + getW() * w;
    }

    /**
     * @param quat
     * @return the dot product of this quaternion with the given quaternion.
     */
    public double dot(final ReadOnlyQuaternion quat) {
        return dot(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
    }

    /**
     * Sets the value of this quaternion to (0, 0, 0, 1). Equivalent to calling set(0, 0, 0, 1)
     * 
     * @return this quaternion for chaining
     */
    public Quaternion setIdentity() {
        return set(0, 0, 0, 1);
    }

    /**
     * @return true if this quaternion is (0, 0, 0, 1)
     */
    public boolean isIdentity() {
        if (equals(IDENTITY)) {
            return true;
        }

        return false;
    }

    /**
     * Check a quaternion... if it is null or its doubles are NaN or infinite, return false. Else return true.
     * 
     * @param quat
     *            the quaternion to check
     * @return true or false as stated above.
     */
    public static boolean isValid(final ReadOnlyQuaternion quat) {
        if (quat == null) {
            return false;
        }
        if (Double.isNaN(quat.getX()) || Double.isInfinite(quat.getX())) {
            return false;
        }
        if (Double.isNaN(quat.getY()) || Double.isInfinite(quat.getY())) {
            return false;
        }
        if (Double.isNaN(quat.getZ()) || Double.isInfinite(quat.getZ())) {
            return false;
        }
        if (Double.isNaN(quat.getW()) || Double.isInfinite(quat.getW())) {
            return false;
        }
        return true;
    }

    /**
     * @return the string representation of this quaternion.
     */
    @Override
    public String toString() {
        return "com.ardor3d.math.Quaternion [X=" + getX() + ", Y=" + getY() + ", Z=" + getZ() + ", W=" + getW() + "]";
    }

    /**
     * @return returns a unique code for this quaternion object based on its values. If two quaternions are numerically
     *         equal, they will return the same hash code value.
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
     * @return true if this quaternion and the provided quaternion have the same x, y, z and w values.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReadOnlyQuaternion)) {
            return false;
        }
        final ReadOnlyQuaternion comp = (ReadOnlyQuaternion) o;
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
    public Quaternion clone() {
        try {
            return (Quaternion) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new AssertionError(); // can not happen
        }
    }

    // /////////////////
    // Methods for Savable
    // /////////////////

    public Class<? extends Quaternion> getClassTag() {
        return this.getClass();
    }

    public void write(final Ardor3DExporter e) throws IOException {
        final OutputCapsule capsule = e.getCapsule(this);
        capsule.write(getX(), "x", 0);
        capsule.write(getY(), "y", 0);
        capsule.write(getY(), "z", 0);
        capsule.write(getY(), "w", 1);
    }

    public void read(final Ardor3DImporter e) throws IOException {
        final InputCapsule capsule = e.getCapsule(this);
        setX(capsule.readDouble("x", 0));
        setY(capsule.readDouble("y", 0));
        setZ(capsule.readDouble("z", 0));
        setW(capsule.readDouble("w", 1));
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
     * @return An instance of Quaternion that is intended for temporary use in calculations and so forth. Multiple calls
     *         to the method should return instances of this class that are not currently in use.
     */
    public final static Quaternion fetchTempInstance() {
        if (Debug.useMathPools) {
            return QUAT_POOL.fetch();
        } else {
            return new Quaternion();
=======
package org.json;

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
 * @version 2012-05-29
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
     * @throws JSONException
     * @exception JSONException If a value is a non-finite number or if a name is duplicated.
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
     * @throws JSONException If there is a syntax error in the source string
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
     * @throws JSONException
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
     * @exception JSONException If there is a syntax error in the source
     *  string or a duplicated key.
     */
    public JSONObject(String source) throws JSONException {
        this(new JSONTokener(source));
    }


    /**
     * Construct a JSONObject from a ResourceBundle.
     * @param baseName The ResourceBundle base name.
     * @param locale The Locale to load the ResourceBundle for.
     * @throws JSONException If any JSONExceptions are detected.
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
     * @throws JSONException If the value is an invalid number
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
     * @throws JSONException If the key is null or if the current value
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
     * @throws      JSONException if the key is not found.
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
     * @throws      JSONException
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
     * @throws JSONException if the key is not found or
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
     * @throws   JSONException if the key is not found or if the value cannot
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
     * @throws      JSONException if the key is not found or
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
     * @throws      JSONException if the key is not found or
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
     * @throws   JSONException if the key is not found or if the value cannot
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
     * @throws   JSONException if there is no string value for the key.
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
     * @throws JSONException If there is already a property with this name
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
     * @throws JSONException If n is a non-finite number.
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
     * @throws JSONException If the key is null.
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
     * @throws JSONException
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
     * @throws JSONException If the key is null or if the number is invalid.
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
     * @throws JSONException If the key is null.
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
     * @throws JSONException If the key is null.
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
     * @throws JSONException
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
     * @throws JSONException If the value is non-finite number
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
     * @throws JSONException if the key is a duplicate
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
     * @throws JSONException If the value is a non-finite number.
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
     * @throws JSONException If o is a non-finite number.
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
     * @throws JSONException If the object contains an invalid number.
     */
    public String toString(int indentFactor) throws JSONException {
        StringWriter w = new StringWriter();
        synchronized (w.getBuffer()) {
            return this.write(w, indentFactor, 0).toString();
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    }

    /**
<<<<<<< HEAD
     * Releases a Quaternion back to be used by a future call to fetchTempInstance. TAKE CARE: this Quaternion object
     * should no longer have other classes referencing it or "Bad Things" will happen.
     * 
     * @param mat
     *            the Quaternion to release.
     */
    public final static void releaseTempInstance(final Quaternion mat) {
        if (Debug.useMathPools) {
            QUAT_POOL.release(mat);
        }
    }

    static final class QuaternionPool extends ObjectPool<Quaternion> {
        public QuaternionPool(final int initialSize) {
            super(initialSize);
        }

        @Override
        protected Quaternion newInstance() {
            return new Quaternion();
        }
    }
=======
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
     * @throws JSONException If the value is or contains an invalid number.
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
             if (object instanceof JSONObject || object instanceof JSONArray  ||
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
      * @throws JSONException
      */
     public Writer write(Writer writer) throws JSONException {
        return this.write(writer, 0, 0);
    }


    static final Writer writeValue(Writer writer, Object value,
            int indentFactor, int indent) throws JSONException, IOException {
        if (value == null || value.equals(null)) {
            writer.write("null");
        } else if (value instanceof JSONObject) {
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
     * @throws JSONException
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
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

