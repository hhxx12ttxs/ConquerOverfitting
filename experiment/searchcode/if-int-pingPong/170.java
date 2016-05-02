package com.pingpong.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.pingpong.utils.Constant;
import com.pingpong.utils.Result;
import com.pingpong.utils.Utils;
import com.tencent.xinge.Message;
import com.tencent.xinge.XingeApp;

public class ChatReceiveService {

	public String pushMessage(String fromUserId, String toUserId,
			String fromName, String toName, String msg) {
		System.out
				.println("fromUserId=" + fromUserId + " toUserId=" + toUserId
						+ " fromName=" + fromName + " toName=" + toName
						+ " msg=" + msg);
		XingeApp push = new XingeApp(Constant.XIN_GE_ACCESS_ID,
				Constant.XIN_GE_SECRET_KEY);
		Message message = new Message();
		message.setTitle("聊天信息");
		message.setType(Message.TYPE_MESSAGE);
		JSONObject ret = null;
		if (toUserId.contains(",")) {
			String[] acIds = toUserId.split(",");
			String[] acNames = toName.split(",");
			for (int i = 0 ;i < acIds.length;i++) {
				String acId = acIds[i];
				String acName = acNames[i];
				String content = packGroupMessage(fromUserId,fromName,toUserId, toName,acId,acName, msg);
				message.setContent(content);
				System.out.println(content);
				ret = push.pushSingleAccount(0, acId, message);
			}
		} else {
			message.setContent(packageMessage(fromUserId, toUserId, fromName,
					toName, msg));
			ret = push.pushSingleAccount(0, toUserId, message);
		}
		int retCode = ret.getInt("ret_code");
		if (retCode == 0) {
			return Result.generateBaseResult(Result.RESULT_SUCCESS);
		} else {
			System.out.println("Push message fail " + ret.getString("err_msg"));
			return Result.generateBaseResult(Result.RESULT_SERVER_ERROR);
		}
	}

	private String packageMessage(String fromUserId, String toUserId,
			String fromName, String toName, String msg) {
		JSONObject base = new JSONObject();
		JSONObject json = new JSONObject();
		json.put("fromUserId", fromUserId);
		json.put("toUserId", toUserId);
		json.put("fromUserName", fromName);
		json.put("toUserName", toName);
		json.put("message", msg);
		base.put("msg_type", "chat");
		base.put("msg", json);
		return base.toString();
	}

	private String packGroupMessage(String fromId,String fromName,String sourceToId,String sourceToName,String toId,String toName,String msg){

		sourceToId = sourceToId.replace(toId, fromId);
		sourceToName = sourceToName.replace(toName, fromName);
		
		JSONObject base = new JSONObject();
		JSONObject json = new JSONObject();
		json.put("fromUserId", Utils.sortString(sourceToId, ","));
		json.put("toUserId", toId);
		json.put("fromUserName", sourceToName);
		json.put("toUserName", toName);
		json.put("message", msg);
		json.put("sendId",fromId);
		json.put("sendName",fromName);
		base.put("msg_type", "chat");
		base.put("msg", json);
		return base.toString();
	}
	
}

