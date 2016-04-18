package com.intuiture.corp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intuiture.corp.dao.CommonRepository;
import com.intuiture.corp.entity.CompanyClientWorkHours;
import com.intuiture.corp.json.ClientWorkHoursJson;
import com.intuiture.corp.util.TransformDomainToJson;
import com.intuiture.corp.util.TransformJsonToDomain;

@Service
@Transactional
public class ClientWorkHoursService {
	@Autowired
	private CommonRepository commonRepository;

	public Boolean saveClientworkHours(ClientWorkHoursJson clientWorkHoursJson) {
		CompanyClientWorkHours clientWorkHours = null;

		try {
			if (clientWorkHoursJson.getClientWorkHourId() != null) {
				clientWorkHours = (CompanyClientWorkHours) commonRepository.findById(clientWorkHoursJson.getClientWorkHourId(), CompanyClientWorkHours.class);
			} else {
				clientWorkHours = new CompanyClientWorkHours();
			}
			TransformJsonToDomain.getClientWorkHours(clientWorkHours, clientWorkHoursJson);
			if (clientWorkHoursJson.getClientWorkHourId() != null) {
				commonRepository.update(clientWorkHours);
			} else {
				commonRepository.persist(clientWorkHours);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public List<ClientWorkHoursJson> getAllClientWorkHoursList(Integer companyId) {
		List<ClientWorkHoursJson> clientWorkHoursJsonList = null;
		try {
			List<CompanyClientWorkHours> companyClientWorkHoursList = (List<CompanyClientWorkHours>) commonRepository.getAllRecordsByCompanyId(companyId, CompanyClientWorkHours.class);
			if (companyClientWorkHoursList != null && companyClientWorkHoursList.size() > 0) {
				clientWorkHoursJsonList = new ArrayList<ClientWorkHoursJson>();
				for (CompanyClientWorkHours companyClientWorkHours : companyClientWorkHoursList) {
					clientWorkHoursJsonList.add(TransformDomainToJson.getClientWorkHoursJson(companyClientWorkHours));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clientWorkHoursJsonList;
	}

	public Boolean deleteClientWorkHours(Integer clientWorkHourId) {
		try {
			CompanyClientWorkHours companyClientWorkHours = (CompanyClientWorkHours) commonRepository.findById(clientWorkHourId, CompanyClientWorkHours.class);
			if (companyClientWorkHours != null) {
				companyClientWorkHours.setIsDeleted(Boolean.TRUE);
				commonRepository.update(companyClientWorkHours);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}

