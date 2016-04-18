package com.artezio.arttime.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.beanutils.BeanUtils;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.exceptions.SaveApprovedHoursException;
import com.artezio.arttime.services.repositories.HoursRepository;
import com.artezio.arttime.web.interceptors.FacesMessage;

@Named
@Stateless
public class HoursService {
	@Inject
	private HoursRepository hoursRepository;
	
	@FacesMessage(onCompleteMessageKey = "message.timesheetIsSaved")
	public void saveManagedHours(Collection<Hours> hours) throws ReflectiveOperationException {
		Map<Employee, List<Hours>> hoursMap = getHoursMap(hours);
		for (Employee employee : hoursMap.keySet()) {
			hoursRepository.lock(employee);
			for (Hours hour : hoursMap.get(employee)) {
				Hours persistedHours = hoursRepository.findHours(hour.getDate(), hour.getEmployee(), hour.getProject(), hour.getType());
				if (persistedHours == null) {
					hoursRepository.create(hour);
				} else {
					BeanUtils.copyProperties(persistedHours, hour);
					hoursRepository.update(persistedHours);
				}
			}
		}
	}
	
	private Map<Employee, List<Hours>> getHoursMap(Collection<Hours> hoursCollection) {
		return hoursCollection
				.stream()
				.collect(Collectors.groupingBy(Hours::getEmployee));
	}
	
	@FacesMessage(onCompleteMessageKey = "message.timesheetIsSaved")
	public void saveReportTime(Collection<Hours> hours) throws SaveApprovedHoursException, ReflectiveOperationException {
		Map<Employee, List<Hours>> hoursMap = getHoursMap(hours);
		for (Employee employee : hoursMap.keySet()) {
			hoursRepository.lock(employee);
			for (Hours hour : hoursMap.get(employee)) {						
				Hours persistedHours = hoursRepository.findHours(hour.getDate(), hour.getEmployee(), hour.getProject(), hour.getType());
				if (persistedHours == null) {
					hoursRepository.create(hour);
				} else {
					if (persistedHours.isApproved()) {
						throw new SaveApprovedHoursException("You try to save hours that already approved.");
					}
					BeanUtils.copyProperties(persistedHours, hour);
					hoursRepository.update(persistedHours);
				}	
			}
		}
	}
}

