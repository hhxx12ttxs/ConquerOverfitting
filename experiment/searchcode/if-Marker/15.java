package com.anand.markers.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class Marker extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.marker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener{

        private static final int DEFAULT_ZOOM = 15;
        private Button goButton;
        private EditText locationValue;
        private GoogleMap mMap;
        private com.google.android.gms.maps.model.Marker marker;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            if(isPlayServicesAvailable(getActivity())) {
                View rootView = inflater.inflate(R.layout.fragment_marker, container, false);
                SupportMapFragment mapFragment=(SupportMapFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.mapFragment);
                mMap=mapFragment.getMap();
                mMap.setMyLocationEnabled(true);
                goButton=(Button)rootView.findViewById(R.id.goLocation);
                goButton.setOnClickListener(this);
                locationValue=(EditText)rootView.findViewById(R.id.location);
                return rootView;
            }return null;
        }

        public boolean isPlayServicesAvailable(final Activity activity) {
            final  int checkServices= GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
            switch (checkServices){
                case ConnectionResult.SUCCESS:
                    return true;
                case ConnectionResult.SERVICE_DISABLED:
                case ConnectionResult.SERVICE_INVALID:
                case ConnectionResult.SERVICE_MISSING:
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(checkServices, activity, 0);
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            activity.finish();
                        }
                    });
                    dialog.show();
            }
            return false;



        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.goLocation:
                    InputMethodManager manager= (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(),0);
                    Geocoder geocoder=new Geocoder(getActivity());
                    List<Address> addresses= null;
                    try {
                        addresses = geocoder.getFromLocationName(locationValue.getText().toString(),1);
                        Address address=addresses.get(0);
                        String locality=address.getLocality();
                        String country=address.getCountryName();
                        
                        double lat=address.getLatitude();
                        double lng=address.getLongitude();
                        goToLocation(lat,lng,DEFAULT_ZOOM);
                        if(marker!=null){
                            marker.remove();
                        }
                        MarkerOptions markerOptions=new MarkerOptions().title(locality).position(new LatLng(lat,lng)).

                                //icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
                                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        if(country.length()>0){
                            markerOptions.snippet(country);
                        }

                        marker= mMap.addMarker(markerOptions);

                        Toast.makeText(getActivity(), locality, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        private void goToLocation(double lat, double lng, float defaultZoom) {
            LatLng ll=new LatLng(lat,lng);
            CameraUpdate update= CameraUpdateFactory.newLatLngZoom(ll,defaultZoom);
            mMap.moveCamera(update);

        }
    }
}

