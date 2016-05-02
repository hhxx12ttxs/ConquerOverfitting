package com.pingpong.service;

import java.util.ArrayList;
import java.util.List;

import com.pingpong.dao.LogoutDao;
import com.pingpong.utils.Constant;
import com.pingpong.utils.Result;

public class LogoutService {

	private LogoutDao dao;

	public LogoutService() {
		dao = new LogoutDao();
	}

	public String logout(String userName, String type) {
		List<Object> params = new ArrayList<Object>();
		params.add(userName);
		int rst = 0;
		if (type.equals(Constant.TYPE_FRIEND + "")) {
			rst = dao.updateFriend(params);
		} else {
			rst = dao.updateHall(params);
		}
		if (rst > 0) {
			return Result.generateBaseResult(Result.RESULT_SUCCESS);
		} else {
			return Result.generateBaseResult(Result.RESULT_SERVER_ERROR);
		}
	}
}

