/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package my.concrete;
import inheritancepractice.*;

/**
 *
 * @author viewt_000
 */
public class HourlyEmployee extends Employee
{
    private double hoursWorked;
    private double hourlyRate;
    private double overtimeMultiplier;
    
    public HourlyEmployee(Name name, double hourlyRate, double overtimeMultiplier)
    {
        super(name);
        this.hoursWorked = 0;
        this.hourlyRate = hourlyRate;
        this.overtimeMultiplier = overtimeMultiplier;
    }
    
    public void addHours(double hours)
    {
        if(hours >= 0)
        {
            this.hoursWorked += hours;
        }
    }
    
    public double getHoursWorked()
    {
        return this.hoursWorked;
    }
    
    public double getPay()
    {
        double pay = 0;
        if(hoursWorked > 40)
        {
            pay += 40 * hourlyRate;
            hoursWorked -= 40;
            pay += hoursWorked * hourlyRate * overtimeMultiplier;
        }
        else
        {
            pay += hoursWorked * hourlyRate;
        }
        hoursWorked = 0;
        return pay;
    }
}

