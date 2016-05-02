package com.pingpong.android.modules.hall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.model.UpdateHallModel;
import com.pingpong.android.utils.Constants;

public class HallUpdateLocationActivity extends NetBaseActivity {

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    private MapView mMapView;
    private BaiduMap mMap;
    private ImageView mUpdateView;

    private String mLatitude;
    private String mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_update_location);

        initView();
        displayEnableGpsDialogIfNeeded();
        initBaidu();
    }

    private void initBaidu() {

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);

        mLocationClient.start();

    }

    private void initView() {
        mMapView = (MapView) findViewById(R.id.bd_map_view);
        mMap = mMapView.getMap();
        mUpdateView = (ImageView) findViewById(R.id.iv_update_location);
        mUpdateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocate();
            }
        });
    }

    private void displayEnableGpsDialogIfNeeded() {
        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGpsEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGpsEnabled) {
            showEnableGpsDialog();
        }
    }

    private void showEnableGpsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.enable_gps_message);
        builder.setPositiveButton("好的", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

        builder.create().show();
    }


    private void startLocate() {
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.requestLocation();
        } else if (mLocationClient != null) {
            mLocationClient.start();
            mLocationClient.requestLocation();
        }
    }

    private void updateMap(BDLocation location) {
        mLatitude = location.getLatitude() + "";
        mLongitude = location.getLongitude() + "";
        mMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(mMap.getMaxZoomLevel()).build()));
        mMap.setMyLocationEnabled(true);
        MyLocationData locationData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                .direction(100)
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();
        mMap.setMyLocationData(locationData);
        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null);
        mMap.setMyLocationConfigeration(config);
        askIfUpdateLocation(location);
    }

    private void askIfUpdateLocation(final BDLocation location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.finish_update_location_tips_layout, null);
        TextView addressText = (TextView) view.findViewById(R.id.tv_address);
        addressText.setText(location.getAddrStr());
        builder.setView(view);
        builder.setNegativeButton("直接上传", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uploadLocation();
            }
        });
        builder.setPositiveButton("好的", null);
        builder.create().show();
    }

    private void uploadLocation() {
        sendHttpRequest(Constants.RequestId.ID_UPDATE_HALL_LOCATION, R.string.common_loading_progress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        startLocate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationClient != null) {
            mLocationClient.stop();
            mMap.setMyLocationEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hall_update_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                uploadLocation();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_UPDATE_HALL_LOCATION:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_UPDATE_HALL_LOCATION, UpdateHallModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("hallLatitude", mLatitude);
                hrp.addParam("hallLongitude", mLongitude);
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_UPDATE_HALL_LOCATION:
                UpdateHallModel hallModel = (UpdateHallModel) response;
                if (hallModel != null && hallModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    Toast.makeText(this, R.string.update_location_success, Toast.LENGTH_SHORT).show();
                    this.finish();
                } else {
                    Toast.makeText(this, R.string.update_location_fail, Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null) return;
            DataManager.getInstance().getLoginHall().setHallAddress(bdLocation.getAddrStr());
            updateMap(bdLocation);
        }
    }
}

