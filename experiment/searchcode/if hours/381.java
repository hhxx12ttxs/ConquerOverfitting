package org.joda.time;

import java.util.Calendar;

import org.joda.time.base.BaseSingleFieldPeriod;
import org.joda.time.field.FieldUtils;



public class Hours extends BaseSingleFieldPeriod {
	
	public static Hours hoursBetween(ReadableInstant start, ReadableInstant end) {
		return new Hours(Minutes.minutesBetween(start, end).getValue() / 60);
	}
	
	private Hours(int hours) {
        super(hours, Calendar.HOUR_OF_DAY);
    }
	
	public Hours multipliedBy(int scalar) {
        return new Hours(FieldUtils.safeMultiply(getValue(), scalar));
    }

    public Hours dividedBy(int divisor) {
        if (divisor == 1) {
            return this;
        }
        return new Hours(getValue() / divisor);
    }
    
    public int getHours() {
    	return getValue();
    }
	
}

