package net.lustlab.partitions.rectangular;

public class Node<_UserDataType> {

	public enum Orientation {
		Horizontal,
		Vertical
	};
	
	private final Rectangle area;
	private final  Orientation orientation;

	private Node<_UserDataType>[] children = null;
	private double ratio = Double.NEGATIVE_INFINITY;
	
	private _UserDataType data = null;
	
	/**
	 * Constructor for Node
	 * @param orientation the orientation of the node. The orientation determines the split axis. A horizontally orientated node will be split in a left and a right node.
	 * @param area the area of the node.
	 */

	public Node(Orientation orientation, Rectangle area) {
		this.area = area;
		this.orientation = orientation;
	}
	
	/**
	 * Returns the orientation of the node.
	 * @return the orientation of the node
	 */
	public Orientation getOrientation() {
		return orientation;
	}
	
	/**
	 * Store user data in the node
	 * @param data the data to store
	 */
	public void setData(_UserDataType data) {
		this.data = data;
	}
	
	public _UserDataType getData() {
		return data;
	}
	
	public boolean isLeaf() {
		return this.children == null || this.children.length == 0;
	}
	
	public Node<_UserDataType>[] getChildren() {
		return children;
	}
	
	public Rectangle getArea() {
		return area;
	}
	
	/**
	 * Splits the node in two child nodes. The sizes of the child nodes are determined by the ratio parameter. The ratio parameter determines the relative size of the first produced child node. 
	 * @param ratio split ratio, between 0 and 1
	 * @return the child nodes produced by splitting the current node
	 */
	public Node<_UserDataType>[] split(double ratio) {
		
		this.ratio = ratio;
		
		if (orientation == Orientation.Horizontal) {
			splitHorizontally(ratio); 
		}
		else {
			splitVertically(ratio);
		}
		
		return this.children;
	}

	@SuppressWarnings("unchecked")
	private void splitVertically(double ratio) {
		double topHeight = area.getHeight() * ratio;
		double bottomHeight = area.getHeight() * (1.0 - ratio);
		
		Rectangle topArea = new Rectangle(area.getX(), area.getY(), area.getWidth(), topHeight);
		Rectangle bottomArea = new Rectangle(area.getX(), area.getY()+topHeight, area.getWidth(), bottomHeight);
		
		Node<_UserDataType> top = new Node<_UserDataType>(Orientation.Horizontal, topArea);
		Node<_UserDataType> bottom = new Node<_UserDataType>(Orientation.Horizontal, bottomArea);
		
		this.children = new Node[] { top, bottom };
	}

	@SuppressWarnings("unchecked")
	private void splitHorizontally(double ratio) {
		double leftWidth = area.getWidth() * ratio;
		
		Rectangle leftArea = new Rectangle(area.getX(), area.getY(), leftWidth, area.getHeight());
		Rectangle rightArea = new Rectangle(area.getX() + leftWidth, area.getY(), area.getWidth()-leftWidth, area.getHeight());
		
		Node<_UserDataType> left = new Node<_UserDataType>(Orientation.Vertical, leftArea);
		Node<_UserDataType> right = new Node<_UserDataType>(Orientation.Vertical, rightArea);

		this.children = new Node[] { left, right};
	}
	
	public double getRatio() {
		return ratio;
	}
	
}

