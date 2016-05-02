package com.pingpong.android.modules.friend;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pingpong.android.R;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.db.ChatDao;
import com.pingpong.android.interfaces.OnChatPreItemClickListener;

import java.util.List;

public class FriendChatPreviewListFragment extends Fragment {

    private ListView mChatPreviewListView;
    private OnChatPreItemClickListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_chat_list, container, false);
        mChatPreviewListView = (ListView) view.findViewById(R.id.lv_chat_preview_list);
        mChatPreviewListView.setAdapter(new FriendChatPreviewAdapter(getActivity()));
        mChatPreviewListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FriendChatPreviewAdapter adapter = (FriendChatPreviewAdapter) mChatPreviewListView.getAdapter();
                FriendChatPreviewAdapter.ChatPreviewItem item = adapter.getItemAtPosition(position);
                mListener.onChatPreItemClick(item.unReadCount);
                item.unReadCount = 0;
                Intent intent = new Intent(getActivity(), FriendChatActivity.class);
                String userId , userName;
                if (TextUtils.equals(item.lastChatDao.fromUserId, DataManager.getInstance().getLoginUserId()+"")){
                    userId = item.lastChatDao.toUserId;
                    userName = item.lastChatDao.toUserName;
                }else {
                    userId = item.lastChatDao.fromUserId;
                    userName = item.lastChatDao.fromUserName;
                }
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                startActivity(intent);
                adapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnChatPreItemClickListener) activity;
        }catch (ClassCastException e){
            throw new ClassCastException("must implements OnChatPreItemClickListener");
        }
    }

    public void setData(List<FriendChatPreviewAdapter.ChatPreviewItem> items) {
        if (mChatPreviewListView != null) {
            FriendChatPreviewAdapter adapter = (FriendChatPreviewAdapter) mChatPreviewListView.getAdapter();
            adapter.setData(items);
            adapter.notifyDataSetChanged();
        }
    }

    public void newMessageArrived(ChatDao chatDao) {
        if (mChatPreviewListView != null) {
            FriendChatPreviewAdapter adapter = (FriendChatPreviewAdapter) mChatPreviewListView.getAdapter();
            adapter.addUnreadChatDao(chatDao);
            adapter.notifyDataSetChanged();
        }
    }

    public void addChatPreview(ChatDao item){
        if (mChatPreviewListView != null) {
            FriendChatPreviewAdapter adapter = (FriendChatPreviewAdapter) mChatPreviewListView.getAdapter();
            adapter.addReadChatDao(item);
            adapter.notifyDataSetChanged();
        }
    }

}

