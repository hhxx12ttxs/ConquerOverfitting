package com.pingpong.android.modules.friend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseActivity;

public class FriendDetailModifyActivity extends BaseActivity {

    public static final int FLAG_SIGN = 1;
    public static final int FLAG_ADDRESS = 2;
    public static final int FLAG_JOB = 3;
    public static final int FLAG_EXPERIENCE = 4;

    private EditText mEditText;
    private int mFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail_modify);
        initView();
        Intent intent = getIntent();
        mFlag = intent.getIntExtra("flag", -1);
        if (mFlag == -1) return;
        switch (mFlag) {
            case FLAG_ADDRESS:
                mActionBar.setTitle(R.string.update_friend_address);
                break;
            case FLAG_EXPERIENCE:
                mActionBar.setTitle(R.string.update_friend_experience);
                break;
            case FLAG_JOB:
                mActionBar.setTitle(R.string.update_friend_job);
                break;
            case FLAG_SIGN:
                mActionBar.setTitle(R.string.update_friend_sign);
                break;
        }
    }

    private void initView() {
        mEditText = (EditText) findViewById(R.id.et_text);
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra("data", mEditText.getText().toString());
        intent.putExtra("flag", mFlag);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }
}

