package com.pingpong.android.modules.friend;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.jauker.widget.BadgeView;
import com.pingpong.android.R;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.SingletonRequestQueue;
import com.pingpong.android.db.ChatDao;
import com.pingpong.android.utils.BitmapCache;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天预览适配器
 */
public class FriendChatPreviewAdapter extends BaseAdapter {

    private List<ChatPreviewItem> chatPreviewItems;
    private Context context;
    private ImageLoader mImageLoader;

    public FriendChatPreviewAdapter(Context context) {
        this.context = context;
        mImageLoader = new ImageLoader(SingletonRequestQueue.getInstance(context).getRequestQueue(), BitmapCache.getInstance());
    }

    /**
     * 增加一条未读的聊天
     *
     * @param dao 聊天
     */
    public void addUnreadChatDao(ChatDao dao) {
        if (chatPreviewItems == null) {
            chatPreviewItems = new ArrayList<>();
            ChatPreviewItem preItem = new ChatPreviewItem();
            preItem.lastChatDao = dao;
            preItem.unReadCount = 1;
            chatPreviewItems.add(preItem);
        } else {
            boolean isNew = true;
            for (ChatPreviewItem preItem : chatPreviewItems) {
                ChatDao item1 = preItem.lastChatDao;
                String readUserId;
                if (TextUtils.equals(item1.fromUserId, DataManager.getInstance().getLoginUserId() + "")) {
                    readUserId = item1.toUserId;
                } else {
                    readUserId = item1.fromUserId;
                }
                if (TextUtils.equals(readUserId, dao.fromUserId)) {
                    isNew = false;
                    preItem.lastChatDao = dao;
                    preItem.unReadCount++;
                    break;
                }
            }
            if (isNew) {
                ChatPreviewItem preItem = new ChatPreviewItem();
                preItem.lastChatDao = dao;
                preItem.unReadCount = 1;
                chatPreviewItems.add(preItem);
            }
        }
    }

    /**
     * 增加一条已读聊天
     *
     * @param dao 聊天
     */
    public void addReadChatDao(ChatDao dao) {

        if (chatPreviewItems == null) {
            chatPreviewItems = new ArrayList<>();
            ChatPreviewItem preItem = new ChatPreviewItem();
            preItem.lastChatDao = dao;
            preItem.unReadCount = 0;
            chatPreviewItems.add(preItem);
        } else {
            boolean isNew = true;
            for (ChatPreviewItem preItem : chatPreviewItems) {
                ChatDao item1 = preItem.lastChatDao;
                String readUserId;
                if (TextUtils.equals(item1.fromUserId, DataManager.getInstance().getLoginUserId() + "")) {
                    readUserId = item1.toUserId;
                } else {
                    readUserId = item1.fromUserId;
                }

                if (TextUtils.equals(readUserId, dao.toUserId)) {
                    isNew = false;
                    preItem.lastChatDao = dao;
                    preItem.unReadCount = 0;
                    break;
                }
            }
            if (isNew) {
                ChatPreviewItem preItem = new ChatPreviewItem();
                preItem.lastChatDao = dao;
                preItem.unReadCount = 0;
                chatPreviewItems.add(preItem);
            }
        }

    }

    public void setData(List<ChatPreviewItem> items) {
        chatPreviewItems = items;
    }

    public List<ChatPreviewItem> getAllData() {
        return chatPreviewItems;
    }

    public ChatPreviewItem getItemAtPosition(int position) {
        if (chatPreviewItems == null) return null;
        return chatPreviewItems.get(position);
    }

    @Override
    public int getCount() {
        return chatPreviewItems == null ? 0 : chatPreviewItems.size();
    }

    @Override
    public Object getItem(int position) {
        return chatPreviewItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.friend_chat_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ChatPreviewItem item = chatPreviewItems.get(position);

        if (TextUtils.equals(item.lastChatDao.fromUserId, DataManager.getInstance().getLoginUserId() + "")) {
            viewHolder.mNickName.setText(item.lastChatDao.toUserName);
        } else {
            viewHolder.mNickName.setText(item.lastChatDao.fromUserName);
        }

        viewHolder.mLastMessage.setText(item.lastChatDao.message);
        viewHolder.mBadgeView.setText(item.unReadCount + "");
        viewHolder.mProfile.setImageUrl(item.lastChatDao.portrait, mImageLoader);
        viewHolder.mProfile.setDefaultImageResId(R.drawable.pingpong_icon);
        return convertView;
    }

    private class ViewHolder {
        public NetworkImageView mProfile;
        public TextView mNickName;
        public TextView mLastMessage;
        public BadgeView mBadgeView;

        public ViewHolder(View view) {
            mProfile = (NetworkImageView) view.findViewById(R.id.iv_chat_item_profile);
            mNickName = (TextView) view.findViewById(R.id.tv_chat_item_nick_name);
            mLastMessage = (TextView) view.findViewById(R.id.tv_chat_preview);
            mBadgeView = (BadgeView) view.findViewById(R.id.bv_chat_tips_count);
        }
    }

    public static class ChatPreviewItem {
        public ChatDao lastChatDao;
        public int unReadCount;
    }


}

