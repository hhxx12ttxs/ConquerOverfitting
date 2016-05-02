package com.pingpong.service;

import java.util.ArrayList;
import java.util.List;

import com.pingpong.dao.ShowGameDao;
import com.pingpong.model.Game;
import com.pingpong.utils.Result;
import com.pingpong.utils.TextUtils;
import com.pingpong.utils.Utils;

public class ShowGameService {

	private ShowGameDao dao;

	public ShowGameService() {
		dao = new ShowGameDao();
	}

	public String showGame(int userId, int hallId, int sinceId, int beforeId,
			int count, String area) {

		if (sinceId <= 0) {
			sinceId = 0;
		}

		if (beforeId <= 0) {
			beforeId = Integer.MAX_VALUE;
		}

		if (count <= 0 || count > 100) {
			count = 20;
		}

		List<Object> params = new ArrayList<Object>();
		List<Game> games = null;
		if (hallId != -1) {
			// 查询指定球馆赛事
			params.add(hallId);
			params.add(sinceId);
			params.add(beforeId);
			params.add(count);
			games = dao.queryByHall(params);
		} else if (!TextUtils.isEmpty(area)) {
			// 查询区域球馆赛事
			String[] areaArray = area.split(",");
			StringBuilder sb = new StringBuilder();
			sb.append("%%");
			for(String str:areaArray){
				sb.append(str);
				sb.append("%%");
			}
			String query = sb.toString();
			query = Utils.queryStringAreaFuzzy(query);
			params.add(query);
			games = dao.queryArea(params);
		} else {
			// 查询用户关联球馆全部赛事
			params.add(userId);
			games = dao.showAssociationGame(params);
		}

		if (games != null) {
			return Result.makeListResult("games", games);
		}

		return "";
	}

}

