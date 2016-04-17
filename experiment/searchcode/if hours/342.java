package com.npsoftwares.heranca;

public class HourlyEmployee extends Employee{
	
	private double wage; //hourly wage
	private double hours; //hours worked per week
	
	public HourlyEmployee(String firstName,String lastName,String ssn,double wage,double hours){
		
		super(firstName, lastName, ssn);
		setWage(wage);
		setHours(hours);
		
	}
	
	public void setWage(double hourWage) {
		this.wage = (hourWage > 0.0)? hourWage: 0.0;
	}
	
	public void setHours(double hoursWorked) {
		this.hours = (hoursWorked >=0.0 ) && (hoursWorked <= 168.0) ? hoursWorked : 0.0 ;
	}
	
	public double getHours() {
		return hours;
	}
	
	public double getWage() {
		return wage;
	}

	@Override
	public double earnings() {
		
		if(getHours() <= 40){
			return getWage() * getHours();
		}else{
			return 40 * getWage() + (getHours()-40) * getWage() * 1.5;
		}
		
	}
	
	
	public String toString(){
		
		return String.format("hourly Employee : %s\n%s $%.2f; %s: %.2f",
				super.toString(), "hourly wage", getWage(), "hours worked:",
				getHours());
	}

}

