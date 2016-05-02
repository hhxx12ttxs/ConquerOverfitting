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
        }
    }

    /**
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
}

=======
//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
package org.nrg.plexiViewer.lite.gui;


import java.awt.*;
import java.awt.event.*;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;

public class PlexiAdjusterWindow extends Frame implements AdjustmentListener, WindowListener, Runnable {

    static final int AUTO_THRESHOLD = 5000;
    static final String[] channelLabels = {"Red", "Green", "Blue", "Cyan", "Magenta", "Yellow", "RGB"};
    static final int[] channelConstants = {4, 2, 1, 3, 5, 6, 7};
    
    ContrastPlot plot = new ContrastPlot();
	Thread thread = null;
	
	private static PlexiAdjusterWindow adjuster=null;
	
    public boolean instance;
        
    int minSliderValue=-1, maxSliderValue=-1, brightnessValue=-1, contrastValue=-1;
    int sliderRange = 256;
    boolean doAutoAdjust,doReset,doSet,doApplyLut,doThreshold,doUpdate;
    
    Panel panel, tPanel;
    Button autoB, resetB, setB, applyB, threshB, updateB;
    int previousImageID;
    int previousType;
    Object previousSnapshot;
    ImageJ ij;
    double min, max;
    double previousMin, previousMax;
    double defaultMin, defaultMax;
    int contrast, brightness;
    boolean RGBImage;
    Scrollbar minSlider, maxSlider, contrastSlider, brightnessSlider;
    Label minLabel, maxLabel, windowLabel, levelLabel;
    boolean done;
    int autoThreshold;
    GridBagLayout gridbag;
    GridBagConstraints c;
    int y = 0;
    boolean windowLevel, balance;
    Font monoFont = new Font("Monospaced", Font.PLAIN, 12);
    Font sanFont = new Font("SansSerif", Font.PLAIN, 12);
    int channels = 7; // RGB
    Choice choice;
	ImagePlus imp;

    public PlexiAdjusterWindow() {
        super("B&C");
        addWindowListener(this);
		instance=false;
    }
    
	public static PlexiAdjusterWindow GetInstance() {
			if (adjuster==null)
				adjuster = new PlexiAdjusterWindow();
			return adjuster;		
	}
    
    /*public void show(java.util.List imageViewers) {
		init();
    	Iterator iter = imageViewers.iterator();
    	while (iter.hasNext()) {
			XNATImageViewerI mrimage = (XNATImageViewerI)iter.next();
			show(mrimage.getImageCopy());
    	}
    }*/

	//Use in conjunction with method setCurrentWindow     
	public void display() {
		if (WindowManager.getCurrentWindow()!=null)
		{
		  imp = WindowManager.getCurrentWindow().getImagePlus();
		  //System.out.println("Adjuster " + imp.getTitle()); 	
		  if (!instance) {
			  init();
			  instance = true;
		  }else {
			  this.setVisible(true);
		  }
		  initRest();
		}else {
			System.out.println("XNATAdjuster::There is no image to display");  		
		}
		
	  }
    
    private void init() {
		windowLevel = false;
		balance=false;
        ij = IJ.getInstance();
        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        setLayout(gridbag);
        
        // plot
        c.gridx = 0;
        y = 0;
        c.gridy = y++;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(10, 10, 0, 10);
        gridbag.setConstraints(plot, c);
        add(plot); 
        
        // min and max labels
        if (!windowLevel) {
            panel = new Panel();
            c.gridy = y++;
            c.insets = new Insets(0, 10, 0, 10);
            gridbag.setConstraints(panel, c);
            panel.setLayout(new BorderLayout());
            minLabel = new Label("      ", Label.LEFT);
            minLabel.setFont(monoFont);
            panel.add("West", minLabel);
            maxLabel = new Label("      " , Label.RIGHT);
            maxLabel.setFont(monoFont);
            panel.add("East", maxLabel);
            add(panel);
        }

        // min slider
        if (!windowLevel) {
            minSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange);
            c.gridy = y++;
            c.insets = new Insets(2, 10, 0, 10);
            gridbag.setConstraints(minSlider, c);
            add(minSlider);
            minSlider.addAdjustmentListener(this);
            minSlider.setUnitIncrement(1);
            addLabel("Minimum", null);
        }

        // max slider
        if (!windowLevel) {
            maxSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange);
            c.gridy = y++;
            c.insets = new Insets(2, 10, 0, 10);
            gridbag.setConstraints(maxSlider, c);
            add(maxSlider);
            maxSlider.addAdjustmentListener(this);
            maxSlider.setUnitIncrement(1);
            addLabel("Maximum", null);
        }
        
        // brightness slider
        brightnessSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange);
        c.gridy = y++;
        c.insets = new Insets(windowLevel?12:2, 10, 0, 10);
        gridbag.setConstraints(brightnessSlider, c);
        add(brightnessSlider);
        brightnessSlider.addAdjustmentListener(this);
        brightnessSlider.setUnitIncrement(1);
        if (windowLevel)
            addLabel("Level: ", levelLabel=new TrimmedLabel("        "));
        else
            addLabel("Brightness", null);
            
        // contrast slider
        if (!balance) {
            contrastSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange);
            c.gridy = y++;
            c.insets = new Insets(2, 10, 0, 10);
            gridbag.setConstraints(contrastSlider, c);
            add(contrastSlider);
            contrastSlider.addAdjustmentListener(this);
            contrastSlider.setUnitIncrement(1);
            if (windowLevel)
                addLabel("Window: ", windowLabel=new TrimmedLabel("        "));
            else
                addLabel("Contrast", null);
        }
        
        pack();
        GUI.center(this);
        show();
    }
     
    void initRest() {
		if (done)
			done = false;
		if (thread==null) { 
			thread = new Thread(this, "ContrastAdjuster");
			thread.start();
		}
			
		setup();
    }
        
    void addLabel(String text, Label label2) {
        panel = new Panel();
        c.gridy = y++;
        c.insets = new Insets(0, 10, 0, 0);
        gridbag.setConstraints(panel, c);
        panel.setLayout(new FlowLayout(label2==null?FlowLayout.CENTER:FlowLayout.LEFT, 0, 0));
        Label label= new TrimmedLabel(text);
        label.setFont(sanFont);
        panel.add(label);
        if (label2!=null) {
            label2.setFont(monoFont);
            label2.setAlignment(Label.LEFT);
            panel.add(label2);
        }
        add(panel);
    }

    void setup() {
        if (imp!=null) {
            //IJ.write("setup");
           // System.out.println("Adjuster " + imp.getTitle());
            ImageProcessor ip = imp.getProcessor();
            setup(imp);
            updatePlot();
            updateLabels(imp, ip);
            imp.updateAndDraw();
        }
    }
    
    public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getSource()==minSlider)
            minSliderValue = minSlider.getValue();
        else if (e.getSource()==maxSlider)
            maxSliderValue = maxSlider.getValue();
        else if (e.getSource()==contrastSlider)
            contrastValue = contrastSlider.getValue();
        else
            brightnessValue = brightnessSlider.getValue();
		notify();

    }

    
    ImageProcessor setup(ImagePlus imp) {
        ImageProcessor ip = imp.getProcessor();
        int type = imp.getType();
        RGBImage = type==ImagePlus.COLOR_RGB;
        boolean snapshotChanged = RGBImage && previousSnapshot!=null && ((ColorProcessor)ip).getSnapshotPixels()!=previousSnapshot;
        if (imp.getID()!=previousImageID || snapshotChanged || type!=previousType)
            setupNewImage(imp, ip);
        previousImageID = imp.getID();
        previousType = type;
        return ip;
    }

    void setupNewImage(ImagePlus imp, ImageProcessor ip)  {
        //IJ.write("setupNewImage");
        previousMin = min;
        previousMax = max;
        if (RGBImage) {
            ip.snapshot();
            previousSnapshot = ((ColorProcessor)ip).getSnapshotPixels();
        } else
            previousSnapshot = null;
        double min2 = ip.getMin();
        double max2 = ip.getMax();
        if (imp.getType()==ImagePlus.COLOR_RGB)
            {min2=0.0; max2=255.0;}
        if ((ip instanceof ShortProcessor) || (ip instanceof FloatProcessor)) {
            ip.resetMinAndMax();
            defaultMin = ip.getMin();
            defaultMax = ip.getMax();
        } else {
            defaultMin = 0;
            defaultMax = 255;
        }
        setMinAndMax(ip, min2, max2);
        min = ip.getMin();
        max = ip.getMax();
        if (IJ.debugMode) {
            IJ.log("min: " + min);
            IJ.log("max: " + max);
            IJ.log("defaultMin: " + defaultMin);
            IJ.log("defaultMax: " + defaultMax);
        }
        plot.defaultMin = defaultMin;
        plot.defaultMax = defaultMax;
        plot.histogram = null;
        updateScrollBars(null);
        if (!doReset)
            plotHistogram(imp);
        autoThreshold = 0;
    }
    
    void setMinAndMax(ImageProcessor ip, double min, double max) {
        if (channels!=7 && ip instanceof ColorProcessor)
            ((ColorProcessor)ip).setMinAndMax(min, max, channels);
        else {
			ip.setMinAndMax(min, max);
        }
    }

    void updatePlot() {
       plot.min = min;
       plot.max = max;
       plot.repaint();
    }
    
    void updateLabels(ImagePlus imp, ImageProcessor ip) {
        double min = ip.getMin();
        double max = ip.getMax();
        int type = imp.getType();
        Calibration cal = imp.getCalibration();
        boolean realValue = type==ImagePlus.GRAY32;
        if (cal.calibrated()) {
            min = cal.getCValue((int)min);
            max = cal.getCValue((int)max);
            if (type!=ImagePlus.GRAY16)
                realValue = true;
        }
        int digits = realValue?2:0;
        if (windowLevel) {
            //IJ.log(min+" "+max);
            double window = max-min;
            double level = min+(window)/2.0;
            windowLabel.setText(IJ.d2s(window, digits));
            levelLabel.setText(IJ.d2s(level, digits));
        } else {
           minLabel.setText(IJ.d2s(min, digits));
           maxLabel.setText(IJ.d2s(max, digits));
        }
    }

    void updateScrollBars(Scrollbar sb) {
        if (sb==null || sb!=contrastSlider) {
            double mid = sliderRange/2;
            double c = ((defaultMax-defaultMin)/(max-min))*mid;
            if (c>mid)
                c = sliderRange - ((max-min)/(defaultMax-defaultMin))*mid;
            contrast = (int)c;
            if (contrastSlider!=null)
                contrastSlider.setValue(contrast);
        }
        if (sb==null || sb!=brightnessSlider) {
            double level = min + (max-min)/2.0;
            double normalizedLevel = 1.0 - (level - defaultMin)/(defaultMax-defaultMin);
            brightness = (int)(normalizedLevel*sliderRange);
            brightnessSlider.setValue(brightness);
        }
        if (minSlider!=null && (sb==null || sb!=minSlider))
            minSlider.setValue(scaleDown(min));
        if (maxSlider!=null && (sb==null || sb!=maxSlider)) 
            maxSlider.setValue(scaleDown(max));
    }
    
    int scaleDown(double v) {
        if (v<defaultMin) v = defaultMin;
        if (v>defaultMax) v = defaultMax;
        return (int)((v-defaultMin)*255.0/(defaultMax-defaultMin));
    }
    

    void adjustMin(ImagePlus imp, ImageProcessor ip, double minvalue) {
        //IJ.log((int)min+" "+(int)max+" "+minvalue+" "+defaultMin+" "+defaultMax);
        min = defaultMin + minvalue*(defaultMax-defaultMin)/255.0;
        if (max>defaultMax)
            max = defaultMax;
        if (min>max)
            max = min;
        setMinAndMax(ip, min, max);
        if (min==max)
            setThreshold(ip);
        updateScrollBars(minSlider);
    }

    void adjustMax(ImagePlus imp, ImageProcessor ip, double maxvalue) {
        //IJ.log(min+" "+max+" "+maxvalue);
        max = defaultMin + maxvalue*(defaultMax-defaultMin)/255.0;
        if (min<0)
            min = 0;
        if (max<min)
            min = max;
        setMinAndMax(ip, min, max);
        if (min==max)
            setThreshold(ip);
        updateScrollBars(maxSlider);
    }

    void adjustBrightness(ImagePlus imp, ImageProcessor ip, double bvalue) {
        double center = defaultMin + (defaultMax-defaultMin)*((sliderRange-bvalue)/sliderRange);
        double width = max-min;
        min = center - width/2.0;
        max = center + width/2.0;
        setMinAndMax(ip, min, max);
        if (min==max)
            setThreshold(ip);
        updateScrollBars(brightnessSlider);
    }

    void adjustContrast(ImagePlus imp, ImageProcessor ip, int cvalue) {
        double slope;
        double center = min + (max-min)/2.0;
        double range = defaultMax-defaultMin;
        double mid = sliderRange/2;
        if (cvalue<=mid)
            slope = cvalue/mid;
        else
            slope = mid/(sliderRange-cvalue);
        if (slope>0.0) {
            min = center-(0.5*range)/slope;
            max = center+(0.5*range)/slope;
        }
        setMinAndMax(ip, min, max);
        updateScrollBars(contrastSlider);
    }

    void reset(ImagePlus imp, ImageProcessor ip) {
        if (RGBImage)
            ip.reset();
        if ((ip instanceof ShortProcessor) || (ip instanceof FloatProcessor)) {
            ip.resetMinAndMax();
            defaultMin = ip.getMin();
            defaultMax = ip.getMax();
            plot.defaultMin = defaultMin;
            plot.defaultMax = defaultMax;
        }
        min = defaultMin;
        max = defaultMax;
        setMinAndMax(ip, min, max);
        updateScrollBars(null);
        plotHistogram(imp);
        autoThreshold = 0;
    }

    void update(ImagePlus imp, ImageProcessor ip) {
        if (previousMin==0.0 && previousMax==0.0 || imp.getType()!=previousType)
            IJ.beep();
        else {
            min = previousMin;
            max = previousMax;
            setMinAndMax(ip, min, max);
            updateScrollBars(null);
            plotHistogram(imp);
        }
    }

    void plotHistogram(ImagePlus imp) {
        ImageStatistics stats;
        if (balance && (channels==4 || channels==2 || channels==1) && imp.getType()==ImagePlus.COLOR_RGB) {
            int w = imp.getWidth();
            int h = imp.getHeight();
            byte[] r = new byte[w*h];
            byte[] g = new byte[w*h];
            byte[] b = new byte[w*h];
            ((ColorProcessor)imp.getProcessor()).getRGB(r,g,b);
            byte[] pixels=null;
            if (channels==4)
                pixels = r;
            else if (channels==2)
                pixels = g;
            else if (channels==1)
                pixels = b;
            ImageProcessor ip = new ByteProcessor(w, h, pixels, null);
            stats = ImageStatistics.getStatistics(ip, 0, imp.getCalibration());
        } else
            stats = imp.getStatistics();
        plot.setHistogram(stats);
    }

    void apply(ImagePlus imp, ImageProcessor ip) {
        if (RGBImage)
            imp.unlock();
        if (!imp.lock())
            return;
        if (imp.getType()==ImagePlus.COLOR_RGB) {
            if (imp.getStackSize()>1)
                applyRGBStack(imp);
            else {
                ip.snapshot();
                reset(imp, ip);
                imp.changes = true;
            }
            imp.unlock();
            return;
        }
        if (imp.getType()!=ImagePlus.GRAY8) {
            IJ.beep();
            IJ.showStatus("Apply requires an 8-bit grayscale image or an RGB stack");
            imp.unlock();
            return;
        }
        int[] table = new int[256];
        int min = (int)ip.getMin();
        int max = (int)ip.getMax();
        for (int i=0; i<256; i++) {
            if (i<=min)
                table[i] = 0;
            else if (i>=max)
                table[i] = 255;
            else
                table[i] = (int)(((double)(i-min)/(max-min))*255);
        }
        if (imp.getStackSize()>1) {
            ImageStack stack = imp.getStack();
            YesNoCancelDialog d = new YesNoCancelDialog(this,
                "Entire Stack?", "Apply LUT to all "+stack.getSize()+" slices in the stack?");
            if (d.cancelPressed())
                {imp.unlock(); return;}
            if (d.yesPressed())
                new StackProcessor(stack, ip).applyTable(table);
            else
                ip.applyTable(table);
        } else
            ip.applyTable(table);
        reset(imp, ip);
        imp.changes = true;
        imp.unlock();
    }

    void applyRGBStack(ImagePlus imp) {
        int current = imp.getCurrentSlice();
        int n = imp.getStackSize();
        if (!IJ.showMessageWithCancel("Update Entire Stack?",
        "Apply brightness and contrast settings\n"+
        "to all "+n+" slices in the stack?\n \n"+
        "NOTE: There is no Undo for this operation."))
            return;
        for (int i=1; i<=n; i++) {
            if (i!=current) {
                imp.setSlice(i);
                ImageProcessor ip = imp.getProcessor();
                setMinAndMax(ip, min, max);
                IJ.showProgress((double)i/n);
            }
        }
        imp.setSlice(current);
        imp.changes = true;
    }

    void threshold(ImagePlus imp, ImageProcessor ip) {
        int threshold = (int)((defaultMax-defaultMin)/2.0);
        min = threshold;
        max = threshold;
        setMinAndMax(ip, min, max);
        setThreshold(ip);
        updateScrollBars(null);
    }

    void setThreshold(ImageProcessor ip) {
        if (!(ip instanceof ByteProcessor))
            return;
        if (((ByteProcessor)ip).isInvertedLut())
            ip.setThreshold(max, 255, ImageProcessor.NO_LUT_UPDATE);
        else
            ip.setThreshold(0, max, ImageProcessor.NO_LUT_UPDATE);
    }

    void autoAdjust(ImagePlus imp, ImageProcessor ip) {
        if (RGBImage)
            ip.reset();
        Calibration cal = imp.getCalibration();
        imp.setCalibration(null);
        ImageStatistics stats = imp.getStatistics(); // get uncalibrated stats
        imp.setCalibration(cal);
        int[] histogram = stats.histogram;
        if (autoThreshold<10)
            autoThreshold = AUTO_THRESHOLD;
        else
            autoThreshold /= 2;
        int threshold = stats.pixelCount/autoThreshold;
        int i = -1;
        boolean found = false;
        do {
            i++;
            found = histogram[i] > threshold;
        } while (!found && i<255);
        int hmin = i;
        i = 256;
        do {
            i--;
            found = histogram[i] > threshold;
        } while (!found && i>0);
        int hmax = i;
        if (hmax>=hmin) {
            imp.killRoi();
            min = stats.histMin+hmin*stats.binSize;
            max = stats.histMin+hmax*stats.binSize;
            if (min==max)
                {min=stats.min; max=stats.max;}
            setMinAndMax(ip, min, max);
        } else {
            reset(imp, ip);
            return;
        }
        updateScrollBars(null);
    }
    
    void setMinAndMax(ImagePlus imp, ImageProcessor ip) {
        min = ip.getMin();
        max = ip.getMax();
        Calibration cal = imp.getCalibration();
        int digits = (ip instanceof FloatProcessor)||cal.calibrated()?2:0;
        double minValue = cal.getCValue(min);
        double maxValue = cal.getCValue(max);
        GenericDialog gd = new GenericDialog("Set Min and Max");
        gd.addNumericField("Minimum Displayed Value: ", minValue, digits);
        gd.addNumericField("Maximum Displayed Value: ", maxValue, digits);
        gd.showDialog();
        if (gd.wasCanceled())
            return;
        minValue = gd.getNextNumber();
        maxValue = gd.getNextNumber();
        minValue = cal.getRawValue(minValue);
        maxValue = cal.getRawValue(maxValue);
        if (maxValue>=minValue) {
            min = minValue;
            max = maxValue;
            setMinAndMax(ip, min, max);
            updateScrollBars(null);
        }
    }

    void setWindowLevel(ImagePlus imp, ImageProcessor ip) {
        min = ip.getMin();
        max = ip.getMax();
        Calibration cal = imp.getCalibration();
        int digits = (ip instanceof FloatProcessor)||cal.calibrated()?2:0;
        double minValue = cal.getCValue(min);
        double maxValue = cal.getCValue(max);
        //IJ.log("setWindowLevel: "+min+" "+max);
        double windowValue = maxValue - minValue;
        double levelValue = minValue + windowValue/2.0;
        GenericDialog gd = new GenericDialog("Set W&L");
        gd.addNumericField("Window Center (Level): ", levelValue, digits);
        gd.addNumericField("Window Width: ", windowValue, digits);
        gd.showDialog();
        if (gd.wasCanceled())
            return;
        levelValue = gd.getNextNumber();
        windowValue = gd.getNextNumber();
        minValue = levelValue-(windowValue/2.0);
        maxValue = levelValue+(windowValue/2.0);
        minValue = cal.getRawValue(minValue);
        maxValue = cal.getRawValue(maxValue);
        if (maxValue>=minValue) {
            min = minValue;
            max = maxValue;
            setMinAndMax(ip, minValue, maxValue);
            updateScrollBars(null);
        }
    }

    static final int RESET=0, AUTO=1, SET=2, APPLY=3, THRESHOLD=4, MIN=5, MAX=6, 
        BRIGHTNESS=7, CONTRAST=8, UPDATE=9;

    // Separate thread that does the potentially time-consuming processing 
    public void run() {
		while (!done) {
			 synchronized(this) {
				 try {wait();}
				 catch(InterruptedException e) {}
			 }
			 doUpdate();
		 }
    }

    void doUpdate() {
        ImageProcessor ip;
        int action;
        int minvalue = minSliderValue;
        int maxvalue = maxSliderValue;
        int bvalue = brightnessValue;
        int cvalue = contrastValue;
        if (doReset) action = RESET;
        else if (doAutoAdjust) action = AUTO;
        else if (doSet) action = SET;
        else if (doApplyLut) action = APPLY;
        else if (doThreshold) action = THRESHOLD;
        else if (doUpdate) action = UPDATE;
        else if (minSliderValue>=0) action = MIN;
        else if (maxSliderValue>=0) action = MAX;
        else if (brightnessValue>=0) action = BRIGHTNESS;
        else if (contrastValue>=0) action = CONTRAST;
        else return;
        minSliderValue = maxSliderValue = brightnessValue = contrastValue = -1;
        doReset = doAutoAdjust = doSet = doApplyLut = doThreshold = doUpdate = false;
        //imp = WindowManager.getCurrentImage();
        if (imp==null) {
            IJ.beep();
            IJ.showStatus("No image");
            return;
        }
        if (action!=UPDATE)
            ip = setup(imp);
        else
            ip = imp.getProcessor();
        if (RGBImage && !imp.lock())
            {imp=null; return;}
        //IJ.write("setup: "+(imp==null?"null":imp.getTitle()));
        switch (action) {
            case RESET: reset(imp, ip); break;
            case AUTO: autoAdjust(imp, ip); break;
            case SET: if (windowLevel) setWindowLevel(imp, ip); else setMinAndMax(imp, ip); break;
            case APPLY: apply(imp, ip); break;
            case THRESHOLD: threshold(imp, ip); break;
            case UPDATE: update(imp, ip); break;
            case MIN: adjustMin(imp, ip, minvalue); break;
            case MAX: adjustMax(imp, ip, maxvalue); break;
            case BRIGHTNESS: adjustBrightness(imp, ip, bvalue); break;
            case CONTRAST: adjustContrast(imp, ip, cvalue); break;
        }
        updatePlot();
        updateLabels(imp, ip);
        imp.updateAndDraw();
        if (RGBImage)
            imp.unlock();
    }

    public void windowClosing(WindowEvent e) {
		if (e.getSource()==this) {
			close();
		}
    }

    /** Overrides close() in PlugInFrame. */
    public void close() {
	   done = true;
	   synchronized(this) {
		   notify();
	   }
	   thread= null;
	   adjuster=null;
	   setVisible(false);
	   dispose();
	   

    }
		public void windowOpened(WindowEvent e) {}
		public void windowClosed(WindowEvent e) { instance = false;}
		public void windowIconified(WindowEvent e) {}
		public void windowDeiconified(WindowEvent e) {}
		public void windowDeactivated(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {
        
		if (WindowManager.getCurrentWindow()!=null){
			imp = WindowManager.getCurrentWindow().getImagePlus(); 	
		} 
        setup();
    }

 
} // ContrastAdjuster class


class ContrastPlot extends Canvas implements MouseListener {
    
    static final int WIDTH = 128, HEIGHT=64;
    double defaultMin = 0;
    double defaultMax = 255;
    double min = 0;
    double max = 255;
    int[] histogram;
    int hmax;
    java.awt.Image os;
    Graphics osg;
    
    public ContrastPlot() {
        addMouseListener(this);
        setSize(WIDTH+1, HEIGHT+1);
    }

    /** Overrides Component getPreferredSize(). Added to work 
        around a bug in Java 1.4.1 on Mac OS X.*/
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH+1, HEIGHT+1);
    }

    void setHistogram(ImageStatistics stats) {
        histogram = stats.histogram;
        if (histogram.length!=256)
            {histogram=null; return;}
        for (int i=0; i<128; i++)
            histogram[i] = (histogram[2*i]+histogram[2*i+1])/2;
        int maxCount = 0;
        int mode = 0;
        for (int i=0; i<128; i++) {
            if (histogram[i]>maxCount) {
                maxCount = histogram[i];
                mode = i;
            }
        }
        int maxCount2 = 0;
        for (int i=0; i<128; i++) {
            if ((histogram[i]>maxCount2) && (i!=mode))
                maxCount2 = histogram[i];
        }
        hmax = stats.maxCount;
        if ((hmax>(maxCount2*2)) && (maxCount2!=0)) {
            hmax = (int)(maxCount2*1.5);
            histogram[mode] = hmax;
        }
        os = null;
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        int x1, y1, x2, y2;
        double scale = (double)WIDTH/(defaultMax-defaultMin);
        double slope = 0.0;
        if (max!=min)
            slope = HEIGHT/(max-min);
        if (min>=defaultMin) {
            x1 = (int)(scale*(min-defaultMin));
            y1 = HEIGHT;
        } else {
            x1 = 0;
            if (max>min)
                y1 = HEIGHT-(int)((defaultMin-min)*slope);
            else
                y1 = HEIGHT;
        }
        if (max<=defaultMax) {
            x2 = (int)(scale*(max-defaultMin));
            y2 = 0;
        } else {
            x2 = WIDTH;
            if (max>min)
                y2 = HEIGHT-(int)((defaultMax-min)*slope);
            else
                y2 = 0;
        }
        if (histogram!=null) {
            if (os==null) {
                os = createImage(WIDTH,HEIGHT);
                osg = os.getGraphics();
                osg.setColor(Color.white);
                osg.fillRect(0, 0, WIDTH, HEIGHT);
                osg.setColor(Color.gray);
                for (int i = 0; i < WIDTH; i++)
                    osg.drawLine(i, HEIGHT, i, HEIGHT - ((int)(HEIGHT * histogram[i])/hmax));
                osg.dispose();
            }
            g.drawImage(os, 0, 0, this);
        } else {
            g.setColor(Color.white);
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }
        g.setColor(Color.black);
        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x2, HEIGHT-5, x2, HEIGHT);
        g.drawRect(0, 0, WIDTH, HEIGHT);
     }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}

} // ContrastPlot class


class TrimmedLabel extends Label {
    int trim = IJ.isMacOSX() ?0:6;

    public TrimmedLabel(String title) {
        super(title);
    }

    public Dimension getMinimumSize() {
        return new Dimension(super.getMinimumSize().width, super.getMinimumSize().height-trim);
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

} // TrimmedLabel class


  
>>>>>>> 76aa07461566a5976980e6696204781271955163
