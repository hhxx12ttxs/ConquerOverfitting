package exercise.app;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import android.graphics.drawable.AnimationDrawable;//new
import android.widget.ImageView;//new

public class MainActivity extends Activity implements SensorEventListener {
	private SensorManager sensorManager;
	public static int runs=1;
	public static float sumx=0;
	public static float sumy=0;
	public static float sumz=0;
	public static float realx=0;
	public static float realy=0;
	public static float realz=0;
	//public static float foodcalories;
	public static float burntcalories;
	public final static String EXTRA_MESSAGE = "app.exercise.MESSAGE";
	//threshold for S22=10, LG =16
	public static int threshold=16;//Accelerometer doesn't read 0 when its not moving. 
	public double goal= 1500;
	public static int bedtime=11;
	public int hours, minutes, seconds;
	public int PM;
	public float conversionfactor=10;
	TextView coords; // declare coordinates object
	TextView burntcaloriesview; // declare burnt calories object view.
	TextView currentgoalview; // declare goal object view.
	TextView paulsays; // declare Paul says object view
	//EditText foodcaloriesview; // declare calories object view.
	
	//LG Optimus range =19.6
	//Samsung Galaxy S2 = 13.0
	
	AnimationDrawable paulAnimation;

	@Override
	public void onCreate(Bundle savedInstanceState){

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		runs = new File("/mnt/sdcard/DCIM").listFiles().length;//set run number for book-keeping
		
		coords=(TextView)findViewById(R.id.coorview); // create coordinate object
		currentgoalview= (TextView)findViewById(R.id.currentgoal); // create currenthour object
		burntcaloriesview=(TextView)findViewById(R.id.burntcaloriesview); // create caloric counter object
		paulsays=(TextView)findViewById(R.id.paulmessageview);
		
		Calendar c = Calendar.getInstance(); 
		hours = c.get(Calendar.HOUR);
		PM = c.get(Calendar.AM_PM);
		displayCurrentHour(hours);
		
		sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE); // add listener. The listener will be Exercise (this) class
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
        
        
	}
	
	/** Called when the user selects the Send button */
	/*
	public void sendMessage(View view) {
	    Intent intent = new Intent(this, DisplayMessageActivity.class);
	    EditText foodText = (EditText) findViewById(R.id.foodcaloriesview);
	    String message = foodText.getText().toString();
	    intent.putExtra(EXTRA_MESSAGE, message);
	    //intent.putExtra(EXTRA_MESSAGE,  8.0);
	    startActivity(intent);
	    
	}
	*/
	
	public void onAccuracyChanged(Sensor sensor,int accuracy){
		//Even though this method is empty it is required for 
		//implementing sensorEvent listener
	}

	public void onSensorChanged(SensorEvent event){

		
		Calendar cal = Calendar.getInstance(); 
		hours = cal.get(Calendar.HOUR);
		minutes= cal.get(Calendar.MINUTE);
		seconds= cal.get(Calendar.SECOND);
		PM = cal.get(Calendar.AM_PM);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE, "VibrateTag");
		
		wakeLock.acquire();
		if (minutes==00 & seconds<5){
		// check sensor type
			if (burntcalories<goal){
				Vibrator alert = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				alert.vibrate(300); // Vibrate for 300 milliseconds
			}
		}
			
		try {
            wakeLock.release();
        } catch (Throwable th) {
            // ignoring this exception, probably wakeLock was already released
        }


		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){

			// assign directions
			float deltax=event.values[0];
			float deltay=event.values[1];
			float deltaz=event.values[2];
			
			float deltax1=event.values[0];
			float deltay1=event.values[1];
			float deltaz1=event.values[2];
			
			float deltax2=event.values[0];
			float deltay2=event.values[1];
			float deltaz2=event.values[2];
			
			float deltax3=event.values[0];
			float deltay3=event.values[1];
			float deltaz3=event.values[2];
			
			//Moving Average
			float smoothx = (deltax+ deltax1 + deltax2 +deltax3)/4.0f;
			float smoothy = (deltay+ deltay1 + deltay2 +deltay3)/4.0f;
			float smoothz = (deltaz+ deltaz1 + deltaz2 +deltaz3)/4.0f;
			
			//updateRawCount(deltax,deltay,deltaz);
			updateRawCount(smoothx,smoothy,smoothz);
			
			//Use this line to find out accelerometer range
			//System.out.println(event.sensor.getMaximumRange());
			
		}
	}
	
	public void updateRawCount(float smoothx, float smoothy, float smoothz){
		
		//make sure the change was valid.
		
		if (smoothx>threshold){
			sumx=sumx+smoothx-threshold;
			realx=smoothx-threshold;
		}
		else if (smoothy>threshold){
			sumy=sumy+smoothy-threshold;
			realy=smoothy-threshold;
		}
		
		else if (smoothz>threshold){
			sumz=sumz+smoothz-threshold;
			realz=smoothz-threshold;
		}
		
		//Quantities to display to screen
		//coords.setText("X: "+ realx + "  Y: "+ realy + "  Z: " + realz );//smooth + threshold
		coords.setText("X: "+ Math.round(smoothx) + "  Y: "+ Math.round(smoothy) + "  Z: " + Math.round(smoothz) );//smooth + threshold
		
		//Quantities being written to SD card
	    File path1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
	    File path2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
	    System.out.println(path2);
	    File rawfile = new File(path1,  "Lsmoothdata" + runs + ".csv");
	    File filtfile = new File(path2, "Lthreshdata" + runs + ".csv");
	    
		createFile(rawfile,path1, smoothx,smoothy,smoothz);//Write smooth but raw output to SD card
		createFile(filtfile,path2, realx,realy, realz);//Write smooth + threshold output to SD card
		
		
		float burntcalories= convertToCalories(sumx,sumy,sumz);
		
		displayCalories(convertToCalories(sumx,sumy,sumz));
		checkProgress(burntcalories);
	}
	
	public float convertToCalories(float sumx, float sumy, float sumz){
		
		float total= (Math.round(sumx+sumy+sumz)/conversionfactor); //random temporary formula
		return total;
	}

	public void displayCalories(float mycalories){
		
		burntcaloriesview.setText("Activity: "+ mycalories + " pts");
		
	}
	
	public void displayCurrentHour(float mycurrenthour){


		currentgoalview.setText("Goal: "+ goal + " pts");
		
	}
	
	public void checkProgress(float burntcalories){

	//if (hours>4 & hours<bedtime & PM==1 & burntcalories<goal) {
	if (burntcalories<goal) {
		ImageView statusImage = (ImageView) findViewById(R.id.paulanimationview);
		statusImage.setBackgroundResource(R.drawable.sadanimation);
        paulAnimation = (AnimationDrawable) statusImage.getBackground();
		paulAnimation.start();
		paulsays.setText("NEED MORE EXERCISE");
		
	}

	
	else if (burntcalories>=goal) {
		ImageView statusImage = (ImageView) findViewById(R.id.paulanimationview);
        statusImage.setBackgroundResource(R.drawable.happyanimation);
        paulAnimation = (AnimationDrawable) statusImage.getBackground();
		paulsays.setText("GOAL ACCOMPLISHED");
		paulAnimation.start();
		
		
		
	}
	
	
	else if (burntcalories<goal & burntcalories>750) {
		ImageView statusImage = (ImageView) findViewById(R.id.paulanimationview);
        statusImage.setBackgroundResource(R.drawable.normalanimation);
        paulAnimation = (AnimationDrawable) statusImage.getBackground();
        paulAnimation.start(); 
		paulsays.setText("DOING OK");
	}
	
	}
	//currently there is no real stop condition.
	
	public void createFile(File file, File path, float deltax, float deltay, float deltaz) {
			// Create a path where we will place our file in the user's
		    // public pictures directory. For pictures and other media 
			//owned by the application, consider Context.getExternalMediaDir().
			//This function was based of the Android developer documentation example.

		    try {
		        // Make sure the directory exists.
		        path.mkdirs();

		        // Note that this code does no error checking, and assumes the data is small (does not
		        // try to copy it in chunks).  Note that if external storage is not currently mounted this will silently fail.
				Calendar cc = Calendar.getInstance(); 
				int horas = cc.get(Calendar.HOUR);
				
		        OutputStream os = new FileOutputStream(file, true);
		        String stringdata = deltax + "," + deltay + "," + deltaz + "," + burntcalories + "," + horas + "\r";
		        byte[] bytedata = new byte[stringdata.length()];
		        bytedata = stringdata.getBytes();
		        os.write(bytedata);
		        os.close();

		        // Tell the media scanner about the new file so that it is immediately available to the user.
		        
		        /*
		        MediaScannerConnection.scanFile(this,
		                new String[] { file.toString() }, null,
		                new MediaScannerConnection.OnScanCompletedListener() {
		            public void onScanCompleted(String path, Uri uri) {
		                Log.i("ExternalStorage", "Scanned " + path + ":");
		                Log.i("ExternalStorage", "-> uri=" + uri);
		            }


		        });*/
		        
		    } catch (IOException e) {
		        // Unable to create file, likely because external storage is
		        // not currently mounted.
		        Log.w("ExternalStorage", "Error writing " + file, e);
		    }
		   
		}
}


