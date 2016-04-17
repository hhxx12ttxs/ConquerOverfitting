package com.medicapital.client.doctor.work;

import java.util.Collection;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.NumberFormat;
import com.medicapital.client.core.entities.EntitiesPresenter;
import com.medicapital.common.date.DateFactory;
import com.medicapital.common.entities.WorkHours;

public class WorkHoursListPresenter extends EntitiesPresenter<WorkHours> {

	private final WorkHoursListView view;

	public WorkHoursListPresenter(WorkHoursListView view, EventBus eventBus) {
		super(WorkHours.class, view, eventBus);
		this.view = view;
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

}

