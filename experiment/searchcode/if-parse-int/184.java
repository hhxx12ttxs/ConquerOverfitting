package com.dianafisher;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class EarthquakeService extends Service {
	  private static final String TAG = "Earthquakes";

  public static final String NEW_EARTHQUAKE_FOUND = "New_Earthquake_Found";
  
  private EarthquakeLookupTask lastLookup = null;

  private float minimumMagnitude;
  AlarmManager alarms;
  PendingIntent alarmIntent;
  // Add global variables needed for the notification
  private Notification newEarthquakeNotification;
  public static final int NOTIFICATION_ID = 1;

  private class EarthquakeLookupTask extends AsyncTask<Void, Quake, Void> {
	    @Override
	    protected Void doInBackground(Void... params) {
	      // Get the XML
	      URL url;
	      try {
	        String quakeFeed = getString(R.string.quake_feed);
	        url = new URL(quakeFeed);
	    	         
	        URLConnection connection;
	        connection = url.openConnection();
	    	       
	        HttpURLConnection httpConnection = (HttpURLConnection)connection; 
	        int responseCode = httpConnection.getResponseCode(); 

	        if (responseCode == HttpURLConnection.HTTP_OK) { 
	          InputStream in = httpConnection.getInputStream(); 
	    	          
	          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	          DocumentBuilder db = dbf.newDocumentBuilder();

	          // Parse the earthquake feed.
	          Document dom = db.parse(in);      
	          Element docEle = dom.getDocumentElement();
	    	        	      
	          // Get a list of each earthquake entry.
	          NodeList nl = docEle.getElementsByTagName("entry");
	          if (nl != null && nl.getLength() > 0) {
	            for (int i = 0 ; i < nl.getLength(); i++) {
	              Element entry = (Element)nl.item(i);
	              Element title = (Element)entry.getElementsByTagName("title").item(0);
	              Element g = (Element)entry.getElementsByTagName("georss:point").item(0);
	              Element when = (Element)entry.getElementsByTagName("updated").item(0);
	              Element link = (Element)entry.getElementsByTagName("link").item(0);

	              String details = title.getFirstChild().getNodeValue();
	              String hostname = "http://earthquake.usgs.gov";
	              String linkString = hostname + link.getAttribute("href");

	              String point = g.getFirstChild().getNodeValue();
	              String dt = when.getFirstChild().getNodeValue();  
	              SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
	              Date qdate = new GregorianCalendar(0,0,0).getTime();
	              try {  
	                qdate = sdf.parse(dt);
	              } catch (ParseException e) {
	                e.printStackTrace();
	              }

	              String[] location = point.split(" ");
	              Location l = new Location("dummyGPS");
	              l.setLatitude(Double.parseDouble(location[0]));
	              l.setLongitude(Double.parseDouble(location[1]));

	              String magnitudeString = details.split(" ")[1];
	              int end =  magnitudeString.length()-1;
	              double magnitude = Double.parseDouble(magnitudeString.substring(0, end));
	    	              
	              details = details.split(",")[1].trim();
	    	             
	              Quake quake = new Quake(qdate, details, l, magnitude, linkString);

	              // Process a newly found earthquake
	              addNewQuake(quake);
	              
	              if (magnitude >= minimumMagnitude) {
	            	  Log.d(TAG, "EarthquakeService magnitude " + magnitude + " is larger than minimumMagnitude " + minimumMagnitude);
	            	  publishProgress(quake);
	              } else {
	            	  Log.d(TAG, "EarthquakeService magnitude " + magnitude + " is less than minimumMagnitude " + minimumMagnitude);	            	  
	              }
	            }
	          }
	        }
	      } catch (MalformedURLException e) {
	        e.printStackTrace();
	      } catch (IOException e) {
	        e.printStackTrace();
	      } catch (ParserConfigurationException e) {
	        e.printStackTrace();
	      } catch (SAXException e) {
	        e.printStackTrace();
	      }
	      finally {
	      }
	      return null;  
	    }

	    @Override
	    protected void onProgressUpdate(Quake... values) {
	    	Log.d(TAG, "EarthquakeService: onProgressUpdate ");
	    	// Get a reference to the NotificationManager 
	    	String svcName = Context.NOTIFICATION_SERVICE;
	    	NotificationManager notificationManager = (NotificationManager)getSystemService( svcName );
	    	
	      // Define the Notifications expanded message and intent
	    	Context context = getApplicationContext();
	    	String expandedText = values[0].getDate().toString();
	    	String expandedTitle = "M:" + values[0].getMagnitude() + " " + values[0].getDetails();
	    	Intent startActivityIntent = new Intent( EarthquakeService.this, EarthquakeNotify.class );
	    	
	    	PendingIntent launchIntent = PendingIntent.getActivity( context, 0, startActivityIntent, 0 );
	    	
	    	newEarthquakeNotification.setLatestEventInfo( context, expandedTitle, expandedText, launchIntent );
	    	newEarthquakeNotification.when = System.currentTimeMillis();	        
	        
	    	// Pass the Notification to the NotificationManager
	    	Log.d(TAG, "EarthquakeService: passing notification to NotificationManager ");
	    	notificationManager.notify( NOTIFICATION_ID, newEarthquakeNotification );	    	

	      // Create a toast containing information (in expandedTitle)	
	    	Toast.makeText( context, expandedTitle, Toast.LENGTH_SHORT ).show();
	    }  

	    @Override
	    protected void onPostExecute(Void result) { 
      	  Log.d(TAG, "EarthquakeService magnitude onPostExecute ");	            	  
 	      super.onPostExecute(result);
	      stopSelf();
	    }
	  }
  
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // Retrieve the shared preferences
    Context context = getApplicationContext();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    minimumMagnitude = Integer.parseInt(prefs.getString(Preferences.PREF_MIN_MAG, "0"));
    int updateFreq = Integer.parseInt(prefs.getString(Preferences.PREF_UPDATE_FREQ, "0"));
    boolean autoUpdate = prefs.getBoolean(Preferences.PREF_AUTO_UPDATE, false);
    
    Log.d(TAG, "EarthquakeServiceonStartCommand " + "minimum Magnitude: " + minimumMagnitude + " AutoUpdate " + autoUpdate);
 
    if (autoUpdate) {
      int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
      long timeToRefresh = SystemClock.elapsedRealtime() + 
                           updateFreq*60*1000;
      alarms.setRepeating(alarmType, timeToRefresh, 
                          updateFreq*60*1000, alarmIntent);  
    }
    else  {
        Log.d(TAG, "EarthquakeServiceonStartCommand " + " cancel alarm");
        alarms.cancel(alarmIntent);
    }

    refreshEarthquakes();
    return Service.START_STICKY;
  };

  
  @Override
  public void onCreate() {
	  
	    // Instantiate the Notification
	  	int icon = R.drawable.ic_launcher;
	  	String tickerText = "New Earthquake Detected";
	  	long when = System.currentTimeMillis();
	  	newEarthquakeNotification = new Notification( icon, tickerText, when );
	  
	    alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

	    String ALARM_ACTION = EarthquakeAlarmReceiver.ACTION_REFRESH_EARTHQUAKE_ALARM; 
	    Intent intentToFire = new Intent(ALARM_ACTION);
	    alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);    
  }
	
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
  
  private void refreshEarthquakes() {
	    if (lastLookup == null || 
	        lastLookup.getStatus().equals(AsyncTask.Status.FINISHED)) {
	      lastLookup = new EarthquakeLookupTask();
	      lastLookup.execute((Void[])null);
	    }
	  }
 


  private void addNewQuake(Quake _quake) {
    ContentResolver cr = getContentResolver();
    // Construct a where clause to make sure we donďż˝t already have this 
    // earthquake in the provider.
    String w = EarthquakeProvider.KEY_DATE + " = " + _quake.getDate().getTime();

    // If the earthquake is new, insert it into the provider.
    if (cr.query(EarthquakeProvider.CONTENT_URI, null, w, null, null).getCount()==0){
      ContentValues values = new ContentValues();    

      values.put(EarthquakeProvider.KEY_DATE, _quake.getDate().getTime());
      values.put(EarthquakeProvider.KEY_DETAILS, _quake.getDetails());

      double lat = _quake.getLocation().getLatitude();
      double lng = _quake.getLocation().getLongitude();
      values.put(EarthquakeProvider.KEY_LOCATION_LAT, lat);
      values.put(EarthquakeProvider.KEY_LOCATION_LNG, lng);
      values.put(EarthquakeProvider.KEY_LINK, _quake.getLink());
      values.put(EarthquakeProvider.KEY_MAGNITUDE, _quake.getMagnitude());

      cr.insert(EarthquakeProvider.CONTENT_URI, values);
      announceNewQuake(_quake);
    }
  }
  
  private void announceNewQuake(Quake quake) {
    Intent intent = new Intent(NEW_EARTHQUAKE_FOUND);
    intent.putExtra("date", quake.getDate().getTime());
    intent.putExtra("details", quake.getDetails());
    intent.putExtra("longitude", quake.getLocation().getLongitude());
    intent.putExtra("latitude", quake.getLocation().getLatitude());
    intent.putExtra("magnitude", quake.getMagnitude());

    sendBroadcast(intent);
  }
}
