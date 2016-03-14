package layout;

import model.Circle;
import model.Edge;
import model.Ellipse;
import model.MultiDirectedGraph;
import model.Rectangle;
import model.Vector2D;
import model.Vertex;
/**
 * 
 * @author Daroczi Krisztian-Zoltan
 *
 */
public class ForceDirectedLayout implements ILayout{
	private MultiDirectedGraph graph;
	private Vector2D drawAreaDimension;
	
	public ForceDirectedLayout(){
		graph = null;
		drawAreaDimension = null;
	}
	
	@Override
	public void setGraph(MultiDirectedGraph g) throws IllegalArgumentException {
		if ((g == null) || (g.getVertexCount() == 0))
			throw new IllegalArgumentException("Can't pass an empty graph to the ForceDirectedLayout class");
		graph = g;
	}

	@Override
	public void setDrawAreaProperties(Vector2D dim, int zoomPercent,
			boolean forceLayout) throws IllegalArgumentException {
		if ((dim == null) || (dim.getX() <= 0) || (dim.getY() <= 0))
			throw new IllegalArgumentException("Can't pass invalid DrawArea dimensions to the ForceDirectedLayout class [" + dim.getX() + ", " + dim.getY() + "]");
		if ((zoomPercent < 10) || (zoomPercent > 200) || (zoomPercent % 10 != 0))
			throw new IllegalArgumentException("Can't pass invalid DrawArea zoom amount to the ForceDirectedLayout class [" + zoomPercent + "]");
		drawAreaDimension = dim;
	}

	@Override
	public boolean canLayout() {
		if ((drawAreaDimension == null) || (graph == null) || (graph.getVertexCount() == 0))
			return false;
		return true;
	}

	@Override
	public int applyLayout() {
		if (!canLayout())
			return 0;
		
		double totalCost = 0.0;
		double mean = 0.0;
		double epsilon = 0.0;
		int w = 0;
		int h = 0;
		
		for (int i = 0; i < graph.getVertexCount(); i++){
			Vertex v = graph.getVertexByIndex(i);
			for (int j = 0; j < v.getOutDegree(); j++){
				Edge e = v.out.get(j);
				e.straightenPoints(1);
				e.setRealDistances(true);
				e.updateDistance();
				totalCost += e.getCost();
			}
			//get the dimensions of the biggest vertex (needed for epsilon)
			int right = 0, bottom = 0;
			if (v instanceof Ellipse){
				right = ((Ellipse) v).getWidth();
				bottom = ((Ellipse) v).getHeight();
			}else if (v instanceof Rectangle){
				right = ((Rectangle) v).getWidth();
				bottom = ((Rectangle) v).getHeight();
			}else{
				right = Math.round(((Circle) v).getRadius() * 2);
				bottom = Math.round(((Circle) v).getRadius() * 2);
			}
			if (right * bottom > w * h){
				w = right;
				h = bottom;
			}
		}
		
		mean = totalCost / graph.getEdgeCount();
		
		epsilon = 10;
		//epsilon = Math.abs(mean - ((Math.max(w, h)) * 3));
		//System.out.println(epsilon);
		int iteration = 0; 
		while (true){
			boolean settled = true;
			for (int i = 0; i < graph.getVertexCount(); i++){
				Vertex s = graph.getVertexByIndex(i);
				for (int j = 0; j < s.getOutDegree(); j++){
					Edge e = s.out.get(j);
					float cost = e.getCost();
					if (Math.abs(cost - mean) > epsilon){
						pullTogether(e, mean, epsilon);
						settled = false;
					}
					e.straightenPoints(1);
					e.updateDistance();
				}
			}
			if (settled)
				break;
			if (iteration > 100)
				return -1;
			iteration++;
		}
		
		
		return 0;
	}
	
	private void pullTogether(Edge e, double mean, double eps){
		Vertex s = e.getStart();
		Vertex d = e.getEnd();
		Vector2D start = s.get100Center();
		Vector2D end = d.get100Center();
		
		
		double dist = 0.0;
		eps = 10;
		int iteration = 0;
		while (Math.abs(mean - dist) > eps){
			start = s.get100Center();
			end = d.get100Center();

			//from s->d
			{
				Vector2D direction = null;
				if (mean < e.getCost())
					direction = new Vector2D(end.getX() - start.getX(), end.getY() - start.getY());
				else
					direction = new Vector2D(-end.getX() + start.getX(), -end.getY() + start.getY());
				double mag = Math.sqrt(Math.pow(direction.getX(), 2) + Math.pow(direction.getY(), 2));
				double nx = direction.getX() / mag;
				double ny = direction.getY() / mag;
				direction.setX((int)Math.round(nx));
				direction.setY((int)Math.round(ny));
				s.setLeft(s.getLeft() + direction.getX());
				s.setTop(s.getTop() + direction.getY());
				
				//s.moveTo(direction.getX(), direction.getY());
			}
			
			//from d->s
			start = s.get100Center();
			{
				Vector2D direction = null;
				if (mean < e.getCost())
					direction = new Vector2D(start.getX() - end.getX(), start.getY() - end.getY());
				else
					direction = new Vector2D(-start.getX() + end.getX(), -start.getY() + end.getY());
				double mag = Math.sqrt(Math.pow(direction.getX(), 2) + Math.pow(direction.getY(), 2));
				double nx = direction.getX() / mag;
				double ny = direction.getY() / mag;
				direction.setX((int)Math.round(nx));
				direction.setY((int)Math.round(ny));
				//d.moveTo(direction.getX(), direction.getY());
				d.setLeft(d.getLeft() + direction.getX());
				d.setTop(d.getTop() + direction.getY());
			}
			start = s.get100Center();
			end = d.get100Center();
			dist = Math.sqrt(
					Math.pow(start.getX() - end.getX(), 2) + 
					Math.pow(start.getY() - end.getY(), 2)
				);
			if (iteration > 200)
				return;
			iteration++;
			//System.out.println("Start: " + start + " | " + "end: " + end + " | dist: " + dist + " | mean: " + mean);
		}
	}
	
}

