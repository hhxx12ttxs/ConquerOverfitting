package org.r3pek.droiduptime;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class Main extends Activity {
	private static final int MENU_ABOUT = 0;
	private static final int MENU_SETUP = 1;
	private static final int DIALOG_ABOUT = 0;
	
	private Button btnStartStopService;
	private Button btnUpdate;
	private TextView tvStatus;
	private TextView tvUptime;
	private TextView tvLastUpdate;
	private Thread uptimeThread;
	private ConfigValues cv;
	
	private volatile boolean stopThread;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	
    	/*AdManager.setTestDevices( new String[] {                 
    		     AdManager.TEST_EMULATOR,             // Android emulator
    		     "7A352EFBD63EAC6DF4C5F8DFBC661C00",  // My Nexus1
    		     } );*/ 
   	
    	cv = new ConfigValues(getApplicationContext());
    	
    	btnStartStopService = (Button)findViewById(R.id.btnStartStopService);
    	btnUpdate = (Button)findViewById(R.id.btnUpdate);
    	tvStatus = (TextView)findViewById(R.id.tvStatus);
    	tvUptime = (TextView)findViewById(R.id.tvUptime);
    	tvLastUpdate = (TextView)findViewById(R.id.tvLastUpdate);
    	
    	btnStartStopService.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent service = new Intent(getApplicationContext(), DroidUptimeService.class);
				if (isServiceRunning()) {
					stopService(service);
				} else {
					startService(service);
				}
				updateStatus();
			}
		});
    	
    	btnUpdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent("org.r3pek.droiduptime.FORCE_UPDATE");
                sendBroadcast(i);
            }
        });
	}
	
	private void startThread() {
		stopThread = false;
    	uptimeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!stopThread) {
					long secondsRunning = SystemClock.elapsedRealtime() / 1000;
					final int months = (int)(secondsRunning / 2592000);
					final int days = (int)((secondsRunning - (months * 2592000)) / 86400);
					final int hours = (int)((secondsRunning - (months * 2592000) - (days * 86400)) / 3600);
					final int minutes = (int)((secondsRunning - (months * 2592000) - (days * 86400) - (hours * 3600)) / 60);
					final int seconds = (int)(secondsRunning % 60);
					runOnUiThread(new Runnable() {
						public void run() {
							tvUptime.setText(String.valueOf(months) + " " + getString(R.string.months) + ", " +
									String.valueOf(days) + " " + getString(R.string.days) + ", " +
									String.valueOf(hours) + " " + getString(R.string.hours) + ", " +
									String.valueOf(minutes) + " " + getString(R.string.minutes) + " " +
									String.valueOf(seconds) + " " + getString(R.string.seconds));
							if (cv.getUsername().equals("") || cv.getPassword().equals(""))
								tvLastUpdate.setText(R.string.no_user_pass);
							else 
								tvLastUpdate.setText(cv.getLastUpdate());
							updateStatus();
						}
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) { return; }
				}
			}
		});
    	uptimeThread.start();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		stopThread = true;
		uptimeThread = null;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (uptimeThread == null)
			startThread();
		updateStatus();
	}
	
	private boolean isServiceRunning() {
		ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);

		if (serviceList.size() <= 0) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			RunningServiceInfo serviceInfo = serviceList.get(i);
			ComponentName serviceName = serviceInfo.service;
			if(serviceName.getClassName().equals(DroidUptimeService.class.getName())) {
				return true;
			}
		}

		return false;
	}
	
	private void updateStatus() {
		if (isServiceRunning()) {
			tvStatus.setText(R.string.status_running);
			btnStartStopService.setText(R.string.operation_stop);
		} else {
			tvStatus.setText(R.string.status_stopped);
			btnStartStopService.setText(R.string.operation_start);
		}
	}
	
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ABOUT, 0, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);;
        menu.add(0, MENU_SETUP, 0, R.string.menu_setup).setIcon(android.R.drawable.ic_menu_preferences);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ABOUT:
        	showDialog(DIALOG_ABOUT);
        	return true;
        case MENU_SETUP:
        	Intent i = new Intent(getApplicationContext(), MyPreferences.class);
        	startActivity(i);
        	return true;
        }
        return false;
    }

    public Dialog onCreateDialog(int id) {
    	Dialog d;
    	switch (id) {
    		case DIALOG_ABOUT: {
    			LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
    			View layout = inflater.inflate(R.layout.about, (ViewGroup)findViewById(R.id.aboutRoot));
    			ImageView image = (ImageView)layout.findViewById(R.id.ImageView01);
    			image.setImageResource(R.drawable.icon);
    			Builder builder = new AlertDialog.Builder(this);
    			builder.setView(layout);
    			d = builder.create();
    			break;
    		}
    		default: d = null;
    	}
    	return d;
    }
}

