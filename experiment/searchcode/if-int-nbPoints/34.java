package plugins.adufour.activemeshes.mesh;

import icy.sequence.Sequence;
import icy.util.StringUtil;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;

import plugins.adufour.activemeshes.energy.Model;
import plugins.adufour.activemeshes.util.SequenceSampler;
import plugins.adufour.activemeshes.util.SlidingWindowConvergence;
import plugins.nchenouard.spot.Detection;
import vtk.vtkOBBTree;
import vtk.vtkPoints;

public class Mesh extends Detection implements Iterable<Point3d>
{
    final ArrayList<Future<Point2d>> meanUpdateTasks = new ArrayList<Future<Point2d>>();
    
    public final ArrayList<Face>     faces;
    public final ArrayList<Vertex>   vertices;
    public final MeshTopology        topology;
    
    private double                   maxDistanceToCenter;
    
    private final VTKMesh            vtkMesh;
    private final ArrayList<Model>   models;
    private SlidingWindowConvergence convergence;
    
    /**
     * Creates a new mesh with the specified vertices and faces.
     * 
     * @param vertices
     *            the vertex array of the mesh
     * @param faces
     *            the face array of the mesh
     * @param resolution
     *            the default distance between two mesh vertices
     */
    public Mesh(ArrayList<Vertex> vertices, ArrayList<Face> faces, double resolution, boolean useVTK)
    {
        this.vertices = vertices;
        this.faces = faces;
        this.topology = new MeshTopology(this, resolution);
        this.vtkMesh = useVTK ? new VTKMesh(this) : null;
        this.models = new ArrayList<Model>(5);
        
        if (vertices.size() > 0) updateMassCenter();
    }
    
    public Mesh(int nbPoints, int nbFaces, double resolution, boolean useVTK)
    {
        this(new ArrayList<Vertex>(nbPoints), new ArrayList<Face>(nbFaces), resolution, useVTK);
    }
    
    public void addModel(Model model)
    {
        models.add(model);
    }
    
    public VTKMesh getVTKMesh()
    {
        return vtkMesh;
    }
    
    public void setConvergence(SlidingWindowConvergence convergence)
    {
        this.convergence = convergence;
    }
    
    public SlidingWindowConvergence getConvergence()
    {
        return convergence;
    }
    
    /**
     * Returns a copy of current contour for use on the next sequence frame.
     * 
     * @return a copy of current Contour.
     */
    public Mesh copyContour()
    {
        ArrayList<Vertex> newVertices = new ArrayList<Vertex>(this.vertices.size());
        ArrayList<Face> newFaces = new ArrayList<Face>(this.faces.size());
        
        for (Vertex v : this.vertices)
            newVertices.add((v == null) ? null : new Vertex(v.position, v.neighbors));
        
        for (Face f : this.faces)
            newFaces.add(new Face(new Integer(f.v1), new Integer(f.v2), new Integer(f.v3)));
        
        return new Mesh(newVertices, newFaces, this.topology.getMeshResolution(), (vtkMesh != null));
    }
    
    public void exportToOFF(PrintStream ps)
    {
        ArrayList<Point3d> pts = new ArrayList<Point3d>();
        ArrayList<Face> newFaces = new ArrayList<Face>(faces.size());
        
        for (Face f : faces)
            newFaces.add(new Face(f.v1.intValue(), f.v2.intValue(), f.v3.intValue()));
        
        int lastNonNullIndex = 0;
        int nullCount = 0;
        
        reordering: for (int i = 0; i < vertices.size(); i++)
        {
            nullCount = 0;
            
            while (vertices.get(i) == null)
            {
                nullCount++;
                i++;
                if (i == vertices.size()) break reordering;
            }
            
            pts.add(vertices.get(i).position);
            lastNonNullIndex++;
            
            if (nullCount != 0)
            {
                // some null vertices were found starting at index (i-nullCount)
                // subtract their number from all faces pointing to an index greater than the
                // previous known valid vertex
                for (Face f : newFaces)
                {
                    if (f.v1 >= lastNonNullIndex) f.v1 -= nullCount;
                    if (f.v2 >= lastNonNullIndex) f.v2 -= nullCount;
                    if (f.v3 >= lastNonNullIndex) f.v3 -= nullCount;
                }
            }
            // lastNonNullIndex = i;
        }
        
        // OFF File format is written as follows:
        
        // line 1: "OFF"
        ps.println("OFF");
        
        // line 2: numVertices numFaces numEdges (numEdges is not important)
        ps.println(pts.size() + " " + newFaces.size() + " 0");
        
        // one line per vertex "x y z"
        for (Point3d v : pts)
            ps.println(v.x + " " + v.y + " " + v.z);
        
        // one line per face "numVertices v1 v2 ... vn"
        for (Face f : newFaces)
            ps.println("3 " + f.v1 + " " + f.v2 + " " + f.v3);
    }
    
    public void exportToVTK(PrintStream ps)
    {
        // re-order points and faces to remove blank spaces
        
        ArrayList<Point3d> pts = new ArrayList<Point3d>();
        ArrayList<Face> newFaces = new ArrayList<Face>(faces.size());
        
        for (Face f : faces)
            newFaces.add(new Face(f.v1.intValue(), f.v2.intValue(), f.v3.intValue()));
        
        int lastNonNullIndex = 0;
        int nullCount = 0;
        
        reordering: for (int i = 0; i < vertices.size(); i++)
        {
            nullCount = 0;
            
            while (vertices.get(i) == null)
            {
                nullCount++;
                i++;
                if (i == vertices.size()) break reordering;
            }
            
            pts.add(vertices.get(i).position);
            lastNonNullIndex++;
            
            if (nullCount != 0)
            {
                // some null vertices were found starting at index (i-nullCount)
                // subtract their number from all faces pointing to an index greater than the
                // previous known valid vertex
                for (Face f : newFaces)
                {
                    if (f.v1 >= lastNonNullIndex) f.v1 -= nullCount;
                    if (f.v2 >= lastNonNullIndex) f.v2 -= nullCount;
                    if (f.v3 >= lastNonNullIndex) f.v3 -= nullCount;
                }
            }
            // lastNonNullIndex = i;
        }
        
        // VTK File format is written as follows:
        
        // line 1: "# vtk DataFile Version x.x"
        ps.println("# vtk DataFile Version 3.0");
        
        // line 2: header information (256 chars max)
        ps.println("A great-looking cell");
        
        // line 3: data format (one of ASCII or BINARY)
        ps.println("ASCII");
        
        // line 4: dataset structure "DATASET [OPTIONS]"
        ps.println("DATASET POLYDATA");
        
        // line 4: dataset type "POINTS number type"
        ps.println("POINTS " + pts.size() + " double");
        
        // one line per vertex "x y z"
        for (Point3d v : pts)
            ps.println(v.x + " " + v.y + " " + v.z);
        
        // neighborhood information "POLYGONS faces nbItems"
        ps.println("POLYGONS " + faces.size() + " " + 4 * faces.size());
        
        // one line per face "numVertices v1 v2 ... vn"
        for (Face f : newFaces)
            ps.println("3 " + f.v1 + " " + f.v2 + " " + f.v3);
    }
    
    public Point3d getMassCenter()
    {
        return new Point3d(x, y, z);
    }
    
    /**
     * Returns the coordinates of the Contour's "boundary box". The boundary box is defined as the
     * smallest rectangle (2D or 3D) area which entirely contains the Contour.
     * 
     * @param min
     *            a point that will represent the top left-hand corner of the box
     * @param max
     *            a point that will represent the bottom right-hand corner of the box
     */
    public void getBoundingBox(Point3d min, Point3d max)
    {
        if (getDimension(0) == 0) throw new MeshException(this, "Empty mesh");
        
        min.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        max.set(0, 0, 0);
        
        for (Point3d p : this)
        {
            if (p == null) continue;
            if (p.x < min.x) min.x = p.x;
            if (p.x > max.x) max.x = p.x;
            if (p.y < min.y) min.y = p.y;
            if (p.y > max.y) max.y = p.y;
            if (p.z < min.z) min.z = p.z;
            if (p.z > max.z) max.z = p.z;
        }
    }
    
    /**
     * Returns the minimum distance between all contour points and the specified point
     * 
     * @param point
     * @return
     */
    public double getMinDistanceTo(Point3d point)
    {
        return getMinDistanceTo(point, null);
    }
    
    /**
     * Returns the minimum distance between all contour points and the specified point.
     * 
     * @param point
     * @param closestPoint
     *            a Point3d object that will be filled with the closest point
     * @return
     */
    public double getMinDistanceTo(Point3d point, Point3d closestPoint)
    {
        double dist = Double.MAX_VALUE;
        
        for (Point3d p : this)
        {
            if (p == null) continue;
            
            double d = p.distance(point);
            
            if (d < dist)
            {
                dist = d;
                if (closestPoint != null) closestPoint.set(p);
            }
        }
        
        return dist;
    }
    
    /**
     * Returns the maximum distance between all contour points and the specified point
     * 
     * @param point
     * @return
     */
    public double getMaxDistanceTo(Point3d point)
    {
        double dist = 0;
        
        for (Vertex v : vertices)
        {
            if (v == null) continue;
            if (v.distanceToCenter > dist) dist = v.distanceToCenter;
        }
        
        return dist;
    }
    
    /**
     * Returns the maximum distance between all mesh points and its mass center
     * 
     * @param point
     * @return
     */
    public double getMaxDistanceToCenter()
    {
        return maxDistanceToCenter;
    }
    
    public double getResolution()
    {
        return topology.getMeshResolution();
    }
    
    public double getCurvature(Point3d pt)
    {
        for (Vertex v : vertices)
        {
            if (v == null) continue;
            
            if (!v.position.equals(pt)) continue;
            
            Vector3d sum = new Vector3d();
            Vector3d diff = new Vector3d();
            for (Integer n : v.neighbors)
            {
                Point3d neighbor = vertices.get(n).position;
                diff.sub(neighbor, pt);
                sum.add(diff);
            }
            sum.scale(1.0 / v.neighbors.size());
            
            return sum.length() * Math.signum(sum.dot(v.normal));
        }
        
        return 0;
    }
    
    public double getDimension(int order)
    {
        switch (order)
        {
            case 0: // number of points
            {
                int nbPts = 0;
                
                for (Vertex v : vertices)
                    if (v != null) nbPts++;
                
                return nbPts;
            }
            case 1:
            case 2: // surface and volume are both computed as algebraic sums
                // over each face
            {
                double surface = 0, volume = 0;
                
                Vector3d v12 = new Vector3d();
                Vector3d v13 = new Vector3d();
                Vector3d cross = new Vector3d();
                
                Point3d v1 = new Point3d();
                Point3d v2 = new Point3d();
                Point3d v3 = new Point3d();
                
                for (Face f : faces)
                {
                    v1.set(vertices.get(f.v1).position);
                    v2.set(vertices.get(f.v2).position);
                    v3.set(vertices.get(f.v3).position);
                    
                    v12.sub(v2, v1);
                    v13.sub(v3, v1);
                    
                    cross.cross(v12, v13);
                    
                    double surf = cross.length() * 0.5f;
                    
                    if (order == 1)
                    {
                        surface += surf;
                    }
                    else
                    {
                        cross.normalize();
                        volume += surf * cross.x * (v1.x + v2.x + v3.x);
                    }
                    
                }
                
                double value = (order == 1) ? surface : volume;
                return value;
            }
            default:
                throw new UnsupportedOperationException("Dimension " + order + " not implemented");
        }
    }
    
    public Point3d getPoint(int index)
    {
        Vertex v = vertices.get(index);
        
        return (v == null) ? null : v.position;
    }
    
    /**
     * 
     * @param v
     * @return 0 if the vertex is outside this mesh, otherwise a positive value measuring the
     *         penetration depth (in metrics unit)
     */
    public double isInside(Vertex v)
    {
        return isInside_IntersectionBased(v);
        // return isInside_VTKBased(pointThatShouldBeOutside, center);
    }
    
    /**
     * 
     * @param p
     * @return 0 if the vertex is outside this mesh, otherwise a positive value measuring the
     *         penetration depth (in metrics unit)
     */
    public double isInside(Point3d p)
    {
        Vertex v = new Vertex(p);
        
        // fake the vertex normal to point away from the mesh center
        v.normal.sub(getMassCenter());
        v.normal.set(p);
        v.normal.normalize();
        
        return isInside(v);
    }
    
    public double isInside_VTKBased(Point3d pointThatShouldBeOutside, Point3d center)
    {
        // FIXME this doesn't work (ugly segfault, thx vtk !)
        
        vtkOBBTree obb = new vtkOBBTree();
        obb.SetDataSet(vtkMesh.polyData);
        
        // Trace a ray from the point going outwards from my mass center
        Vector3d ray = new Vector3d(pointThatShouldBeOutside);
        ray.sub(center);
        ray.normalize();
        
        vtkPoints points = new vtkPoints();
        
        obb.IntersectWithLine(new double[] { center.x, center.y, center.z }, new double[] { pointThatShouldBeOutside.x, pointThatShouldBeOutside.y, pointThatShouldBeOutside.z }, points, null);
        
        if (points.GetNumberOfPoints() % 2 == 1) return topology.getMeshResolution() * 0.5;
        
        return 0.0;
    }
    
    private final Vector3d edge1 = new Vector3d(), edge2 = new Vector3d();
    private final Vector3d vp    = new Vector3d(), vt = new Vector3d(), vq = new Vector3d();
    private final Vector3d ray = new Vector3d();
    
    public double isInside_IntersectionBased(Vertex vTest)
    {
        // FIXME this is one of the hottest spot in the entire plug-in
        
        double epsilon = 1.0e-12;
        
        // the given point belongs to another mesh
        // 1) trace a ray from that point outwards (away from my center)
        // 2) count the intersections with my boundary
        // 3) if the number is odd, the point is inside
        // 4) if the point is inside, measure the penetration
        
        //ray.negate(vTest.normal); // was supper buggy upon close contacts !
        ray.sub(vTest.position, getMassCenter());
        
        double penetration = Double.MAX_VALUE;
        int crossCount = 0;
        
        double det, u, v, distance;
        
        for (Face f : faces)
        {
            Point3d v1 = vertices.get(f.v1).position; // FIXME Null pointer ???
            Point3d v2 = vertices.get(f.v2).position; // FIXME Null pointer ???
            Point3d v3 = vertices.get(f.v3).position; // FIXME Null pointer ???
            
            edge1.sub(v3, v1);
            edge2.sub(v2, v1);
            
            vp.cross(ray, edge2);
            
             det = edge1.dot(vp);
            
            if (det < epsilon) continue;
            
            vt.sub(vTest.position, v1);
            
             u = vt.dot(vp);
            
            if (u < 0 || u > det) continue;
            
            vq.cross(vt, edge1);
            
             v = ray.dot(vq);
            
            if (v < 0 || u + v > det) continue;
            
             distance = edge2.dot(vq) / det;
            
            if (distance < 0) continue;
            
            if (penetration > distance) penetration = distance;
            
            crossCount++;
        }
        
        return crossCount % 2 == 1 ? penetration : 0;
    }
    
    /**
     * 
     * @param seqNormalized
     *            double-precision input normalized in [0-1]
     * @param t
     * @param c
     * @param mask
     *            the byte mask used to store all meshes
     * @return
     * @throws MeshException
     *             if the mesh is flattening and should be removed
     */
    public double computeIntensity_old(SequenceSampler sampler, short[][] mask_Z_XY, short id, Point3d resolution) throws MeshException
    {
        int scanLine = sampler.dimensions.x;
        
        double inCpt = 0, inSum = 0;
        
        Point3d boxMin = new Point3d(), boxMax = new Point3d();
        this.getBoundingBox(boxMin, boxMax);
        
        int minX = Math.max(0, (int) Math.floor(boxMin.x / resolution.x) - 1);
        int minY = Math.max(0, (int) Math.floor(boxMin.y / resolution.y) - 1);
        int minZ = Math.max(0, (int) Math.floor(boxMin.z / resolution.z) - 1);
        
        int maxY = Math.min(sampler.dimensions.y - 1, (int) Math.ceil(boxMax.y / resolution.y) + 1);
        int maxZ = Math.min(sampler.dimensions.z - 1, (int) Math.ceil(boxMax.z / resolution.z) + 1);
        
        double epsilon = 1.0e-12;
        
        Vector3d edge1 = new Vector3d(), edge2 = new Vector3d();
        Vector3d vp = new Vector3d(), vt = new Vector3d(), vq = new Vector3d();
        
        Vector3d direction = new Vector3d(1, 0, 0);
        Point3d origin = new Point3d(minX * resolution.x, minY * resolution.y, minZ * resolution.z);
        
        ArrayList<Integer> crossDistancesList = new ArrayList<Integer>(4);
        int[] crossDistances;
        
        for (int k = minZ; k < maxZ; k++, origin.z += resolution.z)
        {
            short[] maskSlice = (mask_Z_XY != null) ? mask_Z_XY[k] : null;
            double[] dataSlice = sampler.getData()[k];
            
            for (int j = minY; j < maxY; j++, origin.y += resolution.y)
            {
                int lineOffset = j * scanLine;
                
                crossDistancesList.clear();
                int crosses = 0;
                
                for (Face f : faces)
                {
                    Point3d v1 = vertices.get(f.v1).position;
                    Point3d v2 = vertices.get(f.v2).position;
                    Point3d v3 = vertices.get(f.v3).position;
                    
                    if (origin.y < v1.y && origin.y < v2.y && origin.y < v3.y) continue;
                    if (origin.z < v1.z && origin.z < v2.z && origin.z < v3.z) continue;
                    if (origin.y > v1.y && origin.y > v2.y && origin.y > v3.y) continue;
                    if (origin.z > v1.z && origin.z > v2.z && origin.z > v3.z) continue;
                    
                    edge1.sub(v2, v1);
                    edge2.sub(v3, v1);
                    
                    vp.cross(direction, edge2);
                    
                    double det = edge1.dot(vp);
                    
                    if (Math.abs(det) < epsilon) continue;
                    
                    double inv_det = 1.0 / det;
                    
                    vt.sub(origin, v1);
                    double u = vt.dot(vp) * inv_det;
                    if (u < 0 || u > 1.0) continue;
                    
                    vq.cross(vt, edge1);
                    double v = direction.dot(vq) * inv_det;
                    if (v < 0.0 || u + v > 1.0) continue;
                    
                    double distance = edge2.dot(vq) * inv_det;
                    
                    Integer distPx = minX + (int) Math.round(distance / resolution.x);
                    
                    if (distPx < 0) continue;
                    
                    if (!crossDistancesList.contains(distPx))
                    {
                        crossDistancesList.add(distPx);
                        crosses++;
                    }
                    else
                    {
                        // if distPx already exists, then the mesh "slices" the same voxel twice
                        // (round-off error thus gives the same distance for the 2 crosses)
                        // => don't add the (same) cross distance
                        // instead, remove the existing one to discard all crosses for that voxel
                        crossDistancesList.remove(distPx);
                        crosses--;
                    }
                }
                
                // ignore the following cases:
                // - crosses = 0 --> the ray does not cross the mesh
                // - crosses = 1 --> the ray touches the mesh on an edge (only the first distance
                // was recorded)
                if (crosses < 2) continue;
                
                if (crosses == 2)
                {
                    // optimization for the most frequent case: a ray crosses
                    // twice (in & out)
                    
                    int cross1 = crossDistancesList.get(0);
                    int cross2 = crossDistancesList.get(1);
                    
                    // the ray touches the mesh on an edge
                    
                    if (cross1 < cross2) // crosses are ordered
                    {
                        for (int i = cross1; i < cross2; i++)
                        {
                            inCpt++;
                            int offset = i + lineOffset;
                            inSum += dataSlice[offset];
                            if (maskSlice != null) maskSlice[offset] = id;// (byte) 0xff;
                        }
                    }
                    else
                    // invert the order
                    {
                        for (int i = cross2; i < cross1; i++)
                        {
                            inCpt++;
                            int offset = i + lineOffset;
                            inSum += dataSlice[offset];
                            if (maskSlice != null) maskSlice[offset] = id;// (byte) 0xff;
                        }
                    }
                }
                else
                {
                    crossDistances = new int[crosses];
                    
                    for (int item = 0; item < crosses; item++)
                        crossDistances[item] = crossDistancesList.get(item).intValue();
                    
                    java.util.Arrays.sort(crossDistances);
                    
                    int nbSegments = crossDistances.length / 2;
                    int crossOffset, start, stop;
                    
                    for (int segment = 0; segment < nbSegments; segment++)
                    {
                        crossOffset = segment << 1;
                        start = crossDistances[crossOffset];
                        stop = crossDistances[crossOffset + 1];
                        
                        for (int i = start; i < stop; i++)
                        {
                            inCpt++;
                            int offset = i + lineOffset;
                            inSum += dataSlice[offset];
                            if (maskSlice != null) maskSlice[offset] = id;// (byte) 0xff;
                        }
                    }
                }
            }
            
            origin.y = minY * resolution.y;
        }
        
        // long tac = System.nanoTime();
        // System.out.println(faces.size() + " " + (tac - tic) / 1000000);
        
        if (inCpt == 0)
        {
            // no voxel inside the mesh => mesh is becoming extremely flat
            throw new MeshException(this, "Flat mesh (probably on the volume edge)");
        }
        
        return inSum / inCpt;
    }
    
    /**
     * 
     * @param multithreadservice2
     * @param seqNormalized
     *            double-precision input normalized in [0-1]
     * @param t
     * @param c
     * @param mask
     *            the byte mask used to store all meshes
     * @return
     * @throws MeshException
     *             if the mesh is flattening and should be removed
     */
    public double computeIntensity(final SequenceSampler sampler, final short[][] mask_Z_XY, final short id, final Point3d resolution, ExecutorService multiThreadService) throws MeshException
    {
        double inSum = 0, inCpt = 0;
        
        final int scanLine = sampler.dimensions.x;
        
        Point3d boxMin = new Point3d(), boxMax = new Point3d();
        this.getBoundingBox(boxMin, boxMax);
        
        final int minX = Math.max(0, (int) Math.floor(boxMin.x / resolution.x) - 1);
        final int minY = Math.max(0, (int) Math.floor(boxMin.y / resolution.y) - 1);
        final int minZ = Math.max(0, (int) Math.floor(boxMin.z / resolution.z) - 1);
        
        final int maxY = Math.min(sampler.dimensions.y - 1, (int) Math.ceil(boxMax.y / resolution.y) + 1);
        final int maxZ = Math.min(sampler.dimensions.z - 1, (int) Math.ceil(boxMax.z / resolution.z) + 1);
        
        final double epsilon = 1.0e-12;
        
        final Vector3d direction = new Vector3d(1, 0, 0);
        
        meanUpdateTasks.ensureCapacity(maxZ - minZ + 1);
        
        for (int k = minZ; k <= maxZ; k++) // '<' changed to '<=' from single-thread version
        {
            final short[] maskSlice = (mask_Z_XY != null) ? mask_Z_XY[k] : null;
            final double[] dataSlice = sampler.getData()[k];
            
            final int slice = k;
            
            meanUpdateTasks.add(multiThreadService.submit(new Callable<Point2d>()
            {
                @Override
                public Point2d call() throws Exception
                {
                    Vector3d edge1 = new Vector3d(), edge2 = new Vector3d();
                    Vector3d vp = new Vector3d(), vt = new Vector3d(), vq = new Vector3d();
                    
                    ArrayList<Integer> crossDistancesList = new ArrayList<Integer>(4);
                    
                    double localInSum = 0;
                    int localInCpt = 0;
                    
                    Point3d origin = new Point3d(minX * resolution.x, minY * resolution.y, slice * resolution.z);
                    
                    for (int j = minY; j < maxY; j++, origin.y += resolution.y)
                    {
                        int lineOffset = j * scanLine;
                        
                        crossDistancesList.clear();
                        int crosses = 0;
                        
                        for (Face f : faces)
                        {
                            Point3d v1 = vertices.get(f.v1).position;
                            Point3d v2 = vertices.get(f.v2).position;
                            Point3d v3 = vertices.get(f.v3).position;
                            
                            if (origin.y < v1.y && origin.y < v2.y && origin.y < v3.y) continue;
                            if (origin.z < v1.z && origin.z < v2.z && origin.z < v3.z) continue;
                            if (origin.y > v1.y && origin.y > v2.y && origin.y > v3.y) continue;
                            if (origin.z > v1.z && origin.z > v2.z && origin.z > v3.z) continue;
                            
                            edge1.sub(v2, v1);
                            edge2.sub(v3, v1);
                            
                            vp.cross(direction, edge2);
                            
                            double det = edge1.dot(vp);
                            
                            if (Math.abs(det) < epsilon) continue;
                            
                            double inv_det = 1.0 / det;
                            
                            vt.sub(origin, v1);
                            double u = vt.dot(vp) * inv_det;
                            if (u < 0 || u > 1.0) continue;
                            
                            vq.cross(vt, edge1);
                            double v = direction.dot(vq) * inv_det;
                            if (v < 0.0 || u + v > 1.0) continue;
                            
                            double distance = edge2.dot(vq) * inv_det;
                            
                            Integer distPx = minX + (int) Math.round(distance / resolution.x);
                            
                            if (distPx < 0) continue;
                            
                            if (!crossDistancesList.contains(distPx))
                            {
                                crossDistancesList.add(distPx);
                                crosses++;
                            }
                            else
                            {
                                // if distPx already exists, then the mesh "slices" the same voxel
                                // twice
                                // (round-off error thus gives the same distance for the 2 crosses)
                                // => don't add the (same) cross distance
                                // instead, remove the existing one to discard all crosses for that
                                // voxel
                                crossDistancesList.remove(distPx);
                                crosses--;
                            }
                        }
                        
                        // ignore the following cases:
                        // - crosses = 0 --> the ray does not cross the mesh
                        // - crosses = 1 --> the ray touches the mesh on an edge (only the first
                        // distance
                        // was recorded)
                        if (crosses < 2) continue;
                        
                        if (crosses == 2)
                        {
                            // optimization for the most frequent case: a ray crosses
                            // twice (in & out)
                            
                            int cross1 = crossDistancesList.get(0);
                            int cross2 = crossDistancesList.get(1);
                            
                            // the ray touches the mesh on an edge
                            
                            if (cross1 < cross2) // crosses are ordered
                            {
                                for (int i = cross1; i < cross2; i++)
                                {
                                    localInCpt++;
                                    int offset = i + lineOffset;
                                    localInSum += dataSlice[offset];
                                    if (maskSlice != null) maskSlice[offset] = id;// (byte) 0xff;
                                }
                            }
                            else
                            // invert the order
                            {
                                for (int i = cross2; i < cross1; i++)
                                {
                                    localInCpt++;
                                    int offset = i + lineOffset;
                                    localInSum += dataSlice[offset];
                                    if (maskSlice != null) maskSlice[offset] = id;// (byte) 0xff;
                                }
                            }
                        }
                        else
                        {
                            int[] crossDistances = new int[crosses];
                            
                            for (int item = 0; item < crosses; item++)
                                crossDistances[item] = crossDistancesList.get(item).intValue();
                            
                            java.util.Arrays.sort(crossDistances);
                            
                            int nbSegments = crossDistances.length / 2;
                            int crossOffset, start, stop;
                            
                            for (int segment = 0; segment < nbSegments; segment++)
                            {
                                crossOffset = segment << 1;
                                start = crossDistances[crossOffset];
                                stop = crossDistances[crossOffset + 1];
                                
                                for (int i = start; i < stop; i++)
                                {
                                    localInCpt++;
                                    int offset = i + lineOffset;
                                    localInSum += dataSlice[offset];
                                    if (maskSlice != null) maskSlice[offset] = id;// (byte) 0xff;
                                }
                            }
                        }
                    }
                    
                    return (localInCpt == 0) ? new Point2d(0, 0) : new Point2d(localInSum, localInCpt);
                }
            }));
        }
        
        try
        {
            for (Future<Point2d> localMean : meanUpdateTasks)// multiThreadService.invokeAll(tasks))
            {
                Point2d p = localMean.get();
                inSum += p.x;
                inCpt += p.y;
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        
        meanUpdateTasks.clear();
        
        if (inCpt == 0)
        {
            // no voxel inside the mesh => mesh is becoming extremely flat
            throw new MeshException(this, "Flat mesh (probably on the volume edge)");
        }
        return inSum / inCpt;
    }
    
    public Iterator<Point3d> iterator()
    {
        return new Iterator<Point3d>()
        {
            
            private int index = 0;
            
            public boolean hasNext()
            {
                return index < vertices.size();
            }
            
            public Point3d next()
            {
                Vertex v = vertices.get(index);
                index++;
                return (v == null) ? null : v.position;
            }
            
            public void remove()
            {
            }
            
        };
    }
    
    /**
     * 
     * @param unsignedShortSequence
     *            an empty output Sequence with appropriate spatial resolution
     * @param id
     *            the value to store in the output image
     */
    public void rasterize_OLD(Sequence unsignedShortSequence, short id)
    {
        Point3i dimensions = new Point3i(unsignedShortSequence.getSizeX(), unsignedShortSequence.getSizeY(), unsignedShortSequence.getSizeZ());
        Point3d resolution = new Point3d(unsignedShortSequence.getPixelSizeX(), unsignedShortSequence.getPixelSizeY(), unsignedShortSequence.getPixelSizeZ());
        
        short[][] mask_Z_XY = unsignedShortSequence.getDataXYZAsShort(t, 0);
        
        int scanLine = dimensions.x;
        
        Point3d boxMin = new Point3d(), boxMax = new Point3d();
        this.getBoundingBox(boxMin, boxMax);
        
        int minX = Math.max(0, (int) Math.floor(boxMin.x / resolution.x));
        int minY = Math.max(0, (int) Math.floor(boxMin.y / resolution.y));
        int minZ = Math.max(0, (int) Math.floor(boxMin.z / resolution.z));
        
        int maxY = Math.min(dimensions.y - 1, (int) Math.ceil(boxMax.y / resolution.y));
        int maxZ = Math.min(dimensions.z - 1, (int) Math.ceil(boxMax.z / resolution.z));
        double epsilon = 1.0e-12;
        
        Vector3d edge1 = new Vector3d(), edge2 = new Vector3d();
        Vector3d vp = new Vector3d(), vt = new Vector3d(), vq = new Vector3d();
        
        Vector3d direction = new Vector3d(1, 0, 0);
        Point3d origin = new Point3d(minX * resolution.x, minY * resolution.y, minZ * resolution.z);
        
        ArrayList<Integer> crossDistancesList = new ArrayList<Integer>(4);
        int[] crossDistances;
        
        for (int k = minZ; k < maxZ; k++, origin.z += resolution.z)
        {
            short[] maskSlice = (mask_Z_XY != null) ? mask_Z_XY[k] : null;
            
            for (int j = minY; j < maxY; j++, origin.y += resolution.y)
            {
                int lineOffset = j * scanLine;
                
                crossDistancesList.clear();
                int crosses = 0;
                
                for (Face f : faces)
                {
                    Point3d v1 = vertices.get(f.v1).position;
                    Point3d v2 = vertices.get(f.v2).position;
                    Point3d v3 = vertices.get(f.v3).position;
                    
                    if (origin.y < v1.y && origin.y < v2.y && origin.y < v3.y) continue;
                    if (origin.z < v1.z && origin.z < v2.z && origin.z < v3.z) continue;
                    if (origin.y > v1.y && origin.y > v2.y && origin.y > v3.y) continue;
                    if (origin.z > v1.z && origin.z > v2.z && origin.z > v3.z) continue;
                    
                    edge1.sub(v2, v1);
                    edge2.sub(v3, v1);
                    
                    vp.cross(direction, edge2);
                    
                    double det = edge1.dot(vp);
                    
                    if (Math.abs(det) < epsilon) continue;
                    
                    double inv_det = 1.0 / det;
                    
                    vt.sub(origin, v1);
                    double u = vt.dot(vp) * inv_det;
                    if (u < 0 || u > 1.0) continue;
                    
                    vq.cross(vt, edge1);
                    double v = direction.dot(vq) * inv_det;
                    
                    if (v < 0.0 || u + v > 1.0) continue;
                    
                    double distance = edge2.dot(vq) * inv_det;
                    
                    Integer distPx = minX + (int) Math.round(distance / resolution.x);
                    
                    if (distPx < 0) continue;
                    
                    if (!crossDistancesList.contains(distPx))
                    {
                        crossDistancesList.add(distPx);
                        crosses++;
                    }
                    else
                    {
                        // if distPx already exists, then the mesh "slices" the same voxel twice
                        // (round-off error thus gives the same distance for the 2 crosses)
                        // => don't add the (same) cross distance
                        // instead, remove the existing one to discard all crosses for that voxel
                        crossDistancesList.remove(distPx);
                        crosses--;
                    }
                }
                
                // ignore the following cases:
                // - crosses = 0 --> the ray does not cross the mesh
                // - crosses = 1 --> the ray touches the mesh on an edge (only the first distance
                // was recorded)
                if (crosses < 2) continue;
                
                if (crosses == 2)
                {
                    // optimization for the most frequent case: a ray crosses
                    // twice (in & out)
                    
                    int cross1 = crossDistancesList.get(0);
                    int cross2 = crossDistancesList.get(1);
                    
                    // the ray touches the mesh on an edge
                    
                    if (cross1 < cross2) // crosses are ordered
                    {
                        for (int i = cross1; i < cross2; i++)
                            if (maskSlice != null) maskSlice[lineOffset + i] = id;// (byte) 0xff;
                    }
                    else
                    // invert the order
                    {
                        for (int i = cross2; i < cross1; i++)
                            if (maskSlice != null) maskSlice[lineOffset + i] = id;// (byte) 0xff;
                    }
                }
                else
                {
                    crossDistances = new int[crosses];
                    
                    for (int item = 0; item < crosses; item++)
                        crossDistances[item] = crossDistancesList.get(item).intValue();
                    
                    java.util.Arrays.sort(crossDistances);
                    
                    int nbSegments = crossDistances.length / 2;
                    int crossOffset, start, stop;
                    
                    for (int segment = 0; segment < nbSegments; segment++)
                    {
                        crossOffset = segment << 1;
                        start = crossDistances[crossOffset];
                        stop = crossDistances[crossOffset + 1];
                        
                        for (int i = start; i < stop; i++)
                        {
                            if (maskSlice != null) maskSlice[lineOffset + i] = id;// (byte) 0xff;
                        }
                    }
                }
            }
            
            origin.y = minY * resolution.y;
        }
    }
    
    /**
     * 
     * @param unsignedShortSequence
     *            an output Sequence filled with empty images and with appropriate spatial
     *            resolution
     * @param id
     *            the value to store in the output image
     * @param multiThreadService
     *            the service to dispatch threads with
     */
    public void rasterize(Sequence unsignedShortSequence, final short id, ExecutorService multiThreadService)
    {
        final Point3i dimensions = new Point3i(unsignedShortSequence.getSizeX(), unsignedShortSequence.getSizeY(), unsignedShortSequence.getSizeZ());
        final Point3d resolution = new Point3d(unsignedShortSequence.getPixelSizeX(), unsignedShortSequence.getPixelSizeY(), unsignedShortSequence.getPixelSizeZ());
        
        if (dimensions.x == 0 || dimensions.y == 0 || dimensions.z == 0)
            throw new IllegalArgumentException("Cannot raterize a mesh into an empty sequence. Fill the sequence with empty images first.");
        
        Point3d boxMin = new Point3d(), boxMax = new Point3d();
        this.getBoundingBox(boxMin, boxMax);
        
        final int scanLine = dimensions.x;
        
        final int minX = Math.max(0, (int) Math.floor(boxMin.x / resolution.x) - 1);
        final int minY = Math.max(0, (int) Math.floor(boxMin.y / resolution.y) - 1);
        final int minZ = Math.max(0, (int) Math.floor(boxMin.z / resolution.z) - 1);
        
        final int maxY = Math.min(dimensions.y - 1, (int) Math.ceil(boxMax.y / resolution.y));
        final int maxZ = Math.min(dimensions.z - 1, (int) Math.ceil(boxMax.z / resolution.z));
        
        if (maxZ < minZ)
        {
            String message = "Cannot rasterize sequence\n";
            message += "Make sure the following values seem correct (and report this error if not):\n";
            message += "Mesh bounding box: " + boxMin.x + " x " + boxMin.y + " x " + boxMin.z + "\n";
            message += "Image resolution: " + StringUtil.toString(resolution.x, 3) + " x " + StringUtil.toString(resolution.y, 3) + " x " + StringUtil.toString(resolution.z, 3) + "\n";
            throw new IllegalArgumentException(message);
        }
        
        final double epsilon = 1.0e-12;
        
        final Vector3d direction = new Vector3d(1, 0, 0);
        
        short[][] mask_Z_XY = unsignedShortSequence.getDataXYZAsShort(t, 0);
        
        ArrayList<Future<?>> tasks = new ArrayList<Future<?>>(maxZ - minZ + 1);
        
        for (int k = minZ; k <= maxZ; k++) // '<' changed to '<=' from single-thread version
        {
            final short[] maskSlice = (mask_Z_XY != null) ? mask_Z_XY[k] : null;
            
            final int slice = k;
            
            tasks.add(multiThreadService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    Vector3d edge1 = new Vector3d(), edge2 = new Vector3d();
                    Vector3d vp = new Vector3d(), vt = new Vector3d(), vq = new Vector3d();
                    
                    ArrayList<Integer> crossDistancesList = new ArrayList<Integer>(4);
                    
                    Point3d origin = new Point3d(minX * resolution.x, minY * resolution.y, slice * resolution.z);
                    
                    for (int j = minY; j < maxY; j++, origin.y += resolution.y)
                    {
                        int lineOffset = j * scanLine;
                        
                        crossDistancesList.clear();
                        int crosses = 0;
                        
                        for (Face f : faces)
                        {
                            Point3d v1 = vertices.get(f.v1).position;
                            Point3d v2 = vertices.get(f.v2).position;
                            Point3d v3 = vertices.get(f.v3).position;
                            
                            if (origin.y < v1.y && origin.y < v2.y && origin.y < v3.y) continue;
                            if (origin.z < v1.z && origin.z < v2.z && origin.z < v3.z) continue;
                            if (origin.y > v1.y && origin.y > v2.y && origin.y > v3.y) continue;
                            if (origin.z > v1.z && origin.z > v2.z && origin.z > v3.z) continue;
                            
                            edge1.sub(v2, v1);
                            edge2.sub(v3, v1);
                            
                            vp.cross(direction, edge2);
                            
                            double det = edge1.dot(vp);
                            
                            if (Math.abs(det) < epsilon) continue;
                            
                            double inv_det = 1.0 / det;
                            
                            vt.sub(origin, v1);
                            double u = vt.dot(vp) * inv_det;
                            if (u < 0 || u > 1.0) continue;
                            
                            vq.cross(vt, edge1);
                            double v = direction.dot(vq) * inv_det;
                            if (v < 0.0 || u + v > 1.0) continue;
                            
                            double distance = edge2.dot(vq) * inv_det;
                            
                            Integer distPx = minX + (int) Math.round(distance / resolution.x);
                            
                            if (distPx < 0) continue;
                            
                            if (!crossDistancesList.contains(distPx))
                            {
                                crossDistancesList.add(distPx);
                                crosses++;
                            }
                            else
                            {
                                // if distPx already exists, then the mesh "slices" the same voxel
                                // twice
                                // (round-off error thus gives the same distance for the 2 crosses)
                                // => don't add the (same) cross distance
                                // instead, remove the existing one to discard all crosses for that
                                // voxel
                                crossDistancesList.remove(distPx);
                                crosses--;
                            }
                        }
                        
                        // ignore the following cases:
                        // - crosses = 0 --> the ray does not cross the mesh
                        // - crosses = 1 --> the ray touches the mesh on an edge (only the first
                        // distance
                        // was recorded)
                        if (crosses < 2) continue;
                        
                        if (crosses == 2)
                        {
                            // optimization for the most frequent case: a ray crosses
                            // twice (in & out)
                            
                            int cross1 = crossDistancesList.get(0);
                            int cross2 = crossDistancesList.get(1);
                            
                            // the ray touches the mesh on an edge
                            
                            if (cross1 < cross2) // crosses are ordered
                            {
                                for (int i = cross1; i < cross2; i++)
                                    if (maskSlice != null) maskSlice[i + lineOffset] = id;
                            }
                            else
                            // invert the order
                            {
                                for (int i = cross2; i < cross1; i++)
                                    if (maskSlice != null) maskSlice[i + lineOffset] = id;
                            }
                        }
                        else
                        {
                            int[] crossDistances = new int[crosses];
                            
                            for (int item = 0; item < crosses; item++)
                                crossDistances[item] = crossDistancesList.get(item).intValue();
                            
                            java.util.Arrays.sort(crossDistances);
                            
                            int nbSegments = crossDistances.length / 2;
                            int crossOffset, start, stop;
                            
                            for (int segment = 0; segment < nbSegments; segment++)
                            {
                                crossOffset = segment << 1;
                                start = crossDistances[crossOffset];
                                stop = crossDistances[crossOffset + 1];
                                
                                for (int i = start; i < stop; i++)
                                    if (maskSlice != null) maskSlice[i + lineOffset] = id;
                            }
                        }
                    }
                }
            }));
        }
        
        try
        {
            for (Future<?> task : tasks)
                task.get();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
    }
    
    public void computeForces()
    {
        for (final Model model : models)
            model.computeForces();
    }
    
    public void updateMassCenter()
    {
        Point3d center = new Point3d();
        maxDistanceToCenter = 0;
        
        double nbVertices = 0;
        
        for (Vertex v : vertices)
            if (v != null)
            {
                nbVertices++;
                center.add(v.position);
            }
        
        if (nbVertices == 0) return;
        
        center.scale(1.0 / nbVertices);
        setX(center.x);
        setY(center.y);
        setZ(center.z);
        
        for (Vertex v : vertices)
            if (v != null)
            {
                v.distanceToCenter = v.position.distance(center);
                if (v.distanceToCenter > maxDistanceToCenter) maxDistanceToCenter = v.distanceToCenter;
            }
    }
    
    public void updateNormals()
    {
        Vector3d v31 = new Vector3d();
        Vector3d v12 = new Vector3d();
        Vector3d v23 = new Vector3d();
        
        for (Face f : faces)
        {
            // Accumulate face normals in each vertex
            
            Vertex v1 = vertices.get(f.v1);
            Vertex v2 = vertices.get(f.v2);
            Vertex v3 = vertices.get(f.v3);
            
            v31.sub(v1.position, v3.position);
            v12.sub(v2.position, v1.position);
            v23.sub(v3.position, v2.position);
            
            // normal at v1 = [v1 v2] ^ [v1 v3] = [v3 v1] ^ [v1 v2]
            v1.normal.x += v31.y * v12.z - v31.z * v12.y;
            v1.normal.y += v31.z * v12.x - v31.x * v12.z;
            v1.normal.z += v31.x * v12.y - v31.y * v12.x;
            
            // normal at v2 = [v2 v3] ^ [v2 v1] = [v1 v2] ^ [v2 v3]
            v2.normal.x += v12.y * v23.z - v12.z * v23.y;
            v2.normal.y += v12.z * v23.x - v12.x * v23.z;
            v2.normal.z += v12.x * v23.y - v12.y * v23.x;
            
            // normal at v3 = [v3 v1] ^ [v3 v2] = [v2 v3] ^ [v3 v1]
            v3.normal.x += v23.y * v31.z - v23.z * v31.y;
            v3.normal.y += v23.z * v31.x - v23.x * v31.z;
            v3.normal.z += v23.x * v31.y - v23.y * v31.x;
        }
        
        // Normalize the accumulated normals
        for (Vertex v : vertices)
            if (v != null) v.normal.normalize();
    }
    
    /**
     * Updates the VTK structure representing this mesh. This method does nothing if the VTK
     * structure is null
     */
    public void updateVTK()
    {
        if (vtkMesh != null) vtkMesh.update();
    }
    
    public void clean()
    {
        if (vtkMesh != null) vtkMesh.clean();
        for (Model def : models)
            def.removeMesh(this);
        convergence.clear();
    }
}

