package layout.Router;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collections;
import java.util.Vector;

import model.Circle;
import model.Edge;
import model.Ellipse;
import model.MultiDirectedGraph;
import model.MyException;
import model.Rectangle;
import model.ShortestPath;
import model.ShortestPathException;
import model.Vector2D;
import model.Vertex;

/**
 * A class that re-calculates the edges' custom drawing points' positions so it won't intersect with
 * another vertex.
 * @author Daroczi Krisztian-Zoltan
 * @version 1.0
 */
public class EdgeRouter {

	private MultiDirectedGraph grid;
	private MultiDirectedGraph graph;
	private float squareSize;
	private boolean smooth;
	
	public EdgeRouter(MultiDirectedGraph g, boolean smooth){
		if ((g == null) || (g.getVertexCount() == 0))
			throw new IllegalArgumentException("Can't pass an empty graph to the EdgeRouter class");
		graph = g;
		grid = null;
		this.smooth = smooth;
		squareSize = Float.POSITIVE_INFINITY;
		
		calculateSquareSize();
	}
	
	private void makeGridsRectangular(){
		grid = new MultiDirectedGraph();
		
		int sSize = (int) Math.round(squareSize);
		int id = 0;
		for (int i = 0; i < this.graph.getVertexCount(); i++){
			
			Vertex v = graph.getVertexByIndex(i);
			
			int left = v.getLeft();
			int top = v.getTop();
			int right = 0;
			int bottom = 0;
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
			
			right = left + right + 1 * sSize;
			bottom = top + bottom + 1 * sSize;
			
			left = v.getLeft() - 1 * sSize;
			top = v.getTop() - 1 * sSize;
						
			Vector<Vector<Circle>> m = new Vector<Vector<Circle>>();
			
			//add vertices around each vertex from the graph
			int y = 0;
			int x = 0;
			for (int k = top; k <= bottom; k+=(bottom-top)/2){
				x = 0;
				Vector<Circle> temp = new Vector<Circle>();
				for (int j = left; j <= right; j+=(right-left)/2){
					try {
						Circle c = new Circle(id+1, ""+j+"_"+k,Color.BLACK, Color.ORANGE, j, k, 0, 3.0f);
						if (!intersects(c, v)){
							grid.addVertex(new Circle(id++, v.getId() + "." + id ,Color.BLACK, Color.ORANGE, j, k, 0, 3.0f));
							temp.add((Circle) grid.getVertexById(id-1));
						}else
							temp.add(null);
					} catch (MyException e) {
						e.printStackTrace();
					}
					//m.add(temp);
					y++;
				}
				m.add(temp);
				x++;
			}
			int yDim = m.size();
			for (int j = 0; j < yDim; j++){
				Vector<Circle> temp = m.get(j);
				int xDim = temp.size();
				for (int k = 0; k < xDim; k++){
					
					Circle r_m1_c = null;
					
					Circle r_c_m1 = null;
					Circle r_c_p1 = null;
					
					Circle r_p1_c = null;
					
					
					try { r_m1_c = m.get(j-1).get(k); } catch (ArrayIndexOutOfBoundsException e) {}

					
					try { r_c_m1 = m.get(j).get(k-1); } catch (ArrayIndexOutOfBoundsException e) {}
					try { r_c_p1 = m.get(j).get(k+1); } catch (ArrayIndexOutOfBoundsException e) {}
					
					try { r_p1_c = m.get(j+1).get(k); } catch (ArrayIndexOutOfBoundsException e) {}
					
					Vertex vert = temp.get(k);
					
					try { 
						grid.addEdge(vert, r_m1_c, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true);
						//grid.addEdge(r_m1_c, vert, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true); 
						//System.out.println("Edge from " + vert + " to " + r_m1_c);
					} catch (MyException e){ }
					
					try { 
						grid.addEdge(vert, r_c_m1, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true);
						//grid.addEdge(r_c_m1, vert, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true);
						//System.out.println("Edge from " + vert + " to " + r_c_m1);
					} catch (MyException e){ }
					try { 
						grid.addEdge(vert, r_c_p1, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true); 
						//grid.addEdge(r_c_p1, vert, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true);
						//System.out.println("Edge from " + vert + " to " + r_c_p1);
					} catch (MyException e){ }
					

					try { 
						grid.addEdge(vert, r_p1_c, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true); 
						//grid.addEdge(r_p1_c, vert, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true); 
						//System.out.println("Edge from " + vert + " to " + r_p1_c);
					} catch (MyException e){ }
				}
			}
			
		}		
	}
	
	private void makeGrids(){
		grid = new MultiDirectedGraph();
		
		int sSize = (int) Math.round(squareSize);
		int id = 0;
		for (int i = 0; i < this.graph.getVertexCount(); i++){
			
			Vertex v = graph.getVertexByIndex(i);
			
			int left = v.getLeft();
			int top = v.getTop();
			int right = 0;
			int bottom = 0;
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
			
			right = left + right + 1 * sSize;
			bottom = top + bottom + 1 * sSize;
			
			left = v.getLeft() - 1 * sSize;
			top = v.getTop() - 1 * sSize;
						
			Vector<Vector<Circle>> m = new Vector<Vector<Circle>>();
			
			//add vertices around each vertex from the graph
			int y = 0;
			int x = 0;
			//for (int k = top; k <= bottom; k+=sSize){
			for (int k = top; k <= bottom; k+=(bottom-top)/4){
				x = 0;
				Vector<Circle> temp = new Vector<Circle>();
				//for (int j = left; j <= right; j+=sSize){
				for (int j = left; j <= right; j+=(right-left)/4){
					try {
						Circle c = new Circle(id+1, ""+j+"_"+k,Color.BLACK, Color.ORANGE, j, k, 0, 3.0f);
						if (!intersects(c, v)){
							grid.addVertex(new Circle(id++, v.getId() + "." + id ,Color.BLACK, Color.ORANGE, j, k, 0, 3.0f));
							temp.add((Circle) grid.getVertexById(id-1));
						}else
							temp.add(null);
					} catch (MyException e) {
						e.printStackTrace();
					}
					//m.add(temp);
					y++;
				}
				m.add(temp);
				x++;
			}
			int yDim = m.size();
			for (int j = 0; j < yDim; j++){
				Vector<Circle> temp = m.get(j);
				int xDim = temp.size();
				for (int k = 0; k < xDim; k++){
					
					Circle r_m1_c_m1 = null;
					Circle r_m1_c = null;
					Circle r_m1_c_p1 = null;
					
					Circle r_c_m1 = null;
					Circle r_c_p1 = null;
					
					Circle r_p1_c_m1 = null;
					Circle r_p1_c = null;
					Circle r_p1_c_p1 = null;
					
					
					try { r_m1_c_m1 = m.get(j-1).get(k-1); } catch (ArrayIndexOutOfBoundsException e) {}
					try { r_m1_c = m.get(j-1).get(k); } catch (ArrayIndexOutOfBoundsException e) {}
					try { r_m1_c_p1 = m.get(j-1).get(k+1); } catch (ArrayIndexOutOfBoundsException e) {}
					
					try { r_c_m1 = m.get(j).get(k-1); } catch (ArrayIndexOutOfBoundsException e) {}
					try { r_c_p1 = m.get(j).get(k+1); } catch (ArrayIndexOutOfBoundsException e) {}
					
					try { r_p1_c_m1 = m.get(j+1).get(k-1); } catch (ArrayIndexOutOfBoundsException e) {}
					try { r_p1_c = m.get(j+1).get(k); } catch (ArrayIndexOutOfBoundsException e) {}
					try { r_p1_c_p1 = m.get(j+1).get(k+1); } catch (ArrayIndexOutOfBoundsException e) {}
					
					Vertex vert = temp.get(k);
					
					try { 
						grid.addEdge(vert, r_m1_c_m1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), "", 1, null, true); 
						//grid.addEdge(r_m1_c_m1, vert, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), "", 1, null, true);
						//System.out.println("Edge from " + vert + " to " + r_m1_c_m1);
					} catch (MyException e){ }
					try { 
						grid.addEdge(vert, r_m1_c, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true);
						//grid.addEdge(r_m1_c, vert, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true); 
						//System.out.println("Edge from " + vert + " to " + r_m1_c);
					} catch (MyException e){ }
					try { 
						grid.addEdge(vert, r_m1_c_p1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), "", 1, null, true);
						//grid.addEdge(r_m1_c_p1, vert, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), "", 1, null, true);
						//System.out.println("Edge from " + vert + " to " + r_m1_c_p1);
					} catch (MyException e){ }
					
					try { 
						grid.addEdge(vert, r_c_m1, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true);
						//grid.addEdge(r_c_m1, vert, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true);
						//System.out.println("Edge from " + vert + " to " + r_c_m1);
					} catch (MyException e){ }
					try { 
						grid.addEdge(vert, r_c_p1, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true); 
						//grid.addEdge(r_c_p1, vert, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true);
						//System.out.println("Edge from " + vert + " to " + r_c_p1);
					} catch (MyException e){ }
					
					try {
						grid.addEdge(vert, r_p1_c_m1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), "", 1, null, true);
						//grid.addEdge(r_p1_c_m1, vert, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), "", 1, null, true); 
						//System.out.println("Edge from " + vert + " to " + r_p1_c_m1);
					} catch (MyException e){ }
					try { 
						grid.addEdge(vert, r_p1_c, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true); 
						//grid.addEdge(r_p1_c, vert, Color.GREEN, Color.YELLOW, 1.0f, "", 1, null, true); 
						//System.out.println("Edge from " + vert + " to " + r_p1_c);
					} catch (MyException e){ }
					try { 
						grid.addEdge(vert, r_p1_c_p1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), "", 1, null, true); 
						//grid.addEdge(r_p1_c_p1, vert, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), "", 1, null, true); 
						//System.out.println("Edge from " + vert + " to " + r_p1_c_p1);
					} catch (MyException e){ }

					
				}
			}
			
		}
		//System.out.println("MakeGrid: Gird vertex count = " + grid.getVertexCount() + " | edgeCount = " + grid.getEdgeCount());
	}
	
/*	private void makeGrid(){
		float x = drawAreaDimension.getX() / squareSize;
		float y = drawAreaDimension.getY() / squareSize;
		
		int xDim = Math.round(x);
		int yDim = Math.round(y);
		
		grid = new MultiDirectedGraph();
		
		int id = 0;
		for (int i = 0; i < yDim; i++)
			for (int j = 0; j < xDim; j++){
				try {
					grid.addVertex(new Circle(id++, ""+i+"_"+j,Color.BLACK, Color.ORANGE, (int)(j * squareSize), (int)(i * squareSize), 0, 3.0f));
				} catch (MyException e) {
					e.printStackTrace();
				}
			}
		
		id = -1;
		//System.out.println("Grid under construction");
		for (int i = 0; i < yDim; i++){
			for (int j = 0; j < xDim; j++){
				
				id++;
				
				Vertex v = grid.getVertexById(i * xDim + j);
				
				//===============================first "row"
				if (i == 0){
					//first vertex of the first row
					if (j == 0){
						Vertex rowCol_p1 = grid.getVertexById(i * xDim + (j+1));
						Vertex row_p1Col = grid.getVertexById((i+1) * xDim + j);
						Vertex row_p1Col_p1 = grid.getVertexById((i+1) * xDim + (j+1));
						try{
							grid.addEdge(v, rowCol_p1, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
							grid.addEdge(v, row_p1Col, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
							grid.addEdge(v, row_p1Col_p1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), 1, null, true);
						}catch (MyException e){
							System.err.println(e.getMessage());
						}
						continue;
					}
					
					//last vertex of the first row
					if (j == xDim - 1){
						Vertex rowCol_m1 = grid.getVertexById(i * xDim + (j-1));
						Vertex row_p1Col_m1 = grid.getVertexById((i+1) * xDim + (j-1));
						Vertex row_p1Col = grid.getVertexById((i+1) * xDim + j);
						try{
							grid.addEdge(v, rowCol_m1, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
							grid.addEdge(v, row_p1Col_m1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), 1, null, true);
							grid.addEdge(v, row_p1Col, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
						}catch (MyException e){
							System.err.println(e.getMessage());
						}
						continue;
					}
					
					//the vertices on the first row between the first and the last vertex
					Vertex rowCol_m1 = grid.getVertexById(i * xDim + (j-1));
					Vertex rowCol_p1 = grid.getVertexById(i * xDim + (j+1));
					
					Vertex row_p1Col_m1 = grid.getVertexById((i+1) * xDim + (j-1));
					Vertex row_p1Col = grid.getVertexById((i+1) * xDim + j);
					Vertex row_p1Col_p1 = grid.getVertexById((i+1) * xDim + (j+1));
					
					try{
						grid.addEdge(v, rowCol_m1, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
						grid.addEdge(v, rowCol_p1, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
						
						grid.addEdge(v, row_p1Col_m1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), 1, null, true);
						grid.addEdge(v, row_p1Col, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
						grid.addEdge(v, row_p1Col_p1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), 1, null, true);
					} catch (MyException e) {
						System.err.println(e.getMessage());
					}
					continue;
				}
				
				//============================last "row"
				if (i == yDim -1){
					//first vertex of the last row
					if (j == 0){
						Vertex row_m1Col = grid.getVertexById((i-1) * xDim + j);
						Vertex row_m1Col_p1 = grid.getVertexById((i-1) * xDim + (j+1));
						
						Vertex rowCol_p1 = grid.getVertexById(i * xDim + (j+1));
						
						try{
							grid.addEdge(v, row_m1Col, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
							grid.addEdge(v, row_m1Col_p1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), 1, null, true);
							grid.addEdge(v, rowCol_p1, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
						} catch (MyException e) {
							System.err.println(e.getMessage());
						}
						
						continue;
						
					}
					
					//last vertex of the last row
					if (j == xDim - 1){
						Vertex row_m1Col_m1 = grid.getVertexById((i-1) * xDim + (j-1));
						Vertex row_m1Col = grid.getVertexById((i-1) * xDim + j);
						Vertex rowCol_m1 = grid.getVertexById(i * xDim + (j-1));
						try {
							grid.addEdge(v, row_m1Col_m1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), 1, null, true);
							grid.addEdge(v, row_m1Col, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
							grid.addEdge(v, rowCol_m1, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
						} catch (MyException e) {
							System.err.println(e.getMessage());
						}
						continue;
					}
					
					//the vertices on the last row between the first and the last vertex
					Vertex row_m1Col_m1 = grid.getVertexById((i-1) * xDim + (j-1));
					Vertex row_m1Col = grid.getVertexById((i-1) * xDim + j);
					Vertex row_m1Col_p1 = grid.getVertexById((i-1) * xDim + (j+1));
					
					Vertex rowCol_m1 = grid.getVertexById(i * xDim + (j-1));
					Vertex rowCol_p1 = grid.getVertexById(i * xDim + (j+1));
					
					try {
						grid.addEdge(v, row_m1Col_m1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), 1, null, true);
						grid.addEdge(v, row_m1Col, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
						grid.addEdge(v, row_m1Col_p1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), 1, null, true);
						
						grid.addEdge(v, rowCol_m1, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
						grid.addEdge(v, rowCol_p1, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
					} catch (MyException e) {
						System.err.println(e.getMessage());
					}
					
					continue;
				}
				
				//===============================first "column"
				if (j == 0){
					continue;
				}
				
				//===============================last column
				if (j == xDim - 1){
					continue;
				}
				
				Vertex row_m1Col_m1 = grid.getVertexById((i-1) * xDim + (j-1));
				Vertex row_m1Col = grid.getVertexById((i-1) * xDim + j);
				Vertex row_m1Col_p1 = grid.getVertexById((i-1) * xDim + (j+1));
				
				Vertex rowCol_m1 = grid.getVertexById(i * xDim + (j-1));
				Vertex rowCol_p1 = grid.getVertexById(i * xDim + (j+1));
				
				Vertex row_p1Col_m1 = grid.getVertexById((i+1) * xDim + (j-1));
				Vertex row_p1Col = grid.getVertexById((i+1) * xDim + j);
				Vertex row_p1Col_p1 = grid.getVertexById((i+1) * xDim + (j+1));
				
				try {
					grid.addEdge(v, row_m1Col_m1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), 1, null, true);
					grid.addEdge(v, row_m1Col, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
					grid.addEdge(v, row_m1Col_p1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), 1, null, true);
					
					grid.addEdge(v, rowCol_m1, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
					grid.addEdge(v, rowCol_p1, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
					
					grid.addEdge(v, row_p1Col_m1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), 1, null, true);
					grid.addEdge(v, row_p1Col, Color.GREEN, Color.YELLOW, 1.0f, 1, null, true);
					grid.addEdge(v, row_p1Col_p1, Color.GREEN, Color.YELLOW, (float) Math.sqrt(2), 1, null, true);
				} catch (MyException e) {
					System.err.println(e.getMessage());
				}
				
			}
		}
		//System.out.println("Grid completed");
	}
	*/
	private void calculateSquareSize(){
		for (int i = 0; i < graph.getVertexCount(); i++){
		
			Vertex v = graph.getVertexByIndex(i);
			
			int width = 2 * (v.getCenter().getX() - v.getLeft()); 
			int height = 2 * (v.getCenter().getY() - v.getTop());
			
			int min = Math.min(width, height);
			
			squareSize = Math.min(min, squareSize);
		}
		squareSize/=2;
	}

	private boolean collinear(Vector2D p1,Vector2D p2, Vector2D p3){
		int x1 = p1.getX(), y1 = p1.getY();
		int x2 = p2.getX(), y2 = p2.getY();
		int x3 = p3.getX(), y3 = p3.getY();
		
		return (
			(x1*y2-x1*y3 + x2*y3-x2*y1 + x3*y1-x3*y2) == 0 
		);
	}

	private boolean intersects(Circle gridVertex, Vertex v){
		if (v instanceof Ellipse){
			Ellipse e = (Ellipse) v;
			//using the canonical equation of the ellipse
			
			double a = Math.pow(gridVertex.getCenter().getX() - e.getCenter().getX(), 2) / Math.pow(e.getWidth() / 2, 2);
			double b = Math.pow(gridVertex.getCenter().getY() - e.getCenter().getY(), 2) / Math.pow(e.getHeight() / 2, 2);
			
			return (a + b <= 1.5);
			
		}else if (v instanceof Circle){
			Circle c = (Circle) v;
			//using the equation of the circle
			//(x-c.x)^2 + (y-c.y)^2 <= r^2
			
			double dist = Math.sqrt(
					Math.pow(gridVertex.getCenter().getX() - v.getCenter().getX(), 2) +
					Math.pow(gridVertex.getCenter().getY() - v.getCenter().getY(), 2));
			return (dist <= c.getRadius() + 10);	//10 pixel cheat
			
		}else if (v instanceof Rectangle){
			Rectangle r = (Rectangle) v;
			// X & Y axis check
			return (
					(r.getLeft() <= gridVertex.getLeft())
					&& (r.getLeft() + r.getWidth() + squareSize / 5 >= gridVertex.getLeft() + gridVertex.getRadius() * 2)
					&& (r.getTop() <= gridVertex.getTop())
					&& (r.getTop() + r.getHeight() + squareSize / 5 >= gridVertex.getTop() + gridVertex.getRadius() * 2)
					);
		}
		return false;
	}
	
	/**
	 * Deletes occupied points by vertices
	 */
	private void disableCoveredPoints(){
		int disabled = 0;
		int total = 0;
		//for (int i = 0; i < grid.getVertexCount(); i++){
		//	grid.getVertexByIndex(i).setMarked(false);
		//}
		for (int i = 0; i < grid.getVertexCount(); i++){
			Circle gridV = (Circle) grid.getVertexByIndex(i);
			for (int j = 0; j < graph.getVertexCount(); j++){
				Vertex v = graph.getVertexByIndex(j);
				if (intersects(gridV, v)){
					//gridV.setMarked(true);
					try {
						grid.removeVertex(gridV);
					} catch (MyException e) {
						e.printStackTrace();
					}
					disabled++;
				}
				/*else if (v.out != null)
					for (int k = 0; k < v.out.size(); k++){
						Edge e = v.out.get(k);
						if (e.getPoints() != null){
							for (int l = 0; l < e.getPoints().size(); l++){
								Vector2D p = e.getPoints().get(l);
								double eps = 0.01;
								if ((Math.abs(p.getX() - gridV.getCenter().getX()) <= eps) || (Math.abs(p.getY() - gridV.getCenter().getY()) <= eps))
									gridV.setMarked(true);
							}
						}
					}
				*/
				total++;
			}
		}
		//System.out.println("Total # of disabled points " + disabled + " out of " + total);
	}
	
	/**
	 * Returns the closest vertex on the grid to the center of v
	 * @param v the vertex 
	 * @return the closest vertex on the grid to the center of v
	 */
	private Vertex closestToCenter(Vertex v){
		
		Vertex res = null;
		
/*		for (int i = 0; i < grid.getVertexCount(); i++)
			if (grid.getVertexByIndex(i).getMarked() == false){
				res = grid.getVertexByIndex(i);
				break;
			}
*/		
		//only pick verts that are not disabled
		for (int i = 0; i < grid.getVertexCount(); i++){
			Vertex gridV = grid.getVertexByIndex(i);
			//if (gridV.getMarked())
			//	continue;
			double oldDist = Math.sqrt(
							Math.pow(res.getCenter().getX() - v.getCenter().getX(), 2) + 
							Math.pow(res.getCenter().getY() - v.getCenter().getY(), 2)
						);
			double newDist = Math.sqrt(
							Math.pow(gridV.getCenter().getX() - v.getCenter().getX(), 2) + 
							Math.pow(gridV.getCenter().getY() - v.getCenter().getY(), 2)
						);
			
			if (newDist < oldDist)
				res = gridV;
			
		}
		//res.setMarked(true);
		return res;
	}
	
	private double distance(Vertex gridV, Vertex v){
		return Math.sqrt(
				Math.pow(gridV.getCenter().getX() - v.getCenter().getX(), 2) + 
				Math.pow(gridV.getCenter().getY() - v.getCenter().getY(), 2)
			);
	}
	
	private double distance(Vertex start, Vector2D p){
		return Math.sqrt(
				Math.pow(start.getCenter().getX() - p.getX(), 2) + 
				Math.pow(start.getCenter().getY() - p.getY(), 2)
			);
	}
	private Vector2D getVertexSize(Vertex v){
		Vector2D result = new Vector2D(0, 0);
		if (v instanceof Circle){
			result.setX(Math.round(((Circle) v).getRadius() * 2));
			result.setY(result.getX());
		} else if (v instanceof Ellipse){
			result.setX(((Ellipse) v).getWidth());
			result.setY(((Ellipse) v).getHeight());
		} else if (v instanceof Rectangle){
			result.setX(((Rectangle) v).getWidth());
			result.setY(((Rectangle) v).getHeight());
		}
		
		return result;
	}
	
	
	private Vertex oC2C(Vertex v1, Vertex v2, boolean firstIsUserVertex){
		if (!firstIsUserVertex)
			return optimallyCloseToCenter(v1, v2);

		String strUserVertexId = v1.getId()+"";
		
		
		Vector<Circle> neighborhood = new Vector<Circle>();
		
		for (int i = 0; i < grid.getVertexCount(); i++){
			Circle gridV = (Circle) grid.getVertexById(i);
			if (gridV.getText().startsWith(strUserVertexId))
				neighborhood.add(gridV);
		}
		//get the vertex that is the closest to the destination vertex
		Circle result = null;
		if (neighborhood.size() == 0){
			return optimallyCloseToCenter(v1, v2);
		}else
			result = neighborhood.get(0);
		double distToDestination = distance(result, v2);
		for (int i = 1; i < neighborhood.size(); i++){
			double alt = distance(neighborhood.get(i), v2);
			if (alt < distToDestination){
				result = neighborhood.get(i);
				distToDestination = alt;
			}
		}
		return result;
	}
	
	private Vertex optimallyCloseToCenter(Vertex v, Vertex destination){
		//get the grid vertices that are close to the center of the vertex
		Vector<Circle> neighborhood = new Vector<Circle>();
		
		for (int i = 0; i < grid.getVertexCount(); i++){
			Circle gridV = (Circle) grid.getVertexById(i);
			//if (gridV.getMarked())
			//	continue;
			double alt = distance(v, gridV);
			// fix?
			if ((alt < squareSize * 2 + (Math.max(getVertexSize(v).getX() / 2, getVertexSize(v).getY() / 2))))// && (alt / squareSize < 0.1))
				neighborhood.add(gridV);
		}
		
		//get the vertex that is the closest to the destination vertex
		Circle result = null;
		if (neighborhood.size() == 0){
			result = (Circle) closestToCenter(v);
			System.out.println("No neighborhood found for " + v + " to get to " + destination);
		}else
			result = neighborhood.get(0);
		double distToDestination = distance(result, destination);
		for (int i = 1; i < neighborhood.size(); i++){
			double alt = distance(neighborhood.get(i), destination);
			if (alt < distToDestination){
				result = neighborhood.get(i);
				distToDestination = alt;
			}
		}
		//result.setMarked(true);
		return result;
	}
	
	private Vector<Vector2D> splitLine(Vector2D s, Vector2D d){
		Vector2D aux = null;
		
		if (s.getX() > d.getX()){
			aux = s;
			s = d;
			d = aux;
		}
		
		Vector<Vector2D> newPoints = new Vector<Vector2D>();
		int pointCount = (int) (squareSize);
		float step = 0.0f; 
		if (d.getX() - s.getX() == 0){	//same x position
			if (d.getY() - s.getY() == 0){	//same y position
				//they are on the same position => can't calculate the position of the points
			}else{
				float prev = 0.0f;
				if (d.getY() > s.getY()){
					step = (float)(Math.abs(d.getY() - s.getY())) / (float)(pointCount + 1);
					prev = s.getY();
				}
				else{
					step = (float)(Math.abs(s.getY() - d.getY())) / (float)(pointCount + 1);
					prev = d.getY();
				}
				for (int i = 0; i < pointCount; i++){
					float Y = prev + step ;
					
					float a = d.getX() - s.getX();
					float b = d.getY() - s.getY();
					float AperB = a / b;
					float c = Y - s.getY();
					float AperBszorC = AperB * c;
					
					float X = AperBszorC + s.getX();
					//float X = (d.getX()-s.getX()) / (d.getY()-s.getY()) * (Y - s.getY()) + s.getX();
					newPoints.add(new Vector2D((int)X, (int)Y));
					prev = Y;
				}
			}
		}else{
			step = (float)(d.getX() - s.getX()) / (float)(pointCount + 1);
			float prev = s.getX();
			for (int i = 0; i < pointCount; i++){
				float X = prev + step;
				
				float a = d.getY() - s.getY();
				float b = d.getX() - s.getX();
				float aPerb = a/b;
				float c = X - s.getX();
				float AperBszorC = aPerb*c;
				
				float Y = AperBszorC + s.getY();
				
				newPoints.add(new Vector2D((int)X, (int)Y));
				prev = X;
			}
		}

		//System.out.println(newPoints);
		return newPoints;
	}
/*	
	private Vector<Vector2D> splitLine2(Vector2D s, Vector2D d){
		Vector2D aux = null;
		
		if (s.getX() > d.getX()){
			aux = s;
			s = d;
			d = aux;
		}
		
		//decide which formula to use
		Vector<Vector2D> newPoints = new Vector<Vector2D>();
		int pointCount = (int) (squareSize);
		
		float step = 0.0f; 
		if (d.getX() - s.getX() == 0){	//same x position
			if (d.getY() - s.getY() == 0){	//same y position
				//they are on the same position => can't calculate the position of the points
			}else{
				float prev = 0.0f;
				if (d.getY() > s.getY()){
					step = (Math.abs(d.getY() - s.getY())) / (pointCount + 1);
					prev = s.getY();
				}
				else{
					step = (Math.abs(s.getY() - d.getY())) / (pointCount + 1);
					prev = d.getY();
				}
				for (int i = 0; i < pointCount; i++){
					float Y = prev + step ;
					
					float a = d.getX() - s.getX();
					float b = d.getY() - s.getY();
					float AperB = a / b;
					float c = Y - s.getY();
					float AperBszorC = AperB * c;
					
					float X = AperBszorC + s.getX();
					//float X = (d.getX()-s.getX()) / (d.getY()-s.getY()) * (Y - s.getY()) + s.getX();
					newPoints.add(new Vector2D((int)X, (int)Y));
					prev = Y;
				}
				
//				if (d.getY() < s.getY())
//					Collections.reverse(newPoints);
			}
		}else{
			step = (Math.abs(d.getX() - s.getX())) / (pointCount + 1);
			float prev = s.getX();
			for (int i = 0; i < pointCount; i++){
				float X = prev + step;
				
				float a = d.getY() - s.getY();
				float b = d.getX() - s.getX();
				float aPerb = a/b;
				float c = X - s.getX();
				float AperBszorC = aPerb*c;
				
				float Y = AperBszorC + s.getY();
				
				Vector2D p = new Vector2D((int)X, (int)Y);
				if (!newPoints.contains(p))
					newPoints.add(p);
				prev = X;
			}
			if (d.getX() > s.getX())
				Collections.reverse(newPoints);
		}
		return newPoints;
	}
*/
	
	public boolean applyLayout(){
		if (smooth)
			makeGrids();
		else
			makeGridsRectangular();
		//disableCoveredPoints();
		//System.out.println("After elimination: Gird vertex count = " + grid.getVertexCount() + " | edgeCount = " + grid.getEdgeCount());

		//for each vertex from the graph
		for (int i = 0; i < graph.getVertexCount(); i++){
			Vertex v = graph.getVertexByIndex(i);
			
			//for each outgoing edge of the vertex
			for (int j = 0; j < v.getOutDegree(); j++){
				Edge e = v.out.get(j);
				if (distance(v, e.getEnd()) < squareSize * 2){
					e.straightenPoints(1);
					continue;
				}
				Vector<Vector2D> points = new Vector<Vector2D>();
				
				//reverse the edge if needed to correct the bad positioning
				boolean edgeReversed = false;
				Vector2D d = e.getEnd().getCenter();
				Vector2D s = v.getCenter();
				if (
						(s.getX() == d.getX()) && (s.getY() > d.getY()) ||
						((s.getY() == d.getY()) && (s.getX() > d.getX())) ||
						((s.getX() > d.getX()) && (s.getY() < d.getY())) ||
						((s.getX() > d.getX()) && (s.getY() > d.getY()))
					){
					e.reverse();
					edgeReversed = true;
					//System.out.println(e + " has been reversed");
				}
				
				Vertex startGrid = null, endGrid = null;
				//get a point that is closest to v and points in the direction of e.getEnd() 
				if (!edgeReversed){
					startGrid = optimallyCloseToCenter(v, e.getEnd());
					points.add(startGrid.getCenter());	//add the starting point
					//get a point that is closest to e.getEnd() and has the direction of v
					endGrid   = optimallyCloseToCenter(e.getEnd(), v);
				}else{
					startGrid = optimallyCloseToCenter(e.getStart(), v);
					points.add(startGrid.getCenter());	//add the starting point
					//get a point that is closest to e.getEnd() and has the direction of v
					endGrid   = optimallyCloseToCenter(v, e.getStart());
				}
				if (e.getStart().getText().equals("Zalau") && (e.getEnd().getText().equals("Iasi"))){
					System.out.println("now");
				}
				//create & divide the straight line segment into smaller line segments
				Vector<Vector2D>  splitPoints = splitLine(startGrid.getCenter(), endGrid.getCenter());
				
				
				//now check if one of those line segments intersects a vertex from the graph
				Vertex inter1 = startGrid;
				Vertex inter2 = null;
				//for each segment point
				for (int p = 0; p < splitPoints.size(); p++){
					Vector2D segPoint = splitPoints.get(p);

					//for each vertex form the users' graph
					for (int k = 0; k < graph.getVertexCount(); k++){
						Vertex vert = graph.getVertexByIndex(k);
						
						
						//check if they intersect
						if (intersects(
								new Circle(0, "", Color.BLACK, Color.BLACK, segPoint.getX(), segPoint.getY(), 1, 1.0f),
								vert))/* && (vert != e.getStart()) && (vert != e.getEnd())*/{
							
							//if they do, then select a point close to the intersection vertex that points to the last vertex
							inter1 = oC2C(vert, inter1, true);
							//BACKUP: Vertex start1 = optimallyCloseToCenter(inter, last);
							//points.add(start1.getCenter());	//add it
							//start1.setHighLight(true);
							//select another close point to the inters. vertex that points to the destination vertex
							inter2   = oC2C(vert, endGrid, true);
							//end1.setHighLight(true);
							//find the shortest path between these points
							ShortestPath sp = null;
							try {
								sp = grid.getShortestPath(inter1, inter2, true);
							} catch (ShortestPathException e1) {
								e1.printStackTrace();
							}
							
							Vector<Vertex> res = sp.getResultPath();

							if ((res != null) && (res.size() > 0)){
								for (int m = 0; m < res.size(); m++){
									Vector2D newPoint = res.get(m).getCenter();
									if (!points.contains(newPoint)){
										points.add(newPoint);
										res.get(m).setMarked(true);
										/*try {
											grid.removeVertex(res.get(m));
										} catch (MyException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}*/
										//System.out.println("Point " + newPoint + " for the edge " + e);
									}
								}
								//System.out.println("found a path for " + e + " around " + inter + " with " + res.size() + " points");
								
							}else{
								System.out.println("no path to get from " + v + " to " + e.getEnd() + " around " + vert + "with points " + inter1 +  " -> " + inter2);
								points.add(inter1.getCenter());
								points.add(inter2.getCenter());
							}
							inter1 = inter2;
							break;
						}
					}
				}
				if (!points.contains(endGrid.getCenter()))
						points.add(endGrid.getCenter());
				
				Vector<GridPoint> grPoints = new Vector<GridPoint>();
				
				for (int k = 0; k < points.size(); k++){
					GridPoint gp = new GridPoint(points.get(k), distance(v, points.get(k)));
					grPoints.add(gp);
				}
				
				Collections.sort(grPoints, new GridPointComparator());
				
				//Vector<Vector2D> correctedPoints = new Vector<Vector2D>();
				for (int k = 0; k <grPoints.size(); k++)
					points.set(k, grPoints.get(k).getPoint());
				
				//remove points that will go "backwards" from the destination and look ugly
				if (!smooth){
					while (true){
							boolean finished = true;
							for (int k = points.size() - 1; k > 1; k--){
								if (distance(e.getEnd(), points.get(k - 1)) <= distance(e.getEnd(), points.get(k))){
									System.out.println("Removing the " + k + "th element [" + points.get(k) + "] for " + e);
									points.remove(k);
									finished = false;
									break;	//restart
								}
									
							}
							if (finished)
								break;
					}
				
					//remove collinear points
					int k = 1;
					int last = 1;
					while (k < points.size() - 3){
						Vector2D p1 = points.get(k);
						Vector2D p2 = points.get(k + 1);
						Vector2D p3 = points.get(k + 2);
						if (collinear(p1, p2, p3)){
							points.remove(k+2);
							points.remove(k+1);
							//points.remove(k);
							k++;
						}else
							k+=2;
					}
				}
				
				e.setPoints(points);
				if (edgeReversed)
					e.reverse();
				edgeReversed = false;
				/*
				ShortestPath sp = null;
				try {
					sp = grid.getShortestPath(startGrid, endGrid, true);
				} catch (ShortestPathException e1) {
					e1.printStackTrace();
				}
				Vector<Vertex> res = sp.getResultPath();
				//Vector<Vector2D> points = new Vector<Vector2D>();
				
				int found = 0, notFound = 0;
				
				Vector2D last = null;
				if (res != null && res.size() > 0){
					for (int k = 0; k < res.size(); k++){
						Vector2D p = res.get(k).getCenter();
						//make sure the first point is added
						if (points.size() == 0){
							points.add(p);
							//res.get(k).setMarked(true);
							last = points.get(k);
							continue;
						}
						//don't add unnecessary custom drawing points
						//only add a new point when the direction changes
						
						
						
						if ((Math.abs(p.getX() - last.getX()) < 0.1))
							continue;
						
						if ((Math.abs(p.getY() - last.getY()) < 0.1))
							continue;
												
						points.add(p);
						res.get(k).setMarked(true);
						last = p;
						
					}
					found++;
				}else{
					points.add(startGrid.getCenter());
					points.add(endGrid.getCenter());
					notFound++;
				}
				e.setPoints(points);
				System.out.println("Found sp for " + found + " , not found for " + notFound);
				*/
			}
		}
		return true;
	}
	
	public void draw(Graphics g){
		if (grid == null)
			return;
		disableCoveredPoints();
		Color clr = g.getColor();
		for (int i = 0; i < grid.getVertexCount(); i++){
			Vertex v = grid.getVertexByIndex(i);
			for (int j = 0; j < v.getOutDegree(); j++){
				Edge currentEdge = v.out.get(j);
				currentEdge.draw(g);
			}
			v.draw(g);
		}
		g.setColor(clr);
	}
	
}

