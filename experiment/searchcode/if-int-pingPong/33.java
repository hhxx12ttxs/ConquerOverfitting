package com.pingpong.android.modules.friend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.db.ChatDao;
import com.pingpong.android.db.DataBaseHelper;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;

import org.json.JSONException;

public class FriendChatActivity extends NetBaseActivity implements View.OnClickListener {

    private ListView mChatListView;
    private ImageView mExpressionView;
    private Button mSendBtn;
    private EditText mInputView;

    private String mUserId;
    private String mUserName;
    private String mToSendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_chat);

        mUserId = getIntent().getStringExtra("userId");
        mUserName = getIntent().getStringExtra("userName");
        initView();
        setActionBarTitle();
        readHistory();
        registerReceiver();
        DataBaseHelper.setAllMessageReaded(mUserId,DataManager.getInstance().getLoginUserId()+"");
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.getInstance().setFrontChat(mUserId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DataManager.getInstance().setFrontChat("-1");
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mChatReceiver);
    }

    private void initView() {
        mExpressionView = (ImageView) findViewById(R.id.iv_expression);
        mSendBtn = (Button) findViewById(R.id.btn_send);
        mSendBtn.setOnClickListener(this);
        mSendBtn.setEnabled(false);
        mInputView = (EditText) findViewById(R.id.et_input);
        mInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String message = mInputView.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    mSendBtn.setEnabled(false);
                } else {
                    mSendBtn.setEnabled(true);
                }
            }
        });
        mChatListView = (ListView) findViewById(R.id.lv_chat_list);
        ChatRecordAdapter adapter = new ChatRecordAdapter(this);
        if (mUserId.contains(Constants.LINK_CHAR)){
            adapter.setIsGroupChat(true);
        }
        mChatListView.setAdapter(adapter);
    }

    private void setActionBarTitle() {
        if (TextUtils.isEmpty(mUserName)) return;
        mActionBar.setTitle(mUserName);
    }

    private void readHistory() {
        ChatRecordAdapter adapter = new ChatRecordAdapter(this);
        mChatListView.setAdapter(adapter);
        adapter.setChatItem(DataBaseHelper.readChat(mUserId));
        scrollListViewToBottom();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Action.ACTION_UPDATE_CHAT);
        registerReceiver(mChatReceiver, filter);
    }

    private void sendMessage() {
        mToSendMessage = mInputView.getText().toString();
        mInputView.setText("");
        sendHttpRequest(Constants.RequestId.ID_CHAT);
        ChatRecordAdapter adapter = (ChatRecordAdapter) mChatListView.getAdapter();
        ChatDao item = new ChatDao();
        item.fromUserId = DataManager.getInstance().getLoginUserId() + "";
        item.fromUserName = DataManager.getInstance().getLoginFriend().getFriendName();
        item.toUserId = mUserId;
        item.toUserName = mUserName;
        item.message = mToSendMessage;
        item.read = Constants.FLAG_READED;
        item.time = System.currentTimeMillis() + "";
        adapter.addChatItem(item);
        DataBaseHelper.saveChatRecord(item);
        scrollListViewToBottom();
    }

    private void scrollListViewToBottom() {
        final ChatRecordAdapter adapter = (ChatRecordAdapter) mChatListView.getAdapter();
        mChatListView.post(new Runnable() {
            @Override
            public void run() {
                mChatListView.setSelection(adapter.getCount() - 1);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                sendMessage();
                break;
        }
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_CHAT:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_CHAT, BaseModel.class);
                hrp.addParam("fromUserId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("toUserId", mUserId);
                hrp.addParam("fromUserName", DataManager.getInstance().getLoginFriend().getFriendName());
                hrp.addParam("toUserName", mUserName);
                hrp.addParam("message", mToSendMessage);
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_CHAT:
                if (response != null && response.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    L.d("发送成功");
                } else {
                    L.d("发送失败");
                    Toast.makeText(this, "发送失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public BroadcastReceiver mChatReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("data");
            ChatRecordAdapter adapter = (ChatRecordAdapter) mChatListView.getAdapter();
            ChatDao chatDao = null;
            try {
                chatDao = ChatDao.json2ChatDao(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (chatDao != null && TextUtils.equals(chatDao.fromUserId, mUserId)) {
                adapter.addChatItem(chatDao);
            }

            scrollListViewToBottom();
        }
    };


}

