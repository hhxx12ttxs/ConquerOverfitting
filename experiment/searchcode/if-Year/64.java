
public class LeapYearTester {

public LeapYearTester ( ) {
// Testing whether year is correctly identified as a leap year
LeapYear myYear = new LeapYear(year);
myYear.determineLeapYear();
if (myYear.isLeapYear()) {
System.out.println (&quot;Fails for &quot; + year + &quot;, which is a leap year&quot;);

