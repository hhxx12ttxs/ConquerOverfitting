package com.pingpong.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.pingpong.dao.InviteDao;
import com.pingpong.dao.ShowFriendDao;
import com.pingpong.model.BallFriend;
import com.pingpong.utils.Constant;
import com.pingpong.utils.Result;
import com.pingpong.utils.TextUtils;
import com.tencent.xinge.ClickAction;
import com.tencent.xinge.Message;
import com.tencent.xinge.XingeApp;

public class InviteService {

	private InviteDao inviteDao;

	public InviteService() {
		inviteDao = new InviteDao();
	}

	public String invite(String friendId, String gameTime, String inviteTime,
			String inviteAddress, String hallId, String inviteText,
			String invitePerson) {

		List<Object> params = new ArrayList<Object>();
		params.add(friendId);
		params.add(gameTime);
		params.add(inviteTime);
		params.add(inviteAddress);
		params.add(hallId);
		params.add(inviteText);
		if (!TextUtils.isEmpty(invitePerson)) {
			params.add(Constant.INVITY_FRIEND);
		} else {
			params.add(Constant.INVITY_ALL);
		}
		params.add(Constant.STATUS_INVITING);
		int rst = inviteDao.insertInvite(params);
		updatePlay(friendId);
		if (!TextUtils.isEmpty(invitePerson)) {
			String[] inviteMans = invitePerson.split(",");
			Object[][] prms = new Object[inviteMans.length][4];
			for (int i = 0; i < inviteMans.length; i++) {
				Object[] p = new Object[4];
				p[0] = rst;
				p[1] = inviteMans[i];
				p[2] = System.currentTimeMillis();
				p[3] = Constant.STATUS_NOT_REPLY;
				prms[i] = p;
			}
			inviteDao.insertReply(prms);
		}
		sendInvite(rst + "", friendId + "", invitePerson);

		if (rst > 0) {
			return Result.generateBaseResult(Result.RESULT_SUCCESS);
		} else {
			return Result.generateBaseResult(Result.RESULT_SERVER_ERROR);
		}
	}

	private void sendInvite(final String inviteId, final String friendId,
			final String invitePerson) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<Object> params = new ArrayList<Object>();
				params.add(friendId);
				params.add(friendId);
				ShowFriendDao showFriendDao = new ShowFriendDao();
				BallFriend friend = showFriendDao.query(params);
				if(!TextUtils.isEmpty(invitePerson)){
					String[] invitePersons = invitePerson.split(",");
					for (String str : invitePersons) {
						push(str, friend.getFriendName());
					}
				}
			}
		}).start();

	}

	private void push(String toUserId, String userName) {
		XingeApp push = new XingeApp(Constant.XIN_GE_ACCESS_ID,
				Constant.XIN_GE_SECRET_KEY);
		Message message = new Message();
		message.setTitle("收到邀请");
		message.setContent(userName + " 给你发来一条邀请");
		message.setType(Message.TYPE_MESSAGE);
		JSONObject ret = push.pushSingleAccount(0, toUserId, message);
		System.out.println(ret);
	}

	private void updatePlay(final String friendId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<Object> params = new ArrayList<Object>();
				params.add(friendId);
				new ShowFriendDao().updatePlay(params);
			}
		}).start();
	}

}

