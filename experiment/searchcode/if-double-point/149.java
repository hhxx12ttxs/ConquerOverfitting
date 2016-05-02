<<<<<<< HEAD
/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javafx.scene.shape;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.Vec3d;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.input.PickResultChooser;
import com.sun.javafx.sg.PGCylinder;
import com.sun.javafx.sg.PGNode;
import com.sun.javafx.tk.Toolkit;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.input.PickResult;
import javafx.scene.transform.Rotate;

/**
 * The {@code Cylinder} class defines a 3 dimensional cylinder with the specified size.
 * A {@code Cylinder} is a 3D geometry primitive created with a given radius and height.
 * It is centered at the origin.
 *
 * @since JavaFX 8.0
 */
public class Cylinder extends Shape3D {

    static final int DEFAULT_DIVISIONS = 64;
    static final double DEFAULT_RADIUS = 1;
    static final double DEFAULT_HEIGHT = 2;
    
    private int divisions = DEFAULT_DIVISIONS;
    private TriangleMesh mesh;
    
    /**  
     * Creates a new instance of {@code Cylinder} of radius of 1.0 and height of 2.0.
     * Resolution defaults to 15 divisions along X and Z axis.
     */
    public Cylinder() {
        this(DEFAULT_RADIUS, DEFAULT_HEIGHT, DEFAULT_DIVISIONS);
    }

    /**
     * Creates a new instance of {@code Cylinder} of a given radius and height.
     * Resolution defaults to 15 divisions along X and Z axis.
     * 
     * @param radius Radius
     * @param height Height
     */
    public Cylinder (double radius, double height) {
        this(radius, height, DEFAULT_DIVISIONS);
    }

    /**
     * Creates a new instance of {@code Cylinder} of a given radius, height, and
     * divisions. Resolution defaults to 15 divisions along X and Z axis.
     *
     * Note that divisions should be at least 3. Any value less than that will be
     * clamped to 3.
     * 
     * @param radius Radius
     * @param height Height
     * @param divisions Divisions 
     */
    public Cylinder (double radius, double height, int divisions) {
        this.divisions = divisions < 3 ? 3 : divisions;
        setRadius(radius);
        setHeight(height);
    }
    
    /**
     * Defines the height or the Y dimension of the Cylinder.
     *
     * @defaultValue 2.0
     */
    private DoubleProperty height;

    public final void setHeight(double value) {
        heightProperty().set(value);
    }

    public final double getHeight() {
        return height == null ? 2 : height.get();
    }

    public final DoubleProperty heightProperty() {
        if (height == null) {
            height = new SimpleDoubleProperty(Cylinder.this, "height", DEFAULT_HEIGHT) {
                @Override
                public void invalidated() {
                    impl_markDirty(DirtyBits.MESH_GEOM);
                    manager.invalidateCylinderMesh(key);
                    key = 0;
                }
            };
        }
        return height;
    }

    /**
     * Defines the radius in the Z plane of the Cylinder.
     *
     * @defaultValue 1.0
     */
    private DoubleProperty radius;

    public final void setRadius(double value) {
        radiusProperty().set(value);
    }

    public final double getRadius() {
        return radius == null ? 1 : radius.get();
    }

    public final DoubleProperty radiusProperty() {
        if (radius == null) {
            radius = new SimpleDoubleProperty(Cylinder.this, "radius", DEFAULT_RADIUS) {
                @Override
                public void invalidated() {
                    impl_markDirty(DirtyBits.MESH_GEOM);
                    manager.invalidateCylinderMesh(key);
                    key = 0;
                }
            };
        }
        return radius;
    }

    /**
     * Retrieves the divisions attribute use to generate this cylinder.
     *
     * @return the divisions attribute.
     */
    public int getDivisions() {
        return divisions;
    }

    /**
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated   
    @Override
    public void impl_updatePG() {
        super.impl_updatePG();
        if (impl_isDirty(DirtyBits.MESH_GEOM)) {
            PGCylinder pgCylinder = (PGCylinder) impl_getPGNode();
            final float h = (float) getHeight();
            final float r = (float) getRadius();
            if (h < 0 || r < 0) {
                pgCylinder.updateMesh(null);
            } else {
                if (key == 0) {
                    key = generateKey(h, r, divisions);
                }
                mesh = manager.getCylinderMesh(h, r, divisions, key);
                mesh.impl_updatePG();
                pgCylinder.updateMesh(mesh.impl_getPGTriangleMesh());
            }
        }
    }
    
    /**
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    @Override
    protected PGNode impl_createPGNode() {
        return Toolkit.getToolkit().createPGCylinder();
    }

    /**
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    @Override
    public BaseBounds impl_computeGeomBounds(BaseBounds bounds, BaseTransform tx) {
        final float h = (float) getHeight();
        final float r = (float) getRadius();

        if (r < 0 || h < 0) {
            return bounds.makeEmpty();
        }
        
        final float hh = h * 0.5f;
        
        bounds = bounds.deriveWithNewBounds(-r, -hh, -r, r, hh, r);
        bounds = tx.transform(bounds, bounds);
        return bounds;
    }

    /**
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    @Override
    protected boolean impl_computeContains(double localX, double localY) {
        double w = getRadius();
        double hh = getHeight()*.5f;
        return -w <= localX && localX <= w && 
                -hh <= localY && localY <= hh;
    }

    /**
     * @treatAsPrivate implementation detail
     * @deprecated This is an internal API that is not intended for use and will be removed in the next version
     */
    @Deprecated
    @Override
    protected boolean impl_computeIntersects(PickRay pickRay, PickResultChooser pickResult) {

        final boolean exactPicking = divisions < DEFAULT_DIVISIONS && mesh != null;

        final double r = getRadius();
        final Vec3d dir = pickRay.getDirectionNoClone();
        final double dirX = dir.x;
        final double dirY = dir.y;
        final double dirZ = dir.z;
        final Vec3d origin = pickRay.getOriginNoClone();
        final double originX = origin.x;
        final double originY = origin.y;
        final double originZ = origin.z;
        final double h = getHeight();
        final double halfHeight = h / 2.0;
        final CullFace cullFace = getCullFace();

        // Check the open cylinder first

        // Coeficients of a quadratic equation desribing intersection with an infinite cylinder
        final double a = dirX * dirX + dirZ * dirZ;
        final double b = 2 * (dirX * originX + dirZ * originZ);
        final double c = originX * originX + originZ * originZ - r * r;

        final double discriminant = b * b - 4 * a * c;

        double t0, t1, t = Double.POSITIVE_INFINITY;
        final double minDistance = pickRay.getNearClip();
        final double maxDistance = pickRay.getFarClip();

        if (discriminant >= 0 && (dirX != 0.0 || dirZ != 0.0)) {
            // the line hits the infinite cylinder

            final double distSqrt = Math.sqrt(discriminant);
            final double q = (b < 0) ? (-b - distSqrt) / 2.0 : (-b + distSqrt) / 2.0;

            t0 = q / a;
            t1 = c / q;

            if (t0 > t1) {
                double temp = t0;
                t0 = t1;
                t1 = temp;
            }

            // let's see if the hit is between clipping planes and within the cylinder's height
            final double y0 = originY + t0 * dirY;
            if (t0 < minDistance || y0 < -halfHeight || y0 > halfHeight || cullFace == CullFace.FRONT) {
                final double y1 = originY + t1 * dirY;
                if (t1 >= minDistance && t1 <= maxDistance && y1 >= -halfHeight && y1 <= halfHeight) {
                    if (cullFace != CullFace.BACK || exactPicking) {
                        // t0 is outside or behind but t1 hits.

                        // We need to do the exact picking even if the back wall
                        // is culled because the front facing triangles may
                        // still be in front of us
                        t = t1;
                    }
                } // else no hit (but we need to check the caps)
            } else if (t0 <= maxDistance) {
                // t0 hits the height between clipping planes
                t = t0;
            } // else no hit (but we need to check the caps)
        }

        // Now check the caps
        
        // if we already know we are going to do the exact picking,
        // there is no need to check the caps
        
        boolean topCap = false, bottomCap = false;
        if (t == Double.POSITIVE_INFINITY || !exactPicking) {
            final double tBottom = (-halfHeight - originY) / dirY;
            final double tTop = (halfHeight - originY) / dirY;
            boolean isT0Bottom = false;

            if (tBottom < tTop) {
                t0 = tBottom;
                t1 = tTop;
                isT0Bottom = true;
            } else {
                t0 = tTop;
                t1 = tBottom;
            }

            if (t0 >= minDistance && t0 <= maxDistance && t0 < t && cullFace != CullFace.FRONT) {
                final double tX = originX + dirX * t0;
                final double tZ = originZ + dirZ * t0;
                if (tX * tX + tZ * tZ <= r * r) {
                    bottomCap = isT0Bottom; topCap = !isT0Bottom;
                    t = t0;
                }
            }

            if (t1 >= minDistance && t1 <= maxDistance && t1 < t && (cullFace != CullFace.BACK || exactPicking)) {
                final double tX = originX + dirX * t1;
                final double tZ = originZ + dirZ * t1;
                if (tX * tX + tZ * tZ <= r * r) {
                    topCap = isT0Bottom; bottomCap = !isT0Bottom;
                    t = t1;
                }
            }
        }

        if (Double.isInfinite(t) || Double.isNaN(t)) {
            // no hit
            return false;
        }

        if (exactPicking) {
            return mesh.impl_computeIntersects(pickRay, pickResult, this, cullFace, false);
        }

        if (pickResult != null && pickResult.isCloser(t)) {
            final Point3D point = PickResultChooser.computePoint(pickRay, t);

            Point2D txCoords;
            if (topCap) {
                txCoords = new Point2D(
                        0.5 + point.getX() / (2 * r),
                        0.5 + point.getZ() / (2 * r));
            } else if (bottomCap) {
                txCoords = new Point2D(
                        0.5 + point.getX() / (2 * r),
                        0.5 - point.getZ() / (2 * r));
            } else {
                final Point3D proj = new Point3D(point.getX(), 0, point.getZ());
                final Point3D cross = proj.crossProduct(Rotate.Z_AXIS);
                double angle = proj.angle(Rotate.Z_AXIS);
                if (cross.getY() > 0) {
                    angle = 360 - angle;
                }
                txCoords = new Point2D(1 - angle / 360, 0.5 + point.getY() / h);
            }

            pickResult.offer(this, t, PickResult.FACE_UNDEFINED, point, txCoords);
        }
        return true;
    }

    static TriangleMesh createMesh(int div, float h, float r) {

        // NOTE: still create mesh for degenerated cylinder
        final int nPonits = (div + 1) * 2 + 2;
        final int tcCount = (div + 1) * 4 + 1; // 2 cap tex
        final int faceCount = div * 4;

        float textureDelta = 1.f / 256;

        float dA = 1.f / div;
        h *= .5f;

        float points[] = new float[nPonits * 3];
        float tPoints[] = new float[tcCount * 2];
        int faces[] = new int[faceCount * 6];
        int smoothing[] = new int[faceCount];

        int pPos = 0, tPos = 0;

        for (int i = 0; i <= div; ++i) {
            double a = (i < div) ? dA * i * 2 * Math.PI : 0;

            points[pPos + 0] = (float) (Math.sin(a) * r);
            points[pPos + 2] = (float) (Math.cos(a) * r);
            points[pPos + 1] = h;
            tPoints[tPos + 0] = 1 - dA * i;
            tPoints[tPos + 1] = 1 - textureDelta;
            pPos += 3; tPos += 2;
        }

        for (int i = 0; i <= div; ++i) {
            double a = (i < div) ? dA * i * 2 * Math.PI : 0;
            points[pPos + 0] = (float) (Math.sin(a) * r);
            points[pPos + 2] = (float) (Math.cos(a) * r);
            points[pPos + 1] = -h;
            tPoints[tPos + 0] = 1 - dA * i;
            tPoints[tPos + 1] = textureDelta;
            pPos += 3; tPos += 2;
        }

        // add cap central points
        points[pPos + 0] = 0;
        points[pPos + 1] = h;
        points[pPos + 2] = 0;
        points[pPos + 3] = 0;
        points[pPos + 4] = -h;
        points[pPos + 5] = 0;
        pPos += 6;

        // add cap central points
        // bottom cap
        for (int i = 0; i <= div; ++i) {
            double a = (i < div) ? (dA * i * 2) * Math.PI: 0;
            tPoints[tPos + 0] = (float) (Math.sin(a) * 0.5f) + 0.5f;
            tPoints[tPos + 1] = (float) (Math.cos(a) * 0.5f) + 0.5f;
            tPos += 2;
        }

        // top cap
        for (int i = 0; i <= div; ++i) {
            double a = (i < div) ? (dA * i * 2) * Math.PI: 0;
            tPoints[tPos + 0] = 0.5f + (float) (Math.sin(a) * 0.5f);
            tPoints[tPos + 1] = 0.5f - (float) (Math.cos(a) * 0.5f);
            tPos += 2;
        }

        tPoints[tPos + 0] = .5f;
        tPoints[tPos + 1] = .5f;
        tPos += 2;

        int fIndex = 0;

        // build body faces
        for (int p0 = 0; p0 != div; ++p0) {
            int p1 = p0 + 1;
            int p2 = p0 + div + 1;
            int p3 = p1 + div + 1;

            // add p0, p1, p2
            faces[fIndex+0] = p0;
            faces[fIndex+1] = p0;
            faces[fIndex+2] = p2;
            faces[fIndex+3] = p2;
            faces[fIndex+4] = p1;
            faces[fIndex+5] = p1;
            fIndex += 6;

            // add p3, p2, p1
            // *faces++ = SmFace(p3,p1,p2, p3,p1,p2, 1);
            faces[fIndex+0] = p3;
            faces[fIndex+1] = p3;
            faces[fIndex+2] = p1;
            faces[fIndex+3] = p1;
            faces[fIndex+4] = p2;
            faces[fIndex+5] = p2;
            fIndex += 6;

        }
        // build cap faces
        int tStart = (div + 1) * 2, t1 = (div + 1) * 4, p1 = (div + 1) * 2;

        // bottom cap
        for (int p0 = 0; p0 != div; ++p0) {
            int p2 = p0 + 1, t0 = tStart + p0, t2 = t0 + 1;
            // add p0, p1, p2

            faces[fIndex+0] = p0;
            faces[fIndex+1] = t0;
            faces[fIndex+2] = p2;
            faces[fIndex+3] = t2;
            faces[fIndex+4] = p1;
            faces[fIndex+5] = t1;
            fIndex += 6;
        }

        p1 = (div + 1) * 2 + 1;
        tStart = (div + 1) * 3;

        // top cap
        for (int p0 = 0; p0 != div; ++p0) {
            int p2 = p0 + 1 + div + 1, t0 = tStart + p0, t2 = t0 + 1;
            //*faces++ = SmFace(p0+div+1,p1,p2, t0,t1,t2, 2);

            faces[fIndex+0] = p0 + div + 1;
            faces[fIndex+1] = t0;
            faces[fIndex+2] = p1;
            faces[fIndex+3] = t1;
            faces[fIndex+4] = p2;
            faces[fIndex+5] = t2;
            fIndex += 6;
        }

        for (int i = 0; i < div * 2; ++i) {
            smoothing[i] = 1;
        }
        for (int i = div * 2; i < div * 4; ++i) {
            smoothing[i] = 2;
        }

        TriangleMesh m = new TriangleMesh();
        m.getPoints().setAll(points);
        m.getTexCoords().setAll(tPoints);
        m.getFaces().setAll(faces);
        m.getFaceSmoothingGroups().setAll(smoothing);

        return m;
    }

    private static int generateKey(float h, float r, int div) {
        int hash = 7;
        hash = 47 * hash + Float.floatToIntBits(h);
        hash = 47 * hash + Float.floatToIntBits(r);
        hash = 47 * hash + div;
        return hash;
=======
package sounder.pig.points;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.BagFactory;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.apache.pig.impl.util.Utils;

/**
   Constructs a k-d tree from the passed in databag containing
   points. NOTE: This is intended as a proof-of-concept and is
   unlikely to be production worthy.
 */
public class KDTree extends EvalFunc<DataBag> {
    private static Comparator<KDPoint> comparators[];
    private static final Integer ID_FIELD = 0;
    private static final Integer IS_ROOT_FIELD = 1;
    private static final Integer AXIS_FIELD = 2;
    private static final Integer ABOVE_CHILD_FIELD = 3;
    private static final Integer BELOW_CHILD_FIELD = 4;
    private static final Integer POINT_FIELD = 5;
    
    public DataBag exec(Tuple input) throws IOException {
        if (input == null || input.size() < 1 || input.isNull(0)) { return null; }

        DataBag points = (DataBag)input.get(0);       // {(id, point:(x1,x2,...,xK))}
        KDPoint[] asPoints = toPoints(points);

        return generateTree(asPoints);        
    }

    /**
       Check if the input tuple can make a valid KDPoint object
     */
    private boolean isValidPoint(Tuple t) throws ExecException {
        if (t.isNull(0) || t.isNull(1)) { return false; }
        return true;
    }

    /**
       Construct an array of KDPoint objects from the passed in DataBag
       of tuples
     */
    private KDPoint[] toPoints(DataBag points) throws ExecException {
        KDPoint[] result = new KDPoint[((Long)points.size()).intValue()];
        int idx = 0;
        for (Tuple t : points) {
            if (isValidPoint(t)) {
                result[idx] = new KDPoint(t);
                idx++;
            }
        }
        return result;
    }

    /**
       Recursively generate a k-d tree from the passed in array of points
     */
    private DataBag generateTree(KDPoint[] points) throws ExecException {
        if (points.length == 0) { return null; }

        int maxD = points[0].getDimensionality();
        comparators = new Comparator[maxD];
        for (int i = 0; i < maxD; i++) {
            comparators[i] = new KDPointComparator(i);
        }
        KDPoint root = generate(0, maxD, points, 0, points.length-1);
        root.isRoot = true;
        return root.toBag();
    }

    private KDPoint generate(int d, int maxD, KDPoint[] points, int left, int right) throws ExecException {
        if (right < left) { return null; }
        if (right == left) {
            KDPoint returnPoint = points[left];
            if (returnPoint != null) { returnPoint.setAxis(d); }
            return returnPoint;
        }

        int m = (right-left)/2;
        // Yes, sort every time. Not super efficient
        Arrays.sort(points, left, right+1, comparators[d]);

        KDPoint medianPoint = points[left+m];
        medianPoint.setAxis(d);
        
        if (++d >= maxD) { d = 0; }
        
        medianPoint.setBelowChild(generate(d, maxD, points, left, left+m-1));
	medianPoint.setAboveChild(generate(d, maxD, points, left+m+1, right));
        return medianPoint;
    }

    /**
       Set the appropriate output schema so pig doesn't get confused
     */
    public Schema outputSchema(Schema input) {
        Schema schema = null;
        try {
            schema = Utils.getSchemaFromString("result:bag{t:tuple(id:chararray, is_root:int, axis:int, above_child:chararray, below_child:chararray, point:tuple(lng:double, lat:double))}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schema;
    }
    
    /**
       Simple representation of a multi-dimensional point
     */
    class KDPoint {
        
        final String pointId;
        final int dimensionality;
        public boolean isRoot;
        protected String aboveChildId;
        protected String belowChildId;
        protected KDPoint aboveChild; // Above child, right in 2-D case
        protected KDPoint belowChild; // Below child, left in 2-D case
        protected Integer axis; // Splitting axis for this node (0 or 1) in 2-D case
        double values[];

        /**
           Construct a KDPoint from the passed in tuple representation
         */
        public KDPoint(Tuple pointTuple) throws ExecException {
            this.pointId = (String)pointTuple.get(0);
            Tuple point = (Tuple)pointTuple.get(1);
            
            int d = this.dimensionality = point.size();
            values = new double[d];
            for (int i = 0; i < d; i++) {
                values[i] = (Double)point.get(i);
            }
        }

        public KDPoint getAboveChild() { return aboveChild; }
        public String getAboveChildId() { return aboveChildId; }
        public KDPoint getBelowChild() { return belowChild; }
        public String getBelowChildId() { return belowChildId; }
        public Integer getAxis() { return axis; }
        public String getPointId() { return pointId; }
        public int getDimensionality() { return dimensionality; }
        public double getCoordinate(int d) { return values[d]; }

        public void setAboveChild(KDPoint child) {
            this.aboveChild = child;
            if (child != null) { this.aboveChildId = child.getPointId(); }
        }
        
        public void setAboveChildId(String childId) { this.aboveChildId = childId; }
        
        public void setBelowChild(KDPoint child) {
            this.belowChild = child;
            if (child != null) { this.belowChildId = child.getPointId(); }
        }
        
        public void setBelowChildId(String childId) { this.belowChildId = childId; }
        
        public void setAxis(Integer axis) { this.axis = axis; }

        public Tuple toTuple() throws ExecException {
            TupleFactory tfact = TupleFactory.getInstance();
            Tuple result = tfact.newTuple(6);
            Tuple point = tfact.newTuple(dimensionality);

            for (int i = 0; i < dimensionality; i++) {
                point.set(i, values[i]);
            }
            
            result.set(0, pointId);
            result.set(1, (isRoot ? 1 : 0));
            result.set(2, axis);
            result.set(3, aboveChildId);
            result.set(4, belowChildId);
            result.set(5, point);
            return result;
        }

        public DataBag toBag() throws ExecException {
            DataBag result = BagFactory.getInstance().newDefaultBag();
            result.add(toTuple());
            if (aboveChild != null) {
                result.addAll(aboveChild.toBag());
            }

            if (belowChild != null) {
                result.addAll(belowChild.toBag());
            }
            return result;
        }
    }

    /**
       Simple comparator class for sorting KDPoints along a particular dimension
     */
    public class KDPointComparator implements Comparator<KDPoint> {
        public final int d;
        public static final double epsilon = 1E-9;
        
        public KDPointComparator (int d) {
            this.d = d;
	}

        public int compare(KDPoint p1, KDPoint p2) {
            double d1 = p1.getCoordinate(d);
            double d2 = p2.getCoordinate(d);
            if (lesser(d1, d2)) { return -1; }
            if (same(d1, d2)) { return 0; }		
            return +1;
	}

        public double value(double x) {
            if ((x >= 0) && (x <= epsilon)) { return 0.0; }
            
            if ((x < 0) && (-x <= epsilon)) { return 0.0; }
            
            return x;
	}

        public boolean lesser(double x, double y) { return value(x-y) < 0; }

        public boolean same (double d1, double d2) {
            if (Double.isNaN(d1)) { return Double.isNaN(d2); }
            
            if (d1 == d2) { return true; }
            
            if (Double.isInfinite(d1)) { return false; }
            
            return value (d1-d2) == 0;
	}
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }
}

