package com.diegomartin.telemaco.view;

import java.text.DateFormat;
import java.util.Calendar;
import java.sql.Date;

import com.diegomartin.telemaco.R;
import com.diegomartin.telemaco.control.ActionsFacade;
import com.diegomartin.telemaco.control.TripControl;
import com.diegomartin.telemaco.model.Trip;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

public class TripActivity extends Activity {
    protected static final int START_DATE_DIALOG_ID = 0;
    protected static final int END_DATE_DIALOG_ID = 1;
    private static final int YEAR_OFFSET = 1900;

	private Button startDateButton;
    private Button endDateButton;
    private Button saveButton;
    
    private long id;
    private EditText name;
    private EditText description;
    private Date startDate;
    private Date endDate;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip);
        
        this.startDateButton = (Button) findViewById(R.id.startDate);
        this.endDateButton = (Button) findViewById(R.id.endDate);
        this.saveButton = (Button) findViewById(R.id.save);
        this.name = (EditText) findViewById(R.id.name);
        this.description = (EditText) findViewById(R.id.description);

        // add a click listener to the button
        this.startDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(START_DATE_DIALOG_ID);
            }
        });
        
        this.endDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(END_DATE_DIALOG_ID);
            }
        });
        
        Bundle received = getIntent().getExtras();
        
        if(received == null) {
        	// New trip
        	// get the current date
            final Calendar c = Calendar.getInstance();
            Date now = new Date(c.getTimeInMillis());
            
            this.startDate = new Date(now.getYear(), now.getMonth(), now.getDate());
            this.endDate = new Date(now.getYear(), now.getMonth(), now.getDate());
            updateDisplay();
            
            this.saveButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                	createTrip();
                }
            });

        } else {
        	// Edit Trip
            Trip trip = (Trip) received.get(ActionsFacade.EXTRA_TRIP);
        	
        	this.id = trip.getId();
        	this.name.setText(trip.getName());
        	this.description.setText(trip.getDescription());
        	this.startDate = trip.getStartDate();
        	this.endDate = trip.getEndDate();
            updateDisplay();
            
            this.saveButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                	editTrip();
                }
            });
        }
	}
	
	private void createTrip(){
		String n = this.name.getText().toString();
    	if (n.length()>0){
    		if (this.startDate.getTime() <= this.endDate.getTime()) {
    			long id = TripControl.createTrip(n, this.description.getText().toString(), this.startDate, this.endDate);
    			TripControl.setPendingCreate(id, true);
    			finish();
    		}
    		else ToastFacade.show(this, getString(R.string.wrong_dates));
    	}
    	else ToastFacade.show(this, getString(R.string.name_missing));
	}
	
	private void editTrip(){
		String n = this.name.getText().toString();
    	if (n.length()>0){
    		if (this.startDate.getTime() <= this.endDate.getTime()) {
    			TripControl.updateTrip(this.id, n, this.description.getText().toString(), this.startDate, this.endDate);
    			TripControl.setPendingUpdate(this.id, true);
    			finish();
    		}
    		else ToastFacade.show(this, getString(R.string.wrong_dates));
    	}
    	else ToastFacade.show(this, getString(R.string.name_missing));		
	}
	
	// the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener startDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        	startDate = new Date(year-YEAR_OFFSET, monthOfYear, dayOfMonth);
            updateDisplay();
        }
    };
    
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener endDateSetListener = new DatePickerDialog.OnDateSetListener() {
	    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
	    	endDate = new Date(year-YEAR_OFFSET, monthOfYear, dayOfMonth);
	    	updateDisplay();
	    }
    };
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    	case START_DATE_DIALOG_ID:
	    		return new DatePickerDialog(this,
	    									startDateSetListener,
	    									this.startDate.getYear()+YEAR_OFFSET,
	    									this.startDate.getMonth(),
	    									this.startDate.getDate());
	    	case END_DATE_DIALOG_ID:
	    		return new DatePickerDialog(this,
	    									endDateSetListener,
	    									this.endDate.getYear()+YEAR_OFFSET,
	    									this.endDate.getMonth(),
	    									this.endDate.getDate());
	    }
	    return null;
	}
	
    private void updateDisplay() {
    	String date = DateFormat.getDateInstance(DateFormat.LONG).format(this.startDate);
        startDateButton.setText(date);
    	date = DateFormat.getDateInstance(DateFormat.LONG).format(this.endDate);
        endDateButton.setText(date);
    }
}

