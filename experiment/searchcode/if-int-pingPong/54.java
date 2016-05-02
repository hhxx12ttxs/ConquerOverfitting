package com.pingpong.android.modules.register;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.model.RegisterModel;
import com.pingpong.android.modules.login.LoginActivity;
import com.pingpong.android.utils.Constants;

public class RegisterActivity extends NetBaseActivity {

    public static final String KEY_REGISTER_TYPE = "register-type";

    private RegisterFriendFragment mRegisterFriend;
    private RegisterHallFragment mRegisterHall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        int type = getIntent().getIntExtra(KEY_REGISTER_TYPE, Constants.TYPE_FRIEND);
        if (type == Constants.TYPE_FRIEND) {
            mRegisterFriend = new RegisterFriendFragment();
            ft.replace(R.id.fl_register_container, mRegisterFriend);
        } else if (type == Constants.TYPE_HALL) {
            mRegisterHall = new RegisterHallFragment();
            ft.replace(R.id.fl_register_container, mRegisterHall);
        }
        ft.commit();
    }

    private void showIdForHallAfterRegister(final int userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("注册成功，您的ID为 " + userId + "\n请牢记此ID,以后将以此作为登录帐号。");
        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra("user_name", userId + "");
                startActivity(intent);
                finish();
            }
        });
        builder.create().show();
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        switch (requestId) {
            case Constants.RequestId.ID_REGISTER:
                return mRegisterFriend == null ? mRegisterHall.getRegisterHallParams() : mRegisterFriend.getRegisterFriendParams();
        }
        return null;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_REGISTER:
                RegisterModel registerModel = (RegisterModel) response;
                if (registerModel != null && registerModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                    if (mRegisterHall != null) {
                        showIdForHallAfterRegister(registerModel.getUserId());
                    } else {
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("user_name", registerModel.getUserId() + "");
                        startActivity(intent);
                        finish();
                    }
                } else if (registerModel != null && registerModel.getResultCode() == Constants.ResultCode.RESULT_DATA_INVALID) {
                    Toast.makeText(this, R.string.phone_not_unique, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.register_fail, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}

