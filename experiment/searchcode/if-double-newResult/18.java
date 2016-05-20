package fi.cie.chiru.servicefusionar.sensors;

import android.content.Context;
/** if we want to have Landscape/Portrait detection */
//import android.view.Display;
//import android.view.WindowManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.FloatMath;
import java.util.concurrent.atomic.AtomicBoolean;

public class Orientator implements SensorEventListener, LocationListener
{

	OrientationListener myListener;
	Thread myThread;
	Boolean shouldRun;
	private static final AtomicBoolean computes = new AtomicBoolean(false);
	Context myContext;

	private final SensorManager mSensorManager;
	private final Sensor mAll;
	private final Sensor mAccelerometerSensor;
	private final Sensor mLightSensor;
	private final Sensor mGyroscopeSensor;
	private final Sensor mGravitySensor;
	private final Sensor mMagneticFieldSensor;
	private final Sensor mPressureSensor;
	// Fussion Sensors
	private final Sensor mLinearAccelerationSensor;
	private final Sensor mRotationVectorSensor;

	// This can be used with OpenGL as rotation matrix (4x4)
	private final float[] mRotationMatrix = new float[16];
	//private final Sensor mTemperatureSensor;
	private LocationManager locationManager;
	private Location mLocation;
	private GeomagneticField magField;
	private long magFieldTime=0;

	// We use 4 x 4 to access the android.opengl.Matrix operations
	private float[] mrotationMatrixR = new float[16];
	private float[] mrotationMatrixI = new float[16];
	private float[] mrotationMatrixO = new float[16];
	private float[] mGravity = new float[3];
	private float[] mGeoMagnetic = new float[3];
	private float tilt=0;
	private float[] orientationValues = new float[3];

	private final int MAGFIELD_TMOUT=(3600*10000); /* 10 minutes from msecs */

	public Orientator(OrientationListener listener, Context context)
	{
		myListener = listener;
		myContext = context;

		mSensorManager = (SensorManager)myContext.getSystemService(Context.SENSOR_SERVICE);
		// We enable them all...
		mAll = mSensorManager.getDefaultSensor(Sensor.TYPE_ALL);
		// and separately...
		mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		mMagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mPressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		// Fussion Sensors
		mLinearAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		// we use compass instead of gyroscope (as they are not available).
		locationManager = (LocationManager)myContext.getSystemService(Context.LOCATION_SERVICE);
	}

	public void Start()
	{
		shouldRun = true;
		Initialize();
		(myThread = new Thread() {
			public void run() {
				try {
					myListener.newStatus(3, "Running");
					while(shouldRun) {
						MonitorProc();
					}
					myListener.newStatus(4, "Completing");
				} catch(final Exception e) {
					myListener.newStatus(400, "An error has happened while monitoring. Please contact the lousy programmer who did this.");
				}
				Uninitialize();
			}
		}).start();
	}

	public void Stop()
	{
		shouldRun = false;
		try {
			myThread.join();
		} catch(InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//It did not complete nicely!
		}
		myListener.newStatus(5, "Stopped");
		myThread = null;
	}

	private void Initialize()
	{
		myListener.newStatus(1, "Started initializing");
		mSensorManager.registerListener(this, mAll, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mGyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mPressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mLinearAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
		//mSensorManager.registerListener(this, mTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0,this);
		myListener.newStatus(2, "Initialized");
	}

	private void Uninitialize()
	{
		mSensorManager.unregisterListener(this);
		locationManager.removeUpdates(this);
	}

	private void MonitorProc()
	{
		sleep(100);
		//myListener.newResult("some result " +  ((int)(Math.random()
		//* 100 + 1)));
		//This is possibly not even needed, but let it be here for a while if we do need to poll something.
		//Also, if this is not needed, we will need to create different threading model, to avoid jamming UI thread.
		//Of course, final solution can include a service.

		//Check all the other SensorManager.getXXX methods.
		//SensorManager.getRotationMatrix(mrotationMatrixR, mrotationMatrixI,
		//								mGravity, mGeoMagnetic);
		//SensorManager.remapCoordinateSystem(mrotationMatrixR, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z, mrotationMatrixO);
		//SensorManager.getOrientation(mrotationMatrixO, orientationValues);
		//myListener.newResult("Orientation", orientationValues);
		//myListener.newResult("Gravity", mGravity);
	}

	private void sleep(int miliseconds)
	{
		try {
			Thread.sleep(1000);
		} catch(InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//SensorEventListener

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		if(sensor!=null&&sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD) {
			myListener.isCalibrated(sensor.getName(), accuracy>2);
		}
		//myListener.newResult("New accuracy: " + accuracy);
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if(!computes.compareAndSet(false, true)) {
			return;
		}
		float [] values=event.values.clone();
		myListener.newResult(event.sensor.getName(), values);

		if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD&&event.accuracy<SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
			myListener.isCalibrated(event.sensor.getName(), event.accuracy>2);
		}
		switch(event.sensor.getType()) {
		case Sensor.TYPE_ROTATION_VECTOR:
			// This is for OpenGL rotation matrix (helper utility?)
			SensorManager.getRotationMatrixFromVector(mRotationMatrix , values);
			myListener.newResult("Rotation Matrix", mRotationMatrix);
			break;
		case Sensor.TYPE_ACCELEROMETER:
			if(mGravity==null) { // first run
				mGravity=values;
			} else {
				lowPassFilter(0.5f, 1.0f, values, mGravity, mGravity);
				myListener.newResult("Acc_filtered", mGravity);
			}
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			if(mGeoMagnetic==null) { // first run
				mGeoMagnetic=values;
			} else {
				if(tilt!=1) { // stay unchanged if tilted!
					lowPassFilter(2.0f, 4.0f, values, mGeoMagnetic, mGeoMagnetic);
					myListener.newResult("Geo_filtered", mGeoMagnetic);
				}
			}
			break;
		}

		// Check cached geo-magnetic field
		checkMagField();
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ||
				event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			// We should use some filters here
			boolean ret=SensorManager.getRotationMatrix(mrotationMatrixR, mrotationMatrixI,	mGravity, mGeoMagnetic);
			if(ret) {
				// Translate the rotation matrices from X->Z and Y->X (landscape)
				// Using the camera (Y axis along the camera's axis)
				// for an augmented reality application where the rotation angles are needed
				SensorManager.remapCoordinateSystem(mrotationMatrixR, SensorManager.AXIS_X, SensorManager.AXIS_Z, mrotationMatrixO);
				// Orientation: azimuth, pitch, roll: z-rotation, x-rotation, y-rotation (in RAD, CCW)
				//SensorManager.getOrientation(mrotationMatrixR, orientationValues);
				//When remapped, use the new ones
				SensorManager.getOrientation(mrotationMatrixO, orientationValues);
				// Show orientation raw (un-compensated)
				/*
				myListener.newResult("Orientation Raw.", orientationValues); // Dispaly orintation when changes happen
				// Get magnetic declination fix and show in degrees
				myListener.newResult("Orientation Rad.", new float[] {
										 orientationValues[0]+(float)Math.toRadians(magField.getDeclination()),
										 orientationValues[1], orientationValues[2]});
				myListener.newResult("Orientation Deg", new float[] {
										 (float)Math.toDegrees(orientationValues[0])+(float)magField.getDeclination(),
										 (float)Math.toDegrees(orientationValues[1]), (float)Math.toDegrees(orientationValues[2])});
				*/
				/** If we want to have Landscape/Portrait */
				/*
				WindowManager myWM = (WindowManager)myContext.getSystemService(Context.WINDOW_SERVICE);
				Display myDisplay = myWM.getDefaultDisplay();
				myListener.newResult("Orientation Display",new float[] {(float)myDisplay.getRotation()});
				*/
				tilt=(float)orientationValues[1];
				tilt=(tilt<-1||tilt>1.10)?1:0;
				//myListener.newResult("Tilt status", new float[] { tilt });
				myListener.isTilt(tilt>0);
				/** Display orientation only if not tilted */
				if(tilt!=1){
					double angle=(360+Math.toDegrees(orientationValues[0])+magField.getDeclination())%360;
					angle=angle-(angle%5);
					myListener.newOrientation(angle);
				}
			}
		}
		computes.set(false);
	}

	//LocationListener

	@Override
	public void onLocationChanged(Location loc)
	{
		//myListener.newResult("Location", new float[] {(float) loc.getLatitude(), (float) loc.getLongitude(), (float) loc.getAltitude()});
		myListener.newLocation(loc.getLatitude(), loc.getLongitude());
		//myListener.newResult("Speed", new float[] {(float) loc.getSpeed() * 3.6f, (float) loc.getSpeed()});
		// save location
		mLocation = loc;
	}

	/** Check the geo-magnetic field. If too old, update */
	private void checkMagField()
	{
		long now = System.currentTimeMillis();
		// check timeout
		if(now-magFieldTime<MAGFIELD_TMOUT) {
			return;
		}
		// make sure we have something
		if(mLocation==null) {
			magField=new GeomagneticField((float)65.01,(float)25.47,(float)1, System.currentTimeMillis()); // Oulu center
			return; // don't update the time
		} else {
			// Get the geomag data.
			magField=new GeomagneticField((float)mLocation.getLatitude(), (float)mLocation.getLongitude(), (float)mLocation.getAltitude(), now);
		}
		magFieldTime = now;
	}

	/** Basic low pass filter */
	private void lowPassFilter(float low, float high, float[] current, float[] previous, float[] next) {
		if (current==null||previous==null) throw new NullPointerException("Input array must be non-NULL");
        if (current.length!=previous.length) throw new IllegalArgumentException("Arrays must have same length");
        float alpha = computeAlpha(low, high, current, previous);

        for (int i = 0; i < current.length; i++) {
            next[i]=previous[i]+alpha*(current[i]-previous[i]);
        }
	}
	private static final float computeAlpha(float low, float high, float[] c, float[] p) {
        float distance = FloatMath.sqrt((float)(Math.pow((double)(p[0]-c[0]),2d)+Math.pow((double)(p[1]-c[1]),2d)+Math.pow((double)(p[2]-c[2]),2d)));
        if (distance < low) {
            return 0.001f; // steady
        } else if (distance >= low || distance < high) {
            return 0.3f; // start moving
        }
        return 0.6f; // moving!
    }

	@Override
	public void onProviderDisabled(String provider)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		// TODO Auto-generated method stub
	}
}


