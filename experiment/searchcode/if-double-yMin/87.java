package se.geoproject.atlas.map.triangulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Java conversion of c++ Polygon Triangulation
 * http://code.google.com/p/poly2tri/source/browse/#hg%2Fpoly2tri%253Fstate%253Dclosed
 * 
 * 
 * @author viktor
 */
public class SweepContext {
	
	private static double kAlpha = 0.3;
	
	Basin basin;
	EdgeEvent edge_event;
	private List<Point> points_;
	private List<Edge> edge_list = new ArrayList<Edge> ();
	
	private Point head_;
	private Point tail_;
	
	private Node af_head_;
	private Node af_middle_;
	private Node af_tail_;
	
	private AdvancingFront front_;
	
	private List<Triangle> map_ = new ArrayList<Triangle> ();
	private List<Triangle> triangles_ = new ArrayList<Triangle> ();
	
	public SweepContext(List<Point> points) {
		basin = new Basin();
		edge_event = new EdgeEvent();
		
		this.points_ = points;
		
		initEdges(points);
	}
	
	private void initEdges(List<Point> polyline) {
		int num_points = polyline.size();
		  for (int i = 0; i < num_points; i++) {
		    int j = i < num_points - 1 ? i + 1 : 0;
		    edge_list.add(new Edge(polyline.get(i), polyline.get(j)));
		  }
	}
	
	public void InitTriangulation() {
	  double xmax = points_.get(0).x;
	  double xmin = points_.get(0).x;
	  double ymax = points_.get(0).y;
	  double ymin = points_.get(0).y;

	  // Calculate bounds.
	  for (int i = 0; i < points_.size(); i++) {
	    Point p = points_.get(i);
	    if (p.x > xmax)
	      xmax = p.x;
	    if (p.x < xmin)
	      xmin = p.x;
	    if (p.y > ymax)
	      ymax = p.y;
	    if (p.y < ymin)
	      ymin = p.y;
	  }

	  double dx = kAlpha * (xmax - xmin);
	  double dy = kAlpha * (ymax - ymin);
	  head_ = new Point(xmax + dx, ymin - dy);
	  tail_ = new Point(xmin - dx, ymin - dy);

	  // Sort points along y-axis
//	  sort(points_.begin(), points_.end(), cmp);
	  
	  Collections.sort(points_, new Comparator<Point> () {

		@Override
		public int compare(Point arg0, Point arg1) {
			double y = arg0.y - arg1.y;
			if(y == 0) {
				return (int) Math.round(arg0.x - arg1.x);
			}
			else {
				return (int) Math.round(y);
			}
		}
		  
	  });

	}
	
	public void CreateAdvancingFront() {

	  // Initial triangle
	  Triangle triangle = new Triangle(points_.get(0), tail_, head_);

	  map_.add(triangle);

	  af_head_ = new Node(triangle.GetPoint(1), triangle);
	  af_middle_ = new Node(triangle.GetPoint(0), triangle);
	  af_tail_ = new Node(triangle.GetPoint(2));
	  front_ = new AdvancingFront(af_head_, af_tail_);

	  // TODO: More intuitive if head is middles next and not previous?
	  //       so swap head and tail
	  af_head_.next = af_middle_;
	  af_middle_.next = af_tail_;
	  af_middle_.prev = af_head_;
	  af_tail_.prev = af_middle_;
	}

	public int point_count() {
		return points_.size();
	}

	public Point GetPoint(int i) {
		return points_.get(i);
	}

	public Node LocateNode(Point point) {
		// TODO implement search tree
		return front_.LocateNode(point.x);
	}

	public void AddToMap(Triangle triangle) {
		map_.add(triangle);
	}

	public void MapTriangleToNodes(Triangle t) {
		for (int i = 0; i < 3; i++) {
		    if (t.GetNeighbor(i) == null) {
		      Node n = front_.LocatePoint(t.PointCW(t.GetPoint(i)));
		      if (n != null)
		        n.triangle = t;
		    }
		  }
	}

	public AdvancingFront front() {
		return front_;
	}

	public void MeshClean(Triangle triangle) {
		if (triangle != null && !triangle.IsInterior()) {
		    triangle.IsInterior(true);
		    triangles_.add(triangle);
		    for (int i = 0; i < 3; i++) {
		      if (!triangle.constrained_edge[i])
		        MeshClean(triangle.GetNeighbor(i));
		    }
		  }
	}

	public List<Triangle> GetTriangles() {
		return triangles_;
	}

	public void AddHole(List<Point> points) {
		initEdges(points);
		points_.addAll(points);
	}

}

