/**
 * Copyright (C) 2012 Marco A Asteriti
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.masteriti.geometry.dcel.voronoi;

import com.masteriti.datastructures.BST;
import com.masteriti.datastructures.Event;
import com.masteriti.geometry.AbstractLine2D;
import com.masteriti.geometry.Calc;
import com.masteriti.geometry.Circle;
import com.masteriti.geometry.Line2D;
import com.masteriti.geometry.LineSegment2D;
import com.masteriti.geometry.Point;
import com.masteriti.geometry.Ray;
import com.masteriti.geometry.Rectangle;
import com.masteriti.geometry.dcel.Cell;
import com.masteriti.geometry.dcel.DCEL;
import com.masteriti.geometry.dcel.HalfEdge;
import com.masteriti.geometry.dcel.Vertex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

public class FortuneBST extends BST<VoronoiNodeData> {

  public static double sweepline;

  private final VoronoiMachine vm;
  private final DCEL dcel;
  private final PriorityQueue<Event> queue;
  private final double leftBounds;
  private final double topBounds;
  private final double rightBounds;
  private final double bottomBounds;

  public FortuneBST(VoronoiMachine vm) {
    this.vm = vm;
    this.dcel = vm.dcel;
    this.queue = vm.queue;
    leftBounds = vm.bounds.x;
    bottomBounds = vm.bounds.y;
    topBounds = vm.bounds.h + bottomBounds;
    rightBounds = vm.bounds.w + leftBounds;
  }

  public boolean addNewArc(Cell cell) {
    // Start by setting the height of the sweepline = to the new site y value
    sweepline = cell.getOriginSite().y;

    // check if this is the first site event.
    if (root == null) { 	// new BST tree, create the first arc
      root = new Node<>(new VoronoiNodeData(cell));
      return true;
    } else {  // find the arc over the site and add new sub tree in its place
      VoronoiNodeData newArc = new VoronoiNodeData(cell);
      Node<VoronoiNodeData> nodeAbove = root.getNodeClosestTo(newArc);
      
      if(nodeAbove.isBreakPoint()) {
        insertNewArcAtBreakPoint(newArc, nodeAbove);
      } else {
        instertNewArc(newArc, nodeAbove);
      }
      return true;
    }
  }

  /**
   * Inserts a new subtree in the BST in the special case where the new arc
   * happens to be directly below a breakpoint between two other arcs.  This
   * eliminates the need for post processing cleanup of zero-length segments and
   * other unpleasant side effects.
   * 
   * @param newArcData
   * @param breakPointNodeAbove 
   */
  private void insertNewArcAtBreakPoint(VoronoiNodeData newArcData, Node<VoronoiNodeData> breakPointNodeAbove) {
    sweepline = newArcData.getMain().getOriginSite().y;

    Vertex v;   // Vertex where breakpoint splints into two by new arc
    HalfEdge lBPedge, lHEdge, rBPedge, rHEdge, tlHEdge, trHEdge;
    VoronoiNodeData leftBPData, rightBPData;  // two new breakpoints
    Node<VoronoiNodeData> leftArcNode, leftBPNode, newArcNode, rightBPNode, rightArcNode; // arcs involved
        
    leftArcNode = prevInOrder(breakPointNodeAbove);
    rightArcNode = nextInOrder(breakPointNodeAbove);
    newArcNode = new Node<>(newArcData);

    // Grab reference the half-edges traced by original breakpoint
    tlHEdge = breakPointNodeAbove.data.getEdge();
    trHEdge = tlHEdge.getTwin();
    
    // Create Vertex where new arc meets breakpoint above.
    Circle c = new Circle(leftArcNode.data.getMain().getOriginSite(),
                          rightArcNode.data.getMain().getOriginSite(),
                          newArcData.getMain().getOriginSite());
    v = new Vertex(c.getCenter(), breakPointNodeAbove.data.getEdge());
    
    // TODO: change dcel sets to hashmaps to avoid this
    if(dcel.vertexList.contains(v)) {
      Vertex x;
      Iterator<Vertex> it = dcel.vertexList.iterator();
      boolean done = false;
      while(!done && it.hasNext()) {
        x = it.next();
        if(v.equals(x)) {
          v = x;
          done = true;
        }
      }
    }
    dcel.vertexList.add(v);
    
    // Set the Vertex as the origin of the hEdge traced by by the break point above the new arc
    tlHEdge.setOrigin(v);
    
    // Create two hEdges that have v as it's origin and will pair with the ones
    // traced by the two new break points
    lHEdge = new HalfEdge(v);
    lHEdge.setFace(newArcData.getMain());
    dcel.hEdgeList.add(lHEdge);
    
    rHEdge = new HalfEdge(v);
    rHEdge.setFace(rightArcNode.data.getMain());
    dcel.hEdgeList.add(rHEdge);
    
    // Create two new pairs of halfEdges that originate from the new vertex
    leftBPData = new VoronoiNodeData(leftArcNode.data.getMain(), newArcData.getMain());
    lBPedge = leftBPData.getEdge();
    lBPedge.setTwin(lHEdge);
    lHEdge.setDirection(lBPedge.getdirection().opposite());
    dcel.hEdgeList.add(lHEdge);
    dcel.hEdgeList.add(lHEdge.getTwin());
    
    rightBPData = new VoronoiNodeData(newArcData.getMain(), rightArcNode.data.getMain());
    rBPedge = rightBPData.getEdge();
    rBPedge.setTwin(rHEdge);
    rHEdge.setDirection(rBPedge.getdirection().opposite());
    dcel.hEdgeList.add(rHEdge);
    dcel.hEdgeList.add(rHEdge.getTwin());
    
    
    // Link up the three pairs of half-edges
    lBPedge.setNext(tlHEdge);
    trHEdge.setNext(rHEdge);
    rBPedge.setNext(lHEdge);
    
    // Clean up Node Data...
    // remove any circle events associated with left and right arcs.
    CircleEvent removeIt = leftArcNode.data.getCircleEvent();
    if(removeIt != null) {
      queue.remove(removeIt);
      leftArcNode.data.setCircleEvent(null);
    }
    removeIt = rightArcNode.data.getCircleEvent();
    if(removeIt != null) {
      queue.remove(removeIt);
      rightArcNode.data.setCircleEvent(null);
    }
    
    // Insert new rightBPNode
    rightBPNode = new Node<>(rightBPData);
    rightBPNode.right = breakPointNodeAbove.right;
    rightBPNode.right.parent = rightBPNode;
    
    // Insert newArcNode
    rightBPNode.left = newArcNode;
    newArcNode.parent = rightBPNode;
    
    // Modify breakPointNodeAbove into the leftBPNode
    leftBPNode = breakPointNodeAbove;
    leftBPNode.data = leftBPData;
    
    // tie in rightBPNode with leftBPNode
    rightBPNode.parent = leftBPNode;
    leftBPNode.right = rightBPNode;
    
    // Check for new circle events given new Arc Node
    checkForCircleEvent(leftArcNode);
    checkForCircleEvent(rightArcNode);
    
  } // End of InsertNewArcAtBreakPoint method ----------------------------------
  
  /**
   * Inserts a new subtree in the BST
   *
   * @param cell represents a cell (original site event location)
   * @param arcAboveNode this is the node in the BST that is directly above the
   * new site.
   */
  private void instertNewArc(VoronoiNodeData newArcData, Node<VoronoiNodeData> arcAboveNode) {
	// Create a sub tree that represents 
    // original arc A - breakPoint AB - new arc B - breakpoint BA - original arc A
    //
    // 			\                /
    // 			AB              BA
    // 		   /  \       	   /  \
    // 		  A	   BA   or	  AB    A
    // 			  /  \       / \
    // 			 B    A     A   B
    //

    sweepline = newArcData.getMain().getOriginSite().y;

    Node<VoronoiNodeData> leftArcNode, leftBPNode, newArcNode, rightBPNode, rightArcNode;
    VoronoiNodeData origArcData, leftBPData, rightBPData;
    origArcData = arcAboveNode.data;
    leftBPData = new VoronoiNodeData(origArcData.getMain(), newArcData.getMain());
    rightBPData = new VoronoiNodeData(newArcData.getMain(), origArcData.getMain());
    
    // link the halfedges created by the new breakpoints and save them in the dcel
    leftBPData.getEdge().setTwin(rightBPData.getEdge());
    dcel.hEdgeList.add(leftBPData.getEdge());
    dcel.hEdgeList.add(rightBPData.getEdge());
    
    // Clean up node data...
    // Remove any circle events associated with original arc
    CircleEvent falseCircleEvent = arcAboveNode.data.getCircleEvent();
    if (falseCircleEvent != null) {					// check if there is a circleEvent associated with the arc being split
      queue.remove(falseCircleEvent);				// delete that circle event from the queue
      arcAboveNode.data.setCircleEvent(null);		// and from the nodeData.
    }    
    
    // create the new nodes
    leftArcNode = new Node<>(origArcData);
    rightArcNode = new Node<>(new VoronoiNodeData(origArcData));
    rightBPNode = new Node<>(rightBPData);
    newArcNode = new Node<>(newArcData);
    arcAboveNode.data = leftBPData;         // we replace the original arc node with a leftBPData node.
    leftBPNode = arcAboveNode;              // to make this more legible
    
    // tie them all up
    leftBPNode.left = leftArcNode;
    leftArcNode.parent = leftBPNode;
    
    leftBPNode.right = rightBPNode;
    rightBPNode.parent = leftBPNode;
    
    rightBPNode.left = newArcNode;
    newArcNode.parent = rightBPNode;
    
    rightBPNode.right = rightArcNode;
    rightArcNode.parent = rightBPNode;
    
    // Finish by checking for new potential circle events
    checkForCircleEvent(rightArcNode);
    checkForCircleEvent(leftArcNode);
    

  }

  public Node<VoronoiNodeData> nextArcInOrder(Node<VoronoiNodeData> arcNode) {
    Node<VoronoiNodeData> nextArc = nextInOrder(nextInOrder(arcNode));
    if (nextArc == null) {
      return null;
    } else {
      return nextArc;
    }
  }

  public Node<VoronoiNodeData> prevArcInOrder(Node<VoronoiNodeData> arcNode) {
    Node<VoronoiNodeData> prevArc = prevInOrder(prevInOrder(arcNode));
    if (prevArc == null) {
      return null;
    } else {
      return prevArc;
    }
  }

  protected void removeArc(Node<VoronoiNodeData> disappearingArcNode, double directrix) {
	// Runs when a circle event occurs.
    // We have a triplet of arcs where the two breakpoints that separates them
    // merge into one point eliminating the middle arc.  Modify the BST to 
    // reflect this change and eliminate any previous circle events associated 
    // with the 3 arcs, then check for new circle events given the new
    // triplet of arcs formed:  
    // To accomplish this, delete the leaf node L representing the disappearing
    // arc along with the parent node P representing one of its break points 
    // (we don't know which at runtime). Reconnect the orphaned leaf node O from
    // deleted node P with P's parent node.  Then modify the other breakpoint
    // node of L and modify the dcel to reflect that it is a breakpoint between
    // the two arcs to the left and right of the deleted arc.

    sweepline = directrix;
    
    // Objects we're working with
    Point mergePt = disappearingArcNode.data.getCircleEvent().circle.getCenter();
    Node<VoronoiNodeData> lMergeBPNode,
                          rMergeBPNode, 
                          newMergedNode,
                          toDeleteBPNode,
                          orphanNode,
                          leftMostArcNode, 
                          rightMostArcNode;
    HalfEdge lBPEdge, 
             rBPEdge,
             newEdge;
    
    toDeleteBPNode = disappearingArcNode.parent;
     
    lMergeBPNode = prevInOrder(disappearingArcNode);
    rMergeBPNode = nextInOrder(disappearingArcNode);
    lBPEdge = lMergeBPNode.data.getEdge();
    rBPEdge = rMergeBPNode.data.getEdge();
    
    // Make new vertex at mergePt.
    Vertex v = new Vertex(mergePt, lBPEdge);
    dcel.vertexList.add(v);
    
    // set origin of hEdges traced by BreakPoints and connect them
    lBPEdge.setOrigin(v);
    rBPEdge.setOrigin(v);
    lBPEdge.getTwin().setNext(rBPEdge);
    
    // Create new Node
    VoronoiNodeData newNodeData = new VoronoiNodeData(lMergeBPNode.data.getMain(), rMergeBPNode.data.getPair());
    
    // Create and link halfEdges traced by new Break Point
    newEdge = new HalfEdge(v, newNodeData.getEdge(), newNodeData.getPair());
    newEdge.setDirection(newNodeData.getEdge().getdirection().opposite());
    newNodeData.getEdge().setNext(lBPEdge);
    rBPEdge.getTwin().setNext(newEdge);
    
    // add new edges to dcel
    dcel.hEdgeList.add(newEdge);
    dcel.hEdgeList.add(newEdge.getTwin());
    
    // remove disappearingArcNode and extra BP and reconnect Nodes.
    orphanNode = findOrphanNode(disappearingArcNode);
    newMergedNode = findOtherBreakPoint(disappearingArcNode, toDeleteBPNode);
    
    orphanNode.parent = toDeleteBPNode.parent;
    if(toDeleteBPNode.parent.left == toDeleteBPNode) {
      toDeleteBPNode.parent.left = orphanNode;
    } else {
      toDeleteBPNode.parent.right = orphanNode;
    }
    
    newMergedNode.data = newNodeData;

	// The BST structure now reflects the beach line after the arc disappears.
    // Now Data has to be updated: 
    // Any circle events associated with the deleted arc must be deleted.
    leftMostArcNode = prevInOrder(newMergedNode);
    rightMostArcNode = nextInOrder(newMergedNode);
    
    CircleEvent ce = leftMostArcNode.data.getCircleEvent();
    if (ce != null) {
      this.queue.remove(ce);
      leftMostArcNode.data.setCircleEvent(null);
    }
    ce = rightMostArcNode.data.getCircleEvent();
    if (ce != null) {
      this.queue.remove(ce);
      rightMostArcNode.data.setCircleEvent(null);
    }

	// Check the triplet of arcs for new circle events where the arc to the left and right 
    // of the new breakpoint are the middle arc (i.e. leftmost and rightmost arc variables above).
    // W need the position of the directrix (sweep line)
    checkForCircleEvent(leftMostArcNode);
    checkForCircleEvent(rightMostArcNode);
  } // End of removeArc() method -----------------------------------------------

  /**
   * Method that checks the triplet of arcs in the beach line represented by the
   * centerArc and the two neighboring arcs. If there is a potential circle
   * event, it will create and add the event to the queue and link a reference
   * of it on the centerArc.
   *
   * @param midArc
   * @param directrix
   */
  private void checkForCircleEvent(Node<VoronoiNodeData> midArc) {

    Node<VoronoiNodeData> leftArc, rightArc;
    leftArc = prevArcInOrder(midArc);
    rightArc = nextArcInOrder(midArc);

    if ((leftArc != null) && (rightArc != null)) {  // Check if we actually have a valid arcs

      //	Check that the bisectors (corresponding to the edges being traced) converge
      if (isLeft(rightArc.data.getMain().getOriginSite(), midArc.data.getMain().getOriginSite(), leftArc.data.getMain().getOriginSite())) {
				//	Then solve the circle given 3 points (the sites corresponding to the arc triplet

        // Create a circle given 3 points corresponding the the main sites of each arc.
        Circle circle = new Circle(leftArc.data.getMain().getOriginSite(),
                                    midArc.data.getMain().getOriginSite(),
                                  rightArc.data.getMain().getOriginSite());

        if ((Double.isFinite(circle.getRadius())) && (circle.getBottom().y <= sweepline)) {
          // Create circle event, add it to the queue and add a reference to it at the node representing the middle arc.
          CircleEvent cEvent = new CircleEvent(midArc, circle); 		// Create the circle event
          this.queue.add(cEvent);										// add it to the queue
          midArc.data.setCircleEvent(cEvent);							// add reference to the arc associated with the event.
        }
      }
    }
  }

  /**
   * Takes a TreeNode, looks at the parent and returns the other child node of
   * that parent
   *
   * @param node
   * @return Tree Node that is the other child of node's parent.
   */
  private Node<VoronoiNodeData> findOrphanNode(Node<VoronoiNodeData> node) {
    if (node.parent == null) {
      return null;
    } else if (node == node.parent.left) {
      return node.parent.right;
    } else {
      return node.parent.left;
    }
  }

  /**
   * Takes a leaf (Arc) node and an inner (break point) node and returns the
   * opposite inner (break point) node of that same arc.
   *
   * @param leafNode
   * @param bpNode
   * @return Tree Node representing other break point.
   */
  private Node<VoronoiNodeData> findOtherBreakPoint(Node<VoronoiNodeData> leafNode, Node<VoronoiNodeData> bpNode) {
    if ((leafNode.left == null) && (leafNode.right == null)) {
      if (bpNode.data.getPair() == null) {
        // TODO error, break point node dcel missing pair site.
        return null;
      } else {
        if (bpNode.data.getMain() == leafNode.data.getMain()) {
					// the Main site in a breakpoint indicates the corresponding site's arc
          // is to the left...therefore this is the right breakpoint of Main's arc.
          return prevInOrder(leafNode); //return previous in order node (left break point).
        } else {
          return nextInOrder(leafNode); // otherwise return the right break point.
        }
      }
    } else {
      //TODO log error: leaf node passed is not a leaf node.
      return null;
    }
  }

  public void finalizeDCEL() {
    
    // Update the infinite halfEdges with a direction vector and store them here.
    HashMap<HalfEdge, Ray> infHalfEdges = getBoundsIntersectingEdges();
    
    // Get the list of points on our bounding box that are either the corners
    // or intersections with the remaining infinite half-edges
    LinkedHashMap<Point, HalfEdge> pointsOnBoundary = getPointsOnBoundary(infHalfEdges);
    
    // For each point, create a new Vertex and the outer half edge originating
    // from it and store these in appropriate containers
    Set<Vertex> borderVerts = new HashSet();
    Set<HalfEdge> borderHEdges = new HashSet();
    dcel.unboundCell = new Cell();
    LinkedHashMap<Point, Vertex> workinglist = new LinkedHashMap();
    createBoundaryVertsAndOuterHalfEdges(borderVerts, borderHEdges, dcel.unboundCell, pointsOnBoundary, workinglist);
    dcel.unboundCell.addInnerComponentEdges(borderHEdges.iterator().next()); // add arbitrary inner component edge
    
    // Next link each outer half-edge with the following and create and link the
    // half edge twins.
    linkOuterHalfEdgesAndCreateInnerTwins(pointsOnBoundary, workinglist, borderHEdges);
    
    // for each point/vert, if it has an infinite edge associated
    // with it, tie it in with the inner half-edges that originate and arrive at
    // that vert and set the inner half-edge faces to equal that of the infinite
    // half-edges they link to.  Otherwise link the inner half-edges together.
    completeInnerHalfEdgeConnections(pointsOnBoundary, workinglist);    
    
    // Check that all inner-boundary hEdges have a valid face reference, else
    // iterate through previous hEdges in the cylce to find the appropriate.
    checkFacesOnInnerEdges(borderVerts);
    
    dcel.cellList.add(dcel.unboundCell);
    dcel.hEdgeList.addAll(borderHEdges);
    dcel.vertexList.addAll(borderVerts);    
  } // End of finalizeDCEL method ----------------------------------------------
  
  
  private HashMap<HalfEdge, Ray> getBoundsIntersectingEdges() {
    HashMap<HalfEdge, Ray> intersectingHEdges = new HashMap<>();
    Set<HalfEdge> outOfBoundEdges = new HashSet();
    Set<HalfEdge> workSet = new HashSet<>();
    workSet.addAll(dcel.hEdgeList);
    
    for(HalfEdge e : dcel.hEdgeList) {
      if(workSet.contains(e)) {
        AbstractLine2D line = e.getLine();
        
        int result = line.rectangleIntersection(vm.bounds);
        switch(result) {
          case -1:  // case of segment completely within rectangle, do nothing.
            break;
          case  0:  // case of intersetion
            if(e.getOrigin() == null) {
              if(e.getTwin() == null) {
                // case of halfedges composing infinite line
                intersectingHEdges.put(e, new Ray(line.p, e.getdirection()));
                intersectingHEdges.put(e.getTwin(), new Ray(line.p, e.getTwin().getdirection()));
              } else {
                // case of ray represented by twin of this edge.
                if(Calc.isPointWithinRectangle(e.getTwin().getOrigin().getPoint(), vm.bounds)) {
                  // where twin's origin is within bounds.
                  intersectingHEdges.put(e.getTwin(), new Ray(line.p, e.getdirection().opposite()));
                } else {
                  // where ray represented by these half-edges is outside of bounds.
                  Point a, b, mid;
                  a = e.getFace().getOriginSite();
                  b = e.getTwin().getFace().getOriginSite();
                  mid = new Point((a.x + b.x)/2, (a.y + b.y)/2);
                  intersectingHEdges.put(e.getTwin(), new Ray(mid, e.getTwin().getdirection()));
                  dcel.vertexList.remove(e.getTwin().getOrigin());
                }
              }
            } else {
              if(e.getTwin().getOrigin() == null) {
                // case of ray represented by this edge
                if(Calc.isPointWithinRectangle(e.getOrigin().getPoint(), vm.bounds)) {
                  // where ray's origin is within bounds
                  intersectingHEdges.put(e, (Ray)line);
                } else {
                  // where ray's origin is out of bounds
                  Point a, b, mid;
                  a = e.getFace().getOriginSite();
                  b = e.getTwin().getFace().getOriginSite();
                  mid = new Point((a.x + b.x)/2, (a.y + b.y)/2);
                  intersectingHEdges.put(e, new Ray(mid, e.getdirection()));
                  dcel.vertexList.remove(e.getOrigin());
                }
              } else {
                // case of segment,
                if(Calc.isPointWithinRectangle(e.getOrigin().getPoint(), vm.bounds)) {
                  // where this half-edge origin is within bounds.
                  intersectingHEdges.put(e, new Ray(e.getOrigin().getPoint(), e.getdirection()));
                  dcel.vertexList.remove(e.getTwin().getOrigin());
                  
                } else if(Calc.isPointWithinRectangle(e.getTwin().getOrigin().getPoint(), vm.bounds)) {
                  // where twin's origin is within bounds.
                  intersectingHEdges.put(e.getTwin(), new Ray(e.getTwin().getOrigin().getPoint(), e.getTwin().getdirection()));
                  dcel.vertexList.remove(e.getOrigin());
                  
                } else {
                  // where both segment endpoints are outside the rect it intersects with.
                  Point a, b, mid;
                  a = e.getFace().getOriginSite();
                  b = e.getTwin().getFace().getOriginSite();
                  mid = new Point((a.x + b.x)/2, (a.y + b.y)/2);
                  intersectingHEdges.put(e, new Ray(mid, e.getdirection()));
                  intersectingHEdges.put(e.getTwin(), new Ray(mid, e.getTwin().getdirection()));
                  dcel.vertexList.remove(e.getOrigin());
                  dcel.vertexList.remove(e.getTwin().getOrigin());
                }
              }
            }
            break;
          case  1: // case line is completely out of bounds and not intersecting rect.
            if(e.getOrigin() != null) {
              if(e.getTwin().getOrigin() != null) {
                // line is segment
              }
              // half-edge w/origin representing ray, needs to be removed.
              dcel.vertexList.remove(e.getOrigin());
              // make sure edges referenced by respective faces are not out the hEdges represented this.
            }
            if(e.getTwin().getOrigin() != null) {
              dcel.vertexList.remove(e.getTwin().getOrigin());
            }
            outOfBoundEdges.add(e);
            outOfBoundEdges.add(e.getTwin());
            break;
          }
        workSet.remove(e.getTwin());
        }
      }
    
//    fixFaceReferences(outOfBoundEdges);
    dcel.hEdgeList.removeAll(outOfBoundEdges);
    Iterator<HalfEdge> it = intersectingHEdges.keySet().iterator();
    HalfEdge e;
    while(it.hasNext()) {
      e = it.next();
      e.getFace().setOuterComponentEdge(e);
    }
    return intersectingHEdges;
  }
  
  /**
   * Returns true if the point x lies to the left of the line traced from point
   * a to point b.
   *
   * @param a
   * @param b
   * @param x
   * @return
   */
  private boolean isLeft(Point a, Point b, Point x) {
    return ((b.x - a.x) * (x.y - a.y) - (b.y - a.y) * (x.x - a.x) > 0);
  }

  // Returns a Rectangle that encompasses all the Vertices generated by the
  // Voronoi tessalation.
  private Rectangle getBoundingBox() {
    double left, top, right, bot; // coordinate values for the box.
    ArrayList<Point> pts = new ArrayList();
    
    // Put all the points from vertices and cell origins in one list
    dcel.vertexList.stream().forEach(v->{
      pts.add(v.getPoint());
    });
    dcel.cellList.stream().forEach(c->{
      pts.add(c.getOriginSite());
    });
    // set the initial values to that of an arbitrary point in the set.
    Iterator<Point> it = pts.iterator();
    Point p = it.next();
    
    left = leftBounds;
    top = topBounds;
    right = rightBounds;
    bot = bottomBounds;
    
    // update each value to encompass all the vertices in the set
    while(it.hasNext()) {
      p = it.next();
      
      if(left > p.x)
        left = p.x;
      if(right < p.x)
        right = p.x;
      if(top < p.y)
        top = p.y;
      if(bot > p.y)
        bot = p.y;
    }
       
    return new Rectangle(left, bot, right - left, top - bot);    
  }

  private void processIntersectionsWithEdge(Iterator it, TreeMap<Point, HalfEdge> intersectionPoints, LineSegment2D segment) {
    intersectionPoints.put(segment.p, null); // We will add the corner point of the bounds with hEdge reference.
    while(it.hasNext()) {
      HashMap.Entry<HalfEdge, Ray> entry = (Map.Entry)it.next();
      Object p = segment.getIntersection(entry.getValue());
      if((p != null) && (p instanceof Point)) {
        intersectionPoints.put((Point)p, entry.getKey());
      }
    }
  }

  private LinkedHashMap<Point, HalfEdge> getPointsOnBoundary(HashMap<HalfEdge, Ray> infHalfEdges) {
    // Create a bounding box that will become the initial bounds of our dcel.
    Rectangle bounds = getBoundingBox();

    // We will test the intersection between each infinite half-edge and the
    // bounding box edges and store any intersection points in the appropriate
    // list.
    LineSegment2D lSeg, tSeg, rSeg, bSeg;
    TreeMap<Point, HalfEdge> left, top, right, bottom;
    
    left = new TreeMap();
    lSeg = bounds.getLeftEdgeSegment();
    processIntersectionsWithEdge(infHalfEdges.entrySet().iterator(), left, lSeg);
    
    top = new TreeMap();
    tSeg = bounds.getTopEdgeSegment();
    processIntersectionsWithEdge(infHalfEdges.entrySet().iterator(), top, tSeg);
    
    right = new TreeMap();
    rSeg = bounds.getRightEdgeSegment();
    processIntersectionsWithEdge(infHalfEdges.entrySet().iterator(), right, rSeg);
    
    bottom = new TreeMap();
    bSeg = bounds.getBottomEdgeSegment();
    processIntersectionsWithEdge(infHalfEdges.entrySet().iterator(), bottom, bSeg);
    
    // Add all the entries into a single linked hashmap so that each Key Point
    // is followed by the next in clockwise order around the bounding box.
    LinkedHashMap<Point, HalfEdge> pointsOnBoundary = new LinkedHashMap<>();
    pointsOnBoundary.putAll(left.descendingMap());
    pointsOnBoundary.putAll(top.descendingMap());
    pointsOnBoundary.putAll(right);
    pointsOnBoundary.putAll(bottom);
    
    return pointsOnBoundary;     
  } // End of get PointsOnBoundsMethod -----------------------------------------

  private void createBoundaryVertsAndOuterHalfEdges(Set<Vertex> borderVerts, Set<HalfEdge> borderHEdges, Cell unbound, LinkedHashMap<Point, HalfEdge> pointsOnBoundary, LinkedHashMap<Point, Vertex> workinglist) {
    Iterator<Point> it = pointsOnBoundary.keySet().iterator();
    while(it.hasNext()) {
      Point p = it.next();
      Vertex v = new Vertex(p);
      HalfEdge e = new HalfEdge(v);
      e.setFace(unbound);
      v.setEdge(e);
      borderVerts.add(v);
      borderHEdges.add(e);
      workinglist.put(p, v);
    }
  }// End of createBoundaryVerts and OuterHalfEdges method --------------------

  private void linkOuterHalfEdgesAndCreateInnerTwins(LinkedHashMap<Point, HalfEdge> pointsOnBoundary, LinkedHashMap<Point, Vertex> workinglist, Set<HalfEdge> borderHEdges) {
    Iterator<Point> it = pointsOnBoundary.keySet().iterator(); // tracks whether theres a infinite hEdge crossing this point
    Vertex currentVert, followingVert;  // refs to the current and following verts in the list of points around the boundary.
    HalfEdge curVertEdge, twin;         // refs to the outer hEdge originating from vert and it's twin
    Point first, last;                  // refs to first and last point on the list of points around the boundary
    first = last = null;

    if(it.hasNext()) {
      Point p = it.next();
      if(first == null) { // initialize this with the first point on the boundary
        first = p;
      }
      while(it.hasNext()) { // work with two sequential verts at a time
        currentVert = workinglist.get(p);
        p = it.next();
        followingVert = workinglist.get(p);
        curVertEdge = currentVert.getEdge();
        curVertEdge.setNext(followingVert.getEdge());     // link this outer edge with the next.
        twin = new HalfEdge(followingVert, curVertEdge);  // create and link twin for this outer hEdge.
        borderHEdges.add(twin);                           // save new twin (inner hEdge).
        last = p;                                         // initialize with last point in list.
      }      
    }
    // tie the first and last points
    currentVert = workinglist.get(last);
    followingVert = workinglist.get(first);
    curVertEdge = currentVert.getEdge();
    curVertEdge.setNext(followingVert.getEdge());     // link last outer hEdge with next (first).
    twin = new HalfEdge(followingVert, curVertEdge);  // create and link twin for this outer hEdge.
    borderHEdges.add(twin);                           // save new hEdge (inner twin).
    
  } // end of linkOuterHalfEdgesAndCreateInnerTwins method ---------------------

  private void completeInnerHalfEdgeConnections(LinkedHashMap<Point, HalfEdge> pointsOnBoundary, LinkedHashMap<Point, Vertex> workinglist) {
    Iterator<Point> it = pointsOnBoundary.keySet().iterator();
    Point p;
    Vertex currentVert;
    HalfEdge origEdge,      // inner bounds half edge with origin at current Vert
             destEdge,      // inner bounds half edge with destination at curren Vert
             leftInfEdge,   // inf half-edge with destination at current Vert
             rightInfEdge;  // inf half-edge with origin at current Vert
    
    while(it.hasNext()) {
      p = it.next();
      currentVert = workinglist.get(p);
      origEdge = currentVert.getEdge().getPrev().getTwin();
      destEdge = currentVert.getEdge().getTwin();
      leftInfEdge = pointsOnBoundary.get(p);
      if(leftInfEdge != null) {
        rightInfEdge = leftInfEdge.getTwin();
        leftInfEdge.setNext(origEdge);
        origEdge.setFace(leftInfEdge.getFace());
        rightInfEdge.setOrigin(currentVert);
        destEdge.setNext(rightInfEdge);
        destEdge.setFace(rightInfEdge.getFace());
      } else {
        destEdge.setNext(origEdge);
        destEdge.setFace(origEdge.getFace());
      }
      it.remove();
    } 
  } // End of completeInnerHalfEdgeConnections method --------------------------

  private boolean withinBounds(Point p) {
    return  (p.x > leftBounds) &&
            (p.x < rightBounds) &&
            (p.y > bottomBounds) &&
            (p.y < topBounds);
  }

  private void checkFacesOnInnerEdges(Set<Vertex> borderVerts) {
    
    borderVerts.stream().forEach(v->{
      HalfEdge inner, current;
      inner = current = v.getEdge().getTwin();
      while(current.getFace() == null) {
        current = current.getPrev();
      }
      inner.setFace(current.getFace());
    });
  }

  private boolean pointIsWithinBounds(Point p) {
    return     p.x >= leftBounds
            && p.x <= rightBounds
            && p.y >= bottomBounds
            && p.y <= topBounds;
  }

  public boolean isRayCrossingBounds(Ray r) {
    
    // First check if at least two of the points are in front of the Ray (left
    // of it's rightside normal.  If so, verify that you have at least one 
    // each of those points to the left and to the right of the Ray's direction.
    Point a, b, c, d, norm;
    norm = r.dir.add(r.p);
    ArrayList<Point> inFront = new ArrayList<>();
    a = new Point(leftBounds, bottomBounds);
    b = new Point(leftBounds, topBounds);
    c = new Point(rightBounds, topBounds);
    d = new Point(rightBounds, bottomBounds);
    
    if(Calc.isLeft(r.p, norm, a) <= 0) {
      inFront.add(a);
    }
    if(Calc.isLeft(r.p, norm, b) <= 0) {
      inFront.add(b);
    }
    if(Calc.isLeft(r.p, norm, c) <= 0) {
      inFront.add(c);
    }
    if(Calc.isLeft(r.p, norm, d) <= 0) {
      inFront.add(d);
    }
    if(inFront.size() < 2) {
      return false;
    }
    boolean left, right;
    left = right = false;
    for(Point pt : inFront) {
      if(Calc.isLeft(r.p, r.dir.add(r.p), pt) < 0) {
        left = true;
      } else if (Calc.isLeft(r.p, r.dir.add(r.p), pt) > 0){
        right = true;
      }
    }
    return left && right;
  }

  private HalfEdge findPrevLinkedHalfEdgeWithinBounds(HalfEdge e, Set<HalfEdge> set) {
    boolean stillLooking = true;
    HalfEdge candidate = e;
    while((candidate.getPrev() != null) && stillLooking) {
      candidate = candidate.getPrev();
      if(!set.contains(candidate)) {
        stillLooking = false;
      }
    }
    if(stillLooking) {
      return null;
    } else {
      return candidate;
    }
  }
  
  private HalfEdge findNextLinkedHalfEdgeWithinBounds(HalfEdge e, Set<HalfEdge> set) {
    boolean stillLooking = true;
    HalfEdge candidate = e;
    while((candidate.getNext() != null) && stillLooking) {
      candidate = candidate.getNext();
      if(!set.contains(candidate)) {
        stillLooking = false;
      }
    }
    if(stillLooking) {
      return null;
    } else {
      return candidate;
    }
  }
  
  private HalfEdge findLinkedHalfEdgeWithinBounds(HalfEdge e, Set<HalfEdge> set) {
    // try backwards
    HalfEdge candidate = findPrevLinkedHalfEdgeWithinBounds(e, set);
    if(candidate == null) { // if not successful, try forwards
      candidate = findNextLinkedHalfEdgeWithinBounds(e, set);
    }
    
    if(candidate == null) {
      throw new IllegalArgumentException("Could not find linked segment within bounds!");
    }
    return candidate;
  }

  private void fixFaceReferences(Set<HalfEdge> outOfBoundEdges) {
    
  }

} // end of FortuneBST class ///////////////////////////////////////////////////

