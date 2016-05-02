// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) nonlb 

package android.support.v4.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.*;
import android.view.*;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import java.util.*;

// Referenced classes of package android.support.v4.view:
//            PagerAdapter, MotionEventCompat, ViewCompat, VelocityTrackerCompat, 
//            KeyEventCompat, ViewConfigurationCompat, AccessibilityDelegateCompat

public class ViewPager extends ViewGroup {
    public static class LayoutParams extends android.view.ViewGroup.LayoutParams {

        public int gravity;
        public boolean isDecor;
        public boolean needsMeasure;
        public float widthFactor;

        public LayoutParams() {
            super(-1, -1);
            widthFactor = 0.0F;
        }

        public LayoutParams(Context context, AttributeSet attributeset) {
            super(context, attributeset);
            widthFactor = 0.0F;
            TypedArray typedarray = context.obtainStyledAttributes(attributeset, ViewPager.LAYOUT_ATTRS);
            gravity = typedarray.getInteger(0, 48);
            typedarray.recycle();
        }
    }

    private class PagerObserver extends DataSetObserver {

        public void onChanged() {
            dataSetChanged();
        }

        public void onInvalidated() {
            dataSetChanged();
        }

        final ViewPager this$0;

        private PagerObserver() {
            this$0 = ViewPager.this;
            super();
        }

    }

    class MyAccessibilityDelegate extends AccessibilityDelegateCompat {

        public void onInitializeAccessibilityEvent(View view, AccessibilityEvent accessibilityevent) {
            super.onInitializeAccessibilityEvent(view, accessibilityevent);
            accessibilityevent.setClassName(android/support/v4/view/ViewPager.getName());
        }

        public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilitynodeinfocompat) {
            boolean flag = true;
            super.onInitializeAccessibilityNodeInfo(view, accessibilitynodeinfocompat);
            accessibilitynodeinfocompat.setClassName(android/support/v4/view/ViewPager.getName());
            if(mAdapter == null || mAdapter.getCount() <= flag)
                flag = false;
            accessibilitynodeinfocompat.setScrollable(flag);
            if(mAdapter != null && mCurItem >= 0 && mCurItem < -1 + mAdapter.getCount())
                accessibilitynodeinfocompat.addAction(4096);
            if(mAdapter != null && mCurItem > 0 && mCurItem < mAdapter.getCount())
                accessibilitynodeinfocompat.addAction(8192);
        }

        public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
            boolean flag = true;
            if(!super.performAccessibilityAction(view, i, bundle)) goto _L2; else goto _L1
_L1:
            return flag;
_L2:
            switch(i) {
            default:
                flag = false;
                break;

            case 4096: 
                if(mAdapter != null && mCurItem >= 0 && mCurItem < -1 + mAdapter.getCount())
                    setCurrentItem(1 + mCurItem);
                else
                    flag = false;
                break;

            case 8192: 
                if(mAdapter != null && mCurItem > 0 && mCurItem < mAdapter.getCount())
                    setCurrentItem(-1 + mCurItem);
                else
                    flag = false;
                break;
            }
            if(true) goto _L1; else goto _L3
_L3:
        }

        final ViewPager this$0;

        MyAccessibilityDelegate() {
            this$0 = ViewPager.this;
            super();
        }
    }

    public static class SavedState extends android.view.View.BaseSavedState {

        public String toString() {
            return (new StringBuilder()).append("FragmentPager.SavedState{").append(Integer.toHexString(System.identityHashCode(this))).append(" position=").append(position).append("}").toString();
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(position);
            parcel.writeParcelable(adapterState, i);
        }

        public static final android.os.Parcelable.Creator CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks() {

            public SavedState createFromParcel(Parcel parcel, ClassLoader classloader) {
                return new SavedState(parcel, classloader);
            }

            public volatile Object createFromParcel(Parcel parcel, ClassLoader classloader) {
                return createFromParcel(parcel, classloader);
            }

            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }

            public volatile Object[] newArray(int i) {
                return newArray(i);
            }

        });
        Parcelable adapterState;
        ClassLoader loader;
        int position;


        SavedState(Parcel parcel, ClassLoader classloader) {
            super(parcel);
            if(classloader == null)
                classloader = getClass().getClassLoader();
            position = parcel.readInt();
            adapterState = parcel.readParcelable(classloader);
            loader = classloader;
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }
    }

    static interface Decor {
    }

    static interface OnAdapterChangeListener {

        public abstract void onAdapterChanged(PagerAdapter pageradapter, PagerAdapter pageradapter1);
    }

    public static class SimpleOnPageChangeListener
        implements OnPageChangeListener {

        public void onPageScrollStateChanged(int i) {
        }

        public void onPageScrolled(int i, float f, int j) {
        }

        public void onPageSelected(int i) {
        }

        public SimpleOnPageChangeListener() {
        }
    }

    public static interface OnPageChangeListener {

        public abstract void onPageScrollStateChanged(int i);

        public abstract void onPageScrolled(int i, float f, int j);

        public abstract void onPageSelected(int i);
    }

    static class ItemInfo {

        Object object;
        float offset;
        int position;
        boolean scrolling;
        float widthFactor;

        ItemInfo() {
        }
    }


    public ViewPager(Context context) {
        super(context);
        mItems = new ArrayList();
        mTempItem = new ItemInfo();
        mTempRect = new Rect();
        mRestoredCurItem = -1;
        mRestoredAdapterState = null;
        mRestoredClassLoader = null;
        mFirstOffset = -3.402823E+38F;
        mLastOffset = 3.402823E+38F;
        mOffscreenPageLimit = 1;
        mActivePointerId = -1;
        mFirstLayout = true;
        mNeedCalculatePageOffsets = false;
        mScrollState = 0;
        initViewPager();
    }

    public ViewPager(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        mItems = new ArrayList();
        mTempItem = new ItemInfo();
        mTempRect = new Rect();
        mRestoredCurItem = -1;
        mRestoredAdapterState = null;
        mRestoredClassLoader = null;
        mFirstOffset = -3.402823E+38F;
        mLastOffset = 3.402823E+38F;
        mOffscreenPageLimit = 1;
        mActivePointerId = -1;
        mFirstLayout = true;
        mNeedCalculatePageOffsets = false;
        mScrollState = 0;
        initViewPager();
    }

    private void calculatePageOffsets(ItemInfo iteminfo, int i, ItemInfo iteminfo1) {
        int j = mAdapter.getCount();
        int k = getWidth();
        float f;
        if(k > 0)
            f = (float)mPageMargin / (float)k;
        else
            f = 0.0F;
        if(iteminfo1 != null) {
            int k2 = iteminfo1.position;
            if(k2 < iteminfo.position) {
                int j3 = 0;
                float f6 = f + (iteminfo1.offset + iteminfo1.widthFactor);
                for(int k3 = k2 + 1; k3 <= iteminfo.position && j3 < mItems.size(); k3++) {
                    ItemInfo iteminfo5;
                    for(iteminfo5 = (ItemInfo)mItems.get(j3); k3 > iteminfo5.position && j3 < -1 + mItems.size(); iteminfo5 = (ItemInfo)mItems.get(j3))
                        j3++;

                    for(; k3 < iteminfo5.position; k3++)
                        f6 += f + mAdapter.getPageWidth(k3);

                    iteminfo5.offset = f6;
                    f6 += f + iteminfo5.widthFactor;
                }

            } else
            if(k2 > iteminfo.position) {
                int l2 = -1 + mItems.size();
                float f5 = iteminfo1.offset;
                for(int i3 = k2 - 1; i3 >= iteminfo.position && l2 >= 0; i3--) {
                    ItemInfo iteminfo4;
                    for(iteminfo4 = (ItemInfo)mItems.get(l2); i3 < iteminfo4.position && l2 > 0; iteminfo4 = (ItemInfo)mItems.get(l2))
                        l2--;

                    for(; i3 > iteminfo4.position; i3--)
                        f5 -= f + mAdapter.getPageWidth(i3);

                    f5 -= f + iteminfo4.widthFactor;
                    iteminfo4.offset = f5;
                }

            }
        }
        int l = mItems.size();
        float f1 = iteminfo.offset;
        int i1 = -1 + iteminfo.position;
        float f2;
        float f3;
        if(iteminfo.position == 0)
            f2 = iteminfo.offset;
        else
            f2 = -3.402823E+38F;
        mFirstOffset = f2;
        if(iteminfo.position == j - 1)
            f3 = (iteminfo.offset + iteminfo.widthFactor) - 1.0F;
        else
            f3 = 3.402823E+38F;
        mLastOffset = f3;
        for(int j1 = i - 1; j1 >= 0;) {
            ItemInfo iteminfo3;
            int j2;
            for(iteminfo3 = (ItemInfo)mItems.get(j1); i1 > iteminfo3.position; i1 = j2) {
                PagerAdapter pageradapter1 = mAdapter;
                j2 = i1 - 1;
                f1 -= f + pageradapter1.getPageWidth(i1);
            }

            f1 -= f + iteminfo3.widthFactor;
            iteminfo3.offset = f1;
            if(iteminfo3.position == 0)
                mFirstOffset = f1;
            j1--;
            i1--;
        }

        float f4 = f + (iteminfo.offset + iteminfo.widthFactor);
        int k1 = 1 + iteminfo.position;
        for(int l1 = i + 1; l1 < l;) {
            ItemInfo iteminfo2;
            int i2;
            for(iteminfo2 = (ItemInfo)mItems.get(l1); k1 < iteminfo2.position; k1 = i2) {
                PagerAdapter pageradapter = mAdapter;
                i2 = k1 + 1;
                f4 += f + pageradapter.getPageWidth(k1);
            }

            if(iteminfo2.position == j - 1)
                mLastOffset = (f4 + iteminfo2.widthFactor) - 1.0F;
            iteminfo2.offset = f4;
            f4 += f + iteminfo2.widthFactor;
            l1++;
            k1++;
        }

        mNeedCalculatePageOffsets = false;
    }

    private void completeScroll() {
        boolean flag;
        if(mScrollState == 2)
            flag = true;
        else
            flag = false;
        if(flag) {
            setScrollingCacheEnabled(false);
            mScroller.abortAnimation();
            int j = getScrollX();
            int k = getScrollY();
            int l = mScroller.getCurrX();
            int i1 = mScroller.getCurrY();
            if(j != l || k != i1)
                scrollTo(l, i1);
            setScrollState(0);
        }
        mPopulatePending = false;
        for(int i = 0; i < mItems.size(); i++) {
            ItemInfo iteminfo = (ItemInfo)mItems.get(i);
            if(iteminfo.scrolling) {
                flag = true;
                iteminfo.scrolling = false;
            }
        }

        if(flag)
            populate();
    }

    private int determineTargetPage(int i, float f, int j, int k) {
        int l;
        if(Math.abs(k) > mFlingDistance && Math.abs(j) > mMinimumVelocity) {
            if(j > 0)
                l = i;
            else
                l = i + 1;
        } else {
            l = (int)(0.5F + (f + (float)i));
        }
        if(mItems.size() > 0) {
            ItemInfo iteminfo = (ItemInfo)mItems.get(0);
            ItemInfo iteminfo1 = (ItemInfo)mItems.get(-1 + mItems.size());
            l = Math.max(iteminfo.position, Math.min(l, iteminfo1.position));
        }
        return l;
    }

    private void endDrag() {
        mIsBeingDragged = false;
        mIsUnableToDrag = false;
        if(mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private Rect getChildRectInPagerCoordinates(Rect rect, View view) {
        if(rect == null)
            rect = new Rect();
        if(view == null) {
            rect.set(0, 0, 0, 0);
        } else {
            rect.left = view.getLeft();
            rect.right = view.getRight();
            rect.top = view.getTop();
            rect.bottom = view.getBottom();
            android.view.ViewParent viewparent = view.getParent();
            while((viewparent instanceof ViewGroup) && viewparent != this)  {
                ViewGroup viewgroup = (ViewGroup)viewparent;
                rect.left = rect.left + viewgroup.getLeft();
                rect.right = rect.right + viewgroup.getRight();
                rect.top = rect.top + viewgroup.getTop();
                rect.bottom = rect.bottom + viewgroup.getBottom();
                viewparent = viewgroup.getParent();
            }
        }
        return rect;
    }

    private ItemInfo infoForCurrentScrollPosition() {
        float f = 0.0F;
        int i = getWidth();
        float f1;
        int j;
        float f2;
        float f3;
        boolean flag;
        ItemInfo iteminfo;
        int k;
        if(i > 0)
            f1 = (float)getScrollX() / (float)i;
        else
            f1 = 0.0F;
        if(i > 0)
            f = (float)mPageMargin / (float)i;
        j = -1;
        f2 = 0.0F;
        f3 = 0.0F;
        flag = true;
        iteminfo = null;
        k = 0;
        do {
            ItemInfo iteminfo1;
            float f4;
label0:
            {
                if(k < mItems.size()) {
                    iteminfo1 = (ItemInfo)mItems.get(k);
                    if(!flag && iteminfo1.position != j + 1) {
                        iteminfo1 = mTempItem;
                        iteminfo1.offset = f + (f2 + f3);
                        iteminfo1.position = j + 1;
                        iteminfo1.widthFactor = mAdapter.getPageWidth(iteminfo1.position);
                        k--;
                    }
                    f4 = iteminfo1.offset;
                    float f5 = f + (f4 + iteminfo1.widthFactor);
                    if(flag || f1 >= f4) {
                        if(f1 >= f5 && k != -1 + mItems.size())
                            break label0;
                        iteminfo = iteminfo1;
                    }
                }
                return iteminfo;
            }
            flag = false;
            j = iteminfo1.position;
            f2 = f4;
            f3 = iteminfo1.widthFactor;
            iteminfo = iteminfo1;
            k++;
        } while(true);
    }

    private boolean isGutterDrag(float f, float f1) {
        boolean flag;
        if(f < (float)mGutterSize && f1 > 0.0F || f > (float)(getWidth() - mGutterSize) && f1 < 0.0F)
            flag = true;
        else
            flag = false;
        return flag;
    }

    private void onSecondaryPointerUp(MotionEvent motionevent) {
        int i = MotionEventCompat.getActionIndex(motionevent);
        if(MotionEventCompat.getPointerId(motionevent, i) == mActivePointerId) {
            int j;
            if(i == 0)
                j = 1;
            else
                j = 0;
            mLastMotionX = MotionEventCompat.getX(motionevent, j);
            mActivePointerId = MotionEventCompat.getPointerId(motionevent, j);
            if(mVelocityTracker != null)
                mVelocityTracker.clear();
        }
    }

    private boolean pageScrolled(int i) {
        boolean flag = false;
        if(mItems.size() == 0) {
            mCalledSuper = false;
            onPageScrolled(0, 0.0F, 0);
            if(!mCalledSuper)
                throw new IllegalStateException("onPageScrolled did not call superclass implementation");
        } else {
            ItemInfo iteminfo = infoForCurrentScrollPosition();
            int j = getWidth();
            int k = j + mPageMargin;
            float f = (float)mPageMargin / (float)j;
            int l = iteminfo.position;
            float f1 = ((float)i / (float)j - iteminfo.offset) / (f + iteminfo.widthFactor);
            int i1 = (int)(f1 * (float)k);
            mCalledSuper = false;
            onPageScrolled(l, f1, i1);
            if(!mCalledSuper)
                throw new IllegalStateException("onPageScrolled did not call superclass implementation");
            flag = true;
        }
        return flag;
    }

    private boolean performDrag(float f) {
        boolean flag;
        float f2;
        int i;
        float f3;
        float f4;
        boolean flag1;
        boolean flag2;
        flag = false;
        float f1 = mLastMotionX - f;
        mLastMotionX = f;
        f2 = f1 + (float)getScrollX();
        i = getWidth();
        f3 = (float)i * mFirstOffset;
        f4 = (float)i * mLastOffset;
        flag1 = true;
        flag2 = true;
        ItemInfo iteminfo = (ItemInfo)mItems.get(0);
        ItemInfo iteminfo1 = (ItemInfo)mItems.get(-1 + mItems.size());
        if(iteminfo.position != 0) {
            flag1 = false;
            f3 = iteminfo.offset * (float)i;
        }
        if(iteminfo1.position != -1 + mAdapter.getCount()) {
            flag2 = false;
            f4 = iteminfo1.offset * (float)i;
        }
        if(f2 >= f3) goto _L2; else goto _L1
_L1:
        if(flag1) {
            float f6 = f3 - f2;
            flag = mLeftEdge.onPull(Math.abs(f6) / (float)i);
        }
        f2 = f3;
_L4:
        mLastMotionX = mLastMotionX + (f2 - (float)(int)f2);
        scrollTo((int)f2, getScrollY());
        pageScrolled((int)f2);
        return flag;
_L2:
        if(f2 > f4) {
            if(flag2) {
                float f5 = f2 - f4;
                flag = mRightEdge.onPull(Math.abs(f5) / (float)i);
            }
            f2 = f4;
        }
        if(true) goto _L4; else goto _L3
_L3:
    }

    private void recomputeScrollPosition(int i, int j, int k, int l) {
        if(j <= 0 || mItems.isEmpty()) goto _L2; else goto _L1
_L1:
        int j1 = i + k;
        int k1 = j + l;
        int l1 = (int)(((float)getScrollX() / (float)k1) * (float)j1);
        scrollTo(l1, getScrollY());
        if(!mScroller.isFinished()) {
            int i2 = mScroller.getDuration() - mScroller.timePassed();
            ItemInfo iteminfo1 = infoForPosition(mCurItem);
            mScroller.startScroll(l1, 0, (int)(iteminfo1.offset * (float)i), 0, i2);
        }
_L4:
        return;
_L2:
        ItemInfo iteminfo = infoForPosition(mCurItem);
        float f;
        int i1;
        if(iteminfo != null)
            f = Math.min(iteminfo.offset, mLastOffset);
        else
            f = 0.0F;
        i1 = (int)(f * (float)i);
        if(i1 != getScrollX()) {
            completeScroll();
            scrollTo(i1, getScrollY());
        }
        if(true) goto _L4; else goto _L3
_L3:
    }

    private void removeNonDecorViews() {
        for(int i = 0; i < getChildCount(); i++)
            if(!((LayoutParams)getChildAt(i).getLayoutParams()).isDecor) {
                removeViewAt(i);
                i--;
            }

    }

    private void setScrollState(int i) {
        if(mScrollState != i) goto _L2; else goto _L1
_L1:
        return;
_L2:
        mScrollState = i;
        if(mOnPageChangeListener != null)
            mOnPageChangeListener.onPageScrollStateChanged(i);
        if(true) goto _L1; else goto _L3
_L3:
    }

    private void setScrollingCacheEnabled(boolean flag) {
        if(mScrollingCacheEnabled != flag)
            mScrollingCacheEnabled = flag;
    }

    public void addFocusables(ArrayList arraylist, int i, int j) {
        int k;
        int l;
        k = arraylist.size();
        l = getDescendantFocusability();
        if(l != 0x60000) {
            for(int i1 = 0; i1 < getChildCount(); i1++) {
                View view = getChildAt(i1);
                if(view.getVisibility() == 0) {
                    ItemInfo iteminfo = infoForChild(view);
                    if(iteminfo != null && iteminfo.position == mCurItem)
                        view.addFocusables(arraylist, i, j);
                }
            }

        }
        break MISSING_BLOCK_LABEL_87;
        if((l != 0x40000 || k == arraylist.size()) && isFocusable() && ((j & 1) != 1 || !isInTouchMode() || isFocusableInTouchMode()) && arraylist != null)
            arraylist.add(this);
        return;
    }

    ItemInfo addNewItem(int i, int j) {
        ItemInfo iteminfo = new ItemInfo();
        iteminfo.position = i;
        iteminfo.object = mAdapter.instantiateItem(this, i);
        iteminfo.widthFactor = mAdapter.getPageWidth(i);
        if(j < 0 || j >= mItems.size())
            mItems.add(iteminfo);
        else
            mItems.add(j, iteminfo);
        return iteminfo;
    }

    public void addTouchables(ArrayList arraylist) {
        for(int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if(view.getVisibility() != 0)
                continue;
            ItemInfo iteminfo = infoForChild(view);
            if(iteminfo != null && iteminfo.position == mCurItem)
                view.addTouchables(arraylist);
        }

    }

    public void addView(View view, int i, android.view.ViewGroup.LayoutParams layoutparams) {
        if(!checkLayoutParams(layoutparams))
            layoutparams = generateLayoutParams(layoutparams);
        LayoutParams layoutparams1 = (LayoutParams)layoutparams;
        layoutparams1.isDecor = layoutparams1.isDecor | (view instanceof Decor);
        if(mInLayout) {
            if(layoutparams1 != null && layoutparams1.isDecor)
                throw new IllegalStateException("Cannot add pager decor view during layout");
            layoutparams1.needsMeasure = true;
            addViewInLayout(view, i, layoutparams);
        } else {
            super.addView(view, i, layoutparams);
        }
    }

    public boolean arrowScroll(int i) {
        View view;
        boolean flag;
        View view1;
        view = findFocus();
        if(view == this)
            view = null;
        flag = false;
        view1 = FocusFinder.getInstance().findNextFocus(this, view, i);
        if(view1 == null || view1 == view) goto _L2; else goto _L1
_L1:
        if(i != 17) goto _L4; else goto _L3
_L3:
        int l = getChildRectInPagerCoordinates(mTempRect, view1).left;
        int i1 = getChildRectInPagerCoordinates(mTempRect, view).left;
        if(view != null && l >= i1)
            flag = pageLeft();
        else
            flag = view1.requestFocus();
_L6:
        if(flag)
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(i));
        return flag;
_L4:
        if(i == 66) {
            int j = getChildRectInPagerCoordinates(mTempRect, view1).left;
            int k = getChildRectInPagerCoordinates(mTempRect, view).left;
            if(view != null && j <= k)
                flag = pageRight();
            else
                flag = view1.requestFocus();
        }
        continue; /* Loop/switch isn't completed */
_L2:
        if(i == 17 || i == 1)
            flag = pageLeft();
        else
        if(i == 66 || i == 2)
            flag = pageRight();
        if(true) goto _L6; else goto _L5
_L5:
    }

    public boolean beginFakeDrag() {
        boolean flag = false;
        if(!mIsBeingDragged) {
            mFakeDragging = true;
            setScrollState(1);
            mLastMotionX = 0.0F;
            mInitialMotionX = 0.0F;
            long l;
            MotionEvent motionevent;
            if(mVelocityTracker == null)
                mVelocityTracker = VelocityTracker.obtain();
            else
                mVelocityTracker.clear();
            l = SystemClock.uptimeMillis();
            motionevent = MotionEvent.obtain(l, l, 0, 0.0F, 0.0F, 0);
            mVelocityTracker.addMovement(motionevent);
            motionevent.recycle();
            mFakeDragBeginTime = l;
            flag = true;
        }
        return flag;
    }

    protected boolean canScroll(View view, boolean flag, int i, int j, int k) {
        ViewGroup viewgroup;
        int l;
        int i1;
        int j1;
        if(!(view instanceof ViewGroup))
            break MISSING_BLOCK_LABEL_146;
        viewgroup = (ViewGroup)view;
        l = view.getScrollX();
        i1 = view.getScrollY();
        j1 = -1 + viewgroup.getChildCount();
_L3:
        View view1;
        if(j1 < 0)
            break MISSING_BLOCK_LABEL_146;
        view1 = viewgroup.getChildAt(j1);
        if(j + l < view1.getLeft() || j + l >= view1.getRight() || k + i1 < view1.getTop() || k + i1 >= view1.getBottom() || !canScroll(view1, true, i, (j + l) - view1.getLeft(), (k + i1) - view1.getTop())) goto _L2; else goto _L1
_L1:
        boolean flag1 = true;
_L4:
        return flag1;
_L2:
        j1--;
          goto _L3
        if(flag && ViewCompat.canScrollHorizontally(view, -i))
            flag1 = true;
        else
            flag1 = false;
          goto _L4
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams layoutparams) {
        boolean flag;
        if((layoutparams instanceof LayoutParams) && super.checkLayoutParams(layoutparams))
            flag = true;
        else
            flag = false;
        return flag;
    }

    public void computeScroll() {
        if(!mScroller.isFinished() && mScroller.computeScrollOffset()) {
            int i = getScrollX();
            int j = getScrollY();
            int k = mScroller.getCurrX();
            int l = mScroller.getCurrY();
            if(i != k || j != l) {
                scrollTo(k, l);
                if(!pageScrolled(k)) {
                    mScroller.abortAnimation();
                    scrollTo(0, l);
                }
            }
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            completeScroll();
        }
    }

    void dataSetChanged() {
        boolean flag;
        int i;
        boolean flag1;
        int j;
        if(mItems.size() < 1 + 2 * mOffscreenPageLimit && mItems.size() < mAdapter.getCount())
            flag = true;
        else
            flag = false;
        i = mCurItem;
        flag1 = false;
        j = 0;
        while(j < mItems.size())  {
            ItemInfo iteminfo = (ItemInfo)mItems.get(j);
            int i1 = mAdapter.getItemPosition(iteminfo.object);
            if(i1 != -1)
                if(i1 == -2) {
                    mItems.remove(j);
                    j--;
                    if(!flag1) {
                        mAdapter.startUpdate(this);
                        flag1 = true;
                    }
                    mAdapter.destroyItem(this, iteminfo.position, iteminfo.object);
                    flag = true;
                    if(mCurItem == iteminfo.position) {
                        i = Math.max(0, Math.min(mCurItem, -1 + mAdapter.getCount()));
                        flag = true;
                    }
                } else
                if(iteminfo.position != i1) {
                    if(iteminfo.position == mCurItem)
                        i = i1;
                    iteminfo.position = i1;
                    flag = true;
                }
            j++;
        }
        if(flag1)
            mAdapter.finishUpdate(this);
        Collections.sort(mItems, COMPARATOR);
        if(flag) {
            int k = getChildCount();
            for(int l = 0; l < k; l++) {
                LayoutParams layoutparams = (LayoutParams)getChildAt(l).getLayoutParams();
                if(!layoutparams.isDecor)
                    layoutparams.widthFactor = 0.0F;
            }

            setCurrentItemInternal(i, false, true);
            requestLayout();
        }
    }

    public boolean dispatchKeyEvent(KeyEvent keyevent) {
        boolean flag;
        if(super.dispatchKeyEvent(keyevent) || executeKeyEvent(keyevent))
            flag = true;
        else
            flag = false;
        return flag;
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityevent) {
        int i;
        int j;
        i = getChildCount();
        j = 0;
_L3:
        if(j >= i) goto _L2; else goto _L1
_L1:
        boolean flag;
        View view = getChildAt(j);
        if(view.getVisibility() != 0)
            continue; /* Loop/switch isn't completed */
        ItemInfo iteminfo = infoForChild(view);
        if(iteminfo == null || iteminfo.position != mCurItem || !view.dispatchPopulateAccessibilityEvent(accessibilityevent))
            continue; /* Loop/switch isn't completed */
        flag = true;
_L4:
        return flag;
        j++;
          goto _L3
_L2:
        flag = false;
          goto _L4
    }

    float distanceInfluenceForSnapDuration(float f) {
        return (float)Math.sin((float)(0.4712389167638204D * (double)(f - 0.5F)));
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        boolean flag = false;
        int i = ViewCompat.getOverScrollMode(this);
        if(i == 0 || i == 1 && mAdapter != null && mAdapter.getCount() > 1) {
            if(!mLeftEdge.isFinished()) {
                int i1 = canvas.save();
                int j1 = getHeight() - getPaddingTop() - getPaddingBottom();
                int k1 = getWidth();
                canvas.rotate(270F);
                canvas.translate(-j1 + getPaddingTop(), mFirstOffset * (float)k1);
                mLeftEdge.setSize(j1, k1);
                flag = false | mLeftEdge.draw(canvas);
                canvas.restoreToCount(i1);
            }
            if(!mRightEdge.isFinished()) {
                int j = canvas.save();
                int k = getWidth();
                int l = getHeight() - getPaddingTop() - getPaddingBottom();
                canvas.rotate(90F);
                canvas.translate(-getPaddingTop(), -(1.0F + mLastOffset) * (float)k);
                mRightEdge.setSize(l, k);
                flag |= mRightEdge.draw(canvas);
                canvas.restoreToCount(j);
            }
        } else {
            mLeftEdge.finish();
            mRightEdge.finish();
        }
        if(flag)
            ViewCompat.postInvalidateOnAnimation(this);
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable drawable = mMarginDrawable;
        if(drawable != null && drawable.isStateful())
            drawable.setState(getDrawableState());
    }

    public void endFakeDrag() {
        if(!mFakeDragging) {
            throw new IllegalStateException("No fake drag in progress. Call beginFakeDrag first.");
        } else {
            VelocityTracker velocitytracker = mVelocityTracker;
            velocitytracker.computeCurrentVelocity(1000, mMaximumVelocity);
            int i = (int)VelocityTrackerCompat.getXVelocity(velocitytracker, mActivePointerId);
            mPopulatePending = true;
            int j = getWidth();
            int k = getScrollX();
            ItemInfo iteminfo = infoForCurrentScrollPosition();
            setCurrentItemInternal(determineTargetPage(iteminfo.position, ((float)k / (float)j - iteminfo.offset) / iteminfo.widthFactor, i, (int)(mLastMotionX - mInitialMotionX)), true, true, i);
            endDrag();
            mFakeDragging = false;
            return;
        }
    }

    public boolean executeKeyEvent(KeyEvent keyevent) {
        boolean flag = false;
        if(keyevent.getAction() != 0) goto _L2; else goto _L1
_L1:
        keyevent.getKeyCode();
        JVM INSTR lookupswitch 3: default 48
    //                   21: 50
    //                   22: 60
    //                   61: 70;
           goto _L2 _L3 _L4 _L5
_L2:
        return flag;
_L3:
        flag = arrowScroll(17);
        continue; /* Loop/switch isn't completed */
_L4:
        flag = arrowScroll(66);
        continue; /* Loop/switch isn't completed */
_L5:
        if(android.os.Build.VERSION.SDK_INT >= 11)
            if(KeyEventCompat.hasNoModifiers(keyevent))
                flag = arrowScroll(2);
            else
            if(KeyEventCompat.hasModifiers(keyevent, 1))
                flag = arrowScroll(1);
        if(true) goto _L2; else goto _L6
_L6:
    }

    public void fakeDragBy(float f) {
        float f1;
        float f2;
        float f3;
        if(!mFakeDragging)
            throw new IllegalStateException("No fake drag in progress. Call beginFakeDrag first.");
        mLastMotionX = f + mLastMotionX;
        f1 = (float)getScrollX() - f;
        int i = getWidth();
        f2 = (float)i * mFirstOffset;
        f3 = (float)i * mLastOffset;
        ItemInfo iteminfo = (ItemInfo)mItems.get(0);
        ItemInfo iteminfo1 = (ItemInfo)mItems.get(-1 + mItems.size());
        if(iteminfo.position != 0)
            f2 = iteminfo.offset * (float)i;
        if(iteminfo1.position != -1 + mAdapter.getCount())
            f3 = iteminfo1.offset * (float)i;
        if(f1 >= f2) goto _L2; else goto _L1
_L1:
        f1 = f2;
_L4:
        mLastMotionX = mLastMotionX + (f1 - (float)(int)f1);
        scrollTo((int)f1, getScrollY());
        pageScrolled((int)f1);
        long l = SystemClock.uptimeMillis();
        MotionEvent motionevent = MotionEvent.obtain(mFakeDragBeginTime, l, 2, mLastMotionX, 0.0F, 0);
        mVelocityTracker.addMovement(motionevent);
        motionevent.recycle();
        return;
_L2:
        if(f1 > f3)
            f1 = f3;
        if(true) goto _L4; else goto _L3
_L3:
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attributeset) {
        return new LayoutParams(getContext(), attributeset);
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams layoutparams) {
        return generateDefaultLayoutParams();
    }

    public PagerAdapter getAdapter() {
        return mAdapter;
    }

    public int getCurrentItem() {
        return mCurItem;
    }

    public int getOffscreenPageLimit() {
        return mOffscreenPageLimit;
    }

    public int getPageMargin() {
        return mPageMargin;
    }

    ItemInfo infoForAnyChild(View view) {
_L3:
        android.view.ViewParent viewparent;
        viewparent = view.getParent();
        if(viewparent == this)
            break MISSING_BLOCK_LABEL_33;
        if(viewparent != null && (viewparent instanceof View)) goto _L2; else goto _L1
_L1:
        ItemInfo iteminfo = null;
_L4:
        return iteminfo;
_L2:
        view = (View)viewparent;
          goto _L3
        iteminfo = infoForChild(view);
          goto _L4
    }

    ItemInfo infoForChild(View view) {
        int i = 0;
_L3:
        ItemInfo iteminfo;
        if(i >= mItems.size())
            break MISSING_BLOCK_LABEL_48;
        iteminfo = (ItemInfo)mItems.get(i);
        if(!mAdapter.isViewFromObject(view, iteminfo.object)) goto _L2; else goto _L1
_L1:
        return iteminfo;
_L2:
        i++;
          goto _L3
        iteminfo = null;
          goto _L1
    }

    ItemInfo infoForPosition(int i) {
        int j = 0;
_L3:
        ItemInfo iteminfo;
        if(j >= mItems.size())
            break MISSING_BLOCK_LABEL_41;
        iteminfo = (ItemInfo)mItems.get(j);
        if(iteminfo.position != i) goto _L2; else goto _L1
_L1:
        return iteminfo;
_L2:
        j++;
          goto _L3
        iteminfo = null;
          goto _L1
    }

    void initViewPager() {
        setWillNotDraw(false);
        setDescendantFocusability(0x40000);
        setFocusable(true);
        Context context = getContext();
        mScroller = new Scroller(context, sInterpolator);
        ViewConfiguration viewconfiguration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(viewconfiguration);
        mMinimumVelocity = viewconfiguration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = viewconfiguration.getScaledMaximumFlingVelocity();
        mLeftEdge = new EdgeEffectCompat(context);
        mRightEdge = new EdgeEffectCompat(context);
        float f = context.getResources().getDisplayMetrics().density;
        mFlingDistance = (int)(25F * f);
        mCloseEnough = (int)(2.0F * f);
        mDefaultGutterSize = (int)(16F * f);
        ViewCompat.setAccessibilityDelegate(this, new MyAccessibilityDelegate());
        if(ViewCompat.getImportantForAccessibility(this) == 0)
            ViewCompat.setImportantForAccessibility(this, 1);
    }

    public boolean isFakeDragging() {
        return mFakeDragging;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFirstLayout = true;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mPageMargin <= 0 || mMarginDrawable == null || mItems.size() <= 0 || mAdapter == null) goto _L2; else goto _L1
_L1:
        int i;
        int j;
        float f;
        int k;
        ItemInfo iteminfo;
        float f1;
        int l;
        int j1;
        int k1;
        i = getScrollX();
        j = getWidth();
        f = (float)mPageMargin / (float)j;
        k = 0;
        iteminfo = (ItemInfo)mItems.get(0);
        f1 = iteminfo.offset;
        l = mItems.size();
        int i1 = iteminfo.position;
        j1 = ((ItemInfo)mItems.get(l - 1)).position;
        k1 = i1;
_L6:
        if(k1 >= j1) goto _L2; else goto _L3
_L3:
        ArrayList arraylist;
        for(; k1 > iteminfo.position && k < l; iteminfo = (ItemInfo)arraylist.get(k)) {
            arraylist = mItems;
            k++;
        }

        float f3;
        if(k1 == iteminfo.position) {
            f3 = (iteminfo.offset + iteminfo.widthFactor) * (float)j;
            f1 = f + (iteminfo.offset + iteminfo.widthFactor);
        } else {
            float f2 = mAdapter.getPageWidth(k1);
            f3 = (f1 + f2) * (float)j;
            f1 += f2 + f;
        }
        if(f3 + (float)mPageMargin > (float)i) {
            mMarginDrawable.setBounds((int)f3, mTopPageBounds, (int)(0.5F + (f3 + (float)mPageMargin)), mBottomPageBounds);
            mMarginDrawable.draw(canvas);
        }
        if(f3 <= (float)(i + j)) goto _L4; else goto _L2
_L2:
        return;
_L4:
        k1++;
        if(true) goto _L6; else goto _L5
_L5:
    }

    public boolean onInterceptTouchEvent(MotionEvent motionevent) {
        int i;
        boolean flag;
        i = 0xff & motionevent.getAction();
        if(i == 3 || i == 1) {
            mIsBeingDragged = false;
            mIsUnableToDrag = false;
            mActivePointerId = -1;
            if(mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            flag = false;
        } else {
label0:
            {
                if(i == 0)
                    break label0;
                if(mIsBeingDragged) {
                    flag = true;
                } else {
                    if(!mIsUnableToDrag)
                        break label0;
                    flag = false;
                }
            }
        }
_L5:
        return flag;
        i;
        JVM INSTR lookupswitch 3: default 120
    //                   0: 397
    //                   2: 150
    //                   6: 519;
           goto _L1 _L2 _L3 _L4
_L1:
        break; /* Loop/switch isn't completed */
_L4:
        break MISSING_BLOCK_LABEL_519;
_L7:
        if(mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(motionevent);
        flag = mIsBeingDragged;
          goto _L5
_L3:
        int j = mActivePointerId;
        if(j == -1) goto _L7; else goto _L6
_L6:
        float f1;
        float f2;
        float f3;
        float f5;
label1:
        {
            int k = MotionEventCompat.findPointerIndex(motionevent, j);
            f1 = MotionEventCompat.getX(motionevent, k);
            f2 = f1 - mLastMotionX;
            f3 = Math.abs(f2);
            float f4 = MotionEventCompat.getY(motionevent, k);
            f5 = Math.abs(f4 - mLastMotionY);
            if(f2 == 0.0F || isGutterDrag(mLastMotionX, f2) || !canScroll(this, false, (int)f2, (int)f1, (int)f4))
                break label1;
            mLastMotionX = f1;
            mInitialMotionX = f1;
            mLastMotionY = f4;
            mIsUnableToDrag = true;
            flag = false;
        }
          goto _L5
        if(f3 > (float)mTouchSlop && f3 > f5) {
            mIsBeingDragged = true;
            setScrollState(1);
            float f6;
            if(f2 > 0.0F)
                f6 = mInitialMotionX + (float)mTouchSlop;
            else
                f6 = mInitialMotionX - (float)mTouchSlop;
            mLastMotionX = f6;
            setScrollingCacheEnabled(true);
        } else
        if(f5 > (float)mTouchSlop)
            mIsUnableToDrag = true;
        if(mIsBeingDragged && performDrag(f1))
            ViewCompat.postInvalidateOnAnimation(this);
          goto _L7
_L2:
        float f = motionevent.getX();
        mInitialMotionX = f;
        mLastMotionX = f;
        mLastMotionY = motionevent.getY();
        mActivePointerId = MotionEventCompat.getPointerId(motionevent, 0);
        mIsUnableToDrag = false;
        mScroller.computeScrollOffset();
        if(mScrollState == 2 && Math.abs(mScroller.getFinalX() - mScroller.getCurrX()) > mCloseEnough) {
            mScroller.abortAnimation();
            mPopulatePending = false;
            populate();
            mIsBeingDragged = true;
            setScrollState(1);
        } else {
            completeScroll();
            mIsBeingDragged = false;
        }
          goto _L7
        onSecondaryPointerUp(motionevent);
          goto _L7
    }

    protected void onLayout(boolean flag, int i, int j, int k, int l) {
        int i1;
        int j1;
        int k1;
        int l1;
        int i2;
        int j2;
        int k2;
        int l2;
        int i3;
        int j3;
        mInLayout = true;
        populate();
        mInLayout = false;
        i1 = getChildCount();
        j1 = k - i;
        k1 = l - j;
        l1 = getPaddingLeft();
        i2 = getPaddingTop();
        j2 = getPaddingRight();
        k2 = getPaddingBottom();
        l2 = getScrollX();
        i3 = 0;
        j3 = 0;
_L17:
        if(j3 >= i1) goto _L2; else goto _L1
_L1:
        View view1 = getChildAt(j3);
        if(view1.getVisibility() == 8) goto _L4; else goto _L3
_L3:
        LayoutParams layoutparams1 = (LayoutParams)view1.getLayoutParams();
        if(!layoutparams1.isDecor) goto _L4; else goto _L5
_L5:
        int j4;
        int k4;
        j4 = 7 & layoutparams1.gravity;
        k4 = 0x70 & layoutparams1.gravity;
        j4;
        JVM INSTR tableswitch 1 5: default 168
    //                   1 270
    //                   2 168
    //                   3 253
    //                   4 168
    //                   5 290;
           goto _L6 _L7 _L6 _L8 _L6 _L9
_L6:
        int l4 = l1;
_L14:
        k4;
        JVM INSTR lookupswitch 3: default 208
    //                   16: 333
    //                   48: 316
    //                   80: 353;
           goto _L10 _L11 _L12 _L13
_L10:
        int i5 = i2;
_L15:
        int j5 = l4 + l2;
        view1.layout(j5, i5, j5 + view1.getMeasuredWidth(), i5 + view1.getMeasuredHeight());
        i3++;
_L4:
        j3++;
        continue; /* Loop/switch isn't completed */
_L8:
        l4 = l1;
        l1 += view1.getMeasuredWidth();
          goto _L14
_L7:
        l4 = Math.max((j1 - view1.getMeasuredWidth()) / 2, l1);
          goto _L14
_L9:
        l4 = j1 - j2 - view1.getMeasuredWidth();
        j2 += view1.getMeasuredWidth();
          goto _L14
_L12:
        i5 = i2;
        i2 += view1.getMeasuredHeight();
          goto _L15
_L11:
        i5 = Math.max((k1 - view1.getMeasuredHeight()) / 2, i2);
          goto _L15
_L13:
        i5 = k1 - k2 - view1.getMeasuredHeight();
        k2 += view1.getMeasuredHeight();
          goto _L15
_L2:
        for(int k3 = 0; k3 < i1; k3++) {
            View view = getChildAt(k3);
            if(view.getVisibility() == 8)
                continue;
            LayoutParams layoutparams = (LayoutParams)view.getLayoutParams();
            if(layoutparams.isDecor)
                continue;
            ItemInfo iteminfo = infoForChild(view);
            if(iteminfo == null)
                continue;
            int l3 = l1 + (int)((float)j1 * iteminfo.offset);
            int i4 = i2;
            if(layoutparams.needsMeasure) {
                layoutparams.needsMeasure = false;
                view.measure(android.view.View.MeasureSpec.makeMeasureSpec((int)((float)(j1 - l1 - j2) * layoutparams.widthFactor), 0x40000000), android.view.View.MeasureSpec.makeMeasureSpec(k1 - i2 - k2, 0x40000000));
            }
            view.layout(l3, i4, l3 + view.getMeasuredWidth(), i4 + view.getMeasuredHeight());
        }

        mTopPageBounds = i2;
        mBottomPageBounds = k1 - k2;
        mDecorChildCount = i3;
        mFirstLayout = false;
        return;
        if(true) goto _L17; else goto _L16
_L16:
    }

    protected void onMeasure(int i, int j) {
        setMeasuredDimension(getDefaultSize(0, i), getDefaultSize(0, j));
        int k = getMeasuredWidth();
        mGutterSize = Math.min(k / 10, mDefaultGutterSize);
        int l = k - getPaddingLeft() - getPaddingRight();
        int i1 = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int j1 = getChildCount();
        int k1 = 0;
        do {
            if(k1 < j1) {
                View view1 = getChildAt(k1);
                if(view1.getVisibility() != 8) {
                    LayoutParams layoutparams1 = (LayoutParams)view1.getLayoutParams();
                    if(layoutparams1 != null && layoutparams1.isDecor) {
                        int j2 = 7 & layoutparams1.gravity;
                        int k2 = 0x70 & layoutparams1.gravity;
                        int l2 = 0x80000000;
                        int i3 = 0x80000000;
                        boolean flag;
                        boolean flag1;
                        int j3;
                        int k3;
                        if(k2 == 48 || k2 == 80)
                            flag = true;
                        else
                            flag = false;
                        if(j2 == 3 || j2 == 5)
                            flag1 = true;
                        else
                            flag1 = false;
                        if(flag)
                            l2 = 0x40000000;
                        else
                        if(flag1)
                            i3 = 0x40000000;
                        j3 = l;
                        k3 = i1;
                        if(((android.view.ViewGroup.LayoutParams) (layoutparams1)).width != -2) {
                            l2 = 0x40000000;
                            if(((android.view.ViewGroup.LayoutParams) (layoutparams1)).width != -1)
                                j3 = ((android.view.ViewGroup.LayoutParams) (layoutparams1)).width;
                        }
                        if(((android.view.ViewGroup.LayoutParams) (layoutparams1)).height != -2) {
                            i3 = 0x40000000;
                            if(((android.view.ViewGroup.LayoutParams) (layoutparams1)).height != -1)
                                k3 = ((android.view.ViewGroup.LayoutParams) (layoutparams1)).height;
                        }
                        view1.measure(android.view.View.MeasureSpec.makeMeasureSpec(j3, l2), android.view.View.MeasureSpec.makeMeasureSpec(k3, i3));
                        if(flag)
                            i1 -= view1.getMeasuredHeight();
                        else
                        if(flag1)
                            l -= view1.getMeasuredWidth();
                    }
                }
                k1++;
                continue;
            }
            mChildWidthMeasureSpec = android.view.View.MeasureSpec.makeMeasureSpec(l, 0x40000000);
            mChildHeightMeasureSpec = android.view.View.MeasureSpec.makeMeasureSpec(i1, 0x40000000);
            mInLayout = true;
            populate();
            mInLayout = false;
            int l1 = getChildCount();
            for(int i2 = 0; i2 < l1; i2++) {
                View view = getChildAt(i2);
                if(view.getVisibility() == 8)
                    continue;
                LayoutParams layoutparams = (LayoutParams)view.getLayoutParams();
                if(layoutparams == null || !layoutparams.isDecor)
                    view.measure(android.view.View.MeasureSpec.makeMeasureSpec((int)((float)l * layoutparams.widthFactor), 0x40000000), mChildHeightMeasureSpec);
            }

            return;
        } while(true);
    }

    protected void onPageScrolled(int i, float f, int j) {
        int k;
        int l;
        int i1;
        int j1;
        int k1;
        int l1;
        if(mDecorChildCount <= 0)
            break MISSING_BLOCK_LABEL_215;
        k = getScrollX();
        l = getPaddingLeft();
        i1 = getPaddingRight();
        j1 = getWidth();
        k1 = getChildCount();
        l1 = 0;
_L2:
        View view;
        LayoutParams layoutparams;
        if(l1 >= k1)
            break MISSING_BLOCK_LABEL_215;
        view = getChildAt(l1);
        layoutparams = (LayoutParams)view.getLayoutParams();
        if(layoutparams.isDecor)
            break; /* Loop/switch isn't completed */
_L7:
        l1++;
        if(true) goto _L2; else goto _L1
_L1:
        7 & layoutparams.gravity;
        JVM INSTR tableswitch 1 5: default 120
    //                   1 169
    //                   2 120
    //                   3 152
    //                   4 120
    //                   5 189;
           goto _L3 _L4 _L3 _L5 _L3 _L6
_L6:
        break MISSING_BLOCK_LABEL_189;
_L3:
        int i2 = l;
_L8:
        int j2 = (i2 + k) - view.getLeft();
        if(j2 != 0)
            view.offsetLeftAndRight(j2);
          goto _L7
_L5:
        i2 = l;
        l += view.getWidth();
          goto _L8
_L4:
        i2 = Math.max((j1 - view.getMeasuredWidth()) / 2, l);
          goto _L8
        i2 = j1 - i1 - view.getMeasuredWidth();
        i1 += view.getMeasuredWidth();
          goto _L8
        if(mOnPageChangeListener != null)
            mOnPageChangeListener.onPageScrolled(i, f, j);
        if(mInternalPageChangeListener != null)
            mInternalPageChangeListener.onPageScrolled(i, f, j);
        mCalledSuper = true;
        return;
          goto _L7
    }

    protected boolean onRequestFocusInDescendants(int i, Rect rect) {
        byte byte0;
        int i1;
        boolean flag;
        int j = getChildCount();
        int k;
        int l;
        View view;
        ItemInfo iteminfo;
        if((i & 2) != 0) {
            k = 0;
            byte0 = 1;
            l = j;
        } else {
            k = j - 1;
            byte0 = -1;
            l = -1;
        }
        i1 = k;
_L3:
        if(i1 == l) goto _L2; else goto _L1
_L1:
        view = getChildAt(i1);
        if(view.getVisibility() != 0)
            continue; /* Loop/switch isn't completed */
        iteminfo = infoForChild(view);
        if(iteminfo == null || iteminfo.position != mCurItem || !view.requestFocus(i, rect))
            continue; /* Loop/switch isn't completed */
        flag = true;
_L4:
        return flag;
        i1 += byte0;
          goto _L3
_L2:
        flag = false;
          goto _L4
    }

    public void onRestoreInstanceState(Parcelable parcelable) {
        if(!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
        } else {
            SavedState savedstate = (SavedState)parcelable;
            super.onRestoreInstanceState(savedstate.getSuperState());
            if(mAdapter != null) {
                mAdapter.restoreState(savedstate.adapterState, savedstate.loader);
                setCurrentItemInternal(savedstate.position, false, true);
            } else {
                mRestoredCurItem = savedstate.position;
                mRestoredAdapterState = savedstate.adapterState;
                mRestoredClassLoader = savedstate.loader;
            }
        }
    }

    public Parcelable onSaveInstanceState() {
        SavedState savedstate = new SavedState(super.onSaveInstanceState());
        savedstate.position = mCurItem;
        if(mAdapter != null)
            savedstate.adapterState = mAdapter.saveState();
        return savedstate;
    }

    protected void onSizeChanged(int i, int j, int k, int l) {
        super.onSizeChanged(i, j, k, l);
        if(i != k)
            recomputeScrollPosition(i, k, mPageMargin, mPageMargin);
    }

    public boolean onTouchEvent(MotionEvent motionevent) {
        if(!mFakeDragging) goto _L2; else goto _L1
_L1:
        boolean flag = true;
_L11:
        return flag;
_L2:
        int i;
        boolean flag1;
        if(motionevent.getAction() == 0 && motionevent.getEdgeFlags() != 0) {
            flag = false;
            continue; /* Loop/switch isn't completed */
        }
        if(mAdapter == null || mAdapter.getCount() == 0) {
            flag = false;
            continue; /* Loop/switch isn't completed */
        }
        if(mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(motionevent);
        i = motionevent.getAction();
        flag1 = false;
        i & 0xff;
        JVM INSTR tableswitch 0 6: default 128
    //                   0 142
    //                   1 360
    //                   2 198
    //                   3 507
    //                   4 128
    //                   5 554
    //                   6 583;
           goto _L3 _L4 _L5 _L6 _L7 _L3 _L8 _L9
_L9:
        break MISSING_BLOCK_LABEL_583;
_L3:
        break; /* Loop/switch isn't completed */
_L4:
        break; /* Loop/switch isn't completed */
_L12:
        if(flag1)
            ViewCompat.postInvalidateOnAnimation(this);
        flag = true;
        if(true) goto _L11; else goto _L10
_L10:
        mScroller.abortAnimation();
        mPopulatePending = false;
        populate();
        mIsBeingDragged = true;
        setScrollState(1);
        float f4 = motionevent.getX();
        mInitialMotionX = f4;
        mLastMotionX = f4;
        mActivePointerId = MotionEventCompat.getPointerId(motionevent, 0);
          goto _L12
_L6:
        if(!mIsBeingDragged) {
            int j1 = MotionEventCompat.findPointerIndex(motionevent, mActivePointerId);
            float f = MotionEventCompat.getX(motionevent, j1);
            float f1 = Math.abs(f - mLastMotionX);
            float f2 = Math.abs(MotionEventCompat.getY(motionevent, j1) - mLastMotionY);
            if(f1 > (float)mTouchSlop && f1 > f2) {
                mIsBeingDragged = true;
                float f3;
                if(f - mInitialMotionX > 0.0F)
                    f3 = mInitialMotionX + (float)mTouchSlop;
                else
                    f3 = mInitialMotionX - (float)mTouchSlop;
                mLastMotionX = f3;
                setScrollState(1);
                setScrollingCacheEnabled(true);
            }
        }
        if(mIsBeingDragged)
            flag1 = false | performDrag(MotionEventCompat.getX(motionevent, MotionEventCompat.findPointerIndex(motionevent, mActivePointerId)));
          goto _L12
_L5:
        if(mIsBeingDragged) {
            VelocityTracker velocitytracker = mVelocityTracker;
            velocitytracker.computeCurrentVelocity(1000, mMaximumVelocity);
            int k = (int)VelocityTrackerCompat.getXVelocity(velocitytracker, mActivePointerId);
            mPopulatePending = true;
            int l = getWidth();
            int i1 = getScrollX();
            ItemInfo iteminfo = infoForCurrentScrollPosition();
            setCurrentItemInternal(determineTargetPage(iteminfo.position, ((float)i1 / (float)l - iteminfo.offset) / iteminfo.widthFactor, k, (int)(MotionEventCompat.getX(motionevent, MotionEventCompat.findPointerIndex(motionevent, mActivePointerId)) - mInitialMotionX)), true, true, k);
            mActivePointerId = -1;
            endDrag();
            flag1 = mLeftEdge.onRelease() | mRightEdge.onRelease();
        }
          goto _L12
_L7:
        if(mIsBeingDragged) {
            setCurrentItemInternal(mCurItem, true, true);
            mActivePointerId = -1;
            endDrag();
            flag1 = mLeftEdge.onRelease() | mRightEdge.onRelease();
        }
          goto _L12
_L8:
        int j = MotionEventCompat.getActionIndex(motionevent);
        mLastMotionX = MotionEventCompat.getX(motionevent, j);
        mActivePointerId = MotionEventCompat.getPointerId(motionevent, j);
          goto _L12
        onSecondaryPointerUp(motionevent);
        mLastMotionX = MotionEventCompat.getX(motionevent, MotionEventCompat.findPointerIndex(motionevent, mActivePointerId));
          goto _L12
    }

    boolean pageLeft() {
        boolean flag = true;
        if(mCurItem > 0)
            setCurrentItem(-1 + mCurItem, flag);
        else
            flag = false;
        return flag;
    }

    boolean pageRight() {
        boolean flag = true;
        if(mAdapter != null && mCurItem < -1 + mAdapter.getCount())
            setCurrentItem(1 + mCurItem, flag);
        else
            flag = false;
        return flag;
    }

    void populate() {
        populate(mCurItem);
    }

    void populate(int i) {
        ItemInfo iteminfo;
        iteminfo = null;
        if(mCurItem != i) {
            iteminfo = infoForPosition(mCurItem);
            mCurItem = i;
        }
        break MISSING_BLOCK_LABEL_24;
_L24:
        int k;
        int l;
        int i1;
        ItemInfo iteminfo1;
        int j1;
        do
            return;
        while(mAdapter == null || mPopulatePending || getWindowToken() == null);
        mAdapter.startUpdate(this);
        int j = mOffscreenPageLimit;
        k = Math.max(0, mCurItem - j);
        l = mAdapter.getCount();
        i1 = Math.min(l - 1, j + mCurItem);
        iteminfo1 = null;
        j1 = 0;
_L18:
        if(j1 >= mItems.size()) goto _L2; else goto _L1
_L1:
        ItemInfo iteminfo8 = (ItemInfo)mItems.get(j1);
        if(iteminfo8.position < mCurItem) goto _L4; else goto _L3
_L3:
        if(iteminfo8.position == mCurItem)
            iteminfo1 = iteminfo8;
_L2:
        if(iteminfo1 == null && l > 0)
            iteminfo1 = addNewItem(mCurItem, j1);
        if(iteminfo1 == null) goto _L6; else goto _L5
_L5:
        float f;
        int k2;
        ItemInfo iteminfo5;
        int l2;
        float f2;
        int i3;
        ItemInfo iteminfo6;
        int j3;
        f = 0.0F;
        k2 = j1 - 1;
        PagerAdapter pageradapter;
        int k1;
        int l1;
        int i2;
        View view2;
        LayoutParams layoutparams;
        ItemInfo iteminfo4;
        float f1;
        if(k2 >= 0)
            iteminfo5 = (ItemInfo)mItems.get(k2);
        else
            iteminfo5 = null;
        f1 = 2.0F - iteminfo1.widthFactor;
        l2 = -1 + mCurItem;
_L19:
        if(l2 < 0) goto _L8; else goto _L7
_L7:
        if(f < f1 || l2 >= k) goto _L10; else goto _L9
_L9:
        if(iteminfo5 != null) goto _L11; else goto _L8
_L8:
        f2 = iteminfo1.widthFactor;
        i3 = j1 + 1;
        if(f2 >= 2.0F) goto _L13; else goto _L12
_L12:
        int i4;
        int j4;
        int k4;
        PagerAdapter pageradapter2;
        Object obj2;
        if(i3 < mItems.size())
            iteminfo6 = (ItemInfo)mItems.get(i3);
        else
            iteminfo6 = null;
        j3 = 1 + mCurItem;
_L21:
        if(j3 >= l) goto _L13; else goto _L14
_L14:
        if(f2 < 2.0F || j3 <= i1) goto _L16; else goto _L15
_L15:
        if(iteminfo6 != null) goto _L17; else goto _L13
_L13:
        calculatePageOffsets(iteminfo1, j1, iteminfo);
_L6:
        pageradapter = mAdapter;
        k1 = mCurItem;
        Object obj;
        ItemInfo iteminfo7;
        int k3;
        int l3;
        PagerAdapter pageradapter1;
        Object obj1;
        if(iteminfo1 != null)
            obj = iteminfo1.object;
        else
            obj = null;
        pageradapter.setPrimaryItem(this, k1, obj);
        mAdapter.finishUpdate(this);
        l1 = getChildCount();
        for(i2 = 0; i2 < l1; i2++) {
            view2 = getChildAt(i2);
            layoutparams = (LayoutParams)view2.getLayoutParams();
            if(!layoutparams.isDecor && layoutparams.widthFactor == 0.0F) {
                iteminfo4 = infoForChild(view2);
                if(iteminfo4 != null)
                    layoutparams.widthFactor = iteminfo4.widthFactor;
            }
        }

        continue; /* Loop/switch isn't completed */
_L4:
        j1++;
          goto _L18
_L11:
        k4 = iteminfo5.position;
        if(l2 == k4 && !iteminfo5.scrolling) {
            mItems.remove(k2);
            pageradapter2 = mAdapter;
            obj2 = iteminfo5.object;
            pageradapter2.destroyItem(this, l2, obj2);
            k2--;
            j1--;
            if(k2 >= 0)
                iteminfo5 = (ItemInfo)mItems.get(k2);
            else
                iteminfo5 = null;
        }
_L20:
        l2--;
          goto _L19
_L10:
label0:
        {
            if(iteminfo5 == null)
                break label0;
            j4 = iteminfo5.position;
            if(l2 != j4)
                break label0;
            f += iteminfo5.widthFactor;
            if(--k2 >= 0)
                iteminfo5 = (ItemInfo)mItems.get(k2);
            else
                iteminfo5 = null;
        }
          goto _L20
        i4 = k2 + 1;
        f += addNewItem(l2, i4).widthFactor;
        j1++;
        if(k2 >= 0)
            iteminfo5 = (ItemInfo)mItems.get(k2);
        else
            iteminfo5 = null;
          goto _L20
_L17:
        l3 = iteminfo6.position;
        if(j3 == l3 && !iteminfo6.scrolling) {
            mItems.remove(i3);
            pageradapter1 = mAdapter;
            obj1 = iteminfo6.object;
            pageradapter1.destroyItem(this, j3, obj1);
            if(i3 < mItems.size())
                iteminfo6 = (ItemInfo)mItems.get(i3);
            else
                iteminfo6 = null;
        }
_L22:
        j3++;
          goto _L21
_L16:
label1:
        {
            if(iteminfo6 == null)
                break label1;
            k3 = iteminfo6.position;
            if(j3 != k3)
                break label1;
            f2 += iteminfo6.widthFactor;
            if(++i3 < mItems.size())
                iteminfo6 = (ItemInfo)mItems.get(i3);
            else
                iteminfo6 = null;
        }
          goto _L22
        iteminfo7 = addNewItem(j3, i3);
        i3++;
        f2 += iteminfo7.widthFactor;
        if(i3 < mItems.size())
            iteminfo6 = (ItemInfo)mItems.get(i3);
        else
            iteminfo6 = null;
          goto _L22
        if(!hasFocus()) goto _L24; else goto _L23
_L23:
        View view = findFocus();
        ItemInfo iteminfo2;
        int j2;
        View view1;
        ItemInfo iteminfo3;
        if(view != null)
            iteminfo2 = infoForAnyChild(view);
        else
            iteminfo2 = null;
        if(iteminfo2 != null && iteminfo2.position == mCurItem) goto _L24; else goto _L25
_L25:
        j2 = 0;
_L28:
        if(j2 >= getChildCount()) goto _L24; else goto _L26
_L26:
        view1 = getChildAt(j2);
        iteminfo3 = infoForChild(view1);
        if(iteminfo3 != null && iteminfo3.position == mCurItem && view1.requestFocus(2)) goto _L24; else goto _L27
_L27:
        j2++;
          goto _L28
    }

    public void setAdapter(PagerAdapter pageradapter) {
        if(mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
            mAdapter.startUpdate(this);
            for(int i = 0; i < mItems.size(); i++) {
                ItemInfo iteminfo = (ItemInfo)mItems.get(i);
                mAdapter.destroyItem(this, iteminfo.position, iteminfo.object);
            }

            mAdapter.finishUpdate(this);
            mItems.clear();
            removeNonDecorViews();
            mCurItem = 0;
            scrollTo(0, 0);
        }
        PagerAdapter pageradapter1 = mAdapter;
        mAdapter = pageradapter;
        if(mAdapter != null) {
            if(mObserver == null)
                mObserver = new PagerObserver();
            mAdapter.registerDataSetObserver(mObserver);
            mPopulatePending = false;
            mFirstLayout = true;
            if(mRestoredCurItem >= 0) {
                mAdapter.restoreState(mRestoredAdapterState, mRestoredClassLoader);
                setCurrentItemInternal(mRestoredCurItem, false, true);
                mRestoredCurItem = -1;
                mRestoredAdapterState = null;
                mRestoredClassLoader = null;
            } else {
                populate();
            }
        }
        if(mAdapterChangeListener != null && pageradapter1 != pageradapter)
            mAdapterChangeListener.onAdapterChanged(pageradapter1, pageradapter);
    }

    public void setCurrentItem(int i) {
        mPopulatePending = false;
        boolean flag;
        if(!mFirstLayout)
            flag = true;
        else
            flag = false;
        setCurrentItemInternal(i, flag, false);
    }

    public void setCurrentItem(int i, boolean flag) {
        mPopulatePending = false;
        setCurrentItemInternal(i, flag, false);
    }

    void setCurrentItemInternal(int i, boolean flag, boolean flag1) {
        setCurrentItemInternal(i, flag, flag1, 0);
    }

    void setCurrentItemInternal(int i, boolean flag, boolean flag1, int j) {
        boolean flag2 = true;
        if(mAdapter != null && mAdapter.getCount() > 0) goto _L2; else goto _L1
_L1:
        setScrollingCacheEnabled(false);
_L8:
        return;
_L2:
        if(!flag1 && mCurItem == i && mItems.size() != 0) {
            setScrollingCacheEnabled(false);
            continue; /* Loop/switch isn't completed */
        }
        if(i >= 0) goto _L4; else goto _L3
_L3:
        i = 0;
_L6:
        int k = mOffscreenPageLimit;
        if(i > k + mCurItem || i < mCurItem - k) {
            for(int l = 0; l < mItems.size(); l++)
                ((ItemInfo)mItems.get(l)).scrolling = flag2;

        }
        break; /* Loop/switch isn't completed */
_L4:
        if(i >= mAdapter.getCount())
            i = -1 + mAdapter.getCount();
        if(true) goto _L6; else goto _L5
_L5:
        ItemInfo iteminfo;
        int i1;
        if(mCurItem == i)
            flag2 = false;
        populate(i);
        iteminfo = infoForPosition(i);
        i1 = 0;
        if(iteminfo != null)
            i1 = (int)((float)getWidth() * Math.max(mFirstOffset, Math.min(iteminfo.offset, mLastOffset)));
        if(flag) {
            smoothScrollTo(i1, 0, j);
            if(flag2 && mOnPageChangeListener != null)
                mOnPageChangeListener.onPageSelected(i);
            if(flag2 && mInternalPageChangeListener != null)
                mInternalPageChangeListener.onPageSelected(i);
        } else {
            if(flag2 && mOnPageChangeListener != null)
                mOnPageChangeListener.onPageSelected(i);
            if(flag2 && mInternalPageChangeListener != null)
                mInternalPageChangeListener.onPageSelected(i);
            completeScroll();
            scrollTo(i1, 0);
        }
        if(true) goto _L8; else goto _L7
_L7:
    }

    OnPageChangeListener setInternalPageChangeListener(OnPageChangeListener onpagechangelistener) {
        OnPageChangeListener onpagechangelistener1 = mInternalPageChangeListener;
        mInternalPageChangeListener = onpagechangelistener;
        return onpagechangelistener1;
    }

    public void setOffscreenPageLimit(int i) {
        if(i < 1) {
            Log.w("ViewPager", (new StringBuilder()).append("Requested offscreen page limit ").append(i).append(" too small; defaulting to ").append(1).toString());
            i = 1;
        }
        if(i != mOffscreenPageLimit) {
            mOffscreenPageLimit = i;
            populate();
        }
    }

    void setOnAdapterChangeListener(OnAdapterChangeListener onadapterchangelistener) {
        mAdapterChangeListener = onadapterchangelistener;
    }

    public void setOnPageChangeListener(OnPageChangeListener onpagechangelistener) {
        mOnPageChangeListener = onpagechangelistener;
    }

    public void setPageMargin(int i) {
        int j = mPageMargin;
        mPageMargin = i;
        int k = getWidth();
        recomputeScrollPosition(k, k, i, j);
        requestLayout();
    }

    public void setPageMarginDrawable(int i) {
        setPageMarginDrawable(getContext().getResources().getDrawable(i));
    }

    public void setPageMarginDrawable(Drawable drawable) {
        mMarginDrawable = drawable;
        if(drawable != null)
            refreshDrawableState();
        boolean flag;
        if(drawable == null)
            flag = true;
        else
            flag = false;
        setWillNotDraw(flag);
        invalidate();
    }

    void smoothScrollTo(int i, int j) {
        smoothScrollTo(i, j, 0);
    }

    void smoothScrollTo(int i, int j, int k) {
        if(getChildCount() == 0) {
            setScrollingCacheEnabled(false);
        } else {
            int l = getScrollX();
            int i1 = getScrollY();
            int j1 = i - l;
            int k1 = j - i1;
            if(j1 == 0 && k1 == 0) {
                completeScroll();
                populate();
                setScrollState(0);
            } else {
                setScrollingCacheEnabled(true);
                setScrollState(2);
                int l1 = getWidth();
                int i2 = l1 / 2;
                float f = Math.min(1.0F, (1.0F * (float)Math.abs(j1)) / (float)l1);
                float f1 = (float)i2 + (float)i2 * distanceInfluenceForSnapDuration(f);
                int j2 = Math.abs(k);
                int k2;
                int l2;
                if(j2 > 0) {
                    k2 = 4 * Math.round(1000F * Math.abs(f1 / (float)j2));
                } else {
                    float f2 = (float)l1 * mAdapter.getPageWidth(mCurItem);
                    k2 = (int)(100F * (1.0F + (float)Math.abs(j1) / (f2 + (float)mPageMargin)));
                }
                l2 = Math.min(k2, 600);
                mScroller.startScroll(l, i1, j1, k1, l2);
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }

    protected boolean verifyDrawable(Drawable drawable) {
        boolean flag;
        if(super.verifyDrawable(drawable) || drawable == mMarginDrawable)
            flag = true;
        else
            flag = false;
        return flag;
    }

    private static final int CLOSE_ENOUGH = 2;
    private static final Comparator COMPARATOR = new Comparator() {

        public int compare(ItemInfo iteminfo, ItemInfo iteminfo1) {
            return iteminfo.position - iteminfo1.position;
        }

        public volatile int compare(Object obj, Object obj1) {
            return compare((ItemInfo)obj, (ItemInfo)obj1);
        }

    };
    private static final boolean DEBUG = false;
    private static final int DEFAULT_GUTTER_SIZE = 16;
    private static final int DEFAULT_OFFSCREEN_PAGES = 1;
    private static final int INVALID_POINTER = -1;
    private static final int LAYOUT_ATTRS[];
    private static final int MAX_SETTLE_DURATION = 600;
    private static final int MIN_DISTANCE_FOR_FLING = 25;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_SETTLING = 2;
    private static final String TAG = "ViewPager";
    private static final boolean USE_CACHE;
    private static final Interpolator sInterpolator = new Interpolator() {

        public float getInterpolation(float f) {
            float f1 = f - 1.0F;
            return 1.0F + f1 * (f1 * (f1 * (f1 * f1)));
        }

    };
    private int mActivePointerId;
    private PagerAdapter mAdapter;
    private OnAdapterChangeListener mAdapterChangeListener;
    private int mBottomPageBounds;
    private boolean mCalledSuper;
    private int mChildHeightMeasureSpec;
    private int mChildWidthMeasureSpec;
    private int mCloseEnough;
    private int mCurItem;
    private int mDecorChildCount;
    private int mDefaultGutterSize;
    private long mFakeDragBeginTime;
    private boolean mFakeDragging;
    private boolean mFirstLayout;
    private float mFirstOffset;
    private int mFlingDistance;
    private int mGutterSize;
    private boolean mIgnoreGutter;
    private boolean mInLayout;
    private float mInitialMotionX;
    private OnPageChangeListener mInternalPageChangeListener;
    private boolean mIsBeingDragged;
    private boolean mIsUnableToDrag;
    private final ArrayList mItems;
    private float mLastMotionX;
    private float mLastMotionY;
    private float mLastOffset;
    private EdgeEffectCompat mLeftEdge;
    private Drawable mMarginDrawable;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private boolean mNeedCalculatePageOffsets;
    private PagerObserver mObserver;
    private int mOffscreenPageLimit;
    private OnPageChangeListener mOnPageChangeListener;
    private int mPageMargin;
    private boolean mPopulatePending;
    private Parcelable mRestoredAdapterState;
    private ClassLoader mRestoredClassLoader;
    private int mRestoredCurItem;
    private EdgeEffectCompat mRightEdge;
    private int mScrollState;
    private Scroller mScroller;
    private boolean mScrollingCacheEnabled;
    private final ItemInfo mTempItem;
    private final Rect mTempRect;
    private int mTopPageBounds;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;

    static  {
        int ai[] = new int[1];
        ai[0] = 0x10100b3;
        LAYOUT_ATTRS = ai;
    }



}

