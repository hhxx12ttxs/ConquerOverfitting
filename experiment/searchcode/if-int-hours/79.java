package org.imogene.rcp.core.tools;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.imogene.rcp.core.wrapper.CoreMessages;


public class DateSelector extends Dialog{
	
	private Object result=null;
	
	private Shell shell;
	
	private DateTime dt;
	
	private Date date;
	
	private int widgetType = SWT.CALENDAR;
			
	public DateSelector (Shell parent) {
		this (parent, 0);
	}
	
	/**
	 * 
	 * @param parent parent shell
	 * @param widgetType type of wanted widget (Calendar = SWT.CALENDAR, Time = SWT.TIME)
	 */
	public DateSelector (Shell parent, int widgetType) {
		super (parent, 0);
		this.widgetType = widgetType;
	}
	
	public Object open() {
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		if (widgetType==SWT.TIME)
			shell.setText(CoreMessages.getString("date_selector_pick_time"));
		else
			shell.setText(CoreMessages.getString("date_selector_pick_date"));
		shell.setLayout(new GridLayout());
		dt = new DateTime(shell, widgetType);	
		fixDate(dt);
		Composite buttons = new Composite(shell, SWT.NONE);
		buttons.setLayout(new RowLayout());
		
		/* cancel button */
		Button cancel = new Button(buttons, SWT.PUSH);
		cancel.setText(CoreMessages.getString("date_selector_button_cancel"));
		cancel.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = null;
				shell.close();				
			}			
		});
		
		/* ok button */
		Button ok = new Button(buttons, SWT.PUSH);
		ok.setText(CoreMessages.getString("date_selector_button_ok"));
		ok.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (widgetType==SWT.TIME)
					result = createDateTime(dt.getHours(), dt.getMinutes(), dt.getSeconds());
				else
					result = createDate(dt.getDay(), dt.getMonth(), dt.getYear());
				
				shell.close();
			}		
		});
		shell.pack();
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		return result;
	}
	
	/**
	 * Create a Date from the DateTime.
	 * @param day day of month
	 * @param month month
	 * @param year year
	 * @return the matching date
	 */
	private Date createDate(int day, int month, int year){
		Calendar cal = new GregorianCalendar();
		cal.set(year, month, day);
		return cal.getTime();
	}	
	
	/**
	 * Create a DateTime from the DateTime.
	 * @param hours hour in the day
	 * @param minutes minutes
	 * @param seconds seconds
	 * @return the matching date time
	 */
	private Date createDateTime(int hours, int minutes, int seconds){
		Date currentDate = new Date();
		Calendar cal = new GregorianCalendar();
		cal.setTime(currentDate);
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), hours, minutes, seconds);
		return cal.getTime();
	}
	
	/**
	 * Configure the DateTime with the initial date if exists
	 * @param dt the DateTime to set
	 */
	private void fixDate(DateTime dt){
		if(date != null){
			Calendar cal = new GregorianCalendar();
			cal.setTime(date);
			
			dt.setDay(cal.get(Calendar.DAY_OF_MONTH));
			dt.setMonth(cal.get(Calendar.MONTH));
			dt.setYear(cal.get(Calendar.YEAR));	
			
			if (widgetType==SWT.TIME) {				
				dt.setHours(cal.get(Calendar.HOUR_OF_DAY));
				dt.setMinutes(cal.get(Calendar.MINUTE));
				dt.setSeconds(cal.get(Calendar.SECOND));		
			}
	
		}
	}
	
	/**
	 * Set the initial date of the DateTime.
	 * @param date the initial date
	 */
	public void setDate(Date date){
		this.date = date;		
	}
	
}

