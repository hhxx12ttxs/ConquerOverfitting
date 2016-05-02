<<<<<<< HEAD
/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.gef.editpolicies;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.AutomaticRouter;
import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.handles.BendpointCreationHandle;
import org.eclipse.gef.handles.BendpointMoveHandle;
import org.eclipse.gef.requests.BendpointRequest;

/**
 * Used to add bendpoint handles on a {@link ConnectionEditPart}.
 * <P>
 * BendpointEditPolicy will automatically observe the
 * {@link org.eclipse.draw2d.Connection} figure. If the number of bends in the
 * <code>Connection</code> changes, the handles will be updated.
 */
public abstract class BendpointEditPolicy extends SelectionHandlesEditPolicy
		implements PropertyChangeListener {

	private static final List NULL_CONSTRAINT = new ArrayList();

	private List originalConstraint;
	private boolean isDeleting = false;

	private static final Point ref1 = new Point();
	private static final Point ref2 = new Point();

	/**
	 * <code>activate()</code> is extended to add a listener to the
	 * <code>Connection</code> figure.
	 * 
	 * @see org.eclipse.gef.EditPolicy#activate()
	 */
	public void activate() {
		super.activate();
		getConnection().addPropertyChangeListener(Connection.PROPERTY_POINTS,
				this);
	}

	private List createHandlesForAutomaticBendpoints() {
		List list = new ArrayList();
		ConnectionEditPart connEP = (ConnectionEditPart) getHost();
		PointList points = getConnection().getPoints();
		for (int i = 0; i < points.size() - 1; i++)
			list.add(new BendpointCreationHandle(connEP, 0, i));

		return list;
	}

	private List createHandlesForUserBendpoints() {
		List list = new ArrayList();
		ConnectionEditPart connEP = (ConnectionEditPart) getHost();
		PointList points = getConnection().getPoints();
		List bendPoints = (List) getConnection().getRoutingConstraint();
		int bendPointIndex = 0;
		Point currBendPoint = null;

		if (bendPoints == null)
			bendPoints = NULL_CONSTRAINT;
		else if (!bendPoints.isEmpty())
			currBendPoint = ((Bendpoint) bendPoints.get(0)).getLocation();

		for (int i = 0; i < points.size() - 1; i++) {
			// Put a create handle on the middle of every segment
			list.add(new BendpointCreationHandle(connEP, bendPointIndex, i));

			// If the current user bendpoint matches a bend location, show a
			// move handle
			if (i < points.size() - 1 && bendPointIndex < bendPoints.size()
					&& currBendPoint.equals(points.getPoint(i + 1))) {
				list.add(new BendpointMoveHandle(connEP, bendPointIndex, i + 1));

				// Go to the next user bendpoint
				bendPointIndex++;
				if (bendPointIndex < bendPoints.size())
					currBendPoint = ((Bendpoint) bendPoints.get(bendPointIndex))
							.getLocation();
			}
		}

		return list;
	}

	/**
	 * Creates selection handles for the bendpoints. Explicit (user-defined)
	 * bendpoints will have {@link BendpointMoveHandle}s on them with a single
	 * {@link BendpointCreationHandle} between 2 consecutive explicit
	 * bendpoints. If implicit bendpoints (such as those created by the
	 * {@link AutomaticRouter}) are used, one {@link BendpointCreationHandle} is
	 * placed in the middle of the Connection.
	 * 
	 * @see SelectionHandlesEditPolicy#createSelectionHandles()
	 */
	protected List createSelectionHandles() {
		List list = new ArrayList();
		if (isAutomaticallyBending())
			list = createHandlesForAutomaticBendpoints();
		else
			list = createHandlesForUserBendpoints();
		return list;
	}

	/**
	 * <code>deactivate()</code> is extended to remove the property change
	 * listener on the <code>Connection</code> figure.
	 * 
	 * @see org.eclipse.gef.EditPolicy#deactivate()
	 */
	public void deactivate() {
		getConnection().removePropertyChangeListener(
				Connection.PROPERTY_POINTS, this);
		super.deactivate();
	}

	/**
	 * Erases all bendpoint feedback. Since the original <code>Connection</code>
	 * figure is used for feedback, we just restore the original constraint that
	 * was saved before feedback started to show.
	 * 
	 * @param request
	 *            the BendpointRequest
	 */
	protected void eraseConnectionFeedback(BendpointRequest request) {
		restoreOriginalConstraint();
		originalConstraint = null;
	}

	/**
	 * @see org.eclipse.gef.EditPolicy#eraseSourceFeedback(Request)
	 */
	public void eraseSourceFeedback(Request request) {
		if (REQ_MOVE_BENDPOINT.equals(request.getType())
				|| REQ_CREATE_BENDPOINT.equals(request.getType()))
			eraseConnectionFeedback((BendpointRequest) request);
	}

	/**
	 * Factors the Request into either a MOVE, a DELETE, or a CREATE of a
	 * bendpoint.
	 * 
	 * @see org.eclipse.gef.EditPolicy#getCommand(Request)
	 */
	public Command getCommand(Request request) {
		if (REQ_MOVE_BENDPOINT.equals(request.getType())) {
			if (isDeleting)
				return getDeleteBendpointCommand((BendpointRequest) request);
			return getMoveBendpointCommand((BendpointRequest) request);
		}
		if (REQ_CREATE_BENDPOINT.equals(request.getType()))
			return getCreateBendpointCommand((BendpointRequest) request);
		return null;
	}

	/**
	 * Convenience method for obtaining the host's <code>Connection</code>
	 * figure.
	 * 
	 * @return the Connection figure
	 */
	protected Connection getConnection() {
		return (Connection) ((ConnectionEditPart) getHost()).getFigure();
	}

	/**
	 * Implement this method to return a Command that will create a bendpoint.
	 * 
	 * @param request
	 *            the BendpointRequest
	 * @return a Command to create a bendpoint
	 */
	protected abstract Command getCreateBendpointCommand(
			BendpointRequest request);

	/**
	 * Implement this method to return a Command that will delete a bendpoint.
	 * 
	 * @param request
	 *            the BendpointRequest
	 * @return a Command to delete a bendpoint
	 */
	protected abstract Command getDeleteBendpointCommand(
			BendpointRequest request);

	/**
	 * Implement this method to return a Command that will move a bendpoint.
	 * 
	 * @param request
	 *            the BendpointRequest
	 * @return a Command to move a bendpoint
	 */
	protected abstract Command getMoveBendpointCommand(BendpointRequest request);

	private boolean isAutomaticallyBending() {
		List constraint = (List) getConnection().getRoutingConstraint();
		PointList points = getConnection().getPoints();
		return ((points.size() > 2) && (constraint == null || constraint
				.isEmpty()));
	}

	private boolean lineContainsPoint(Point p1, Point p2, Point p) {
		int tolerance = 7;
		Rectangle rect = Rectangle.SINGLETON;
		rect.setSize(0, 0);
		rect.setLocation(p1.x, p1.y);
		rect.union(p2.x, p2.y);
		rect.expand(tolerance, tolerance);
		if (!rect.contains(p.x, p.y))
			return false;

		int v1x, v1y, v2x, v2y;
		int numerator, denominator;
		double result = 0.0;

		if (p1.x != p2.x && p1.y != p2.y) {

			v1x = p2.x - p1.x;
			v1y = p2.y - p1.y;
			v2x = p.x - p1.x;
			v2y = p.y - p1.y;

			numerator = v2x * v1y - v1x * v2y;
			denominator = v1x * v1x + v1y * v1y;

			result = ((numerator << 10) / denominator * numerator) >> 10;
		}

		// if it is the same point, and it passes the bounding box test,
		// the result is always true.
		return result <= tolerance * tolerance;
	}

	/**
	 * If the number of bendpoints changes, handles are updated.
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		// $TODO optimize so that handles aren't added constantly.
		if (getHost().getSelected() != EditPart.SELECTED_NONE)
			addSelectionHandles();
	}

	/**
	 * Restores the original constraint that was saved before feedback began to
	 * show.
	 */
	protected void restoreOriginalConstraint() {
		if (originalConstraint != null) {
			if (originalConstraint == NULL_CONSTRAINT)
				getConnection().setRoutingConstraint(null);
			else
				getConnection().setRoutingConstraint(originalConstraint);
		}
	}

	/**
	 * Since the original figure is used for feedback, this method saves the
	 * original constraint, so that is can be restored when the feedback is
	 * erased.
	 */
	protected void saveOriginalConstraint() {
		originalConstraint = (List) getConnection().getRoutingConstraint();
		if (originalConstraint == null)
			originalConstraint = NULL_CONSTRAINT;
		getConnection().setRoutingConstraint(new ArrayList(originalConstraint));
	}

	private void setReferencePoints(BendpointRequest request) {
		PointList points = getConnection().getPoints();
		int bpIndex = -1;
		List bendPoints = (List) getConnection().getRoutingConstraint();
		Point bp = ((Bendpoint) bendPoints.get(request.getIndex()))
				.getLocation();

		int smallestDistance = -1;

		// points include the bend points as well as start and end, which we may
		// leave out when searching for the bend point index
		for (int i = 1; i < points.size() - 1; i++) {
			if (smallestDistance == -1
					|| points.getPoint(i).getDistance2(bp) < smallestDistance) {
				bpIndex = i;
				smallestDistance = points.getPoint(i).getDistance2(bp);
				if (smallestDistance == 0)
					break;
			}
		}

		points.getPoint(ref1, bpIndex - 1);
		getConnection().translateToAbsolute(ref1);
		points.getPoint(ref2, bpIndex + 1);
		getConnection().translateToAbsolute(ref2);
	}

	/**
	 * Shows feedback when a bendpoint is being created. The original figure is
	 * used for feedback and the original constraint is saved, so that it can be
	 * restored when feedback is erased.
	 * 
	 * @param request
	 *            the BendpointRequest
	 */
	protected void showCreateBendpointFeedback(BendpointRequest request) {
		Point p = new Point(request.getLocation());
		List constraint;
		getConnection().translateToRelative(p);
		Bendpoint bp = new AbsoluteBendpoint(p);
		if (originalConstraint == null) {
			saveOriginalConstraint();
			constraint = (List) getConnection().getRoutingConstraint();
			constraint.add(request.getIndex(), bp);
		} else {
			constraint = (List) getConnection().getRoutingConstraint();
		}
		constraint.set(request.getIndex(), bp);
		getConnection().setRoutingConstraint(constraint);
	}

	/**
	 * Shows feedback when a bendpoint is being deleted. This method is only
	 * called once when the bendpoint is first deleted, not every mouse move.
	 * The original figure is used for feedback and the original constraint is
	 * saved, so that it can be restored when feedback is erased.
	 * 
	 * @param request
	 *            the BendpointRequest
	 */
	protected void showDeleteBendpointFeedback(BendpointRequest request) {
		if (originalConstraint == null) {
			saveOriginalConstraint();
			List constraint = (List) getConnection().getRoutingConstraint();
			constraint.remove(request.getIndex());
			getConnection().setRoutingConstraint(constraint);
		}
	}

	/**
	 * Shows feedback when a bendpoint is being moved. Also checks to see if the
	 * bendpoint should be deleted and then calls
	 * {@link #showDeleteBendpointFeedback(BendpointRequest)} if needed. The
	 * original figure is used for feedback and the original constraint is
	 * saved, so that it can be restored when feedback is erased.
	 * 
	 * @param request
	 *            the BendpointRequest
	 */
	protected void showMoveBendpointFeedback(BendpointRequest request) {
		Point p = new Point(request.getLocation());
		if (!isDeleting)
			setReferencePoints(request);

		if (lineContainsPoint(ref1, ref2, p)) {
			if (!isDeleting) {
				isDeleting = true;
				eraseSourceFeedback(request);
				showDeleteBendpointFeedback(request);
			}
			return;
		}
		if (isDeleting) {
			isDeleting = false;
			eraseSourceFeedback(request);
		}
		if (originalConstraint == null)
			saveOriginalConstraint();
		List constraint = (List) getConnection().getRoutingConstraint();
		getConnection().translateToRelative(p);
		Bendpoint bp = new AbsoluteBendpoint(p);
		constraint.set(request.getIndex(), bp);
		getConnection().setRoutingConstraint(constraint);
	}

	/**
	 * Shows feedback when appropriate. Calls a different method depending on
	 * the request type.
	 * 
	 * @see #showCreateBendpointFeedback(BendpointRequest)
	 * @see #showMoveBendpointFeedback(BendpointRequest)
	 * @param request
	 *            the Request
	 */
	public void showSourceFeedback(Request request) {
		if (REQ_MOVE_BENDPOINT.equals(request.getType()))
			showMoveBendpointFeedback((BendpointRequest) request);
		else if (REQ_CREATE_BENDPOINT.equals(request.getType()))
			showCreateBendpointFeedback((BendpointRequest) request);
	}

=======
/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.jikesrvm;

import org.jikesrvm.mm.mminterface.MemoryManagerConstants;
import org.jikesrvm.mm.mminterface.MemoryManager;
import org.jikesrvm.runtime.Entrypoints;
import org.jikesrvm.runtime.Magic;
import org.jikesrvm.scheduler.Synchronization;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Interruptible;
import org.vmmagic.pragma.NoInline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptibleNoWarn;
import org.vmmagic.pragma.Unpreemptible;
import org.vmmagic.pragma.UnpreemptibleNoWarn;
import org.vmmagic.unboxed.Offset;

/**
 *  Various service utilities.  This is a common place for some shared utility routines
 */
@Uninterruptible
public class Services implements SizeConstants {
  /**
   * Biggest buffer you would possibly need for {@link org.jikesrvm.scheduler.RVMThread#dump(char[], int)}
   * Modify this if you modify that method.
   */
  public static final int MAX_DUMP_LEN =
    10 /* for thread ID  */ + 7 + 5 + 5 + 11 + 5 + 10 + 13 + 17 + 10;

  /** Pre-allocate the dump buffer, since dump() might get called inside GC. */
  private static final char[] dumpBuffer = new char[MAX_DUMP_LEN];

  @SuppressWarnings({"unused", "CanBeFinal", "UnusedDeclaration"})// accessed via EntryPoints
  private static int dumpBufferLock = 0;

  /** Reset at boot time. */
  private static Offset dumpBufferLockOffset = Offset.max();

  /**
   * A map of hexadecimal digit values to their character representations.
   * <P>
   * XXX We currently only use '0' through '9'.  The rest are here pending
   * possibly merging this code with the similar code in Log.java, or breaking
   * this code out into a separate utility class.
   */
  private static final char [] hexDigitCharacter =
  { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
    'f' };

  /**
   * How many characters we need to have in a buffer for building string
   * representations of <code>long</code>s, such as {@link #intBuffer}. A
   * <code>long</code> is a signed 64-bit integer in the range -2^63 to
   * 2^63+1. The number of digits in the decimal representation of 2^63 is
   * ceiling(log10(2^63)) == ceiling(63 * log10(2)) == 19. An extra character
   * may be required for a minus sign (-). So the maximum number of characters
   * is 20.
   */
  private static final int INT_BUFFER_SIZE = 20;

  /** A buffer for building string representations of <code>long</code>s */
  private static final char [] intBuffer = new char[INT_BUFFER_SIZE];

  /** A lock for {@link #intBuffer} */
  @SuppressWarnings({"unused", "CanBeFinal", "UnusedDeclaration"})// accessed via EntryPoints
  private static int intBufferLock = 0;

  /** The offset of {@link #intBufferLock} in this class's TIB.
   *  This is set properly at boot time, even though it's a
   *  <code>private</code> variable. . */
  private static Offset intBufferLockOffset = Offset.max();

  /**
   * Called during the boot sequence, any time before we go multi-threaded. We
   * do this so that we can leave the lockOffsets set to -1 until the VM
   * actually needs the locking (and is running multi-threaded).
   */
  public static void boot() {
    dumpBufferLockOffset = Entrypoints.dumpBufferLockField.getOffset();
    intBufferLockOffset = Entrypoints.intBufferLockField.getOffset();
  }

  public static char[] grabDumpBuffer() {
    if (!dumpBufferLockOffset.isMax()) {
      while (!Synchronization.testAndSet(Magic.getJTOC(), dumpBufferLockOffset, 1)) {
        ;
      }
    }
    return dumpBuffer;
  }

  public static void releaseDumpBuffer() {
    if (!dumpBufferLockOffset.isMax()) {
      Synchronization.fetchAndStore(Magic.getJTOC(), dumpBufferLockOffset, 0);
    }
  }


  /** Copy a String into a character array.
   *
   *  This function may be called during GC and may be used in conjunction
   *  with the MMTk {@link org.mmtk.utility.Log} class.   It avoids write barriers and allocation.
   *  <p>
   *  XXX This function should probably be moved to a sensible location where
   *   we can use it as a utility.   Suggestions welcome.
   *  <P>
   *
   * @param dest char array to copy into.
   * @param destOffset Offset into <code>dest</code> where we start copying
   *
   * @return 1 plus the index of the last character written.  If we were to
   *         write zero characters (which we won't) then we would return
   *         <code>offset</code>.  This is intended to represent the first
   *         unused position in the array <code>dest</code>.  However, it also
   *         serves as a pseudo-overflow check:  It may have the value
   *         <code>dest.length</code>, if the array <code>dest</code> was
   *         completely filled by the call, or it may have a value greater
   *         than <code>dest.length</code>, if the info needs more than
   *         <code>dest.length - offset</code> characters of space.
   *
   * @return  -1 if <code>offset</code> is negative.
   *
   * the MMTk {@link org.mmtk.utility.Log} class).
   */
  public static int sprintf(char[] dest, int destOffset, String s) {
    final char[] sArray = java.lang.JikesRVMSupport.getBackingCharArray(s);
    return sprintf(dest, destOffset, sArray);
  }

  public static int sprintf(char[] dest, int destOffset, char[] src) {
    return sprintf(dest, destOffset, src, 0, src.length);
  }

  /** Copies characters from <code>src</code> into the destination character
   * array <code>dest</code>.
   *
   *  The first character to be copied is at index <code>srcBegin</code>; the
   *  last character to be copied is at index <code>srcEnd-1</code>.  (This is
   *  the same convention as followed by java.lang.String#getChars).
   *
   * @param dest char array to copy into.
   * @param destOffset Offset into <code>dest</code> where we start copying
   * @param src Char array to copy from
   * @param srcStart index of the first character of <code>src</code> to copy.
   * @param srcEnd index after the last character of <code>src</code> to copy.
   */
  public static int sprintf(char[] dest, int destOffset, char[] src, int srcStart, int srcEnd) {
    for (int i = srcStart; i < srcEnd; ++i) {
      char nextChar = getArrayNoBarrier(src, i);
      destOffset = sprintf(dest, destOffset, nextChar);
    }
    return destOffset;
  }

  public static int sprintf(char[] dest, int destOffset, char c) {
    if (destOffset < 0) {
      // bounds check
      return -1;
    }

    if (destOffset < dest.length) {
      setArrayNoBarrier(dest, destOffset, c);
    }
    return destOffset + 1;
  }

  /** Copy the printed decimal representation of a long into
   * a character array.  The value is not padded and no
   * thousands seperator is copied.  If the value is negative a
   * leading minus sign (-) is copied.
   *
   *  This function may be called during GC and may be used in conjunction
   *  with the Log class.   It avoids write barriers and allocation.
   *  <p>
   *  XXX This function should probably be moved to a sensible location where
   *   we can use it as a utility.   Suggestions welcome.
   * <p>
   *  XXX This method's implementation is stolen from the {@link org.mmtk.utility.Log} class.
   *
   * @param dest char array to copy into.
   * @param offset Offset into <code>dest</code> where we start copying
   *
   * @return 1 plus the index of the last character written.  If we were to
   *         write zero characters (which we won't) then we would return
   *         <code>offset</code>.  This is intended to represent the first
   *         unused position in the array <code>dest</code>.  However, it also
   *         serves as a pseudo-overflow check:  It may have the value
   *         <code>dest.length</code>, if the array <code>dest</code> was
   *         completely filled by the call, or it may have a value greater
   *         than <code>dest.length</code>, if the info needs more than
   *         <code>dest.length - offset</code> characters of space.
   *
   * @return  -1 if <code>offset</code> is negative.
   */
  public static int sprintf(char[] dest, int offset, long l) {
    boolean negative = l < 0;
    int nextDigit;
    char nextChar;
    int index = INT_BUFFER_SIZE - 1;
    char[] intBuffer = grabIntBuffer();

    nextDigit = (int) (l % 10);
    nextChar = getArrayNoBarrier(hexDigitCharacter, negative ? -nextDigit : nextDigit);
    setArrayNoBarrier(intBuffer, index--, nextChar);
    l = l / 10;

    while (l != 0) {
      nextDigit = (int) (l % 10);
      nextChar = getArrayNoBarrier(hexDigitCharacter, negative ? -nextDigit : nextDigit);
      setArrayNoBarrier(intBuffer, index--, nextChar);
      l = l / 10;
    }

    if (negative) {
     setArrayNoBarrier(intBuffer, index--, '-');
    }

    int newOffset = sprintf(dest, offset, intBuffer, index + 1, INT_BUFFER_SIZE);
    releaseIntBuffer();
    return newOffset;
  }

  /**
   * Get exclusive access to {@link #intBuffer}, the buffer for building
   * string representations of integers.
   */
  private static char[] grabIntBuffer() {
    if (!intBufferLockOffset.isMax()) {
      while (!Synchronization.testAndSet(Magic.getJTOC(), intBufferLockOffset, 1)) {
        ;
      }
    }
    return intBuffer;
  }

  /**
   * Release {@link #intBuffer}, the buffer for building string
   * representations of integers.
   */
  private static void releaseIntBuffer() {
    if (!intBufferLockOffset.isMax()) {
      Synchronization.fetchAndStore(Magic.getJTOC(), intBufferLockOffset, 0);
    }
  }

  /**
   * Utility printing function.
   * @param i
   * @param blank
   */
  @Interruptible
  public static String getHexString(int i, boolean blank) {
    StringBuilder buf = new StringBuilder(8);
    for (int j = 0; j < 8; j++, i <<= 4) {
      int n = i >>> 28;
      if (blank && (n == 0) && (j != 7)) {
        buf.append(' ');
      } else {
        buf.append(Character.forDigit(n, 16));
        blank = false;
      }
    }
    return buf.toString();
  }

  @NoInline
  public static void breakStub() {
  }

  static void println() { VM.sysWrite("\n"); }

  static void print(String s) { VM.sysWrite(s); }

  static void println(String s) {
    print(s);
    println();
  }

  static void print(int i) { VM.sysWrite(i); }

  static void println(int i) {
    print(i);
    println();
  }

  static void print(String s, int i) {
    print(s);
    print(i);
  }

  static void println(String s, int i) {
    print(s, i);
    println();
  }

  public static void percentage(int numerator, int denominator, String quantity) {
    print("\t");
    if (denominator > 0) {
      print((int) ((((double) numerator) * 100.0) / ((double) denominator)));
    } else {
      print("0");
    }
    print("% of ");
    println(quantity);
  }

  static void percentage(long numerator, long denominator, String quantity) {
    print("\t");
    if (denominator > 0L) {
      print((int) ((((double) numerator) * 100.0) / ((double) denominator)));
    } else {
      print("0");
    }
    print("% of ");
    println(quantity);
  }

  /**
   * Sets an element of a object array without possibly losing control.
   * NB doesn't perform checkstore or array index checking.
   *
   * @param dst the destination array
   * @param index the index of the element to set
   * @param value the new value for the element
   */
  @UninterruptibleNoWarn("Interruptible code not reachable at runtime")
  @Inline
  public static void setArrayUninterruptible(Object[] dst, int index, Object value) {
    if (VM.runningVM) {
      if (MemoryManagerConstants.NEEDS_WRITE_BARRIER) {
        MemoryManager.arrayStoreWriteBarrier(dst, index, value);
      } else {
        Magic.setObjectAtOffset(dst, Offset.fromIntZeroExtend(index << LOG_BYTES_IN_ADDRESS), value);
      }
    } else {
      dst[index] = value;
    }
  }

  /**
   * Sets an element of a char array without invoking any write
   * barrier.  This method is called by the Log method, as it will be
   * used during garbage collection and needs to manipulate character
   * arrays without causing a write barrier operation.
   *
   * @param dst the destination array
   * @param index the index of the element to set
   * @param value the new value for the element
   */
  public static void setArrayNoBarrier(char[] dst, int index, char value) {
    if (VM.runningVM)
      Magic.setCharAtOffset(dst, Offset.fromIntZeroExtend(index << LOG_BYTES_IN_CHAR), value);
    else
      dst[index] = value;
  }

  /**
   * Gets an element of an Object array without invoking any read
   * barrier or performing bounds checks.
   *
   * @param src the source array
   * @param index the natural array index of the element to get
   * @return the new value of element
   */
  public static Object getArrayNoBarrier(Object[] src, int index) {
    if (VM.runningVM)
      return Magic.getObjectAtOffset(src, Offset.fromIntZeroExtend(index << LOG_BYTES_IN_ADDRESS));
    else
      return src[index];
  }

  /**
   * Gets an element of an int array without invoking any read barrier
   * or performing bounds checks.
   *
   * @param src the source array
   * @param index the natural array index of the element to get
   * @return the new value of element
   */
  public static int getArrayNoBarrier(int[] src, int index) {
    if (VM.runningVM)
      return Magic.getIntAtOffset(src, Offset.fromIntZeroExtend(index << LOG_BYTES_IN_INT));
    else
      return src[index];
  }

  /**
   * Gets an element of a char array without invoking any read barrier
   * or performing bounds check.
   *
   * @param src the source array
   * @param index the natural array index of the element to get
   * @return the new value of element
   */
  public static char getArrayNoBarrier(char[] src, int index) {
    if (VM.runningVM)
      return Magic.getCharAtOffset(src, Offset.fromIntZeroExtend(index << LOG_BYTES_IN_CHAR));
    else
      return src[index];
  }

  /**
   * Gets an element of a byte array without invoking any read barrier
   * or bounds check.
   *
   * @param src the source array
   * @param index the natural array index of the element to get
   * @return the new value of element
   */
  public static byte getArrayNoBarrier(byte[] src, int index) {
    if (VM.runningVM)
      return Magic.getByteAtOffset(src, Offset.fromIntZeroExtend(index));
    else
      return src[index];
  }

  /**
   * Gets an element of an array of byte arrays without causing the potential
   * thread switch point that array accesses normally cause.
   *
   * @param src the source array
   * @param index the index of the element to get
   * @return the new value of element
   */
  public static byte[] getArrayNoBarrier(byte[][] src, int index) {
    if (VM.runningVM)
      return Magic.addressAsByteArray(Magic.objectAsAddress(Magic.getObjectAtOffset(src, Offset.fromIntZeroExtend(index << LOG_BYTES_IN_ADDRESS))));
    else
      return src[index];
  }

  @Unpreemptible("Call interruptible string API")
  public static String stringConcatenator(String... args) {
    String result="";
    for (String s:args) {
      result = stringConcatenate(result, s);
    }
    return result;
  }

  @UnpreemptibleNoWarn("Call interruptible string API")
  public static String stringConcatenate(String a, String b) {
    return a.concat(b);
  }

  @UnpreemptibleNoWarn("Call interruptible string API")
  public static String stringConcatenate(String a, int b) {
    return a + b;
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

