/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tp2;

/**
 *
 * @author asiron
 */
public class HourlyEmployee extends Employee {
    private double wage;
    private double hours;

    public HourlyEmployee( String first, String last, String ssn, double hours, double wage ) {
        super(first,last,ssn);
        this.hours = hours;
        this.wage  = wage;
    }

    public double getWage() {
        return wage;
    }

    public void setWage(double wage) {
        this.wage = wage;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }
        
    @Override
    public double earnings() {
        if (hours < 40) {
            return hours * wage;
        } else {
            return 40 * wage + (hours - 40) * wage * 1.5;
        }
    }
    
    
}

