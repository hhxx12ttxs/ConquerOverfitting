/*
 * Copyright (C) 2014 Marco
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.masteriti.geometry.dcel.overlay;

import com.masteriti.geometry.Calc;
import com.masteriti.geometry.LineSegment2D;
import com.masteriti.geometry.Point;
import com.masteriti.geometry.dcel.HalfEdge;
import com.masteriti.geometry.dcel.Vertex;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Marco
 */
public class OverlayStatusTree {
  private Double sweepline;
  private Double nextSweepline;
  private TreeSet<EventData> set;

  public OverlayStatusTree() {
    System.out.println("*** StatusTree constructor called***");
    Comparator<EventData> sweeplineComparator = new Comparator<EventData>() {
      @Override
      public int compare(EventData d1, EventData d2) {
        LineSegment2D s1;
        LineSegment2D s2;
        s1 = d1.getLineSegment();
        s2 = d2.getLineSegment();
        // if they are the same object
        if (s1.equals(s2)) {
          return 0;
        }
        Double x1;
        Double x2;
        x1 = s1.getXatY(sweepline);
        x2 = s2.getXatY(sweepline);
        // Check for degenerate case ( one or both Line segments are horizontal).
        if (x1.isNaN() || x2.isNaN()) {
          if (x1.isNaN() && x2.isNaN()) {
            // Both segments are horizontal.
            return compareTwoHorizontalSegments(d1, d2);
          } else {
            // only one of the two segmets is horizontal, find which one.
            int returnValue;
            EventData horizontalSegmentData;
            double intersectionPointX;
            if (x1.isNaN()) {
              horizontalSegmentData = d1;
              returnValue = -1;
              intersectionPointX = x2;
            } else {
              horizontalSegmentData = d2;
              returnValue = 1;
              intersectionPointX = x1;
            }
            if (horizontalSegmentData.hEdge.getOrigin().getPoint().x >= intersectionPointX) {
              // d1 hEdge left end point it to the right of d2 (or coincides with)
              return -returnValue;
            } else if (d1.hEdge.getTwin().getOrigin().getPoint().x <= intersectionPointX) {
              // d1 hEdge's right end-point is located left of d2 (or coincides)
              return returnValue;
            } else {
              // d2 must be within d1, split it at the intersection point.
              HalfEdge rightHEdgeTop;
              HalfEdge leftHEdgeBottom;
              HalfEdge rightHEdgeBottom;
              HalfEdge leftHEdgeTop;
              leftHEdgeTop = horizontalSegmentData.hEdge;
              rightHEdgeBottom = horizontalSegmentData.hEdge.getTwin();
              Vertex v1 = new Vertex(new Point(intersectionPointX, sweepline));
              d2.queue.work.result.vertexList.add(v1);
              rightHEdgeTop = new HalfEdge(v1, rightHEdgeBottom, leftHEdgeTop.getFace());
              leftHEdgeBottom = new HalfEdge(v1, leftHEdgeTop, rightHEdgeBottom.getFace());
              d2.queue.work.result.hEdgeList.add(rightHEdgeTop); // Add new hEdges to result set
              d2.queue.work.result.hEdgeList.add(leftHEdgeBottom);
              rightHEdgeTop.setNext(leftHEdgeTop.getNext());
              rightHEdgeTop.getNext().setPrev(rightHEdgeTop);
              rightHEdgeTop.setPrev(leftHEdgeTop);
              rightHEdgeTop.getPrev().setNext(rightHEdgeTop);
              leftHEdgeBottom.setNext(rightHEdgeBottom.getNext());
              leftHEdgeBottom.getNext().setPrev(leftHEdgeBottom);
              leftHEdgeBottom.setPrev(rightHEdgeBottom);
              leftHEdgeBottom.getPrev().setNext(leftHEdgeBottom);
              rightHEdgeBottom.setTwin(rightHEdgeTop);
              leftHEdgeTop.setTwin(leftHEdgeBottom);
              horizontalSegmentData.queue.insertNewHalfEdge(rightHEdgeTop, horizontalSegmentData.originalSet);
              horizontalSegmentData.queue.insertNewHalfEdge(leftHEdgeBottom, horizontalSegmentData.originalSet);
              return returnValue;
            }
          }
        }
        // Neither segment is horizontal, handle normally.
        final Point p1 = new Point(s1.getXatY(sweepline), sweepline);
        final Point p2 = new Point(s2.getXatY(sweepline), sweepline);
        if (compare2Points(p1, p2) < 0) {
          return -1;
        } else if (compare2Points(p1, p2) > 0) {
          return 1;
        } else {
          // Points coincide, compare top end points then bottom end points,
          // but make sure they're not the same as the current event point.
          Point b1 = s1.getTopPoint();
          Point b2 = s2.getTopPoint();
          if ((compare2Points(b1, p1) != 0) && (compare2Points(b2, p1) != 0)) {
            if (compare2Points(b1, b2) < 0) {
              return -1;
            } else {
              return 1;
            }
          } else {
            // Try bottom points
            b1 = s1.getBottomPoint();
            b2 = s2.getBottomPoint();
            if (compare2Points(b1, b2) < 0) {
              return -1;
            } else {
              return 1;
            }
          }
        }
      }

      private int compare2Points(Point p1, Point p2) {
        if (p1.x < p2.x) {
          return -1;
        } else if (p1.x > p2.x) {
          return 1;
        } else {
          return 0;
        }
      }

      private int compareTwoHorizontalSegments(EventData d1, EventData d2) {
        LineSegment2D s1;
        LineSegment2D s2;
        s1 = d1.getLineSegment();
        s2 = d2.getLineSegment();
        // We need to check if and how they overlap
        Object result = Calc.getIntersection(s1, s2);
        if (result == null) {
          // Case of no overlap.
          if (s1.getPointAx() < s2.getPointAx()) {
            return -1;
          } else {
            return 1;
          }
        } else if (result instanceof Point) {
          // The segments happen to have one point coincident (i.e. they're right
          // next to each other.
          if (s1.getLeftMostXBoundary() < s2.getLeftMostXBoundary()) {
            return -1;
          } else {
            return 1;
          }
        } else {
          /**
           *  SEGMENTS OVERLAP - There are 3 possible solutions to the overlap:
           *  1.the have matching end points and overlap exactly (s1 == s2)
           *    resulting so that the resulting segment of the overlap is congruent
           *    two the original segments.
           *
           *    Eliminate s2 from the DCEL, substitute the old segment on
           *    the map by return 0 for the comparison.  We don't need to touch the
           *    DCEL; by replacing s2 with s1, we remove s2 origin from the DCEL
           *    computation, which means when it comes to tieing in the relationship
           *    around the vertex at that point, they'll tie into s2 and s1 will be
           *    garbage bait.
           *
           *  2.the segments have just one of the end points in common:
           *
           *    Here we will retain both segments, but modify the larger on so
           *    that it origin is the destination of the smaller one (if the
           *    common endpoint is on the left, or vice versa if the common end
           *    point is on the right.  we also update the sets the two segments
           *    belong to as the resulting set (this will allow these segments to
           *    be considered intersection candidates with segments from both
           *    input sets) and then return the comparison value according to
           *    s1 position with respect to s2.
           *
           *  3.the segments have no end-points in common. The result be 3
           *    distinct segments.  There are two possible cases: both end points
           *    of one segments are between those of the other segment, or only
           *    one end point of each segment is between the end points of the
           *    other.  Either way we end up with a series of 4 points on the
           *    same y coordinate line p0, p1, p2, p3 ascending order of x
           *
           *    In the case that on segment is completely encompassed by the other,
           *    we only need to split the larger segment into two separate segments,
           *    one that comes before the smaller one, and a new one that comes
           *    right after it. Let sBg and sSm be the larger encompassing and
           *    smaller encompassed segments respectively.  sBg will keep p0 as
           *    it's left point but update p1 as it's right end point. sSm will not
           *    change at all, keeping p1 as it's left endpoint and p2 as it's
           *    right end point. sNw will be a newly created segment that will
           *    have p2 as it's left endpoint and p3 as it's right end point.
           *    sNw will then be inserted into the Queue as an event belonging to
           *    the new resulting set(in order for it to intersect with adjoining
           *    edges).
           *
           *    In the other case, let sLt be the segment with it's left-most
           *    end point furthest to the left, and sRt the other segment and sNw
           *    the new segment. sLt will keep p0 as it's left end point but will
           *    update it's right endpoint to p1.  sRt will keep p1 as it's left
           *    end point but update it's right endpoint to p2. sNw will be the
           *    newly created segment with p2 as it's left end point and p3 as
           *    it's right end point.
           */
          TreeSet<Point> points = new TreeSet(); // set will order points in descending value of x.
          points.add(s1.getPointA());
          points.add(s1.getPointB());
          points.add(s2.getPointA());
          points.add(s2.getPointB());
          // determine what case we're dealing with
          switch (points.size()) {
            case 2:
              // s1 and s2 coincide perfectly. We simply replace s2 with s1.
              d1.originalSet = null;
              return 0;
            case 3:
              // s1 and s2 have a point in common.
              return compare3PointSegmentOverlap(d1, d2, points);
            case 4:
              return compare4PointSegmentOverlap(d1, d2, points);
            default:
              throw new IllegalArgumentException("overlap results in wrong number of intersection points!");
          }
        }
      }

      private int compare3PointSegmentOverlap(EventData d1, EventData d2, TreeSet<Point> points) {
        Point p0;
        Point p1;
        Point p2;
        p0 = points.pollLast();
        p1 = points.pollLast();
        p2 = points.pollLast();
        HalfEdge lgHEdge;
        HalfEdge smHEdge;
        EventData lgSegData;
        EventData smSegData;
        LineSegment2D s1;
        LineSegment2D s2;
        s1 = d1.getLineSegment();
        s2 = d2.getLineSegment();
        Vertex v1;
        int returnValue;
        if (s1.getLeftMostXBoundary() == s2.getLeftMostXBoundary()) {
          // the common end point is p0, find the larger segment
          if (s1.getRightMostXBoundary() < s2.getRightMostXBoundary()) {
            // s1 is smaller than s2.
            lgSegData = d2;
            smSegData = d1;
            returnValue = -1;
          } else {
            // s2 is smaller than s1.
            lgSegData = d1;
            smSegData = d2;
            returnValue = 1;
          }
          // Update larger segment so that it has vertices at p1 and p2 while
          // the smaller segment remains unchanged with vertices at p0 and p1.
          lgHEdge = lgSegData.hEdge;
          if (lgHEdge.getOrigin().getPoint() != p0) {
            lgHEdge = lgHEdge.getTwin();
            if (lgHEdge.getOrigin().getPoint() != p0) {
              throw new IllegalArgumentException("compare3PointSegmentOverlap: have no points matching left origin of largest segment!");
            }
          }
          smHEdge = smSegData.hEdge;
          if (smHEdge.getOrigin().getPoint() != p1) {
            smHEdge = smHEdge.getTwin();
          }
          if (smHEdge.getOrigin().getPoint() != p1) {
            throw new IllegalArgumentException("compare3PointSegmentOverlap: have no points matching left origin of smallest segment!");
          }
          v1 = smHEdge.getOrigin();
          changeOriginsEdgeRef(lgHEdge);
          lgHEdge.setOrigin(v1);
          d1.originalSet = null;
          d2.originalSet = null;
          return returnValue;
        } else {
          // The common end point is p2
          if (s1.getLeftMostXBoundary() > s2.getLeftMostXBoundary()) {
            // s1 is smaller than s2.
            lgSegData = d2;
            smSegData = d1;
            returnValue = 1;
          } else {
            // s2 is smaller than s1.
            lgSegData = d1;
            smSegData = d2;
            returnValue = -1;
          }
          // Update larger segment right end point is at p1. smaller segment
          // remains unchanged. Result: p0-lgSegment-p1-smSegment-p2.
          lgHEdge = lgSegData.hEdge;
          if (lgHEdge.getOrigin().getPoint() != p2) {
            lgHEdge = lgHEdge.getTwin();
            if (lgHEdge.getOrigin().getPoint() != p2) {
              throw new IllegalArgumentException("compare3PointSegmentOverlap: have no points matching left origin of largest segment!");
            }
          }
          smHEdge = smSegData.hEdge;
          if (smHEdge.getOrigin().getPoint() != p1) {
            smHEdge = smHEdge.getTwin();
          }
          if (smHEdge.getOrigin().getPoint() != p1) {
            throw new IllegalArgumentException("compare3PointSegmentOverlap: have no points matching left origin of smallest segment!");
          }
          v1 = smHEdge.getOrigin();
          changeOriginsEdgeRef(lgHEdge);
          lgHEdge.setOrigin(v1);
          d1.originalSet = null;
          d1.originalSet = null;
          return returnValue;
        }
      }

      private int compare4PointSegmentOverlap(EventData d1, EventData d2, Set<Point> points) {
        // There are two possible cases that provide a result of 4 distinct points:
        // The two segment are either staggered or one is completely encompassed by
        // the other.
        LineSegment2D s1;
        LineSegment2D s2;
        s1 = d1.getLineSegment();
        s2 = d2.getLineSegment();
        EventData leftSegData;
        EventData centerSegData;
        EventData rightSegData;
        HalfEdge leftHEdgeTop;
        HalfEdge leftHEdgeBottom;
        HalfEdge centerHEdgeTop;
        HalfEdge centerHEdgeBottom;
        HalfEdge rightHEdgeTop;
        HalfEdge rightHEdgeBottom;
        int returnValue;
        boolean isSegmentEncompassed;
        Vertex v0;
        Vertex v1;
        Vertex v2;
        Vertex v3;
        // find how the segments overlap and which is to the left of the other.
        if (s1.getLeftMostXBoundary() < s2.getLeftMostXBoundary()) {
          leftSegData = d1;
          centerSegData = d2;
          returnValue = -1;
          isSegmentEncompassed = s1.getRightMostXBoundary() >= s2.getRightMostXBoundary();
        } else {
          leftSegData = d2;
          centerSegData = d1;
          returnValue = 1;
          isSegmentEncompassed = s1.getRightMostXBoundary() < s2.getRightMostXBoundary();
        }
        // find the left half-edge with origin at v0
        leftHEdgeTop = (leftSegData.hEdge.getOrigin().getPoint().x < leftSegData.hEdge.getTwin().getOrigin().getPoint().x) ? leftSegData.hEdge : leftSegData.hEdge.getTwin();
        // identify v0
        v0 = leftHEdgeTop.getOrigin();
        // find the center half edge with origin at v1
        centerHEdgeTop = (centerSegData.hEdge.getOrigin().getPoint().x < centerSegData.hEdge.getTwin().getOrigin().getPoint().x) ? centerSegData.hEdge : centerSegData.hEdge.getTwin();
        v1 = centerHEdgeTop.getOrigin();
        if (isSegmentEncompassed) {
          centerHEdgeBottom = centerHEdgeTop.getTwin();
          rightHEdgeBottom = leftHEdgeTop.getTwin();
        } else {
          centerHEdgeBottom = leftHEdgeTop.getTwin();
          rightHEdgeBottom = centerHEdgeTop.getTwin();
        }
        v2 = centerHEdgeBottom.getOrigin();
        v3 = rightHEdgeBottom.getOrigin();
        // Either case requires a new segment (two new half-edges). One hEdge
        // with origin at v2 and one at v1.
        rightHEdgeTop = new HalfEdge(v2, rightHEdgeBottom);
        leftHEdgeBottom = new HalfEdge(v1, leftHEdgeTop);
        d2.queue.work.result.hEdgeList.add(rightHEdgeTop); // Add new hEdges to result set
        d2.queue.work.result.hEdgeList.add(leftHEdgeBottom);
        // fix the connections between edges
        rightHEdgeBottom.setTwin(rightHEdgeTop);
        leftHEdgeTop.setTwin(leftHEdgeBottom);
        if (!isSegmentEncompassed) {
          centerHEdgeBottom.setTwin(centerHEdgeTop);
          centerHEdgeTop.setTwin(centerHEdgeBottom);
        }
        // add new half-edges to the queue
        d1.queue.insertNewHalfEdge(rightHEdgeTop, null);
        d1.queue.insertNewHalfEdge(leftHEdgeTop, null);
        // update the EventData of other segment for intersection processing
        d1.originalSet = null;
        d2.originalSet = null;
        return returnValue;
      }

      /**
       * Checks if the origin attribute for this hEdge has this hEdge
       * referenced as it's incident half edge. If so, change it to another one.
       * @param lgHEdge
       */
      private void changeOriginsEdgeRef(HalfEdge lgHEdge) {
        if (lgHEdge.getOrigin().getEdge() == lgHEdge) {
          // We don't want it the old origin vertex to reference this edge anymore.
          if (lgHEdge.getPrev().getTwin() != lgHEdge) {
            // If the current origin vertex has other half edges adjacent to it,
            // change the attribute to point to one of them.
            lgHEdge.getOrigin().setEdge(lgHEdge.getPrev().getTwin());
          } else {
            lgHEdge.getOrigin().setEdge(null);
          }
        }
      }
    }; // end of Comparator for StatusTree map
    set = new TreeSet(sweeplineComparator);
    System.out.println("=== StatusTree Constructor done ===");
  }

  public Double getSweeplinePos() {
    return sweepline;
  }

  public void setSweeplinePos(Double pos) {
    this.sweepline = pos;
  }

  public void put(EventData data) {
    if (set.add(data)) {
      System.out.println("\tAdded segment" + data.getLineSegment().printPointsToString() + " into T.");
    } else {
      System.out.println("\tFailed to add " + data.getLineSegment().printPointsToString() + " into T.");
    }
  }

  public EventData lowerEntry(EventData data) {
    if (set.lower(data) == null) {
      return null;
    } else {
      return set.lower(data);
    }
  }

  public EventData higherEntry(EventData data) {
    if (set.higher(data) == null) {
      return null;
    } else {
      return set.higher(data);
    }
  }

  public boolean remove(EventData data) {
    if (set.remove(data)) {
      System.out.println("\tRemoved segment " + data.hEdge.getSegment().printPointsToString() + " successfully");
      return true;
    } else {
      System.out.println("\t/////Removal for " + data.hEdge.hashCode() + " for segment" + data.hEdge.getSegment().printPointsToString() + " failed./////");
      return false;
    }
  }

  public void setNextSweeplinePos(Double nextSweeplinePosition) {
    this.nextSweepline = nextSweeplinePosition;
  }

  public int size() {
    return set.size();
  }

  public EventData firstEntry() {
    return set.first();
  }
  
} // End of StatusTree class

