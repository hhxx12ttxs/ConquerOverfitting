package com.pingpong.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pingpong.dao.ShowHallDao;
import com.pingpong.dao.ShowImageDao;
import com.pingpong.model.BallHall;
import com.pingpong.model.BallImage;
import com.pingpong.utils.Constant;
import com.pingpong.utils.Location;
import com.pingpong.utils.LocationUtils;
import com.pingpong.utils.Result;
import com.pingpong.utils.TextUtils;
import com.pingpong.utils.Utils;

public class ShowHallService {

	private ShowHallDao showHallDao;
	
	public ShowHallService() {
		showHallDao = new ShowHallDao();
	}

	public String showHall(String userId, String latitude, String longitude,
			String area, String hallName, String hallId) {
		List<Object> params = new ArrayList<Object>();
		List<BallHall> ballHalls;
		/* userId 为必传参数 */
		if (!TextUtils.isEmpty(hallId)) {
			// 根据ID查询
			params.add(hallId);
			ballHalls = showHallDao.queryById(params);
		} else if (!TextUtils.isEmpty(hallName)) {
			// 根据名称查询
			//TODO : 优化
			params.add("%"+hallName+"%");
			ballHalls = showHallDao.queryByName(params);
		} else if (!TextUtils.isEmpty(latitude)
				&& !TextUtils.isEmpty(longitude)) {
			// 根据经纬度查询
			Location current = new Location(Double.parseDouble(latitude),Double.parseDouble(longitude));
			Location[] locations = LocationUtils.findSquareLocation(current);
			
			double minLat , maxLat , minLng , maxLng;
			if(locations[0].latitude < locations[2].latitude){
				minLat = locations[0].latitude;
				maxLat = locations[2].latitude;
			}else{
				minLat = locations[2].latitude;
				maxLat = locations[0].latitude;
			}
			
			if(locations[0].longitude < locations[1].longitude){
				minLng = locations[0].longitude;
				maxLng = locations[1].longitude;
			}else{
				minLng = locations[1].longitude;
				maxLng = locations[0].longitude;
			}
			
			params.add(minLat);
			params.add(maxLat);
			params.add(minLng);
			params.add(maxLng);
			
			List<BallHall> temp ;
			temp = showHallDao.queryByLocation(params);
			ballHalls = new ArrayList<BallHall>();
			
			for(BallHall hall : temp){
				Location loc = new Location(Double.parseDouble(hall.getHallLatitude()),Double.parseDouble(hall.getHallLongitude()));
				double distance = LocationUtils.distance(current, loc);
				if(distance <= LocationUtils.DISTANCE){
					hall.setHallDistance(distance);
					ballHalls.add(hall);
				}
			}
			
			Collections.sort(ballHalls, new Comparator<BallHall>() {
				@Override
				public int compare(BallHall hall1, BallHall hall2) {
					double d = hall1.getHallDistance() - hall2.getHallDistance();
					return (int) (d * 1000);
				}
			});
			
		} else if (!TextUtils.isEmpty(area)) {
			// 根据区域查询
			String[] queryStrs = area.split(",");
			StringBuilder sb = new StringBuilder();
			for(String str:queryStrs){		
				sb.append("%%");
				sb.append(str);
			}
			sb.append("%%");
			String query = Utils.queryStringAreaFuzzy(sb.toString());
			params.add(query);
			ballHalls = showHallDao.queryByArea(params);
		} else {
			// 根据userId查询关联球馆
			params.add(userId);
			ballHalls = showHallDao.queryAssociate(params);
		}

		if (ballHalls == null) {
			return Result.generateBaseResult(Result.RESULT_OTHER_ERROR);
		}

		// 查询该球馆对应的图片
		List<List<BallImage>> allImages = new ArrayList<List<BallImage>>();
		ShowImageDao showImageDao = new ShowImageDao();
		
		for (BallHall hall : ballHalls) {
			params.clear();
			params.add(hall.getHallId());
			params.add(Constant.TYPE_HALL);
			List<BallImage> images = showImageDao.queryImage(params);
			allImages.add(images);
		}

		String base = Result.generateBaseResult(Result.RESULT_SUCCESS);
		try {
			JSONObject baseJson = new JSONObject(base);
			JSONArray hallArray = new JSONArray(ballHalls);
			for (int i = 0; i < hallArray.length(); i++) {
				JSONObject json = hallArray.getJSONObject(i);
				JSONArray imageArray = new JSONArray();
				for (BallImage image : allImages.get(i)) {
					StringBuilder sb = new StringBuilder(
							Constant.URL_SERVER_IMAGE);
					sb.append("/" + image.getImageName());
					imageArray.put(sb.toString());
				}
				json.put("hallImages", imageArray);
			}
			baseJson.put("halls", hallArray);
			return baseJson.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return Result.generateBaseResult(Result.RESULT_SERVER_ERROR);
	}
}

