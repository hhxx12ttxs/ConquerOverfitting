package com.ppl.sinodar.controllers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.ppl.sinodar.activities.BigMenuActivity;
import com.ppl.sinodar.activities.QuickDialActivity;
import com.ppl.sinodar.daos.InstanceDao;
import com.ppl.sinodar.models.InstanceModel;

public class QuickDialController extends Controller {
	
	public static final int QUICK_DIAL = 1;
	public static final int VIEW_BIG_MENU = 2;
	
	private final int MAX_DIST = 10;
	private final String EMERGENCY_HOSPITAL = "118";
	private final String EMERGENCY_FIRE_STATION = "113";
	private final String EMERGENTY_POLICE_STATION = "110";

	private QuickDialActivity view;
	private ArrayList<InstanceModel> list;
	

	public QuickDialController(QuickDialActivity view) {
		this.view = view;
		
		workerThread = new HandlerThread("QuickDialController Worker Thread");
		workerThread.start();
		workerHandler = new Handler(workerThread.getLooper());
	}

	@Override
	public boolean handleMessage(int what, Object data) {
		switch (what) {
			case QUICK_DIAL:
				getNextCalledInstance((int) data);
				return true;
				
			case VIEW_BIG_MENU:
				viewBigMenu((int) data);
				return true;
		}
		return false;
	}

	public void viewBigMenu(int instanceCode) {
		Intent intent = new Intent(view, BigMenuActivity.class);
		intent.putExtra(BigMenuActivity.INSTANCE_CODE, instanceCode);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		view.startActivity(intent);
	}

	public void getNextCalledInstance(int instanceCode) {
		this.list = new ArrayList<InstanceModel>();
		ArrayList<InstanceModel> tempList = new ArrayList<InstanceModel>();

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		LocationManager locMgr = (LocationManager) view.getSystemService(Context.LOCATION_SERVICE);
		String provider = locMgr.getBestProvider(criteria, true);
		
		final LocationListener locListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location arg0) {}

			@Override
			public void onProviderDisabled(String arg0) {}

			@Override
			public void onProviderEnabled(String arg0) {}

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
		};

		Location loc = null;
		if (locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			loc = locMgr.getLastKnownLocation(provider);
			
			locMgr.requestLocationUpdates(provider, 50, 100, locListener);
			int counter = 0;
			// Jika sinyal GPS lemah sangat
			while (loc == null && counter < 500) {
				loc = locMgr.getLastKnownLocation(provider);
				locMgr.requestLocationUpdates(provider, 50, 100, locListener);
				counter++;
			}
			
			
			double user_Latitude = loc.getLatitude();
			double user_Longitude = loc.getLongitude();

			locMgr.requestLocationUpdates(provider, 50, 100, locListener);			
			
			try {
				tempList = InstanceDao.getCalledInstance(instanceCode, view);
				if(tempList == null ) {
					Toast.makeText(view, "Empty local cache", Toast.LENGTH_SHORT).show();
					emergencyCall(instanceCode);
				}
				
				
				// algo sorting
				for(int i = 0; i < tempList.size(); i++) {
					double iLat = (tempList.get(i).getLatitude());
					double iLong = (tempList.get(i).getLongitude());
					double dist = InstanceDao.calculateGCD(Math.toRadians(user_Latitude), Math.toRadians(user_Longitude), iLat, iLong);
					if (dist <= MAX_DIST) list.add(tempList.get(i));
				}
	
				if (list.size() == 0) {
					Toast.makeText(view, "No instance within 10 km", Toast.LENGTH_SHORT).show();
					emergencyCall(instanceCode);
				} else { 
					locMgr.requestLocationUpdates(provider, 50, 100, locListener);
					dialNumber(user_Latitude, user_Longitude, instanceCode);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}

		} else {
			Toast.makeText(view, "Please enable GPS", Toast.LENGTH_LONG).show();
			locMgr.requestLocationUpdates(provider, 50, 100, locListener);
			view.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		}
	}

/* ------------------------------ batas dirapikan baru sampai sini ------------------------------------------- */
	
	public void emergencyCall(int instanceCode) throws Exception {
		TelephonyManager telephonyManager = (TelephonyManager) view.getSystemService(Context.TELEPHONY_SERVICE);
		String emerNumber;
		if (instanceCode == 2) {
			emerNumber = EMERGENCY_FIRE_STATION;
			Toast.makeText(view, "Calling emergency number Fire Station",
					Toast.LENGTH_SHORT).show();
		} else if (instanceCode == 1) {
			emerNumber = EMERGENCY_HOSPITAL;
			Toast.makeText(view, "Calling emergency number Hospital",
					Toast.LENGTH_SHORT).show();
		} else {
			emerNumber = EMERGENTY_POLICE_STATION;
			Toast.makeText(view, "Calling emergency number Police Station",
					Toast.LENGTH_SHORT).show();
		}
		String numberToDial = "tel:" + emerNumber;
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		// callIntent.setPackage("com.android.phone");
		callIntent.setData(Uri.parse(numberToDial));
		view.startActivity(callIntent);
	}

	public String[] getMinD(double latitude, double longitude) {
		String[] info = new String[2];
		InstanceDao instDao = new InstanceDao();
		if (this.list.size() == 0) {
			info[0] = "";
			info[1] = "";
			return info;
			//return "";
		} else {
			InstanceModel dMin = this.list.get(0);
			for (int i = 1; i < this.list.size(); i++) {
				InstanceModel temp = this.list.get(i);

				double dist1 = instDao.calculateGCD(Math.toRadians(latitude), Math.toRadians(longitude),
						dMin.getLatitude(), dMin.getLongitude());

				double dist2 = instDao.calculateGCD(Math.toRadians(latitude), Math.toRadians(longitude),
						temp.getLatitude(), temp.getLongitude());

				if (dist2 < dist1) {
					dMin = this.list.get(i);
					this.list.remove(i);
				}

			}
			
			info[0] = dMin.getMainPhoneNumber()+"";
			info[1] = dMin.getName()+"";
			return info;
			//return dMin.getPhoneNumber();

		}

	}

	// monitor phone call activities
	private class PhoneCallListener extends PhoneStateListener {

		private boolean isPhoneCalling = false;
		String LOG_TAG = "LOGGING 123";

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			if (TelephonyManager.CALL_STATE_RINGING == state) {
				// phone ringing
				Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);

			}

			if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
				// active
				Log.i(LOG_TAG, "OFFHOOK");

				isPhoneCalling = true;

			}

			if (TelephonyManager.CALL_STATE_IDLE == state) {
				// run when class initial and phone call ended,
				// need detect flag from CALL_STATE_OFFHOOK
				Log.i(LOG_TAG, "IDLE");

				if (isPhoneCalling) {

					Log.i(LOG_TAG, "restart app");

					// restart app
					Intent intent = new Intent(view.getBaseContext(),
							com.ppl.sinodar.activities.MainMenuActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					view.startActivity(intent);

					isPhoneCalling = false;
				}

			}
		}
	}

	public void dialNumber(final double latitude, final double longitude,
			final int type) throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			RemoteException {
		
		PhoneCallListener phoneListener = new PhoneCallListener();
		TelephonyManager telephonyManager = (TelephonyManager) view
				.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener,
				PhoneStateListener.LISTEN_CALL_STATE);
		Toast.makeText(view, list.size()+" SIZEA", Toast.LENGTH_LONG).show();
		
		Class clazz = Class.forName(telephonyManager.getClass().getName());
		Method method = clazz.getDeclaredMethod("getITelephony");
		method.setAccessible(true);
		final com.android.internal.telephony.ITelephony telephonyService = (com.android.internal.telephony.ITelephony) method
				.invoke(telephonyManager);
				
		final Intent callIntent = new Intent(Intent.ACTION_CALL);
		final Intent callIntent2 = new Intent(Intent.ACTION_CALL);
		final Intent callIntent3 = new Intent(Intent.ACTION_CALL);
		
		String[] res = this.getMinD(latitude, longitude);
		String numberToDial = "tel:" + res[0];
		callIntent.setData(Uri.parse(numberToDial));		
		callIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Toast toast = Toast.makeText(view, "Calling "+res[1] ,
				Toast.LENGTH_SHORT);
		
		String[] res2 = this.getMinD(latitude, longitude);
		final String numberToDial2 = "tel:" + res2[0];
		callIntent2.setData(Uri.parse(numberToDial2));		
		callIntent2.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		callIntent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		final Toast toast2 = Toast.makeText(view, "Calling "+res2[1] ,
				Toast.LENGTH_SHORT);
		
		String[] res3 = this.getMinD(latitude, longitude);
		final String numberToDial3 = "tel:" + res3[0];
		callIntent3.setData(Uri.parse(numberToDial3));		
		callIntent3.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		callIntent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		final Toast toast3 = Toast.makeText(view, "Calling "+res3[1] ,
				Toast.LENGTH_SHORT);
		
		toast.show();
		view.startActivity(callIntent);
		
		//telephonyService.call(numberToDial);
		boolean checkEmer = false;
		CountDownTimer countDownTimer = new CountDownTimer(5000, 1000) {

			public void onTick(long millisUntilFinished) {
				// do nothing
			}

			public void onFinish() {
			
				try {				
					if (telephonyService.isOffhook()==true) {
						// finish();
						try {
							telephonyService.endCall();
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						if (numberToDial2.equals("tel:")) {
							try {
								try {
									emergencyCall(type);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							toast2.show();
							view.startActivity(callIntent2);
							CountDownTimer countDownTimer = new CountDownTimer(
									5000, 1000) {

								public void onTick(long millisUntilFinished) {
									//
								}

								public void onFinish() {
									try {
										if (telephonyService.isOffhook()) {
											// finish();
											try {
												telephonyService.endCall();
											} catch (RemoteException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
											if (numberToDial3.equals("tel:")) {
												try {
													try {
														emergencyCall(type);
													} catch (Exception e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
												} catch (
														IllegalArgumentException e) {
													// TODO Auto-generated catch
													// block
													e.printStackTrace();
												}
											} else {
												toast3.show();
												view.startActivity(callIntent3);
												CountDownTimer countDownTimer = new CountDownTimer(
														5000, 1000) {

													public void onTick(
															long millisUntilFinished) {
														//
													}

													public void onFinish() {
														try {
															if (telephonyService
																	.isOffhook()) {
																// finish();
																try {
																	telephonyService
																			.endCall();
																} catch (RemoteException e) {
																	e.printStackTrace();
																}
																try {
																	try {
																		emergencyCall(type);
																	} catch (Exception e) {
																		// TODO Auto-generated catch block
																		e.printStackTrace();
																	}
																} catch (
																		IllegalArgumentException e) {
																	e.printStackTrace();
																}
															} else if (telephonyService
																	.isRinging()) {
															} else if (telephonyService
																	.isIdle()) {
															}

														} catch (RemoteException e) {
															// TODO
															// Auto-generated
															// catch block
															e.printStackTrace();
														}

													}

												}.start();
											}
										} else if (telephonyService.isRinging()) {
										} else if (telephonyService.isIdle()) {
										}

									} catch (RemoteException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								}

							}.start();
						
						}

					} else if (telephonyService.isRinging()) {
					} else if (telephonyService.isIdle()) {
					}
					

				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

			}

		}.start();

	}
}

