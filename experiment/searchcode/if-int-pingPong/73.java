package com.pingpong.service;

import java.util.ArrayList;
import java.util.List;

import com.pingpong.dao.ShowFriendDao;
import com.pingpong.dao.UpdateFriendDao;
import com.pingpong.model.BallFriend;
import com.pingpong.utils.Constant;
import com.pingpong.utils.Result;
import com.pingpong.utils.TextUtils;

public class UpdateFriendService {

	private UpdateFriendDao updateFriendDao;

	public UpdateFriendService() {
		updateFriendDao = new UpdateFriendDao();
	}

	public String updateFriend(String userId, String friendSign,
			String friendAddress, String friendAge, String friendJob,
			String friendExperiences) {
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		params.add(userId);
		ShowFriendDao showFriendDao = new ShowFriendDao();
		BallFriend friend = showFriendDao.query(params);

		if(friend == null){
			return Result.generateBaseResult(Result.RESULT_ARGUMENT_ILLEGAL);
		}
		
		if (TextUtils.isEmpty(friendSign)) {
			friendSign = friend.getFriendSign();
		}

		if (TextUtils.isEmpty(friendAddress)) {
			friendAddress = friend.getFriendAddress();
		}

		if (TextUtils.isEmpty(friendJob)) {
			friendJob = friend.getFriendJob();
		}

		if (TextUtils.isEmpty(friendExperiences)) {
			friendExperiences = friend.getFriendExperiences();
		}

		if (TextUtils.isEmpty(friendAge)) {
			friendAge = friend.getFriendAge() + "";
		}

		params.clear();
		params.add(friendSign);
		params.add(friendAddress);
		params.add(friendAge); 
		params.add(friendJob);
		params.add(friendExperiences);
		params.add(userId);
		int rst = updateFriendDao.update(params);
		if (rst > 0) {
			return Result.generateBaseResult(Result.RESULT_SUCCESS);
		}
		return Result.generateBaseResult(Result.RESULT_SERVER_ERROR);
	}

	public String uploadPortrait(String userId) {
		String url = Constant.URL_SERVER_PORTRAIt + "/" + userId + ".jpg";
		List<Object> params = new ArrayList<Object>();
		params.add(url);
		params.add(userId);
		int rst = updateFriendDao.updatePortrait(params);
		if (rst > 0) {
			return Result.generateBaseResult(Result.RESULT_SUCCESS);
		} else {
			return Result.generateBaseResult(Result.RESULT_SERVER_ERROR);
		}
	}
}

