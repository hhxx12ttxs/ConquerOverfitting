package team10.cst438.sl_time_tracker_plus.DataClasses;

/**
 * Created by Nigel on 11/20/2015.
 */

public class Timesheet
{
    // Instance variables.
    private String timesheetStartDate;
    private int sundayHours;
    private int mondayHours;
    private int tuesdayHours;
    private int wednesdayHours;
    private int thursdayHours;
    private int fridayHours;
    private int saturdayHours;
    private boolean status;

    // Default constructor.
    public Timesheet()
    {
        this.timesheetStartDate = "";
        this.sundayHours = 0;
        this.mondayHours = 0;
        this.tuesdayHours = 0;
        this.wednesdayHours = 0;
        this.thursdayHours = 0;
        this.fridayHours = 0;
        this.saturdayHours = 0;
        this.status = false;
    }

    // Overloaded constructor.
    public Timesheet(String sd, int sun, int mon, int tue, int wed, int thu, int fri, int sat, boolean status)
    {
        this.timesheetStartDate = sd;
        this.sundayHours = sun;
        this.mondayHours = mon;
        this.tuesdayHours = tue;
        this.wednesdayHours = wed;
        this.thursdayHours = thu;
        this.fridayHours = fri;
        this.saturdayHours = sat;
        this.status = status;
    }

    // Mutator methods.
    public void setTimesheetStartDate(String sd) { timesheetStartDate = sd; }
    public void setSundayHours(int sun)          { sundayHours = sun; }
    public void setMondayHours(int mon)          { mondayHours = mon; }
    public void setTuesdayHours(int tue)         { tuesdayHours = tue; }
    public void setWednesdayHours(int wed)       { wednesdayHours = wed; }
    public void setThursdayHours(int thu)        { thursdayHours = thu; }
    public void setFridayHours(int fri)          { fridayHours = fri; }
    public void setSaturdayHours(int sat)        { saturdayHours = sat; }
    public void setStatus(boolean s)             { status = s; }

    // Accessor methods
    public String getTimesheetStartDate() { return timesheetStartDate; }
    public int getSundayHours()           { return sundayHours; }
    public int getMondayHours()           { return mondayHours; }
    public int getTuesdayHours()          { return tuesdayHours; }
    public int getWednesdayHours()        { return wednesdayHours; }
    public int getThursdayHours()         { return thursdayHours; }
    public int getFridayHours()           { return fridayHours; }
    public int getSaturdayHours()         { return saturdayHours; }
    public int getTotalHours()            { return sundayHours + mondayHours + tuesdayHours + wednesdayHours + fridayHours + saturdayHours; }
    public boolean getStatus()            { return status; }

    // Returns the timesheet status as a string instead of boolean.
    public String getStatusAsString()
    {
        if (status)
            return "APPROVED";
        else
            return "NOT APPROVED";
    }

    // To String method.
    public String toString()
    {
        String result = "";

        result.concat("Start Date: ");
        result.concat(this.getTimesheetStartDate());
        result.concat("\nSunday Hours: ");
        result.concat(Integer.toString(this.getSundayHours()));
        result.concat("\nMonday Hours: ");
        result.concat(Integer.toString(this.getMondayHours()));
        result.concat("\nTuesday Hours: ");
        result.concat(Integer.toString(this.getTuesdayHours()));
        result.concat("\nWednesday Hours: ");
        result.concat(Integer.toString(this.getWednesdayHours()));
        result.concat("\nThursday Hours: ");
        result.concat(Integer.toString(this.getThursdayHours()));
        result.concat("\nFriday Hours: ");
        result.concat(Integer.toString(this.getFridayHours()));
        result.concat("\nSaturday Hours: ");
        result.concat(Integer.toString(this.getSaturdayHours()));
        result.concat("\nStatus: ");
        result.concat(this.getStatusAsString());

        return result;
    }
}

