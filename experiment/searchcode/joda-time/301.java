package com.main;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class AlarmDialog extends Activity{
    protected AppPreferences appPrefs;
    Ringtone r;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarmdialog);
      //Toast.makeText(context, "R.string.one_shot_received", Toast.LENGTH_SHORT).show();
        try {
       Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
       r = RingtoneManager.getRingtone(getBaseContext(), notification);
       r.play();
       
       
        }
        catch(Exception e) {
            //ringtone will not work on emulator
        }
        final SQLiteAdapter mySQLiteAdapter = new SQLiteAdapter(getBaseContext());
        mySQLiteAdapter.openToWrite();
        Date date = new Date();
        final Calendar calendar = Calendar.getInstance();
        final int hours = calendar.get(Calendar.HOUR_OF_DAY);
        final int mins = calendar.get(Calendar.MINUTE);
        appPrefs = new AppPreferences(getApplicationContext());
        
        final int snoozeTime = Integer.parseInt(appPrefs.getSnoozeTime());
        Button snooze = (Button)findViewById(R.id.alarmsnooze);
        final String contactText = "Wake me up";
        snooze.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                HashMap<String,String> info = mySQLiteAdapter.getInfo(hours+ ":" + mins);
                int snooze;
                try {
                snooze = Integer.parseInt(info.get("snooze")) - 1;
                }
                catch (Exception e) {
                    snooze = 2;
                }
                if (snooze <= 0) {
                    ArrayList<String>numbers = new ArrayList<String>();
                    String[] contacts = appPrefs.getEmContacts().split("!!");
                    for (String c : contacts) {
                        numbers.add(c.split("\\|")[1]);
                        //Toast.makeText(arg0.getContext(), numbers.toString(), Toast.LENGTH_LONG).show();
                        
                    }
                    
                    
                    PendingIntent sentPI = PendingIntent.getBroadcast(getBaseContext(), 0,
                            new Intent("SMS_SENT"), 0);
                 
                        PendingIntent deliveredPI = PendingIntent.getBroadcast(getBaseContext(), 0,
                            new Intent("SMS_DELIVERED"), 0);
                    for (String phone : numbers) {
                        SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(phone, null, contactText, sentPI, null); 
                        
                    }
                
            
            
                }
                mySQLiteAdapter.deleteAlarm(hours + ":" + mins);
                int true_month = calendar.get(Calendar.MONTH) + 1;
                String date =  true_month + "-" + calendar.get(Calendar.DATE) + "-" + calendar.get(Calendar.YEAR);
                calendar.add(Calendar.MINUTE, snoozeTime);
                final int snooze_hours = calendar.get(Calendar.HOUR_OF_DAY);
                final int snooze_mins = calendar.get(Calendar.MINUTE);
                mySQLiteAdapter.insertAlarm("", "", snooze_hours + ":" + snooze_mins, Integer.toString(snooze), date);
                Intent intent = new Intent(AlarmDialog.this, OneShotAlarm.class);
                PendingIntent sender = PendingIntent.getBroadcast(AlarmDialog.this,
                        0, intent, 0);
                
                AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
                
                
                Toast.makeText(arg0.getContext(), snooze_hours + ":" + snooze_mins + "\t" + snooze, Toast.LENGTH_SHORT).show();
                r.stop();
                finish();
            }
            
        });
        
        Button dismiss = (Button)findViewById(R.id.alarmdismiss);
        dismiss.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mySQLiteAdapter.deleteAlarm(hours + ":" + mins);
                r.stop();
                finish();
                
            }
            
        });
    }

}

