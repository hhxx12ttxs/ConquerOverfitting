
package main;

import main.Calendar;

public class Employee {
    
    private final int maxWorkingHoursInDay = 8;
    private final int maxWorkingHoursInWeek = 40;
    
    private int idEmployee;
    public String employeePost;
    public String employeePost2;
    public String employeePost3;
    int workingHoursInDay;
    int workingHoursInWeek;
    int allWorkingHours;
    
    boolean isBusy = false;
    
    private Calendar calendar;
    
    Employee(){
        
    }
    
    Employee(int idEmployee, String postName){
        this.employeePost = postName;
        this.idEmployee = idEmployee;
    }
    
    
    Employee(int idEmployee, String postName, String postName2){
        this.idEmployee = idEmployee;
        this.employeePost = postName;
        this. employeePost2 = postName2;
    }
    
    public void setEmployeePost2(String postName){
        this.employeePost2 = postName;
    }
    
    public int getIdEmployee(){
        return this.idEmployee;
    }
    
    public String getIdToString(){
        return Integer.toString(this.idEmployee);
    }
    
    public void generateWorkingSchedule(){ //сгенерировать рабочий график
        workingHoursInWeek = maxWorkingHoursInWeek;
        
        if(calendar.getProcessDate().getDayOfWeek().toString() == "MONDAY"){
            workingHoursInDay = 1 + (int) (Math.random() * maxWorkingHoursInDay);
            workingHoursInWeek = workingHoursInWeek - workingHoursInDay;
        }
        
        if(calendar.getProcessDate().getDayOfWeek().toString() == "TUESDAY"){
            workingHoursInDay = 1 + (int) (Math.random() * maxWorkingHoursInDay);
            workingHoursInWeek = workingHoursInWeek - workingHoursInDay;
        }
        
        if(calendar.getProcessDate().getDayOfWeek().toString() == "WEDNESDAY"){
            workingHoursInDay = 1 + (int) (Math.random() * maxWorkingHoursInDay);
            workingHoursInWeek = workingHoursInWeek - workingHoursInDay;
        }
        
        if(calendar.getProcessDate().getDayOfWeek().toString() == "THURSDAY"){
            workingHoursInDay = 1 + (int) (Math.random() * maxWorkingHoursInDay);
            workingHoursInWeek = workingHoursInWeek - workingHoursInDay;
        }
        
        if(calendar.getProcessDate().getDayOfWeek().toString() == "FRIDAY"){
            workingHoursInDay = 1 + (int) (Math.random() * maxWorkingHoursInDay);
            workingHoursInWeek = workingHoursInWeek - workingHoursInDay;
        }
        
        if(calendar.getProcessDate().getDayOfWeek().toString() == "SATURDAY" && workingHoursInWeek > 0){
            workingHoursInDay = 1 + (int) (Math.random() * maxWorkingHoursInDay);
            workingHoursInWeek = workingHoursInWeek - workingHoursInDay;
        }
        
        if(calendar.getProcessDate().getDayOfWeek().toString() == "SUNDAY" && workingHoursInWeek > 0){
            workingHoursInDay = 1 + (int) (Math.random() * maxWorkingHoursInDay);
            workingHoursInWeek = workingHoursInWeek - workingHoursInDay;
        }
    }
    
}

