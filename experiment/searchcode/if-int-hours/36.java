/*
 * Copyright 2007 Future Earth, info@future-earth.eu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package eu.future.earth.gwt.client.date.week.staend;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.future.earth.gwt.client.FtrGwtDateCss;
import eu.future.earth.gwt.client.date.DateEvent;
import eu.future.earth.gwt.client.date.DateEventListener;
import eu.future.earth.gwt.client.date.DateRenderer;
import eu.future.earth.gwt.client.date.DateUtils;
import eu.future.earth.gwt.client.date.EventPanel;
import eu.future.earth.gwt.client.date.HasDateEventHandlers;
import eu.future.earth.gwt.client.date.DateEvent.DateEventActions;
import eu.future.earth.gwt.client.date.week.DayEventElement;
import eu.future.earth.gwt.client.date.week.DayHelper;

public class DayPanel<T> extends BaseDayPanel<T> implements MouseDownHandler, MouseMoveHandler, MouseUpHandler, MouseOutHandler, DayEventElement<T>, HasDateEventHandlers<T>, DateEventListener<T> {

	private DateTimeFormat timeParser = null;

	private Calendar helper = new GregorianCalendar();

	private DayHelper<T> theDay = new DayHelper<T>();

	private DateRenderer<T> renderer = null;

	private String startTimeString = null;

	private ArrayList<FocusPanel> hh = new ArrayList<FocusPanel>();

	private ArrayList<EventPanel<? extends T>> events = new ArrayList<EventPanel<? extends T>>();

	private ResizeDragController<? extends T> resizeDragController = null;

	public Date getDate() {
		return theDay.getTime();
	}

	public DayPanel(DateRenderer<T> newRenderer) {
		super(newRenderer);
		theDay.setRenderer(newRenderer);
		setStyleName(FtrGwtDateCss.DAY_PANEL);
		renderer = newRenderer;
		timeParser = DateTimeFormat.getFormat("HH:mm");
		if (renderer.enableDragAndDrop()) {
			resizeDragController = new ResizeDragController<T>(this);
		}
		int intervalMinutes = (60 / renderer.getIntervalsPerHour());
		int y = 0;
		final int startHour = renderer.getStartHour();
		final int endHour = renderer.getEndHour();
		for (int hour = startHour; hour < endHour; hour++) {
			int displayHour = hour;
			String displaySuffix = "";
			if (!renderer.show24HourClock()) {
				if (hour < 12) {
					displaySuffix = " AM";
				} else {
					displaySuffix = " PM";
				}
				if (hour == 0) {
					displayHour = 12;
				} else {
					if (hour > 12) {
						displayHour = hour - 12;
					}
				}
			}

			for (int interval = 0; interval < renderer.getIntervalsPerHour(); interval++) {
				final FocusPanel drow = new FocusPanel();
				int minute = (interval * intervalMinutes);
				String displayMinute = (minute < 10) ? "0" + minute : "" + minute;
				drow.setTitle(displayHour + ":" + displayMinute + displaySuffix);
				if (interval == 0) {
					drow.setStyleName(FtrGwtDateCss.HOUR_INTERVAL_START);
				} else {
					drow.setStyleName(FtrGwtDateCss.HOUR_INTERVAL_BETWEEN);
				}
				drow.setSize("100%", renderer.getIntervalHeight() + "px");
				drow.addMouseDownHandler(this);
				drow.addMouseMoveHandler(this);
				drow.addMouseUpHandler(this);
				hh.add(drow);
				super.add(drow, 0, y);
				y = y + renderer.getIntervalHeight();
			}
		}
		this.addMouseOutHandler(this);
	}

	public void destroy() {
		if (getParent() != null) {
			removeFromParent();
		}
	}

	public void clearEvents() {
		for (int i = 0; i < events.size(); i++) {
			final EventPanel<? extends T> ev = events.get(i);
			super.removeFromBody(ev);
		}
		events.clear();
	}

	public int calculateIntervalSnapForY(int y) {
		return (int) Math.round(((double) y) / renderer.getIntervalHeight());
	}

	public void addEventDrop(DayField<T> newEvent, boolean sameDay) {
		GWT.log("Not same day", null);
		if (renderer.enableDragAndDrop()) {
			newEvent.addStyleName(FtrGwtDateCss.DND_GETTING_STARTED_LABEL);
			newEvent.setResizeDragController(resizeDragController);
		}
		events.add(newEvent);
		repaintEvents();
		GWT.log("Adding", null);
		DateEvent.fire(this, DateEventActions.DRAG_DROP, newEvent.getValue());
	}

	public HandlerRegistration addDateEventHandler(DateEventListener<? extends T> handler) {
		return addHandler(handler, DateEvent.getType());
	}

	@SuppressWarnings("unchecked")
	public void addEvent(EventPanel<? extends T> newEvent, boolean partOfBatch) {
		if (!partOfBatch) {
			removeEvent(newEvent.getValue()); // Be safe
		}
		if (renderer.enableDragAndDrop()) {
			newEvent.addStyleName(FtrGwtDateCss.DND_GETTING_STARTED_LABEL);
			if (newEvent instanceof DayField<?>) {
				DayField<T> real = (DayField<T>) newEvent;
				real.setResizeDragController(resizeDragController);
			}
		}
		events.add(newEvent);
		if (!partOfBatch) {
			repaintEvents();
		}
	}

	@SuppressWarnings("unchecked")
	public void updateEvent(EventPanel<? extends T> newEvent) {
		EventPanel<? extends T> found = null;
		for (int i = 0; i < events.size(); i++) {
			final EventPanel<? extends T> test = events.get(i);
			if (renderer.equal(test.getValue(), newEvent.getValue())) {
				found = test;
			}
		}
		if (found != null) {
			removeEvent(found.getValue(), false);
		}
		if (renderer.enableDragAndDrop()) {
			newEvent.addStyleName(FtrGwtDateCss.DND_GETTING_STARTED_LABEL);
			if (newEvent instanceof DayField<?>) {
				DayField<T> real = (DayField<T>) newEvent;
				real.setResizeDragController(resizeDragController);
			}
		}
		events.add(newEvent);
		repaintEvents();
	}

	public int calculateYForHoursMinutes(int hours, int minutes) {
		return (int) Math.round((hours + (minutes / 60.0)) * renderer.getIntervalsPerHour() * renderer.getIntervalHeight());
	}

	private int getStartTimePos(Date theDate) {
		if (DateUtils.isSameDay(theDay.getTime(), theDate)) {
			helper.setTime(theDate);
			final int hours = helper.get(Calendar.HOUR_OF_DAY) - renderer.getStartHour();
			final int minutes = helper.get(Calendar.MINUTE);
			return calculateYForHoursMinutes(hours, minutes);
		} else {
			return calculateYForHoursMinutes(renderer.getStartHour(), 0);
		}
	}

	private int getEndTimePos(Date theDate) {
		helper.setTime(theDate);

		if (DateUtils.isSameDay(theDay.getTime(), theDate)) {
			final int hours = helper.get(Calendar.HOUR_OF_DAY) - renderer.getStartHour();
			final int minutes = helper.get(Calendar.MINUTE);
			return calculateYForHoursMinutes(hours, minutes);
		} else {
			return calculateYForHoursMinutes(renderer.getEndHour(), 59);
		}

	}

	private void paintEvent(EventPanel<? extends T> newEvent) {
		final int y = getStartTimePos(newEvent.getStart());
		// Make is a default heigt for is no end time is set.
		int y2 = y + (renderer.getIntervalHeight() * renderer.getIntervalsPerHour());

		if (newEvent.getEnd() != null) {
			y2 = getEndTimePos(newEvent.getEnd());
		}
		newEvent.setPosY(y);
		newEvent.setContentHeight(y2 - y);
		newEvent.repaintTime();
		int wide = getOffsetWidth() - 6;
		if (wide < 0) {
			wide = 80;
		}
		final List<EventPanel<? extends T>> overlap = getOverlapping(y, y2, newEvent);
		if (overlap.size() > 0) {
			wide = wide / (overlap.size() + 1);
			for (int i = 0; i < overlap.size(); i++) {
				super.removeFromBody(overlap.get(i));
				overlap.get(i).setWidth(wide + "px");
				super.add(overlap.get(i), wide * i, overlap.get(i).getPosY());
			}
			newEvent.setWidth(wide + "px");
			super.add(newEvent, (wide * overlap.size()) + 1, y);

		} else {
			// Here we need to get overlapping event so we can repaint them and
			// paint this one after them.
			newEvent.setWidth(wide + "px");
			super.add(newEvent, 0, y);
		}

	}

	private List<EventPanel<? extends T>> getOverlapping(int y1, int y2, EventPanel<? extends T> me) {
		final List<EventPanel<? extends T>> overlapping = new ArrayList<EventPanel<? extends T>>();
		for (int i = 0; i < events.size(); i++) {
			final EventPanel<? extends T> cur = events.get(i);
			final int cy1 = cur.getPosY();
			if (!renderer.equal(me.getValue(), cur.getValue())) {
				if (cy1 >= y1 && cy1 < y2) {
					overlapping.add(cur);
				} else {
					final int cy2 = cur.getPosY() + cur.getContentHeight();
					if (cy2 > y1 && cy2 <= y2) {
						overlapping.add(cur);
					} else {
						// IS one fall complete in the other forgot this one
						// first
						if (cy1 >= y1 && cy2 <= y2) {
							overlapping.add(cur);
						} else {
							if (cy1 <= y1 && cy2 >= y2) {
								overlapping.add(cur);
							}

						}
					}
				}
			}
		}
		return overlapping;
	}

	public EventPanel<? extends T> removeEvent(T event) {
		return removeEvent(event, true);
	}

	public EventPanel<? extends T> removeEvent(T event, boolean repaint) {
		for (int i = 0; i < events.size(); i++) {
			final EventPanel<? extends T> ev = events.get(i);
			if (renderer.equal(ev.getValue(), event)) {
				super.removeFromBody(ev);
				events.remove(i);
				// Only repaint when there is something removed.
				if (repaint) {
					repaintEvents();
				}
				return ev;
			}
		}
		return null;
	}

	public void repaintEvents() {
		for (int i = 0; i < events.size(); i++) {
			final EventPanel<? extends T> ev = events.get(i);
			super.removeFromBody(ev);
			paintEvent(ev);
		}
	}

	public void repaint(DayField<T> notRepaint) {
		for (int i = 0; i < events.size(); i++) {
			final EventPanel<? extends T> ev = events.get(i);
			if (!renderer.equal(ev.getValue(), notRepaint.getValue())) {
				super.removeFromBody(ev);
				paintEvent(ev);
			}
		}
	}

	public void setDay(Calendar newDay) {
		theDay.setTime(newDay.getTime());
		theDay.setFirstDayOfWeek(newDay.getFirstDayOfWeek());
	}

	public boolean isDay(T newEvent) {
		if (renderer.isWholeDayEvent(newEvent)) {
			return false;
		}
		return theDay.isDay(newEvent);
	}

	protected String getPrefferedWitdh() {
		return "100%";
	}

	public void onMouseDown(MouseDownEvent event) {
		if (event.getSource() instanceof Widget) {
			Widget widget = (Widget) event.getSource();
			if (widget != this) {
				startTimeString = widget.getTitle();
			}
		}
	}

	public void onMouseOut(MouseOutEvent event) {
		if (event.getSource() instanceof Widget) {
			Widget widget = (Widget) event.getSource();
			if (widget == this) {
				startTimeString = null;
				clearSelection();
			}
		}
	}

	public void onMouseMove(MouseMoveEvent event) {
		if (event.getSource() instanceof Widget) {
			Widget widget = (Widget) event.getSource();
			if (startTimeString != null && widget != this) {
				widget.addStyleName(FtrGwtDateCss.HOURS_SELECTED);
			}
		}
	}

	public void onMouseUp(MouseUpEvent event) {
		if (event.getSource() instanceof Widget) {
			Widget widget = (Widget) event.getSource();
			// startTime
			Boolean add12 = false;
			if (startTimeString != null && widget != this) {
				if (startTimeString.indexOf("AM") > -1 || startTimeString.indexOf("PM") > -1) {
					if (renderer.show24HourClock() == false) {
						if (startTimeString.indexOf("PM") > -1) {
							add12 = true;
						}
					}
					startTimeString = startTimeString.substring(0, startTimeString.length() - 3);
				}
				helper.setTime(theDay.getTime());
				final Calendar helperTime = new GregorianCalendar();

				final Date newDate = timeParser.parse(startTimeString);
				helperTime.setTime(newDate);
				// Transplant the hour and minutes

				if (add12 && helperTime.get(Calendar.HOUR_OF_DAY) != 12) {
					helper.set(Calendar.HOUR_OF_DAY, helperTime.get(Calendar.HOUR_OF_DAY) + 12);
				} else {
					helper.set(Calendar.HOUR_OF_DAY, helperTime.get(Calendar.HOUR_OF_DAY));
				}
				// helper.set(Calendar.HOUR_OF_DAY,
				// helperTime.get(Calendar.HOUR_OF_DAY));
				helper.set(Calendar.MINUTE, helperTime.get(Calendar.MINUTE));
				helper.set(Calendar.SECOND, 0);
				helper.set(Calendar.MILLISECOND, 0);
				final Date start = helper.getTime();

				// add one interval's worth of time to get to the END of the
				// selected interval

				String endTimeString = widget.getTitle();
				if (endTimeString != null) {
					if (endTimeString.indexOf("AM") > -1 || endTimeString.indexOf("PM") > -1) {
						endTimeString = endTimeString.substring(0, endTimeString.length() - 3);
					}
				}

				if (!startTimeString.equalsIgnoreCase(endTimeString)) {
					Date endTime = timeParser.parse(endTimeString);
					// If drag is backwards, hack to reverse times shown in
					// display.
					helperTime.setTime(endTime);
					helperTime.add(Calendar.MINUTE, 60 / renderer.getIntervalsPerHour());
					// Transplant the hour and minutes
					helper.set(Calendar.HOUR_OF_DAY, helperTime.get(Calendar.HOUR_OF_DAY));
					helper.set(Calendar.MINUTE, helperTime.get(Calendar.MINUTE));
					helper.set(Calendar.SECOND, 0);
					helper.set(Calendar.MILLISECOND, 0);
					Date end = helper.getTime();
					if (newDate.getTime() > endTime.getTime()) {
						renderer.createNewAfterClick(end, start, this);
					} else {
						renderer.createNewAfterClick(start, end, this);
					}

				} else {
					renderer.createNewAfterClick(start, this);
				}

				startTimeString = null;
			}
			clearSelection();
		}
	}

	private void clearSelection() {
		for (int i = 0; i < hh.size(); i++) {
			if (hh.get(i) instanceof FocusPanel) {
				final FocusPanel walk = (FocusPanel) hh.get(i);
				walk.removeStyleName(FtrGwtDateCss.HOURS_SELECTED);
			}
		}
	}

	public void setElementWide(int newWide) {
		// for (int i = 0; i < events.size(); i++) {
		// final EventPanel ev = (EventPanel) events.get(i);
		// ev.setWidth(newWide + "px");
		// }

	}

	public void notifyParentOfUpdate(DateEventActions action, T data) {
		DateEvent.fire(this, action, data);
	}

	public void handleDateEvent(DateEvent<T> newEvent) {
		GWT.log("Here foor " + newEvent.getCommand(), null);
		DateEvent.fire(this, newEvent.getDate(), newEvent.getCommand(), newEvent.getData());

	}

}

