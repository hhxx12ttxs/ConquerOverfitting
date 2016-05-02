package com.pingpong.android.modules.login;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.model.Friend;
import com.pingpong.android.model.Hall;
import com.pingpong.android.model.LoginModel;
import com.pingpong.android.modules.friend.FriendMainActivity;
import com.pingpong.android.modules.hall.HallMainActivity;
import com.pingpong.android.modules.register.RegisterActivity;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;
import com.pingpong.android.utils.PreferenceUtils;
import com.pingpong.android.utils.UIUtils;
import com.pingpong.android.utils.Utils;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

public class LoginActivity extends NetBaseActivity {

    private TextView mUserNameView;
    private TextView mPassword;
    private RadioGroup mTypeGroup;
    private RadioButton mTypeFriend;
    private RadioButton mTypeHall;
    private TextView mLoginTips;
    private Button mLoginSubmit;
    private TextView mNewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*注册完成后返回更新用户名*/
        String userName = getIntent().getStringExtra("user_name");
        if (!TextUtils.isEmpty(userName)) {
            mUserNameView.setText(userName);
        }
    }

    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mUserNameView = (TextView) findViewById(R.id.et_user_name);
        mPassword = (TextView) findViewById(R.id.et_password);
        mTypeGroup = (RadioGroup) findViewById(R.id.rg_type_select);
        mTypeFriend = (RadioButton) findViewById(R.id.rb_type_friend);
        mTypeHall = (RadioButton) findViewById(R.id.rb_type_hall);
        mLoginTips = (TextView) findViewById(R.id.tv_login_tips);
        mLoginSubmit = (Button) findViewById(R.id.btn_submit_login);
        mNewUser = (TextView) findViewById(R.id.tv_new_user);

        mTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_type_hall:
                        mLoginTips.setVisibility(View.VISIBLE);
                        mLoginTips.setText(R.string.hall_type_login_tips);
                        break;
                    case R.id.rb_type_friend:
                        mLoginTips.setVisibility(View.INVISIBLE);
                        mLoginTips.setText("");
                        break;
                }
            }
        });
    }

    private boolean loginCheck() {
        if (TextUtils.isEmpty(mUserNameView.getText().toString())) {
            mLoginTips.setVisibility(View.VISIBLE);
            mLoginTips.setText(R.string.user_name_can_not_null);
            return false;
        }

        if (TextUtils.isEmpty(mPassword.getText().toString())) {
            mLoginTips.setVisibility(View.VISIBLE);
            mLoginTips.setText(R.string.password_invalid);
            return false;
        }

        mLoginTips.setVisibility(View.INVISIBLE);
        mLoginTips.setText("");

        return true;
    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.btn_submit_login:
                if (loginCheck()) {
                    sendHttpRequest(Constants.RequestId.ID_LOGIN, R.string.common_loading_progress);
                }
                break;
            case R.id.tv_new_user:
                showTypeSelector();
                break;
        }
    }

    private void showTypeSelector() {
        UIUtils.showSelectorDialog(this, new String[]{"球友", "球馆"}, new DialogInterface.OnClickListener() {
            int type;

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        type = Constants.TYPE_FRIEND;
                        break;
                    case 1:
                        type = Constants.TYPE_HALL;
                        break;
                }
                redirectToRegister(type);
            }
        });
    }

    private void redirectToRegister(int type) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra(RegisterActivity.KEY_REGISTER_TYPE, type);
        startActivity(intent);
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

    private void loginSucceed(LoginModel loginModel) {
        int type = mTypeFriend.isChecked() ? Constants.TYPE_FRIEND : Constants.TYPE_HALL;
        long userId;
        String password = Utils.encryptByMD5(mPassword.getText().toString());
        if (type == Constants.TYPE_HALL) {
            Hall hall = loginModel.getHall();
            userId = hall.getHallId();
            DataManager.getInstance().setLoginHall(hall);
        } else {
            Friend friend = loginModel.getFriend();
            userId = friend.getFriendId();
            DataManager.getInstance().setLoginFriend(friend);
        }
        DataManager.getInstance().setLoginType(type);
        DataManager.getInstance().setLoginUserId(userId);
        PreferenceUtils.getInstance().saveUserKeyChain(userId + "", password, type);
        Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_LOGIN:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_LOGIN, LoginModel.class);
                hrp.addParam("userName", mUserNameView.getText().toString());
                hrp.addParam("password", Utils.encryptByMD5(mPassword.getText().toString()));
                int type = mTypeFriend.isChecked() ? Constants.TYPE_FRIEND : Constants.TYPE_HALL;
                hrp.addParam("type", type + "");
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_LOGIN:
                LoginModel loginModel = (LoginModel) response;
                if (loginModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    loginSucceed(loginModel);
                    int type = mTypeFriend.isChecked() ? Constants.TYPE_FRIEND : Constants.TYPE_HALL;
                    if (type == Constants.TYPE_HALL) {
                        startActivity(new Intent(this, HallMainActivity.class));
                    } else {
                        registerXGPush();
                        startActivity(new Intent(this, FriendMainActivity.class));
                    }
                    finish();
                } else {
                    L.e("Login Fail : " + response.toString());
                    Toast.makeText(this, R.string.login_fail, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}

