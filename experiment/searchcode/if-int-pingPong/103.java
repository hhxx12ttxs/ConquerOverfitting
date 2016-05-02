package com.pingpong.service;

import java.util.ArrayList;
import java.util.List;

import com.pingpong.dao.ShowMomentDao;
import com.pingpong.model.BallHallMoment;
import com.pingpong.utils.Result;

public class ShowMomentService {

	private ShowMomentDao dao;

	public ShowMomentService() {
		dao = new ShowMomentDao();
	}

	public String showMoment(int hallId, int sinceId, int beforeId, int count) {
		if (beforeId == 0) {
			beforeId = Integer.MAX_VALUE;
		}

		if (count <= 0 || count > 100) {
			count = 20;
		}
		List<Object> params = new ArrayList<Object>();

		params.add(hallId);
		params.add(sinceId);
		params.add(beforeId);
		params.add(count);

		List<BallHallMoment> moments = dao.queryMoment(params);

		if (moments != null){
			return Result.makeListResult("moments", moments);
		}

		return "";

	}

}

