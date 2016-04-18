package com.mycompany.bullbuy;

import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.Date;

public class USFLocations extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener, GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemSelectedListener {

    private GoogleMap mMap;
    private Marker mMarker;
    private GoogleApiClient mGoogleApiClient;
    private LatLng destLocation;
    private Marker destMarker = null;
    private Polyline route = null;

    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;
    protected String mLastUpdateTime;

    TextView mLastUpdateTimeTextView;
    TextView hours;
    TextView hoursMonday;
    TextView hoursTuesday;
    TextView hoursWednesday;
    TextView hoursThursday;
    TextView hoursFriday;
    TextView hoursSaturday;
    TextView hoursSunday;
    ImageView location;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usflocations);

        /* Set up map, populate spinner list with items from string array (locations)
         * Build google api client
         */

        setUpMapIfNeeded();

        mLastUpdateTimeTextView = (TextView)findViewById(R.id.lastUpdateTime);
        spinner = (Spinner) findViewById(R.id.spinner_MapsActivity);
        ArrayAdapter<CharSequence> myAdapter = ArrayAdapter.createFromResource(this, R.array.locations, android.R.layout.simple_spinner_item);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(myAdapter);
        spinner.setOnItemSelectedListener(this);
        location = (ImageView) findViewById(R.id.location);
        hours = (TextView)findViewById(R.id.hours);
        hoursMonday = (TextView)findViewById(R.id.hoursMon);
        hoursTuesday = (TextView)findViewById(R.id.hoursTues);
        hoursWednesday = (TextView)findViewById(R.id.hoursWed);
        hoursThursday = (TextView)findViewById(R.id.hoursThurs);
        hoursFriday = (TextView)findViewById(R.id.hoursFri);
        hoursSaturday = (TextView)findViewById(R.id.hoursSat);
        hoursSunday = (TextView)findViewById(R.id.hoursSun);
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        // build google api client and connect
        mGoogleApiClient = new
                GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // map setup find fragment in xml and get map
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // when connected create/start to request location updates move camera to the last know location
        createLocationRequest();
        startLocationUpdates();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new
                LatLng(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLatitude(),
                LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient).getLongitude()), 15));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected void createLocationRequest(){
        // create the location request and set up intervals, priority then start actual updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        startLocationUpdates();
    }

    protected void startLocationUpdates(){
        // request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    public void onLocationChanged(Location location){
        /* when location changes make corresponding updates to map
         * move camera, remove route
         * add new route if a selection was made, update the time and the map
         */
        mCurrentLocation = location;
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude());
        if(mMarker == null) {
            mMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 16));
            mMarker.setTitle("Your Current Location");
            mMarker.showInfoWindow();
            hours.setVisibility(View.GONE);
            hoursMonday.setVisibility(View.GONE);
            hoursTuesday.setVisibility(View.GONE);
            hoursWednesday.setVisibility(View.GONE);
            hoursThursday.setVisibility(View.GONE);
            hoursFriday.setVisibility(View.GONE);
            hoursSaturday.setVisibility(View.GONE);
            hoursSunday.setVisibility(View.GONE);
        } else {
            mMarker.setPosition(latLng);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new
                LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), mMap.getCameraPosition().zoom));

        if(route != null && destMarker != null){
            LatLngBounds bounds = LatLngBounds.builder().include(new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()))
                    .include(destLocation).build();
            //Google Developers LatLngBuilder
            route.remove();
            route = mMap.addPolyline(new PolylineOptions().add(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                    new LatLng(destLocation.latitude, destLocation.longitude)).width(8).color(Color.rgb(4, 94, 5)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int)mMap.getCameraPosition().zoom));

        }

        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateMap();
    }

    private void updateMap(){
        if(mCurrentLocation != null){
            // show time of last update
            mLastUpdateTimeTextView.setText(mLastUpdateTime);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        /* remove current destination marker and route
         * determine what item was selected from spinner
         * set the lat and lon, corresponding picture, and text
         * add the marker, add the route
        */
        if(destMarker != null) {
            destMarker.remove();
            destMarker = null;
        }
        if (route != null) {
            route.remove();
            route = null;
        }

        switch(parent.getSelectedItem().toString()){
            case "Current Location":
                return;
            case "Tampa Campus Library":
                destLocation = new LatLng(28.0595525,-82.412246415);
                hours.setVisibility(View.VISIBLE);
                hoursMonday.setVisibility(View.VISIBLE);
                hoursTuesday.setVisibility(View.VISIBLE);
                hoursWednesday.setVisibility(View.VISIBLE);
                hoursThursday.setVisibility(View.VISIBLE);
                hoursFriday.setVisibility(View.VISIBLE);
                hoursSaturday.setVisibility(View.VISIBLE);
                hoursSunday.setVisibility(View.VISIBLE);
                hours.setText("HOURS");
                hoursMonday.setText("Monday: Open 24 hours");
                hoursTuesday.setText("Tuesday: Open 24 hours");
                hoursWednesday.setText("Wednesday: Open 24 hours");
                hoursThursday.setText("Thursday: Open 24 hours");
                hoursFriday.setText("Friday: 12:00am – 6:00pm");
                hoursSaturday.setText("Saturday: 10:00am - 6:00pm");
                hoursSunday.setText("Sunday: Open 24 hours");
                location.setImageDrawable(getResources().getDrawable(R.drawable.library));
                break;
            case "Marshall Student Center":
                destLocation = new LatLng(28.0639342,-82.4134536);
                hours.setVisibility(View.VISIBLE);
                hoursMonday.setVisibility(View.VISIBLE);
                hoursTuesday.setVisibility(View.VISIBLE);
                hoursWednesday.setVisibility(View.VISIBLE);
                hoursThursday.setVisibility(View.VISIBLE);
                hoursFriday.setVisibility(View.VISIBLE);
                hoursSaturday.setVisibility(View.VISIBLE);
                hoursSunday.setVisibility(View.VISIBLE);
                hours.setText("HOURS");
                hoursMonday.setText("Monday: 7:00am - 12:00am");
                hoursTuesday.setText("Tuesday: 7:00am - 12:00am ");
                hoursWednesday.setText("Wednesday: 7:00am - 12:00am");
                hoursThursday.setText("Thursday: 7:00am - 12:00am");
                hoursFriday.setText("Friday: 7:00am - 1:00am");
                hoursSaturday.setText("Saturday: 8:00am - 1:00am");
                hoursSunday.setText("Sunday: 10:00am - 12:00am");
                location.setImageDrawable(getResources().getDrawable(R.drawable.msc));
                break;
            case "Interdisciplinary Sciences":
                destLocation = new LatLng(28.061931, -82.414225);
                hours.setVisibility(View.VISIBLE);
                hoursMonday.setVisibility(View.VISIBLE);
                hoursTuesday.setVisibility(View.VISIBLE);
                hoursWednesday.setVisibility(View.VISIBLE);
                hoursThursday.setVisibility(View.VISIBLE);
                hoursFriday.setVisibility(View.VISIBLE);
                hoursSaturday.setVisibility(View.VISIBLE);
                hoursSunday.setVisibility(View.VISIBLE);
                hours.setText("HOURS");
                hoursMonday.setText("Monday: Open 24 hours");
                hoursTuesday.setText("Tuesday: Open 24 hours ");
                hoursWednesday.setText("Wednesday: Open 24 hours");
                hoursThursday.setText("Thursday: Open 24 hours");
                hoursFriday.setText("Friday: Open 24 hours");
                hoursSaturday.setText("Saturday: Open 24 hours");
                hoursSunday.setText("Sunday: Open 24 hours");
                location.setImageDrawable(getResources().getDrawable(R.drawable.isa));
                break;

            case "Muma College of Business":
                destLocation = new LatLng(28.0583084,-82.40996);
                hours.setVisibility(View.VISIBLE);
                hoursMonday.setVisibility(View.VISIBLE);
                hoursTuesday.setVisibility(View.VISIBLE);
                hoursWednesday.setVisibility(View.VISIBLE);
                hoursThursday.setVisibility(View.VISIBLE);
                hoursFriday.setVisibility(View.VISIBLE);
                hoursSaturday.setVisibility(View.VISIBLE);
                hoursSunday.setVisibility(View.VISIBLE);
                hours.setText("HOURS");
                hoursMonday.setText("Monday: Open 24 hours");
                hoursTuesday.setText("Tuesday: Open 24 hours ");
                hoursWednesday.setText("Wednesday: Open 24 hours");
                hoursThursday.setText("Thursday: Open 24 hours");
                hoursFriday.setText("Friday: Open 24 hours");
                hoursSaturday.setText("Saturday: Open 24 hours");
                hoursSunday.setText("Sunday: Open 24 hours");
                location.setImageDrawable(getResources().getDrawable(R.drawable.cob));
                break;

            case "Cooper Hall":
                destLocation = new LatLng(28.0595977,-82.410524);
                hours.setVisibility(View.VISIBLE);
                hoursMonday.setVisibility(View.VISIBLE);
                hoursTuesday.setVisibility(View.VISIBLE);
                hoursWednesday.setVisibility(View.VISIBLE);
                hoursThursday.setVisibility(View.VISIBLE);
                hoursFriday.setVisibility(View.VISIBLE);
                hoursSaturday.setVisibility(View.VISIBLE);
                hoursSunday.setVisibility(View.VISIBLE);
                hours.setText("HOURS");
                hoursMonday.setText("Monday: Open 24 hours");
                hoursTuesday.setText("Tuesday: Open 24 hours ");
                hoursWednesday.setText("Wednesday: Open 24 hours");
                hoursThursday.setText("Thursday: Open 24 hours");
                hoursFriday.setText("Friday: Open 24 hours");
                hoursSaturday.setText("Saturday: Open 24 hours");
                hoursSunday.setText("Sunday: Open 24 hours");
                location.setImageDrawable(getResources().getDrawable(R.drawable.cooper));
                break;

            case "CIS Building":
                destLocation = new LatLng(28.0586563,-82.4110936);
                hours.setVisibility(View.VISIBLE);
                hoursMonday.setVisibility(View.VISIBLE);
                hoursTuesday.setVisibility(View.VISIBLE);
                hoursWednesday.setVisibility(View.VISIBLE);
                hoursThursday.setVisibility(View.VISIBLE);
                hoursFriday.setVisibility(View.VISIBLE);
                hoursSaturday.setVisibility(View.VISIBLE);
                hoursSunday.setVisibility(View.VISIBLE);
                hours.setText("HOURS");
                hoursMonday.setText("Monday: Open 24 hours");
                hoursTuesday.setText("Tuesday: Open 24 hours ");
                hoursWednesday.setText("Wednesday: Open 24 hours");
                hoursThursday.setText("Thursday: Open 24 hours");
                hoursFriday.setText("Friday: Open 24 hours");
                hoursSaturday.setText("Saturday: Open 24 hours");
                hoursSunday.setText("Sunday: Open 24 hours");
                location.setImageDrawable(getResources().getDrawable(R.drawable.cis));
                break;

            case "Recreation Center":
                destLocation = new LatLng(28.0602013,-82.4076104);
                hours.setVisibility(View.VISIBLE);
                hoursMonday.setVisibility(View.VISIBLE);
                hoursTuesday.setVisibility(View.VISIBLE);
                hoursWednesday.setVisibility(View.VISIBLE);
                hoursThursday.setVisibility(View.VISIBLE);
                hoursFriday.setVisibility(View.VISIBLE);
                hoursSaturday.setVisibility(View.VISIBLE);
                hoursSunday.setVisibility(View.VISIBLE);
                hours.setText("HOURS");
                hoursMonday.setText("Monday: Open 24 hours");
                hoursTuesday.setText("Tuesday: Open 24 hours ");
                hoursWednesday.setText("Wednesday: Open 24 hours");
                hoursThursday.setText("Thursday: Open 24 hours");
                hoursFriday.setText("Friday: Open 24 hours");
                hoursSaturday.setText("Saturday: Open 24 hours");
                hoursSunday.setText("Sunday: Open 24 hours");
                location.setImageDrawable(getResources().getDrawable(R.drawable.rec));
                break;

            case "Juniper-Poplar Hall":
                destLocation = new LatLng(28.059692, -82.418909);
                hours.setVisibility(View.VISIBLE);
                hoursMonday.setVisibility(View.VISIBLE);
                hoursTuesday.setVisibility(View.VISIBLE);
                hoursWednesday.setVisibility(View.VISIBLE);
                hoursThursday.setVisibility(View.VISIBLE);
                hoursFriday.setVisibility(View.VISIBLE);
                hoursSaturday.setVisibility(View.VISIBLE);
                hoursSunday.setVisibility(View.VISIBLE);
                hours.setText("HOURS");
                hoursMonday.setText("Monday: Open 24 hours");
                hoursTuesday.setText("Tuesday: Open 24 hours ");
                hoursWednesday.setText("Wednesday: Open 24 hours");
                hoursThursday.setText("Thursday: Open 24 hours");
                hoursFriday.setText("Friday: Open 24 hours");
                hoursSaturday.setText("Saturday: Open 24 hours");
                hoursSunday.setText("Sunday: Open 24 hours");
                location.setImageDrawable(getResources().getDrawable(R.drawable.jp));
                break;

            case "Engineering Building II":
                destLocation = new LatLng(28.058722, -82.415355);
                hours.setVisibility(View.VISIBLE);
                hoursMonday.setVisibility(View.VISIBLE);
                hoursTuesday.setVisibility(View.VISIBLE);
                hoursWednesday.setVisibility(View.VISIBLE);
                hoursThursday.setVisibility(View.VISIBLE);
                hoursFriday.setVisibility(View.VISIBLE);
                hoursSaturday.setVisibility(View.VISIBLE);
                hoursSunday.setVisibility(View.VISIBLE);
                hours.setText("HOURS");
                hoursMonday.setText("Monday: Open 24 hours");
                hoursTuesday.setText("Tuesday: Open 24 hours ");
                hoursWednesday.setText("Wednesday: Open 24 hours");
                hoursThursday.setText("Thursday: Open 24 hours");
                hoursFriday.setText("Friday: Open 24 hours");
                hoursSaturday.setText("Saturday: Open 24 hours");
                hoursSunday.setText("Sunday: Open 24 hours");
                location.setImageDrawable(getResources().getDrawable(R.drawable.enb));
                break;

            case "USF Bookstore":
                destLocation = new LatLng(28.0634588,-82.4125229);
                hours.setVisibility(View.VISIBLE);
                hoursMonday.setVisibility(View.VISIBLE);
                hoursTuesday.setVisibility(View.VISIBLE);
                hoursWednesday.setVisibility(View.VISIBLE);
                hoursThursday.setVisibility(View.VISIBLE);
                hoursFriday.setVisibility(View.VISIBLE);
                hoursSaturday.setVisibility(View.VISIBLE);
                hoursSunday.setVisibility(View.VISIBLE);
                hours.setText("HOURS");
                hoursMonday.setText("Monday: Open 24 hours");
                hoursTuesday.setText("Tuesday: Open 24 hours ");
                hoursWednesday.setText("Wednesday: Open 24 hours");
                hoursThursday.setText("Thursday: Open 24 hours");
                hoursFriday.setText("Friday: Open 24 hours");
                hoursSaturday.setText("Saturday: Open 24 hours");
                hoursSunday.setText("Sunday: Open 24 hours");
                location.setImageDrawable(getResources().getDrawable(R.drawable.bookstore));
                break;
        }
        destMarker =  mMap.addMarker(new MarkerOptions().position(destLocation).title("Destination"));
        float hue = 150;
        destMarker.setIcon(BitmapDescriptorFactory.defaultMarker(hue));
        route = mMap.addPolyline(new PolylineOptions().add(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                new LatLng(destLocation.latitude, destLocation.longitude)).width(8).color(Color.rgb(4, 94, 5)));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}

