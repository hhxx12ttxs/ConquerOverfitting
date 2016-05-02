/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.widget;

import com.android.internal.R;
import com.android.internal.util.Predicate;
import com.google.android.collect.Lists;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.util.SparseBooleanArray;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RemoteViews.RemoteView;

import java.util.ArrayList;

/*
 * Implementation Notes:
 *
 * Some terminology:
 *
 *     index    - index of the items that are currently visible
 *     position - index of the items in the cursor
 */


/**
 * A view that shows items in a vertically scrolling list. The items
 * come from the {@link ListAdapter} associated with this view.
 *
 * <p>See the <a href="{@docRoot}guide/topics/ui/layout/listview.html">List View</a>
 * guide.</p>
 *
 * @attr ref android.R.styleable#ListView_entries
 * @attr ref android.R.styleable#ListView_divider
 * @attr ref android.R.styleable#ListView_dividerHeight
 * @attr ref android.R.styleable#ListView_headerDividersEnabled
 * @attr ref android.R.styleable#ListView_footerDividersEnabled
 */
@RemoteView
public class ListView extends AbsListView {
    /**
     * Used to indicate a no preference for a position type.
     */
    static final int NO_POSITION = -1;

    /**
     * When arrow scrolling, ListView will never scroll more than this factor
     * times the height of the list.
     */
    private static final float MAX_SCROLL_FACTOR = 0.33f;

    /**
     * When arrow scrolling, need a certain amount of pixels to preview next
     * items.  This is usually the fading edge, but if that is small enough,
     * we want to make sure we preview at least this many pixels.
     */
    private static final int MIN_SCROLL_PREVIEW_PIXELS = 2;

    /**
     * A class that represents a fixed view in a list, for example a header at the top
     * or a footer at the bottom.
     */
    public class FixedViewInfo {
        /** The view to add to the list */
        public View view;
        /** The data backing the view. This is returned from {@link ListAdapter#getItem(int)}. */
        public Object data;
        /** <code>true</code> if the fixed view should be selectable in the list */
        public boolean isSelectable;
    }

    private ArrayList<FixedViewInfo> mHeaderViewInfos = Lists.newArrayList();
    private ArrayList<FixedViewInfo> mFooterViewInfos = Lists.newArrayList();

    Drawable mDivider;
    int mDividerHeight;

    Drawable mOverScrollHeader;
    Drawable mOverScrollFooter;

    private boolean mIsCacheColorOpaque;
    private boolean mDividerIsOpaque;

    private boolean mHeaderDividersEnabled;
    private boolean mFooterDividersEnabled;

    private boolean mAreAllItemsSelectable = true;

    private boolean mItemsCanFocus = false;

    // used for temporary calculations.
    private final Rect mTempRect = new Rect();
    private Paint mDividerPaint;

    // the single allocated result per list view; kinda cheesey but avoids
    // allocating these thingies too often.
    private final ArrowScrollFocusResult mArrowScrollFocusResult = new ArrowScrollFocusResult();

    // Keeps focused children visible through resizes
    private FocusSelector mFocusSelector;

    public ListView(Context context) {
        this(context, null);
    }

    public ListView(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.listViewStyle);
    }

    public ListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.ListView, defStyle, 0);

        CharSequence[] entries = a.getTextArray(
                com.android.internal.R.styleable.ListView_entries);
        if (entries != null) {
            setAdapter(new ArrayAdapter<CharSequence>(context,
                    com.android.internal.R.layout.simple_list_item_1, entries));
        }

        final Drawable d = a.getDrawable(com.android.internal.R.styleable.ListView_divider);
        if (d != null) {
            // If a divider is specified use its intrinsic height for divider height
            setDivider(d);
        }
        
        final Drawable osHeader = a.getDrawable(
                com.android.internal.R.styleable.ListView_overScrollHeader);
        if (osHeader != null) {
            setOverscrollHeader(osHeader);
        }

        final Drawable osFooter = a.getDrawable(
                com.android.internal.R.styleable.ListView_overScrollFooter);
        if (osFooter != null) {
            setOverscrollFooter(osFooter);
        }

        // Use the height specified, zero being the default
        final int dividerHeight = a.getDimensionPixelSize(
                com.android.internal.R.styleable.ListView_dividerHeight, 0);
        if (dividerHeight != 0) {
            setDividerHeight(dividerHeight);
        }

        mHeaderDividersEnabled = a.getBoolean(R.styleable.ListView_headerDividersEnabled, true);
        mFooterDividersEnabled = a.getBoolean(R.styleable.ListView_footerDividersEnabled, true);

        a.recycle();
    }

    /**
     * @return The maximum amount a list view will scroll in response to
     *   an arrow event.
     */
    public int getMaxScrollAmount() {
        return (int) (MAX_SCROLL_FACTOR * (mBottom - mTop));
    }

    /**
     * Make sure views are touching the top or bottom edge, as appropriate for
     * our gravity
     */
    private void adjustViewsUpOrDown() {
        final int childCount = getChildCount();
        int delta;

        if (childCount > 0) {
            View child;

            if (!mStackFromBottom) {
                // Uh-oh -- we came up short. Slide all views up to make them
                // align with the top
                child = getChildAt(0);
                delta = child.getTop() - mListPadding.top;
                if (mFirstPosition != 0) {
                    // It's OK to have some space above the first item if it is
                    // part of the vertical spacing
                    delta -= mDividerHeight;
                }
                if (delta < 0) {
                    // We only are looking to see if we are too low, not too high
                    delta = 0;
                }
            } else {
                // we are too high, slide all views down to align with bottom
                child = getChildAt(childCount - 1);
                delta = child.getBottom() - (getHeight() - mListPadding.bottom);

                if (mFirstPosition + childCount < mItemCount) {
                    // It's OK to have some space below the last item if it is
                    // part of the vertical spacing
                    delta += mDividerHeight;
                }

                if (delta > 0) {
                    delta = 0;
                }
            }

            if (delta != 0) {
                offsetChildrenTopAndBottom(-delta);
            }
        }
    }

    /**
     * Add a fixed view to appear at the top of the list. If addHeaderView is
     * called more than once, the views will appear in the order they were
     * added. Views added using this call can take focus if they want.
     * <p>
     * NOTE: Call this before calling setAdapter. This is so ListView can wrap
     * the supplied cursor with one that will also account for header and footer
     * views.
     *
     * @param v The view to add.
     * @param data Data to associate with this view
     * @param isSelectable whether the item is selectable
     */
    public void addHeaderView(View v, Object data, boolean isSelectable) {

        if (mAdapter != null && ! (mAdapter instanceof HeaderViewListAdapter)) {
            throw new IllegalStateException(
                    "Cannot add header view to list -- setAdapter has already been called.");
        }

        FixedViewInfo info = new FixedViewInfo();
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
        mHeaderViewInfos.add(info);

        // in the case of re-adding a header view, or adding one later on,
        // we need to notify the observer
        if (mAdapter != null && mDataSetObserver != null) {
            mDataSetObserver.onChanged();
        }
    }

    /**
     * Add a fixed view to appear at the top of the list. If addHeaderView is
     * called more than once, the views will appear in the order they were
     * added. Views added using this call can take focus if they want.
     * <p>
     * NOTE: Call this before calling setAdapter. This is so ListView can wrap
     * the supplied cursor with one that will also account for header and footer
     * views.
     *
     * @param v The view to add.
     */
    public void addHeaderView(View v) {
        addHeaderView(v, null, true);
    }

    @Override
    public int getHeaderViewsCount() {
        return mHeaderViewInfos.size();
    }

    /**
     * Removes a previously-added header view.
     *
     * @param v The view to remove
     * @return true if the view was removed, false if the view was not a header
     *         view
     */
    public boolean removeHeaderView(View v) {
        if (mHeaderViewInfos.size() > 0) {
            boolean result = false;
            if (mAdapter != null && ((HeaderViewListAdapter) mAdapter).removeHeader(v)) {
                if (mDataSetObserver != null) {
                    mDataSetObserver.onChanged();
                }
                result = true;
            }
            removeFixedViewInfo(v, mHeaderViewInfos);
            return result;
        }
        return false;
    }

    private void removeFixedViewInfo(View v, ArrayList<FixedViewInfo> where) {
        int len = where.size();
        for (int i = 0; i < len; ++i) {
            FixedViewInfo info = where.get(i);
            if (info.view == v) {
                where.remove(i);
                break;
            }
        }
    }

    /**
     * Add a fixed view to appear at the bottom of the list. If addFooterView is
     * called more than once, the views will appear in the order they were
     * added. Views added using this call can take focus if they want.
     * <p>
     * NOTE: Call this before calling setAdapter. This is so ListView can wrap
     * the supplied cursor with one that will also account for header and footer
     * views.
     *
     * @param v The view to add.
     * @param data Data to associate with this view
     * @param isSelectable true if the footer view can be selected
     */
    public void addFooterView(View v, Object data, boolean isSelectable) {

        // NOTE: do not enforce the adapter being null here, since unlike in
        // addHeaderView, it was never enforced here, and so existing apps are
        // relying on being able to add a footer and then calling setAdapter to
        // force creation of the HeaderViewListAdapter wrapper

        FixedViewInfo info = new FixedViewInfo();
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
        mFooterViewInfos.add(info);

        // in the case of re-adding a footer view, or adding one later on,
        // we need to notify the observer
        if (mAdapter != null && mDataSetObserver != null) {
            mDataSetObserver.onChanged();
        }
    }

    /**
     * Add a fixed view to appear at the bottom of the list. If addFooterView is called more
     * than once, the views will appear in the order they were added. Views added using
     * this call can take focus if they want.
     * <p>NOTE: Call this before calling setAdapter. This is so ListView can wrap the supplied
     * cursor with one that will also account for header and footer views.
     *
     *
     * @param v The view to add.
     */
    public void addFooterView(View v) {
        addFooterView(v, null, true);
    }

    @Override
    public int getFooterViewsCount() {
        return mFooterViewInfos.size();
    }

    /**
     * Removes a previously-added footer view.
     *
     * @param v The view to remove
     * @return
     * true if the view was removed, false if the view was not a footer view
     */
    public boolean removeFooterView(View v) {
        if (mFooterViewInfos.size() > 0) {
            boolean result = false;
            if (mAdapter != null && ((HeaderViewListAdapter) mAdapter).removeFooter(v)) {
                if (mDataSetObserver != null) {
                    mDataSetObserver.onChanged();
                }
                result = true;
            }
            removeFixedViewInfo(v, mFooterViewInfos);
            return result;
        }
        return false;
    }

    /**
     * Returns the adapter currently in use in this ListView. The returned adapter
     * might not be the same adapter passed to {@link #setAdapter(ListAdapter)} but
     * might be a {@link WrapperListAdapter}.
     *
     * @return The adapter currently used to display data in this ListView.
     *
     * @see #setAdapter(ListAdapter)
     */
    @Override
    public ListAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Sets up this AbsListView to use a remote views adapter which connects to a RemoteViewsService
     * through the specified intent.
     * @param intent the intent used to identify the RemoteViewsService for the adapter to connect to.
     */
    @android.view.RemotableViewMethod
    public void setRemoteViewsAdapter(Intent intent) {
        super.setRemoteViewsAdapter(intent);
    }

    /**
     * Sets the data behind this ListView.
     *
     * The adapter passed to this method may be wrapped by a {@link WrapperListAdapter},
     * depending on the ListView features currently in use. For instance, adding
     * headers and/or footers will cause the adapter to be wrapped.
     *
     * @param adapter The ListAdapter which is responsible for maintaining the
     *        data backing this list and for producing a view to represent an
     *        item in that data set.
     *
     * @see #getAdapter() 
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        resetList();
        mRecycler.clear();

        if (mHeaderViewInfos.size() > 0|| mFooterViewInfos.size() > 0) {
            mAdapter = new HeaderViewListAdapter(mHeaderViewInfos, mFooterViewInfos, adapter);
        } else {
            mAdapter = adapter;
        }

        mOldSelectedPosition = INVALID_POSITION;
        mOldSelectedRowId = INVALID_ROW_ID;

        // AbsListView#setAdapter will update choice mode states.
        super.setAdapter(adapter);

        if (mAdapter != null) {
            mAreAllItemsSelectable = mAdapter.areAllItemsEnabled();
            mOldItemCount = mItemCount;
            mItemCount = mAdapter.getCount();
            checkFocus();

            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);

            mRecycler.setViewTypeCount(mAdapter.getViewTypeCount());

            int position;
            if (mStackFromBottom) {
                position = lookForSelectablePosition(mItemCount - 1, false);
            } else {
                position = lookForSelectablePosition(0, true);
            }
            setSelectedPositionInt(position);
            setNextSelectedPositionInt(position);

            if (mItemCount == 0) {
                // Nothing selected
                checkSelectionChanged();
            }
        } else {
            mAreAllItemsSelectable = true;
            checkFocus();
            // Nothing selected
            checkSelectionChanged();
        }

        requestLayout();
    }


    /**
     * The list is empty. Clear everything out.
     */
    @Override
    void resetList() {
        // The parent's resetList() will remove all views from the layout so we need to
        // cleanup the state of our footers and headers
        clearRecycledState(mHeaderViewInfos);
        clearRecycledState(mFooterViewInfos);

        super.resetList();

        mLayoutMode = LAYOUT_NORMAL;
    }

    private void clearRecycledState(ArrayList<FixedViewInfo> infos) {
        if (infos != null) {
            final int count = infos.size();

            for (int i = 0; i < count; i++) {
                final View child = infos.get(i).view;
                final LayoutParams p = (LayoutParams) child.getLayoutParams();
                if (p != null) {
                    p.recycledHeaderFooter = false;
                }
            }
        }
    }

    /**
     * @return Whether the list needs to show the top fading edge
     */
    private boolean showingTopFadingEdge() {
        final int listTop = mScrollY + mListPadding.top;
        return (mFirstPosition > 0) || (getChildAt(0).getTop() > listTop);
    }

    /**
     * @return Whether the list needs to show the bottom fading edge
     */
    private boolean showingBottomFadingEdge() {
        final int childCount = getChildCount();
        final int bottomOfBottomChild = getChildAt(childCount - 1).getBottom();
        final int lastVisiblePosition = mFirstPosition + childCount - 1;

        final int listBottom = mScrollY + getHeight() - mListPadding.bottom;

        return (lastVisiblePosition < mItemCount - 1)
                         || (bottomOfBottomChild < listBottom);
    }


    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {

        int rectTopWithinChild = rect.top;

        // offset so rect is in coordinates of the this view
        rect.offset(child.getLeft(), child.getTop());
        rect.offset(-child.getScrollX(), -child.getScrollY());

        final int height = getHeight();
        int listUnfadedTop = getScrollY();
        int listUnfadedBottom = listUnfadedTop + height;
        final int fadingEdge = getVerticalFadingEdgeLength();

        if (showingTopFadingEdge()) {
            // leave room for top fading edge as long as rect isn't at very top
            if ((mSelectedPosition > 0) || (rectTopWithinChild > fadingEdge)) {
                listUnfadedTop += fadingEdge;
            }
        }

        int childCount = getChildCount();
        int bottomOfBottomChild = getChildAt(childCount - 1).getBottom();

        if (showingBottomFadingEdge()) {
            // leave room for bottom fading edge as long as rect isn't at very bottom
            if ((mSelectedPosition < mItemCount - 1)
                    || (rect.bottom < (bottomOfBottomChild - fadingEdge))) {
                listUnfadedBottom -= fadingEdge;
            }
        }

        int scrollYDelta = 0;

        if (rect.bottom > listUnfadedBottom && rect.top > listUnfadedTop) {
            // need to MOVE DOWN to get it in view: move down just enough so
            // that the entire rectangle is in view (or at least the first
            // screen size chunk).

            if (rect.height() > height) {
                // just enough to get screen size chunk on
                scrollYDelta += (rect.top - listUnfadedTop);
            } else {
                // get entire rect at bottom of screen
                scrollYDelta += (rect.bottom - listUnfadedBottom);
            }

            // make sure we aren't scrolling beyond the end of our children
            int distanceToBottom = bottomOfBottomChild - listUnfadedBottom;
            scrollYDelta = Math.min(scrollYDelta, distanceToBottom);
        } else if (rect.top < listUnfadedTop && rect.bottom < listUnfadedBottom) {
            // need to MOVE UP to get it in view: move up just enough so that
            // entire rectangle is in view (or at least the first screen
            // size chunk of it).

            if (rect.height() > height) {
                // screen size chunk
                scrollYDelta -= (listUnfadedBottom - rect.bottom);
            } else {
                // entire rect at top
                scrollYDelta -= (listUnfadedTop - rect.top);
            }

            // make sure we aren't scrolling any further than the top our children
            int top = getChildAt(0).getTop();
            int deltaToTop = top - listUnfadedTop;
            scrollYDelta = Math.max(scrollYDelta, deltaToTop);
        }

        final boolean scroll = scrollYDelta != 0;
        if (scroll) {
            scrollListItemsBy(-scrollYDelta);
            positionSelector(INVALID_POSITION, child);
            mSelectedTop = child.getTop();
            invalidate();
        }
        return scroll;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void fillGap(boolean down) {
        final int count = getChildCount();
        if (down) {
            int paddingTop = 0;
            if ((mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK) {
                paddingTop = getListPaddingTop();
            }
            final int startOffset = count > 0 ? getChildAt(count - 1).getBottom() + mDividerHeight :
                    paddingTop;
            fillDown(mFirstPosition + count, startOffset);
            correctTooHigh(getChildCount());
        } else {
            int paddingBottom = 0;
            if ((mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK) {
                paddingBottom = getListPaddingBottom();
            }
            final int startOffset = count > 0 ? getChildAt(0).getTop() - mDividerHeight :
                    getHeight() - paddingBottom;
            fillUp(mFirstPosition - 1, startOffset);
            correctTooLow(getChildCount());
        }
    }

    /**
     * Fills the list from pos down to the end of the list view.
     *
     * @param pos The first position to put in the list
     *
     * @param nextTop The location where the top of the item associated with pos
     *        should be drawn
     *
     * @return The view that is currently selected, if it happens to be in the
     *         range that we draw.
     */
    private View fillDown(int pos, int nextTop) {
        View selectedView = null;

        int end = (mBottom - mTop);
        if ((mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK) {
            end -= mListPadding.bottom;
        }

        while (nextTop < end && pos < mItemCount) {
            // is this the selected item?
            boolean selected = pos == mSelectedPosition;
            View child = makeAndAddView(pos, nextTop, true, mListPadding.left, selected);

            nextTop = child.getBottom() + mDividerHeight;
            if (selected) {
                selectedView = child;
            }
            pos++;
        }

        setVisibleRangeHint(mFirstPosition, mFirstPosition + getChildCount() - 1);
        return selectedView;
    }

    /**
     * Fills the list from pos up to the top of the list view.
     *
     * @param pos The first position to put in the list
     *
     * @param nextBottom The location where the bottom of the item associated
     *        with pos should be drawn
     *
     * @return The view that is currently selected
     */
    private View fillUp(int pos, int nextBottom) {
        View selectedView = null;

        int end = 0;
        if ((mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK) {
            end = mListPadding.top;
        }

        while (nextBottom > end && pos >= 0) {
            // is this the selected item?
            boolean selected = pos == mSelectedPosition;
            View child = makeAndAddView(pos, nextBottom, false, mListPadding.left, selected);
            nextBottom = child.getTop() - mDividerHeight;
            if (selected) {
                selectedView = child;
            }
            pos--;
        }

        mFirstPosition = pos + 1;
        setVisibleRangeHint(mFirstPosition, mFirstPosition + getChildCount() - 1);
        return selectedView;
    }

    /**
     * Fills the list from top to bottom, starting with mFirstPosition
     *
     * @param nextTop The location where the top of the first item should be
     *        drawn
     *
     * @return The view that is currently selected
     */
    private View fillFromTop(int nextTop) {
        mFirstPosition = Math.min(mFirstPosition, mSelectedPosition);
        mFirstPosition = Math.min(mFirstPosition, mItemCount - 1);
        if (mFirstPosition < 0) {
            mFirstPosition = 0;
        }
        return fillDown(mFirstPosition, nextTop);
    }


    /**
     * Put mSelectedPosition in the middle of the screen and then build up and
     * down from there. This method forces mSelectedPosition to the center.
     *
     * @param childrenTop Top of the area in which children can be drawn, as
     *        measured in pixels
     * @param childrenBottom Bottom of the area in which children can be drawn,
     *        as measured in pixels
     * @return Currently selected view
     */
    private View fillFromMiddle(int childrenTop, int childrenBottom) {
        int height = childrenBottom - childrenTop;

        int position = reconcileSelectedPosition();

        View sel = makeAndAddView(position, childrenTop, true,
                mListPadding.left, true);
        mFirstPosition = position;

        int selHeight = sel.getMeasuredHeight();
        if (selHeight <= height) {
            sel.offsetTopAndBottom((height - selHeight) / 2);
        }

        fillAboveAndBelow(sel, position);

        if (!mStackFromBottom) {
            correctTooHigh(getChildCount());
        } else {
            correctTooLow(getChildCount());
        }

        return sel;
    }

    /**
     * Once the selected view as been placed, fill up the visible area above and
     * below it.
     *
     * @param sel The selected view
     * @param position The position corresponding to sel
     */
    private void fillAboveAndBelow(View sel, int position) {
        final int dividerHeight = mDividerHeight;
        if (!mStackFromBottom) {
            fillUp(position - 1, sel.getTop() - dividerHeight);
            adjustViewsUpOrDown();
            fillDown(position + 1, sel.getBottom() + dividerHeight);
        } else {
            fillDown(position + 1, sel.getBottom() + dividerHeight);
            adjustViewsUpOrDown();
            fillUp(position - 1, sel.getTop() - dividerHeight);
        }
    }


    /**
     * Fills the grid based on positioning the new selection at a specific
     * location. The selection may be moved so that it does not intersect the
     * faded edges. The grid is then filled upwards and downwards from there.
     *
     * @param selectedTop Where the selected item should be
     * @param childrenTop Where to start drawing children
     * @param childrenBottom Last pixel where children can be drawn
     * @return The view that currently has selection
     */
    private View fillFromSelection(int selectedTop, int childrenTop, int childrenBottom) {
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        final int selectedPosition = mSelectedPosition;

        View sel;

        final int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength,
                selectedPosition);
        final int bottomSelectionPixel = getBottomSelectionPixel(childrenBottom, fadingEdgeLength,
                selectedPosition);

        sel = makeAndAddView(selectedPosition, selectedTop, true, mListPadding.left, true);


        // Some of the newly selected item extends below the bottom of the list
        if (sel.getBottom() > bottomSelectionPixel) {
            // Find space available above the selection into which we can scroll
            // upwards
            final int spaceAbove = sel.getTop() - topSelectionPixel;

            // Find space required to bring the bottom of the selected item
            // fully into view
            final int spaceBelow = sel.getBottom() - bottomSelectionPixel;
            final int offset = Math.min(spaceAbove, spaceBelow);

            // Now offset the selected item to get it into view
            sel.offsetTopAndBottom(-offset);
        } else if (sel.getTop() < topSelectionPixel) {
            // Find space required to bring the top of the selected item fully
            // into view
            final int spaceAbove = topSelectionPixel - sel.getTop();

            // Find space available below the selection into which we can scroll
            // downwards
            final int spaceBelow = bottomSelectionPixel - sel.getBottom();
            final int offset = Math.min(spaceAbove, spaceBelow);

            // Offset the selected item to get it into view
            sel.offsetTopAndBottom(offset);
        }

        // Fill in views above and below
        fillAboveAndBelow(sel, selectedPosition);

        if (!mStackFromBottom) {
            correctTooHigh(getChildCount());
        } else {
            correctTooLow(getChildCount());
        }

        return sel;
    }

    /**
     * Calculate the bottom-most pixel we can draw the selection into
     *
     * @param childrenBottom Bottom pixel were children can be drawn
     * @param fadingEdgeLength Length of the fading edge in pixels, if present
     * @param selectedPosition The position that will be selected
     * @return The bottom-most pixel we can draw the selection into
     */
    private int getBottomSelectionPixel(int childrenBottom, int fadingEdgeLength,
            int selectedPosition) {
        int bottomSelectionPixel = childrenBottom;
        if (selectedPosition != mItemCount - 1) {
            bottomSelectionPixel -= fadingEdgeLength;
        }
        return bottomSelectionPixel;
    }

    /**
     * Calculate the top-most pixel we can draw the selection into
     *
     * @param childrenTop Top pixel were children can be drawn
     * @param fadingEdgeLength Length of the fading edge in pixels, if present
     * @param selectedPosition The position that will be selected
     * @return The top-most pixel we can draw the selection into
     */
    private int getTopSelectionPixel(int childrenTop, int fadingEdgeLength, int selectedPosition) {
        // first pixel we can draw the selection into
        int topSelectionPixel = childrenTop;
        if (selectedPosition > 0) {
            topSelectionPixel += fadingEdgeLength;
        }
        return topSelectionPixel;
    }

    /**
     * Smoothly scroll to the specified adapter position. The view will
     * scroll such that the indicated position is displayed.
     * @param position Scroll to this adapter position.
     */
    @android.view.RemotableViewMethod
    public void smoothScrollToPosition(int position) {
        super.smoothScrollToPosition(position);
    }

    /**
     * Smoothly scroll to the specified adapter position offset. The view will
     * scroll such that the indicated position is displayed.
     * @param offset The amount to offset from the adapter position to scroll to.
     */
    @android.view.RemotableViewMethod
    public void smoothScrollByOffset(int offset) {
        super.smoothScrollByOffset(offset);
    }

    /**
     * Fills the list based on positioning the new selection relative to the old
     * selection. The new selection will be placed at, above, or below the
     * location of the new selection depending on how the selection is moving.
     * The selection will then be pinned to the visible part of the screen,
     * excluding the edges that are faded. The list is then filled upwards and
     * downwards from there.
     *
     * @param oldSel The old selected view. Useful for trying to put the new
     *        selection in the same place
     * @param newSel The view that is to become selected. Useful for trying to
     *        put the new selection in the same place
     * @param delta Which way we are moving
     * @param childrenTop Where to start drawing children
     * @param childrenBottom Last pixel where children can be drawn
     * @return The view that currently has selection
     */
    private View moveSelection(View oldSel, View newSel, int delta, int childrenTop,
            int childrenBottom) {
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        final int selectedPosition = mSelectedPosition;

        View sel;

        final int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength,
                selectedPosition);
        final int bottomSelectionPixel = getBottomSelectionPixel(childrenTop, fadingEdgeLength,
                selectedPosition);

        if (delta > 0) {
            /*
             * Case 1: Scrolling down.
             */

            /*
             *     Before           After
             *    |       |        |       |
             *    +-------+        +-------+
             *    |   A   |        |   A   |
             *    |   1   |   =>   +-------+
             *    +-------+        |   B   |
             *    |   B   |        |   2   |
             *    +-------+        +-------+
             *    |       |        |       |
             *
             *    Try to keep the top of the previously selected item where it was.
             *    oldSel = A
             *    sel = B
             */

            // Put oldSel (A) where it belongs
            oldSel = makeAndAddView(selectedPosition - 1, oldSel.getTop(), true,
                    mListPadding.left, false);

            final int dividerHeight = mDividerHeight;

            // Now put the new selection (B) below that
            sel = makeAndAddView(selectedPosition, oldSel.getBottom() + dividerHeight, true,
                    mListPadding.left, true);

            // Some of the newly selected item extends below the bottom of the list
            if (sel.getBottom() > bottomSelectionPixel) {

                // Find space available above the selection into which we can scroll upwards
                int spaceAbove = sel.getTop() - topSelectionPixel;

                // Find space required to bring the bottom of the selected item fully into view
                int spaceBelow = sel.getBottom() - bottomSelectionPixel;

                // Don't scroll more than half the height of the list
                int halfVerticalSpace = (childrenBottom - childrenTop) / 2;
                int offset = Math.min(spaceAbove, spaceBelow);
                offset = Math.min(offset, halfVerticalSpace);

                // We placed oldSel, so offset that item
                oldSel.offsetTopAndBottom(-offset);
                // Now offset the selected item to get it into view
                sel.offsetTopAndBottom(-offset);
            }

            // Fill in views above and below
            if (!mStackFromBottom) {
                fillUp(mSelectedPosition - 2, sel.getTop() - dividerHeight);
                adjustViewsUpOrDown();
                fillDown(mSelectedPosition + 1, sel.getBottom() + dividerHeight);
            } else {
                fillDown(mSelectedPosition + 1, sel.getBottom() + dividerHeight);
                adjustViewsUpOrDown();
                fillUp(mSelectedPosition - 2, sel.getTop() - dividerHeight);
            }
        } else if (delta < 0) {
            /*
             * Case 2: Scrolling up.
             */

            /*
             *     Before           After
             *    |       |        |       |
             *    +-------+        +-------+
             *    |   A   |        |   A   |
             *    +-------+   =>   |   1   |
             *    |   B   |        +-------+
             *    |   2   |        |   B   |
             *    +-------+        +-------+
             *    |       |        |       |
             *
             *    Try to keep the top of the item about to become selected where it was.
             *    newSel = A
             *    olSel = B
             */

            if (newSel != null) {
                // Try to position the top of newSel (A) where it was before it was selected
                sel = makeAndAddView(selectedPosition, newSel.getTop(), true, mListPadding.left,
                        true);
            } else {
                // If (A) was not on screen and so did not have a view, position
                // it above the oldSel (B)
                sel = makeAndAddView(selectedPosition, oldSel.getTop(), false, mListPadding.left,
                        true);
            }

            // Some of the newly selected item extends above the top of the list
            if (sel.getTop() < topSelectionPixel) {
                // Find space required to bring the top of the selected item fully into view
                int spaceAbove = topSelectionPixel - sel.getTop();

               // Find space available below the selection into which we can scroll downwards
                int spaceBelow = bottomSelectionPixel - sel.getBottom();

                // Don't scroll more than half the height of the list
                int halfVerticalSpace = (childrenBottom - childrenTop) / 2;
                int offset = Math.min(spaceAbove, spaceBelow);
                offset = Math.min(offset, halfVerticalSpace);

                // Offset the selected item to get it into view
                sel.offsetTopAndBottom(offset);
            }

            // Fill in views above and below
            fillAboveAndBelow(sel, selectedPosition);
        } else {

            int oldTop = oldSel.getTop();

            /*
             * Case 3: Staying still
             */
            sel = makeAndAddView(selectedPosition, oldTop, true, mListPadding.left, true);

            // We're staying still...
            if (oldTop < childrenTop) {
                // ... but the top of the old selection was off screen.
                // (This can happen if the data changes size out from under us)
                int newBottom = sel.getBottom();
                if (newBottom < childrenTop + 20) {
                    // Not enough visible -- bring it onscreen
                    sel.offsetTopAndBottom(childrenTop - sel.getTop());
                }
            }

            // Fill in views above and below
            fillAboveAndBelow(sel, selectedPosition);
        }

        return sel;
    }

    private class FocusSelector implements Runnable {
        private int mPosition;
        private int mPositionTop;
        
        public FocusSelector setup(int position, int top) {
            mPosition = position;
            mPositionTop = top;
            return this;
        }
        
        public void run() {
            setSelectionFromTop(mPosition, mPositionTop);
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (getChildCount() > 0) {
            View focusedChild = getFocusedChild();
            if (focusedChild != null) {
                final int childPosition = mFirstPosition + indexOfChild(focusedChild);
                final int childBottom = focusedChild.getBottom();
                final int offset = Math.max(0, childBottom - (h - mPaddingTop));
                final int top = focusedChild.getTop() - offset;
                if (mFocusSelector == null) {
                    mFocusSelector = new FocusSelector();
                }
                post(mFocusSelector.setup(childPosition, top));
            }
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Sets up mListPadding
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childWidth = 0;
        int childHeight = 0;
        int childState = 0;

        mItemCount = mAdapter == null ? 0 : mAdapter.getCount();
        if (mItemCount > 0 && (widthMode == MeasureSpec.UNSPECIFIED ||
                heightMode == MeasureSpec.UNSPECIFIED)) {
            final View child = obtainView(0, mIsScrap);

            measureScrapChild(child, 0, widthMeasureSpec);

            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            childState = combineMeasuredStates(childState, child.getMeasuredState());

            if (recycleOnMeasure() && mRecycler.shouldRecycleViewType(
                    ((LayoutParams) child.getLayoutParams()).viewType)) {
                mRecycler.addScrapView(child, -1);
            }
        }

        if (widthMode == MeasureSpec.UNSPECIFIED) {
            widthSize = mListPadding.left + mListPadding.right + childWidth +
                    getVerticalScrollbarWidth();
        } else {
            widthSize |= (childState&MEASURED_STATE_MASK);
        }

        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = mListPadding.top + mListPadding.bottom + childHeight +
                    getVerticalFadingEdgeLength() * 2;
        }

        if (heightMode == MeasureSpec.AT_MOST) {
            // TODO: after first layout we should maybe start at the first visible position, not 0
            heightSize = measureHeightOfChildren(widthMeasureSpec, 0, NO_POSITION, heightSize, -1);
        }

        setMeasuredDimension(widthSize , heightSize);
        mWidthMeasureSpec = widthMeasureSpec;        
    }

    private void measureScrapChild(View child, int position, int widthMeasureSpec) {
        LayoutParams p = (LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (AbsListView.LayoutParams) generateDefaultLayoutParams();
            child.setLayoutParams(p);
        }
        p.viewType = mAdapter.getItemViewType(position);
        p.forceAdd = true;

        int childWidthSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec,
                mListPadding.left + mListPadding.right, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * @return True to recycle the views used to measure this ListView in
     *         UNSPECIFIED/AT_MOST modes, false otherwise.
     * @hide
     */
    @ViewDebug.ExportedProperty(category = "list")
    protected boolean recycleOnMeasure() {
        return true;
    }

    /**
     * Measures the height of the given range of children (inclusive) and
     * returns the height with this ListView's padding and divider heights
     * included. If maxHeight is provided, the measuring will stop when the
     * current height reaches maxHeight.
     *
     * @param widthMeasureSpec The width measure spec to be given to a child's
     *            {@link View#measure(int, int)}.
     * @param startPosition The position of the first child to be shown.
     * @param endPosition The (inclusive) position of the last child to be
     *            shown. Specify {@link #NO_POSITION} if the last child should be
     *            the last available child from the adapter.
     * @param maxHeight The maximum height that will be returned (if all the
     *            children don't fit in this value, this value will be
     *            returned).
     * @param disallowPartialChildPosition In general, whether the returned
     *            height should only contain entire children. This is more
     *            powerful--it is the first inclusive position at which partial
     *            children will not be allowed. Example: it looks nice to have
     *            at least 3 completely visible children, and in portrait this
     *            will most likely fit; but in landscape there could be times
     *            when even 2 children can not be completely shown, so a value
     *            of 2 (remember, inclusive) would be good (assuming
     *            startPosition is 0).
     * @return The height of this ListView with the given children.
     */
    final int measureHeightOfChildren(int widthMeasureSpec, int startPosition, int endPosition,
            final int maxHeight, int disallowPartialChildPosition) {

        final ListAdapter adapter = mAdapter;
        if (adapter == null) {
            return mListPadding.top + mListPadding.bottom;
        }

        // Include the padding of the list
        int returnedHeight = mListPadding.top + mListPadding.bottom;
        final int dividerHeight = ((mDividerHeight > 0) && mDivider != null) ? mDividerHeight : 0;
        // The previous height value that was less than maxHeight and contained
        // no partial children
        int prevHeightWithoutPartialChild = 0;
        int i;
        View child;

        // mItemCount - 1 since endPosition parameter is inclusive
        endPosition = (endPosition == NO_POSITION) ? adapter.getCount() - 1 : endPosition;
        final AbsListView.RecycleBin recycleBin = mRecycler;
        final boolean recyle = recycleOnMeasure();
        final boolean[] isScrap = mIsScrap;

        for (i = startPosition; i <= endPosition; ++i) {
            child = obtainView(i, isScrap);

            measureScrapChild(child, i, widthMeasureSpec);

            if (i > 0) {
                // Count the divider for all but one child
                returnedHeight += dividerHeight;
            }

            // Recycle the view before we possibly return from the method
            if (recyle && recycleBin.shouldRecycleViewType(
                    ((LayoutParams) child.getLayoutParams()).viewType)) {
                recycleBin.addScrapView(child, -1);
            }

            returnedHeight += child.getMeasuredHeight();

            if (returnedHeight >= maxHeight) {
                // We went over, figure out which height to return.  If returnedHeight > maxHeight,
                // then the i'th position did not fit completely.
                return (disallowPartialChildPosition >= 0) // Disallowing is enabled (> -1)
                            && (i > disallowPartialChildPosition) // We've past the min pos
                            && (prevHeightWithoutPartialChild > 0) // We have a prev height
                            && (returnedHeight != maxHeight) // i'th child did not fit completely
                        ? prevHeightWithoutPartialChild
                        : maxHeight;
            }

            if ((disallowPartialChildPosition >= 0) && (i >= disallowPartialChildPosition)) {
                prevHeightWithoutPartialChild = returnedHeight;
            }
        }

        // At this point, we went through the range of children, and they each
        // completely fit, so return the returnedHeight
        return returnedHeight;
    }

    @Override
    int findMotionRow(int y) {
        int childCount = getChildCount();
        if (childCount > 0) {
            if (!mStackFromBottom) {
                for (int i = 0; i < childCount; i++) {
                    View v = getChildAt(i);
                    if (y <= v.getBottom()) {
                        return mFirstPosition + i;
                    }
                }
            } else {
                for (int i = childCount - 1; i >= 0; i--) {
                    View v = getChildAt(i);
                    if (y >= v.getTop()) {
                        return mFirstPosition + i;
                    }
                }
            }
        }
        return INVALID_POSITION;
    }

    /**
     * Put a specific item at a specific location on the screen and then build
     * up and down from there.
     *
     * @param position The reference view to use as the starting point
     * @param top Pixel offset from the top of this view to the top of the
     *        reference view.
     *
     * @return The selected view, or null if the selected view is outside the
     *         visible area.
     */
    private View fillSpecific(int position, int top) {
        boolean tempIsSelected = position == mSelectedPosition;
        View temp = makeAndAddView(position, top, true, mListPadding.left, tempIsSelected);
        // Possibly changed again in fillUp if we add rows above this one.
        mFirstPosition = position;

        View above;
        View below;

        final int dividerHeight = mDividerHeight;
        if (!mStackFromBottom) {
            above = fillUp(position - 1, temp.getTop() - dividerHeight);
            // This will correct for the top of the first view not touching the top of the list
            adjustViewsUpOrDown();
            below = fillDown(position + 1, temp.getBottom() + dividerHeight);
            int childCount = getChildCount();
            if (childCount > 0) {
                correctTooHigh(childCount);
            }
        } else {
            below = fillDown(position + 1, temp.getBottom() + dividerHeight);
            // This will correct for the bottom of the last view not touching the bottom of the list
            adjustViewsUpOrDown();
            above = fillUp(position - 1, temp.getTop() - dividerHeight);
            int childCount = getChildCount();
            if (childCount > 0) {
                 correctTooLow(childCount);
            }
        }

        if (tempIsSelected) {
            return temp;
        } else if (above != null) {
            return above;
        } else {
            return below;
        }
    }

    /**
     * Check if we have dragged the bottom of the list too high (we have pushed the
     * top element off the top of the screen when we did not need to). Correct by sliding
     * everything back down.
     *
     * @param childCount Number of children
     */
    private void correctTooHigh(int childCount) {
        // First see if the last item is visible. If it is not, it is OK for the
        // top of the list to be pushed up.
        int lastPosition = mFirstPosition + childCount - 1;
        if (lastPosition == mItemCount - 1 && childCount > 0) {

            // Get the last child ...
            final View lastChild = getChildAt(childCount - 1);

            // ... and its bottom edge
            final int lastBottom = lastChild.getBottom();

            // This is bottom of our drawable area
            final int end = (mBottom - mTop) - mListPadding.bottom;

            // This is how far the bottom edge of the last view is from the bottom of the
            // drawable area
            int bottomOffset = end - lastBottom;
            View firstChild = getChildAt(0);
            final int firstTop = firstChild.getTop();

            // Make sure we are 1) Too high, and 2) Either there are more rows above the
            // first row or the first row is scrolled off the top of the drawable area
            if (bottomOffset > 0 && (mFirstPosition > 0 || firstTop < mListPadding.top))  {
                if (mFirstPosition == 0) {
                    // Don't pull the top too far down
                    bottomOffset = Math.min(bottomOffset, mListPadding.top - firstTop);
                }
                // Move everything down
                offsetChildrenTopAndBottom(bottomOffset);
                if (mFirstPosition > 0) {
                    // Fill the gap that was opened above mFirstPosition with more rows, if
                    // possible
                    fillUp(mFirstPosition - 1, firstChild.getTop() - mDividerHeight);
                    // Close up the remaining gap
                    adjustViewsUpOrDown();
                }

            }
        }
    }

    /**
     * Check if we have dragged the bottom of the list too low (we have pushed the
     * bottom element off the bottom of the screen when we did not need to). Correct by sliding
     * everything back up.
     *
     * @param childCount Number of children
     */
    private void correctTooLow(int childCount) {
        // First see if the first item is visible. If it is not, it is OK for the
        // bottom of the list to be pushed down.
        if (mFirstPosition == 0 && childCount > 0) {

            // Get the first child ...
            final View firstChild = getChildAt(0);

            // ... and its top edge
            final int firstTop = firstChild.getTop();

            // This is top of our drawable area
            final int start = mListPadding.top;

            // This is bottom of our drawable area
            final int end = (mBottom - mTop) - mListPadding.bottom;

            // This is how far the top edge of the first view is from the top of the
            // drawable area
            int topOffset = firstTop - start;
            View lastChild = getChildAt(childCount - 1);
            final int lastBottom = lastChild.getBottom();
            int lastPosition = mFirstPosition + childCount - 1;

            // Make sure we are 1) Too low, and 2) Either there are more rows below the
            // last row or the last row is scrolled off the bottom of the drawable area
            if (topOffset > 0) {
                if (lastPosition < mItemCount - 1 || lastBottom > end)  {
                    if (lastPosition == mItemCount - 1) {
                        // Don't pull the bottom too far up
                        topOffset = Math.min(topOffset, lastBottom - end);
                    }
                    // Move everything up
                    offsetChildrenTopAndBottom(-topOffset);
                    if (lastPosition < mItemCount - 1) {
                        // Fill the gap that was opened below the last position with more rows, if
                        // possible
                        fillDown(lastPosition + 1, lastChild.getBottom() + mDividerHeight);
                        // Close up the remaining gap
                        adjustViewsUpOrDown();
                    }
                } else if (lastPosition == mItemCount - 1) {
                    adjustViewsUpOrDown();                    
                }
            }
        }
    }

    @Override
    protected void layoutChildren() {
        final boolean blockLayoutRequests = mBlockLayoutRequests;
        if (!blockLayoutRequests) {
            mBlockLayoutRequests = true;
        } else {
            return;
        }

        try {
            super.layoutChildren();

            invalidate();

            if (mAdapter == null) {
                resetList();
                invokeOnItemScrollListener();
                return;
            }

            int childrenTop = mListPadding.top;
            int childrenBottom = mBottom - mTop - mListPadding.bottom;

            int childCount = getChildCount();
            int index = 0;
            int delta = 0;

            View sel;
            View oldSel = null;
            View oldFirst = null;
            View newSel = null;

            View focusLayoutRestoreView = null;

            AccessibilityNodeInfo accessibilityFocusLayoutRestoreNode = null;
            View accessibilityFocusLayoutRestoreView = null;
            int accessibilityFocusPosition = INVALID_POSITION;

            // Remember stuff we will need down below
            switch (mLayoutMode) {
            case LAYOUT_SET_SELECTION:
                index = mNextSelectedPosition - mFirstPosition;
                if (index >= 0 && index < childCount) {
                    newSel = getChildAt(index);
                }
                break;
            case LAYOUT_FORCE_TOP:
            case LAYOUT_FORCE_BOTTOM:
            case LAYOUT_SPECIFIC:
            case LAYOUT_SYNC:
                break;
            case LAYOUT_MOVE_SELECTION:
            default:
                // Remember the previously selected view
                index = mSelectedPosition - mFirstPosition;
                if (index >= 0 && index < childCount) {
                    oldSel = getChildAt(index);
                }

                // Remember the previous first child
                oldFirst = getChildAt(0);

                if (mNextSelectedPosition >= 0) {
                    delta = mNextSelectedPosition - mSelectedPosition;
                }

                // Caution: newSel might be null
                newSel = getChildAt(index + delta);
            }


            boolean dataChanged = mDataChanged;
            if (dataChanged) {
                handleDataChanged();
            }

            // Handle the empty set by removing all views that are visible
            // and calling it a day
            if (mItemCount == 0) {
                resetList();
                invokeOnItemScrollListener();
                return;
            } else if (mItemCount != mAdapter.getCount()) {
                throw new IllegalStateException("The content of the adapter has changed but "
                        + "ListView did not receive a notification. Make sure the content of "
                        + "your adapter is not modified from a background thread, but only "
                        + "from the UI thread. [in ListView(" + getId() + ", " + getClass() 
                        + ") with Adapter(" + mAdapter.getClass() + ")]");
            }

            setSelectedPositionInt(mNextSelectedPosition);

            // Pull all children into the RecycleBin.
            // These views will be reused if possible
            final int firstPosition = mFirstPosition;
            final RecycleBin recycleBin = mRecycler;

            // reset the focus restoration
            View focusLayoutRestoreDirectChild = null;

            // Don't put header or footer views into the Recycler. Those are
            // already cached in mHeaderViews;
            if (dataChanged) {
                for (int i = 0; i < childCount; i++) {
                    recycleBin.addScrapView(getChildAt(i), firstPosition+i);
                }
            } else {
                recycleBin.fillActiveViews(childCount, firstPosition);
            }

            // take focus back to us temporarily to avoid the eventual
            // call to clear focus when removing the focused child below
            // from messing things up when ViewAncestor assigns focus back
            // to someone else
            final View focusedChild = getFocusedChild();
            if (focusedChild != null) {
                // TODO: in some cases focusedChild.getParent() == null

                // we can remember the focused view to restore after relayout if the
                // data hasn't changed, or if the focused position is a header or footer
                if (!dataChanged || isDirectChildHeaderOrFooter(focusedChild)) {
                    focusLayoutRestoreDirectChild = focusedChild;
                    // remember the specific view that had focus
                    focusLayoutRestoreView = findFocus();
                    if (focusLayoutRestoreView != null) {
                        // tell it we are going to mess with it
                        focusLayoutRestoreView.onStartTemporaryDetach();
                    }
                }
                requestFocus();
            }

            // Remember which child, if any, had accessibility focus.
            final ViewRootImpl viewRootImpl = getViewRootImpl();
            if (viewRootImpl != null) {
                final View accessFocusedView = viewRootImpl.getAccessibilityFocusedHost();
                if (accessFocusedView != null) {
                    final View accessFocusedChild = findAccessibilityFocusedChild(
                            accessFocusedView);
                    if (accessFocusedChild != null) {
                        if (!dataChanged || isDirectChildHeaderOrFooter(accessFocusedChild)) {
                            // If the views won't be changing, try to maintain
                            // focus on the current view host and (if
                            // applicable) its virtual view.
                            accessibilityFocusLayoutRestoreView = accessFocusedView;
                            accessibilityFocusLayoutRestoreNode = viewRootImpl
                                    .getAccessibilityFocusedVirtualView();
                        } else {
                            // Otherwise, try to maintain focus at the same
                            // position.
                            accessibilityFocusPosition = getPositionForView(accessFocusedChild);
                        }
                    }
                }
            }

            // Clear out old views
            detachAllViewsFromParent();
            recycleBin.removeSkippedScrap();

            switch (mLayoutMode) {
            case LAYOUT_SET_SELECTION:
                if (newSel != null) {
                    sel = fillFromSelection(newSel.getTop(), childrenTop, childrenBottom);
                } else {
                    sel = fillFromMiddle(childrenTop, childrenBottom);
                }
                break;
            case LAYOUT_SYNC:
                sel = fillSpecific(mSyncPosition, mSpecificTop);
                break;
            case LAYOUT_FORCE_BOTTOM:
                sel = fillUp(mItemCount - 1, childrenBottom);
                adjustViewsUpOrDown();
                break;
            case LAYOUT_FORCE_TOP:
                mFirstPosition = 0;
                sel = fillFromTop(childrenTop);
                adjustViewsUpOrDown();
                break;
            case LAYOUT_SPECIFIC:
                sel = fillSpecific(reconcileSelectedPosition(), mSpecificTop);
                break;
            case LAYOUT_MOVE_SELECTION:
                sel = moveSelection(oldSel, newSel, delta, childrenTop, childrenBottom);
                break;
            default:
                if (childCount == 0) {
                    if (!mStackFromBottom) {
                        final int position = lookForSelectablePosition(0, true);
                        setSelectedPositionInt(position);
                        sel = fillFromTop(childrenTop);
                    } else {
                        final int position = lookForSelectablePosition(mItemCount - 1, false);
                        setSelectedPositionInt(position);
                        sel = fillUp(mItemCount - 1, childrenBottom);
                    }
                } else {
                    if (mSelectedPosition >= 0 && mSelectedPosition < mItemCount) {
                        sel = fillSpecific(mSelectedPosition,
                                oldSel == null ? childrenTop : oldSel.getTop());
                    } else if (mFirstPosition < mItemCount) {
                        sel = fillSpecific(mFirstPosition,
                                oldFirst == null ? childrenTop : oldFirst.getTop());
                    } else {
                        sel = fillSpecific(0, childrenTop);
                    }
                }
                break;
            }

            // Flush any cached views that did not get reused above
            recycleBin.scrapActiveViews();

            if (sel != null) {
                // the current selected item should get focus if items
                // are focusable
                if (mItemsCanFocus && hasFocus() && !sel.hasFocus()) {
                    final boolean focusWasTaken = (sel == focusLayoutRestoreDirectChild &&
                            focusLayoutRestoreView != null &&
                            focusLayoutRestoreView.requestFocus()) || sel.requestFocus();
                    if (!focusWasTaken) {
                        // selected item didn't take focus, fine, but still want
                        // to make sure something else outside of the selected view
                        // has focus
                        final View focused = getFocusedChild();
                        if (focused != null) {
                            focused.clearFocus();
                        }
                        positionSelector(INVALID_POSITION, sel);
                    } else {
                        sel.setSelected(false);
                        mSelectorRect.setEmpty();
                    }
                } else {
                    positionSelector(INVALID_POSITION, sel);
                }
                mSelect
