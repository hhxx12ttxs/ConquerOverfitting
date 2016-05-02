/**************************************************************************/
/* MaclawStudios Camera App for Samsung Galaxy Ace and Gio                */
/* Copyright (C) 2012 Pavel Kirpichyov & Marcin Chojnacki & MaclawStudios */
/*                                                                        */
/* This program is free software: you can redistribute it and/or modify   */
/* it under the terms of the GNU General Public License as published by   */
/* the Free Software Foundation, either version 3 of the License, or      */
/* (at your option) any later version.                                    */
/*                                                                        */
/* This program is distributed in the hope that it will be useful,        */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of         */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the           */
/* GNU General Public License for more details.                           */
/*                                                                        */
/* You should have received a copy of the GNU General Public License      */
/* along with this program.  If not, see <http://www.gnu.org/licenses/>   */
/**************************************************************************/

package com.galaxyics.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * A @{code ImageView} which can rotate it's content.
 */
public class RotateImageView extends TwoStateImageView implements Rotatable {

	@SuppressWarnings("unused")
	private static final String TAG = "RotateImageView";

	private static final int ANIMATION_SPEED = 270; // 270 deg/sec

	private int mCurrentDegree = 0; // [0, 359]
	private int mStartDegree = 0;
	private int mTargetDegree = 0;

	private boolean mClockwise = false, mEnableAnimation = true;

	private long mAnimationStartTime = 0;
	private long mAnimationEndTime = 0;

	public RotateImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RotateImageView(Context context) {
		super(context);
	}

	public void enableAnimation(boolean enable) {
		mEnableAnimation = enable;
	}

	protected int getDegree() {
		return mTargetDegree;
	}

	// Rotate the view counter-clockwise
	public void setOrientation(int degree) {
		// make sure in the range of [0, 359]
		degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
		if (degree == mTargetDegree)
			return;

		mTargetDegree = degree;
		mStartDegree = mCurrentDegree;
		mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();

		int diff = mTargetDegree - mCurrentDegree;
		diff = diff >= 0 ? diff : 360 + diff; // make it in range [0, 359]

		// Make it in range [-179, 180]. That's the shorted distance between the
		// two angles
		diff = diff > 180 ? diff - 360 : diff;

		mClockwise = diff >= 0;
		mAnimationEndTime = mAnimationStartTime + Math.abs(diff) * 1000 / ANIMATION_SPEED;

		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable drawable = getDrawable();
		if (drawable == null)
			return;

		Rect bounds = drawable.getBounds();
		int w = bounds.right - bounds.left;
		int h = bounds.bottom - bounds.top;

		if (w == 0 || h == 0)
			return; // nothing to draw

		if (mCurrentDegree != mTargetDegree) {
			long time = AnimationUtils.currentAnimationTimeMillis();
			if (time < mAnimationEndTime) {
				int deltaTime = (int) (time - mAnimationStartTime);
				int degree = mStartDegree + ANIMATION_SPEED * (mClockwise ? deltaTime : -deltaTime) / 1000;
				degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
				mCurrentDegree = degree;
				invalidate();
			} else {
				mCurrentDegree = mTargetDegree;
			}
		}

		int left = getPaddingLeft();
		int top = getPaddingTop();
		int right = getPaddingRight();
		int bottom = getPaddingBottom();
		int width = getWidth() - left - right;
		int height = getHeight() - top - bottom;

		int saveCount = canvas.getSaveCount();

		// Scale down the image first if required.
		if ((getScaleType() == ImageView.ScaleType.FIT_CENTER) && ((width < w) || (height < h))) {
			float ratio = Math.min((float) width / w, (float) height / h);
			canvas.scale(ratio, ratio, width / 2.0f, height / 2.0f);
		}
		canvas.translate(left + width / 2, top + height / 2);
		canvas.rotate(-mCurrentDegree);
		canvas.translate(-w / 2, -h / 2);
		drawable.draw(canvas);
		canvas.restoreToCount(saveCount);
	}

	private Bitmap mThumb;
	private Drawable[] mThumbs;
	private TransitionDrawable mThumbTransition;

	public void setBitmap(Bitmap bitmap) {
		// Make sure uri and original are consistently both null or both
		// non-null.
		if (bitmap == null) {
			mThumb = null;
			mThumbs = null;
			setImageDrawable(null);
			setVisibility(GONE);
			return;
		}

		/*
		 * LayoutParams param = getLayoutParams(); final int miniThumbWidth =
		 * param.width - getPaddingLeft() - getPaddingRight(); final int
		 * miniThumbHeight = param.height - getPaddingTop() -
		 * getPaddingBottom();
		 */
		final int miniThumbWidth = 50; // temporary hardcoded
		final int miniThumbHeight = 50;

		mThumb = ThumbnailUtils.extractThumbnail(bitmap, miniThumbWidth, miniThumbHeight);
		Drawable drawable;
		if (mThumbs == null || !mEnableAnimation) {
			mThumbs = new Drawable[2];
			mThumbs[1] = new BitmapDrawable(getContext().getResources(), mThumb);
			setImageDrawable(mThumbs[1]);
		} else {
			mThumbs[0] = mThumbs[1];
			mThumbs[1] = new BitmapDrawable(getContext().getResources(), mThumb);
			mThumbTransition = new TransitionDrawable(mThumbs);
			setImageDrawable(mThumbTransition);
			mThumbTransition.startTransition(500);
		}
		setVisibility(VISIBLE);
	}
}
