
package myabstract;

/**
 *
 * @author Max
 */
public class HourlyEmployee extends Employee {
    
    private double hoursWorked;
    private double hourlyPay;
    
    public HourlyEmployee (String name, String empID, int age) {
        super(name, empID, age);
    }
    
    public void setHoursAndPay (double hoursWorked, double hourlyPay) {
        this.hoursWorked = hoursWorked;
        this.hourlyPay = hourlyPay;
    }
    
    public double getHoursWorked () {
        return hoursWorked;
    }
    
    public double getHourlyPay() {
        return hourlyPay;
    }
    
    public double getPay () {
        if(hoursWorked > 40) {
            return (40 * hourlyPay) + ((hoursWorked - 40) * (hourlyPay * 1.5));
        }
        else return (hoursWorked * hourlyPay);
    }
    
}

