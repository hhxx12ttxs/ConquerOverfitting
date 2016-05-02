package com.pingpong.android.modules.friend;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.model.Friend;
import com.pingpong.android.model.InviteInfo;
import com.pingpong.android.model.ShowInviteModel;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;

/**
 * 我收到的约球
 */
public class FriendReceiveInviteActivity extends NetBaseActivity implements FriendInviteFragment.ReplyInviteListener {

    private FriendInviteFragment mInviteFragment;
    private InviteInfo mReplyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_receive_invite);
        initView();
        sendHttpRequest(Constants.RequestId.ID_SHOW_INVITE, R.string.common_loading_progress);
    }

    private void initView() {
        mInviteFragment = (FriendInviteFragment) getSupportFragmentManager().findFragmentById(R.id.friend_receive_invite_fragment);
    }

    private void showReplyInviteSucceed() {
        for (Friend fri : mReplyInfo.getInvitePeoples()) {
            if (fri.getFriendId() == DataManager.getInstance().getLoginUserId()) {
                fri.setFriendStatus(Constants.FLAG_ACCEPT_INVITE);
                mInviteFragment.notifyDataSetChanged();
                break;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.reply_invite_success);
        builder.setNegativeButton("好的", null);
        builder.create().show();
    }

    @Override
    public void replyInvite(InviteInfo info) {
        mReplyInfo = info;
        sendHttpRequest(Constants.RequestId.ID_REPLY_INVITE, R.string.common_loading_progress);
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_INVITE:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_SHOW_INVITE, ShowInviteModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("flag", Constants.FLAG_SHOW_RECEIVE_INVITE + "");
                break;
            case Constants.RequestId.ID_REPLY_INVITE:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_REPLY_INVITE, BaseModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("inviteId", mReplyInfo.getInviteId() + "");
                hrp.addParam("inviteAgree", 0 + "");
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_INVITE:
                ShowInviteModel showInviteModel = (ShowInviteModel) response;
                if (showInviteModel != null && showInviteModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    mInviteFragment.updateUI(showInviteModel.getInvites());
                } else {
                    Toast.makeText(this, R.string.request_data_fail, Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.RequestId.ID_REPLY_INVITE:
                if (response != null && response.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    showReplyInviteSucceed();
                } else {
                    Toast.makeText(this, R.string.reply_invite_fail, Toast.LENGTH_SHORT).show();
                    if (response != null)
                        L.d(response.getResultMessage());
                }
                break;
        }
    }
}

