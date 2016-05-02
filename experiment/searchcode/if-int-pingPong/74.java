package com.pingpong.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.pingpong.dao.InviteDao;
import com.pingpong.dao.ReplyInviteDao;
import com.pingpong.dao.ShowFriendDao;
import com.pingpong.model.BallFriend;
import com.pingpong.model.BallInvite;
import com.pingpong.model.BallReply;
import com.pingpong.utils.Constant;
import com.pingpong.utils.Result;
import com.tencent.xinge.Message;
import com.tencent.xinge.XingeApp;

public class ReplyInviteService {

	private ReplyInviteDao dao;

	public ReplyInviteService() {
		dao = new ReplyInviteDao();
	}

	/**
	 * 应约约球
	 * 
	 * @param userId
	 * @param inviteId
	 * @param inviteAgree
	 * @return
	 */
	public String replyInvite(String userId, String inviteId, String inviteAgree) {
		List<Object> params = new ArrayList<Object>();
		
		// TODO 未做过期处理，需客户端自行处理
		
		//先判断是否存在，若存在，更新状态，否则插入
		params.add(inviteId);
		params.add(userId);
		BallReply reply = dao.queryByIdFriend(params);
		int rst = 0;
		if(reply != null){
			params.clear();
			params.add(1);
			params.add(inviteId);
			params.add(userId);
			rst = dao.update(params);
		}else {
			params.clear();
			params.add(inviteId);
			params.add(userId);
			params.add(System.currentTimeMillis());
			params.add(1);
			rst = dao.insert(params);
		}
		if (rst >= 0) {
			startPush(userId, inviteId);
			return Result.generateBaseResult(Result.RESULT_SUCCESS);
		} else {
			return Result.generateBaseResult(Result.RESULT_SERVER_ERROR);
		}
	}
	
	private void startPush(final String userId, final String inviteId){
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<Object> params = new ArrayList<Object>();
				InviteDao inviteDao = new InviteDao();
				params.clear();
				params.add(inviteId);
				BallInvite invite = inviteDao.query(params);
				ShowFriendDao friendDao = new ShowFriendDao();
				params.clear();
				params.add(invite.getFriendId());
				params.add(invite.getFriendId());
				BallFriend inviteFriend = friendDao.query(params);
				params.clear();
				params.add(userId);
				params.add(userId);
				BallFriend replyFriend = friendDao.query(params);
				
				XingeApp push = new XingeApp(Constant.XIN_GE_ACCESS_ID,
						Constant.XIN_GE_SECRET_KEY);
				Message message = new Message();
				message.setTitle("收到应约");
				message.setContent(replyFriend.getFriendName() + "应约了你");
				message.setType(Message.TYPE_MESSAGE);
				JSONObject ret = push.pushSingleAccount(0, inviteFriend.getFriendId()+"", message);
				System.out.println(ret);
			}
		}).start();
	}

	
	/**
	 * 得到某个约球的邀请/应约人，供showinvite使用
	 * @param inviteId
	 * @return
	 */
	public List<BallFriend> getInvitePeople(String inviteId) {
		List<Object> params = new ArrayList<Object>();
		params.add(inviteId);
		List<BallReply> replys = dao.queryById(params);
		if(replys == null || replys.size() == 0){
			return null;
		}
		params.clear();
		for (BallReply reply : replys) {
			params.add(reply.getFriendId());
		}
		ShowFriendDao dao = new ShowFriendDao();
		List<BallFriend> friends = dao.queryBatch(params);
		for (BallFriend friend : friends) {
			for (BallReply reply : replys) {
				if (friend.getFriendId() == reply.getFriendId()) {
					friend.setFriendStatus(reply.getStatus());
					continue;
				}
			}
		}

		return friends;
	}
}

