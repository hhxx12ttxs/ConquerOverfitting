package com.main;

import android.app.Activity;
import android.os.Bundle;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.SystemClock;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class NewAlarm extends Activity {
    Toast mToast;
    
    TimePicker alarm_time = null;
    
    Calendar calendar = Calendar.getInstance();
    int hours = calendar.get(Calendar.HOUR_OF_DAY);
    int mins = calendar.get(Calendar.MINUTE);
    int c_month = calendar.get(Calendar.MONTH);
    int c_date = calendar.get(Calendar.DAY_OF_MONTH);
    int c_year = calendar.get(Calendar.YEAR);
    DatePicker date;
    Button dateButton;
    Button descButton;
    EditText description; 
    protected AppPreferences appPrefs;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        appPrefs = new AppPreferences(getApplicationContext());

        setContentView(R.layout.newalarm);
        date = (DatePicker)findViewById(R.id.datePicker1);
        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.one_shot);
        button.setOnClickListener(mOneShotListener);
        button.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
        
        
        alarm_time = (TimePicker)findViewById(R.id.timePicker1);
        alarm_time.setOnTimeChangedListener(new OnTimeChangedListener() {

            @Override
            public void onTimeChanged(TimePicker arg0, int hour, int min) {
                hours = hour;
                mins = min;
                
            }
            
        });
        
        date.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker arg0, int arg1, int arg2,
                    int arg3) {
                
                   c_date = arg3;
                   c_month = arg2;
                   c_year = arg1;
                }
                
            });
                
        dateButton = (Button)findViewById(R.id.dateButton2);
        descButton = (Button)findViewById(R.id.descButton2);
        description = (EditText)findViewById(R.id.alarm_description);
        date.setVisibility(4);
        descButton.getBackground().setColorFilter(0xFFFFFF00, PorterDuff.Mode.MULTIPLY);
        
        dateButton.getBackground().setColorFilter(0xFFC0C0C0, PorterDuff.Mode.MULTIPLY);
        dateButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                date.setVisibility(0);
                dateButton.getBackground().setColorFilter(0xFFFFFF00, PorterDuff.Mode.MULTIPLY);
                description.setVisibility(4);
                descButton.getBackground().setColorFilter(0xFFC0C0C0, PorterDuff.Mode.MULTIPLY);
                
            }
            
        });
        
        descButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                date.setVisibility(4);
                description.setVisibility(0);
                descButton.getBackground().setColorFilter(0xFFFFFF00, PorterDuff.Mode.MULTIPLY);
                
                dateButton.getBackground().setColorFilter(0xFFC0C0C0, PorterDuff.Mode.MULTIPLY);
                
            }
            
        });
            
            
         
         

           
    }
    
    

    private OnClickListener mOneShotListener = new OnClickListener() {
        public void onClick(View v) {
            // When the alarm goes off, we want to broadcast an Intent to our
            // BroadcastReceiver.  Here we make an Intent with an explicit class
            // name to have our own receiver (which has been published in
            // AndroidManifest.xml) instantiated and called, and then create an
            // IntentSender to have the intent executed as a broadcast.
            
            Intent intent = new Intent(NewAlarm.this, OneShotAlarm.class);
            PendingIntent sender = PendingIntent.getBroadcast(NewAlarm.this,
                    0, intent, 0);

          
            Date date = new Date();
            int year = c_year;
            int day = c_date;
            int month = c_month;
            
            calendar.set(year, month, day, hours, mins, 0);
            
            

            // Schedule the alarm!
            
            
            SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(getBaseContext());
            mySQLiteAdapter.openToWrite();
            appPrefs.saveSnoozeCount(Integer.toString(Integer.parseInt(appPrefs.getSnoozeCount())));
            int true_month = month + 1;
            mySQLiteAdapter.insertAlarm("", description.getText().toString(), hours + ":" + mins, appPrefs.getSnoozeCount(),true_month + "-" + day + "-" + year);
            
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

            // Tell the user about what we did.
            if (mToast != null) {
                mToast.cancel();
            }
            //mToast = Toast.makeText(NewAlarm.this, "one_shot_scheduled",      Toast.LENGTH_LONG);
            //mToast.show();
            finish();
        }
    };

    
    
}


