package com.pingpong.android.modules.hall;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.model.Hall;
import com.pingpong.android.model.UpdateHallModel;
import com.pingpong.android.utils.Constants;

public class HallModifyDetailActivity extends NetBaseActivity implements View.OnClickListener {

    private static final int MAX_LENGTH_ADDRESS = 128;
    private static final int MAX_LENGTH_CHARGE = 128;
    private static final int MAX_LENGTH_PARK = 128;
    private static final int PHONE_LENGTH = 11;

    private EditText mHallAddress;
    private EditText mHallCharges;
    private EditText mHallPark;
    private EditText mHallPhone;
    private TextView mUpdateLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_modify_detail);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateView();
    }

    private void initView() {
        mHallAddress = (EditText) findViewById(R.id.et_hall_address);
        mHallCharges = (EditText) findViewById(R.id.et_hall_charge);
        mHallPark = (EditText) findViewById(R.id.et_hall_park);
        mHallPhone = (EditText) findViewById(R.id.et_hall_phone);
        mUpdateLocation = (TextView) findViewById(R.id.tv_update_location);
        mUpdateLocation.setOnClickListener(this);
    }

    private void updateView() {
        Hall hall = DataManager.getInstance().getLoginHall();
        if (hall == null) return;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(hall.getHallName());

        mHallAddress.setText(hall.getHallAddress());
        mHallCharges.setText(hall.getHallCharges());
        mHallPark.setText(hall.getHallPark());
        mHallPhone.setText(hall.getHallPhone());
    }

    private void submitModify() {
        Hall hall = DataManager.getInstance().getLoginHall();

        String newAddress = mHallAddress.getText().toString();
        String newCharge = mHallCharges.getText().toString();
        String newPhone = mHallPhone.getText().toString();
        String newPark = mHallPark.getText().toString();


        if (newAddress.length() > MAX_LENGTH_ADDRESS) {
            Toast.makeText(this, R.string.address_too_long, Toast.LENGTH_SHORT).show();
            return;
        }

        if (newCharge.length() > MAX_LENGTH_CHARGE) {
            Toast.makeText(this, R.string.charge_too_long, Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPark.length() > MAX_LENGTH_PARK) {
            Toast.makeText(this, R.string.park_too_long, Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPhone.length() != 0 && newPhone.length() != PHONE_LENGTH) {
            Toast.makeText(this, R.string.phone_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        sendHttpRequest(Constants.RequestId.ID_UPDATE_HALL, R.string.common_loading_progress);
    }

    private void updateInfo(){
        String newAddress = mHallAddress.getText().toString();
        String newCharge = mHallCharges.getText().toString();
        String newPhone = mHallPhone.getText().toString();
        String newPark = mHallPark.getText().toString();
        Hall hall = DataManager.getInstance().getLoginHall();
        hall.setHallAddress(newAddress);
        hall.setHallCharges(newCharge);
        hall.setHallPark(newPark);
        hall.setHallPhone(newPhone);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hall_modify_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_finish:
                submitModify();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_UPDATE_HALL:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_UPDATE_HALL, UpdateHallModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("hallAddress", mHallAddress.getText().toString().trim());
                hrp.addParam("hallCharges", mHallCharges.getText().toString().trim());
                hrp.addParam("hallPark", mHallPark.getText().toString().trim());
                hrp.addParam("hallPhone", mHallPhone.getText().toString().trim());
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_UPDATE_HALL:
                UpdateHallModel model = (UpdateHallModel) response;
                if (model.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    Toast.makeText(this, R.string.modify_finish, Toast.LENGTH_SHORT).show();
                    updateInfo();
                    this.finish();
                } else {
                    Toast.makeText(this, R.string.modify_fail, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_update_location:
                startActivity(new Intent(this, HallUpdateLocationActivity.class));
                break;
        }
    }
}

