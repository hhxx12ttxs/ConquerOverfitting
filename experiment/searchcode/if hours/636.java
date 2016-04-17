package org.joesoft.timetogohomelogic.operator;

import org.joesoft.timetogohomelogic.common.CalendarUtil;
import java.util.Calendar;
import java.util.Date;

public class HoursAndMinutes implements Comparable<HoursAndMinutes> {
    private static final int MINUTES_IN_A_HOUR = 60;
    private static final int MAXIMUM_MINUTES = 60;
    private static final int MAXIMUM_HOURS = 24;
    private static final CalendarUtil calendarUtil = new CalendarUtil();
    public int hours;
    public int minutes;

    public static HoursAndMinutes fromDate(Date actualDate) {
        int hour = calendarUtil.getFromDate(Calendar.HOUR_OF_DAY, actualDate);
        int minute = calendarUtil.getFromDate(Calendar.MINUTE, actualDate);
        
        return new HoursAndMinutes(hour, minute);
    }

    public HoursAndMinutes(int hours, int minutes) {
        cantBeBiggerThan(minutes, MAXIMUM_MINUTES);

        this.hours = hours;
        this.minutes = minutes;
    }
    
    public HoursAndMinutes() {
        this(0, 0);
    }

    public HoursAndMinutes minus(HoursAndMinutes other) {
        nullWatch(other);

        int newHours = hours - other.hours;
        int newMinutes = minutes - other.minutes;
        if (newMinutes < 0) {
            newHours--;
            newMinutes += MAXIMUM_MINUTES;
        }
        if (newHours < 0) {
            throw new TimeCantBeNegativeException();
        }
        return new HoursAndMinutes(newHours, newMinutes);
    }

    public HoursAndMinutes plus(HoursAndMinutes other) {
        nullWatch(other);

        int newHours = hours + other.hours;
        int newMinutes = minutes + other.minutes;
        if (newMinutes > MAXIMUM_MINUTES) {
            newHours++;
            newMinutes -= MAXIMUM_MINUTES;
        }

        return new HoursAndMinutes(newHours, newMinutes);
    }

    public HoursAndMinutes divide(double divideBy) {
        int summaMinutes = convertToMinutes(this);
        double divided = summaMinutes / divideBy;
        double rounded = Math.round(divided);

        return convertMinutes(rounded);
    }

    public HoursAndMinutes plusMinutes(int minimumLunchTime) {
        return this.plus(convertMinutes((double) minimumLunchTime));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HoursAndMinutes other = (HoursAndMinutes) obj;
        if (this.hours != other.hours) {
            return false;
        }
        if (this.minutes != other.minutes) {
            return false;
        }
        return true;
    }

    public int compareTo(HoursAndMinutes target) {
        if (hours < target.hours) {
            return -1;
        } else if (hours == target.hours) {
            if (minutes < target.minutes) {
                return -1;
            } else if (minutes == target.minutes) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }

    public boolean isGreaterThan(HoursAndMinutes target) {
        return compareTo(target) > 0;
    }

    @Override
    public String toString() {
        return hours + ":" + minutes;
    }

    private void cantBeBiggerThan(int input, int maximum) {
        if (input > maximum) {
            throw new IllegalArgumentException(
                    String.format("This input cant be bigger than %d!", maximum));
        }
    }

    private HoursAndMinutes convertMinutes(double toConvert) {
        int newHours = (int) Math.floor(toConvert / MINUTES_IN_A_HOUR);
        int newMinutes = (int) (toConvert % MINUTES_IN_A_HOUR);

        return new HoursAndMinutes(newHours, newMinutes);
    }

    private void nullWatch(HoursAndMinutes other) throws IllegalArgumentException {
        if (other == null) {
            throw new IllegalArgumentException("Null parameter is not allowed.");
        }
    }

    public HoursAndMinutes multiply(int by) {
        int convertedMinutes = convertToMinutes(this);
        convertedMinutes *= by;
        
        return minutesToHoursAndMinutes(convertedMinutes);
    }

    private int convertToMinutes(HoursAndMinutes convertable) {
        return (convertable.hours * MINUTES_IN_A_HOUR) + convertable.minutes;
    }

    private HoursAndMinutes minutesToHoursAndMinutes(int minutes) {
        int hoursInMinutes = minutes/MINUTES_IN_A_HOUR;
        int minutesLeft = minutes%MINUTES_IN_A_HOUR;
        
        return new HoursAndMinutes(hoursInMinutes, minutesLeft);
    }

    public static class TimeCantBeNegativeException extends RuntimeException {

        public TimeCantBeNegativeException() {
            super("Time cant turn into negative.");
        }
    }

    static class MaximumHoursExceededException extends RuntimeException {

        public MaximumHoursExceededException() {
            super(String.format("The hour value cant be bigger than %d!", MAXIMUM_HOURS));
        }
    }
}

