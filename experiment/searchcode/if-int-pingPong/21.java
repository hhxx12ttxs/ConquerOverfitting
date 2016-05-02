package com.pingpong.android.modules.friend;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.pingpong.android.R;
import com.pingpong.android.base.BaseModel;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.HttpRequestParam;
import com.pingpong.android.base.NetBaseActivity;
import com.pingpong.android.base.SingletonRequestQueue;
import com.pingpong.android.common.ImageUploadUtils;
import com.pingpong.android.interfaces.OnAgeSelectedListener;
import com.pingpong.android.model.Friend;
import com.pingpong.android.model.ShowFriendModel;
import com.pingpong.android.utils.BitmapCache;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;
import com.pingpong.android.utils.UIUtils;
import com.pingpong.android.utils.Utils;

import org.apache.http.Header;

import java.util.List;

public class FriendDetailActivity extends NetBaseActivity implements View.OnClickListener {

    private static final int REQUEST_CHOOSE_IMAGE = 123;
    private static final int REQUEST_MODIFY = 124;

    public static final String ACTION_ADD_FRIEND = "com.pingpong.android.action.ADD_FRIEND";

    private NetworkImageView mProfileImage;
    private TextView mNickNameView;
    private TextView mFriendNumView;
    private TextView mInviteNumView;
    private TextView mLikeNumView;
    private TextView mHallNumView;
    private TextView mPersonSignView;
    private TextView mBallYearView;
    private TextView mAddressView;
    private TextView mJobView;
    private RecyclerView mAlbumsView;
    private TextView mExperiencesView;
    private Button mAssociateBtn;
    private Button mChatBtn;
    private TextView mDoLikeView;

    private ImageLoader mImageLoader;
    private Friend mFriend;
    private String mAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);
        initView();
        getExtra();
    }

    private void initView() {
        mProfileImage = (NetworkImageView) findViewById(R.id.iv_friend_detail_profile);
        mNickNameView = (TextView) findViewById(R.id.tv_friend_detail_nick_name);
        mFriendNumView = (TextView) findViewById(R.id.tv_friend_detail_friend_num);
        mInviteNumView = (TextView) findViewById(R.id.tv_friend_detail_invite_num);
        mLikeNumView = (TextView) findViewById(R.id.tv_friend_detail_like_num);
        mHallNumView = (TextView) findViewById(R.id.tv_friend_detail_hall_num);
        mPersonSignView = (TextView) findViewById(R.id.tv_friend_detail_sign);
        mBallYearView = (TextView) findViewById(R.id.tv_friend_detail_ball_year);
        mAddressView = (TextView) findViewById(R.id.tv_friend_detail_address);
        mJobView = (TextView) findViewById(R.id.tv_friend_detail_job);
        mAlbumsView = (RecyclerView) findViewById(R.id.rl_friend_detail_albums);
        mExperiencesView = (TextView) findViewById(R.id.tv_friend_detail_experience);
        mAssociateBtn = (Button) findViewById(R.id.btn_add_friend);
        mChatBtn = (Button) findViewById(R.id.btn_chat);
        mDoLikeView = (TextView) findViewById(R.id.tv_friend_detail_do_like);
        mAlbumsView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    Intent intent = new Intent(FriendDetailActivity.this,FriendAlbumActivity.class);
                    intent.putExtra("friend",mFriend);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        mAssociateBtn.setOnClickListener(this);
        mChatBtn.setOnClickListener(this);

        mImageLoader = new ImageLoader(SingletonRequestQueue.getInstance(this).getRequestQueue(), BitmapCache.getInstance());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mAlbumsView.setLayoutManager(layoutManager);
    }


    /**
     * 得到其他Activity传进的数据<br/>
     * 优先级为：<br/>
     * 1. Friend实例 <br/>
     * 2. FriendId <br/>
     * 3. 无 <br/>
     */
    private void getExtra() {
        Friend friend = getIntent().getParcelableExtra("friend");
        if (friend == null) {
            int friendId = getIntent().getIntExtra("friendId", -1);
            if (friendId == -1) {
                updateUI(DataManager.getInstance().getLoginFriend());  // 显示登录球馆
            } else {
                mFriend = new Friend();
                mFriend.setFriendId(friendId);
                sendHttpRequest(Constants.RequestId.ID_SHOW_FRIEND, R.string.common_loading_progress);
            }
        } else {
            updateUI(friend);
        }

        if (DataManager.getInstance().getLoginUserId() == mFriend.getFriendId()) {
            mBallYearView.setOnClickListener(this);
            mProfileImage.setOnClickListener(this);
            mAddressView.setOnClickListener(this);
            mJobView.setOnClickListener(this);
            mExperiencesView.setOnClickListener(this);
            mPersonSignView.setOnClickListener(this);
        }

        mAlbumsView.setAdapter(new ThumbnailImageAdapter());

    }

    private void updateUI(Friend friend) {
        if (friend == null) return;
        mFriend = friend;
        mNickNameView.setText(mFriend.getFriendName() + " (ID: " + mFriend.getFriendId() + ")");
        mFriendNumView.setText("球友 " + mFriend.getFriendFans());
        mInviteNumView.setText("约球 " + mFriend.getFriendPlay());
        mLikeNumView.setText("膜拜 " + mFriend.getFriendLike());
        mHallNumView.setText("关联球馆 " + mFriend.getFriendHall());
        mPersonSignView.setText(mFriend.getFriendSign());
        mBallYearView.setText(Utils.formatAge(mFriend.getFriendAge()));
        mAddressView.setText(mFriend.getFriendAddress());
        mJobView.setText(mFriend.getFriendJob());
        mExperiencesView.setText(mFriend.getFriendExperiences());
        mProfileImage.setImageUrl(friend.getFriendProfile(), mImageLoader);
        mProfileImage.setDefaultImageResId(R.drawable.pingpong_icon);

        if (DataManager.getInstance().getLoginUserId() == mFriend.getFriendId()) {  //自己
            mChatBtn.setVisibility(View.GONE);
            mAssociateBtn.setVisibility(View.GONE);
            mDoLikeView.setVisibility(View.GONE);
        } else if (mFriend.getIsFriend() == Constants.FLAG_IS_FRIEND) {
            mChatBtn.setVisibility(View.VISIBLE);
            mAssociateBtn.setVisibility(View.GONE);
        } else {
            mChatBtn.setVisibility(View.GONE);
            mAssociateBtn.setVisibility(View.VISIBLE);
        }
    }

    private void redirectToSelect() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
    }

    private void uploadImage(String path) {
        ImageUploadUtils.uploadPortrait(this, path, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                L.d(new String(bytes));
                String portraitUrl = Constants.RequestUrl.URL_PORTRAIT + "/" + mFriend.getFriendId() + ".jpg";
                mFriend.setFriendProfile(portraitUrl);
                BitmapCache.getInstance().deleteBitmap(portraitUrl);
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                L.d(throwable.getMessage());
            }
        });
    }

    private void updateInformation(int flag, String data) {
        if (TextUtils.isEmpty(data)) return;
        switch (flag) {
            case FriendDetailModifyActivity.FLAG_ADDRESS:
                mAddressView.setText(data);
                sendHttpRequest(Constants.RequestId.ID_UPDATE_FRIEND_ADDRESS);
                break;
            case FriendDetailModifyActivity.FLAG_EXPERIENCE:
                mExperiencesView.setText(data);
                sendHttpRequest(Constants.RequestId.ID_UPDATE_FRIEND_EXPERIENCE);
                break;
            case FriendDetailModifyActivity.FLAG_JOB:
                mJobView.setText(data);
                sendHttpRequest(Constants.RequestId.ID_UPDATE_FRIEND_JOB);
                break;
            case FriendDetailModifyActivity.FLAG_SIGN:
                mPersonSignView.setText(data);
                sendHttpRequest(Constants.RequestId.ID_UPDATE_FRIEND_SIGN);
                break;

        }
    }

    private void sendAddFriendBroadcast() {
        Intent intent = new Intent(ACTION_ADD_FRIEND);
        sendBroadcast(intent);
    }

    @Override
    public HttpRequestParam makeParam(int requestId) {
        HttpRequestParam hrp = null;
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_FRIEND:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_SHOW_FRIEND, ShowFriendModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("userName", mFriend.getFriendId() + "");
                break;
            case Constants.RequestId.ID_ADD_FRIEND:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_ADD_FRIEND, BaseModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("userName", mFriend.getFriendId() + "");
                break;
            case Constants.RequestId.ID_UPDATE_FRIEND_AGE:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_UPDATE_FRIEND, BaseModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("friendAge", mAge);
                break;
            case Constants.RequestId.ID_UPDATE_FRIEND_SIGN:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_UPDATE_FRIEND, BaseModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("friendSign", mPersonSignView.getText().toString());
                break;
            case Constants.RequestId.ID_UPDATE_FRIEND_ADDRESS:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_UPDATE_FRIEND, BaseModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("friendAddress", mAddressView.getText().toString());
                break;
            case Constants.RequestId.ID_UPDATE_FRIEND_JOB:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_UPDATE_FRIEND, BaseModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("friendJob", mJobView.getText().toString());
                break;
            case Constants.RequestId.ID_UPDATE_FRIEND_EXPERIENCE:
                hrp = new HttpRequestParam(Constants.RequestUrl.URL_UPDATE_FRIEND, BaseModel.class);
                hrp.addParam("userId", DataManager.getInstance().getLoginUserId() + "");
                hrp.addParam("friendExperiences", mExperiencesView.getText().toString());
                break;
        }
        return hrp;
    }

    @Override
    public void dataReceived(int requestId, BaseModel response) {
        switch (requestId) {
            case Constants.RequestId.ID_SHOW_FRIEND:
                ShowFriendModel friendModel = (ShowFriendModel) response;
                if (friendModel != null && friendModel.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    if (friendModel.getFriends() != null && friendModel.getFriends().size() != 0) {
                        updateUI(friendModel.getFriends().get(0));
                    }
                } else {
                    Toast.makeText(this, R.string.request_data_fail, Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.RequestId.ID_ADD_FRIEND:
                if (response != null && response.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    mFriend.setIsFriend(Constants.FLAG_IS_FRIEND);
                    mFriend.setFriendFans(mFriend.getFriendFans() + 1);
                    updateUI(mFriend);
                    sendAddFriendBroadcast();
                } else {
                    Toast.makeText(this, R.string.associate_fail, Toast.LENGTH_SHORT).show();
                    if (response != null) {
                        L.e(response.getResultMessage());
                    }
                }
                break;
            case Constants.RequestId.ID_UPDATE_FRIEND_AGE:
                if (response != null && response.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    Toast.makeText(this, R.string.update_friend_age_success, Toast.LENGTH_SHORT).show();
                    mFriend.setFriendAge(Integer.parseInt(mAge));
                } else {
                    Toast.makeText(this, R.string.update_friend_fail, Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.RequestId.ID_UPDATE_FRIEND_SIGN:
                if (response != null && response.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    Toast.makeText(this, R.string.update_friend_sign_success, Toast.LENGTH_SHORT).show();
                    mFriend.setFriendSign(mPersonSignView.getText().toString());
                } else {
                    Toast.makeText(this, R.string.update_friend_fail, Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.RequestId.ID_UPDATE_FRIEND_ADDRESS:
                if (response != null && response.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    Toast.makeText(this, R.string.update_friend_address_success, Toast.LENGTH_SHORT).show();
                    mFriend.setFriendAddress(mAddressView.getText().toString());
                } else {
                    Toast.makeText(this, R.string.update_friend_fail, Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.RequestId.ID_UPDATE_FRIEND_JOB:
                if (response != null && response.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    Toast.makeText(this, R.string.update_friend_job_success, Toast.LENGTH_SHORT).show();
                    mFriend.setFriendJob(mJobView.getText().toString());
                } else {
                    Toast.makeText(this, R.string.update_friend_fail, Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.RequestId.ID_UPDATE_FRIEND_EXPERIENCE:
                if (response != null && response.getResultCode() == Constants.ResultCode.RESULT_OK) {
                    Toast.makeText(this, R.string.update_friend_experience_success, Toast.LENGTH_SHORT).show();
                    mFriend.setFriendExperiences(mExperiencesView.getText().toString());
                } else {
                    Toast.makeText(this, R.string.update_friend_fail, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.iv_friend_detail_profile:
                redirectToSelect();
                break;
            case R.id.btn_chat:
                intent = new Intent(this, FriendChatActivity.class);
                intent.putExtra("userId", mFriend.getFriendId() + "");
                intent.putExtra("userName", mFriend.getFriendName());
                startActivity(intent);
                break;
            case R.id.btn_add_friend:
                sendHttpRequest(Constants.RequestId.ID_ADD_FRIEND, R.string.common_loading_progress);
                break;
            case R.id.tv_friend_detail_ball_year:
                UIUtils.showAgeSelector(this, new OnAgeSelectedListener() {
                    @Override
                    public void onAgeSelected(int type, int number) {
                        String typeStr;
                        if (type == 0) {
                            typeStr = "月";
                            mAge = number + "";
                        } else {
                            typeStr = "年";
                            mAge = (number * 12) + "";
                        }
                        mBallYearView.setText(number + " " + typeStr);
                        sendHttpRequest(Constants.RequestId.ID_UPDATE_FRIEND_AGE);
                    }
                });
                break;
            case R.id.tv_friend_detail_sign:
                intent = new Intent(this, FriendDetailModifyActivity.class);
                intent.putExtra("flag", FriendDetailModifyActivity.FLAG_SIGN);
                startActivityForResult(intent, REQUEST_MODIFY);
                break;
            case R.id.tv_friend_detail_address:
                intent = new Intent(this, FriendDetailModifyActivity.class);
                intent.putExtra("flag", FriendDetailModifyActivity.FLAG_ADDRESS);
                startActivityForResult(intent, REQUEST_MODIFY);
                break;
            case R.id.tv_friend_detail_experience:
                intent = new Intent(this, FriendDetailModifyActivity.class);
                intent.putExtra("flag", FriendDetailModifyActivity.FLAG_EXPERIENCE);
                startActivityForResult(intent, REQUEST_MODIFY);
                break;
            case R.id.tv_friend_detail_job:
                intent = new Intent(this, FriendDetailModifyActivity.class);
                intent.putExtra("flag", FriendDetailModifyActivity.FLAG_JOB);
                startActivityForResult(intent, REQUEST_MODIFY);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CHOOSE_IMAGE:
                    Uri uri = data.getData();
                    if (uri != null) {
                        String path = Utils.getFilePathByContentResolver(this, uri);
                        if (!TextUtils.isEmpty(path)) {
                            uploadImage(path);
                        }
                    }
                    break;
                case REQUEST_MODIFY:
                    int flag = data.getIntExtra("flag", -1);
                    String str = data.getStringExtra("data");
                    updateInformation(flag, str);
                    break;
            }
        }
    }

    private class ThumbnailImageAdapter extends RecyclerView.Adapter<ThumbnailImageHolder> {

        private List<String> albums;
        private ImageLoader imageLoader;

        public ThumbnailImageAdapter() {
            albums = mFriend.getAlbums();
            imageLoader = new ImageLoader(SingletonRequestQueue.getInstance(FriendDetailActivity.this).getRequestQueue(),BitmapCache.getInstance());
        }

        @Override
        public ThumbnailImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(FriendDetailActivity.this).inflate(R.layout.friend_detail_album_thumbnail_item, parent, false);
            return new ThumbnailImageHolder(view);
        }

        @Override
        public void onBindViewHolder(ThumbnailImageHolder holder, int position) {
            String albumUrl = albums.get(position);
            holder.networkImageView.setDefaultImageResId(R.drawable.pingpong_icon);
            holder.networkImageView.setImageUrl(albumUrl,imageLoader);
        }

        @Override
        public int getItemCount() {
            if (albums == null || albums.size() == 0){
                return 0;
            } else if (albums.size() > 4){
                return 4;
            } else {
                return albums.size();
            }
        }
    }


    private class ThumbnailImageHolder extends RecyclerView.ViewHolder {

        public NetworkImageView networkImageView;

        public ThumbnailImageHolder(View itemView) {
            super(itemView);
            networkImageView = (NetworkImageView) itemView.findViewById(R.id.network_image_view);
        }
    }
}

