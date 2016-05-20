/* Copyright (C) 2012-2013 The Augumenta Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package fi.cie.chiru.servicefusionar.sensors;

/**
 * This is just an example for SensorFussion Project
 *
 * @author Peter Antoniac <peter@augumenta.com>
 * @date   2012-2013
 * @warning This is just an examle
 * @copyright Apache License, Version 2.0
 */

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * This is a utility class example for OrientationListener implementation.
 *
 * It just shows how to use the sensor output provided by the Orientetor (as activity).
 * The idea is to use Android's own Fussion Sensors that are optimized for in the lower
 * level. Tested from SDK leve 14 up.
 */

public class SensorActivity extends Activity implements OrientationListener
{

	TextView textStatus;
	TextView textResult;
	Orientator orientator;
	Handler myHandler;
	SensorResults sensorResults = new SensorResults();

	/** the displaying of sensors data is based on the order when they emit something */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensor);

		textStatus = (TextView) findViewById(R.id.textStatus);
		textResult = (TextView) findViewById(R.id.textResult);
		//Make text fields as read-only
		textStatus.setKeyListener(null);
		textResult.setKeyListener(null);

		orientator = new Orientator(this, this);
		myHandler = new Handler();

		newStatus(0, "Ready. Click something.");
		//newResult("No results yet...");

		((Button) findViewById(R.id.buttonStart)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newStatus(0, "Started monitoring.");
				orientator.Start();
			}
		});
		((Button) findViewById(R.id.ButtonStop)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newStatus(0, "Stopped monitoring.");
				orientator.Stop();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// We will comment out the menu for now
		//getMenuInflater().inflate(R.menu.activity_sensor, menu);
		return true;
	}

	/**
	 * Fix the screen Orientation to PORTRAIT only
	 *
	 * Android has a nasty way of changing screen orientation:
	 * it removes your activity and creates a new one. This is not desirable, as
	 * all objects owned by Activity need to be either recreated or stored
	 * somewhere globally. To simplify our design (as it is not so important from
	 * UI perspective), we disable the orientation.
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	/** change the text to the new updated one */
	@Override
	public void newStatus(int status, final String info)
	{
		myHandler.post(new Runnable() {
			@Override
			public void run() {
				textStatus.setText(info);
			}
		});
	}

	/** storage for sensors: Name:Reading */
	class SensorResult
	{
		public String sensorName;
		public String sensorReading;
		public SensorResult(String n, String r)
		{
			sensorName = n;
			sensorReading = r;
		}
	}

	/** interpret the data from sensor and translate it to readable format */
	class SensorResults
	{
		List<SensorResult> results = new ArrayList<SensorResult>();

		public void NewResult(String n, float[] r, String f)
		{
			String data = "";
			if(r == null || r.length == 0) {
				data = "null";
			} else {
				for(Float v : r) {
					data += (data.length() > 0 ? ", " : "");
					data += String.format(f, v);
				}
			}
			for(SensorResult cur : results) {
				if(cur.sensorName == n) {
					cur.sensorReading = data;
					post();
					return;
				}
			}
			results.add(new SensorResult(n,data));
		}
		public void NewResult(String n, float[] r)
		{
			NewResult(n,r,"%.2f");
		}

		private void post()
		{
			String p = "";
			for(SensorResult cur : results) {
				p += (p.length() > 0 ? "\n" : "");
				p += cur.sensorName + ":\n" + cur.sensorReading;
			}
			textResult.setText(p);
		}
	}

	/** add new sensor result to the list */
	@Override
	public void newResult(final String sensor, final float[] result)
	{
		newResult(sensor,result,"%.2f");
	}
	@Override
	public void newResult(final String sensor, final float[] result, final String format)
	{
		myHandler.post(new Runnable() {
			@Override
			public void run() {
				sensorResults.NewResult(sensor, result, format);
			}
		});
	}
	/** add implementation for the newLocation. This is how you get the location as Latitude, Longitude */
	@Override
	public void newLocation(double latitude, double longitude)
	{
		/** display the location */
		newResult("Location", new float[] {(float) latitude, (float) longitude},"%.6f");
	}
	/** add implementation for the newOrientation. This is how you get the orientation. As angle */
	@Override
	public void newOrientation(double angle)
	{
		newResult("Angle", new float[] {(float)angle});
	}
	/** add implementation for the isTilt. This is how we know if the device should stop showing the image when tilted */
	public void isTilt(Boolean bool)
	{
		if(bool) {
			newStatus(0, "Stopped due to tilting...");
		} else {
			newStatus(0, "Started as no tilting...");
		}
	}
	/** add implementation for the isCalibrated. This is how we know if the device sensors are reliable */
	public void isCalibrated(final String sensor, Boolean bool)
	{
		if(!bool) {
			newStatus(0, "Sensor "+sensor+" need 8 figure calibration..."); // You need to draw an 8 in the air :)
		}
	}
}

