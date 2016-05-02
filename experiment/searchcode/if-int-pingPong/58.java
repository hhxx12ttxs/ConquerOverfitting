package com.pingpong.android.modules.friend;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.SearchView;
import android.widget.Toast;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.model.ShowFriendModel;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;

public class FriendAddFriendActivity extends NetBaseActivity {

    private SearchView mSearchView;
    private FriendFragment mFriendFragment;

    private String mQueryString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add_friend);
        initView();
    }

    private void initView() {
        mSearchView = (SearchView) findViewById(R.id.sv_add_friend);
        mSearchView.setIconified(false);
        FragmentManager fm = getSupportFragmentManager();
        mFriendFragment = (FriendFragment) fm.findFragmentById(R.id.friend_fragment);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.isEmpty(query)) {
                    return false;
                }
                mQueryString = query;
                sendHttpRequest(Constants.RequestId.ID_SHOW_FRIEND, R.string.common_loading_progress);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_FRIEND:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_SHOW_FRIEND, ShowFriendModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("userName", mQueryString);
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_FRIEND:
                ShowFriendModel showFriendModel = (ShowFriendModel) response;
                if (showFriendModel != null && showFriendModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    mFriendFragment.setFriends(showFriendModel.getFriends());
                } else {
                    Toast.makeText(this, R.string.request_data_fail, Toast.LENGTH_SHORT).show();
                    if (showFriendModel != null)
                        L.e(showFriendModel.getResultMessage());
                }
                break;
        }
    }
}

