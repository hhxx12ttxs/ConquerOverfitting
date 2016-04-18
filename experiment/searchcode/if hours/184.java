package alectagms;

import java.util.Calendar;

/*
 * Access the daily attendance records and calculates the number of hours of
 * each employee required for salary calculation
 */

public class MonthlyAttendance {
    
    private String empID; //ID of the employee
    private int HOURS = 9; //No of maximum hours for work per day
    private int HOURS_SAT = 5; //No of maximum hours for Saturdays
    private int OT1 = 2; //No of maximum hours for OT1
    private int OT1_SAT = 6; //No of maximum hours for OT1 for Saturday
    private double work_hours; //No of normal working hours
    private double overTime1_hours; //No of OT1 hours
    private double overTime2_hours; //No of OT2 hours
    private double sunday_hours; //No of Sunday and Poya day hours
    private Calendar c; //Calendar instance to set the particular month to do the hour calculations
    private int absentDays;
    
    public MonthlyAttendance(int year, int month) { //Constructor sets the year, month details
        initiallizeHours();
        c = Calendar.getInstance();
        c.set(Calendar.YEAR, year); //Set the year of attendance
        c.set(Calendar.MONTH, month); //Set the month of attendance
    }
    
    private void initiallizeHours() {
        work_hours = 0; //Initiallize the hours to zero
        overTime1_hours = 0;
        overTime2_hours = 0;
        sunday_hours = 0;
        absentDays = 0;
    }
    
    private void calculateHours(String id) { //Calculate the no of hours of the Employee ID
        empID = id;
        initiallizeHours();
        
        //Access the DB to get difference between arrival and departure time
        int daysForMonth = c.getActualMaximum(Calendar.MONTH);
        double AT = 12.5;
        
        for( int day = 1; day <= daysForMonth; day++ ) {
            c.set(Calendar.DAY_OF_MONTH, day); //Sets the particular date in the calendar instance
            
            //*****Check if he is present or not
            //*****If absent increment absentDays count
            if( c.get(Calendar.DAY_OF_WEEK) != 1 && c.get(Calendar.DAY_OF_WEEK) != 7 ) { //Check whether the date is a weekday
                if( AT >= HOURS ) { //Check whether the no of hours he has worked is greater than the maximum working hours                
                    work_hours += HOURS; //Add the no of hours in normal working hours 
                    AT -= HOURS; //Reduce the no of hours to calculate the OTs                
                    if( AT >= OT1) { //Checks if the employee has worked both OT1 & OT2                       
                        overTime1_hours += OT1; //Add the no of hours for OT1
                        AT -= OT1; //Reduce the no of hours he has worked OT1
                        overTime2_hours += AT; //Add the no of hours for OT2
                    }
                    else {
                        overTime1_hours += AT;
                    }
                }
                else {
                    work_hours += AT;
                }
            }
            //If it is a Saturday, then the no of normal working hours will be HOURS_SAT
            else if( c.get(Calendar.DAY_OF_WEEK) == 7 ) { //If the date is a Saturday
                if( AT >= HOURS_SAT ) { //Check whether the no of hours he has worked is greater than the maximum working hours for Saturday                
                    work_hours += HOURS_SAT; //Add the no of hours in normal working hours for Saturday
                    AT -= HOURS_SAT; //Reduce the no of hours to calculate the OTs                
                    if( AT >= OT1_SAT) { //Checks if the employee has worked both OT1 & OT2                       
                        overTime1_hours += OT1_SAT; //Add the no of hours for OT1
                        AT -= OT1_SAT; //Reduce the no of hours he has worked OT1
                        overTime2_hours += AT; //Add the no of hours for OT2
                    }
                    else {
                        overTime1_hours += AT;
                    }
                }
                else {
                    work_hours += AT;
                }
            }
            //If it is a Sunday, then the employee is paid the double salary per hour normal HOURS
            else if( AT >= HOURS ) {
                work_hours += 2 * HOURS;
                AT -= HOURS; //Reduce the no of hours to calculate the OTs                
                if( AT >= OT1) { //Checks if the employee has worked both OT1 & OT2                       
                    overTime1_hours += OT1; //Add the no of hours for OT1
                    AT -= OT1; //Reduce the no of hours he has worked OT1
                    overTime2_hours += AT; //Add the no of hours for OT2
                }
                else {
                    overTime1_hours += AT;
                }
            }
            else {
                work_hours += AT;
            }
        }
    }

    public int getAbsentDays() {
        return absentDays;
    }

    public String getEmpID() {
        return empID;
    }

    public double getWork_hours() {
        return work_hours;
    }

    public double getOverTime1_hours() {
        return overTime1_hours;
    }

    public double getOverTime2_hours() {
        return overTime2_hours;
    }

    public double getSunday_hours() {
        return sunday_hours;
    }
}

