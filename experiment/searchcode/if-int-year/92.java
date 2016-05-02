/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fw.time;

import java.util.Calendar;

/**
 *
 * @author ghasedak
 */
public class EnglishDate extends DateTime {
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public EnglishDate(int year, int month, int day, int hour, int minute, int second) {
        super(year, month, day, hour, minute, second);
    }

    public EnglishDate(String date) {
        super(date);
    }

    public EnglishDate(int year, int month, int day) {
        super(year, month, day);
    }

    public EnglishDate() {
    }

    // </editor-fold>
    
    public static EnglishDate valueOf(DateTime pd) {
        EnglishDate dt = null;
        int miladiYear, i, dayCount, remainDay, marchDayDiff;
        // this buffer has day count of Miladi month from April to January for a none year.
        int[] miladiMonth = {30, 31, 30, 31, 31, 30, 31, 30, 31, 31, 28, 31};
        miladiYear = pd.year + 621;
        //Detemining the Farvardin the First
        //this is a Miladi leap year so Shamsi is leap too so the 1st of Farvardin is March 20 (3/20)
        //this is not a Miladi leap year so Shamsi is not leap too so the 1st of Farvardin is March 21 (3/21)
        // If next year is leap we will add one day to Feb.
        int milady_leap_year = miladiYear + 1;
        if (((miladiYear % 100) != 0 && (miladiYear % 4) == 0) || ((miladiYear % 100) == 0 && (miladiYear % 400) == 0)) {
            marchDayDiff = 12;
        } else {
            marchDayDiff = 11;
        }
        if (((milady_leap_year % 100) != 0 && (milady_leap_year % 4) == 0) || ((milady_leap_year % 100) == 0 && (milady_leap_year % 400) == 0)) {
            miladiMonth[10] = miladiMonth[10] + 1; //Adding one day to Feb
        }
        //Calculate the day count for input shamsi date from 1st Farvadin
        if ((pd.month >= 1) && (pd.month <= 6)) {
            dayCount = ((pd.month - 1) * 31) + pd.day;
        } else {
            dayCount = (6 * 31) + ((pd.month - 7) * 30) + pd.day;
        }
        //Finding the correspond miladi month and day
        if (dayCount <= marchDayDiff) //So we are in 20(for leap year) or 21for none leap year) to 31 march
        {
            dt = new EnglishDate(miladiYear, 3, dayCount + (31 - marchDayDiff), pd.hour, pd.minute, pd.second);
        } else {
            remainDay = dayCount - marchDayDiff;
            i = 0; //starting from April
            while ((remainDay > miladiMonth[i])) {
                remainDay = remainDay - miladiMonth[i];
                i++;
            }
            //  miladiDate.setDate(remainDay);
            if (i > 8) // We are in the next Miladi Year
            {
                dt = new EnglishDate(miladiYear + 1, i - 8, remainDay, pd.hour, pd.minute, pd.second);
            } else {
                dt = new EnglishDate(miladiYear, i + 4, remainDay, pd.hour, pd.minute, pd.second);
            }
        }
        return (EnglishDate) dt;
    }

    public static EnglishDate Now() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        int second = cal.get(Calendar.SECOND);
        int hour = cal.get(Calendar.HOUR);
        int minute = cal.get(Calendar.MINUTE);
        return new EnglishDate(year, month, day, hour, minute, second);
    }
}

