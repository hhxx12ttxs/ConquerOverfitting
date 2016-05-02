package com.pingpong.android.modules.friend;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.pingpong.android.interfaces.DateTimePickerCallBack;
import com.pingpong.android.model.Friend;
import com.pingpong.android.model.Hall;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.UIUtils;
import com.pingpong.android.utils.Utils;

import java.util.Calendar;
import java.util.List;

public class FriendInviteActivity extends NetBaseActivity {

    public static final int REQUEST_HALL = 123;
    public static final int REQUEST_FRIEND = 124;

    private TextView mInviteTimeView;
    private TextView mInviteHallView;
    private TextView mInvitePeopleView;
    private EditText mInviteTextView;
    private Button mInviteBtn;

    private long mInviteTimeInMillis;
    private Hall mHall;
    private String mAddress;
    private List<Friend> mInviteFriends;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_invite);
        initView();
        initBaidu();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friend_invite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_my_invite:
                startActivity(new Intent(this,FriendPublishInviteActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_HALL:
                    mHall = data.getParcelableExtra("hall");
                    if (mHall != null) {
                        mInviteHallView.setText(mHall.getHallName());
                    }
                    break;
                case REQUEST_FRIEND:
                    mInviteFriends = data.getParcelableArrayListExtra("friends");
                    if (mInviteFriends != null) {
                        StringBuilder sb = new StringBuilder();
                        for (Friend friend : mInviteFriends) {
                            sb.append(friend.getFriendName());
                            sb.append(",");
                        }
                        sb.delete(sb.length() - 1, sb.length());
                        mInvitePeopleView.setText(sb.toString());
                    } else {
                        mInvitePeopleView.setText(R.string.invite_around_people);
                    }
                    break;
            }
        }
    }

    private void initView() {
        mInviteTimeView = (TextView) findViewById(R.id.tv_invite_time);
        mInviteHallView = (TextView) findViewById(R.id.tv_invite_hall);
        mInvitePeopleView = (TextView) findViewById(R.id.tv_invite_people);
        mInviteTextView = (EditText) findViewById(R.id.et_invite_text);
        mInviteBtn = (Button) findViewById(R.id.btn_send_invite);
    }

    public void viewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_invite_time:
                selectGameTime();
                break;
            case R.id.tv_invite_hall:
                startActivityForResult(new Intent(this, FriendInviteSelectHallActivity.class), REQUEST_HALL);
                break;
            case R.id.tv_invite_people:
                startActivityForResult(new Intent(this, FriendSelectActivity.class), REQUEST_FRIEND);
                break;
            case R.id.btn_send_invite:
                startInvite();
                break;
        }
    }

    private void selectGameTime() {
        UIUtils.showDateTimePicker(this, new DateTimePickerCallBack() {
            @Override
            public void onClick(DialogInterface dialog, DatePicker datePicker, TimePicker timePicker) {
                int year = datePicker.getYear();
                int month = datePicker.getMonth();
                int day = datePicker.getDayOfMonth();
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();

                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day, hour, minute);
                long timeInMillis = cal.getTimeInMillis();
                if (timeInMillis <= System.currentTimeMillis()) {
                    Toast.makeText(FriendInviteActivity.this, R.string.invite_time_invaild, Toast.LENGTH_SHORT).show();
                    timeInMillis += 1 * 24 * 60 * 60 * 1000;
                }
                mInviteTimeInMillis = timeInMillis;
                mInviteTimeView.setText(Utils.formatTime(timeInMillis));
            }
        });
    }

    private void startInvite() {
        if (mInviteTimeInMillis <= 0) {
            Toast.makeText(this, R.string.not_select_invite_time, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mHall == null) {
            Toast.makeText(this, R.string.not_select_invite_hall, Toast.LENGTH_SHORT).show();
            return;
        }

        String inviteText = mInviteTextView.getText().toString();
        if (TextUtils.isEmpty(inviteText)) {
            Toast.makeText(this, R.string.write_down_invite_text, Toast.LENGTH_SHORT).show();
            return;
        }

        sendHttpRequest(Constants.RequestId.ID_INVITE, R.string.common_loading_progress);
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

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_INVITE:  // 发起约球
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_INVITE, BaseModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("gameTime", mInviteTimeInMillis + "");
                hrp.addParam("inviteTime", System.currentTimeMillis() + "");
                hrp.addParam("inviteAddress", mAddress);
                hrp.addParam("hallId", mHall.getHallId() + "");
                hrp.addParam("inviteText", mInviteTextView.getText().toString());
                if (mInviteFriends != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Friend friend : mInviteFriends) {
                        sb.append(friend.getFriendId());
                        sb.append(",");
                    }
                    sb.delete(sb.length() - 1, sb.length());
                    hrp.addParam("invitePerson", sb.toString());
                }
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_INVITE:
                if (response != null && response.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    Toast.makeText(this, R.string.invite_success, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, R.string.invite_fail, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null) return;
            mAddress = bdLocation.getAddrStr();
        }
    }
}

