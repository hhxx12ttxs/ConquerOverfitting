/*
 * Copyright 2011 Mobispectra Technologies LLC.
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
 */
package com.mobispectra.android.apps.dolconnect.maps;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mobispectra.android.apps.dolconnect.AppConstants;
import com.mobispectra.android.apps.dolconnect.BaseActivity;
import com.mobispectra.android.apps.dolconnect.R;
import com.mobispectra.android.apps.dolconnect.data.OSHAData.OSHAItem;
import static com.mobispectra.android.apps.dolconnect.OSHAFoodActivity.mAddressList;

public class OSHAMapActivityJSInterface extends BaseActivity {
	private static final String MAP_URL = "file:///android_asset/osha-map.html";
	private WebView webView;
	
	@Override
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jsinterface);
		
/*
		// Extract the Extra Data passed to this activity
		Intent intent = getIntent();
		Bundle indexBundle = intent.getExtras();
		if (indexBundle != null) {
			mAddressList = indexBundle.getStringArrayList(AppConstants.ADDRESS);
		}
*/
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
		setupWebView();
	}



	/** Sets up the WebView object and loads the URL of the page **/
	private void setupWebView() {

		webView = (WebView) findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient());
		webView.loadUrl(MAP_URL);

		/** Allows JavaScript calls to access application resources **/
		webView.addJavascriptInterface(new OSHAJavaScriptInterface(), "OSHA");

	}

	/**
	 * Sets up the interface for getting access to Latitude and Longitude data
	 * from device
	 **/
	private class OSHAJavaScriptInterface {
		private int mIndex = 0;
		private OSHAItem mOSHAItem = null;
		private String mAddress = null;
		
		public void getOSHAItem() {
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface : + getOSHAItem");
			if (mIndex == 10) {
				mIndex = 0;
			}
			if (mAddressList != null) {
				mAddress = mAddressList.get(mIndex);
				Log.d(AppConstants.TAG,"OSHAJavaScriptInterface : + getOSHAItem : Address = "+ mAddress);
				
				// Get the Object from HashMap using address as key
				mOSHAItem = OSHAaddressMap.get(mAddress);
			}
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface : - getOSHAItem");
		}
		
		public double getLatitude() {
			double lattitude =  40.016521;
			if (mOSHAItem != null) {
				if (mOSHAItem.getPoint() != null) {
					lattitude = mOSHAItem.getPoint().getLatitudeE6() / 1E6;
				}
			}
			Log.d(AppConstants.TAG, "OSHAJavaScriptInterface: latitude="+ lattitude);
			return lattitude;
		}

		public double getLongitude() {
			double longitude = -105.282866;
			if (mOSHAItem != null) {
				if (mOSHAItem.getPoint() != null) {
					longitude = mOSHAItem.getPoint().getLongitudeE6() / 1E6;
				}
			}
			Log.d(AppConstants.TAG, "OSHAJavaScriptInterface: longitude="+ longitude);
			return longitude;
		}
		
		public void moveToNextGeoPoint() {
			mIndex++;
		}
		
		public boolean isGeoListEmpty() {
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: index=" + mIndex);
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: size=" + mAddressList.size());
			return mAddressList.size() == mIndex ? false : true;
		}
		
		public String getContent(){
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: + getContent");
			String penalty = null;
			String name = null;
			String result = null;
			String naicsCode = null;
			String date = null;
			String indicator = null;
			String seriousViolations = null;
			String totalViolations = null;
			if (mOSHAItem != null) {
				penalty = mOSHAItem.getPenalty(getApplicationContext());
				name = mOSHAItem.getName();
				naicsCode = mOSHAItem.getNaics_code(getApplicationContext());
				date = mOSHAItem.getOpen_date(getApplicationContext());
				indicator = mOSHAItem.getOsha_violation_indicator(getApplicationContext());
				seriousViolations = mOSHAItem.getSerious_violations(getApplicationContext());
				totalViolations = mOSHAItem.getTotal_violations(getApplicationContext());
			}	
			result = " Name : " + name + "</br> Address : " + mAddress
			+ "</br> Penalty :  " + penalty + "</br>NAICS Code : "
			+ naicsCode + "</br>Inspection Starting Date : " + date
			+ "</br>OSHA Violation Indicator :" + indicator
			+ "</br>Serious Violations : " + seriousViolations
			+ "</br>Total Violations : " + totalViolations;
			
			return result;
		}
		
		public String getPentaltyFromIfc(){
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: + getPentaltyFromIfc");
			String penalty = null;
			if (mOSHAItem != null) {
				penalty = mOSHAItem.getPenalty(getApplicationContext());
			}
			
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: -getPentaltyFromIfc");	
			return penalty;
		}
	}

}
