/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package payroll;

/**
 *
 * @author Bhavik
 */
public class HourlyEmployee extends Employee {

    private double hRate;
    private double numberOfHours;
    private double numberOfHoursWorked;

    public double gethRate() {
        return hRate;
    }

    public void sethRate(double hRate) {
        this.hRate = hRate;
    }

    public double getNumberOfHours() {
        return numberOfHours;
    }

    public void setNumberOfHours(double numberOfHours) {
        this.numberOfHours = numberOfHours;
    }

    public double getNumberOfHoursWorked() {
        return numberOfHoursWorked;
    }

    public void setNumberOfHoursWorked(double numberOfHoursWorked) {
        this.numberOfHoursWorked = numberOfHoursWorked;
    }

    public HourlyEmployee(String employeeID, String name, String address, String phone, double hRate, double numberOfHours, double numberOfHoursWorked) {
        setEmployeeID(employeeID);
        setName(name);
        setAddress(address);
        setPhone(phone);
        sethRate(hRate);
        setNumberOfHours(numberOfHours);
        setNumberOfHoursWorked(numberOfHoursWorked);
    }

    public double getMonthlySalary() {

        double getSalary = hRate * numberOfHoursWorked;
        if (getSalary > hRate * numberOfHours) {
            return hRate * numberOfHours;
        } else {
            return hRate * numberOfHoursWorked;
        }
    }

}

