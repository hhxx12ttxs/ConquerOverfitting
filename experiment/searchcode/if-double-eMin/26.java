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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class ProcessQuery extends MapActivity {

	Button del_query, reply_queryBut,cancel_query;
	EditText query_reply;
	String query,sender;
	Double query_lat=0.0;
	Double query_long=0.0;
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
		setContentView(R.layout.process_query);
        
		Bundle query_bundle = getIntent().getExtras();
		query = query_bundle.getString("query");
		sender = query_bundle.getString("sender");
		active = query_bundle.getBoolean("active");
		eYr = query_bundle.getInt("endYear");
		eMonth = query_bundle.getInt("endMonth");
		eDate = query_bundle.getInt("endDate");
		eHr = query_bundle.getInt("endHr");
		eMin = query_bundle.getInt("endMin");
		sYr = query_bundle.getInt("startYear");
		sMonth = query_bundle.getInt("startMonth");
		sDate = query_bundle.getInt("startDate");
		sHr = query_bundle.getInt("startHr");
		sMin = query_bundle.getInt("startMin");
		query_lat = query_bundle.getDouble("latitude");
		query_long = query_bundle.getDouble("longitude");
	
		// init mapview and show the pin
		if(query_lat.intValue() != 0 && query_long.intValue() != 0) {
		  initMapView();
		  cleanCustomItem();
		  if (sender.length() > 10){
			  String mod_sender = sender.substring(0, 1) + "-(" + sender.substring(1, 4) + ")-"
					  + sender.substring(4, 7) + "-" + sender.substring(7);
			  createMarker(query, mod_sender, query_lat, query_long, "");
		  }
		  else
			  createMarker(query, sender, query_lat, query_long, "");
		  zoomInToPoint();
		}
		String s = "";
		if (sender.length() > 10){
			String mod_sender = sender.substring(0, 1) + "-(" + sender.substring(1, 4) + ")-"
                + sender.substring(4, 7) + "-" + sender.substring(7);
			s = "   From " + mod_sender + " :\n" +query;
		}
		else
			s = "   From " + sender + " :\n" +query;
		TextView query_view = (TextView)findViewById(R.id.queryView);
		query_view.setText(s);
		
		query_reply = (EditText)findViewById(R.id.queryReply_editText);
		reply_queryBut = (Button)findViewById(R.id.reply_query_button);
		
		TextView query_active = (TextView)findViewById(R.id.query_statusView);
		if (active){
			query_active.setText("Active");
		    query_reply.setEnabled(true);
		    reply_queryBut.setEnabled(true);
		}
		else{
			query_active.setText("InActive");
			query_reply.setEnabled(false);
			reply_queryBut.setEnabled(false);
		}
		
		TextView query_dueDate = (TextView)findViewById(R.id.query_dueDateTimeView);
		query_dueDate.setText(
		        new StringBuilder()
		                .append(pad(eMonth)).append("/")
		                .append(pad(eDate)).append("/")
		                .append(pad(eYr)).append(" ")
		                .append(pad(eHr)).append(":")
		                .append(pad(eMin)));
		
		del_query = (Button)findViewById( R.id.del_query_button);
		del_query.setOnClickListener(new View.OnClickListener() {
			
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				// delete message from database
				AlertPopUp("Delete Query","Are you sure you want to delete this query ?");
				Log.w("C2DM","del query =" + query);	
			}
		});
		
		cancel_query = (Button)findViewById( R.id.cancel_query_button);
		cancel_query.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				// delete message from database
				Intent i = new Intent(ProcessQuery.this,QueryPage.class);
				ProcessQuery.this.startActivity(i);	
			}
		});
		
		reply_queryBut.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String queryReply = query_reply.getText().toString().trim();
				String msg = queryReply + ";1;00;00;0000;00;00;0000;00;00;00;00;"+Double.toString(query_lat)
																			+";"+Double.toString(query_long);
				replyQuery(sender,msg);
			}
		});
		
	}//end onCreate
	
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
					LocationTaskActivity.cmnDATABASE.deleteQuery(LocationTaskActivity.username, new Query(query,new Location(query_lat,query_long),new Time(sYr,sMonth,sDate,sHr,sMin,0),new Time(eYr,eMonth,eDate,eHr,eMin,0),sender));
					LocationTaskActivity.cmnDATABASE.close();
					
					Toast.makeText(getBaseContext(),"Query is deleted", Toast.LENGTH_LONG).show();
					
					Intent i = new Intent(ProcessQuery.this,QueryPage.class);
					ProcessQuery.this.startActivity(i);
				}
			})
			.setNegativeButton("NO", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Toast.makeText(getBaseContext(),"Query is not deleted", Toast.LENGTH_LONG).show();
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
	
	private void replyQuery(String phoneNumber, final String message)
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
					    Toast.makeText(getBaseContext(), "Your Reply is sent",Toast.LENGTH_SHORT).show();
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
    }
	
  private void initMapView() {
    mapView = (MapView) findViewById(R.id.process_query_mapView);
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
    GeoPoint point = new GeoPoint((int)(query_lat *1e6), (int)(query_long * 1e6));
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

