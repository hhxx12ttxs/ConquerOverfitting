<<<<<<< HEAD
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang.math;

import java.io.Serializable;

/**
 * <p><code>DoubleRange</code> represents an inclusive range of <code>double</code>s.</p>
 *
 * @author Stephen Colebourne
 * @since 2.0
 * @version $Id: DoubleRange.java 437554 2006-08-28 06:21:41Z bayard $
 */
public final class DoubleRange extends Range implements Serializable {
    
    /**
     * Required for serialization support.
     * 
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 71849363892740L;

    /**
     * The minimum number in this range (inclusive).
     */
    private final double min;
    /**
     * The maximum number in this range (inclusive).
     */
    private final double max;
    
    /**
     * Cached output minObject (class is immutable).
     */
    private transient Double minObject = null;
    /**
     * Cached output maxObject (class is immutable).
     */
    private transient Double maxObject = null;
    /**
     * Cached output hashCode (class is immutable).
     */
    private transient int hashCode = 0;
    /**
     * Cached output toString (class is immutable).
     */
    private transient String toString = null;
    
    /**
     * <p>Constructs a new <code>DoubleRange</code> using the specified
     * number as both the minimum and maximum in this range.</p>
     *
     * @param number  the number to use for this range
     * @throws IllegalArgumentException if the number is <code>NaN</code>
     */
    public DoubleRange(double number) {
        super();
        if (Double.isNaN(number)) {
            throw new IllegalArgumentException("The number must not be NaN");
        }
        this.min = number;
        this.max = number;
    }

    /**
     * <p>Constructs a new <code>DoubleRange</code> using the specified
     * number as both the minimum and maximum in this range.</p>
     *
     * @param number  the number to use for this range, must not
     *  be <code>null</code>
     * @throws IllegalArgumentException if the number is <code>null</code>
     * @throws IllegalArgumentException if the number is <code>NaN</code>
     */
    public DoubleRange(Number number) {
        super();
        if (number == null) {
            throw new IllegalArgumentException("The number must not be null");
        }
        this.min = number.doubleValue();
        this.max = number.doubleValue();
        if (Double.isNaN(min) || Double.isNaN(max)) {
            throw new IllegalArgumentException("The number must not be NaN");
        }
        if (number instanceof Double) {
            this.minObject = (Double) number;
            this.maxObject = (Double) number;
        }
    }

    /**
     * <p>Constructs a new <code>DoubleRange</code> with the specified
     * minimum and maximum numbers (both inclusive).</p>
     * 
     * <p>The arguments may be passed in the order (min,max) or (max,min). The
     * getMinimum and getMaximum methods will return the correct values.</p>
     * 
     * @param number1  first number that defines the edge of the range, inclusive
     * @param number2  second number that defines the edge of the range, inclusive
     * @throws IllegalArgumentException if either number is <code>NaN</code>
     */
    public DoubleRange(double number1, double number2) {
        super();
        if (Double.isNaN(number1) || Double.isNaN(number2)) {
            throw new IllegalArgumentException("The numbers must not be NaN");
        }
        if (number2 < number1) {
            this.min = number2;
            this.max = number1;
        } else {
            this.min = number1;
            this.max = number2;
=======
/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.bounding;

import java.io.IOException;
import java.nio.FloatBuffer;

import com.ardor3d.intersection.IntersectionRecord;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Plane;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyPlane;
import com.ardor3d.math.type.ReadOnlyQuaternion;
import com.ardor3d.math.type.ReadOnlyRay3;
import com.ardor3d.math.type.ReadOnlyTriangle;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.math.type.ReadOnlyPlane.Side;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.geom.BufferUtils;

/**
 * <code>BoundingBox</code> defines an axis-aligned cube that defines a container for a group of vertices of a
 * particular piece of geometry. This box defines a center and extents from that center along the x, y and z axis. <br>
 * <br>
 * A typical usage is to allow the class define the center and radius by calling either <code>containAABB</code> or
 * <code>averagePoints</code>. A call to <code>computeFramePoint</code> in turn calls <code>containAABB</code>.
 */
public class BoundingBox extends BoundingVolume {

    private static final long serialVersionUID = 1L;

    private double _xExtent, _yExtent, _zExtent;

    /**
     * Default constructor instantiates a new <code>BoundingBox</code> object.
     */
    public BoundingBox() {}

    /**
     * Constructor instantiates a new <code>BoundingBox</code> object with given values.
     */
    public BoundingBox(final Vector3 c, final double x, final double y, final double z) {
        center.set(c);
        setXExtent(x);
        setYExtent(y);
        setZExtent(z);
    }

    @Override
    public Type getType() {
        return Type.AABB;
    }

    public void setXExtent(final double xExtent) {
        _xExtent = xExtent;
    }

    public double getXExtent() {
        return _xExtent;
    }

    public void setYExtent(final double yExtent) {
        _yExtent = yExtent;
    }

    public double getYExtent() {
        return _yExtent;
    }

    public void setZExtent(final double zExtent) {
        _zExtent = zExtent;
    }

    public double getZExtent() {
        return _zExtent;
    }

    @Override
    public BoundingVolume transform(final ReadOnlyMatrix3 rotate, final ReadOnlyVector3 translate,
            final ReadOnlyVector3 scale, final BoundingVolume store) {

        BoundingBox box;
        if (store == null || store.getType() != Type.AABB) {
            box = new BoundingBox();
        } else {
            box = (BoundingBox) store;
        }

        center.multiply(scale, box.center);
        rotate.applyPost(box.center, box.center);
        box.center.addLocal(translate);

        final Vector3 compVect1 = Vector3.fetchTempInstance();
        final Vector3 compVect2 = Vector3.fetchTempInstance();

        final Matrix3 transMatrix = Matrix3.fetchTempInstance();
        transMatrix.set(rotate);
        // Make the rotation matrix all positive to get the maximum x/y/z extent
        transMatrix.setValue(0, 0, Math.abs(transMatrix.getValue(0, 0)));
        transMatrix.setValue(0, 1, Math.abs(transMatrix.getValue(0, 1)));
        transMatrix.setValue(0, 2, Math.abs(transMatrix.getValue(0, 2)));
        transMatrix.setValue(1, 0, Math.abs(transMatrix.getValue(1, 0)));
        transMatrix.setValue(1, 1, Math.abs(transMatrix.getValue(1, 1)));
        transMatrix.setValue(1, 2, Math.abs(transMatrix.getValue(1, 2)));
        transMatrix.setValue(2, 0, Math.abs(transMatrix.getValue(2, 0)));
        transMatrix.setValue(2, 1, Math.abs(transMatrix.getValue(2, 1)));
        transMatrix.setValue(2, 2, Math.abs(transMatrix.getValue(2, 2)));

        compVect1.set(getXExtent() * scale.getX(), getYExtent() * scale.getY(), getZExtent() * scale.getZ());
        transMatrix.applyPost(compVect1, compVect2);
        // Assign the biggest rotations after scales.
        box.setXExtent(Math.abs(compVect2.getX()));
        box.setYExtent(Math.abs(compVect2.getY()));
        box.setZExtent(Math.abs(compVect2.getZ()));

        Vector3.releaseTempInstance(compVect1);
        Vector3.releaseTempInstance(compVect2);
        Matrix3.releaseTempInstance(transMatrix);

        return box;
    }

    /**
     * <code>computeFromPoints</code> creates a new Bounding Box from a given set of points. It uses the
     * <code>containAABB</code> method as default.
     * 
     * @param points
     *            the points to contain.
     */
    @Override
    public void computeFromPoints(final FloatBuffer points) {
        containAABB(points);
    }

    /**
     * <code>computeFromTris</code> creates a new Bounding Box from a given set of triangles. It is used in OBBTree
     * calculations.
     * 
     * @param tris
     * @param start
     * @param end
     */
    @Override
    public void computeFromTris(final ReadOnlyTriangle[] tris, final int start, final int end) {
        if (end - start <= 0) {
            return;
        }

        final Vector3 min = Vector3.fetchTempInstance().set(
                new Vector3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        final Vector3 max = Vector3.fetchTempInstance().set(
                new Vector3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));

        for (int i = start; i < end; i++) {
            checkMinMax(min, max, tris[i].getA());
            checkMinMax(min, max, tris[i].getB());
            checkMinMax(min, max, tris[i].getC());
        }

        center.set(min.addLocal(max));
        center.multiplyLocal(0.5);

        setXExtent(max.getX() - center.getX());
        setYExtent(max.getY() - center.getY());
        setZExtent(max.getZ() - center.getZ());

        Vector3.releaseTempInstance(min);
        Vector3.releaseTempInstance(max);
    }

    @Override
    public void computeFromTris(final int[] indices, final Mesh mesh, final int start, final int end) {
        if (end - start <= 0) {
            return;
        }

        final Vector3 min = Vector3.fetchTempInstance().set(
                new Vector3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        final Vector3 max = Vector3.fetchTempInstance().set(
                new Vector3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));

        final Vector3[] verts = { Vector3.fetchTempInstance(), Vector3.fetchTempInstance(), Vector3.fetchTempInstance() };
        for (int i = start; i < end; i++) {
            PickingUtil.getTriangle(mesh, indices[i], verts);
            checkMinMax(min, max, verts[0]);
            checkMinMax(min, max, verts[1]);
            checkMinMax(min, max, verts[2]);
        }

        center.set(min.addLocal(max));
        center.multiplyLocal(0.5);

        setXExtent(max.getX() - center.getX());
        setYExtent(max.getY() - center.getY());
        setZExtent(max.getZ() - center.getZ());

        Vector3.releaseTempInstance(min);
        Vector3.releaseTempInstance(max);
        for (final Vector3 vec : verts) {
            Vector3.releaseTempInstance(vec);
        }
    }

    private void checkMinMax(final Vector3 min, final Vector3 max, final ReadOnlyVector3 point) {
        if (point.getX() < min.getX()) {
            min.setX(point.getX());
        } else if (point.getX() > max.getX()) {
            max.setX(point.getX());
        }
        if (point.getY() < min.getY()) {
            min.setY(point.getY());
        } else if (point.getY() > max.getY()) {
            max.setY(point.getY());
        }
        if (point.getZ() < min.getZ()) {
            min.setZ(point.getZ());
        } else if (point.getZ() > max.getZ()) {
            max.setZ(point.getZ());
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    }

    /**
<<<<<<< HEAD
     * <p>Constructs a new <code>DoubleRange</code> with the specified
     * minimum and maximum numbers (both inclusive).</p>
     * 
     * <p>The arguments may be passed in the order (min,max) or (max,min). The
     * getMinimum and getMaximum methods will return the correct values.</p>
     *
     * @param number1  first number that defines the edge of the range, inclusive
     * @param number2  second number that defines the edge of the range, inclusive
     * @throws IllegalArgumentException if either number is <code>null</code>
     * @throws IllegalArgumentException if either number is <code>NaN</code>
     */
    public DoubleRange(Number number1, Number number2) {
        super();
        if (number1 == null || number2 == null) {
            throw new IllegalArgumentException("The numbers must not be null");
        }
        double number1val = number1.doubleValue();
        double number2val = number2.doubleValue();
        if (Double.isNaN(number1val) || Double.isNaN(number2val)) {
            throw new IllegalArgumentException("The numbers must not be NaN");
        }
        if (number2val < number1val) {
            this.min = number2val;
            this.max = number1val;
            if (number2 instanceof Double) {
                this.minObject = (Double) number2;
            }
            if (number1 instanceof Double) {
                this.maxObject = (Double) number1;
            }
        } else {
            this.min = number1val;
            this.max = number2val;
            if (number1 instanceof Double) {
                this.minObject = (Double) number1;
            }
            if (number2 instanceof Double) {
                this.maxObject = (Double) number2;
            }
        }
    }

    // Accessors
    //--------------------------------------------------------------------

    /**
     * <p>Returns the minimum number in this range.</p>
     *
     * @return the minimum number in this range
     */
    public Number getMinimumNumber() {
        if (minObject == null) {
            minObject = new Double(min);            
        }
        return minObject;
    }

    /**
     * <p>Gets the minimum number in this range as a <code>long</code>.</p>
     * 
     * <p>This conversion can lose information for large values or decimals.</p>
     *
     * @return the minimum number in this range
     */
    public long getMinimumLong() {
        return (long) min;
    }

    /**
     * <p>Gets the minimum number in this range as a <code>int</code>.</p>
     * 
     * <p>This conversion can lose information for large values or decimals.</p>
     *
     * @return the minimum number in this range
     */
    public int getMinimumInteger() {
        return (int) min;
    }

    /**
     * <p>Gets the minimum number in this range as a <code>double</code>.</p>
     *
     * @return the minimum number in this range
     */
    public double getMinimumDouble() {
        return min;
    }

    /**
     * <p>Gets the minimum number in this range as a <code>float</code>.</p>
     * 
     * <p>This conversion can lose information for large values.</p>
     *
     * @return the minimum number in this range
     */
    public float getMinimumFloat() {
        return (float) min;
    }

    /**
     * <p>Returns the maximum number in this range.</p>
     *
     * @return the maximum number in this range
     */
    public Number getMaximumNumber() {
        if (maxObject == null) {
            maxObject = new Double(max);            
        }
        return maxObject;
    }

    /**
     * <p>Gets the maximum number in this range as a <code>long</code>.</p>
     * 
     * <p>This conversion can lose information for large values or decimals.</p>
     *
     * @return the maximum number in this range
     */
    public long getMaximumLong() {
        return (long) max;
    }

    /**
     * <p>Gets the maximum number in this range as a <code>int</code>.</p>
     * 
     * <p>This conversion can lose information for large values or decimals.</p>
     *
     * @return the maximum number in this range
     */
    public int getMaximumInteger() {
        return (int) max;
    }

    /**
     * <p>Gets the maximum number in this range as a <code>double</code>.</p>
     *
     * @return the maximum number in this range
     */
    public double getMaximumDouble() {
        return max;
    }

    /**
     * <p>Gets the maximum number in this range as a <code>float</code>.</p>
     * 
     * <p>This conversion can lose information for large values.</p>
     *
     * @return the maximum number in this range
     */
    public float getMaximumFloat() {
        return (float) max;
    }

    // Tests
    //--------------------------------------------------------------------
    
    /**
     * <p>Tests whether the specified <code>number</code> occurs within
     * this range using <code>double</code> comparison.</p>
     * 
     * <p><code>null</code> is handled and returns <code>false</code>.</p>
     *
     * @param number  the number to test, may be <code>null</code>
     * @return <code>true</code> if the specified number occurs within this range
     */
    public boolean containsNumber(Number number) {
        if (number == null) {
            return false;
        }
        return containsDouble(number.doubleValue());
    }

    /**
     * <p>Tests whether the specified <code>double</code> occurs within
     * this range using <code>double</code> comparison.</p>
     * 
     * <p>This implementation overrides the superclass for performance as it is
     * the most common case.</p>
     * 
     * @param value  the double to test
     * @return <code>true</code> if the specified number occurs within this
     *  range by <code>double</code> comparison
     */
    public boolean containsDouble(double value) {
        return value >= min && value <= max;
    }

    // Range tests
    //--------------------------------------------------------------------

    /**
     * <p>Tests whether the specified range occurs entirely within this range
     * using <code>double</code> comparison.</p>
     * 
     * <p><code>null</code> is handled and returns <code>false</code>.</p>
     *
     * @param range  the range to test, may be <code>null</code>
     * @return <code>true</code> if the specified range occurs entirely within this range
     * @throws IllegalArgumentException if the range is not of this type
     */
    public boolean containsRange(Range range) {
        if (range == null) {
            return false;
        }
        return containsDouble(range.getMinimumDouble())
            && containsDouble(range.getMaximumDouble());
    }

    /**
     * <p>Tests whether the specified range overlaps with this range
     * using <code>double</code> comparison.</p>
     * 
     * <p><code>null</code> is handled and returns <code>false</code>.</p>
     *
     * @param range  the range to test, may be <code>null</code>
     * @return <code>true</code> if the specified range overlaps with this range
     */
    public boolean overlapsRange(Range range) {
        if (range == null) {
            return false;
        }
        return range.containsDouble(min)
            || range.containsDouble(max)
            || containsDouble(range.getMinimumDouble());
    }

    // Basics
    //--------------------------------------------------------------------

    /**
     * <p>Compares this range to another object to test if they are equal.</p>.
     * 
     * <p>To be equal, the class, minimum and maximum must be equal.</p>
     *
     * @param obj the reference object with which to compare
     * @return <code>true</code> if this object is equal
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof DoubleRange == false) {
            return false;
        }
        DoubleRange range = (DoubleRange) obj;
        return (Double.doubleToLongBits(min) == Double.doubleToLongBits(range.min) &&
                Double.doubleToLongBits(max) == Double.doubleToLongBits(range.max));
    }

    /**
     * <p>Gets a hashCode for the range.</p>
     *
     * @return a hash code value for this object
     */
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = 17;
            hashCode = 37 * hashCode + getClass().hashCode();
            long lng = Double.doubleToLongBits(min);
            hashCode = 37 * hashCode + ((int) (lng ^ (lng >> 32)));
            lng = Double.doubleToLongBits(max);
            hashCode = 37 * hashCode + ((int) (lng ^ (lng >> 32)));
        }
        return hashCode;
    }

    /**
     * <p>Gets the range as a <code>String</code>.</p>
     *
     * <p>The format of the String is 'Range[<i>min</i>,<i>max</i>]'.</p>
     *
     * @return the <code>String</code> representation of this range
     */
    public String toString() {
        if (toString == null) {
            StringBuffer buf = new StringBuffer(32);
            buf.append("Range[");
            buf.append(min);
            buf.append(',');
            buf.append(max);
            buf.append(']');
            toString = buf.toString();
        }
        return toString;
    }

=======
     * <code>containAABB</code> creates a minimum-volume axis-aligned bounding box of the points, then selects the
     * smallest enclosing sphere of the box with the sphere centered at the boxes center.
     * 
     * @param points
     *            the list of points.
     */
    public void containAABB(final FloatBuffer points) {
        if (points == null) {
            return;
        }

        points.rewind();
        if (points.remaining() <= 2) {
            return;
        }

        final Vector3 compVect = Vector3.fetchTempInstance();
        BufferUtils.populateFromBuffer(compVect, points, 0);
        double minX = compVect.getX(), minY = compVect.getY(), minZ = compVect.getZ();
        double maxX = compVect.getX(), maxY = compVect.getY(), maxZ = compVect.getZ();

        for (int i = 1, len = points.remaining() / 3; i < len; i++) {
            BufferUtils.populateFromBuffer(compVect, points, i);

            if (compVect.getX() < minX) {
                minX = compVect.getX();
            } else if (compVect.getX() > maxX) {
                maxX = compVect.getX();
            }

            if (compVect.getY() < minY) {
                minY = compVect.getY();
            } else if (compVect.getY() > maxY) {
                maxY = compVect.getY();
            }

            if (compVect.getZ() < minZ) {
                minZ = compVect.getZ();
            } else if (compVect.getZ() > maxZ) {
                maxZ = compVect.getZ();
            }
        }
        Vector3.releaseTempInstance(compVect);

        center.set(minX + maxX, minY + maxY, minZ + maxZ);
        center.multiplyLocal(0.5f);

        setXExtent(maxX - center.getX());
        setYExtent(maxY - center.getY());
        setZExtent(maxZ - center.getZ());
    }

    /**
     * <code>transform</code> modifies the center of the box to reflect the change made via a rotation, translation and
     * scale.
     * 
     * @param rotate
     *            the rotation change.
     * @param translate
     *            the translation change.
     * @param scale
     *            the size change.
     * @param store
     *            box to store result in
     */
    @Override
    public BoundingVolume transform(final ReadOnlyQuaternion rotate, final ReadOnlyVector3 translate,
            final ReadOnlyVector3 scale, final BoundingVolume store) {

        BoundingBox box;
        if (store == null || store.getType() != Type.AABB) {
            box = new BoundingBox();
        } else {
            box = (BoundingBox) store;
        }

        center.multiply(scale, box.center);
        rotate.apply(box.center, box.center);
        box.center.addLocal(translate);

        final Vector3 compVect1 = Vector3.fetchTempInstance();
        final Vector3 compVect2 = Vector3.fetchTempInstance();

        final Matrix3 transMatrix = Matrix3.fetchTempInstance();
        transMatrix.set(rotate);
        // Make the rotation matrix all positive to get the maximum x/y/z extent
        transMatrix.setValue(0, 0, Math.abs(transMatrix.getValue(0, 0)));
        transMatrix.setValue(0, 1, Math.abs(transMatrix.getValue(0, 1)));
        transMatrix.setValue(0, 2, Math.abs(transMatrix.getValue(0, 2)));
        transMatrix.setValue(1, 0, Math.abs(transMatrix.getValue(1, 0)));
        transMatrix.setValue(1, 1, Math.abs(transMatrix.getValue(1, 1)));
        transMatrix.setValue(1, 2, Math.abs(transMatrix.getValue(1, 2)));
        transMatrix.setValue(2, 0, Math.abs(transMatrix.getValue(2, 0)));
        transMatrix.setValue(2, 1, Math.abs(transMatrix.getValue(2, 1)));
        transMatrix.setValue(2, 2, Math.abs(transMatrix.getValue(2, 2)));

        compVect1.set(getXExtent() * scale.getX(), getYExtent() * scale.getY(), getZExtent() * scale.getZ());
        transMatrix.applyPost(compVect1, compVect2);
        // Assign the biggest rotations after scales.
        box.setXExtent(Math.abs(compVect2.getX()));
        box.setYExtent(Math.abs(compVect2.getY()));
        box.setZExtent(Math.abs(compVect2.getZ()));

        Vector3.releaseTempInstance(compVect1);
        Vector3.releaseTempInstance(compVect2);
        Matrix3.releaseTempInstance(transMatrix);

        return box;
    }

    /**
     * <code>whichSide</code> takes a plane (typically provided by a view frustum) to determine which side this bound is
     * on.
     * 
     * @param plane
     *            the plane to check against.
     */
    @Override
    public Side whichSide(final ReadOnlyPlane plane) {
        final ReadOnlyVector3 normal = plane.getNormal();
        final double radius = Math.abs(getXExtent() * normal.getX()) + Math.abs(getYExtent() * normal.getY())
                + Math.abs(getZExtent() * normal.getZ());

        final double distance = plane.pseudoDistance(center);

        if (distance < -radius) {
            return Plane.Side.Inside;
        } else if (distance > radius) {
            return Plane.Side.Outside;
        } else {
            return Plane.Side.Neither;
        }
    }

    /**
     * <code>merge</code> combines this sphere with a second bounding sphere. This new sphere contains both bounding
     * spheres and is returned.
     * 
     * @param volume
     *            the sphere to combine with this sphere.
     * @return the new sphere
     */
    @Override
    public BoundingVolume merge(final BoundingVolume volume) {
        if (volume == null) {
            return this;
        }

        switch (volume.getType()) {
            case AABB: {
                final BoundingBox vBox = (BoundingBox) volume;
                return merge(vBox.center, vBox.getXExtent(), vBox.getYExtent(), vBox.getZExtent(), new BoundingBox(
                        new Vector3(0, 0, 0), 0, 0, 0));
            }

            case Sphere: {
                final BoundingSphere vSphere = (BoundingSphere) volume;
                return merge(vSphere.center, vSphere.getRadius(), vSphere.getRadius(), vSphere.getRadius(),
                        new BoundingBox(new Vector3(0, 0, 0), 0, 0, 0));
            }

            case OBB: {
                final OrientedBoundingBox box = (OrientedBoundingBox) volume;
                final BoundingBox rVal = (BoundingBox) this.clone(null);
                return rVal.mergeOBB(box);
            }

            default:
                return null;
        }
    }

    /**
     * <code>mergeLocal</code> combines this sphere with a second bounding sphere locally. Altering this sphere to
     * contain both the original and the additional sphere volumes;
     * 
     * @param volume
     *            the sphere to combine with this sphere.
     * @return this
     */
    @Override
    public BoundingVolume mergeLocal(final BoundingVolume volume) {
        if (volume == null) {
            return this;
        }

        switch (volume.getType()) {
            case AABB: {
                final BoundingBox vBox = (BoundingBox) volume;
                return merge(vBox.center, vBox.getXExtent(), vBox.getYExtent(), vBox.getZExtent(), this);
            }

            case Sphere: {
                final BoundingSphere vSphere = (BoundingSphere) volume;
                return merge(vSphere.center, vSphere.getRadius(), vSphere.getRadius(), vSphere.getRadius(), this);
            }

            case OBB: {
                return mergeOBB((OrientedBoundingBox) volume);
            }

            default:
                return null;
        }
    }

    /**
     * Merges this AABB with the given OBB.
     * 
     * @param volume
     *            the OBB to merge this AABB with.
     * @return This AABB extended to fit the given OBB.
     */
    private BoundingBox mergeOBB(final OrientedBoundingBox volume) {
        if (!volume.correctCorners) {
            volume.computeCorners();
        }

        double minX, minY, minZ;
        double maxX, maxY, maxZ;

        minX = center.getX() - getXExtent();
        minY = center.getY() - getYExtent();
        minZ = center.getZ() - getZExtent();

        maxX = center.getX() + getXExtent();
        maxY = center.getY() + getYExtent();
        maxZ = center.getZ() + getZExtent();

        for (int i = 1; i < volume.vectorStore.length; i++) {
            final Vector3 temp = volume.vectorStore[i];
            if (temp.getX() < minX) {
                minX = temp.getX();
            } else if (temp.getX() > maxX) {
                maxX = temp.getX();
            }

            if (temp.getY() < minY) {
                minY = temp.getY();
            } else if (temp.getY() > maxY) {
                maxY = temp.getY();
            }

            if (temp.getZ() < minZ) {
                minZ = temp.getZ();
            } else if (temp.getZ() > maxZ) {
                maxZ = temp.getZ();
            }
        }

        center.set(minX + maxX, minY + maxY, minZ + maxZ);
        center.multiplyLocal(0.5);

        setXExtent(maxX - center.getX());
        setYExtent(maxY - center.getY());
        setZExtent(maxZ - center.getZ());
        return this;
    }

    /**
     * <code>merge</code> combines this bounding box with another box which is defined by the center, x, y, z extents.
     * 
     * @param boxCenter
     *            the center of the box to merge with
     * @param boxX
     *            the x extent of the box to merge with.
     * @param boxY
     *            the y extent of the box to merge with.
     * @param boxZ
     *            the z extent of the box to merge with.
     * @param rVal
     *            the resulting merged box.
     * @return the resulting merged box.
     */
    private BoundingBox merge(final Vector3 boxCenter, final double boxX, final double boxY, final double boxZ,
            final BoundingBox rVal) {
        final Vector3 compVect1 = Vector3.fetchTempInstance();
        final Vector3 compVect2 = Vector3.fetchTempInstance();

        compVect1.setX(center.getX() - getXExtent());
        if (compVect1.getX() > boxCenter.getX() - boxX) {
            compVect1.setX(boxCenter.getX() - boxX);
        }
        compVect1.setY(center.getY() - getYExtent());
        if (compVect1.getY() > boxCenter.getY() - boxY) {
            compVect1.setY(boxCenter.getY() - boxY);
        }
        compVect1.setZ(center.getZ() - getZExtent());
        if (compVect1.getZ() > boxCenter.getZ() - boxZ) {
            compVect1.setZ(boxCenter.getZ() - boxZ);
        }

        compVect2.setX(center.getX() + getXExtent());
        if (compVect2.getX() < boxCenter.getX() + boxX) {
            compVect2.setX(boxCenter.getX() + boxX);
        }
        compVect2.setY(center.getY() + getYExtent());
        if (compVect2.getY() < boxCenter.getY() + boxY) {
            compVect2.setY(boxCenter.getY() + boxY);
        }
        compVect2.setZ(center.getZ() + getZExtent());
        if (compVect2.getZ() < boxCenter.getZ() + boxZ) {
            compVect2.setZ(boxCenter.getZ() + boxZ);
        }

        center.set(compVect2).addLocal(compVect1).multiplyLocal(0.5f);

        setXExtent(compVect2.getX() - center.getX());
        setYExtent(compVect2.getY() - center.getY());
        setZExtent(compVect2.getZ() - center.getZ());

        Vector3.releaseTempInstance(compVect1);
        Vector3.releaseTempInstance(compVect2);

        return rVal;
    }

    /**
     * <code>clone</code> creates a new BoundingBox object containing the same data as this one.
     * 
     * @param store
     *            where to store the cloned information. if null or wrong class, a new store is created.
     * @return the new BoundingBox
     */
    @Override
    public BoundingVolume clone(final BoundingVolume store) {
        if (store != null && store.getType() == Type.AABB) {
            final BoundingBox rVal = (BoundingBox) store;
            rVal.center.set(center);
            rVal.setXExtent(_xExtent);
            rVal.setYExtent(_yExtent);
            rVal.setZExtent(_zExtent);
            rVal.checkPlane = checkPlane;
            return rVal;
        }

        final BoundingBox rVal = new BoundingBox(center, getXExtent(), getYExtent(), getZExtent());
        return rVal;
    }

    /**
     * <code>toString</code> returns the string representation of this object. The form is:
     * "Radius: RRR.SSSS Center: <Vector>".
     * 
     * @return the string representation of this.
     */
    @Override
    public String toString() {
        return "com.ardor3d.scene.BoundingBox [Center: " + center + "  xExtent: " + getXExtent() + "  yExtent: "
                + getYExtent() + "  zExtent: " + getZExtent() + "]";
    }

    /**
     * intersects determines if this Bounding Box intersects with another given bounding volume. If so, true is
     * returned, otherwise, false is returned.
     * 
     * @see com.ardor3d.bounding.BoundingVolume#intersects(com.ardor3d.bounding.BoundingVolume)
     */
    @Override
    public boolean intersects(final BoundingVolume bv) {
        if (bv == null) {
            return false;
        }

        return bv.intersectsBoundingBox(this);
    }

    /**
     * determines if this bounding box intersects a given bounding sphere.
     * 
     * @see com.ardor3d.bounding.BoundingVolume#intersectsSphere(com.ardor3d.bounding.BoundingSphere)
     */
    @Override
    public boolean intersectsSphere(final BoundingSphere bs) {
        if (!Vector3.isValid(center) || !Vector3.isValid(bs.center)) {
            return false;
        }

        if (Math.abs(center.getX() - bs.getCenter().getX()) < bs.getRadius() + getXExtent()
                && Math.abs(center.getY() - bs.getCenter().getY()) < bs.getRadius() + getYExtent()
                && Math.abs(center.getZ() - bs.getCenter().getZ()) < bs.getRadius() + getZExtent()) {
            return true;
        }

        return false;
    }

    /**
     * determines if this bounding box intersects a given bounding box. If the two boxes intersect in any way, true is
     * returned. Otherwise, false is returned.
     * 
     * @see com.ardor3d.bounding.BoundingVolume#intersectsBoundingBox(com.ardor3d.bounding.BoundingBox)
     */
    @Override
    public boolean intersectsBoundingBox(final BoundingBox bb) {
        if (!Vector3.isValid(center) || !Vector3.isValid(bb.center)) {
            return false;
        }

        if (center.getX() + getXExtent() < bb.center.getX() - bb.getXExtent()
                || center.getX() - getXExtent() > bb.center.getX() + bb.getXExtent()) {
            return false;
        } else if (center.getY() + getYExtent() < bb.center.getY() - bb.getYExtent()
                || center.getY() - getYExtent() > bb.center.getY() + bb.getYExtent()) {
            return false;
        } else if (center.getZ() + getZExtent() < bb.center.getZ() - bb.getZExtent()
                || center.getZ() - getZExtent() > bb.center.getZ() + bb.getZExtent()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * determines if this bounding box intersects with a given oriented bounding box.
     * 
     * @see com.ardor3d.bounding.BoundingVolume#intersectsOrientedBoundingBox(com.ardor3d.bounding.OrientedBoundingBox)
     */
    @Override
    public boolean intersectsOrientedBoundingBox(final OrientedBoundingBox obb) {
        return obb.intersectsBoundingBox(this);
    }

    /**
     * determines if this bounding box intersects with a given ray object. If an intersection has occurred, true is
     * returned, otherwise false is returned.
     * 
     * @see com.ardor3d.bounding.BoundingVolume#intersects(com.ardor3d.math.Ray)
     */
    @Override
    public boolean intersects(final ReadOnlyRay3 ray) {
        if (!Vector3.isValid(center)) {
            return false;
        }

        final Vector3 compVect1 = Vector3.fetchTempInstance();
        final Vector3 compVect2 = Vector3.fetchTempInstance();

        try {
            final Vector3 diff = ray.getOrigin().subtract(getCenter(), compVect1);

            final double fWdU0 = ray.getDirection().dot(Vector3.UNIT_X);
            final double fAWdU0 = Math.abs(fWdU0);
            final double fDdU0 = diff.dot(Vector3.UNIT_X);
            final double fADdU0 = Math.abs(fDdU0);
            if (fADdU0 > getXExtent() && fDdU0 * fWdU0 >= 0.0) {
                return false;
            }

            final double fWdU1 = ray.getDirection().dot(Vector3.UNIT_Y);
            final double fAWdU1 = Math.abs(fWdU1);
            final double fDdU1 = diff.dot(Vector3.UNIT_Y);
            final double fADdU1 = Math.abs(fDdU1);
            if (fADdU1 > getYExtent() && fDdU1 * fWdU1 >= 0.0) {
                return false;
            }

            final double fWdU2 = ray.getDirection().dot(Vector3.UNIT_Z);
            final double fAWdU2 = Math.abs(fWdU2);
            final double fDdU2 = diff.dot(Vector3.UNIT_Z);
            final double fADdU2 = Math.abs(fDdU2);
            if (fADdU2 > getZExtent() && fDdU2 * fWdU2 >= 0.0) {
                return false;
            }

            final Vector3 wCrossD = ray.getDirection().cross(diff, compVect2);

            final double fAWxDdU0 = Math.abs(wCrossD.dot(Vector3.UNIT_X));
            double rhs = getYExtent() * fAWdU2 + getZExtent() * fAWdU1;
            if (fAWxDdU0 > rhs) {
                return false;
            }

            final double fAWxDdU1 = Math.abs(wCrossD.dot(Vector3.UNIT_Y));
            rhs = getXExtent() * fAWdU2 + getZExtent() * fAWdU0;
            if (fAWxDdU1 > rhs) {
                return false;
            }

            final double fAWxDdU2 = Math.abs(wCrossD.dot(Vector3.UNIT_Z));
            rhs = getXExtent() * fAWdU1 + getYExtent() * fAWdU0;
            if (fAWxDdU2 > rhs) {
                return false;
            }

            return true;
        } finally {
            Vector3.releaseTempInstance(compVect1);
            Vector3.releaseTempInstance(compVect2);
        }
    }

    /**
     * @see com.ardor3d.bounding.BoundingVolume#intersectsWhere(com.ardor3d.math.Ray)
     */
    @Override
    public IntersectionRecord intersectsWhere(final ReadOnlyRay3 ray) {
        final Vector3 compVect1 = Vector3.fetchTempInstance();
        final Vector3 compVect2 = Vector3.fetchTempInstance();

        final Vector3 diff = ray.getOrigin().subtract(center, compVect1);

        final ReadOnlyVector3 direction = ray.getDirection();

        final double[] t = { 0.0, Double.POSITIVE_INFINITY };

        final double saveT0 = t[0], saveT1 = t[1];
        final boolean notEntirelyClipped = clip(direction.getX(), -diff.getX() - getXExtent(), t)
                && clip(-direction.getX(), diff.getX() - getXExtent(), t)
                && clip(direction.getY(), -diff.getY() - getYExtent(), t)
                && clip(-direction.getY(), diff.getY() - getYExtent(), t)
                && clip(direction.getZ(), -diff.getZ() - getZExtent(), t)
                && clip(-direction.getZ(), diff.getZ() - getZExtent(), t);

        if (notEntirelyClipped && (Double.compare(t[0], saveT0) != 0 || Double.compare(t[1], saveT1) != 0)) {
            if (Double.compare(t[1], t[0]) > 0) {
                final double[] distances = t;
                final Vector3[] points = new Vector3[] {
                        new Vector3(ray.getDirection()).multiplyLocal(distances[0]).addLocal(ray.getOrigin()),
                        new Vector3(ray.getDirection()).multiplyLocal(distances[1]).addLocal(ray.getOrigin()) };
                final IntersectionRecord record = new IntersectionRecord(distances, points);
                Vector3.releaseTempInstance(compVect1);
                Vector3.releaseTempInstance(compVect2);
                return record;
            }

            final double[] distances = new double[] { t[0] };
            final Vector3[] points = new Vector3[] { new Vector3(ray.getDirection()).multiplyLocal(distances[0])
                    .addLocal(ray.getOrigin()), };
            final IntersectionRecord record = new IntersectionRecord(distances, points);

            Vector3.releaseTempInstance(compVect1);
            Vector3.releaseTempInstance(compVect2);
            return record;
        }

        return new IntersectionRecord();

    }

    @Override
    public boolean contains(final ReadOnlyVector3 point) {
        return Math.abs(center.getX() - point.getX()) < getXExtent()
                && Math.abs(center.getY() - point.getY()) < getYExtent()
                && Math.abs(center.getZ() - point.getZ()) < getZExtent();
    }

    @Override
    public double distanceToEdge(final ReadOnlyVector3 point) {
        // compute coordinates of point in box coordinate system
        final Vector3 closest = point.subtract(center, Vector3.fetchTempInstance());

        // project test point onto box
        double sqrDistance = 0.0;
        double delta;

        if (closest.getX() < -getXExtent()) {
            delta = closest.getX() + getXExtent();
            sqrDistance += delta * delta;
            closest.setX(-getXExtent());
        } else if (closest.getX() > getXExtent()) {
            delta = closest.getX() - getXExtent();
            sqrDistance += delta * delta;
            closest.setX(getXExtent());
        }

        if (closest.getY() < -getYExtent()) {
            delta = closest.getY() + getYExtent();
            sqrDistance += delta * delta;
            closest.setY(-getYExtent());
        } else if (closest.getY() > getYExtent()) {
            delta = closest.getY() - getYExtent();
            sqrDistance += delta * delta;
            closest.setY(getYExtent());
        }

        if (closest.getZ() < -getZExtent()) {
            delta = closest.getZ() + getZExtent();
            sqrDistance += delta * delta;
            closest.setZ(-getZExtent());
        } else if (closest.getZ() > getZExtent()) {
            delta = closest.getZ() - getZExtent();
            sqrDistance += delta * delta;
            closest.setZ(getZExtent());
        }
        Vector3.releaseTempInstance(closest);

        return Math.sqrt(sqrDistance);
    }

    /**
     * <code>clip</code> determines if a line segment intersects the current test plane.
     * 
     * @param denom
     *            the denominator of the line segment.
     * @param numer
     *            the numerator of the line segment.
     * @param t
     *            test values of the plane.
     * @return true if the line segment intersects the plane, false otherwise.
     */
    private boolean clip(final double denom, final double numer, final double[] t) {
        // Return value is 'true' if line segment intersects the current test
        // plane. Otherwise 'false' is returned in which case the line segment
        // is entirely clipped.
        if (Double.compare(denom, 0.0) > 0) {
            if (Double.compare(numer, denom * t[1]) > 0) {
                return false;
            }
            if (Double.compare(numer, denom * t[0]) > 0) {
                t[0] = numer / denom;
            }
            return true;
        } else if (Double.compare(denom, 0.0) < 0) {
            if (Double.compare(numer, denom * t[0]) > 0) {
                return false;
            }
            if (Double.compare(numer, denom * t[1]) > 0) {
                t[1] = numer / denom;
            }
            return true;
        } else {
            return Double.compare(numer, 0.0) <= 0;
        }
    }

    /**
     * Query extent.
     * 
     * @param store
     *            where extent gets stored - null to return a new vector
     * @return store / new vector
     */
    public Vector3 getExtent(Vector3 store) {
        if (store == null) {
            store = new Vector3();
        }
        store.set(getXExtent(), getYExtent(), getZExtent());
        return store;
    }

    @Override
    public void write(final Ardor3DExporter e) throws IOException {
        super.write(e);
        final OutputCapsule capsule = e.getCapsule(this);
        capsule.write(getXExtent(), "xExtent", 0);
        capsule.write(getYExtent(), "yExtent", 0);
        capsule.write(getZExtent(), "zExtent", 0);
    }

    @Override
    public void read(final Ardor3DImporter e) throws IOException {
        super.read(e);
        final InputCapsule capsule = e.getCapsule(this);
        setXExtent(capsule.readDouble("xExtent", 0));
        setYExtent(capsule.readDouble("yExtent", 0));
        setZExtent(capsule.readDouble("zExtent", 0));
    }

    @Override
    public double getVolume() {
        return (8 * getXExtent() * getYExtent() * getZExtent());
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

