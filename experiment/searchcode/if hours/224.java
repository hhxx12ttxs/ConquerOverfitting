package com.medicapital.common.entities.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.medicapital.common.date.DateFactory;
import com.medicapital.common.date.DateFormatter;
import com.medicapital.common.date.DateManager;
import com.medicapital.common.date.DayTimePeriod;
import com.medicapital.common.date.TimePeriod;
import com.medicapital.common.entities.CalendarEvent;
import com.medicapital.common.entities.Day;
import com.medicapital.common.entities.WorkHours;

final public class WorkHoursMap implements Iterable<WorkHours> {

	private static final long serialVersionUID = 5244950749964002718L;
	private List<WorkHours> workHoursList = new ArrayList<WorkHours>();
	private Map<Day, List<WorkHours>> dayWorkHoursMap = new HashMap<Day, List<WorkHours>>();
	private Map<String, List<WorkHours>> specialWorkHoursMap = new HashMap<String, List<WorkHours>>();
	private DateFormatter dateFormatter = DateFactory.createDateFormatter();
	private DateManager dateManager = DateFactory.createDateManager();

	/**
	 * Check whether given date is a day free of work
	 * 
	 * @param date
	 * @return
	 */
	public boolean isFreeDay(Date date) {
		String day = getDateKey(date);
		List<WorkHours> specialWorkHours = specialWorkHoursMap.get(day);
		if (specialWorkHours != null) {
			for (WorkHours workHours : specialWorkHours) {
				if (workHours.isFreeDay()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get work hours for given date.
	 * 
	 * @param date
	 * @return work hours for not free days
	 */
	public List<WorkHours> getWorkHours(Date date) {
		List<WorkHours> workHours = new ArrayList<WorkHours>();
		if (isFreeDay(date)) {
			return workHours;
		}
		String day = getDateKey(date);
		List<WorkHours> specialWorkHours = specialWorkHoursMap.get(day);
		if (specialWorkHours != null) {
			workHours.addAll(specialWorkHours);
		} else {
			List<WorkHours> dayWorkHours = dayWorkHoursMap.get(dateManager.getWeekDay(date));
			if (dayWorkHours != null) {
				workHours.addAll(dayWorkHours);
			}
		}
		return workHours;
	}

	/**
	 * Check whether is working during this event
	 * 
	 * @param <T>
	 * @param event
	 * @return
	 */
	public <T extends CalendarEvent> boolean isWorking(T event) {
		return isWorking(event.getStartTime(), event.getEndTime());
	}

	/**
	 * Check whether is working during given period
	 * 
	 * @param <T>
	 * @param event
	 * @return
	 */
	public boolean isWorking(Date dateFrom, Date dateTo) {
		TimePeriod timePeriod = new TimePeriod(dateFrom, dateTo);
		for (DayTimePeriod dayTimePeriod : timePeriod) {
			String day = getDateKey(dayTimePeriod.getDayDate());
			List<WorkHours> specialWorkHours = specialWorkHoursMap.get(day);
			if (specialWorkHours != null) {
				if (!isWorking(dayTimePeriod, specialWorkHours)) {
					return false;
				}
			} else {
				List<WorkHours> dayWorkHours = dayWorkHoursMap.get(dayTimePeriod.getDay());
				if (!isWorking(dayTimePeriod, dayWorkHours)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Is working in given work hours
	 * 
	 * @param dayTimePeriod
	 * @param dayWorkHours
	 * @return
	 */
	private boolean isWorking(DayTimePeriod dayTimePeriod, List<WorkHours> dayWorkHours) {
		if (dayWorkHours != null) {
			for (WorkHours workHours : dayWorkHours) {
				if (workHours.isFreeDay()) {
					return false;
				} else if (workHours.isSpecialEvent()) {
					Date dateFrom = new Date(workHours.getDateFrom().getTime());
					dateManager.setTimeStamp(dateFrom, workHours.getStartHour(), workHours.getStartMinutes(), 0);
					Date dateTo = new Date(workHours.getDateTo().getTime());
					dateManager.setTimeStamp(dateTo, workHours.getEndHour(), workHours.getEndMinutes(), 0);
					TimePeriod timePeriod = new TimePeriod(dateFrom, dateTo);
					if (timePeriod.contains(dayTimePeriod)) {
						return true;
					}
				} else {
					DayTimePeriod workTimePeriod = new DayTimePeriod(workHours.getDay(), workHours.getStartHour(), workHours.getStartMinutes(), workHours.getEndHour(), workHours.getEndMinutes());
					if (workTimePeriod.contains(dayTimePeriod)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void add(WorkHours workHours) {
		workHoursList.add(workHours);
		if (workHours.isSpecialEvent()) {
			addSpecialDayWork(workHours);
		} else {
			addDayWork(workHours);
		}
	}

	/**
	 * Add not regular work hours like free days, holidays, special events etc.
	 * 
	 * @param workHours
	 */
	private void addSpecialDayWork(WorkHours workHours) {
		String day = getDateKey(workHours.getDateFrom());
		List<WorkHours> specialWorkHours = dayWorkHoursMap.get(day);
		if (specialWorkHours == null) {
			specialWorkHours = new ArrayList<WorkHours>();
			specialWorkHoursMap.put(day, specialWorkHours);
		}
		specialWorkHours.add(workHours);
	}

	private void addDayWork(WorkHours workHours) {
		Day day = workHours.getDay();
		List<WorkHours> dayWorkHours = dayWorkHoursMap.get(day);
		if (dayWorkHours == null) {
			dayWorkHours = new ArrayList<WorkHours>();
			dayWorkHoursMap.put(day, dayWorkHours);
		}
		dayWorkHours.add(workHours);
	}

	public void addAll(Collection<WorkHours> workHoursCollection) {
		for (WorkHours workHours : workHoursCollection) {
			add(workHours);
		}
	}

	private String getDateKey(Date date) {
		return dateFormatter.toDateString(date);
	}

	@Override
	public Iterator<WorkHours> iterator() {
		return workHoursList.iterator();
	}
}

