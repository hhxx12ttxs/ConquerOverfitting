/* Name : Le Van Hung
 *Class : K55CD
 */

public class HourlyEmployee extends Employee 
{
   private double wage;
   private double hours; 
   
   public HourlyEmployee( String first, String last, String ssn, Date ns,
      double hourlyWage, double hoursWorked ){
      super( first, last, ssn , ns);
      setWage( hourlyWage ); 
      setHours( hoursWorked ); 
   } 
   public void setWage( double hourlyWage ){
      wage = ( hourlyWage < 0.0 ) ? 0.0 : hourlyWage; //if hourlyWage < 0 then wage = 0; else wage = hourlyWage
   } 
   public double getWage(){
      return wage;
   } 
   public void setHours( double hoursWorked ){
      hours = ( ( hoursWorked >= 0.0 ) && ( hoursWorked <= 168.0 ) ) ?
         hoursWorked : 0.0;
   } 
    public double getHours(){
      return hours;
   } 
  public double earnings(){
      if ( getHours() <= 40 )
         return getWage() * getHours() + addbonus();
      else
         return 40 * getWage() + ( getHours() - 40 ) * getWage() * 1.5 + addbonus();
   } 
   public String toString(){
      return String.format( "hourly employee: %s\n%s: $%,.2f; %s: %,.2f", 
         super.toString(), "hourly wage", getWage(), 
         "hours worked", getHours() );
   } 
} 


