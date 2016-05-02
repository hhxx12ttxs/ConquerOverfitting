package com.example.weather;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class GoToAlertsActivity extends Activity {
	/**
	 * Displays all saved Alerts and allows for the edit or deletion of those alerts, as well as the creation of
	 * additional alerts
	 */

	//some globals
	public final static String PREFS_NAME = "MyPrefsFile";
	public Boolean Deg;
	public int count;
	public ArrayList<Map<String, Object>> alerts;
	SimpleAdapter adapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//create the alerts page
		setContentView(R.layout.activity_go_to_alerts);	

		
		//retrieves the degree type and the number of alerts from internal storage
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
		SharedPreferences.Editor editor = settings.edit();
		count = settings.getInt("THE_COUNT", 0);
		Deg = settings.getBoolean("DEGREE_TYPE", true);
		
		//get the degree type that was sent from the previous activity 
		//(the preferred degree type may have changed)
		Intent intent = getIntent();
		Boolean value = intent.getBooleanExtra("Switch",Deg);	
		
		//the switch, which will be set to the correct degree type
		Switch Degree = (Switch) findViewById(R.id.SwitchF_C2);
		
		//check to see if the degree type is different than the saved degree type
		if (value != Deg) { //if value != Deg, then they have changed their preferences, update file
			Deg = value;
			editor.putBoolean("DEGREE_TYPE", Deg);
			editor.commit();
		}
		
		//set the switch
		Degree.setChecked(Deg);		
		
		final GoToAlertsActivity act = this;
		Degree.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					//is F
					act.Deg = false;
				} else {
					//is C
					act.Deg = true;
				}
				act.ConvertDegrees2(buttonView);
			}
		});
		
		
		//display all saved alerts
		ListView AlertList = (ListView) findViewById(R.id.alertList);
		alerts = new ArrayList<Map<String, Object>>();
		for (int i=1;i<=count;i++) {
			String FileName = "Alert" + i;
			Alert SavedAlert = ReadAlertFromFile(FileName);
			Object info[] = {SavedAlert.getName(),FileName,SavedAlert.isOn()};
			alerts.add(putInfo(info));
		}

		String[] from = { "name", "save","on" };
		int[] to = {R.id.AlertLabel, R.id.AlertInfo, R.id.AlertSwitch};
		adapter = new SimpleAdapter(this, alerts,R.layout.rowbuttonlayout, from, to);
		AlertList.setAdapter(adapter);

		
		//find out if the alerts need to be updated (edit or add)
		int isNew = intent.getIntExtra("New", 2);
		if (isNew == 0) {
			//Then this was an existing alert, so save the changes
			Alert changes = (Alert) intent.getSerializableExtra("TheAlert");
			String AlertName = changes.getName();
			String SaveName = changes.getSaveName();
			boolean on = changes.isOn();
			SaveAlertToFile(changes);

			//make display changes
			int AlertNum = Integer.valueOf(SaveName.substring(5));
			ChangeInfo(AlertName,SaveName,on,AlertNum-1,alerts);

		}else if (isNew == 1) {
			//New Alert
			Alert newAlert = (Alert) intent.getSerializableExtra("TheAlert");
			count ++;
			
			//save the count change
			editor.putInt("THE_COUNT", count);
			editor.commit();
			
			String SaveName = "Alert" + count;
			newAlert.setSaveName(SaveName);

			//display the alert
		    Object info[] = {newAlert.getName(), SaveName, newAlert.isOn()};
			alerts.add(putInfo(info));
	
			//save the alert
			SaveAlertToFile(newAlert);	
		}
		

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	public void SaveAlertToFile(Alert alert) {
		//save the Alert in internal storage
	    String SaveFile = alert.getSaveName();
	    ObjectOutputStream out = null;
	    try {
			out = new ObjectOutputStream (openFileOutput(SaveFile,0));
			out.writeObject(alert);
		    out.close();
		} catch (FileNotFoundException e) {
			//The file is missing...
			//nothing we can do about it
		} catch (IOException e) {
			//Bad juju...
			//nothing we can do about it
		}
	}


	public Alert ReadAlertFromFile(String fileName) {
		//returns the Alert that was saved in the parameter file

		Alert newAlert = new Alert();
		ObjectInputStream in = null;
		try {
		    in = new ObjectInputStream(openFileInput(fileName));
		    newAlert = (Alert)in.readObject();
		    in.close();
		} catch (StreamCorruptedException e) {
			//Bad juju...
			//nothing we can do about it
		} catch (FileNotFoundException e) {
			//File is missing...
			//nothing we can do about it
		} catch (IOException e) {
			//Bad juju...
			//nothing we can do about it
		} catch (ClassNotFoundException e) {
			//Bad juju...
			//nothing we can do about it
		}
	
		return newAlert;
	}
	
	public void EditAlert(View v){
		//Edit the Alert ==> Calls build alert and display all the Alert's data
		
		RelativeLayout parentRow = (RelativeLayout)v.getParent();
		TextView alertInfo = (TextView)parentRow.getChildAt(2);
		//find the filename the Alert is saved under
		String saveName = alertInfo.getText().toString();
		//load the alert
		Alert editAlert = ReadAlertFromFile(saveName);
		//send the alert to BuildAlerts
		Intent intent = new Intent(this, GoToBuildAlertActivity.class);
		intent.putExtra("TheAlert", editAlert);
		intent.putExtra("Switch", Deg);
		startActivity(intent);
	}
	
	public void DeleteAlert(View v){
		//Delete the Alert ==> Decrement all the following alert's filenames, overriding (deleting) the specified
		//alert and preventing memory gaps from occurring between any saved alerts.
		
		//get the Alert's filename
		RelativeLayout parentRow = (RelativeLayout)v.getParent();
		TextView alertInfo = (TextView)parentRow.getChildAt(2);
		String saveName = alertInfo.getText().toString();
	    

		int alertNum = Integer.parseInt(saveName.substring(5));
		//decrement all following alert's filenames
		for (int i = alertNum+1;i<=count;i++){
			Alert SavedAlert = ReadAlertFromFile("Alert"+i);
			SavedAlert.setSaveName("Alert"+(i-1));
			SaveAlertToFile(SavedAlert);
			//make the changes within the GUI
			ChangeInfo(SavedAlert.getName(),SavedAlert.getSaveName(),SavedAlert.isOn(),i-1,alerts);
		}
		//delete the last file and decrement the alert count
		deleteFile("Alert"+count);
		count--;
		
		//save the count changes
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("THE_COUNT", count);
		editor.commit();
		
		//update the display
		alerts.remove(alertNum-1);
		adapter.notifyDataSetChanged();

	}
	
	public void ToggleAlert(View v){
		//Turns an Alert on or off
		
		RelativeLayout parentRow = (RelativeLayout)v.getParent();
		//get the Alert's filename
		TextView alertInfo = (TextView)parentRow.getChildAt(2);
		String saveName = alertInfo.getText().toString();
		
		//load the alert
		Alert SavedAlert = ReadAlertFromFile(saveName);
		boolean on_off = SavedAlert.isOn();
		
		//make the changes and save
		SavedAlert.setOn(!on_off);
		SaveAlertToFile(SavedAlert);
	}
	
	private void ChangeInfo(String Name, String SaveName, boolean On, int place, ArrayList<Map<String, Object>> list) {
		//change the ListView display of alerts, for a specific alert
		list.remove(place);
		Object info[] = {Name,SaveName,On};
		list.add(place, putInfo(info));
	}

	
	private HashMap<String, Object> putInfo(Object info[]) {
		//hash a list of objects into a HashMap and return it
	    HashMap<String, Object> item = new HashMap<String, Object>();
	    item.put("name", info[0]);
	    item.put("save", info[1]);
	    item.put("on", info[2]);
	    return item;
	  }
	
	public void ConvertDegrees2(View view) {
		//change the degree type
		Deg = !Deg;
		//save the changes
		SharedPreferences settings = getPreferences(0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("DEGREE_TYPE", Deg);
		editor.commit();
		
	}
	
	public void SwitchAlertsCur(View view){
		//switch from alerts page to the current weather page
		//take the alerts degree type to current weather
		
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("Switch", Deg);
		intent.putExtra("TheCount", count);
		startActivity(intent);
	}
	
	public void SwitchAlertsFuture(View view){
		//switch from alerts to the forecast page
		//take the alerts degree type to forecast
		Intent intent = new Intent(this, GoToForecastActivity.class);
		intent.putExtra("Switch", Deg);
		intent.putExtra("TheCount", count);
		startActivity(intent);
	}
	
	public void AddAlert(View view){
		//create another alert
		//use the current degree type unit within the build Activity
		Intent intent = new Intent(this, GoToBuildAlertActivity.class);
		intent.putExtra("Switch", Deg);
		startActivity(intent);
	}
	
}

