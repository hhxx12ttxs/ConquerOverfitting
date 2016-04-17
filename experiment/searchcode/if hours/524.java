package Payroll;

public class HourlyEmployee extends Employee {
	double wage ;
	int hours;
	
	public HourlyEmployee(String firstName, String lastName, String ssn,double wage, int hours) {
		super(firstName, lastName, ssn);
		this.wage = wage;
		this.hours = hours;
	}

	public double getWage() {
		return wage;
	}

	public void setWage(double wage) {
		this.wage = wage;
	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public double earnings()
	{
		System.out.println(wage*hours);
		return wage*hours;
	}
	public boolean equals(Object obj) {
		if ( obj instanceof HourlyEmployee ) {
			HourlyEmployee otherEmployee = (HourlyEmployee)obj;
			return super.equals(obj) && this.wage == otherEmployee.wage && this.hours == otherEmployee.hours;
			
		} else {
			return false;
		}
	}
}



/*
Create a Concrete class HourlyEmployee which extends Employee and adds properties 
wage and hours. Provide implementation for earnings method to calculate the earnings 
based on wage and number of hours worked.*/

