/*
 * 
 * MahjongLib
 * 
 * Copyright Š 2009-2012 United States Government as represented by 
 * the Chief Information Officer of the National Center for Telehealth 
 * and Technology. All Rights Reserved.
 * 
 * Copyright Š 2009-2012 Contributors. All Rights Reserved. 
 * 
 * THIS OPEN SOURCE AGREEMENT ("AGREEMENT") DEFINES THE RIGHTS OF USE, 
 * REPRODUCTION, DISTRIBUTION, MODIFICATION AND REDISTRIBUTION OF CERTAIN 
 * COMPUTER SOFTWARE ORIGINALLY RELEASED BY THE UNITED STATES GOVERNMENT 
 * AS REPRESENTED BY THE GOVERNMENT AGENCY LISTED BELOW ("GOVERNMENT AGENCY"). 
 * THE UNITED STATES GOVERNMENT, AS REPRESENTED BY GOVERNMENT AGENCY, IS AN 
 * INTENDED THIRD-PARTY BENEFICIARY OF ALL SUBSEQUENT DISTRIBUTIONS OR 
 * REDISTRIBUTIONS OF THE SUBJECT SOFTWARE. ANYONE WHO USES, REPRODUCES, 
 * DISTRIBUTES, MODIFIES OR REDISTRIBUTES THE SUBJECT SOFTWARE, AS DEFINED 
 * HEREIN, OR ANY PART THEREOF, IS, BY THAT ACTION, ACCEPTING IN FULL THE 
 * RESPONSIBILITIES AND OBLIGATIONS CONTAINED IN THIS AGREEMENT.
 * 
 * Government Agency: The National Center for Telehealth and Technology
 * Government Agency Original Software Designation: MahjongLib001
 * Government Agency Original Software Title: MahjongLib
 * User Registration Requested. Please send email 
 * with your contact information to: robert.kayl2@us.army.mil
 * Government Agency Point of Contact for Original Software: robert.kayl2@us.army.mil
 * 
 */
package com.t2.mahjong;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class MahjongViewGroup extends ViewGroup implements OnClickListener {
	private static int sMargin;

	private static Paint sSelectionPaint;
	private static Paint sDefaultPaint;
	private static Paint sFreeHighlightPaint;
	private static Paint sFocusPaint;

	private String[] mDescriptions;

	private Animation mWobbleAnim;
	private Animation mShrinkAnim;

	private int mVerticalSteps;
	private int mHorizontalSteps;

	private int mLeftPan = 0;
	private int mTopPan = 0;
	private float mZoom = 1.0f;

	private Animation mHintOne;
	private Animation mHintTwo;

	private MahjongListener mMahjongListener;

	static {
		sFreeHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		ColorFilter cf = new LightingColorFilter(0xFFCCE3BF, 1);
		sFreeHighlightPaint.setColorFilter(cf);

		sFocusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		cf = new LightingColorFilter(0xFFe7851c, 1);
		sFocusPaint.setColorFilter(cf);

		sSelectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		cf = new LightingColorFilter(0xFFBFDBE3, 1);
		sSelectionPaint.setColorFilter(cf);

		sDefaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	private Mahjong mGame;

	private int mSelectionId = -1;
	private boolean mShowFreeTiles;

	private static final float TILE_LEFT_ISO = 40;

	private static final float TILE_TOP_ISO = 40;

	private static final float TILE_FACE_WIDTH = 322;

	private static final float TILE_FACE_HEIGHT = 426;

	private static final float TILE_WIDTH_STEP = 161;

	private static final float TILE_HEIGHT_STEP = 213;

	private static Rect sViewRect = new Rect();

	final private Map<Integer, Bitmap> mIconCache = new HashMap<Integer, Bitmap>();

	static final private Rect VIEW_RECT = new Rect();

	private static Matrix sMatrix = new Matrix();

	public MahjongViewGroup(Context context) {
		super(context);
		init();
	}

	public MahjongViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MahjongViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void clearCache() {
		for (Bitmap bmp : mIconCache.values()) {
			bmp.recycle();
		}
		mIconCache.clear();
	}

	public String getCurrentState() {
		StringBuilder sb = new StringBuilder();
		for (Tile tile : mGame.getTiles()) {
			sb.append(tile.isVisible() ? 1 : 0);
		}
		return sb.toString();
	}

	public boolean isComplete() {
		int size = mGame.getTiles().size();
		for (int i = 0; i < size; i++) {
			if (mGame.getTiles().get(i).isVisible()) {
				return false;
			}
		}
		return true;
	}

	public boolean isIncompletable() {
		List<Tile> freeTiles = mGame.getFreeTiles();
		Set<Integer> matchIds = new HashSet<Integer>();
		for (Tile tile : freeTiles) {
			if (!matchIds.add(tile.getMatchId())) {
				return false;
			}
		}
		return true;
	}

	public void onClick(View v) {
		TileView tv = (TileView) v;
		if (!mGame.isFree(tv.getTile())) {
			return;
		}

		if (mHintOne != null) {
			mHintOne.cancel();
		}

		if (mHintTwo != null) {
			mHintTwo.cancel();
		}

		if (mSelectionId >= 0 && mSelectionId != tv.getId()) {
			final TileView selView = (TileView) findViewById(mSelectionId);

			if (selView.getTile().getMatchId() != tv.getTile().getMatchId()) {
				mSelectionId = tv.getId();
				selView.setSelected(false);
				tv.setSelected(true);
				tv.invalidate();
				selView.invalidate();
				return;
			}
			mSelectionId = -1;
			tv.setSelected(false);
			selView.setSelected(false);
			selView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shrink));
			mShrinkAnim.setAnimationListener(new RemoveTileAnimationListener(tv, selView));
			tv.startAnimation(mShrinkAnim);

			for (int i = 0; i < getChildCount(); i++) {
				TileView tile = (TileView) getChildAt(i);
				if (tile.isFocusable()) {
					tile.requestFocus();
					break;
				}
			}

			// invalidate();
		} else {
			mSelectionId = tv.isSelected() ? -1 : tv.getId();
			tv.setSelected(!tv.isSelected());
			tv.invalidate();
		}
	}

	public void restartGame() {
		if (mSelectionId >= 0) {
			final TileView selView = (TileView) findViewById(mSelectionId);
			selView.setSelected(false);
			mSelectionId = -1;
		}

		for (int i = 0; i < getChildCount(); i++) {
			TileView tile = (TileView) getChildAt(i);
			tile.setSelected(false);
			tile.clearAnimation();
			tile.setClickable(true);
			tile.getTile().setVisible(true);
		}
		refreshEnabledState();
		requestLayout();
		invalidate();
	}

	public void setGame(Mahjong game) {
		mGame = game;
		removeAllViews();

		int id = 0;
		for (Tile tile : mGame.mTiles) {
			TileView tv = new TileView(getContext());
			tv.setId(id);
			tv.setTag(tile);
			tv.setTile(tile);
			addView(tv);
			id++;
		}

		mWobbleAnim = AnimationUtils.loadAnimation(getContext(), R.anim.wobble);
		mShrinkAnim = AnimationUtils.loadAnimation(getContext(), R.anim.shrink);

		mVerticalSteps = mGame.getHeight();
		mHorizontalSteps = mGame.getWidth();

		refreshEnabledState();

		requestLayout();
	}

	public void setMahjonggListener(MahjongListener mahjonggListener) {
		mMahjongListener = mahjonggListener;
	}

	public void showHint() {
		List<Tile> freeTiles = mGame.getFreeTiles();
		Collections.shuffle(freeTiles);

		Map<Integer, Tile> tileMap = new HashMap<Integer, Tile>();
		for (Tile tile : freeTiles) {
			if (!tileMap.containsKey(tile.getMatchId())) {
				tileMap.put(tile.getMatchId(), tile);
			} else {
				if (mHintOne != null) {
					mHintOne.cancel();
				}

				if (mHintTwo != null) {
					mHintTwo.cancel();
				}

				TileView matchView = (TileView) findViewWithTag(tileMap.get(tile.getMatchId()));
				TileView otherView = (TileView) findViewWithTag(tile);

				TileView tvOne = new TileView(getContext());
				tvOne.setClickable(false);
				tvOne.setSelected(matchView.isSelected());
				// tvOne.setEnabled(false);
				tvOne.setTile(matchView.getTile());
				tvOne.setId(1000);

				TileView tvTwo = new TileView(getContext());
				tvTwo.setClickable(false);
				tvTwo.setSelected(otherView.isSelected());
				// tvTwo.setEnabled(false);
				tvTwo.setTile(otherView.getTile());
				tvTwo.setId(1001);

				addView(tvOne);
				addView(tvTwo);

				tvOne.bringToFront();
				tvTwo.bringToFront();
				mHintOne = AnimationUtils.loadAnimation(getContext(), R.anim.tile_pulse);
				mHintOne.setAnimationListener(new HintAnimationListener(tvOne));
				mHintTwo = AnimationUtils.loadAnimation(getContext(), R.anim.tile_pulse);
				mHintTwo.setAnimationListener(new HintAnimationListener(tvTwo));
				tvOne.startAnimation(mHintOne);

				tvTwo.startAnimation(mHintTwo);

				return;
			}
		}
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		return super.getChildDrawingOrder(childCount, i);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mGame == null) {
			return;
		}

		mHorizontalSteps = mGame.getWidth();
		mVerticalSteps = mGame.getHeight();

		final int minCol = mGame.getMinCol();
		final int minRow = mGame.getMinRow();

		sViewRect.set(l, t, r, b);
		sViewRect.offset(mLeftPan, mTopPan);
		sViewRect.inset((int) (sViewRect.width() - (sViewRect.width() * mZoom)), (int) (sViewRect.height() - (sViewRect.height() * mZoom)));

		final float fitRatio = Math.min(sViewRect.width() / getPuzzleWidth(), sViewRect.height() / getPuzzleHeight());
		final float stepWidth = fitRatio * TILE_WIDTH_STEP;
		final float stepHeight = fitRatio * TILE_HEIGHT_STEP;
		final float tileWidth = stepWidth * 2.0f;
		final float tileHeight = stepHeight * 2.0f;
		final float leftIso = fitRatio * TILE_LEFT_ISO;
		final float topIso = fitRatio * TILE_TOP_ISO;
		final float leftIsoStep = fitRatio * (TILE_LEFT_ISO / 2.0f);
		final float topIsoStep = fitRatio * (TILE_TOP_ISO / 2.0f);
		final float maxRightIso = (mGame.getGreatestRightmostDepth() - 1) * leftIso;
		final float maxTopIso = (mGame.getGreatestTopmostDepth() - 1) * topIso;

		final float finalWidth = (stepWidth * mHorizontalSteps) - (mHorizontalSteps * leftIsoStep) + (maxRightIso);
		final float finalHeight = (stepHeight * mVerticalSteps) - (mVerticalSteps * topIsoStep) + (maxTopIso);
		final int topShift = Math.round((getMeasuredHeight() - finalHeight) / 2.0f);
		final int leftShift = Math.round((getMeasuredWidth() - finalWidth) / 2.0f);

		final int count = getChildCount();
		TileView child;
		TileSlot slot;
		Tile tile;
		for (int i = 0; i < count; i++) {
			child = (TileView) getChildAt(i);
			slot = child.getTile().getSlot();
			tile = child.getTile();
			if (tile.isVisible()) {
				final int layer = slot.getLayer();
				final int col = slot.getCol() - minCol;
				final int row = slot.getRow() - minRow;
				final int left = leftShift + Math.round((col * stepWidth) - (col * leftIsoStep) - maxRightIso);
				final int top = topShift + Math.round((row * stepHeight) - (row * topIsoStep) + maxTopIso);

				final int leftLayerOffset = Math.round(leftIso * layer);
				final int topLayerOffset = Math.round(topIso * layer);

				child.layout(
						left + leftLayerOffset,
						top - topLayerOffset,
						Math.round(left + tileWidth + leftLayerOffset),
						Math.round(top + tileHeight - topLayerOffset));
			}
		}
	}

	private float getPuzzleHeight() {
		final float tiles = mVerticalSteps / 2.0f;
		return (TILE_FACE_HEIGHT * tiles) + sMargin + (TILE_TOP_ISO * 2.0f)
				+ (mGame.getGreatestTopmostDepth() * TILE_TOP_ISO) - (TILE_TOP_ISO * (tiles - 1));
	}

	private float getPuzzleWidth() {
		final float tiles = mHorizontalSteps / 2.0f;
		return (TILE_FACE_WIDTH * tiles) + sMargin + (TILE_LEFT_ISO * 2.0f)
				+ (mGame.getGreatestRightmostDepth() * TILE_LEFT_ISO) - (TILE_LEFT_ISO * (tiles - 1));
	}

	private void init() {
		final float scale = getResources().getDisplayMetrics().density;
		sMargin = (int) (10 * scale);
		mDescriptions = getContext().getResources().getStringArray(R.array.tile_descriptions);
	}

	private void refreshEnabledState() {
		List<Tile> freeTiles = mGame.getFreeTiles();
		for (int i = 0; i < getChildCount(); i++) {
			TileView tile = (TileView) getChildAt(i);
			boolean enabled = freeTiles.contains(tile.getTile());
			tile.setEnabled(enabled);
			tile.setFocusable(enabled);
		}
	}

	public static interface MahjongListener {
		public void onGameComplete();

		public void onGameIncompletable();
	}

	private class HintAnimationListener implements AnimationListener {
		private TileView mView;

		public HintAnimationListener(TileView view) {
			mView = view;
		}

		public void onAnimationEnd(Animation animation) {
			mView.clearAnimation();
			post(new Runnable() {
				public void run() {
					MahjongViewGroup.this.removeView(mView);
				}
			});
		}

		public void onAnimationRepeat(Animation animation) {
		}

		public void onAnimationStart(Animation animation) {
		}
	}

	private class RemoveTileAnimationListener implements AnimationListener {
		private TileView mViewOne, mViewTwo;

		public RemoveTileAnimationListener(TileView viewOne, TileView viewTwo) {
			mViewOne = viewOne;
			mViewTwo = viewTwo;
		}

		public void onAnimationEnd(Animation animation) {
			mViewOne.hide();
			mViewTwo.hide();

			if (isComplete()) {
				if (mMahjongListener != null) {
					mMahjongListener.onGameComplete();
				}
				return;
			}

			if (isIncompletable()) {
				if (mMahjongListener != null) {
					mMahjongListener.onGameIncompletable();
				}
				return;
			}

			refreshEnabledState();
			requestLayout();
		}

		public void onAnimationRepeat(Animation animation) {
		}

		public void onAnimationStart(Animation animation) {
			mViewOne.setClickable(false);
			mViewTwo.setClickable(false);
		}
	}

	private class TileView extends View {

		private Tile mTile;

		public TileView(Context context) {
			super(context);
			init();
		}

		public TileView(Context context, AttributeSet attrs) {
			super(context, attrs);
			init();
		}

		public TileView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			init();
		}

		public Tile getTile() {
			return mTile;
		}

		public void hide() {
			setSelected(false);
			mTile.setVisible(false);
			setClickable(false);
			invalidate();
		}

		@Override
		public void setSelected(boolean selected) {
			super.setSelected(selected);
			if (selected) {
				startAnimation(mWobbleAnim);
			} else {
				clearAnimation();
			}
		}

		public void setTile(Tile tile) {
			mTile = tile;
		}

		@Override
		public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
			if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
				event.getText().add(getTileDescription(mTile) + " Tile.");
			}
			return true;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			if (!mTile.isVisible()) {
				return;
			}

			getDrawingRect(VIEW_RECT);

			if (mIconCache.get(mTile.getTileId()) == null) {
				mIconCache.put(mTile.getTileId(), BitmapFactory.decodeResource(getResources(), getResourceForTile(mTile)));
			}

			Paint paint = sDefaultPaint;
			if (mShowFreeTiles && isEnabled() && !isSelected() && !isFocused()) {
				paint = sFreeHighlightPaint;
			} else if (isSelected()) {
				paint = sSelectionPaint;
			} else if (isFocused()) {
				paint = sFocusPaint;
			}

			// if (isInEditMode()) {
			// bitmap = BitmapFactory.decodeResource(getResources(),
			// R.drawable.tile_0_0);
			// }

			sMatrix.reset();
			sMatrix.postScale(VIEW_RECT.width() / (float) mIconCache.get(mTile.getTileId()).getWidth(), VIEW_RECT.height()
					/ (float) mIconCache.get(mTile.getTileId()).getHeight());
			canvas.drawBitmap(mIconCache.get(mTile.getTileId()), sMatrix, paint);
		}

		private void init() {
			setOnClickListener(MahjongViewGroup.this);
		}

	}

	private String getTileDescription(Tile tile) {
		if (tile.getTileId() < mDescriptions.length) {
			return mDescriptions[tile.getTileId()];
		}
		return "Unknown";
	}

	private int getResourceForTile(Tile tile) {
		switch (tile.getTileId()) {
		case 0:
			return R.drawable.tile_0_0;
		case 1:
			return R.drawable.tile_0_1;
		case 2:
			return R.drawable.tile_0_2;
		case 3:
			return R.drawable.tile_0_3;
		case 4:
			return R.drawable.tile_0_4;
		case 5:
			return R.drawable.tile_0_5;
		case 6:
			return R.drawable.tile_0_6;
		case 7:
			return R.drawable.tile_0_7;
		case 8:
			return R.drawable.tile_0_8;
		case 9:
			return R.drawable.tile_0_9;
		case 10:
			return R.drawable.tile_0_10;
		case 11:
			return R.drawable.tile_0_11;
		case 12:
			return R.drawable.tile_0_12;
		case 13:
			return R.drawable.tile_0_13;
		case 14:
			return R.drawable.tile_0_14;
		case 15:
			return R.drawable.tile_0_15;
		case 16:
			return R.drawable.tile_0_16;
		case 17:
			return R.drawable.tile_0_17;
		case 18:
			return R.drawable.tile_0_18;
		case 19:
			return R.drawable.tile_0_19;
		case 20:
			return R.drawable.tile_0_20;
		case 21:
			return R.drawable.tile_0_21;
		case 22:
			return R.drawable.tile_0_22;
		case 23:
			return R.drawable.tile_0_23;
		case 24:
			return R.drawable.tile_0_24;
		case 25:
			return R.drawable.tile_0_25;
		case 26:
			return R.drawable.tile_0_26;
		case 27:
			return R.drawable.tile_0_27;
		case 28:
			return R.drawable.tile_0_28;
		case 29:
			return R.drawable.tile_0_29;
		case 30:
			return R.drawable.tile_0_30;
		case 31:
			return R.drawable.tile_0_31;
		case 32:
			return R.drawable.tile_0_32;
		case 33:
			return R.drawable.tile_0_33;
		case 34:
			return R.drawable.tile_0_34;
		case 35:
			return R.drawable.tile_0_35;
		case 36:
			return R.drawable.tile_0_36;
		case 37:
			return R.drawable.tile_0_37;
		case 38:
			return R.drawable.tile_0_38;
		case 39:
			return R.drawable.tile_0_39;
		case 40:
			return R.drawable.tile_0_40;
		}
		return -1;
	}

	public boolean isShowFreeTiles() {
		return mShowFreeTiles;
	}

	public void setShowFreeTiles(boolean b) {
		mShowFreeTiles = b;
		invalidate();
	}

}

