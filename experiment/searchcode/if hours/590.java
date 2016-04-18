package service;

import java.util.ArrayList;
import java.util.List;

import models.HoursWorked;
import models.StateHoursWorked;
import models.User;
import views.data.HoursWorkedDto;
import views.data.UserDto;
import daos.impl.DAOs;

public class HoursWorkedConverter {
	
	public static HoursWorked convertToEntity(HoursWorkedDto orig) {
		HoursWorked res = new HoursWorked();
		res.setDescription(orig.getDescription());
		res.setHoursWorkedId(orig.getHoursWorkedId());
		res.setNumberOfHours(orig.getNumberOfHours());
		res.setProject(DAOs.getProjectDao().findById(orig.getProjectId()));
		res.setTimeFrom(orig.getTimeFrom());
		res.setTimeTo(orig.getTimeTo());
		return res;
	}
	
	public static HoursWorkedDto convertToDto(HoursWorked orig) {
		HoursWorkedDto res = new HoursWorkedDto();
		res.setDescription(orig.getDescription());
		res.setHoursWorkedId(orig.getHoursWorkedId());
		res.setNumberOfHours(orig.getNumberOfHours());
		res.setTimeFrom(orig.getTimeFrom());
		res.setTimeTo(orig.getTimeTo());
		User userEntity = orig.getUser();
		if (userEntity != null) {
			UserDto user = new UserDto();
			user.setId(userEntity.getId());
			user.setFirstName(userEntity.getFirstName());
			user.setLastName(userEntity.getLastName());
			res.setUser(user);
		}
		StateHoursWorked stateHoursWorked = orig.getStateHoursWorked();
		if (stateHoursWorked != null) {
			res.setStateHoursWorkedKey(stateHoursWorked.getKey());
		}
		return res;
	}
	
	public static List<HoursWorkedDto> convertListToDto(List<HoursWorked> orig) {
		List<HoursWorkedDto> res = new ArrayList<HoursWorkedDto>();
		if (orig != null) {
			for (HoursWorked hw : orig) {
				res.add(convertToDto(hw));
			}
		}
		return res;
	}
}

