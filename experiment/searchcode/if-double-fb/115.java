/*Anupama Sikchi
 * anupamasikchi@gmail.com
 * Main activity finding gps location,facebook likes and latitude and longitudes of geocoder
 */
package com.fb.fbNotifier;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.facebook.*;
import com.facebook.android.Facebook;
import com.facebook.model.*;
import com.fb.fbNotifier.MyLocation;
import com.fb.fbNotifier.R;

import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
@SuppressLint("NewApi")
public class MainActivity extends Activity {
	String name=null ;
	private double latitude;//latitude of current location
	private double longitude;//longitude of current location
	public final float DIST=50000f;//The limiting distance
	protected LocationManager lm;//Finds the GPS location
	//public final static String EXTRA_MESSAGE = "com.fb.fbNotifier.MESSAGE";
	protected static final String TAG = null;
	MyLocation location=new MyLocation();
	String location_text="";
	public static ArrayList<MyLocation> locationArl = new ArrayList<MyLocation>();//keeps location object consisting of the latitude and longitude
	public static ArrayList<String> nearlocationArl = new ArrayList<String>();//keeps the arraylist of only places of nearby locations
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		StrictMode.ThreadPolicy policy = new StrictMode.//Created to suppress Network Thread Exceptions
				ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new MyLocationListener());
		
		// Define a listener that responds to location updates
		showCurrentLocation();
		
		//Facebook session is opened when the session state changes
		Session.openActiveSession(this, true, new Session.StatusCallback() {
		
			public void call(Session session, SessionState state, Exception exception) {
				final String[] PERMS = new String[] { "publish_stream" };
			
				if (session.isOpened()) {
					
					Toast.makeText(getApplicationContext(),"Please wait...", Toast.LENGTH_SHORT).show();
					// make request to the /me API
					Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

						// callback after Graph API response with user object
						/** this function onCompleted is called after the request to graph api is made..here only the user name is retrived from facebook**/
						@Override
						public void onCompleted(GraphUser user, Response response) {
							if (user != null) {
								TextView welcome = (TextView) findViewById(R.id.welcome);
								welcome.setText("Welcome "+user.getName());
								Toast.makeText(getApplicationContext(),user.getFirstName()+user.getId() , Toast.LENGTH_SHORT).show();
								System.out.println(user.getName());//gives the name of the graph user
							}
						
		//checking for the active session
		Session session = Session.getActiveSession();
		Request.Callback callback = new Request.Callback() {
			/** this function onCompleted is called after the request to graph api is made. Here the facebook likes are retrived**/
			@Override
			public void onCompleted(Response response) {

				/*********fetching the json of the response containing the facebook likes****/
				ArrayList<String> namesLikesArl= new ArrayList<String>();//has the names of fb  likes
				try
				{
					GraphObject go  = response.getGraphObject();//the entire graph object
					JSONObject  jso = go.getInnerJSONObject();//the json of the graph object
					JSONArray   arr = jso.getJSONArray( "data" );//the data in the json..starts parsing from the data tags

					for ( int i = 0; i < ( arr.length() ); i++ )
					{
						JSONObject json_obj = arr.getJSONObject( i );


						name = json_obj.getString( "name");//the name value of data has the facebook likes
						if (name.length()<40)//filtering out long likes
							namesLikesArl.add(name);
						//Log.i("name",a.get(i));
					}
				}
				catch ( Throwable t )
				{
					t.printStackTrace();
				}
				for ( int i = 0; i < namesLikesArl.size(); i++ )
				{

					String namesLikes=namesLikesArl.get(i);//the individual likes are retrives and checked with google maps geocoder api
					location.setPlace(namesLikes);
					System.out.println("here"+namesLikes);
					String r = namesLikes.replace(" ","+");//A replacement made to search to googleapi
					try {
						/*********checking if the likes are present on google maps and retrieving their latitude and longitude****/
						
						URL url = new URL("http://maps.google.com/maps/api/geocode/json?address="+r+"&sensor=false");
						HttpURLConnection con = (HttpURLConnection) url
								.openConnection();
						
						float r1=readStream(con.getInputStream());
						//System.out.println("newdist"+r1);
						if (Math.abs(r1)<DIST)
						{
							nearlocationArl.add(namesLikes);
							System.out.println("finally got here");

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				for ( int i = 0; i < nearlocationArl.size(); i++ )
				{
					Toast.makeText(getApplicationContext(),nearlocationArl.get(i), Toast.LENGTH_SHORT).show();
				}
				
			}


		};
/** likes request is made here to facebook**/
		Request request = new Request(session, "me/likes", null, HttpMethod.GET, callback);
		RequestAsyncTask task = new RequestAsyncTask(request);
		task.execute();
						}
					});
				}
			}
		});

	}	
						
	protected void showCurrentLocation() {

        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            
        	latitude= location.getLatitude();
			longitude = location.getLongitude();
            String msg=String.format("current %1$s \n %2$s",location.getLatitude(),location.getLongitude());
            //this.wait(1000);
            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            
        }
}
	private class MyLocationListener implements LocationListener {
		
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location provider.

			if (location != null) {
				latitude= location.getLatitude();
				longitude = location.getLongitude();

				location_text= "latitude: "+latitude+"longitude: "+longitude;

				Toast.makeText(getApplicationContext(), location_text, Toast.LENGTH_SHORT).show();

			}
			else
				System.out.println("nope");

		}

        public void onStatusChanged(String s, int i, Bundle b) {Toast.makeText(MainActivity.this,"Provider status changed",Toast.LENGTH_LONG).show();
        }

        public void onProviderDisabled(String s) {
            Toast.makeText(MainActivity.this,"Provider disabled by the user. GPS turned off",Toast.LENGTH_LONG).show();
        }

        public void onProviderEnabled(String s) {
            Toast.makeText(MainActivity.this,"Provider enabled by the user. GPS turned on",Toast.LENGTH_LONG).show();
        }

    }
	
	
	

	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	/****called on click of button*/
	public void sendMessage(View view) {
		System.out.println("hrlll");
		Bundle b=new Bundle();
		String [] place = nearlocationArl.toArray(new String[nearlocationArl.size()]);//array sent to the next activity
		for(int i=0;i<place.length;i++)
			System.out.println("gg"+place[i]);
		System.out.println("nnnn"+view.getContext());
		b.putStringArray("place",place);
		Intent intent = new Intent(view.getContext(),InterestList.class); 
		intent.putExtras(b);


		startActivity(intent);
	}
	/******checking for location on maps********/
	private float readStream(InputStream in) throws IOException {
		BufferedReader reader = null;
		StringBuilder stringbuilderJson = new StringBuilder();//used to convert the input stream to string
		float r = 9999999;   //if the location doesn't exists a large value is put to distance
		
		reader = new BufferedReader(new InputStreamReader(in));
		String line = "";
		while ((line = reader.readLine()) != null) {
			stringbuilderJson .append(line);
			

		}
		String jSonString=stringbuilderJson .toString();//Contains the entire string of Json
		
	JSONObject jObject = null;
	double lat = 0;//contains latitude of the location in the json
	double lng = 0;//contains longitude of the location in json
		try {
			jObject = new JSONObject(jSonString);
			System.out.println("amm"+jObject.getString("status"));
			if (jObject.getString("status").equals("OK"))
			{
				System.out.println("yup");
				JSONObject location = jObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
				lat = location.getDouble("lat");//picking latitude from the json
				System.out.println("yuppo"+lat);
				lng = location.getDouble("lng");//picking longitude from the json
			}
			
			} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
			

			float[] result=new float[3];//the distance between the current location and json location

		
			
			Location.distanceBetween(latitude, longitude, lat, lng,result) ;
			
			r=result[0];

			System.out.println("distance"+r);
			location.setLatitude(lat);
			location.setLongitude(lng);
			locationArl.add(location);
			
		

		
		return r;

	}
}





