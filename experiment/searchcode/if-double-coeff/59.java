package org.gtug.classes;

import org.gtug.beans.Bean;
import org.gtug.beans.DBCollageContent;
import org.gtug.beans.DBCollageObject;
import org.gtug.beans.DBImageObject;
import org.gtug.database.Provider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.test.MoreAsserts;
import android.util.Config;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ImageObject extends CollageObject {

	public ImageObject(int x, int y, int resource, Context ctx) {
		super(x, y, resource, ctx);
		
	}

	public void setParent(Collage parent){
		this.parent = parent;
	}
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setSource(Bitmap source){
		this.source = source;
		currentBitmap = source.copy(source.getConfig(), false);
		width = source.getWidth();
		height = source.getHeight();
		Log.d("collage","width - "+Integer.toString(width));
	}
	public Uri getSourceURI() {
		return sourceURI;
	}

	public void setSourceURI(Uri sourceURI) {
		this.sourceURI = sourceURI;
	}

	@Override
	public View generateView(Boolean isDraggable) {
		picture = new ImageView(ctx);
		picture.setImageBitmap(source);
		if(isDraggable){
//			matrix.setTranslate(1f, 1f);
//			picture.setImageMatrix(matrix);
			setDraggable();
		}
		return picture;
	}
	
	public void setDraggable() {
		picture.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getPointerCount()==2) mode = ZOOM;
				if(event.getPointerCount()==1) mode = DRAG;
				switch(event.getAction()){
					case MotionEvent.ACTION_DOWN:{
						switch(mode) {
						case DRAG:
							dragStart(event);
							break;
						case ZOOM:
							zoomStart(event);
							break;
						}
						
						break;
					}
					
					
					case MotionEvent.ACTION_POINTER_2_DOWN:
						if(mode == ZOOM){
							zoomStart(event);
						}
						break;
					
					case MotionEvent.ACTION_UP:{
						Log.d("collage","up!");
						width = currentBitmap.getWidth();
						height = currentBitmap.getHeight();
						break;
					}
					
					case MotionEvent.ACTION_MOVE:{
						switch(mode) {
						case DRAG:
							drag(event);
							break;
						case ZOOM:
							zoom(event);
							break;
						}
						break;
					}
					
					
				}
				
				return true;
			}
		});
	}
	
	private void dragStart(MotionEvent event){
		offsetX = (int) event.getX();
		offsetY = (int) event.getY();
		parent.changeZIndexOf(ImageObject.this);
		parent.getBg().bringChildToFront(picture);
	}
	
	private void zoomStart(MotionEvent event){
		oldDistance = getDistance(event);
		Log.d("collage",Double.toString(oldDistance));
	}
	
	private double getDistance(MotionEvent event) {
		return Math.sqrt(Math.pow((event.getX(0) - event.getX(1)), 2)+Math.pow((event.getY(0) - event.getY(1)), 2));
		//return Math.abs((event.getX(0)-event.getX(1)));
	}
	
	private void drag(MotionEvent event){
		x = (int) event.getRawX() - offsetX;
		y = (int) event.getRawY() - offsetY;
		setCoordsToView();
	}
	
	private void zoom(MotionEvent event){
		newDistance = getDistance(event);
		double coeff = newDistance/oldDistance;
		if(coeff>0){
			currentBitmap = Bitmap.createScaledBitmap(source, (int)(width*coeff), (int)(height*coeff), true);
			picture.setImageBitmap(currentBitmap);
		}
	}
	
	public void setCoordsToView() {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) picture.getLayoutParams();
		params.leftMargin = x;
		params.topMargin = y;
		params.rightMargin = -x;
		params.bottomMargin = -y;
		picture.setLayoutParams(params);
	}
	
	@Override
	public void moveToPoint(int x, int y) {
		this.x = x;
		this.y = y;
		setCoordsToView();
	}
	
	@Override
	public Bean save(long id,Provider p, String type) {
		DBCollageObject collageObject = (DBCollageObject)super.save(id,p,"image");
		long objId = p.addObject(collageObject);
		p.addCollageContent(new DBCollageContent(id, objId));
		p.addImageObject(makeBean(objId));
		return null;
	}

	private DBImageObject makeBean(long id) {
		return new DBImageObject(width, height, path, id, scalingCoeff);
	}
	
	@Override
	public void show(RelativeLayout layoutParent) {
		super.show(layoutParent);
		Log.d("collage","test");
		setSourceFromPath(parent.width,parent.height);
		
		picture = new ImageView(ctx);
		picture.setImageBitmap(source);
		layoutParent.addView(picture);
		setCoordsToView();
	}
	
	private void setSourceFromPath(int dispWidth,int dispHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		source = BitmapFactory.decodeFile(path, options);
		int dstWidth = 0;
		int dstHeight = 0;
		if(scalingCoeff == 0) {
			scalingCoeff = scaleBitmap(options.outWidth, options.outHeight, dispWidth, dispHeight);
			 dstWidth = options.outWidth/scalingCoeff;
			 dstHeight = options.outHeight/scalingCoeff;
		} else{
			dstWidth = width;
			dstHeight = height;
		}
		
		options.inSampleSize = 	scalingCoeff;
		options.inJustDecodeBounds = false;
		Bitmap resultBitmap = BitmapFactory.decodeFile(path,options);
		setSource(Bitmap.createScaledBitmap(resultBitmap, dstWidth, dstHeight, true));
	}
	
	private int scaleBitmap(int bmWidth,int bmHeight,int displayWidth,int displayHeight) {
		double scalingCoeff = 1;
		double scalingCoeffX = (double)bmWidth/(double)displayWidth;
		double scalingCoeffY = (double)bmHeight/(double)displayHeight;
		if(bmWidth>displayWidth && bmHeight>displayHeight){
			scalingCoeff = (scalingCoeffX<scalingCoeffY)?scalingCoeffY:scalingCoeffX;			
		} else if(bmWidth>displayWidth) {
			scalingCoeff = scalingCoeffX;
		} else if(bmHeight>displayHeight) {
			scalingCoeff = scalingCoeffY;
		}
			
		return (int) Math.round(scalingCoeff);
	}

	
		
	public int getScalingCoeff() {
		return scalingCoeff;
	}

	public void setScalingCoeff(int scalingCoeff) {
		this.scalingCoeff = scalingCoeff;
	}

	@Override
	public void stop() {
		super.stop();
		source.recycle();
	}

	private final int ZOOM = 2;
	private final int DRAG = 1;
	private final int NONE = 0;
	
	private int mode = NONE;
	
	private double oldDistance = 0;
	private double newDistance = 0;
	
	private int offsetX = 0;
	private int offsetY = 0;

	private int width;
	private int height;
	private Uri sourceURI;
	public String path;
	private int scalingCoeff = 0;
	Bitmap source;
	private Bitmap currentBitmap;
	private Collage parent;
	public ImageView picture;
}

