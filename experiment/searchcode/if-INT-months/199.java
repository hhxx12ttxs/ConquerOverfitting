/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uip.todoapp.minical;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.GregorianCalendar;
import javax.swing.*;
import org.joda.time.DateTime;
import org.joda.time.IllegalFieldValueException;

/**
 *
 * @author Mesfin Mamuye
 */
public class MiniCalendar extends JPanel {
     
    JLabel lblMonth, lblYear;
    JButton btnPrev, btnNext;
    JTable tblCalendar;
    JComboBox cmbYear;
    JComboBox cmbMonth;
    JFrame frmMain;
    JPanel jpanel;
    int months;
    int years;
    int day;
    //static DefaultTableModel mtblCalendar; //Table model
    JScrollPane stblCalendar; //The scrollpane
    JPanel pnlCalendar; //The panel
    int realDay, realMonth, realYear, currentMonth, currentYear;
    //MonthViewCalendar monthViewCalendar;
    //TodoForm mainForm; // main frame
    private static final long serialVersionUID = 1L;
    //private org.apache.log4j.Logger log = Logger
    //.getLogger(TodoForm.class);
    public static Dimension HGAP15 = new Dimension(15, 1);
    static final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
        "Sun"}; // list of days
    public static Dimension HGAP20 = new Dimension(20, 1);
    //DateTime selectedDate; // this date task will be shown in calendar
    int year;
    int month;
    JPanel listPane = new JPanel();
    JPanel ttilePane = new JPanel();
    JPanel calanderPane = new JPanel();

    public MiniCalendar() {
        iniComponent();
    }

    private void iniComponent() {
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    	
        lblMonth = new JLabel("January");
        lblYear = new JLabel("Change year:");
        btnPrev = new JButton("<<");
        btnPrev.addActionListener(new BackCalendarButtonListner(this));
        btnNext = new JButton(">>");
        btnNext.addActionListener(new NextCalanderButtonListner(this));
        cmbYear = new JComboBox();
        cmbYear.addActionListener(new YearCalenderComboxListner(this));
        cmbMonth = new JComboBox();
        cmbMonth.addActionListener(new MonthCalenderComboxListner(this));
        // monthViewCalendar = new MonthViewCalendar();
        jpanel = new JPanel();
        addControlToPanel();
        setBoundComponents();
        assignCurrentMonthYear();
        // addHeaderOfCalendar();
        populateYearToComboBox();
        populateMonthToComboBox();
        refreshCalendar(realMonth, realYear);
        // buildDay(realYear, realMonth);



    }

    private void addControlToPanel() {
        jpanel.add(btnPrev);
        jpanel.add(cmbMonth);
        jpanel.add(cmbYear);


        jpanel.add(btnNext);
        this.add(jpanel);
        this.setBorder(BorderFactory.createTitledBorder("Calendar"));

    }

    private void setBoundComponents() {
        //Set bounds
        this.setBounds(20, 0, 250, 350);
        lblMonth.setBounds(160 - lblMonth.getPreferredSize().width / 2, 25, 100, 25);
        lblYear.setBounds(10, 305, 80, 20);
        cmbMonth.setBounds(230, 305, 80, 20);
        cmbYear.setBounds(60, 305, 80, 20);

        btnPrev.setBounds(10, 25, 50, 25);
        btnNext.setBounds(260, 25, 50, 25);
        listPane.setBounds(30, 50, 250, 228);


    }

    private void assignCurrentMonthYear() {
        //Get real month/year
        GregorianCalendar cal = new GregorianCalendar(); //Create calendar
        realDay = cal.get(GregorianCalendar.DAY_OF_MONTH); //Get day
        realMonth = cal.get(GregorianCalendar.MONTH); //Get month
        realYear = cal.get(GregorianCalendar.YEAR); //Get year
//         DateTime dt1 = new DateTime();
//        realMonth =dt1.getMonthOfYear();
//        realDay =dt1.getDayOfMonth();
//        realYear= dt1.getYear();
        currentMonth = realMonth; //Match month and year
        currentYear = realYear;

    }

    private void populateYearToComboBox() {
        //Populate combo box
        for (int i = realYear - 100; i <= realYear + 100; i++) {
            cmbYear.addItem(String.valueOf(i));
        }
    }

    private void populateMonthToComboBox() {
        //Populate combo box
        String[] monthss = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

        for (int i = 0; i < monthss.length; i++) {
            cmbMonth.addItem(monthss[i]);
        }
    }

    public void refreshCalendar(int month, int year) {

        String[] monthss = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        btnPrev.setEnabled(true); //Enable buttons at first
        btnNext.setEnabled(true);
        if (month == 0 && year <= realYear - 100) {
            btnPrev.setEnabled(false);
        } //Too early
        if (month == 11 && year >= realYear + 100) {
            btnNext.setEnabled(false);
        } //Too late
        cmbYear.setSelectedItem(String.valueOf(year)); //Select the correct year in the combo box
        cmbMonth.setSelectedItem(String.valueOf(monthss[month])); //Select the correct month in the combo box
       

    }

    public int findIndexOfMonth(String month) {
        String[] monthList = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

        for (int i = 0; i < 12; i++) {
            if (month.equals(monthList[i])) {
                return i;
            }
        }
        return 0;
    }

    public void buildDay(int year, int month) {
        listPane.removeAll();
        // this.updateUI();
        this.setYear(year);
        this.setMonth(month);

        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
        this.add(listPane);
        listPane.add(createMonthlyCalendarTitle());
        listPane.add(createManthCalendar(year, month));
        listPane.updateUI();

    }
    /*
     * create title bar for manthly calender retrun JComponent instance
     */

    public JComponent createMonthlyCalendarTitle() {
        JPanel panel = new JPanel();
        GridLayout gridLayout = new GridLayout(1, 7);

        panel.setLayout(gridLayout);
        for (int i = 0; i < days.length; i++) {
            DayTitle d = new DayTitle(days[i]);
            panel.add(d, Component.CENTER_ALIGNMENT);
        }

        return panel;
    }

    /*
     * create day card panel to show monthly calendar
     *
     * @param int year , given year calender will be displayed
     *
     * @param int month, given month will be displayed according to year
     */
    public JComponent createManthCalendar(int year, int month) {
        int currentDay = 0;
        if ((month == realMonth && year == realYear)) { //Today
            currentDay = realDay;
        }

        JPanel panel = new JPanel();
        GridLayout gridLayout = new GridLayout(6, 7);


        panel.setLayout(gridLayout);

        int count = 1;

        try {

            int dayMonth = 1; // day of month
            int dayWeek = 1; // day of week
            do {

                DateTime dt1 = new DateTime(year, month + 1, dayMonth, 0, 0, 0, 0);
                //log.debug("  day " + dt1.dayOfWeek().getAsText());

                //log.debug("Date " + dt1.toString() + " DayOfMonth : "
                //+ dt1.getDayOfMonth() + " DayOfWeek "
                //+ dt1.getDayOfWeek());

                if (dayWeek == dt1.getDayOfWeek()) { // days will be printed
                    // only when day of week
                    // of current loop date
                    // and loop dayWeek will
                    // be same
                    panel.add(new DayComponet(dt1, this, currentDay));
                    dayMonth++;
                } else {
                    panel.add(new DayComponet(null, this, currentDay));

                }
                dayWeek++;
                if (dayWeek == 8) {
                    dayWeek = 1;

                }
                count++;
            } while (dayMonth < 32); // if days more than 31 than loop will be
            // stop

        } catch (IllegalFieldValueException e) { // if given month days is less
            // than loop value , like if
            // dayMonth value is 31 but
            // that month max day is 30
            // then exception will be
            // raised
            //log.error("error " + e.getMessage());
        }

        do {
            panel.add(new DayComponet(null, this, currentDay));
            count++;
        } while (count < 43);

        return panel;
    }

    /**
     * @return the month
     */
    public int getMonth() {
        return month;
    }

    /**
     * @param month the month to set
     */
    public void setMonth(int month) {
        this.month = month;
    }

    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * @return the day
     */
    public int getDay() {
        return day;
    }

    /**
     * @param day the day to set
     */
    public void setDay(int day) {
        this.day = day;
    }

    /**
     * @return the months
     */
    public int getMonths() {
        return months;
    }

    /**
     * @param months the months to set
     */
    public void setMonths(int months) {
        this.months = months;
    }

    /**
     * @return the years
     */
    public int getYears() {
        return years;
    }

    /**
     * @param years the years to set
     */
    public void setYears(int years) {
        this.years = years;
    }
}

