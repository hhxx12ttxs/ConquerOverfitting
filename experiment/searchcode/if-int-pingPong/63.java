package com.pingpong.android.modules.friend;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.pingpong.android.R;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.SingletonRequestQueue;
import com.pingpong.android.db.ChatDao;
import com.pingpong.android.utils.BitmapCache;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天记录适配器
 */
public class ChatRecordAdapter extends BaseAdapter {

    private Context mContext;
    private List<ChatDao> mChatItems;
    private boolean isGroupChat;
    private ImageLoader mImageLoader;

    public ChatRecordAdapter(Context context) {
        mContext = context;
        isGroupChat = false;
        mImageLoader = new ImageLoader(SingletonRequestQueue.getInstance(context).getRequestQueue(), BitmapCache.getInstance());
    }

    public void setChatItem(List<ChatDao> chatItems) {
        mChatItems = chatItems;
        notifyDataSetChanged();
    }

    public void addChatItem(ChatDao chatItem) {
        if (mChatItems == null) {
            mChatItems = new ArrayList<>();
        }
        mChatItems.add(chatItem);
        notifyDataSetChanged();
    }

    public void setIsGroupChat(boolean b){
        isGroupChat = b;
    }

    @Override
    public int getCount() {
        return mChatItems == null ? 0 : mChatItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mChatItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_msg_list_item, parent, false);
            holder = new ViewHolder();
            holder.myChatLayout = (RelativeLayout) convertView.findViewById(R.id.rl_my_chat_layout);
            holder.myPortraitView = (NetworkImageView) convertView.findViewById(R.id.iv_my_portrait);
            holder.myMessageView = (TextView) convertView.findViewById(R.id.tv_my_chat_msg);
            holder.hisChatLayout = (RelativeLayout) convertView.findViewById(R.id.rl_his_chat_layout);
            holder.hisMessageView = (TextView) convertView.findViewById(R.id.tv_his_chat_msg);
            holder.hisPortraitView = (NetworkImageView) convertView.findViewById(R.id.iv_his_portrait);
            holder.hisName = (TextView) convertView.findViewById(R.id.tv_his_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ChatDao item = mChatItems.get(position);
        if (TextUtils.equals(item.fromUserId, DataManager.getInstance().getLoginUserId() + "")) {  //我发的信息
            holder.myChatLayout.setVisibility(View.VISIBLE);
            holder.hisChatLayout.setVisibility(View.INVISIBLE);
            holder.myMessageView.setText(item.message);
            holder.myPortraitView.setImageUrl(DataManager.getInstance().getLoginFriend().getFriendProfile(),mImageLoader);
            holder.myPortraitView.setDefaultImageResId(R.drawable.pingpong_icon);
        } else {  // 我收到的信息
            holder.hisChatLayout.setVisibility(View.VISIBLE);
            holder.myChatLayout.setVisibility(View.INVISIBLE);
            holder.hisMessageView.setText(item.message);
            // TODO : 这里显示姓名还有问题。
            if (isGroupChat){
                holder.hisName.setVisibility(View.VISIBLE);
                holder.hisName.setText(item.fromUserName);
            }else {
                holder.hisName.setVisibility(View.GONE);
            }
            holder.hisPortraitView.setImageUrl(item.portrait,mImageLoader);
            holder.hisPortraitView.setDefaultImageResId(R.drawable.pingpong_icon);
        }
        return convertView;
    }

    private class ViewHolder {
        RelativeLayout myChatLayout;
        RelativeLayout hisChatLayout;
        NetworkImageView myPortraitView;
        NetworkImageView hisPortraitView;
        TextView hisName;
        TextView myMessageView;
        TextView hisMessageView;
    }

}

