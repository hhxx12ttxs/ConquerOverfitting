package digisky.cpro.wma;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.Overlay;


public class GoogleMapActivity extends MapActivity {
	MapView mapView;
	LinearLayout zoomLayout;
	MapController mc;
	GeoPoint p;
	ArrayList<GeoPoint> mGeoPointList;
	private static final int MENU_UPDATE = 0;
	private static final int MENU_BACK= 1;
    class MapOverlay extends Overlay
    {
        @Override
        public boolean draw(Canvas canvas, MapView mapView, 
        boolean shadow, long when) 
        {
            super.draw(canvas, mapView, shadow);                   
 
            //---translate the GeoPoint to screen pixels---
            Point screenPts = new Point();
            Paint paint = new Paint();
            for (int i = 0 ; i< mGeoPointList.size(); i++){
	            mapView.getProjection().toPixels(mGeoPointList.get(i), screenPts);
	 
	            //---add the marker---
	            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pinicon);            
	            canvas.drawBitmap(bmp,null, new RectF(screenPts.x,screenPts.y-32,screenPts.x+32,screenPts.y), null);
	            String deviceName = WMA.mDeviceList.get(i).getDeviceName();
	            paint.setTextSize(15);
	            paint.setColor(Color.BLUE);
	            canvas.drawText(deviceName, screenPts.x+32,screenPts.y-32, paint);
	            canvas.drawText(WMA.mDeviceList.get(i).getPhoneNumber(), screenPts.x+32,screenPts.y-10, paint);
            }
            return true;
        }
//        @Override
///*        
// * 		Hien thi Longitude vs Latitude khi touch
//        public boolean onTouchEvent(MotionEvent event, MapView mapView) 
//        {   
//            //---when user lifts his finger---
//            if (event.getAction() == 1) {                
//                GeoPoint p = mapView.getProjection().fromPixels(
//                    (int) event.getX(),
//                    (int) event.getY());
//                    Toast.makeText(getBaseContext(), 
//                        p.getLatitudeE6() / 1E6 + "," + 
//                        p.getLongitudeE6() /1E6 , 
//                        Toast.LENGTH_SHORT).show();
//            }                            
//            return false;
//        }     
//*/
//        /* Display address name */
//        public boolean onTouchEvent(MotionEvent event, MapView mapView) 
//        {   
//            //---when user lifts his finger---
//            if (event.getAction() == 1) {                
//                GeoPoint p = mapView.getProjection().fromPixels(
//                    (int) event.getX(),
//                    (int) event.getY());
// 
//                Geocoder geoCoder = new Geocoder(
//                    getBaseContext(), Locale.getDefault());
//                try {
//                    List<Address> addresses = geoCoder.getFromLocation(
//                        p.getLatitudeE6()  / 1E6, 
//                        p.getLongitudeE6() / 1E6, 1);
// 
//                    String add = "";
//                    if (addresses.size() > 0) 
//                    {
//                        for (int i=0; i<addresses.get(0).getMaxAddressLineIndex(); 
//                             i++)
//                           add += addresses.get(0).getAddressLine(i) + "\n";
//                    }
// 
//                    Toast.makeText(getBaseContext(), add, Toast.LENGTH_SHORT).show();
//                }
//                catch (IOException e) {                
//                    e.printStackTrace();
//                }   
//                return true;
//            }
//            else                
//                return false;
//        }
        
    } 
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        mapView = (MapView) findViewById(R.id.mapView);
        zoomLayout = (LinearLayout)findViewById(R.id.zoom);  
        View zoomView = mapView.getZoomControls(); 
        WMA.mCurrentContext=this;
        zoomLayout.addView(zoomView, 
            new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, 
                LayoutParams.WRAP_CONTENT)); 
        mapView.displayZoomControls(true);  
        
        mapView.setSatellite(false);
//        mapView.setTraffic(false);
        mapView.setStreetView(true);
        
        mc = mapView.getController();
        //javascript:void(prompt('',gApplication.getMap().getCenter()));
        
		ArrayList<DeviceInfo> mDeviceList = WMA.mDeviceList;
        //String coordinates[] = {"21.030673628606102", "105.77997207641602"};
        if (mDeviceList!=null){
	        mGeoPointList = new ArrayList<GeoPoint>();
	        for (int i = 0 ;i < mDeviceList.size(); i++){
		        double lat = mDeviceList.get(i).mLatitude;
		        double lng = mDeviceList.get(i).mLongitude;		        	
	        	GeoPoint geoPoint = new GeoPoint(
			            (int) (lat * 1E6), 
			            (int) (lng * 1E6));
	        	mGeoPointList.add(geoPoint);
	        }
	        if (mGeoPointList.size()!=0){
	        	mc.animateTo(mGeoPointList.get(0));
	        }
        }
        mc.setZoom(14); 

        MapOverlay mapOverlay = new MapOverlay();
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(mapOverlay);        
      
        mapView.invalidate();

    }
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);

//		menu.add(0, MENU_EDIT, 0, R.string.menu_edit)
//		       .setIcon(android.R.drawable.ic_menu_edit)
//		       .setAlphabeticShortcut('E');		
        menu.add(0, MENU_UPDATE, 0, "Update all devices")
		        .setIcon(android.R.drawable.ic_menu_rotate)
		        .setAlphabeticShortcut('U');
		menu.add(0, MENU_BACK, 0, R.string.menu_back)
		        .setIcon(android.R.drawable.ic_menu_revert)
		        .setAlphabeticShortcut('B');		
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_UPDATE:
            	processUpdate();
                return true;
            case MENU_BACK:
            	finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void processUpdate(){
    	WMA.mCommandDevice.clear();
    	for ( int i = 0 ; i < WMA.mDeviceList.size();i++){
    		WMA.mCommandDevice.add(i);
    	}
    	Intent intent = new Intent(this, GPSResolver.class);
    	startActivity(intent);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	WMA.mCurrentContext=this;
        mc = mapView.getController();
        //javascript:void(prompt('',gApplication.getMap().getCenter()));
        
		ArrayList<DeviceInfo> mDeviceList = WMA.mDeviceList;
        //String coordinates[] = {"21.030673628606102", "105.77997207641602"};
        if (mDeviceList!=null){
	        mGeoPointList = new ArrayList<GeoPoint>();
	        for (int i = 0 ;i < mDeviceList.size(); i++){
		        double lat = mDeviceList.get(i).mLatitude;
		        double lng = mDeviceList.get(i).mLongitude;		        	
	        	GeoPoint geoPoint = new GeoPoint(
			            (int) (lat * 1E6), 
			            (int) (lng * 1E6));
	        	mGeoPointList.add(geoPoint);
	        }
	        if (mGeoPointList.size()!=0){
	        	mc.animateTo(mGeoPointList.get(0));
	        }
        }
        mc.setZoom(14); 

        MapOverlay mapOverlay = new MapOverlay();
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(mapOverlay);        
      
        mapView.invalidate();
    }
}

