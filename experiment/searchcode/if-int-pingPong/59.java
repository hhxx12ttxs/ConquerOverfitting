package com.pingpong.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pingpong.dao.ShowFriendDao;
import com.pingpong.dao.ShowImageDao;
import com.pingpong.model.BallFriend;
import com.pingpong.model.BallHall;
import com.pingpong.model.BallImage;
import com.pingpong.utils.Constant;
import com.pingpong.utils.Result;
import com.pingpong.utils.TextUtils;

public class ShowFriendService {

	private ShowFriendDao showFriendDao;

	public ShowFriendService() {
		showFriendDao = new ShowFriendDao();
	}

	public String showFriend(int friendId, String userName) {
		List<Object> params = new ArrayList<Object>();
		List<BallFriend> ballFriends;
		if (!TextUtils.isEmpty(userName)) {
			params.add(userName);
			params.add(userName);
			ballFriends = new ArrayList<BallFriend>();
			BallFriend friend = showFriendDao.query(params);
			if(friend == null){  //没有找到
				return Result.makeListResult("friends", ballFriends);
			}
			ballFriends.add(friend);

			params.clear();
			params.add(friendId);
			params.add(friendId);
			List<BallFriend> friends = showFriendDao.queryAll(params);  //查找好友

			for(BallFriend f : friends){
				if(friend.getFriendId() == f.getFriendId()){
					friend.setIsFriend(1);
					break;
				}
			}
		} else {
			params.add(friendId);
			params.add(friendId);
			ballFriends = showFriendDao.queryAll(params);
			for (BallFriend friend : ballFriends) {
				friend.setIsFriend(1);
			}
		}
		
		// 添加图片
		List<List<BallImage>> allImages = new ArrayList<List<BallImage>>();
		ShowImageDao showImageDao = new ShowImageDao();
		for (BallFriend friend : ballFriends) {
			params.clear();
			params.add(friend.getFriendId());
			params.add(Constant.TYPE_FRIEND);
			List<BallImage> images = showImageDao.queryImage(params);
			allImages.add(images);
		}
		
		for (int i = 0; i < allImages.size() ; i++) {
			List<String> albums = new ArrayList<String>();
			for (BallImage image : allImages.get(i)) {
				StringBuilder sb = new StringBuilder(
						Constant.URL_SERVER_IMAGE);
				sb.append("/" + image.getImageName());
				albums.add(sb.toString());
			}
			ballFriends.get(i).setAlbums(albums);
		}
		
		return Result.makeListResult("friends", ballFriends);
	}

}

