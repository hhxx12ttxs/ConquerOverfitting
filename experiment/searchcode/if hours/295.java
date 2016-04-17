
package com.dilax.aq.ajax.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.dilax.aq.persistence.entities.HoursInterval;
import com.dilax.aq.persistence.entities.Queue;

public class ExternalHours {

    private Integer from;

    private Integer length;

    public ExternalHours() {

    }

    public ExternalHours(Integer from, Integer length) {
        this.from = from;
        this.length = length;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public static List<HoursInterval> createHours(Queue queue, List<ExternalHours> hours) {
        List<HoursInterval> newHours = new ArrayList<HoursInterval>();
        for (ExternalHours externalHours : hours) {
            newHours.add(new HoursInterval(externalHours.getFrom(), externalHours.getLength(), queue));
        }
        return newHours;
    }

    public static List<ExternalHours> createFrom(List<HoursInterval> hours) {
        List<ExternalHours> extHours = new ArrayList<ExternalHours>();
        for (HoursInterval hoursInterval : hours) {
            extHours.add(new ExternalHours(hoursInterval.getFrom(), hoursInterval.getLength()));
        }
        return extHours;
    }

    public static List<ExternalHours> createBreaks(List<HoursInterval> hours) {
        List<HoursInterval> hoursCopy = new ArrayList<HoursInterval>(hours);
        List<ExternalHours> breaks = new ArrayList<ExternalHours>();
        Collections.sort(hoursCopy);

        int lastFrom = 0;
        for (HoursInterval hoursInterval : hoursCopy) {
            addNonZeroBreak(breaks, lastFrom, hoursInterval.getFrom());
            lastFrom = hoursInterval.getFrom() + hoursInterval.getLength();
        }
        addNonZeroBreak(breaks, lastFrom, 1440);

        return breaks;
    }

    private static void addNonZeroBreak(List<ExternalHours> breaks, Integer from, Integer to) {
        int breakLength = to - from;
        if (breakLength > 0) {
            breaks.add(new ExternalHours(from, breakLength));
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this).toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}

