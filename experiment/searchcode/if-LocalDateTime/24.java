package com.atlassian.confluence.extra.cal2.action;

import java.net.URI;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.TimeZone;
import com.atlassian.confluence.extra.cal2.mgr.CalendarException;
import com.atlassian.confluence.extra.cal2.mgr.TimeZoneUtils;
import com.atlassian.confluence.extra.cal2.model.EventId;
import com.atlassian.confluence.extra.cal2.model.ICalEvent;
import com.atlassian.confluence.extra.cal2.model.ICalOrganizer;
import com.atlassian.confluence.extra.cal2.model.ICalendar;
import com.atlassian.confluence.extra.cal2.model.IEvent;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.LinkRenderer;
import com.atlassian.renderer.links.LinkResolver;
import com.atlassian.user.User;
import com.opensymphony.util.TextUtils;

public class CalendarEventAction extends CalendarHolderAction
{
	protected static final Logger log = Logger.getLogger(CalendarEventAction.class);

	protected static final String CONF_LINK = "CONF-LINK";

	protected String eventDescription;

	protected String organizerName;

	protected String link;

	protected LinkResolver linkResolver;

	private LinkRenderer linkRenderer;

	private String _eventId;

	private ICalEvent _event;

	private ICalEvent _baseEvent;// used when modifying all occurrences of
	// recurring event

	protected String eventSummary;

	protected String eventLocation;

	protected String repeat;// recurring events type

	protected boolean allDay;

	private int startYear;

	private int startMonth;

	private int startDay;

	private int startHour;

	private int startMinute;

	private int endYear;

	private int endMonth;

	private int endDay;

	private int endHour;

	private int endMinute;

	private int lastYear;

	private int lastMonth;

	private int lastDay;

	private DateTimeZone eventDateTimeZone;

	private TimeZone eventTimeZone;

	private boolean allOccurrences;// Are we editing/deleting one or all
	// occurrences of recurring event

	private boolean editMode;// Is this in edit mode

	private boolean deleteForward;// Are we deleting forward

	public CalendarEventAction()
	{
		 super();
	}

	
	/**
	 * Set Event Details Fields in GUI
	 */
	public String execute()
	{
		ICalEvent event = null;

		if (isAllOccurrences() && isEditMode() && !isOverrideEvent())
		{
			// Edit AllOccurrences so show the base event
			// TODO TEMP
			event = (ICalEvent) getBaseEvent();
		}
		else
		{
			// TODO TEMP
			event = (ICalEvent) getEvent();
		}

		if (event == null)
		{
			User user = getRemoteUser();
			if (user != null)
				setOrganizerName(user.getName());

			LocalDateTime start = getStartDate();
			if (start == null)
				start = new LocalDateTime(getEventDateTimeZone());

			LocalTime startTime = TimeZoneUtils.getDefaultStartTime(user);
			if (startTime == null)
				startTime = new LocalTime(0, 0, 0);

			start = start.withTime(startTime.getHourOfDay(), startTime.getMinuteOfHour(), 0, 0);

			setStartDate(start);
			setEndDate(start.plus(Period.hours(1)));
			setEventDateTimeZone(TimeZoneUtils.getDateTimeZone(user));

		}
		else
		{
			setEventSummary(event.getSummary());
			setEventLocation(event.getLocation());

			setEventDescription(event.getDescription());

			setOrganizerName(null);
			if (event.getOrganizer() != null)
			{
				String username = event.getOrganizer().getUsername();
				if (username == null)
					username = event.getOrganizer().getName();
				User user = userAccessor.getUser(username);

				if (user != null)
					setOrganizerName(user.getName());
			}

			setRepeat(event.getRepeat());// set type of recurrence
			setLastDate(event.getLastDate());// set repeat event last date

			setAllDay(event.isAllDay());

			setStartDate(event.getStartDate());

			LocalDateTime endDate = event.getEndDate();
			if (endDate != null && isAllDay()) // subtract a day from the end date
				endDate = endDate.minus(Period.days(1));

			setEndDate(endDate);

			setEventDateTimeZone(event.getTimeZone());
			String confLink = event.getStringProperty(CONF_LINK, null);
			if (confLink == null)
			{
				URI url = event.getUrl();
				if (url != null)
					confLink = url.getPath();
			}
			setLink(confLink);
		}

		return INPUT;
	}

	public LocalDateTime getEndDate()
	{
		if (isAllDay())
			return new LocalDateTime(endYear, endMonth, endDay, 0, 0);
		else
			return new LocalDateTime(endYear, endMonth, endDay, endHour, endMinute, 0, 0);
	}

	protected void setEndDate(LocalDateTime endDate)
	{
		if (endDate != null)
		{
			setEndYear(endDate.getYear());
			setEndMonth(endDate.getMonthOfYear());
			setEndDay(endDate.getDayOfMonth());
			setEndHour(endDate.getHourOfDay());
			setEndMinute(endDate.getMinuteOfHour());
		}
		else
		{
			setEndYear(0);
			setEndMonth(0);
			setEndDay(0);
			setEndHour(0);
			setEndMinute(0);
		}
	}

	protected void setStartDate(LocalDateTime startDate)
	{
		if (startDate != null)
		{
			setStartYear(startDate.getYear());
			setStartMonth(startDate.getMonthOfYear());
			setStartDay(startDate.getDayOfMonth());
			setStartHour(startDate.getHourOfDay());
			setStartMinute(startDate.getMinuteOfHour());
		}
		else
		{
			setStartYear(0);
			setStartMonth(0);
			setStartDay(0);
			setStartHour(0);
			setStartMinute(0);
		}
	}

	public LocalDateTime getStartDate()
	{
		if (startYear != 0 && startMonth > 0 && startMonth <= 12 && startDay > 0)
		{
			if (isAllDay())
				return new LocalDateTime(startYear, startMonth, startDay, 0, 0);
			else
				return new LocalDateTime(startYear, startMonth, startDay, startHour, startMinute);
		}
		return null;
	}

	protected void setLastDate(LocalDateTime lastDate)
	{
		if (lastDate != null)
		{
			lastYear = lastDate.getYear();
			setLastMonth(lastDate.getMonthOfYear());
			setLastDay(lastDate.getDayOfMonth());
		}
		else
		{
			lastYear = 0;
			setLastMonth(0);
			setLastDay(0);
		}
	}

	public LocalDateTime getLastDate()
	{
		if (lastYear > 0 && lastMonth > 0 && lastMonth <= 12 && lastDay > 0)
		{
			return new LocalDateTime(lastYear, lastMonth, lastDay, 23, 59);
		}
		return null;
	}

	public String getEventId()
	{
		return _eventId;
	}

	public void setEventId(String eventId)
	{
		this._eventId = eventId.trim();
		this._event = null;
	}

	public IEvent getEvent()
	{
		if (_event == null && _eventId != null)
		{
			ICalendar cal = getSubCalendar();
			if (cal == null) // try the group
				cal = getCalendar();

			if (cal != null)
			{
				try
				{
					// TODO TEMP
					_event = (ICalEvent) cal.findEvent(_eventId);
				}
				catch (CalendarException e)
				{
					log.error("Problem while retrieving event '" + _eventId + "' from calendar '" + cal.getId(), e);
				}
			}
		}
		return _event;
	}

	/*
	 * Used when we need to modify all occurrences of an recurring event
	 */
	public IEvent getBaseEvent()
	{
		if (_baseEvent == null && _eventId != null)
		{
			ICalendar cal = getSubCalendar();
			if (cal == null) // try the group
				cal = getCalendar();

			if (cal != null)
			{
				try
				{
					// TODO TEMP
					_baseEvent = (ICalEvent) cal.findBaseEvent(_eventId);
				}
				catch (CalendarException e)
				{
					log.error("Problem while retrieving event '" + _eventId + "' from calendar '" + cal.getId(), e);
				}
			}
		}
		return _baseEvent;
	}

	public String getEventSummary()
	{
		return eventSummary;
	}

	public void setEventSummary(String eventSummary)
	{
		this.eventSummary = eventSummary;
	}

	public String getEventLocation()
	{
		return eventLocation;
	}

	public void setEventLocation(String eventLocation)
	{
		this.eventLocation = eventLocation;
	}

	public int getStartYear()
	{
		return startYear;
	}

	public void setStartYear(int startYear)
	{
		this.startYear = startYear;
	}

	public int getStartMonth()
	{
		return startMonth;
	}

	public void setStartMonth(int startMonth)
	{
		this.startMonth = startMonth;
	}

	public int getStartDay()
	{
		return startDay;
	}

	public void setStartDay(int startDay)
	{
		this.startDay = startDay;
	}

	public int getStartHour()
	{
		return startHour;
	}

	public void setStartHour(int startHour)
	{
		this.startHour = startHour;
	}

	public int getStartHour12()
	{
		int hour12 = startHour % 12;
		return (hour12 == 0) ? 12 : hour12;
	}

	public void setStartHour12(int startHour12)
	{
		startHour12 = startHour12 % 12;
		startHour = (startHour < 12) ? startHour12 : 12 + startHour12;
	}

	public boolean isStartAM()
	{
		return startHour < 12;
	}

	public void setStartAM(boolean startAM)
	{
		if (startAM && startHour >= 12)
			startHour -= 12;
		else if (!startAM && startHour < 12)
			startHour += 12;
	}

	public int getStartMinute()
	{
		return startMinute;
	}

	public void setStartMinute(int startMinute)
	{
		this.startMinute = startMinute;
	}

	public int getEndYear()
	{
		return endYear;
	}

	public void setEndYear(int endYear)
	{
		this.endYear = endYear;
	}

	public int getEndMonth()
	{
		return endMonth;
	}

	public void setEndMonth(int endMonth)
	{
		this.endMonth = endMonth;
	}

	public int getEndDay()
	{
		return endDay;
	}

	public void setEndDay(int endDay)
	{
		this.endDay = endDay;
	}

	public int getEndHour()
	{
		return endHour;
	}

	public void setEndHour(int endHour)
	{
		this.endHour = endHour;
	}

	public int getEndHour12()
	{
		int hour12 = endHour % 12;
		return (hour12 == 0) ? 12 : hour12;
	}

	public void setEndHour12(int endHour12)
	{
		endHour12 = endHour12 % 12;
		endHour = (endHour < 12) ? endHour12 : 12 + endHour12;
	}

	public boolean isEndAM()
	{
		return endHour < 12;
	}

	public void setEndAM(boolean endAM)
	{
		if (endAM && endHour >= 12)
			endHour -= 12;
		else if (!endAM && endHour < 12)
			endHour += 12;
	}

	public int getEndMinute()
	{
		return endMinute;
	}

	public void setEndMinute(int endMinute)
	{
		this.endMinute = endMinute;
	}

	public String getLastYear()
	{
		return lastYear == 0 ? "" : "" + lastYear;
	}

	public int getIntLastYear()
	{
		return lastYear;
	}

	/**
	 * Using string so we can show a blank year field
	 * 
	 * @param lastYear
	 */
	public void setLastYear(String lastYear)
	{
		try
		{
			this.lastYear = Integer.parseInt(lastYear); // catch a
			// NumberFormatException
		}
		catch (NumberFormatException nfe)
		{
			this.lastYear = 0;
		}
	}

	public int getLastMonth()
	{
		return lastMonth;
	}

	public void setLastMonth(int lastMonth)
	{
		this.lastMonth = lastMonth;
	}

	public int getLastDay()
	{
		return lastDay;
	}

	public void setLastDay(int lastDay)
	{
		this.lastDay = lastDay;
	}

	public String getRepeat()
	{
		return repeat;
	}

	public void setRepeat(String repeat)
	{
		this.repeat = repeat;
	}

	public boolean isAllOccurrences()
	{
		return allOccurrences;
	}

	public void setAllOccurrences(boolean allOccurrences)
	{
		this.allOccurrences = allOccurrences;
	}

	public boolean isEditMode()
	{
		return editMode;
	}

	public void setEditMode(boolean editMode)
	{
		this.editMode = editMode;
	}

	public boolean isDeleteForward()
	{
		return deleteForward;
	}

	public void setDeleteForward(boolean deleteForward)
	{
		this.deleteForward = deleteForward;
	}

	public boolean isAllDay()
	{
		return allDay;
	}

	public void setAllDay(boolean allDay)
	{
		this.allDay = allDay;
	}

	public String getTimeZoneId()
	{
		if (eventDateTimeZone != null)
			return eventDateTimeZone.getID();

		return null;
	}

	public void setTimeZoneId(String eventDateTimeZoneId)
	{
		if (eventDateTimeZoneId != null && eventDateTimeZoneId.trim().length() > 0)
		{
			eventDateTimeZone = DateTimeZone.forID(eventDateTimeZoneId);
		}
		else
		{
			eventDateTimeZone = null;
		}
		
		// TODO Should this be here?
		eventTimeZone = null;
	}

	public TimeZone getEventTimeZone()
	{
		if (eventTimeZone == null && eventDateTimeZone != null)
			eventTimeZone = TimeZoneUtils.getTimeZone(eventDateTimeZone);

		return eventTimeZone;
	}

	/**
	 * Returns the {@link DateTimeZone} for the event.
	 * 
	 * @return the current event's timezone.
	 */
	public DateTimeZone getEventDateTimeZone()
	{
		return eventDateTimeZone;
	}

	public void setEventDateTimeZone(DateTimeZone eventDateTimeZone)
	{
		this.eventDateTimeZone = eventDateTimeZone;
		eventTimeZone = null;
	}

	protected void setEventId(EventId eventId)
	{
		setEventId(eventId.toString());
	}

	public String getEventDescription()
	{
		return eventDescription;
	}

	public void setEventDescription(String eventDescription)
	{
		this.eventDescription = eventDescription;
	}

	public String getOrganizerName()
	{
		return organizerName;
	}

	public void setOrganizerName(String organizerName)
	{
		this.organizerName = organizerName;
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}

	public String getLinkHtml()
	{
		ICalEvent event = (ICalEvent) getEvent();

		if (event != null)
		{
			String evtLink = event.getStringProperty(CONF_LINK, null);

			if (evtLink != null)
			{
				ContentEntityObject content = getContent();

				if (content != null)
				{
					PageContext ctx = content.toPageContext();
					ctx.setSiteRoot(getContextPath());
					ctx.setImagePath(getContextPath() + "/images");

					Link rlink = linkResolver.createLink(ctx, evtLink);
					if (rlink != null)
						return linkRenderer.renderLink(rlink, ctx);
				}
			}

			URI uri = event.getUrl();

			if (uri != null)
			{
				String path = uri.getPath();

				String disp = path;
				if (disp.length() > 25)
					disp = disp.substring(0, 10) + "..." + disp.substring(disp.length() - 10);

				return "<a href=\"" + path + "\" title=\"" + path + "\">" + disp + "</a>";
			}
		}

		return null;
	}

	public String getOrganizerHtml(ICalOrganizer organizer)
	{
		StringBuffer out = new StringBuffer();
		 if (organizer != null)
		{
			String username = organizer.getUsername();
			if (username == null) // The name was used to store usernames
				// previously. Check as backup.
				username = organizer.getName();

			User user = userAccessor.getUser(username);
			if (user != null)
			{
				out.append("<a href='").append(getContextPath()).append("/display/~").append(GeneralUtil.doubleUrlEncode(user.getName()))
						.append("'>");
				if (TextUtils.stringSet(user.getFullName()))
					out.append(GeneralUtil.htmlEncode(user.getFullName()));
				else
					out.append(GeneralUtil.htmlEncode(user.getName()));

				out.append("</a>");
			}
			else
			{
				String email = organizer.getEmail();
				if (email != null)
				{
					out.append("<a href='mailto:").append(GeneralUtil.urlEncode(email)).append("'>");

					if (TextUtils.stringSet(organizer.getName()))
						out.append(organizer.getName());
					else
						out.append(email);

					out.append("</a>");
				}
				else
					out.append(organizer.getName());
			}
		}

		return out.toString();
	}

	public void setLinkResolver(LinkResolver linkResolver)
	{
		this.linkResolver = linkResolver;
	}

	public void setLinkRenderer(LinkRenderer linkRenderer)
	{
		this.linkRenderer = linkRenderer;
	}

	/*
	 * is this an override Event?
	 */
	public boolean isOverrideEvent()
	{
		String strEventId = getEventId();
		if (strEventId == null || strEventId.trim().length() == 0)
		{
			return false;
		}
		else
		{
			EventId eventId = new EventId(strEventId);
			return (eventId.getOverrideDate() != null ? true : false);
		}
	}
}

