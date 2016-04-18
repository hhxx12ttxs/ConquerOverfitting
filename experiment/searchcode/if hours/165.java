package io.tasteit.rest.service.model;

import java.util.List;

public class WeekdaysOpenHours {
    private List<WeekdaysOpenHour> weekdaysOpenHours;

    public List<WeekdaysOpenHour> getWeekdaysOpenHours() {
        return weekdaysOpenHours;
    }

    public void setWeekdaysOpenHours(List<WeekdaysOpenHour> weekdaysOpenHours) {
        this.weekdaysOpenHours = weekdaysOpenHours;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((weekdaysOpenHours == null) ? 0 : weekdaysOpenHours
                        .hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WeekdaysOpenHours other = (WeekdaysOpenHours) obj;
        if (weekdaysOpenHours == null) {
            if (other.weekdaysOpenHours != null)
                return false;
        } else if (!weekdaysOpenHours.equals(other.weekdaysOpenHours))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "WeekdaysOpenHours [weekdaysOpenHours=" + weekdaysOpenHours
                + "]";
    }
}

