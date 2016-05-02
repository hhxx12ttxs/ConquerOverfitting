package es.grp5.android;

import es.grp5.android.Setting;
import java.util.ArrayList;
import java.util.List;
//import java.math.*;
//import android.util.FloatMath;
import 	java.util.Date;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuInflater;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class GPSTracking extends MapActivity {
	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private Location location;
	LocationListener locationListener;
	LocationListener firstTimeLocationListener;
	List<Overlay> overlays;
	ArrayList<GeoPoint> pointlist = new ArrayList<GeoPoint>();
	protected CountDownTimer locationTimer;
	DrawPointOverlay start;
	DrawPointOverlay pointOverlay;
	int index = 0;
	boolean isRunning;
	double time[] = {0,0};
	boolean firstRun = true;
	int Radius = 6371000; // Earth's Radius (Unit: meter)
	TextView viewSpeed;
	Setting setting;
	int unit = 1;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		isRunning = true;
		setContentView(R.layout.gpstracking); // bind the layout to the activity
		// kiểm tra đơn vị vận tốc
		  Bundle extras=getIntent().getExtras();
		  if(extras != null) unit = extras.getInt("unitSelected");
		
		viewSpeed = (TextView) findViewById(R.id.viewspeed);
		// Configure the Map
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(false);
		mapController = mapView.getController();
		mapController.setZoom(14); // Zoom 1 is world view
		Log.i("done", "1");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}
		Log.i("Done", "2");
		Date dt = new Date(System.currentTimeMillis());
		time[1] =time[0]= dt.getTime()/1000;
	}
	
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"GPS của bạn không được bật, bạn có muốn bật nó không?")
				.setCancelable(false)
				.setPositiveButton("Có", new DialogInterface.OnClickListener() {
					public void onClick(
							final DialogInterface dialog,
							final int id) {
						// startActivity(new
						// Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				})
				.setNegativeButton("Không",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		GeoPoint point = new GeoPoint((int) (21.035 * 1E6),
				(int) (105.850182 * 1E6));
		mapController.animateTo(point, new Message());
		DrawPointOverlay pointOverlay1 = new DrawPointOverlay();
		pointOverlay1.setpoint(point);
		overlays = mapView.getOverlays();
		overlays.clear();
		overlays.add(pointOverlay1);

		Log.i("Done", "4");
		locationListener = new LocationListener() {

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub

			}

			public void onProviderEnabled(String provider) {
				

			}

			public void onProviderDisabled(String provider) {
				


			}

			public void onLocationChanged(Location loc) {
				// TODO Auto-generated method stub
				location = loc;
				if (!isRunning)
					locationManager.removeUpdates(this);
				if (loc.getLatitude() == 0 || loc.getLongitude() == 0) {
				} else {
					Date dt = new Date(System.currentTimeMillis());
					time[1] = dt.getTime()/1000;
					double time_space = time[1] - time[0];
					time[0] = time[1];
					double lat = loc.getLatitude();
					double lon = loc.getLongitude();
					if(!firstRun) {
						double oldLat = pointlist.get(pointlist.size() - 1).getLatitudeE6()/1E6;
						double oldLon = pointlist.get(pointlist.size() - 1).getLongitudeE6()/1E6;
						double latRadian = Math.toRadians(lat - oldLat);
						double lonRadian = Math.toRadians(lon - oldLon);
						double temp = Math.sin(latRadian/2) * Math.sin(latRadian/2) +
								Math.cos(Math.toRadians(oldLat)) * Math.cos(Math.toRadians(lat)) *
								Math.sin(lonRadian/2) * Math.sin(lonRadian/2);
						double distance = Radius * 2 * Math.asin(Math.sqrt(temp));
						
						double speed = distance / time_space;
						//lam tron 2 chu so sau dau phay
						speed = Round(speed,2);
						if(unit == 2)
							viewSpeed.setText("Tốc độ: " + speed + " m/s");
						else if (unit == 1) {
							speed = speed * 3.6;
							speed = Round(speed,2);
							viewSpeed.setText("Tốc độ: " + speed + " km/h");
						}
					}
					firstRun = false;
					GeoPoint geoPoint = new GeoPoint((int) (lat * 1E6),
							(int) (lon * 1E6));
					pointlist.add(geoPoint);
					Log.i("Done", "6");
					mapController.animateTo(geoPoint);
					pointOverlay = new DrawPointOverlay();
					pointOverlay.setpoint(geoPoint);
					overlays.add(pointOverlay);

				}
				
			}
		};
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 10f, locationListener);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1000, 10f, locationListener);
		locationTimer = new CountDownTimer(30000, 5000) {
			@Override
			public void onTick(long millisUntilFinished) {
				if (location != null)
					locationTimer.cancel();
			}
			//public void getSpeed(long time){}

			@Override
			public void onFinish() {
				if (location == null) {
				}
			}
		};
		locationTimer.start();
		Log.i("Done", "5");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isRunning = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// myLocationOverlay.enableMyLocation();
		// myLocationOverlay.enableCompass();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// myLocationOverlay.disableMyLocation();
		// myLocationOverlay.disableCompass();
	}

	class DrawPointOverlay extends Overlay {
		private GeoPoint point;

		public void setpoint(GeoPoint point) {
			this.point = point;
		}

		public GeoPoint getPointToDraw() {
			return point;
		}

		@Override
		public boolean onTap(GeoPoint p, MapView mapView) {
			// TODO Auto-generated method stub
			if (p.getLatitudeE6() == pointlist.get(0).getLatitudeE6()
					&& p.getLongitudeE6() == pointlist.get(0).getLongitudeE6()) {
				Toast.makeText(getBaseContext(), "Starting position", 1).show();
			} else if (p.getLatitudeE6() == pointlist.get(pointlist.size() - 1)
					.getLatitudeE6()
					&& p.getLongitudeE6() == pointlist
							.get(pointlist.size() - 1).getLongitudeE6()) {
				Toast.makeText(getBaseContext(), "Current position", 1).show();
			}
			return super.onTap(p, mapView);
		}

		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
				long when) {
			// TODO Auto-generated method stub
			super.draw(canvas, mapView, shadow);
			Paint paint = new Paint();
			paint.setColor(Color.RED);
			paint.setStyle(Paint.Style.FILL);
			paint.setStrokeWidth(3);
			Point screenPts = new Point();
			mapView.getProjection().toPixels(point, screenPts);
			for (int i = 0; i < pointlist.size() - 1; i++) {
				Point srcPoint = new Point();
				Point desPoint = new Point();
				mapView.getProjection().toPixels(pointlist.get(i), srcPoint);
				mapView.getProjection()
						.toPixels(pointlist.get(i + 1), desPoint);
				canvas.drawLine(srcPoint.x, srcPoint.y, desPoint.x, desPoint.y,
						paint);
			}
			/*
			 * Bitmap bmp = BitmapFactory.decodeResource(getResources(),
			 * R.drawable.marker60x60); canvas.drawBitmap(bmp, screenPts.x - 25,
			 * screenPts.y - 10, null);
			 */
			return true;

		}

	}
	//Hàm làm tròn 2 chữ số thập phân
	public static double Round(double Rval, int Rpl) {
		  double p = (double)Math.pow(10,Rpl);
		  Rval = Rval * p;
		  double tmp = Math.round(Rval);
		  return tmp/p;
		  }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tracking_menu, menu);
		return true; 

	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.settracking:
			Intent i = new Intent(getBaseContext(), GPSTrackingSetting.class);
			i.putExtra("Selected",unit);
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	
	}




}
