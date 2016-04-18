/*
Simple payroll calculator that calculates paycheck given a certain number of hours and hourly pay rate.  
Limitations: Hours must be less than 60, pay must be greater than minimum wage, overtime (hours in excess of 40) must be paid at time and a half.
*/
class Payroll{
	public static void pay( String employeename, double hourlywage, double hours){
		double minimumwage = 8.75;
		double overtimehours = 0;
		double paycheck;

		if (hourlywage < minimumwage) {
			System.out.println("You must pay "+ employeename + " at least the minimum wage of $8.75/hour.");
		} else if (hours > 60.0) {
			System.out.println(employeename+ " cannot work more than 60 hours per week.");
		} else { // Wage is at least minimum wage and hours are under 60 hour cap. 
			if (hours > 40.0){ //Calculate overtime hours 
				overtimehours = hours - 40.0;
				hours = 40.0;
			}
			paycheck = hourlywage * hours + hourlywage*1.5*overtimehours; // OT hours paid at 1.5 times the hours rate. 
			System.out.println("Pay " + employeename + " $" + paycheck +".");
		}
	}

	public static void main(String[] args) {
		pay("Linda", 7.0, 35.0);
		pay("Max", 9, 40.0);
		pay("Jane", 12, 70.0);
	}
}
