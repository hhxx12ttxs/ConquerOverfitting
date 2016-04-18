package com.company;

/**
 * Created by turk on 3/1/16.
 */
public class netID_HourlyEmployee extends netID_Employee {

    private Double hourlyRate;
    private Double hoursWorked;


    public netID_HourlyEmployee(String firstName, String lastName, Character mI, Character gender, Integer employeeNumber, Boolean fulltime, Double hourlyRate) {
        super(firstName, lastName, mI, gender, employeeNumber, fulltime);
        this.hourlyRate = hourlyRate;
    }


    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Double getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(Double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }


    public void increaseHours(Double hours){
        if(hours < 0){
            System.out.println("Can't increase hours by a negative value. No change.");
        }else{
            setHoursWorked(getHoursWorked() + hours) ;
        }
    }

    public Double calculateWeeklyPay(){

        setHoursWorked(hoursWorked);
        setHourlyRate(hourlyRate);

        Double calculatethe_pay;

        if(hoursWorked >40){
            calculatethe_pay= (hoursWorked -40) * hourlyRate * 2 + 40 * hourlyRate;


        }else{

            calculatethe_pay=  hoursWorked *hourlyRate;
        }

        return  calculatethe_pay;

    }

    public void annualRaise(){

        getHourlyRate();
        setHourlyRate(hourlyRate + hourlyRate*5/100);


    }
    public Double holidayBonus(){

        Double holiday_bonus ;
        getHourlyRate();

        holiday_bonus =hourlyRate * 40;

        return holiday_bonus;


    }
    public  void  resetWeek(){

        setHoursWorked(0.0);
    }

    @Override
    public String toString() {
        return "netID_HourlyEmployee{" +
                "hourlyRate=" + hourlyRate +
                ", hoursWorked=" + hoursWorked +
                '}';
    }
}

