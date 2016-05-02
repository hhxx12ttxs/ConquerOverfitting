package com.pingpong.android.modules.hall;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.model.PublishMomentModel;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;

public class HallPublishMomentActivity extends NetBaseActivity {

    private static final int MAX_CONTENT_LENGTH = 1024;

    private EditText mContent;
    private Button mSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_publish_moment);
        initView();
    }

    private void initView() {
        mContent = (EditText) findViewById(R.id.et_moment_content);
        mSubmit = (Button) findViewById(R.id.btn_submit_publish_moment);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewClicked(v);
            }
        });
    }

    private boolean checkBeforeSubmit() {
        String content = mContent.getText().toString();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "动态内容不能为空！", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            Toast.makeText(this, "内容不能超过1024个字！", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void publishMoment() {
        sendHttpRequest(Constants.RequestId.ID_PUBLISH_MOMENT, R.string.common_loading_progress);
    }

    public void viewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_submit_publish_moment:
                if (checkBeforeSubmit()) {
                    publishMoment();
                }
                break;
        }
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_PUBLISH_MOMENT:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_PUBLISH_MOMENT, PublishMomentModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("momentContent", mContent.getText().toString());
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_PUBLISH_MOMENT:
                PublishMomentModel momentModel = (PublishMomentModel) response;
                if (momentModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    Toast.makeText(this, "动态发送成功！", Toast.LENGTH_SHORT).show();
                    this.finish();
                } else {
                    Toast.makeText(this, "动态发送失败！", Toast.LENGTH_SHORT).show();
                    L.e(response.getResultMessage());
                }
                break;
        }
    }
}

