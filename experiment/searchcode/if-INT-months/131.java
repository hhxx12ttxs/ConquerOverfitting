package net.mufly.client.ui;

/*
 * Copyright 2007 Manuel Carrasco Mo?ino. (manuel_carrasco at users.sourceforge.net)
 * http://code.google.com/p/gwtchismes
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
import java.util.Date;


import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ChangeListenerCollection;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.Widget;
/*
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.constants.DateTimeConstants;
 */
/**
 * @author Manuel Carrasco Mo?ino
 *
     <h3>Class description</h3>
       <p>
         A widget to pick a date. It could be implemented as an independent
         dialog box or it could be included into another widget.
       </p>
       <p>
         You can configure minimalDate, maximalDate, cursorDate and locales
         (day names, month names, help, and weekStart)
       </p>
       <p>
         This class has public static methods useful for Date manipulation
       </p>
   <h3>Example</h3>
      <pre>
        // Configure internationalized strings using english language
        private String[] days_en = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
        private String[] months_en = new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };

        // Create a GWTCDatePicker that is show into the page
        final GWTCDatePicker picker_en = new GWTCDatePicker(false);
        // Internationalization
        picker_en.setLocale(days_en, months_en, 0);
        // Disable close button, becouse it is not a dialog
        picker_en.disableCloseButton();
        // Configure limits
        picker_en.setMinimalDate(GWTCDatePicker.increaseYear(new Date(), -1));
        picker_en.setMaximalDate(GWTCDatePicker.increaseYear(new Date(), 10));
        // Add an action when the user selects a day
        picker_en.addChangeListener(new ChangeListener() {
            public void onChange(Widget sender) {
                Window.alert(picker_en.getSelectedDateStr("MMMM dd, yyyy (dddd)"));
            }
        });
        // Repaint the calendar
        picker_en.drawCalendar();

      </pre>

       <h3>CSS Style Rules</h3>
         <ul>
           <li>.GWTCDatePicker { GWTCDatePicket container, it can be overwritten }</li>
           <li>.Caption { calendar text }</li>
           <li>.Cal_buttons { navigation buttons }</li>
           <li>.Cal_Header { text with the current month and year }</li>
           <li>.Cal_WeekHeader { week headers row}</li>
           <li>.Cal_CellDayNames { cells with day names} </li>
           <li>.Cal_CellEmpty { cell without days }</li>
           <li>.Cal_InvalidDay { cell with days which can not be selected because are out of the allowed interval }</li>
           <li>.Cal_Selected { selected day }</li>
           <li>.Cal_AfterSelected { days after the selected day and before the maximal day } </li>
           <li>.Cal_BeforeSelected { days before the selected day and after the minimal day}</li>
           <li>.Cal_Today { today } </li>
         </ul>
 */
@SuppressWarnings("deprecation")
public class GWTCDatePicker extends Composite implements ClickListener, SourcesChangeEvents {
	// Style classes
	private String styleName = "GWTCDatePicker";

	private static String StyleCButtons = "Cal_Buttons";

	private static String StyleCHeader = "Cal_Header";

	private static String StyleCGrid = "Cal_Grid";

	private static String StyleCWeekHeader = "Cal_WeekHeader";

	private static String StyleCCellDayNames = "Cal_CellDayNames";

	private static String StyleCCellEmpty = "Cal_CellEmpty";

	private static String StyleCCellDays = "Cal_CellDays";

	private static String StyleCInvalidDay = "Cal_InvalidDay";

	private static String StyleCSelected = "Cal_Selected";

	private static String StyleCAfterSelected = "Cal_AfterSelected";

	private static String StyleCBeforeSelected = "Cal_BeforeSelected";

	private static String StyleCToday = "Cal_Today";

	// Configurable parameters

	private Date minimalDate = setHourToZero(new Date());

	private Date selectedDate = setHourToZero(new Date());

	private Date cursorDate = setHourToZero(new Date());

	private Date maximalDate = GWTCDatePicker.increaseDate(selectedDate, 365);

	// Internationalizable elements
	private String[] days = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };

	private String[] months = new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };

	private int weekStart = 1;

	private String helpStr = "Calendar-Picker is a component of GWTChismes library.\n" +
	"(c) Manuel Carrasco 2007\nhttp://code.google.com/p/gwtchismes\n\n" +
	"Navigation buttons:\n" +
	"\u003c Previous Month\n\u003e Next Month\n\u00AB Previous Year\n\u00BB Next Year\n- Actual Month\nx Close\n ";

	// Containers
	private final DockPanel outer = new DockPanel();

	private DialogBox calendarContainer = null;

	// Navigation Buttons
	private final DockPanel navButtons = new DockPanel();

	private final DockPanel bottonButtons = new DockPanel();

	private final DockPanel topButtons = new DockPanel();

	private final HTML titleBtn = new HTML();

	private final GWTCButton helpBtn = new GWTCButton("?", this);

	private final GWTCButton closeBtn = new GWTCButton("x", this);

	private final GWTCButton actualMBtn = new GWTCButton("-", this);

	// public final GWTCButton prevMBtn = new GWTCButton("\u003c", this);
	private final GWTCButton prevMBtn = new GWTCButton("&lt;", this);

	private final GWTCButton prevYBtn = new GWTCButton("\u00AB", this);

	private final GWTCButton nextMBtn = new GWTCButton("\u003e", this);

	private final GWTCButton nextYBtn = new GWTCButton("\u00BB", this);

	private HorizontalPanel prevButtons = new HorizontalPanel();

	private HorizontalPanel nextButtons = new HorizontalPanel();

	private boolean btnClose = false;

	/**
	 * Constructor, you need specify the behaviour: floating dialog box or embeded widget
	 *
	 * @param dialog
	 *            true if you wan an independient and drageable dialog box when
	 *            the picker is showed
	 */
	public GWTCDatePicker(boolean dialog) {
		if (dialog) {
			calendarContainer = new DialogBox();
			calendarContainer.setWidget(outer);
			initWidget(new DockPanel());
			////drawCalendar();
		} else {
			initWidget(outer);
			//drawCalendar();
		}
		//helpBtn.setVisible(false);
		setHelp(null);

		setStyleName(styleName);

		navButtons.setStyleName(GWTCDatePicker.StyleCButtons);
		titleBtn.setStyleName(GWTCDatePicker.StyleCHeader);

		navButtons.add(bottonButtons, DockPanel.SOUTH);
		navButtons.add(topButtons, DockPanel.NORTH);

		prevButtons.add(prevYBtn);
		prevButtons.add(prevMBtn);
		nextButtons.add(nextMBtn);
		nextButtons.add(nextYBtn);

		bottonButtons.add(prevButtons, DockPanel.WEST);
		bottonButtons.add(actualMBtn, DockPanel.CENTER);
		bottonButtons.add(nextButtons, DockPanel.EAST);
		bottonButtons.setVerticalAlignment(DockPanel.ALIGN_MIDDLE);
		bottonButtons.setCellVerticalAlignment(actualMBtn, HasAlignment.ALIGN_MIDDLE);
		bottonButtons.setCellHorizontalAlignment(prevButtons, DockPanel.ALIGN_LEFT);
		bottonButtons.setCellHorizontalAlignment(nextButtons, DockPanel.ALIGN_RIGHT);
		bottonButtons.setCellHorizontalAlignment(actualMBtn, HasAlignment.ALIGN_CENTER);
		bottonButtons.setCellWidth(actualMBtn, "100%");
		actualMBtn.setWidth("100%");

		topButtons.add(helpBtn, DockPanel.WEST);
		topButtons.add(titleBtn, DockPanel.CENTER);
		topButtons.add(closeBtn, DockPanel.EAST);
		topButtons.setVerticalAlignment(DockPanel.ALIGN_MIDDLE);
		topButtons.setCellVerticalAlignment(titleBtn, HasAlignment.ALIGN_MIDDLE);
		topButtons.setCellHorizontalAlignment(helpBtn, DockPanel.ALIGN_LEFT);
		topButtons.setCellHorizontalAlignment(closeBtn, DockPanel.ALIGN_RIGHT);
		topButtons.setCellHorizontalAlignment(titleBtn, HasAlignment.ALIGN_CENTER);
		topButtons.setCellWidth(titleBtn, "100%");
	}

	/**
	 * Sets the object's style name to the calendar container, removing all
	 * other styles.
	 *
	 * @see com.google.gwt.user.client.ui.UIObject#setStyleName(java.lang.String)
	 */
	public void setStyleName(String s) {
		styleName = s;
		if (calendarContainer != null) {
			calendarContainer.setStyleName(styleName);
		} else
			outer.setStyleName(styleName);
	}

	/**
	 * Adds a secondary or dependent style name to this object
	 *
	 * @see com.google.gwt.user.client.ui.UIObject#addStyleName(java.lang.String)
	 */
	public void addStyleName(String s) {
		if (calendarContainer != null) {
			calendarContainer.addStyleName(s);
		} else
			outer.addStyleName(s);
	}

	/**
	 * Draw or redraw all calendar elements into the container
	 *
	 */
	public void drawCalendar() {

		FlexTable grid = new FlexTable();
		grid.setStyleName(GWTCDatePicker.StyleCGrid);
		grid.setCellSpacing(0);

		outer.clear();
		outer.add(navButtons, DockPanel.NORTH);
		outer.add(grid, DockPanel.CENTER);

		titleBtn.setHTML(GWTCDatePicker.formatDate("MMMM, yyyy", cursorDate, months, days));

		grid.getRowFormatter().setStyleName(0, GWTCDatePicker.StyleCWeekHeader);
		int l = 0;
		for (int i = weekStart; i < 7; i++) {
			grid.getCellFormatter().setStyleName(0, l, GWTCDatePicker.StyleCCellDayNames);
			grid.setText(0, l++, ((String) days[i]).substring(0, 3));
		}
		if (l < 7) {
			grid.getCellFormatter().setStyleName(0, l, GWTCDatePicker.StyleCCellDayNames);
			grid.setText(0, l++, ((String) days[0]).substring(0, 3));
		}

		Date firstDate = new Date(cursorDate.getYear(), cursorDate.getMonth(), 1);
		long todayNum = 1 + GWTCDatePicker.compareDate(firstDate, new Date());
		long minimalNum = 1 + GWTCDatePicker.compareDate(firstDate, minimalDate);
		long maximalNum = 1 + GWTCDatePicker.compareDate(firstDate, maximalDate);
		long selectedNum = 1 + GWTCDatePicker.compareDate(firstDate, selectedDate);
		// long cursorNum = 1 + GWTCDatePicker.compareDate(firstDate,
		// cursorDate);
		int firstWDay = firstDate.getDay();
		int numOfDays = GWTCDatePicker.daysInMonth(cursorDate);
		int j = 0 + weekStart;

		for (int i = 1; i < 7; i++) { // each row in the grid
			for (int k = 0; k < 7; k++, j++) { // each day in the week
				int displayNum = (firstWDay < weekStart) ? (j - firstWDay - 6) : (j - firstWDay + 1);
				if (j < firstWDay || displayNum > numOfDays || displayNum <= 0) {
					grid.getCellFormatter().setStyleName(i, k, GWTCDatePicker.StyleCCellEmpty);
					grid.setHTML(i, k, "&nbsp;");
				} else {
					HTML html = new CellHTML(displayNum);
					grid.getCellFormatter().setStyleName(i, k, GWTCDatePicker.StyleCCellDays);
					html.setStyleName(GWTCDatePicker.StyleCCellDays);
					if (displayNum < minimalNum || displayNum > maximalNum) {
						html.addStyleName(GWTCDatePicker.StyleCInvalidDay);
						grid.getCellFormatter().addStyleName(i, k, GWTCDatePicker.StyleCInvalidDay);
					} else if (displayNum == selectedNum) {
						html.addStyleName(GWTCDatePicker.StyleCSelected);
						grid.getCellFormatter().addStyleName(i, k, GWTCDatePicker.StyleCSelected);
						html.addClickListener(this);
					} else if (displayNum >= selectedNum) {
						html.addStyleName(GWTCDatePicker.StyleCAfterSelected);
						grid.getCellFormatter().addStyleName(i, k, GWTCDatePicker.StyleCAfterSelected);
						html.addClickListener(this);
					} else {
						html.addStyleName(GWTCDatePicker.StyleCBeforeSelected);
						grid.getCellFormatter().addStyleName(i, k, GWTCDatePicker.StyleCBeforeSelected);
						html.addClickListener(this);
					}
					if (displayNum == todayNum) {
						html.addStyleName(GWTCDatePicker.StyleCToday);
						grid.getCellFormatter().addStyleName(i, k, GWTCDatePicker.StyleCToday);
					}
					// else if (displayNum == cursorNum) {
					// grid.getCellFormatter().addStyleName(i, k, "Cal_Cursor");
					// }
					grid.setWidget(i, k, html);
				}
			}
		}
		prevMBtn.setEnabled(isVisibleMonth(cursorDate, -1));
		nextMBtn.setEnabled(isVisibleMonth(cursorDate, 1));
		prevYBtn.setEnabled(isVisibleMonth(cursorDate, -12));
		nextYBtn.setEnabled(isVisibleMonth(cursorDate, 12));

	}

	/**
	 * Unhide the calendar container, if the calendar picker is a dialog box and
	 * param sender is not null the dialog is positioned near of it
	 *
	 * @param sender
	 *            the widget that the user has clicked
	 */
	public void show(Widget sender) {
		if (calendarContainer != null) {
			calendarContainer.show();
			if (sender != null) {
				int left = sender.getAbsoluteLeft() + 10 - 200;
				int top = sender.getAbsoluteTop() + 30;
				calendarContainer.setPopupPosition(left, top);
				DOM.scrollIntoView(calendarContainer.getElement());
			}
		} else
			outer.setVisible(true);
	}

	/**
	 * Hide the calendar container.
	 *
	 */
	public void hide() {
		if (calendarContainer != null)
			calendarContainer.hide();
		else
			outer.setVisible(false);
	}

	/**
	 * Set the text for the caption of the dialog box. It is only available if the calendar is shown as a dialog.
	 *
	 * @param t
	 *            the message to display
	 */
	public void setText(String t) {
		if (calendarContainer != null)
			calendarContainer.setText(t);
	}

	/**
	 * Set the help text.
	 *
	 * @param t
	 *            the help message to display, if t is null help button is
	 *            disabled
	 */
	public void setHelp(String t) {
		helpStr = t;
		if (t == null || t.length() == 0)
			helpBtn.setEnabled(false);
		else
			helpBtn.setEnabled(true);
	}

	/**
	 * Disable the close Button
	 */
	public void disableCloseButton(){
		this.closeBtn.setVisible(false);
	}

	/**
	 * Internationalize the calendar.
	 *
	 * @param d
	 *            array with the full names of the week days (default english
	 *            names [Sunday ... Saturday] )
	 * @param m
	 *            array with the full names of the months (default english
	 *            names: [January ... December])
	 * @param s
	 *            number of the first day in the week [1...7] (default 1 =
	 *            sunday)
	 */
	public void setLocale(String[] d, String[] m, int s) {
		if (days.length >= 7)
			days = d;
		if (months.length >= 12)
			months = m;
		weekStart = s;
		drawCalendar();
	}

	/**
	 * This method returns true or false whether a month has selectable days in
	 * the allowed interval
	 *
	 * @param date
	 *            Date of the selected day
	 * @param months
	 *            increment of months
	 * @return true if the month has selectable days
	 */
	public boolean isVisibleMonth(Date date, int months) {
		Date d = GWTCDatePicker.increaseMonth(date, months);
		Date firstD = new Date(d.getTime());
		firstD.setDate(1);
		Date lastD = new Date(d.getTime());
		lastD.setDate(GWTCDatePicker.daysInMonth(d));

		if (GWTCDatePicker.compareDate(minimalDate, lastD) < 0) {
			return false;
		}
		if (GWTCDatePicker.compareDate(maximalDate, firstD) > 0) {
			return false;
		}
		return true;
	}

	/**
	 * Set the date where the calendar is positioned
	 *
	 * @param d
	 *            Date
	 */
	public void setCursorDate(Date d) {
		if (isVisibleMonth(d, 0)) {
			cursorDate = setHourToZero(d);
		}
	}

	/**
	 * Set the date selected by the user
	 *
	 * @param d
	 *            Date
	 */
	public void setSelectedDate(Date d) {
		cursorDate = setHourToZero(d);
		selectedDate = cursorDate;
		drawCalendar();
	}

	/**
	 * Set the minimal selectable date
	 *
	 * @param d
	 *            Date
	 */
	public void setMinimalDate(Date d) {
		minimalDate = setHourToZero(d);
		if (maximalDate.getTime() < minimalDate.getTime())
			maximalDate = d;
		if (selectedDate.getTime() < minimalDate.getTime())
			selectedDate = d;
		if (cursorDate.getTime() < minimalDate.getTime())
			this.setSelectedDate(d);
	}

	/**
	 * Set the maximal selectable date
	 *
	 * @param d
	 *            Date
	 */
	public void setMaximalDate(Date d) {
		maximalDate = setHourToZero(d);
		if (minimalDate.getTime() > maximalDate.getTime())
			minimalDate = d;
		if (selectedDate.getTime() > maximalDate.getTime())
			selectedDate = d;
		if (cursorDate.getTime() > maximalDate.getTime())
			this.setSelectedDate(d);
	}

	/**
	 * Get the date selected by the user
	 *
	 * @return Date
	 */
	public Date getSelectedDate() {
		return selectedDate;
	}

	/**
	 * Get a string with the selected date in the desired format
	 *
	 * @param format
	 *            representation of the desired format [dddd ddd dd yyyy yy MMMM  MMM MM]
	 * @return String
	 */
	public String getSelectedDateStr(String format) {
		return formatDate(format, selectedDate, months, days);
	}

	/*
	 * (non-Javadoc)
	 */
	private ChangeListenerCollection changeListeners;
	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.ui.ClickListener#onClick(com.google.gwt.user.client.ui.Widget)
	 */
	public void onClick(Widget sender) {
		btnClose = false;
		if (sender == prevMBtn) {
			setCursorDate(GWTCDatePicker.increaseMonth(cursorDate, -1));
			drawCalendar();
		} else if (sender == nextMBtn) {
			setCursorDate(GWTCDatePicker.increaseMonth(cursorDate, 1));
			drawCalendar();
		} else if (sender == prevYBtn) {
			setCursorDate(GWTCDatePicker.increaseYear(cursorDate, -1));
			drawCalendar();
		} else if (sender == nextYBtn) {
			setCursorDate(GWTCDatePicker.increaseYear(cursorDate, 1));
			drawCalendar();
		} else if (sender == actualMBtn) {
			setCursorDate(new Date());
			drawCalendar();
		} else if (sender == helpBtn) {
			Window.alert(helpStr);
		} else if (sender == closeBtn) {
			if (calendarContainer != null) {
				calendarContainer.hide();
				if (changeListeners != null) {
					btnClose = true;
					changeListeners.fireChange(this);
				}
			} else {
				outer.setVisible(false);
			}
		} else {
			CellHTML cell = (CellHTML) sender;
			setSelectedDate(new Date(cursorDate.getYear(), cursorDate.getMonth(), cell.getDay()));
			if (changeListeners != null) {
				changeListeners.fireChange(this);
			}
		}
	}

	public boolean getIsCloseButton() {
		return this.btnClose;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.ui.SourcesChangeEvents#addChangeListener(com.google.gwt.user.client.ui.ChangeListener)
	 */
	public void addChangeListener(ChangeListener listener) {
		if (changeListeners == null)
			changeListeners = new ChangeListenerCollection();
		changeListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.gwt.user.client.ui.SourcesChangeEvents#removeChangeListener(com.google.gwt.user.client.ui.ChangeListener)
	 */
	public void removeChangeListener(ChangeListener listener) {
		if (changeListeners != null)
			changeListeners.remove(listener);
	}

	private static final int YEARS = 1;
	private static final int MONTHS = 2;
	private static final int DAYS = 3;

	/**
	 * Add days to a reference Date
	 *
	 * @param d
	 *            Date of reference
	 * @param n
	 *            number of days to increase (for decrease use negative values)
	 * @return the new Date
	 */
	public static Date increaseDate(Date d, int n) {
		Date ret = new Date(GWTCDatePicker.add(d.getTime(), n, GWTCDatePicker.DAYS));
		return ret;
	}

	/**
	 * Add months to a reference Date
	 *
	 * @param d
	 *            Date of reference
	 * @param n
	 *            number of months to increase (for decrease use negative
	 *            values)
	 * @return the new Date
	 */
	public static Date increaseMonth(Date d, int n) {
//		if (d.getDate() > 28) {
//		Date tmp = new Date(d.getTime());
//		tmp.setDate(1);
//		GWTCDatePicker.add(tmp.getTime(), n, GWTCDatePicker.MONTHS);
//		int d1 = daysInMonth(d);
//		int d2 = daysInMonth(tmp);
//		if (d1 > d2)
//		d.setDate(d2);
//		}

//		return new Date(GWTCDatePicker.add(d.getTime(), n, GWTCDatePicker.MONTHS));
		if (d.getDate() > 28) {
			Date tmp = new Date(d.getTime());
			tmp.setDate(1);
			return new Date(GWTCDatePicker.add(tmp.getTime(), n, GWTCDatePicker.MONTHS));
		}
		else{
			return new Date(GWTCDatePicker.add(d.getTime(), n, GWTCDatePicker.MONTHS));
		}
	}

/**
 * Add years to a reference Date
 *
 * @param d
 *            Date of reference
 * @param n
 *            number of years to increase (for decrease use negative values)
 * @return the new Date
 */
public static Date increaseYear(Date d, int n) {
	return new Date(GWTCDatePicker.add(d.getTime(), n, GWTCDatePicker.YEARS));
}

/**
 * Calculate the number of days in a month
 *
 * @param d
 *            reference date
 * @return the number of days in this month [1...31]
 */
public static int daysInMonth(Date d) {
//	Date nd = new Date(GWTCDatePicker.add(d.getTime(), 1, GWTCDatePicker.MONTHS));
//	return GWTCDatePicker.compareDate(d, nd);
	final int daysPerMonth[] = {31,28,31,30,31,30,31,31,30,31,30,31};
	int month = d.getMonth();
	int year  = d.getYear();
	int days = daysPerMonth[month];

	//Check for leap days in Feb.
	if (month == 1) {
		if ((((year % 4) == 0) && ((year % 100) != 0)) || ((year % 400) == 0)) {
			days++; //add a leap day
		}
	}
	return days;
}

/**
 * Calculate the number of days betwen two dates
 *
 * @param a
 *            Date
 * @param b
 *            Date
 * @return the difference in days betwen b and a (b - a)
 */
public static int compareDate(Date a, Date b) {
	long d1 = setHourToZero(a).getTime();
	long d2 = setHourToZero(b).getTime();
	return (int) ((d2 - d1) / 1000 / 60 / 60 / 24);
}

/**
 * Increase/decrease a date based in a type parameter which specifies the type of operation
 *
 * This method is coded enterely in native javascript because it has native methods to increase dates
 *
 * @param time
 *            in milliseconds since 1-1-1970
 * @param value
 *            interval to add (use negative values to decrease)
 * @param type
 *            type of addition (1=days, 2=months, 3=years, 4=hours
 * @return number of milliseconds from 1-1-1970
 */
private static native double addfix(double time, int value, int type)
/*-{
     var d = new Date(time);
     if (type == 1) {
       // this is a hack because getYear returns diferent results with diferent browsers
        var y = d.getYear();
        y = y < 1000 ? y + 1900 : y;
        d.setYear( value + y);
     }
     if (type == 2) d.setMonth(d.getMonth() + value);
     if (type == 3) d.setDate(d.getDate() + value);
     return d.getTime();
     }-*/;

private static long add(long time, int value, int type){
	double timedbl = time;
	double newtime = addfix(timedbl, value, type);

	long anstime = (long) newtime;
	return anstime;
}

/**
 * Set hour, minutes, second and milliseconds to zero.
 *
 * @param d
 *            Date
 * @return Modified date
 */
public static Date setHourToZero(Date in) {
	Date d = new Date(in.getTime());
	d.setHours(0);
	d.setMinutes(0);
	d.setSeconds(0);
	// a trick to set milliseconds to zero
	long t = d.getTime() / 1000;
	t = t * 1000;
	return new Date(t);
}

/**
 * Create a new Date
 *
 * @param y
 *            year (ie 1980)
 * @param m
 *            month (1...12)
 * @param d
 *            day of month (1...31)
 * @return new Date
 */
public static Date newDateFromYMD(int y, int m, int d) {
	Date dat = new Date();
	dat.setYear(y - 1900);
	dat.setMonth(m -1);
	dat.setDate(d);
	return setHourToZero(dat);
}

/**
 *  Basic method to format dates
 *
 *  TODO: use the new class DateTimeFormat available in GWT 1.4.  But still there are many people using old versions
 *
 * @param format
 *            (supported dddd ddd dd yyyy yy MMMM MMM MM)
 * @param date
 * @param months
 *            array with the months names [January ... December]
 * @param days
 *            array with the days names [Sunday .... Saturday]
 * @return formated string
 */
static public String formatDate(String format, Date date, String[] months, String days[]) {
	/*
        DateTimeFormat dateFormat  = DateTimeFormat.getFormat(format);
        Window.alert ("" + date + " " + dateFormat.format(date));
	 */

	if (date == null || format == null || months == null || days == null)
		return "NULL";
	String ret = format;
	String month = months[date.getMonth()];
	String day = days[date.getDay()];
	String month_3 = month.length() >= 3 ? month.substring(0, 3) : month;
	String day_3 = day.length() >= 3 ? day.substring(0, 3) : day;
	//String day_4 = day.length() >= 4 ? day.substring(0, 4) : day;
	ret = ret.replaceAll("ddddd", day);
	ret = ret.replaceAll("dddd", day);
	ret = ret.replaceAll("ddd", day_3);
	ret = ret.replaceAll("dd", leftPadding(String.valueOf(date.getDate()), "0", 2));
	ret = ret.replaceAll("yyyy", String.valueOf(1900 + date.getYear()));
	ret = ret.replaceAll("yy", "" + leftPadding(String.valueOf(date.getYear() % 100), "0", 2));
	ret = ret.replaceAll("MMMM", month);
	ret = ret.replaceAll("MMM", month_3);
	ret = ret.replaceAll("MM", leftPadding(String.valueOf(date.getMonth() + 1), "0", 2));
	return ret;
}

static private String leftPadding(String text, String character, int maxNumberChars) {
	StringBuffer ret = new StringBuffer();
	for (int i = text.length(); i < maxNumberChars; i++) {
		ret.append(character);
	}
	ret.append(text);
	return ret.toString();
}

/**
 * Basic Widget that represents each cell in the calendar picker
 *
 */
private static class CellHTML extends HTML {
	private int day;

	public CellHTML(int day) {
		super (String.valueOf(day));
		this.day = day;
	}

	public void addClickListener(ClickListener listener) {
		// If the cell has a click-listernet, we add a link, so Selenium is able to use it
//		setHTML("<a href=\"#\">" + String.valueOf(day) + "</a>");
		setHTML("<a href='javascript:;'>" + String.valueOf(day) + "</a>");

		super.addClickListener(listener);
		//  IE6 does not support div:hover style, this listener adds .gwtc-Hover class when the mouse is over
		if (GWTCButton.isIE6()) {
			this.addMouseListener(GWTCButton.mouseOverListener);
		}
	}

	public int getDay() {
		return day;
	}

}
}

