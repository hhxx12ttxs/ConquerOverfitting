
package abstract1;


public class HourlyEmployee extends Employee {
    
    private double hourlyWage;
    private double hoursWorked;
    

    public HourlyEmployee(String lName, String fName, String dept, int ID) {
        super(lName, fName, dept, ID);
    }

    public double getHourlyWage() {
        return hourlyWage;
    }

    public void setHourlyWage(double hourlyWage) {
        this.hourlyWage = hourlyWage;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }
    
     
    
    @Override
    public double weeklyPay() {
     double weeklyPay;
        if (hoursWorked<=40){
        weeklyPay = hourlyWage*hoursWorked;
        }
        else{
        weeklyPay = (hourlyWage*40)+((hourlyWage*1.5)*(hoursWorked-40));
        }
    return weeklyPay;   
    }
}

