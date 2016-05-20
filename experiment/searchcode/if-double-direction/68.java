package com.fsahoy.android10.activities;

import java.util.Calendar;

import com.fsahoy.android10.activities.validation.FormValidator_Text_Length;
import com.fsahoy.android10.activities.validation.IFormValidator;
import com.fsahoy.android10.activities.validation.ValidationSet;
import com.fsahoy.android10.model.ModelManager;
import com.fsahoy.android10.model.logbook.Logbook;
import com.fsahoy.android10.model.logbook.LogbookRecordUpdate;
import com.fsahoy.android10.util.Utilities;
import com.fsahoy.android10.R;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

/**
 * Contains all the activity functionality for the displaying of a single Log book
 * 
 * @author Team E
 * @version 1.0
 * @since 2012-06-05
 */

public class LogbookRecordActivity extends FSAhoy_Activity {

	/**
	 * Called when activity is first created,
	 * 
	 * This method is called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 * State of the instance given to the the activities by android
	 * API.
	 */

	private int DEP_HOUR;
	private int ARI_HOUR;
	private int DEP_MINUTE;
	private int ARI_MINUTE;
	private int DEP_YEAR;
	private int DEP_DAY;
	private int DEP_MONTH;
	private int ARI_YEAR;
	private int ARI_DAY;
	private int ARI_MONTH;
	static final int DEP_TIME_DIALOG_ID = 0;
	static final int ARI_TIME_DIALOG_ID = 1;
	static final int DEP_DATE_DIALOG_ID = 2;
	static final int ARI_DATE_DIALOG_ID = 3;
	EditText logbookDepDate;
	EditText logbookAriDate;
	
	/**
	 * Called when activity is first created,
	 * 
	 * This method is called when the Activity is first created. It defines the
	 * view regarding to the activity.
	 * 
	 * @param savedInstanceState
	 *            State of the instance given to the the activities by android
	 *            API.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logbookrecord);

		/*
		 * Get the current Log book
		 */
		Logbook logbook = ModelManager.getInstance_ModelLogbook().getCurrentLogbook();

		/*
		 *  SET THE DEPARTURE / ARRIVAL PLACE(S) 
		 */

		/*
		 *  Set the Departure place 
		 */
		EditText logbookDepPlace = (EditText) findViewById(R.id.logbookDepPlace);
		logbookDepPlace.setText(logbook.getDeparturePlace());

		/*
		 *  Set the Arrival place
		 */
		EditText logbookAriPlace = (EditText) findViewById(R.id.logbookAriPlace);
		logbookAriPlace.setText(logbook.getArrivalPlace());

		/*
		 * SET THE DEPARTURE / ARRIVAL DATE(S) 
		 */
		
		/*
		 *  Set the Departure date
		 */
		logbookDepDate = (EditText) findViewById(R.id.logbookDepDate);
		logbookDepDate.setText(logbook.departureDateTimeAsString());

		logbookDepDate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DEP_DATE_DIALOG_ID);
			}
		});

		/*
		 *  Set the Arrival date
		 */
		logbookAriDate = (EditText) findViewById(R.id.logbookAriDate);
		logbookAriDate.setText(logbook.arrivalDateTimeAsString());

		logbookAriDate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(ARI_DATE_DIALOG_ID);
			}
		});

		/*
		 *  SET THE DEPARTURE / ARRIVAL TIMEZONE(S)
		 */

		/* 
		 * Set the Departure timezone
		 */
		String[] timeZones = new String[] {"+1", "+2","+3","+4","+5","+6","+7","+8","+9","+10","+11","+12","-1", "-2","-3","-4","-5","-6","-7","-8","-9","-10","-11","-12"};
		Spinner logbookDepTimeZone = (Spinner) findViewById(R.id.logbookDepTimeZone);
		ArrayAdapter<String> depTimeZoneAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, timeZones);
		depTimeZoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		logbookDepTimeZone.setAdapter(depTimeZoneAdapter);

		for (int i = 0; i < depTimeZoneAdapter.getCount(); i++) {
			if (Double.toString(logbook.getDepartureTimeZone()).equals(depTimeZoneAdapter.getItem(i).toString())) {
				logbookDepTimeZone.setSelection(i);
			}
		}

		/*
		 *  Set the Arival timezone
		 */
		Spinner logbookAriTimeZone = (Spinner) findViewById(R.id.logbookAriTimeZone);
		ArrayAdapter<String> ariTimeZoneAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, timeZones);
		ariTimeZoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		logbookAriTimeZone.setAdapter(ariTimeZoneAdapter);

		for (int i = 0; i < ariTimeZoneAdapter.getCount(); i++) {
			if (Double.toString(logbook.getArrivalTimeZone()).equals(ariTimeZoneAdapter.getItem(i).toString())) {
				logbookAriTimeZone.setSelection(i);
			}
		}

		/*
		 *  SET THE LONGTITUDE / LATITUDE 
		 */
		
		/*
		 *  Set the Longtitude degrees
		 */
		EditText logbookLongtitudeDeg = (EditText) findViewById(R.id.logbookLongtitudeDeg);
		logbookLongtitudeDeg.setText(logbook.longitudeDegreesAsString());

		/* 
		 * Set the Longtitude minutes
		 */
		EditText logbookLongtitudeMin = (EditText) findViewById(R.id.logbookLongtitudeMin);
		logbookLongtitudeMin.setText(logbook.longitudeMinutesAsString());

		/*
		 *  Set the Longtitude direction
		 */
		String[] longDirections = new String[] {"E", "W"};
		Spinner logbookLongtitudeDir = (Spinner) findViewById(R.id.logbookLongtitudeDir);
		ArrayAdapter<String> longAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, longDirections);
		longAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		logbookLongtitudeDir.setAdapter(longAdapter);

		for (int i = 0; i < longAdapter.getCount(); i++) {
			if (logbook.getLongitudeDirection().equals(longAdapter.getItem(i).toString())) {
				logbookLongtitudeDir.setSelection(i);
			}
		}

		/*
		 *  Set the Latitude degrees
		 */
		EditText logbookLatitudeDeg = (EditText) findViewById(R.id.logbookLatitudeDeg);
		logbookLatitudeDeg.setText(logbook.latitudeDegreesAsString());

		/*
		 *  Set the Latitude minutes
		 */
		EditText logbookLatitudeMin = (EditText) findViewById(R.id.logbookLatitudeMin);
		logbookLatitudeMin.setText(logbook.latitudeMinutesAsString());

		/*
		 *  Set the Latitude direction
		 */
		String[] latDirections = new String[] {"N", "S"};
		Spinner logbookLatitudeDir = (Spinner) findViewById(R.id.logbookLatitudeDir);
		ArrayAdapter<String> latAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, latDirections);
		latAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		logbookLatitudeDir.setAdapter(latAdapter);

		for (int i = 0; i < latAdapter.getCount(); i++) {
			if (logbook.getLatitudeDirection().equals(latAdapter.getItem(i).toString())) {
				logbookLatitudeDir.setSelection(i);
			}
		}

		/*
		 * DEFINE THE CALENDAR OBJECT
		 */
		final Calendar c = Calendar.getInstance();
	    DEP_YEAR   = c.get(Calendar.YEAR);
	    ARI_YEAR   = c.get(Calendar.YEAR);
	    DEP_MONTH  = c.get(Calendar.MONTH);
	    ARI_MONTH  = c.get(Calendar.MONTH);
	    DEP_DAY    = c.get(Calendar.DAY_OF_MONTH);
	    ARI_DAY    = c.get(Calendar.DAY_OF_MONTH);
	    DEP_HOUR   = c.get(Calendar.HOUR_OF_DAY);
	    ARI_HOUR   = c.get(Calendar.HOUR_OF_DAY);
	    DEP_MINUTE = c.get(Calendar.MINUTE);
	    ARI_MINUTE = c.get(Calendar.MINUTE);
	}
	/**
	 * Shows the logbook entry on map
	 * 
	 * @param view
	 */
	public void showLogbookMap(View view) {

		// Get the Longtitude degrees
		EditText logbookLongtitudeDeg = (EditText) findViewById(R.id.logbookLongtitudeDeg);
		double longdegrees = Double.parseDouble(logbookLongtitudeDeg.getText().toString());

		// Get the Longtitude minutes
		EditText logbookLongtitudeMin = (EditText) findViewById(R.id.logbookLongtitudeMin);
		double longminutes = Double.parseDouble(logbookLongtitudeMin.getText().toString());

		// Get the Longtitude direction
		Spinner logbookLongtitudeDir = (Spinner) findViewById(R.id.logbookLongtitudeDir);
		String longdir = logbookLongtitudeDir.getSelectedItem().toString();

		// Get the Latitude degrees
		EditText logbookLatitudeDeg = (EditText) findViewById(R.id.logbookLatitudeDeg);
		double latdegrees = Double.parseDouble(logbookLatitudeDeg.getText().toString());

		// Get the Latitude minutes
		EditText logbookLatitudeMin = (EditText) findViewById(R.id.logbookLatitudeMin);
		double latminutes = Double.parseDouble(logbookLatitudeMin.getText().toString());

		// Get the Latitude direction
		Spinner logbookLatitudeDir = (Spinner) findViewById(R.id.logbookLatitudeDir);
		String latdir = logbookLatitudeDir.getSelectedItem().toString();

		// Pass the current longitude / latitude to the map activity
		Intent i = new Intent(getApplicationContext(),LogbookMapActivity.class);		
		i.putExtra("longdegrees", longdegrees);
		i.putExtra("longminutes", longminutes);
		i.putExtra("longdir", longdir);
		i.putExtra("latdegrees", latdegrees);
		i.putExtra("latminutes", latminutes);
		i.putExtra("latdir", latdir);

		startActivity(i);
	}

	/**
	 * Deletes the current logbook entry
	 * 
	 * @param view
	 */

	public void delete(View view) {

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				switch (which) {

					case DialogInterface.BUTTON_POSITIVE:

						Logbook logbook = ModelManager.getInstance_ModelLogbook().getCurrentLogbook();
						ModelManager.getInstance_ModelLogbook().deleteLogbook(logbook);

						Utilities.toast(getApplicationContext(),
										getApplicationContext().getString(R.string.activity_logbook_button_deleteprogress));

						startActivity(new Intent(getApplicationContext(), LogBookActivity.class));
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						break;
				}
			}};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(
				getApplicationContext().getString(R.string.activity_logbook_button_deleteconfirmation))
				.setPositiveButton(getApplicationContext().getString(R.string.app_yes), dialogClickListener)
				.setNegativeButton(getApplicationContext().getString(R.string.app_no), dialogClickListener).show();
	}

	/**
	 * Update the current Logbook Entry
	 * 
	 * @param view
	 */

	public void update(View view) {

		/*
		 * Initialize the current logbook
		 */
		Logbook logbook = ModelManager.getInstance_ModelLogbook().getCurrentLogbook();


		/*
		 *  Get the Departure place
		 */
		EditText logbookDepPlace = (EditText) findViewById(R.id.logbookDepPlace);

		/* 
		 * Get the Departure date
		 */
		EditText logbookDepDate = (EditText) findViewById(R.id.logbookDepDate);

		/*
		 * Get the Departure timezone
		 */
		Spinner logbookDepTimeZone = (Spinner) findViewById(R.id.logbookDepTimeZone);

		/*
		 * Get the Arrival place
		 */
		EditText logbookAriPlace = (EditText) findViewById(R.id.logbookAriPlace);

		/*
		 * Get the Arrival date
		 */
		EditText logbookAriDate = (EditText) findViewById(R.id.logbookAriDate);

		/*
		 * Get the Arrival timezone
		 */
		Spinner logbookAriTimeZone = (Spinner) findViewById(R.id.logbookAriTimeZone);

		/*
		 * Get the Longtitude degrees
		 */
		EditText logbookLongtitudeDeg = (EditText) findViewById(R.id.logbookLongtitudeDeg);

		/*
		 * Get the Longtitude minutes
		 */
		EditText logbookLongtitudeMin = (EditText) findViewById(R.id.logbookLongtitudeMin);

		/*
		 * Get the Longtitude direction
		 */
		Spinner logbookLongtitudeDir = (Spinner) findViewById(R.id.logbookLongtitudeDir);

		/*
		 * Get the Latitude degrees
		 */
		EditText logbookLatitudeDeg = (EditText) findViewById(R.id.logbookLatitudeDeg);

		/*
		 * Get the Latitude minutes
		 */
		EditText logbookLatitudeMin = (EditText) findViewById(R.id.logbookLatitudeMin);

		/*
		 * Get the Latitude direction
		 */
		Spinner logbookLatitudeDir = (Spinner) findViewById(R.id.logbookLatitudeDir);

		/*
		 * NOT USED IN THE UI
		 */

		/*
		 * Get the Logbook notes
		 */
		String notes = logbook.getNotes();

		/*
		 *  Get the Stopover place
		 */
		String stopoverPlace = logbook.getStopoverPlace();

		/* 
		 * Get the Crew Guests
		 */
		String crewGuests = logbook.getCrewGuests();

		/*
		 * Get the Stopover Date Time
		 */
		String stopoverDateTime = logbook.stopoverDateTimeAsString();

		/*
		 * Get the Stopover Timezone
		 */
		double stopoverTimeZone = logbook.getStopoverTimeZone();

		/*
		 * Get the wind speed
		 */
		int windSpeed = logbook.getWindSpeed();

		/*
		 * Get the fuel status
		 */
		int fuelStatus = logbook.getFuelStatus();

		/*
		 * Get the air temperature
		 */
		int airTemp = logbook.getAirTemp();

		/*
		 * Get the wind speed label
		 */
		String windSpeedLabel = logbook.getWindSpeedLabel();

		/*
		 * Get the water level
		 */
		String waterLevel = logbook.getWaterLevel();

		/*
		 * Get the author
		 */
		String author = logbook.getAuthor();

		/*
		 * Get the wind direction
		 */
		String windDirection = logbook.getWindDirection();

		/*
		 * Get the sea state
		 */
		String seaState = logbook.getSeaState();

		/*
		 * Get the weather sky
		 */
		String weatherSky = logbook.getWeatherSky();

		/*
		 * Get the air temperature label
		 */
		String airTempLabel = logbook.getAirTempLabel();

		/*
		 * Get the motor time
		 */
		String motorTime = logbook.getMotorTime();
		
		/*
		 * Validation of the fields
		 */

		ValidationSet validation = new ValidationSet();
		validation.add((IFormValidator) new FormValidator_Text_Length(logbookAriPlace,1,64));
		validation.add((IFormValidator) new FormValidator_Text_Length(logbookDepPlace,1,64));
		validation.add((IFormValidator) new FormValidator_Text_Length(logbookLongtitudeDeg,1,64));
		validation.add((IFormValidator) new FormValidator_Text_Length(logbookLongtitudeMin,1,64));
		validation.add((IFormValidator) new FormValidator_Text_Length(logbookLatitudeDeg,1,64));
		validation.add((IFormValidator) new FormValidator_Text_Length(logbookLatitudeMin,1,64));

		if (validation.isValid()) {

			//--------------------------------------------------------------------------//
			//-------  SET THE LOGBOOKRECORDUPDATE OBJECT WITH THE UPDATED VALUES ------//
			//--------------------------------------------------------------------------//
	
			/*
			 * Initialize a LogbookRecordUpdate Object
			 */

			LogbookRecordUpdate logbookUpdate = new LogbookRecordUpdate();

			logbookUpdate.setDepaturePlace(logbookDepPlace.getText().toString());
			logbookUpdate.setDepartureDateTime(logbookDepDate.getText().toString());
			logbookUpdate.setDepatureTimeZone(Double.parseDouble(logbookDepTimeZone.getSelectedItem().toString()));
			logbookUpdate.setArrivalPlace(logbookAriPlace.getText().toString());
			logbookUpdate.setArrivalDateTime(logbookAriDate.getText().toString());
			logbookUpdate.setArrivalTimeZone(Double.parseDouble(logbookAriTimeZone.getSelectedItem().toString()));
			logbookUpdate.setLongitudeDegrees(Double.parseDouble(logbookLongtitudeDeg.getText().toString()));
			logbookUpdate.setLongitudeMinutes(Double.parseDouble(logbookLongtitudeMin.getText().toString()));
			logbookUpdate.setLongitudeDirection(logbookLongtitudeDir.getSelectedItem().toString());
			logbookUpdate.setLatitudeDegrees(Double.parseDouble(logbookLatitudeDeg.getText().toString()));
			logbookUpdate.setLatitudeMinutes(Double.parseDouble(logbookLatitudeMin.getText().toString()));
			logbookUpdate.setLatitudeDirection(logbookLatitudeDir.getSelectedItem().toString());
			logbookUpdate.setNotes(notes);
			logbookUpdate.setStopoverPlace(stopoverPlace);
			logbookUpdate.setCrewGuests(crewGuests);
			logbookUpdate.setStopoverDateTime(stopoverDateTime);
			logbookUpdate.setStopoverTimeZone(stopoverTimeZone);
			logbookUpdate.setWindSpeed(windSpeed);
			logbookUpdate.setFuelStatus(fuelStatus);
			logbookUpdate.setAirTemp(airTemp);
			logbookUpdate.setWindSpeedLabel(windSpeedLabel);
			logbookUpdate.setWaterLevel(waterLevel);
			logbookUpdate.setAuthor(author);
			logbookUpdate.setWindDirection(windDirection);
			logbookUpdate.setSeaState(seaState);
			logbookUpdate.setWeatherSky(weatherSky);
			logbookUpdate.setAirTempLabel(airTempLabel);
			logbookUpdate.setMotorTime(motorTime);

			/*
			 * CALL THE LOGBOOK MODEL TO UPDATE THE VALUE
			 */

			ModelManager.getInstance_ModelLogbook().updateLogbook(logbookUpdate);
			Utilities.toast(getApplicationContext(), getApplicationContext().getString(R.string.activity_logbook_updated));
		} else {

			Utilities.toast(getApplicationContext(), getApplicationContext().getString(R.string.form_unvalid));
		}
	}


	/**
	 * Creation of the dialog for picking dates / times. 
	 * 
	 * @param id Description of the selection kind and context.
	 */
	@Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {
	        case DEP_DATE_DIALOG_ID:
	            return new DatePickerDialog(this, mDepDateSetListener, DEP_YEAR, DEP_MONTH, DEP_DAY);
	
	        case DEP_TIME_DIALOG_ID:
	            return new TimePickerDialog(this, mDepTimeSetListener, DEP_HOUR, DEP_MINUTE, false);

		    case ARI_DATE_DIALOG_ID:
		        return new DatePickerDialog(this, mAriDateSetListener, ARI_YEAR, ARI_MONTH, ARI_DAY);
		
		    case ARI_TIME_DIALOG_ID:
		        return new TimePickerDialog(this, mAriTimeSetListener, ARI_HOUR, ARI_MINUTE, false);
	    }

        return null;
    }

	/**
	 * Adds the 1st digit (0) to numbers smaller <10.
	 * 
	 * @param c The refered number
	 */
	private static String pad(int c) {
    	if (c >= 10)
    		return String.valueOf(c);
    	else
    		return "0" + String.valueOf(c);
    }

	/**
	 * Builds the string to the date based on the inputs made after calling showDialog
	 */
	private void updateDepDate() {

		logbookDepDate.setText(
	        new StringBuilder()
	                // Month is 0 based so add 1
	        		.append(DEP_MONTH + 1).append("/")    
	        		.append(DEP_DAY).append("/")
	                .append(DEP_YEAR).append(" "));

	    showDialog(DEP_TIME_DIALOG_ID);
	}

	/**
	 * Builds the string to the time based on the inputs made after calling showDialog
	 */
	public void updateDepTime()
	{
		String final_date = null;
		if (DEP_HOUR > 12) {
			final_date = pad(DEP_HOUR-12) + ":" + pad(DEP_MINUTE) + " PM";
		}

		if (DEP_HOUR < 12) {
			final_date = pad(DEP_HOUR) + ":" + pad(DEP_MINUTE) + " AM";
		}

		if (DEP_HOUR == 12) {
			final_date = "12:"  + pad(DEP_MINUTE) + " AM";
		}

		logbookDepDate.setText(logbookDepDate.getText().append(final_date));
	}

	/**
	 * Datepicker dialog generation  
	 */
	private DatePickerDialog.OnDateSetListener mDepDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            DEP_YEAR = year;
            DEP_MONTH = monthOfYear;
            DEP_DAY = dayOfMonth;
            updateDepDate();
        }
	};


	/**
	 * Timepicker dialog generation
	 */
    private TimePickerDialog.OnTimeSetListener mDepTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            DEP_HOUR = hourOfDay;
            DEP_MINUTE = minute;
            updateDepTime();
        }
    };

	/**
	 * Builds the string to the date based on the inputs made after calling showDialog
	 */
	private void updateAriDate() {

		logbookAriDate.setText(
	        new StringBuilder()
	                // Month is 0 based so add 1
	        		.append(ARI_MONTH + 1).append("/")        
	        		.append(ARI_DAY).append("/")
	                .append(ARI_YEAR).append(" "));

	    showDialog(ARI_TIME_DIALOG_ID);
	}
	
	/**
	 * Builds the string to the time based on the inputs made after calling showDialog
	 */
	public void updateAriTime()
	{
		String final_date = null;
		if (ARI_HOUR > 12) {
			final_date = pad(ARI_HOUR-12) + ":" + pad(ARI_MINUTE) + " PM";
		}

		if (ARI_HOUR < 12) {
			final_date = pad(ARI_HOUR) + ":" + pad(ARI_MINUTE) + " AM";
		}

		if (ARI_HOUR == 12) {
			final_date = "12:"  + pad(ARI_MINUTE) + " AM";
		}

		logbookAriDate.setText(logbookAriDate.getText().append(final_date));
	}

	/**
	 * Datepicker dialog generation  
	 */
	private DatePickerDialog.OnDateSetListener mAriDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            ARI_YEAR = year;
            ARI_MONTH = monthOfYear;
            ARI_DAY = dayOfMonth;
            updateAriDate();
        }
	};

	/**
	 * Timepicker dialog generation
	 */
    private TimePickerDialog.OnTimeSetListener mAriTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            ARI_HOUR = hourOfDay;
            ARI_MINUTE = minute;
            updateAriTime();
        }
    };
}
