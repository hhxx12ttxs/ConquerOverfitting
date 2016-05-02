package com.pingpong.android.modules.friend;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.pingpong.android.R;
import com.pingpong.android.base.SingletonRequestQueue;
import com.pingpong.android.model.Friend;
import com.pingpong.android.utils.BitmapCache;
import com.pingpong.android.utils.L;

import java.util.HashSet;
import java.util.List;

public class FriendFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView mFriendList;
    private List<Friend> mMyFriends;

    private HashSet<Friend> mGroupChatFriends;

    private boolean mIsSelectGroup;

    public FriendFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        mFriendList = (ListView) view.findViewById(R.id.rv_friend_list);
        mFriendList.setAdapter(new FriendListAdapter());
        mFriendList.setOnItemClickListener(this);
        mGroupChatFriends = new HashSet<>();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void setFriends(List<Friend> friends) {
        mMyFriends = friends;
        if (mFriendList == null) return;
        ((FriendListAdapter) mFriendList.getAdapter()).notifyDataSetChanged();
    }

    public void setGroupSelect(boolean b) {
        mIsSelectGroup = b;
        ((FriendListAdapter) mFriendList.getAdapter()).notifyDataSetChanged();
        mGroupChatFriends.clear();
    }

    public HashSet<Friend> didSelectFriend() {
        return mGroupChatFriends;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), FriendDetailActivity.class);
        intent.putExtra("friend", mMyFriends.get(position));
        startActivity(intent);
    }


    private class FriendListAdapter extends BaseAdapter {

        private ImageLoader mImageLoader;

        public FriendListAdapter() {
            mImageLoader = new ImageLoader(SingletonRequestQueue.getInstance(getActivity()).getRequestQueue(), BitmapCache.getInstance());
        }

        @Override
        public int getCount() {
            return mMyFriends == null ? 0 : mMyFriends.size();
        }

        @Override
        public Object getItem(int position) {
            return mMyFriends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.friend_list_item, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Friend friend = (Friend) getItem(position);
            viewHolder.mNickName.setText(friend.getFriendName());
            viewHolder.mPersonSign.setText(friend.getFriendSign());
            viewHolder.mProfile.setImageUrl(friend.getFriendProfile(), mImageLoader);
            viewHolder.mProfile.setDefaultImageResId(R.drawable.friend_default_profile);
            viewHolder.mCheckBox.setOnCheckedChangeListener(new CheckedChangeListener());
            viewHolder.mCheckBox.setTag(position);
            if (mIsSelectGroup) {
                viewHolder.mCheckBox.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mCheckBox.setVisibility(View.GONE);
            }
            if (mGroupChatFriends.contains(mMyFriends.get(position))) {
                viewHolder.mCheckBox.setChecked(true);
            } else {
                viewHolder.mCheckBox.setChecked(false);
            }

            return convertView;
        }

        private class ViewHolder {
            public NetworkImageView mProfile;
            public TextView mNickName;
            public TextView mPersonSign;
            public CheckBox mCheckBox;

            public ViewHolder(View view) {
                mProfile = (NetworkImageView) view.findViewById(R.id.iv_friend_item_profile);
                mNickName = (TextView) view.findViewById(R.id.tv_friend_item_nick_name);
                mPersonSign = (TextView) view.findViewById(R.id.tv_friend_item_sign);
                mCheckBox = (CheckBox) view.findViewById(R.id.cb_group_chat_select);
                mCheckBox.setOnCheckedChangeListener(new CheckedChangeListener());
            }
        }

        private class CheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                L.d("check change");
                if (mGroupChatFriends == null) {
                    mGroupChatFriends = new HashSet<>();
                }
                int tag = Integer.parseInt(buttonView.getTag().toString());
                if (isChecked) {
                    mGroupChatFriends.add(mMyFriends.get(tag));
                } else {
                    mGroupChatFriends.remove(mMyFriends.get(tag));
                }
            }
        }

    }

}

