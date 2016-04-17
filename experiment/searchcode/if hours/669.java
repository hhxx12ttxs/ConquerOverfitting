package com.medicapital.common.commands.calendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.medicapital.common.commands.entity.SelectCommandResp;
import com.medicapital.common.entities.CalendarEvent;
import com.medicapital.common.entities.WorkHours;

public class SelectCalendarEventCommandResp extends SelectCommandResp<CalendarEvent> {

	private List<WorkHours> workHours = new ArrayList<WorkHours>();

	public SelectCalendarEventCommandResp() {
		super(CalendarEvent.class);
	}

	public void addWorkHours(Collection<WorkHours> workHours) {
		if (workHours != null) {
			this.workHours.addAll(workHours);
		}
	}

	public List<WorkHours> getWorkHours() {
		return workHours;
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder("[");
		string.append("work hours: ").append(workHours.size());
		string.append(", ").append(super.toString());
		string.append("]");
		return string.toString();
	}
}

