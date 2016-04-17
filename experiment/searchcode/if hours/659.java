/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.concrete;

/**
 *
 * @author Don
 */
public class HourlyEmployee extends Employee {

    private static final double HOURLYWAGE = 9.00;
    private double standardPayHoursWorked;
    private double overTimeHours;
    private double overTimePay;
    private double totalPay;
    private double totalHoursWorked;

    public double getHOURLYWAGE() {
        return HOURLYWAGE;
    }

    public double getStandardPayHoursWorked() {
        return standardPayHoursWorked;
    }

    public void setStandardPayHoursWorked(double standardPayHoursWorked) {
        this.standardPayHoursWorked = standardPayHoursWorked;
    }

    public double getOverTimeHours() {
        return overTimeHours;
    }

    public void setOverTimeHours(double overTimeHours) {
        this.overTimeHours = totalHoursWorked - standardPayHoursWorked;
    }

    public double getOverTimePay() {
        return overTimePay;
    }

    public void setOverTimePay(double overTimePay) {
        this.overTimePay = overTimePay;
    }

    public double getTotalPay() {
        return totalPay;
    }

    public void setTotalPay() {
        this.totalPay = standardPayHoursWorked * HOURLYWAGE + overTimePay;
    }

    public double getTotalHoursWorked() {
        return totalHoursWorked;
    }

    public void setTotalHoursWorked(double totalHoursWorked) {
        this.totalHoursWorked = totalHoursWorked;
    }
    @Override
    public void getEmployeeInformation() {
        System.out.println(HOURLYWAGE + getName() + getIdNumber() + getRegisterLoginID() + getRegisterLoginPassword());
    }
//    public double overTimePay(){
//        if (totalHoursWorked > 40){
//            overTimeHours =  totalHoursWorked - standardPayHoursWorked;
//            
//            System.out.println("You have worked " + overTimeHours + " hours of overtime");
//        }
//        else{
//            standardPayHoursWorked = totalHoursWorked;
//        }
//        return totalPay;
//    }
}

