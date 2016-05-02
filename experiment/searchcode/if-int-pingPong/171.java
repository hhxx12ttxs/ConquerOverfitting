package com.pingpong.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pingpong.dao.DeleteImageDao;
import com.pingpong.utils.Constant;
import com.pingpong.utils.Result;
import com.pingpong.utils.TextUtils;

public class DeleteImageService {

	private DeleteImageDao dao;

	public DeleteImageService() {
		dao = new DeleteImageDao();
	}

	public String deleteImage(String userId, String imageUrl) {
		if (TextUtils.isEmpty(userId)) {
			return Result.generateBaseResult(Result.RESULT_NEED_LOGIN_ID);
		}

		if (TextUtils.isEmpty(imageUrl)) {
			return Result.generateBaseResult(Result.RESULT_ARGUMENT_ILLEGAL);
		}

		List<Object> params = new ArrayList<Object>();
		String[] temp = imageUrl.split("/");
		String imageName = temp[temp.length - 1];
		params.add(userId);
		params.add(imageName);
		int rst = dao.delete(params);
		if (rst > 0) {
			deleteFromDisk(imageName);
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

	private void deleteFromDisk(String imageName) {
		new Thread(new Runnable() {
			public void run() {
				// TODO : delete from disk
			}
		}).start();
	}

}

