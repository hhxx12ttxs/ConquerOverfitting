package com.medicapital.server.database.hibernate.transform;

import java.util.Date;
import com.medicapital.common.entities.Day;
import com.medicapital.common.entities.WorkHours;
import com.medicapital.server.database.hibernate.entities.WorkHoursEntity;

public class WorkHoursTransformer implements Transformer<WorkHours, WorkHoursEntity> {

	@Override
	public WorkHours createPojo(WorkHoursEntity persistenceEntity) {
		if (persistenceEntity == null) {
			return null;
		}
		WorkHours workHours = new WorkHours();
		workHours.setId(persistenceEntity.getWorkHoursId());
		workHours.setDoctorId(persistenceEntity.getDoctorId());
		workHours.setStartHour(persistenceEntity.getStartHour());
		workHours.setStartMinutes(persistenceEntity.getStartMinutes());
		workHours.setEndHour(persistenceEntity.getEndHour());
		workHours.setEndMinutes(persistenceEntity.getEndMinutes());
		workHours.setDay(persistenceEntity.getDay() != null ? Day.valueOf(persistenceEntity.getDay()) : null);
		workHours.setDateFrom(persistenceEntity.getDateFrom() != null ? new Date(persistenceEntity.getDateFrom().getTime()) : null);
		workHours.setDateTo(persistenceEntity.getDateTo() != null ? new Date(persistenceEntity.getDateTo().getTime()) : null);
		workHours.setDescription(persistenceEntity.getDescription());
		if (persistenceEntity.getLocalization() != null) {
			workHours.setLocalization(new LocalizationTransformer().createPojo(persistenceEntity.getLocalization()));
		}
		return workHours;
	}

	@Override
	public WorkHoursEntity createEntity(WorkHours pojoEntity) {
		if (pojoEntity == null) {
			return null;
		}
		WorkHoursEntity workHours = new WorkHoursEntity();
		workHours.setWorkHoursId(pojoEntity.getId());
		workHours.setDoctorId(pojoEntity.getDoctorId());
		workHours.setStartHour(pojoEntity.getStartHour());
		workHours.setStartMinutes(pojoEntity.getStartMinutes());
		workHours.setEndHour(pojoEntity.getEndHour());
		workHours.setEndMinutes(pojoEntity.getEndMinutes());
		workHours.setDay(pojoEntity.getDay() != null ? pojoEntity.getDay().toString() : null);
		workHours.setDateFrom(pojoEntity.getDateFrom());
		workHours.setDateTo(pojoEntity.getDateTo());
		workHours.setDescription(pojoEntity.getDescription());
		if (pojoEntity.getLocalization() != null) {
			workHours.setLocalization(new LocalizationTransformer().createEntity(pojoEntity.getLocalization()));
		}
		return workHours;
	}

	@Override
	public WorkHoursEntity createEntity(int entityId) {
		WorkHoursEntity workHoursEntity = new WorkHoursEntity();
		workHoursEntity.setWorkHoursId(entityId);
		return workHoursEntity;
	}

}

