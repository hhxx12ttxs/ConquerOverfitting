package com.pingpong.service;

import java.util.ArrayList;
import java.util.List;

import com.pingpong.dao.AddFriendDao;
import com.pingpong.model.BallFriend;
import com.pingpong.model.FriendFollow;
import com.pingpong.utils.Result;

public class AddFriendService {

	private AddFriendDao addFriendDao;

	public AddFriendService() {
		addFriendDao = new AddFriendDao();
	}

	public String addFriend(int friendId, String userName) {
		List<Object> params = new ArrayList<Object>();
		params.add(userName);
		params.add(userName);
		BallFriend follower = addFriendDao.queryFriend(params);

		if (follower == null) {
			return Result.generateBaseResult(Result.RESULT_DATA_INVALID);
		}

		params.clear();
		params.add(friendId);
		params.add(follower.getFriendId());
		FriendFollow friendFollow = addFriendDao.query(params);

		if (friendFollow != null) {
			return Result.generateBaseResult(Result.RESULT_DUPLICATE_KEY);
		}

		params.add(System.currentTimeMillis());
		addFriendDao.insert(params);
		
		params.clear();
		params.add(friendId);
		addFriendDao.update(params);
		
		params.clear();
		params.add(follower.getFriendId());
		addFriendDao.update(params);
		
		return Result.generateBaseResult(Result.RESULT_SUCCESS);
	}

}

