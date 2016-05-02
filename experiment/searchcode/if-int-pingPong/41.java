package com.pingpong.android.modules.friend;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.pingpong.android.R;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.base.SingletonRequestQueue;
import com.pingpong.android.model.Friend;
import com.pingpong.android.model.InviteInfo;
import com.pingpong.android.utils.BitmapCache;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.Utils;

import java.util.List;

/**
 * Created by JiangZhenJie on 2015/3/30.
 */
public class FriendInviteAdapter extends BaseAdapter {

    private List<InviteInfo> mInvites;
    private Context mContext;
    private FriendInviteFragment.ReplyInviteListener mListener;
    private ImageLoader mImageLoader;
    private boolean isHideInviteButton;

    public FriendInviteAdapter(Context context) {
        mContext = context;
        mImageLoader = new ImageLoader(SingletonRequestQueue.getInstance(context).getRequestQueue(), BitmapCache.getInstance());
    }

    public FriendInviteAdapter(Context context, FriendInviteFragment.ReplyInviteListener listener) {
        this(context);
        mListener = listener;
    }

    public void setInvites(List<InviteInfo> invites) {
        mInvites = invites;
        notifyDataSetChanged();
    }

    public InviteInfo getInviteInPosition(int position) {
        if (mInvites != null && position >= 0 && position < mInvites.size()) {
            return mInvites.get(position);
        }
        return null;
    }

    public void removeObject(Object obj) {
        if (mInvites != null) {
            mInvites.remove(obj);
        }
    }

    private void showReplyConfirmDialog(final InviteInfo inviteInfo) {
        if (inviteInfo == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.confirm_reply_invite);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.replyInvite(inviteInfo);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    public void setIsHideInviteButton(boolean b) {
        isHideInviteButton = b;
    }

    @Override
    public int getCount() {
        return mInvites == null ? 0 : mInvites.size();
    }

    @Override
    public Object getItem(int position) {
        return mInvites.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.invite_item_layout, parent, false);
            holder = new ViewHolder();
            holder.friendPortrait = (NetworkImageView) convertView.findViewById(R.id.iv_friend_portrait);
            holder.friendName = (TextView) convertView.findViewById(R.id.tv_friend_name);
            holder.inviteAddr = (TextView) convertView.findViewById(R.id.tv_friend_address);
            holder.inviteTime = (TextView) convertView.findViewById(R.id.tv_invite_time);
            holder.inviteText = (TextView) convertView.findViewById(R.id.tv_invite_text);
            holder.gameHall = (TextView) convertView.findViewById(R.id.tv_game_hall);
            holder.gameTime = (TextView) convertView.findViewById(R.id.tv_game_time);
            holder.replyInvite = (TextView) convertView.findViewById(R.id.tv_reply_invite);
            holder.invitePeople = (TextView) convertView.findViewById(R.id.tv_invite_people);
            holder.replyPeople = (TextView) convertView.findViewById(R.id.tv_reply_people);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final InviteInfo inviteInfo = mInvites.get(position);
        Friend friend = null;
        holder.friendPortrait.setImageUrl(inviteInfo.getFriend().getFriendProfile(), mImageLoader);
        holder.friendName.setText(inviteInfo.getFriend().getFriendName());
        holder.inviteAddr.setText("发自 " + inviteInfo.getInviteAddress());
        holder.inviteTime.setText("时间 " + Utils.formatTime(inviteInfo.getInviteTime()));
        holder.inviteText.setText(inviteInfo.getInviteText());
        holder.gameHall.setText("地点：" + inviteInfo.getHall().getHallName());
        holder.gameTime.setText("时间：" + Utils.formatTime(inviteInfo.getGameTime()));

        // 配置邀请人
        if (inviteInfo.getInviteType() == Constants.FLAG_INVITE_NEAR) {  //邀请的是附近的人
            holder.invitePeople.setText("邀请：附近的人");
        } else {  // 邀请的是好友
            StringBuilder sb = new StringBuilder("邀请：");
            for (Friend friend1 : inviteInfo.getInvitePeoples()) {
                sb.append(friend1.getFriendName());
                sb.append("，");
            }
            sb.delete(sb.length() - 1, sb.length());
            holder.invitePeople.setText(sb.toString());
        }

        // 配置应约人
        if (inviteInfo.getInviteType() == Constants.FLAG_INVITE_NEAR) {
            if (inviteInfo.getInvitePeoples() != null && inviteInfo.getInvitePeoples().size() != 0) {
                StringBuilder sb = new StringBuilder("应约：");
                for (Friend friend1 : inviteInfo.getInvitePeoples()) {
                    sb.append(friend1.getFriendName());
                    sb.append("，");
                }
                sb.deleteCharAt(sb.length() - 1);
                holder.replyPeople.setText(sb.toString());
            }
        } else {
            if (inviteInfo.getInvitePeoples() != null && inviteInfo.getInvitePeoples().size() != 0) {
                StringBuilder sb = new StringBuilder("应约：");
                for (Friend friend1 : inviteInfo.getInvitePeoples()) {
                    if (friend1.getFriendStatus() == Constants.FLAG_ACCEPT_INVITE) {
                        sb.append(friend1.getFriendName());
                        sb.append("，");
                    }
                }
                sb.deleteCharAt(sb.length() - 1);
                holder.replyPeople.setText(sb.toString());
            }
        }

        // 配置应约按钮
        if (isHideInviteButton) {
            holder.replyInvite.setVisibility(View.GONE);
        } else {
            holder.replyInvite.setVisibility(View.VISIBLE);
        }
        if (inviteInfo.getInvitePeoples() != null && inviteInfo.getInvitePeoples().size() != 0) {
            for (Friend fri : inviteInfo.getInvitePeoples()) {
                if (fri.getFriendId() == DataManager.getInstance().getLoginUserId()) {
                    friend = fri;
                    break;
                }
            }
            if (friend != null) {
                if (friend.getFriendStatus() == Constants.FLAG_ACCEPT_INVITE) {
                    holder.replyInvite.setText("已应约");
                } else if (Long.parseLong(inviteInfo.getGameTime()) <= System.currentTimeMillis()) {
                    holder.replyInvite.setText("已过期");
                } else {
                    holder.replyInvite.setText("应约");
                    holder.replyInvite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showReplyConfirmDialog(inviteInfo);
                        }
                    });
                }
            }
        } else {
            holder.replyInvite.setText("应约");
            holder.replyInvite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showReplyConfirmDialog(inviteInfo);
                }
            });
        }

        return convertView;
    }

    private class ViewHolder {
        NetworkImageView friendPortrait;
        TextView friendName;
        TextView inviteAddr;
        TextView inviteTime;
        TextView inviteText;
        TextView gameHall;
        TextView gameTime;
        TextView replyInvite;
        TextView invitePeople;
        TextView replyPeople;
    }
}

