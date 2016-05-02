package com.atlassian.confluence.extra.cal2.action;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.extra.cal2.mgr.CalendarException;
import com.atlassian.confluence.extra.cal2.model.BaseEventDetails;
import com.atlassian.confluence.extra.cal2.model.EventDetails;
import com.atlassian.confluence.extra.cal2.model.EventId;
import com.atlassian.confluence.extra.cal2.model.ICalCalendar;
import com.atlassian.confluence.extra.cal2.model.ICalEvent;
import com.atlassian.confluence.extra.cal2.model.ICalOrganizer;
import com.atlassian.confluence.extra.cal2.model.OverrideEventDetails;
import com.atlassian.confluence.links.linktypes.PageCreateLink;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.UnresolvedLink;
import com.atlassian.user.User;

public class ModifyCalendarEventAction extends CalendarEventAction
{
    private static final String CALENDAR = "calendar";

    private int startYear;

    private int startMonth;

    private int startDay;

    private static final String NEW = "new";

    public ModifyCalendarEventAction()
    {
        setRequireEditPermission();
    }

    
    public String addEvent()
    {
        // TODO Check permissions

        if (subCalendarId == null)
        {
            return CALENDAR;
        }
        else if (subCalendarId.equals(""))
        {
            return NEW;
        }

        return execute();
    }

    public String doAdd()
    {
        ICalCalendar calendar = (ICalCalendar) getSubCalendar();
        if (calendar == null)
        {
            addActionError("Unable to find the calendar to add the event to.");
            return ERROR;
        }

        BaseEventDetails eventDetails = new BaseEventDetails();

        String result = updateEventDetails(eventDetails);
        if (result != null)
            return result;

        try
        {
            calendar.addBaseDetails(eventDetails);
        }
        catch (CalendarException e)
        {
            e.printStackTrace();
            addActionError("An error occurred while adding the event: " + e.getMessage());
            return ERROR;
        }
        setEventId(new EventId(eventDetails, eventDetails.getStartDate(), eventDetails.getTimeZone()));
        return saveCalendar();
    }

    /**
     * saves user form changes to VEVENT
     * 
     * @return
     */
    public String doEdit()
    {
        ICalEvent event = (ICalEvent) getEvent();

        if (event == null)
        {
            addActionError("Unable to find the event to edit.");
            return ERROR;
        }

        EventDetails details = event.getDetails();
        ICalCalendar calendar = (ICalCalendar) event.getCalendar();
        OverrideEventDetails overrideDetails = null;
        EventId eventId = new EventId(getEventId());
        if (!isAllOccurrences() && details instanceof BaseEventDetails)
        {// create a new override event
            BaseEventDetails baseDetails = (BaseEventDetails) details;

            // Create an overrideeventdetail record
            overrideDetails = new OverrideEventDetails(calendar, baseDetails, eventId);
            String result = updateEventDetails(overrideDetails);
            if (result != null)
            {
                return result;
            }
            baseDetails.addOverride(overrideDetails);
            try
            {
                calendar.addOverrideDetails(overrideDetails);
            }
            catch (CalendarException e)
            {
                e.printStackTrace();
                addActionError("An error occurred while adding override event to calendar: " + e.getMessage());
                return ERROR;
            }
            setEventId(new EventId(overrideDetails.getUid(), overrideDetails.getOverrideDate(), eventId.getInstanceDate(), eventId.getTimeZone()));
            return saveCalendar();
        }
        else
        {// Non recurring event or old override event or all occurrences of
            // reccurrence event
            String result = updateEventDetails(details);
            if (result != null)
                return result;
            result = saveCalendar();
            if (result == ERROR)
            {
                return result;
            }
            // When an all occurrence of recurrence event is edited the event details show the
            // first occurrence of the recurring event, so we may need to adjust the eventId
            // to the new instance eventId or else it will go back to the first occurrence
            // rather than the instance that the user mouse clicked
            if (!(details instanceof OverrideEventDetails) && !getRepeat().equals(BaseEventDetails.NO_REPEAT) && !getRepeat().equals(BaseEventDetails.NOT_IMPLEMENTED))
            {
                LocalDateTime oldInstanceDate = eventId.getInstanceDate();
                LocalDateTime newStartDate = getStartDate();
                LocalDateTime newInstanceDate = new LocalDateTime(oldInstanceDate.getYear(), oldInstanceDate.getMonthOfYear(), oldInstanceDate.getDayOfMonth(), newStartDate.getHourOfDay(), newStartDate.getMinuteOfHour(), newStartDate
                        .getSecondOfMinute());
                EventId newEventId = new EventId(details, newInstanceDate, details.getTimeZone());
                List events = null;
                // Check if the recurring event dates has changed
                try
                {
                    events = calendar.findEvents(new Interval(newEventId.getInstanceDate().toLocalDate().toInterval(details.getTimeZone())), details.getTimeZone());
                }
                catch (CalendarException e)
                {// no problem if you can't find it
                }
                boolean found = false;
                if (events != null)
                {
                    Iterator i = events.iterator();
                    while (i.hasNext())
                    {
                        ICalEvent tmpEvent = (ICalEvent) i.next();
                        if (tmpEvent.getDetails().getUid().equals(newEventId.getUid()))
                        {
                            found = true;
                            break;
                        }
                    }
                }
                if (found)
                {// show event details of the specific event that was mouse
                    // clicked
                    setEventId(new EventId(details, newEventId.getInstanceDate(), newEventId.getTimeZone()));
                }
                else
                {// show event details of first occurrence of recurring event
                    setEventId(new EventId(details, details.getStartDate(), details.getTimeZone()));
                }
            }
            addActionError("Please provide a start date for the event." + ((BaseEventDetails) details).getLastDate());
            return result;
        }
    }

    /*
     * Saves event details form values to ICAL's VEVENT
     */
    private String updateEventDetails(EventDetails eventDetails)
    {
        if (eventSummary == null || eventSummary.trim().length() == 0)
        {
            addActionError("Please provide a summary for the event.");
            return ERROR;
        }
        eventDetails.setSummary(eventSummary);

        if (eventLocation != null && eventLocation.trim().length() > 0)
            eventDetails.setLocation(eventLocation);
        else
            eventDetails.setLocation(null);

        if (eventDescription != null && eventDescription.trim().length() > 0)
            eventDetails.setDescription(eventDescription);
        else
            eventDetails.setDescription(null);

        LocalDateTime startDate = getStartDate();
        if (startDate == null)
        {
            addActionError("Please provide a start date for the event.");
            return ERROR;
        }

        LocalDateTime endDate = getEndDate();
        if (endDate == null)
        {
            addActionError("Please provide an end date for the event.");
            return ERROR;
        }
        else if (endDate.isBefore(startDate))
        {
            addActionError("The event currently ends before it starts.");
            return ERROR;
        }

        // TODO  This doesn't flush the EventTimeZone cache
        DateTimeZone dateTimeZone = getEventDateTimeZone();

        if (isAllDay())
        {
            // we add a day to the end date because it seems more logical in the UI
            // to have the end date being the day the event ends on, whereas iCalendar
            // specifies it to be midnight of the next day.
            eventDetails.setDates(startDate.toLocalDate(), endDate.toLocalDate().plus(Period.days(1)));
        }
        else
        {
            eventDetails.setDates(startDate, endDate, dateTimeZone);
        }

        if (getIntLastYear() != 0 || getLastMonth() != 0 || getLastDay() != 0)
        {
            LocalDateTime lastDate = getLastDate();
            if (getIntLastYear() == 0)
            {
                addActionError("The Repeat end year cannot be blank if repeat end month or end day is not empty.");
                return ERROR;
            }
            else if (getLastMonth() == 0)
            {
                addActionError("The Repeat end month cannot be blank if repeat end year or end day is not empty.");
                return ERROR;
            }
            else if (getLastDay() == 0)
            {
                addActionError("The Repeat end day cannot be blank if repeat end year or end month is not empty.");
                return ERROR;
            }
            else if (lastDate != null && (lastDate.isBefore(startDate) || isStartAndLastDateEqual()))
            {
                addActionError("The Repeat end date must be after the From date.");
                return ERROR;
            }
        }
        // set RRULE
        if (eventDetails instanceof BaseEventDetails)
        {
            if (repeat != null && repeat.trim().length() > 0)
            {
                ((BaseEventDetails) eventDetails).setRepeat(repeat, getLastDate());
            }
            else
                ((BaseEventDetails) eventDetails).setRepeat(null, null);
        }

        User user = null;
        if (organizerName != null && organizerName.trim().length() > 0)
            user = userAccessor.getUser(organizerName);

        if (user != null)
        {
            ICalOrganizer org = eventDetails.getOrganizer();
            if (org == null)
            {
                org = new ICalOrganizer();
                eventDetails.setOrganizer(org);
            }

            org.setUsername(user.getName());
            org.setName(user.getFullName() != null ? user.getFullName() : user.getName());
            try
            {
                String email = user.getEmail();
                if (email != null)
                {
                    email = email.trim();
                    if (email.length() == 0)
                        email = null;
                }
                org.setEmail(email);
            }
            catch (URISyntaxException e)
            {
                log.info(e);
                addActionError(e.getMessage());
                return ERROR;
            }
        }
        else
        {
            eventDetails.setOrganizer(null);
        }

        // Set the link.
        if (link != null && link.trim().length() > 0)
        {
            ContentEntityObject content = getContent();
            if (content != null)
            {
                PageContext ctx = content.toPageContext();

                String url = null;
                Link rlink = linkResolver.createLink(ctx, link);

                if (rlink instanceof UnresolvedLink || rlink instanceof PageCreateLink)
                {
                    addActionError("The content linked to could not be found: " + link);
                    return ERROR;
                }

                if (rlink != null)
                {
                    url = rlink.getUrl();
                    if (url != null && rlink.isRelativeUrl())
                        url = getFullContextPath() + url;

                    eventDetails.setStringProperty(CONF_LINK, link);
                }

                if (url != null)
                    link = url;
            }

            try
            {
                eventDetails.setUrl(new URI(link));
            }
            catch (URISyntaxException e)
            {
                e.printStackTrace();
                addActionError("Invalid URL: " + link);
                return ERROR;
            }
        }
        else
        {
            eventDetails.setUrl(null);
        }

        return null;
    }

    public String deleteEvent()
    {
        ICalEvent evt = (ICalEvent) getEvent();
        if (evt == null)
        {
            addActionError("No event was selected to delete.");
            return ERROR;
        }

        EventDetails details = evt.getDetails();
        BaseEventDetails baseDetails = null;
        OverrideEventDetails overrideDetails = null;
        if (details instanceof BaseEventDetails)
        {
            baseDetails = (BaseEventDetails) details;
        }
        else
        {
            overrideDetails = (OverrideEventDetails) details;
            baseDetails = overrideDetails.getBaseEvent();
        }
        ICalCalendar cal = (ICalCalendar) evt.getCalendar();
        if (cal != null)
        {
            if (overrideDetails != null)
            {// remove override details this occurrence event becomes normal
                // recurrence
                try
                {
                    if (cal.removeOverrideDetails(overrideDetails))
                    {
                        return saveCalendar();
                    }
                }
                catch (CalendarException e)
                {
                    e.printStackTrace();
                    addActionError("An error occurred while deleting override details: " + e.getMessage());
                    return ERROR;
                }
            }
            else if (isDeleteForward())
            {// delete from this occurrence forward (really just set the until
                // date)
                EventId eventId = new EventId(getEventId());
                LocalDateTime lastDate = eventId.getInstanceDate().minusDays(1);
                baseDetails.setLastDate(lastDate);
                return saveCalendar();
            }
            else if (!isAllOccurrences())
            {// delete only one occurrence of reccurrence event
                EventId eventId = new EventId(getEventId());
                baseDetails.addExcludedDate(eventId.getInstanceDate(), eventId.getTimeZone());
                return saveCalendar();
            }
            else
            {// delete nonrecurrence event or all occurrences of recurrence
                // event
                try
                {
                    if (cal.removeBaseDetails(baseDetails))
                    {
                        return saveCalendar();
                    }
                }
                catch (CalendarException e)
                {
                    e.printStackTrace();
                    addActionError("An error occurred while deleting: " + e.getMessage());
                    return ERROR;
                }
            }
        }

        addActionError("The event could not be deleted.");
        return ERROR;
    }

    private boolean isStartAndLastDateEqual()
    {
        if (getLastDate() == null)
            return false;
        else
        {
            LocalDateTime startDate = new LocalDateTime(getEndYear(), getEndMonth(), getEndDay(), 0, 0);
            LocalDateTime lastDate = new LocalDateTime(getIntLastYear(), getLastMonth(), getLastDay(), 0, 0);
            return lastDate.equals(startDate);
        }
    }

}

