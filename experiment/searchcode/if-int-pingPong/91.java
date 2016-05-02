package com.pingpong.android.base;

import android.content.DialogInterface;
import android.os.Bundle;

import com.pingpong.android.R;
import com.pingpong.android.utils.L;
import com.pingpong.android.utils.UIUtils;

/**
 * Created by JiangZhenJie on 2015/2/3.
 */
public abstract class NetBaseActivity extends BaseActivity implements IHttpCallBacker {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void sendHttpRequest(final int requestId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpRequest request = new HttpRequest(mContext, requestId, NetBaseActivity.this);
                request.doRequest();
            }
        }).start();
    }

    public void sendHttpRequest(final int requestId, final int message) {
        if (message > 0) {
            UIUtils.showProgressDialog(this, message);
        }
        sendHttpRequest(requestId);
    }

    @Override
    public void processHttpException(int httpState) {
        switch (httpState) {
            case -1:
                L.e("can not connect to server");
                UIUtils.showAlertDialog(this, R.string.can_not_link_server,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                break;
        }
    }
}

