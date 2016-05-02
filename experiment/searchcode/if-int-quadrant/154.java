package Minor.P3.DS;

import java.util.Vector;

import Minor.P3.Client.Point;

/**
* @author Anthony Sosso
* Project 3:  PR Quadtrree
* Last modification date: 10/15/2012
*/

//The test harness will belong to the following package; the quadtree
// implementation must belong to it as well.  In addition, the quadtree
// implementation must specify package access for the node types and tree
// members so that the test harness may have access to it.
//

public class prQuadtree<T extends Compare2D<? super T>> {

	// You must use a hierarchy of node types with an abstract base
	// class. You may use different names for the node types if
	// you like (change displayHelper() accordingly).
	abstract class prQuadNode {
	}

	class prQuadLeaf extends prQuadNode {
		Vector<T> Elements;

		public prQuadLeaf(T elem) {
			this.Elements = new Vector<T>();
			this.Elements.add(elem);
		}
	}

	class prQuadInternal extends prQuadNode {
		prQuadNode NW, NE, SE, SW;

		// Pre: elem != null, Direction is a valid direction
		// Post: sets the corresponding child pointer to a leaf containing the element
		public void insertLeaf(T elem, Direction elemQuadrant) {
			switch (elemQuadrant) {
			case NE:
				this.NE = new prQuadLeaf(elem);
			case NW:
				this.NW = new prQuadLeaf(elem);
			case SE:
				this.SE = new prQuadLeaf(elem);
			case SW:
				this.SW = new prQuadLeaf(elem);
			default:
				break;
			}
		}

	}

	prQuadNode root;
	long xMin, xMax, yMin, yMax;

	// Initialize quadtree to empty state, representing the specified region.
	public prQuadtree(long xMin, long xMax, long yMin, long yMax) {
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMax = yMax;
		this.yMin = yMin;
	}

	// Pre: elem != null
	// Post: If elem lies within the tree's region, and elem is not already
	// present in the tree, elem has been inserted into the tree.
	// Return true iff elem is inserted into the tree.
	public boolean insert(T elem) {
		// if the item already in tree or is outside the world boundaries, don't insert
		// it
		if (this.find(elem) != null
				|| !elem.inBox(this.xMin, this.xMax, this.yMin, this.yMax)) {
			return false;
		} else {// recursively insert it
			this.root = insertHelper(this.root, elem, this.xMin, this.xMax,
					this.yMin, this.yMax);
			return true;
		}
	}

	// Pre: elem != null
	// Post: If elem lies within the tree's region, and elem is not already
	// present in the tree, elem has been inserted into the tree.
	// Return true iff elem is inserted into the tree.
	@SuppressWarnings("unchecked")
	private prQuadNode insertHelper(prQuadNode sRoot, T elem, double xLo,
			double xHi, double yLo, double yHi) {
		if (sRoot == null) {// creating a leaf
			return new prQuadLeaf(elem);
		} else if (sRoot.getClass().getName()
				.equals("Minor.P3.DS.prQuadtree$prQuadInternal")) {// internal node; find the location for the new leaf
																	// recursively and insert it
			prQuadInternal sNode = (prQuadInternal) sRoot;

			// Find the direction the node should be in, and then get those bounds
			Direction elemQuadrant = elem.inQuadrant(xLo, xHi, yLo, yHi);
			double[] quadrantBounds = getQuadrantBounds(elemQuadrant, xLo, xHi,
					yLo, yHi);
			switch (elemQuadrant) {
			case NE:
				sNode.NE = insertHelper(sNode.NE, elem, quadrantBounds[0],
						quadrantBounds[1], quadrantBounds[2], quadrantBounds[3]);
				break;
			case NW:
				sNode.NW = insertHelper(sNode.NW, elem, quadrantBounds[0],
						quadrantBounds[1], quadrantBounds[2], quadrantBounds[3]);
				break;
			case SE:
				sNode.SE = insertHelper(sNode.SE, elem, quadrantBounds[0],
						quadrantBounds[1], quadrantBounds[2], quadrantBounds[3]);
				break;
			case SW:
				sNode.SW = insertHelper(sNode.SW, elem, quadrantBounds[0],
						quadrantBounds[1], quadrantBounds[2], quadrantBounds[3]);
				break;
			default:
				break;
			}
			return sNode;
		} else {// leaf node, must partition; create an internal, insert the 'old' leaf, insert the new leaf
			prQuadInternal partitionedNode = new prQuadInternal();
			prQuadLeaf sLeaf = (prQuadLeaf) sRoot;

			// Find which quadrant the leaf's value goes in first
			partitionedNode = (prQuadInternal) insertHelper(partitionedNode,
					sLeaf.Elements.get(0), xLo, xHi, yLo, yHi);
			partitionedNode = (prQuadInternal) insertHelper(partitionedNode,
					elem, xLo, xHi, yLo, yHi);
			return partitionedNode;
		}
	}

	// Pre: dir != null
	// Post: Tree is not modified
	// Return an array of doubles bounding the desired quadrant in the format (xLo, xHi, yLo, yHi)
	// Bounds operate according to the project specification.
	// NE: user data object lies in NE quadrant, including non-negative
	// x-axis, but not the positive y-axis
	// NW: user data object lies in the NW quadrant, including the positive
	// y-axis, but not the negative x-axis
	// SW: user data object lies in the SW quadrant, including the negative
	// x-axis, but not the negative y-axis
	// SE: user data object lies in the SE quadr
	private double[] getQuadrantBounds(Direction dir, double xLo, double xHi,
			double yLo, double yHi) {
		double[] quadrantBounds = null;
		double xAxis = (yLo + yHi) / 2.0;
		double yAxis = (xLo + xHi) / 2.0;

		switch (dir) {
		case NE:
			quadrantBounds = new double[] { yAxis, xHi, xAxis, yHi };
			break;
		case NW:
			quadrantBounds = new double[] { xLo, yAxis, xAxis, yHi };
			break;
		case SE:
			quadrantBounds = new double[] { yAxis, xHi, yLo, xAxis };
			break;
		case SW:
			quadrantBounds = new double[] { xLo, yAxis, yLo, xAxis };
			break;
		default:
			break;
		}
		return quadrantBounds;

	}

	// Pre: elem != null
	// Post: If elem lies in the tree's region, and a matching element occurs
	// in the tree, then that element has been removed.
	// Returns true iff a matching element has been removed from the tree.
	public boolean delete(T Elem) {
		if (this.find(Elem) == null
				|| !Elem.inBox(this.xMin, this.xMax, this.yMin, this.yMax)) {// if element isn't in the tree or lies
																				// outside the tree, don't delete it
			return false;
		} else {// recursively delete the node
			this.root = deleteHelper(this.root, Elem, this.xMin, this.xMax,
					this.yMin, this.yMax);
			return true;
		}

	}

	// Pre: elem != null
	// Post: If elem lies within the tree's region, and elem is not already
	// present in the tree, elem has been inserted into the tree.
	// Return reference to the result of the recursive delete
	private prQuadNode deleteHelper(prQuadNode node, T elem, double xLo,
			double xHi, double yLo, double yHi) {
		if (node == null) {
			return null;
		} else if (node.getClass().getName()
				.equals("Minor.P3.DS.prQuadtree$prQuadInternal")) {
			prQuadInternal sNode = (prQuadInternal) node;

			// Recursively find the node to delete
			Direction elemQuadrant = elem.inQuadrant(xLo, xHi, yLo, yHi);
			double[] quadrantBounds = getQuadrantBounds(elemQuadrant, xLo, xHi,
					yLo, yHi);
			switch (elemQuadrant) {
			case NE:
				sNode.NE = deleteHelper(sNode.NE, elem, quadrantBounds[0],
						quadrantBounds[1], quadrantBounds[2], quadrantBounds[3]);
				break;
			case NW:
				sNode.NW = deleteHelper(sNode.NW, elem, quadrantBounds[0],
						quadrantBounds[1], quadrantBounds[2], quadrantBounds[3]);
				break;
			case SE:
				sNode.SE = deleteHelper(sNode.SE, elem, quadrantBounds[0],
						quadrantBounds[1], quadrantBounds[2], quadrantBounds[3]);
				break;
			case SW:
				sNode.SW = deleteHelper(sNode.SW, elem, quadrantBounds[0],
						quadrantBounds[1], quadrantBounds[2], quadrantBounds[3]);
				break;
			default:
				break;
			}
			return contractIfNecessary(sNode);// contract branches if necessary
		} else {// we're a leaf
			prQuadLeaf leafNode = (prQuadLeaf) node;
			if (leafNode.Elements.get(0).equals(elem)) {// node to remove, return null so that it's removed
				return null;
			}
		}
		return null;
	}

	// Pre: node
	// Post: tree is not modified
	// Return true iff elem is inserted into the tree.
	private prQuadNode contractIfNecessary(prQuadNode node) {
		if (node == null) {
			return null;
		} else if (node.getClass().getName()
				.equals("Minor.P3.DS.prQuadtree$prQuadInternal")) {
			int childrenCount = 0;
			prQuadInternal sNode = (prQuadInternal) node;
			prQuadNode nodeToContract = null;

			// count how many children it has
			if (sNode.SE != null) {
				nodeToContract = sNode.SE;
				childrenCount++;
			}
			if (sNode.SW != null) {
				nodeToContract = sNode.SW;
				childrenCount++;
			}
			if (sNode.NE != null) {
				nodeToContract = sNode.NE;
				childrenCount++;
			}
			if (sNode.NW != null) {
				nodeToContract = sNode.NW;
				childrenCount++;
			}

			// If the node has >1 children, don't contract it
			// If it has 1 child, replace the current node with that child if the child is a leaf
			if (childrenCount == 0) {
				return null;
			} else if (childrenCount > 1) {
				return node;
			} else if (nodeToContract.getClass().getName()
					.equals("Minor.P3.DS.prQuadtree$prQuadLeaf")) { // it equals 1 and is a leaf, need to contract
				return contractIfNecessary(nodeToContract);
			} else {
				return node;
			}
		} else {// we're a leaf
			return node;
		}
	}

	// Pre: elem != null
	// Returns reference to an element x within the tree such that
	// elem.equals(x)is true, provided such a matching element occurs within
	// the tree; returns null otherwise.
	public T find(T Elem) {
		return findHelper(Elem, this.root, this.xMin, this.xMax, this.yMin,
				this.yMax);
	}

	private T findHelper(T Elem, prQuadNode node, double xLo, double xHi,
			double yLo, double yHi) {
		if (node == null) {
			return null;
		} else if (node.getClass().getName()
				.equals("Minor.P3.DS.prQuadtree$prQuadInternal")) {// recursively find the node
			prQuadInternal internalNode = (prQuadInternal) node;

			// Determine what quadrant it should be in and traverse the tree
			Direction elemQuadrant = Elem.inQuadrant(xLo, xHi, yLo, yHi);
			double[] quadrantBounds = getQuadrantBounds(elemQuadrant, xLo, xHi,
					yLo, yHi);
			switch (elemQuadrant) {
			case NE:
				return findHelper(Elem, internalNode.NE, quadrantBounds[0],
						quadrantBounds[1], quadrantBounds[2], quadrantBounds[3]);
			case NW:
				return findHelper(Elem, internalNode.NW, quadrantBounds[0],
						quadrantBounds[1], quadrantBounds[2], quadrantBounds[3]);
			case SE:
				return findHelper(Elem, internalNode.SE, quadrantBounds[0],
						quadrantBounds[1], quadrantBounds[2], quadrantBounds[3]);
			case SW:
				return findHelper(Elem, internalNode.SW, quadrantBounds[0],
						quadrantBounds[1], quadrantBounds[2], quadrantBounds[3]);
			default:
				return null;
			}

		} else {// we're a leaf; if we're at the right place, return the data element
			prQuadLeaf leafNode = (prQuadLeaf) node;
			if (leafNode.Elements.get(0).equals(Elem)) {
				return leafNode.Elements.get(0);
			} else {
				return null;
			}
		}
	}

	// Pre: xLo, xHi, yLo and yHi define a rectangular region
	// Returns a collection of (references to) all elements x such that x is
	// in the tree and x lies at coordinates within the defined rectangular
	// region, including the boundary of the region.
	public Vector<T> find(long xLo, long xHi, long yLo, long yHi) {
		Vector<T> returnVector = new Vector<T>();
		findHelper(returnVector, this.root, xLo, xHi, yLo, yHi);
		return returnVector;
	}

	// Pre: xLo, xHi, yLo and yHi define a rectangular region
	// Returns a collection of (references to) all elements x such that x is
	// in the tree and x lies at coordinates within the defined rectangular
	// region, including the boundary of the region.
	private void findHelper(Vector<T> container, prQuadNode node, long xLo,
			long xHi, long yLo, long yHi) {
		if (node == null) {
			return;
		} else if (node.getClass().getName()
				.equals("Minor.P3.DS.prQuadtree$prQuadInternal")) {

			// Find out which quadrants (if any) overlap, and then see if their leaves are within the region.

			prQuadInternal internalNode = (prQuadInternal) node;
			double[] NEBounds = getQuadrantBounds(Direction.NE, xLo, xHi, yLo,
					yHi);
			double[] NWBounds = getQuadrantBounds(Direction.NW, xLo, xHi, yLo,
					yHi);
			double[] SEBounds = getQuadrantBounds(Direction.SE, xLo, xHi, yLo,
					yHi);
			double[] SWBounds = getQuadrantBounds(Direction.SW, xLo, xHi, yLo,
					yHi);
			if (overlappingRegions(NEBounds, xLo, xHi, yLo, yHi)) {
				findHelper(container, internalNode.NE, xLo, xHi, yLo, yHi);
			}
			if (overlappingRegions(NWBounds, xLo, xHi, yLo, yHi)) {
				findHelper(container, internalNode.NW, xLo, xHi, yLo, yHi);
			}
			if (overlappingRegions(SEBounds, xLo, xHi, yLo, yHi)) {
				findHelper(container, internalNode.SE, xLo, xHi, yLo, yHi);
			}
			if (overlappingRegions(SWBounds, xLo, xHi, yLo, yHi)) {
				findHelper(container, internalNode.SW, xLo, xHi, yLo, yHi);
			}

		} else {// we're a leaf
			prQuadLeaf leafNode = (prQuadLeaf) node;
			if (leafNode.Elements.get(0).inBox(xLo, xHi, yLo, yHi)) {
				container.addElement(leafNode.Elements.get(0));// leaf is in the region, add it to the container
			}
		}

	}

	// Pre: xLo, xHi, yLo and yHi define a rectangular region, and region1Bounds is of the format {xLo, xHi, yLo, yHi}
	// determines if two regions overlap
	// Returns true iff regions overlap
	private boolean overlappingRegions(double[] region1Bounds,
			double region2XLo, double region2XHi, double region2YLo,
			double region2YHi) {
		double region1Width = Math.abs(region1Bounds[0])
				+ Math.abs(region1Bounds[1]);
		double region1Height = Math.abs(region1Bounds[2])
				+ Math.abs(region1Bounds[3]);

		double region2Width = Math.abs(region2XLo) + Math.abs(region2XHi);
		double region2Height = Math.abs(region2YLo) + Math.abs(region2YHi);

		return (region1Bounds[0] + region1Width >= region2XLo
				&& region1Bounds[2] + region1Height >= region2YLo
				&& region1Bounds[0] <= region2XLo + region2Width && region1Bounds[2] <= region2YLo
				+ region2Height);
	}

}
/**
 * //    On my honor:
//   
//    - I have not discussed the Java language code in my program with
//      anyone other than my instructor or the teaching assistants
//      assigned to this course.
//   
//    - I have not used Java language code obtained from another student,
//      or any other unauthorized source, either modified or unmodified. 
//   
//    - If any Java language code or documentation used in my program
//      was obtained from another source, such as a text book or course
//      notes, that has been clearly noted with a proper citation in
//      the comments of my program.
//   
//    - I have not designed this program in such a way as to defeat or
//      interfere with the normal operation of the Curator System.
//
//    Anthony Sosso
 */
