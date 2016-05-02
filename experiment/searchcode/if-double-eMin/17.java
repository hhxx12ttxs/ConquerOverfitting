package ecs160.project.locationtask;

import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;


import mapviewballoons.example.custom.CustomItemizedOverlay;
import mapviewballoons.example.custom.CustomOverlayItem;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.gdata.util.AuthenticationException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import ecs160.project.locationtask.Message;
import ecs160.project.locationtask.Task;
import ecs160.project.locationtask.Query;

public class Make extends Activity implements LocationListener {

  Button choosePic;
  Button sendSMS,startDate,endDate,startTime,endTime,make_home_but;
  TextView startDateView,endDateView,startTimeView,endTimeView;
  EditText locLabel_input,receiver_input ;
  ImageView imageNameView;
  Spinner spinner;
  
  RadioButton myselfRadioButton;
  RadioButton otherRadioButton;
  boolean isOther = true;
  
  // PICASA WEB ALBUM INFO
  public static String pwa_email;
  public static String pwa_username;
  private String pwa_pass;	
  boolean isGood_PWA_Auth = false;
  static final int PWA_DIALOG = 4;
  AlertDialog.Builder choosePicPopUp ;
  Bundle geo_msg_bundle = null;
  String img_url,img_title,imgData;
  
  // MSG INFORMATION
  public static String msg = "";
  String txtmsg = "";
  String receiver;
  public int type = 0;
  static double latitude = 0.0;
  static double longitude = 0.0;
  Bundle location_bundle = null;
  
  Bundle favorite_bundle = null;
  
  Message sentMSG;
  Task sentTask;
  Query sentQuery;
  GeoMessage sentGeoMsg;
  
  // DATE and TIME INFORMATION
  static final int DATE_DIALOG_0 = 0;
  static final int DATE_DIALOG_1 = 1;
  int sYear,eYear;
  int sMonth,eMonth;
  int sDay,eDay;
  
  static final int TIME_DIALOG_0 = 2;
  static final int TIME_DIALOG_1 = 3;
  int sHr,eHr;
  int sMin,eMin;
  int sSec,eSec;
  
  String[] cmn_Type = new String[] { "SMS Message", "Query", "Task" };
  
  // LOCATION INFORMATION
  private String locLabel;
  double latituteLoc = 0;
  double longitudeLoc = 0;
  private Geocoder geocoder;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.make);
	  
	  geo_msg_bundle = getIntent().getBundleExtra("gotPhoto");
	  location_bundle = getIntent().getBundleExtra("location");
	  favorite_bundle = getIntent().getBundleExtra("favorite_bundle");
	  
	  if (geo_msg_bundle != null){
	    	Toast.makeText(getBaseContext(), "Image is selected", Toast.LENGTH_SHORT).show();
			imgData = geo_msg_bundle.getString("imgData");
			latitude = geo_msg_bundle.getDouble("imgLat");
			longitude = geo_msg_bundle.getDouble("imgLong");
			int index = imgData.indexOf(";");
			img_url = imgData.substring(index+1);
			img_title = imgData.substring(0,index);
			Log.i("getPhoto", "In MAKE: url = "+img_url);
	  }	
      
    // call map view
	  if(location_bundle != null) {
	    Log.i("mapViewGenerater", "mapViewGenerater is called");
		  mapViewGenerater();
	  }
    // Get msg type
    spinner = (Spinner) findViewById(R.id.msgSpinner);
    choosePic = (Button)findViewById(R.id.selectPic_but);
    imageNameView = (ImageView)findViewById(R.id.selectPic_imageView);
    startDate = (Button) findViewById(R.id.startDate_but);
    receiver_input = (EditText)findViewById(R.id.receiver);
    endDate = (Button) findViewById(R.id.endDate_but);
    startTime = (Button) findViewById(R.id.startTime_but);
    endTime = (Button) findViewById(R.id.endTime_but);
    locLabel_input = (EditText) findViewById(R.id.locText);
    
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.msgArray,android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
     
    // Get information of Geo_msg
   
   if (geo_msg_bundle != null){
    	spinner.setSelection(3);
    	Drawable d = ImageOperations(getBaseContext(), img_url, "image.jpg");
    	imageNameView.setImageDrawable(d);
    }	
   
  if (favorite_bundle !=null){
	  latitude = favorite_bundle.getDouble("fromfav_lat");
	  longitude =  favorite_bundle.getDouble("fromfav_long");
	  locLabel_input.setText(Double.toString(latitude) + ", "+Double.toString(longitude));
	  locLabel_input.setEnabled(false);
  }
   
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
		
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			type = arg2;
			//Toast.makeText(getApplicationContext(),"Click ListItem Number " + type, Toast.LENGTH_SHORT).show();
			
			if (type==3)
				choosePic.setEnabled(true);
			else
				choosePic.setEnabled(false);
			
			if (type==0 || type == 3){
				startDate.setEnabled(false);
				startTime.setEnabled(false);
				endDate.setEnabled(false);
				endTime.setEnabled(false);
			}
			else{
				startDate.setEnabled(true);
				startTime.setEnabled(true);
				endDate.setEnabled(true);
				endTime.setEnabled(true);
			}
		}

		
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	});
    
    
    // Capturing Date and Time
    startDateView = (TextView) findViewById(R.id.startDate);
    endDateView = (TextView)findViewById(R.id.endDate);
    startTimeView = (TextView) findViewById(R.id.startTime);
    endTimeView = (TextView) findViewById(R.id.endTime);
    
    startDate.setOnClickListener(new View.OnClickListener() {
		
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showDialog(DATE_DIALOG_0);
		}
	});
    
    endDate.setOnClickListener(new View.OnClickListener() {
		
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showDialog(DATE_DIALOG_1);
		}
	});
    
    startTime.setOnClickListener(new View.OnClickListener() {
		
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showDialog(TIME_DIALOG_0);
		}
	});
    
   endTime.setOnClickListener(new View.OnClickListener() {
		
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showDialog(TIME_DIALOG_1);
		}
	});
    
    final Calendar c = Calendar.getInstance();
    sYear = c.get(Calendar.YEAR);
    sMonth = c.get(Calendar.MONTH);
    sDay = c.get(Calendar.DAY_OF_MONTH);
    
    eYear = c.get(Calendar.YEAR);
    eMonth = c.get(Calendar.MONTH);
    eDay = c.get(Calendar.DAY_OF_MONTH);
    
    sHr = c.get(Calendar.HOUR_OF_DAY);
    sMin = c.get(Calendar.MINUTE);
    sSec = 0;
    
    eHr = c.get(Calendar.HOUR_OF_DAY);
    eMin = c.get(Calendar.MINUTE);
    eSec = 0;
    
    updateStartDateDisplay();
    updateEndDateDisplay();
    updateStartTimeDisplay();
    updateEndTimeDisplay();
    
    //*** CHOOSE PIC BUTTON ********************************
    choosePic.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.i("choosePic Button", "Select Picture Button Clicked");
			showDialog(PWA_DIALOG);
			}
	});//**** END CHOOSE_PIC BUTTON ************************
    
    myselfRadioButton = (RadioButton)findViewById(R.id.myself_radioButton);
    myselfRadioButton.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			System.out.println("Myself is selected");
			isOther = false;
			receiver_input.setEnabled(false);
		}
	});
    
    otherRadioButton = (RadioButton)findViewById(R.id.other_radioButton);
    otherRadioButton.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			System.out.println("Other is selected");
			isOther = true;
			receiver_input.setEnabled(true);
			
		}
	});
	
//    int selectedId = radioReceiverGroup.getCheckedRadioButtonId();
//	if (selectedId == 0){
//		Toast.makeText(this, "Myself Selected", Toast.LENGTH_LONG).show();
//		
//	}
//	else{
//		Toast.makeText(this, "Other Selected", Toast.LENGTH_LONG).show();
//		
//	}
    
    //*********************** SEND BUTTON : Sending messages *********************************************
    sendSMS = (Button)findViewById(R.id.send_but);
	sendSMS.setOnClickListener(new View.OnClickListener() {
			
		
		public void onClick(View v) {
		// TODO Auto-generated method stub
      
			/*--- Set msg information----*/ 
			if (type == 3 && geo_msg_bundle == null){
				AlertNoChoosePic("Geo-Msg Error", "You did not choose any geo-tag image !");
			}

			// get senderId
			if (isOther)
				receiver = receiver_input.getText().toString().trim();
			else
				receiver = "Me";
			
			// get text message
			EditText msg_input = (EditText)findViewById(R.id.msgText);
			txtmsg = msg_input.getText().toString().trim();
			
			// get location data

			if (favorite_bundle == null){
//				EditText locLabel_input = (EditText) findViewById(R.id.locText);
				locLabel = locLabel_input.getText().toString().trim();

				ecs160.project.locationtask.Location locationData = null;
				if (location_bundle != null)
					locationData = Make.translateToAddressFromName(locLabel);
				if (locationData != null){
					latitude = locationData.getLatitude();
					longitude = locationData.getLongitude();
				}

			}
			
			// concatenate message type
			boolean isGeoMsg = false;
			if (type == 0)
			    msg = txtmsg + ";1";
			else if (type==1)
				msg = txtmsg +";2";
			else if (type==2)	
			    msg = txtmsg +";3";
			else{
				msg = ";4;" + txtmsg;
				isGeoMsg = true;
			}
			
			//* Construct different format for each type of message 					*/
			//* Message, Task, and Query: each has Location, Time and Date				*/
			//*   Format: usr_message;type;mm;dd;yyyy;mm;dd;yyyy;hh;mm;hh;mm;lat;long 	*/
			//* Geo-msg has url. Location is retrieve from url							*/
			//*   Format: geomsg_url;type;usr_message;#.00;#.00							*/
			
			if (!isGeoMsg){
				// concatenate startDate, endDate;
				msg += ";"+ pad(sMonth+1);
				msg += ";"+ pad(sDay);
				msg += ";"+ pad(sYear);
				
				msg += ";"+ pad(eMonth+1);
				msg += ";"+ pad(eDay);
				msg += ";"+ pad(eYear);
				
				// concatenate startTime, endTime;
				msg += ";"+ pad(sHr);
				msg += ";"+ pad(sMin);
				msg += ";"+ pad(eHr);
				msg += ";"+ pad(eMin);
				
				// concatenate latitute,longitude
				BigDecimal bd1 = new BigDecimal(latitude).setScale(2, RoundingMode.HALF_EVEN);
				latitude = bd1.doubleValue();
				BigDecimal bd2 = new BigDecimal(longitude).setScale(2, RoundingMode.HALF_EVEN);
				longitude = bd2.doubleValue();
				
				msg +=  ";"+Double.toString(latitude);
				msg +=  ";"+Double.toString(longitude);
			}
			else
			{
				if (geo_msg_bundle != null){
					String short_url = "";
					int csv = img_url.indexOf("/") + 1;
					short_url = img_url.substring(csv+1);
					String tmp_msg = msg;
					msg = "" + short_url + tmp_msg;
					
					BigDecimal bd1 = new BigDecimal(latitude).setScale(2, RoundingMode.HALF_EVEN);
					latitude = bd1.doubleValue();
					BigDecimal bd2 = new BigDecimal(longitude).setScale(2, RoundingMode.HALF_EVEN);
					longitude = bd2.doubleValue();
				
					locLabel = locLabel_input.getText().toString().trim();
					Log.i("LAT_LONG", "locLabel = "+locLabel);
					if (locLabel.equals("")){
						
						// keep the current longitude and latitude
						Log.i("LAT_LONG", "No specified location in input text");
					}
					else{
						ecs160.project.locationtask.Location locationData = null;
						locationData = Make.translateToAddressFromName(locLabel);
						latitude = locationData.getLatitude();
						longitude = locationData.getLongitude();
						Log.i("LAT_LONG", "lat = "+ Double.toString(latitude) + "Long = "+ Double.toString(longitude));
					}
					
					msg += ";" + Double.toString(latitude);
					msg += ";" + Double.toString(longitude);
				}	
			}
			
			// start sending msg
			if (receiver.length()>0 && msg.length()>0) {          
				if (isOther)
					sendSMS(receiver, msg);  
				else
				{
					LocationTaskActivity.cmnDATABASE.open();
				    
					if (type == 0){
						sentMSG = new Message(txtmsg,new ecs160.project.locationtask.Location(latitude,longitude));
				        sentMSG.setSender(receiver);
				        sentMSG.setMessageRead(true); 
				    	LocationTaskActivity.cmnDATABASE.storeMessage(LocationTaskActivity.username,sentMSG);
				    	Log.i("Self Storage","DID STORE Msg in myDATABASE");
					}
				    else if (type == 1){
				    	sentTask = new Task(txtmsg,new ecs160.project.locationtask.Location(latitude,longitude),new Time(sYear,sMonth+1,sDay,sHr,sMin,0),new Time(eYear,eMonth+1,eDay,eHr,eMin,0));
				    	sentTask.setSender(receiver);
				    	sentTask.setTaskActive(true);
				    	LocationTaskActivity.cmnDATABASE.storeTask(LocationTaskActivity.username, sentTask);
				    	Log.i("Self Storage","DID STORE Task in myDATABASE");
				    }
				    else if(type ==2){	
				    	sentQuery = new Query(txtmsg,new ecs160.project.locationtask.Location(latitude,longitude),new Time(sYear,sMonth+1,sDay,sHr,sMin,0),new Time(eYear,eMonth+1,eDay,eHr,eMin,0));
				    	sentQuery.setSender(receiver);
				    	sentQuery.setQueryActive(true);
				    	LocationTaskActivity.cmnDATABASE.storeQuery(LocationTaskActivity.username,sentQuery);
				    	Log.i("Self Storage","DID STORE Query in myDATABASE");
				    }	
				    else if(type ==3){
				    	sentGeoMsg = new GeoMessage(img_url,receiver,txtmsg,new ecs160.project.locationtask.Location(latitude,longitude));
		            	LocationTaskActivity.cmnDATABASE.storeGeoMsg(LocationTaskActivity.username, sentGeoMsg);
		            	Log.i("Self Storage","DID STORE GeoMsg in myDATABASE");
				    }
				    else
				    	Log.w ("Self Storage","error type in self storage");
					
					LocationTaskActivity.cmnDATABASE.close();
				}
					
					
				//=============================================
				// save message sent to myDATABASE 
				//=============================================
				
				LocationTaskActivity.myDATABASE.open();
			    
				if (type == 0){
					sentMSG = new Message(txtmsg,new ecs160.project.locationtask.Location(latitude,longitude));
			        sentMSG.setSender(receiver);
			        sentMSG.setMessageRead(true);
			    	LocationTaskActivity.myDATABASE.storeMessage(LocationTaskActivity.username,sentMSG);
			    	Log.i("Self Storage","DID STORE Msg in myDATABASE");
				}
			    else if (type == 1){
			    	sentTask = new Task(txtmsg,new ecs160.project.locationtask.Location(latitude,longitude),new Time(sYear,sMonth+1,sDay,sHr,sMin,0),new Time(eYear,eMonth+1,eDay,eHr,eMin,0));
			    	sentTask.setSender(receiver);
			    	sentTask.setTaskActive(true);
			    	LocationTaskActivity.myDATABASE.storeTask(LocationTaskActivity.username, sentTask);
			    	Log.i("Self Storage","DID STORE Task in myDATABASE");
			    }
			    else if(type ==2){	
			    	sentQuery = new Query(txtmsg,new ecs160.project.locationtask.Location(latitude,longitude),new Time(sYear,sMonth+1,sDay,sHr,sMin,0),new Time(eYear,eMonth+1,eDay,eHr,eMin,0));
			    	sentQuery.setSender(receiver);
			    	sentQuery.setQueryActive(true);
			    	LocationTaskActivity.myDATABASE.storeQuery(LocationTaskActivity.username,sentQuery);
			    	Log.i("Self Storage","DID STORE Query in myDATABASE");
			    }	
			    else if(type ==3){
			    	sentGeoMsg = new GeoMessage(img_url,receiver,txtmsg,new ecs160.project.locationtask.Location(latitude,longitude));
	            	LocationTaskActivity.myDATABASE.storeGeoMsg(LocationTaskActivity.username, sentGeoMsg);
	            	Log.i("Self Storage","DID STORE GeoMsg in myDATABASE");
			    }
			    else
			    	Log.w ("Self Storage","error type in self storage");
				
				LocationTaskActivity.myDATABASE.close();
//				Intent i = new Intent(Make.this,Home.class);
//				Make.this.startActivity(i);
				//=============================================
				// end save message sent to myDATABASE 
				//=============================================
				
			}
			else
				Toast.makeText(getBaseContext(),"Please enter both phone number and message.", Toast.LENGTH_SHORT).show();
			}
		});// ********************** END SEND BUTTON ************************************************************
	
		make_home_but = (Button)findViewById(R.id.make_cancel_but);
		make_home_but.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(Make.this,Home.class);
				Make.this.startActivity(i);
			}
		});
	
	}// end onCreate
	
	public void AlertNoChoosePic(String title, String warningtask){
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(title);
		    alert.setMessage(warningtask);
			alert.setIcon(R.drawable.attentionicon);
			alert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			});
			alert.show();
	}
	
//	private boolean addListenerOnRadioButton(){
//		radioReceiverGroup = (RadioGroup)findViewById(R.id.radio_chooseReceiver);
//		int selectedId = radioReceiverGroup.getCheckedRadioButtonId();
//		if (selectedId == 0){
//			Toast.makeText(this, "Myself Selected", Toast.LENGTH_LONG).show();
//			return false;
//		}
//		else{
//			Toast.makeText(this, "Other Selected", Toast.LENGTH_LONG).show();
//			return true;
//		}
//	}
	
	// ===== group of functions for getting PICTURES =================
	private AlertDialog.Builder createChoosePicPopUp(){
		Log.i("choosePic Button", "Start Launching PopUp");
		AlertDialog.Builder popUp = new AlertDialog.Builder(this);

		popUp.setTitle("Login Picasa Web Album");

		final LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(1);
		
		// Set an EditText view to get user input 
        final EditText pwa_input_usr = new EditText(this);
        pwa_input_usr.setHint("Picasa_username@gmail.com");
        //pwa_input_usr.setText("locationtask@gmail.com");
        layout.addView(pwa_input_usr);
        
        // Set an EditText view to get user input 
        final EditText pwa_input_pss = new EditText(this);
        pwa_input_pss.setHint("*******");
        //pwa_input_pss.setText("androidclass");
        pwa_input_pss.setInputType(0x00000081);
        layout.addView(pwa_input_pss);
        
        popUp.setView(layout);
        
        popUp.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.i("Make PWA","Cancel Button Clicked");
				//isCancel_PWA_Auth = true;
			}
		});
        
        popUp.setPositiveButton("Login", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.i("Make PWA","Login Button Clicked");
				pwa_email = pwa_input_usr.getText().toString().trim();
				pwa_pass = pwa_input_pss.getText().toString().trim();
				pwa_username = "";
				isGood_PWA_Auth = PWA_authentication(pwa_email,pwa_pass);
				
				if (!isGood_PWA_Auth){
					Toast.makeText(getBaseContext(), 
							"Picasa Web Album Authentication:\nInvalid combination of Username and Password",
							Toast.LENGTH_LONG).show();
				}
				else{
					// get into ChooseAlbum page
					Log.i("choosePic Button", "Finshed launching PopUp");
					Intent i_album = new Intent(Make.this,ChooseAlbum.class);
					Make.this.startActivity(i_album);
				}
				
			}
		});
		
        return popUp;
	}
	
	private boolean PWA_authentication(String pwaUSR, String pwaPASS){
		
		try {
			LocationTaskActivity.servicePWA.setUserCredentials(pwa_email,pwa_pass);
			Log.i("Authentication", "setUserCredentials SUCCEEDED");
			int index = pwa_email.indexOf("@");
			pwa_username = pwa_email.substring(0, index);
			return true;
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			Log.i("Authentication", "setUserCredentials FAILED");
			e.printStackTrace();
			return false;
		}
		
	}
	
	// ===== END group of function for getting PICTURES ==============
	
	//===== group of functions for getting DATE/TIME ================
	private DatePickerDialog.OnDateSetListener sDateSetListener = 
			new DatePickerDialog.OnDateSetListener() {
				
				
				public void onDateSet(DatePicker view, int year, int monthOfYear,
						int dayOfMonth) {
					// TODO Auto-generated method stub
					sYear = year;
					sMonth = monthOfYear;
					sDay = dayOfMonth;
					updateStartDateDisplay();
				}
			};
			
	private DatePickerDialog.OnDateSetListener eDateSetListener = 
			new DatePickerDialog.OnDateSetListener() {
						
				
				public void onDateSet(DatePicker view, int year, int monthOfYear,
								int dayOfMonth) {
						// TODO Auto-generated method stub
						eYear = year;
						eMonth = monthOfYear;
						eDay = dayOfMonth;
						updateEndDateDisplay();
				}
			};			
	
	private TimePickerDialog.OnTimeSetListener sTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {
				
				
				public void onTimeSet(TimePicker arg0, int arg1, int arg2) {
					// TODO Auto-generated method stub
					sHr = arg1;
			        sMin = arg2;
			        updateStartTimeDisplay();
				}
		   };		
			
    private TimePickerDialog.OnTimeSetListener eTimeSetListener =
			new TimePickerDialog.OnTimeSetListener() {
						
				
				public void onTimeSet(TimePicker arg0, int arg1, int arg2) {
					// TODO Auto-generated method stub
					eHr = arg1;
					eMin = arg2;
					updateEndTimeDisplay();
				}
		};	
		   
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
	    case DATE_DIALOG_0:
	        return new DatePickerDialog(this,
	        		    sDateSetListener,
	                    sYear, sMonth, sDay);
	    case DATE_DIALOG_1:
	        return new DatePickerDialog(this,
	        		    eDateSetListener,
	                    eYear, eMonth, eDay);
	    case TIME_DIALOG_0:
	        return new TimePickerDialog(this,
	                sTimeSetListener, sHr, sMin, false);
	    case TIME_DIALOG_1:
	        return new TimePickerDialog(this,
	                eTimeSetListener, sHr, sMin, false);
	    case PWA_DIALOG: 
	    	choosePicPopUp = createChoosePicPopUp();
	    	AlertDialog alert = choosePicPopUp.create();
	    	return alert;
	    }
		return null;

	}

	private void updateStartDateDisplay(){
		startDateView.setText(new StringBuilder() 
								.append(sMonth+1).append("/")
								.append(sDay).append("/")
								.append(sYear).append(""));
	}
	
	private void updateEndDateDisplay(){
		endDateView.setText(new StringBuilder() 
								.append(eMonth+1).append("/")
								.append(eDay).append("/")
								.append(eYear).append(""));
	}
	
	private void updateStartTimeDisplay() {
	    startTimeView.setText(
	        new StringBuilder()
	                .append(pad(sHr)).append(":")
	                .append(pad(sMin)).append(":00"));
	}
	
	private void updateEndTimeDisplay() {
	    endTimeView.setText(
	        new StringBuilder()
	                .append(pad(eHr)).append(":")
	                .append(pad(eMin)).append(":00"));
	}
	
	private static String pad(int c) {
	    if (c >= 10)
	        return String.valueOf(c);
	    else
	        return "0" + String.valueOf(c);
	}
	
	//===== END group of functions for getting DATE/TIME ================
	
	// sends a SMS message to another device =========================
    private void sendSMS(String phoneNumber, final String message)
    {      
    	
    	String SENT = "SMS_SENT";
    	String DELIVERED = "SMS_DELIVERED";
    	
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,new Intent(DELIVERED), 0);
        
        //--------------------------------
        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode())
				{
				    case Activity.RESULT_OK:
					    Toast.makeText(getBaseContext(), "Message has been sent", 
					    		Toast.LENGTH_SHORT).show();
					    //Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
					    break;
				    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					    Toast.makeText(getBaseContext(), "Generic failure", 
					    		Toast.LENGTH_SHORT).show();
					    break;
				    case SmsManager.RESULT_ERROR_NO_SERVICE:
					    Toast.makeText(getBaseContext(), "No service", 
					    		Toast.LENGTH_SHORT).show();
					    break;
				    case SmsManager.RESULT_ERROR_NULL_PDU:
					    Toast.makeText(getBaseContext(), "Null PDU", 
					    		Toast.LENGTH_SHORT).show();
					    break;
				    case SmsManager.RESULT_ERROR_RADIO_OFF:
					    Toast.makeText(getBaseContext(), "Radio off", 
					    		Toast.LENGTH_SHORT).show();
					    break;
				}
			}// end onReceive
        }, new IntentFilter(SENT));
        
         //--------------------------------
        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode())
				{
				    case Activity.RESULT_OK:
					    Toast.makeText(getBaseContext(), "SMS delivered", 
					    		Toast.LENGTH_SHORT).show();
					    break;
				    case Activity.RESULT_CANCELED:
					    Toast.makeText(getBaseContext(), "SMS not delivered", 
					    		Toast.LENGTH_SHORT).show();
					    break;					    
				}
			}// end onReceive
        }, new IntentFilter(DELIVERED));        
    	
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI); 
       
        Log.w("C2DM","msg ="+message);
    } // end sendSMS   

    private Drawable ImageOperations(Context ctx, String url, String saveFilename) {
    	
		 try {
		   InputStream is = (InputStream) new URL(url).getContent();
		   Drawable d = Drawable.createFromStream(is, "src");		
		   return d;	
		 } catch (MalformedURLException e) {		
		   e.printStackTrace();		
		   return null;		
		 } catch (IOException e) {		
		   e.printStackTrace();		
		   return null;		
		 }	
	} 
    
    /*=========================================
     *  Methods for implementing the Google Map
     * Writen by Daihua Ye
     *========================================*/
    
    public void mapViewGenerater() {
      
      latituteLoc = location_bundle.getDouble("latitude");
      longitudeLoc = location_bundle.getDouble("longitude");
    }
    
    public static ecs160.project.locationtask.Location translateToAddressFromName(String locLabel) {

    	ecs160.project.locationtask.Location returnLoc = null;
    	JSONObject jsonObj = Home.getLocationInfo(locLabel);
    	if (jsonObj != null) {
    		GeoPoint point = Home.getLatLong(jsonObj);
    		double lat = 0;
    		double log = 0;
    		if (point != null) {
    			lat = point.getLatitudeE6() / 1E6;
    			log = point.getLongitudeE6() / 1E6;
    		}
      
    		Log.i("Get the location from the ponit", lat + " " + log);
    		if(point != null) {
    			returnLoc = new ecs160.project.locationtask.Location(lat, log);
    		}
    	}
    	return returnLoc;
    }
    
    
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(android.location.Location arg0) {
		// TODO Auto-generated method stub
		
	}

    
}

	
	

