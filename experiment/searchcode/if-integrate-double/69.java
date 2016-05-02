package se.geoproject.atlas.map.triangulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Java conversion of c++ Polygon Triangulation
 * http://code.google.com/p/poly2tri/source/browse/#hg%2Fpoly2tri%253Fstate%253Dclosed
 * 
 * 
 * @author viktor
 */
public class Sweep {
	
	public static final double EPSILON = 1e-12;
	public static final double PI_3div4 = 3 * Math.PI / 4;
	private static final int	EDGE_EVENT_DEPTH_THRESHOLD	= 200;
	
	public static int EDGE_EVENT_DEPTH = 0;
	
	private List<Node> nodes_ = new ArrayList<Node> ();
	
	public void Triangulate(SweepContext tcx) {
		tcx.InitTriangulation();
		tcx.CreateAdvancingFront();
		// Sweep points; build mesh
		SweepPoints(tcx);
		// Clean up
		FinalizationPolygon(tcx);
	}
	
	private void FinalizationPolygon(SweepContext tcx) {
		// Get an Internal triangle to start with
		  Triangle t = tcx.front().head().next.triangle;
		  Point p = tcx.front().head().next.point;
		  while (!t.GetConstrainedEdgeCW(p)) {
		    t = t.NeighborCCW(p);
		  }

		  // Collect interior triangles constrained by edges
		  tcx.MeshClean(t);
	}

	private void SweepPoints(SweepContext tcx)
	{
	  for (int i = 1; i < tcx.point_count(); i++) {
	    Point point = tcx.GetPoint(i);
	    Node node = PointEvent(tcx, point);
	    for (int j = 0; j < point.edge_list.size(); j++) {
	      EdgeEvent(tcx, point.edge_list.get(j), node);
	    }
	  }
	}

	private void EdgeEvent(SweepContext tcx, Edge edge, Node node) {
		EDGE_EVENT_DEPTH++;
		if(EDGE_EVENT_DEPTH > EDGE_EVENT_DEPTH_THRESHOLD) {
			throw new RuntimeException("Too deep recursion");
		}
		tcx.edge_event.constrained_edge = edge;
		  tcx.edge_event.right = (edge.p.x > edge.q.x);

		  if (IsEdgeSideOfTriangle(node.triangle, edge.p, edge.q)) {
			  EDGE_EVENT_DEPTH--;
		    return;
		  }

		  // For now we will do all needed filling
		  // TODO: integrate with flip process might give some better performance
		  //       but for now this avoid the issue with cases that needs both flips and fills
		  FillEdgeEvent(tcx, edge, node);
		  EdgeEvent(tcx, edge.p, edge.q, node.triangle, edge.q);
		  EDGE_EVENT_DEPTH--;
	}

	private void EdgeEvent(SweepContext tcx, Point ep, Point eq, Triangle triangle, Point point) {
		EDGE_EVENT_DEPTH++;
		if(EDGE_EVENT_DEPTH > EDGE_EVENT_DEPTH_THRESHOLD) {
			throw new RuntimeException("Too deep recursion");
		}
		if (IsEdgeSideOfTriangle(triangle, ep, eq)) {
			EDGE_EVENT_DEPTH--;
		    return;
		  }

		  Point p1 = triangle.PointCCW(point);
		  Orientation o1 = null;
		  if (p1 != null) {
			o1 = Orient2d(eq, p1, ep);
			if (o1 == Orientation.COLLINEAR) {
				if (triangle.Contains(eq, p1)) {
					triangle.MarkConstrainedEdge(eq, p1);
					// We are modifying the constraint maybe it would be better to 
					// not change the given constraint and just keep a variable for the new constraint
					tcx.edge_event.constrained_edge.q = p1;
					triangle = triangle.NeighborAcross(point);
					if (triangle != null) {
						EdgeEvent(tcx, ep, p1, triangle, p1);
					}
				} else {
					System.err
							.println("EdgeEvent - collinear points not supported");
					//		    	throw new RuntimeException("[Unsupported] EdgeEvent - collinear points not supported");
					assert (false);
				}
				EDGE_EVENT_DEPTH--;
				return;
			}
		}
		Point p2 = triangle.PointCW(point);
		  Orientation o2 = null;
		  if (p2 != null) {
			o2 = Orient2d(eq, p2, ep);
			if (o2 == Orientation.COLLINEAR) {
				if (triangle.Contains(eq, p2)) {
					triangle.MarkConstrainedEdge(eq, p2);
					// We are modifying the constraint maybe it would be better to 
					// not change the given constraint and just keep a variable for the new constraint
					tcx.edge_event.constrained_edge.q = p2;
					triangle = triangle.NeighborAcross(point);
					if (triangle != null) {
						EdgeEvent(tcx, ep, p2, triangle, p2);
					}
				} else {
					System.err.println("EdgeEvent - collinear points not supported");
					throw new RuntimeException("[Unsupported] EdgeEvent - collinear points not supported");
//					assert (false);
				}
				EDGE_EVENT_DEPTH--;
				return;
			}
		}
		if(o1 == null) {
			EDGE_EVENT_DEPTH--;
			return;
		}
		if (o1 == o2) {
		    // Need to decide if we are rotating CW or CCW to get to a triangle
		    // that will cross edge
		    if (o1 == Orientation.CW) {
		      triangle = triangle.NeighborCCW(point);
		    }       else{
		      triangle = triangle.NeighborCW(point);
		    }
		    EdgeEvent(tcx, ep, eq, triangle, point);
		  } else {
		    // This triangle crosses constraint so lets flippin start!
		    FlipEdgeEvent(tcx, ep, eq, triangle, point);
		  }
		EDGE_EVENT_DEPTH--;
	}

	private void FlipEdgeEvent(SweepContext tcx, Point ep, Point eq, Triangle t, Point p) {
		Triangle ot = t.NeighborAcross(p);

		  if (ot == null) {
		    // If we want to integrate the fillEdgeEvent do it here
		    // With current implementation we should never get here
		    throw new RuntimeException( "[BUG:FIXME] FLIP failed due to missing triangle");
//		    assert(false);
		  }

		  Point op = ot.OppositePoint(t, p);

		  if (InScanArea(p, t.PointCCW(p), t.PointCW(p), op)) {
		    // Lets rotate shared edge one vertex CW
		    RotateTrianglePair(t, p, ot, op);
		    tcx.MapTriangleToNodes(t);
		    tcx.MapTriangleToNodes(ot);

		    if (p == eq && op == ep) {
		      if (eq == tcx.edge_event.constrained_edge.q && ep == tcx.edge_event.constrained_edge.p) {
		        t.MarkConstrainedEdge(ep, eq);
		        ot.MarkConstrainedEdge(ep, eq);
		        Legalize(tcx, t);
		        Legalize(tcx, ot);
		      } else {
		        // XXX: I think one of the triangles should be legalized here?
		      }
		    } else {
		      Orientation o = Orient2d(eq, op, ep);
		      t = NextFlipTriangle(tcx, o, t, ot, p, op);
		      FlipEdgeEvent(tcx, ep, eq, t, p);
		    }
		  } else {
		    Point newP = NextFlipPoint(ep, eq, ot, op);
		    FlipScanEdgeEvent(tcx, ep, eq, t, ot, newP);
		    EdgeEvent(tcx, ep, eq, t, p);
		  }
	}

	private void FlipScanEdgeEvent(SweepContext tcx, Point ep, Point eq, Triangle flip_triangle,
									Triangle t, Point p) {
		Triangle ot = t.NeighborAcross(p);
		
		if(ot == null) {
			return;
		}
		
		  Point op = ot.OppositePoint(t, p);
		  
		  if(op == eq) {
			  return;
		  }

		  if (t.NeighborAcross(p) == null) {
		    // If we want to integrate the fillEdgeEvent do it here
		    // With current implementation we should never get here
		    throw new RuntimeException( "[BUG:FIXME] FLIP failed due to missing triangle");
//		    assert(false);
		  }

		  if (InScanArea(eq, flip_triangle.PointCCW(eq), flip_triangle.PointCW(eq), op)) {
		    // flip with new edge op->eq
		    FlipEdgeEvent(tcx, eq, op, ot, op);
		    // TODO: Actually I just figured out that it should be possible to
		    //       improve this by getting the next ot and op before the the above
		    //       flip and continue the flipScanEdgeEvent here
		    // set new ot and op here and loop back to inScanArea test
		    // also need to set a new flip_triangle first
		    // Turns out at first glance that this is somewhat complicated
		    // so it will have to wait.
		  } else{
		    Point newP = NextFlipPoint(ep, eq, ot, op);
			FlipScanEdgeEvent(tcx, ep, eq, flip_triangle, ot, newP);
		  }
	}

	private Point NextFlipPoint(Point ep, Point eq, Triangle ot, Point op) {
		Orientation o2d = Orient2d(eq, op, ep);
		  if (o2d == Orientation.CW) {
		    // Right
		    return ot.PointCCW(op);
		  } else if (o2d == Orientation.CCW) {
		    // Left
		    return ot.PointCW(op);
		  } else{
		    throw new RuntimeException("[Unsupported] Opposing point on constrained edge");
//		    assert(false);
//		    return null;
		  }
	}

	private Triangle NextFlipTriangle(SweepContext tcx, Orientation o, Triangle t, Triangle ot, Point p, Point op) {
		if (o == Orientation.CCW) {
		    // ot is not crossing edge after flip
		    int edge_index = ot.EdgeIndex(p, op);
		    ot.delaunay_edge[edge_index] = true;
		    Legalize(tcx, ot);
		    ot.ClearDelunayEdges();
		    return t;
		  }

		  // t is not crossing edge after flip
		  int edge_index = t.EdgeIndex(p, op);

		  t.delaunay_edge[edge_index] = true;
		  Legalize(tcx, t);
		  t.ClearDelunayEdges();
		  return ot;
	}

	public static boolean InScanArea(Point pa, Point pb, Point pc, Point pd) {
		double pdx = pd.x;
		  double pdy = pd.y;
		  double adx = pa.x - pdx;
		  double ady = pa.y - pdy;
		  double bdx = pb.x - pdx;
		  double bdy = pb.y - pdy;

		  double adxbdy = adx * bdy;
		  double bdxady = bdx * ady;
		  double oabd = adxbdy - bdxady;

		  if (oabd <= EPSILON) {
		    return false;
		  }

		  double cdx = pc.x - pdx;
		  double cdy = pc.y - pdy;

		  double cdxady = cdx * ady;
		  double adxcdy = adx * cdy;
		  double ocad = cdxady - adxcdy;

		  if (ocad <= EPSILON) {
		    return false;
		  }

		  return true;
	}

	private void FillEdgeEvent(SweepContext tcx, Edge edge, Node node) {
		if (tcx.edge_event.right) {
		    FillRightAboveEdgeEvent(tcx, edge, node);
		  } else {
		    FillLeftAboveEdgeEvent(tcx, edge, node);
		  }
	}

	private void FillLeftAboveEdgeEvent(SweepContext tcx, Edge edge, Node node) {
		while (node.prev.point.x > edge.p.x) {
		    // Check if next node is below the edge
		    if (Orient2d(edge.q, node.prev.point, edge.p) == Orientation.CW) {
		      FillLeftBelowEdgeEvent(tcx, edge, node);
		    } else {
		      node = node.prev;
		    }
		  }
	}

	private void FillLeftBelowEdgeEvent(SweepContext tcx, Edge edge, Node node) {
		if (node.point.x > edge.p.x) {
		    if (Orient2d(node.point, node.prev.point, node.prev.prev.point) == Orientation.CW) {
		      // Concave
		      FillLeftConcaveEdgeEvent(tcx, edge, node);
		    } else {
		      // Convex
		      FillLeftConvexEdgeEvent(tcx, edge, node);
		      // Retry this one
		      FillLeftBelowEdgeEvent(tcx, edge, node);
		    }
		  }
	}

	private void FillLeftConvexEdgeEvent(SweepContext tcx, Edge edge, Node node) {
		// Next concave or convex?
		  if (Orient2d(node.prev.point, node.prev.prev.point, node.prev.prev.prev.point) == Orientation.CW) {
		    // Concave
		    FillLeftConcaveEdgeEvent(tcx, edge, node.prev);
		  } else{
		    // Convex
		    // Next above or below edge?
		    if (Orient2d(edge.q, node.prev.prev.point, edge.p) == Orientation.CW) {
		      // Below
		      FillLeftConvexEdgeEvent(tcx, edge, node.prev);
		    } else{
		      // Above
		    }
		  }
	}

	private void FillLeftConcaveEdgeEvent(SweepContext tcx, Edge edge, Node node) {
		Fill(tcx, node.prev);
		  if (node.prev.point != edge.p) {
		    // Next above or below edge?
		    if (Orient2d(edge.q, node.prev.point, edge.p) == Orientation.CW) {
		      // Below
		      if (Orient2d(node.point, node.prev.point, node.prev.prev.point) == Orientation.CW) {
		        // Next is concave
		        FillLeftConcaveEdgeEvent(tcx, edge, node);
		      } else{
		        // Next is convex
		      }
		    }
		  }
	}

	private void FillRightAboveEdgeEvent(SweepContext tcx, Edge edge, Node node) {
		while (node.next.point.x < edge.p.x) {
		    // Check if next node is below the edge
		    if (Orient2d(edge.q, node.next.point, edge.p) == Orientation.CCW) {
		      FillRightBelowEdgeEvent(tcx, edge, node);
		    } else {
		      node = node.next;
		    }
		  }
	}

	private void FillRightBelowEdgeEvent(SweepContext tcx, Edge edge, Node node) {
		if (node.point.x < edge.p.x) {
		    if (Orient2d(node.point, node.next.point, node.next.next.point) == Orientation.CCW) {
		      // Concave
		      FillRightConcaveEdgeEvent(tcx, edge, node);
		    } else{
		      // Convex
		      FillRightConvexEdgeEvent(tcx, edge, node);
		      // Retry this one
		      FillRightBelowEdgeEvent(tcx, edge, node);
		    }
		  }
	}

	private void FillRightConvexEdgeEvent(SweepContext tcx, Edge edge, Node node) {
		// Next concave or convex?
		  if (Orient2d(node.next.point, node.next.next.point, node.next.next.next.point) == Orientation.CCW) {
		    // Concave
		    FillRightConcaveEdgeEvent(tcx, edge, node.next);
		  } else{
		    // Convex
		    // Next above or below edge?
		    if (Orient2d(edge.q, node.next.next.point, edge.p) == Orientation.CCW) {
		      // Below
		      FillRightConvexEdgeEvent(tcx, edge, node.next);
		    } else{
		      // Above
		    }
		  }
	}

	private void FillRightConcaveEdgeEvent(SweepContext tcx, Edge edge, Node node) {
		Fill(tcx, node.next);
		  if (node.next.point != edge.p) {
		    // Next above or below edge?
		    if (Orient2d(edge.q, node.next.point, edge.p) == Orientation.CCW) {
		      // Below
		      if (Orient2d(node.point, node.next.point, node.next.next.point) == Orientation.CCW) {
		        // Next is concave
		        FillRightConcaveEdgeEvent(tcx, edge, node);
		      } else {
		        // Next is convex
		      }
		    }
		  }
	}

	private boolean IsEdgeSideOfTriangle(Triangle triangle, Point ep, Point eq) {
		int index = triangle.EdgeIndex(ep, eq);

		  if (index != -1) {
		    triangle.MarkConstrainedEdge(index);
		    Triangle t = triangle.GetNeighbor(index);
		    if (t != null) {
		      t.MarkConstrainedEdge(ep, eq);
		    }
		    return true;
		  }
		  return false;
	}

	private Node PointEvent(SweepContext tcx, Point point) {
		Node node = tcx.LocateNode(point);
		  Node new_node = NewFrontTriangle(tcx, point, node);

		  // Only need to check +epsilon since point never have smaller
		  // x value than node due to how we fetch nodes from the front
		  if (point.x <= node.point.x + EPSILON) {
		    Fill(tcx, node);
		  }

		  //tcx.AddNode(new_node);

		  FillAdvancingFront(tcx, new_node);
		  return new_node;
	}

	private void FillAdvancingFront(SweepContext tcx, Node n) {
		// Fill right holes
		  Node node = n.next;

		  while (node.next != null) {
		    double angle = HoleAngle(node);
		    if (angle > Math.PI / 2 || angle < Math.PI / 2 * -1) break;
		    Fill(tcx, node);
		    node = node.next;
		  }

		  // Fill left holes
		  node = n.prev;

		  while (node.prev != null) {
		    double angle = HoleAngle(node);
		    if (angle > Math.PI / 2 || angle < Math.PI / 2 * -1) break;
		    Fill(tcx, node);
		    node = node.prev;
		  }

		  // Fill right basins
		  if (n.next != null && n.next.next != null) {
		    double angle = BasinAngle(n);
		    if (angle < PI_3div4) {
		      FillBasin(tcx, n);
		    }
		  }
	}

	private void FillBasin(SweepContext tcx, Node node) {
		if (Orient2d(node.point, node.next.point, node.next.next.point) == Orientation.CCW) {
		    tcx.basin.left_node = node.next.next;
		  } else {
		    tcx.basin.left_node = node.next;
		  }

		  // Find the bottom and right node
		  tcx.basin.bottom_node = tcx.basin.left_node;
		  while (tcx.basin.bottom_node.next != null
		         && tcx.basin.bottom_node.point.y >= tcx.basin.bottom_node.next.point.y) {
		    tcx.basin.bottom_node = tcx.basin.bottom_node.next;
		  }
		  if (tcx.basin.bottom_node == tcx.basin.left_node) {
		    // No valid basin
		    return;
		  }

		  tcx.basin.right_node = tcx.basin.bottom_node;
		  while (tcx.basin.right_node.next != null
		         && tcx.basin.right_node.point.y < tcx.basin.right_node.next.point.y) {
		    tcx.basin.right_node = tcx.basin.right_node.next;
		  }
		  if (tcx.basin.right_node == tcx.basin.bottom_node) {
		    // No valid basins
		    return;
		  }

		  tcx.basin.width = tcx.basin.right_node.point.x - tcx.basin.left_node.point.x;
		  tcx.basin.left_highest = tcx.basin.left_node.point.y > tcx.basin.right_node.point.y;

		  FillBasinReq(tcx, tcx.basin.bottom_node);
	}

	private void FillBasinReq(SweepContext tcx, Node node) {
		// if shallow stop filling
		  if (IsShallow(tcx, node)) {
		    return;
		  }

		  Fill(tcx, node);

		  if (node.prev == tcx.basin.left_node && node.next == tcx.basin.right_node) {
		    return;
		  } else if (node.prev == tcx.basin.left_node) {
		    Orientation o = Orient2d(node.point, node.next.point, node.next.next.point);
		    if (o == Orientation.CW) {
		      return;
		    }
		    node = node.next;
		  } else if (node.next == tcx.basin.right_node) {
		    Orientation o = Orient2d(node.point, node.prev.point, node.prev.prev.point);
		    if (o == Orientation.CCW) {
		      return;
		    }
		    node = node.prev;
		  } else {
		    // Continue with the neighbor node with lowest Y value
		    if (node.prev.point.y < node.next.point.y) {
		      node = node.prev;
		    } else {
		      node = node.next;
		    }
		  }

		  FillBasinReq(tcx, node);
	}

	private static boolean IsShallow(SweepContext tcx, Node node) {
		double height;

		  if (tcx.basin.left_highest) {
		    height = tcx.basin.left_node.point.y - node.point.y;
		  } else {
		    height = tcx.basin.right_node.point.y - node.point.y;
		  }

		  // if shallow stop filling
		  if (tcx.basin.width > height) {
		    return true;
		  }
		  return false;
	}

	public static Orientation Orient2d(Point pa, Point pb, Point pc) {
		 double detleft = (pa.x - pc.x) * (pb.y - pc.y);
		  double detright = (pa.y - pc.y) * (pb.x - pc.x);
		  double val = detleft - detright;
		  if (val > -EPSILON && val < EPSILON) {
		    return Orientation.COLLINEAR;
		  } else if (val > 0) {
		    return Orientation.CCW;
		  }
		  return Orientation.CW;
	}

	private double BasinAngle(Node node) {
		double ax = node.point.x - node.next.next.point.x;
		  double ay = node.point.y - node.next.next.point.y;
		  return Math.atan2(ay, ax);
	}

	private double HoleAngle(Node node) {
		/* Complex plane
		   * ab = cosA +i*sinA
		   * ab = (ax + ay*i)(bx + by*i) = (ax*bx + ay*by) + i(ax*by-ay*bx)
		   * atan2(y,x) computes the principal value of the argument function
		   * applied to the complex number x+iy
		   * Where x = ax*bx + ay*by
		   *       y = ax*by - ay*bx
		   */
		  double ax = node.next.point.x - node.point.x;
		  double ay = node.next.point.y - node.point.y;
		  double bx = node.prev.point.x - node.point.x;
		  double by = node.prev.point.y - node.point.y;
		  return Math.atan2(ax * by - ay * bx, ax * bx + ay * by);
	}

	private void Fill(SweepContext tcx, Node node) {
		Triangle triangle = new Triangle(node.prev.point, node.point, node.next.point);

		  // TODO: should copy the constrained_edge value from neighbor triangles
		  //       for now constrained_edge values are copied during the legalize
		  triangle.MarkNeighbor(node.prev.triangle);
		  triangle.MarkNeighbor(node.triangle);

		  tcx.AddToMap(triangle);

		  // Update the advancing front
		  node.prev.next = node.next;
		  node.next.prev = node.prev;

		  // If it was legalized the triangle has already been mapped
		  if (!Legalize(tcx, triangle)) {
		    tcx.MapTriangleToNodes(triangle);
		  }
	}

	private Node NewFrontTriangle(SweepContext tcx, Point point, Node node) {
		Triangle triangle = new Triangle(point, node.point, node.next.point);

		  triangle.MarkNeighbor(node.triangle);
		  tcx.AddToMap(triangle);

		  Node new_node = new Node(point);
		  nodes_.add(new_node);

		  new_node.next = node.next;
		  new_node.prev = node;
		  node.next.prev = new_node;
		  node.next = new_node;

		  if (!Legalize(tcx, triangle)) {
		    tcx.MapTriangleToNodes(triangle);
		  }

		  return new_node;
	}

	private boolean Legalize(SweepContext tcx, Triangle t) {
		// To legalize a triangle we start by finding if any of the three edges
		  // violate the Delaunay condition
		  for (int i = 0; i < 3; i++) {
		    if (t.delaunay_edge[i])
		      continue;

		    Triangle ot = t.GetNeighbor(i);

		    if (ot != null) {
		      Point p = t.GetPoint(i);
		      Point op = ot.OppositePoint(t, p);
		      int oi = ot.Index(op);

		      // If this is a Constrained Edge or a Delaunay Edge(only during recursive legalization)
		      // then we should not try to legalize
		      if (ot.constrained_edge[oi] || ot.delaunay_edge[oi]) {
		        t.constrained_edge[i] = ot.constrained_edge[oi];
		        continue;
		      }

		      boolean inside = Incircle(p, t.PointCCW(p), t.PointCW(p), op);

		      if (inside) {
		        // Lets mark this shared edge as Delaunay
		        t.delaunay_edge[i] = true;
		        ot.delaunay_edge[oi] = true;

		        // Lets rotate shared edge one vertex CW to legalize it
		        RotateTrianglePair(t, p, ot, op);

		        // We now got one valid Delaunay Edge shared by two triangles
		        // This gives us 4 new edges to check for Delaunay

		        // Make sure that triangle to node mapping is done only one time for a specific triangle
		        boolean not_legalized = !Legalize(tcx, t);
		        if (not_legalized) {
		          tcx.MapTriangleToNodes(t);
		        }

		        not_legalized = !Legalize(tcx, ot);
		        if (not_legalized)
		          tcx.MapTriangleToNodes(ot);

		        // Reset the Delaunay edges, since they only are valid Delaunay edges
		        // until we add a new triangle or point.
		        // XXX: need to think about this. Can these edges be tried after we
		        //      return to previous recursive level?
		        t.delaunay_edge[i] = false;
		        ot.delaunay_edge[oi] = false;

		        // If triangle have been legalized no need to check the other edges since
		        // the recursive legalization will handles those so we can end here.
		        return true;
		      }
		    }
		  }
		  return false;
	}

	private void RotateTrianglePair(Triangle t, Point p, Triangle ot, Point op) {
		Triangle n1, n2, n3, n4;
		  n1 = t.NeighborCCW(p);
		  n2 = t.NeighborCW(p);
		  n3 = ot.NeighborCCW(op);
		  n4 = ot.NeighborCW(op);

		  boolean ce1, ce2, ce3, ce4;
		  ce1 = t.GetConstrainedEdgeCCW(p);
		  ce2 = t.GetConstrainedEdgeCW(p);
		  ce3 = ot.GetConstrainedEdgeCCW(op);
		  ce4 = ot.GetConstrainedEdgeCW(op);

		  boolean de1, de2, de3, de4;
		  de1 = t.GetDelunayEdgeCCW(p);
		  de2 = t.GetDelunayEdgeCW(p);
		  de3 = ot.GetDelunayEdgeCCW(op);
		  de4 = ot.GetDelunayEdgeCW(op);

		  t.Legalize(p, op);
		  ot.Legalize(op, p);

		  // Remap delaunay_edge
		  ot.SetDelunayEdgeCCW(p, de1);
		  t.SetDelunayEdgeCW(p, de2);
		  t.SetDelunayEdgeCCW(op, de3);
		  ot.SetDelunayEdgeCW(op, de4);

		  // Remap constrained_edge
		  ot.SetConstrainedEdgeCCW(p, ce1);
		  t.SetConstrainedEdgeCW(p, ce2);
		  t.SetConstrainedEdgeCCW(op, ce3);
		  ot.SetConstrainedEdgeCW(op, ce4);

		  // Remap neighbors
		  // XXX: might optimize the markNeighbor by keeping track of
		  //      what side should be assigned to what neighbor after the
		  //      rotation. Now mark neighbor does lots of testing to find
		  //      the right side.
		  t.ClearNeighbors();
		  ot.ClearNeighbors();
		  if (n1 != null) ot.MarkNeighbor(n1);
		  if (n2 != null) t.MarkNeighbor(n2);
		  if (n3 != null) t.MarkNeighbor(n3);
		  if (n4 != null) ot.MarkNeighbor(n4);
		  t.MarkNeighbor(ot);
	}

	private boolean Incircle(Point pa, Point pb, Point pc, Point pd) {
		double adx = pa.x - pd.x;
		  double ady = pa.y - pd.y;
		  double bdx = pb.x - pd.x;
		  double bdy = pb.y - pd.y;

		  double adxbdy = adx * bdy;
		  double bdxady = bdx * ady;
		  double oabd = adxbdy - bdxady;

		  if (oabd <= 0)
		    return false;

		  double cdx = pc.x - pd.x;
		  double cdy = pc.y - pd.y;

		  double cdxady = cdx * ady;
		  double adxcdy = adx * cdy;
		  double ocad = cdxady - adxcdy;

		  if (ocad <= 0)
		    return false;

		  double bdxcdy = bdx * cdy;
		  double cdxbdy = cdx * bdy;

		  double alift = adx * adx + ady * ady;
		  double blift = bdx * bdx + bdy * bdy;
		  double clift = cdx * cdx + cdy * cdy;

		  double det = alift * (bdxcdy - cdxbdy) + blift * ocad + clift * oabd;

		  return det > 0;
	}

}

