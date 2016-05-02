package com.istic.mmm.arcampus.modes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.istic.mmm.arcampus.R;
import com.istic.mmm.arcampus.menus.OptionsMenuFactory;
import com.istic.mmm.arcampus.modes.ar.ARMarker;
import com.istic.mmm.arcampus.modes.ar.EventMarkerManager;
import com.istic.mmm.arcampus.modes.ar.MathUtil;
import com.istic.mmm.arcampus.modes.ar.listener.CameraOnClickListener;
import com.istic.mmm.arcampus.modes.ar.listener.MarkerOnClickListener;
import com.istic.mmm.arcampus.modes.ar.listener.MarkerOnSecondClickListener;
import com.istic.mmm.arcampus.modes.ar.listener.MenuOnClickListener;
import com.istic.mmm.arcampus.shared.PI.GeoPointProxy;
import com.istic.mmm.arcampus.shared.PI.PointsOfInterestFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ScreenModeAR extends Activity implements EventMarkerManager {

    private float mGpsUpdateMoveInterval = 0;
    private long mGpsUpdateTimeInterval = 0;

    private List<ARMarker> markers;
    private int mScreenWidth, mScreenHeight;

    private SensorManager sensorMngr;
    private SensorEventListener sensorLstr;
    private final float[] historyOrentiationValues = { 0, 0, 0 };
    private Context mContext;
    private boolean displayEnable = false;

    // GPS location part
    private Location mLocation = null;
    private LocationListener mlocListener = null;
    private LocationManager mlocManager = null;
    private boolean locationEnable = false, fineLocationEnable = false;

    // PI location part
    private static HashMap<String, GeoPointProxy> mPointsOfInterestHashMap = null;
    private boolean dataLoaded = false;

    @Override
    public void cameraTouched() {

	this.unselectAllMarkers();
	this.refreshDisplay();
    }

    public SensorEventListener createListener() {
	return new SensorEventListener() {
	    @Override
	    public void onAccuracyChanged(final Sensor _sensor,
		    final int _accuracy) {

	    }

	    @Override
	    public void onSensorChanged(final SensorEvent evt) {
		ScreenModeAR.this.sensorMobileChanged(evt.sensor, evt.values);
	    }
	};
    }

    public int getScreenHeight() {

	final DisplayMetrics metrics = new DisplayMetrics();
	((Activity) this).getWindowManager().getDefaultDisplay()
		.getMetrics(metrics);
	return metrics.heightPixels;
    }

    public int getScreenWidth() {

	final DisplayMetrics metrics = new DisplayMetrics();
	((Activity) this).getWindowManager().getDefaultDisplay()
		.getMetrics(metrics);
	return metrics.widthPixels;
    }

    @Override
    public void markerTouched(final ARMarker marker) {

	this.setNewSelectedMarker(marker);
    }

    @Override
    public void markerTouchedSecond(final ARMarker marker) {

	final AlertDialog.Builder dialog = new AlertDialog.Builder(
		this.mContext);
	dialog.setTitle(marker.getTitle());
	dialog.setMessage(marker.getSnippet());

	dialog.setNeutralButton("Show directions", new MenuOnClickListener(
		this, marker));

	dialog.setNegativeButton("Close", new OnClickListener() {

	    @Override
	    public void onClick(final DialogInterface dialog, final int which) {

	    }
	});

	dialog.show();
    }

    @Override
    public void menuTouched(final ARMarker marker) {

	final String pi_name = marker.getTitle();
	final Intent intent = new Intent(ScreenModeAR.this, ScreenModeMap.class);
	final Bundle b = new Bundle();
	b.putString("direction_pi", pi_name);
	intent.putExtras(b);

	Log.d("Mode AR", "starting mode map with: " + pi_name);

	// http://developer.android.com/reference/android/content/Intent.html#setFlags(int)
	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

	ScreenModeAR.this.startActivity(intent);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {

	super.onCreate(savedInstanceState);
	this.setContentView(R.layout.mode_ar);

	this.mContext = this;
	this.markers = new ArrayList<ARMarker>();

	this.mScreenWidth = this.getScreenWidth();
	this.mScreenHeight = this.getScreenHeight();

	ScreenModeAR.mPointsOfInterestHashMap = (new PointsOfInterestFactory())
		.createPointsOfInterest().getPointsOfInterest(this);

	final Bundle bundle = this.getIntent().getExtras();

	if (!this.locationEnable) {
	    this.initLocation();
	    Log.i(Context.ACTIVITY_SERVICE, "initLocation done");
	} else {
	    Log.i(Context.ACTIVITY_SERVICE, "initLocation already done");
	}

	if (!this.dataLoaded) {
	    this.loadData();
	    Log.i(Context.ACTIVITY_SERVICE, "loadData done");
	} else {
	    Log.i(Context.ACTIVITY_SERVICE, "loadData already done");
	}

	if (bundle != null) {

	    Log.i(Context.ACTIVITY_SERVICE,
		    "bundle not null, we need to do sothing in ScreenAR oncreate?");
	}

	if (this.locationEnable && !this.displayEnable) {
	    this.initDisplay();
	    Log.i(Context.ACTIVITY_SERVICE, "initDisplay done");
	} else {
	    if (!this.locationEnable) {
		Log.i(Context.ACTIVITY_SERVICE, "locationEnable not enable");
	    }
	    if (this.displayEnable) {
		Log.i(Context.ACTIVITY_SERVICE, "display not enable");
	    }
	}

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
	super.onCreateOptionsMenu(menu);

	final MenuInflater inflater = this.getMenuInflater();
	inflater.inflate(R.layout.mode_ar_menu, menu);
	return true;
    }

    @Override
    /**
     * Delegate the work to the Menu factory.
     */
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
	super.onMenuItemSelected(featureId, item);

	final boolean b = OptionsMenuFactory.createModeAROptionsMenu()
		.handleUserAction(item.getItemId(), this);
	return b ? b : super.onContextItemSelected(item);
    }

    /**
     * Method to assign GPS prefs.
     */
    public void updateGPSprefs() {
	final int gpsPref = Integer.parseInt(ScreenModePrefs.getGPSPref(this
		.getApplicationContext()));
	switch (gpsPref) {
	case 1:
	    this.mGpsUpdateTimeInterval = 5000; // milliseconds
	    this.mGpsUpdateMoveInterval = 1; // meters
	    break;
	case 2:
	    this.mGpsUpdateTimeInterval = 10000;
	    this.mGpsUpdateMoveInterval = 100;
	    break;
	case 3:
	    this.mGpsUpdateTimeInterval = 125000;
	    this.mGpsUpdateMoveInterval = 1000;
	    break;
	}
    }

    @Override
    protected void onDestroy() {

	super.onDestroy();
	if ((this.mlocManager != null) && (this.mlocListener != null)) {
	    this.mlocManager.removeUpdates(this.mlocListener);
	}
    }

    @Override
    protected void onPause() {

	super.onPause();
	if ((this.mlocManager != null) && (this.mlocListener != null)) {
	    this.mlocManager.removeUpdates(this.mlocListener);
	}
    }

    @Override
    protected void onResume() {
	super.onResume();

	this.initLocation();
    }

    @Override
    protected void onStart() {
	super.onStart();

	this.sensorMngr = (SensorManager) this
		.getSystemService(Context.SENSOR_SERVICE);
	this.sensorLstr = this.createListener();
	this.sensorMngr.registerListener(this.sensorLstr,
		this.sensorMngr.getDefaultSensor(Sensor.TYPE_ORIENTATION),
		SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStop() {
	super.onStop();
	this.sensorMngr = (SensorManager) this
		.getSystemService(Context.SENSOR_SERVICE);
	this.sensorMngr.unregisterListener(this.sensorLstr,
		this.sensorMngr.getDefaultSensor(Sensor.TYPE_ORIENTATION));
    }

    private void gpsMobileChanged(final Location location) {

	if (location != null) {
	    this.mLocation = location;

	    if (this.fineLocationEnable) {
		final Iterator<ARMarker> iter = this.markers.iterator();
		while (iter.hasNext()) {
		    final ARMarker marker = iter.next();
		    final GeoPointProxy markerGeo = marker.getGeoPoint();
		    if (marker.isSelected()) {
			Log.i(Context.ACTIVITY_SERVICE, marker.toString());
		    }
		    marker.setDistanceToUser(MathUtil.getDistance(markerGeo,
			    this.mLocation));
		}
		Collections.sort(this.markers);
	    }

	} else {
	    Log.i(Context.ACTIVITY_SERVICE,
		    "updatePositions with null location");
	}
    }

    private void initDisplay() {

	final View camera = this.findViewById(R.id.camera);
	camera.setOnClickListener(new CameraOnClickListener(this));
	for (final ARMarker marker : this.markers) {

	    marker.getView().setOnClickListener(
		    new MarkerOnClickListener(this, marker));
	    marker.getSelectedView().setOnClickListener(
		    new MarkerOnSecondClickListener(this, marker));
	    this.refreshMarker(marker);
	}
	this.displayEnable = true;
    }

    private void initLocation() {

	this.mlocManager = (LocationManager) this
		.getSystemService(Context.LOCATION_SERVICE);
	final Criteria criteria = new Criteria();
	criteria.setAccuracy(Criteria.ACCURACY_FINE);
	final String provider = this.mlocManager
		.getBestProvider(criteria, true);
	final Location location = this.mlocManager
		.getLastKnownLocation(provider);

	this.mlocListener = new LocationListener() {
	    @Override
	    public void onLocationChanged(final Location location) {

		if (location != null) {
		    ScreenModeAR.this.gpsMobileChanged(location);
		} else {
		    Log.i(Context.ACTIVITY_SERVICE,
			    "onLocationChanged location with null location");
		}
	    }

	    @Override
	    public void onProviderDisabled(final String provider) {

		Toast.makeText(ScreenModeAR.this.getApplicationContext(),
			"The provider '" + provider + "' has been disabled!",
			Toast.LENGTH_LONG).show();
	    }

	    @Override
	    public void onProviderEnabled(final String provider) {

		Toast.makeText(ScreenModeAR.this.getApplicationContext(),
			"The provider '" + provider + "' has been enabled!",
			Toast.LENGTH_LONG).show();
	    }

	    @Override
	    public void onStatusChanged(final String provider,
		    final int status, final Bundle extras) {

		Log.i("***", "The provider '" + provider + "' new status is: "
			+ status);
	    }
	};

	if (location != null) {
	    this.gpsMobileChanged(location);
	    this.locationEnable = true;

	}

	if (this.locationEnable
		&& provider.equals(LocationManager.GPS_PROVIDER)) {
	    this.fineLocationEnable = true;
	    this.mlocManager.requestLocationUpdates(
		    LocationManager.GPS_PROVIDER, this.mGpsUpdateTimeInterval,
		    this.mGpsUpdateMoveInterval, this.mlocListener);
	} else {
	    Toast.makeText(
		    this.getApplicationContext(),
		    "Sorry! Your fine location is not available yet! Please try later and check your GPS.",
		    Toast.LENGTH_LONG).show();
	    this.mlocManager.requestLocationUpdates(
		    LocationManager.NETWORK_PROVIDER,
		    this.mGpsUpdateTimeInterval, this.mGpsUpdateMoveInterval,
		    this.mlocListener);
	}
    }

    /**
     * private method that load all GeoPoints
     */
    private void loadData() {

	for (final String key : ScreenModeAR.mPointsOfInterestHashMap.keySet()) {

	    final GeoPointProxy geoPoint = ScreenModeAR.mPointsOfInterestHashMap
		    .get(key);
	    ViewGroup markerView, markerSelectedView;

	    // markerView part
	    if (geoPoint.getIcon().equals("resto")) {
		markerView = (ViewGroup) this.getLayoutInflater().inflate(
			R.layout.mode_ar_marker_type1_template, null);
	    } else if (geoPoint.getIcon().equals("building")) {
		markerView = (ViewGroup) this.getLayoutInflater().inflate(
			R.layout.mode_ar_marker_type2_template, null);
	    } else {
		markerView = (ViewGroup) this.getLayoutInflater().inflate(
			R.layout.mode_ar_marker_default_template, null);
	    }

	    TextView titleView, locationView;
	    ImageView imgView;

	    locationView = (TextView) markerView
		    .findViewById(R.id.text_location);
	    locationView.setId(View.NO_ID);
	    locationView.setText("ND");

	    imgView = (ImageView) markerView.findViewById(R.id.img_template);
	    imgView.setId(View.NO_ID);

	    titleView = (TextView) markerView.findViewById(R.id.text_template);
	    titleView.setId(View.NO_ID);
	    String name = geoPoint.getName();
	    if (name.length() > 10) {
		name = name.subSequence(0, 10).toString();
		name += "...";
	    }
	    titleView.setText(name);

	    // markerSelectedView part
	    markerSelectedView = (ViewGroup) this.getLayoutInflater().inflate(
		    R.layout.mode_ar_marker_selected_template, null);

	    TextView titleViewSelected, locationViewSelected;
	    ImageView imgViewSelected;

	    locationViewSelected = (TextView) markerSelectedView
		    .findViewById(R.id.text_location_selected);
	    locationViewSelected.setId(View.NO_ID);
	    locationViewSelected.setText("ND");

	    imgViewSelected = (ImageView) markerSelectedView
		    .findViewById(R.id.img_template_selected);
	    imgViewSelected.setId(View.NO_ID);

	    titleViewSelected = (TextView) markerSelectedView
		    .findViewById(R.id.text_template_selected);
	    titleViewSelected.setId(View.NO_ID);
	    titleViewSelected.setText(geoPoint.getName());

	    LayoutParams lp = new FrameLayout.LayoutParams(
		    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
		    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
	    lp.gravity = Gravity.CENTER;
	    markerView.setLayoutParams(lp);

	    lp = new FrameLayout.LayoutParams(
		    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
		    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
	    lp.gravity = Gravity.CENTER;
	    markerSelectedView.setLayoutParams(lp);

	    // create marker
	    final ARMarker marker = new ARMarker(markerView, locationView,
		    markerSelectedView, locationViewSelected);

	    marker.setGeoPoint(geoPoint);

	    if (this.fineLocationEnable) {
		marker.setDistanceToUser(MathUtil.getDistance(geoPoint,
			this.mLocation));
	    }

	    this.markers.add(marker);// keep in markers map

	}
	Collections.sort(this.markers);
	this.dataLoaded = true;
    }

    private void refreshDisplay() {

	for (final ARMarker marker : this.markers) {

	    this.refreshMarker(marker);
	}
    }

    private void refreshMarker(final ARMarker marker) {

	if (!marker.isRefreshed()) {

	    final ViewGroup parent = (ViewGroup) this
		    .findViewById(R.id.ar_layout_root);

	    if (marker.isSelected()) {

		parent.removeView(marker.getView());
		parent.addView(marker.getSelectedView());

	    } else {

		parent.removeView(marker.getSelectedView());
		parent.addView(marker.getView());
	    }
	    marker.setRefreshed();
	}
    }

    private void sensorMobileChanged(final Sensor sensor, final float[] values) {

	if ((sensor.getType() == Sensor.TYPE_ORIENTATION)
		&& (this.mLocation != null)) {
	    // North=0, East=90, South=180, West=270 => azimut between magnetic
	    // north direction and z-axis.
	    final float azimut;
	    final float roll;
	    final float pitch;

	    if (Math.abs(this.historyOrentiationValues[0] - values[0]) > 1.1) {
		azimut = values[0] - 270;
		this.historyOrentiationValues[0] = values[0];
	    } else {
		azimut = this.historyOrentiationValues[0] - 270;
	    }

	    // Rotation around y-axis (-90 to 90).
	    if (Math.abs(this.historyOrentiationValues[2] - values[2]) > 0.9) {
		roll = values[2];
		this.historyOrentiationValues[2] = values[2];
	    } else {
		roll = this.historyOrentiationValues[2];
	    }

	    // Rotation around x-axis (-180 to 180).
	    if (Math.abs(this.historyOrentiationValues[1] - values[1]) > 0.9) {
		pitch = Math.abs(values[1]);
		this.historyOrentiationValues[1] = values[1];
	    } else {
		pitch = Math.abs(this.historyOrentiationValues[1]);
	    }

	    final Iterator<ARMarker> iter = this.markers.iterator();
	    while (iter.hasNext()) {
		final ARMarker marker = iter.next();

		if (marker.isSelected()) {
		    MathUtil.moveTrackedSpot(marker.getCurrentView(),
			    marker.getGeoPoint(), azimut, this.mLocation,
			    this.mScreenWidth, roll, this.mScreenHeight, pitch);
		} else {
		    MathUtil.moveSpot(marker.getCurrentView(),
			    marker.getGeoPoint(), azimut, this.mLocation,
			    this.mScreenWidth, roll, this.mScreenHeight, pitch);
		}
	    }
	}
    }

    private void setNewSelectedMarker(final ARMarker newSelectedmarker) {

	this.unselectAllMarkers();

	newSelectedmarker.setSelected(true);
	this.refreshDisplay();

    }

    private void unselectAllMarkers() {

	for (final ARMarker marker : this.markers) {
	    marker.setSelected(false);
	}
    }
}

