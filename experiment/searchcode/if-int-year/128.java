package fw.time;

import java.util.Calendar;

public class PersianDate extends DateTime {

    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public PersianDate(String date) {
        super(date);
    }

    public PersianDate(int year, int month, int day) {
        super(year, month, day);
    }

    public PersianDate(int year, int month, int day, int hour, int minute, int second) {
        super(year, month, day, hour, minute, second);
    }
     // </editor-fold>
    
    public static PersianDate valueOf(DateTime ed) {
        int g_days_in_month[] = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int j_days_in_month[] = new int[]{31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29};
        int i;
        int gy = ed.year - 1600;
        int gm = ed.month - 1;
        int gd = ed.day - 1;
        int g_day_no = 365 * gy + (int) ((gy + 3) / 4) - (int) ((gy + 99) / 100) + ((int) ((gy + 399) / 400));
        for (i = 0; i < gm; ++i) {
            g_day_no += g_days_in_month[i];
        }
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            g_day_no++;
        }
        g_day_no += gd;
        int j_day_no = g_day_no - 79;
        int j_np = (int) (j_day_no / 12053);
        j_day_no = j_day_no % 12053;
        int jy = 979 + 33 * j_np + 4 * (int) (j_day_no / 1461);
        j_day_no %= 1461;
        if (j_day_no >= 366) {
            jy += (int) ((j_day_no - 1) / 365);
            j_day_no = (j_day_no - 1) % 365;
        }
        for (i = 0; i < 11 && j_day_no >= j_days_in_month[i]; ++i) {
            j_day_no -= j_days_in_month[i];
        }
        int jm = i + 1;
        j_day_no++;
   
        return new PersianDate(jy, jm, j_day_no, ed.hour, ed.minute, ed.second);
     
    }

    public static PersianDate Now() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        int second = cal.get(Calendar.SECOND);
        int hour =cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        return PersianDate.valueOf(new DateTime(year,month,day,hour,minute,second));
    }
    
}

