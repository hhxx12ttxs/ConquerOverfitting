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

public class ProcessOutTask extends Activity {
	Button del_outtask,cancel_outtask;
	String outtask,toReceiver;
	Double outtask_lat=0.0;
	Double outtask_long=0.0;
	int eYr,eMonth,eDay,eHr,eMin;
	int sYr,sMonth,sDay,sHr,sMin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.process_outtask);
        
		Bundle task_bundle = getIntent().getExtras();
		outtask = task_bundle.getString("outtask");
		toReceiver = task_bundle.getString("toReceiver");
		outtask_lat = task_bundle.getDouble("outlatitude");
		outtask_long = task_bundle.getDouble("outlongitude");
		eYr = task_bundle.getInt("outeYear");
		eMonth = task_bundle.getInt("outeMonth");
		eDay = task_bundle.getInt("outeDay");
		eHr = task_bundle.getInt("outeHr");
		eMin = task_bundle.getInt("outeMin");
		sYr = task_bundle.getInt("outsYear");
		sMonth = task_bundle.getInt("outsMonth");
		sDay = task_bundle.getInt("outsDay");
		sHr = task_bundle.getInt("outsHr");
		sMin = task_bundle.getInt("outsMin");
		
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
		
		loc   += Double.toString(outtask_lat) + ", Longitude = " + Double.toString(outtask_long);
		
		TextView outsender_view = (TextView)findViewById(R.id.outtask_senderView);
		outsender_view.setText(to);
		TextView outtask_view = (TextView)findViewById(R.id.outtask_taskView);
		outtask_view.setText(outtask);
		
		TextView outstart_view = (TextView)findViewById(R.id.outtask_startView);
		outstart_view.setText(start);
		TextView outend_view = (TextView)findViewById(R.id.outtask_endView);
		outend_view.setText(end);
		TextView outloc_view = (TextView)findViewById(R.id.outtask_locView);
		outloc_view.setText(loc);
		
		
		del_outtask = (Button)findViewById( R.id.outtask_delButton);
		del_outtask.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// delete message from database
				
				AlertPopUp("Delete Task","Are you sure you want to delete this task ?");
				Log.w("C2DM","del Out task =" + outtask);
			}
		});
		
		cancel_outtask = (Button)findViewById( R.id.outtask_cancel_button);
		cancel_outtask.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// delete message from database
				Intent i = new Intent(ProcessOutTask.this,outTaskPage.class);
				ProcessOutTask.this.startActivity(i);
			}
		});
	} // end onCreate
	
	public void AlertPopUp(String title, String warningtask){
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(warningtask)
			.setIcon(R.drawable.attentionicon)
			.setPositiveButton("YES", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					LocationTaskActivity.myDATABASE.open();
					LocationTaskActivity.myDATABASE.deleteTask(LocationTaskActivity.username, new Task(outtask,new Location(outtask_lat,outtask_long),new Time(sYr,sMonth,sDay,sHr,sMin,0),new Time(eYr,eMonth,eDay,eHr,eMin,0),toReceiver));
					LocationTaskActivity.myDATABASE.close();
					
					Toast.makeText(getBaseContext(),"Task is deleted", Toast.LENGTH_LONG).show();
					
					Intent i = new Intent(ProcessOutTask.this,outTaskPage.class);
					ProcessOutTask.this.startActivity(i);
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
}

