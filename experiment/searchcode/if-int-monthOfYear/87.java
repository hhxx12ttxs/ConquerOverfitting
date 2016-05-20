package com.softporcupine.progresstracker;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.softporcupine.progresstracker.gdocs.GDocsAuthenticateActivity;
import com.softporcupine.progresstracker.gdocs.GDocsHelper;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NewOrEditGoalActivity extends Activity {
	
	private String name;
	private float goal;
	private Date endDate;
	private Date startDate;
	private String units;
	private TextView summaryText;
	private TextView daysText;
	private Button startDateButton;
	private Button endDateButton;
	
	private static final int DATE_DIALOG_START = 0;
	private static final int DATE_DIALOG_END = 1;
	
	// Editing a goal that has already been created, or creating a new one?
	private boolean isCurrentGoal;
	
    // The callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener startDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    startDate = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
                    calculateAndUpdateText();
                }
            };
            
    // The callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener endDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    endDate = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
                    calculateAndUpdateText();
                }
            };        
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Determine if we're creating a brand new goal or editing a currently existing one
        Bundle mBundle = getIntent().getExtras();
        if (mBundle == null) {
        	isCurrentGoal = false;
        } else {
        	Log.i("NewOrEditGoalActivity", "mBundle.getBoolean(\"isCurrentGoal\"): " + mBundle.getBoolean("isCurrentGoal"));
        	isCurrentGoal = mBundle.getBoolean("isCurrentGoal");
        }

        setContentView(R.layout.new_or_edit_goal);
        
        final EditText goalNameField = (EditText) findViewById(R.id.goal_name);
        final EditText goalValueField = (EditText) findViewById(R.id.goal_value);
        final EditText goalUnitsField = (EditText) findViewById(R.id.goal_units);
        startDateButton = (Button) findViewById(R.id.start_date_button);
        final Button increaseButton = (Button) findViewById(R.id.increase_day);
        daysText = (TextView) findViewById(R.id.day_count);
        final Button decreaseButton = (Button) findViewById(R.id.decrease_day);
        endDateButton = (Button) findViewById(R.id.enter_end_date);
        final Button createButton = (Button) findViewById(R.id.button_confirm_goal);
        summaryText = (TextView) findViewById(R.id.daily_goal_calculation);
        
        
        calculateDates();
        
        Utilities.logDebug("The start date has been calculated to: " + startDate);
        
        if (isCurrentGoal) {
        	Goal mGoal = CurrentGoal.getCurrentGoal();
        	name = mGoal.getName();
        	units = mGoal.getUnits();
        	goal = mGoal.getGoal();
        	goalNameField.setText(name);
        	goalValueField.setText(Float.toString(goal));
        	goalUnitsField.setText(units);
        	createButton.setText(R.string.done_editing);
        	calculateAndUpdateText();
        }

        /* Goal Value -- update text on change */
        goalValueField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean isFocused) {
				if (isFocused == false) {
					try {
						goal = Float.valueOf(goalValueField.getText().toString());
						calculateAndUpdateText();
					} catch (Exception e) {
						// Don't calculate and update text
					}
				}
			}
		});
        
        /* Goal units -- update text on change */
        goalUnitsField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean isFocused) {
				if (isFocused == false) {
					units = goalUnitsField.getText().toString();
				}
			}
		});
        
        /* Start date */
        startDateButton.setText(Goal.SDF.format(startDate));
        startDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Unfocus all the damn buttons
				goalNameField.clearFocus();
				goalValueField.clearFocus();
				goalUnitsField.clearFocus();
				
				showDialog(DATE_DIALOG_START);
			}
		});
        
        /* End date */
        decreaseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Unfocus all the damn buttons
				goalNameField.clearFocus();
				goalValueField.clearFocus();
				goalUnitsField.clearFocus();
				
				endDate.setTime(endDate.getTime() - 86400000);
				calculateAndUpdateText();
				endDateButton.setText(Goal.SDF.format(endDate));
			}
		});
        increaseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Unfocus all the damn buttons
				goalNameField.clearFocus();
				goalValueField.clearFocus();
				goalUnitsField.clearFocus();
				
				endDate.setTime(endDate.getTime() + 86400000);
				Log.i("NewOrEditGoalActivy", "+ startDate: " + startDate);
				calculateAndUpdateText();
				endDateButton.setText(Goal.SDF.format(endDate));
			}
		});
        
        endDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Unfocus all the damn buttons
				goalNameField.clearFocus();
				goalValueField.clearFocus();
				goalUnitsField.clearFocus();
				
				showDialog(DATE_DIALOG_END);
			}
		});
        
        /* Create button - do some validation and set CurrentGoal */
        createButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Read in the values:
				String newName = goalNameField.getText().toString().trim();
				units = goalUnitsField.getText().toString().trim();
				
				try {
					goal = Float.valueOf(goalValueField.getText().toString());
				} catch (Exception e) {
					goal = 0;
				}
				
				String error = validate(newName);
				if (error == null) {
					// Either save this (update current goal) or set this new goal as the current goal
					if (isCurrentGoal) {
						
						CurrentGoal.updateGoal(goal, units, startDate, endDate);				
						
						if (newName.compareTo(name) != 0) {
							CurrentGoal.rename(getApplicationContext(), newName);
						}
						CurrentGoal.saveToDisk(getApplicationContext());
					} else {
						CurrentGoal.setCurrentGoal(getApplicationContext(), new Goal(newName, goal, units, startDate, endDate));
					}
					
					// Move to the goal main menu
					Intent i = new Intent(NewOrEditGoalActivity.this, GoalActivity.class);
	            	startActivity(i);
	            	
	            	// End this activity
	            	finish();
				} else {
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(getApplicationContext(), error, duration);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.new_or_edit_goal_menu, menu);
        return true;
    }
        
   /* Menu buttons - when a user presses the menu button and then one of these */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.importFile:
            	// Move to FileList Activity
            	Intent i1 = new Intent(NewOrEditGoalActivity.this, FileListActivity.class);
            	i1.putExtra("isRemote", false);
            	startActivity(i1);
                return true;
            case R.id.importSpreadsheet:
            	if(!GDocsHelper.isLinked(getApplicationContext())) {
        			Intent i2 = new Intent(NewOrEditGoalActivity.this, GDocsAuthenticateActivity.class);
        			
        			startActivity(i2);
        			
        			Toast toast = Toast.makeText(getApplicationContext(), "You must link your Google Docs account before importing from Google Docs", Toast.LENGTH_SHORT);
        			toast.show();
        		}
        		// Move to FileList Activity
            	Intent i3 = new Intent(NewOrEditGoalActivity.this, FileListActivity.class);
            	i3.putExtra("isRemote", true);
        		startActivity(i3);
                return true;
            case R.id.importClone:
            	return true;
            default:
            	return false;
        }
    }

    
    private String validate(String name) {
		String error = null;
		if (endDate == null || endDate.getTime() - startDate.getTime() <= 0 && !isCurrentGoal) {
			error = "Pick an end date in the future.";
		}
		if (units == null || units.equalsIgnoreCase("")) {
			error = "Enter in the units.";
		}
		if (goal <= 0) {
			error = "Enter a goal that is greater than 0.";
		}
		if (name == null || name.equalsIgnoreCase("")) {
			error = "Give this goal a name.";
		}
		return error;
    }
    
    private void calculateAndUpdateText() {   	
    	long difference = endDate.getTime() - startDate.getTime();
    	int differenceDays = (int) (difference/86400000); // 86400000 ms in 1 day;
    	
    	startDateButton.setText(Goal.SDF.format(startDate));
    	daysText.setText(differenceDays + " days");
    	endDateButton.setText(Goal.SDF.format(endDate));
    	
    	if (units == null) {
    		units = "";
    	}
    	
    	if (goal > 0 && differenceDays == 1) {
        	DecimalFormat df = new DecimalFormat("0.00");
        	summaryText.setText("There is 1 day to complete this goal. That's a daily goal of " + df.format(goal) + " " + units);
    	} else if (goal > 0 && differenceDays > 1) {
    		float dailyGoal = goal / differenceDays;
        	DecimalFormat df = new DecimalFormat("0.00");
        	summaryText.setText("There are " + differenceDays + " days to complete this goal. That's a daily goal of " + df.format(dailyGoal) + " " + units);
    	} else {
    		summaryText.setText("Create a goal and pick a completion date in the future.");
    	}
    }
    
    private void calculateDates() {
    	Log.i("NewOrEditGoalActivity","calculateDates() is called");
    	if (!isCurrentGoal) {
            // Take the date and round to the DAY (not minute)
            // There's probably a better way to do this?
    		Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            startDate = new GregorianCalendar(year, month, day).getTime();
            endDate = new GregorianCalendar(year, month, day).getTime();
    	} else {
    		startDate = CurrentGoal.getCurrentGoal().getStartDate();
    		endDate = CurrentGoal.getCurrentGoal().getEndDate();
    	}
    }
    
    protected Dialog onCreateDialog(int id) {
    	//TODO Bug if the user hits the "cancel" button, onCreateDialog is not called the next time this dialog should to appear
    	//	   This makes the date look stale
    	Calendar c = Calendar.getInstance();
        switch (id) {
        case DATE_DIALOG_START:
        	c.setTime(startDate);
            return new DatePickerDialog(this, startDateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        case DATE_DIALOG_END:
        	c.setTime(endDate);
            return new DatePickerDialog(this, endDateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        }
        return null;
    }
}

