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
package ds2.taskerville.business.impl;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import ds2.taskerville.api.TimeAmount;
import ds2.taskerville.api.TimeAmountPrefs;
import ds2.taskerville.api.remote.TimeAmountDto;
import ds2.taskerville.api.svc.TimeAmountService;

/**
 * The base implemenation of the timeamount service.
 * 
 * @author dstrauss
 * @version 0.1
 */
@Singleton
public class TimeAmountServiceImpl implements TimeAmountService {
    /**
     * A logger.
     */
    private static final Logger LOG = Logger
        .getLogger(TimeAmountServiceImpl.class.getName());
    /**
     * The number 60.
     */
    private static final int SIXTY = 60;
    /**
     * Some preferences.
     */
    @Inject
    private TimeAmountPrefs prefs;
    
    /**
     * Inits the impl.
     */
    public TimeAmountServiceImpl() {
        // nothing special to do
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final TimeAmount createTimeAmount(final int m, final Integer h,
        final Integer d, final Integer w) {
        int hours = parseInt(0, h);
        int days = parseInt(0, d);
        int weeks = parseInt(0, w);
        int minutes = m;
        
        if (minutes >= SIXTY) {
            LOG.warning("Minutes must be recalculated!");
            final int count = minutes / SIXTY;
            hours += count;
            minutes -= hours * SIXTY;
        }
        if (hours >= prefs.getNumHourPerDay()) {
            LOG.warning("Hours must be recalculated!");
            final int count = hours / prefs.getNumHourPerDay();
            days += count;
            hours -= days * prefs.getNumHourPerDay();
        }
        if (days >= prefs.getNumDaysPerWeek()) {
            LOG.warning("Days must be recalculated!");
            final int count = days / prefs.getNumDaysPerWeek();
            weeks += count;
            days -= weeks * prefs.getNumDaysPerWeek();
        }
        final TimeAmountDto rc = new TimeAmountDto();
        rc.setMinutes(minutes);
        rc.setHours(hours);
        rc.setDays(days);
        rc.setWeeks(weeks);
        return rc;
    }
    
    /**
     * Parses a given Integer object.
     * 
     * @param def
     *            a default value to return in case of null
     * @param v
     *            the Integer object to parse
     * @return the default value, or the int value from the given Integer object
     */
    private int parseInt(final int def, final Integer v) {
        int rc = def;
        if (v != null) {
            rc = v.intValue();
        }
        return rc;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final TimeAmount parseTimeAmount(final String s) {
        final int minutes = 0;
        final Integer hours = null;
        final Integer days = null;
        final Integer weeks = null;
        final TimeAmount rc = createTimeAmount(minutes, hours, days, weeks);
        return rc;
    }
}

