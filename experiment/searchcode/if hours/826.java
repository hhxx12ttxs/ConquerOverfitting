package myabstract;

/**
 *
 * @author schra_000
 */
public class HourlyEmployee extends Employee {
    
    private double wage;
    private int hoursWorked;
    private final static double OVER_TIME = 1.5;
    private double totalPay;
    
    
    //@Override
    public double getTotalPay() {
        totalPay = 0;
        if (hoursWorked > 40) {
            totalPay = ((hoursWorked - 40) * (OVER_TIME * wage)) + (wage * 40);
        } else {
            totalPay = hoursWorked * wage;
        }
        return totalPay;
    }

    public void setWage(double wage) {
        this.wage = wage;
    }

    public int getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(int hoursWorked) {
        this.hoursWorked = hoursWorked;
    }
    
    
    
    
}
