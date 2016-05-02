package com.kylegengler.sleepcalc;

import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.StringTokenizer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SleepCalcActivity extends Activity{
	
	final int CYCLE = 90;
	protected int alarmHours, alarmMinutes;
	int[][] sleepTime = new int[2][2];
	StringBuffer bedTime = new StringBuffer();
	DecimalFormat df = new java.text.DecimalFormat("00");
	
	@Override
		public void onCreate(Bundle savedInstanceState) {

			Log.i(DEBUG, "Activity started");
 
			super.onCreate(savedInstanceState);
			setContentView(R.layout.sleepcalc);

			Log.i(DEBUG, "Content view started");
		}
		Scanner keyboard = new Scanner(System.in);
		
		public void onCalcButtonClicked(View v) {
			
			Log.i(DEBUG, "Calculate button clicked");
			
			getAlarm();
			printString();
		}

		// TAKE IN ALARM TIME AND PASS TO setAlarm()
		public void getAlarm() {
			Log.i(DEBUG, "Getting Alarm Time");
			final EditText alarmTime = (EditText) findViewById(R.id.alarmtime);
			Log.i(DEBUG, "Alarm Time: "+alarmTime.getText().toString());
			Log.i(DEBUG, "Passing to setAlarm");
			setAlarm(alarmTime.getText().toString());
		}

		// SET CLASS ALARM VARIABLE
		public void setAlarm(String Alarm_Time) {
			String[] time = new String[2];
			int counter = 0;
			StringTokenizer st = new StringTokenizer(Alarm_Time);
			while (st.hasMoreTokens()) {
				time[counter] = st.nextToken();
				counter++;
			}
			alarmHours = Integer.parseInt(time[0]);
			alarmMinutes = Integer.parseInt(time[1]);

			while (alarmMinutes >= 60) {
				alarmMinutes -= 60;
				alarmHours++;
			}
			
			Log.i(DEBUG, "Alarm Time Set");

		}

		// function that calculates the ideal sleep time when called. Not final
		// product by any means
		public void calculateSleepTime() {
			int timeInMinutes = (alarmHours * 60) + alarmMinutes;

			int[] sleepTimeMinutes = new int[2];
			String[] ampm = new String[2];

			sleepTimeMinutes[0] = timeInMinutes - (CYCLE * 5);
			sleepTimeMinutes[1] = timeInMinutes - (CYCLE * 6);

			for (int i = 0; i < sleepTimeMinutes.length; i++) {
				sleepTime[i][0] = sleepTimeMinutes[i] / 60;
				sleepTime[i][1] = sleepTimeMinutes[i] % 60;
			}

			for (int row = 0; row < 2; row++) {
				if (sleepTime[row][0] < 0)
					sleepTime[row][0] += 24;
				else if (sleepTime[row][0] == 0)
					sleepTime[row][0] = 24;

				if (sleepTime[row][1] < 0) {
					sleepTime[row][1] += 60;
					sleepTime[row][0]--;
				}

				while (sleepTime[row][1] >= 60) {
					sleepTime[row][1] -= 60;
					sleepTime[row][0]++;
				}

				if (sleepTime[row][0] > 12 && sleepTime[row][0] < 24) {
					ampm[row] = "PM";
					sleepTime[row][0] -= 12;
				} else if (sleepTime[row][0] == 24) {
					ampm[row] = "AM";
					sleepTime[row][0] -= 12;
				} else {
					ampm[row] = "AM";
				}

			}

			bedTime.append("You should try to fall asleep at ");
			bedTime.append(df.format(sleepTime[0][0]) + ":" + df.format(sleepTime[0][1]) + " "
					+ ampm[0] + " or " + df.format(sleepTime[1][0]) + ":" + df.format(sleepTime[1][1])
					+ " " + ampm[1]);

		}

		// FORMAT AND PRINT
		public void printString() {
			String ampm;
			int hours = alarmHours;

			TextView bedtime = (TextView) findViewById(R.id.bedtimemessage);
			
			bedTime.append("\n\nThe Alarm is set for: ");

			if (alarmHours > 12 && alarmHours < 24) {
				ampm = "PM";
				hours = alarmHours - 12;
			} else if (alarmHours == 24) {
				ampm = "AM";
				hours = alarmHours - 12;
			} else {
				ampm = "AM";
				hours = alarmHours;
			}

			bedTime.append(df.format(hours) + ":" + df.format(alarmMinutes) + " " + ampm + "\n\n");
			calculateSleepTime();
			
			bedtime.setText(bedTime.toString()); 
		}
		private static String DEBUG = "***DEBUG - SLEEP CALC ACTIVITY: ";
}
