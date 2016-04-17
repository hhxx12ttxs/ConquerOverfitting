/*
Author: Antony Rivera
Date: 10/7/2014
 */
package wagecalculator;

/*

 */
public class Employee {
    private String name,ssn;
    private double hourlyRate;
    private int hoursWorked;
    
    Employee() {
        name="Joe";
        ssn="123-45-6789";
        hourlyRate=0;
        hoursWorked=0;
    }
    
    Employee(String name,String ssn, double rate) {
        this.ssn = ssn;
        this.name = name;
        hourlyRate = rate;
        hoursWorked = 0;
    }
    
    public void setHoursWorked(int hours) {
        if (hours>=0){
            hoursWorked = hours;
        }
    }
    
    public double computeWages() {
        return hourlyRate * hoursWorked;
    }
    
    public String toString() {
        return name + ", " + ssn + ", " + hourlyRate + ", " + hoursWorked
                + ", " + computeWages();
    }
}

