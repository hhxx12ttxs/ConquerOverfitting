package com.pingpong.android.modules.friend;

import android.os.Bundle;
import android.widget.Toast;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.model.InviteInfo;
import com.pingpong.android.model.ShowInviteModel;
import com.pingpong.android.utils.Constants;

/**
 * 我发起的约球
 */
public class FriendPublishInviteActivity extends NetBaseActivity implements FriendInviteFragment.ReplyInviteListener {

    private FriendInviteFragment mInviteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_publish_invite);
        mInviteFragment = (FriendInviteFragment) getSupportFragmentManager().findFragmentById(R.id.friend_publish_invite_fragment);
        mInviteFragment.hideInviteButton();
        sendHttpRequest(Constants.RequestId.ID_SHOW_INVITE,R.string.common_loading_progress);
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId){
            case Constants.RequestId.ID_SHOW_INVITE: //我发起的约球
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_SHOW_INVITE, ShowInviteModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("flag", Constants.FLAG_SHOW_PUBLISH_INVITE + "");
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId){
            case Constants.RequestId.ID_SHOW_INVITE:
                ShowInviteModel showInviteModel = (ShowInviteModel) response;
                if (showInviteModel != null && showInviteModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    mInviteFragment.updateUI(showInviteModel.getInvites());
                } else {
                    Toast.makeText(this, R.string.request_data_fail, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void replyInvite(InviteInfo info) {

    }
}

