package com.corejsf.model;


import java.io.Serializable;
import java.math.BigDecimal;

public class TimesheetRow implements Serializable {
    
    /** Timesheet row index for Saturday. */
    public static final int SAT = 0;
    
    /** Timesheet row index for Sunday. */
    public static final int SUN = 1;
    
    /** Timesheet row index for Monday. */
    public static final int MON = 2;
    
    /** Timesheet row index for Tuesday. */
    public static final int TUE = 3;
    
    /** Timesheet row index for Wednesday. */
    public static final int WED = 4;
    
    /** Timesheet row index for Thursday. */
    public static final int THU = 5;
    
    /** Timesheet row index for Friday. */
    public static final int FRI = 6;
    
    /** The corresponding timesheet the row belongs to */
    private Timesheet timesheet;
    
    /** The ID of the row **/ 
    private int rowID;
    
    /** The projectID **/
    private int projectID;
    
    /** The WorkPackge **/
    private String workPackage;
    
    /** any notes added to the end of the row **/
    private String notes;
  
    
    /**
     * An array holding all the hours charged for each day of the week. Day 0 is
     * Saturday, ... day 6 is Friday
     */
    private BigDecimal[] hoursForWeek = new BigDecimal[Timesheet.DAYS_IN_WEEK];
    
    /**
     * TimesheetRow constructor
     * @param projectID
     * @param workPackage
     * @param notes
     * @param satHours
     * @param sunHours
     * @param monHours
     * @param tueHours
     * @param wedHours
     * @param thuHours
     * @param friHours
     */
    public TimesheetRow(int projectID, String workPackage, String notes, BigDecimal satHours, BigDecimal sunHours,
            BigDecimal monHours, BigDecimal tueHours, BigDecimal wedHours, BigDecimal thuHours, BigDecimal friHours) {
        this.projectID = projectID;
        this.workPackage = workPackage;
        this.notes = notes;
        setSatHours(satHours);
        setSunHours(sunHours);
        setMonHours(monHours);
        setTueHours(tueHours);
        setWedHours(wedHours);
        setThuHours(thuHours);
        setFriHours(friHours);
    }
    
    /**
     * TimesheetRow constructor
     * @param timesheet
     * @param projectID
     * @param workPackage
     * @param notes
     * @param satHours
     * @param sunHours
     * @param monHours
     * @param tueHours
     * @param wedHours
     * @param thuHours
     * @param friHours
     * @param rowID
     */
    public TimesheetRow(Timesheet timesheet, int projectID, String workPackage, String notes, BigDecimal satHours, BigDecimal sunHours,
            BigDecimal monHours, BigDecimal tueHours, BigDecimal wedHours, BigDecimal thuHours, BigDecimal friHours, int rowID) {
        this.timesheet = timesheet;
        this.rowID = rowID;
        this.projectID = projectID;
        this.workPackage = workPackage;
        this.notes = notes;
        setSatHours(satHours);
        setSunHours(sunHours);
        setMonHours(monHours);
        setTueHours(tueHours);
        setWedHours(wedHours);
        setThuHours(thuHours);
        setFriHours(friHours);
    }

    /**
     * Default constructor
     */
    public TimesheetRow() {
        
    }

    /**
     * Sets the rowID
     * @param rowID
     */
    public void setRowID(int rowID) {
        this.rowID = rowID;
    }
    
    /**
     * 
     * @return the rowID
     */
    public int getRowID() {
        return rowID;
    }

    /***
     * 
     * @return the timesheet
     */
    public Timesheet getTimesheet() {
        return timesheet;
    }

    /**
     * Sets the timesheet
     * @param timesheet
     */
    public void setTimesheet(Timesheet timesheet) {
        this.timesheet = timesheet;
    }

    /**
     * Sets the projectID
     * @param projectID
     */
    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    /**
     * 
     * @return the projectID
     */
    public int getProjectID() {
        return projectID;
    }

    /**
     * Sets the work package
     * @param workPackage
     */
    public void setWorkPackage(String workPackage) {
        this.workPackage = workPackage;
    }

    /**
     * 
     * @return the workPackage
     */
    public String getWorkPackage() {
        return workPackage;
    }

    /**
     * 
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the notes
     * @param notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * 
     * @return the hours for saturday
     */
    public BigDecimal getSatHours() {
        return hoursForWeek[SAT];
    }

    /**
     * Set saturday hours
     * @param satHours
     */
    public void setSatHours(BigDecimal satHours) {
        hoursForWeek[SAT] = satHours;
    }

    /**
     * 
     * @return the hours for sunday
     */
    public BigDecimal getSunHours() {
        return hoursForWeek[SUN];
    }

    /**
     * Set sunday hours
     * @param sunHours
     */
    public void setSunHours(BigDecimal sunHours) {
        hoursForWeek[SUN] = sunHours;
    }

    /**
     * 
     * @return the hours for monday
     */
    public BigDecimal getMonHours() {
        return hoursForWeek[MON];
    }

    /**
     * Set monday hours
     * @param monHours
     */
    public void setMonHours(BigDecimal monHours) {
        hoursForWeek[MON] = monHours;
    }

    /**
     * 
     * @return the hours for tuesday
     */
    public BigDecimal getTueHours() {
        return hoursForWeek[TUE];
    }

    /**
     * Set tuesday hour
     * @param tueHours
     */
    public void setTueHours(BigDecimal tueHours) {
        hoursForWeek[TUE] = tueHours;
    }

    /**
     * 
     * @return the hours for wednesday
     */
    public BigDecimal getWedHours() {
        return hoursForWeek[WED];
    }

    /**
     * Set wednesday hour
     * @param wedHours
     */
    public void setWedHours(BigDecimal wedHours) {
        hoursForWeek[WED] = wedHours;
    }

    /**
     * 
     * @return the hours for thursday
     */
    public BigDecimal getThuHours() {
        return hoursForWeek[THU];
    }

    /**
     * Set thursday hours
     * @param thuHours
     */
    public void setThuHours(BigDecimal thuHours) {
        hoursForWeek[THU] = thuHours;
    }

    /**
     * 
     * @return the hours for friday
     */
    public BigDecimal getFriHours() {
        return hoursForWeek[FRI];
    }

    /**
     * Set friday hours
     * @param friHours
     */
    public void setFriHours(BigDecimal friHours) {
        hoursForWeek[FRI] = friHours;
    }
    
    /**
     * @return the hours charged for each day
     */
    public BigDecimal[] getHoursForWeek() {
        return hoursForWeek;
    }
    
    /***
     * 
     * @return the total hours for the whole row
     */
    public BigDecimal getTotalRowHours() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal next : hoursForWeek) {
            if (next != null) {
                sum = sum.add(next);
            }
        }
        return sum;
    }
    
    /**
     * @return the weekly hours
     */
    public BigDecimal getSum() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal next : hoursForWeek) {
            if (next != null) {
                sum = sum.add(next);
            }
        }
        return sum;
    }
}

