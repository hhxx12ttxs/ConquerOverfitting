package com.pingpong.android.modules.friend;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.LocateBaseActivity;
import com.pingpong.android.model.Friend;
import com.pingpong.android.model.InviteInfo;
import com.pingpong.android.model.ShowInviteModel;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;
import com.pingpong.android.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 附近的约球
 */
public class FriendReplyInviteActivity extends LocateBaseActivity implements FriendInviteFragment.ReplyInviteListener {

    private FriendInviteFragment mInviteFragment;
    private InviteInfo mReplyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_reply_invite);
        initView();
        UIUtils.showProgressDialog(this, R.string.locating);
        startLocate();
    }

    private void initView() {
        mInviteFragment = (FriendInviteFragment) getSupportFragmentManager().findFragmentById(R.id.friend_invite_fragment);
    }

    private void showReplyInviteSucceed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.reply_invite_success);
        builder.setNegativeButton("好的", null);
        builder.create().show();
        replySuccess();
    }

    private void replySuccess(){
        Friend friend = DataManager.getInstance().getLoginFriend();
        friend.setFriendStatus(Constants.FLAG_ACCEPT_INVITE);
        List<Friend> invitePeople =  mReplyInfo.getInvitePeoples();
        if (invitePeople == null || invitePeople.size() == 0){
            invitePeople = new ArrayList<>();
            mReplyInfo.setInvitePeoples(invitePeople);
        }
        invitePeople.add(friend);
        mInviteFragment.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friend_reply_invite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.my_receive_invite:
                startActivity(new Intent(this, FriendReceiveInviteActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        UIUtils.dismissProgressDialog();
        if (location == null) return;
        mLocation = location;
        sendHttpRequest(Constants.RequestId.ID_SHOW_INVITE);
        stopLocate();
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_INVITE:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_SHOW_INVITE, ShowInviteModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("latitude", mLocation.getLatitude() + "");
                hrp.addParam("longitude", mLocation.getLongitude() + "");
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
                ShowInviteModel inviteModel = (ShowInviteModel) response;
                if (inviteModel != null && inviteModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    if (inviteModel.getInvites() != null && inviteModel.getInvites().size() != 0) {
                        mInviteFragment.updateUI(inviteModel.getInvites());
                    }
                } else {
                    Toast.makeText(this, R.string.show_invite_fail, Toast.LENGTH_SHORT).show();
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

    @Override
    public void replyInvite(InviteInfo info) {
        mReplyInfo = info;
        sendHttpRequest(Constants.RequestId.ID_REPLY_INVITE, R.string.common_loading_progress);
    }
}

