package ecs160.project.locationtask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ProcessOutQuery extends Activity {
	Button del_outquery,cancel_outquery;
	String outquery,toReceiver;
	Double outquery_lat=0.0;
	Double outquery_long=0.0;
	int eYr,eMonth,eDay,eHr,eMin;
	int sYr,sMonth,sDay,sHr,sMin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.process_outquery);
        
		Bundle query_bundle = getIntent().getExtras();
		outquery = query_bundle.getString("outquery");
		toReceiver = query_bundle.getString("toReceiver");
		outquery_lat = query_bundle.getDouble("outlatitude");
		outquery_long = query_bundle.getDouble("outlongitude");
		eYr = query_bundle.getInt("outeYear");
		eMonth = query_bundle.getInt("outeMonth");
		eDay = query_bundle.getInt("outeDay");
		eHr = query_bundle.getInt("outeHr");
		eMin = query_bundle.getInt("outeMin");
		sYr = query_bundle.getInt("outsYear");
		sMonth = query_bundle.getInt("outsMonth");
		sDay = query_bundle.getInt("outsDay");
		sHr = query_bundle.getInt("outsHr");
		sMin = query_bundle.getInt("outsMin");
		
		String to    = "To "+toReceiver+" :";
		String start = "Start On: ";
		String end   = "End On: ";
		String loc   = "Latitude = ";
		
		if(toReceiver.length()>10){
	        String mod_recevier = toReceiver.substring(0, 1) + "-(" + toReceiver.substring(1, 4) + ")-"
	        		                + toReceiver.substring(4, 7) + "-" + toReceiver.substring(7);
	        to = "To " + mod_recevier + " :";
		}
		
		start += Integer.toString(sMonth) + "/"
				+Integer.toString(sDay)   + "/"
				+Integer.toString(sYr)    + " at "
		        +Integer.toString(sHr)    + ":" 
		        +Integer.toString(sMin)   ;
		
		end   += Integer.toString(eMonth) + "/"
				+Integer.toString(eDay)   + "/"
				+Integer.toString(eYr)    + " at "
		        +Integer.toString(eHr)    + ":" 
		        +Integer.toString(eMin)   ;
		
		loc   += Double.toString(outquery_lat) + ", Longitude = " + Double.toString(outquery_long);
		
		TextView outsender_view = (TextView)findViewById(R.id.outquery_senderView);
		outsender_view.setText(to);
		TextView outquery_view = (TextView)findViewById(R.id.outquery_queryView);
		outquery_view.setText(outquery);
		
		TextView outstart_view = (TextView)findViewById(R.id.outquery_startView);
		outstart_view.setText(start);
		TextView outend_view = (TextView)findViewById(R.id.outquery_endView);
		outend_view.setText(end);
		TextView outloc_view = (TextView)findViewById(R.id.outquery_locView);
		outloc_view.setText(loc);
		
		
		del_outquery = (Button)findViewById( R.id.outquery_delButton);
		del_outquery.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// delete message from database
				
				AlertPopUp("Delete Query","Are you sure you want to delete this query ?");
				Log.w("C2DM","del Out query =" + outquery);
			}
		});
		
		cancel_outquery = (Button)findViewById( R.id.outquery_cancel_button);
		cancel_outquery.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// delete message from database
				
				Intent i = new Intent(ProcessOutQuery.this,outQueryPage.class);
				ProcessOutQuery.this.startActivity(i);
			}
		});
	} // end onCreate
	
	public void AlertPopUp(String title, String warningquery){
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(warningquery)
			.setIcon(R.drawable.attentionicon)
			.setPositiveButton("YES", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					LocationTaskActivity.myDATABASE.open();
					LocationTaskActivity.myDATABASE.deleteQuery(LocationTaskActivity.username, new Query(outquery,new Location(outquery_lat,outquery_long),new Time(sYr,sMonth,sDay,sHr,sMin,0),new Time(eYr,eMonth,eDay,eHr,eMin,0),toReceiver));
					LocationTaskActivity.myDATABASE.close();
					
					Toast.makeText(getBaseContext(),"Query is deleted", Toast.LENGTH_LONG).show();
					
					Intent i = new Intent(ProcessOutQuery.this,outQueryPage.class);
					ProcessOutQuery.this.startActivity(i);
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
}

