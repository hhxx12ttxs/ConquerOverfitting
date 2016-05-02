package com.pingpong.android.modules.friend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.jauker.widget.BadgeView;
import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.common.SettingActivity;
import com.pingpong.android.db.ChatDao;
import com.pingpong.android.interfaces.OnChatPreItemClickListener;
import com.pingpong.android.library.zxing.CaptureActivity;
import com.pingpong.android.model.Friend;
import com.pingpong.android.model.ShowFriendModel;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;
import com.umeng.update.UmengUpdateAgent;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;


public class FriendMainActivity extends NetBaseActivity implements OnChatPreItemClickListener {



    private TabHost mTabHost;

    private FriendFragment mFriendFragment;
    private FriendHallFragment mHallFragment;
    private FriendDiscoveryFragment mDiscoveryFragment;
    private FriendChatPreviewListFragment mChatListFragment;

    private FriendMainReceiver mAddFriendReceiver;
    private FriendMainReceiver mChatReceiver;

    private BadgeView mChatBadgeView;

    private ReadRecentChatTask mReadRecentChatTask;

    private List<Friend> mFriends;
    private boolean mGroupChat;
    private int mSelectIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reLoginIfNeeded();
        initView();
        mAddFriendReceiver = new FriendMainReceiver();
        mChatReceiver = new FriendMainReceiver();
        sendListFriendRequest();

        UmengUpdateAgent.update(this);
        UmengUpdateAgent.setUpdateOnlyWifi(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
        DataManager.getInstance().setIsMainActivityOnForeground(true);
        mReadRecentChatTask = new ReadRecentChatTask();
        mReadRecentChatTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        DataManager.getInstance().setIsMainActivityOnForeground(false);
        mReadRecentChatTask.cancel(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
    }

    private void initView() {

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();

        View friendLayout = LayoutInflater.from(this).inflate(R.layout.friend_main_tab_indictor, null);
        View hallLayout = LayoutInflater.from(this).inflate(R.layout.friend_main_tab_indictor, null);
        View chatLayout = LayoutInflater.from(this).inflate(R.layout.friend_main_tab_indictor, null);
        View discoveryLayout = LayoutInflater.from(this).inflate(R.layout.friend_main_tab_indictor, null);

        mChatBadgeView = (BadgeView) chatLayout.findViewById(R.id.bv_unread_count);

        TextView friendIndicator = (TextView) friendLayout.findViewById(R.id.tab_item);
        TextView hallIndicator = (TextView) hallLayout.findViewById(R.id.tab_item);
        TextView chatIndicator = (TextView) chatLayout.findViewById(R.id.tab_item);
        TextView discoveryIndicator = (TextView) discoveryLayout.findViewById(R.id.tab_item);

        friendIndicator.setText(R.string.ball_friend);
        friendIndicator.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_friend, 0, 0);

        hallIndicator.setText(R.string.ball_hall);
        hallIndicator.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_hall, 0, 0);

        chatIndicator.setText(R.string.chat);
        chatIndicator.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_chat, 0, 0);

        discoveryIndicator.setText(R.string.discovery);
        discoveryIndicator.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_discovery, 0, 0);

        TabHost.TabSpec friendTab = mTabHost.newTabSpec("friendTab").setContent(R.id.friend_fragment).setIndicator(friendLayout);
        TabHost.TabSpec hallTab = mTabHost.newTabSpec("hallTab").setContent(R.id.hall_fragment).setIndicator(hallLayout);
        TabHost.TabSpec chatTab = mTabHost.newTabSpec("chatTab").setContent(R.id.chat_fragment).setIndicator(chatLayout);
        TabHost.TabSpec discoveryTab = mTabHost.newTabSpec("discoveryTab").setContent(R.id.discovery_fragment).setIndicator(discoveryLayout);

        mTabHost.addTab(friendTab);
        mTabHost.addTab(hallTab);
        mTabHost.addTab(chatTab);
        mTabHost.addTab(discoveryTab);

        mFriendFragment = (FriendFragment) getSupportFragmentManager().findFragmentById(R.id.friend_fragment);
        mHallFragment = (FriendHallFragment) getSupportFragmentManager().findFragmentById(R.id.hall_fragment);
        mChatListFragment = (FriendChatPreviewListFragment) getSupportFragmentManager().findFragmentById(R.id.chat_fragment);
        mDiscoveryFragment = (FriendDiscoveryFragment) getSupportFragmentManager().findFragmentById(R.id.discovery_fragment);

        DataManager.getInstance().setMainActivity(this);

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                ActionBar actionBar = getSupportActionBar();
                if (TextUtils.equals("friendTab", tabId)) {
                    actionBar.setTitle(R.string.ball_friend_list);
                    mSelectIndex = 0;
                } else if (TextUtils.equals("hallTab", tabId)) {
                    actionBar.setTitle(R.string.ball_hall_list);
                    mSelectIndex = 1;
                } else if (TextUtils.equals("chatTab", tabId)) {
                    actionBar.setTitle(R.string.chat_list);
                    mSelectIndex = 2;
                } else if (TextUtils.equals("discoveryTab", tabId)) {
                    actionBar.setTitle(R.string.discovery_list);
                    mSelectIndex = 3;

                }
                FriendMainActivity.this.invalidateOptionsMenu();
            }
        });
    }



    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(FriendDetailActivity.ACTION_ADD_FRIEND);
        registerReceiver(mAddFriendReceiver, filter);
        filter.addAction(Constants.Action.ACTION_RECEIVE_CHAT);
        registerReceiver(mChatReceiver, filter);
    }

    private void unRegisterReceiver() {
        unregisterReceiver(mAddFriendReceiver);
        unregisterReceiver(mChatReceiver);
    }

    public void sendListFriendRequest() {
        sendHttpRequest(Constants.RequestId.ID_GET_FRIEND_LIST);
    }

    public List<Friend> getFriends() {
        return mFriends;
    }

    public void addChatTips() {
        if (mChatBadgeView != null) {
            mChatBadgeView.incrementBadgeCount(1);
        }
    }

    private void startToGroupChat(HashSet<Friend> friends) {
        if (friends == null || friends.size() == 0) {
            Toast.makeText(this, R.string.no_select_friend, Toast.LENGTH_SHORT).show();
            return;
        }

        List<Friend> friendList = new ArrayList<>(friends);

        Collections.sort(friendList, new Comparator<Friend>() {
            @Override
            public int compare(Friend lhs, Friend rhs) {
                return (int) (lhs.getFriendId() - rhs.getFriendId());
            }
        });

        StringBuilder userIdSb = new StringBuilder();
        StringBuilder userNameSb = new StringBuilder();

        for (Friend friend : friendList) {
            userIdSb.append(friend.getFriendId());
            userNameSb.append(friend.getFriendName());
            userIdSb.append(Constants.LINK_CHAR);
            userNameSb.append(Constants.LINK_CHAR);
        }
        userIdSb.delete(userIdSb.length() - 1, userIdSb.length());
        userNameSb.delete(userNameSb.length() - 1, userNameSb.length());
        L.d(userIdSb.toString());
        L.d(userNameSb.toString());

        Intent intent = new Intent(this, FriendChatActivity.class);
        intent.putExtra("userId", userIdSb.toString());
        intent.putExtra("userName", userNameSb.toString());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friend_main, menu);

        MenuItem addFriendMenu = menu.findItem(R.id.add_friend);
        MenuItem goToChatMenu = menu.findItem(R.id.go_to_group_chat);
        MenuItem chatMenu = menu.findItem(R.id.group_chat);
        MenuItem settingMenu = menu.findItem(R.id.action_settings);
        MenuItem scanMenu = menu.findItem(R.id.action_scan);
        switch (mSelectIndex) {
            case 0:
                if (mGroupChat) {
                    goToChatMenu.setVisible(true);
                    chatMenu.setVisible(false);
                } else {
                    goToChatMenu.setVisible(false);
                    chatMenu.setVisible(true);
                }
                addFriendMenu.setVisible(true);
                scanMenu.setVisible(true);
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                settingMenu.setVisible(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.add_friend:
                startActivity(new Intent(this, FriendAddFriendActivity.class));
                break;
            case R.id.group_chat:
                mGroupChat = true;
                mFriendFragment.setGroupSelect(true);
                break;
            case R.id.go_to_group_chat:
                startToGroupChat(mFriendFragment.didSelectFriend());
                mGroupChat = false;
                mFriendFragment.setGroupSelect(false);

                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.action_scan:
                startActivityForResult(new Intent(this, CaptureActivity.class), 0);
                break;
        }
        invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mGroupChat) {
            mGroupChat = false;
            mFriendFragment.setGroupSelect(false);
            invalidateOptionsMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_GET_FRIEND_LIST:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_SHOW_FRIEND, ShowFriendModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        ShowFriendModel friendModel;
        switch (requestId) {
            case Constants.RequestId.ID_GET_FRIEND_LIST:
                friendModel = (ShowFriendModel) response;
                if (friendModel != null && friendModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    mFriends = friendModel.getFriends();
                    mFriendFragment.setFriends(mFriends);
                } else {
                    Toast.makeText(this, R.string.request_data_fail, Toast.LENGTH_SHORT).show();
                    if (friendModel != null) {
                        L.e(friendModel.getResultMessage());
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String result = data.getExtras().getString("result");
            L.i(result);
            Intent intent = new Intent(this, FriendDetailActivity.class);
            intent.putExtra("friendId", Integer.parseInt(result));
            startActivity(intent);
            L.d(result);
        }
    }

    @Override
    public void onChatPreItemClick(int unread) {
        mChatBadgeView.decrementBadgeCount(unread);
    }

    private class FriendMainReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            L.d("receive broadcast");
            String action = intent.getAction();
            if (TextUtils.equals(action, FriendDetailActivity.ACTION_ADD_FRIEND)) {     //添加好友
                sendListFriendRequest();
            } else if (TextUtils.equals(action, Constants.Action.ACTION_RECEIVE_CHAT)) {  //收到聊天
                String data = intent.getStringExtra("data");
                ChatDao chatDao = null;
                try {
                    chatDao = ChatDao.json2ChatDao(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (mChatListFragment != null && chatDao != null) {
                    mChatListFragment.newMessageArrived(chatDao);
                    addChatTips();
                }
            }
        }
    }


    private class ReadRecentChatTask extends AsyncTask<Void, Void, List<FriendChatPreviewAdapter.ChatPreviewItem>> {

        String readRecentChaSql = "select * from chat_record_table t " +
                "where not exists (select 1 from chat_record_table " +
                "where ((fromUserId=t.fromUserId and toUserId=t.toUserId) " +
                "or(fromUserId=t.toUserId and toUserId=t.fromUserId)) " +
                "and id>t.id) and (fromUserId = ? or toUserId = ?) ";

        String unreadCountSql = "select count(*) from chat_record_table where ((fromUserId=? and toUserId=?) or (fromUserId=? and toUserId=?)) and read = 0";

        int count = 0;

        @Override
        protected List<FriendChatPreviewAdapter.ChatPreviewItem> doInBackground(Void... params) {
            String userId = DataManager.getInstance().getLoginUserId() + "";
            Cursor cursor = ActiveAndroid.getDatabase().rawQuery(readRecentChaSql, new String[]{userId, userId});
            List<ChatDao> chats = new ArrayList<>();
            while (cursor.moveToNext()) {
                ChatDao chatDao = new ChatDao();
                chatDao.fromUserId = cursor.getString(cursor.getColumnIndex("fromUserId"));
                chatDao.fromUserName = cursor.getString(cursor.getColumnIndex("fromUserName"));
                chatDao.message = cursor.getString(cursor.getColumnIndex("message"));
                chatDao.portrait = cursor.getString(cursor.getColumnIndex("portrait"));
                chatDao.read = cursor.getInt(cursor.getColumnIndex("read"));
                chatDao.time = cursor.getString(cursor.getColumnIndex("time"));
                chatDao.toUserId = cursor.getString(cursor.getColumnIndex("toUserId"));
                chatDao.toUserName = cursor.getString(cursor.getColumnIndex("toUserName"));
                chats.add(chatDao);
            }

            List<FriendChatPreviewAdapter.ChatPreviewItem> chatPreviewItems = new ArrayList<>();
            for (ChatDao chat :  chats){
                cursor = ActiveAndroid.getDatabase().rawQuery(unreadCountSql,new String[]{chat.fromUserId,chat.toUserId, chat.toUserId,chat.fromUserId});
                FriendChatPreviewAdapter.ChatPreviewItem item = new FriendChatPreviewAdapter.ChatPreviewItem();
                if (cursor.moveToNext()){
                    item.unReadCount = cursor.getInt(0);
                    count += item.unReadCount;
                }
                item.lastChatDao = chat;
                chatPreviewItems.add(item);
            }

            cursor.close();
            return chatPreviewItems;
        }

        @Override
        protected void onPostExecute(List<FriendChatPreviewAdapter.ChatPreviewItem> chatPreviewItems) {
            mChatListFragment.setData(chatPreviewItems);
            mChatBadgeView.setBadgeCount(count);
        }

    }
}

