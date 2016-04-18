package com.swengr2_di_dj_ly.letournapp_v1_0;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class ScheduleHoursActivity extends Activity {
	
	public static final int[] DAYS_OF_WEEK = 
			new int[] {
		R.string.Day1Title,
		R.string.Day2Title,
		R.string.Day3Title,
		R.string.Day4Title,
		R.string.Day5Title,
		R.string.Day6Title,
		R.string.Day7Title
	};
	
	public static final int[] ASC_MAIN_HOURS = 
			new int[] {
		R.string.Hours_ASC_Main_Day1,
		R.string.Hours_ASC_Main_Day2,
		R.string.Hours_ASC_Main_Day3,
		R.string.Hours_ASC_Main_Day4,
		R.string.Hours_ASC_Main_Day5,
		R.string.Hours_ASC_Main_Day6,
		R.string.Hours_ASC_Main_Day7
	};
	public static final int[] ASC_HIVE_HOURS = 
			new int[] {
		R.string.Hours_ASC_Hive_and_Coffee_Day1,
		R.string.Hours_ASC_Hive_and_Coffee_Day2,
		R.string.Hours_ASC_Hive_and_Coffee_Day3,
		R.string.Hours_ASC_Hive_and_Coffee_Day4,
		R.string.Hours_ASC_Hive_and_Coffee_Day5,
		R.string.Hours_ASC_Hive_and_Coffee_Day6,
		R.string.Hours_ASC_Hive_and_Coffee_Day7
	};
	public static final int[] ASC_COFFEE_HOURS = 
			new int[] {
		R.string.Hours_ASC_Hive_and_Coffee_Day1,
		R.string.Hours_ASC_Hive_and_Coffee_Day2,
		R.string.Hours_ASC_Hive_and_Coffee_Day3,
		R.string.Hours_ASC_Hive_and_Coffee_Day4,
		R.string.Hours_ASC_Hive_and_Coffee_Day5,
		R.string.Hours_ASC_Hive_and_Coffee_Day6,
		R.string.Hours_ASC_Hive_and_Coffee_Day7
	};
	public static final int[] ASC_MAIL_HOURS = 
			new int[] {
		R.string.Hours_ASC_Mail_Day1,
		R.string.Hours_ASC_Mail_Day2,
		R.string.Hours_ASC_Mail_Day3,
		R.string.Hours_ASC_Mail_Day4,
		R.string.Hours_ASC_Mail_Day5,
		R.string.Hours_ASC_Mail_Day6,
		R.string.Hours_ASC_Mail_Day7
	};

	public static final int[] SERV_LIBRARY_HOURS = 
			new int[] {
		R.string.Hours_Services_Library_Day1,
		R.string.Hours_Services_Library_Day2,
		R.string.Hours_Services_Library_Day3,
		R.string.Hours_Services_Library_Day4,
		R.string.Hours_Services_Library_Day5,
		R.string.Hours_Services_Library_Day6,
		R.string.Hours_Services_Library_Day7
	};
	public static final int[] SERV_REGISTRAR_HOURS =
			new int[] {
		R.string.Hours_Services_Registrar_Day1,
		R.string.Hours_Services_Registrar_Day2,
		R.string.Hours_Services_Registrar_Day3,
		R.string.Hours_Services_Registrar_Day4,
		R.string.Hours_Services_Registrar_Day5,
		R.string.Hours_Services_Registrar_Day6,
		R.string.Hours_Services_Registrar_Day7
	};
	public static final int[] SERV_FIN_AID_HOURS = 
			new int[] {
		R.string.Hours_Services_FinancialAid_Day1,
		R.string.Hours_Services_FinancialAid_Day2,
		R.string.Hours_Services_FinancialAid_Day3,
		R.string.Hours_Services_FinancialAid_Day4,
		R.string.Hours_Services_FinancialAid_Day5,
		R.string.Hours_Services_FinancialAid_Day6,
		R.string.Hours_Services_FinancialAid_Day7
	};
	public static final int[] SERV_CASHIER_HOURS = 
			new int[] {
		R.string.Hours_Services_Cashier_Day1,
		R.string.Hours_Services_Cashier_Day2,
		R.string.Hours_Services_Cashier_Day3,
		R.string.Hours_Services_Cashier_Day4,
		R.string.Hours_Services_Cashier_Day5,
		R.string.Hours_Services_Cashier_Day6,
		R.string.Hours_Services_Cashier_Day7
	};

	public static final int[] SAGA_BREAKFAST_HOURS = 
			new int[] {
		R.string.Hours_SAGA_Breakfast_Day1,
		R.string.Hours_SAGA_Breakfast_Day2,
		R.string.Hours_SAGA_Breakfast_Day3,
		R.string.Hours_SAGA_Breakfast_Day4,
		R.string.Hours_SAGA_Breakfast_Day5,
		R.string.Hours_SAGA_Breakfast_Day6,
		R.string.Hours_SAGA_Breakfast_Day7
	};
	public static final int[] SAGA_LUNCH_HOURS = 
			new int[] {
		R.string.Hours_SAGA_Lunch_Day1,
		R.string.Hours_SAGA_Lunch_Day2,
		R.string.Hours_SAGA_Lunch_Day3,
		R.string.Hours_SAGA_Lunch_Day4,
		R.string.Hours_SAGA_Lunch_Day5,
		R.string.Hours_SAGA_Lunch_Day6,
		R.string.Hours_SAGA_Lunch_Day7
	};
	public static final int[] SAGA_DINNER_HOURS = 
			new int[] {
		R.string.Hours_SAGA_Dinner_Day1,
		R.string.Hours_SAGA_Dinner_Day2,
		R.string.Hours_SAGA_Dinner_Day3,
		R.string.Hours_SAGA_Dinner_Day4,
		R.string.Hours_SAGA_Dinner_Day5,
		R.string.Hours_SAGA_Dinner_Day6,
		R.string.Hours_SAGA_Dinner_Day7
	};
	
	public static final int[] SOLHEIM_MAIN_HOURS = 
			new int[] {
		R.string.Hours_Solheim_Main_Day1,
		R.string.Hours_Solheim_Main_Day2,
		R.string.Hours_Solheim_Main_Day3,
		R.string.Hours_Solheim_Main_Day4,
		R.string.Hours_Solheim_Main_Day5,
		R.string.Hours_Solheim_Main_Day6,
		R.string.Hours_Solheim_Main_Day7
	};
	public static final int[] SOLHEIM_POOL1_HOURS = 
			new int[] {
		R.string.Hours_Solheim_Pool1_Day1,
		R.string.Hours_Solheim_Pool1_Day2,
		R.string.Hours_Solheim_Pool1_Day3,
		R.string.Hours_Solheim_Pool1_Day4,
		R.string.Hours_Solheim_Pool1_Day5,
		R.string.Hours_Solheim_Pool1_Day6,
		R.string.Hours_Solheim_Pool1_Day7
	};
	public static final int[] SOLHEIM_POOL2_HOURS = 
			new int[] {
		R.string.Hours_Solheim_Pool2_Day1,
		R.string.Hours_Solheim_Pool2_Day2,
		R.string.Hours_Solheim_Pool2_Day3,
		R.string.Hours_Solheim_Pool2_Day4,
		R.string.Hours_Solheim_Pool2_Day5,
		R.string.Hours_Solheim_Pool2_Day6,
		R.string.Hours_Solheim_Pool2_Day7
	};
	public static final int[] SOLHEIM_POOL3_HOURS = 
			new int[] {
		R.string.Hours_Solheim_Pool3_Day1,
		R.string.Hours_Solheim_Pool3_Day2,
		R.string.Hours_Solheim_Pool3_Day3,
		R.string.Hours_Solheim_Pool3_Day4,
		R.string.Hours_Solheim_Pool3_Day5,
		R.string.Hours_Solheim_Pool3_Day6,
		R.string.Hours_Solheim_Pool3_Day7
	};
	
	public static final int[][] ASC_HOURS = 
			new int[][] {
		ASC_MAIN_HOURS,
		ASC_HIVE_HOURS,
		ASC_COFFEE_HOURS,
		ASC_MAIL_HOURS
	};
	public static final int[][] SERV_HOURS = 
			new int[][] {
		SERV_LIBRARY_HOURS,
		SERV_REGISTRAR_HOURS,
		SERV_FIN_AID_HOURS,
		SERV_CASHIER_HOURS
	};
	public static final int[][] SAGA_HOURS = 
			new int[][] {
		SAGA_BREAKFAST_HOURS,
		SAGA_LUNCH_HOURS,
		SAGA_DINNER_HOURS
	};
	public static final int[][] SOLHEIM_HOURS = 
			new int[][] {
		SOLHEIM_MAIN_HOURS,
		SOLHEIM_POOL1_HOURS,
		SOLHEIM_POOL2_HOURS,
		SOLHEIM_POOL3_HOURS
	};
	
	private TextView[] ascTable;
	private TextView[] servTable;
	private TextView[] sagaTable;
	private TextView[] solheimTable;
	
	private TextView dayView;
	private String[] dayStrings;
	private int displayDay;
	
//	private final String TAG = "HoursDebug";
	private static final String SAVED_STATE_KEY_DAY = "Day";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule_hours);
		initActivityObjects();
		
		if (savedInstanceState != null) {
			displayDay = savedInstanceState.getInt(SAVED_STATE_KEY_DAY);
		} else {
		
			/*
			 * Explanation of the modular arithmetic below:
			 * 
			 * Calendar.get(Calendar.DAY_OF_WEEK) returns the following
			 * Sunday - 1
			 * Monday - 2
			 * Tuesday - 3
			 * Wednesday - 4
			 * Thursday - 5
			 * Friday - 6
			 * Saturday - 7
			 * 
			 * We need this order:
			 * Sunday - 6
			 * Monday - 0
			 * Tuesday - 1
			 * Wednesday - 2
			 * Thursday - 3
			 * Friday - 4
			 * Saturday - 5
			 * 
			 * @author Devon Johnson
			 */
			Calendar cal = Calendar.getInstance();
			displayDay = (cal.get(Calendar.DAY_OF_WEEK) + 5) % DAYS_OF_WEEK.length;
			// Start display with current day of the week
			
		} // end if-else
		
		updateLabelsToDay();
		
	} // end onCreate

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.schedule_hours, menu);
		return true;
	} // end onCreateOptionsMenu

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	} // end onOptionsItemSelected
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(SAVED_STATE_KEY_DAY, displayDay);
		super.onSaveInstanceState(outState);
	}
	
	/**
	 * Inflates the UI objects for use
	 * in changing the text of labels
	 * 
	 * @author Devon Johnson
	 */
	private void initActivityObjects() {
		
		ascTable = new TextView[ASC_HOURS.length];
		servTable = new TextView[SERV_HOURS.length];
		sagaTable = new TextView[SAGA_HOURS.length];
		solheimTable = new TextView[SOLHEIM_HOURS.length];
		
		ascTable[0] = (TextView)findViewById(R.id.Editable_ASC_Main_Hours);
		ascTable[1] = (TextView)findViewById(R.id.Editable_ASC_Hive_Hours);
		ascTable[2] = (TextView)findViewById(R.id.Editable_ASC_Coffee_Hours);
		ascTable[3] = (TextView)findViewById(R.id.Editable_ASC_Mail_Hours);
		
		servTable[0] = (TextView)findViewById(R.id.Editable_Services_Library_Hours);
		servTable[1] = (TextView)findViewById(R.id.Editable_Services_Registrar_Hours);
		servTable[2] = (TextView)findViewById(R.id.Editable_Services_FinancialAid_Hours);
		servTable[3] = (TextView)findViewById(R.id.Editable_Services_Cashier_Hours);
		
		sagaTable[0] = (TextView)findViewById(R.id.Editable_SAGA_Breakfast_Hours);
		sagaTable[1] = (TextView)findViewById(R.id.Editable_SAGA_Lunch_Hours);
		sagaTable[2] = (TextView)findViewById(R.id.Editable_SAGA_Dinner_Hours);
		
		solheimTable[0] = (TextView)findViewById(R.id.Editable_Solheim_Main_Hours);
		solheimTable[1] = (TextView)findViewById(R.id.Editable_Solheim_Pool1_Hours);
		solheimTable[2] = (TextView)findViewById(R.id.Editable_Solheim_Pool2_Hours);
		solheimTable[3] = (TextView)findViewById(R.id.Editable_Solheim_Pool3_Hours);
		
		dayView = (TextView)findViewById(R.id.DayText);
		
		dayStrings = new String[DAYS_OF_WEEK.length];
		for (int i = 0; i < DAYS_OF_WEEK.length; i++)
			dayStrings[i] = getString(DAYS_OF_WEEK[i]);
		
	} // end initActivityObjects
	
	/**
	 * Changes all of the hours labels in the layout
	 * to reflect the hours of a certain day. Also
	 * updates the label at the bottom to reflect
	 * which day has been selected
	 * 
	 * @return true if operation is a success
	 * @author Devon Johnson
	 */
	private boolean updateLabelsToDay() {
		if (displayDay < 0 || displayDay >= DAYS_OF_WEEK.length)
			return false;
		for (int i = 0; i < ascTable.length; i++)
			ascTable[i].setText(getString(ASC_HOURS[i][displayDay]));
		for (int i = 0; i < servTable.length; i++)
			servTable[i].setText(getString(SERV_HOURS[i][displayDay]));
		for (int i = 0; i < sagaTable.length; i++)
			sagaTable[i].setText(getString(SAGA_HOURS[i][displayDay]));
		for (int i = 0; i < solheimTable.length; i++)
			solheimTable[i].setText(getString(SOLHEIM_HOURS[i][displayDay]));
		dayView.setText("Displaying " + dayStrings[displayDay]+ " Hours");
		return true;
	} // end updateLabelsToDay
	
	/**
	 * Called when "Change Day" button is pressed.
	 * Displays an alert dialog so that the user can
	 * select a different day for which to view hours.
	 * 
	 * @param view Required for onClick attribute. Not used.
	 * @author Devon Johnon
	 */
	public void changeDay(View view) {
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Change Day");
		
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alert.setItems(dayStrings, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dayView.setText("Displaying " + dayStrings[which] + " hours");
				displayDay = which;
				updateLabelsToDay();
				dialog.dismiss();
			} // end onClick
		});
		alert.show();
		
	} // end changeDay
	
} // end ScheduleHoursActivity class


