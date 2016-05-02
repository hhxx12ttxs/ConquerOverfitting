package se.geoproject.atlas.ui.maprender;

import java.util.ArrayList;
import java.util.List;

import se.geoproject.atlas.location.DataFetcher;
import se.geoproject.atlas.map.data.BoundingBox;
import se.geoproject.atlas.map.data.MapEngine;
import se.geoproject.atlas.map.data.MapItem;
import se.geoproject.atlas.map.data.MapRect;
import se.geoproject.atlas.map.data.Tile;
import se.geoproject.atlas.utils.OnItemClickedListener;
//import se.geoproject.atlas.ui.maprender.triangulation.Triangulate;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * MapView class, heavily based (one could say stolen) from OSMAndroid
 * http://code.google.com/p/osm-android/source/browse/trunk/OSMandroid/src/com/google/code/osmandroid/view/OsmMapView.java
 * 
 * @author viktor
 */

public class MapViewGL extends GLSurfaceView implements DataFetcher.Callback{

	public static MapTransformation transformation;
	private static final int MAX_ZOOM_LEVEL = 17;
	private static final int MIN_ZOOM_LEVEL = 16;
	private int prevX;
	private int prevY;
	private boolean mapPanning = false;
	private ArrayList<MapItem> iconList = new ArrayList<MapItem>();
	private OnItemClickedListener listener;
	
	private Thread dataFetcher = null;
	private double nextMinY = Double.NaN;
	private double nextMaxY = Double.NaN;
	private double nextMinX = Double.NaN;
	private double nextMaxX = Double.NaN;
	
	private static final int[] zoomSize = { 0, 40850000, 31850000, 18850000, 17550000, 19500000, 1120000, 560000,
											280000, 280000, 150000, 50000, 20000, 6000, 3000, 1500, 500, 0};

	private MapEngine mapEngine;
	private MapRenderer renderer;
	private MapRect mapRect;
	public MapRect getMapRect() {
		return mapRect;
	}

	private int zoomLevel;

	private float aspectRatio;

	public MapViewGL(Context context) {
		super(context);
		init(context);
	}

	public MapViewGL(Context context, AttributeSet attr) {
		super(context, attr);
		init(context);
	}

	private void init(Context context) {
		renderer = new MapRenderer(context);
		mapRect = new MapRect();
		transformation = new MapTransformation();
		
		List<Tile> tileList = new ArrayList<Tile>();
		mapEngine = new MapEngine(tileList);
		setRenderer(renderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	public void setMapArea(BoundingBox area, int zoomLevel) {
		mapRect.maxX = area.maxX;
		mapRect.maxY = area.maxY;
		mapRect.minX = area.minX;
		mapRect.minY = area.minY;

		this.zoomLevel = zoomLevel;

		renderScene();
	}

	public void setTiles(List<Tile> tiles) {
		if (tiles.size() > 0) {
			mapEngine = new MapEngine(tiles);
			renderScene();
		}
		if(!Double.isNaN(nextMinY)) {
			dataFetcher = new Thread(new DataFetcher(nextMinY, nextMinX, nextMaxY, nextMaxX, this));
			dataFetcher.start();
			
			nextMinY = Double.NaN;
		}
		
	}

	public void renderScene() {

		mapRect = mapEngine.getMapRect(mapRect, zoomLevel);

		for (int v = 0; v < iconList.size(); v++) {
			if (mapRect.overlaps(iconList.get(v))) {
				mapRect.getMapItems().add(iconList.get(v));
			}
		}

		this.renderer.setMap(mapRect, (short) zoomLevel, true);
		this.requestRender();
	}

	public void zoomIn() {

		if (zoomLevel == MAX_ZOOM_LEVEL)
			return;

		
		Log.v("Atlas", ">>>>>>>previous zoomLevel:" + String.valueOf(zoomLevel));

		long initialWidth = (long) Math.abs((long) mapRect.maxX
				- mapRect.minX);

		mapRect.minY += zoomSize[zoomLevel];
		mapRect.maxY -= zoomSize[zoomLevel];
		Log.v("Atlas", ">>>>>>> relevant change in the level:" + String.valueOf(zoomSize[zoomLevel]));

		long zoomedWidth = (long) ((long) Math.abs((long) mapRect.maxY
				- (long) mapRect.minY) * this.aspectRatio);

		int step = (int) (initialWidth - zoomedWidth) / 2;
		mapRect.minX += step;
		mapRect.maxX -= step;

		this.zoomLevel++;
		Log.v("Atlas", ">>>>>>>new zoomLevel:" + String.valueOf(zoomLevel));

		renderScene();
	}

	public void zoomOut() {

		if (this.zoomLevel == MIN_ZOOM_LEVEL)
			return;

		this.zoomLevel--;

		mapRect.minY -= zoomSize[zoomLevel];
		mapRect.maxY += zoomSize[zoomLevel];

		long zoomedWidth = (long) (Math.abs((long) mapRect.maxY - mapRect.minY) * this.aspectRatio);
		long initialWidth = (long) Math.abs((long) mapRect.maxX - mapRect.minX);

		int size = (int) (zoomedWidth - initialWidth) / 2;
		mapRect.minX -= size;
		mapRect.maxX += size;
		
		Log.d("MAP", "Write code for checking if map data should be fetched");
		if (dataFetcher == null || !dataFetcher.isAlive()) {
			dataFetcher = new Thread(new DataFetcher((double) mapRect.minY / 1000000, (double) mapRect.minX / 1000000,
					(double) mapRect.maxY / 1000000, (double) mapRect.maxX / 1000000, this));
			dataFetcher.start();
		}
		else {
			nextMinY = (double)mapRect.minY / 10000000;
			nextMaxY = (double)mapRect.maxY / 10000000;
			nextMinX = (double)mapRect.minX / 10000000;
			nextMaxX = (double)mapRect.maxX / 10000000;
		}
		renderScene();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		super.surfaceChanged(holder, format, w, h);
		transformation.windowHeight = h;
		transformation.windowWidth = w;
		this.aspectRatio = (float) w / h;

	}
	
	private void pan(int screenDx, int screenDy) {

		short zoomLevelLocal = (short) this.zoomLevel;
		int stepX, stepY;

		stepX = transformation.screenToWorldX(0, mapRect.maxX - mapRect.minX, -screenDx);
		stepY = transformation.screenToWorldY(0, mapRect.maxY - mapRect.minY, transformation.windowHeight - screenDy);

		mapRect.minX += stepX;
		mapRect.maxX += stepX;

		mapRect.minY += stepY;
		mapRect.maxY += stepY;

		this.renderer.setMap(mapRect, zoomLevelLocal, !this.mapPanning);
		this.requestRender();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			int dx = x - this.prevX;
			int dy = y - this.prevY;
			if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
				this.mapPanning = true;
				pan(2 * dx, 2 * dy);
			}
			break;

		case MotionEvent.ACTION_UP:
			if(!mapPanning) {
				checkForClickedItem(x, y);
			}
			else {
				Log.d("MAP", "Code for checking if map data should be fetched");
				if (dataFetcher == null || !dataFetcher.isAlive()) {
					dataFetcher = new Thread(new DataFetcher((double) mapRect.minY / 1000000, (double) mapRect.minX / 1000000,
							(double) mapRect.maxY / 1000000, (double) mapRect.maxX / 1000000, this));
					dataFetcher.start();
				}
				else {
					nextMinY = (double)mapRect.minY / 10000000;
					nextMaxY = (double)mapRect.maxY / 10000000;
					nextMinX = (double)mapRect.minX / 10000000;
					nextMaxX = (double)mapRect.maxX / 10000000;
				}
			}
			this.mapPanning = false;
			renderScene();
			
			break;
		}

		this.prevX = x;
		this.prevY = y;
		
		return true;
	}

	private void checkForClickedItem(int x, int y) {
		
		if(listener == null) {
			return;
		}

		int screenX = transformation.screenToWorldX(mapRect.minX, mapRect.maxX, x);
		int screenY = transformation.screenToWorldY(mapRect.minY, mapRect.maxY, y);
		boolean iconFound = false;
		
		for(MapItem mi: iconList) {
			if(screenX >= mi.minX && screenX <= mi.maxX && screenY >= mi.minY && screenY <= mi.maxY) {
				listener.onClick(mi);
				iconFound = true;
			}
		}
		
		if (!iconFound) {
			listener.onClick(screenX, screenY);
		}
	}

	public ArrayList<MapItem> getIconList() {
		return iconList;
	}
	
	public void setListener(OnItemClickedListener listener) {
		this.listener = listener;
	}

	public BoundingBox getMapArea() {
		return new BoundingBox(mapRect);
	}
	
	
}
