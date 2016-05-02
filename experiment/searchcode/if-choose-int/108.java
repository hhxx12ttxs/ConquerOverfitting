/**
 * Created by IntelliJ IDEA.
 * User: AnderWeb
 * Date: 25/03/11
 * Time: 14:35
 */


package com.javielinux.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;
import android.widget.Scroller;

public class ImageViewZoomTouch extends ImageView {
    private boolean initializing = true;

    // This is the base transformation which is used to show the image
    // initially.  The current computation for this shows the image in
    // it's entirety, letterboxing as needed.  One could choose to
    // show the image as cropped instead.
    //
    // This matrix is recomputed when we go from the thumbnail image to
    // the full size image.
    protected Matrix mBaseMatrix = new Matrix();

    // This is the supplementary transformation which reflects what
    // the user has done in terms of zooming and panning.
    //
    // This matrix remains the same when we go from the thumbnail image
    // to the full size image.
    protected Matrix mSuppMatrix = new Matrix();

    // This is the final matrix which is computed as the concatentation
    // of the base matrix and the supplementary matrix.
    private final Matrix mDisplayMatrix = new Matrix();

    // Temporary buffer used for getting the values out of a matrix.
    private final float[] mMatrixValues = new float[9];

    int mThisWidth = -1, mThisHeight = -1;

    float mMaxZoom;
    private GestureDetector mGestureDetector;
    private VersionedGestureDetector mScaleDetector;

    @SuppressWarnings("unused")
    private Scroller mScroller;
    private int mPrevX;
    private int mPrevY;

    @Override
    protected void onLayout(boolean changed, int left, int top,
                            int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mThisWidth = right - left;
        mThisHeight = bottom - top;
        if (initializing) {
            post(new Runnable() {
                public void run() {
                    setImageRotateBitmapResetBase(true);
                }
            });
            initializing = false;
        }
        Drawable d = getDrawable();
        if (d != null) {
            getProperBaseMatrix(d, mBaseMatrix);
            setImageMatrix(getImageViewMatrix());
        }
    }

    @Override
    public void setImageDrawable(Drawable d) {
        super.setImageDrawable(d);
        setImageRotateBitmapResetBase(true);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        setImageRotateBitmapResetBase(true);
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        setImageRotateBitmapResetBase(true);
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        setImageRotateBitmapResetBase(true);
    }

    public void setImageRotateBitmapResetBase(final boolean resetSupp) {
        final int viewWidth = getWidth();
        Drawable drawable = getDrawable();
        if (viewWidth <= 0 || initializing) {
            return;
        }
        mBaseMatrix.reset();
        if (drawable != null) {
            getProperBaseMatrix(drawable, mBaseMatrix);
            super.setImageDrawable(drawable);
        } else {
            mBaseMatrix.reset();
            super.setImageDrawable(null);
        }

        if (resetSupp) {
            mSuppMatrix.reset();
        }
        setImageMatrix(getImageViewMatrix());
        mMaxZoom = maxZoom();
    }

    // Center as much as possible in one or both axis.  Centering is
    // defined as follows:  if the image is scaled down below the
    // view's dimensions then center it (literally).  If the image
    // is scaled larger than the view and is translated out of view
    // then translate it back into view (i.e. eliminate black bars).
    protected void center(boolean horizontal, boolean vertical) {

        Matrix m = getImageViewMatrix();
        Drawable d = getDrawable();

        if (d != null) {

            RectF rect = new RectF(0, 0,
                    d.getIntrinsicWidth(),
                    d.getIntrinsicHeight());

            m.mapRect(rect);

            float height = rect.height();
            float width = rect.width();

            float deltaX = 0, deltaY = 0;

            if (vertical) {
                int viewHeight = getHeight();
                if (height < viewHeight) {
                    deltaY = (viewHeight - height) / 2 - rect.top;
                } else if (rect.top > 0) {
                    deltaY = -rect.top;
                } else if (rect.bottom < viewHeight) {
                    deltaY = getHeight() - rect.bottom;
                }
            }

            if (horizontal) {
                int viewWidth = getWidth();
                if (width < viewWidth) {
                    deltaX = (viewWidth - width) / 2 - rect.left;
                } else if (rect.left > 0) {
                    deltaX = -rect.left;
                } else if (rect.right < viewWidth) {
                    deltaX = viewWidth - rect.right;
                }
            }

            postTranslate(deltaX, deltaY);
            setImageMatrix(getImageViewMatrix());
        }
    }

    public ImageViewZoomTouch(Context context) {
        super(context);
        init();
    }

    public ImageViewZoomTouch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageViewZoomTouch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setScaleType(ImageView.ScaleType.MATRIX);
        mGestureDetector = new GestureDetector(getContext(), new TapListener());
        mScaleDetector = VersionedGestureDetector.newInstance(getContext(), new ScaleListener());
        mScroller = new Scroller(getContext());
    }

    protected float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    // Get the scale factor out of the matrix.
    protected float getScale(Matrix matrix) {
        return getValue(matrix, Matrix.MSCALE_X);
    }

    protected float getScale() {
        return getScale(mSuppMatrix);
    }

    // Setup the base matrix so that the image is centered and scaled properly.
    private void getProperBaseMatrix(Drawable drawable, Matrix matrix) {
        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float w = drawable.getIntrinsicWidth();
        float h = drawable.getIntrinsicHeight();
        matrix.reset();

        // We limit up-scaling to 3x otherwise the result may look bad if it's
        // a small icon.
        float widthScale = Math.min(viewWidth / w, 3.0f);
        float heightScale = Math.min(viewHeight / h, 3.0f);
        float scale = Math.min(widthScale, heightScale);

        //matrix.postConcat(bitmap.getRotateMatrix());
        matrix.postScale(scale, scale);

        matrix.postTranslate(
                (viewWidth - w * scale) / 2F,
                (viewHeight - h * scale) / 2F);
    }

    // Combine the base matrix and the supp matrix to make the final matrix.
    protected Matrix getImageViewMatrix() {
        // The final matrix is computed as the concatentation of the base matrix
        // and the supplementary matrix.
        mDisplayMatrix.set(mBaseMatrix);
        mDisplayMatrix.postConcat(mSuppMatrix);
        return mDisplayMatrix;
    }

    static final float SCALE_RATE = 1.25F;

    // Sets the maximum zoom, which is a scale relative to the base matrix. It
    // is calculated to show the image at 400% zoom regardless of screen or
    // image orientation. If in the future we decode the full 3 megapixel image,
    // rather than the current 1024x768, this should be changed down to 200%.
    protected float maxZoom() {
        Drawable d = getDrawable();
        if (d == null) {
            return 1F;
        }

        float fw = (float) d.getIntrinsicWidth() / (float) mThisWidth;
        float fh = (float) d.getIntrinsicHeight() / (float) mThisHeight;
        return Math.max(fw, fh) * 2;
    }

    protected void zoomTo(float scale, float centerX, float centerY) {
        if (scale > mMaxZoom) {
            scale = mMaxZoom;
        }
        if (scale < 1F) scale = 1F;
        float oldScale = getScale();
        float deltaScale = scale / oldScale;

        mSuppMatrix.postScale(deltaScale, deltaScale, centerX, centerY);
        setImageMatrix(getImageViewMatrix());
        center(true, true);
    }

    public void zoomTo(float scale) {
        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        zoomTo(scale, cx, cy);
    }

    protected void zoomToPoint(float scale, float pointX, float pointY) {
        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        panBy(cx - pointX, cy - pointY);
        zoomTo(scale, cx, cy);
    }

    public void zoomIn() {
        zoomIn(SCALE_RATE);
    }

    public void zoomOut() {
        zoomOut(SCALE_RATE);
    }

    protected void zoomIn(float rate) {
        if (getScale() >= mMaxZoom) {
            return;     // Don't let the user zoom into the molecular level.
        }
        Drawable d = getDrawable();
        if (d == null) {
            return;
        }

        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        mSuppMatrix.postScale(rate, rate, cx, cy);
        setImageMatrix(getImageViewMatrix());
    }

    protected void zoomOut(float rate) {
        Drawable d = getDrawable();
        if (d == null) {
            return;
        }

        float cx = getWidth() / 2F;
        float cy = getHeight() / 2F;

        // Zoom out to at most 1x.
        Matrix tmp = new Matrix(mSuppMatrix);
        tmp.postScale(1F / rate, 1F / rate, cx, cy);

        if (getScale(tmp) < 1F) {
            mSuppMatrix.setScale(1F, 1F, cx, cy);
        } else {
            mSuppMatrix.postScale(1F / rate, 1F / rate, cx, cy);
        }
        setImageMatrix(getImageViewMatrix());
        center(true, true);
    }

    protected void postTranslate(float dx, float dy) {
        mSuppMatrix.postTranslate(dx, dy);
    }

    protected void panBy(float dx, float dy) {
        postTranslate(dx, dy);
        setImageMatrix(getImageViewMatrix());
    }


    protected void postTranslateCenter(float dx, float dy) {
        postTranslate(dx, dy);
        center(true, true);
    }

    private class TapListener extends
            GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            return performClick();
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mPrevX = mScroller.getCurrX();
            mPrevY = mScroller.getCurrY();
            mScroller.fling(mPrevX, mPrevY, -(int) (velocityX), -(int) (velocityY), Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            postInvalidate();
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Switch between the original scale and 3x scale.
            if (getScale() > 2F) {
                zoomTo(1f);
            } else {
                zoomToPoint(mMaxZoom, e.getX(), e.getY());
            }
            return true;
        }
    }

    private class ScaleListener implements VersionedGestureDetector.OnGestureListener {
        public void onDrag(float dx, float dy) {
            if (getScale() > 1F) {
                postTranslateCenter(dx, dy);
            }
        }

        public void onScale(float scaleFactor) {
            float s = getScale() * scaleFactor;
            // Don't let the object get too small or too large.
            s = Math.max(0.1f, Math.min(s, 5.0f));
            zoomTo(s);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //return super.onTouchEvent(event);
        mScroller.forceFinished(true);
        if (!mGestureDetector.onTouchEvent(event))
            mScaleDetector.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mScroller.computeScrollOffset()) {
            postTranslateCenter(mPrevX - mScroller.getCurrX(), mPrevY - mScroller.getCurrY());
            mPrevX = mScroller.getCurrX();
            mPrevY = mScroller.getCurrY();
            postInvalidate();
        }

        super.onDraw(canvas);
    }


    /*********
     * Gesture detector
     */
    private static abstract class VersionedGestureDetector {
        OnGestureListener mListener;

        public static VersionedGestureDetector newInstance(Context context,
                OnGestureListener listener) {
            final int sdkVersion = Build.VERSION.SDK_INT;
            VersionedGestureDetector detector;
            if (sdkVersion < 5) {
                detector = new CupcakeDetector();
            } else if (sdkVersion < 8) {
                detector = new EclairDetector();
            } else {
                detector = new FroyoDetector(context);
            }

            detector.mListener = listener;

            return detector;
        }

        public abstract boolean onTouchEvent(MotionEvent ev);

        public interface OnGestureListener {
            public void onDrag(float dx, float dy);
            public void onScale(float scaleFactor);
        }

        private static class CupcakeDetector extends VersionedGestureDetector {
            float mLastTouchX;
            float mLastTouchY;

            float getActiveX(MotionEvent ev) {
                return ev.getX();
            }

            float getActiveY(MotionEvent ev) {
                return ev.getY();
            }

            boolean shouldDrag() {
                return true;
            }

            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    mLastTouchX = getActiveX(ev);
                    mLastTouchY = getActiveY(ev);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    final float x = getActiveX(ev);
                    final float y = getActiveY(ev);

                    if (shouldDrag()) {
                        mListener.onDrag(x - mLastTouchX, y - mLastTouchY);
                    }

                    mLastTouchX = x;
                    mLastTouchY = y;
                    break;
                }
                }
                return true;
            }
        }

        private static class EclairDetector extends CupcakeDetector {
            private static final int INVALID_POINTER_ID = -1;
            private int mActivePointerId = INVALID_POINTER_ID;
            private int mActivePointerIndex = 0;

            @Override
            float getActiveX(MotionEvent ev) {
                return ev.getX(mActivePointerIndex);
            }

            @Override
            float getActiveY(MotionEvent ev) {
                return ev.getY(mActivePointerIndex);
            }

            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                final int action = ev.getAction();
                switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mActivePointerId = ev.getPointerId(0);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mActivePointerId = INVALID_POINTER_ID;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                            >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    final int pointerId = ev.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mActivePointerId = ev.getPointerId(newPointerIndex);
                        mLastTouchX = ev.getX(newPointerIndex);
                        mLastTouchY = ev.getY(newPointerIndex);
                    }
                    break;
                }

                mActivePointerIndex = ev.findPointerIndex(mActivePointerId);
                return super.onTouchEvent(ev);
            }
        }

        private static class FroyoDetector extends EclairDetector {
            private ScaleGestureDetector mDetector;

            public FroyoDetector(Context context) {
                mDetector = new ScaleGestureDetector(context,
                        new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override public boolean onScale(ScaleGestureDetector detector) {
                        mListener.onScale(detector.getScaleFactor());
                        return true;
                    }
                });
            }

            @Override
            boolean shouldDrag() {
                return !mDetector.isInProgress();
            }

            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                mDetector.onTouchEvent(ev);
                return super.onTouchEvent(ev);
            }
        }
    }
}
