package com.pingpong.android.common;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.pingpong.android.R;
import com.pingpong.android.WelcomeActivity;
import com.pingpong.android.base.DataManager;
import com.pingpong.android.db.DataBaseHelper;
import com.pingpong.android.model.InviteInfo;
import com.pingpong.android.model.ShowInviteModel;
import com.pingpong.android.modules.friend.FriendChatActivity;
import com.pingpong.android.modules.friend.FriendPublishInviteActivity;
import com.pingpong.android.modules.friend.FriendReceiveInviteActivity;
import com.pingpong.android.utils.Constants;
import com.pingpong.android.utils.L;
import com.pingpong.android.utils.Utils;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by JiangZhenJie on 2015/4/4.
 */
public class AppPushReceiver extends XGPushBaseReceiver {


    @Override
    public void onRegisterResult(Context context, int i, XGPushRegisterResult xgPushRegisterResult) {

    }

    @Override
    public void onUnregisterResult(Context context, int i) {

    }

    @Override
    public void onSetTagResult(Context context, int i, String s) {

    }

    @Override
    public void onDeleteTagResult(Context context, int i, String s) {

    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage xgPushTextMessage) {
        L.d("receive message " + xgPushTextMessage.getContent());
        String title = xgPushTextMessage.getTitle();
        L.e(title);
        if (TextUtils.equals(title, "收到邀请")) {
            Intent intent = new Intent(context, WelcomeActivity.class);
            intent.putExtra("redirect", FriendReceiveInviteActivity.class.getCanonicalName());
            pushNotification(context, intent, title, title, xgPushTextMessage.getContent());
        } else if (TextUtils.equals(title, "收到应约")) {
            Intent intent = new Intent(context, WelcomeActivity.class);
            intent.putExtra("redirect", FriendPublishInviteActivity.class.getCanonicalName());
            pushNotification(context, intent, title, title, xgPushTextMessage.getContent());
        } else {
            try {
                JSONObject json = new JSONObject(xgPushTextMessage.getContent());
                String msgType = json.getString("msg_type");
                if (msgType.equals("chat")) {               //接收到聊天信息
                    json = json.getJSONObject("msg");
                    handleChatMessage(context, json);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {
    }

    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {

    }

    private void handleChatMessage(Context context, JSONObject json) throws JSONException {

        String fromUserId = json.getString("fromUserId");
        String fromName = json.getString("fromUserName");
        String message = json.getString("message");

        /**
         * 聊天提醒策略：
         *  1. 如果当前界面是当前用户的聊天界面，发送Update广播，不发通知，写入数据库，标记为已读。
         *  2. 如果MainActivity在前台，发送Receive广播，不发通知，写入数据库，标志为未读。
         *  3. 以上情况都不是，发送Receive广播，发送通知，写入数据库，标记为未读。
         */

        if (DataManager.getInstance() != null && TextUtils.equals(DataManager.getInstance().getFrontChat(), fromUserId)) {  // 当前界面为聊天界面
            DataBaseHelper.saveChatRecord(json, Constants.FLAG_READED);
            Intent intent = new Intent(Constants.Action.ACTION_UPDATE_CHAT);
            intent.putExtra("data", json.toString());
            context.sendBroadcast(intent);
        } else if (DataManager.getInstance() != null && DataManager.getInstance().isMainActivityOnForeground()) {  // 当前界面为主界面
            DataBaseHelper.saveChatRecord(json, Constants.FLAG_UNREAD);
            Intent intent = new Intent(Constants.Action.ACTION_RECEIVE_CHAT);
            intent.putExtra("data", json.toString());
            context.sendBroadcast(intent);
        } else {   // 其他
            DataBaseHelper.saveChatRecord(json, Constants.FLAG_UNREAD);
            Intent intent = new Intent(context, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("userId", fromUserId);
            intent.putExtra("userName", fromName);
            intent.putExtra("redirect", FriendChatActivity.class.getCanonicalName());
            pushNotification(context, intent, "收到聊天", fromName, message);

            intent = new Intent(Constants.Action.ACTION_RECEIVE_CHAT);
            intent.putExtra("data", json.toString());
            context.sendBroadcast(intent);
        }
    }

    private void handleInviteMessage(Context context, JSONObject json) {
        L.d("handle invite message");
        Intent intent = new Intent(context, FriendReceiveInviteActivity.class);
        intent.putExtra("data", json.toString());
        ShowInviteModel inviteModel = (ShowInviteModel) Utils.json2Model(json.toString(), ShowInviteModel.class);
        InviteInfo info = inviteModel.getInvites().get(0);
        pushNotification(context, intent, "收到邀请", info.getFriend().getFriendName(), "邀请你打球啦");
    }

    private void pushNotification(Context context, Intent intent, String ticker, String title, String contentText) {
        L.d("push notification");
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent[] intents = new Intent[1];
        intents[0] = intent;
        PendingIntent pendingIntent = PendingIntent.getActivities(context, R.string.app_name, intents, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(context)
                .setAutoCancel(true)
                .setTicker(ticker)
                .setSmallIcon(R.drawable.pingpong_icon)
                .setContentTitle(title)
                .setContentText(contentText)
                .setDefaults(Notification.DEFAULT_SOUND
                        | Notification.DEFAULT_LIGHTS)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent).getNotification();

        notification.flags = Notification.FLAG_AUTO_CANCEL;
        manager.notify(R.string.app_name, notification);
    }
}

