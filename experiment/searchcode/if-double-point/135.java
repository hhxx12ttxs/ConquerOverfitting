<<<<<<< HEAD
package sounder.pig.points;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.BagFactory;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.apache.pig.impl.util.Utils;

/**
   Constructs a k-d tree from the passed in databag containing
   points. NOTE: This is intended as a proof-of-concept and is
   unlikely to be production worthy.
 */
public class KDTree extends EvalFunc<DataBag> {
    private static Comparator<KDPoint> comparators[];
    private static final Integer ID_FIELD = 0;
    private static final Integer IS_ROOT_FIELD = 1;
    private static final Integer AXIS_FIELD = 2;
    private static final Integer ABOVE_CHILD_FIELD = 3;
    private static final Integer BELOW_CHILD_FIELD = 4;
    private static final Integer POINT_FIELD = 5;
    
    public DataBag exec(Tuple input) throws IOException {
        if (input == null || input.size() < 1 || input.isNull(0)) { return null; }

        DataBag points = (DataBag)input.get(0);       // {(id, point:(x1,x2,...,xK))}
        KDPoint[] asPoints = toPoints(points);

        return generateTree(asPoints);        
    }

    /**
       Check if the input tuple can make a valid KDPoint object
     */
    private boolean isValidPoint(Tuple t) throws ExecException {
        if (t.isNull(0) || t.isNull(1)) { return false; }
        return true;
    }

    /**
       Construct an array of KDPoint objects from the passed in DataBag
       of tuples
     */
    private KDPoint[] toPoints(DataBag points) throws ExecException {
        KDPoint[] result = new KDPoint[((Long)points.size()).intValue()];
        int idx = 0;
        for (Tuple t : points) {
            if (isValidPoint(t)) {
                result[idx] = new KDPoint(t);
                idx++;
            }
        }
        return result;
    }

    /**
       Recursively generate a k-d tree from the passed in array of points
     */
    private DataBag generateTree(KDPoint[] points) throws ExecException {
        if (points.length == 0) { return null; }

        int maxD = points[0].getDimensionality();
        comparators = new Comparator[maxD];
        for (int i = 0; i < maxD; i++) {
            comparators[i] = new KDPointComparator(i);
        }
        KDPoint root = generate(0, maxD, points, 0, points.length-1);
        root.isRoot = true;
        return root.toBag();
    }

    private KDPoint generate(int d, int maxD, KDPoint[] points, int left, int right) throws ExecException {
        if (right < left) { return null; }
        if (right == left) {
            KDPoint returnPoint = points[left];
            if (returnPoint != null) { returnPoint.setAxis(d); }
            return returnPoint;
        }

        int m = (right-left)/2;
        // Yes, sort every time. Not super efficient
        Arrays.sort(points, left, right+1, comparators[d]);

        KDPoint medianPoint = points[left+m];
        medianPoint.setAxis(d);
        
        if (++d >= maxD) { d = 0; }
        
        medianPoint.setBelowChild(generate(d, maxD, points, left, left+m-1));
	medianPoint.setAboveChild(generate(d, maxD, points, left+m+1, right));
        return medianPoint;
    }

    /**
       Set the appropriate output schema so pig doesn't get confused
     */
    public Schema outputSchema(Schema input) {
        Schema schema = null;
        try {
            schema = Utils.getSchemaFromString("result:bag{t:tuple(id:chararray, is_root:int, axis:int, above_child:chararray, below_child:chararray, point:tuple(lng:double, lat:double))}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schema;
    }
    
    /**
       Simple representation of a multi-dimensional point
     */
    class KDPoint {
        
        final String pointId;
        final int dimensionality;
        public boolean isRoot;
        protected String aboveChildId;
        protected String belowChildId;
        protected KDPoint aboveChild; // Above child, right in 2-D case
        protected KDPoint belowChild; // Below child, left in 2-D case
        protected Integer axis; // Splitting axis for this node (0 or 1) in 2-D case
        double values[];

        /**
           Construct a KDPoint from the passed in tuple representation
         */
        public KDPoint(Tuple pointTuple) throws ExecException {
            this.pointId = (String)pointTuple.get(0);
            Tuple point = (Tuple)pointTuple.get(1);
            
            int d = this.dimensionality = point.size();
            values = new double[d];
            for (int i = 0; i < d; i++) {
                values[i] = (Double)point.get(i);
            }
        }

        public KDPoint getAboveChild() { return aboveChild; }
        public String getAboveChildId() { return aboveChildId; }
        public KDPoint getBelowChild() { return belowChild; }
        public String getBelowChildId() { return belowChildId; }
        public Integer getAxis() { return axis; }
        public String getPointId() { return pointId; }
        public int getDimensionality() { return dimensionality; }
        public double getCoordinate(int d) { return values[d]; }

        public void setAboveChild(KDPoint child) {
            this.aboveChild = child;
            if (child != null) { this.aboveChildId = child.getPointId(); }
        }
        
        public void setAboveChildId(String childId) { this.aboveChildId = childId; }
        
        public void setBelowChild(KDPoint child) {
            this.belowChild = child;
            if (child != null) { this.belowChildId = child.getPointId(); }
        }
        
        public void setBelowChildId(String childId) { this.belowChildId = childId; }
        
        public void setAxis(Integer axis) { this.axis = axis; }

        public Tuple toTuple() throws ExecException {
            TupleFactory tfact = TupleFactory.getInstance();
            Tuple result = tfact.newTuple(6);
            Tuple point = tfact.newTuple(dimensionality);

            for (int i = 0; i < dimensionality; i++) {
                point.set(i, values[i]);
            }
            
            result.set(0, pointId);
            result.set(1, (isRoot ? 1 : 0));
            result.set(2, axis);
            result.set(3, aboveChildId);
            result.set(4, belowChildId);
            result.set(5, point);
            return result;
        }

        public DataBag toBag() throws ExecException {
            DataBag result = BagFactory.getInstance().newDefaultBag();
            result.add(toTuple());
            if (aboveChild != null) {
                result.addAll(aboveChild.toBag());
            }

            if (belowChild != null) {
                result.addAll(belowChild.toBag());
            }
            return result;
        }
    }

    /**
       Simple comparator class for sorting KDPoints along a particular dimension
     */
    public class KDPointComparator implements Comparator<KDPoint> {
        public final int d;
        public static final double epsilon = 1E-9;
        
        public KDPointComparator (int d) {
            this.d = d;
	}

        public int compare(KDPoint p1, KDPoint p2) {
            double d1 = p1.getCoordinate(d);
            double d2 = p2.getCoordinate(d);
            if (lesser(d1, d2)) { return -1; }
            if (same(d1, d2)) { return 0; }		
            return +1;
	}

        public double value(double x) {
            if ((x >= 0) && (x <= epsilon)) { return 0.0; }
            
            if ((x < 0) && (-x <= epsilon)) { return 0.0; }
            
            return x;
	}

        public boolean lesser(double x, double y) { return value(x-y) < 0; }

        public boolean same (double d1, double d2) {
            if (Double.isNaN(d1)) { return Double.isNaN(d2); }
            
            if (d1 == d2) { return true; }
            
            if (Double.isInfinite(d1)) { return false; }
            
            return value (d1-d2) == 0;
	}
    }
=======
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.waveprotocol.wave.client.paging;

import com.google.common.base.Preconditions;

import org.waveprotocol.wave.client.common.util.LinkedSequence;
import org.waveprotocol.wave.client.common.util.MathUtil;
import org.waveprotocol.wave.client.paging.Traverser.BlockSide;
import org.waveprotocol.wave.client.paging.Traverser.MoveablePoint;
import org.waveprotocol.wave.client.paging.Traverser.Point;
import org.waveprotocol.wave.client.paging.Traverser.SimplePoint;
import org.waveprotocol.wave.model.util.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

/**
 * Maintains a continuous active (paged-in) region within block tree.
 *
 */
public final class ActiveRegion {

  /**
   * A point defining the start of an active region.
   */
  static final class StartPoint extends MoveablePoint {

    private StartPoint(BlockSide side, Block block) {
      super(side, block);
    }

    static StartPoint startOf(Block block) {
      return new StartPoint(BlockSide.START, block);
    }

    static StartPoint endOf(Block block) {
      return new StartPoint(BlockSide.END, block);
    }

    static StartPoint nowhere() {
      return new StartPoint(null, null);
    }

    static StartPoint at(Point point) {
      return new StartPoint(point.side, point.block);
    }

    /**
     * Moves to the next point, adding any blocks left behind into a
     * collection.
     *
     * @param toExclude collector of blocks left behind
     */
    void trawlNext(Collection<? super Block> toExclude) {
      // If this point moves over a block end, that block becomes excluded.
      if (side == BlockSide.END) {
        toExclude.add(block);
      }
      next();
    }

    /**
     * Moves to the previous point, adding any new blocks included into a
     * collection.
     *
     * @param toInclude collector of new blocks
     */
    void trawlPrevious(Collection<? super Block> toInclude) {
      // If this point moves into a block end, that block becomes included.
      previous();
      if (side == BlockSide.END) {
        toInclude.add(block);
      }
    }

    /**
     * Moves through next points until the last point before a given position,
     * adding any blocks left behind into a collection.
     *
     * @param position location to move to, but not beyond
     * @param toExclude collector of new blocks encountered
     */
    void trawlNextUntil(double position, Collection<? super Block> toExclude) {
      while (nextIfBefore(position, toExclude)) {
        // Condition does the work.
      }
    }


    /**
     * Moves through previous points until the first point before a given position,
     * adding any new blocks included into a collection.
     *
     * @param position location to move to, but not beyond
     * @param toInclude collector of new blocks encountered
     */
    void trawlPreviousUntil(double position, Collection<? super Block> toInclude) {
      while (previousIfAfter(position, toInclude)) {
        // Condition does the work.
      }
    }

    /**
     * Moves to the next point, if the next point is before a position.
     *
     * @param position boundary
     * @param toExclude
     * @return true if moved.
     */
    private boolean nextIfBefore(double position, Collection<? super Block> toExclude) {
      if (!hasNext()) {
        return false;
      }

      Block oldBlock = block;
      BlockSide oldSide = side;

      next();

      // Moved too far?
      if (absoluteLocation() >= position) {
        // Oops. Backtrack.
        previous();
        return false;
      } else {
        // Moved beyond the end of a block, so that block is now excluded.
        if (oldSide == BlockSide.END) {
          toExclude.add(oldBlock);
        }
        return true;
      }
    }

    /**
     * Moves to the previous point, if this point is at, or after, a position.
     *
     * @param position boundary
     * @return true if moved.
     */
    private boolean previousIfAfter(double position, Collection<? super Block> toInclude) {
      if (hasPrevious() && absoluteLocation() >= position) {
        trawlPrevious(toInclude);
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * A point defining the end of an active region.
   */
  final static class EndPoint extends MoveablePoint {

    private EndPoint(BlockSide side, Block block) {
      super(side, block);
    }

    static EndPoint startOf(Block block) {
      return new EndPoint(BlockSide.START, block);
    }

    static EndPoint endOf(Block block) {
      return new EndPoint(BlockSide.END, block);
    }

    static EndPoint nowhere() {
      return new EndPoint(null, null);
    }

    static EndPoint at(Point point) {
      return new EndPoint(point.side, point.block);
    }

    /**
     * Moves to the previous point, adding any blocks left behind into a
     * collection.
     *
     * @param toExclude collector of blocks left behind
     */
    void trawlPrevious(Collection<? super Block> toExclude) {
      // If this point moves over a block start, that block becomes excluded.
      if (side == BlockSide.START) {
        toExclude.add(block);
      }
      previous();
    }

    /**
     * Moves to the next point, adding any new blocks included into a
     * collection.
     *
     * @param toInclude collector of new blocks
     */
    void trawlNext(Collection<? super Block> toInclude) {
      // If this point moves into a block start, that block becomes included.
      next();
      if (side == BlockSide.START) {
        toInclude.add(block);
      }
    }

    /**
     * Moves through previous points until the first point after a given
     * position, adding any blocks left behind into a collection.
     *
     * @param position location to move to, but not beyond
     * @param toExclude collector of blocks left behind
     */
    void trawlPreviousUntil(double position, Collection<? super Block> toExclude) {
      while (previousIfAfter(position, toExclude)) {
        // Condition does the work.
      }
    }

    /**
     * Moves through next points until the first point after a given position,
     * adding any new blocks included into a collection.
     *
     * @param position location to move to, but not beyond
     * @param toInclude collector of new blocks encountered
     */
    void trawlNextUntil(double position, Collection<? super Block> toInclude) {
      while (nextIfBefore(position, toInclude)) {
        // Condition does the work.
      }
    }

    /**
     * Moves to the previous point, if the previous point is after a position.
     *
     * @param position boundary
     * @param toExclude
     * @return true if moved.
     */
    private boolean previousIfAfter(double position, Collection<? super Block> toExclude) {
      if (!hasPrevious()) {
        return false;
      }

      Block oldBlock = block;
      BlockSide oldSide = side;

      previous();

      // Moved too far?
      if (absoluteLocation() <= position) {
        // Oops. Backtrack.
        next();

        return false;
      } else {
        // Moved beyond the end of a block, so that block is now excluded.
        if (oldSide == BlockSide.START) {
          toExclude.add(oldBlock);
        }
        return true;
      }
    }

    /**
     * Moves to the next point, if this point is at, or before, a position.
     *
     * @param position boundary
     * @return true if moved.
     */
    private boolean nextIfBefore(double position, Collection<? super Block> toInclude) {
      if (hasNext() && absoluteLocation() <= position) {
        trawlNext(toInclude);
        return true;
      } else {
        return false;
      }
    }
  }

  /** Start of the active region. */
  private final StartPoint start;

  /** End of the active region. */
  private final EndPoint end;

  /** Root block of pageable tree. */
  private final Block root;

  /** Paging queues. */
  private final Queue<Block> toPageIn = CollectionUtils.createQueue();
  private final Queue<Block> toPageOut = CollectionUtils.createQueue();

  private ActiveRegion(Block root, StartPoint start, EndPoint end) {
    this.root = root;
    this.start = start;
    this.end = end;
  }

  /**
   * Creates an active region.
   *
   * @param root root of the block tree to activate.
   * @return a new active region.
   */
  public static ActiveRegion over(Block root) {
    return new ActiveRegion(root, StartPoint.nowhere(), EndPoint.nowhere());
  }

  /** @return true if a region has been activated. */
  private boolean isActive() {
    return start.isActive();
  }

  /**
   * Invalidates any positions cached in the terminal points. This needs to be
   * called on all public entry points whose control flow queries positions.
   */
  private void invalidatePositionCache() {
    start.invalidateCachedOrigin();
    end.invalidateCachedOrigin();
  }

  /**
   * @return the location of the activated part of the content.
   */
  public Region getActiveRegion() {
    invalidatePositionCache();
    return isActive() ? RegionImpl.at(start.absoluteLocation(), end.absoluteLocation()) : null;
  }

  /**
   * Grows the active region at the end.
   *
   * @param n number of blocks to page in
   */
  public double growDown(int n) {
    assert toPageIn.isEmpty();  // Reminder that every public method flushes this.
    PagingDebugHelper.maybeCheckBlocks(root);
    Preconditions.checkState(isActive(), "No active region");

    while (toPageIn.size() < n && end.hasNext()) {
      end.trawlNext(toPageIn);
    }
    page();
    return 0;
  }

  /**
   * Grows the active region at the start.
   *
   * @param n number of blocks to page in
   * @return the change in
   */
  public double growUp(int n) {
    assert toPageIn.isEmpty(); // Reminder that every public method flushes this.
    PagingDebugHelper.maybeCheckBlocks(root);
    Preconditions.checkState(isActive(), "No active region");

    double oldEndLocation = end.absoluteLocation();
    while (toPageIn.size() < n && start.hasPrevious()) {
      start.trawlPrevious(toPageIn);
    }
    page();
    double newEndLocation = end.absoluteLocation();
    return newEndLocation - oldEndLocation;
  }

  /**
   * Ensures that all blocks within a given region are paged in, and all those
   * outside the region are paged out.
   *
   * @param viewport viewport region
   * @return by how much all preserved points (points that are active both
   *         before and after this method call) have shifted due to activation.
   *         If there are no preserved points, returns {@code 0}.
   */
  public double activate(Region viewport) {
    PagingDebugHelper.maybeCheckBlocks(root);
    PagingDebugHelper.enterActivate();

    Point toPreserve;  // A point whose location should be preserved through paging.
    Region active = getActiveRegion();
    if (active == null) {
      // Initialize
      Point init = Traverser.locateStartWithin(root, viewport.getStart());
      if (init == null) {
        init = Traverser.locateEndWithin(root, viewport.getStart());
      }
      init(init);
      expandTo(viewport);
      // Preserve the start of the viewport. No particular reason.
      toPreserve = start;
    } else {
      // Adjust from previous state.
      switch (OverlapKind.compare(viewport, active)) {
        case FULLY_AFTER:
          Point newStart = Traverser.locateStartAfter(end, viewport.getStart());
          shrinkToEnd();
          moveActivePoint(newStart);
          expandTo(viewport);
          toPreserve = start;
          break;
        case FULLY_BEFORE:
          Point newEnd = Traverser.locateEndBefore(start, viewport.getEnd());
          shrinkToStart();
          moveActivePoint(newEnd);
          expandTo(viewport);
          toPreserve = end;
          break;
        case ENCLOSED:
          toPreserve = SimplePoint.at(start);
          shrinkStartUntil(viewport.getStart());
          shrinkEndUntil(viewport.getEnd());
          break;
        case ENCLOSES:
          growStartUntil(viewport.getStart());
          growEndUntil(viewport.getEnd());
          toPreserve = SimplePoint.at(start);
          break;
        case INTERSECTS_BEFORE:
          toPreserve = SimplePoint.at(start);
          growStartUntil(viewport.getStart());
          shrinkEndUntil(viewport.getEnd());
          break;
        case INTERSECTS_AFTER:
          shrinkStartUntil(viewport.getStart());
          growEndUntil(viewport.getEnd());
          toPreserve = SimplePoint.at(start);
          break;
        default:
          throw new RuntimeException("invalid enum value");
      }
    }

    assert enclosesClipped(viewport);
    assert !new HashSet<Block>(toPageIn).removeAll(toPageOut);

    double shift = 0;
    if (!toPageIn.isEmpty() || !toPageOut.isEmpty()) {
      double oldLocation = toPreserve.absoluteLocation();
      page();
      double newLocation = toPreserve.absoluteLocation();
      shift += newLocation - oldLocation;

      // Paging may have changed block sizes, so another round of activation is
      // necessary.
      shift += activate(RegionImpl.at(viewport).moveBy(shift));
    } else {
      return 0;
    }

    PagingDebugHelper.leaveActivate();
    return shift;
  }

  /**
   * Initializes the active region at a point.
   *
   * @param init initial point.
   */
  private void init(Point init) {
    assert !isActive();
    // Set active ends, page in from root to initialising point.
    end.set(init);
    start.set(init);
    collectAncestors(true, toPageIn, start.block, null);
  }

  /**
   * Resets the active region to be nothing.
   */
  public void reset() {
    shrinkToStart();
    collectAncestors(false, toPageOut, start.block, null);
    end.clear();
    start.clear();
    page();
  }

  /**
   * Collects the ancestors of a block (inclusive).
   *
   * @param direction true if parents are to be collected before children; otherwise, false
   * @param list collector
   * @param block block whose ancestry is to be collected.
   * @param to  terminal (null for all)
   */
  private static void collectAncestors(boolean direction, Collection<? super Block> list,
      Block block, Block to) {
    if (block == to) {
      return;
    } else {
      if (direction) {
        collectAncestors(direction, list, block.getParent(), to);
        list.add(block);
      } else {
        list.add(block);
        collectAncestors(direction, list, block.getParent(), to);
      }
    }
  }

  /** @return true if the active region inclusively encloses a viewport. */
  private boolean enclosesClipped(Region viewport) {
    Region clipped = RegionImpl.at( // \u2620
        MathUtil.clip(root.getStart(), root.getEnd(), viewport.getStart()), // \u2620
        MathUtil.clip(root.getStart(), root.getEnd(), viewport.getEnd())); // \u2620
    Region active = getActiveRegion();
    return OverlapKind.compare(active, clipped) == OverlapKind.ENCLOSES;
  }

  /**
   * Shrinks the active region to its start point.
   */
  private void shrinkToStart() {
    // Shrink active region to a point.
    while (!end.equals(start)) {
      end.trawlPrevious(toPageOut);
    }
  }

  /**
   * Shrinks the active region to its end point.
   */
  private void shrinkToEnd() {
    // Shrink active region to a point.
    while (!start.equals(end)) {
      start.trawlNext(toPageOut);
    }
  }

  /**
   * Moves the active region (assumed to be collapsed) to another point. Unique
   * ancestors of the current point are paged out, and unique ancestors of the
   * new point are paged in.
   *
   * @param point new active point
   */
  private void moveActivePoint(Point point) {
    assert start.equals(end);

    // To find the unique ancestors of the old point, and the unique ancestors
    // of the new point, the LCA needs to be found.
    LinkedSequence<Block> oldAncestors = LinkedSequence.create();
    for (Block block = start.block; block != null; block = block.getParent()) {
      oldAncestors.append(block);
    }

    List<Block> newAncestors = CollectionUtils.newArrayList();
    Block lca = point.block;
    while (lca != null && !oldAncestors.contains(lca)) {
      newAncestors.add(lca);
      lca = lca.getParent();
    }

    if (lca == null) {
      throw new IllegalArgumentException("Point not in block tree");
    }

    // Page out unique old ancestors, in order from old point up to LCA.
    for (Block oldAncestor : oldAncestors) {
      if (oldAncestor == lca) {
        break;
      } else {
        toPageOut.add(oldAncestor);
      }
    }

    // Page in unique new ancestors, in order from LCA down to new point.
    while (!newAncestors.isEmpty()) {
      toPageIn.add(newAncestors.remove(newAncestors.size() - 1));
    }

    // Then initiate active region.
    start.set(point);
    end.set(point);
  }

  /**
   * Expands a collapsed region.
   *
   * @param viewport
   */
  private void expandTo(Region viewport) {
    assert isActive();
    growStartUntil(viewport.getStart());
    growEndUntil(viewport.getEnd());
  }

  /**
   * Grows the active region from the start, until it includes a given position,
   * or the start of the entire tree is reached.
   *
   * @param position position to include in the active region
   */
  private void growStartUntil(double position) {
    start.trawlPreviousUntil(position, toPageIn);
  }

  /**
   * Grows the active region from the end, until it includes a given position,
   * or the end of the entire tree is reached.
   *
   * @param position position to include in the active region
   */
  private void growEndUntil(double position) {
    end.trawlNextUntil(position, toPageIn);
  }

  /**
   * Shrinks the active region from the start, until it includes a given
   * position, or the start of the entire tree is reached.
   *
   * @param position position to include in the active region
   */
  private void shrinkStartUntil(double position) {
    start.trawlNextUntil(position, toPageOut);
  }

  /**
   * Shrinks the active region from the end, until it includes a given position,
   * or the end of the entire tree is reached.
   *
   * @param position position to include in the active region
   */
  private void shrinkEndUntil(double position) {
    end.trawlPreviousUntil(position, toPageOut);
  }

  /**
   * Pages-in all the blocks in the page-in queue, and pages-out all the blocks
   * in the page-out queue.
   */
  private void page() {
    // Do page-out then page-in, so that moving via reset() ; init() will work.
    while (!toPageOut.isEmpty()) {
      toPageOut.poll().pageOut();
    }
    while (!toPageIn.isEmpty()) {
      toPageIn.poll().pageIn();
    }
    invalidatePositionCache();
  }

  //
  // Dynamic block handling.
  //

  /**
   * Notifies this region that a new subtree is now attached and ready to be
   * included as a pageable entity.
   */
  public void onAfterBlockAdded(Block added) {
    if (!isActive()) {
      return;
    }
    if (Traverser.isBetween(start, end, SimplePoint.startOf(added))) {
      pageInCompletely(added);
    }
  }

  private void pageInCompletely(Block block) {
    block.pageIn();
    for (Block child = block.getFirstChild(); child != null; child = child.getNextSibling()) {
      pageInCompletely(child);
    }
  }

  /**
   * Notifies this region that a subtree is about to be removed. All parts of
   * the accumulated block tree (i.e., the original tree, plus changes observed
   * through {@link #onAfterBlockAdded(Block)} and
   * {@link #onBeforeBlockRemoved(Block)}) are assumed to be still attached.
   *
   * @param block root of a subtree that is about to be removed
   */
  public void onBeforeBlockRemoved(Block block) {
    if (!isActive()) {
      return;
    }
    if (block == root) {
      start.clear();
      end.clear();
    } else {
      // If the block is an ancestor of a terminal (start or end), then that
      // terminal needs to be moved.
      boolean startIsDescendant = Traverser.isDescendant(block, start.block);
      boolean endIsDescendant = Traverser.isDescendant(block, end.block);
      if (startIsDescendant && endIsDescendant) {
        start.set(SimplePoint.startOf(block.getParent()));
        end.set(start);
      } else if (startIsDescendant) {
        start.set(SimplePoint.endOf(block));
        start.next();
      } else if (endIsDescendant) {
        end.set(SimplePoint.startOf(block));
        end.previous();
      }
    }
  }

  //@VisibleForTesting(productionVisibility = Visibility.PRIVATE)
  public Point getStart() {
    return start;
  }

  // @VisibleForTesting(productionVisibility = Visibility.PRIVATE)
  public Point getEnd() {
    return end;
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

