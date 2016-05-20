// **************************************************************************
// Home.java
// **************************************************************************
package nz.net.catalyst.fixmystreet.ui;

import java.io.File;
import java.io.IOException;

import nz.net.catalyst.fixmystreet.Constants;
import nz.net.catalyst.fixmystreet.EditPreferences;
import nz.net.catalyst.fixmystreet.Event;
import nz.net.catalyst.fixmystreet.GeoDegree;
import nz.net.catalyst.fixmystreet.R;
import nz.net.catalyst.fixmystreet.Utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.media.ExifInterface;

public class Home extends Activity implements OnClickListener {
	private static final String TAG = "Home";

	private Button btnReport;
	private Button btnDetails;
	private Button btnPicture;
	private TextView textProgress;

	// Our Event object to submit to FixMyStreet
	private Event mEvent;

	// hacky way of checking the results
	private static int globalStatus = -1;
	private String exception_string = "";
	private String serverResponse;

	private File photo = null;
	
	// Location info
	LocationManager locationmanager = null;
	LocationListener listener;

	SharedPreferences settings;
	
	// Thread handling
	ProgressDialog myProgressDialog = null;
	private ProgressDialog pd;
	final Handler mHandler = new Handler();
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			pd.dismiss();
			updateResultsInUi();
		}
	};
	
	// Called when the activity is first created
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.home);
		
//		// First up check for update information
//		Integer d;
//		d = Utils.showUpdateInformation(getApplicationContext());
//		if ( d != null )
//			showDialog(d);

		// Log.d(LOG_TAG, "onCreate, havePicture = " + havePicture);
		settings = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		// Are we 'sharing' a photo 
		if (intent.getAction() != null) {
			if (intent.getAction().equals(Intent.ACTION_SEND)) {
				if (extras.containsKey("android.intent.extra.STREAM")) {
					mEvent = new Event();

					String pf = Utils.getFilePath(getApplicationContext(),
										(Uri) extras.get("android.intent.extra.STREAM"));
					mEvent.setPhoto(pf);
					Log.d(TAG, "got photo: " + pf);
				}
			}
		} else {
			if (extras != null) {
				mEvent = (Event) extras.getParcelable(Constants.EVENT);
				Log.d(TAG, "Got event from extras: " + mEvent.toString());
			} else {
				if (savedInstanceState != null) {
					mEvent = (Event) savedInstanceState.getParcelable(Constants.EVENT);
					Log.d(TAG, "Got event from save instance: " + mEvent.toString());
				} else {
					mEvent = new Event();
				}
			}
		}
		
		// Load input and validate
		setupUI(extras);
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		removeListeners();
	}

	@Override
	protected void onStop() {
		super.onStop();
		removeListeners();
	}

	@Override
	public void onRestart() {
		setupUI(null);
		super.onRestart();
	}

	private void setupUI(Bundle extras) {
		btnPicture = (Button) findViewById(R.id.photo_button);
		btnPicture.setOnClickListener(this);
		btnDetails = (Button) findViewById(R.id.details_button);
		btnDetails.setOnClickListener(this);
		btnReport = (Button) findViewById(R.id.report_button);
		btnReport.setVisibility(View.GONE);
		btnReport.setOnClickListener(this);
		textProgress = (TextView) findViewById(R.id.progress_text);
		textProgress.setVisibility(View.GONE);

		Drawable checked = getResources().getDrawable(R.drawable.done);
		
		if (extras == null)
			extras = new Bundle();
		else { 
			if ( mEvent == null ) 	
				mEvent = (Event) extras.getParcelable(Constants.EVENT);
		}

		if ( mEvent == null )
			mEvent = new Event();
		
		// Check for a photo ...
		String photoFile = mEvent.getPhoto();
		photo = new File(photoFile);
		Log.d(TAG, "photoFile: " + photoFile + ", hasPicture: " + mEvent.hasPhoto());
		
		if ( mEvent.hasPhoto() && photo.exists() ) {
			updateButton(btnPicture, checked, "Photo taken", false);

			try {
				ExifInterface ei = new ExifInterface(photoFile);
				GeoDegree  gd = new GeoDegree(ei);
				if ( gd.isValid() ) {
					mEvent.setLoction(((Float) gd.getLatitude()).toString(), 
									  ((Float) gd.getLongitude()).toString());
					Log.d(TAG, "got location from Exif");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Do we have the details?
			if ( mEvent.hasLocation() && mEvent.hasDetails() ) {
				
				btnReport.setVisibility(View.VISIBLE);
				btnReport.setText("Photo GPS location found! Report to Fix My Street");
				textProgress.setVisibility(View.GONE);
				
			// No GPS from the photo - enable GPS now.
			} else if ( ! mEvent.hasLocation() ) {
				textProgress.setVisibility(View.VISIBLE);
				testProviders();
			}
		}
		
		// Do we have the details?
		if (mEvent.hasDetails()) 
			updateButton(btnDetails, checked, "Details added: '" + mEvent.getSubject() + "'", false);
	}
	
	private void updateButton(Button b, Drawable d, String t, Boolean clickable ) {
		d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		b.setText(t);
		b.setCompoundDrawables(null, null, d, null);
		if ( clickable != null )
			b.setClickable(clickable);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (resultCode == Activity.RESULT_OK ) {
			switch(requestCode) {
				case Constants.IMAGE_CAPTURE_RESULT:
					Drawable checked = getResources().getDrawable(R.drawable.done);
					checked.setBounds(0, 0, checked.getIntrinsicWidth(), checked.getIntrinsicHeight());
					btnPicture.setCompoundDrawables(null, null, checked, null);
					btnPicture.setText("Photo taken");
					mEvent.setPhotoTaken();
					break;
				case Constants.DETAILS_RESULT:
					Bundle extras = intent.getExtras();

					if (extras != null) {
						mEvent = (Event) extras.getParcelable(Constants.EVENT);
						Log.d(TAG, "Got event from extras: " + mEvent.toString());
					} else {
						mEvent = new Event();
					}
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(Constants.EVENT, (Parcelable) mEvent);

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mEvent = (Event) savedInstanceState.getParcelable(Constants.EVENT);
		} else {
			mEvent = new Event();
		}

		super.onRestoreInstanceState(savedInstanceState);
	}

	// **********************************************************************
	// uploadToFMS: uploads details, handled via a background thread
	// Also checks the age and accuracy of the GPS data first
	// **********************************************************************
	private void uploadToFMS() {
		// Log.d(LOG_TAG, "uploadToFMS");
		pd = ProgressDialog
		.show(
				this,
				"Uploading, please wait...",
				"Uploading. This can take up to a minute, depending on your connection speed. Please be patient!",
				true, false);
		Thread t = new Thread() {
			public void run() {
				doUploadinBackground();
				mHandler.post(mUpdateResults);
			}
		};
		t.start();
	}

	private void updateResultsInUi() {
		if (globalStatus == Constants.UPLOAD_ERROR) {
			showDialog(Constants.UPLOAD_ERROR);
		} else if (globalStatus == Constants.UPLOAD_ERROR_SERVER) {
			showDialog(Constants.UPLOAD_ERROR_SERVER);
		} else if (globalStatus == Constants.LOCATION_NOT_FOUND) {
			showDialog(Constants.LOCATION_NOT_FOUND);
		} else if (globalStatus == Constants.PHOTO_NOT_FOUND) {
			showDialog(Constants.PHOTO_NOT_FOUND);
		} else {
			// Success! - Proceed to the success activity!
			Intent i = new Intent(Home.this, Success.class);
			i.putExtra(Constants.EVENT, mEvent);
			startActivity(i);
			finish();
		}
	}

	// **********************************************************************
	// onCreateDialog: Dialog warnings
	// **********************************************************************
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case Constants.COUNTRY_ERROR:
			return new AlertDialog.Builder(this)
			.setTitle("Country or network error")
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int whichButton) {
				}
			})
			.setMessage(getString(R.string.country_error))
			.create();
		case Constants.UPLOAD_ERROR:
			return new AlertDialog.Builder(this)
			.setTitle("Upload error")
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int whichButton) {
				}
			})
			.setMessage(getString(R.string.upload_error) + exception_string + " " + serverResponse).create();
		case Constants.UPLOAD_ERROR_SERVER:
			return new AlertDialog.Builder(this)
			.setTitle("Upload error")
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int whichButton) {
				}
			})
			.setMessage(getString(R.string.upload_error) + serverResponse)
			.create();
		case Constants.LOCATION_NOT_FOUND:
			return new AlertDialog.Builder(this)
			.setTitle("Location problem")
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int whichButton) {
				}
			})
			.setMessage(getString(R.string.location_error))
			.create();
		case Constants.PHOTO_NOT_FOUND:
			return new AlertDialog.Builder(this).setTitle("No photo")
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int whichButton) {
				}
			}).setMessage("Photo not found!").create();
		}
		return null;
	}

	// **********************************************************************
	// doUploadinBackground: POST request to FixMyStreet
	// **********************************************************************
	private boolean doUploadinBackground() {
		// Log.d(LOG_TAG, "doUploadinBackground");

		String responseString = null;
		PostMethod method;

		method = new PostMethod(this.getString(R.string.endpoint));

		try {

			byte[] imageByteArray = null;
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(
					100000);
			
			String auth_string = getString(R.string.endpoint_auth); 
			if ( auth_string != null && auth_string.trim().length() > 0 ) {
				method.setDoAuthentication(true);
				method.setRequestHeader("Authorization", "Basic " + auth_string);
				Log.d(TAG, "HTTP auth = " + auth_string );
				Log.d(TAG, method.toString() );
			}

			// TODO - add a check here
			if (! photo.exists()) {
				Toast.makeText(this, "Sorry failed to attach photo", Toast.LENGTH_SHORT);
			}
			imageByteArray = Utils.getBytesFromFile(photo);

			FilePart photo = new FilePart("photo", new ByteArrayPartSource(
					"photo", imageByteArray));
			photo.setContentType("image/jpeg"); // TODO .. ideally don't guess
			photo.setCharSet(null);
			
			TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			
			Part[] parts = { new StringPart("service", "Android phone"),
					new StringPart("subject", mEvent.getSubject()),
					new StringPart("name", mEvent.getName()),
					new StringPart("email", mEvent.getEmail()),
					new StringPart("phone", mTelephonyMgr.getLine1Number()),
					new StringPart("lat", mEvent.getLat()),
					new StringPart("lon", mEvent.getLon()), photo };

			// Log.d(LOG_TAG, "sending off with lat " + latString + " and lon "
			// + longString);

			method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
			client.executeMethod(method);
			responseString = method.getResponseBodyAsString();
			method.releaseConnection();

			Log.d("httpPost", "Response status: " + responseString);
			//Log.e("httpPost", "Latitude = " + latString + " and Longitude = "	+ longString);

		} catch (Exception ex) {
			Log.e(TAG, "Exception", ex);
			exception_string = ex.getMessage();
			globalStatus = Constants.UPLOAD_ERROR;
			serverResponse = "";
			return false;
		} finally {
			method.releaseConnection();
		}

		if (responseString.equals("SUCCESS")) {
			// launch the Success page
			globalStatus = Constants.SUCCESS;
			return true;
		} else {
			// print the response string?
			serverResponse = responseString;
			globalStatus = Constants.UPLOAD_ERROR;
			return false;
		}
	}

	private boolean checkLoc(Location location) {
		
		long firstGPSFixTime = 0;
		long latestGPSFixTime = 0;
		long previousGPSFixTime = 0;
		int locAccuracy;
		
		// get accuracy
		locAccuracy = (int) location.getAccuracy();
		// get time - store the GPS time the first time
		// it is reported, then check it against future reported times
		latestGPSFixTime = location.getTime();
		if (firstGPSFixTime == 0) {
			firstGPSFixTime = latestGPSFixTime;
		}
		if (previousGPSFixTime == 0) {
			previousGPSFixTime = latestGPSFixTime;
		}
		long timeDiffSecs = (latestGPSFixTime - previousGPSFixTime) / 1000;

		Log.d(TAG, "checkLocation:" + 
				       " accuracy = " + locAccuracy + 
					  ", firstGPSFixTime = " + firstGPSFixTime + 
					  ", gpsTime = " + latestGPSFixTime + 
					  ", timeDiffSecs = " + timeDiffSecs);

		// Check our location - no good if the GPS accuracy is more than 24m
		if ( locAccuracy > 24 ) {
			if (timeDiffSecs == 0) {
				// nor do we want to report if the GPS time hasn't changed at
				// all - it is probably out of date
				textProgress
				.setText("Waiting for a GPS fix: phone says last fix is out of date. Please make sure you can see the sky.");
			} else {
				textProgress
				.setText("Waiting for a GPS fix: phone says last fix had accuracy of "
						+ locAccuracy
						+ "m. (We need accuracy of 24m.) Please make sure you can see the sky.");
			}
		} else if ( locAccuracy == 0 ) {
			// or if no accuracy data is available
			textProgress
			.setText("Waiting for a GPS fix... Please make sure you can see the sky.");
		} else {
			// but if all the requirements have been met, proceed
			mEvent.setLoction(((Double) location.getLatitude()).toString(), 
							  ((Double) location.getLongitude()).toString());
			
			previousGPSFixTime = latestGPSFixTime;
			return true;
		}
		previousGPSFixTime = latestGPSFixTime;
		return false;
	}

	public boolean testProviders() {
		String location_context = Context.LOCATION_SERVICE;
		locationmanager = (LocationManager) getSystemService(location_context);
		if (!locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
			return false;
		}
		listener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// keep checking the location + updating text - until we have
				// what we need
				if (!mEvent.hasLocation()) {
					checkLoc(location);
				}
			}

			public void onProviderDisabled(String provider) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		};
		locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, listener);
		return true;
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
		.setMessage(
				"Your GPS seems to be disabled. Do you want to turn it on now?")
				.setCancelable(false).setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
					public void onClick(
							final DialogInterface dialog,
							final int id) {
						Intent j = new Intent();
						j
						.setAction("android.settings.LOCATION_SOURCE_SETTINGS");
						startActivity(j);
					}
				}).setNegativeButton("No",
						new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	public void removeListeners() {
		// Log.e(LOG_TAG, "removeListeners");
		if ((locationmanager != null) && (listener != null)) {
			locationmanager.removeUpdates(listener);
		}
		locationmanager = null;
		// Log.d(LOG_TAG, "Removed " + listener.toString());
	}

	// ****************************************************
	// Options menu functions
	// ****************************************************

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		
		menu.add(Menu.NONE, Constants.ABOUT, 1, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(Menu.NONE, Constants.HELP, 2, R.string.menu_help).setIcon(android.R.drawable.ic_menu_help);
		menu.add(Menu.NONE, Constants.PREFERENCES, 3, R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		Bundle extras = new Bundle();
		extras.putParcelable(Constants.EVENT, mEvent);

		switch (item.getItemId()) {
			case Constants.ABOUT:
				intent = new Intent(Home.this, About.class);
				intent.putExtras(extras);
				startActivity(intent);
				break;
			case Constants.HELP:
				intent = new Intent(Home.this, Help.class);
				intent.putExtras(extras);
				startActivity(intent);
				break;
			case Constants.PREFERENCES:
				startActivity(new Intent(Home.this, EditPreferences.class));
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		
		switch ( v.getId() ) {

		case R.id.photo_button:
			// If our old photo exists make sure we clobber before creating a new one.
			if (photo.exists() && mEvent.getPhoto().equals(Constants.DEFAULT_PHOTO))
				photo.delete();
			Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
			startActivityForResult(imageCaptureIntent, Constants.IMAGE_CAPTURE_RESULT);
			break;

		case R.id.details_button:
			Intent i = new Intent(Home.this, Details.class);
			Bundle extras = new Bundle();
			extras.putParcelable(Constants.EVENT, mEvent);
			i.putExtras(extras);
			startActivityForResult(i, Constants.DETAILS_RESULT);
			break;

		case R.id.report_button:
			uploadToFMS();
			break;
		}
	}
}

