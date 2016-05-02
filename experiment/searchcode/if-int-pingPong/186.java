package com.pingpong.service;

import java.util.ArrayList;
import java.util.List;

import com.pingpong.dao.UpdateHallDao;
import com.pingpong.utils.Result;

public class UpdateHallService {

	private UpdateHallDao updateHallDao;

	public UpdateHallService() {
		updateHallDao = new UpdateHallDao();
	}

	public String updateHall(int userId, String hallAddress,
			String hallCharges, String hallPark, String hallPhone) {
		List<Object> params = new ArrayList<Object>();
		params.add(hallAddress);
		params.add(hallCharges);
		params.add(hallPark);
		params.add(hallPhone);
		params.add(userId);
		int rst = updateHallDao.update(params);
		if (rst > 0) {
			return Result.generateBaseResult(Result.RESULT_SUCCESS);
		} else {
			return Result.generateBaseResult(Result.RESULT_SERVER_ERROR);
		}
	}

	public String updateHallLocation(int userId, String hallLatitude,
			String hallLongitude) {
		List<Object> params = new ArrayList<Object>();
		params.add(hallLatitude);
		params.add(hallLongitude);
		params.add(userId);
		int rst = updateHallDao.updateLocation(params);
		if(rst>0){
			return Result.generateBaseResult(Result.RESULT_SUCCESS);
		}else{
			return Result.generateBaseResult(Result.RESULT_SERVER_ERROR);
		}
	}

}

