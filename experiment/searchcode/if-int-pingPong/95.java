package com.pingpong.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pingpong.dao.UploadImageDao;
import com.pingpong.utils.Constant;
import com.pingpong.utils.Result;
import com.pingpong.utils.TextUtils;

public class UploadImageService {

	private UploadImageDao dao;

	public UploadImageService() {
		dao = new UploadImageDao();
	}

	public String uploadImage(String imageName, String imageSize,
			String imageUser, String imageType) {
		if (isImageFull(imageUser,imageType)) {
			return Result.generateBaseResult(Result.RESULT_EXCEED_THRESHOLD);
		}

		List<Object> params = new ArrayList<Object>();
		params.add(imageName);
		params.add(imageSize);
		params.add(imageUser);
		params.add(imageType);
		params.add(System.currentTimeMillis());

		int rst = dao.insertImage(params);
		if (rst > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(Constant.URL_SERVER_IMAGE);
			sb.append("/" + imageName);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("imageUrl", sb.toString());
			return Result.makeSimpleResult(Result.RESULT_SUCCESS, map);
		} else {
			return Result.generateBaseResult(Result.RESULT_SERVER_ERROR);
		}
	}

	private boolean isImageFull(String imageUser,String imageType) {
		List<Object> params = new ArrayList<Object>();
		params.add(imageUser);
		params.add(imageType);
		int count = dao.queryImageCount(params);
		if(TextUtils.equals(imageType,Constant.TYPE_FRIEND+"")){
			return count >= Constant.FRIEND_IMAGE_MAX_COUNT;
		}else {
			return count >= Constant.HALL_IMAGE_MAX_COUNT;
		}
		
	}

}

