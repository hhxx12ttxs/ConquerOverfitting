/*
 * Created on 12-sep-2006 by azabala
 *
 */
package com.vividsolutions.jts.geomgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geomgraph.index.SegmentIntersector;
import com.vividsolutions.jts.geomgraph.index.SimpleMCSweepLineIntersector;
import com.vividsolutions.jts.geomgraph.index.SnapSimpleMCSweepLineIntersector;
import com.vividsolutions.jts.util.Assert;

/**
 */
public class SnappingGeometryGraph extends GeometryGraph {

	// Properties of PlanarGraph that we want to overwrite to
	// use snapping
	public static final CGAlgorithms cga = new CGAlgorithms();

	protected SnappingNodeMap nodes;


	// overwrite to catch them when returned by Snapping map
	protected Collection boundaryNodes;

	protected int argIndex;

	private boolean useBoundaryDeterminationRule = false;

	private boolean hasTooFewPoints = false;

	private Coordinate invalidPoint;

	private Map lineEdgeMap = new HashMap(); 
	
	private Geometry parentGeometry;
	
	double snapTolerance;

	public SnappingGeometryGraph(NodeFactory nodeFact, double tolerance,
			int argIndex, Geometry parentGeometry) {
		//le pasamos al constructor padre GeometryCollection vacia para que
		//no replique la construccion del grafo
		super(argIndex, new GeometryCollection(new Geometry[0], parentGeometry.getFactory()));
		nodes = new SnappingNodeMap(nodeFact, tolerance);
		this.argIndex = argIndex;
		this.parentGeometry = parentGeometry;
		this.snapTolerance = tolerance;
		add(parentGeometry);
	}
	
	
	public Geometry getGeometry(){
		return this.parentGeometry;
	}
	
	
	public void dumpNodes(){
		this.nodes.dump();
	}

	public SnappingGeometryGraph(double tolerance, int argIndex, Geometry parent) {
		super(argIndex, new GeometryCollection(new Geometry[0], parent.getFactory()));
		nodes = new SnappingNodeMap(new NodeFactory(), tolerance);
		this.argIndex = argIndex;
		this.snapTolerance = tolerance;
		this.parentGeometry = parent;
		add(parent);
	}

	public Collection getBoundaryNodes() {
		if (boundaryNodes == null)
			boundaryNodes = nodes.getBoundaryNodes(argIndex);
		return boundaryNodes;
	}

	public Coordinate[] getBoundaryPoints() {
		Collection coll = getBoundaryNodes();
		Coordinate[] pts = new Coordinate[coll.size()];
		int i = 0;
		for (Iterator it = coll.iterator(); it.hasNext();) {
			Node node = (Node) it.next();
			pts[i++] = (Coordinate) node.getCoordinate().clone();
		}
		return pts;
	}

	public Iterator getNodeIterator() {
		return nodes.iterator();
	}

	public Collection getNodes() {
		return nodes.values();
	}

	public Node addNode(Node node) {
		return nodes.addNode(node);
	}

	public Node addNode(Coordinate coord) {
		return nodes.addNode(coord);
	}

	/**
	 * @return the node if found; null otherwise
	 */
	public Node find(Coordinate coord) {
		return nodes.find(coord);
	}
	
	public boolean isBoundaryNode(int geomIndex, Coordinate coord) {
		Node node = nodes.find(coord);
		if (node == null)
			return false;
		Label label = node.getLabel();
		if (label != null && label.getLocation(geomIndex) == Location.BOUNDARY)
			return true;
		return false;
	}

	

	public void add(EdgeEnd e) {
		nodes.add(e);
		edgeEndList.add(e);
	}
	
	

	/**
	 * Link the DirectedEdges at the nodes of the graph. This allows clients to
	 * link only a subset of nodes in the graph, for efficiency (because they
	 * know that only a subset is of interest).
	 */
	public void linkResultDirectedEdges() {
		for (Iterator nodeit = nodes.iterator(); nodeit.hasNext();) {
			Node node = (Node) nodeit.next();
			((DirectedEdgeStar) node.getEdges()).linkResultDirectedEdges();
		}
	}

	/**
	 * Link the DirectedEdges at the nodes of the graph. This allows clients to
	 * link only a subset of nodes in the graph, for efficiency (because they
	 * know that only a subset is of interest).
	 */
	public void linkAllDirectedEdges() {
		for (Iterator nodeit = nodes.iterator(); nodeit.hasNext();) {
			Node node = (Node) nodeit.next();
			((DirectedEdgeStar) node.getEdges()).linkAllDirectedEdges();
		}
	}

	/**
	 * Returns the EdgeEnd which has edge e as its base edge (MD 18 Feb 2002 -
	 * this should return a pair of edges)
	 * 
	 * @return the edge, if found <code>null</code> if the edge was not found
	 */
	public EdgeEnd findEdgeEnd(Edge e) {
		for (Iterator i = getEdgeEnds().iterator(); i.hasNext();) {
			EdgeEnd ee = (EdgeEnd) i.next();
			if (ee.getEdge() == e)
				return ee;
		}
		return null;
	}

	/**
	 * Returns the edge whose first two coordinates are p0 and p1
	 * 
	 * @return the edge, if found <code>null</code> if the edge was not found
	 */
	public Edge findEdge(Coordinate p0, Coordinate p1) {
		for (int i = 0; i < edges.size(); i++) {
			Edge e = (Edge) edges.get(i);
			Coordinate[] eCoord = e.getCoordinates();
			if (p0.equals(eCoord[0]) && p1.equals(eCoord[1]))
				return e;
		}
		return null;
	}

	/**
	 * Returns the edge which starts at p0 and whose first segment is parallel
	 * to p1
	 * 
	 * @return the edge, if found <code>null</code> if the edge was not found
	 */
	public Edge findEdgeInSameDirection(Coordinate p0, Coordinate p1) {
		for (int i = 0; i < edges.size(); i++) {
			Edge e = (Edge) edges.get(i);

			Coordinate[] eCoord = e.getCoordinates();
			if (matchInSameDirection(p0, p1, eCoord[0], eCoord[1]))
				return e;

			if (matchInSameDirection(p0, p1, eCoord[eCoord.length - 1],
					eCoord[eCoord.length - 2]))
				return e;
		}
		return null;
	}

	/**
	 * The coordinate pairs match if they define line segments lying in the same
	 * direction. E.g. the segments are parallel and in the same quadrant (as
	 * opposed to parallel and opposite!).
	 */
	private boolean matchInSameDirection(Coordinate p0, Coordinate p1,
			Coordinate ep0, Coordinate ep1) {
		if (!p0.equals(ep0))
			return false;

		if (CGAlgorithms.computeOrientation(p0, p1, ep1) == CGAlgorithms.COLLINEAR
				&& Quadrant.quadrant(p0, p1) == Quadrant.quadrant(ep0, ep1))
			return true;
		return false;
	}

	
	private void add(Geometry g)
	  {
	    if (g.isEmpty()) return;

	    // check if this Geometry should obey the Boundary Determination Rule
	    // all collections except MultiPolygons obey the rule
	    if (g instanceof GeometryCollection
	        && ! (g instanceof MultiPolygon))
	            useBoundaryDeterminationRule = true;

	    if (g instanceof Polygon)                 addPolygon((Polygon) g);
	                        // LineString also handles LinearRings
	    else if (g instanceof LineString)         addLineString((LineString) g);
	    else if (g instanceof Point)              addPoint((Point) g);
	    else if (g instanceof MultiPoint)         addCollection((MultiPoint) g);
	    else if (g instanceof MultiLineString)    addCollection((MultiLineString) g);
	    else if (g instanceof MultiPolygon)       addCollection((MultiPolygon) g);
	    else if (g instanceof GeometryCollection) addCollection((GeometryCollection) g);
	    else  throw new UnsupportedOperationException(g.getClass().getName());
	  }

	  private void addCollection(GeometryCollection gc)
	  {
	    for (int i = 0; i < gc.getNumGeometries(); i++) {
	      Geometry g = gc.getGeometryN(i);
	      add(g);
	    }
	  }
	  /**
	   * Add a Point to the graph.
	   */
	  private void addPoint(Point p)
	  {
	    Coordinate coord = p.getCoordinate();
	    insertPoint(argIndex, coord, Location.INTERIOR);
	  }
	  /**
	   * The left and right topological location arguments assume that the ring is oriented CW.
	   * If the ring is in the opposite orientation,
	   * the left and right locations must be interchanged.
	   */
	  private void addPolygonRing(LinearRing lr, int cwLeft, int cwRight)
	  {
	    Coordinate[] coord = CoordinateArrays.removeRepeatedPoints(lr.getCoordinates());

	    if (coord.length < 4) {
	      hasTooFewPoints = true;
	      invalidPoint = coord[0];
	      return;
	    }

	    int left  = cwLeft;
	    int right = cwRight;
	    if (cga.isCCW(coord)) {
	      left = cwRight;
	      right = cwLeft;
	    }
	    SnappingEdge e = new SnappingEdge(coord,
	                        new Label(argIndex, Location.BOUNDARY, left, right), this.snapTolerance);
	    lineEdgeMap.put(lr, e);

	    insertEdge(e);
	    // insert the endpoint as a node, to mark that it is on the boundary
	    insertPoint(argIndex, coord[0], Location.BOUNDARY);
	  }

	  private void addPolygon(Polygon p)
	  {
	    addPolygonRing(
	            (LinearRing) p.getExteriorRing(),
	            Location.EXTERIOR,
	            Location.INTERIOR);

	    for (int i = 0; i < p.getNumInteriorRing(); i++) {
	      // Holes are topologically labelled opposite to the shell, since
	      // the interior of the polygon lies on their opposite side
	      // (on the left, if the hole is oriented CW)
	      addPolygonRing(
	            (LinearRing) p.getInteriorRingN(i),
	            Location.INTERIOR,
	            Location.EXTERIOR);
	    }
	  }

	  private void addLineString(LineString line)
	  {
	    Coordinate[] coord = CoordinateArrays.removeRepeatedPoints(line.getCoordinates());

	    if (coord.length < 2) {
	      hasTooFewPoints = true;
	      invalidPoint = coord[0];
	      return;
	    }

	    // add the edge for the LineString
	    // line edges do not have locations for their left and right sides
	    SnappingEdge e = new SnappingEdge(coord, new Label(argIndex, Location.INTERIOR) , this.snapTolerance);
	    lineEdgeMap.put(line, e);
	    insertEdge(e);
	    /**
	     * Add the boundary points of the LineString, if any.
	     * Even if the LineString is closed, add both points as if they were endpoints.
	     * This allows for the case that the node already exists and is a boundary point.
	     */
	    Assert.isTrue(coord.length >= 2, "found LineString with single point");
	    insertBoundaryPoint(argIndex, coord[0]);
	    insertBoundaryPoint(argIndex, coord[coord.length - 1]);

	  }

	  /**
	   * Add an Edge computed externally.  The label on the Edge is assumed
	   * to be correct.
	   */
	  public void addEdge(Edge e)
	  {
	    insertEdge(e);
	    Coordinate[] coord = e.getCoordinates();
	    // insert the endpoint as a node, to mark that it is on the boundary
	    insertPoint(argIndex, coord[0], Location.BOUNDARY);
	    insertPoint(argIndex, coord[coord.length - 1], Location.BOUNDARY);
	  }

	  /**
	   * Add a point computed externally.  The point is assumed to be a
	   * Point Geometry part, which has a location of INTERIOR.
	   */
	  public void addPoint(Coordinate pt)
	  {
	    insertPoint(argIndex, pt, Location.INTERIOR);
	  }
	  
	  
	  /**
	   * Compute self-nodes, taking advantage of the Geometry type to
	   * minimize the number of intersection tests.  (E.g. rings are
	   * not tested for self-intersection, since they are assumed to be valid).
	   * @param li the LineIntersector to use
	   * @param computeRingSelfNodes if <false>, intersection checks are optimized to not test rings for self-intersection
	   * @return the SegmentIntersector used, containing information about the intersections found
	   */
	  public SegmentIntersector computeSelfNodes(LineIntersector li, boolean computeRingSelfNodes)
	  {
	    SegmentIntersector si = new SegmentIntersector(li, true, false);
	    SnapSimpleMCSweepLineIntersector esi = new SnapSimpleMCSweepLineIntersector();
	    // optimized test for Polygons and Rings
	    if (! computeRingSelfNodes
	        && (parentGeometry instanceof LinearRing
	        || parentGeometry instanceof Polygon
	        || parentGeometry instanceof MultiPolygon)) {
	      esi.computeIntersections(edges, si, false);
	    }
	    else {
	      esi.computeIntersections(edges, si, true);
	    }
	    addSelfIntersectionNodes(argIndex);
	    return si;
	  }


	  
	  public SegmentIntersector computeEdgeIntersections(
	    GeometryGraph g,
	    LineIntersector li,
	    boolean includeProper)
	  {
	    SegmentIntersector siSnap = new SegmentIntersector(li, includeProper, true);
	    siSnap.setBoundaryNodes(this.getBoundaryNodes(), g.getBoundaryNodes());

	    SnapSimpleMCSweepLineIntersector esiSnap = new SnapSimpleMCSweepLineIntersector();
	    esiSnap.computeIntersections(edges, g.edges, siSnap);
	    return siSnap;
		  
//		  return super.computeEdgeIntersections(g, li, includeProper);
	  }

	 
	 

	  private void addSelfIntersectionNodes(int argIndex)
	  {
	    for (Iterator i = edges.iterator(); i.hasNext(); ) {
	      Edge e = (Edge) i.next();
	      int eLoc = e.getLabel().getLocation(argIndex);
	      for (Iterator eiIt = e.eiList.iterator(); eiIt.hasNext(); ) {
	        EdgeIntersection ei = (EdgeIntersection) eiIt.next();
	        addSelfIntersectionNode(argIndex, ei.coord, eLoc);
	      }
	    }
	  }
	  /**
	   * Add a node for a self-intersection.
	   * If the node is a potential boundary node (e.g. came from an edge which
	   * is a boundary) then insert it as a potential boundary node.
	   * Otherwise, just add it as a regular node.
	   */
	  private void addSelfIntersectionNode(int argIndex, Coordinate coord, int loc)
	  {
	    // if this node is already a boundary node, don't change it
	    if (isBoundaryNode(argIndex, coord)) return;
	    if (loc == Location.BOUNDARY && useBoundaryDeterminationRule)
	        insertBoundaryPoint(argIndex, coord);
	    else
	      insertPoint(argIndex, coord, loc);
	  }

	  private void insertPoint(int argIndex, Coordinate coord, int onLocation)
	  {
	    Node n = nodes.addNode(coord);
	    Label lbl = n.getLabel();
	    if (lbl == null) {
	      n.label = new Label(argIndex, onLocation);
	    }
	    else
	      lbl.setLocation(argIndex, onLocation);
	  }
	  
	  /**
	   * Adds points using the mod-2 rule of SFS.  This is used to add the boundary
	   * points of dim-1 geometries (Curves/MultiCurves).  According to the SFS,
	   * an endpoint of a Curve is on the boundary
	   * iff if it is in the boundaries of an odd number of Geometries
	   */
	  private void insertBoundaryPoint(int argIndex, Coordinate coord)
	  {
	    Node n = nodes.addNode(coord);
	    Label lbl = n.getLabel();
	    // the new point to insert is on a boundary
	    int boundaryCount = 1;
	    // determine the current location for the point (if any)
	    int loc = Location.NONE;
	    if (lbl != null) loc = lbl.getLocation(argIndex, Position.ON);
	    if (loc == Location.BOUNDARY) boundaryCount++;

	    // determine the boundary status of the point according to the Boundary Determination Rule
	    int newLoc = determineBoundary(boundaryCount);
	    lbl.setLocation(argIndex, newLoc);
	  }
	  
	  
	  public void computeSplitEdges(List edgelist)
	  {
	    for (Iterator i = edges.iterator(); i.hasNext(); ) {
	      SnappingEdge e = (SnappingEdge) i.next();
	      e.getEdgeIntersectionList().addSplitEdges(edgelist);
	    }
	  }
	  
	  /**
	   * Borra las intersecciones registradas en los nodos
	   * (util de cara a reutilizar un GeometryGraph en multiples intersecciones)
	   * */
	  public void clearIntersections(){
		  for (Iterator i = edges.iterator(); i.hasNext(); ) {
		      SnappingEdge e = (SnappingEdge) i.next();
		      e.clearIntersections();
		    }
	  }
}

