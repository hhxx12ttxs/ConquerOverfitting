package perspectives.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;


public class QuadTree {
	
	// Index of the tree node. If not a graph node then -1
	private int index;
	// Max and Min position of the corresponding rectangle of the tree node
	private double maxX;
	private double minX;
	private double maxY;
	private double minY;
	//Mass of the tree node. For this simulation it assumed as total mass of the children. All the node has mass of 1.
	private int centerMass;
	//Center of gravity of the tree node
	private double centerX;
	private double centerY;
	// Number of graph node available in the corresponding rectangle
	private int nodeCount;
	//children list
	private LinkedList<QuadTree> childrenList;
	// Parent tree node
	private QuadTree parent;
	// Number of children. Atmost 4 and atleast 0.
	private int childCount;
	/**
	 * Constructor of the Tree node
	 * @param index if its a graph node then the index of the node else -1.
	 * @param maxX maxX of rectangle
	 * @param minX minX of rectangle
	 * @param maxY maxY of rectangle
	 * @param minY minY of rectangle
	 * @param centerMass Mass of tree node
	 * @param centerX center of gravity X
	 * @param centerY center of gravity X
	 * @param nodeCount total graph node in the rectangle
	 */
	public QuadTree(int index, double maxX, double minX, double maxY, double minY, int centerMass, double centerX, double centerY, int nodeCount)
	{
		this.index = index;
		this.maxX = maxX;
		this.minX = minX;
		this.maxY = maxY;
		this.minY = minY;
		this.centerMass = centerMass;
		this.centerX = centerX;
		this.centerY = centerY;
		this.nodeCount = nodeCount;
		this.childrenList = new LinkedList<QuadTree>();
		//initially the child count is 0.
		this.childCount = 0;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public boolean isLeaf()
	{
		if(this.childCount==0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	/**
	 * Add child to the node
	 * @param child the added child
	 */ 
	public void addChild(QuadTree child)
	{		
		child.setParent(this); // Make the parent of the child as this. 
		this.childrenList.add(child); // Add child
		this.childCount++; // Increase the child count
	}
	
	// Getter setter methods
	

	public double getMaxX() {
		return maxX;
	}

	public void setMaxX(double maxX) {
		this.maxX = maxX;
	}

	public double getMinX() {
		return minX;
	}

	public void setMinX(double minX) {
		this.minX = minX;
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}

	public double getMinY() {
		return minY;
	}

	public void setMinY(double minY) {
		this.minY = minY;
	}

	public int getCenterMass() {
		return centerMass;
	}

	public void setCenterMass(int centerMass) {
		this.centerMass = centerMass;
	}

	public double getCenterX() {
		return centerX;
	}

	public void setCenterX(double centerX) {
		this.centerX = centerX;
	}

	public double getCenterY() {
		return centerY;
	}

	public void setCenterY(double centerY) {
		this.centerY = centerY;
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}

	

	public LinkedList<QuadTree> getChildrenList() {
		return childrenList;
	}

	public void setChildrenList(LinkedList<QuadTree> childrenList) {
		this.childrenList = childrenList;
	}

	public QuadTree getParent() {
		return parent;
	}

	public void setParent(QuadTree parent) {
		this.parent = parent;
	}
	/**
	 * Getting the diameter of rectangle
	 * @return the diameter of rectangle
	 */
	public double getDiameter()
	{		
		return Util.distanceBetweenPoints(this.getMinX(), this.getMinY(), this.getMaxX(), this.getMaxY());
	}
	
	public int getChildCount() {
		return childCount;
	}
	public void setChildCount(int childCount) {
		this.childCount = childCount;
	}
	/**
	 * Printing the Quad tree as an XML format.
	 */
	public String toString()
	{
		String msg = "<Node index=\""+this.index
						+"\" childCount=\""+this.childCount+"\" diameter=\""+this.getDiameter()
						+"\" x=\""+this.getCenterX()+"\" y=\""+this.getCenterY()+"\">";
		for(QuadTree child:this.childrenList)
		{
			msg+= child.toString();			
		}

		msg+="</Node>";
		return msg;
		
	}
	
	public void render(Graphics2D g)
	{
		g.setColor(Color.LIGHT_GRAY);
		g.drawRect((int)minX,(int)minY, (int)(maxX-minX), (int)(maxY-minY));
		for(QuadTree child:this.childrenList)
			child.render(g);
	}
}

