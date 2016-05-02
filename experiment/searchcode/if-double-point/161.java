package model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import controller.AppController;
/**
 * Represents an Edge between two {@link Vertex}s.
 * @author Daroczi Krisztian-Zoltan
 * @version 1.0
 */
public class Edge {
	/**
	 * The id of the edge
	 */
	protected int id;
	/**
	 * The color of the edge
	 */
	protected Color color;
	/**
	 * The color used to highlight the edge
	 */
	protected Color highlightColor;
	/**
	 * A bit to decide if the edge needs to be highlighted
	 */
	protected boolean highLight;
	
	/**
	 * Used to highlight the edge when it's dragged by the mouse
	 */
	protected boolean moving;
	
	/**
	 * The source vertex of the edge
	 */
	protected Vertex start;
	/**
	 * The destination vertex of the edge
	 */
	protected Vertex end;
	/**
	 * The cost to get from the starting vertex to the destination vertex
	 */
	protected float cost;
	
	/**
	 * A label assigned to this edge
	 */
	protected String label;
	
	/**
	 * The thickness of the line that represents the edge. [1-5]
	 */
	protected int thickness;
	
	/**
	 * The base thickness of the edge, it's used to modify the draw of the thickness when zooming in or out
	 */
	private int baseThickness;
	/**
	 * The size of the custom drawing points in pixels
	 */
	private int pointSize;
	/**
	 * The basic size of the points, it's used to modify the draw of the pointSize when zooming in or out
	 */
	private int basePointSize;
	/**
	 * The scale of the zoom level
	 */
	protected int zoomPercent;
	/**
	 * A vector of custom intermediary points on the edge. These points can be moved.
	 */
	private Vector<Vector2D> points;
	
	private Vector<Map<Integer, Vector2D>> pointsZoom; 
	
	/**
	 * Determines if the edge cost is calculated based on the vertices positions
	 */
	private boolean realDistances;
	
	/**
	 * Used to determine the orientation of the translation of the edge points
	 */
	public static final int LEFT = 0;
	public static final int TOP = 1;
	
	/**
	 * Creates a new Edge linking two {@link Vertex}s.
	 * @param cost the cost to get from the starting vertex to the destination vertex(works also with negative values)
	 * @param color the color of the edge
	 * @param highLightColor the color of the edge when it's highlighted
	 * @param start the starting vertex of the edge
	 * @param end the destination vertex of the edge
	 * @param thickness the thickness of the edge(between [1-5])
	 * @param points the set of additional drawing points of the edge
	 * @since Version 1.0
	 */
	public Edge(float cost, Color color, Color highLightColor, Vertex start, Vertex end, int thickness, Vector<Vector2D> points, boolean realDistances, int zoom){
		this.id = -1;
		this.cost = cost;
		this.label = "";
		this.color = color;
		this.highlightColor = highLightColor;
		this.highLight = false;
		this.start = start;
		this.end = end;
		this.zoomPercent = 100;
		
		if (thickness > 5)
			this.thickness = 5;
		else if (thickness < 1)
			this.thickness = 1;
		else
			this.thickness = thickness;
		
		this.baseThickness = thickness;
		
		if (zoom != 100){
			this.zoomPercent = zoom;
			for (int i = 0; i < points.size(); i++){
				int newX = 100 * points.get(i).getX() / zoom;
				int newY = 100 * points.get(i).getY() / zoom;
				points.get(i).setX(newX);
				points.get(i).setY(newY);
			}
		}
		this.setPoints(points);

		this.setPointSize(this.thickness * 10);
		
		this.basePointSize = this.getPointSize();
		
		this.realDistances = realDistances;
		this.refreshPointsZoomLevel();
	}
	
	/**
	 * Removes itself from the out- and in neighbor list of the start and end vertices.
	 * It also sets them to be isolated, if needed.
	 * @since Version 1.0
	 */
	public void finalize(){
		//check if the ending vertex of the edge will be isolated if we remove this edge
		if ((this.getEnd().getInDegree() == 1) && (this.getEnd().getOutDegree() == 0))
			this.getEnd().setIsolated(true);
		
		
		//check if the starting vertex will be isolated if we remove this edge
		if ((this.getStart().getOutDegree() == 1) && (this.getStart().getInDegree() == 0))
			this.getStart().setIsolated(true);
	}
	
	/**
	 * Draws the edge(its custom points, and cost) on the {@link Graphics}.
	 * @param g the graphics to draw on
	 * @since Version 1.0
	 */
	public void draw(Graphics g){
		
		if (this.realDistances)
			this.updateDistance();
		
		Graphics2D g2 = (Graphics2D)g;

		Color oldColor = g2.getColor();
		Vector2D startCenter = start.getCenter();
		Vector2D endCenter = end.getCenter();
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(this.thickness*2.0f));
		if (moving)
			g2.setColor(this.highlightColor);
		else if (highLight)
			g2.setColor(Color.GREEN);
		else
			g2.setColor(this.color);
		
		if ((this.points == null) || (this.points.size() == 0))
			g2.draw(new Line2D.Float(startCenter.getX(), startCenter.getY(), endCenter.getX(), endCenter.getY()));
		
		else{
			//draw intermediary points
			Vector2D x = new Vector2D(0, 0);
			Vector2D y = new Vector2D(0, 0);
			x = startCenter;
			//y = this.points.get(0);
			y = this.pointsZoom.get(0).get(this.zoomPercent);
			
			//draw the first line
			g2.draw(new Line2D.Float(x.getX(), x.getY(), y.getX(), y.getY()));
			
			for (int i = 1; i < this.points.size(); i++){
				//x = this.points.get(i - 1);
				//y = this.points.get(i);
				
				x = this.pointsZoom.get(i - 1).get(this.zoomPercent);
				y = this.pointsZoom.get(i).get(this.zoomPercent);
				
				//draw the line segments between the first and last points
				g2.draw(new Line2D.Float(x.getX(), x.getY(), y.getX(), y.getY()));
				//now draw a small circle around each point
				g2.fillOval(x.getX() - getPointSize() / 2, x.getY() - getPointSize() / 2, getPointSize(), getPointSize());
				g2.fillOval(y.getX() - getPointSize() / 2, y.getY() - getPointSize() / 2, getPointSize(), getPointSize());
				//g2.fillRect(x.getX() - pointSize / 2, x.getY() - pointSize / 2, pointSize, pointSize);
				//g2.fillRect(y.getX() - pointSize / 2, y.getY() - pointSize / 2, pointSize, pointSize);
			}

			//draw the last line
			//x = this.points.get(this.points.size() - 1);
			x = this.pointsZoom.get(this.points.size() - 1).get(this.zoomPercent);
			y = endCenter;
			g2.draw(new Line2D.Float(x.getX(), x.getY(), y.getX(), y.getY()));
			if (this.points.size() == 1){
				//g2.fillRect(x.getX() - 5, x.getY() - 5, 10, 10);
				g2.fillOval(x.getX() - getPointSize() / 2, x.getY() - getPointSize() / 2, getPointSize(), getPointSize());
			}
		}
		
	
		//draw the arrows
		g2.setStroke(new BasicStroke(this.thickness*2.0f));
		if (moving)
			g.setColor(this.highlightColor);
		else if (highLight)
			g.setColor(Color.GREEN);
		else
			g.setColor(this.color);
		Vector<Vector2D> allPoints = new Vector<Vector2D>();
		allPoints.add(startCenter);
		if ((this.points != null) && (this.points.size() > 0))
			for (int i = 0; i < this.pointsZoom.size(); i++)
				allPoints.add(this.pointsZoom.get(i).get(this.zoomPercent));
		allPoints.add(endCenter);
		
		for (int i = 1; i < allPoints.size(); i++){
			Vector2D p0 = allPoints.get(i - 1);
			Vector2D pFinal = allPoints.get(i);
			//the mid-point
			Vector2D p1 = new Vector2D((p0.getX() + pFinal.getX())/2, (p0.getY() + pFinal.getY())/2);

			//calculate the direction formed by the points p0 and pFinal(or p1)
			
			double m = 0.0f;
			//the fictional quarter of a proper coordinate system(1,2,3,4)
			int n = -1;
			try{
				//m = (y.getX() - lastPointToHalf.getX())/(y.getY() - lastPointToHalf.getY());	//tangent
				float denom = p1.getX() - p0.getX();
				float nom = p1.getY() - p0.getY();
				//in this case:
				if ((nom >= 0) && (denom < 0))
					n = 1;
				else if ((nom < 0) && (denom >= 0))
					n = 2;
				else if ((nom >= 0) && (denom >=0))
					n = 4;
				else if ((nom < 0) && (denom < 0))
					n = 3;
				
				if ((denom == 0)&&(nom == 0))	// 0/0=NaN
					m = Float.NEGATIVE_INFINITY;
				else
					m = nom/denom;
			} catch (ArithmeticException e) {
				m = Float.POSITIVE_INFINITY;
			}

			double angle = Math.atan(m) * 180/Math.PI;
			if (m == Float.POSITIVE_INFINITY)
				angle = 90;
			else if (m == Float.NEGATIVE_INFINITY)
				angle = -90;
			//correcting the angle based on the quarter
			switch (n){
			case 1:
				angle += 180;
				break;
			case 2:
				break;
			case 3:
				angle += 180;
				break;
			case 4:	//ok as it is
				break;
			default:
				break;	
			}
			
			double rad = angle*Math.PI/180;

			
			//now use the perpendicular line to this line to create the triangle p1-perp1-perp2
			Vector2D perp1 = new Vector2D(0, 0);
			Vector2D perp2 = new Vector2D(0, 0);

			double theta = rad + Math.PI / 4;
			g2.rotate(theta);
			
			Vector2D newP1 = posAfterRotation(theta, p1);
			perp1.setY(newP1.getY() + this.getPointSize());
			perp1.setX(newP1.getX() - 2*this.getPointSize());
			
			perp2.setX(newP1.getX() - this.getPointSize());
			perp2.setY(newP1.getY() + 2*this.getPointSize());
			
			if (!AppController.debug){
				g2.draw(new Line2D.Float(newP1.getX(), newP1.getY(), perp1.getX(), perp1.getY()));
				g2.draw(new Line2D.Float(newP1.getX(), newP1.getY(), perp2.getX(), perp2.getY()));
			}
			g2.rotate(-theta);
			
		}
/*===================================================================================================================\
		+-------------------------+
		|Drawing the costs/labels |
		+-------------------------+
\===================================================================================================================*/
			//kiszamitani az edge hosszat pixelben, es a kozepehez irni(fole, vagy ala) + megnezni a kozepenel az edge szoget, es ugy forditani a szoveget
			g2.setColor(Color.BLACK);
			Vector2D x = new Vector2D(0, 0);
			Vector2D y = new Vector2D(0, 0);
			Vector2D textPos = new Vector2D(0, 0);
			//save the last interm. drawing point here when it reaches half the distance
			Vector2D lastPointToHalf = new Vector2D(0, 0);
			float edgeLength = 0.0f;
			//boolean hasOneMore = true;
			
			
			if ((this.points != null) && (this.points.size() > 0)){
				x = startCenter;
				y = this.pointsZoom.get(0).get(this.zoomPercent);
				
				//calculate thhe total length of the edge
				edgeLength += Math.sqrt(Math.pow((y.getX() - x.getX()), 2) + Math.pow((y.getY() - x.getY()), 2));
				for (int j = 1; j < this.points.size(); j++){
					//x = this.points.get(j - 1);
					//y = this.points.get(j);
					
					x = this.pointsZoom.get(j - 1).get(this.zoomPercent);
					y = this.pointsZoom.get(j).get(this.zoomPercent);
					
					//add up the distances between the intermediary drawing points
					edgeLength += Math.sqrt(Math.pow((y.getX() - x.getX()), 2) + Math.pow((y.getY() - x.getY()), 2));
				}
				x = this.pointsZoom.get(this.points.size() - 1).get(this.zoomPercent);
				y = endCenter;
				//add the last point too
				edgeLength += Math.sqrt(Math.pow((y.getX() - x.getX()), 2) + Math.pow((y.getY() - x.getY()), 2));
				
				edgeLength /= 2;
				
				//check where it reaches the half
				float edgeLength2 = 0.0f;
				
				
				x = startCenter;
				y = this.pointsZoom.get(0).get(this.zoomPercent);
				//save the index of the point, later we use it to get the next point from the vector
				int idx = -1;
				edgeLength2 += Math.sqrt(Math.pow((y.getX() - x.getX()), 2) + Math.pow((y.getY() - x.getY()), 2));
				if (edgeLength2 >= edgeLength){
					//check if we reached just with the first point
					lastPointToHalf = this.pointsZoom.get(0).get(this.zoomPercent);
				} else {
					for (int j = 1; j < this.points.size(); j++){
						x = this.pointsZoom.get(j - 1).get(this.zoomPercent);
						y = this.pointsZoom.get(j).get(this.zoomPercent);
						edgeLength2 += Math.sqrt(Math.pow((y.getX() - x.getX()), 2) + Math.pow((y.getY() - x.getY()), 2));
						lastPointToHalf = x;
						idx = j - 1;
						//keep adding up until we get to half the distance
						if (edgeLength2 >= edgeLength){
							break;
						}
					}
					//check if we breaked out, otherwise the last point to half is the last intermediary drawing point
					if (edgeLength2 < edgeLength)
						lastPointToHalf = this.pointsZoom.get(this.points.size() - 1).get(this.zoomPercent);
				}
				
				//at this point we should have the correct intermediary drawing point where to draw the cost
				
				//to get the orientation of the text we need the angle between this point and the very next one
				//we store the next point in y
				
				//check if our point is the last one from the points vector
				if (lastPointToHalf == this.pointsZoom.get(this.points.size() - 1).get(this.zoomPercent)){
					y = endCenter;
				}
				else{
					//it works even with the first point, because in that case y will be the first point from the vector(-1+1)
					y = this.pointsZoom.get(idx + 1).get(this.zoomPercent);
				}
			}else{
				//in case of no intermediary drawing point
				lastPointToHalf.setX((startCenter.getX() + endCenter.getX())/2);
				
				int min = Math.min(startCenter.getY(), endCenter.getY());
				int abs = Math.abs(startCenter.getY() - endCenter.getY());
				
				lastPointToHalf.setY(min + abs / 2);
				y = endCenter;
			}
			
			//lastPointToHalf and y(next point) are now set in both cases(with - & without additional drawing points) 
			
			//now get the direction of the line formed by lastPointToHalf and y
			double m = 0.0;
			//the fictional quarter of a proper coordinate system(1,2,3,4)
			int n = -1;
			try{
				//m = (y.getX() - lastPointToHalf.getX())/(y.getY() - lastPointToHalf.getY());	//tangent
				float denom = y.getX() - lastPointToHalf.getX();
				float nom = y.getY() - lastPointToHalf.getY();
				//in this case:
				if ((nom >= 0) && (denom < 0))
					n = 1;
				else if ((nom < 0) && (denom >= 0))
					n = 2;
				else if ((nom >= 0) && (denom >=0))
					n = 4;
				else if ((nom < 0) && (denom < 0))
					n = 3;
				if ((denom == 0)&&(nom == 0))	// 0/0=NaN
					m = Float.NEGATIVE_INFINITY;
				else
					m = nom/denom;
			} catch (ArithmeticException e) {
				m = Float.POSITIVE_INFINITY;
			}

			double angle = Math.atan(m) * 180/Math.PI;
			if (m == Float.POSITIVE_INFINITY)
				angle = 90;
			else if (m == Float.NEGATIVE_INFINITY)
				angle = -90;
			//correcting the angle based on the quarter
			switch (n){
			case 1:
				angle += 180;
				break;
			case 2:
				break;
			case 3:
				angle += 180;
				break;
			case 4:	//ok as it is
				break;
			default:
				break;	
			}
			
			double rad = angle*Math.PI/180;
			
			String strToDraw = "";
			if (!this.label.equals(""))
				strToDraw = this.label;
			else
				strToDraw = this.cost + "";
			
			//rotate the graphics
			if (rad != 0){
				g2.rotate(rad);
				int x1 = lastPointToHalf.getX();
				int y1 = lastPointToHalf.getY();
				
				textPos.setX((int) Math.round(Math.cos(-rad) * x1 - Math.sin(-rad)*y1));
				textPos.setY((int) Math.round(Math.sin(-rad) * x1 + Math.cos(-rad)*y1));
				

				
				if (!AppController.debug){
					g2.drawString(strToDraw, textPos.getX() ,textPos.getY());
					//g2.drawString(angle+"", textPos.getX() ,textPos.getY());
				}
				g2.rotate(-rad);

			}else
				if (!AppController.debug)
					g2.drawString(strToDraw, lastPointToHalf.getX() + 5, lastPointToHalf.getY());
	/*====================================================================================================================
			+-------------------------+
			|End of Drawing the costs |
			+-------------------------+
	====================================================================================================================*/

			
			
			//break;
		g2.setColor(oldColor);
		g2.setStroke(oldStroke);
	}

	public void updateDistance() {
		if (!this.realDistances)
			return;
		Vector2D startCenter = start.get100Center();
		Vector2D endCenter = end.get100Center();
		Vector<Vector2D> allPoints = new Vector<Vector2D>();
		allPoints.add(startCenter);
		if ((this.points != null) && (this.points.size() > 0))
			for (int i = 0; i < this.points.size(); i++)
				allPoints.add(this.pointsZoom.get(i).get(100));		//calculate for 100% distances
		allPoints.add(endCenter);
		float distance = 0.0f;
		
		for (int i = 1; i < allPoints.size(); i++){
			Vector2D p0 = allPoints.get(i - 1);
			Vector2D p1 = allPoints.get(i);
			distance += Math.sqrt(Math.pow((p1.getX() - p0.getX()), 2) + Math.pow((p1.getY() - p0.getY()), 2));
		}
		this.cost = distance;
	}

	/**
	 * Calculates the new position of a point after rotating the graphics with <i>theta</i> degrees
	 * @param theta the angle of rotation in radians
	 * @param point the point to convert
	 * @return a point with the new coordinates
	 * @since Version 1.0
	 */
	private Vector2D posAfterRotation(double theta, Vector2D point){
		int x = point.getX();
		int y = point.getY();
		Vector2D result = new Vector2D(0, 0);
		
		result.setX((int) Math.round(Math.cos(-theta) * x - Math.sin(-theta)*y));
		result.setY((int) Math.round(Math.sin(-theta) * x + Math.cos(-theta)*y));

		return result;
	}
	
	/**
	 * @return the id of the edge
	 * @since Version 1.0
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * @return the color of the edge
	 * @since Version 1.0
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color of the edge
	 * @param color the new {@link Color}
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @return the color used to highlight the edge
	 * @since Version 1.0
	 */
	public Color getHighlightColor() {
		return highlightColor;
	}

	/**
	 * Sets the color used to highlight the edge
	 * @param highlightColor the new {@link Color}
	 */
	public void setHighlightColor(Color highlightColor) {
		this.highlightColor = highlightColor;
	}

	/**
	 * @return true if the edge will be drawn as highlighted, false otherwise
	 * @since Version 1.0
	 */
	public boolean isHighLight() {
		return highLight;
	}

	/**
	 * Sets whether or not the edge should be drawn as highlighted
	 * @param highLight the boolean value to set
	 * @since Version 1.0
	 */
	public void setHighLight(boolean highLight) {
		this.highLight = highLight;
	}

	public boolean isMoving() {
		return moving;
	}

	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	/**
	 * @return the starting {@link Vertex} of the edge
	 * @since Version 1.0
	 */
	public Vertex getStart() {
		return start;
	}

	/**
	 * Sets the starting {@link Vertex} of the edge
	 * @param start the vertex to set
	 * @since Version 1.0
	 */
	public void setStart(Vertex start) {
		this.start = start;
		this.refreshPointsZoomLevel();
	}

	/**
	 * @return the destination {@link Vertex} of the edge
	 * @since Version 1.0
	 */	
	public Vertex getEnd() {
		return end;
	}
	
	/**
	 * Sets the destination {@link Vertex} of the edge
	 * @param end the vertex to set
	 * @since Version 1.0
	 */
	public void setEnd(Vertex end) {
		this.end = end;
		this.refreshPointsZoomLevel();
	}

	/**
	 * @return the cost of the edge (the cost to get from the starting vertex to the destination vertex)
	 * @since Version 1.0
	 */		
	public float getCost() {
		return cost;
	}
	
	/**
	 * Sets the cost of the edge (the cost to get from the starting vertex to the destination vertex)
	 * @param cost the new cost to set
	 * @since Version 1.0
	 */	
	public void setCost(float cost) {
		this.cost = cost;
		this.label = "";
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
		this.cost = 0.0f;
	}

	/**
	 * @return the thickness of the edge
	 * @since Version 1.0
	 */	
	public int getThickness() {
		return thickness;
	}
	/**
	 * Sets the thickness value of the edge
	 * @param thickness the new thickness value
	 * @since Version 1.0
	 */	
	public void setThickness(int thickness) {
		if (thickness > 5)
			thickness = 5;
		else if (thickness < 1)
			thickness = 1;
		this.thickness = thickness;
	}
	
	public Vector<Vector2D> getPoints() {
		return points;
	}

	/**
	 * Sets the intermediary drawing points of the edge
	 * @param points the vector of points to set
	 * @since Version 1.0
	 */
	public void setPoints(Vector<Vector2D> points) {
		if (this.points != null){
			this.points.removeAllElements();
			this.points.clear();
			this.points = null;
		}
		/*
		//sort on points[i].x+points[i].y
		int e1 = 0, e2 = 0;
		Vector<Vector2D> a = points;
		for (int i = 0; i < a.size();i++)
			for (int j = a.size() - 1; j > i; j--){
				e1 = a.get(j - 1).getX() + a.get(j - 1).getY();
				e2 = a.get(j).getX() + a.get(j).getY();
				if (e1 > e2){
					Vector2D aux = new Vector2D(0, 0);
					aux.setX(a.get(j - 1).getX());
					aux.setY(a.get(j - 1).getY());
					a.set(j - 1, a.get(j));
					a.set(j, aux);
				}
			}
		this.points = a;
		*/
		this.points = points;
		this.refreshPointsZoomLevel();
	}
	
	/**
	 * @return the bit that determines if the edge cost will be calculated based on the vertices positions
	 * @since Version 1.0
	 */
	public boolean getRealDistances() {
		return realDistances;
	}
	
	/**
	 * Sets the bit that determines if the edge cost will be calculated based on the vertices positions
	 * @param realDistances the value to set
	 */

	public void setRealDistances(boolean realDistances) {
		this.realDistances = realDistances;
	}
	

	public int getZoomPercent() {
		return zoomPercent;
	}

	public void setZoomPercent(int z) {
		this.zoomPercent = z;
		this.refreshPointsZoomLevel();
		this.thickness = (int) (this.baseThickness * z/100);
		this.setPointSize((int)(this.basePointSize * z/100));
	}
	

	public void setPointSize(int pointSize) {
		this.pointSize = pointSize;
	}

	public int getPointSize() {
		return pointSize;
	}

	/**
	 * Uses the `points'(at 100%) vector to calculate the zoom points for the points on every 10%
	 */
	public void refreshPointsZoomLevel(){
		if ((points == null) || (points.size() < 1))
			return;
		this.pointsZoom = new Vector<Map<Integer,Vector2D>>();

		//for each custom drawing point (100%)
		for (int j = 0; j < this.points.size(); j++){
			//create a map to store <Percent, Point>
			Map<Integer, Vector2D> tempMap = new HashMap<Integer, Vector2D>();
			
			//for each 10%
			for (int i = 10; i <= 200; i+=10){
				//get the j-th point, and calculate the x & y positions on each i-th %
				int x = points.get(j).getX() * i / 100;
				int y = points.get(j).getY() * i / 100;
				Vector2D p = new Vector2D(x, y);
				tempMap.put(i, p);
			}
			this.pointsZoom.add(tempMap);
		}
	}
	

	/**
	 * Changes the index-th point to `p' taking into account the current zoomLevel, 
	 * then recalculates the zoom points for this edge
	 * @param index the index of the point to change
	 * @param p the point to change to
	 */
	public void setPoint(int index, Vector2D p){
		/*
		 * let's calculate the point at i%
		 * P ... zoomPercent%
		 * pt... i%
		 */		
		Map<Integer, Vector2D> tempMap = new HashMap<Integer, Vector2D>();
		for (int i = 10; i <= 200; i+=10){
			int x = p.getX() * i / this.zoomPercent;
			int y = p.getY() * i / this.zoomPercent;
			Vector2D pt = new Vector2D(x, y);
			tempMap.put(i, pt);
		}
		
		// Now we have the new points for every 10% for the index-th point of the edge

		// change the old point to the new one
		this.pointsZoom.set(index, tempMap);
		
		// Update the point used with the one calculated in 100% 
		this.points.set(index, tempMap.get(100));	
		
	}
	
	/**
	 * Returns the index of the point `p' in the private vector of custom points of the edge
	 * @param p the point for which the index is asked
	 * @return the index of the point in the private vector of custom points of the edge if the point was found, -1 otherwise
	 */
	public int getPointIndex(Vector2D p){
		//for each point
		for (int i = 0; i < this.points.size(); i++){
			//get its corresponding point at the current zoom level
			Vector2D pt = this.pointsZoom.get(i).get(this.zoomPercent);
			if (pt.equals(p))
				return i;
		}
		
		return -1;
	}
	
	public Vector2D getPoint(int index){
		return this.getZoomPoint(index);
		//return this.points.get(index);
	}
	
	/**
	 * Returns a point with the index `index' from the current zoomlevel
	 * @param index the index of the point in the private vector of points
	 * @return the `index'th point in the private vector of points from the current zoomlevel
	 */
	private Vector2D getZoomPoint(int index){
		return this.pointsZoom.get(index).get(this.zoomPercent);
	}
	
	public int getPointsSize(){
		return this.points.size();
	}
	
	public boolean isPointsNull(){
		return (this.points == null);
	}
	
	/**
	 * Generates a {@link String} representation of the Edge
	 * @return the generated string
	 */
	@Override
	public String toString() {
		return "`" + start + "' --> `" + end + "' [" + cost + "]";
		//return "Edge [color=" + color + ", start=" + start
		//		+ ", end=" + end + ", cost=" + cost + ", thickness="
		//		+ thickness + "]";
	}

	public void translate(int orientation, int value) {
		//move everything
		
		for (int i = 0; i < getPointsSize(); i++){
			Vector2D p = this.getZoomPoint(i);
			
			if (orientation == LEFT)
				this.setPoint(i, new Vector2D(p.getX() + value, p.getY()));
			else if (orientation == TOP)
				this.setPoint(i, new Vector2D(p.getX(), p.getY() + value));
		}
	}
	
	/**
	 * Recreates an edge between its starting and ending vertex as a straight line
	 * @param pointCount determines the number of intermediary points used in the edge; should be at least 1
	 */
	public void straightenPoints(int pointCount){
		if (pointCount < 1)
			return;
		//FIXED -- now works on every zoom level
		Vector2D s = start.get100Center();
		Vector2D d = end.get100Center();
		Vector2D aux = null;
		
		if (s.getX() > d.getX()){
			aux = s;
			s = d;
			d = aux;
		}
		
		//decide which formula to use
		Vector<Vector2D> newPoints = new Vector<Vector2D>();
		
		float step = 0.0f; 
		if (d.getX() - s.getX() == 0){	//same x position
			if (d.getY() - s.getY() == 0){	//same y position
				//they are on the same position => can't calculate the position of the points=>
				//=>place them in the top-left corner
				for (int i = 0; i < 2; i++)
					newPoints.add(new Vector2D(start.getLeft() + 20 * i, start.getTop() - 10*i));
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
				
				newPoints.add(new Vector2D((int)X, (int)Y));
				prev = X;
			}
		}
		setPoints(newPoints);
	}
	
	private boolean intersects(Vector2D a1, Vector2D a2, Vector2D b1, Vector2D b2){
		int sz1 = (b2.getX() - b1.getX()) * (a1.getY() - b1.getY()) - (b2.getY() - b1.getY()) * (a1.getX() - b1.getX());
		int sz2 = (a2.getX() - a1.getX()) * (a1.getY() - b1.getY()) - (a2.getY() - a1.getY()) * (a1.getX() - b1.getX());
		int n = (b2.getY() - b1.getY()) * (a2.getX() - a1.getX()) - (b2.getX() - b1.getX()) * (a2.getY() - a1.getY());
		
		if (n != 0){
			int detA = sz1 / n;
			int detB = sz2 / n;
			if ((detA >= 0) && (detA <= 1) && (detB >= 0) && (detB <= 1))
				return true;
			else
				return false;
		}
		return true;
	}
	
	/*
	 * If points A and B are separated by segment CD and points C and D are separated by segment AB. 
	 * If points A and B are separated by segment CD then ACD and BCD should have opposite orientation, 
	 * meaning either ACD or BCD is counterclockwise but not both.
	 */
	/*
	private boolean isCounterClockWise(Vector2D a, Vector2D b, Vector2D c){
		return (c.getY() - a.getY()) * (b.getX() - a.getX()) > (b.getY() - a.getY()) * (c.getX() - a.getX());
	}
	
	private boolean intersects2(Vector2D a, Vector2D b, Vector2D c, Vector2D d){
		return isCounterClockWise(a, c, d) != isCounterClockWise(b, c, d) && isCounterClockWise(a, b, c) != isCounterClockWise(a, b, d);
	}
	*/
	public int getIntersectionCount(Edge e){
		
		Vector<Vector2D> allPoints_this = new Vector<Vector2D>();
		Vector2D startCenter_this = start.getCenter();
		Vector2D endCenter_this = end.getCenter();
		
		Vector<Vector2D> allPoints_e = new Vector<Vector2D>();
		Vector2D startCenter_e = e.start.getCenter();
		Vector2D endCenter_e = e.end.getCenter();
		
		allPoints_this.add(startCenter_this);
		if ((this.points != null) && (this.points.size() > 0))
			for (int i = 0; i < this.pointsZoom.size(); i++)
				allPoints_this.add(this.pointsZoom.get(i).get(this.zoomPercent));
		allPoints_this.add(endCenter_this);
		
		allPoints_e.add(startCenter_e);
		if ((e.points != null) && (e.points.size() > 0))
			for (int i = 0; i < e.pointsZoom.size(); i++)
				allPoints_e.add(e.pointsZoom.get(i).get(e.zoomPercent));
		allPoints_e.add(endCenter_e);
		
		int result = 0;
		for (int i = 0; i < allPoints_this.size() - 1; i++){
			Vector2D p1_this = allPoints_this.get(i);
			Vector2D p2_this = allPoints_this.get(i + 1);
			for (int j = 0; j < allPoints_e.size() - 1; j++){
				Vector2D p1_e = allPoints_e.get(j);
				Vector2D p2_e = allPoints_e.get(j + 1);
				if (intersects(p1_this, p2_this, p1_e, p2_e))
					result++;
			}
		}
		
		
		return result;
	}

	public void reverse() {
		Vertex aux = getStart();
		start = end;
		end = aux;
	}
}

