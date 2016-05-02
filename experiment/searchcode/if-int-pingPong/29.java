package com.pingpong.android;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;

import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.db.DataBaseHelper;
import com.pingpong.android.model.LoginModel;
import com.pingpong.android.modules.friend.FriendMainActivity;
import com.pingpong.android.modules.hall.HallMainActivity;
import com.pingpong.android.modules.login.LoginActivity;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;
import com.pingpong.android.utils.PreferenceUtils;
import com.pingpong.android.utils.Utils;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * 负责静默登录以及跳板机
 */
public class WelcomeActivity extends NetBaseActivity {

    private String mUserName;
    private String mPassword;
    private String mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

//        Context context = this;
//        Intent intent = new Intent(context,WelcomeActivity.class);
//        intent.putExtra("redirect",FriendReceiveInviteActivity.class.getCanonicalName());
//        L.e(intent.toUri(Intent.URI_INTENT_SCHEME));


        initCityDataBase();

        if (canSilentLogin()) {
            silentLogin();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private boolean canSilentLogin() {
        mUserName = PreferenceUtils.getInstance().readUserKeyChainByKey(PreferenceUtils.KEYCHAIN_USERNAME);
        mPassword = PreferenceUtils.getInstance().readUserKeyChainByKey(PreferenceUtils.KEYCHAIN_PASSWORD);
        mType = PreferenceUtils.getInstance().readUserKeyChainByKey(PreferenceUtils.KEYCHAIN_TYPE);
        return !TextUtils.isEmpty(mUserName) && !TextUtils.isEmpty(mPassword) && !TextUtils.isEmpty(mType);
    }

    private void silentLogin() {
        sendHttpRequest(Constants.RequestId.ID_LOGIN);
    }

    private void registerXGPush() {
        Context context = getApplicationContext();
        XGPushManager.registerPush(context, DataManager.getInstance().getLoginUserId() + "", new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                L.d("data=" + data + "  flag=" + flag);
            }

            @Override
            public void onFail(Object data, int error, String msg) {
                L.d("data=" + data + "  error=" + error + "  msg=" + msg);
            }
        });
    }

    private void silentLoginSucceed(LoginModel loginModel) {
        DataManager.getInstance().setLoginType(Integer.parseInt(mType));
        DataManager.getInstance().setLoginUserId(Long.parseLong(mUserName));

        Intent intent = getIntent();
        String redirect = intent.getStringExtra("redirect");
        if (!TextUtils.isEmpty(redirect)) {
            redirectTo(redirect);
        } else {
            if (mType.equals(Constants.TYPE_FRIEND + "")) {
                DataManager.getInstance().setLoginFriend(loginModel.getFriend());
                registerXGPush();
                startActivity(new Intent(this, FriendMainActivity.class));
            } else {
                DataManager.getInstance().setLoginHall(loginModel.getHall());
                startActivity(new Intent(this, HallMainActivity.class));
            }
        }
        finish();
    }

    /**
     * 跳板机
     */
    private void redirectTo(String redirect) {
        startActivity(new Intent(this,FriendMainActivity.class));
        Intent intent = getIntent();
        try {
            intent.setClass(this, Class.forName(redirect));
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * 初始化城市列表数据库
     */
    private void initCityDataBase() {
        AssetManager manager = getAssets();
        try {
            InputStream inputStream = manager.open("city-data.sql");
            String dataSql = Utils.inputStreamToString(inputStream);
            DataBaseHelper.initCityDataBase(dataSql);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_LOGIN:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_LOGIN, LoginModel.class);
                hrp.addParam("userName", mUserName);
                hrp.addParam("password", mPassword);
                hrp.addParam("type", mType);
                break;
        }
        return hrp;
    }


    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_LOGIN:
                LoginModel loginModel = (LoginModel) response;
                if (loginModel != null && loginModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    silentLoginSucceed(loginModel);
                } else {
                    L.e("Login Fail : " + response.toString());
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
                break;
        }
    }
}

