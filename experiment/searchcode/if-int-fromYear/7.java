public class LeapYear extends GregorianCalendar {

/**
*
*/
private static final long serialVersionUID = 1L;

public void printLeapYears(int fromYear, int toYear) {
for (int i = fromYear; i <= toYear; i++) {
boolean leapYear = isLeapYear(i);

