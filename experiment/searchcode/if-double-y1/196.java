// Created by plusminus on 17:45:56 - 25.09.2008
package org.andnav.osm.views;

/*import gt.software.maps.DrawTuchArmy;
import gt.software.maps.MainMapActivity;
import gt.software.maps.R;
import gt.software.maps.TipTSBH;
import gt.software.maps.overlays.DrawOverlay;
import gt.software.maps.overlays.QBOverlay;
import gt.software.net.InfoDetailUI;
import gt.software.net.InfoImageUI;
import gt.software.net.InfoResourceAdapter;
import gt.software.net.InfoVideoUI;*/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.andnav.osm.util.BoundingBoxE6;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.util.MyMath;
import org.andnav.osm.util.constants.GeoConstants;
import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.andnav.osm.views.controller.OpenStreetMapViewController;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.util.OpenStreetMapRendererInfo;
import org.andnav.osm.views.util.OpenStreetMapTileDownloader;
import org.andnav.osm.views.util.OpenStreetMapTileFilesystemProvider;
import org.andnav.osm.views.util.OpenStreetMapTileProvider;
import org.andnav.osm.views.util.Util;
import org.andnav.osm.views.util.VersionedGestureDetector;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class OpenStreetMapView extends View implements OpenStreetMapConstants,
		OpenStreetMapViewConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	//从mapActivity里面提取出来的flag
	private boolean mapflag=false;


	private SimpleInvalidationHandler mSimpleInvalidationHandler;
	protected int mLatitudeE6 = 0, mLongitudeE6 = 0;
	protected int mZoomLevel = 0;
	public float mBearing = 0;
	private boolean mActionMoveDetected;
	private boolean mStopMoveDetecting;
	protected OpenStreetMapRendererInfo mRendererInfo;
	protected final OpenStreetMapTileProvider mTileProvider;

	protected final GestureDetector mGestureDetector = new GestureDetector(
			new OpenStreetMapViewGestureDetectorListener());
	private VersionedGestureDetector mDetector = VersionedGestureDetector
			.newInstance(new GestureCallback());

	protected final List<OpenStreetMapViewOverlay> mOverlays = new ArrayList<OpenStreetMapViewOverlay>();

	protected final Paint mPaint = new Paint();
	protected final Paint mfPaint = new Paint();
	public int mTouchDownX;
	public int mTouchDownY;
	public int mTouchMapOffsetX;
	public int mTouchMapOffsetY;
	public double mTouchScale;
	private double mTouchDiagonalSize;

	private OpenStreetMapView mMiniMap, mMaxiMap;

	private OpenStreetMapViewController mController;
	private int mMiniMapOverriddenVisibility = NOT_SET;
	private int mMiniMapZoomDiff = NOT_SET;
	private Handler mMainActivityCallbackHandler;
	private ArrayList<Point> pointlist = null;// added by cui 2013 3 19
	// added by cui
	
	private Context context;
	private double d_lon = 0;
	private double d_lat = 0;
	double last_max_lon;
	double last_min_lon;
	double last_max_lat;
	double last_min_lat;

	public boolean clickable = true;

	// end

	// ===========================================================
	// Constructors
	// ===========================================================

	/**
	 * Standard Constructor for {@link OpenStreetMapView}.
	 * 
	 * @param context
	 * @param aRendererInfo
	 *            pass a {@link OpenStreetMapRendererInfo} you like.
	 */
	public OpenStreetMapView(final Context context,
			OpenStreetMapRendererInfo aRendererInfo) {
		super(context);
		this.context = context;
		this.mSimpleInvalidationHandler = new SimpleInvalidationHandler();
		this.mRendererInfo = aRendererInfo;
		this.mTileProvider = new OpenStreetMapTileProvider(context,
				mSimpleInvalidationHandler, aRendererInfo,
				CACHE_MAPTILECOUNT_DEFAULT);
		this.mPaint.setAntiAlias(true);
		this.mTouchScale = 1;
		mfPaint.setColor(Color.RED);
		mfPaint.setStyle(Style.STROKE);
		mfPaint.setStrokeWidth(2);
		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	public OpenStreetMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.mSimpleInvalidationHandler = new SimpleInvalidationHandler();
		
		this.mTileProvider = new OpenStreetMapTileProvider(context,
				mSimpleInvalidationHandler, new OpenStreetMapRendererInfo(getResources(), ""),
				CACHE_MAPTILECOUNT_DEFAULT);
		this.mPaint.setAntiAlias(true);
		this.mTouchScale = 1;
		mfPaint.setColor(Color.RED);
		mfPaint.setStyle(Style.STROKE);
		mfPaint.setStrokeWidth(2);
		setFocusable(true);
		setFocusableInTouchMode(true);
		
	}
	
	public Handler getHandler() {
		return mSimpleInvalidationHandler;
	}

	/**
	 * 
	 * @param context
	 * @param aRendererInfo
	 *            pass a {@link OpenStreetMapRendererInfo} you like.
	 * @param osmv
	 *            another {@link OpenStreetMapView}, to share the TileProvider
	 *            with.<br/>
	 *            May significantly improve the render speed, when using the
	 *            same {@link OpenStreetMapRendererInfo}.
	 */
	// public OpenStreetMapView(final Context context, final
	// OpenStreetMapRendererInfo aRendererInfo,
	// final OpenStreetMapView aMapToShareTheTileProviderWith) {
	// super(context);
	// this.mRendererInfo = aRendererInfo;
	// this.mTileProvider = aMapToShareTheTileProviderWith.mTileProvider;
	// this.mPaint.setAntiAlias(true);
	// }

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	/**
	 * This MapView takes control of the {@link OpenStreetMapView} passed as
	 * parameter.<br />
	 * I.e. it zoomes it to x levels less than itself and centers it the same
	 * coords.<br />
	 * Its pretty usefull when the MiniMap uses the same TileProvider.
	 * 
	 * @see OpenStreetMapView.OpenStreetMapView(
	 * @param aOsmvMinimap
	 * @param aZoomDiff
	 *            3 is a good Value. Pass {@link OpenStreetMapViewConstants}
	 *            .NOT_SET to disable autozooming of the minimap.
	 */
	public void setMiniMap(final OpenStreetMapView aOsmvMinimap,
			final int aZoomDiff) {
		this.mMiniMapZoomDiff = aZoomDiff;
		this.mMiniMap = aOsmvMinimap;
		aOsmvMinimap.setMaxiMap(this);

		// Synchronize the Views.
		this.setMapCenter(this.mLatitudeE6, this.mLongitudeE6);
		this.setZoomLevel(this.getZoomLevel());
	}

	public boolean hasMiniMap() {
		return this.mMiniMap != null;
	}

	/**
	 * @return {@link View}.GONE or {@link View}.VISIBLE or {@link View}
	 *         .INVISIBLE or {@link OpenStreetMapViewConstants}.NOT_SET
	 * */
	public int getOverrideMiniMapVisiblity() {
		return this.mMiniMapOverriddenVisibility;
	}

	/**
	 * Use this method if you want to make the MiniMap visible i.e.: always or
	 * never. Use {@link View}.GONE , {@link View}.VISIBLE, {@link View}
	 * .INVISIBLE. Use {@link OpenStreetMapViewConstants}.NOT_SET to reset this
	 * feature.
	 * 
	 * @param aVisiblity
	 */
	public void setOverrideMiniMapVisiblity(final int aVisiblity) {
		switch (aVisiblity) {
		case View.GONE:
		case View.VISIBLE:
		case View.INVISIBLE:
			if (this.mMiniMap != null)
				this.mMiniMap.setVisibility(aVisiblity);
		case NOT_SET:
			this.setZoomLevel(this.mZoomLevel);
			break;
		default:
			throw new IllegalArgumentException(
					"See javadoch of this method !!!");
		}
		this.mMiniMapOverriddenVisibility = aVisiblity;
	}

	protected void setMaxiMap(final OpenStreetMapView aOsmvMaxiMap) {
		this.mMaxiMap = aOsmvMaxiMap;
	}

	public OpenStreetMapViewController getController() {
		if (this.mController != null)
			return this.mController;
		else
			return this.mController = new OpenStreetMapViewController(this);
	}

	/**
	 * You can add/remove/reorder your Overlays using the List of
	 * {@link OpenStreetMapViewOverlay}. The first (index 0) Overlay gets drawn
	 * first, the one with the highest as the last one.
	 */
	public List<OpenStreetMapViewOverlay> getOverlays() {
		return this.mOverlays;
	}

	public double getLatitudeSpan() {
		return this.getDrawnBoundingBoxE6().getLongitudeSpanE6() / 1E6;
	}

	public int getLatitudeSpanE6() {
		return this.getDrawnBoundingBoxE6().getLatitudeSpanE6();
	}

	public double getLongitudeSpan() {
		return this.getDrawnBoundingBoxE6().getLatitudeSpanE6() / 1E6;
	}

	public int getLongitudeSpanE6() {
		return this.getDrawnBoundingBoxE6().getLatitudeSpanE6();
	}

	public BoundingBoxE6 getDrawnBoundingBoxE6() {
		return getBoundingBox(this.getWidth(), this.getHeight());
	}

	public BoundingBoxE6 getVisibleBoundingBoxE6() {
		// final ViewParent parent = this.getParent();
		// if(parent instanceof RotateView){
		// final RotateView par = (RotateView)parent;
		// return getBoundingBox(par.getMeasuredWidth(),
		// par.getMeasuredHeight());
		// }else{
		return getBoundingBox(this.getWidth(), this.getHeight());
		// }
	}

	private BoundingBoxE6 getBoundingBox(final int pViewWidth,
			final int pViewHeight) {
		/*
		 * Get the center MapTile which is above this.mLatitudeE6 and
		 * this.mLongitudeE6 .
		 */
		final int[] centerMapTileCoords = Util.getMapTileFromCoordinates(
				this.mLatitudeE6, this.mLongitudeE6, this.mZoomLevel, null,
				this.mRendererInfo.PROJECTION);

		final BoundingBoxE6 tmp = Util.getBoundingBoxFromMapTile(
				centerMapTileCoords, this.mZoomLevel, mRendererInfo.PROJECTION);

		final int mLatitudeSpan_2 = (int) (1.0f * tmp.getLatitudeSpanE6()
				* pViewHeight / this.mRendererInfo
				.getTileSizePx(this.mZoomLevel)) / 2;
		final int mLongitudeSpan_2 = (int) (1.0f * tmp.getLongitudeSpanE6()
				* pViewWidth / this.mRendererInfo
				.getTileSizePx(this.mZoomLevel)) / 2;

		final int north = this.mLatitudeE6 + mLatitudeSpan_2;
		final int south = this.mLatitudeE6 - mLatitudeSpan_2;
		final int west = this.mLongitudeE6 - mLongitudeSpan_2;
		final int east = this.mLongitudeE6 + mLongitudeSpan_2;

		return new BoundingBoxE6(north, east, south, west);
	}

	/**
	 * This class is only meant to be used during on call of onDraw(). Otherwise
	 * it may produce strange results.
	 * 
	 * @return
	 */
	public OpenStreetMapViewProjection getProjection() {
		return new OpenStreetMapViewProjection();
	}

	public void setMapCenter(final GeoPoint aCenter) {
		this.setMapCenter(aCenter.getLatitudeE6(), aCenter.getLongitudeE6());
	}

	public void setMapCenter(final double aLatitude, final double aLongitude) {
		this.setMapCenter((int) (aLatitude * 1E6), (int) (aLongitude * 1E6));
	}

	public void setMapCenter(final int aLatitudeE6, final int aLongitudeE6) {
		this.setMapCenter(aLatitudeE6, aLongitudeE6, true);
	}

	protected void setMapCenter(final int aLatitudeE6, final int aLongitudeE6,
			final boolean doPassFurther) {
		this.mLatitudeE6 = aLatitudeE6;
		this.mLongitudeE6 = aLongitudeE6;

		if (doPassFurther && this.mMiniMap != null)
			this.mMiniMap.setMapCenter(aLatitudeE6, aLongitudeE6, false);
		else if (this.mMaxiMap != null)
			this.mMaxiMap.setMapCenter(aLatitudeE6, aLongitudeE6, false);

		this.postInvalidate();
	}

	public void setBearing(final float aBearing) {
		this.mBearing = aBearing;
	}

	public float getBearing() {
		return this.mBearing;
	}

	public boolean setRenderer(final OpenStreetMapRendererInfo aRenderer) {
		this.mRendererInfo = aRenderer;
		final boolean ret = this.mTileProvider.setRender(aRenderer,
				mSimpleInvalidationHandler);
		Log.i("zoomlever", this.mZoomLevel + ".." + aRenderer.ZOOM_MAXLEVEL
				+ "-" + aRenderer.ZOOM_MINLEVEL);
		if (this.mZoomLevel > aRenderer.ZOOM_MAXLEVEL)
			this.mZoomLevel = aRenderer.ZOOM_MAXLEVEL;
		if (this.mZoomLevel < aRenderer.ZOOM_MINLEVEL)
			this.mZoomLevel = aRenderer.ZOOM_MINLEVEL;

		this.setZoomLevel(this.mZoomLevel); // Invalidates the map and zooms to
		// the maximum level of the
		// this.setZoomLevel(aRenderer.ZOOM_MAXLEVEL);// modified by cui 2013 4
		// 19
		// renderer.
		// added by cui 2013 4 19
		// GeoPoint p1 =
		// this.getProjection().fromPixels(MainMapActivity.Width/2,
		// MainMapActivity.Height/2,mBearing);
		// Toast.makeText(getContext(),
		// "p1="+p1.getLongitude()+".."+p1.getLatitude(),
		// Toast.LENGTH_LONG).show();
		// this.getController().animateTo(p1,OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED,
		// OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH,
		// OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);
		// this.setZoomLevel(aRenderer.ZOOM_MAXLEVEL);
		// this.getController().animateTo(p1,OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED,
		// OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH,OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);

		// end
		return ret;
	}

	public OpenStreetMapRendererInfo getRenderer() {
		return this.mRendererInfo;
	}

	/**
	 * @param aZoomLevel
	 *            between 0 (equator) and 18/19(closest), depending on the
	 *            Renderer chosen.
	 */
	public void setZoomLevel(final int aZoomLevel) {
		this.mZoomLevel = Math.max(this.mRendererInfo.ZOOM_MINLEVEL,
				Math.min(this.mRendererInfo.ZOOM_MAXLEVEL, aZoomLevel));

		if (this.mMiniMap != null) {
			if (this.mZoomLevel < this.mMiniMapZoomDiff) {
				if (this.mMiniMapOverriddenVisibility == NOT_SET)
					this.mMiniMap.setVisibility(View.INVISIBLE);
			} else {
				if (this.mMiniMapOverriddenVisibility == NOT_SET
						&& this.mMiniMap.getVisibility() != View.VISIBLE) {
					this.mMiniMap.setVisibility(View.VISIBLE);
				}
				if (this.mMiniMapZoomDiff != NOT_SET)
					this.mMiniMap.setZoomLevel(this.mZoomLevel
							- this.mMiniMapZoomDiff);
			}
		}
		this.postInvalidate();
	}

	/**
	 * Zooms in if possible.
	 */
	public void zoomIn() {

		// final String nextBelowMaptileUrlString =
		// this.mRendererInfo.getTileURLString(Util
		// .getMapTileFromCoordinates(this.mLatitudeE6, this.mLongitudeE6,
		// this.mZoomLevel + 1,
		// null), this.mZoomLevel + 1);
		// this.mTileProvider.preCacheTile(nextBelowMaptileUrlString);
		this.setZoomLevel(this.mZoomLevel + 1);
	}

	/**
	 * Zooms out if possible.
	 */
	public void zoomOut() {
		this.setZoomLevel(this.mZoomLevel - 1);
	}

	/**
	 * @return the current ZoomLevel between 0 (equator) and 18/19(closest),
	 *         depending on the Renderer chosen.
	 */
	public int getZoomLevel() {
		return this.mZoomLevel;
	}

	public GeoPoint getMapCenter() {
		return new GeoPoint(this.mLatitudeE6, this.mLongitudeE6);
	}

	public int getMapCenterLatitudeE6() {
		return this.mLatitudeE6;
	}

	public int getMapCenterLongitudeE6() {
		return this.mLongitudeE6;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	public void onLongPress(MotionEvent e) {
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			if (osmvo.onLongPress(e, this)) {
				mActionMoveDetected = true;
				return;
			}

	}

	public boolean onSingleTapUp(MotionEvent e) {
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			if (osmvo.onSingleTapUp(e, this))
				return true;

		return false;
	}

	public boolean onDoubleTap(MotionEvent e) {
		if (mBearing != 0) {
			mBearing = 0;
			Message.obtain(mMainActivityCallbackHandler, OpenStreetMapTileFilesystemProvider.MAP_ACTIVITY_MOVEMAP)
					.sendToTarget();
		} else {
			final GeoPoint newCenter = this.getProjection().fromPixels(
					e.getX(), e.getY());
			this.setMapCenter(newCenter);

			zoomIn();
			Message.obtain(mMainActivityCallbackHandler, OpenStreetMapTileFilesystemProvider.MAP_ACTIVITY_SETTITLE)
					.sendToTarget();
		}

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Log.e(DEBUGTAG, "onKeyDown keyCode="+keyCode);
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			if (osmvo.onKeyDown(keyCode, event, this))
				return true;

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			if (osmvo.onKeyUp(keyCode, event, this))
				return true;

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			if (osmvo.onTrackballEvent(event, this))
				return true;

		return super.onTrackballEvent(event);
	}

	public boolean canCreateContextMenu() {
		return !mActionMoveDetected;
	}

	public GeoPoint getTouchDownPoint() {
		return this.getProjection().fromPixels(mTouchDownX, mTouchDownY,
				mBearing);
	}

	boolean dialoif = true;
	boolean dialotsif = true;

	private class GestureCallback implements
			VersionedGestureDetector.OnGestureListener {

		public void onDown(MotionEvent event) {

			mActionMoveDetected = false;
			mStopMoveDetecting = false;
			OpenStreetMapView.this.mTouchDownX = (int) event.getX();
			OpenStreetMapView.this.mTouchDownY = (int) event.getY();
			
			//点击地图时，发送通知
			
			Message.obtain(mMainActivityCallbackHandler,
					OpenStreetMapTileFilesystemProvider.MAP_ACTIVITY_ONDOWN).sendToTarget();
						
			
		}

		public void onMove(MotionEvent event, int count, float x1, float y1,
				float x2, float y2) {			

			if (Math.max(Math.abs(mTouchDownX - event.getX()),
					Math.abs(mTouchDownY - event.getY())) > 6
					&& !mStopMoveDetecting) {

				mActionMoveDetected = true; // 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
											// 锟斤拷锟斤拷锟斤拷锟斤拷
											// 锟斤拷锟�
				final float aRotateToAngle = 360 - mBearing;
				OpenStreetMapView.this.mTouchMapOffsetX = (int) (Math
						.sin(Math.toRadians(aRotateToAngle)) * (event
						.getY() - OpenStreetMapView.this.mTouchDownY))
						+ (int) (Math.cos(Math.toRadians(aRotateToAngle)) * (event
								.getX() - OpenStreetMapView.this.mTouchDownX));
				OpenStreetMapView.this.mTouchMapOffsetY = (int) (Math
						.cos(Math.toRadians(aRotateToAngle)) * (event
						.getY() - OpenStreetMapView.this.mTouchDownY))
						- (int) (Math.sin(Math.toRadians(aRotateToAngle)) * (event
								.getX() - OpenStreetMapView.this.mTouchDownX));

				if (count > 1) {

					final double DiagonalSize = Math.hypot(
							(double) (x1 - x2), (double) (y1 - y2));
					mTouchScale = (DiagonalSize / mTouchDiagonalSize);
				}

				invalidate();

				Message.obtain(mMainActivityCallbackHandler,
						OpenStreetMapTileFilesystemProvider.MAP_ACTIVITY_MOVEMAP).sendToTarget();
			}
			
		}

		public void onUp(MotionEvent event) {
			mActionMoveDetected = false;
			mStopMoveDetecting = true;
			final int viewWidth_2 = OpenStreetMapView.this.getWidth() / 2;
			final int viewHeight_2 = OpenStreetMapView.this.getHeight() / 2;
			final GeoPoint newCenter = OpenStreetMapView.this.getProjection()
					.fromPixels(viewWidth_2, viewHeight_2);
			OpenStreetMapView.this.mTouchMapOffsetX = 0;
			OpenStreetMapView.this.mTouchMapOffsetY = 0;
			OpenStreetMapView.this.setMapCenter(newCenter); // Calls invalidate
		}

		public void onDown2(MotionEvent event, float x1, float y1, float x2,
				float y2) {

			mTouchDiagonalSize = Math.hypot((double) (x1 - x2),
					(double) (y1 - y2));
			mActionMoveDetected = true;
		}

		public void onUp2(MotionEvent event) {			
			if (mTouchScale > 1)
				setZoomLevel(getZoomLevel() + (int) Math.round(mTouchScale) - 1);
			else
				setZoomLevel(getZoomLevel() - (int) Math.round(1 / mTouchScale)
						+ 1);
			mTouchScale = 1;

			mActionMoveDetected = false;
			mStopMoveDetecting = true;
			final GeoPoint newCenter2 = OpenStreetMapView.this.getProjection()
					.fromPixels(OpenStreetMapView.this.getWidth() / 2,
							OpenStreetMapView.this.getHeight() / 2);
			OpenStreetMapView.this.mTouchMapOffsetX = 0;
			OpenStreetMapView.this.mTouchMapOffsetY = 0;
			OpenStreetMapView.this.setMapCenter(newCenter2); // Calls invalidate

			Message.obtain(mMainActivityCallbackHandler, OpenStreetMapTileFilesystemProvider.MAP_ACTIVITY_SETTITLE)
					.sendToTarget();
		}

	}

	public void setClickable(boolean clickble) {
		this.clickable = clickble;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (OpenStreetMapView.this.clickable) {
			for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
				if (osmvo.onTouchEvent(event, this))
					return true;

			this.mGestureDetector.onTouchEvent(event);
			this.mDetector.onTouchEvent(event);

			return super.onTouchEvent(event);
		}
		return false;
	}


/*=======
		ScrollView scrollPanel = new ScrollView(context);
		scrollPanel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		LinearLayout yiPanel = new LinearLayout(context);
		yiPanel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		yiPanel.setOrientation(LinearLayout.HORIZONTAL);

		TextView mbmc = new TextView(context);
		mbmc.setText("态势名称:");
		TextView mbmc2 = new TextView(context);
		mbmc2.setText(ts.getName());
		yiPanel.addView(mbmc);
		yiPanel.addView(mbmc2);

		LinearLayout sanPanel = new LinearLayout(context);
		sanPanel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		sanPanel.setOrientation(LinearLayout.HORIZONTAL);

		TextView ZXJD = new TextView(context);
		ZXJD.setText("经度:");
		TextView ZXJD2 = new TextView(context);
		ZXJD2.setText(ts.getJingdu());
		sanPanel.addView(ZXJD);
		sanPanel.addView(ZXJD2);

		LinearLayout siPanel = new LinearLayout(context);
		siPanel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		siPanel.setOrientation(LinearLayout.HORIZONTAL);
		TextView ZXWD = new TextView(context);
		ZXWD.setText("纬度:");
		TextView ZXWD2 = new TextView(context);
		ZXWD2.setText(ts.getWeidu());
		siPanel.addView(ZXWD);
		siPanel.addView(ZXWD2);

		LinearLayout wuPanel = new LinearLayout(context);
		wuPanel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		wuPanel.setOrientation(LinearLayout.HORIZONTAL);

		TextView MBMS = new TextView(context);
		MBMS.setText("态势描述:");
		TextView MBMS2 = new TextView(context);
		MBMS2.setText(ts.getDes());
		wuPanel.addView(MBMS);
		wuPanel.addView(MBMS2);

		// 总layer
		LinearLayout mainPanel = new LinearLayout(context);
		mainPanel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		mainPanel.setOrientation(LinearLayout.VERTICAL);

		LinearLayout liuPanel = new LinearLayout(context);
		liuPanel.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		liuPanel.setOrientation(LinearLayout.HORIZONTAL);

		Button button = new Button(context);
		button.setText("关闭");
		button.setWidth(100);
		button.setHeight(40);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				infoDialog.dismiss();
				dialotsif = true;
			}
		});

		liuPanel.addView(button);
		mainPanel.addView(yiPanel);
		mainPanel.addView(siPanel);
		mainPanel.addView(sanPanel);
		mainPanel.addView(wuPanel);
		mainPanel.addView(liuPanel);
		scrollPanel.addView(mainPanel);

		infoDialog.setContentView(scrollPanel);
		infoDialog.show();

	}

	public void selectInfoDetail(String id) {

		// TODO Auto-generated method stub
		int MBID = new Integer(id).intValue();
		TargetManagerDao mTargetManagerDao=new TargetManagerDao(context);
		final Cursor cursor =mTargetManagerDao.find(MBID);
		if (cursor.moveToFirst()) {

			final Dialog infoDialog;
			infoDialog = new Dialog(context);
			infoDialog.setCanceledOnTouchOutside(false);// modified by cui
														// 2012 12 10
			infoDialog.setCancelable(false);// modified by cui 2012 12 10
			infoDialog
					.setTitle(cursor.getString(cursor.getColumnIndex("MBMC")));

			ScrollView scrollPanel = new ScrollView(context);
			scrollPanel.setLayoutParams(new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

			LinearLayout yiPanel = new LinearLayout(context);
			yiPanel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			yiPanel.setOrientation(LinearLayout.HORIZONTAL);

			TextView mbmc = new TextView(context);
			mbmc.setText("目标名称:");
			TextView mbmc2 = new TextView(context);
			mbmc2.setText(cursor.getString(cursor.getColumnIndex("MBMC")));
			yiPanel.addView(mbmc);
			yiPanel.addView(mbmc2);

			LinearLayout erPanel = new LinearLayout(context);
			erPanel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			erPanel.setOrientation(LinearLayout.HORIZONTAL);

			TextView MBLB = new TextView(context);
			MBLB.setText("目标类别:");
			TextView MBLB2 = new TextView(context);
			MBLB2.setText(cursor.getString(cursor.getColumnIndex("MBLB")));
			erPanel.addView(MBLB);
			erPanel.addView(MBLB2);

			LinearLayout sanPanel = new LinearLayout(context);
			sanPanel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			sanPanel.setOrientation(LinearLayout.HORIZONTAL);

			TextView DLWZ = new TextView(context);
			DLWZ.setText("地理位置:");
			TextView DLWZ2 = new TextView(context);
			DLWZ2.setText(cursor.getString(cursor.getColumnIndex("DLWZ")));
			sanPanel.addView(DLWZ);
			sanPanel.addView(DLWZ2);

			LinearLayout siPanel = new LinearLayout(context);
			siPanel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			siPanel.setOrientation(LinearLayout.HORIZONTAL);

			TextView ZXJD = new TextView(context);
			ZXJD.setText("中心点经度:");
			TextView ZXJD2 = new TextView(context);
			ZXJD2.setText(cursor.getString(cursor.getColumnIndex("ZXJD")));
			siPanel.addView(ZXJD);
			siPanel.addView(ZXJD2);
			TextView ZXWD = new TextView(context);
			ZXWD.setText("中心点纬度:");
			TextView ZXWD2 = new TextView(context);
			ZXWD2.setText(cursor.getString(cursor.getColumnIndex("ZXWD")));
			siPanel.addView(ZXWD);
			siPanel.addView(ZXWD2);

			LinearLayout wuPanel = new LinearLayout(context);
			wuPanel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			wuPanel.setOrientation(LinearLayout.HORIZONTAL);

			TextView MBMS = new TextView(context);
			MBMS.setText("目标描述:");
			TextView MBMS2 = new TextView(context);
			MBMS2.setText(cursor.getString(cursor.getColumnIndex("MBMS")));
			wuPanel.addView(MBMS);
			wuPanel.addView(MBMS2);

			// //总layer
			LinearLayout mainPanel = new LinearLayout(context);
			mainPanel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			mainPanel.setOrientation(LinearLayout.VERTICAL);

			LinearLayout liuPanel = new LinearLayout(context);
			liuPanel.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			liuPanel.setOrientation(LinearLayout.HORIZONTAL);
			final String datapath = cursor.getString(cursor
					.getColumnIndex("DATAPATH"));

			Button tbtn = new Button(context);
			tbtn.setWidth(100);
			tbtn.setHeight(40);
			tbtn.setText("查看");
			tbtn.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub

					bh(cursor.getString(cursor.getColumnIndex("DATAPATH")));
					infoDialog.dismiss();
					dialoif = true;
				}

			});

			Button sbtn = new Button(context);
			sbtn.setWidth(85);
			sbtn.setHeight(40);
			sbtn.setText("查看");
			sbtn.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					huizhi2(cursor.getString(cursor.getColumnIndex("MBMC"))
							+ ".xml");
					infoDialog.dismiss();
					// dialoif = true;
				}

			});

			Button seebutton = new Button(context);
			seebutton.setWidth(100);
			seebutton.setHeight(40);
			seebutton.setText("查看");
			seebutton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					// 获取路径信息

					InfoResourceAdapter.setFilePath(datapath);
					Log.i("DDDD", datapath);
					if (datapath.substring(datapath.lastIndexOf(".") + 1)
							.equals("3gp")) {
						Intent i = new Intent(context, InfoVideoUI.class);
						context.startActivity(i);
					} else if (datapath
							.substring(datapath.lastIndexOf(".") + 1).equals(
									"jpg")) {
						Intent i = new Intent(context, InfoImageUI.class);
						context.startActivity(i);
					} else if (datapath
							.substring(datapath.lastIndexOf(".") + 1).equals(
									"amr")) {
						Intent i = new Intent(context, InfoVideoUI.class);
						context.startActivity(i);
					} else if (datapath
							.substring(datapath.lastIndexOf(".") + 1).equals(
									"xml")) {
						Intent i = new Intent(context, InfoDetailUI.class);
						context.startActivity(i);
					}
					infoDialog.dismiss();
					dialoif = true;
				}
			});

			Button button = new Button(context);
			button.setText("关闭");
			button.setWidth(100);
			button.setHeight(40);
			button.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					infoDialog.dismiss();
					dialoif = true;
				}
			});
			if (datapath.substring(datapath.lastIndexOf(".") + 1).equals("3gp")
					|| datapath.substring(datapath.lastIndexOf(".") + 1)
							.equals("jpg")
					|| datapath.substring(datapath.lastIndexOf(".") + 1)
							.equals("amr")) {
				liuPanel.addView(seebutton);
			}
			if (cursor.getString(cursor.getColumnIndex("MBLB")).equals("矢量情报"))
				liuPanel.addView(sbtn);
			if (cursor.getString(cursor.getColumnIndex("MBLB")).equals("态势情报"))
				liuPanel.addView(tbtn);

			liuPanel.addView(button);
			mainPanel.addView(yiPanel);
			mainPanel.addView(erPanel);
			if (!cursor.getString(cursor.getColumnIndex("MBLB")).equals("矢量情报")) {
				mainPanel.addView(sanPanel);

			}
			mainPanel.addView(wuPanel);
			mainPanel.addView(siPanel);
			mainPanel.addView(liuPanel);
			scrollPanel.addView(mainPanel);

			infoDialog.setContentView(scrollPanel);
			infoDialog.show();

		}
	}

	protected void bh(String pathname) {
		// TODO Auto-generated method stub
		File f = new File("/sdcard/gtMaps/signs/signs.sqlitedb");
		if (!f.exists()) {
			Toast.makeText(getContext(), "军标图标路径不存在！", 0).show();
			return;
		}

		terminalXmlParse.XmlParseToMap xm = new terminalXmlParse.XmlParseToMap();
		HashMap<String, String> xmlValue = new HashMap<String, String>();
		List<DrawTuchArmy> lst2 = new ArrayList<DrawTuchArmy>();
		try {
			xm.readDoc(pathname, xmlValue, lst2);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < lst2.size(); i++) {

			DrawOverlay.armysign = true;
			QYConstants.listDTA2.add(lst2.get(i));
			Log.i("TS1", lst2.get(i).getName() + ".."
					+ lst2.get(i).getMarkname() + ".."
					+ lst2.get(i).getJingdu() + "...." + lst2.get(i).getWeidu());
		}

		invalidate();

	}

	protected void huizhi2(String pathname) {
		// TODO Auto-generated method stub
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dombuilder = domfac.newDocumentBuilder();
			InputStream in = new FileInputStream(
					Environment.getExternalStorageDirectory()
							+ "/gtMaps/qb/slqb/" + pathname);
			Document doc = dombuilder.parse(in);
			Element root = doc.getDocumentElement();
			NodeList books = root.getChildNodes();
			Log.i("子节点数量", "" + books.getLength());
			if (books != null) {
				for (int i = 0; i < books.getLength(); i++) {
					Node book = books.item(i);
					Log.i("子节点名称：", "" + book.getNodeName());
					if (book.getNodeName().equals("Point")) {
						String jing = null;
						String wei = null;
						NodeList childnodes = book.getChildNodes();

						for (int n = 0; n < childnodes.getLength(); n++) {

							Node childnode = childnodes.item(n);

							if (childnode.getNodeName().equals("jingdu")) {
								jing = childnode.getFirstChild().getNodeValue();
							}
							if (childnode.getNodeName().equals("weidu")) {
								wei = childnode.getFirstChild().getNodeValue();
							}

						}
						GeoPoint geopoint = GeoPoint.from2DoubleString(wei,
								jing);
						DrawOverlay.points.add(geopoint);
						invalidate();
					}
					if (book.getNodeName().equals("Line")) {

						NodeList childnodes = book.getChildNodes();
						DrawOverlay.line = new ArrayList<GeoPoint>();
						for (int n = 0; n < childnodes.getLength(); n++) {
							String jing2 = null;
							String wei2 = null;
							GeoPoint geopoint = null;
							Node childnode = childnodes.item(n);
							NodeList aa = childnode.getChildNodes();
							for (int m = 0; m < aa.getLength(); m++) {

								Node bb = aa.item(m);

								if (bb.getNodeName().equals("jingdu")) {
									jing2 = bb.getFirstChild().getNodeValue();
								}
								if (bb.getNodeName().equals("weidu")) {
									wei2 = bb.getFirstChild().getNodeValue();
								}

							}
							geopoint = GeoPoint.from2DoubleString(wei2, jing2);
							DrawOverlay.line.add(geopoint);

						}
						DrawOverlay.lines.add(DrawOverlay.line);
					}
					if (book.getNodeName().equals("Mian")) {

						NodeList childnodes = book.getChildNodes();
						DrawOverlay.cover = new ArrayList<GeoPoint>();
						for (int n = 0; n < childnodes.getLength(); n++) {
							String jing2 = null;
							String wei2 = null;
							GeoPoint geopoint = null;
							Node childnode = childnodes.item(n);
							NodeList aa = childnode.getChildNodes();
							for (int m = 0; m < aa.getLength(); m++) {

								Node bb = aa.item(m);

								if (bb.getNodeName().equals("jingdu")) {
									jing2 = bb.getFirstChild().getNodeValue();
								}
								if (bb.getNodeName().equals("weidu")) {
									wei2 = bb.getFirstChild().getNodeValue();
								}

							}
							geopoint = GeoPoint.from2DoubleString(wei2, jing2);
							DrawOverlay.cover.add(geopoint);

						}
						DrawOverlay.covers.add(DrawOverlay.cover);
					}
					this.invalidate();
				}
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

>>>>>>> .r2109*/
	@Override
	public void onDraw(final Canvas c) {
		 //MainMapActivity.canva = c;
		/*
		 * Do some calculations and drag attributes to local variables to save
		 * some performance.
		 */
		if(this.mRendererInfo==null) return;
		if (this.mZoomLevel >= 18) {
			this.mZoomLevel = 18;
			// Toast.makeText(OpenStreetMapView.this, "目前已放到最大", 1).show();
		}
		final int zoomLevel = this.mZoomLevel;

		final int tileSizePxNotScale = this.mRendererInfo
				.getTileSizePx(this.mZoomLevel);
		final int tileSizePx = (int) (tileSizePxNotScale * mTouchScale);

		c.save();
		final float aRotateToAngle = 360 - mBearing;
		c.rotate(aRotateToAngle, this.getWidth() / 2, this.getHeight() / 2);

		c.drawRGB(255, 255, 255);

		final int[] centerMapTileCoords = Util.getMapTileFromCoordinates(
				this.mLatitudeE6, this.mLongitudeE6, zoomLevel, null,
				this.mRendererInfo.PROJECTION);

		/*
		 * Calculate the Latitude/Longitude on the left-upper ScreenCoords of
		 * the center MapTile. So in the end we can determine which MapTiles we
		 * additionally need next to the centerMapTile.
		 */
		final Point upperLeftCornerOfCenterMapTileNotScale = getUpperLeftCornerOfCenterMapTileInScreen(
				centerMapTileCoords, tileSizePxNotScale, null);

		final int centerMapTileScreenLeftNotScale = upperLeftCornerOfCenterMapTileNotScale.x;
		final int centerMapTileScreenTopNotScale = upperLeftCornerOfCenterMapTileNotScale.y;
		final int centerMapTileScreenRightNotScale = centerMapTileScreenLeftNotScale
				+ tileSizePxNotScale;
		final int centerMapTileScreenBottomNotScale = centerMapTileScreenTopNotScale
				+ tileSizePxNotScale;

		final Point upperLeftCornerOfCenterMapTile = getUpperLeftCornerOfCenterMapTileInScreen(
				centerMapTileCoords, tileSizePx, null);
		final int centerMapTileScreenLeft = upperLeftCornerOfCenterMapTile.x;
		final int centerMapTileScreenTop = upperLeftCornerOfCenterMapTile.y;

		/*
		 * Calculate the amount of tiles needed for each side around the center
		 * one.
		 */
		// TODO 锟斤拷锟斤拷锟�锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷 锟斤拷锟斤拷锟斤拷锟斤拷 锟斤拷锟�锟斤拷锟斤拷锟斤拷
		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�锟斤拷锟斤拷锟斤拷, 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
		// 锟�锟斤拷锟斤拷锟�锟斤拷锟�锟斤拷锟斤拷锟斤拷锟斤拷 锟斤拷锟斤拷锟�
		final int iDelta = (mBearing > 0 && mRendererInfo.YANDEX_TRAFFIC_ON == 0) ? 1
				: 0;
		/*Log.i("iDelta",
				"iDelta=======" + iDelta + "this.getHeight()="
						+ this.getHeight() + "this.getwidth()="
						+ this.getWidth() + "" + MainMapActivity.Width + ".."
						+ MainMapActivity.Height);*/
		// int additionalTilesNeededToLeftOfCenter =((int) Math
		// .ceil((float) centerMapTileScreenLeftNotScale
		// / tileSizePxNotScale))
		// + iDelta; // i.e.
		//
		// int additionalTilesNeededToRightOfCenter =((int) Math
		// .ceil((float) (this.getWidth() - centerMapTileScreenRightNotScale)
		// / tileSizePxNotScale))
		// + iDelta;
		// int additionalTilesNeededToTopOfCenter = ((int) Math
		// .ceil((float) centerMapTileScreenTopNotScale
		// / tileSizePxNotScale))+iDelta; // i.e.
		// int additionalTilesNeededToBottomOfCenter = ((int) Math
		// .ceil((float) (this.getHeight() - centerMapTileScreenBottomNotScale)
		// / tileSizePxNotScale))+iDelta;
		// if (MainMapActivity.Height == 1280) {
		// int max1 = Math.max(additionalTilesNeededToTopOfCenter,
		// additionalTilesNeededToBottomOfCenter);
		// int max2 = Math.max(additionalTilesNeededToRightOfCenter,
		// additionalTilesNeededToLeftOfCenter);
		// if(max1==max2&&max1!=2){
		// if(additionalTilesNeededToRightOfCenter>additionalTilesNeededToLeftOfCenter){
		// additionalTilesNeededToRightOfCenter =
		// additionalTilesNeededToRightOfCenter-1;
		// }else{
		// additionalTilesNeededToLeftOfCenter =
		// additionalTilesNeededToLeftOfCenter-1;
		// }
		// }
		// }else if(MainMapActivity.Height==960){
		// int max1 = Math.max(additionalTilesNeededToTopOfCenter,
		// additionalTilesNeededToBottomOfCenter);
		// int max2 = Math.max(additionalTilesNeededToRightOfCenter,
		// additionalTilesNeededToLeftOfCenter);
		// if(max1==max2&&max1!=2){
		// if(additionalTilesNeededToRightOfCenter>additionalTilesNeededToLeftOfCenter){
		// additionalTilesNeededToRightOfCenter =
		// additionalTilesNeededToRightOfCenter-1;
		// }else{
		// additionalTilesNeededToLeftOfCenter =
		// additionalTilesNeededToLeftOfCenter-1;
		// }
		// }
		// // additionalTilesNeededToLeftOfCenter =
		// additionalTilesNeededToLeftOfCenter+1;
		// // additionalTilesNeededToRightOfCenter =
		// additionalTilesNeededToRightOfCenter+1;
		// }
		int additionalTilesNeededToLeftOfCenter = 2;
		int additionalTilesNeededToRightOfCenter = 2;
		int additionalTilesNeededToBottomOfCenter = 2;
		int additionalTilesNeededToTopOfCenter = 2;
		/*if (MainMapActivity.Height == 960 || MainMapActivity.Height == 540) {
			additionalTilesNeededToLeftOfCenter = 2;
			additionalTilesNeededToRightOfCenter = 2;
			additionalTilesNeededToBottomOfCenter = 2;
			additionalTilesNeededToTopOfCenter = 2;
		}*/
		// if(MainMapActivity.Height==720){
		// additionalTilesNeededToLeftOfCenter = 2;
		// additionalTilesNeededToRightOfCenter=3;
		// additionalTilesNeededToBottomOfCenter=2;
		// additionalTilesNeededToTopOfCenter=2;
		// }
		// if(MainMapActivity.Height==1280){
		// additionalTilesNeededToLeftOfCenter = 2;
		// additionalTilesNeededToRightOfCenter=2;
		// additionalTilesNeededToBottomOfCenter=3;
		// additionalTilesNeededToTopOfCenter=2;
		// }

		final int mapTileUpperBound = mRendererInfo
				.getTileUpperBound(zoomLevel);
		final int[] mapTileCoords = new int[] {
				centerMapTileCoords[MAPTILE_LATITUDE_INDEX],
				centerMapTileCoords[MAPTILE_LONGITUDE_INDEX] };
		Log.i("TipCount", "Top=" + additionalTilesNeededToTopOfCenter
				+ "..button=" + additionalTilesNeededToBottomOfCenter
				+ "..left=" + additionalTilesNeededToLeftOfCenter + "..right="
				+ additionalTilesNeededToRightOfCenter);
		int countY = 0;
		/* Draw all the MapTiles (from the upper left to the lower right). */
		for (int y = -additionalTilesNeededToTopOfCenter; y <= additionalTilesNeededToBottomOfCenter; y++) {
			countY++;
			int countX = 0;
			for (int x = -additionalTilesNeededToLeftOfCenter; x <= additionalTilesNeededToRightOfCenter; x++) {
				/*
				 * Add/substract the difference of the tile-position to the one
				 * of the center.
				 */
				mapTileCoords[MAPTILE_LATITUDE_INDEX] = MyMath.mod(
						centerMapTileCoords[MAPTILE_LATITUDE_INDEX] + y,
						mapTileUpperBound);
				mapTileCoords[MAPTILE_LONGITUDE_INDEX] = MyMath.mod(
						centerMapTileCoords[MAPTILE_LONGITUDE_INDEX] + x,
						mapTileUpperBound);
				/* Construct a URLString, which represents the MapTile. */
				final String tileURLString = this.mRendererInfo
						.getTileURLString(mapTileCoords, zoomLevel);
				// Ut.dd("onDraw: " + tileURLString);

				/* Draw the MapTile 'i tileSizePx' above of the centerMapTile */

				final Bitmap currentMapTile = this.mTileProvider.getMapTile(
						tileURLString, this.mRendererInfo.TILE_SOURCE_TYPE,
						mapTileCoords[MAPTILE_LONGITUDE_INDEX],
						mapTileCoords[MAPTILE_LATITUDE_INDEX], zoomLevel);
				if (currentMapTile != null) {
					final int tileLeft = this.mTouchMapOffsetX
							+ centerMapTileScreenLeft + (x * tileSizePx);
					final int tileTop = this.mTouchMapOffsetY
							+ centerMapTileScreenTop + (y * tileSizePx);
					final Rect r = new Rect(tileLeft, tileTop, tileLeft
							+ tileSizePx + 2, tileTop + tileSizePx + 2);
					if (!currentMapTile.isRecycled()) {
						countX++;
						c.drawBitmap(currentMapTile, null, r, this.mPaint);
						Log.i("drawBitmap", currentMapTile + "");
					}
					if (DEBUGMODE) {
						c.drawLine(tileLeft, tileTop, tileLeft + tileSizePx,
								tileTop, this.mPaint);
						c.drawLine(tileLeft, tileTop, tileLeft, tileTop
								+ tileSizePx, this.mPaint);
						c.drawText(
								"y x = "
										+ mapTileCoords[MAPTILE_LATITUDE_INDEX]
										+ " "
										+ mapTileCoords[MAPTILE_LONGITUDE_INDEX]
										+ " zoom "
										+ zoomLevel
										+ " "
										+ mRendererInfo
												.getQRTS(
														mapTileCoords[MAPTILE_LONGITUDE_INDEX],
														mapTileCoords[MAPTILE_LATITUDE_INDEX],
														zoomLevel),
								tileLeft + 5, tileTop + 15, this.mPaint);
					}

				}
			}
			countY++;
		}
		
		// this.mTileProvider.CommitCash();

		/* Draw all Overlays. */
		for (OpenStreetMapViewOverlay osmvo : this.mOverlays)
			osmvo.onManagedDraw(c, this);

		this.mPaint.setStyle(Style.STROKE);
		if (this.mMaxiMap != null) // If this is a MiniMap
			c.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1,
					this.mPaint);

		c.restore();

		// c.drawLine(viewWidth/2, 0, viewWidth/2, viewHeight, this.mPaint);
		// c.drawLine(0, viewHeight/2, viewWidth, viewHeight/2, this.mPaint);
		// c.drawCircle(viewWidth/2, viewHeight/2, 100, this.mPaint);
		// c.drawLine(viewWidth/2-100, viewHeight/2-100, viewWidth/2+100,
		// viewHeight/2+100, this.mPaint);
		// c.drawLine(viewWidth/2+100, viewHeight/2-100, viewWidth/2-100,
		// viewHeight/2+100, this.mPaint);

	}

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * @param centerMapTileCoords
	 * @param tileSizePx
	 * @param reuse
	 *            just pass null if you do not have a Point to be 'recycled'.
	 */
	private Point getUpperLeftCornerOfCenterMapTileInScreen(
			final int[] centerMapTileCoords, final int tileSizePx,
			final Point reuse) {
		final Point out = (reuse != null) ? reuse : new Point();

		final int viewWidth = this.getWidth();
		final int viewWidth_2 = viewWidth / 2;
		final int viewHeight = this.getHeight();
		final int viewHeight_2 = viewHeight / 2;

		/*
		 * Calculate the Latitude/Longitude on the left-upper ScreenCoords of
		 * the center MapTile. So in the end we can determine which MapTiles we
		 * additionally need next to the centerMapTile.
		 */
		final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(
				centerMapTileCoords, this.mZoomLevel, mRendererInfo.PROJECTION);
		final float[] relativePositionInCenterMapTile = bb
				.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
						this.mLatitudeE6, this.mLongitudeE6, null);

		final int centerMapTileScreenLeft = viewWidth_2
				- (int) (0.5f + (relativePositionInCenterMapTile[MAPTILE_LONGITUDE_INDEX] * tileSizePx));
		final int centerMapTileScreenTop = viewHeight_2
				- (int) (0.5f + (relativePositionInCenterMapTile[MAPTILE_LATITUDE_INDEX] * tileSizePx));

		out.set(centerMapTileScreenLeft, centerMapTileScreenTop);
		return out;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	/**
	 * This class may return valid results until the underlying
	 * {@link OpenStreetMapView} gets modified in any way (i.e. new center).
	 * 
	 * @author Nicolas Gramlich
	 */
	public class OpenStreetMapViewProjection {

		final int viewWidth;
		final int viewHeight;
		final BoundingBoxE6 bb;
		final int zoomLevel;
		final int tileSizePx;
		final int[] centerMapTileCoords;
		final Point upperLeftCornerOfCenterMapTile;

		public OpenStreetMapViewProjection() {
			viewWidth = OpenStreetMapView.this.getWidth();
			viewHeight = OpenStreetMapView.this.getHeight();

			/*
			 * Do some calculations and drag attributes to local variables to
			 * save some performance.
			 */
			zoomLevel = OpenStreetMapView.this.mZoomLevel; // LATER Draw to
															// attributes and so
															// make it only
															// 'valid' for a
															// short time.
			tileSizePx = (int) (OpenStreetMapView.this.mRendererInfo
					.getTileSizePx(OpenStreetMapView.this.mZoomLevel) * OpenStreetMapView.this.mTouchScale);

			/*
			 * Get the center MapTile which is above this.mLatitudeE6 and
			 * this.mLongitudeE6 .
			 */
			centerMapTileCoords = Util.getMapTileFromCoordinates(
					OpenStreetMapView.this.mLatitudeE6,
					OpenStreetMapView.this.mLongitudeE6, zoomLevel, null,
					OpenStreetMapView.this.mRendererInfo.PROJECTION);
			upperLeftCornerOfCenterMapTile = getUpperLeftCornerOfCenterMapTileInScreen(
					centerMapTileCoords, tileSizePx, null);

			bb = OpenStreetMapView.this.getDrawnBoundingBoxE6();
		}

		/**
		 * Converts x/y ScreenCoordinates to the underlying GeoPoint.
		 * 
		 * @param x
		 * @param y
		 * @return GeoPoint under x/y.
		 */
		public GeoPoint fromPixels(float x, float y) {
			/* Subtract the offset caused by touch. */
			// Log.d(DEBUGTAG,
			// "x = "+x+" mTouchMapOffsetX = "+OpenStreetMapView.this.mTouchMapOffsetX+"   ");

			x -= OpenStreetMapView.this.mTouchMapOffsetX;
			y -= OpenStreetMapView.this.mTouchMapOffsetY;

			// int xx =
			// centerMapTileCoords[0]*tileSizePx+(int)x-upperLeftCornerOfCenterMapTile.x;
			// int asd = Util.x2lon(xx, zoomLevel, tileSizePx);
			GeoPoint p = bb
					.getGeoPointOfRelativePositionWithLinearInterpolation(x
							/ viewWidth, y / viewHeight);

			// Log.d(DEBUGTAG,
			// "lon "+p.getLongitudeE6()+" "+xx+" "+asd+" OffsetX = "+OpenStreetMapView.this.mTouchMapOffsetX);
			// Log.d(DEBUGTAG,
			// "	"+centerMapTileCoords[0]+" "+tileSizePx+" "+x+" "+upperLeftCornerOfCenterMapTile.x);
			// p.setLongitudeE6(asd);

			// for(int i =0; i<=tileSizePx*(1<<zoomLevel); i++){int Q =
			// Util.x2lon(i, zoomLevel, tileSizePx);Log.d(DEBUGTAG,
			// "lon "+i+" "+Q);}

			return p;
		}

		public GeoPoint fromPixels(float x, float y, double bearing) {
			final int x1 = (int) (x - OpenStreetMapView.this.getWidth() / 2);
			final int y1 = (int) (y - OpenStreetMapView.this.getHeight() / 2);
			final double hypot = Math.hypot(x1, y1);
			final double angle = -1 * Math.signum(y1)
					* Math.toDegrees(Math.acos(x1 / hypot));
			final double angle2 = angle - bearing;
			final int x2 = (int) (Math.cos(Math.toRadians(angle2)) * hypot);
			final int y2 = (int) (Math.sin(Math.toRadians(angle2 - 180)) * hypot);

			return fromPixels(
					(float) (OpenStreetMapView.this.getWidth() / 2 + x2),
					(float) (OpenStreetMapView.this.getHeight() / 2 + y2));
		}

		private static final int EQUATORCIRCUMFENCE = 40075676; // 40075004;

		public float metersToEquatorPixels(final float aMeters) {
			return aMeters
					/ EQUATORCIRCUMFENCE
					* OpenStreetMapView.this.mRendererInfo
							.getTileSizePx(OpenStreetMapView.this.mZoomLevel);
		}

		/**
		 * Converts a GeoPoint to its ScreenCoordinates. <br/>
		 * <br/>
		 * <b>CAUTION</b> ! Conversion currently has a large error on
		 * <code>zoomLevels <= 7</code>.<br/>
		 * The Error on ZoomLevels higher than 7, the error is below
		 * <code>1px</code>.<br/>
		 * LATER: Add a linear interpolation to minimize this error.
		 * 
		 * <PRE>
		 * Zoom 	Error(m) 	Error(px)
		 * 11 	6m 	1/12px
		 * 10 	24m 	1/6px
		 * 8 	384m 	1/2px
		 * 6 	6144m 	3px
		 * 4 	98304m 	10px
		 * </PRE>
		 * 
		 * @param in
		 *            the GeoPoint you want the onScreenCoordinates of.
		 * @param reuse
		 *            just pass null if you do not have a Point to be
		 *            'recycled'.
		 * @return the Point containing the approximated ScreenCoordinates of
		 *         the GeoPoint passed.
		 */
		public Point toPixels(final GeoPoint in, final Point reuse) {
			return toPixels(in, reuse, true);
		}

		public Point tomyPixels(final GeoPoint in, final Point reuse) {
			return tomyPixels(in, reuse, true);
		}

		public Point toPixels(final GeoPoint in, final double bearing,
				final Point reuse) {
			final Point point = toPixels(in, reuse, true);
			final Point out = (reuse != null) ? reuse : new Point();

			final int x1 = point.x - OpenStreetMapView.this.getWidth() / 2;
			final int y1 = point.y - OpenStreetMapView.this.getHeight() / 2;
			final double hypot = Math.hypot(x1, y1);
			final double angle = -1 * Math.signum(y1)
					* Math.toDegrees(Math.acos(x1 / hypot));
			final double angle2 = angle + bearing;
			final int x2 = (int) (Math.cos(Math.toRadians(angle2)) * hypot);
			final int y2 = (int) (Math.sin(Math.toRadians(angle2 - 180)) * hypot);
			out.set(OpenStreetMapView.this.getWidth() / 2 + x2,
					OpenStreetMapView.this.getHeight() / 2 + y2);
			return out;
		}

		protected Point tomyPixels(final GeoPoint in, final Point reuse,
				final boolean doGudermann) {

			final Point out = (reuse != null) ? reuse : new Point();
			final int[] underGeopointTileCoords = Util
					.getMapTileFromCoordinates(in.getLatitudeE6(),
							in.getLongitudeE6(), zoomLevel, null,
							OpenStreetMapView.this.mRendererInfo.PROJECTION);

			/*
			 * Calculate the Latitude/Longitude on the left-upper ScreenCoords
			 * of the MapTile.
			 */
			final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(
					underGeopointTileCoords, zoomLevel,
					mRendererInfo.PROJECTION);
			final float[] relativePositionInCenterMapTile;
			if (doGudermann && zoomLevel < 7)
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
								in.getLatitudeE6(), in.getLongitudeE6(), null);
			else
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
								in.getLatitudeE6(), in.getLongitudeE6(), null);

			final int tileDiffX = centerMapTileCoords[MAPTILE_LONGITUDE_INDEX]
					- underGeopointTileCoords[MAPTILE_LONGITUDE_INDEX];
			final int tileDiffY = centerMapTileCoords[MAPTILE_LATITUDE_INDEX]
					- underGeopointTileCoords[MAPTILE_LATITUDE_INDEX];
			final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x
					- (tileSizePx * tileDiffX);
			final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y
					- (tileSizePx * tileDiffY);

			final int x = underGeopointTileScreenLeft
					+ (int) (relativePositionInCenterMapTile[MAPTILE_LONGITUDE_INDEX] * tileSizePx);
			final int y = underGeopointTileScreenTop
					+ (int) (relativePositionInCenterMapTile[MAPTILE_LATITUDE_INDEX] * tileSizePx);

			/* Add up the offset caused by touch. */
			out.set(x, y);
			return out;
		}

		protected Point toPixels(final GeoPoint in, final Point reuse,
				final boolean doGudermann) {

			final Point out = (reuse != null) ? reuse : new Point();

			final int[] underGeopointTileCoords = Util
					.getMapTileFromCoordinates(in.getLatitudeE6(),
							in.getLongitudeE6(), zoomLevel, null,
							OpenStreetMapView.this.mRendererInfo.PROJECTION);

			/*
			 * Calculate the Latitude/Longitude on the left-upper ScreenCoords
			 * of the MapTile.
			 */
			final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(
					underGeopointTileCoords, zoomLevel,
					mRendererInfo.PROJECTION);

			final float[] relativePositionInCenterMapTile;
			if (doGudermann && zoomLevel < 7)
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
								in.getLatitudeE6(), in.getLongitudeE6(), null);
			else
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
								in.getLatitudeE6(), in.getLongitudeE6(), null);

			final int tileDiffX = centerMapTileCoords[MAPTILE_LONGITUDE_INDEX]
					- underGeopointTileCoords[MAPTILE_LONGITUDE_INDEX];
			final int tileDiffY = centerMapTileCoords[MAPTILE_LATITUDE_INDEX]
					- underGeopointTileCoords[MAPTILE_LATITUDE_INDEX];
			final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x
					- (tileSizePx * tileDiffX);
			final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y
					- (tileSizePx * tileDiffY);

			final int x = underGeopointTileScreenLeft
					+ (int) (relativePositionInCenterMapTile[MAPTILE_LONGITUDE_INDEX] * tileSizePx);
			final int y = underGeopointTileScreenTop
					+ (int) (relativePositionInCenterMapTile[MAPTILE_LATITUDE_INDEX] * tileSizePx);

			/* Add up the offset caused by touch. */
			out.set(x + OpenStreetMapView.this.mTouchMapOffsetX, y
					+ OpenStreetMapView.this.mTouchMapOffsetY);
			return out;
		}

		public Point toPixels2(final GeoPoint in) {

			final Point out = new Point();
			final boolean doGudermann = true;

			final int[] underGeopointTileCoords = Util
					.getMapTileFromCoordinates(in.getLatitudeE6(),
							in.getLongitudeE6(), zoomLevel, null,
							OpenStreetMapView.this.mRendererInfo.PROJECTION);

			/*
			 * Calculate the Latitude/Longitude on the left-upper ScreenCoords
			 * of the MapTile.
			 */
			final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(
					underGeopointTileCoords, zoomLevel,
					mRendererInfo.PROJECTION);

			final float[] relativePositionInCenterMapTile;
			if (doGudermann && zoomLevel < 7)
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
								in.getLatitudeE6(), in.getLongitudeE6(), null);
			else
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
								in.getLatitudeE6(), in.getLongitudeE6(), null);

			final int tileDiffX = centerMapTileCoords[MAPTILE_LONGITUDE_INDEX]
					- underGeopointTileCoords[MAPTILE_LONGITUDE_INDEX];
			final int tileDiffY = centerMapTileCoords[MAPTILE_LATITUDE_INDEX]
					- underGeopointTileCoords[MAPTILE_LATITUDE_INDEX];
			final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x
					- (tileSizePx * tileDiffX);
			final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y
					- (tileSizePx * tileDiffY);

			final int x = underGeopointTileScreenLeft
					+ (int) (relativePositionInCenterMapTile[MAPTILE_LONGITUDE_INDEX] * tileSizePx);
			final int y = underGeopointTileScreenTop
					+ (int) (relativePositionInCenterMapTile[MAPTILE_LATITUDE_INDEX] * tileSizePx);

			/* Add up the offset caused by touch. */
			out.set(x, y);
			return out;
		}

		public Path toPixels(final List<GeoPoint> in, final Path reuse) {
			return toPixels(in, reuse, true);
		}

		protected Path toPixels(final List<GeoPoint> in, final Path reuse,
				final boolean doGudermann) throws IllegalArgumentException {
			if (in.size() < 2)
				throw new IllegalArgumentException(
						"List of GeoPoints needs to be at least 2.");

			final Path out = (reuse != null) ? reuse : new Path();

			int i = 0;
			for (GeoPoint gp : in) {
				i++;
				final int[] underGeopointTileCoords = Util
						.getMapTileFromCoordinates(gp.getLatitudeE6(),
								gp.getLongitudeE6(), zoomLevel, null,
								OpenStreetMapView.this.mRendererInfo.PROJECTION);

				/*
				 * Calculate the Latitude/Longitude on the left-upper
				 * ScreenCoords of the MapTile.
				 */
				final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(
						underGeopointTileCoords, zoomLevel,
						mRendererInfo.PROJECTION);

				final float[] relativePositionInCenterMapTile;
				if (doGudermann && zoomLevel < 7)
					relativePositionInCenterMapTile = bb
							.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
									gp.getLatitudeE6(), gp.getLongitudeE6(),
									null);
				else
					relativePositionInCenterMapTile = bb
							.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
									gp.getLatitudeE6(), gp.getLongitudeE6(),
									null);

				final int tileDiffX = centerMapTileCoords[MAPTILE_LONGITUDE_INDEX]
						- underGeopointTileCoords[MAPTILE_LONGITUDE_INDEX];
				final int tileDiffY = centerMapTileCoords[MAPTILE_LATITUDE_INDEX]
						- underGeopointTileCoords[MAPTILE_LATITUDE_INDEX];
				final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x
						- (tileSizePx * tileDiffX);
				final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y
						- (tileSizePx * tileDiffY);

				final int x = underGeopointTileScreenLeft
						+ (int) (relativePositionInCenterMapTile[MAPTILE_LONGITUDE_INDEX] * tileSizePx);
				final int y = underGeopointTileScreenTop
						+ (int) (relativePositionInCenterMapTile[MAPTILE_LATITUDE_INDEX] * tileSizePx);

				/* Add up the offset caused by touch. */
				if (i == 0)
					out.moveTo(x + OpenStreetMapView.this.mTouchMapOffsetX, y
							+ OpenStreetMapView.this.mTouchMapOffsetY);
				else
					out.lineTo(x + OpenStreetMapView.this.mTouchMapOffsetX, y
							+ OpenStreetMapView.this.mTouchMapOffsetY);
			}

			return out;
		}

		// addded by cui
		public Point tomy2Pixels(GeoPoint geopoint) {
			Point p = new Point();
			final int[] underGeopointTileCoords = Util
					.getMapTileFromCoordinates(geopoint.getLatitudeE6(),
							geopoint.getLongitudeE6(), zoomLevel, null,
							OpenStreetMapView.this.mRendererInfo.PROJECTION);

			/*
			 * Calculate the Latitude/Longitude on the left-upper ScreenCoords
			 * of the MapTile.
			 */
			final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(
					underGeopointTileCoords, zoomLevel,
					mRendererInfo.PROJECTION);
			final float[] relativePositionInCenterMapTile;
			if (zoomLevel < 7)
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
								geopoint.getLatitudeE6(),
								geopoint.getLongitudeE6(), null);
			else
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
								geopoint.getLatitudeE6(),
								geopoint.getLongitudeE6(), null);
			final int tileDiffX = centerMapTileCoords[MAPTILE_LONGITUDE_INDEX]
					- underGeopointTileCoords[MAPTILE_LONGITUDE_INDEX];
			final int tileDiffY = centerMapTileCoords[MAPTILE_LATITUDE_INDEX]
					- underGeopointTileCoords[MAPTILE_LATITUDE_INDEX];
			final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x
					- (tileSizePx * tileDiffX);
			final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y
					- (tileSizePx * tileDiffY);
			final int x = underGeopointTileScreenLeft
					+ (int) (relativePositionInCenterMapTile[MAPTILE_LONGITUDE_INDEX] * tileSizePx);
			final int y = underGeopointTileScreenTop
					+ (int) (relativePositionInCenterMapTile[MAPTILE_LATITUDE_INDEX] * tileSizePx);
			p.x = x;
			p.y = y;
			return p;

		}

		// end
		public Path toPixelsTrackPoints(List<GeoPoint> in, Point baseCoord,
				GeoPoint baseLocation) throws IllegalArgumentException {
			if (in.size() < 2)
				return null;
			// throw new
			// IllegalArgumentException("List of GeoPoints needs to be at least 2.");

			final Path out = new Path();
			final boolean doGudermann = true;

			int i = 0;
			int lastX = 0, lastY = 0;
			for (GeoPoint tp : in) {
				final int[] underGeopointTileCoords = Util
						.getMapTileFromCoordinates(tp.getLatitudeE6(),
								tp.getLongitudeE6(), zoomLevel, null,
								OpenStreetMapView.this.mRendererInfo.PROJECTION);

				/*
				 * Calculate the Latitude/Longitude on the left-upper
				 * ScreenCoords of the MapTile.
				 */
				final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(
						underGeopointTileCoords, zoomLevel,
						mRendererInfo.PROJECTION);

				final float[] relativePositionInCenterMapTile;
				if (doGudermann && zoomLevel < 7)
					relativePositionInCenterMapTile = bb
							.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
									tp.getLatitudeE6(), tp.getLongitudeE6(),
									null);
				else
					relativePositionInCenterMapTile = bb
							.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
									tp.getLatitudeE6(), tp.getLongitudeE6(),
									null);

				final int tileDiffX = centerMapTileCoords[MAPTILE_LONGITUDE_INDEX]
						- underGeopointTileCoords[MAPTILE_LONGITUDE_INDEX];
				final int tileDiffY = centerMapTileCoords[MAPTILE_LATITUDE_INDEX]
						- underGeopointTileCoords[MAPTILE_LATITUDE_INDEX];
				final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x
						- (tileSizePx * tileDiffX);
				final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y
						- (tileSizePx * tileDiffY);

				final int x = underGeopointTileScreenLeft
						+ (int) (relativePositionInCenterMapTile[MAPTILE_LONGITUDE_INDEX] * tileSizePx);
				final int y = underGeopointTileScreenTop
						+ (int) (relativePositionInCenterMapTile[MAPTILE_LATITUDE_INDEX] * tileSizePx);

				/* Add up the offset caused by touch. */
				if (i == 0) {
					out.setLastPoint(x, y);
					lastX = x;
					lastY = y;
					baseCoord.x = x;
					baseCoord.y = y;
					baseLocation.setCoordsE6(tp.getLatitudeE6(),
							tp.getLongitudeE6());
					i++;
				} else {
					if (Math.abs(lastX - x) > 5 || Math.abs(lastY - y) > 5) {
						out.lineTo(x, y);
						lastX = x;
						lastY = y;
						i++;
					}
				}
			}

			return out;
		}

		// added by cui 2013 3 19
		public Path toPixelsDrawLine(List<GeoPoint> in, Point baseCoord,
				GeoPoint baseLocation) throws IllegalArgumentException {
			if (in.size() < 2)
				return null;

			final Path out = new Path();
			final boolean doGudermann = true;

			int i = 0;
			int lastX = 0, lastY = 0;
			for (GeoPoint tp : in) {
				Log.i("DrawOverlay1", "in.size()==" + in.size());
				final int[] underGeopointTileCoords = Util
						.getMapTileFromCoordinates(tp.getLatitudeE6(),
								tp.getLongitudeE6(), zoomLevel, null,
								OpenStreetMapView.this.mRendererInfo.PROJECTION);

				/*
				 * Calculate the Latitude/Longitude on the left-upper
				 * ScreenCoords of the MapTile.
				 */
				final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(
						underGeopointTileCoords, zoomLevel,
						mRendererInfo.PROJECTION);

				final float[] relativePositionInCenterMapTile;
				if (doGudermann && zoomLevel < 7)
					relativePositionInCenterMapTile = bb
							.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
									tp.getLatitudeE6(), tp.getLongitudeE6(),
									null);
				else
					relativePositionInCenterMapTile = bb
							.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
									tp.getLatitudeE6(), tp.getLongitudeE6(),
									null);
				final int tileDiffX = centerMapTileCoords[MAPTILE_LONGITUDE_INDEX]
						- underGeopointTileCoords[MAPTILE_LONGITUDE_INDEX];
				final int tileDiffY = centerMapTileCoords[MAPTILE_LATITUDE_INDEX]
						- underGeopointTileCoords[MAPTILE_LATITUDE_INDEX];
				final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x
						- (tileSizePx * tileDiffX);
				final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y
						- (tileSizePx * tileDiffY);

				final int x = underGeopointTileScreenLeft
						+ (int) (relativePositionInCenterMapTile[MAPTILE_LONGITUDE_INDEX] * tileSizePx);
				final int y = underGeopointTileScreenTop
						+ (int) (relativePositionInCenterMapTile[MAPTILE_LATITUDE_INDEX] * tileSizePx);

				/* Add up the offset caused by touch. */
				if (i == 0) {
					out.setLastPoint(x, y);

					lastX = x;
					lastY = y;
					baseCoord.x = x;
					baseCoord.y = y;
					baseLocation.setCoordsE6(tp.getLatitudeE6(),
							tp.getLongitudeE6());
					i++;

				} else {
					if (Math.abs(lastX - x) > 5 || Math.abs(lastY - y) > 5) {
						out.lineTo(x, y);

						lastX = x;
						lastY = y;
						i++;
					}

				}
			}

			return out;
		}

		// added by cui 2013 3 21
		public Point toPointfromGeoPoint(GeoPoint tp)
				throws IllegalArgumentException {
			Point p = new Point();

			final int[] underGeopointTileCoords = Util
					.getMapTileFromCoordinates(tp.getLatitudeE6(),
							tp.getLongitudeE6(), zoomLevel, null,
							OpenStreetMapView.this.mRendererInfo.PROJECTION);

			/*
			 * Calculate the Latitude/Longitude on the left-upper ScreenCoords
			 * of the MapTile.
			 */
			final BoundingBoxE6 bb = Util.getBoundingBoxFromMapTile(
					underGeopointTileCoords, zoomLevel,
					mRendererInfo.PROJECTION);

			final float[] relativePositionInCenterMapTile;
			if (zoomLevel < 7)
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(
								tp.getLatitudeE6(), tp.getLongitudeE6(), null);
			else
				relativePositionInCenterMapTile = bb
						.getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(
								tp.getLatitudeE6(), tp.getLongitudeE6(), null);

			final int tileDiffX = centerMapTileCoords[MAPTILE_LONGITUDE_INDEX]
					- underGeopointTileCoords[MAPTILE_LONGITUDE_INDEX];
			final int tileDiffY = centerMapTileCoords[MAPTILE_LATITUDE_INDEX]
					- underGeopointTileCoords[MAPTILE_LATITUDE_INDEX];
			final int underGeopointTileScreenLeft = upperLeftCornerOfCenterMapTile.x
					- (tileSizePx * tileDiffX);
			final int underGeopointTileScreenTop = upperLeftCornerOfCenterMapTile.y
					- (tileSizePx * tileDiffY);

			final int x = underGeopointTileScreenLeft
					+ (int) (relativePositionInCenterMapTile[MAPTILE_LONGITUDE_INDEX] * tileSizePx);
			final int y = underGeopointTileScreenTop
					+ (int) (relativePositionInCenterMapTile[MAPTILE_LATITUDE_INDEX] * tileSizePx);

			/* Add up the offset caused by touch. */

			p.x = x;
			p.y = y;

			return p;
		}

	}

	private class SimpleInvalidationHandler extends Handler {

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case OpenStreetMapTileDownloader.MAPTILEDOWNLOADER_SUCCESS_ID:
			case OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID:
				OpenStreetMapView.this.invalidate();
				break;
			case OpenStreetMapTileFilesystemProvider.ERROR_MESSAGE:
				Message.obtain(mMainActivityCallbackHandler,
						OpenStreetMapTileFilesystemProvider.ERROR_MESSAGE,
						msg.obj).sendToTarget();
				break;
			case OpenStreetMapTileFilesystemProvider.INDEXIND_SUCCESS_ID:
				if (mZoomLevel > mRendererInfo.ZOOM_MAXLEVEL)
					mZoomLevel = mRendererInfo.ZOOM_MAXLEVEL;
				if (mZoomLevel < mRendererInfo.ZOOM_MINLEVEL)
					mZoomLevel = mRendererInfo.ZOOM_MINLEVEL;

				Message.obtain(mMainActivityCallbackHandler, OpenStreetMapTileFilesystemProvider.MAP_ACTIVITY_SETTITLE)
						.sendToTarget();

				OpenStreetMapView.this.invalidate();
				break;
			case OpenStreetMapTileFilesystemProvider.MAP_LONLAT_ID:
				Bundle bundle = msg.getData();
				int maxx = bundle.getInt("maxx");
				int minx = bundle.getInt("minx");
				int maxy = bundle.getInt("maxy");
				int miny = bundle.getInt("miny");
				boolean isfirst = bundle.getBoolean("isfirst");

				double lon_max = Util.tile2lon(maxx, 17);
				double lon_min = Util.tile2lon(minx, 17);
				double lat_min = Util.tile2lat(maxy, 17, 1);
				double lat_max = Util.tile2lat(miny, 17, 1);
				d_lat = (lat_max + lat_min) / 2;
				d_lon = (lon_max + lon_min) / 2;
				Log.i("CUITEST", "d_lon=" + d_lon + "   d_lat=" + d_lat);
				if (mapflag)
					if ((lon_min > last_max_lon && lat_min > last_max_lat)
							|| (lon_max < last_min_lon && lat_max < last_min_lat)) {
						if (isfirst) {
							setZoomLevel(18);
							Log.i("CUITEST", "isfirst=" + isfirst);
						}
						tomap();
						mapflag = false;
					}
				last_max_lon = lon_max;
				last_min_lon = lon_min;
				last_max_lat = lat_max;
				last_min_lat = lat_min;
				break;
			}
		}
	}

	private class OpenStreetMapViewGestureDetectorListener implements
			OnGestureListener, OnDoubleTapListener {

		// @Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		// @Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// LATER Could be used for smoothly 'scroll-out' the map on a fast
			// motion.
			return false;
		}

		// @Override
		public void onLongPress(MotionEvent e) {
			OpenStreetMapView.this.onLongPress(e);
		}

		// @Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		// @Override
		public void onShowPress(MotionEvent e) {
		}

		// @Override
		public boolean onSingleTapUp(MotionEvent e) {
			return OpenStreetMapView.this.onSingleTapUp(e);
		}

		public boolean onDoubleTap(MotionEvent e) {
			return OpenStreetMapView.this.onDoubleTap(e);
		}

		public boolean onDoubleTapEvent(MotionEvent e) {
			// Auto-generated method stub
			return false;
		}

		public boolean onSingleTapConfirmed(MotionEvent e) {
			// Auto-generated method stub
			return false;
		}

	}

	public void setMainActivityCallbackHandler(Handler callbackHandler) {
		this.mMainActivityCallbackHandler = callbackHandler;

	}

	public void freeDatabases() {
		mTileProvider.freeDatabases();
	}
	
	public void tomap() {
		// TODO Auto-generated method stub

		if (d_lon != 0 && d_lat != 0) {
			GeoPoint p = GeoPoint.from2DoubleString(String.valueOf(d_lat),
					String.valueOf(d_lon));

			if (p != null) {
				this.getController()
						.animateTo(
								p,
								OpenStreetMapViewController.AnimationType.MIDDLEPEAKSPEED,
								OpenStreetMapViewController.ANIMATION_SMOOTHNESS_HIGH,
								OpenStreetMapViewController.ANIMATION_DURATION_DEFAULT);

				this.invalidate();
			}
		}
	}

	public void setMapFlag(boolean mapflag){
		this.mapflag=mapflag;
	}
}

