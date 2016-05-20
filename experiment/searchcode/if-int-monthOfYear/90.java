/*
 * Copyright (C) Trungthi (Calvin) Bui 2014
 */
package com.id11413010.circle.app.events;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.id11413010.circle.app.Constants;
import com.id11413010.circle.app.R;
import com.id11413010.circle.app.dao.EventDAO;
import com.id11413010.circle.app.pojo.Event;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This class is used to create a new Events. Users can fill out the details of their event which is
 * then created within the database. The event requires a name, description, location, start and
 * end date as well as time. When the event has been created, the user is redirected to the class
 * listing all events within their group of friends.
 */
public class EventAdd extends Activity {
    /**
     * These TextViews will represent the start date selected by the user
     */
    private TextView startDate;
    /**
     * These TextViews will represent the end date selected by the user
     */
    private TextView endDate;
    /**
     * These TextViews will represent the start time selected by the user
     */
    private TextView startTime;
    /**
     * These TextViews will represent the end time selected by the user
     */
    private TextView endTime;
    /**
     * The EditTexts representing the name of the event being created by the user.
     */
    private EditText name;
    /**
     * The EditTexts representing the location of the event being created by the user
     */
    private EditText location;
    /**
     * The EditTexts representing the details of the event being created by the user
     */
    private EditText details;
    /**
     * The starting time and date for the event, to compare with the ending time/date
     */
    private Calendar startDateTime;
    /**
     * The ending time and date for the event, to compare with the starting time/date
     */
    private Calendar endDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_add);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //finds and stores a view that was identified by the id attribute
        name = (EditText) findViewById(R.id.eventName); //EditText for the event name
        location = (EditText) findViewById(R.id.eventLocation); //EditText for event location
        details = (EditText) findViewById(R.id.eventDescription); //EditText for event details
        startDate = (TextView) findViewById(R.id.eventStartDate); //TextView for start date
        endDate = (TextView) findViewById(R.id.eventEndDate); //TextView for end date
        startTime = (TextView) findViewById(R.id.eventStartTime); //TextView for start time
        endTime = (TextView) findViewById(R.id.eventEndTime); //TextView for end time
        // initialise the calendar
        startDateTime = Calendar.getInstance();
        endDateTime = Calendar.getInstance();
        Log.i(Constants.LOG, "Started Event Add");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.event_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        if (id == R.id.createEvent) {
            /*
            when the user clicks on this option, it will execute an AsyncTask to add the event to
            the database.
             */
            if (validateFields()) {
                new CreateEventTask().execute();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Validates all fields have been entered or chosen.
     */
    public boolean validateFields() {
        // check if any fields are empty
        if (name.getText().toString().equals("") || location.getText().toString().equals("") ||
                details.getText().toString().equals("") || startDate.getText().toString().equals("")
                || endDate.getText().toString().equals("") || startTime.getText().toString().equals("")
                || endTime.getText().toString().equals("")) {
            // create a toast with a message of what is missing.
            // toast the user they should enter all fields
            Toast.makeText(getApplicationContext(), getText(R.string.missingFields).toString(), Toast.LENGTH_LONG).show();
            return false;
            // check if the start day is before this exact moment
        } else if (startDateTime.before(Calendar.getInstance())) {
            Toast.makeText(this, getText(R.string.dateError), Toast.LENGTH_SHORT).show();
            return false;
        } // check if the end date is after the start date
        else if (startDateTime.after(endDateTime)) {
            Toast.makeText(this, getText(R.string.dateError), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    /**
     * Creates and shows a DatePickerDialog for the user to select a start or end date. Currently
     * attached to an onClick event within the XML.
     */
    public void pickEndDate(View v) {
        if (isStartEmpty()) {
            // Process to get current date (today)
            final Calendar c = Calendar.getInstance();

            // Create a DatePickerDialog using the current date captured above as the starting position
            DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    // Display selected date in TextView upon selection.
                    endDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    endDateTime.set(Calendar.MONTH, monthOfYear);
                    endDateTime.set(Calendar.YEAR, year);
                    endDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                }
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            // show the DatePickerDialog.
            dpd.show();
        }
    }

    /**
     * Creates and shows a DatePickerDialog for the user to select the end date. Currently
     * attached to an onClick event within the XML.
     */
    public void pickStartDate(View v) {
        // Process to get Current Date
        final Calendar c = Calendar.getInstance();

        // Launch Date Picker Dialog
        DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Display Selected date in textbox
                        startDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        startDateTime.set(Calendar.MONTH, monthOfYear);
                        startDateTime.set(Calendar.YEAR, year);
                        startDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show();
    }

    /**
     * Creates and shows a TimePickerDialog for the user to select a start or end time. Currently
     * attached to an onClick event within the XML.
     */
    public void pickStartTime(View v) {
        // Process to get current time (right now)
        final Calendar c = Calendar.getInstance();

        // Create new TimePickerDialog using the time captured above as the starting position.
        TimePickerDialog tpd = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                try {
                    Date date = new SimpleDateFormat("HH:mm").parse(hourOfDay + ":" + minute);
                    // Display Selected time in textbox
                    DateFormat df = new SimpleDateFormat("HH:mm");
                    // Display selected time in TextView
                    startDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    startDateTime.set(Calendar.MINUTE, minute);
                    startTime.setText(df.format(date));
                } catch (ParseException e) {
                    Log.e(Constants.LOG, "Event Add - SimpleDateFormat ParseException");
                }
            }
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
        // show the the created TimePickerDialog
        tpd.show();
    }

    /**
     * Creates and shows a TimePickerDialog for the user to select an end time. Currently
     * attached to an onClick event within the XML.
     */
    public void pickEndTime(View v) {
        if (isStartEmpty() && isEndDateEmpty()) {
            // Process to get Current Time
            final Calendar c = Calendar.getInstance();
            // Launch Time Picker Dialog
            TimePickerDialog tpd = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            endDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            endDateTime.set(Calendar.MINUTE, minute);
                            try {
                                Date date = new SimpleDateFormat("HH:mm").parse(hourOfDay + ":" + minute);
                                // Display Selected time in textbox
                                DateFormat df = new SimpleDateFormat("HH:mm");
                                endTime.setText(df.format(date));
                            } catch (ParseException e) {
                                Log.e(Constants.LOG, "Event Add - SimpleDateFormat ParseException");
                            }
                        }
                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false
            );
            tpd.show();
        }
    }

    private boolean isEndDateEmpty() {
        return !endDate.getText().toString().equals("");
    }

    private boolean isStartEmpty() {
        if (startTime.getText().toString().equals("") || startDate.getText().toString().equals("")) {
            Toast.makeText(this, getText(R.string.enterstarttimedate), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * An AsyncTask which captures the information inputted by the User and sends it via Internet
     * to the a web service to be added into the database. Separates network activity from the main
     * thread.
     * <p/>
     * Creates a new event into the database.
     */
    private class CreateEventTask extends AsyncTask<Void, Void, Void> {
        /**
         * A String representing the Starting Date
         */
        private String startDateString;
        /**
         * A String representing the End Date
         */
        private String endDateString;
        /**
         * A String representing a circle
         */
        private String circle;

        protected void onPreExecute() {
            // format the date inputted by the user using the DatePickerDialog to a date readable by the database in the format YYYY-MM-DD.
            try {
                // format the start and end date from DD-MM-YYYY format to YYYY-MM-DD format.
                startDateString = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd-MM-yyyy").parse(startDate.getText().toString()));
                endDateString = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd-MM-yyyy").parse(endDate.getText().toString()));
            } catch (Exception e) {
                Log.e(Constants.LOG, "Event Add - SimpleDateFormat exception");
            }
            // retrieves the User's Events ID stored within the Shared Preferences and store it within the String circle.
            SharedPreferences sp = getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
            circle = sp.getString(Constants.CIRCLE, null);
        }

        protected Void doInBackground(Void... params) {
            // create a new Event object containing event data.
            Event event = new Event(name.getText().toString(), details.getText().toString(),
                    location.getText().toString(), startDateString, endDateString, endTime.getText().toString(),
                    startTime.getText().toString(), circle);
            // pass the object to the data-access-object class to add it to the database
            EventDAO.createEvent(event);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // return the user to the activity Listing the events.
            startActivity(new Intent(EventAdd.this, Events.class));
            finish();
        }
    }
}

