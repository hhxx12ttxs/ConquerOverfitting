public static void main(String[] args) {
int year = 2000;
boolean isLeapYear;

isLeapYear = (year % 4 == 0);
isLeapYear = isLeapYear || (year % 400 == 0);
if (isLeapYear) {
System.out.println(year + &quot; is a leap year.&quot;);

