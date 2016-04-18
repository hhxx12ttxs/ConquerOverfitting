//HourlyEmployee2.java
//HourlyEmployee2 concrete class extends abstract class Employee which implements Payable interface.

public class HourlyEmployee2 extends Employee2
{
	private double wage;
	private double hours;

	public HourlyEmployee2 ( String first, String last, String ssn, double hourlyWage, double hoursWorked ) {
		super( first, last, ssn );
		setWage ( hourlyWage );
		setHours ( hoursWorked );
	}

	public void setWage ( double hourlyWage ) {
		if ( wage >= 0.0 ) 
			wage = hourlyWage;
		else 
			throw new IllegalArgumentException( " Hourly wage must be >= 0.0 ");
	}

	public double getWage( ) {
		return wage;
	}

	public void setHours ( double hoursWorked ) {
		if ( hours >= 0.0 && hours <= 168.0 ) 
			hours = hoursWorked;
		else 
			throw new IllegalArgumentException( " Hours worked must be >= 0.0 ");
	}

	public double getHours( ) {
		return hours;
	}

	@Override 
	public double getPaymentAmount ( ) {
		if ( getHours ( ) <= 40 ) 
			return getWage( )*getHours( );
		else
			return 40*getWage( )*( getHours( ) - 40 )*getWage ( )*1.5;
	}

	@Override
	public String toString( ) {
		return String.format( "salaried employee : %s\n%s : $%,.2f;  %s : $%,.2f", super.toString( ),
			"hourly wage",getWage( ),
			"hours worked", getHours( ) );
	}
}
