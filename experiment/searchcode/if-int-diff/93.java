package layout;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

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
public class GridLayout implements ILayout{

	private MultiDirectedGraph graph;
	private Vector2D drawAreaDimension;
	private boolean keepRatio;
	private int zoom;
	private boolean force;
	private int gridX;
	private int gridY;
	private int diff;
	private int avgWidth;
	private Vertex[][] gridMatrix;
	private Vector<Vertex> sorted;
	
	public GridLayout(boolean keepAspectRatio){
		graph = null;
		drawAreaDimension = null;
		keepRatio = keepAspectRatio;
		gridX = 0;
		gridY = 0;
		diff = 0;
		
		avgWidth = 0;
		gridMatrix = null;
	}
	@Override
	public void setGraph(MultiDirectedGraph g) throws IllegalArgumentException{
		if ((g == null) || (g.getVertexCount() == 0))
			throw new IllegalArgumentException("Can't pass an empty graph to the GridLayout class");
		graph = g;
	}

	@Override
	public void setDrawAreaProperties(Vector2D dim, int zoomPercent, boolean forceLayout) throws IllegalArgumentException {
		if ((dim == null) || (dim.getX() <= 0) || (dim.getY() <= 0))
			throw new IllegalArgumentException("Can't pass invalid DrawArea dimensions to the GridLayout class [" + dim.getX() + ", " + dim.getY() + "]");
		if ((zoomPercent < 10) || (zoomPercent > 200) || (zoomPercent % 10 != 0))
			throw new IllegalArgumentException("Can't pass invalid DrawArea zoom amount to the GridLayout class [" + zoomPercent + "]");
		drawAreaDimension = dim;
		zoom = zoomPercent;
		force = forceLayout;
	}

	private void getGridLayout(){
		boolean found = false;
		int a = graph.getVertexCount();
		int i = 0;
		if (keepRatio)
			i = (int) (Math.sqrt(a) * 
					(Math.max(drawAreaDimension.getX(), drawAreaDimension.getY()) / Math.min(drawAreaDimension.getX(), drawAreaDimension.getY())));
		else
			i = (int) (Math.sqrt(a));
		int j = i;
		int sqrt = (int) (Math.sqrt(a));
		if (sqrt * sqrt == a) {
			gridX = sqrt;
			gridY = sqrt;
		} else {
			while (i >= 2) {
				j = i;
				if (i * j < a)
					break;
				while (j >= 2) {
					if (i * j == a) {
						found = true;
						break;
					}

					if (i * j < a)
						break;
					j--;
				}
				if (found)
					break;
				i--;
			}

			diff = a - i * j;
			while ((diff > i) || (diff > j)){
				i++;
				diff = a - i * j;
			}
			
			if (gridX >= gridY){
				gridX = Math.max(i, j);
				gridY = Math.min(i, j);
			} else {
				gridX = Math.min(i, j);
				gridY = Math.max(i, j);
			}
		}

	}
	
	private boolean canCreateGridMatrix(){
		int vCount = graph.getVertexCount();
		float totalWidth = 0;
		
		for (int i = 0; i < vCount; i++){
			Vertex v = graph.getVertexByIndex(i);
			if (v instanceof Circle)
				totalWidth += ((Circle) v).getRadius() * 2;
			else if (v instanceof Rectangle)
				totalWidth += ((Rectangle) v).getWidth();
			else
				totalWidth += ((Ellipse) v).getWidth();
		}
		
		// the average width of a vertex
		avgWidth = Math.round(totalWidth / vCount);
		
		// if the average row width is less than the width of the drawArea and the layout is not forced, 
		// then it's not possible to layout the graph
		if ((drawAreaDimension.getX() < avgWidth * gridX) && (!force))
			return false;
		return true;
	}
	
	@Override
	public boolean canLayout() {
		if ((drawAreaDimension == null) || (graph == null))
			return false;
		
		//get the number of grid lines that will be used
		getGridLayout();
		if ((gridX == 0) || (gridY == 0))
			return false;
		
		//get an evenly distributed grid matrix of the vertices based on their width
		return canCreateGridMatrix();
	}

	public void interchangeColumns(int c1, int c2){
		if (!((c1 >= 0) && (c1 < gridY) && (c2 >= 0) && (c2 < gridY)))
			return;
		for (int i = 0; i < gridY; i++){
			Vertex aux = gridMatrix[c2][i];
			gridMatrix[c2][i] = gridMatrix[c1][i];
			gridMatrix[c1][i] = aux;
		}
	}
	
	public void interchangeLines(int l1, int l2){
		if (!((l1 >= 0) && (l1 < gridX) && (l2 >= 0) && (l2 < gridX)))
			return;
		for (int i = 0; i < gridX; i++){
			Vertex aux = gridMatrix[i][l2];
			gridMatrix[i][l2] = gridMatrix[i][l1];
			gridMatrix[i][l1] = aux;
		}
	}
	
	public class VertexWidthComparator implements Comparator<Vertex> {

		@Override
		public int compare(Vertex o1, Vertex o2) {
			float lW = 0, rW = 0;
			if (o1 instanceof Circle){
				lW = ((Circle) o1).getRadius() * 2;
			}else if (o1 instanceof Rectangle)
				lW = ((Rectangle) o1).getWidth();
			else
				lW = ((Ellipse) o1).getWidth();
			
			if (o2 instanceof Circle){
				rW = ((Circle) o2).getRadius() * 2;
			}else if (o2 instanceof Rectangle)
				rW = ((Rectangle) o2).getWidth();
			else
				rW = ((Ellipse) o2).getWidth();
			
			return Float.compare(lW, rW);
		}
		
	}
	
	public class VertexNeighborCountComparator implements Comparator<Vertex> {
		@Override
		public int compare(Vertex o1, Vertex o2){
			float n1 = (o1.getInDegree() + o1.getOutDegree());
			float n2 = (o2.getInDegree() + o2.getOutDegree());
			return Float.compare(n1, n2);
		}
	}

/*	private void resetEdges(){
		for (int i = 0; i < this.graph.getVertexCount(); i++){
			Vertex currentVertex = this.graph.getVertexByIndex(i);
			for (int j = 0; j < currentVertex.getOutDegree(); j++){
				Edge currentEdge = currentVertex.out.get(j);
				currentEdge.straightenPoints(1);
			}
		}
	}*/
	

	@Override
	public int applyLayout() {
		if (!canLayout())
			return zoom;
		
		int gridW = drawAreaDimension.getX();
		int gridH = drawAreaDimension.getY();
		
		if (diff > 0){
			if ((gridX > gridY) || (gridW > gridH)) 
				gridMatrix = new Vertex[gridX + 1][gridY];
			else
				gridMatrix = new Vertex[gridX][gridY + 1];
		}
		else
			gridMatrix = new Vertex[gridX][gridY];
		
		int vCount = graph.getVertexCount();
		sorted = new Vector<Vertex>();
		for (int i = 0; i < vCount; i++)
			sorted.add(graph.getVertexByIndex(i));
		
		Collections.sort(sorted, new VertexNeighborCountComparator());
		gridMatrix[0][0] = sorted.get(0);
		
		Vertex last = sorted.get(0);
		
		int colLength = gridMatrix.length;
		Vertex[] row = gridMatrix[0];
		int rowLength = row.length;
		
		for (int i = 1; i < sorted.size(); i++){
			Vertex vert = sorted.get(i);
			int j = 0;
			int k = 0;
			for (j = 0; j < rowLength; j++){
				boolean found = false;
				for (k = 0; k < colLength; k++){
					if (gridMatrix[k][j] == last){
						found = true;
						break;
					}
				}
				if (found)
					break;
			}
			Vector2D pos = new Vector2D(j, k);
			getFreeCell(pos);
			gridMatrix[pos.getX()][pos.getY()] = vert;
			last = vert;
		}
		
		
		if (diff > 0){
			if ((gridX > gridY) || (gridW > gridH)) 
				gridX++;
			else
				gridY++;
		}
		
		gridW = gridW / gridX + 1;
		gridH = gridH / gridY + 1;

		
		for (int i = 0; i < gridX; i++){
			for (int j = 0; j < gridY; j++){
				Vertex v = gridMatrix[i][j];
				if (v == null)
					continue;
				int x = gridW / 2 + i * gridW;
				int y = gridH / 2 + j * gridH;
				if (v instanceof Circle){
					Circle c = (Circle) v;
					c.setLeft(Math.round(x - c.getRadius()));
					c.setTop(Math.round(y - c.getRadius()));
				} else if (v instanceof Ellipse){
					Ellipse e = (Ellipse) v;
					e.setLeft(x - (e.getWidth() / 2 ));
					e.setTop (y - (e.getHeight() / 2));
				} else if (v instanceof Rectangle){
					Rectangle r = (Rectangle) v;
					r.setLeft(x - ( r.getWidth() / 2) );
					r.setTop (y - ( r.getHeight() / 2) );
				}
				
			}
		}

		for (int i = 0; i < graph.getVertexCount(); i++){
			Vertex v = graph.getVertexByIndex(i);
			for (int j = 0; j < v.getOutDegree(); j++){
				Edge e = v.out.get(j);
				e.straightenPoints(1);
			}
		}
		return zoom;
	}
	
/*	private void pushVerts(Vertex v){
		for (int i = 0; i < graph.getVertexCount(); i++){
			graph.getVertexByIndex(i).setMarked(false);
			graph.getVertexByIndex(i).setIsolated(false);
		}
		for (int i = 0; i < graph.getVertexCount(); i++){
			if (!v.getMarked() || !v.getIsolated())
				nextNeighbor(v);
		}
	}
	*/
	private void getFreeCell(Vector2D rowCol){
		int i = rowCol.getX();
		int j = rowCol.getY();
		
		//check the neighborhood of the cell
		try{ Vertex v = gridMatrix[i-1][j-1]; 	if (v == null){ 	rowCol.setX(i-1); rowCol.setY(j-1);		return;} } catch (Exception e){}
		try{ Vertex v = gridMatrix[i-1][j]; 	if (v == null){		rowCol.setX(i-1); rowCol.setY(j); 		return;} } catch (Exception e){}
		try{ Vertex v = gridMatrix[i-1][j+1]; 	if (v == null){		rowCol.setX(i-1); rowCol.setY(j+1);		return;} } catch (Exception e){}
		try{ Vertex v = gridMatrix[i][j-1]; 	if (v == null){		rowCol.setX(i); rowCol.setY(j-1); 		return;} } catch (Exception e){}
		try{ Vertex v = gridMatrix[i][j+1]; 	if (v == null){		rowCol.setX(i); rowCol.setY(j+1); 		return;} } catch (Exception e){}
		try{ Vertex v = gridMatrix[i+1][j-1]; 	if (v == null){		rowCol.setX(i+1); rowCol.setY(j-1);		return;} } catch (Exception e){}
		try{ Vertex v = gridMatrix[i+1][j]; 	if (v == null){		rowCol.setX(i+1); rowCol.setY(j); 		return;} } catch (Exception e){}
		try{ Vertex v = gridMatrix[i+1][j+1]; 	if (v == null){		rowCol.setX(i+1); rowCol.setY(j+1);		return;} } catch (Exception e){}
		
		for (i = rowCol.getX() - 1; i >= 0; i--){
			for (j = 0; j < gridMatrix[0].length; j++){
				if (gridMatrix[i][j] == null){
					rowCol.setX(i);
					rowCol.setY(j);
					return;
				}
			}
		}
		
		for (i = rowCol.getX() + 1; i < gridMatrix.length; i++){
			for (j = 0; j < gridMatrix[0].length; j++){
				if (gridMatrix[i][j] == null){
					rowCol.setX(i);
					rowCol.setY(j);
					return;
				}
			}
		}

		
	}
	
	/*
	private void nextNeighbor(Vertex v){
		v.setMarked(true);
		if (v.getOutDegree() > 0){
			for (int i = 0 ; i < v.getOutDegree(); i++){
				Vertex vert = v.out.get(i).getEnd();
				//if (!vert.getMarked() && !(vert.getIsolated())){
					int colLength = gridMatrix.length;
					Vertex[] row = gridMatrix[0];
					int rowLength = row.length;
					int j = 0;
					int k = 0;
					for (j = 0; j < rowLength; j++){
						boolean found = false;
						for (k = 0; k < colLength; k++){
							if (gridMatrix[k][j] == v){
								found = true;
								break;
							}
						}
						if (found)
							break;
					}
					System.out.println(vert + " in the first for");
					Vector2D pos = new Vector2D(j, k);
					getFreeCell(pos);
					gridMatrix[pos.getX()][pos.getY()] = vert;
					nextNeighbor(vert);
				//}
			}
		}else if (v.getInDegree() > 0){
			for (int i = 0 ; i < v.getInDegree(); i++){
				Vertex vert = v.in.get(i).getStart();
				//if (!vert.getMarked() && !vert.getIsolated()){
					int colLength = gridMatrix.length;
					Vertex[] row = gridMatrix[0];
					int rowLength = row.length;
					int j = 0;
					int k = 0;
					for (j = 0; j < rowLength; j++){
						boolean found = false;
						for (k = 0; k < colLength; k++){
							if (gridMatrix[k][j] == v){
								found = true;
								break;
							}
						}
						if (found)
							break;
					}
					System.out.println(vert + " in the second for");
					Vector2D pos = new Vector2D(j, k);
					getFreeCell(pos);
					gridMatrix[pos.getX()][pos.getY()] = vert;
					nextNeighbor(vert);
				//}
			}
		}
		v.setIsolated(true);
		
	}*/
	/*
	public int applyLayout2() {
		if (!canLayout())
			return zoom;
		
		int gridW = drawAreaDimension.getX();
		int gridH = drawAreaDimension.getY();
		
		if (diff > 0){
			if ((gridX > gridY) || (gridW > gridH)) 
				gridMatrix = new Vertex[gridX + 1][gridY];
			else
				gridMatrix = new Vertex[gridX][gridY + 1];
		}
		else
			gridMatrix = new Vertex[gridX][gridY];
		
		int vCount = graph.getVertexCount();
		sorted = new Vector<Vertex>();
		for (int i = 0; i < vCount; i++)
			sorted.add(graph.getVertexByIndex(i));
		
		Collections.sort(sorted, new VertexWidthComparator());
		
		//create the gridmatrix and put in the vertices evenly distributed by width
		if (diff > 0){
			if ((gridX > gridY) || (gridW > gridH)) {
				for (int i = 0; i < diff; i++){
					gridMatrix[gridX][i] = sorted.lastElement();
					sorted.remove(sorted.size() - 1);
				}
				//gridX--;
			}else {
				for (int i = 0; i < diff; i++){
					gridMatrix[i][gridY] = sorted.lastElement();
					sorted.remove(sorted.size() - 1);
				}
				//gridY--;
			}
		}
		
		for (int i = 0; i < gridX; i++){
			int j = 0;
			while (j < gridY){
				gridMatrix[i][j] = sorted.get(0);
				sorted.remove(0);
				j++;
				if (j < gridY){
					gridMatrix[i][j] = sorted.lastElement();
					sorted.remove(sorted.size() - 1);
					j++;
				}
			}
		}
		
		//update the graph based on the gridmatrix
		
		if (diff > 0){
			if ((gridX > gridY) || (gridW > gridH)) 
				gridX++;
			else
				gridY++;
		}
		
		gridW = gridW / gridX + 1;
		gridH = gridH / gridY + 1;

		
		

		
		for (int i = 0; i < gridX; i++){
			for (int j = 0; j < gridY; j++){
				Vertex v = gridMatrix[i][j];
				if (v == null)
					continue;
				int x = gridW / 2 + i * gridW;
				int y = gridH / 2 + j * gridH;
				if (v instanceof Circle){
					Circle c = (Circle) v;
					c.setLeft(Math.round(x - c.getRadius()));
					c.setTop(Math.round(y - c.getRadius()));
				} else if (v instanceof Ellipse){
					Ellipse e = (Ellipse) v;
					e.setLeft(x - (e.getWidth() / 2 ));
					e.setTop (y - (e.getHeight() / 2));
				} else if (v instanceof Rectangle){
					Rectangle r = (Rectangle) v;
					r.setLeft(x - ( r.getWidth() / 2) );
					r.setTop (y - ( r.getHeight() / 2) );
				}
				
			}
		}

		//===============================================
		int treshHold = 130;
		int step = 0;
		
		Vertex bestGrid[][] = new Vertex[gridX][gridY];
		
		Vector<Edge> edges = new Vector<Edge>();
		for (int i = 0; i < this.graph.getVertexCount(); i++){
			Vertex currentVertex = this.graph.getVertexByIndex(i);
			for (int j = 0; j < currentVertex.getOutDegree(); j++){
				if (!(edges.contains(currentVertex.out.get(j))))
					edges.add(currentVertex.out.get(j));
			}
		}
		
		int min = Integer.MAX_VALUE;
		while (step < treshHold){
			int iCount = 0;
			for (int i = 0; i < edges.size(); i++){
				//first count the number of intersections
				for (int j = i + 1; j < edges.size(); j++){
					iCount += edges.get(i).getIntersectionCount(edges.get(j));
				}
			}
				
			
			//generate a new random gridlayout of the graph
			Random r = new Random();
			//if (r.nextInt() % 2 == 0){
			//	interchangeColumns(r.nextInt(gridY), r.nextInt(gridY));
			//}else
			//interchangeLines(r.nextInt(gridX), r.nextInt(gridX));
			
			//re-straighten the edges
			resetEdges();
			
			if (iCount < min){
				bestGrid = gridMatrix;
				min = iCount;
			}
			
			System.out.println("new intersection count = " + iCount);
			
			if (min == 0)
				break;
			
			step++;
		}

		System.out.println("number of steps: " + step);
		gridMatrix = bestGrid;
		
		
		
		return zoom;
	}*/

}

