package com.pingpong.android.modules.friend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.model.Friend;

import java.util.ArrayList;
import java.util.HashSet;

public class FriendSelectActivity extends NetBaseActivity {

    private FriendFragment mFriendFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_select);
        initView();
    }

    private void initView() {
        FragmentManager fm = getSupportFragmentManager();
        mFriendFragment = (FriendFragment) fm.findFragmentById(R.id.invite_select_friend_fragment);
        FriendMainActivity ac = (FriendMainActivity) DataManager.getInstance().getMainActivity();
        mFriendFragment.setFriends(ac.getFriends());
        mFriendFragment.setGroupSelect(true);
    }

    private void didSelected() {
        HashSet<Friend> friends = mFriendFragment.didSelectFriend();
        ArrayList<Friend> result = null;
        if (friends != null) {
            result = new ArrayList<>(friends);
        }
        Intent data = new Intent();
        data.putParcelableArrayListExtra("friends", result);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friend_select, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_did_selected:
                didSelected();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        return null;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {

    }
}

