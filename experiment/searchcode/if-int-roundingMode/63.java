package com.chrisk.gpsnotifier;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

public class MainActivity extends Activity {

    private static final int CONTACT_PICKER_RESULT = 1001;
    private static final String DEBUG_TAG = "GPSNotifier";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textViewCoordinates = (TextView) findViewById(R.id.tvCoordinates);
        textViewCoordinates.setText(convertLocationToString(getLocation()));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onRefreshClicked(View view) {
        TextView textViewCoordinates = (TextView) findViewById(R.id.tvCoordinates);
        textViewCoordinates.setText(convertLocationToString(getLocation()));
    }

    public void doLaunchContactPicker(View view) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICKER_RESULT:
                    Cursor cursor = null;
                    String phone = "";
                    try {
                        Uri result = data.getData();
                        Log.v(DEBUG_TAG, "Got a contact result: " + result.toString());

                        //Get the contact ID from the Uri
                        String id = result.getLastPathSegment();

                        //Query for phone
                        cursor = getContentResolver().query(Phone.CONTENT_URI, null,
                                Phone.CONTACT_ID + "=?", new String[]{id}, null);
                        int phoneIdx = cursor.getColumnIndex(Phone.DATA);

                        //Let's get the first phone number
                        if (cursor.moveToFirst()) {
                            phone = cursor.getString(phoneIdx);
                            Log.v(DEBUG_TAG, "Got phone: " + phone);
                            Location location = getLocation();
                            Log.v(DEBUG_TAG, location.toString());
                            sendSMS(phone, convertLocationToString(location));
                        } else {
                            Log.w(DEBUG_TAG, "No results");
                        }
                    } catch (Exception e) {
                        Log.e(DEBUG_TAG, "Failed to get phone data");
                        e.printStackTrace();
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (phone.length() == 0) {
                            Toast.makeText(this, "No phone number found for contact.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
            }
        } else {
            Log.w(DEBUG_TAG, "Warning: Activity result not OK");
        }
    }

    private Location getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        locationManager.requestLocationUpdates(bestProvider, 0, 0, locationListener);
        location = locationManager.getLastKnownLocation(bestProvider);
        locationManager.removeUpdates(locationListener);
        return location;
    }

    private void sendSMS(String number, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, message, null, null);
    }

    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }

    public String convertLocationToString(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        double altitude = round(location.getAltitude(), 2, BigDecimal.ROUND_UP);

        String locationString = latitude + ", " + longitude + ", " + altitude + " meters above " +
                "sea-level";
        return locationString;
    }
}

