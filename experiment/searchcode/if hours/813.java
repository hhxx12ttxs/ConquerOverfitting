package ru.droogcompanii.application.data.data_structure.working_hours.working_hours_impl;

import ru.droogcompanii.application.data.data_structure.working_hours.Time;
import ru.droogcompanii.application.data.data_structure.working_hours.WorkingHours;

/**
 * Created by Leonid on 19.12.13.
 */
public class WorkingHoursBuilder {
    private static final String MESSAGE_FOR_DAY_AND_NIGHT_WORKING_HOURS = "Круглосуточно";

    private static final Time TIME_00_00 = new Time(0, 0);

    public WorkingHours onBusinessDay(Time from, Time to) {
        if (dayAndNightWorkingHours(from, to)) {
            return new DayAndNightWorkingHours(MESSAGE_FOR_DAY_AND_NIGHT_WORKING_HOURS);
        }
        return new WorkingHoursOnBusinessDay(from, to);
    }

    private boolean dayAndNightWorkingHours(Time from, Time to) {
        return from.equals(TIME_00_00) && to.equals(TIME_00_00);
    }

    public WorkingHours onHoliday(String messageToShowInHoliday) {
        return new WorkingHoursOnHoliday(messageToShowInHoliday);
    }
}

