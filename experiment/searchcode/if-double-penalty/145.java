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

import static com.mobispectra.android.apps.dolconnect.BaseActivity.OSHAaddressMap;
import static com.mobispectra.android.apps.dolconnect.BaseActivity.WHDAddressMap;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mobispectra.android.apps.dolconnect.AppConstants;
import com.mobispectra.android.apps.dolconnect.R;
import com.mobispectra.android.apps.dolconnect.data.OSHAData.OSHAItem;
import com.mobispectra.android.apps.dolconnect.data.WHDData.WHDItem;

public class MapFragment extends Fragment {
	private static final String OSHA_URL = "file:///android_asset/osha-map.html";
	private static final String WHD_URL = "file:///android_asset/whd-map.html";
	private WebView webView;
	private ArrayList<String> mAddressList;
	private ViewGroup mRootView;
	private int mIndex = 0;
	private String mAddress = null;
	private OSHAItem mOSHAItem = null;
	private WHDItem mWHDItem = null;
	private String mAgency = null;
	
	
	public MapFragment(ArrayList<String> addressList,String agency) {
		mAddressList = addressList;
		mAgency = agency;
		Log.d(AppConstants.TAG,"MapFragment: MapFragment : AddressList = "+mAddressList.toString());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(AppConstants.TAG," MapFragment : + onCreate()");
		super.onCreate(savedInstanceState);
		Log.d(AppConstants.TAG," MapFragment : - onCreate()");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(R.layout.activity_jsinterface, null);
		
		if (!getRetainInstance()) {
			setupWebView();
			setRetainInstance(true);
		}
		return mRootView;
	}
	
	/** Sets up the WebView object and loads the URL of the page **/
	private void setupWebView() {

		webView = (WebView) mRootView.findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient());
		
		if (mAgency.equals(getString(R.string.agency_osha))) {
			webView.loadUrl(OSHA_URL);
			/** Allows JavaScript calls to access application resources **/
			webView.addJavascriptInterface(new OSHAJavaScriptInterface(),"OSHA");
		} else {
			webView.loadUrl(WHD_URL);
			webView.addJavascriptInterface(new WHDJavaScriptInterface(), "WHD");
		}
	}
	
	/**
	 * Sets up the interface for getting access to Latitude and Longitude data
	 * from device
	 **/
	private class OSHAJavaScriptInterface {
		private String mShareResult = null;
		
		public void getOSHAItem() {
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface : + getOSHAItem");
			Log.d(AppConstants.TAG,"mIndex= " + mIndex);
			mAddress = mAddressList.get(mIndex);
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface : + getOSHAItem : Address = " + mAddress);
			// Get the Object from HashMap using address as key
			mOSHAItem = OSHAaddressMap.get(mAddress);
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface : - getOSHAItem");
		}
		
		public double getLatitude() {
			double lattitude = 40.016521;
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
				penalty = mOSHAItem.getPenalty(getActivity());
				name = mOSHAItem.getName();
				naicsCode = mOSHAItem.getNaics_code(getActivity());
				date = mOSHAItem.getOpen_date(getActivity());
				indicator = mOSHAItem.getOsha_violation_indicator(getActivity());
				seriousViolations = mOSHAItem.getSerious_violations(getActivity());
				totalViolations = mOSHAItem.getTotal_violations(getActivity());
			}	
			result = " Name : " + name + "</br> Address : " + mAddress
					+ "</br> Penalty :  " + penalty + "</br>NAICS Code : "
					+ naicsCode + "</br>Inspection Starting Date : " + date
					+ "</br>OSHA Violation Indicator :" + indicator
					+ "</br>Serious Violations : " + seriousViolations
					+ "</br>Total Violations : " + totalViolations;
			mShareResult = " Name : " + name + ",Address : " + mAddress
			+ ", Penalty :  " + penalty + ",NAICS Code : "
			+ naicsCode + ",Inspection Starting Date : " + date
			+ ",OSHA Violation Indicator :" + indicator
			+ ",Serious Violations : " + seriousViolations
			+ ",Total Violations : " + totalViolations;
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: - getContent"+ mShareResult );
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: - getContent");
			return result;
		}
		
		public void shareViolation(){
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: + shareViolation");
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: + shareViolation : shareData = " + mShareResult);
			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_SUBJECT,getResources().getString(R.string.share_osha_subject));
			shareIntent.putExtra(Intent.EXTRA_TEXT,String.format(getString(R.string.share_osha_message)+  mShareResult));
			shareIntent.setType("text/plain");
			startActivity(Intent.createChooser(shareIntent,
					getString(R.string.share_osha_violation)));
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: - shareViolation");
		}
		
		public String getPentaltyFromIfc(){
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: + getPentaltyFromIfc");
			String penalty = "";
			if (mOSHAItem != null) {
				penalty = mOSHAItem.getPenalty(getActivity());
			}
			
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: -getPentaltyFromIfc");	
			return penalty;
		}
	}
	
	/**
	 * Sets up the interface for getting access to Latitude and Longitude data
	 * from device
	 **/
	private class WHDJavaScriptInterface {
		private String mShareResult = null;
		
		public void getWHDItem() {
			Log.d(AppConstants.TAG,"WHDJavaScriptInterface : + getWHDItem");
			mAddress = mAddressList.get(mIndex);
			Log.d(AppConstants.TAG,"WHDJavaScriptInterface : + getWHDItem : Address = " + mAddress);
			// Get the Object from HashMap using address as key
			mWHDItem = WHDAddressMap.get(mAddress);
			Log.d(AppConstants.TAG,"WHDJavaScriptInterface : - getWHDItem");
		}
		
		public double getLatitude() {
			double lattitude = 40.016521;
			if (mWHDItem != null) {
				if (mWHDItem.getPoint() != null) {
					lattitude = mWHDItem.getPoint().getLatitudeE6() / 1E6;
				}
			}
			Log.d(AppConstants.TAG, "WHDJavaScriptInterface: point="+ lattitude);
			return lattitude;
		}

		public double getLongitude() {
			double longitude = -105.282866;
			if (mWHDItem != null) {
				if (mWHDItem.getPoint() != null) {
					longitude = mWHDItem.getPoint().getLongitudeE6() / 1E6;
				}
			}
			Log.d(AppConstants.TAG, "WHDJavaScriptInterface: point="+ longitude);
			return longitude;
		}
		
		public void moveToNextGeoPoint() {
			mIndex++;
		}
		
		public boolean isGeoListEmpty() {
			Log.d(AppConstants.TAG,"WHDJavaScriptInterface(): index=" + mIndex);
			Log.d(AppConstants.TAG,"WHDJavaScriptInterface(): size=" + mAddressList.size());
			return mAddressList.size() == mIndex ? false : true;
		}
		
		public String getContent(){
			Log.d(AppConstants.TAG,"WHDJavaScriptInterface(): + getContent");
			String name = null;
			String result = null;	
			String NAICDescription = null;
			String startDate = null;
			String endDate = null;
			String FLSAviolationCount = null;
			String childLabourFindingsIndicator = null;
			String childLaborViolation = null;
			String repeatViolator = null;
			String flsa15a3BwAtpAmt = null;
			String flsaOtBwAtpAmt = null;
			String flsaEeBwAtpCnt = null;
			String flsaBwAtpAmt = null;
			String flsaMwBwAtpCnt = null;
			String flsaCmpAssdAmt = null;
			if (mWHDItem != null) {
				name = mWHDItem.getTrade_nm();
				NAICDescription = mWHDItem.getNaics_code_description();
				startDate = mWHDItem.getFindings_start_date();
				endDate = mWHDItem.getFindings_end_date();
				FLSAviolationCount = mWHDItem.getFlsa_violtn_cnt();
				childLabourFindingsIndicator = mWHDItem.getFlsa_cl_cmp_assd_amt();
				childLaborViolation = mWHDItem.getFlsa_cl_minor_cnt();
				repeatViolator = mWHDItem.getFlsa_repeat_violator();
				flsa15a3BwAtpAmt = mWHDItem.getFlsa_15a3_bw_atp_amt();
				flsaOtBwAtpAmt = mWHDItem.getFlsa_ot_bw_atp_am();
				flsaEeBwAtpCnt = mWHDItem.getFlsa_ee_atp_cnt();
				flsaBwAtpAmt = mWHDItem.getFlsa_bw_atp_amt();
				flsaMwBwAtpCnt = mWHDItem.getFlsa_mw_bw_atp_amt();
				flsaCmpAssdAmt = mWHDItem.getFlsa_cmp_assd_amt();
			}	
			result = " Name : " + name + "</br> Address : " + mAddress
					+ "</br>NAICDesription : " + NAICDescription
					+ "</br>Finding Date Start : " + startDate
					+ "</br>Finding Date End : " + endDate
					+ "</br>FLSA Violation Count : " + FLSAviolationCount
					+ "</br>Child Labour Finding Indicator : "+ childLabourFindingsIndicator
					+ "</br>Minors found employed: " + childLaborViolation
					+ "</br>FLSA Repeat violator: " + repeatViolator
					+ "</br>FLSA 15(a)(3) BW ATP: " + flsa15a3BwAtpAmt
					+ "</br>BW Agreed to under FLSA Overtime: " +flsaOtBwAtpAmt
					+ "</br>EE\'s Agreed to under FLSA: " +flsaEeBwAtpCnt
					+ "</br>BW Agreed to under FLSA: "    +flsaBwAtpAmt
					+ "</br>BW Agreed to under FLSA  Minimum Wages: " + flsaMwBwAtpCnt
					+ "</br>CMP\'s assessed under FLSA: " +	flsaCmpAssdAmt ;
			;
			mShareResult = " Name : " + name + ", Address : " + mAddress
			+ ",NAICDesription : " + NAICDescription
			+ ",Finding Date Start : " + startDate
			+ ",Finding Date End : " + endDate
			+ ",FLSA Violation Count : " + FLSAviolationCount
			+ ",Child Labour Finding Indicator : "+ childLabourFindingsIndicator
			+ ",Minors found employed: " + childLaborViolation
			+ ",FLSA Repeat violator: " + repeatViolator
			+ ",FLSA 15(a)(3) BW ATP: " + flsa15a3BwAtpAmt
			+ ",BW Agreed to under FLSA Overtime: " +flsaOtBwAtpAmt
			+ ",EE\'s Agreed to under FLSA: " +flsaEeBwAtpCnt
			+ ",BW Agreed to under FLSA: "    +flsaBwAtpAmt
			+ ",BW Agreed to under FLSA  Minimum Wages: " + flsaMwBwAtpCnt
			+ ",CMP\'s assessed under FLSA: " +	flsaCmpAssdAmt ;
			
			Log.d(AppConstants.TAG,"WHDJavaScriptInterface(): - getContent");	
			return result;
		}
		
		public void shareViolation(){
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: + shareViolation");
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: + shareViolation : shareData = " + mShareResult);
			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_SUBJECT,getResources().getString(R.string.share_whd_subject));
			shareIntent.putExtra(Intent.EXTRA_TEXT,String.format(getString(R.string.share_whd_message)+  mShareResult));
			shareIntent.setType("text/plain");
			startActivity(Intent.createChooser(shareIntent,
					getString(R.string.share_whd_violation)));
			Log.d(AppConstants.TAG,"OSHAJavaScriptInterface: - shareViolation");
		}
	}
	
	public void upDateAddressDataset(ArrayList<String> addressList) {
		Log.d(AppConstants.TAG,"MapFragment: upDateAddressDataset : AddressList = " + mAddressList.toString());
		mAddressList = addressList;
		if(webView!=null) { 
			mIndex = 0;
			webView.loadUrl("javascript:clearPopUpMarker()");
			webView.loadUrl("javascript:clearOverlays()");
			webView.loadUrl("javascript:setMarkers()");
		
		}
	}
	
	public void popUpSelectedMarker(String address) {
		Log.d(AppConstants.TAG,"MapFragment : popUpSelectedMarker : Address = " + address);
		mAddress = address;
		if (mAgency.equals(getString(R.string.agency_osha))) {
			mOSHAItem = OSHAaddressMap.get(mAddress);
		}else{
			mWHDItem = WHDAddressMap.get(mAddress);
		}
		if (webView != null) {
			webView.loadUrl("javascript:clearPopUpMarker()");
			webView.loadUrl("javascript:placeListItemClickMarker()");

		}
	}
}

