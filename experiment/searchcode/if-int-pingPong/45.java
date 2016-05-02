package com.pingpong.android.common;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.modules.login.LoginActivity;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;
import com.pingpong.android.utils.PreferenceUtils;

public class SettingActivity extends NetBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

    public void viewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_check_update:
                Toast.makeText(this, "亲，检查更新功能暂时还没上线，有新包我们会直接推送给你的", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_about_us:
                Toast.makeText(this,"该功能尚未上线",Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_logout:
                showLogoutConfirmDialog();
                break;
        }
    }

    private void showLogoutConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.logout_confirm);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendHttpRequest(Constants.RequestId.ID_LOGOUT, R.string.common_loading_progress);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_LOGOUT:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_LOGOUT, BaseModel.class);
                hrp.addParam("userName", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("type", DataManager.getInstance().getLoginType() + "");
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_LOGOUT:
                if (response != null && response.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    PreferenceUtils.getInstance().clearUserKeyChain();
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    DataManager.getInstance().getMainActivity().finish();
                    finish();
                } else {
                    if (response != null) {
                        L.d(response.getResultMessage());
                    }
                    Toast.makeText(this, R.string.logout_fail, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}

