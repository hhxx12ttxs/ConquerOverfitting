package com.medicapital.client.doctor.work;

import java.util.Collection;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.NumberFormat;
import com.medicapital.client.core.entities.EditableEntitiesPresenter;
import com.medicapital.common.date.DateFactory;
import com.medicapital.common.entities.WorkHours;

public class EditableWorkHoursListPresenter extends EditableEntitiesPresenter<WorkHours> {

	private final EditableWorkHoursListView view;
	private CreateWorkHoursPresenter createWorkHoursPresenter;
	private EditWorkHoursPresenter editWorkHoursPresenter;
	private int doctorId;

	public EditableWorkHoursListPresenter(EditableWorkHoursListView view, EventBus eventBus) {
		super(WorkHours.class, view, eventBus);
		this.view = view;
	}

	@Override
	public void clearPresenter() {
		super.clearPresenter();
		if (createWorkHoursPresenter != null) {
			createWorkHoursPresenter.clearPresenter();
		}
		if (editWorkHoursPresenter != null) {
			editWorkHoursPresenter.clearPresenter();
		}
	}

	public void init(int doctorId) {
		this.doctorId = doctorId;
	}

	@Override
	protected void handleCreatedEntity(WorkHours createdEntity) {
		super.handleCreatedEntity(createdEntity);
		refreshDisplay();
	}

	@Override
	protected void displayDataOnView(Collection<WorkHours> data) {
		NumberFormat timeFormat = DateFactory.getTimeFormat();
		for (WorkHours workHours : data) {
			StringBuilder workTime = new StringBuilder();
			workTime.append(timeFormat.format(workHours.getStartHour())).append(":").append(timeFormat.format(workHours.getStartMinutes()));
			workTime.append('-');
			workTime.append(timeFormat.format(workHours.getEndHour())).append(":").append(timeFormat.format(workHours.getEndMinutes()));
			String address = workHours.getLocalization() != null ? workHours.getLocalization().getAddress() : "";
			if (workHours.getDateFrom() != null) {
				view.display(workHours.getId(), workHours.getDateFrom(), workHours.getDateTo(), workTime.toString(), address, workHours.getDescription());
			} else {
				view.display(workHours.getId(), workHours.getDay(), workTime.toString(), address, workHours.getDescription());
			}
		}
	}

	public void createWorkHours() {
		if (createWorkHoursPresenter == null) {
			throw new IllegalArgumentException("Create work hours presenter not set");
		} else if (doctorId <= 0) {
			throw new IllegalArgumentException("DoctorId is not set");
		}
		WorkHours workHours = new WorkHours();
		workHours.setDoctorId(doctorId);
		createWorkHoursPresenter.display(workHours);
	}

	public void editWorkHours(int workHourId) {
		if (editWorkHoursPresenter == null) {
			throw new IllegalArgumentException("Edit work hours presenter not set");
		} else if (!getDisplayedElements().containsKey(workHourId)) {
			throw new IllegalArgumentException("Work hours not displayed on this presenter - workHourId: " + workHourId);
		}
		WorkHours workHours = getDisplayedElements().get(workHourId);
		editWorkHoursPresenter.display(workHours);
	}

	public void setCreateWorkHoursPresenter(CreateWorkHoursPresenter createWorkHoursPresenter) {
		this.createWorkHoursPresenter = createWorkHoursPresenter;
	}

	public CreateWorkHoursPresenter getCreateWorkHoursPresenter() {
		return createWorkHoursPresenter;
	}

	public void setEditWorkHoursPresenter(EditWorkHoursPresenter editWorkHoursPresenter) {
		this.editWorkHoursPresenter = editWorkHoursPresenter;
	}

	public EditWorkHoursPresenter getEditWorkHoursPresenter() {
		return editWorkHoursPresenter;
	}

}

