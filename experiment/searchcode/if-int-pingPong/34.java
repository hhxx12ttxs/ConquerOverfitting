package com.pingpong.android.modules.friend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.model.Hall;
import com.pingpong.android.model.ShowHallModel;
import com.pingpong.android.modules.hall.HallShowDetailActivity;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;
import com.pingpong.android.utils.UIUtils;

public class FriendHallListActivity extends NetBaseActivity {

    /* 提供外部Activity选择球馆的功能，传入该参数为true即可*/
    public static final String CHOOSE_FLAG = "Choose_Flag";

    private ListView mHallListView;
    private TextView mHallTypeView;
    private SearchView mHallSearchView;

    private int mHallType;
    private String mQueryText;
    private double mCurrentLatitude;
    private double mCurrentLongitude;
    private boolean mIsChooseHall;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_hall_list);

        mIsChooseHall = getIntent().getBooleanExtra(CHOOSE_FLAG, false);
        if (mIsChooseHall) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(R.string.please_choose);
        } else {
            DataManager.getInstance().finishAllActivityInList();
        }

        mHallListView = (ListView) findViewById(R.id.lv_hall_list);
        mHallListView.setAdapter(new FriendHallAdapter(this));
        mHallListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                FriendHallAdapter adapter = (FriendHallAdapter) mHallListView.getAdapter();
                Hall hall = adapter.getHallInPosition(position);

                if (mIsChooseHall) {
                    Intent intent = new Intent();
                    intent.putExtra("hall", hall);
                    setResult(RESULT_OK, intent);
                    mHallListView.setSelection(position);
                    finish();
                    return;
                }

                if (hall != null) {
                    Intent intent = new Intent(FriendHallListActivity.this, HallShowDetailActivity.class);
                    intent.putExtra("hall", hall);
                    startActivity(intent);
                }

            }
        });
        mHallTypeView = (TextView) findViewById(R.id.tv_hall_type);
        mHallSearchView = (SearchView) findViewById(R.id.sv_search_hall);

        mHallType = getIntent().getIntExtra("hall_type", FriendHallFragment.TYPE_MY_ASSOCIATE_HALL);
        if (mHallType == FriendHallFragment.TYPE_MY_ASSOCIATE_HALL) {
            mHallTypeView.setText(R.string.my_association_hall);
            sendRequest();
        } else if (mHallType == FriendHallFragment.TYPE_SEARCH_HALL) {
            mHallTypeView.setText(R.string.search_hall_result);
            mHallSearchView.setVisibility(View.VISIBLE);
            mHallSearchView.setIconified(false);
            mHallSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!TextUtils.isEmpty(query)) {
                        mQueryText = query;
                        sendRequest();
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        } else if (mHallType == FriendHallFragment.TYPE_NEARBY_HALL) {
            mHallTypeView.setText(R.string.nearby_hall);
            FriendHallAdapter adapter = (FriendHallAdapter) mHallListView.getAdapter();
            adapter.setHallType(FriendHallFragment.TYPE_NEARBY_HALL);
            locate();
        } else if (mHallType == FriendHallFragment.TYPE_FIND_HALL) {
            mHallTypeView.setText(R.string.find_hall);
            sendRequest();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
    }

    private void sendRequest() {
        sendHttpRequest(Constants.RequestId.ID_SHOW_HALL, R.string.common_loading_progress);
    }

    private void locate() {
        UIUtils.showProgressDialog(this, R.string.locating);
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

        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.requestLocation();
        } else if (mLocationClient != null) {
            mLocationClient.start();
            mLocationClient.requestLocation();
        }

    }

    private void updateUI(ShowHallModel hallModel) {
        if (hallModel == null || hallModel.getHalls() == null) return;
        mHallTypeView.setVisibility(View.VISIBLE);
        FriendHallAdapter adapter = (FriendHallAdapter) mHallListView.getAdapter();
        adapter.setHalls(hallModel.getHalls());
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_HALL:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_SHOW_HALL, ShowHallModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                switch (mHallType) {
                    case FriendHallFragment.TYPE_MY_ASSOCIATE_HALL:
                        // 不需要额外参数
                        break;
                    case FriendHallFragment.TYPE_FIND_HALL:
                        hrp.addParam("area", DataManager.getInstance().getSelectedCityString());
                        break;
                    case FriendHallFragment.TYPE_NEARBY_HALL:
                        hrp.addParam("latitude", mCurrentLatitude + "");
                        hrp.addParam("longitude", mCurrentLongitude + "");
                        break;
                    case FriendHallFragment.TYPE_SEARCH_HALL:
                        hrp.addParam("hallName", mQueryText);
                        break;
                }
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_HALL:
                ShowHallModel hallModel = (ShowHallModel) response;
                if (hallModel != null && hallModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    updateUI(hallModel);
                } else {
                    L.e("get hall fail");
                    Toast.makeText(this, R.string.request_data_fail, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null) return;
            UIUtils.dismissProgressDialog();
            mCurrentLatitude = bdLocation.getLatitude();
            mCurrentLongitude = bdLocation.getLongitude();
            L.i("locate use in latitude=" + bdLocation.getLatitude() + " longitude=" + bdLocation.getLongitude());
            sendRequest();
        }
    }
}

