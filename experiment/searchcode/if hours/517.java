package exam2011;

/**
 * Created by Kristian on 17/05/15.
 */
public class TimeSlot implements Comparable<TimeSlot> {
    protected final String description;
    protected final DayTime start, end;
    protected final int duration;

    public TimeSlot(String description, int hours, int minutes, int duration) {
        this.description = description;
        this.duration = duration;
        start = new DayTime(hours, minutes);
        end = calculateEndTime(hours, minutes, duration);
    }

    private DayTime calculateEndTime(int hours, int minutes, int duration) {
        int endHours = hours + duration / 60;
        int endMinutes = minutes + duration % 60;
        if (endMinutes > 60) {
            endHours += 1;
            endMinutes = endMinutes % 60;
        }
        return new DayTime(endHours%24, endMinutes);

    }

    public String toString() {
        String startHours = makeDoubleDigits(start.hours);
        String startMinutes = makeDoubleDigits(start.minutes);
        String endHours = makeDoubleDigits(end.hours);
        String endMinutes = makeDoubleDigits(end.minutes);
        return description + "@" + startHours + ":" + startMinutes + "-" + endHours + ":" + endMinutes;


    }

    private String makeDoubleDigits(int time) {
        if (time > 9) {
            return time + "";
        } else {
            return "0" + time;
        }
    }

    public DayTime getStartTime() {
        return start;
    }

    public DayTime getEndTime() {
        return end;
    }

    public int getDuration() {
        return duration;
    }

    public boolean contains(int hours, int minutes) {
        if (this.start.hours > hours && this.end.hours < hours) {
            return true;
        } else if (this.end.hours == hours && this.end.minutes > minutes) {
            return true;
        } else {
            return false;
        }
    }

    public boolean overlaps(TimeSlot timeSlot) {
        return contains(timeSlot.start.hours, timeSlot.start.minutes) || contains(timeSlot.end.hours, timeSlot.end.minutes);
    }

    @Override
    public int compareTo(TimeSlot timeSlot) {
        if (this.start.hours > timeSlot.start.hours) {
            return 1;
        } else if (this.start.hours == timeSlot.start.hours && this.start.minutes < timeSlot.start.minutes) {
            return 1;
        } else if (this.start.hours == timeSlot.start.hours && this.start.minutes == timeSlot.start.minutes && this.end.hours == timeSlot.end.hours && this.end.minutes == timeSlot.end.minutes) {
            return 0;
        } else {
            return -1;
        }
    }
}

