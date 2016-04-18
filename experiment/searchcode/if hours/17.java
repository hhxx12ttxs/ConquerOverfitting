package com.bbaron.timetracker.temporal;

import org.joda.time.Duration;
import org.joda.time.Hours;
import org.springframework.util.Assert;

import com.bbaron.timetracker.model.TemporalValue;

public final class TimecardHours extends TemporalValue<Hours> {

    public static final TimecardHours ZERO;
    public static final TimecardHours[] hours;
    
    static {
        hours = new TimecardHours[25];
        hours[0] = new TimecardHours(0);
        ZERO = hours[0];
    }

    private TimecardHours(int hours) {
        super(Hours.hours(hours), 0, 24);
    }
    
    @Override
    protected int nullSafeToInteger(Hours hours) {
        return hours.getHours();
    }

    public static TimecardHours hours(int h) {
        Assert.isTrue(0 <= h && h <= 24, "hours must be [0, 24]");
        if (hours[h] == null) {
            hours[h] = new TimecardHours(h);
        }
        return hours[h];
    }

    @Override
    public Duration toDuration() {
        return Duration.standardHours(toInteger());
    }    
    
}

