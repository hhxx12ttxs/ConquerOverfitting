package com.pingpong.android.modules.friend;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.zxing.WriterException;
import com.pingpong.android.R;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.SingletonRequestQueue;
import com.pingpong.android.library.zxing.encode.EncodingHandler;
import com.pingpong.android.model.Friend;
import com.pingpong.android.utils.BitmapCache;
import com.pingpong.android.utils.UIUtils;
import com.pingpong.android.utils.Utils;


public class FriendDiscoveryFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private DiscoveryData[] discoveryDatas = {
            new DiscoveryData(R.drawable.icon_new_information, "最新球讯"),
            new DiscoveryData(R.drawable.icon_pingpong_game, "国球赛事"),
            new DiscoveryData(R.drawable.icon_invite, "约球"),
    };

    private FriendMainActivity mActivity;
    private ListView mDiscoveryList;
    private TextView mQRCodeFriendName;
    private ImageView mQRCodeImage;
    private TextView mFriendNameView;
    private TextView mFriendSignView;
    private NetworkImageView mFriendPortrait;

    private ImageLoader mImageLoader;

    public FriendDiscoveryFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discovery, container, false);
        mDiscoveryList = (ListView) view.findViewById(R.id.lv_discovery_list);
        mDiscoveryList.setAdapter(new DiscoveryAdapter());
        mDiscoveryList.setOnItemClickListener(this);
        mFriendNameView = (TextView) view.findViewById(R.id.tv_discovery_nick_name);
        mFriendSignView = (TextView) view.findViewById(R.id.tv_friend_sign);
        mFriendPortrait = (NetworkImageView) view.findViewById(R.id.iv_discovery_profile);
        mImageLoader = new ImageLoader(SingletonRequestQueue.getInstance(getActivity()).getRequestQueue(), BitmapCache.getInstance());
        view.findViewById(R.id.rl_my_card).setOnClickListener(this);
        view.findViewById(R.id.iv_my_qr_code).setOnClickListener(this);
        updateUI();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FriendMainActivity) activity;
    }

    private void showMyQRCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.show_qr_code_layout, null);
        mQRCodeImage = (ImageView) view.findViewById(R.id.iv_qr_code_image);
        mQRCodeFriendName = (TextView) view.findViewById(R.id.tv_friend_name);
        mQRCodeFriendName.setText(DataManager.getInstance().getLoginFriend().getFriendName());
        try {
            Bitmap qrCode = EncodingHandler.createQRCode(DataManager.getInstance().getLoginUserId() + "", Utils.dip2px(getActivity(), 250));
            mQRCodeImage.setImageBitmap(qrCode);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        builder.setView(view);
        builder.create().show();
    }

    public void updateUI() {
        Friend friend = DataManager.getInstance().getLoginFriend();
        if (mFriendNameView == null || mFriendSignView == null) return;
        mFriendNameView.setText(friend.getFriendName());
        mFriendSignView.setText(friend.getFriendSign());
        mFriendPortrait.setImageUrl(friend.getFriendProfile(),mImageLoader);
        mFriendPortrait.setDefaultImageResId(R.drawable.pingpong_icon);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                break;
            case 1:
                startActivity(new Intent(getActivity(), FriendGameActivity.class));
                break;
            case 2:
                UIUtils.showSelectorDialog(getActivity(), new String[]{"发起约球", "应约约球"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        switch (which) {
                            case 0:
                                intent.setClass(getActivity(), FriendInviteActivity.class);
                                break;
                            case 1:
                                intent.setClass(getActivity(), FriendReplyInviteActivity.class);
                                break;
                        }
                        startActivity(intent);
                    }
                });
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_my_card:
                Intent intent = new Intent(getActivity(), FriendDetailActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_my_qr_code:
                showMyQRCode();
                break;
        }
    }

    private class DiscoveryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return discoveryDatas.length;
        }

        @Override
        public Object getItem(int position) {
            return discoveryDatas[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.discovery_item_layout, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            DiscoveryData data = (DiscoveryData) getItem(position);
            viewHolder.mDiscoveryName.setCompoundDrawablesWithIntrinsicBounds(data.discoveryIconRes, 0, 0, 0);
            viewHolder.mDiscoveryName.setText(data.discoveryName);
            return convertView;
        }

        private class ViewHolder {
            public TextView mDiscoveryName;

            public ViewHolder(View view) {
                mDiscoveryName = (TextView) view.findViewById(R.id.tv_discovery_item_name);
            }
        }
    }


    private class DiscoveryData {
        public int discoveryIconRes;
        public String discoveryName;

        public DiscoveryData() {

        }

        public DiscoveryData(int res, String name) {
            discoveryIconRes = res;
            discoveryName = name;
        }

    }
}

