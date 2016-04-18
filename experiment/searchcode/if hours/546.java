package ru.droogcompanii.application.data.working_hours.parser;

import ru.droogcompanii.application.data.working_hours.WorkingHours;
import ru.droogcompanii.application.data.working_hours.working_hours_impl.DayAndNightWorkingHours;
import ru.droogcompanii.application.data.working_hours.working_hours_impl.WorkingHoursOnHoliday;
import ru.droogcompanii.application.util.constants.StringConstants.PartnersXmlConstants.TypesOfDay;

/**
 * Created by Leonid on 19.12.13.
 */
public class WorkingHoursParser {

    public WorkingHours parse(String typeOfDay, String text) {
        if (typeOfDay.equals(TypesOfDay.usualDay)) {
            WorkingHoursOnBusinessDayParser parser = new WorkingHoursOnBusinessDayParser();
            return parser.parse(text);
        } else if (typeOfDay.equals(TypesOfDay.dayAndNight)) {
            return new DayAndNightWorkingHours(text);
        } else if (typeOfDay.equals(TypesOfDay.holiday)) {
            return new WorkingHoursOnHoliday(text);
        }
        throw new IllegalArgumentException("Unknown type of day: " + typeOfDay);
    }
}

