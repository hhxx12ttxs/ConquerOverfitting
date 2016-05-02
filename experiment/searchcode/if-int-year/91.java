/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fw.time;

/**
 *
 * @author mohsen
 */
public class DateTime {

    // <editor-fold defaultstate="collapsed" desc="Variables">
    protected int year = 0;
    protected int month = 0;
    protected int day = 0;
    protected int hour = 0;
    protected int minute = 0;
    protected int second = 0;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public DateTime() {
    }

    public DateTime(String date) {
        date = date.replace("/", "-");
        String[] main = date.split(" ");
        String[] dates = main[0].split("-");
        this.year = Integer.valueOf(dates[0]);
        this.month = Integer.valueOf(dates[1]);
        this.day = Integer.valueOf(dates[2]);
        if (main.length > 1) {
            String[] times = main[1].split(":");
            this.hour = Integer.valueOf(times[0]);
            this.minute = Integer.valueOf(times[1]);
            this.second = Float.valueOf(times[2]).intValue();
        }

    }

    public DateTime(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public DateTime(int year, int month, int day, int hour, int minute, int second) {
        this(year, month, day);
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Properties">
    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getMonth() {
        return month;
    }

    public int getSecond() {
        return second;
    }

    public int getYear() {
        return year;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public void setYear(int year) {
        this.year = year;
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Formatting">
    @Override
    public String toString() {
        if (hour != 0 || minute != 0) {
            return this.toDateString() + " " + this.toTimeString();
        } else {
            return this.toDateString();
        }
    }

    public String toDateString() {
        return year + "/" + month + "/" + day;
    }

    public String toTimeString() {
        return hour + ":" + minute + ":" + second;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Utils Methods">
    public long getTimeStamp() {
        
        return year * month * day * hour * day * second;  //@todo -> get Time Stamp
    }
//    @Override
//    public long getTimeStamp() {
//        try {
//            EnglishDate ed = Convert(DateTypes.Gregorian);
//            Calendar cal = Calendar.getInstance();
//            cal.clear();
//
//            cal.set(Calendar.YEAR, ed.getYear());
//            cal.set(Calendar.MONTH, ed.getMonth());
//            cal.set(Calendar.DATE, ed.getDay());
//
//            return cal.getTimeInMillis();
//
//        } catch (Exception ex) {
//            return -1;
//        }
//    }

    public boolean after(DateTime dt) {
        return this.getTimeStamp() > dt.getTimeStamp();
    }

    public boolean before(DateTime dt) {
        return this.getTimeStamp() < dt.getTimeStamp();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Value Of">
    public static DateTime valueOf(String date) {
        return new DateTime(date);
    }

    public static DateTime valueOf(long timestamp) {
        return new DateTime();
    }
    // </editor-fold>
}

