package ecs160.project.locationtask;

import java.net.URL;
import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class C2DMMessageReceiver extends BroadcastReceiver {
	int type;
	String sender;
	String msg;
	String imgURL;
	Double latitude;
	Double longitude;
	
	int sYear,sMonth,sDate,eYear,eMonth,eDate;
	int sHr,sMin,eHr,eMin;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        String str = "";            
        if (bundle != null)
        {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];            
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                sender = msgs[i].getOriginatingAddress(); 
                str += msgs[i].getMessageBody();
                //str += "\n";        
            }
            Log.w("C2DM","get str ="+str);
            
            //---store the new SMS message to database---
            //* Construct different format for each type of message 					*/
			//* Message, Task, and Query: each has Location, Time and Date				*/
			//*   Format: usr_message;type;mm;dd;yyyy;mm;dd;yyyy;hh;mm;hh;mm;lat;long 	*/
			//* Geo-msg has url. Location is retrieve from url							*/
			//*   Format: geomsg_url;type;usr_message;#.00;#.00							*/
            
            int index = str.indexOf(';');
            int lastIndex = str.lastIndexOf(';');
            String geoMsgString = str.substring(index+3);
            type = Integer.parseInt(str.substring(index+1, index+2));

            // PARSING FOR GEO-MSG
            if (type != 4){
	            msg = str.substring(0, index);
	            
	            sMonth = Integer.parseInt(str.substring(index+3, index+5));
	            sDate = Integer.parseInt(str.substring(index+6, index+8));
	            sYear = Integer.parseInt(str.substring(index+9, index+13));
	           
	            eMonth = Integer.parseInt(str.substring(index+14, index+16));
	            eDate = Integer.parseInt(str.substring(index+17, index+19));
	            eYear = Integer.parseInt(str.substring(index+20, index+24));
	            
	            sHr = Integer.parseInt(str.substring(index+25, index+27));
	            sMin = Integer.parseInt(str.substring(index+28, index+30));
	            eHr = Integer.parseInt(str.substring(index+31, index+33));
	            eMin = Integer.parseInt(str.substring(index+34, index+36));
	            
	            
	            latitude = Double.parseDouble(str.substring(index+37, lastIndex));
	            longitude = Double.parseDouble(str.substring(lastIndex+1));
            }
            // PARSING FOR MSG, TASK, QUERY
            else{
            	int i1 = geoMsgString.indexOf(";");
            	int li = geoMsgString.lastIndexOf(";");
            	msg = geoMsgString.substring(0,i1);
            	latitude = Double.parseDouble(geoMsgString.substring(i1+1,li));
            	longitude = Double.parseDouble(geoMsgString.substring(li+1));
            	imgURL = "https://" + str.substring(0, index);
            }
            	
            //===== Create a Notifications to User ======
            String payload = "";
            if (type == 1)
            	payload = "message";
            else if (type == 2)
            	payload = "task";
            else if (type == 3)
            	payload = "query";
            else
            	payload = "geo-message";
            
            createNotification(context, "You have a new "+payload);	

            Message newMSG;
            Task newTask;
            Query newQuery;
            GeoMessage newGeoMsg;
            
            LocationTaskActivity.cmnDATABASE.open();
            
			if (type == 1){
                newMSG = new Message(msg,new ecs160.project.locationtask.Location(latitude,longitude));
			    newMSG.setSender(sender);
            	LocationTaskActivity.cmnDATABASE.storeMessage(LocationTaskActivity.username,newMSG);
			}
            else if (type == 2){
            	
            	final Calendar c = Calendar.getInstance();
    		    int cur_Year = c.get(Calendar.YEAR);
    		    int cur_Month = c.get(Calendar.MONTH) +1;
    		    int cur_Day = c.get(Calendar.DAY_OF_MONTH);
    		    int cur_Hr = c.get(Calendar.HOUR_OF_DAY);
    		    int cur_Min = c.get(Calendar.MINUTE);
    			boolean cur_active = true; 
    		    // Check active status of Task
    		 	if (cur_Year > eYear)
    		 		cur_active = false;
    			else if (cur_Year < eYear)
    		 		cur_active = true;
   	 			else{			// same year
  		 			if (cur_Month > eMonth)
    	 				cur_active = false;
    		 		else if (cur_Month < eMonth)
    		 			cur_active = true;
    		 		else{		// same month
    		 			if (cur_Day > eDate)
    		 				cur_active = false;
    		 			else if (cur_Day < eDate)
    		 				cur_active = true;
    		 			else{	// same day
    		 				if (cur_Hr > eHr)
    		 					cur_active = false;
    		 				else if (cur_Hr < eHr)
    		 					cur_active = true;
    		 				else{//same hr
    		 					if (cur_Min > eMin)
    		 						cur_active = false;
    		 					else 
    		 					    cur_active = true;
    		 				}
    		 			}
    		 		}
    		 	}
            	
            	if (cur_active)
            		newTask = new Task(msg,new ecs160.project.locationtask.Location(latitude,longitude),new Time(sYear,sMonth,sDate,sHr,sMin,0),new Time(eYear,eMonth,eDate,eHr,eMin,0),sender,true);
            	else
            		newTask = new Task(msg,new ecs160.project.locationtask.Location(latitude,longitude),new Time(sYear,sMonth,sDate,sHr,sMin,0),new Time(eYear,eMonth,eDate,eHr,eMin,0),sender,false);
            	LocationTaskActivity.cmnDATABASE.storeTask(LocationTaskActivity.username, newTask);
            }
            else if(type ==3){	
            	
            	final Calendar c = Calendar.getInstance();
    		    int cur_Year = c.get(Calendar.YEAR);
    		    int cur_Month = c.get(Calendar.MONTH) +1;
    		    int cur_Day = c.get(Calendar.DAY_OF_MONTH);
    		    int cur_Hr = c.get(Calendar.HOUR_OF_DAY);
    		    int cur_Min = c.get(Calendar.MINUTE);
    			boolean cur_active = true; 
    		    // Check active status of Task
    		 			if (cur_Year > eYear)
    		 				cur_active = false;
    		 			else if (cur_Year < eYear)
    		 				cur_active = true;
    		 			else{			// same year
    		 				if (cur_Month > eMonth)
    		 					cur_active = false;
    		 				else if (cur_Month < eMonth)
    		 					cur_active = true;
    		 				else{		// same month
    		 					if (cur_Day > eDate)
    		 						cur_active = false;
    		 					else if (cur_Day < eDate)
    		 						cur_active = true;
    		 					else{	// same day
    		 						if (cur_Hr > eHr)
    		 							cur_active = false;
    		 						else if (cur_Hr < eHr)
    		 							cur_active = true;
    		 						else{//same hr
    		 							if (cur_Min > eMin)
    		 								cur_active = false;
    		 							else 
    		 								cur_active = true;
    		 						}
    		 					}
    		 				}
    		 			}
            	if (cur_active)
            		newQuery = new Query(msg,new ecs160.project.locationtask.Location(latitude,longitude),new Time(sYear,sMonth,sDate,sHr,sMin,0),new Time(eYear,eMonth,eDate,eHr,eMin,0),sender,true);
            	else	
            		newQuery = new Query(msg,new ecs160.project.locationtask.Location(latitude,longitude),new Time(sYear,sMonth,sDate,sHr,sMin,0),new Time(eYear,eMonth,eDate,eHr,eMin,0),sender,false);
            	LocationTaskActivity.cmnDATABASE.storeQuery(LocationTaskActivity.username,newQuery);
             	
            }	
            else if(type ==4){
            	//Insert code here for Geo_msg...
            	newGeoMsg = new GeoMessage(imgURL,sender,msg,new ecs160.project.locationtask.Location(latitude,longitude));
            	// Store into the cmnDATABASE
            	LocationTaskActivity.cmnDATABASE.storeGeoMsg(LocationTaskActivity.username, newGeoMsg);
            }
            else
            	Log.w ("C2DM","error type");
			
			LocationTaskActivity.cmnDATABASE.close();
        } // end if                		
		
	}// end onReceive


   //========== create Notification ============
	
	public void createNotification(Context context, String payload) {

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_launcher,payload, System.currentTimeMillis());
		
		// Hide the notification after its selected
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		Intent intent = new Intent(context, Make.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,intent, 0);
		notification.setLatestEventInfo(context, "Message",payload, pendingIntent);
		notificationManager.notify(0, notification);
		
		//Toast.makeText(context, payload, Toast.LENGTH_LONG).show();

	}// end createNotification
 
}

