package com.pingpong.service;

import java.util.ArrayList;
import java.util.List;

import com.pingpong.dao.ShowInfoDao;
import com.pingpong.model.BallInformation;
import com.pingpong.utils.Result;

public class ShowInfoService {

	private ShowInfoDao showInfoDao;

	public ShowInfoService() {
		showInfoDao = new ShowInfoDao();
	}

	public String getBallInfo(int hallId, int sinceId, int beforeId, int count) {
		List<Object> params = new ArrayList<Object>();
		List<BallInformation> ballInfos = null;
		if (sinceId == 0 && beforeId == 0) {
			params.add(count);
			ballInfos = showInfoDao.query(params);
		} else if (sinceId != 0 && beforeId != 0) {
			params.add(sinceId);
			params.add(beforeId);
			params.add(count);
			ballInfos = showInfoDao.querySinceBefore(params);
		} else if (sinceId != 0) {
			params.add(sinceId);
			params.add(count);
			ballInfos = showInfoDao.querySince(params);
		} else if (beforeId != 0) {
			params.add(beforeId);
			params.add(count);
			ballInfos = showInfoDao.queryBefore(params);
		}
		return Result.makeListResult("informations", ballInfos);
	}

}

