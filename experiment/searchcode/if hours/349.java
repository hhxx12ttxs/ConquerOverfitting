package com.intuiture.corp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intuiture.corp.dao.CommonRepository;
import com.intuiture.corp.entity.DeptWorkHours;
import com.intuiture.corp.json.DeptWorkHoursJson;
import com.intuiture.corp.util.TransformDomainToJson;
import com.intuiture.corp.util.TransformJsonToDomain;

@Service
@Transactional
public class DeptWorkHoursService {
	@Autowired
	private CommonRepository commonRepository;

	public Boolean saveDeptWorkHours(DeptWorkHoursJson deptWorkHoursJson) {
		DeptWorkHours deptWorkHours = null;

		try {
			if (deptWorkHoursJson.getDeptWorkHourId() != null) {
				deptWorkHours = (DeptWorkHours) commonRepository.findById(deptWorkHoursJson.getDeptWorkHourId(), DeptWorkHours.class);
			} else {
				deptWorkHours = new DeptWorkHours();
			}
			TransformJsonToDomain.getDeptWorkHours(deptWorkHours, deptWorkHoursJson);
			if (deptWorkHoursJson.getDeptWorkHourId() != null) {
				commonRepository.update(deptWorkHours);
			} else {
				commonRepository.persist(deptWorkHours);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public List<DeptWorkHoursJson> getAllDeptWorkHoursList(Integer companyId) {
		List<DeptWorkHoursJson> deptWorkHoursJsonList = null;
		try {
			List<DeptWorkHours> deptWorkHoursList = (List<DeptWorkHours>) commonRepository.getAllRecordsByCompanyId(companyId, DeptWorkHours.class);
			if (deptWorkHoursList != null && deptWorkHoursList.size() > 0) {
				deptWorkHoursJsonList = new ArrayList<DeptWorkHoursJson>();
				for (DeptWorkHours deptWorkHours : deptWorkHoursList) {
					deptWorkHoursJsonList.add(TransformDomainToJson.getDeptWorkHoursJson(deptWorkHours));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return deptWorkHoursJsonList;
	}

	public Boolean deleteDeptWorkHours(Integer deptWorkHourId) {
		try {
			DeptWorkHours deptWorkHours = (DeptWorkHours) commonRepository.findById(deptWorkHourId, DeptWorkHours.class);
			if (deptWorkHours != null) {
				deptWorkHours.setIsDeleted(Boolean.TRUE);
				commonRepository.update(deptWorkHours);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}

