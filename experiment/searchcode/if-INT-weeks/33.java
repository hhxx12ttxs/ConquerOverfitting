/*
 * TaskerVille - issue and project management
 * Copyright (C) 2012  Dirk Strauss
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ds2.taskerville.api.remote;

import ds2.taskerville.api.TimeAmount;

/**
 * A basic implementation of the TimeAmount.
 * 
 * @author dstrauss
 * @version 0.1
 * 
 */
public class TimeAmountDto implements TimeAmount {
    
    /**
     * the svuid.
     */
    private static final long serialVersionUID = -4215857869210911223L;
    /**
     * The days.
     */
    private int days;
    /**
     * The hours.
     */
    private int hours;
    /**
     * The minutes.
     */
    private int minutes;
    /**
     * The weeks.
     */
    private int weeks;
    
    /**
     * Constructs an empty time amount.
     */
    public TimeAmountDto() {
        // nothing to do
    }
    
    /**
     * Constructs a time amount with the given parameters.
     * 
     * @param min
     *            the minutes
     * @param h
     *            the hours
     * @param d
     *            the days
     * @param w
     *            the weeks
     */
    public TimeAmountDto(final int min, final int h, final int d, final int w) {
        this();
        setMinutes(min);
        setHours(h);
        setDays(d);
        setWeeks(w);
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final TimeAmount o) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TimeAmountDto)) {
            return false;
        }
        final TimeAmountDto other = (TimeAmountDto) obj;
        if (days != other.days) {
            return false;
        }
        if (hours != other.hours) {
            return false;
        }
        if (minutes != other.minutes) {
            return false;
        }
        if (weeks != other.weeks) {
            return false;
        }
        return true;
    }
    
    /*
     * (non-Javadoc)
     * @see ds2.taskerville.api.TimeAmount#getDays()
     */
    @Override
    public int getDays() {
        return days;
    }
    
    /*
     * (non-Javadoc)
     * @see ds2.taskerville.api.TimeAmount#getHours()
     */
    @Override
    public int getHours() {
        return hours;
    }
    
    /*
     * (non-Javadoc)
     * @see ds2.taskerville.api.TimeAmount#getMinutes()
     */
    @Override
    public int getMinutes() {
        return minutes;
    }
    
    /*
     * (non-Javadoc)
     * @see ds2.taskerville.api.TimeAmount#getParsedString()
     */
    @Override
    public String getParsedString() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public float getPercentComparedTo(final TimeAmount t) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    /*
     * (non-Javadoc)
     * @see ds2.taskerville.api.TimeAmount#getWeeks()
     */
    @Override
    public int getWeeks() {
        return weeks;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + days;
        result = prime * result + hours;
        result = prime * result + minutes;
        result = prime * result + weeks;
        return result;
    }
    
    /**
     * Sets the days.
     * 
     * @param days
     *            the days to set
     */
    public synchronized void setDays(final int days) {
        this.days = days;
    }
    
    /**
     * Sets the hours.
     * 
     * @param hours
     *            the hours to set
     */
    public synchronized void setHours(final int hours) {
        if (hours < 0) {
            return;
        }
        this.hours = hours;
    }
    
    /**
     * Sets the minutes.
     * 
     * @param minutes
     *            the minutes to set
     */
    public synchronized void setMinutes(final int minutes) {
        if (minutes < 0 || minutes >= 60) {
            return;
        }
        this.minutes = minutes;
    }
    
    /**
     * Sets the weeks.
     * 
     * @param weeks
     *            the weeks to set
     */
    public synchronized void setWeeks(final int weeks) {
        if (weeks < 0) {
            return;
        }
        this.weeks = weeks;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuffer builder = new StringBuffer();
        builder.append("TimeAmountDto [days=");
        builder.append(days);
        builder.append(", hours=");
        builder.append(hours);
        builder.append(", minutes=");
        builder.append(minutes);
        builder.append(", weeks=");
        builder.append(weeks);
        builder.append("]");
        return builder.toString();
    }
    
}

