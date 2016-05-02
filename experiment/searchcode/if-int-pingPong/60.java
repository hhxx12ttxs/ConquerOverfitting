package com.pingpong.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pingpong.dao.LoginDao;
import com.pingpong.dao.ShowHallDao;
import com.pingpong.dao.ShowImageDao;
import com.pingpong.model.BallFriend;
import com.pingpong.model.BallHall;
import com.pingpong.model.BallImage;
import com.pingpong.utils.Constant;
import com.pingpong.utils.Result;

public class LoginService {

	private LoginDao loginDao;
	private String userName;
	private String password;
	private int type;
	private BallHall hall;
	private BallFriend friend;

	public LoginService() {
		loginDao = new LoginDao();
	}

	public String login(String userName, String password, int type) {
		this.userName = userName;
		this.password = password;
		this.type = type;
		if (loginCheck()) {
			// 登录成功
			List<Object> params = new ArrayList<Object>();
			if (type == Constant.TYPE_FRIEND) {
				params.clear();
				params.add(friend.getFriendId());
				params.add(Constant.TYPE_FRIEND);
				ShowImageDao showImageDao = new ShowImageDao();
				List<BallImage> images = showImageDao.queryImage(params);
				params.clear();
				params.add(Constant.STATUS_ONLINE);
				params.add(userName);
				params.add(userName);
				loginDao.updateFriend(params);
				return generateResult(friend,images);
			} else if (type == Constant.TYPE_HALL) {
				params.clear();
				params.add(hall.getHallId());
				params.add(Constant.TYPE_HALL);
				ShowImageDao showImageDao = new ShowImageDao();
				List<BallImage> images = showImageDao.queryImage(params);

				params.clear();
				params.add(Constant.STATUS_ONLINE);
				params.add(userName);
				loginDao.updateHall(params);
				return generateResult(hall,images);
			}
		}
		return Result.makeIdResult("userId",
				Result.RESULT_USERNAME_PASSWORD_INVALID);
	}

	/**
	 * 检查用户名和密码
	 * 
	 * @return true 用户名存在且密码正确 <br/>
	 *         false 用户名不存在或者密码错误
	 */
	private boolean loginCheck() {
		List<Object> params = new ArrayList<Object>();
		if (type == Constant.TYPE_FRIEND) {
			params.add(userName);
			params.add(password);
			params.add(userName);
			params.add(password);
			friend = loginDao.queryFriend(params);
			return friend != null;
		} else if (type == Constant.TYPE_HALL) {
			params.add(userName);
			params.add(password);
			hall = loginDao.queryBallHall(params);
			return hall != null;
		}
		return false;
	}

	private String generateResult(Object obj,List<BallImage> images) {
		String base = Result.generateBaseResult(Result.RESULT_SUCCESS);
		try {
			JSONObject baseJson = new JSONObject(base);
			JSONArray imageArray = new JSONArray();
			for (BallImage image : images) {
				StringBuilder sb = new StringBuilder(
						Constant.URL_SERVER_IMAGE);
				sb.append("/" + image.getImageName());
				imageArray.put(sb.toString());
			}
			if (obj instanceof BallHall) {
				JSONObject json = new JSONObject(obj);
				json.put("hallImages", imageArray);
				baseJson.put("hall", json);
			} else {
				JSONObject json = new JSONObject(obj);
				json.put("albums", imageArray);
				baseJson.put("friend", json);
			}
			return baseJson.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Result.generateBaseResult(Result.RESULT_SERVER_ERROR);
	}

}

