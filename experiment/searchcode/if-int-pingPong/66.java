package com.pingpong.service;

import java.util.ArrayList;
import java.util.List;

import com.pingpong.dao.AssociateDao;
import com.pingpong.dao.ShowFriendDao;
import com.pingpong.dao.ShowHallDao;
import com.pingpong.model.BallFriend;
import com.pingpong.model.BallHall;
import com.pingpong.utils.Result;

public class AssociateService {

	private AssociateDao associateDao;

	public AssociateService() {
		associateDao = new AssociateDao();
	}

	public String associate(int userId, int hallId) {

		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		BallFriend ballFriend = associateDao.getBallFriend(params);

		if (ballFriend == null) {
			return Result.generateBaseResult(Result.RESULT_ARGUMENT_ILLEGAL);
		}

		params.clear();
		params.add(hallId);
		BallHall ballHall = associateDao.getBallHall(params);

		if (ballHall == null) {
			return Result.generateBaseResult(Result.RESULT_ARGUMENT_ILLEGAL);
		}

		params.add(userId);
		params.add(System.currentTimeMillis());
		associateDao.insertAssociation(params);
		updateHallAndFans(userId + "", hallId + "");
		return Result.generateBaseResult(Result.RESULT_SUCCESS);
	}

	private void updateHallAndFans(final String friendId, final String hallId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<Object> params = new ArrayList<Object>();
				params.add(friendId);
				new ShowFriendDao().updateHall(params);
				params.clear();
				params.add(hallId);
				new ShowHallDao().updateFans(params);
			}
		}).start();
	}
}

