/*
 * Copyright 2011-2012 Joonas Keturi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.volunteer;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.vaadin.common.Support;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import org.vaadin.risto.stylecalendar.StyleCalendar;

public class Reservation implements Serializable {
    private final static long serialVersionUID = 0L;

    protected VolunteerApplication volunteerApplication = null;
    protected Calendar currentCalendar = null;

    protected final TreeMap<Date, Integer> disabledMap = new TreeMap<Date, Integer>();
    protected final TreeMap<Date, Integer> yellowMap = new TreeMap<Date, Integer>();
    protected final TreeMap<Date, Integer> greenMap = new TreeMap<Date, Integer>();
    protected final TreeMap<Date, Integer> redMap = new TreeMap<Date, Integer>();
    protected final StyleCalendar styleCalendar = new StyleCalendar() {
            private final static long serialVersionUID = 0L;

            public void changeVariables(Object source,
                                        Map variables) {
                super.changeVariables(source, variables);
                if (variables.containsKey("nextClick") || variables.containsKey("prevClick")) {
                    fireValueChange(false);
                }
            }

        };
    protected final List<Integer> hours = new ArrayList<Integer>();
    protected final WorkType[] workTypeValues;
    protected final Reservation.WorkReservationComparator workReservationComparator
        = new Reservation.WorkReservationComparator();
    protected final Reservation.WorkReservationList workReservationList = new Reservation.WorkReservationList();
    protected final Reservation.WorkReservationList workReservationStoreList = new Reservation.WorkReservationList();
    protected final Reservation.RegistrationList registrationList = new Reservation.RegistrationList();
    protected final Date currentDate = truncateDate(new Date());

    protected Date date = null;
    protected Integer hour = null;
    protected WorkType workType = null;
    protected Reservation.Search search = new Reservation.Search();

    public static class WorkReservationComparator implements Comparator<WorkReservation> {

        public int compare(WorkReservation workReservation1, WorkReservation workReservation2) {
            int workTypeNameDelta = workReservation1.getWorkTypeName().compareTo(workReservation2.getWorkTypeName());
            return workTypeNameDelta == 0 ? workReservation1.getVolatileDate().compareTo(workReservation2.getVolatileDate()) : workTypeNameDelta;
        }

        public boolean equals(Object obj) {
            return obj.getClass().equals(getClass());
        }

    }

    public static class WorkReservationList extends ArrayList<WorkReservation> {
        private final static long serialVersionUID = 0L;
    }

    public static class RegistrationList extends ArrayList<Registration> {
        private final static long serialVersionUID = 0L;
    }

    public static class Search {
        private final static long serialVersionUID = 0L;

        private Reservation.WorkReservationList workReservationList = new Reservation.WorkReservationList();
        private Reservation.RegistrationList registrationList = new Reservation.RegistrationList();

        public Reservation.WorkReservationList getWorkReservationList() {
            return workReservationList;
        }

        public void setWorkReservationList(final Reservation.WorkReservationList workReservationList) {
            this.workReservationList = workReservationList;
        }

        public Reservation.RegistrationList getRegistrationList() {
            return registrationList;
        }

        public void setRegistrationList(final Reservation.RegistrationList registrationList) {
            this.registrationList = registrationList;
        }

    }

    private Calendar truncateCalendar(final Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private Date truncateDate(final Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return truncateCalendar(calendar).getTime();
    }

    private void resetHours(final List<Integer> hours) {
        hours.clear();
        hours.add(8);
        hours.add(12);
        hours.add(16);
        hours.add(20);
    }

    public Reservation(final VolunteerApplication volunteerApplication) {
        this.volunteerApplication = volunteerApplication;
        currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);
        styleCalendar.setImmediate(true);
        styleCalendar.setDateOptionsGenerator(new StyleCalendar.DateOptionsGenerator() {

                public String getStyleName(Date date,
                                           final StyleCalendar context) {
                    date = truncateDate(date);
                    if (redMap.containsKey(date)) {
                        return "red";
                    }
                    if (yellowMap.containsKey(date)) {
                        return "yellow";
                    }
                    if (greenMap.containsKey(date)) {
                        return "green";
                    }
                    return null;
                }

                public boolean isDateDisabled(Date date,
                                              final StyleCalendar context) {
                    date = truncateDate(date);
                    return date.compareTo(currentDate) <= 0 || disabledMap.containsKey(date);
                }

            });
        workTypeValues = volunteerApplication.workTypeList.toArray(new WorkType[volunteerApplication.workTypeList.size()]);
        resetHours(hours);
    }

    public Reservation.Search getStartSearch() {
        return getStyleCalendarSearch();
    }

    public void setStartSearch(final Reservation.Search search) {
        setStyleCalendarSearch(search);
    }

    public String[] start()
        throws MalformedURLException, ParseException {
        return new String[] {"VolunteerObjectView.tabList.Reservation.groupWorkType", "VolunteerObjectView.tabList.Reservation.styleCalendar", "VolunteerObjectView.tabList.Reservation.date", "VolunteerObjectView.tabList.Reservation.reserveStore", "VolunteerObjectView.tabList.Reservation.reserveRemove", "VolunteerObjectView.tabList.Reservation.workReservationList", "VolunteerObjectView.tabList.Reservation.registrationList"};
    }

    public String getTitle() {
        return "Reserve a job";
    }

    public String getTextInstructions() {
        return "Reservation tool: select work types and dates from calendar.";
    }

    public WorkType getGroupWorkType() {
        return workType;
    }

    public void setGroupWorkType(final WorkType workType) {
        this.workType = workType;
    }

    public String[] editGroupWorkType(final WorkType workType)
        throws MalformedURLException, ParseException {
        setGroupWorkType(workType);
        return new String[] {"VolunteerObjectView.tabList.Reservation.styleCalendar", "VolunteerObjectView.tabList.Reservation.date", "VolunteerObjectView.tabList.Reservation.reserveStore", "VolunteerObjectView.tabList.Reservation.reserveRemove", "VolunteerObjectView.tabList.Reservation.workReservationList", "VolunteerObjectView.tabList.Reservation.registrationList"};
    }

    public WorkType[] valuesGroupWorkType() {
        return workTypeValues;
    }

    public StyleCalendar getStyleCalendar() {
        return styleCalendar;
    }

    public String[] editStyleCalendar(Date date)
        throws MalformedURLException, ParseException {
        if (date != null) {
            hour = null;
            date = truncateDate(date);
            if (date.compareTo(currentDate) <= 0 || disabledMap.containsKey(date)) {
                this.date = null;
                return null;
            }
            this.date = date;
            if (yellowMap.containsKey(date)) {
                yellowMap.remove(date);
            } else {
                if (!redMap.containsKey(date)) {
                    if (!greenMap.containsKey(date)) {
                        yellowMap.put(date, null);
                    } else {
                        hour = greenMap.get(date);
                    }
                }
            }
        }
        return new String[] {"VolunteerObjectView.tabList.Reservation.groupWorkType", "VolunteerObjectView.tabList.Reservation.hour.values", "VolunteerObjectView.tabList.Reservation.hour", "VolunteerObjectView.tabList.Reservation.date", "VolunteerObjectView.tabList.Reservation.reserveStore", "VolunteerObjectView.tabList.Reservation.reserveRemove", "VolunteerObjectView.tabList.Reservation.workReservationList", "VolunteerObjectView.tabList.Reservation.registrationList"};
    }

    public Reservation.Search getStyleCalendarSearch() {
        final Date showingDate = styleCalendar.getShowingDate();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(showingDate);
        Integer year = calendar.get(Calendar.YEAR);
        Integer month = calendar.get(Calendar.MONTH);
        search.getWorkReservationList().clear();
        if (workType == null) {
            final WorkReservation workReservation = new WorkReservation();
            workReservation.setStorageYear(year);
            workReservation.setStorageMonth(month);
            search.getWorkReservationList().add(workReservation);
        } else {
            final WorkReservation workReservation = new WorkReservation();
            workReservation.setWorkTypeName(workType.getName());
            workReservation.setStorageYear(year);
            workReservation.setStorageMonth(month);
            search.getWorkReservationList().add(workReservation);
        }
        if (volunteerApplication.isMainUser) {
            search.getRegistrationList().clear();
            search.getRegistrationList().add(new Registration());
        }
        return search;
    }

    public void setStyleCalendarSearch(final Reservation.Search search) {
        workReservationList.clear();
        redMap.clear();
        greenMap.clear();
        Collections.sort(search.getWorkReservationList(), workReservationComparator);
        List<Integer> hours = null;
        Date runningDate = null;
        for (final WorkReservation workReservation : search.getWorkReservationList()) {
            if (currentCalendar.compareTo(workReservation.calendar) > 0) {
                continue;
            }
            final Date date = truncateDate(workReservation.calendar.getTime());
            if (runningDate == null || !date.equals(runningDate)) {
                if (date.equals(this.date)) {
                    hours = this.hours;
                } else {
                    hours = new ArrayList<Integer>();
                }
                resetHours(hours);
            }
            runningDate = date;
            if (volunteerApplication.isMainUser) {
                workReservationList.add(workReservation);
            }
            if (workReservation.getWorkTypeName().equals(workType.getName())) {
                final boolean isCurrentUser = volunteerApplication.volunteer.login.userName != null
                    && volunteerApplication.volunteer.login.userName.equals(workReservation.userName);
                if (!isCurrentUser) {
                    hours.remove(workReservation.hour);
                }
                if (isCurrentUser
                    || !volunteerApplication.volunteer.login.loggedIn && !hours.isEmpty()) {
                    greenMap.put(date, workReservation.getStorageHour());
                    if (date.equals(this.date)) {
                        hour = workReservation.hour;
                    }
                    yellowMap.remove(date);
                    if (!volunteerApplication.isMainUser && volunteerApplication.volunteer.login.loggedIn) {
                        workReservationList.add(workReservation);
                    }
                } else {
                    if (!greenMap.containsKey(date) && hours.isEmpty()) {
                        redMap.put(date, null);
                        yellowMap.remove(date);
                    }
                }
            }
        }
        if (volunteerApplication.isMainUser) {
            registrationList.clear();
            registrationList.addAll(search.getRegistrationList());
        }
        Collections.sort(workReservationList, workReservationComparator);
    }

    public void setHour(final Integer hour) {
        this.hour = hour;
    }

    public String[] editHour(final Integer hour) {
        if (hour != null && date != null && yellowMap.containsKey(date)) {
            yellowMap.put(date, hour);
            this.hour = hour;
        }
        return new String[] {"VolunteerObjectView.tabList.Reservation.date"};
    }

    public Integer getHour() {
        return hour;
    }

    public List<Integer> valuesHour() {
        return hours;
    }

    public Date getDate() {
        if (date == null || hour == null) {
            return date;
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        return calendar.getTime();
    }

    public boolean disabledReserveStore() {
        return !volunteerApplication.volunteer.login.loggedIn;
    }

    public Reservation.WorkReservationList getReserveStore() {
        return workReservationStoreList;
    }

    private String[] reserve(final boolean isRemove)
        throws MalformedURLException, ParseException {
        if (workType == null || (isRemove ? greenMap.isEmpty() || date == null : yellowMap.isEmpty())
            || volunteerApplication.volunteer.login.userName == null) {
            return null;
        }
        workReservationStoreList.clear();
        if (isRemove) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            final WorkReservation workReservation
                = new WorkReservation(workType.getName(), calendar, hour, volunteerApplication.volunteer.login.userName);
            if (workReservationList.remove(workReservation)) {
                workReservationStoreList.add(workReservation);
            }
            greenMap.remove(date);
        } else {
            if (yellowMap.isEmpty()) {
                return null;
            }
            Map.Entry<Date, Integer> entry = yellowMap.firstEntry();
            for (;;) {
                final Date date = entry.getKey();
                final Integer hour = entry.getValue();
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                final WorkReservation workReservation
                    = new WorkReservation(workType.getName(), calendar, hour, volunteerApplication.volunteer.login.userName);
                workReservationStoreList.add(workReservation);
                if (isRemove) {
                    workReservationList.remove(workReservation);
                } else {
                    workReservationList.add(workReservation);
                }
                entry = yellowMap.higherEntry(date);
                if (entry == null) {
                    break;
                }
            }
            greenMap.putAll(yellowMap);
            yellowMap.clear();
        }
        Collections.sort(workReservationList, workReservationComparator);
        return new String[] {null, "VolunteerObjectView.tabList.Reservation.styleCalendar", "VolunteerObjectView.tabList.Reservation.workReservationList"};
    }

    public String[] reserveStore()
        throws MalformedURLException, ParseException {
        return reserve(false);
    }

    public boolean disabledReserveRemove() {
        return !volunteerApplication.volunteer.login.loggedIn;
    }

    public Reservation.WorkReservationList getReserveRemove() {
        return getReserveStore();
    }

    public String[] reserveRemove()
        throws MalformedURLException, ParseException {
        return reserve(true);
    }

    public Reservation.WorkReservationList getWorkReservationList() {
        return workReservationList;
    }

    public boolean invisibleWorkReservationList() {
        return !volunteerApplication.isMainUser && !volunteerApplication.volunteer.login.loggedIn;
    }

    public Reservation.RegistrationList getRegistrationList() {
        return registrationList;
    }

    public boolean invisibleRegistrationList() {
        return !volunteerApplication.isMainUser;
    }

}

