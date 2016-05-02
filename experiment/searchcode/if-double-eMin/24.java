package ecs160.project.locationtask;

import java.util.List;

import mapviewballoons.example.custom.CustomItemizedOverlay;
import mapviewballoons.example.custom.CustomOverlayItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class ProcessTask extends MapActivity {

	Button del_task, accept_but,cancel_task;
	String task,sender;
	Double task_lat=0.0;
	Double task_long=0.0;
	Boolean active;
	int eYr,eMonth,eDate,eHr,eMin;
	int sYr,sMonth,sDate,sHr,sMin;
	
  // map view avaliables
  private MapView mapView = null;
  private MapController mapController = null;
  private List<Overlay> mapOverlays; 
  private Drawable drawable;
  private CustomItemizedOverlay<CustomOverlayItem> customItemizedOverlay;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.process_task);
		
		Bundle task_bundle = getIntent().getExtras();
		task = task_bundle.getString("task");
		sender = task_bundle.getString("sender");
		active = task_bundle.getBoolean("active");
		eYr = task_bundle.getInt("endYear");
		eMonth = task_bundle.getInt("endMonth");
		eDate = task_bundle.getInt("endDate");
		eHr = task_bundle.getInt("endHr");
		eMin = task_bundle.getInt("endMin");
		sYr = task_bundle.getInt("startYear");
		sMonth = task_bundle.getInt("startMonth");
		sDate = task_bundle.getInt("startDate");
		sHr = task_bundle.getInt("startHr");
		sMin = task_bundle.getInt("startMin");
		
   	  	task_lat = task_bundle.getDouble("latitude");
   	  	task_long = task_bundle.getDouble("longitude");
		
    // init mapview and show the pin
 	  Log.i("task locaiton", task_lat.intValue() + " " + (task_lat.intValue() == 0));
    if(task_lat.intValue() != 0 && task_long.intValue() != 0) {
      initMapView();
      cleanCustomItem();
      if (sender.length() >10){
    	  String mod_sender = sender.substring(0, 1) + "-(" + sender.substring(1, 4) + ")-"
          + sender.substring(4, 7) + "-" + sender.substring(7);
    	  createMarker(task, mod_sender, task_lat, task_long, "");
      }
      else	  
    	  createMarker(task, sender, task_lat, task_long, "");
      zoomInToPoint();
    }
		String s;
		if (sender.length() >10){
			String mod_sender = sender.substring(0, 1) + "-(" + sender.substring(1, 4) + ")-"
                + sender.substring(4, 7) + "-" + sender.substring(7);
			s = "   From " + mod_sender + " :\n" +task;
		}
		else
			s = "   From " + sender + " :\n" +task;
		TextView task_view = (TextView)findViewById(R.id.taskView);
		task_view.setText(s);
		
		accept_but = (Button)findViewById( R.id.accept_task_button);
		TextView task_active = (TextView)findViewById(R.id.task_statusView);
		if (active){
			task_active.setText("Active");
			accept_but.setEnabled(true);
			}
		else{
			task_active.setText("InActive");
			accept_but.setEnabled(false);
		}
		
		TextView task_dueDate = (TextView)findViewById(R.id.task_dueDateTimeView);
		task_dueDate.setText(
		        new StringBuilder()
		                .append(pad(eMonth)).append("/")
		                .append(pad(eDate)).append("/")
		                .append(pad(eYr)).append(" ")
		                .append(pad(eHr)).append(":")
		                .append(pad(eMin)));
		
		// Delete the Task
		del_task = (Button)findViewById( R.id.del_task_button);
		del_task.setOnClickListener(new View.OnClickListener() {
			
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				// delete message from database
				AlertPopUp("Delete Task","Are you sure you want to delete this task ?");
				Log.w("C2DM","del Task =" + task);
				
			}
		});
		
		cancel_task = (Button)findViewById( R.id.cancel_task_button);
		cancel_task.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				// delete message from database
				Intent i = new Intent(ProcessTask.this,TaskPage.class);
				ProcessTask.this.startActivity(i);
				
			}
		});
		
		// Accept the Task
		accept_but.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				accept_but.setEnabled(false);
				
				
				// notifying the creator
				String taskReply = LocationTaskActivity.username + " has accepted the task: \""
				                                                 + task + "\" from you";
				String msg = taskReply + ";1;00;00;0000;00;00;0000;00;00;00;00;"+ Double.toString(task_lat) 
																			+";"+ Double.toString(task_long);
				acceptTask(sender, msg); 
			}
		});
	}
	
	public void AlertPopUp(String title, String warningMsg){
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(warningMsg)
			.setIcon(R.drawable.attentionicon)
			.setPositiveButton("YES", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					LocationTaskActivity.cmnDATABASE.open();
					LocationTaskActivity.cmnDATABASE.deleteTask(LocationTaskActivity.username, new Task(task,new Location(task_lat,task_long),new Time(sYr,sMonth,sDate,sHr,sMin,0),new Time(eYr,eMonth,eDate,eHr,eMin,0),sender));
					LocationTaskActivity.cmnDATABASE.close();
					
					Toast.makeText(getBaseContext(),"Task is deleted", Toast.LENGTH_LONG).show();
					
					Intent i = new Intent(ProcessTask.this,TaskPage.class);
					ProcessTask.this.startActivity(i);
				}
			})
			.setNegativeButton("NO", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Toast.makeText(getBaseContext(),"Task is not deleted", Toast.LENGTH_LONG).show();
				}
			})
			.show();
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private static String pad(int c) {
	    if (c >= 10)
	        return String.valueOf(c);
	    else
	        return "0" + String.valueOf(c);
	}
	
	private void acceptTask(String phoneNumber, final String message)
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
					    Toast.makeText(getBaseContext(), "Notifying the Task's Owner",Toast.LENGTH_SHORT).show();
					    break;
				    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					    Toast.makeText(getBaseContext(), "Generic failure",Toast.LENGTH_SHORT).show();
					    break;
				    case SmsManager.RESULT_ERROR_NO_SERVICE:
					    Toast.makeText(getBaseContext(), "No service",Toast.LENGTH_SHORT).show();
					    break;
				    case SmsManager.RESULT_ERROR_NULL_PDU:
					    Toast.makeText(getBaseContext(), "Null PDU",Toast.LENGTH_SHORT).show();
					    break;
				    case SmsManager.RESULT_ERROR_RADIO_OFF:
					    Toast.makeText(getBaseContext(), "Radio off",Toast.LENGTH_SHORT).show();
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
					    //Toast.makeText(getBaseContext(), "SMS delivered",Toast.LENGTH_SHORT).show();
					    break;
				    case Activity.RESULT_CANCELED:
					    Toast.makeText(getBaseContext(), "SMS not delivered",Toast.LENGTH_SHORT).show();
					    break;					    
				}
			}// end onReceive
        }, new IntentFilter(DELIVERED));        
    	
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);      
        
    } // end sendSMS   
	
	// map
  private void initMapView() {
    mapView = (MapView) findViewById(R.id.process_task_mapView);
    mapView.setBuiltInZoomControls(true);
    mapView.displayZoomControls(true);
    mapView.setSatellite(false);
    
    mapController = mapView.getController();
    mapOverlays = mapView.getOverlays();
    drawable = getResources().getDrawable(R.drawable.marker2);
    customItemizedOverlay = new CustomItemizedOverlay<CustomOverlayItem>(drawable, mapView);
  }
  public void cleanCustomItem() {
    customItemizedOverlay.getOverLays().clear();
  }
  
  public void zoomInToPoint() {
    GeoPoint point = new GeoPoint((int)(task_lat *1e6), (int)(task_long * 1e6));
    //mapController.animateTo(point);
    mapController.setZoom(16);
    mapController.setCenter(point);
  }
  
  public void createMarker(String text, String sender, double lat, double log, String img_url) {
      GeoPoint point = new GeoPoint((int)(lat * 1e6), (int)(log * 1e6));
      CustomOverlayItem overlayItem = new CustomOverlayItem(point, sender, text, img_url);
      customItemizedOverlay.addOverlay(overlayItem);
      mapOverlays.add(customItemizedOverlay);
  }
}

