/*
 * Copyright (c) 2004, Mikael Grev, MiG InfoCom AB. (miglayout (at) miginfocom (dot) com), 
 * modifications by Nikolaus Moll
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * Neither the name of the MiG InfoCom AB nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 */
package org.jowidgets.impl.layout.miglayout.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.jowidgets.impl.layout.miglayout.MigLayoutToolkit;

/**
 * Holds components in a grid. Does most of the logic behind the layout manager.
 */
public final class Grid {
	public static final boolean TEST_GAPS = true;

	private static final Float[] GROW_100 = new Float[] {ResizeConstraint.WEIGHT_100};

	private static final DimConstraint DOCK_DIM_CONSTRAINT = new DimConstraint();
	static {
		DOCK_DIM_CONSTRAINT.setGrowPriority(0);
	}

	@SuppressWarnings("rawtypes")
	private static WeakHashMap[] parentRowColSizesMap = null;

	private static WeakHashMap<Object, LinkedHashMap<Integer, Cell>> parentGridPosMap = null;

	/**
	 * This is the maximum grid position for "normal" components. Docking components use the space out to
	 * <code>MAX_DOCK_GRID</code> and below 0.
	 */
	private static final int MAX_GRID = 30000;

	/**
	 * Docking components will use the grid coordinates <code>-MAX_DOCK_GRID -> 0</code> and
	 * <code>MAX_GRID -> MAX_DOCK_GRID</code>.
	 */
	private static final int MAX_DOCK_GRID = 32767;

	/**
	 * A constraint used for gaps.
	 */
	private static final ResizeConstraint GAP_RC_CONST = new ResizeConstraint(200, ResizeConstraint.WEIGHT_100, 50, null);
	private static final ResizeConstraint GAP_RC_CONST_PUSH = new ResizeConstraint(
		200,
		ResizeConstraint.WEIGHT_100,
		50,
		ResizeConstraint.WEIGHT_100);

	/**
	 * The constraints. Never <code>null</code>.
	 */
	private final LC lc;

	/**
	 * The parent that is layout out and this grid is done for. Never <code>null</code>.
	 */
	private final IContainerWrapper container;

	/**
	 * An x, y array implemented as a sparse array to accommodate for any grid size without wasting memory (or rather 15 bit
	 * (0-MAX_GRID * 0-MAX_GRID).
	 */
	private final LinkedHashMap<Integer, Cell> grid = new LinkedHashMap<Integer, Cell>(); // [(y << 16) + x] -> Cell. null key for absolute positioned compwraps

	private HashMap<Integer, BoundSize> wrapGapMap = null; // Row or Column index depending in the dimension that "wraps". Normally row indexes but may be column indexes if "flowy". 0 means before first row/col.

	/**
	 * The size of the grid. Row count and column count.
	 */
	private final TreeSet<Integer> rowIndexes = new TreeSet<Integer>();
	private final TreeSet<Integer> colIndexes = new TreeSet<Integer>();

	/**
	 * The row and column specifications.
	 */
	private final AC rowConstr;
	private final AC colConstr;

	/**
	 * The in the constructor calculated min/pref/max sizes of the rows and columns.
	 */
	private FlowSizeSpec colFlowSpecs = null;
	private FlowSizeSpec rowFlowSpecs = null;

	/**
	 * Components that are connections in one dimension (such as baseline alignment for instance) are grouped together and stored
	 * here.
	 * One for each row/column.
	 */
	private final ArrayList<LinkedDimGroup>[] colGroupLists;
	private final ArrayList<LinkedDimGroup>[] rowGroupLists; //[(start)row/col number]

	/**
	 * The in the constructor calculated min/pref/max size of the whole grid.
	 */
	private int[] width = null;
	private int[] height = null;

	/**
	 * If debug is on contains the bounds for things to paint when calling
	 * {@link IContainerWrapper#paintDebugCell(int, int, int, int)}
	 */
	private ArrayList<int[]> debugRects = null; // [x, y, width, height]

	/**
	 * If any of the absolute coordinates for component bounds has links the name of the target is in this Set.
	 * Since it requires some memory and computations this is checked at the creation so that
	 * the link information is only created if needed later.
	 * <p>
	 * The boolean is true for groups id:s and null for normal id:s.
	 */
	private HashMap<String, Boolean> linkTargetIDs = null;

	private final int dockOffY;
	private final int dockOffX;

	private final Float[] pushXs;
	private final Float[] pushYs;

	private final ArrayList<AbstractLayoutCallback> callbackList;

	/**
	 * Constructor.
	 * 
	 * @param container The container that will be laid out.
	 * @param lc The form flow constraints.
	 * @param rowConstr The rows specifications. If more cell rows are required, the last element will be used for when there is
	 *            no corresponding element in this array.
	 * @param colConstr The columns specifications. If more cell rows are required, the last element will be used for when there
	 *            is no corresponding element in this array.
	 * @param ccMap The map containing the parsed constraints for each child component of <code>parent</code>. Will not be
	 *            alterted.
	 * @param callbackList A list of callbacks or <code>null</code> if none. Will not be alterted.
	 */
	public Grid(
		final IContainerWrapper container,
		final LC lc,
		final AC rowConstr,
		final AC colConstr,
		final Map<IComponentWrapper, CC> ccMap,
		final ArrayList<AbstractLayoutCallback> callbackList) {
		this.lc = lc;
		this.rowConstr = rowConstr;
		this.colConstr = colConstr;
		this.container = container;
		this.callbackList = callbackList;

		final int wrap = lc.getWrapAfter() != 0
				? lc.getWrapAfter() : (lc.isFlowX() ? colConstr : rowConstr).getConstaints().length;

		final IComponentWrapper[] comps = container.getComponents();

		boolean hasTagged = false; // So we do not have to sort if it will not do any good
		boolean hasPushX = false;
		boolean hasPushY = false;
		boolean hitEndOfRow = false;
		final int[] cellXY = new int[2];
		final ArrayList<int[]> spannedRects = new ArrayList<int[]>(2);

		final DimConstraint[] specs = (lc.isFlowX() ? rowConstr : colConstr).getConstaints();

		int sizeGroupsX = 0;
		int sizeGroupsY = 0;
		int[] dockInsets = null; // top, left, bottom, right insets for docks.

		MigLayoutToolkit.getMigLinkHandler().clearTemporaryBounds(container.getLayout());

		for (int i = 0; i < comps.length;) {
			final IComponentWrapper comp = comps[i];
			final CC rootCc = getCC(comp, ccMap);

			addLinkIDs(rootCc);

			int hideMode = comp.isVisible() ? -1 : rootCc.getHideMode() != -1 ? rootCc.getHideMode() : lc.getHideMode();

			if (hideMode == 3) { // To work with situations where there are components that does not have a layout manager, or not this one.
				setLinkedBounds(comp, rootCc, comp.getX(), comp.getY(), comp.getWidth(), comp.getHeight(), rootCc.isExternal());
				i++;
				continue; // The "external" component should not be handled further.
			}

			if (rootCc.getHorizontal().getSizeGroup() != null) {
				sizeGroupsX++;
			}
			if (rootCc.getVertical().getSizeGroup() != null) {
				sizeGroupsY++;
			}

			// Special treatment of absolute positioned components.
			UnitValue[] pos = getPos(comp, rootCc);
			BoundSize[] cbSz = getCallbackSize(comp);
			if (pos != null || rootCc.isExternal()) {

				final CompWrap cw = new CompWrap(comp, rootCc, hideMode, pos, cbSz);
				final Cell cell = grid.get(null);
				if (cell == null) {
					grid.put(null, new Cell(cw));
				}
				else {
					cell.compWraps.add(cw);
				}

				if (rootCc.isBoundsInGrid() == false || rootCc.isExternal()) {
					setLinkedBounds(
							comp,
							rootCc,
							comp.getX(),
							comp.getY(),
							comp.getWidth(),
							comp.getHeight(),
							rootCc.isExternal());
					i++;
					continue;
				}
			}

			if (rootCc.getDockSide() != -1) {
				if (dockInsets == null) {
					dockInsets = new int[] {-MAX_DOCK_GRID, -MAX_DOCK_GRID, MAX_DOCK_GRID, MAX_DOCK_GRID};
				}

				addDockingCell(dockInsets, rootCc.getDockSide(), new CompWrap(comp, rootCc, hideMode, pos, cbSz));
				i++;
				continue;
			}

			final Boolean cellFlowX = rootCc.getFlowX();
			Cell cell = null;

			if (rootCc.isNewline()) {
				wrap(cellXY, rootCc.getNewlineGapSize());
			}
			else if (hitEndOfRow) {
				wrap(cellXY, null);
			}
			hitEndOfRow = false;

			final boolean rowNoGrid = lc.isNoGrid()
				|| ((DimConstraint) MigLayoutToolkit.getMigLayoutUtil().getIndexSafe(specs, lc.isFlowX() ? cellXY[1] : cellXY[0])).isNoGrid();

			// Move to a free y, x  if no absolute grid specified
			final int cx = rootCc.getCellX();
			final int cy = rootCc.getCellY();
			if ((cx < 0 || cy < 0) && rowNoGrid == false && rootCc.getSkip() == 0) { // 3.7.2: If skip, don't find an empty cell first.
				while (isCellFree(cellXY[1], cellXY[0], spannedRects) == false) {
					if (Math.abs(increase(cellXY, 1)) >= wrap) {
						wrap(cellXY, null);
					}
				}
			}
			else {
				if (cx >= 0 && cy >= 0) {
					if (cy >= 0) {
						cellXY[0] = cx;
						cellXY[1] = cy;
					}
					else { // Only one coordinate is specified. Use the current row (flowx) or column (flowy) to fill in.
						if (lc.isFlowX()) {
							cellXY[0] = cx;
						}
						else {
							cellXY[1] = cx;
						}
					}
				}
				cell = getCell(cellXY[1], cellXY[0]); // Might be null
			}

			// Skip a number of cells. Changed for 3.6.1 to take wrap into account and thus "skip" to the next and possibly more rows.
			final int skipCount = rootCc.getSkip();
			for (int s = 0; s < skipCount; s++) {
				do {
					if (Math.abs(increase(cellXY, 1)) >= wrap) {
						wrap(cellXY, null);
					}
				}
				while (isCellFree(cellXY[1], cellXY[0], spannedRects) == false);
			}

			// If cell is not created yet, create it and set it.
			if (cell == null) {
				final int spanx = Math.min(rowNoGrid && lc.isFlowX() ? LayoutUtil.INF : rootCc.getSpanX(), MAX_GRID - cellXY[0]);
				final int spany = Math.min(rowNoGrid && !lc.isFlowX() ? LayoutUtil.INF : rootCc.getSpanY(), MAX_GRID - cellXY[1]);

				cell = new Cell(spanx, spany, cellFlowX != null ? cellFlowX : lc.isFlowX());

				setCell(cellXY[1], cellXY[0], cell);

				// Add a rectangle so we can know that spanned cells occupy more space.
				if (spanx > 1 || spany > 1) {
					spannedRects.add(new int[] {cellXY[0], cellXY[1], spanx, spany});
				}
			}

			// Add the one, or all, components that split the grid position to the same Cell.
			boolean wrapHandled = false;
			int splitLeft = rowNoGrid ? LayoutUtil.INF : rootCc.getSplit() - 1;
			boolean splitExit = false;
			final boolean spanRestOfRow = (lc.isFlowX() ? rootCc.getSpanX() : rootCc.getSpanY()) == LayoutUtil.INF;

			for (; splitLeft >= 0 && i < comps.length; splitLeft--) {
				final IComponentWrapper compAdd = comps[i];
				final CC cc = getCC(compAdd, ccMap);

				addLinkIDs(cc);

				final boolean visible = compAdd.isVisible();
				hideMode = visible ? -1 : cc.getHideMode() != -1 ? cc.getHideMode() : lc.getHideMode();

				if (cc.isExternal() || hideMode == 3) {
					i++;
					splitLeft++; // Added for 3.5.5 so that these components does not "take" a split slot.
					continue; // To work with situations where there are components that does not have a layout manager, or not this one.
				}

				hasPushX |= (visible || hideMode > 1) && (cc.getPushX() != null);
				hasPushY |= (visible || hideMode > 1) && (cc.getPushY() != null);

				if (cc != rootCc) { // If not first in a cell
					if (cc.isNewline() || cc.isBoundsInGrid() == false || cc.getDockSide() != -1) {
						break;
					}

					if (splitLeft > 0 && cc.getSkip() > 0) {
						splitExit = true;
						break;
					}

					pos = getPos(compAdd, cc);
					cbSz = getCallbackSize(compAdd);
				}

				final CompWrap cw = new CompWrap(compAdd, cc, hideMode, pos, cbSz);
				cell.compWraps.add(cw);
				cell.hasTagged |= cc.getTag() != null;
				hasTagged |= cell.hasTagged;

				if (cc != rootCc) {
					if (cc.getHorizontal().getSizeGroup() != null) {
						sizeGroupsX++;
					}
					if (cc.getVertical().getSizeGroup() != null) {
						sizeGroupsY++;
					}
				}

				i++;

				if ((cc.isWrap() || (spanRestOfRow && splitLeft == 0))) {
					if (cc.isWrap()) {
						wrap(cellXY, cc.getWrapGapSize());
					}
					else {
						hitEndOfRow = true;
					}
					wrapHandled = true;
					break;
				}
			}

			if (wrapHandled == false && rowNoGrid == false) {
				final int span = lc.isFlowX() ? cell.spanx : cell.spany;
				if (Math.abs((lc.isFlowX() ? cellXY[0] : cellXY[1])) + span >= wrap) {
					hitEndOfRow = true;
				}
				else {
					increase(cellXY, splitExit ? span - 1 : span);
				}
			}
		}

		// If there were size groups, calculate the largest values in the groups (for min/pref/max) and enforce them on the rest in the group.
		if (sizeGroupsX > 0 || sizeGroupsY > 0) {
			final HashMap<String, int[]> sizeGroupMapX = sizeGroupsX > 0 ? new HashMap<String, int[]>(sizeGroupsX) : null;
			final HashMap<String, int[]> sizeGroupMapY = sizeGroupsY > 0 ? new HashMap<String, int[]>(sizeGroupsY) : null;
			final ArrayList<CompWrap> sizeGroupCWs = new ArrayList<CompWrap>(Math.max(sizeGroupsX, sizeGroupsY));

			for (final Cell cell : grid.values()) {
				for (int i = 0; i < cell.compWraps.size(); i++) {
					final CompWrap cw = cell.compWraps.get(i);
					final String sgx = cw.cc.getHorizontal().getSizeGroup();
					final String sgy = cw.cc.getVertical().getSizeGroup();

					if (sgx != null || sgy != null) {
						if (sgx != null && sizeGroupMapX != null) {
							addToSizeGroup(sizeGroupMapX, sgx, cw.horSizes);
						}
						if (sgy != null && sizeGroupMapY != null) {
							addToSizeGroup(sizeGroupMapY, sgy, cw.verSizes);
						}
						sizeGroupCWs.add(cw);
					}
				}
			}

			// Set/equalize the sizeGroups to same the values.
			for (final CompWrap cw : sizeGroupCWs) {
				if (sizeGroupMapX != null) {
					cw.setSizes(sizeGroupMapX.get(cw.cc.getHorizontal().getSizeGroup()), true); // Target method handles null sizes
				}
				if (sizeGroupMapY != null) {
					cw.setSizes(sizeGroupMapY.get(cw.cc.getVertical().getSizeGroup()), false); // Target method handles null sizes
				}
			}
		} // Component loop

		// If there were size groups, calculate the largest values in the groups (for min/pref/max) and enforce them on the rest in the group.
		if (sizeGroupsX > 0 || sizeGroupsY > 0) {
			final HashMap<String, int[]> sizeGroupMapX = sizeGroupsX > 0 ? new HashMap<String, int[]>(sizeGroupsX) : null;
			final HashMap<String, int[]> sizeGroupMapY = sizeGroupsY > 0 ? new HashMap<String, int[]>(sizeGroupsY) : null;
			final ArrayList<CompWrap> sizeGroupCWs = new ArrayList<CompWrap>(Math.max(sizeGroupsX, sizeGroupsY));

			for (final Cell cell : grid.values()) {
				for (int i = 0; i < cell.compWraps.size(); i++) {
					final CompWrap cw = cell.compWraps.get(i);
					final String sgx = cw.cc.getHorizontal().getSizeGroup();
					final String sgy = cw.cc.getVertical().getSizeGroup();

					if (sgx != null || sgy != null) {
						if (sgx != null && sizeGroupMapX != null) {
							addToSizeGroup(sizeGroupMapX, sgx, cw.horSizes);
						}
						if (sgy != null && sizeGroupMapY != null) {
							addToSizeGroup(sizeGroupMapY, sgy, cw.verSizes);
						}
						sizeGroupCWs.add(cw);
					}
				}
			}

			// Set/equalize the sizeGroups to same the values.
			for (final CompWrap cw : sizeGroupCWs) {
				if (sizeGroupMapX != null) {
					cw.setSizes(sizeGroupMapX.get(cw.cc.getHorizontal().getSizeGroup()), true); // Target method handles null sizes
				}
				if (sizeGroupMapY != null) {
					cw.setSizes(sizeGroupMapY.get(cw.cc.getVertical().getSizeGroup()), false); // Target method handles null sizes
				}
			}
		}

		if (hasTagged) {
			sortCellsByPlatform(grid.values(), container);
		}

		// Calculate gaps now that the cells are filled and we know all adjacent components.
		final boolean ltr = MigLayoutToolkit.getMigLayoutUtil().isLeftToRight(lc, container);
		for (final Cell cell : grid.values()) {
			final ArrayList<CompWrap> cws = cell.compWraps;

			final int lastI = cws.size() - 1;
			for (int i = 0; i <= lastI; i++) {
				final CompWrap cw = cws.get(i);
				final IComponentWrapper cwBef = i > 0 ? cws.get(i - 1).comp : null;
				final IComponentWrapper cwAft = i < lastI ? cws.get(i + 1).comp : null;

				final String tag = getCC(cw.comp, ccMap).getTag();
				final CC ccBef = cwBef != null ? getCC(cwBef, ccMap) : null;
				final CC ccAft = cwAft != null ? getCC(cwAft, ccMap) : null;

				cw.calcGaps(cwBef, ccBef, cwAft, ccAft, tag, cell.flowx, ltr);
			}
		}

		dockOffX = getDockInsets(colIndexes);
		dockOffY = getDockInsets(rowIndexes);

		// Add synthetic indexes for empty rows and columns so they can get a size
		int iSz = rowConstr.getCount();
		for (int i = 0; i < iSz; i++) {
			rowIndexes.add(i);
		}
		iSz = colConstr.getCount();
		for (int i = 0; i < iSz; i++) {
			colIndexes.add(i);
		}

		colGroupLists = divideIntoLinkedGroups(false);
		rowGroupLists = divideIntoLinkedGroups(true);

		pushXs = hasPushX || lc.isFillX() ? getDefaultPushWeights(false) : null;
		pushYs = hasPushY || lc.isFillY() ? getDefaultPushWeights(true) : null;

		if (MigLayoutToolkit.getMigLayoutUtil().isDesignTime(container)) {
			saveGrid(container, grid);
		}
	}

	private static CC getCC(final IComponentWrapper comp, final Map<IComponentWrapper, CC> ccMap) {
		final CC cc = ccMap.get(comp);
		return cc != null ? cc : new CC();
	}

	private void addLinkIDs(final CC cc) {
		final String[] linkIDs = cc.getLinkTargets();
		for (final String linkID : linkIDs) {
			if (linkTargetIDs == null) {
				linkTargetIDs = new HashMap<String, Boolean>();
			}
			linkTargetIDs.put(linkID, null);
		}
	}

	/**
	 * If the container (parent) that this grid is laying out has changed its bounds, call this method to
	 * clear any cached values.
	 */
	public void invalidateContainerSize() {
		colFlowSpecs = null;
	}

	/**
	 * Does the actual layout. Uses many values calculated in the constructor.
	 * 
	 * @param bounds The bounds to layout against. Normally that of the parent. [x, y, width, height].
	 * @param alignX The alignment for the x-axis.
	 * @param alignY The alignment for the y-axis.
	 * @param debug If debug information should be saved in {@link #debugRects}.
	 * @param checkPrefChange If a check should be done to see if the setting of any new bounds changes the preferred size
	 *            of a component.
	 * @return If the layout has probably changed the preferred size and there is need for a new layout (normally only SWT).
	 */
	public boolean layout(
		final int[] bounds,
		final UnitValue alignX,
		final UnitValue alignY,
		final boolean debug,
		final boolean checkPrefChange) {
		if (debug) {
			debugRects = new ArrayList<int[]>();
		}

		checkSizeCalcs();

		resetLinkValues(true, true);

		layoutInOneDim(bounds[2], alignX, false, pushXs);
		layoutInOneDim(bounds[3], alignY, true, pushYs);

		HashMap<String, Integer> endGrpXMap = null;
		HashMap<String, Integer> endGrpYMap = null;
		final int compCount = container.getComponentCount();

		// Transfer the calculated bound from the ComponentWrappers to the actual Components.
		boolean layoutAgain = false;
		if (compCount > 0) {
			for (int j = 0; j < (linkTargetIDs != null ? 2 : 1); j++) { // First do the calculations (maybe more than once) then set the bounds when done
				boolean doAgain;
				int count = 0;
				do {
					doAgain = false;
					for (final Cell cell : grid.values()) {
						final ArrayList<CompWrap> compWraps = cell.compWraps;
						final int iSz = compWraps.size();
						for (int i = 0; i < iSz; i++) {
							final CompWrap cw = compWraps.get(i);

							if (j == 0) {
								doAgain |= doAbsoluteCorrections(cw, bounds);
								if (doAgain == false) { // If we are going to do this again, do not bother this time around
									if (cw.cc.getHorizontal().getEndGroup() != null) {
										endGrpXMap = addToEndGroup(endGrpXMap, cw.cc.getHorizontal().getEndGroup(), cw.x + cw.w);
									}
									if (cw.cc.getVertical().getEndGroup() != null) {
										endGrpYMap = addToEndGroup(endGrpYMap, cw.cc.getVertical().getEndGroup(), cw.y + cw.h);
									}
								}

								// @since 3.7.2 Needed or absolute "pos" pointing to "visual" or "container" didn't work if
								// their bounds changed during the layout cycle. At least not in SWT.
								if (linkTargetIDs != null
									&& (linkTargetIDs.containsKey("visual") || linkTargetIDs.containsKey("container"))) {
									layoutAgain = true;
								}
							}

							if (linkTargetIDs == null || j == 1) {
								if (cw.cc.getHorizontal().getEndGroup() != null) {
									cw.w = endGrpXMap.get(cw.cc.getHorizontal().getEndGroup()) - cw.x;
								}
								if (cw.cc.getVertical().getEndGroup() != null) {
									cw.h = endGrpYMap.get(cw.cc.getVertical().getEndGroup()) - cw.y;
								}

								cw.x += bounds[0];
								cw.y += bounds[1];
								layoutAgain |= cw.transferBounds(checkPrefChange && !layoutAgain);

								if (callbackList != null) {
									for (final AbstractLayoutCallback callback : callbackList) {
										callback.correctBounds(cw.comp);
									}
								}
							}
						}
					}
					clearGroupLinkBounds();
					if (++count > ((compCount << 3) + 10)) {
						//CHECKSTYLE:OFF
						System.err.println("Unstable cyclic dependency in absolute linked values!");
						//CHECKSTYLE:ON
						break;
					}

				}
				while (doAgain);
			}
		}

		// Add debug shapes for the "cells". Use the CompWraps as base for inding the cells.
		if (debug) {
			for (final Cell cell : grid.values()) {
				final ArrayList<CompWrap> compWraps = cell.compWraps;
				final int iSz = compWraps.size();
				for (int i = 0; i < iSz; i++) {
					final CompWrap cw = compWraps.get(i);
					final LinkedDimGroup hGrp = getGroupContaining(colGroupLists, cw);
					final LinkedDimGroup vGrp = getGroupContaining(rowGroupLists, cw);

					if (hGrp != null && vGrp != null) {
						debugRects.add(new int[] {
								hGrp.lStart + bounds[0] - (hGrp.fromEnd ? hGrp.lSize : 0),
								vGrp.lStart + bounds[1] - (vGrp.fromEnd ? vGrp.lSize : 0), hGrp.lSize, vGrp.lSize});
					}
				}
			}
		}
		return layoutAgain;
	}

	public void paintDebug() {
		if (debugRects != null) {
			container.paintDebugOutline();

			final ArrayList<int[]> painted = new ArrayList<int[]>();
			for (int i = 0; i < debugRects.size(); i++) {
				final int[] r = debugRects.get(i);
				if (painted.contains(r) == false) {
					container.paintDebugCell(r[0], r[1], r[2], r[3]);
					painted.add(r);
				}
			}

			for (final Cell cell : grid.values()) {
				final ArrayList<CompWrap> compWraps = cell.compWraps;
				for (int i = 0; i < compWraps.size(); i++) {
					compWraps.get(i).comp.paintDebugOutline();
				}
			}
		}
	}

	public IContainerWrapper getContainer() {
		return container;
	}

	public int[] getWidth() {
		checkSizeCalcs();
		return width.clone();
	}

	public int[] getHeight() {
		checkSizeCalcs();
		return height.clone();
	}

	private void checkSizeCalcs() {
		if (colFlowSpecs == null) {

			colFlowSpecs = calcRowsOrColsSizes(true);
			rowFlowSpecs = calcRowsOrColsSizes(false);

			width = getMinPrefMaxSumSize(true);
			height = getMinPrefMaxSumSize(false);

			if (linkTargetIDs == null) {
				resetLinkValues(false, true);
			}
			else {
				// This call makes some components flicker on SWT. They get their bounds changed twice since
				// the change might affect the absolute size adjustment below. There's no way around this that
				// I know of.
				layout(new int[4], null, null, false, false);
				resetLinkValues(false, false);
			}

			adjustSizeForAbsolute(true);
			adjustSizeForAbsolute(false);
		}
	}

	private UnitValue[] getPos(final IComponentWrapper cw, final CC cc) {
		UnitValue[] cbPos = null;
		if (callbackList != null) {
			for (int i = 0; i < callbackList.size() && cbPos == null; i++) {
				cbPos = callbackList.get(i).getPosition(cw); // NOT a copy!
			}
		}

		// If one is null, return the other (which many also be null)
		final UnitValue[] ccPos = cc.getPos(); // A copy!!
		if (cbPos == null || ccPos == null) {
			return cbPos != null ? cbPos : ccPos;
		}

		// Merge
		for (int i = 0; i < 4; i++) {
			final UnitValue cbUv = cbPos[i];
			if (cbUv != null) {
				ccPos[i] = cbUv;
			}
		}

		return ccPos;
	}

	private BoundSize[] getCallbackSize(final IComponentWrapper cw) {
		if (callbackList != null) {
			for (final AbstractLayoutCallback callback : callbackList) {
				final BoundSize[] bs = callback.getSize(cw); // NOT a copy!
				if (bs != null) {
					return bs;
				}
			}
		}
		return null;
	}

	private static int getDockInsets(final TreeSet<Integer> set) {
		int c = 0;
		for (final Integer i : set) {
			if (i < -MAX_GRID) {
				c++;
			}
			else {
				break; // Since they are sorted we can break
			}
		}
		return c;
	}

	/**
	 * @param cw Never <code>null</code>.
	 * @param cc Never <code>null</code>.
	 * @param external The bounds should be stored even if they are not in {@link #linkTargetIDs}.
	 * @return If a change has been made.
	 */
	private boolean setLinkedBounds(
		final IComponentWrapper cw,
		final CC cc,
		final int x,
		final int y,
		final int w,
		final int h,
		final boolean external) {
		String id = cc.getId() != null ? cc.getId() : cw.getLinkId();
		if (id == null) {
			return false;
		}

		String gid = null;
		final int grIx = id.indexOf('.');
		if (grIx != -1) {
			gid = id.substring(0, grIx);
			id = id.substring(grIx + 1);
		}

		final Object lay = container.getLayout();
		boolean changed = false;
		if (external || (linkTargetIDs != null && linkTargetIDs.containsKey(id))) {
			changed = MigLayoutToolkit.getMigLinkHandler().setBounds(lay, id, x, y, w, h, !external, false);
		}

		if (gid != null && (external || (linkTargetIDs != null && linkTargetIDs.containsKey(gid)))) {
			if (linkTargetIDs == null) {
				linkTargetIDs = new HashMap<String, Boolean>(4);
			}

			linkTargetIDs.put(gid, Boolean.TRUE);
			changed |= MigLayoutToolkit.getMigLinkHandler().setBounds(lay, gid, x, y, w, h, !external, true);
		}

		return changed;
	}

	/**
	 * Go to next cell.
	 * 
	 * @param p The point to increase
	 * @param cnt How many cells to advance.
	 * @return The new value in the "incresing" dimension.
	 */
	private int increase(final int[] p, final int cnt) {
		if (lc.isFlowX()) {
			p[0] = p[0] + cnt;
			return p[0];
		}
		else {
			p[1] = p[1] + cnt;
			return p[1];
		}
	}

	/**
	 * Wraps to the next row or column depending on if horizontal flow or vertical flow is used.
	 * 
	 * @param cellXY The point to wrap and thus set either x or y to 0 and increase the other one.
	 * @param gapSize The gaps size specified in a "wrap XXX" or "newline XXX" or <code>null</code> if none.
	 */
	private void wrap(final int[] cellXY, final BoundSize gapSize) {
		final boolean flowx = lc.isFlowX();
		cellXY[0] = flowx ? 0 : cellXY[0] + 1;
		cellXY[1] = flowx ? cellXY[1] + 1 : 0;

		if (gapSize != null) {
			if (wrapGapMap == null) {
				wrapGapMap = new HashMap<Integer, BoundSize>(8);
			}

			wrapGapMap.put(cellXY[flowx ? 1 : 0], gapSize);
		}

		// add the row/column so that the gap in the last row/col will not be removed.
		if (flowx) {
			rowIndexes.add(cellXY[1]);
		}
		else {
			colIndexes.add(cellXY[0]);
		}
	}

	/**
	 * Sort components (normally buttons in a button bar) so they appear in the correct order.
	 * 
	 * @param cells The cells to sort.
	 * @param parent The parent.
	 */
	private static void sortCellsByPlatform(final Collection<Cell> cells, final IContainerWrapper parent) {
		final String order = MigLayoutToolkit.getMigPlatformDefaults().getButtonOrder();
		final String orderLo = order.toLowerCase();

		final int unrelSize = MigLayoutToolkit.getMigPlatformDefaults().convertToPixels(1, "u", true, 0, parent, null);

		if (unrelSize == UnitConverter.UNABLE) {
			throw new IllegalArgumentException("'unrelated' not recognized by PlatformDefaults!");
		}

		final int[] gapUnrel = new int[] {unrelSize, unrelSize, LayoutUtil.NOT_SET};
		final int[] flGap = new int[] {0, 0, LayoutUtil.NOT_SET};

		for (final Cell cell : cells) {
			if (cell.hasTagged == false) {
				continue;
			}

			CompWrap prevCW = null;
			boolean nextUnrel = false;
			boolean nextPush = false;
			final ArrayList<CompWrap> sortedList = new ArrayList<CompWrap>(cell.compWraps.size());

			final int iSz = orderLo.length();
			for (int i = 0; i < iSz; i++) {
				final char c = orderLo.charAt(i);
				if (c == '+' || c == '_') {
					nextUnrel = true;
					if (c == '+') {
						nextPush = true;
					}
				}
				else {
					final String tag = PlatformDefaults.getTagForChar(c);
					if (tag != null) {
						final int jSz = cell.compWraps.size();
						for (int j = 0; j < jSz; j++) {
							final CompWrap cw = cell.compWraps.get(j);
							if (!tag.equals(cw.cc.getTag())) {
								continue;
							}
							if (Character.isUpperCase(order.charAt(i))) {
								final int min = MigLayoutToolkit.getMigPlatformDefaults().getMinimumButtonWidth().getPixels(
										0,
										parent,
										cw.comp);
								if (min > cw.horSizes[LayoutUtil.MIN]) {
									cw.horSizes[LayoutUtil.MIN] = min;
								}

								correctMinMax(cw.horSizes);
							}

							sortedList.add(cw);

							if (nextUnrel) {
								(prevCW != null ? prevCW : cw).mergeGapSizes(gapUnrel, cell.flowx, prevCW == null);
								if (nextPush) {
									cw.forcedPushGaps = 1;
									nextUnrel = false;
									nextPush = false;
								}
							}

							// "unknown" components will always get an Unrelated gap.
							if (c == 'u') {
								nextUnrel = true;
							}
							prevCW = cw;
						}
					}
				}
			}

			// If we have a gap that was supposed to push but no more components was found to but the "gap before" then compensate.
			if (sortedList.size() > 0) {
				CompWrap cw = sortedList.get(sortedList.size() - 1);
				if (nextUnrel) {
					cw.mergeGapSizes(gapUnrel, cell.flowx, false);
					if (nextPush) {
						cw.forcedPushGaps |= 2;
					}
				}

				// Remove first and last gap if not set explicitly.
				if (cw.cc.getHorizontal().getGapAfter() == null) {
					cw.setGaps(flGap, 3);
				}

				cw = sortedList.get(0);
				if (cw.cc.getHorizontal().getGapBefore() == null) {
					cw.setGaps(flGap, 1);
				}
			}

			// Exchange the unsorted CompWraps for the sorted one.
			if (cell.compWraps.size() == sortedList.size()) {
				cell.compWraps.clear();
			}
			else {
				cell.compWraps.removeAll(sortedList);
			}
			cell.compWraps.addAll(sortedList);
		}
	}

	private Float[] getDefaultPushWeights(final boolean isRows) {
		final ArrayList<LinkedDimGroup>[] groupLists = isRows ? rowGroupLists : colGroupLists;

		Float[] pushWeightArr = GROW_100; // Only create specific if any of the components have grow.
		int ix = 1;
		for (int i = 0; i < groupLists.length; i++, ix += 2) {
			final ArrayList<LinkedDimGroup> grps = groupLists[i];
			Float rowPushWeight = null;
			for (final LinkedDimGroup grp : grps) {
				for (int c = 0; c < grp.ldgCompWraps.size(); c++) {
					final CompWrap cw = grp.ldgCompWraps.get(c);
					final int hideMode = cw.comp.isVisible() ? -1 : cw.cc.getHideMode() != -1
							? cw.cc.getHideMode() : lc.getHideMode();

					final Float pushWeight = hideMode < 2 ? (isRows ? cw.cc.getPushY() : cw.cc.getPushX()) : null;
					if (rowPushWeight == null || (pushWeight != null && pushWeight.floatValue() > rowPushWeight.floatValue())) {
						rowPushWeight = pushWeight;
					}
				}
			}

			if (rowPushWeight != null) {
				if (pushWeightArr == GROW_100) {
					pushWeightArr = new Float[(groupLists.length << 1) + 1];
				}
				pushWeightArr[ix] = rowPushWeight;
			}
		}

		return pushWeightArr;
	}

	private void clearGroupLinkBounds() {
		if (linkTargetIDs == null) {
			return;
		}

		for (final Map.Entry<String, Boolean> o : linkTargetIDs.entrySet()) {
			if (o.getValue() == Boolean.TRUE) {
				MigLayoutToolkit.getMigLinkHandler().clearBounds(container.getLayout(), o.getKey());
			}
		}
	}

	private void resetLinkValues(final boolean parentSize, final boolean compLinks) {
		final Object lay = container.getLayout();
		if (compLinks) {
			MigLayoutToolkit.getMigLinkHandler().clearTemporaryBounds(lay);
		}

		final boolean defIns = !hasDocks();

		final int parW = parentSize
				? lc.getWidth().constrain(container.getWidth(), getParentSize(container, true), container) : 0;
		final int parH = parentSize
				? lc.getHeight().constrain(container.getHeight(), getParentSize(container, false), container) : 0;

		final LayoutUtil layoutUtil = MigLayoutToolkit.getMigLayoutUtil();
		final int insX = layoutUtil.getInsets(lc, 0, defIns).getPixels(0, container, null);
		final int insY = layoutUtil.getInsets(lc, 1, defIns).getPixels(0, container, null);
		final int visW = parW - insX - layoutUtil.getInsets(lc, 2, defIns).getPixels(0, container, null);
		final int visH = parH - insY - layoutUtil.getInsets(lc, 3, defIns).getPixels(0, container, null);

		MigLayoutToolkit.getMigLinkHandler().setBounds(lay, "visual", insX, insY, visW, visH, true, false);
		MigLayoutToolkit.getMigLinkHandler().setBounds(lay, "container", 0, 0, parW, parH, true, false);
	}

	/**
	 * Returns the {@link net.miginfocom.layout.Grid.LinkedDimGroup} that has the {@link net.miginfocom.layout.Grid.CompWrap}
	 * <code>cw</code>.
	 * 
	 * @param groupLists The lists to search in.
	 * @param cw The component wrap to find.
	 * @return The linked group or <code>null</code> if none had the component wrap.
	 */
	private static LinkedDimGroup getGroupContaining(final ArrayList<LinkedDimGroup>[] groupLists, final CompWrap cw) {
		for (final ArrayList<LinkedDimGroup> groups : groupLists) {
			final int jSz = groups.size();
			for (int j = 0; j < jSz; j++) {
				final ArrayList<CompWrap> cwList = groups.get(j).ldgCompWraps;
				final int kSz = cwList.size();
				for (int k = 0; k < kSz; k++) {
					if (cwList.get(k) == cw) {
						return groups.get(j);
					}
				}
			}
		}
		return null;
	}

	private boolean doAbsoluteCorrections(final CompWrap cw, final int[] bounds) {
		boolean changed = false;

		int[] stSz = getAbsoluteDimBounds(cw, bounds[2], true);
		if (stSz != null) {
			cw.setDimBounds(stSz[0], stSz[1], true);
		}

		stSz = getAbsoluteDimBounds(cw, bounds[3], false);
		if (stSz != null) {
			cw.setDimBounds(stSz[0], stSz[1], false);
		}

		// If there is a link id, store the new bounds.
		if (linkTargetIDs != null) {
			changed = setLinkedBounds(cw.comp, cw.cc, cw.x, cw.y, cw.w, cw.h, false);
		}

		return changed;
	}

	private void adjustSizeForAbsolute(final boolean isHor) {
		final int[] curSizes = isHor ? width : height;

		final Cell absCell = grid.get(null);
		if (absCell == null || absCell.compWraps.size() == 0) {
			return;
		}

		final ArrayList<CompWrap> cws = absCell.compWraps;

		int maxEnd = 0;
		final int cwSz = absCell.compWraps.size();
		for (int j = 0; j < cwSz + 3; j++) { // "Do Again" max absCell.compWraps.size() + 3 times.
			boolean doAgain = false;
			for (int i = 0; i < cwSz; i++) {
				final CompWrap cw = cws.get(i);
				final int[] stSz = getAbsoluteDimBounds(cw, 0, isHor);
				final int end = stSz[0] + stSz[1];
				if (maxEnd < end) {
					maxEnd = end;
				}

				// If there is a link id, store the new bounds.
				if (linkTargetIDs != null) {
					doAgain |= setLinkedBounds(cw.comp, cw.cc, stSz[0], stSz[0], stSz[1], stSz[1], false);
				}
			}
			if (doAgain == false) {
				break;
			}

			// We need to check this again since the coords may be smaller this round.
			maxEnd = 0;
			clearGroupLinkBounds();
		}

		maxEnd += MigLayoutToolkit.getMigLayoutUtil().getInsets(lc, isHor ? 3 : 2, !hasDocks()).getPixels(0, container, null);

		if (curSizes[LayoutUtil.MIN] < maxEnd) {
			curSizes[LayoutUtil.MIN] = maxEnd;
		}
		if (curSizes[LayoutUtil.PREF] < maxEnd) {
			curSizes[LayoutUtil.PREF] = maxEnd;
		}
	}

	private int[] getAbsoluteDimBounds(final CompWrap cw, final int refSize, final boolean isHor) {
		if (cw.cc.isExternal()) {
			if (isHor) {
				return new int[] {cw.comp.getX(), cw.comp.getWidth()};
			}
			else {
				return new int[] {cw.comp.getY(), cw.comp.getHeight()};
			}
		}

		final int[] plafPad = lc.isVisualPadding() ? cw.comp.getVisualPadding() : null;
		final UnitValue[] pad = cw.cc.getPadding();

		// If no changes do not create a lot of objects
		if (cw.pos == null && plafPad == null && pad == null) {
			return null;
		}

		// Set start
		int st = isHor ? cw.x : cw.y;
		int sz = isHor ? cw.w : cw.h;

		// If absolute, use those coordinates instead.
		if (cw.pos != null) {
			final UnitValue stUV = cw.pos != null ? cw.pos[isHor ? 0 : 1] : null;
			final UnitValue endUV = cw.pos != null ? cw.pos[isHor ? 2 : 3] : null;

			final int minSz = cw.getSize(LayoutUtil.MIN, isHor);
			final int maxSz = cw.getSize(LayoutUtil.MAX, isHor);
			sz = Math.min(Math.max(cw.getSize(LayoutUtil.PREF, isHor), minSz), maxSz);

			if (stUV != null) {
				st = stUV.getPixels(stUV.getUnit() == UnitValueToolkit.ALIGN ? sz : refSize, container, cw.comp);

				if (endUV != null) { // if (endUV == null && cw.cc.isBoundsIsGrid() == true)
					sz = Math.min(Math.max((isHor ? (cw.x + cw.w) : (cw.y + cw.h)) - st, minSz), maxSz);
				}
			}

			if (endUV != null) {
				if (stUV != null) { // if (stUV != null || cw.cc.isBoundsIsGrid()) {
					sz = Math.min(Math.max(endUV.getPixels(refSize, container, cw.comp) - st, minSz), maxSz);
				}
				else {
					st = endUV.getPixels(refSize, container, cw.comp) - sz;
				}
			}
		}

		// If constraint has padding -> correct the start/size
		if (pad != null) {
			UnitValue uv = pad[isHor ? 1 : 0];
			final int p = uv != null ? uv.getPixels(refSize, container, cw.comp) : 0;
			st += p;
			uv = pad[isHor ? 3 : 2];
			sz += -p + (uv != null ? uv.getPixels(refSize, container, cw.comp) : 0);
		}

		// If the plaf converter has padding -> correct the start/size
		if (plafPad != null) {
			final int p = plafPad[isHor ? 1 : 0];
			st += p;
			sz += -p + (plafPad[isHor ? 3 : 2]);
		}

		return new int[] {st, sz};
	}

	private void layoutInOneDim(final int refSize, final UnitValue align, final boolean isRows, final Float[] defaultPushWeights) {
		final LayoutUtil layoutUtil = MigLayoutToolkit.getMigLayoutUtil();

		final boolean fromEnd = !(isRows ? lc.isTopToBottom() : layoutUtil.isLeftToRight(lc, container));
		final DimConstraint[] primDCs = (isRows ? rowConstr : colConstr).getConstaints();
		final FlowSizeSpec fss = isRows ? rowFlowSpecs : colFlowSpecs;
		final ArrayList<LinkedDimGroup>[] rowCols = isRows ? rowGroupLists : colGroupLists;

		final int[] rowColSizes = layoutUtil.calculateSerial(
				fss.sizes,
				fss.resConstsInclGaps,
				defaultPushWeights,
				LayoutUtil.PREF,
				refSize);

		if (layoutUtil.isDesignTime(container)) {
			final TreeSet<Integer> indexes = isRows ? rowIndexes : colIndexes;
			final int[] ixArr = new int[indexes.size()];
			int ix = 0;
			for (final Integer i : indexes) {
				ixArr[ix++] = i;
			}

			putSizesAndIndexes(container.getComponent(), rowColSizes, ixArr, isRows);
		}

		int curPos = align != null ? align.getPixels(refSize - LayoutUtil.sum(rowColSizes), container, null) : 0;

		if (fromEnd) {
			curPos = refSize - curPos;
		}

		for (int i = 0; i < rowCols.length; i++) {
			final ArrayList<LinkedDimGroup> linkedGroups = rowCols[i];
			final int scIx = i - (isRows ? dockOffY : dockOffX);

			final int bIx = i << 1;
			final int bIx2 = bIx + 1;

			curPos += (fromEnd ? -rowColSizes[bIx] : rowColSizes[bIx]);

			final DimConstraint primDC = scIx >= 0
					? primDCs[scIx >= primDCs.length ? primDCs.length - 1 : scIx] : DOCK_DIM_CONSTRAINT;

			final int rowSize = rowColSizes[bIx2];

			for (final LinkedDimGroup group : linkedGroups) {
				int groupSize = rowSize;
				if (group.span > 1) {
					groupSize = LayoutUtil.sum(rowColSizes, bIx2, Math.min((group.span << 1) - 1, rowColSizes.length - bIx2 - 1));
				}

				group.layout(primDC, curPos, groupSize, group.span);
			}

			curPos += (fromEnd ? -rowSize : rowSize);
		}
	}

	private static void addToSizeGroup(final HashMap<String, int[]> sizeGroups, final String sizeGroup, final int[] size) {
		final int[] sgSize = sizeGroups.get(sizeGroup);
		if (sgSize == null) {
			sizeGroups.put(sizeGroup, new int[] {size[LayoutUtil.MIN], size[LayoutUtil.PREF], size[LayoutUtil.MAX]});
		}
		else {
			sgSize[LayoutUtil.MIN] = Math.max(size[LayoutUtil.MIN], sgSize[LayoutUtil.MIN]);
			sgSize[LayoutUtil.PREF] = Math.max(size[LayoutUtil.PREF], sgSize[LayoutUtil.PREF]);
			sgSize[LayoutUtil.MAX] = Math.min(size[LayoutUtil.MAX], sgSize[LayoutUtil.MAX]);
		}
	}

	private static HashMap<String, Integer> addToEndGroup(HashMap<String, Integer> endGroups, final String endGroup, final int end) {
		if (endGroup != null) {
			if (endGroups == null) {
				endGroups = new HashMap<String, Integer>(2);
			}

			final Integer oldEnd = endGroups.get(endGroup);
			if (oldEnd == null || end > oldEnd) {
				endGroups.put(endGroup, end);
			}
		}
		return endGroups;
	}

	/**
	 * Calculates Min, Preferred and Max size for the columns OR rows.
	 * 
	 * @param isHor If it is the horizontal dimension to calculate.
	 * @return The sizes in a {@link net.miginfocom.layout.Grid.FlowSizeSpec}.
	 */
	private FlowSizeSpec calcRowsOrColsSizes(final boolean isHor) {
		final ArrayList<LinkedDimGroup>[] groupsLists = isHor ? colGroupLists : rowGroupLists;
		final Float[] defPush = isHor ? pushXs : pushYs;
		int refSize = isHor ? container.getWidth() : container.getHeight();

		final BoundSize cSz = isHor ? lc.getWidth() : lc.getHeight();
		if (cSz.isUnset() == false) {
			refSize = cSz.constrain(refSize, getParentSize(container, isHor), container);
		}

		final DimConstraint[] primDCs = (isHor ? colConstr : rowConstr).getConstaints();
		final TreeSet<Integer> primIndexes = isHor ? colIndexes : rowIndexes;

		final int[][] rowColBoundSizes = new int[primIndexes.size()][];
		final HashMap<String, int[]> sizeGroupMap = new HashMap<String, int[]>(2);
		final DimConstraint[] allDCs = new DimConstraint[primIndexes.size()];

		final Iterator<Integer> primIt = primIndexes.iterator();
		for (int r = 0; r < rowColBoundSizes.length; r++) {
			final int cellIx = primIt.next();
			final int[] rowColSizes = new int[3];

			if (cellIx >= -MAX_GRID && cellIx <= MAX_GRID) { // If not dock cell
				allDCs[r] = primDCs[cellIx >= primDCs.length ? primDCs.length - 1 : cellIx];
			}
			else {
				allDCs[r] = DOCK_DIM_CONSTRAINT;
			}

			final ArrayList<LinkedDimGroup> groups = groupsLists[r];

			final int[] groupSizes = new int[] {
					getTotalGroupsSizeParallel(groups, LayoutUtil.MIN, false),
					getTotalGroupsSizeParallel(groups, LayoutUtil.PREF, false), LayoutUtil.INF};

			correctMinMax(groupSizes);
			final BoundSize dimSize = allDCs[r].getSize();

			for (int sType = LayoutUtil.MIN; sType <= LayoutUtil.MAX; sType++) {

				int rowColSize = groupSizes[sType];

				final UnitValue uv = dimSize.getSize(sType);
				if (uv != null) {
					// If the size of the column is a link to some other size, use that instead
					final int unit = uv.getUnit();
					if (unit == UnitValueToolkit.PREF_SIZE) {
						rowColSize = groupSizes[LayoutUtil.PREF];
					}
					else if (unit == UnitValueToolkit.MIN_SIZE) {
						rowColSize = groupSizes[LayoutUtil.MIN];
					}
					else if (unit == UnitValueToolkit.MAX_SIZE) {
						rowColSize = groupSizes[LayoutUtil.MAX];
					}
					else {
						rowColSize = uv.getPixels(refSize, container, null);
					}
				}
				else if (cellIx >= -MAX_GRID && cellIx <= MAX_GRID && rowColSize == 0) {
					final LayoutUtil layoutUtil = MigLayoutToolkit.getMigLayoutUtil();
					rowColSize = layoutUtil.isDesignTime(container) ? layoutUtil.getDesignTimeEmptySize() : 0; // Empty rows with no size set gets XX pixels if design time
				}

				rowColSizes[sType] = rowColSize;
			}

			correctMinMax(rowColSizes);
			addToSizeGroup(sizeGroupMap, allDCs[r].getSizeGroup(), rowColSizes);

			rowColBoundSizes[r] = rowColSizes;
		}

		// Set/equalize the size groups to same the values.
		if (sizeGroupMap.size() > 0) {
			for (int r = 0; r < rowColBoundSizes.length; r++) {
				if (allDCs[r].getSizeGroup() != null) {
					rowColBoundSizes[r] = sizeGroupMap.get(allDCs[r].getSizeGroup());
				}
			}
		}

		// Add the gaps
		final ResizeConstraint[] resConstrs = getRowResizeConstraints(allDCs);

		final boolean[] fillInPushGaps = new boolean[allDCs.length + 1];
		final int[][] gapSizes = getRowGaps(allDCs, refSize, isHor, fillInPushGaps);

		final FlowSizeSpec fss = mergeSizesGapsAndResConstrs(resConstrs, fillInPushGaps, rowColBoundSizes, gapSizes);

		// Spanning components are not handled yet. Check and adjust the multi-row min/pref they enforce.
		adjustMinPrefForSpanningComps(allDCs, defPush, fss, groupsLists);

		return fss;
	}

	private static int getParentSize(final IComponentWrapper cw, final boolean isHor) {
		final IComponentWrapper p = cw.getParent();
		return p != null ? (isHor ? cw.getWidth() : cw.getHeight()) : 0;
	}

	private int[] getMinPrefMaxSumSize(final boolean isHor) {
		final int[][] sizes = isHor ? colFlowSpecs.sizes : rowFlowSpecs.sizes;

		final int[] retSizes = new int[3];

		final BoundSize sz = isHor ? lc.getWidth() : lc.getHeight();

		for (int i = 0; i < sizes.length; i++) {
			if (sizes[i] == null) {
				continue;
			}

			final int[] size = sizes[i];
			for (int sType = LayoutUtil.MIN; sType <= LayoutUtil.MAX; sType++) {
				if (sz.getSize(sType) != null) {
					if (i == 0) {
						retSizes[sType] = sz.getSize(sType).getPixels(getParentSize(container, isHor), container, null);
					}
				}
				else {
					int s = size[sType];

					if (s != LayoutUtil.NOT_SET) {
						if (sType == LayoutUtil.PREF) {
							int bnd = size[LayoutUtil.MAX];
							if (bnd != LayoutUtil.NOT_SET && bnd < s) {
								s = bnd;
							}

							bnd = size[LayoutUtil.MIN];
							if (bnd > s) {
								s = bnd;
							}
						}

						retSizes[sType] += s; // MAX compensated below.
					}

					// So that MAX is always correct.
					if (size[LayoutUtil.MAX] == LayoutUtil.NOT_SET || retSizes[LayoutUtil.MAX] > LayoutUtil.INF) {
						retSizes[LayoutUtil.MAX] = LayoutUtil.INF;
					}
				}
			}
		}

		correctMinMax(retSizes);

		return retSizes;
	}

	private static ResizeConstraint[] getRowResizeConstraints(final DimConstraint[] specs) {
		final ResizeConstraint[] resConsts = new ResizeConstraint[specs.length];
		for (int i = 0; i < resConsts.length; i++) {
			resConsts[i] = specs[i].resize;
		}
		return resConsts;
	}

	private static ResizeConstraint[] getComponentResizeConstraints(final ArrayList<CompWrap> compWraps, final boolean isHor) {
		final ResizeConstraint[] resConsts = new ResizeConstraint[compWraps.size()];
		for (int i = 0; i < resConsts.length; i++) {
			final CC fc = compWraps.get(i).cc;
			resConsts[i] = fc.getDimConstraint(isHor).resize;

			// Always grow docking components in the correct dimension.
			final int dock = fc.getDockSide();
			if (isHor ? (dock == 0 || dock == 2) : (dock == 1 || dock == 3)) {
				final ResizeConstraint dc = resConsts[i];
				resConsts[i] = new ResizeConstraint(dc.shrinkPrio, dc.shrink, dc.growPrio, ResizeConstraint.WEIGHT_100);
			}
		}
		return resConsts;
	}

	private static boolean[] getComponentGapPush(final ArrayList<CompWrap> compWraps, final boolean isHor) {
		// Make one element bigger and or the after gap with the next before gap.
		final boolean[] barr = new boolean[compWraps.size() + 1];
		for (int i = 0; i < barr.length; i++) {

			boolean push = i > 0 && compWraps.get(i - 1).isPushGap(isHor, false);

			if (push == false && i < (barr.length - 1)) {
				push = compWraps.get(i).isPushGap(isHor, true);
			}

			barr[i] = push;
		}
		return barr;
	}

	/**
	 * Returns the row gaps in pixel sizes. One more than there are <code>specs</code> sent in.
	 * 
	 * @param specs
	 * @param refSize
	 * @param isHor
	 * @param fillInPushGaps If the gaps are pushing. <b>NOTE!</b> this argument will be filled in and thus changed!
	 * @return The row gaps in pixel sizes. One more than there are <code>specs</code> sent in.
	 */
	private int[][] getRowGaps(final DimConstraint[] specs, final int refSize, final boolean isHor, final boolean[] fillInPushGaps) {
		BoundSize defGap = isHor ? lc.getGridGapX() : lc.getGridGapY();
		if (defGap == null) {
			defGap = isHor
					? MigLayoutToolkit.getMigPlatformDefaults().getGridGapX()
					: MigLayoutToolkit.getMigPlatformDefaults().getGridGapY();
		}
		final int[] defGapArr = defGap.getPixelSizes(refSize, container, null);

		final boolean defIns = !hasDocks();

		final LayoutUtil layoutUtil = MigLayoutToolkit.getMigLayoutUtil();
		final UnitValue firstGap = layoutUtil.getInsets(lc, isHor ? 1 : 0, defIns);
		final UnitValue lastGap = layoutUtil.getInsets(lc, isHor ? 3 : 2, defIns);

		final int[][] retValues = new int[specs.length + 1][];

		int wgIx = 0;
		for (int i = 0; i < retValues.length; i++) {
			final DimConstraint specBefore = i > 0 ? specs[i - 1] : null;
			final DimConstraint specAfter = i < specs.length ? specs[i] : null;

			// No gap if between docking components.
			final boolean edgeBefore = (specBefore == DOCK_DIM_CONSTRAINT || specBefore == null);
			final boolean edgeAfter = (specAfter == DOCK_DIM_CONSTRAINT || specAfter == null);
			if (edgeBefore && edgeAfter) {
				continue;
			}

			final BoundSize wrapGapSize = (wrapGapMap == null || isHor == lc.isFlowX()
					? null : wrapGapMap.get(Integer.valueOf(wgIx++)));

			if (wrapGapSize == null) {

				final int[] gapBefore = specBefore != null ? specBefore.getRowGaps(container, null, refSize, false) : null;
				final int[] gapAfter = specAfter != null ? specAfter.getRowGaps(container, null, refSize, true) : null;

				if (edgeBefore && gapAfter == null && firstGap != null) {

					final int bef = firstGap.getPixels(refSize, container, null);
					retValues[i] = new int[] {bef, bef, bef};

				}
				else if (edgeAfter && gapBefore == null && firstGap != null) {

					final int aft = lastGap.getPixels(refSize, container, null);
					retValues[i] = new int[] {aft, aft, aft};

				}
				else {
					retValues[i] = gapAfter != gapBefore ? mergeSizes(gapAfter, gapBefore) : new int[] {
							defGapArr[0], defGapArr[1], defGapArr[2]};
				}

				if (specBefore != null && specBefore.isGapAfterPush() || specAfter != null && specAfter.isGapBeforePush()) {
					fillInPushGaps[i] = true;
				}
			}
			else {

				if (wrapGapSize.isUnset()) {
					retValues[i] = new int[] {defGapArr[0], defGapArr[1], defGapArr[2]};
				}
				else {
					retValues[i] = wrapGapSize.getPixelSizes(refSize, container, null);
				}
				fillInPushGaps[i] = wrapGapSize.getGapPush();
			}
		}
		return retValues;
	}

	private static int[][] getGaps(final ArrayList<CompWrap> compWraps, final boolean isHor) {
		final int compCount = compWraps.size();
		final int[][] retValues = new int[compCount + 1][];

		retValues[0] = compWraps.get(0).getGaps(isHor, true);
		for (int i = 0; i < compCount; i++) {
			final int[] gap1 = compWraps.get(i).getGaps(isHor, false);
			final int[] gap2 = i < compCount - 1 ? compWraps.get(i + 1).getGaps(isHor, true) : null;

			retValues[i + 1] = mergeSizes(gap1, gap2);
		}

		return retValues;
	}

	private boolean hasDocks() {
		return (dockOffX > 0 || dockOffY > 0 || rowIndexes.last() > MAX_GRID || colIndexes.last() > MAX_GRID);
	}

	/**
	 * Adjust min/pref size for columns(or rows) that has components that spans multiple columns (or rows).
	 * 
	 * @param specs The specs for the columns or rows. Last index will be used if <code>count</code> is greater than this array's
	 *            length.
	 * @param defPush The default grow weight if the specs does not have anyone that will grow. Comes from "push" in the CC.
	 * @param fss
	 * @param groupsLists
	 */
	private void adjustMinPrefForSpanningComps(
		final DimConstraint[] specs,
		final Float[] defPush,
		final FlowSizeSpec fss,
		final ArrayList<LinkedDimGroup>[] groupsLists) {
		for (int r = groupsLists.length - 1; r >= 0; r--) { // Since 3.7.3 Iterate from end to start. Will solve some multiple spanning components hard to solve problems.
			final ArrayList<LinkedDimGroup> groups = groupsLists[r];

			for (final LinkedDimGroup group : groups) {
				if (group.span == 1) {
					continue;
				}

				final int[] sizes = group.getMinPrefMax();
				for (int s = LayoutUtil.MIN; s <= LayoutUtil.PREF; s++) {
					final int cSize = sizes[s];
					if (cSize == LayoutUtil.NOT_SET) {
						continue;
					}

					int rowSize = 0;
					final int sIx = (r << 1) + 1;
					final int len = Math.min((group.span << 1), fss.sizes.length - sIx) - 1;
					for (int j = sIx; j < sIx + len; j++) {
						final int sz = fss.sizes[j][s];
						if (sz != LayoutUtil.NOT_SET) {
							rowSize += sz;
						}
					}

					if (rowSize < cSize && len > 0) {
						int newRowSize = 0;
						for (int eagerness = 0; eagerness < 4 && newRowSize < cSize; eagerness++) {
							newRowSize = fss.expandSizes(specs, defPush, cSize, sIx, len, s, eagerness);
						}
					}
				}
			}
		}
	}

	/**
	 * For one dimension divide the component wraps into logical groups. One group for component wraps that share a common
	 * something,
	 * line the property to layout by base line.
	 * 
	 * @param isRows If rows, and not columns, are to be divided.
	 * @return One <code>ArrayList<LinkedDimGroup></code> for every row/column.
	 */
	private ArrayList<LinkedDimGroup>[] divideIntoLinkedGroups(final boolean isRows) {
		final LayoutUtil layoutUtil = MigLayoutToolkit.getMigLayoutUtil();
		final boolean fromEnd = !(isRows ? lc.isTopToBottom() : layoutUtil.isLeftToRight(lc, container));
		final TreeSet<Integer> primIndexes = isRows ? rowIndexes : colIndexes;
		final TreeSet<Integer> secIndexes = isRows ? colIndexes : rowIndexes;
		final DimConstraint[] primDCs = (isRows ? rowConstr : colConstr).getConstaints();

		@SuppressWarnings("unchecked")
		final ArrayList<LinkedDimGroup>[] groupLists = new ArrayList[primIndexes.size()];

		int gIx = 0;
		for (final int i : primIndexes) {
			DimConstraint dc;
			if (i >= -MAX_GRID && i <= MAX_GRID) { // If not dock cell
				dc = primDCs[i >= primDCs.length ? primDCs.length - 1 : i];
			}
			else {
				dc = DOCK_DIM_CONSTRAINT;
			}

			final ArrayList<LinkedDimGroup> groupList = new ArrayList<LinkedDimGroup>(2);
			groupLists[gIx++] = groupList;

			for (final Integer ix : secIndexes) {
				final Cell cell = isRows ? getCell(i, ix) : getCell(ix, i);
				if (cell == null || cell.compWraps.size() == 0) {
					continue;
				}

				int span = (isRows ? cell.spany : cell.spanx);
				if (span > 1) {
					span = convertSpanToSparseGrid(i, span, primIndexes);
				}

				final boolean isPar = (cell.flowx == isRows);

				if ((isPar == false && cell.compWraps.size() > 1) || span > 1) {

					final int linkType = isPar ? LinkedDimGroup.TYPE_PARALLEL : LinkedDimGroup.TYPE_SERIAL;
					final LinkedDimGroup lg = new LinkedDimGroup("p," + ix, span, linkType, !isRows, fromEnd);
					lg.setCompWraps(cell.compWraps);
					groupList.add(lg);
				}
				else {
					for (int cwIx = 0; cwIx < cell.compWraps.size(); cwIx++) {
						final CompWrap cw = cell.compWraps.get(cwIx);
						final boolean rowBaselineAlign = (isRows && lc.isTopToBottom() && dc.getAlignOrDefault(!isRows) == MigLayoutToolkit.getMigUnitValueToolkit().BASELINE_IDENTITY); // Disable baseline for bottomToTop since I can not verify it working.
						final boolean isBaseline = isRows && cw.isBaselineAlign(rowBaselineAlign);

						final String linkCtx = isBaseline ? "baseline" : null;

						// Find a group with same link context and put it in that group.
						boolean foundList = false;
						final int lastGl = groupList.size() - 1;
						for (int glIx = 0; glIx <= lastGl; glIx++) {
							final LinkedDimGroup group = groupList.get(glIx);
							if (group.linkCtx == linkCtx || linkCtx != null && linkCtx.equals(group.linkCtx)) {
								group.addCompWrap(cw);
								foundList = true;
								break;
							}
						}

						// If none found and at last add a new group.
						if (foundList == false) {
							final int linkType = isBaseline ? LinkedDimGroup.TYPE_BASELINE : LinkedDimGroup.TYPE_PARALLEL;
							final LinkedDimGroup lg = new LinkedDimGroup(linkCtx, 1, linkType, !isRows, fromEnd);
							lg.addCompWrap(cw);
							groupList.add(lg);
						}
					}
				}
			}
		}
		return groupLists;
	}

	/**
	 * Spanning is specified in the uncompressed grid number. They can for instance be more than 60000 for the outer
	 * edge dock grid cells. When the grid is compressed and indexed after only the cells that area occupied the span
	 * is erratic. This method use the row/col indexes and corrects the span to be correct for the compressed grid.
	 * 
	 * @param span The span un the uncompressed grid. <code>LayoutUtil.INF</code> will be interpreted to span the rest
	 *            of the column/row excluding the surrounding docking components.
	 * @param indexes The indexes in the correct dimension.
	 * @return The converted span.
	 */
	private static int convertSpanToSparseGrid(final int curIx, final int span, final TreeSet<Integer> indexes) {
		final int lastIx = curIx + span;
		int retSpan = 1;

		for (final Integer ix : indexes) {
			if (ix <= curIx) {
				continue; // We have not arrived to the correct index yet
			}

			if (ix >= lastIx) {
				break;
			}

			retSpan++;
		}
		return retSpan;
	}

	private boolean isCellFree(final int r, final int c, final ArrayList<int[]> occupiedRects) {
		if (getCell(r, c) != null) {
			return false;
		}

		for (final int[] rect : occupiedRects) {
			if (rect[0] <= c && rect[1] <= r && rect[0] + rect[2] > c && rect[1] + rect[3] > r) {
				return false;
			}
		}
		return true;
	}

	private Cell getCell(final int r, final int c) {
		return grid.get(Integer.valueOf((r << 16) + c));
	}

	private void setCell(final int r, final int c, final Cell cell) {
		if (c < 0 || r < 0) {
			throw new IllegalArgumentException("Cell position cannot be negative. row: " + r + ", col: " + c);
		}

		if (c > MAX_GRID || r > MAX_GRID) {
			throw new IllegalArgumentException("Cell position out of bounds. Out of cells. row: " + r + ", col: " + c);
		}

		rowIndexes.add(r);
		colIndexes.add(c);

		grid.put((r << 16) + c, cell);
	}

	/**
	 * Adds a docking cell. That cell is outside the normal cell indexes.
	 * 
	 * @param dockInsets The current dock insets. Will be updated!
	 * @param side top == 0, left == 1, bottom = 2, right = 3.
	 * @param cw The compwrap to put in a cell and add.
	 */
	private void addDockingCell(final int[] dockInsets, final int side, final CompWrap cw) {
		int r;
		int c;
		int spanx = 1;
		int spany = 1;
		switch (side) {
			case 0:
			case 2:
				r = side == 0 ? dockInsets[0]++ : dockInsets[2]--;
				c = dockInsets[1];
				spanx = dockInsets[3] - dockInsets[1] + 1; // The +1 is for cell 0.
				colIndexes.add(dockInsets[3]); // Make sure there is a receiving cell
				break;

			case 1:
			case 3:
				c = side == 1 ? dockInsets[1]++ : dockInsets[3]--;
				r = dockInsets[0];
				spany = dockInsets[2] - dockInsets[0] + 1; // The +1 is for cell 0.
				rowIndexes.add(dockInsets[2]); // Make sure there is a receiving cell
				break;

			default:
				throw new IllegalArgumentException("Internal error 123.");
		}

		rowIndexes.add(r);
		colIndexes.add(c);

		grid.put((r << 16) + c, new Cell(cw, spanx, spany, spanx > 1));
	}

	/**
	 * A simple representation of a cell in the grid. Contains a number of component wraps and if they span more than one cell.
	 */
	private static final class Cell {
		private final int spanx;
		private final int spany;
		private final boolean flowx;
		private final ArrayList<CompWrap> compWraps = new ArrayList<CompWrap>(1);

		private boolean hasTagged = false; // If one or more components have styles and need to be checked by the component sorter

		private Cell(final CompWrap cw) {
			this(cw, 1, 1, true);
		}

		private Cell(final int spanx, final int spany, final boolean flowx) {
			this(null, spanx, spany, flowx);
		}

		private Cell(final CompWrap cw, final int spanx, final int spany, final boolean flowx) {
			if (cw != null) {
				compWraps.add(cw);
			}
			this.spanx = spanx;
			this.spany = spany;
			this.flowx = flowx;
		}
	}

	/**
	 * A number of component wraps that share a layout "something" <b>in one dimension</b>
	 */
	private static final class LinkedDimGroup {
		private static final int TYPE_SERIAL = 0;
		private static final int TYPE_PARALLEL = 1;
		private static final int TYPE_BASELINE = 2;

		private final String linkCtx;
		private final int span;
		private final int linkType;
		private final boolean isHor;
		private final boolean fromEnd;

		private ArrayList<CompWrap> ldgCompWraps = new ArrayList<CompWrap>(4);

		private int[] sizes = null;
		private int lStart = 0;
		private int lSize = 0; // Currently mostly for debug painting

		private LinkedDimGroup(
			final String linkCtx,
			final int span,
			final int linkType,
			final boolean isHor,
			final boolean fromEnd) {
			this.linkCtx = linkCtx;
			this.span = span;
			this.linkType = linkType;
			this.isHor = isHor;
			this.fromEnd = fromEnd;
		}

		private void addCompWrap(final CompWrap cw) {
			ldgCompWraps.add(cw);
			sizes = null;
		}

		private void setCompWraps(final ArrayList<CompWrap> cws) {
			if (ldgCompWraps != cws) {
				ldgCompWraps = cws;
				sizes = null;
			}
		}

		private void layout(final DimConstraint dc, final int start, final int size, final int spanCount) {
			lStart = start;
			lSize = size;

			if (ldgCompWraps.size() == 0) {
				return;
			}

			final IContainerWrapper parent = ldgCompWraps.get(0).comp.getParent();
			if (linkType == TYPE_PARALLEL) {
				layoutParallel(parent, ldgCompWraps, dc, start, size, isHor, fromEnd);
			}
			else if (linkType == TYPE_BASELINE) {
				layoutBaseline(parent, ldgCompWraps, dc, start, size, LayoutUtil.PREF, spanCount);
			}
			else {
				layoutSerial(parent, ldgCompWraps, dc, start, size, isHor, spanCount, fromEnd);
			}
		}

		/**
		 * Returns the min/pref/max sizes for this cell. Returned array <b>must not be altered</b>
		 * 
		 * @return A shared min/pref/max array of sizes. Always of length 3 and never <code>null</code>. Will always be of type
		 *         STATIC and PIXEL.
		 */
		private int[] getMinPrefMax() {
			if (sizes == null && ldgCompWraps.size() > 0) {
				size
