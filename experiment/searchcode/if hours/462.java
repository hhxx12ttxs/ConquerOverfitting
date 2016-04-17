package com.mikeyaworski.workhoursanalyzation;

public class Job {
    
    // instance variables
    private String jobName = "", firstDate = "", lastDate = "";
    private double hours = 0;
    private int numberOfShifts = 0;
    
    // constructor methods
    public Job (String jobName, String firstDate, String lastDate, double hours) {
        this.jobName = jobName;
        this.firstDate = firstDate;
        this.lastDate = lastDate;
        this.hours = hours;
    }
    public Job (String jobName) {
        this.jobName = jobName;
    }
    
    // method to print this object's properties
    @Override
    public String toString() {
        
        String hoursStr = String.valueOf((int)this.hours) + " hours";
        double minutesPercentage = hours % 1;
        if (minutesPercentage == 0.75) {
            hoursStr = (int)Math.floor(hours) + " hours 45 minutes";
        } else if (minutesPercentage == 0.5) {
            hoursStr = (int)Math.floor(hours) + " hours 30 minutes";
        } else if (minutesPercentage == 0.25) {
            hoursStr = (int)Math.floor(hours) + " hours 15 minutes";
        }
        
        String str = "Job Name: " + this.jobName + "\n" + 
                     "First Date Ever: " + this.firstDate + "\n" + 
                     "Last Date Ever: " + this.lastDate + "\n" + 
                     "Total Hours: " + hoursStr + "\n" +
                     "Average Hours Per Week: " + getAverageHoursPerWeek() + "\n" +
                     "Average Hours Per Shift: " + getAverageHoursPerShift()
                     ;
        
        return str;
    }
    
    // accessor methods
    public String getName() {
        return this.jobName;
    }
    public String getFirstDate() {
        return this.firstDate;
    }
    public String getLastDate() {
        return this.lastDate;
    }
    public double getHours() {
        return this.hours;
    }
    public String getAverageHoursPerWeek() {
        double numberOfWeeks = Dates.weekDifference(this.firstDate, this.lastDate);
        if (numberOfWeeks < 1) numberOfWeeks = 1;
        double avgHours = this.hours / numberOfWeeks;
        
        return Work_Hours_Analyzation.convertHoursToString(avgHours);
    }
    public String getAverageHoursPerShift() {        
        double avgHours = this.hours / this.numberOfShifts;        
        return Work_Hours_Analyzation.convertHoursToString(avgHours);
    }
    public int getNumberOfShifts() {
        return this.numberOfShifts;
    }
    
    // mutator methods
    public void setName(String jobName) {
        this.jobName = jobName;
    }
    public void setFirstDate(String firstDate) {
        this.firstDate = firstDate;
    }
    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }
    public void setHours(double hours) {
        this.hours = hours;
    }
    public void addHours(double hours) {
        this.hours += hours;
    }
    public void addShift() {
        this.numberOfShifts++;
    }
}

