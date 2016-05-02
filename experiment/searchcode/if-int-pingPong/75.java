package com.pingpong.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pingpong.dao.ShowHallDao;
import com.pingpong.dao.ShowInviteDao;
import com.pingpong.model.BallFriend;
import com.pingpong.model.BallHall;
import com.pingpong.model.InviteFriendHall;
import com.pingpong.utils.Location;
import com.pingpong.utils.LocationUtils;
import com.pingpong.utils.Result;
import com.pingpong.utils.TextUtils;

public class ShowInviteService {

	private static final String FLAG_PUBLISH = "0";
	private static final String FLAG_RECEIVE = "1";

	private ShowInviteDao dao;
	private ShowHallDao showHallDao;

	public ShowInviteService() {
		dao = new ShowInviteDao();
		showHallDao = new ShowHallDao();
	}

	public String showInvite(String userId, String latitude, String longitude,
			String count, String flag) {

		// TODO count未用到

		/* 根据经纬度查询 */
		if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
			return showNearInvite(userId, latitude, longitude);
		} else if (flag.equals(FLAG_PUBLISH)) {
			return showPublishInvite(userId);
		} else if (flag.equals(FLAG_RECEIVE)) {
			return showReceiveInvite(userId);
		}
		return Result.generateBaseResult(Result.RESULT_ARGUMENT_ILLEGAL);
	}

	public String showNearInvite(String userId, String latitude,
			String longitude) {
		Location current = new Location(Double.parseDouble(latitude),
				Double.parseDouble(longitude));
		Location[] locations = LocationUtils.findSquareLocation(current);

		double minLat, maxLat, minLng, maxLng;
		if (locations[0].latitude < locations[2].latitude) {
			minLat = locations[0].latitude;
			maxLat = locations[2].latitude;
		} else {
			minLat = locations[2].latitude;
			maxLat = locations[0].latitude;
		}

		if (locations[0].longitude < locations[1].longitude) {
			minLng = locations[0].longitude;
			maxLng = locations[1].longitude;
		} else {
			minLng = locations[1].longitude;
			maxLng = locations[0].longitude;
		}

		List<Object> params = new ArrayList<Object>();
		params.add(minLat);
		params.add(maxLat);
		params.add(minLng);
		params.add(maxLng);

		List<BallHall> halls = showHallDao.queryByLocation(params);
		params.clear();
		for (BallHall hall : halls) {
			params.add(hall.getHallId());
		}
		List<InviteFriendHall> invites = dao.query(params);
		return addInvitePeople(invites);
	}

	public String showPublishInvite(String userId) {
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		List<InviteFriendHall> result = dao.queryPublish(params);
		return addInvitePeople(result);
	}

	public String showReceiveInvite(String userId) {
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		List<InviteFriendHall> result = dao.queryReceive(params);
		return addInvitePeople(result);
	}
	
	/**
	 * 得到约球列表后，增加这些约球的邀请者或者应约者
	 * @param invites
	 * @return
	 */
	private String addInvitePeople(List<InviteFriendHall> invites){
		if (invites != null) {
			List<List<BallFriend>> inviteFriends = new ArrayList<List<BallFriend>>();
			ReplyInviteService service = new ReplyInviteService();
			for (InviteFriendHall invite : invites) {
				List<BallFriend> friends = service.getInvitePeople(invite
						.getInvite().getInviteId() + "");
				inviteFriends.add(friends);
			}
			return makeResult(invites, inviteFriends);
		}
		return Result.makeListResult("invites", null);
	}

	public String makeResult(List<InviteFriendHall> invites) {

		try {
			JSONObject baseJson = new JSONObject(
					Result.generateBaseResult(Result.RESULT_SUCCESS));
			JSONArray invitesArray = new JSONArray();
			if (invites != null && invites.size() != 0) {
				for (InviteFriendHall ifh : invites) {
					JSONObject inviteJson = new JSONObject(ifh.getInvite());
					JSONObject hallJson = new JSONObject(ifh.getHall());
					inviteJson.put("hall", hallJson);
					JSONObject friendJson = new JSONObject(ifh.getFriend());
					inviteJson.put("friend", friendJson);
					invitesArray.put(inviteJson);
				}
			}
			baseJson.put("invites", invitesArray);
			return baseJson.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Result.generateBaseResult(Result.RESULT_SERVER_ERROR);
	}

	public String makeResult(List<InviteFriendHall> invites,
			List<List<BallFriend>> inviteFriends) {

		try {
			JSONObject baseJson = new JSONObject(
					Result.generateBaseResult(Result.RESULT_SUCCESS));
			JSONArray invitesArray = new JSONArray();
			if (invites != null) {
				for (int i = 0; i < invites.size(); i++) { // 对每个约球，遍历邀请者/应约者
					InviteFriendHall ifh = invites.get(i);
					JSONObject inviteJson = new JSONObject(ifh.getInvite());
					JSONObject hallJson = new JSONObject(ifh.getHall());
					inviteJson.put("hall", hallJson);
					JSONObject friendJson = new JSONObject(ifh.getFriend());
					inviteJson.put("friend", friendJson);
					JSONArray peopleArray = new JSONArray();
					if (inviteFriends != null && inviteFriends.size() != 0) {
						List<BallFriend> iPeoples = inviteFriends.get(i);
						if (iPeoples != null && iPeoples.size() != 0) {
							for (BallFriend friend : iPeoples) {
								peopleArray.put(new JSONObject(friend));
							}
						}
					}
					inviteJson.put("invitePeoples", peopleArray);
					invitesArray.put(inviteJson);
				}
			}
			baseJson.put("invites", invitesArray);
			return baseJson.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Result.generateBaseResult(Result.RESULT_SERVER_ERROR);
	}
}

